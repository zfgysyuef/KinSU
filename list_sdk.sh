#!/bin/bash
export ANDROID_SDK_ROOT=/mnt/d/rekernel/KernelSU/manager/android-sdk
"$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --list | grep -E "ndk|cmake|build-tools|platforms" | head -50
