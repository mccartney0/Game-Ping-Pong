#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
TRAINER="$SCRIPT_DIR/neon_asset_trainer.py"

python3 "$TRAINER" generate \
  --config "$ROOT_DIR/assets/config/neon_assets.json" \
  --output "$ROOT_DIR/assets/generated/neon"
python3 "$TRAINER" preview \
  --input "$ROOT_DIR/assets/generated/neon" \
  --output "$ROOT_DIR/assets/generated/previews/neon_preview.png"
python3 "$TRAINER" validate \
  --input "$ROOT_DIR/assets/generated/neon"

(
  cd "$ROOT_DIR/libgdx-touch"
  ./gradlew packNeonAssets --no-daemon
)

 echo "Assets neon e atlas prontos em $ROOT_DIR/assets/generated"
