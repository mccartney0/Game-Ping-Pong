#!/usr/bin/env python3
"""Mock local para testar a API releases/latest e downloads dos três assets."""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import argparse
import hashlib
import json


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


class Handler(BaseHTTPRequestHandler):
    release = {}
    assets = {}

    def do_GET(self):
        if self.path == "/repos/mccartney0/Game-Ping-Pong/releases/latest":
            body = json.dumps(self.release).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/vnd.github+json")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
            return

        prefix = "/assets/"
        if self.path.startswith(prefix):
            name = self.path[len(prefix):]
            path = self.assets.get(name)
            if path and path.is_file():
                self.send_response(200)
                self.send_header("Content-Type", "application/octet-stream")
                self.send_header("Content-Length", str(path.stat().st_size))
                self.end_headers()
                with path.open("rb") as stream:
                    for block in iter(lambda: stream.read(64 * 1024), b""):
                        self.wfile.write(block)
                return

        self.send_error(404)

    def log_message(self, format_string, *args):
        print(format_string % args)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--assets", required=True, type=Path)
    parser.add_argument("--version", default="99.0.0")
    parser.add_argument("--port", default=8787, type=int)
    args = parser.parse_args()

    names = [
        "neon-ping-pong-awt.jar",
        "game-ping-pong-touch-android.apk",
        "game-ping-pong-touch-desktop.zip",
    ]
    assets = {}
    for name in names:
        path = args.assets / name
        if not path.is_file():
            raise SystemExit(f"asset ausente: {path}")
        checksum = args.assets / f"{name}.sha256"
        if not checksum.is_file():
            checksum.write_text(f"{sha256(path)}  {name}\n", encoding="utf-8")
        assets[name] = path
        assets[f"{name}.sha256"] = checksum

    base = f"http://127.0.0.1:{args.port}"
    Handler.assets = assets
    Handler.release = {
        "tag_name": f"v{args.version}",
        "name": f"Mock Release v{args.version}",
        "draft": False,
        "prerelease": False,
        "assets": [
            {
                "name": name,
                "browser_download_url": f"{base}/assets/{name}",
            }
            for name in assets
        ],
    }
    print(f"Mock Release disponível em {base}")
    print(f"Base para o updater: -Dgithub.api.base={base}")
    ThreadingHTTPServer(("127.0.0.1", args.port), Handler).serve_forever()


if __name__ == "__main__":
    main()
