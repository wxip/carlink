call ./gradlew.bat server:build
adb -s 87fa2842 push server/build/outputs/apk/debug/server-debug.apk /data/local/tmp/carlink-server.jar