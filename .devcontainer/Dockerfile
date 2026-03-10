# Devcontainer for Android development (Kotlin + Jetpack Compose)
# Ubuntu 22.04, JDK 17, Android SDK command-line tools
FROM ubuntu:22.04

# Dev user (override to detach from vscode devcontainer defaults)
ARG DEV_USER=vscode
ARG DEV_UID=1000
ARG DEV_GID=1000

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_HOME=/opt/android-sdk
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# System packages: JDK 17, tools for SDK install, git, Python for pre-commit
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    unzip \
    wget \
    git \
    python3 \
    python3-pip \
    python3-venv \
    && rm -rf /var/lib/apt/lists/*

# Android command-line tools: download and install to $ANDROID_HOME/cmdline-tools/latest
ARG CMDLINE_TOOLS_URL=https://dl.google.com/android/repository/commandlinetools-linux-14742923_latest.zip
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools/latest \
    && wget -q -O /tmp/cmdline-tools.zip "${CMDLINE_TOOLS_URL}" \
    && unzip -q /tmp/cmdline-tools.zip -d /tmp \
    && mv /tmp/cmdline-tools/* ${ANDROID_HOME}/cmdline-tools/latest/ \
    && rm -rf /tmp/cmdline-tools.zip /tmp/cmdline-tools

# Install SDK components: platform-tools, platforms 34 & 36, build-tools
RUN yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_HOME} \
    "platform-tools" \
    "platforms;android-34" \
    "platforms;android-36" \
    "build-tools;34.0.0" \
    || true

# Accept remaining licenses (non-interactive)
RUN yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_HOME} --licenses || true

# Upgrade to Android SDK Command-line Tools 17+ so SDK XML version 4 is supported
# (avoids "only understands SDK XML versions up to 3" when using Gradle / newer SDK repos)
RUN yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --sdk_root=${ANDROID_HOME} "cmdline-tools;latest" \
    && cd ${ANDROID_HOME}/cmdline-tools \
    && NEWVER=$(ls -d [0-9]* 2>/dev/null | sort -V | tail -1) \
    && if [ -n "$$NEWVER" ] && [ -d "$$NEWVER" ] && [ "$$NEWVER" != "latest" ]; then rm -rf latest && mv "$$NEWVER" latest; fi

# PATH for sdkmanager, adb, etc.
ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}"

# Create dev user (home = /home/${DEV_USER})
RUN groupadd --gid ${DEV_GID} ${DEV_USER} \
    && useradd --uid ${DEV_UID} --gid ${DEV_USER} --shell /bin/bash --create-home ${DEV_USER} \
    && mkdir -p /home/${DEV_USER}/.gradle && chown -R ${DEV_USER}:${DEV_USER} /home/${DEV_USER}

# SDK must be writable so Gradle can install components (e.g. new platforms)
RUN chown -R ${DEV_USER}:${DEV_USER} ${ANDROID_HOME}

# Pre-commit: install for dev user (hooks run in repo, need git + Gradle)
ENV PATH="/home/${DEV_USER}/.local/bin:${PATH}"
USER ${DEV_USER}
RUN pip3 install --user pre-commit

WORKDIR /workspace
