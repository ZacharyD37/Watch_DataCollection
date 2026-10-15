# Contributing

This repository focuses on the Wear OS sensor collection app. The [README](README.md) covers Windows setup and use; [validation](docs/VALIDATION.md) covers build and watch checks.

## Reporting problems

Include the repository commit, Windows version, JDK version, watch model and OS version, steps to reproduce, and expected and actual behavior. For build problems, include the failing task and relevant error text. For collection problems, include the sampling option and trial duration.

Use synthetic participant settings in examples. Remove participant information, local usernames, credentials, and device identifiers from logs or screenshots before sharing them. Do not attach participant exports to public issues.

## Preparing changes

Keep changes focused and describe the resulting behavior. Run the following from the repository directory in PowerShell with the SDK and JDK configured as described in the README:

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
git diff --check
```

For changes to recording, timing, export, navigation, or device lifecycle behavior, also perform the relevant [on-watch checks](docs/VALIDATION.md#on-watch-acceptance-checks). Report which checks ran and any that remain untested.

Preserve the CSV format and metadata semantics where possible. Document any format or timing changes in [DATA_FORMAT.md](docs/DATA_FORMAT.md), and distinguish the current app behavior from the original study collection setup.

Keep participant records, SDK paths, generated build outputs, and signing credentials out of commits. See the [README](README.md#license) for the current license status.
