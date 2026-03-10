# Devcontainer for Android development (Kotlin + Jetpack Compose)
# Ubuntu 22.04, JDK 17, Android SDK command-line tools
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_HOME=/opt/android-sdk
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# System packages: JDK 17, tools for SDK install, git (adb comes from SDK platform-tools)
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    unzip \
    wget \
    git \
    && rm -rf /var/lib/apt/lists/*

# Android command-line tools: download and install to $ANDROID_HOME/cmdline-tools/latest
ARG CMDLINE_TOOLS_URL=https://dl.google.com/android/repository/commandlinetools-linux-14742923_latest.zip
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools/latest \
    && wget -q -O /tmp/cmdline-tools.zip "${CMDLINE_TOOLS_URL}" \
    && unzip -q /tmp/cmdline-tools.zip -d /tmp \
    && mv /tmp/cmdline-tools/* ${ANDROID_HOME}/cmdline-tools/latest/ \
    && rm -rf /tmp/cmdline-tools.zip /tmp/cmdline-tools

# Install SDK components: platform-tools, platform android-34, build-tools 34.0.0
RUN yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_HOME} \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    || true

# Accept remaining licenses (non-interactive)
RUN yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_HOME} --licenses || true

# PATH for sdkmanager, adb, etc.
ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}"

# Create vscode user for devcontainer (UID 1000 matches typical host)
RUN groupadd --gid 1000 vscode \
    && useradd --uid 1000 --gid vscode --shell /bin/bash --create-home vscode \
    && mkdir -p /home/vscode/.gradle && chown -R vscode:vscode /home/vscode

USER vscode
WORKDIR /workspace
