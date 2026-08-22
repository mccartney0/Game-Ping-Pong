#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

command -v ffprobe >/dev/null 2>&1 || {
  echo "ffprobe e obrigatorio para validar os assets de audio" >&2
  exit 1
}

count=0
for file in "$ROOT_DIR"/assets/audio/music/*.ogg "$ROOT_DIR"/assets/audio/boss/*.ogg; do
  [ -f "$file" ] || continue
  codec="$(ffprobe -v error -select_streams a:0 -show_entries stream=codec_name -of csv=p=0 "$file")"
  [ "$codec" = "vorbis" ] || { echo "ERRO: $file nao e OGG/Vorbis" >&2; exit 1; }
  count=$((count + 1))
done

while IFS= read -r -d '' file; do
  codec="$(ffprobe -v error -select_streams a:0 -show_entries stream=codec_name -of csv=p=0 "$file")"
  rate="$(ffprobe -v error -select_streams a:0 -show_entries stream=sample_rate -of csv=p=0 "$file")"
  channels="$(ffprobe -v error -select_streams a:0 -show_entries stream=channels -of csv=p=0 "$file")"
  [ "$codec" = "pcm_s16le" ] || { echo "ERRO: $file nao e PCM 16-bit" >&2; exit 1; }
  [ "$rate" = "44100" ] || { echo "ERRO: $file nao esta em 44.1 kHz" >&2; exit 1; }
  [ "$channels" = "1" ] || { echo "ERRO: $file nao e mono" >&2; exit 1; }
  count=$((count + 1))
done < <(find "$ROOT_DIR/assets/audio/sfx" -type f -name '*.wav' -print0 | sort -z)

[ "$count" -gt 0 ] || { echo "ERRO: nenhum asset de audio encontrado" >&2; exit 1; }
echo "OK: $count asset(s) de audio validados"
