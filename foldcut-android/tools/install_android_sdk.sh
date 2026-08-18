#!/usr/bin/env bash
set -euo pipefail
SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
mkdir -p "$SDK_ROOT/cmdline-tools"
if [ ! -x "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]; then
  tmp_dir="$(mktemp -d)"
  curl -fsSL -o "$tmp_dir/cmdline-tools.zip" "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
  unzip -q "$tmp_dir/cmdline-tools.zip" -d "$tmp_dir"
  rm -rf "$SDK_ROOT/cmdline-tools/latest"
  mv "$tmp_dir/cmdline-tools" "$SDK_ROOT/cmdline-tools/latest"
  rm -rf "$tmp_dir"
fi
export ANDROID_SDK_ROOT="$SDK_ROOT"
export ANDROID_HOME="$SDK_ROOT"
yes | "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --licenses >/dev/null || true
"$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;android-35" "build-tools;35.0.0"
