#!/bin/bash
# Toggles the HuKi app language between Hungarian (hu-HU) and English (en-US) on the connected Android device/emulator via adb.

package="hu.mostoha.mobile.android.huki.debug"

current=$(adb shell cmd locale get-app-locales "$package" | sed -n 's/.*\[\(.*\)\].*/\1/p')

if [[ "$current" == hu* ]]; then
  adb shell cmd locale set-app-locales "$package" --locales en-US
  echo "Language set to English (en-US)"
else
  adb shell cmd locale set-app-locales "$package" --locales hu-HU
  echo "Language set to Hungarian (hu-HU)"
fi
