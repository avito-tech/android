#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path(name)

set -e

source $(dirname $0)/_main.sh

runInBuilder "set -e; ./gradlew build -PcustomFilter=ci ${GRADLE_ARGS}"
