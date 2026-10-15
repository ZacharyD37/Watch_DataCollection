# Validation and release checks

## Automated build

With JDK 17 and the Android SDK configured as described in the [README](../README.md#build-and-install), run these commands from the repository directory in Windows PowerShell:

```powershell
.\gradlew.bat --version
.\gradlew.bat :app:assembleDebug :app:lintDebug
git diff --check
```

The [GitHub Actions workflow](../.github/workflows/android.yml) is configured to build and lint on Windows with JDK 17 on pushes, pull requests, and manual runs. Check the actual run result before release. Lint reports, when generated, are retained as the `lint-reports` artifact for 14 days. Locally, the HTML report is `app/build/reports/lint-results-debug.html` and the debug APK is `app/build/outputs/apk/debug/app-debug.apk`.

A successful build does not validate sensor behavior or the study protocol. The Gradle distribution is pinned to 8.13 with the SHA-256 checksum from [Gradle's checksum reference](https://gradle.org/release-checksums/).

## On-watch acceptance checks

Use synthetic participant settings for testing. Record the device model, Wear OS/Android version, repository commit, and APK used.

1. Install the debug APK. Verify settings survive reopening the app and the UI displays **Trial**.
2. Record a 30-second trial at 25 Hz. Verify the countdown, start/end cues, timer, and nonzero sample counts from both sensors.
3. During countdown and recording, verify sampling changes, export, and clear are disabled. Stop during countdown and confirm recording does not start later.
4. Stop early and reset. Confirm buffered readings remain, and Back/Start do not discard unexported readings. Clear explicitly and confirm a new trial can start.
5. Export, pull the CSV, and verify metadata, both sensor types, numeric values, and that the row count equals `Total Readings`. The `Trail #` metadata spelling is intentional.
6. Reopen the same participant/trial and record again. Confirm export reports the existing filename and the original file's contents remain unchanged. Verify readings remain buffered after the failure. Retrieve and verify the original file, move it to an archive path on the watch, then retry export and confirm that the new recording is saved.
7. Use a new trial number and repeat for 50 Hz and Fastest. Estimate delivered rates separately from each sensor's timestamp differences; record duplicates, gaps, and clock discontinuities.
8. Record a maximum-duration trial (600 seconds), keeping the app visible. Check completion and successful export. Test battery and thermal behavior under your study conditions.
9. Verify missing or unavailable sensors cancel recording with an error, where a suitable device or test environment is available.

## Before making the repository public

- Review the README's authors, title, conference, location, year, and presentation information. Keep the reference marked provisional until the published paper details are available.
- Review the study setup and dataset descriptions, including the relation between the 120-second app default and approximately 80 seconds reported per dictionary entry.
- Confirm the README accurately states the license status. The current decision is to make the source publicly viewable without providing a software license.
- Review the exact files selected for the public snapshot for participant data, credentials, signing keys, and local configuration. Copy reviewed source files only; exclude `.git/`, `.gradle/`, `.kotlin/`, `.idea/`, build directories, `local.properties`, and recordings. Keep the development history in the private development repository.
- Run the build/lint checks and relevant device checks above, recording results and known failures.

## Before tagging a research release

- Tag the exact validated commit. Distinguish it from the original data-collection revision if behavior has changed since collection.
- Record the watch model, OS version, sampling option, measured rates, and collection protocol alongside the release.
- When the published paper becomes available, update the reference and add its DOI or stable link. Archive the tagged source with a persistent identifier if required by the venue; document dataset access separately from software access.

## Repository review status

Review date: **2026-10-15**. These results apply to the reviewed source snapshot, not to the original study collection revision.

- Local Markdown links and anchors, Markdown code fences, Android XML parsing, and `git diff --check` passed.
- `:app:assembleDebug` and `:app:lintDebug` passed locally on Windows using Gradle 8.13 and the installed JDK 21.0.2. The debug APK was generated. The Windows workflow also passed with JDK 17 in the development repository after correcting SDK setup and updating the workflow actions. Run the workflow again in the new public repository; its Actions history starts separately.
- Lint reported **0 errors, 20 warnings, and 7 hints**. The remaining findings include dependency update notices, high sampling rate permission usage, backup configuration guidance, wake-lock analysis, obsolete SDK checks, an unused launcher resource, and Kotlin API suggestions. Review the full report before changing collection behavior or dependency versions.
- The initial build stopped on a Java certificate validation error when downloading Google Maven dependencies. The successful retry used `-Djavax.net.ssl.trustStoreType=Windows-ROOT` and `-Djavax.net.ssl.trustStore=NONE` for that command only, using the Windows trusted certificate store. These local options are not required by the project's build configuration.
- Ignore rules were checked for local SDK configuration, environment files, signing keys, and recording output paths.
- A pattern-based review of the selected public files found no common credential patterns, personal absolute paths, email addresses, or IP addresses. No participant exports, signing keys, environment files, or `local.properties` are included in the selected files. This check is limited to the reviewed files and patterns; it does not certify the research claims or rights to the original application code.
- The maintainer reports testing on Windows and confirmed that the app launches on the Wear OS emulator. Full physical-watch collection and export acceptance checks were not performed during this review; run and record the checks above before a research release.
- Bundled Gradle wrapper files and Android robot assets are identified in [the third-party notices](../THIRD_PARTY_NOTICES.md). Their licenses remain separate from the license status of the original application code.
- Links to the private development history and unavailable evaluation repository are excluded from the public-facing documentation.

No software license is provided for the original application code at this time. Publication metadata remains provisional until a published reference is available.
