/**
 * Gravura de Cinzas — malhas procedurais legíveis para a party e os inimigos de Ferrosul.
 */
import { Color3 } from "@babylonjs/core/Maths/math.color";
import { Vector3 } from "@babylonjs/core/Maths/math.vector";
import { MeshBuilder } from "@babylonjs/core/Meshes/meshBuilder";
import { StandardMaterial } from "@babylonjs/core/Materials/standardMaterial";
import { TransformNode } from "@babylonjs/core/Meshes/transformNode";
import type { Scene } from "@babylonjs/core/scene";
import type { CharacterDefinition, EnemyDefinition } from "./types";

function material(scene: Scene, name: string, color: string, emissive = 0) {
  const value = Color3.FromHexString(color);
  const result = new StandardMaterial(name, scene);
  result.diffuseColor = value.scale(0.7); result.specularColor = Color3.Black(); result.emissiveColor = value.scale(emissive);
  return result;
}

export class Actor {
  readonly root: TransformNode;
  readonly aura: ReturnType<typeof MeshBuilder.CreateTorus>;
  health: number;
  energy: number;
  velocity = Vector3.Zero();
  attackCooldown = 0;
  dodgeTimer = 0;
  isActive = false;

  constructor(readonly scene: Scene, readonly definition: CharacterDefinition, position: Vector3) {
    this.root = new TransformNode(`actor-${definition.id}`, scene); this.root.position.copyFrom(position);
    const coat = material(scene, `${definition.id}-coat`, definition.color, 0.05);
    const skin = material(scene, `${definition.id}-skin`, "#d0a58d");
    const dark = material(scene, `${definition.id}-gear`, "#1d2426");
    const body = MeshBuilder.CreateCapsule(`${definition.id}-body`, { height: 2.1, radius: 0.34 }, scene); body.parent = this.root; body.position.y = 1.12; body.material = coat;
    const head = MeshBuilder.CreateSphere(`${definition.id}-head`, { diameter: 0.55, segments: 12 }, scene); head.parent = this.root; head.position.y = 2.35; head.material = skin;
    const hair = MeshBuilder.CreateSphere(`${definition.id}-hair`, { diameter: 0.57, segments: 10 }, scene); hair.parent = this.root; hair.position.y = 2.5; hair.scaling.y = 0.52; hair.material = dark;
    const shoulder = MeshBuilder.CreateBox(`${definition.id}-shoulder`, { width: 1.05, height: 0.2, depth: 0.45 }, scene); shoulder.parent = this.root; shoulder.position.y = 1.78; shoulder.material = dark;
    const weapon = MeshBuilder.CreateBox(`${definition.id}-weapon`, { width: definition.id === "dheren" ? 0.1 : 0.06, height: definition.id === "dheren" ? 1.45 : 0.38, depth: 0.08 }, scene); weapon.parent = this.root; weapon.position.set(0.55, 1.2, 0); weapon.rotation.z = -0.35; weapon.material = definition.id === "dheren" ? material(scene, "dheren-blade", definition.accent, 0.45) : coat;
    this.aura = MeshBuilder.CreateTorus(`${definition.id}-aura`, { diameter: 1.05, thickness: 0.035, tessellation: 32 }, scene); this.aura.parent = this.root; this.aura.position.y = 0.08; this.aura.rotation.x = Math.PI / 2; this.aura.material = material(scene, `${definition.id}-aura-material`, definition.accent, 0.8); this.aura.isVisible = false;
    this.health = definition.maxHealth; this.energy = definition.maxEnergy;
  }
  setActive(active: boolean) { this.isActive = active; this.aura.isVisible = active; }
  update(delta: number, moving: boolean) {
    this.attackCooldown = Math.max(0, this.attackCooldown - delta); this.dodgeTimer = Math.max(0, this.dodgeTimer - delta);
    const bob = moving ? Math.sin(performance.now() * 0.012) * 0.035 : 0; this.root.position.y = Math.max(0, bob);
    this.aura.rotation.z += delta * 0.8;
  }
  hurt(amount: number) { this.health = Math.max(0, this.health - amount); return this.health <= 0; }
  heal() { this.health = this.definition.maxHealth; this.energy = this.definition.maxEnergy; }
  dispose() { this.root.dispose(false, true); }
}

export class Enemy {
  readonly root: TransformNode;
  readonly ring: ReturnType<typeof MeshBuilder.CreateTorus>;
  health: number;
  attackTimer = 0;
  hitFlash = 0;
  alive = true;
  private readonly bodyMaterial: StandardMaterial;
  constructor(readonly scene: Scene, readonly definition: EnemyDefinition, position: Vector3) {
    this.root = new TransformNode(`enemy-${definition.id}-${Math.random().toString(36).slice(2)}`, scene); this.root.position.copyFrom(position);
    this.bodyMaterial = material(scene, `${this.root.name}-body`, definition.color, definition.isBoss ? 0.2 : 0.06);
    const body = MeshBuilder.CreateCapsule(`${this.root.name}-body`, { height: definition.isBoss ? 2.9 : 1.8, radius: definition.isBoss ? 0.64 : 0.38 }, scene); body.parent = this.root; body.position.y = definition.isBoss ? 1.45 : 0.9; body.material = this.bodyMaterial;
    const horn = MeshBuilder.CreateCylinder(`${this.root.name}-horn`, { height: definition.isBoss ? 1.2 : 0.5, diameterTop: 0, diameterBottom: 0.28, tessellation: 4 }, scene); horn.parent = this.root; horn.position.y = definition.isBoss ? 3.1 : 1.85; horn.material = material(scene, `${this.root.name}-horn-mat`, "#281f28", 0.1);
    this.ring = MeshBuilder.CreateTorus(`${this.root.name}-ring`, { diameter: definition.isBoss ? 1.8 : 1, thickness: 0.025 }, scene); this.ring.parent = this.root; this.ring.position.y = 0.05; this.ring.rotation.x = Math.PI / 2; this.ring.material = material(scene, `${this.root.name}-ring-mat`, definition.isBoss ? "#ff6759" : "#c77572", 0.7);
    this.health = definition.maxHealth;
  }
  hurt(amount: number) {
    this.health = Math.max(0, this.health - amount); this.hitFlash = 0.18;
    if (this.health === 0) { this.alive = false; this.root.setEnabled(false); }
    return !this.alive;
  }
  update(delta: number) { this.attackTimer = Math.max(0, this.attackTimer - delta); this.hitFlash = Math.max(0, this.hitFlash - delta); this.ring.rotation.z += delta * 1.3; this.bodyMaterial.emissiveColor = Color3.FromHexString(this.definition.color).scale(this.hitFlash > 0 ? 0.8 : this.definition.isBoss ? 0.18 : 0.06); }
  dispose() { this.root.dispose(false, true); }
}

