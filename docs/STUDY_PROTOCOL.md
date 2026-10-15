# Study protocol

These study details describe the collection setup for the [associated research](../README.md#associated-research). Publication details are pending. The protocol is preserved from the original repository documentation; the app records trial numbers and does not enforce walking conditions.

## Study configuration

The accompanying study used the following collection setup:

| Item | Configuration |
|---|---|
| Smartwatch | Samsung Galaxy Watch Ultra |
| Placement | Participant's dominant wrist |
| Sensors | Tri-axial accelerometer and gyroscope |
| Requested sampling rate | 25 Hz |
| Environment | Indoor hallway, approximately 40 m long |
| Controlled factors | Walking speed, step length, arm swing arc, and arm rotation |
| Conditions per participant | 12 |
| Participants | 35 |
| Dictionary entries | 420 |
| Data reported per dictionary entry | Approximately 80 seconds |

The application supports configurable recording durations and nominal 25 and 50 Hz options and a device-dependent Fastest option. Its software defaults are 120 seconds and 25 Hz. The study used the 25 Hz option; Android treats requested sampling intervals as hints rather than guarantees.

## Controlled walking conditions

Participants completed a normal trial followed by two modified trials for each of four gait factors. The trial-number convention used during collection was:

| Trial | Factor | Condition |
|---|---|---|
| T1 | Walking speed | Normal |
| T2 | Walking speed | Slow |
| T3 | Walking speed | Fast |
| T4 | Step length | Normal |
| T5 | Step length | Short |
| T6 | Step length | Long |
| T7 | Arm swing arc | Normal |
| T8 | Arm swing arc | Low |
| T9 | Arm swing arc | High |
| T10 | Arm rotation | Normal |
| T11 | Arm rotation | Inward |
| T12 | Arm rotation | Outward |

The operator assigns and explains each condition to the participant. The application records the trial number but does not independently enforce or recognize the walking condition.

## Research ethics and data availability

The study's dictionary dataset was collected from 35 participants under an approved university Institutional Review Board protocol. This source-code repository does not contain participant data.

Exported records contain demographic metadata and wall-clock timestamps. Treat them as controlled research data, use pseudonymous identifiers, and follow the applicable consent, ethics, and data-management requirements before sharing them.
