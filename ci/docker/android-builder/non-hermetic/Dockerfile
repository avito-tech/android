FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

RUN \
  apt-get update && \
  apt-get install -y \
    zip \
    unzip \
    curl \
    git \
    git-lfs \
    gcc \
    openssh-client \
    ca-certificates \
    build-essential \
    acl \
    sudo \
    openjdk-17-jdk \
    ca-certificates-java && \
  apt-get clean && \
  apt-get purge

ENV LANG C.UTF-8
ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk-amd64/

# ----------------- Android SDK -----------------
ENV ANDROID_HOME /opt/android-sdk
ENV PATH ${PATH}:${ANDROID_HOME}/tools/:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/platform-tools/bin:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/cmdline-tools/bin
# https://developer.android.com/studio/index.html#command-tools
ARG ANDROID_SDK_URL=https://dl.google.com/android/repository/commandlinetools-linux-8092744_latest.zip

COPY unzip_from_url.sh /usr/local/bin/unzip_from_url.sh

RUN unzip_from_url.sh $ANDROID_SDK_URL $ANDROID_HOME

COPY non-hermetic/packages.txt $ANDROID_HOME/packages.txt

# Update sdk and install components
RUN mkdir $HOME/.android && \
  echo "y" | sdkmanager --verbose \
    --sdk_root=$ANDROID_HOME \
    --package_file=$ANDROID_HOME/packages.txt && \
  chmod -R o+rwX $ANDROID_HOME && \
  rm $ANDROID_HOME/packages.txt

# --------------- Gradle Profiler -----------------
# https://github.com/gradle/gradle-profiler/releases
ARG PROFILER_VERSION=0.19.0

# Install gradle-profiler
RUN \
  cd /opt/ && \
  curl https://repo1.maven.org/maven2/org/gradle/profiler/gradle-profiler/${PROFILER_VERSION}/gradle-profiler-${PROFILER_VERSION}.zip \
    --progress-bar --location --output gradle-profiler.zip && \
  unzip gradle-profiler.zip && \
  mv gradle-profiler-${PROFILER_VERSION} gradle-profiler && \
  cp -a gradle-profiler/. /usr/local/ && \
  rm -rf /opt/gradle-profiler && \
  rm -f /opt/gradle-profiler.zip

ENV PATH $PATH:/usr/local/gradle-profiler/bin

# ----------------------------------------------------------------------------

# Entrypoint script will allow us run as non-root in the container.
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
