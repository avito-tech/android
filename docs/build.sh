#!/usr/bin/env bash

# Build documentation locally

set -euf -o pipefail

cd docs/

docker build --tag android/docs/local .

HOST=http://localhost:1313/avito-android

function openBrowser {
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        bash -c "sleep 3 ; xdg-open ${HOST}" &
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        bash -c "sleep 3 ; open ${HOST}" &
    else
        echo "Please, fix this script for your OS: ${OSTYPE}"
    fi
}

openBrowser

# WARNING:
# Override by volumes only directories without generated files.
# Otherwise it'll crash hugo due to missing files.
# You can find them in .dockerignore

docker run --rm \
        --volume "$(pwd)":/app \
        -w="/app" \
        -p 1313:1313 \
        --entrypoint hugo \
        android/docs/local:latest \
        server --cleanDestinationDir --minify --theme book --bind 0.0.0.0

