/**
 * Gravura de Cinzas — Ferrosul como palco principal: madeira, pedra, fogo, magia azul e ouro ritual.
 */
import { ArcRotateCamera } from "@babylonjs/core/Cameras/arcRotateCamera";
import { Color3, Color4 } from "@babylonjs/core/Maths/math.color";
import { Vector3 } from "@babylonjs/core/Maths/math.vector";
import { HemisphericLight } from "@babylonjs/core/Lights/hemisphericLight";
import { DirectionalLight } from "@babylonjs/core/Lights/directionalLight";
import { PointLight } from "@babylonjs/core/Lights/pointLight";
import { Mesh } from "@babylonjs/core/Meshes/mesh";
import { MeshBuilder } from "@babylonjs/core/Meshes/meshBuilder";
import { StandardMaterial } from "@babylonjs/core/Materials/standardMaterial";
import { GlowLayer } from "@babylonjs/core/Layers/glowLayer";
import { Scene } from "@babylonjs/core/scene";
import { AudioManager } from "./AudioManager";
import { Actor, Enemy } from "./actors";
import { CHARACTERS, ENEMIES, INITIAL_DIALOGUE, QUESTS } from "./data";
import { InputManager } from "./InputManager";
import { defaultSave, SaveManager } from "./SaveManager";
import type { CharacterId, NarrativeStage, SaveData } from "./types";
import { UIController } from "./UIController";

interface Interactable { id: "forge" | "wood" | "tavern" | "exit"; position: Vector3; radius: number; mesh: Mesh; }
interface Projectile { mesh: Mesh; velocity: Vector3; damage: number; life: number; color: string; }

const clamp = (value: number, min: number, max: number) => Math.max(min, Math.min(max, value));

export class GameWorld {
  private readonly input: InputManager;
  private readonly audio = new AudioManager();
  private readonly saves = new SaveManager();
  private readonly ui: UIController;
  private readonly camera: ArcRotateCamera;
  private readonly actors: Record<"kael" | "dheren", Actor>;
  private enemies: Enemy[] = [];
  private projectiles: Projectile[] = [];
  private interactables: Interactable[] = [];
  private fireMeshes: Mesh[] = [];
  private villageMaterials: StandardMaterial[] = [];
  private active: CharacterId = "kael";
  private stage: NarrativeStage = "title";
  private saveData: SaveData = defaultSave();
  private magicControl = 20;
  private magicInstability = 0;
  private trainingCasts = 0;
  private attackStarted = false;
  private saveTimer = 0;
  private demoTime = 0;
  private autoDemo = new URLSearchParams(window.location.search).has("demo");
  private lockTarget: Enemy | null = null;
  private parryWindow = 0;
  private readonly forge = new Vector3(-12, 0, 6);
  private readonly tavern = new Vector3(8, 0, -5);
  private readonly clearing = new Vector3(-10, 0, -12);
  private readonly wood = new Vector3(20, 0, 17);
  private readonly exit = new Vector3(29, 0, 27);

