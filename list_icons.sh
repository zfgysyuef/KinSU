#!/bin/bash
cd /mnt/d/FollKernel/tmp/folkpatch_extract/res
for f in *.png; do
    sz=$(stat -c '%s' "$f")
    if [ "$sz" -gt 5000 ] && [ "$sz" -lt 200000 ]; then
        echo "$sz $f"
    fi
done | sort -n | head -40
