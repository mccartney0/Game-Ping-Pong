/**
 * Gravura de Cinzas — dados de gameplay independentes do renderizador.
 * A chama azul representa instabilidade de Kael; ouro representa precisão de Dheren.
 */

export type CharacterId = "kael" | "dheren" | "lyra" | "mira";
export type EnemyId = "blood-tracker" | "ash-soldier" | "ashling";
export type QuestId = "training" | "wood-for-darion" | "return-to-forge" | "red-fox" | "ferrosul-under-attack" | "escape-ferrosul";
export type NarrativeStage = "title" | "training" | "wood" | "forge" | "tavern" | "attack" | "escape" | "ending";

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
  maxHealth: number;
  maxEnergy: number;
  speed: number;
  abilities: AbilityDefinition[];
}

export interface EnemyDefinition {
  id: EnemyId;
  name: string;
  maxHealth: number;
  speed: number;
  damage: number;
  color: string;
  isBoss?: boolean;
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
  unlockedAbilities: string[];
  settings: { difficulty: "Historia" | "Aventura" | "Veterano" | "Lendario"; reducedMotion: boolean };
  savedAt: number;
}
