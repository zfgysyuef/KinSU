#!/bin/bash
set -e
export PATH="/home/hanha/.cargo/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
cd /mnt/d/KinSU/KernelSU

echo "=== Rust version ==="
cargo --version
rustc --version

echo "=== Installed targets ==="
rustup target list --installed 2>&1

echo "=== Clean ksud ==="
cargo clean -p ksud 2>&1

echo "=== Build ksud for aarch64-linux-android ==="
cargo build --release --target aarch64-linux-android -p ksud 2>&1 | tail -50

echo "=== Check output ==="
ls -la target/aarch64-linux-android/release/ksud 2>&1
file target/aarch64-linux-android/release/ksud 2>&1
