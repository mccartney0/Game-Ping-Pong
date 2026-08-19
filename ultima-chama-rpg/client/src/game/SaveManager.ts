/**
 * Gravura de Cinzas — save local de capítulo, party, grimório e progresso em IndexedDB.
 */
import type { SaveData } from "./types";

const DB_NAME = "chama-ultimo-reino";
const STORE = "saves";
const KEY = "ferrosul-slot-1";

export const defaultSave = (): SaveData => ({
  version: 1, chapter: "Ato I — Ferrosul", narrativeStage: "title", activeQuest: "training", activeCharacter: "kael", party: ["kael", "dheren"], magicControl: 20, magicInstability: 0,
  inventory: ["Foco da Centelha Azul", "Guarda Dourada de Dheren", "Fragmento de Cinza Lapidada", "Fragmento de Cinza Lapidada", "Fragmento de Cinza Lapidada"], codexEntries: ["Ferrosul", "Dheren Varenn", "Centelha Azul"], investigationLinks: [], guildChoice: null, underwaysChallenges: [], underwaysStealth: "pending", lanternReputation: 0, lanternFactionUnlocked: false, lanternMissions: [], soundTrapsTriggered: [], equippedItems: { kael: { reliquia: "kael-focus" }, dheren: { traje: "dheren-guard" }, lyra: {}, mira: {} }, equipmentUpgrades: {}, upgradeTokens: 3, materials: { "sal-da-senda": 0, "cobre-afogado": 0, "cinza-lunar": 0, "semente-bruma": 0 }, veyraMarks: 12, consumableCounts: { "bruma-guardada": 0, "sal-do-vigia": 0, "fio-de-veyra": 0 }, quickBelt: ["bruma-guardada", "sal-do-vigia", "fio-de-veyra"], collectedMaterialNodes: [], craftedRecipes: [], dailyCycleKey: "", dailyMissions: [], dynamicEvent: { id: "", title: "", objective: "", kind: "combat", active: false, completed: false }, unlockedAbilities: ["blue-spark", "impulse", "astral-strike", "ethereal-step"], settings: { difficulty: "Aventura", reducedMotion: false, language: "pt-BR", narrationEnabled: true, dialogueSubtitlesEnabled: true, actionSubtitlesEnabled: true }, savedAt: Date.now(),
});

export class SaveManager {
  private dbPromise: Promise<IDBDatabase | null>;
  constructor() { this.dbPromise = this.open(); }
  private open(): Promise<IDBDatabase | null> {
    return new Promise((resolve) => {
      if (!("indexedDB" in window)) return resolve(null);
      const request = indexedDB.open(DB_NAME, 1);
      request.onupgradeneeded = () => request.result.createObjectStore(STORE);
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => resolve(null);
    });
  }
  async load(): Promise<SaveData | null> {
    const db = await this.dbPromise;
    if (!db) { try { return JSON.parse(localStorage.getItem(KEY) || "null") as SaveData | null; } catch { return null; } }
    return new Promise((resolve) => {
      const request = db.transaction(STORE, "readonly").objectStore(STORE).get(KEY);
      request.onsuccess = () => resolve((request.result as SaveData | undefined) ?? null);
      request.onerror = () => resolve(null);
    });
  }
  async save(data: SaveData): Promise<void> {
    const payload = { ...data, savedAt: Date.now() };
    const db = await this.dbPromise;
    if (!db) { localStorage.setItem(KEY, JSON.stringify(payload)); return; }
    await new Promise<void>((resolve) => {
      const request = db.transaction(STORE, "readwrite").objectStore(STORE).put(payload, KEY);
      request.onsuccess = () => resolve(); request.onerror = () => { localStorage.setItem(KEY, JSON.stringify(payload)); resolve(); };
    });
  }
}
