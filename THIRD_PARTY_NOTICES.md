# Third-party notices

These notices apply to the components identified below. They do not grant a software license for this project's original application code.

## Gradle Wrapper

`gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` are Gradle Wrapper components from the [Gradle project](https://github.com/gradle/gradle), distributed under the [Apache License, Version 2.0](third_party/Apache-2.0.txt). Copyright and license notices in the startup scripts are preserved.

The wrapper downloads Gradle 8.13. The downloaded distribution includes its own license and notice files; it is not bundled in this source snapshot.

## Android robot artwork

The Android robot artwork was created and shared by Google. The launcher and splash assets reproduce or adapt that artwork under the [Creative Commons Attribution 3.0 Unported License](https://creativecommons.org/licenses/by/3.0/). See [Google's Android robot guidelines](https://developer.android.com/distribute/marketing-tools/brand-guidelines#android-robot).

Affected files are `app/src/main/res/drawable/ic_launcher_foreground.xml`, `app/src/main/res/drawable/splash_icon.xml`, and the `ic_launcher.webp` and `ic_launcher_round.webp` files in the density-specific `mipmap-*` directories. Google retains trademark rights to the Android robot. Android is a trademark of Google LLC.

## Downloaded dependencies

The Android and Kotlin libraries declared in `app/build.gradle` and `gradle/libs.versions.toml` are downloaded by Gradle. Their source and binary distributions retain their upstream licenses. This source snapshot does not include dependency caches or compiled APKs.
