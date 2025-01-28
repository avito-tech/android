FROM ubuntu:22.04

ARG SDK_VERSION
ARG EMULATOR_ARCH
ARG EMULATOR_TYPE

ENV EMULATOR_ARCH=${EMULATOR_ARCH}
ENV SDK_VERSION=${SDK_VERSION}
ENV EMULATOR_TYPE=${EMULATOR_TYPE}

# -------------------- Common -------------------
# net-tools             basic network primitives
# socat                 redirecting adb and VNC from emulator to host
# libglu1               emulators software rendering
# libpulse0, libx...    qemu x64 startup (API 30)
# lib32stdc++6          mksdcard android sdk tool
RUN apt-get update && \
	apt-get install --no-install-recommends -y \
	        curl \
    	    unzip \
    	    openjdk-17-jdk && \
    apt-get install -y \
            net-tools \
            socat \
            libglu1 \
            libpulse0 \
            libx11-6 libxcb1 libxdamage1 libnss3 libxcomposite1 libxcursor1 libxi6 libxext6 libxfixes3 \
            lib32stdc++6 && \
    apt-get clean && apt-get purge

ARG ANDROID_HOME=/opt/android-sdk

# DEBIAN_FRONTEND - to prevent timezone questions
ENV LANG=C.UTF-8 \
    SHELL=/bin/bash \
    JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
    DEBIAN_FRONTEND=noninteractive \
    VERSION=${SDK_VERSION} \
    ANDROID_SDK_ROOT=$ANDROID_HOME \
    PATH=$PATH:$ANDROID_HOME/cmdline-tools/tools/bin:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools \
    LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${ANDROID_HOME}/emulator/lib64/qt/lib:${ANDROID_HOME}/emulator/lib64:${ANDROID_HOME}/emulator/lib64/gles_swiftshader
# -----------------------------------------------

# ----------------- Android SDK -----------------
# https://developer.android.com/studio/index.html#command-tools
# Additional info about directory structure: https://stackoverflow.com/a/61176718/981330
ARG COMMANDLINE_TOOLS_URL=https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
ARG ANDROID_SDK_FILE_NAME=commandlinetools.zip

RUN curl $COMMANDLINE_TOOLS_URL --progress-bar --location --output $ANDROID_SDK_FILE_NAME && \
  unzip $ANDROID_SDK_FILE_NAME -d $ANDROID_SDK_ROOT && \
  mv $ANDROID_SDK_ROOT/cmdline-tools $ANDROID_SDK_ROOT/tools && \
  mkdir $ANDROID_SDK_ROOT/cmdline-tools && \
  mv $ANDROID_SDK_ROOT/tools $ANDROID_SDK_ROOT/cmdline-tools/tools && \
  rm -f $ANDROID_SDK_FILE_NAME

# platform-tools For adb and libc
# emulator       For emulator command and libraries, mksdcard and avdmanager
# Channels: 0 (stable), 1 (beta), 2 (dev), or 3 (canary)
RUN mkdir $HOME/.android && touch $HOME/.android/repositories.cfg && \
    yes | sdkmanager --verbose --channel=0 \
    "platforms;android-${SDK_VERSION}" \
    "emulator" \
    "platform-tools" \
    "system-images;android-${SDK_VERSION};${EMULATOR_TYPE};${EMULATOR_ARCH}" && \
    chmod -R o+rwX ${ANDROID_SDK_ROOT}

# ------------------ Emulators ------------------
# Create emulator and increase internal storage
RUN echo "no" | avdmanager create avd \
    --name emulator_${SDK_VERSION} \
    --package "system-images;android-${SDK_VERSION};${EMULATOR_TYPE};${EMULATOR_ARCH}" \
    --abi ${EMULATOR_TYPE}/${EMULATOR_ARCH} && \
    mksdcard -l e 512M /sdcard.img

COPY hardware/config_${SDK_VERSION}_${EMULATOR_TYPE}.ini /root/.android/avd/emulator_${SDK_VERSION}.avd/config.ini
# -----------------------------------------------

# ----------------- Entrypoint ------------------
COPY prepare_snapshot.sh adb_redirect.sh run_emulator.sh entrypoint.sh /

# https://developer.android.com/studio/command-line/adb#howadbworks
# 5037 - ADB server port
# 5554 - Console port
# 5555 - ADB
# 5900 - VNC
EXPOSE 5037 5554 5555 5900

CMD ["/entrypoint.sh"]
