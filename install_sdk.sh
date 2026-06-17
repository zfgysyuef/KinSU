#!/bin/bash
set -e
export ANDROID_SDK_ROOT=/mnt/d/rekernel/KernelSU/manager/android-sdk
"$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;android-37.0" "build-tools;37.0.0" "ndk;29.0.14206865" "cmake;3.31.1"
