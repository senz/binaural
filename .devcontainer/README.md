# Dev Container — Android (Kotlin + Jetpack Compose)

This dev container provides a reproducible environment for developing and building the Binaural Beats Android app. It is tuned for the **target stack** after migration to **Kotlin** and **Jetpack Compose**.

## What's inside

- **Ubuntu 22.04**
- **JDK 17**
- **Android SDK (command-line tools)**  
  - Platform API 34, build-tools 34.0.0, platform-tools (adb, etc.)
- **Gradle** via the project’s Gradle Wrapper (`./gradlew`)

## How to use

1. Open the project in VS Code or Cursor.
2. Install the **Dev Containers** extension if needed.
3. Run **“Dev Containers: Reopen in Container”** from the Command Palette.
4. Wait for the container to build and start (first time may take several minutes).
5. Use the integrated terminal to run `./gradlew assembleDebug` (after the project is migrated to a Gradle/AGP version compatible with JDK 17 and the SDK in the container).

## Note about the current project

The app is currently **Java + XML layouts** and uses an old Android Gradle Plugin and SDK. This container uses **JDK 17 and Android SDK 34**, so the existing project may not build as-is. The environment is intended for:

- Developing after migrating to Kotlin and Jetpack Compose, and
- Running builds once Gradle, AGP, and dependencies have been updated.

For building the legacy project unchanged, use a local environment with JDK 8 and the older SDK, or update the project’s build files first.
