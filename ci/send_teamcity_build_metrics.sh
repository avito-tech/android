#!/usr/bin/env bash

# This is an entrypoint for CI build step, don't change it's relative path(name)

set -xue

source $(dirname $0)/_main.sh

readonly TASK="./gradlew :subprojects:teamcity-metrics-collector:sendTeamcityBuildsMetrics"
PARAMS=""
PARAMS+="-PmetricsSourcesConfigPath=/app/ci/config/teamcity_build_metrics_config.json "
PARAMS+="-Pgraphite.host=${GRAPHITE_HOST} "
PARAMS+="-Pgraphite.port=${GRAPHITE_PORT} "

runInBuilder "${TASK} ${PARAMS} --no-daemon ${GRADLE_ARGS}"
