adb shell am start \
    -a android.intent.action.SEND \
    -t "text/plain" \
    -n "hu.mostoha.mobile.android.huki.debug/hu.mostoha.mobile.android.huki.ui.home.HomeActivity" \
    --es "android.intent.extra.TEXT" "https://maps.app.goo.gl/o6hKGF8YRqztQbWWA"
