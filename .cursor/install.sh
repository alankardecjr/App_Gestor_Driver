#!/usr/bin/env bash
#
# Cloud Agent install script for Gestor Driver.
#
# Prepares both parts of the repository:
#   * Python calculation core (core/, notifications/, app/, tests/) in a venv
#   * Android app (android-app/) with a headless Android SDK for gradle builds
#
# The script is idempotent: it skips work that is already done, so it is safe
# to run repeatedly and against a warm/cached VM.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

log() { printf '\n\033[1;34m[install]\033[0m %s\n' "$*"; }

# ---------------------------------------------------------------------------
# System packages (python venv + unzip for the Android SDK download)
# ---------------------------------------------------------------------------
missing_pkgs=()
dpkg -s python3-venv   >/dev/null 2>&1 || dpkg -s python3.12-venv >/dev/null 2>&1 || missing_pkgs+=(python3.12-venv)
command -v unzip >/dev/null 2>&1 || missing_pkgs+=(unzip)
command -v curl  >/dev/null 2>&1 || missing_pkgs+=(curl)
if [ "${#missing_pkgs[@]}" -gt 0 ]; then
  log "Installing system packages: ${missing_pkgs[*]}"
  sudo apt-get update -qq
  sudo apt-get install -y -qq "${missing_pkgs[@]}"
fi

# ---------------------------------------------------------------------------
# Python calculation core
# ---------------------------------------------------------------------------
log "Setting up Python core virtualenv"
if [ ! -d .venv ]; then
  python3 -m venv .venv
fi
# shellcheck disable=SC1091
. .venv/bin/activate
python -m pip install --upgrade pip -q
pip install -q -r requirements.txt
deactivate

# ---------------------------------------------------------------------------
# Android SDK (headless, command-line only)
# ---------------------------------------------------------------------------
export ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
CMDLINE_TOOLS_VERSION="11076708"
CMDLINE_BIN="$ANDROID_HOME/cmdline-tools/latest/bin"

if [ ! -x "$CMDLINE_BIN/sdkmanager" ]; then
  log "Installing Android command-line tools into $ANDROID_HOME"
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  tmp_dir="$(mktemp -d)"
  curl -fsSL -o "$tmp_dir/cmdtools.zip" \
    "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip"
  unzip -q "$tmp_dir/cmdtools.zip" -d "$ANDROID_HOME/cmdline-tools"
  rm -rf "$ANDROID_HOME/cmdline-tools/latest"
  mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
  rm -rf "$tmp_dir"
fi

log "Accepting Android SDK licenses and installing required packages"
yes | "$CMDLINE_BIN/sdkmanager" --licenses >/dev/null 2>&1 || true
# platforms/android-36 -> compileSdk/targetSdk; build-tools 35 & 36 are both
# pulled in by AGP 8.13 depending on the task.
"$CMDLINE_BIN/sdkmanager" \
  "platform-tools" \
  "platforms;android-36" \
  "build-tools;36.0.0" \
  "build-tools;35.0.0" >/dev/null

# ---------------------------------------------------------------------------
# Gradle wiring for the Android module
# ---------------------------------------------------------------------------
log "Writing android-app/local.properties (git-ignored, machine specific)"
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > android-app/local.properties
chmod +x android-app/gradlew || true

log "Environment setup complete."
