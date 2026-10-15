# Exported data

## Output files

Files are written to the application's external files directory:

```text
/sdcard/Android/data/test.gait/files/U<UserID>_T<TrialNumber>.csv
```

For example, participant 7 and trial 3 produce `U7_T3.csv`.

The filename contains only the User ID and trial number. Export refuses an existing filename. If a filename is already present, retrieve and verify the existing file, then move it to an archive path on the watch before retrying export. Never delete a valid trial just to make an export succeed. For subsequent recordings, assign a new trial number. Export is available only after recording stops. Readings stay in memory if export fails so you can retry after resolving the storage problem.

## CSV format

Each file begins with `#` metadata lines, followed by the CSV header and sensor readings. This synthetic excerpt shows one row from each stream; a complete file contains the number of rows given by `Total Readings`:

```text
# User ID: 7
# Trail #: 3
# Gender: Male
# Age: 25
# Dominant Hand: Right
# Timer Duration: 120s
# Export Time: 20250413_142233
# Sampling Rate: 25Hz
# Total Readings: 6012
# Recording Start Time: 1744554153000
# Start Time Alignment: 0ms into second
sensor_type,x,y,z,timestamp_ms
accel,-0.317,9.664,1.204,1744554153001
gyro,0.0021,-0.0043,0.0011,1744554153003
```

| Column | Description |
|---|---|
| `sensor_type` | `accel` or `gyro` |
| `x`, `y`, `z` | Accelerometer axes in m/s^2, including gravity, or gyroscope axes in rad/s |
| `timestamp_ms` | Unix epoch time in milliseconds when the sensor event was delivered to the application |

Accelerometer rows are written first, followed by gyroscope rows; the two streams are not interleaved in the file. Split by `sensor_type` and sort by `timestamp_ms` if a merged delivery-time order is required.

Programs such as pandas should skip or separately parse the metadata lines:

```python
import pandas as pd

data = pd.read_csv("U7_T3.csv", comment="#")
```

## Retrieving files with ADB

Connect the watch using the [Windows installation instructions](../README.md#build-and-install), export the recording, and run this command from the repository directory in PowerShell:

```powershell
adb pull /sdcard/Android/data/test.gait/files/ ./collected_data/
```

## Timing and compatibility

`timestamp_ms` is callback-delivery wall-clock time, not `SensorEvent.timestamp`. It may reflect callback latency or clock changes. Estimate each stream's delivered rate separately, and do not assume paired or synchronized samples. Axis values use the Android device sensor coordinate frame; the app does not rotate them into a body or world frame.

The metadata key `Trail #` retains the original spelling for compatibility. The interface now displays **Trial**. The old `100Hz` metadata label requested `SENSOR_DELAY_FASTEST`; current exports label the same request `Fastest`. The CSV columns, row grouping, filename convention, and timestamp semantics remain unchanged. `Export Time` uses the watch's local timezone; `timestamp_ms` is Unix epoch time.

The example above is illustrative, not participant data. No dataset or analysis software is included here.
