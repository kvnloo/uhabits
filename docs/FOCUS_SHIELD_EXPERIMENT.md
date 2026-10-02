# Focus-shield experiment v0

Status: downstream experiment. **Not proposed as a Loop feature yet.**

## Question

Does delaying a nonurgent habit reminder during an active focus block reduce interruption/fragmentation without meaningfully increasing missed habits?

This branch tests the intervention independently from the focus detector. The first experiment uses an explicit short-lived debug lease so we can determine whether reminder shielding is useful before building production ActivityWatch integration.

## Safety / behavior contract

When the lease is active and a reminder becomes due:

- the habit is **not** marked complete;
- the reminder is **not** deleted;
- other habits are **not** rescheduled;
- the due reminder is scheduled again at the lease expiry;
- any currently displayed notification for that habit is removed;
- the experiment records only the original reminder timestamp and deferred-until timestamp.

It does not store habit IDs, names, notes, or checkmark values in the experiment log.

Release builds do not contain the debug broadcast receiver used to control the lease.

## Build

Build/install the debug APK using the normal Loop development instructions.

The application id remains `org.isoron.uhabits`.

## Debug controls

Set a 25-minute lease:

```sh
adb shell am broadcast \
  -n org.isoron.uhabits/.experiments.FlowShieldExperimentReceiver \
  -a org.isoron.uhabits.EXPERIMENT_SET_FOCUS_SHIELD \
  --ei minutes 25
```

Clear the lease:

```sh
adb shell am broadcast \
  -n org.isoron.uhabits/.experiments.FlowShieldExperimentReceiver \
  -a org.isoron.uhabits.EXPERIMENT_CLEAR_FOCUS_SHIELD
```

Print the content-free experiment counters/timestamp pairs to logcat:

```sh
adb shell am broadcast \
  -n org.isoron.uhabits/.experiments.FlowShieldExperimentReceiver \
  -a org.isoron.uhabits.EXPERIMENT_REPORT_FOCUS_SHIELD

adb logcat -d -s FlowShieldExperiment:I
```

Reset experiment metrics:

```sh
adb shell am broadcast \
  -n org.isoron.uhabits/.experiments.FlowShieldExperimentReceiver \
  -a org.isoron.uhabits.EXPERIMENT_RESET_FOCUS_SHIELD_METRICS
```

The report's `records` field is a flat sequence of 4-value records:

```text
observedEpochMillis,reminderTime,deferredUntil,shieldedFlag,...
```

- `observedEpochMillis`: real UTC epoch milliseconds; use this to join against ActivityWatch.
- `reminderTime`: Loop's internal scheduled-reminder time.
- `deferredUntil`: `0` for control reminders, otherwise the persisted shield expiry.
- `shieldedFlag`: `0` for an eligible reminder shown normally, `1` for an eligible reminder deferred.

Only reminders that pass Loop's normal completion/archive/day eligibility checks are recorded. Up to the newest 200 eligible reminder events are retained.

## Phase 1: intervention-only validation

Do **not** automate ActivityWatch detection yet.

Run shielded and unshielded work sessions while ActivityWatch records normal desktop telemetry. Use the ActivityWatch downstream flow-proxy branch to calculate:

- context switches / active hour;
- fragmentation;
- sustained-context time;
- return-to-context latency.

For each eligible Loop reminder, compare ActivityWatch behavior around `observedEpochMillis`, which is recorded in both control and shield conditions.

### Suggested within-person design

Run enough ordinary sessions to collect at least several reminder events in each condition:

- **control**: no lease; reminder appears normally;
- **shield**: set a lease covering the expected reminder time.

Avoid changing the habit, reminder text, notification sound, or task type between conditions.

Primary comparison window:

- 10 minutes before the original reminder;
- 30 minutes after the original reminder.

Candidate outcomes:

1. probability of a context switch within 2 minutes of the original reminder;
2. number of switches in the following 10 minutes;
3. return-to-prior-context latency;
4. whether a sustained context block survives the reminder boundary.

Secondary outcome:

- reminder delay introduced by shielding.

Later we should add habit-completion latency/missed-reminder outcomes before making any product claim.

## Phase 2: ActivityWatch-driven lease

Only if Phase 1 shows that shielding helps.

The intended cross-device architecture is:

```text
desktop ActivityWatch telemetry
        |
        v
local focus-proxy computation
        |
        v
content-free expiring lease
        |
      aw-sync
        |
        v
Android ActivityWatch
        |
        v
Loop reminder gate
```

`aw-sync` syncs buckets to Android by default, so a future version can carry an expiring lease as an ActivityWatch bucket. Settings are deliberately not suitable because aw-sync does not sync settings.

The lease should contain no app/title/URL information. A minimal event is sufficient:

```json
{
  "status": "protected",
  "until": "2026-10-01T20:30:00Z",
  "version": 0
}
```

Loop should consume only the lease state. It should not need access to the user's ActivityWatch window history.

## Falsifiers

Do not continue toward an automatic feature if:

- reminders rarely cause measurable interruption in the first place;
- shielding does not improve fragmentation/resumption measures;
- habit adherence meaningfully worsens;
- users find delayed reminders less predictable or more annoying;
- useful shielding requires invasive content inference.

The purpose of v0 is to learn whether the intervention has value before optimizing the detector.