  constructor(private readonly scene: Scene, canvas: HTMLCanvasElement) {
    scene.clearColor = new Color4(0.037, 0.07, 0.085, 1);
    scene.fogMode = Scene.FOGMODE_EXP2; scene.fogDensity = 0.017; scene.fogColor = new Color3(0.06, 0.11, 0.12);
    this.camera = new ArcRotateCamera("ferrosul-camera", -2.15, 1.08, 14, new Vector3(-7, 1.5, -9), scene);
    this.camera.lowerBetaLimit = 0.55; this.camera.upperBetaLimit = 1.38; this.camera.lowerRadiusLimit = 7; this.camera.upperRadiusLimit = 17; this.camera.wheelPrecision = 50; scene.activeCamera = this.camera;
    this.input = new InputManager(canvas);
    this.ui = new UIController({ startNewGame: () => this.startNewGame(), continueGame: () => void this.continueGame(), saveGame: () => void this.saveGame(), changeDifficulty: (difficulty) => { this.saveData.settings.difficulty = difficulty; this.ui.notify(`Dificuldade: ${difficulty}`); }, debugAction: (action) => this.debug(action) });
    this.createLighting(); this.createFerrosul();
    this.actors = { kael: new Actor(scene, CHARACTERS.kael, this.clearing.add(new Vector3(0, 0, 1))), dheren: new Actor(scene, CHARACTERS.dheren, this.clearing.add(new Vector3(2.3, 0, -1))) };
    this.actors.kael.setActive(true); this.createInteractables(); this.createAmbientRunes();
    new GlowLayer("ember-glow", scene, { blurKernelSize: 32 }).intensity = 0.65;
  }
  async initialize() {
    const recovered = await this.saves.load();
    if (recovered) this.saveData = recovered;
    if (this.autoDemo) this.startDemo();
    else this.ui.showTitle();
    this.scene.onBeforeRenderObservable.add(() => this.update(this.scene.getEngine().getDeltaTime() / 1000));
  }
  private createLighting() {
    const sky = new HemisphericLight("cold-sky", new Vector3(0.2, 1, 0.1), this.scene); sky.intensity = 1.25; sky.diffuse = Color3.FromHexString("#9ed4dc"); sky.groundColor = Color3.FromHexString("#17201e");
    const sun = new DirectionalLight("distant-sun", new Vector3(-0.4, -1, 0.25), this.scene); sun.position = new Vector3(20, 35, -20); sun.intensity = 1.45; sun.diffuse = Color3.FromHexString("#ffd3a0");
    const forgeLight = new PointLight("forge-amber", this.forge.add(new Vector3(0, 3.3, 0)), this.scene); forgeLight.diffuse = Color3.FromHexString("#ff9b45"); forgeLight.intensity = 4.2; forgeLight.range = 22;
    const tavernLight = new PointLight("tavern-amber", this.tavern.add(new Vector3(0, 3, 0)), this.scene); tavernLight.diffuse = Color3.FromHexString("#e8b96c"); tavernLight.intensity = 2.4; tavernLight.range = 14;
  }
  private material(name: string, color: string, emissive = 0) {
    const value = Color3.FromHexString(color); const result = new StandardMaterial(name, this.scene); result.diffuseColor = value.scale(0.72); result.specularColor = Color3.Black(); result.emissiveColor = value.scale(emissive); this.villageMaterials.push(result); return result;
  }
  private createFerrosul() {
    const ground = MeshBuilder.CreateGround("ferrosul-ground", { width: 90, height: 90, subdivisions: 2 }, this.scene); ground.material = this.material("peat-and-stone", "#294339");
    const road = MeshBuilder.CreateGround("old-road", { width: 13, height: 72 }, this.scene); road.position.z = 0; road.rotation.y = 0.22; road.material = this.material("cobble-road", "#4c5149");
    const square = MeshBuilder.CreateCylinder("village-square", { diameter: 17, height: 0.12, tessellation: 24 }, this.scene); square.position.y = 0.06; square.material = this.material("square-stone", "#5c6259");
    for (let index = 0; index < 23; index += 1) {
      const angle = index * 2.399; const radius = 26 + (index % 4) * 3; const tree = MeshBuilder.CreateCylinder(`pine-${index}`, { height: 4.3 + (index % 3), diameterTop: 0.22, diameterBottom: 0.55, tessellation: 6 }, this.scene); tree.position.set(Math.cos(angle) * radius, 2, Math.sin(angle) * radius); tree.material = this.material(`pine-trunk-${index}`, "#3b2d20");
      const crown = MeshBuilder.CreateCylinder(`pine-crown-${index}`, { height: 7 + (index % 2), diameterTop: 0, diameterBottom: 4.5, tessellation: 8 }, this.scene); crown.position.copyFrom(tree.position); crown.position.y += 4.3; crown.material = this.material(`pine-crown-mat-${index}`, index % 2 ? "#1d4339" : "#254d40");
    }
    this.createHouse("forge", this.forge, 7.5, 5.5, "#5b4533", "#352727", "FERRARIA");
    this.createHouse("red-fox", this.tavern, 8.8, 6.4, "#644934", "#422c29", "RAPOSA VERMELHA");
    this.createHouse("kael-home", new Vector3(-1, 0, 9), 5.2, 5, "#70513b", "#4c3430", "CASA DE KAEL");
    this.createHouse("chapel", new Vector3(0, 0, -14), 5.6, 7, "#676256", "#3b3d38", "CAPELA");
    this.createHouse("home-east", new Vector3(13, 0, 7), 5.5, 4.5, "#72543d", "#44312b", "");
    this.createHouse("home-west", new Vector3(-18, 0, -3), 5.5, 4.5, "#5d4637", "#39302c", "");
    const forge = MeshBuilder.CreateBox("forge-glow", { width: 2.4, height: 1.7, depth: 1 }, this.scene); forge.position.copyFrom(this.forge.add(new Vector3(-2.3, 0.85, 0.8))); forge.material = this.material("forge-flame", "#ff7b35", 1.1);
    const bell = MeshBuilder.CreateCylinder("ferrosul-bell", { height: 1.1, diameterTop: 0.5, diameterBottom: 1.15, tessellation: 12 }, this.scene); bell.position.set(1.5, 4.3, -2); bell.material = this.material("old-bell", "#c69753", 0.12);
    const arch = MeshBuilder.CreateBox("village-arch", { width: 4.5, height: 4.5, depth: 1.1 }, this.scene); arch.position.set(22, 2.25, 21); arch.material = this.material("forest-gate", "#514737");
  }
  private createHouse(name: string, position: Vector3, width: number, depth: number, wall: string, roofColor: string, label: string) {
    const walls = MeshBuilder.CreateBox(`${name}-walls`, { width, height: 3.8, depth }, this.scene); walls.position.copyFrom(position); walls.position.y = 1.9; walls.material = this.material(`${name}-walls-mat`, wall);
    const roof = MeshBuilder.CreateCylinder(`${name}-roof`, { height: depth * 0.9, diameterTop: 0, diameterBottom: width * 1.5, tessellation: 3 }, this.scene); roof.position.copyFrom(position); roof.position.y = 5.1; roof.rotation.z = Math.PI / 2; roof.material = this.material(`${name}-roof-mat`, roofColor);
    const door = MeshBuilder.CreateBox(`${name}-door`, { width: 1.25, height: 2.15, depth: 0.14 }, this.scene); door.position.copyFrom(position); door.position.y = 1.08; door.position.z -= depth / 2 + 0.08; door.material = this.material(`${name}-door-mat`, "#251c18");
    if (label) { const sign = MeshBuilder.CreateBox(`${name}-sign`, { width: Math.min(width - 1, 4), height: 0.45, depth: 0.1 }, this.scene); sign.position.copyFrom(door.position); sign.position.y = 3.05; sign.position.z -= 0.12; sign.material = this.material(`${name}-sign-mat`, "#d6ad74", 0.2); }
  }
  private createInteractables() {
    const makeMarker = (id: Interactable["id"], position: Vector3, color: string, visible: boolean) => {
      const mesh = MeshBuilder.CreateTorus(`${id}-marker`, { diameter: 1.35, thickness: 0.05, tessellation: 24 }, this.scene); mesh.position.copyFrom(position); mesh.position.y = 0.18; mesh.rotation.x = Math.PI / 2; mesh.material = this.material(`${id}-marker-mat`, color, 0.9); mesh.setEnabled(visible); this.interactables.push({ id, position, radius: 3, mesh });
    };
    makeMarker("forge", this.forge.add(new Vector3(0, 0, -3.7)), "#ffca75", false); makeMarker("wood", this.wood, "#62c9ff", false); makeMarker("tavern", this.tavern.add(new Vector3(0, 0, -4.5)), "#f0bd69", false); makeMarker("exit", this.exit, "#d7efff", false);
    const log = MeshBuilder.CreateCylinder("darion-wood", { height: 2.8, diameter: 0.46, tessellation: 8 }, this.scene); log.position.copyFrom(this.wood); log.position.y = 0.3; log.rotation.z = Math.PI / 2; log.material = this.material("darion-wood-material", "#6c4b2e"); log.setEnabled(false); this.interactables.find((entry) => entry.id === "wood")!.mesh.metadata = { prop: log };
  }
  private createAmbientRunes() {
    for (let index = 0; index < 8; index += 1) { const rune = MeshBuilder.CreateTorus(`old-rune-${index}`, { diameter: 0.45 + index * 0.05, thickness: 0.018, tessellation: 6 }, this.scene); rune.position.set(-11 + (index % 4) * 1.2, 0.05, -10 + Math.floor(index / 4) * 1.1); rune.rotation.x = Math.PI / 2; rune.material = this.material(`rune-mat-${index}`, "#4a7990", 0.23); }
  }
  private activeActor() { return this.active === "dheren" ? this.actors.dheren : this.actors.kael; }
  private setActive(id: CharacterId) {
    if (id === "lyra" || id === "mira") { this.ui.notify(`${id === "lyra" ? "Lyra" : "Mira"} ainda não se juntou à jornada.`); return; }
    if (id === "dheren" && !this.attackStarted) { this.ui.notify("Dheren fica ao seu lado. A troca será necessária quando Ferrosul pedir sua lâmina."); return; }
    this.active = id; this.actors.kael.setActive(id === "kael"); this.actors.dheren.setActive(id === "dheren"); this.ui.notify(`${this.activeActor().definition.name} assume a dianteira.`);
  }
  startNewGame() {
    this.stage = "training"; this.saveData = defaultSave(); this.magicControl = 20; this.magicInstability = 0; this.attackStarted = false; this.active = "kael"; this.actors.kael.root.position.copyFrom(this.clearing.add(new Vector3(0, 0, 1))); this.actors.dheren.root.position.copyFrom(this.clearing.add(new Vector3(2.3, 0, -1))); this.actors.kael.heal(); this.actors.dheren.heal(); this.actors.kael.setActive(true); this.actors.dheren.setActive(false); this.ui.beginGame(); void this.audio.unlock(); this.ui.dialogue(INITIAL_DIALOGUE[0].speaker, INITIAL_DIALOGUE[0].text); this.ui.notify("Prólogo: 800 anos antes, um rei foi selado. Agora a cinza encontra o seu caminho.");
  }
  private async continueGame() { const saved = await this.saves.load(); this.startNewGame(); if (!saved || saved.narrativeStage === "title") return; this.ui.notify("A crônica foi retomada no início deste vertical slice."); }
  private startAttack(fromDebug = false) {
    if (this.attackStarted) return;
    this.attackStarted = true; this.stage = "attack"; this.active = "dheren"; this.actors.kael.setActive(false); this.actors.dheren.setActive(true); this.actors.dheren.root.position.set(2, 0, 1); this.actors.kael.root.position.set(-0.8, 0, 2.8); this.setVillageOnFire();
    this.spawnEnemy("blood-tracker", new Vector3(1, 0, -7)); this.spawnEnemy("ash-soldier", new Vector3(6, 0, 1)); this.spawnEnemy("ashling", new Vector3(-5, 0, 0)); this.spawnEnemy("ashling", new Vector3(7, 0, -4));
    this.ui.dialogue("Dheren", "Fica atrás de mim, Kael. Pela primeira vez, tenta não queimar a vila inteira."); this.ui.notify(fromDebug ? "Vigília: ataque de Ferrosul ativado." : "O sino toca. Ferrosul está em chamas.");
  }
  private setVillageOnFire() {
    this.scene.clearColor = new Color4(0.16, 0.035, 0.035, 1); this.scene.fogColor = Color3.FromHexString("#421f20"); this.scene.fogDensity = 0.03; this.villageMaterials.forEach((mat, index) => { mat.diffuseColor = mat.diffuseColor.scale(index % 3 ? 0.62 : 0.85); });
    const spots = [new Vector3(-7, 0.7, 4), new Vector3(10, 0.7, -2), new Vector3(-2, 0.7, 8), new Vector3(-12, 0.7, 0), new Vector3(4, 0.7, -9)];
    spots.forEach((position, index) => { const fire = MeshBuilder.CreateSphere(`fire-${index}`, { diameter: 1.1 + (index % 2) * 0.4, segments: 8 }, this.scene); fire.position.copyFrom(position); fire.material = this.material(`fire-mat-${index}`, index % 2 ? "#ff7545" : "#ffba57", 1.4); this.fireMeshes.push(fire); const light = new PointLight(`fire-light-${index}`, position.add(new Vector3(0, 1, 0)), this.scene); light.diffuse = Color3.FromHexString("#ff6046"); light.intensity = 2.3; light.range = 10; });
  }
  private spawnEnemy(id: keyof typeof ENEMIES, position: Vector3) { const enemy = new Enemy(this.scene, ENEMIES[id], position); this.enemies.push(enemy); return enemy; }
  private update(delta: number) {
    if (this.stage === "title") return;
    this.saveTimer += delta; this.magicInstability = Math.max(0, this.magicInstability - delta * 2.2); this.parryWindow = Math.max(0, this.parryWindow - delta);
    if (this.saveTimer > 25) { this.saveTimer = 0; void this.saveGame(true); }
    if (this.autoDemo) this.updateDemo(delta); else this.handleInput(delta);
    this.updateCompanion(delta); this.updateProjectiles(delta); this.updateEnemies(delta); this.updateCamera(delta); this.updateInteractions(); this.updateHud();
    if (this.stage === "attack" && this.enemies.length > 0 && this.enemies.every((enemy) => !enemy.alive)) { this.stage = "escape"; this.interactable("exit")?.mesh.setEnabled(true); this.ui.dialogue("Kael", "A floresta. Agora."); this.ui.notify("O caminho para Elwen está aberto."); }
  }
  private handleInput(delta: number) {
    if (this.input.consume("debug")) this.ui.toggleDebug(); if (this.input.consume("menu")) this.ui.toggle("pause-panel");
    if (this.input.consume("kael")) this.setActive("kael"); if (this.input.consume("dheren")) this.setActive("dheren"); if (this.input.consume("lyra")) this.setActive("lyra"); if (this.input.consume("mira")) this.setActive("mira");
    if (this.input.consume("lock")) this.toggleLock(); if (this.input.consume("interact")) this.interact(); if (this.input.consume("attack")) this.primaryAttack(); if (this.input.consume("special")) this.special(); if (this.input.consume("ability1")) this.abilityOne(); if (this.input.consume("ability2")) this.abilityTwo(); if (this.input.consume("ultimate")) this.ui.notify("A habilidade especial ainda é uma página selada.");
    const look = this.input.takeLookDelta(); this.camera.alpha -= look.x * 0.004; this.camera.beta = clamp(this.camera.beta + look.y * 0.0035, this.camera.lowerBetaLimit!, this.camera.upperBetaLimit!);
    const actor = this.activeActor(); const forward = actor.root.position.subtract(this.camera.position); forward.y = 0; forward.normalize(); const right = Vector3.Cross(forward, Vector3.Up()).normalize(); let direction = Vector3.Zero();
    if (this.input.isDown("forward")) direction.addInPlace(forward); if (this.input.isDown("back")) direction.subtractInPlace(forward); if (this.input.isDown("right")) direction.addInPlace(right); if (this.input.isDown("left")) direction.subtractInPlace(right);
    if (direction.lengthSquared() > 0) { direction.normalize(); const speed = actor.definition.speed * (this.input.isDown("sprint") ? 1.55 : 1) * (actor.dodgeTimer > 0 ? 1.8 : 1); actor.root.position.addInPlace(direction.scale(speed * delta)); actor.root.position.x = clamp(actor.root.position.x, -33, 33); actor.root.position.z = clamp(actor.root.position.z, -33, 33); actor.root.rotation.y = Math.atan2(direction.x, direction.z); actor.update(delta, true); } else actor.update(delta, false);
    if (this.input.consume("dodge")) { actor.dodgeTimer = 0.32; actor.root.position.addInPlace(forward.scale(2.8)); this.audio.tone(200, 0.12, "triangle", 0.05); this.ui.notify("Esquiva precisa."); }
  }
  private updateDemo(delta: number) {
    this.demoTime += delta; if (this.demoTime < 0.2) return; if (!this.attackStarted) this.startAttack(true); const actor = this.activeActor(); const closest = this.nearestEnemy(actor.root.position); if (closest) { const direction = closest.root.position.subtract(actor.root.position); direction.y = 0; if (direction.length() > 3) { direction.normalize(); actor.root.position.addInPlace(direction.scale(actor.definition.speed * 0.68 * delta)); actor.root.rotation.y = Math.atan2(direction.x, direction.z); } if (this.demoTime > 6 && Math.floor(this.demoTime * 1.5) !== Math.floor((this.demoTime - delta) * 1.5)) this.primaryAttack(); }
    this.camera.alpha += delta * 0.08; actor.update(delta, true);
  }
  private updateCamera(delta: number) {
    const focus = this.activeActor().root.position.add(new Vector3(0, 1.5, 0)); this.camera.setTarget(Vector3.Lerp(this.camera.target, focus, Math.min(1, delta * 7))); if (this.lockTarget?.alive) this.camera.setTarget(Vector3.Lerp(focus, this.lockTarget.root.position.add(new Vector3(0, 1.1, 0)), 0.28));
  }
  private primaryAttack() { const actor = this.activeActor(); if (actor.attackCooldown > 0) return; actor.attackCooldown = actor.definition.id === "dheren" ? 0.52 : 0.33; if (this.active === "kael") this.fireProjectile("#62c9ff", 17, 11); else this.meleeStrike(3.2, 27, "#e8b951"); }
  private abilityOne() { const actor = this.activeActor(); if (actor.attackCooldown > 0) return; actor.attackCooldown = 0.8; if (this.active === "kael") { this.fireProjectile("#9ce1ff", 29, 14); this.trainingCasts += this.stage === "training" ? 1 : 0; this.magicInstability = clamp(this.magicInstability + 12, 0, 100); this.audio.tone(480, 0.22, "sine", 0.06); if (this.magicInstability > 48) this.fireProjectile("#5ebeff", 12, 9, 0.22); if (this.stage === "training" && this.trainingCasts >= 2) this.completeTraining(); } else { this.meleeStrike(4, 48, "#fff0ae"); this.audio.tone(380, 0.18, "triangle", 0.06); } }
  private abilityTwo() { const actor = this.activeActor(); if (actor.attackCooldown > 0) return; actor.attackCooldown = 1.15; if (this.active === "kael") { const origin = actor.root.position; this.enemies.filter((enemy) => enemy.alive && Vector3.Distance(enemy.root.position, origin) < 5.8).forEach((enemy) => { enemy.hurt(31); const push = enemy.root.position.subtract(origin).normalize(); enemy.root.position.addInPlace(push.scale(3.1)); }); this.magicInstability = clamp(this.magicInstability + 18, 0, 100); this.spawnPulse(origin, "#62c9ff", 4.2); this.audio.tone(240, 0.33, "sawtooth", 0.045); } else { const forward = new Vector3(Math.sin(actor.root.rotation.y), 0, Math.cos(actor.root.rotation.y)); actor.root.position.addInPlace(forward.scale(5.8)); this.meleeStrike(3.6, 36, "#f3d06f"); this.spawnPulse(actor.root.position, "#e8b951", 2.2); this.audio.tone(630, 0.15, "triangle", 0.05); } }
  private special() { if (this.active === "dheren") { this.parryWindow = 0.55; this.ui.notify("Contra-ataque preparado."); } else { this.ui.notify("Kael ergue um escudo instável por um instante."); this.spawnPulse(this.activeActor().root.position, "#7fd8ff", 1.5); } }
  private fireProjectile(color: string, damage: number, speed: number, lateral = 0) { const actor = this.activeActor(); const forward = actor.root.position.subtract(this.camera.position); forward.y = 0; forward.normalize(); const right = Vector3.Cross(forward, Vector3.Up()); const mesh = MeshBuilder.CreateSphere("blue-spark", { diameter: 0.34, segments: 8 }, this.scene); mesh.position.copyFrom(actor.root.position.add(new Vector3(0, 1.35, 0)).add(forward.scale(0.7)).add(right.scale(lateral))); mesh.material = this.material(`spark-${performance.now()}`, color, 1.8); this.projectiles.push({ mesh, velocity: forward.scale(speed).add(right.scale(lateral * 2)), damage, life: 2, color }); this.audio.tone(530, 0.1, "sine", 0.035); }
  private meleeStrike(range: number, damage: number, color: string) { const actor = this.activeActor(); const hit = this.enemies.filter((enemy) => enemy.alive && Vector3.Distance(enemy.root.position, actor.root.position) < range); hit.forEach((enemy) => this.damageEnemy(enemy, damage)); this.spawnPulse(actor.root.position.add(new Vector3(0, 1.1, 0)), color, range * 0.55); this.audio.tone(180, 0.12, "square", 0.045); }
  private spawnPulse(origin: Vector3, color: string, size: number) { const pulse = MeshBuilder.CreateTorus("arcane-pulse", { diameter: 0.7, thickness: 0.07, tessellation: 32 }, this.scene); pulse.position.copyFrom(origin); pulse.position.y = Math.max(0.2, origin.y + 0.1); pulse.rotation.x = Math.PI / 2; pulse.material = this.material(`pulse-${performance.now()}`, color, 1.5); let time = 0; const observer = this.scene.onBeforeRenderObservable.add(() => { time += this.scene.getEngine().getDeltaTime() / 1000; pulse.scaling.setAll(1 + time * size); pulse.visibility = Math.max(0, 1 - time * 2.8); if (time > 0.42) { this.scene.onBeforeRenderObservable.remove(observer); pulse.dispose(); } }); }
  private updateProjectiles(delta: number) { this.projectiles = this.projectiles.filter((projectile) => { projectile.life -= delta; projectile.mesh.position.addInPlace(projectile.velocity.scale(delta)); projectile.mesh.scaling.setAll(1 + Math.sin(performance.now() * 0.016) * 0.12); const hit = this.enemies.find((enemy) => enemy.alive && Vector3.Distance(enemy.root.position.add(new Vector3(0, 1.1, 0)), projectile.mesh.position) < (enemy.definition.isBoss ? 1.05 : 0.72)); if (hit) { this.damageEnemy(hit, projectile.damage); projectile.life = 0; } if (projectile.life <= 0) { projectile.mesh.dispose(); return false; } return true; }); }
  private updateEnemies(delta: number) { const actor = this.activeActor(); this.enemies.forEach((enemy) => { if (!enemy.alive) return; enemy.update(delta); const direction = actor.root.position.subtract(enemy.root.position); direction.y = 0; const distance = direction.length(); if (distance < 15 && distance > 1.35) { direction.normalize(); enemy.root.position.addInPlace(direction.scale(enemy.definition.speed * delta)); enemy.root.rotation.y = Math.atan2(direction.x, direction.z); } else if (distance <= 1.8 && enemy.attackTimer <= 0) { enemy.attackTimer = 1.1; const damage = this.parryWindow > 0 && this.active === "dheren" ? 0 : enemy.definition.damage; if (damage === 0) { this.damageEnemy(enemy, 35); this.ui.notify("Contra-ataque perfeito."); } else { actor.hurt(damage); this.ui.notify(`${enemy.definition.name} atinge ${actor.definition.name}.`); if (actor.health <= 0) { actor.heal(); actor.root.position.copyFrom(this.clearing); this.ui.notify("A chama se recusa a apagar. Retorno à clareira."); } } } }); }
  private updateCompanion(delta: number) { const companion = this.active === "kael" ? this.actors.dheren : this.actors.kael; const lead = this.activeActor(); const distance = Vector3.Distance(companion.root.position, lead.root.position); if (distance > 3.2) { const direction = lead.root.position.subtract(companion.root.position); direction.y = 0; direction.normalize(); companion.root.position.addInPlace(direction.scale(companion.definition.speed * 0.72 * delta)); companion.root.rotation.y = Math.atan2(direction.x, direction.z); companion.update(delta, true); } else companion.update(delta, false); const target = this.nearestEnemy(companion.root.position); if (!this.autoDemo && target && this.attackStarted && Vector3.Distance(target.root.position, companion.root.position) < 3.2 && companion.attackCooldown <= 0) { companion.attackCooldown = 1.1; this.damageEnemy(target, this.active === "kael" ? 21 : 18); this.spawnPulse(companion.root.position, companion.definition.color, 1.3); } }
  private nearestEnemy(position: Vector3) { return this.enemies.filter((enemy) => enemy.alive).sort((a, b) => Vector3.DistanceSquared(a.root.position, position) - Vector3.DistanceSquared(b.root.position, position))[0] ?? null; }
  private damageEnemy(enemy: Enemy, damage: number) { const dead = enemy.hurt(damage); if (dead) { this.ui.notify(`${enemy.definition.name} derrotado.`); this.audio.tone(100, 0.22, "sawtooth", 0.04); if (this.lockTarget === enemy) this.lockTarget = null; } }
  private toggleLock() { this.lockTarget = this.lockTarget?.alive ? null : this.nearestEnemy(this.activeActor().root.position); this.ui.notify(this.lockTarget ? `Foco: ${this.lockTarget.definition.name}` : "Foco desfeito."); }
  private interactable(id: Interactable["id"]) { return this.interactables.find((entry) => entry.id === id); }
  private updateInteractions() { const actor = this.activeActor(); const nearest = this.interactables.find((entry) => entry.mesh.isEnabled() && Vector3.Distance(entry.position, actor.root.position) < entry.radius); if (!nearest) { this.ui.setInteraction(""); return; } const labels = { forge: "[F] falar com Darion", wood: "[F] recolher lenha", tavern: "[F] entrar na Raposa Vermelha", exit: "[F] atravessar para Elwen" }; this.ui.setInteraction(labels[nearest.id]); }
  private interact() {
    const actor = this.activeActor(); const nearest = this.interactables.find((entry) => entry.mesh.isEnabled() && Vector3.Distance(entry.position, actor.root.position) < entry.radius); if (!nearest) return;
    if (nearest.id === "wood" && this.stage === "wood") { this.stage = "forge"; this.saveData.inventory = ["Lenha marcada para Darion"]; nearest.mesh.setEnabled(false); const prop = nearest.mesh.metadata?.prop as Mesh | undefined; prop?.setEnabled(false); this.interactable("forge")?.mesh.setEnabled(true); this.ui.dialogue("Kael", "Isso deve bastar. Darion vai fingir que não precisava de ajuda."); this.ui.notify("Lenha obtida. Volte à ferraria."); }
    if (nearest.id === "forge" && this.stage === "forge") { this.stage = "tavern"; nearest.mesh.setEnabled(false); this.interactable("tavern")?.mesh.setEnabled(true); this.ui.dialogue("Darion", "Você trouxe a madeira. Agora encontre Dheren na Raposa Vermelha; alguma coisa rondou a estrada." ); this.ui.notify("Novo registro no grimório: Darion."); }
    if (nearest.id === "tavern" && this.stage === "tavern") this.startAttack();
    if (nearest.id === "exit" && this.stage === "escape") { this.stage = "ending"; this.ui.showEnding(); void this.saveGame(); }
  }
  private completeTraining() { this.stage = "wood"; this.interactable("wood")?.mesh.setEnabled(true); const prop = this.interactable("wood")?.mesh.metadata?.prop as Mesh | undefined; prop?.setEnabled(true); this.ui.dialogue("Dheren", "Viu? Uma vez foi acidente. Duas vezes é quase competência."); this.ui.notify("Treino concluído. Darion precisa de lenha perto da floresta."); }
  private updateHud() { const actor = this.activeActor(); const objective = this.stage === "training" ? QUESTS.training.objective : this.stage === "wood" ? QUESTS["wood-for-darion"].objective : this.stage === "forge" ? QUESTS["return-to-forge"].objective : this.stage === "tavern" ? QUESTS["red-fox"].objective : this.stage === "attack" ? QUESTS["ferrosul-under-attack"].objective : this.stage === "escape" ? QUESTS["escape-ferrosul"].objective : "Ferrosul arde atrás de vocês."; const boss = this.enemies.find((enemy) => enemy.definition.isBoss && enemy.alive); this.ui.update({ characterName: actor.definition.name, characterRole: actor.definition.role, health: actor.health, maxHealth: actor.definition.maxHealth, energy: actor.energy, maxEnergy: actor.definition.maxEnergy, magicControl: this.magicControl, magicInstability: Math.round(this.magicInstability), objective, party: ["kael", "dheren", "lyra", "mira"], active: this.active, boss: boss ? { name: boss.definition.name, health: boss.health, maxHealth: boss.definition.maxHealth } : undefined, stage: this.stage }); this.ui.setSkills(actor.definition.abilities[0]?.name || "—", actor.definition.abilities[1]?.name || "—"); }
  private async saveGame(quiet = false) { this.saveData = { ...this.saveData, narrativeStage: this.stage, activeQuest: this.stage === "training" ? "training" : this.stage === "wood" ? "wood-for-darion" : this.stage === "forge" ? "return-to-forge" : this.stage === "tavern" ? "red-fox" : this.stage === "attack" ? "ferrosul-under-attack" : "escape-ferrosul", activeCharacter: this.active, magicControl: this.magicControl, magicInstability: this.magicInstability, party: ["kael", "dheren"], savedAt: Date.now() }; await this.saves.save(this.saveData); if (!quiet) this.ui.notify("Crônica gravada no grimório."); }
  private startDemo() { this.startNewGame(); this.autoDemo = true; this.ui.notify("Demonstração automática: Ferrosul em chamas."); }
  private debug(action: string) { if (action === "heal") { this.actors.kael.heal(); this.actors.dheren.heal(); this.ui.notify("Party restaurada."); } if (action === "spawn") this.spawnEnemy("ashling", this.activeActor().root.position.add(new Vector3(0, 0, 4))); if (action === "kill") this.enemies.forEach((enemy) => enemy.hurt(enemy.health)); if (action === "attack") this.startAttack(true); if (action === "kael") this.setActive("kael"); if (action === "dheren") { if (!this.attackStarted) this.startAttack(true); this.setActive("dheren"); } }
  dispose() { this.input.dispose(); this.audio.dispose(); this.ui.dispose(); this.actors.kael.dispose(); this.actors.dheren.dispose(); this.enemies.forEach((enemy) => enemy.dispose()); this.projectiles.forEach((projectile) => projectile.mesh.dispose()); }
}
