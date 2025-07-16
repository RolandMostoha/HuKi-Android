#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Get list of all connected devices (ignoring the first line "List of devices attached")
devices=$(adb devices | grep -w "device" | awk '{print $1}')

if [ -z "$devices" ]; then
    echo "No connected devices found."
    exit 1
fi

echo "Found devices:"
echo "$devices"

# Iterate through each device
for device in $devices; do
    echo "---------------------------------------"
    echo "Clear app data: $device"
    echo "---------------------------------------"
    adb -s $device shell pm clear hu.mostoha.mobile.android.huki.debug

    echo "---------------------------------------"
    echo "Disable animations: $device"
    echo "---------------------------------------"
    adb -s $device shell settings put global window_animation_scale 0
    adb -s $device shell settings put global transition_animation_scale 0
    adb -s $device shell settings put global animator_duration_scale 0

    echo "---------------------------------------"
    echo "Running tests on device: $device"
    echo "---------------------------------------"
    # Set the target device via ANDROID_SERIAL
    export ANDROID_SERIAL=$device
    # Run the instrumentation tests (adjust the Gradle task if needed)
    ./gradlew connectedAndroidTest --info

    echo "Finished tests on $device"
done

echo "✅ All tests completed on all devices."
