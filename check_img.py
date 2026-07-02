#!/usr/bin/env python3
"""Embed images as base64 into a single HTML file for direct viewing."""
import base64

html = """<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>Icon Preview</title>
<style>
body { background: #f0f0f0; font-family: sans-serif; padding: 20px; }
.img-box { display: inline-block; margin: 10px; text-align: center; background: white; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.img-box img { max-width: 300px; max-height: 300px; border: 1px solid #ddd; }
.img-box .label { margin-top: 8px; font-size: 14px; color: #333; }
</style></head><body>
<h2>AI Generated Bohr Icon Preview</h2>
"""

images = [
    ("/mnt/d/FollKernel/tmp/bohe_ai_icon_v2.png", "AI Bohr (square)"),
    ("/mnt/d/FollKernel/tmp/bohe_ai_circle.png", "AI Bohr (circle)"),
    ("/mnt/d/FollKernel/tmp/folkpatch_icon_192.png", "FolkPatch AnQuQu (reference)"),
]

for path, label in images:
    try:
        with open(path, "rb") as f:
            data = base64.b64encode(f.read()).decode()
        html += f'<div class="img-box"><img src="data:image/png;base64,{data}"><div class="label">{label}</div></div>\n'
    except Exception as e:
        html += f'<div class="img-box"><div class="label">{label}: ERROR - {e}</div></div>\n'

html += "</body></html>"

with open("/mnt/d/FollKernel/tmp/preview.html", "w", encoding="utf-8") as f:
    f.write(html)
print("HTML preview saved: d:\\FollKernel\\tmp\\preview.html")
