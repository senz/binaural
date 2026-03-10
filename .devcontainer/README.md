# Dev Container — Android (Kotlin + Jetpack Compose)

This dev container provides a reproducible environment for developing and building the Binaural Beats Android app (Kotlin, Jetpack Compose, Gradle 9–ready).

## What's inside

- **Ubuntu 22.04**
- **JDK 17**
- **Android SDK (command-line tools 17+)**
  - Platform API 34 & 36, build-tools 34.0.0, platform-tools (adb, etc.)
  - Command-line tools are upgraded in the image so SDK XML v4 is supported (no “understands SDK XML versions up to 3” warning).
- **Gradle** via the project’s Gradle Wrapper (`./gradlew`).
- **Pre-commit** (Python 3): installed for the dev user; hook is registered automatically after the container is created.

## How to use

1. Open the project in VS Code or Cursor.
2. Install the **Dev Containers** extension if needed.
3. Run **“Dev Containers: Reopen in Container”** from the Command Palette.
4. Wait for the container to build and start (first time may take several minutes).
5. In the integrated terminal, run `./gradlew assembleDebug` to build the app.

## Pre-commit

After reopening in the container, `pre-commit install` is run once, so every `git commit` will run the hooks (trailing whitespace, YAML/JSON checks, ktlint, detekt). To run all hooks manually: `pre-commit run --all-files`. To auto-format Kotlin: `./gradlew ktlintFormat`.
