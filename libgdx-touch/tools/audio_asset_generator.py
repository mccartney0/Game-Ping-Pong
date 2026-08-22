#!/usr/bin/env python3
"""Gera efeitos sonoros originais e determinísticos para o Game-Ping-Pong.

Dependência: numpy.
Os WAVs são mono, 44.1 kHz, 16-bit e curtos para uso com libGDX Sound.
"""

from __future__ import annotations

import argparse
import json
import math
import wave
from pathlib import Path

import numpy as np

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_OUTPUT = ROOT / "assets/audio"
SAMPLE_RATE = 44_100
RNG = np.random.default_rng(4242)


def time_array(duration: float) -> np.ndarray:
    return np.arange(max(1, int(duration * SAMPLE_RATE)), dtype=np.float32) / SAMPLE_RATE


def envelope(length: int, attack: float = 0.01, release: float = 0.06) -> np.ndarray:
    result = np.ones(length, dtype=np.float32)
    attack_n = min(length, max(1, int(attack * SAMPLE_RATE)))
    release_n = min(length, max(1, int(release * SAMPLE_RATE)))
    result[:attack_n] = np.linspace(0.0, 1.0, attack_n, dtype=np.float32)
    result[-release_n:] *= np.linspace(1.0, 0.0, release_n, dtype=np.float32)
    return result


def tone(frequency: float, duration: float, amplitude: float = 0.5,
         waveform: str = "sine", attack: float = 0.005, release: float = 0.06) -> np.ndarray:
    t = time_array(duration)
    phase = 2.0 * math.pi * frequency * t
    if waveform == "square":
        signal = np.sign(np.sin(phase))
    elif waveform == "triangle":
        signal = 2.0 * np.abs(2.0 * ((frequency * t) % 1.0) - 1.0) - 1.0
    elif waveform == "saw":
        signal = 2.0 * ((frequency * t) % 1.0) - 1.0
    else:
        signal = np.sin(phase)
    return amplitude * signal.astype(np.float32) * envelope(len(t), attack, release)


def sweep(start: float, end: float, duration: float, amplitude: float = 0.5,
          waveform: str = "sine") -> np.ndarray:
    t = time_array(duration)
    phase = 2.0 * math.pi * (start * t + (end - start) * t * t / (2.0 * duration))
    if waveform == "saw":
        signal = 2.0 * ((phase / (2.0 * math.pi)) % 1.0) - 1.0
    else:
        signal = np.sin(phase)
    return amplitude * signal.astype(np.float32) * envelope(len(t), 0.004, min(0.12, duration * 0.4))


def noise(duration: float, amplitude: float = 0.25, release: float = 0.06) -> np.ndarray:
    values = RNG.normal(0.0, 1.0, len(time_array(duration))).astype(np.float32)
    return amplitude * values * envelope(len(values), 0.001, release)


def silence(duration: float) -> np.ndarray:
    return np.zeros(len(time_array(duration)), dtype=np.float32)


def mix(*parts: np.ndarray) -> np.ndarray:
    if not parts:
        return np.zeros(1, dtype=np.float32)
    length = max(len(part) for part in parts)
    result = np.zeros(length, dtype=np.float32)
    for part in parts:
        result[:len(part)] += part
    return result


def concat(*parts: np.ndarray, gap: float = 0.0) -> np.ndarray:
    chunks: list[np.ndarray] = []
    for index, part in enumerate(parts):
        chunks.append(part)
        if gap > 0.0 and index != len(parts) - 1:
            chunks.append(silence(gap))
    return np.concatenate(chunks).astype(np.float32)


def lowpass(signal: np.ndarray, amount: int = 5) -> np.ndarray:
    if amount <= 1:
        return signal
    kernel = np.ones(amount, dtype=np.float32) / amount
    return np.convolve(signal, kernel, mode="same").astype(np.float32)


def normalize(signal: np.ndarray, peak: float = 0.88) -> np.ndarray:
    max_value = float(np.max(np.abs(signal))) if signal.size else 0.0
    return signal if max_value == 0.0 else signal * min(1.0, peak / max_value)


