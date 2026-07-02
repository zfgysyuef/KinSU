#!/bin/bash
set -ex
export PATH="/home/hanha/.cargo/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
cd /mnt/d/KinSU/KernelSU

echo "=== Install aarch64-linux-android target ==="
rustup target add aarch64-linux-android 2>&1 || echo "target add failed"

echo "=== Check git dependency cache ==="
ls -la /home/hanha/.cargo/git/db/android_bootimg-9264538b49fbfae9/ 2>&1 || true

echo "=== Try fetch with CLI ==="
export CARGO_NET_GIT_FETCH_WITH_CLI=true
cargo build --release --target aarch64-linux-android -p ksud 2>&1 | tail -60

echo "=== Check output ==="
ls -la target/aarch64-linux-android/release/ksud 2>&1
