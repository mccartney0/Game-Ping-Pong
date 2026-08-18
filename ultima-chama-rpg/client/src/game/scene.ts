/**
 * Gravura de Cinzas — ponto de entrada Babylon seguro e sem dependência de React.
 */
import type { Engine } from "@babylonjs/core/Engines/engine";
import { Scene } from "@babylonjs/core/scene";
import { GameWorld } from "./GameWorld";

export interface GameHandle { scene: Scene; dispose: () => void; }

export async function createGameScene(engine: Engine, canvas: HTMLCanvasElement): Promise<GameHandle> {
  const scene = new Scene(engine); const world = new GameWorld(scene, canvas); await world.initialize();
  return { scene, dispose: () => { world.dispose(); scene.dispose(); } };
}