def make_sounds() -> dict[str, tuple[str, np.ndarray]]:
    sounds: dict[str, tuple[str, np.ndarray]] = {}
    sounds["ui_tap"] = ("ui", tone(720, 0.075, 0.42, "sine", 0.002, 0.035))
    sounds["ui_confirm"] = ("ui", concat(tone(540, 0.09, 0.32), tone(810, 0.13, 0.42), gap=0.018))
    sounds["ui_back"] = ("ui", concat(tone(620, 0.08, 0.28), tone(390, 0.12, 0.34), gap=0.015))
    sounds["ui_pause"] = ("ui", concat(tone(440, 0.12, 0.32), tone(330, 0.15, 0.28), gap=0.025))
    sounds["countdown_beep"] = ("ui", tone(620, 0.17, 0.42, "square", 0.002, 0.05))
    sounds["countdown_go"] = ("ui", concat(tone(660, 0.10, 0.34), tone(990, 0.25, 0.46), gap=0.025))

    hit_body = tone(260, 0.07, 0.42, "triangle", 0.001, 0.045)
    sounds["paddle_hit"] = ("gameplay", normalize(mix(hit_body, lowpass(noise(0.065, 0.18, 0.04), 3))))
    sounds["wall_bounce"] = ("gameplay", normalize(mix(tone(420, 0.06, 0.32, "triangle"), noise(0.04, 0.10, 0.025))))
    sounds["score_point"] = ("gameplay", concat(tone(480, 0.08, 0.30), tone(720, 0.11, 0.34), tone(960, 0.18, 0.42), gap=0.018))
    sounds["match_win"] = ("results", concat(tone(520, 0.12, 0.34), tone(660, 0.12, 0.36), tone(780, 0.22, 0.42), tone(1040, 0.35, 0.48), gap=0.035))
    sounds["match_loss"] = ("results", concat(tone(420, 0.16, 0.30), tone(330, 0.18, 0.32), tone(220, 0.30, 0.34), gap=0.04))

    sounds["powerup_spawn"] = ("powerups", normalize(mix(sweep(260, 880, 0.28, 0.30), tone(1320, 0.08, 0.18))))
    sounds["powerup_collect"] = ("powerups", concat(sweep(460, 920, 0.18, 0.34), tone(1240, 0.15, 0.32), gap=0.015))
    sounds["powerup_energy"] = ("powerups", concat(tone(440, 0.10, 0.28), tone(660, 0.12, 0.32), tone(880, 0.18, 0.36), gap=0.02))
    sounds["powerup_slow"] = ("powerups", normalize(mix(tone(300, 0.22, 0.35, "sine"), sweep(520, 230, 0.28, 0.25))))
    sounds["powerup_split"] = ("powerups", concat(tone(500, 0.12, 0.30), tone(760, 0.12, 0.30), tone(1040, 0.16, 0.34), gap=0.018))
    sounds["powerup_multi"] = ("powerups", concat(tone(600, 0.08, 0.28), tone(800, 0.08, 0.30), tone(1000, 0.08, 0.32), tone(1300, 0.22, 0.42), gap=0.015))

    sounds["ability_overdrive"] = ("abilities", normalize(mix(sweep(180, 1180, 0.42, 0.38, "saw"), tone(720, 0.22, 0.15))))
    sounds["ability_shield"] = ("abilities", normalize(mix(tone(220, 0.42, 0.32, "sine"), sweep(400, 900, 0.35, 0.20))))
    sounds["ability_wide"] = ("abilities", normalize(mix(sweep(420, 180, 0.25, 0.34), sweep(780, 300, 0.25, 0.22))))
    sounds["ability_denied"] = ("abilities", concat(tone(220, 0.10, 0.35, "square"), tone(160, 0.13, 0.28, "square"), gap=0.02))
    sounds["boss_alert"] = ("boss", concat(tone(110, 0.18, 0.42, "square"), silence(0.06), tone(110, 0.18, 0.42, "square"), sweep(160, 620, 0.42, 0.36)))
    sounds["boss_phase"] = ("boss", normalize(mix(sweep(180, 980, 0.32, 0.34), noise(0.25, 0.10, 0.08))))
    sounds["transition_whoosh"] = ("transitions", normalize(mix(sweep(120, 900, 0.52, 0.32), lowpass(noise(0.50, 0.12, 0.18), 10))))
    sounds["error"] = ("ui", normalize(mix(tone(130, 0.20, 0.34, "saw"), tone(95, 0.24, 0.24, "square"))))
    return sounds


def write_wav(path: Path, signal: np.ndarray) -> None:
    signal = normalize(signal)
    pcm = np.clip(signal * 32767.0, -32768, 32767).astype(np.int16)
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "wb") as stream:
        stream.setnchannels(1)
        stream.setsampwidth(2)
        stream.setframerate(SAMPLE_RATE)
        stream.writeframes(pcm.tobytes())


def main() -> int:
    parser = argparse.ArgumentParser(description="Gera SFX do Game-Ping-Pong")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    sounds = make_sounds()
    metadata: dict[str, object] = {
        "sample_rate": SAMPLE_RATE,
        "channels": 1,
        "format": "PCM_S16LE",
        "looping": False,
        "assets": {},
    }
    for name, (category, signal) in sounds.items():
        target = args.output / "sfx" / category / f"{name}.wav"
        write_wav(target, signal)
        metadata["assets"][name] = {
            "file": str(target.relative_to(ROOT)).replace("\\", "/"),
            "category": category,
            "duration_seconds": round(len(signal) / SAMPLE_RATE, 4),
        }
    metadata_path = args.output / "metadata" / "sfx_catalog.json"
    metadata_path.parent.mkdir(parents=True, exist_ok=True)
    metadata_path.write_text(json.dumps(metadata, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"OK: {len(sounds)} efeitos gerados em {args.output / 'sfx'}")
    print(f"OK: catálogo escrito em {metadata_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
