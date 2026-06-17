#!/bin/bash
export ANDROID_SDK_ROOT=/mnt/d/rekernel/KernelSU/manager/android-sdk
"$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --list | grep -E "build-tools;37|ndk;29|cmake;3\.3|platforms;android-3[67]"
