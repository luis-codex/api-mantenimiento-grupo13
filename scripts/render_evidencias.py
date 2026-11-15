#!/usr/bin/env python3
"""Render evidence TXT files as readable, paginated PNG using ImageMagick."""
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
import subprocess

project_dir = Path(__file__).resolve().parent.parent
evidence_dir = project_dir.parent / "evidencias"
png_dir = evidence_dir / "png"
png_dir.mkdir(parents=True, exist_ok=True)
page_lines = 65
jobs = []
text_files = sorted(evidence_dir.glob("*.txt"))

for source in text_files:
    lines = source.read_text(encoding="utf-8", errors="replace").splitlines() or ["(archivo vacío)"]
    chunks = [lines[i:i + page_lines] for i in range(0, len(lines), page_lines)]
    for index, chunk in enumerate(chunks, start=1):
        output = png_dir / f"{source.stem}-{index:03}.png"
        if not output.exists() or output.stat().st_mtime < source.stat().st_mtime:
            jobs.append((chunk, output))

def render(job):
    chunk, output = job
    subprocess.run(
        [
            "magick", "-background", "#ffffff", "-fill", "#17202a",
            "-font", "DejaVu-Sans-Mono", "-pointsize", "13", "+antialias",
            "-size", "1600x", "caption:@-", "-depth", "8",
            "-define", "png:compression-level=1", "-define", "png:compression-filter=0", str(output),
        ],
        input=("\n".join(chunk) + "\n").encode("utf-8"),
        check=True,
    )
    return output.name

if jobs:
    with ThreadPoolExecutor(max_workers=min(4, len(jobs))) as executor:
        futures = [executor.submit(render, job) for job in jobs]
        for future in as_completed(futures):
            future.result()
print(f"TXT fuente: {len(text_files)}; PNG generados ahora: {len(jobs)}; destino: {png_dir}")
