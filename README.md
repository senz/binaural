# Binaural Beats Example App
For Android
Created by Russell Eric Dobda
Guided Meditation Treks

This is the core of the "Binaural" application, created by Russell Dobda in 2016
That was my first ever android app, and it generates sine waves to be used
for binaural beats.

This sample app is a cut-down version of that, 
which includes the core engine to create binaural beats.

Some background on the algorithms can be found here
http://www.gunkyfunky.com/blog/open-source-binaural-beats

The production app can be found on the Google Play store at
https://play.google.com/store/apps/details?id=com.guidedmeditationtreks.binaural

There is also an iOS app with the same name, which does the same thing
https://itunes.apple.com/us/app/binaural/id958442825?ls=1&mt=8

If you use any of these algorithms in your own binaural beats app, it would be cool to get a shout out!

---

## Development

This repo includes a **Dev Container** for building and developing the app with a consistent environment (JDK 17, Android SDK, Gradle). The container is set up for the **target stack after migration: Kotlin and Jetpack Compose**. The current project is still Java + XML; it may not build inside the container until the Gradle/AGP and dependencies are updated.

- **Open in Dev Container:** In VS Code or Cursor, install the "Dev Containers" extension, then use **Command Palette → "Dev Containers: Reopen in Container"** (or "Open Folder in Container" when opening the project). The first build may take a few minutes to pull the image and install the SDK.
- For more details see [.devcontainer/README.md](.devcontainer/README.md).
