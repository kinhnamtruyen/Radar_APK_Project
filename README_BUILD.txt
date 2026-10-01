RADAR - Android Project
========================
This is a clean Android Studio/Gradle project reconstructed from the supplied source.

Build:
  ./gradlew assembleRelease
or open the folder in Android Studio and Build > Generate App Bundles / APKs > Generate APKs.

Output:
  app/build/outputs/apk/release/app-release.apk

Main features:
  - Radar animation
  - Wi-Fi scan results
  - Bluetooth discovery results
  - Runtime permissions for modern Android

Important:
  - Wi-Fi scanning on Android may require Location Services to be enabled.
  - Android 12+ requires BLUETOOTH_SCAN / BLUETOOTH_CONNECT permissions.
  - The generated project uses minSdk 21 and targetSdk 35.
