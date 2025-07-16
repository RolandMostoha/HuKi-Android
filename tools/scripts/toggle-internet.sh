#!/bin/bash

# Check Wi-Fi state
WIFI_STATE=$(adb shell dumpsys wifi | grep "Wi-Fi is" | awk '{print $3}' | tr -d '\r')

# Check Cellular data state
DATA_STATE=$(adb shell dumpsys telephony.registry | grep mDataConnectionState | awk -F= '{print $2}' | tr -d '\r')

# Function to disable both
disable_networks() {
    echo "Disabling Wi-Fi..."
    adb shell svc wifi disable
    echo "Disabling cellular data..."
    adb shell svc data disable
    echo "Wi-Fi and cellular data are now disabled."
}

# Function to enable both
enable_networks() {
    echo "Enabling Wi-Fi..."
    adb shell svc wifi enable
    echo "Enabling cellular data..."
    adb shell svc data enable
    echo "Wi-Fi and cellular data are now enabled."
}

# Determine overall state
if [[ "$WIFI_STATE" == "enabled" || "$DATA_STATE" == "2" ]]; then
    # Wi-Fi enabled or data connected (state 2 means connected)
    disable_networks
else
    enable_networks
fi