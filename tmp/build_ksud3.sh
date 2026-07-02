#!/bin/bash
set -ex
export PATH="/home/hanha/.cargo/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
cd /mnt/d/KinSU/KernelSU

echo "=== Clean ksud build (rust-embed requires clean) ==="
cargo clean -p ksud 2>&1 || true

echo "=== Install aarch64-linux-android target ==="
rustup target add aarch64-linux-android 2>&1 || echo "target add failed"

echo "=== Build ksud with CLI git fetch ==="
export CARGO_NET_GIT_FETCH_WITH_CLI=true
cargo build --release --target aarch64-linux-android -p ksud 2>&1 | tail -40

echo "=== Check output ==="
ls -la target/aarch64-linux-android/release/ksud 2>&1
