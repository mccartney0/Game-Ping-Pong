/**
 * Gravura de Cinzas — GLBs CC0 com orientação física, fallback legível e estados de animação para party e inimigos.
 */
import "@babylonjs/loaders/glTF";
import { SceneLoader } from "@babylonjs/core/Loading/sceneLoader";
import { Color3 } from "@babylonjs/core/Maths/math.color";
import { Vector3 } from "@babylonjs/core/Maths/math.vector";
import { MeshBuilder } from "@babylonjs/core/Meshes/meshBuilder";
import type { AbstractMesh } from "@babylonjs/core/Meshes/abstractMesh";
import { StandardMaterial } from "@babylonjs/core/Materials/standardMaterial";
import { TransformNode } from "@babylonjs/core/Meshes/transformNode";
import type { AnimationGroup } from "@babylonjs/core/Animations/animationGroup";
import type { Scene } from "@babylonjs/core/scene";
import type { CharacterDefinition, EnemyDefinition, EquipmentBonuses } from "./types";

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
  maxHealth: number;
  maxEnergy: number;
  speedBonus = 0;
  velocity = Vector3.Zero();
  attackCooldown = 0;
  dodgeTimer = 0;
  isActive = false;
  private fallbackMeshes: AbstractMesh[] = [];
  private animations: AnimationGroup[] = [];
  private currentAnimation: AnimationGroup | null = null;

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
    this.fallbackMeshes = [body, head, hair, shoulder, weapon];
    this.aura = MeshBuilder.CreateTorus(`${definition.id}-aura`, { diameter: 1.05, thickness: 0.035, tessellation: 32 }, scene); this.aura.parent = this.root; this.aura.position.y = 0.08; this.aura.rotation.x = Math.PI / 2; this.aura.material = material(scene, `${definition.id}-aura-material`, definition.accent, 0.8); this.aura.isVisible = false;
    this.maxHealth = definition.maxHealth; this.maxEnergy = definition.maxEnergy; this.health = definition.maxHealth; this.energy = definition.maxEnergy;
  }
  async loadVisual() {
    const model = this.definition.model;
    if (!model) return;
    try {
      const result = await SceneLoader.ImportMeshAsync("", "", model.url, this.scene);
      const visualRoot = new TransformNode(`${this.definition.id}-glb-root`, this.scene);
      visualRoot.parent = this.root; visualRoot.rotation.y = model.yaw ?? 0; visualRoot.scaling.setAll(model.scale);
      result.meshes.filter((mesh) => !mesh.parent).forEach((mesh) => { mesh.parent = visualRoot; });
      result.meshes.forEach((mesh, index) => {
        const palette = this.definition.id === "dheren" ? ["#9c7335", "#4d3925", "#d7b45b"] : this.definition.id === "lyra" ? ["#4d7140", "#213727", "#9fcf79"] : this.definition.id === "mira" ? ["#50355e", "#19161e", "#c993df"] : ["#315d76", "#182a32", "#5f9fc1"];
        const visualMaterial = material(this.scene, `${this.definition.id}-glb-${index}`, palette[index % palette.length], index % 3 === 2 ? 0.06 : 0.02);
        mesh.material = visualMaterial;
      });
      this.animations = result.animationGroups;
      this.fallbackMeshes.forEach((mesh) => mesh.setEnabled(false));
      this.playVisualAnimation("idle");
    } catch (error) {
      console.warn(`Não foi possível carregar o GLB de ${this.definition.name}.`, error);
    }
  }
  private playVisualAnimation(state: "idle" | "move" | "attack" | "special" | "hurt" | "death") {
    const requested = this.definition.model?.animations?.[state]; const intent = state === "move" ? ["walk", "run"] : state === "attack" ? ["attack", "punch", "slash", "shot"] : state === "special" ? ["rain", "special", "burst"] : state === "hurt" ? ["hurt", "hit", "damage"] : state === "death" ? ["death", "die", "dead"] : ["idle", "stand"];
    const next = (requested ? this.animations.find((group) => group.name === requested) : undefined) ?? this.animations.find((group) => intent.some((keyword) => group.name.toLowerCase().includes(keyword))) ?? this.animations[0] ?? null;
    if (!next || next === this.currentAnimation) return;
    this.currentAnimation?.stop(); this.currentAnimation = next; next.start(state !== "attack" && state !== "special" && state !== "hurt" && state !== "death", 1, next.from, next.to, false);
  }
  triggerAnimation(state: "attack" | "special" | "hurt" | "death") { this.playVisualAnimation(state); }
  setActive(active: boolean) { this.isActive = active; this.aura.isVisible = active; }
  update(delta: number, moving: boolean) {
    this.attackCooldown = Math.max(0, this.attackCooldown - delta); this.dodgeTimer = Math.max(0, this.dodgeTimer - delta);
    const bob = moving ? Math.sin(performance.now() * 0.012) * 0.035 : 0; this.root.position.y = Math.max(0, bob);
    this.aura.rotation.z += delta * 0.8; this.playVisualAnimation(moving ? "move" : "idle");
  }
  hurt(amount: number) { this.health = Math.max(0, this.health - amount); this.triggerAnimation(this.health <= 0 ? "death" : "hurt"); return this.health <= 0; }
  setEquipmentBonuses(bonuses: EquipmentBonuses) { const previousHealth = this.maxHealth; const previousEnergy = this.maxEnergy; this.maxHealth = this.definition.maxHealth + (bonuses.health ?? 0); this.maxEnergy = this.definition.maxEnergy + (bonuses.energy ?? 0); this.speedBonus = bonuses.speed ?? 0; this.health = Math.min(this.maxHealth, Math.max(1, this.health + this.maxHealth - previousHealth)); this.energy = Math.min(this.maxEnergy, Math.max(0, this.energy + this.maxEnergy - previousEnergy)); }
  getSpeed() { return this.definition.speed + this.speedBonus; }
  heal() { this.health = this.maxHealth; this.energy = this.maxEnergy; }
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
  private fallbackMeshes: AbstractMesh[] = [];
  private animations: AnimationGroup[] = [];
  private currentAnimation: AnimationGroup | null = null;
  constructor(readonly scene: Scene, readonly definition: EnemyDefinition, position: Vector3) {
    this.root = new TransformNode(`enemy-${definition.id}-${Math.random().toString(36).slice(2)}`, scene); this.root.position.copyFrom(position);
    this.bodyMaterial = material(scene, `${this.root.name}-body`, definition.color, definition.isBoss ? 0.2 : 0.06);
    const body = MeshBuilder.CreateCapsule(`${this.root.name}-body`, { height: definition.isBoss ? 2.9 : 1.8, radius: definition.isBoss ? 0.64 : 0.38 }, scene); body.parent = this.root; body.position.y = definition.isBoss ? 1.45 : 0.9; body.material = this.bodyMaterial;
    const horn = MeshBuilder.CreateCylinder(`${this.root.name}-horn`, { height: definition.isBoss ? 1.2 : 0.5, diameterTop: 0, diameterBottom: 0.28, tessellation: 4 }, scene); horn.parent = this.root; horn.position.y = definition.isBoss ? 3.1 : 1.85; horn.material = material(scene, `${this.root.name}-horn-mat`, "#281f28", 0.1);
    const mask = MeshBuilder.CreateBox(`${this.root.name}-mask`, { width: definition.isBoss ? .48 : .3, height: definition.isBoss ? .62 : .3, depth: .1 }, scene); mask.parent = this.root; mask.position.set(0, definition.isBoss ? 2.3 : 1.4, definition.isBoss ? .68 : .4); mask.material = material(scene, `${this.root.name}-mask-mat`, definition.id === "masked-crown" ? "#ded6c8" : "#45353c", definition.id === "masked-crown" ? .12 : 0.03); mask.setEnabled(definition.id === "masked-crown");
    this.ring = MeshBuilder.CreateTorus(`${this.root.name}-ring`, { diameter: definition.isBoss ? 1.8 : 1, thickness: 0.025 }, scene); this.ring.parent = this.root; this.ring.position.y = 0.05; this.ring.rotation.x = Math.PI / 2; this.ring.material = material(scene, `${this.root.name}-ring-mat`, definition.isBoss ? "#ff6759" : "#c77572", 0.7);
    this.fallbackMeshes = [body, horn, mask]; this.health = definition.maxHealth;
  }
  async loadVisual() {
    const model = this.definition.model;
    if (!model) return;
    try {
      const result = await SceneLoader.ImportMeshAsync("", "", model.url, this.scene);
      const visualRoot = new TransformNode(`${this.root.name}-glb-root`, this.scene); visualRoot.parent = this.root; visualRoot.rotation.y = model.yaw ?? 0; visualRoot.position.y = model.offsetY ?? 0; visualRoot.scaling.setAll(model.scale);
      result.meshes.filter((mesh) => !mesh.parent).forEach((mesh) => { mesh.parent = visualRoot; });
      result.meshes.forEach((mesh, index) => { mesh.material = material(this.scene, `${this.root.name}-glb-${index}`, index % 3 === 0 ? "#6f2528" : index % 3 === 1 ? "#35171b" : "#bf4b45", index % 3 === 2 ? 0.12 : 0.025); });
      this.animations = result.animationGroups; this.fallbackMeshes.forEach((mesh) => mesh.setEnabled(false)); this.play("idle");
    } catch (error) { console.warn(`Não foi possível carregar o GLB de ${this.definition.name}.`, error); }
  }
  private play(state: "idle" | "move" | "attack" | "hurt" | "death") {
    const keywords = state === "move" ? ["walk", "run"] : state === "attack" ? ["attack", "punch", "bite", "slash"] : state === "hurt" ? ["hurt", "hit", "damage"] : state === "death" ? ["death", "die", "dead"] : ["idle", "stand"];
    const next = this.animations.find((group) => keywords.some((keyword) => group.name.toLowerCase().includes(keyword))) ?? this.animations[0] ?? null;
    if (!next || next === this.currentAnimation) return;
    this.currentAnimation?.stop(); this.currentAnimation = next; next.start(state !== "attack" && state !== "hurt" && state !== "death", 1, next.from, next.to, false);
  }
  triggerAnimation(state: "attack" | "hurt" | "death") { this.play(state); }
  hurt(amount: number) {
    this.health = Math.max(0, this.health - amount); this.hitFlash = 0.18;
    if (this.health === 0) { this.alive = false; this.triggerAnimation("death"); window.setTimeout(() => this.root.setEnabled(false), 900); } else this.triggerAnimation("hurt");
    return !this.alive;
  }
  update(delta: number, moving = false) { this.attackTimer = Math.max(0, this.attackTimer - delta); this.hitFlash = Math.max(0, this.hitFlash - delta); this.ring.rotation.z += delta * 1.3; this.bodyMaterial.emissiveColor = Color3.FromHexString(this.definition.color).scale(this.hitFlash > 0 ? 0.8 : this.definition.isBoss ? 0.18 : 0.06); this.play(moving ? "move" : "idle"); }
  dispose() { this.root.dispose(false, true); }
}
