#!/usr/bin/env python3
"""Convert SVG with <g fill="..."> and <path d="..."/> to Android Vector Drawable."""
import re
import sys

def main():
    svg_path = "binaural_icon.svg"
    out_path = "app/src/main/res/drawable/ic_binaural.xml"

    with open(svg_path, "r", encoding="utf-8") as f:
        content = f.read()

    # ViewBox from SVG (e.g. viewBox="0 0 1024 1024")
    viewbox = re.search(r'viewBox="([^"]+)"', content)
    if viewbox:
        parts = viewbox.group(1).split()
        vw, vh = parts[2], parts[3]
    else:
        vw, vh = "1024", "1024"

    # Line-by-line: track current fill from <g fill="...">, collect path d="..." with that fill
    current_fill = "#000000"
    paths = []
    path_pattern = re.compile(r'<path\s+d="([^"]+)"\s*/>')
    g_pattern = re.compile(r'<g\s+fill="(#[0-9a-fA-F]+)"')

    for line in content.splitlines():
        g_match = g_pattern.search(line)
        if g_match:
            current_fill = g_match.group(1)
        path_match = path_pattern.search(line)
        if path_match:
            path_data = path_match.group(1)
            paths.append((current_fill, path_data))

    # Build Android Vector Drawable
    lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        f'    android:width="108dp"',
        f'    android:height="108dp"',
        f'    android:viewportWidth="{vw}"',
        f'    android:viewportHeight="{vh}">',
    ]
    for fill, path_data in paths:
        lines.append(f'    <path')
        lines.append(f'        android:fillColor="{fill}"')
        lines.append(f'        android:pathData="{path_data}"/>')
    lines.append('</vector>')

    import os
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

    print(f"Written {len(paths)} paths to {out_path}")

if __name__ == "__main__":
    main()
