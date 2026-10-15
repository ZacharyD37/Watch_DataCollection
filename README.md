# Watch Data Collection

A standalone **Wear OS app for recording smartwatch accelerometer and gyroscope data during gait research**. Operators enter participant and trial settings, record a timed walking trial, and export both sensor streams to CSV for retrieval with ADB.

This repository contains the collection app's source code. Participant data, preprocessing, authentication models, and attack evaluation are outside its scope. See the [study protocol](docs/STUDY_PROTOCOL.md) and [CSV format](docs/DATA_FORMAT.md) for the collection workflow.

## Associated research

Zi Wang and Rajesh Kumar. “Evaluating Smartwatch-Based Gait Authentication Under Dictionary Attacks.” *2026 IEEE International Joint Conference on Biometrics (IJCB)*, Rome, Italy, 2026.

Zi Wang is the first author and oral presenter. **Publication details are pending; a published paper link and DOI are not yet available.** This conference reference will be updated when those details are available. A provisional BibTeX reference is provided [below](#reference).

## Build and install

**Building and installing the app has been tested on Windows.** The instructions below use Windows PowerShell. The target device must be a **Wear OS watch**; Apple Watch is not supported.

You need **JDK 17**, Android SDK **Platform 35**, **Build-Tools 35.0.0**, **Platform-Tools** (which includes ADB), and a Wear OS watch with an accelerometer and gyroscope. The minimum device Android API is **30**. Use Android Studio compatible with [Android Gradle Plugin 8.11.2](https://developer.android.com/build/releases/agp-8-11-0-release-notes), or install the Android command-line tools and these SDK packages. The Gradle wrapper pins Gradle 8.13; you do not need a separate Gradle installation.

1. Clone the repository and enter its directory:

   ```powershell
   git clone https://github.com/ZacharyD37/Watch_DataCollection.git
   cd Watch_DataCollection
   ```

2. Open the cloned repository directory in Android Studio, choose JDK 17 as the Gradle JDK, and use SDK Manager to install the SDK packages listed above. For a command-line build, also set `JAVA_HOME` to your JDK 17 installation; the Gradle JDK setting in Android Studio does not configure PowerShell. Android Studio normally creates `local.properties`. If configuring it manually, set `sdk.dir` to your SDK directory using forward slashes, for example `sdk.dir=C:/Users/YOUR_USERNAME/AppData/Local/Android/Sdk`. Keep this machine-specific file untracked.

   Make ADB available in the current PowerShell session. For Android Studio's default Windows SDK location, run:

   ```powershell
   $env:Path = "$env:LOCALAPPDATA\Android\Sdk\platform-tools;$env:Path"
   adb version
   ```

   If your SDK is elsewhere, replace the path with the SDK Manager location followed by `\platform-tools`.

3. Build and check the app:

   ```powershell
   java -version
   .\gradlew.bat --version
   .\gradlew.bat :app:assembleDebug :app:lintDebug
   ```

   Run these commands from the repository directory. The first build downloads dependencies and requires internet access. JDK 17 runs the build; the app's Java/Kotlin bytecode target is 11.

4. Enable developer options and ADB debugging on the watch. For a watch with **Wireless debugging** and **Pair new device**, connect the computer and watch to the same Wi-Fi network, enable Wireless debugging, and allow debugging on this network. Replace the placeholders with the values shown on the watch:

   ```powershell
   adb pair WATCH_IP:PAIRING_PORT
   adb connect WATCH_IP:CONNECTION_PORT
   adb devices
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

   Enter the pairing code when `adb pair` prompts for it. The pairing port comes from **Pair new device**; the connection port comes from the main **Wireless debugging** screen and is usually different. Confirm the watch appears with status `device` before installing. See [Android's Wear OS Wi-Fi debugging guide](https://developer.android.com/training/wearables/get-started/debug-wifi) for troubleshooting. Older watches without the pairing menu need the debugging method supported by their OS; use USB debugging if the watch supports a USB data connection.

   With multiple devices attached, add `-s WATCH_SERIAL` after `adb` for installation and retrieval, replacing `WATCH_SERIAL` with the watch's entry from `adb devices`. Launch **Gait** on the watch. Alternatively, select the connected watch and the **app** run configuration in Android Studio and click **Run** to build, install, and launch it. A companion phone app is not required.

If the build reports a Java version error, check both `java -version` and `.\gradlew.bat --version`. If it cannot locate the Android SDK, check `local.properties` or Android Studio's SDK settings. An emulator can help inspect the UI, but real sensor collection must be checked on the target watch.

## Record a trial

1. Tap **Start Sensors** and enter a pseudonymous **User ID**, **Trial**, gender, age, dominant hand, and timer duration. Tap **Continue**.
2. Select the sampling option. The default is **25 Hz** and the default duration is **120 seconds**.
3. Tap **Start**. A three-second countdown and vibration cues precede recording; the app attempts to start at the next whole wall-clock second. The start vibration cues walking.
4. Check that both sample counts increase. The timer stops collection automatically; **Stop** ends it early and retains the readings.
5. Tap **Export** after collection stops. Check the success message before leaving the screen or starting another trial.
6. Retrieve the file, increment the trial number, and repeat as required by your protocol.

**Export refuses to overwrite an existing file.** Readings are buffered in memory until export. Starting another trial is blocked while unexported readings remain. **Clear** explicitly discards them. **Reset** stops collection and resets the timer while retaining readings. Closing the app or process termination can still lose unexported data; keep the app visible throughout collection.

The app prevents changes to the sampling option while a countdown is active or readings remain buffered, and prevents export/clear during recording. After clearing the buffer or starting the next trial, the sampling controls become available again. Participant settings persist locally between launches; the sampling option resets to 25 Hz when the recording screen is reopened.

## Sampling and output

| Option | Android request | Interpretation |
|---|---|---|
| 25 Hz (default) | 40,000 microseconds | Nominal 25 Hz |
| 50 Hz | `SENSOR_DELAY_GAME` | Nominal 50 Hz |
| Fastest | `SENSOR_DELAY_FASTEST` | Device-dependent maximum; previously labeled 100 Hz |

Android treats sampling delays as requests. Effective rates can differ between devices and between sensors; **Fastest does not mean 100 Hz**. See [Android's sensor documentation](https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview) for platform limits.

Exports are stored at:

```text
/sdcard/Android/data/test.gait/files/U<UserID>_T<TrialNumber>.csv
```

Retrieve them with:

```powershell
adb pull /sdcard/Android/data/test.gait/files/ ./collected_data/
```

Each file has `#` metadata lines followed by:

```csv
sensor_type,x,y,z,timestamp_ms
```

Accelerometer values include gravity and use m/s²; gyroscope values use rad/s. Timestamps are Unix epoch milliseconds measured when callbacks arrive. Rows are grouped by sensor, with accelerometer rows first. See [the complete data format](docs/DATA_FORMAT.md) for metadata, an example, parsing guidance, and compatibility details.

## Research scope and limitations

- The study collection device was the **Samsung Galaxy Watch Ultra**. Validate the current revision on the target watch using the [on-watch checks](docs/VALIDATION.md#on-watch-acceptance-checks); missing or failed sensor registration blocks recording.
- Sensor streams are neither hardware synchronized nor resampled. Callback timestamps, coroutine timer scheduling, and whole-second alignment do not provide laboratory-grade timing.
- The app performs no gait-condition recognition, filtering, calibration, segmentation, or authentication.
- There is no background recording service or durable recording buffer. Keep the recording screen visible and verify exports after every trial.
- Exported demographics and timestamps are research data. Use pseudonymous identifiers and follow the applicable consent and ethics protocol. The manifest disables Android backup; the app requests no network permission.

The manifest requests `WAKE_LOCK` and `VIBRATE` for collection and cues. It retains `HIGH_SAMPLING_RATE_SENSORS` because Fastest may exceed 200 Hz. No physiological sensors are read. The CPU wake lock is acquired when the activity resumes, with an 11-minute timeout, and released when the activity pauses. The timeout runs from activity resume rather than trial start; keep the app visible and verify collection on the target watch.

## Repository guide

| Path | Purpose |
|---|---|
| `app/src/main/java/test/gait/presentation/` | Activity, participant settings, sensor UI, and CSV export |
| `app/src/main/res/` | App strings, theme, and launcher assets |
| `gradle/libs.versions.toml` | Dependency and plugin versions |
| `gradle/wrapper/`, `gradlew`, `gradlew.bat` | Reproducible Gradle entry points; keep these files |
| [docs/STUDY_PROTOCOL.md](docs/STUDY_PROTOCOL.md) | Study setup and trial mapping |
| [docs/DATA_FORMAT.md](docs/DATA_FORMAT.md) | CSV schema and timing semantics |
| [docs/VALIDATION.md](docs/VALIDATION.md) | Build and on-watch checks before release |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Reporting problems and preparing changes |
| [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) | Notices for the bundled Gradle wrapper and Android robot artwork |

Build directories, IDE settings, local SDK paths, and retrieved participant records are ignored by Git. Do not commit participant exports or signed APK credentials.

## Reference

Until a published reference is available, use the following provisional entry. Update it with the final proceedings details and DOI when available:

```bibtex
@unpublished{wang2026evaluating,
  author    = {Zi Wang and Rajesh Kumar},
  title     = {Evaluating Smartwatch-Based Gait Authentication Under Dictionary Attacks},
  year      = {2026},
  note      = {2026 IEEE International Joint Conference on Biometrics (IJCB), Rome, Italy. Publication details pending}
}
```

For reproducibility, report the public repository commit or release tag alongside the reference and record the watch model, OS version, sampling option, measured rates, and collection protocol. This public snapshot includes maintenance changes after the original collection and should not be treated as an exact archive of the software used for that collection.

## License

No software license is provided for the project's original application code at this time. Bundled third-party components retain their own licenses; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
