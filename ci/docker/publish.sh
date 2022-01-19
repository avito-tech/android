#!/usr/bin/env bash

set -e

source $(dirname "$0")/../_environment.sh

if test "$#" -ne 1; then
    echo "ERROR: Missing arguments. You should pass a path to a directory with Dockerfile: ./publish.sh <directory>"
    exit 1
fi

BUILD_DIRECTORY=$(pwd)/$1

docker run --rm \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${BUILD_DIRECTORY}":/build \
    "${IMAGE_BUILDER}" publish \
        --buildDir /build \
        --dockerHubUsername "${DOCKER_HUB_USERNAME}" \
        --dockerHubPassword "${DOCKER_HUB_PASSWORD}" \
        --registryUsername "${DOCKER_REGISTRY_USERNAME}" \
        --registryPassword "${DOCKER_REGISTRY_PASSWORD}" \
        --registry "${DOCKER_REGISTRY}"
