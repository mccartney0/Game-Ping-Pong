#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
VERSION_NAME="${VERSION_NAME:?VERSION_NAME is required}"
BUILD_DIR="$ROOT_DIR/build/release-awt"
DIST_DIR="$ROOT_DIR/dist"

rm -rf "$BUILD_DIR" "$DIST_DIR"
mkdir -p "$BUILD_DIR/classes" "$BUILD_DIR/resources" "$DIST_DIR"

find "$ROOT_DIR/release-updater/src/main/java" "$ROOT_DIR/src" -name '*.java' -print | sort > "$BUILD_DIR/sources.txt"
javac --release 13 -encoding UTF-8 -d "$BUILD_DIR/classes" @"$BUILD_DIR/sources.txt"
sed "s/^version=.*/version=$VERSION_NAME/" "$ROOT_DIR/src/release-version.properties" > "$BUILD_DIR/resources/release-version.properties"

jar --create \
    --file "$DIST_DIR/neon-ping-pong-awt.jar" \
    --main-class pong.main.Game \
    -C "$BUILD_DIR/classes" . \
    -C "$BUILD_DIR/resources" release-version.properties

sha256sum "$DIST_DIR/neon-ping-pong-awt.jar" | awk '{print $1}' > "$DIST_DIR/neon-ping-pong-awt.jar.sha256"
