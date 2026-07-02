#!/bin/bash
set -e
cd /tmp
rm -rf android-kernel-6.1 2>/dev/null || true
echo "Starting clone..."
GIT_SSL_NO_VERIFY=1 git clone --depth=1 --single-branch --branch=android14-6.1 https://gitee.com/mirrors_aosp/kernel_common android-kernel-6.1
echo "Clone done, checking..."
ls /tmp/android-kernel-6.1/Makefile && echo "SUCCESS" || echo "FAILED"
