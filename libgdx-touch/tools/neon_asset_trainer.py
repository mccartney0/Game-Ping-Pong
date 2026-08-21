#!/usr/bin/env python3
"""Trainer/editor procedural de assets neon para o Game-Ping-Pong.

Dependência: Pillow.
Exemplos:
  python3 neon_asset_trainer.py generate
  python3 neon_asset_trainer.py preview
  python3 neon_asset_trainer.py tune --glow-values 6,10,14 --stroke-values 4,6,8
  python3 neon_asset_trainer.py validate
"""

from __future__ import annotations

import argparse
import json
import math
import shutil
from pathlib import Path
from typing import Callable, Iterable

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_CONFIG = ROOT / "assets/config/neon_assets.json"
DEFAULT_OUTPUT = ROOT / "assets/generated/neon"
DEFAULT_PREVIEW = ROOT / "assets/generated/previews/neon_preview.png"

RGBA = tuple[int, int, int, int]


def rgba(value: Iterable[int]) -> RGBA:
    values = list(value)
    if len(values) == 3:
        values.append(255)
    return tuple(max(0, min(255, int(part))) for part in values[:4])  # type: ignore[return-value]


def load_config(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as stream:
        return json.load(stream)


def add_glow(base: Image.Image, radius: float, strength: float = 0.86) -> Image.Image:
    if radius <= 0:
        return base
    blurred = base.filter(ImageFilter.GaussianBlur(radius))
    alpha = blurred.getchannel("A").point(lambda value: int(value * strength))
    blurred.putalpha(alpha)
    result = Image.new("RGBA", base.size, (0, 0, 0, 0))
    result.alpha_composite(blurred)
    result.alpha_composite(base)
    return result


def draw_arrow(draw: ImageDraw.ImageDraw, center: tuple[float, float], direction: int,
               length: float, color: RGBA, width: int) -> None:
    x, y = center
    end_x = x + direction * length
    draw.line((x, y, end_x, y), fill=color, width=width)
    head = width * 1.8
    draw.line((end_x, y, end_x - direction * head, y - head), fill=color, width=width)
    draw.line((end_x, y, end_x - direction * head, y + head), fill=color, width=width)


def draw_shape(draw: ImageDraw.ImageDraw, kind: str, color: RGBA, size: int, stroke: int) -> None:
    center = size / 2
    margin = size * 0.18
    width = max(2, stroke)
    white = (235, 250, 255, 255)

    if kind == "bolt":
        points = [
            (center + size * 0.05, margin),
            (center - size * 0.16, center - size * 0.04),
            (center - size * 0.02, center - size * 0.04),
            (center - size * 0.20, size - margin),
            (center + size * 0.20, center + size * 0.02),
            (center + size * 0.04, center + size * 0.02),
        ]
        draw.polygon(points, fill=color)
        draw.line(points + [points[0]], fill=white, width=max(2, width // 2), joint="curve")
    elif kind == "shield":
        points = [
            (center, margin), (size - margin, margin + size * 0.12),
            (size - margin * 1.25, center + size * 0.12),
            (center, size - margin), (margin * 1.25, center + size * 0.12),
            (margin, margin + size * 0.12),
        ]
        draw.polygon(points, fill=color)
        draw.line(points + [points[0]], fill=white, width=max(2, width // 2), joint="curve")
        draw.line((center, margin * 1.8, center, size - margin * 1.9), fill=white, width=max(2, width // 2))
    elif kind == "wide":
        y = center
        draw.rounded_rectangle((margin, y - width * 1.4, size - margin, y + width * 1.4),
                               radius=width, fill=color)
        draw_arrow(draw, (center - size * 0.22, center), -1, size * 0.16, white, max(2, width // 2))
        draw_arrow(draw, (center + size * 0.22, center), 1, size * 0.16, white, max(2, width // 2))
    elif kind == "energy":
        draw.ellipse((margin, margin, size - margin, size - margin), outline=white, width=max(2, width // 2))
        draw.ellipse((margin + width, margin + width, size - margin - width, size - margin - width), fill=color)
        draw.line((center - size * 0.13, center, center + size * 0.04, center + size * 0.16), fill=white, width=width)
        draw.line((center + size * 0.04, center + size * 0.16, center + size * 0.20, center - size * 0.17), fill=white, width=width)
    elif kind == "slow":
        draw.ellipse((margin, margin, size - margin, size - margin), fill=color, outline=white, width=max(2, width // 2))
        draw.line((center, center, center, center - size * 0.19), fill=white, width=width)
        draw.line((center, center, center + size * 0.14, center + size * 0.08), fill=white, width=width)
        for angle in range(0, 360, 45):
            radians = math.radians(angle)
            inner = center + math.cos(radians) * size * 0.29
            inner_y = center + math.sin(radians) * size * 0.29
            outer = center + math.cos(radians) * size * 0.37
            outer_y = center + math.sin(radians) * size * 0.37
            draw.line((inner, inner_y, outer, outer_y), fill=white, width=max(1, width // 2))
    elif kind == "split":
        draw.ellipse((margin, center - size * 0.16, center - size * 0.05, center + size * 0.16), fill=color, outline=white, width=max(2, width // 2))
        draw.ellipse((center + size * 0.05, center - size * 0.16, size - margin, center + size * 0.16), fill=color, outline=white, width=max(2, width // 2))
        draw.line((center, center - size * 0.25, center, center + size * 0.25), fill=white, width=max(2, width // 2))
    elif kind == "multi":
        points = []
        for index in range(16):
            angle = -math.pi / 2 + index * math.pi / 8
            radius = size * (0.34 if index % 2 == 0 else 0.16)
            points.append((center + math.cos(angle) * radius, center + math.sin(angle) * radius))
        draw.polygon(points, fill=color, outline=white)
        draw.line((center - size * 0.13, center, center + size * 0.13, center), fill=white, width=width)
        draw.line((center, center - size * 0.13, center, center + size * 0.13), fill=white, width=width)
    elif kind == "button":
        draw.rounded_rectangle((margin, size * 0.30, size - margin, size * 0.70), radius=width * 2, fill=color, outline=white, width=max(2, width // 2))
        draw.line((size * 0.36, center, size * 0.64, center), fill=white, width=width)
    elif kind == "grid":
        for offset in range(int(margin), int(size - margin), max(8, width * 2)):
            draw.line((margin, offset, size - margin, offset), fill=color, width=max(1, width // 2))
            draw.line((offset, margin, offset, size - margin), fill=color, width=max(1, width // 2))
        draw.rectangle((margin, margin, size - margin, size - margin), outline=white, width=max(1, width // 2))
    else:
        draw.ellipse((margin, margin, size - margin, size - margin), fill=color, outline=white, width=max(2, width // 2))


def render_asset(kind: str, color: RGBA, size: int, stroke: int, glow_radius: float) -> Image.Image:
    base = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(base)
    draw_shape(draw, kind, color, size, stroke)
    return add_glow(base, glow_radius)


def generate_assets(config: dict, output: Path) -> list[Path]:
    if output.exists():
        shutil.rmtree(output)
    output.mkdir(parents=True, exist_ok=True)

    base_size = int(config.get("size", 128))
    glow = float(config.get("glow_radius", 10))
    stroke = int(config.get("stroke", 6))
    colors = {name: rgba(value) for name, value in config["colors"].items()}
    assets = config["assets"]
    generated: list[Path] = []

    for scale in config.get("scales", [1, 2, 3]):
        scale = int(scale)
        size = base_size * scale
        scale_dir = output / f"{scale}x"
        scale_dir.mkdir(parents=True, exist_ok=True)
        for name, spec in assets.items():
            image = render_asset(spec["kind"], colors[spec["color"]], size,
                                 stroke * scale, glow * scale)
            target = scale_dir / f"{name}.png"
            image.save(target, "PNG", optimize=True)
            generated.append(target)
    return generated


def create_preview(input_dir: Path, target: Path) -> None:
    files = sorted(input_dir.glob("1x/*.png"))
    if not files:
        raise FileNotFoundError(f"Nenhum PNG 1x encontrado em {input_dir}")
    thumb_size = 180
    columns = 3
    rows = math.ceil(len(files) / columns)
    sheet = Image.new("RGBA", (columns * thumb_size, rows * thumb_size), (4, 9, 28, 255))
    draw = ImageDraw.Draw(sheet)
    for index, path in enumerate(files):
        with Image.open(path) as source:
            image = source.convert("RGBA")
            image.thumbnail((128, 128), Image.Resampling.LANCZOS)
            x = (index % columns) * thumb_size + (thumb_size - image.width) // 2
            y = (index // columns) * thumb_size + 18
            sheet.alpha_composite(image, (x, y))
        draw.text(((index % columns) * thumb_size + 10, (index // columns) * thumb_size + 150),
                  path.stem, fill=(220, 248, 255, 255))
    target.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(target, "PNG", optimize=True)


def validate_assets(input_dir: Path) -> int:
    files = sorted(input_dir.rglob("*.png"))
    errors: list[str] = []
    for path in files:
        try:
            with Image.open(path) as image:
                if image.mode != "RGBA":
                    errors.append(f"{path}: mode {image.mode}, esperado RGBA")
                if image.width == 0 or image.height == 0:
                    errors.append(f"{path}: dimensao vazia")
                if image.getchannel("A").getbbox() is None:
                    errors.append(f"{path}: alpha totalmente vazio")
        except Exception as exc:  # pragma: no cover - CLI diagnostics
            errors.append(f"{path}: {exc}")
    if errors:
        for error in errors:
            print(f"ERRO: {error}")
        return 1
    print(f"OK: {len(files)} PNG(s) RGBA validado(s) em {input_dir}")
    return 0


def parse_values(raw: str) -> list[float]:
    return [float(value.strip()) for value in raw.split(",") if value.strip()]


def command_generate(args: argparse.Namespace) -> None:
    config = load_config(args.config)
    generated = generate_assets(config, args.output)
    print(f"OK: {len(generated)} asset(s) gerado(s) em {args.output}")


def command_preview(args: argparse.Namespace) -> None:
    create_preview(args.input, args.output)
    print(f"OK: preview criado em {args.output}")


def command_tune(args: argparse.Namespace) -> None:
    config = load_config(args.config)
    glow_values = parse_values(args.glow_values)
    stroke_values = [int(value) for value in parse_values(args.stroke_values)]
    if args.clean and args.output.exists():
        shutil.rmtree(args.output)
    args.output.mkdir(parents=True, exist_ok=True)
    variants = 0
    for glow in glow_values:
        for stroke in stroke_values:
            variant = dict(config)
            variant["glow_radius"] = glow
            variant["stroke"] = stroke
            name = f"glow-{glow:g}_stroke-{stroke:g}"
            variant_dir = args.output / name
            generate_assets(variant, variant_dir)
            create_preview(variant_dir, variant_dir / "preview.png")
            variants += 1
    print(f"OK: {variants} variante(s) treinada(s) em {args.output}")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Trainer/editor procedural de assets neon")
    subparsers = parser.add_subparsers(dest="command", required=True)

    generate = subparsers.add_parser("generate", help="gera PNGs RGBA nas escalas configuradas")
    generate.add_argument("--config", type=Path, default=DEFAULT_CONFIG)
    generate.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    generate.set_defaults(function=command_generate)

    preview = subparsers.add_parser("preview", help="cria uma folha de preview dos assets 1x")
    preview.add_argument("--input", type=Path, default=DEFAULT_OUTPUT)
    preview.add_argument("--output", type=Path, default=DEFAULT_PREVIEW)
    preview.set_defaults(function=command_preview)

    tune = subparsers.add_parser("tune", help="gera uma grade de variantes glow/stroke")
    tune.add_argument("--config", type=Path, default=DEFAULT_CONFIG)
    tune.add_argument("--output", type=Path, default=ROOT / "assets/generated/tuning")
    tune.add_argument("--glow-values", default="6,10,14", help="valores separados por virgula")
    tune.add_argument("--stroke-values", default="4,6,8", help="valores separados por virgula")
    tune.add_argument("--clean", action="store_true")
    tune.set_defaults(function=command_tune)

    validate = subparsers.add_parser("validate", help="valida modo, alpha e dimensoes dos PNGs")
    validate.add_argument("--input", type=Path, default=DEFAULT_OUTPUT)
    validate.set_defaults(function=lambda args: setattr(args, "return_code", validate_assets(args.input)))
    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()
    args.function(args)
    return int(getattr(args, "return_code", 0))


if __name__ == "__main__":
    raise SystemExit(main())
