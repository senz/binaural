# Binaural Beats

## Credits

This app uses the core of the "Binaural" application, created by Russell Dobda in 2016

Some background on the algorithms can be found here
http://www.gunkyfunky.com/blog/open-source-binaural-beats

---

## Development

This repo includes a **Dev Container** for building and developing the app with a consistent environment (JDK 17, Android SDK, Gradle). The container is set up for the **target stack after migration: Kotlin and Jetpack Compose**. The current project is still Java + XML; it may not build inside the container until the Gradle/AGP and dependencies are updated.

- **Open in Dev Container:** In VS Code or Cursor, install the "Dev Containers" extension, then use **Command Palette → "Dev Containers: Reopen in Container"** (or "Open Folder in Container" when opening the project). The first build may take a few minutes to pull the image and install the SDK.
- For more details see [.devcontainer/README.md](.devcontainer/README.md).

## Tests

- **Unit tests** (JVM): `./gradlew test`
- **Instrumented tests** (device/emulator): `./gradlew connectedDebugAndroidTest`

### Pre-commit и тесты при push

Установка (один раз):

```bash
pip install pre-commit
pre-commit install
pre-commit install --hook-type pre-push
```

Коммит: проверки ktlint и detekt. Push: перед отправкой запускаются юнит-тесты (см. `.pre-commit-config.yaml`). Пуш без проверок: `git push --no-verify`.
