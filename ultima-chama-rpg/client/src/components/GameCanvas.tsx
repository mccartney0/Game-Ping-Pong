/**
 * Gravura de Cinzas — React é a moldura; Babylon é o palco integral de Ferrosul.
 */
import { useEffect, useRef } from "react";
import { Engine } from "@babylonjs/core/Engines/engine";
import { createGameScene, type GameHandle } from "@/game/scene";

export default function GameCanvas() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const startedRef = useRef(false);
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || startedRef.current) return;
    startedRef.current = true;
    const engine = new Engine(canvas, true, { preserveDrawingBuffer: true, stencil: true, adaptToDeviceRatio: true });
    let handle: GameHandle | null = null; let disposed = false;
    createGameScene(engine, canvas).then((game) => { if (disposed) { game.dispose(); return; } handle = game; engine.runRenderLoop(() => game.scene.render()); }).catch((error) => { console.error("Não foi possível acender o mundo de Ferrosul.", error); });
    const onResize = () => engine.resize(); window.addEventListener("resize", onResize);
    return () => { disposed = true; window.removeEventListener("resize", onResize); handle?.dispose(); engine.dispose(); startedRef.current = false; };
  }, []);
  return <canvas ref={canvasRef} className="rpg-canvas" aria-label="A Chama do Último Reino: Ferrosul" style={{ touchAction: "none" }} />;
}
