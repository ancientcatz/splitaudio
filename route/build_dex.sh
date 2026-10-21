#!/bin/sh
# Compile route/AppRoute.java into app/src/main/assets/approute.dex (needed before building the APK).
# Needs: JDK 17, Android SDK with platforms;android-34 and build-tools;34.0.0 (ANDROID_HOME set).
set -e
cd "$(dirname "$0")"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:?set ANDROID_HOME}}"
AJ="$SDK/platforms/android-34/android.jar"
D8="$SDK/build-tools/34.0.0/d8"
rm -rf out && mkdir -p out/classes
javac --release 8 -nowarn -cp "$AJ" -d out/classes AppRoute.java
"$D8" --release --min-api 29 --lib "$AJ" --output out out/classes/*.class
cp out/classes.dex ../app/src/main/assets/approute.dex
rm -rf out
echo "built app/src/main/assets/approute.dex"
