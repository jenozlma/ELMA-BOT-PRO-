#!/usr/bin/env sh
# Minimal fallback launcher for environments where Gradle is installed globally.
# For a pinned Gradle wrapper, generate the official wrapper from Android Studio/Gradle.
set -eu
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle n'est pas installé. Ouvre ce projet dans Android Studio ou installe Gradle 8.9." >&2
exit 127
