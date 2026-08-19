/**
 * Gravura de Cinzas — dados de gameplay independentes do renderizador.
 * A chama azul representa instabilidade de Kael; ouro representa precisão de Dheren.
 */

export type CharacterId = "kael" | "dheren" | "lyra" | "mira";
export type EquipmentSlot = "arma" | "traje" | "reliquia";
export type EquipmentRarity = "raro" | "épico" | "lendário";
export type EnemyId = "blood-tracker" | "ash-soldier" | "ashling" | "ash-scout" | "road-wraith" | "crown-agent" | "masked-crown";
export type QuestId = "training" | "wood-for-darion" | "return-to-forge" | "red-fox" | "ferrosul-under-attack" | "escape-ferrosul" | "elwen-threshold" | "elwen-trail" | "dead-road-gathering" | "veyra-arrival" | "mira-and-the-grimoire" | "market-whispers" | "three-flames-trail" | "needle-house" | "market-witnesses" | "investigation-board" | "crown-agents" | "after-the-ambush" | "masked-herald" | "guild-of-paths" | "guild-archive" | "underways-entry" | "underways-trials" | "underways-stealth" | "underways-proof";
export type NarrativeStage = "title" | "training" | "wood" | "forge" | "tavern" | "attack" | "escape" | "elwen-gate" | "elwen-trail" | "elwen-camp" | "dead-road" | "veyra-gate" | "mira-intro" | "mira-chase" | "market-whispers" | "three-flames-clue" | "needle-house" | "mira-unlocked" | "market-witnesses" | "investigation-board" | "crown-ambush" | "investigation-resolved" | "masked-boss" | "guild-gate" | "guild-inquiry" | "guild-archive" | "guild-resolved" | "underways-entry" | "underways-trials" | "underways-stealth" | "underways-proof" | "underways-resolved" | "ending";
export type EvidenceId = "market" | "contract" | "coin" | "crown" | "needle" | "ledger" | "canal" | "hidden-route" | "guild-map" | "runner" | "mask-seal" | "ripped-route" | "underways-sigil" | "courier-knot";

export interface AbilityDefinition {
  id: string;
  name: string;
  description: string;
  key: "Q" | "E" | "R";
  unlocked: boolean;
}

export interface CharacterDefinition {
  id: CharacterId;
  name: string;
  role: string;
  age: number;
  color: string;
  accent: string;
  model?: { url: string; scale: number; yaw?: number; animations?: Partial<Record<"idle" | "move" | "attack" | "special" | "hurt" | "death", string>> };
  maxHealth: number;
  maxEnergy: number;
  speed: number;
  abilities: AbilityDefinition[];
}

export interface EquipmentBonuses {
  health?: number;
  energy?: number;
  speed?: number;
  damage?: number;
  abilityDamage?: number;
  dodge?: number;
  parry?: number;
  instability?: number;
  stealth?: number;
}

export interface EquipmentDefinition {
  id: string;
  name: string;
  owner: CharacterId;
  slot: EquipmentSlot;
  rarity: EquipmentRarity;
  collection: string;
  description: string;
  bonuses: EquipmentBonuses;
  upgradeLabel: string;
}

export interface EquipmentSetDefinition {
  id: string;
  name: string;
  owner: CharacterId;
  pieces: 2 | 3;
  bonuses: Partial<Record<2 | 3, EquipmentBonuses>>;
}

export interface EquipmentSetProgress {
  id: string;
  name: string;
  pieces: number;
  required: number;
  activeTiers: number[];
  bonuses: EquipmentBonuses;
}

export interface CraftRecipeDefinition {
  id: string;
  name: string;
  description: string;
  materials: Record<string, number>;
  result: string;
  resultDescription: string;
}

export type ConsumableId = "bruma-guardada" | "sal-do-vigia" | "fio-de-veyra";

export interface ConsumableDefinition {
  id: ConsumableId;
  name: string;
  description: string;
  combatEffect: string;
  accent: string;
  glyph: string;
}

export interface DailyMissionState {
  id: string;
  type: "collect" | "craft" | "event";
  title: string;
  objective: string;
  target: number;
  progress: number;
  reward: string;
  completed: boolean;
}

export interface DynamicEventState {
  id: string;
  title: string;
  objective: string;
  kind: "combat" | "collection";
  active: boolean;
  completed: boolean;
}

export interface EnemyDefinition {
  id: EnemyId;
  name: string;
  maxHealth: number;
  speed: number;
  damage: number;
  color: string;
  isBoss?: boolean;
  model?: { url: string; scale: number; yaw?: number; offsetY?: number };
}

export interface QuestDefinition {
  id: QuestId;
  title: string;
  objective: string;
}

export interface SaveData {
  version: 1;
  chapter: string;
  narrativeStage: NarrativeStage;
  activeQuest: QuestId;
  activeCharacter: CharacterId;
  party: CharacterId[];
  magicControl: number;
  magicInstability: number;
  inventory: string[];
  codexEntries: string[];
  investigationLinks: string[];
  guildChoice: "mapas" | "nomes" | null;
  underwaysChallenges: string[];
  underwaysStealth: "pending" | "clean" | "alerted";
  lanternReputation: number;
  lanternFactionUnlocked: boolean;
  lanternMissions: string[];
  soundTrapsTriggered: string[];
  equippedItems: Record<CharacterId, Partial<Record<EquipmentSlot, string>>>;
  equipmentUpgrades: Record<string, number>;
  upgradeTokens: number;
  materials: Record<string, number>;
  consumableCounts: Record<ConsumableId, number>;
  quickBelt: Array<ConsumableId | null>;
  collectedMaterialNodes: string[];
  craftedRecipes: string[];
  dailyCycleKey: string;
  dailyMissions: DailyMissionState[];
  dynamicEvent: DynamicEventState;
  unlockedAbilities: string[];
  settings: { difficulty: "Historia" | "Aventura" | "Veterano" | "Lendario"; reducedMotion: boolean };
  savedAt: number;
}
