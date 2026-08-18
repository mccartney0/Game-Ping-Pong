/**
 * Gravura de Cinzas — conteúdo configurável do vertical slice.
 * Use estes dados para manter personagens e combate coerentes com o cânone disponível.
 */
import type { CharacterDefinition, EnemyDefinition, QuestDefinition } from "./types";

export const CHARACTERS: Record<string, CharacterDefinition> = {
  kael: {
    id: "kael", name: "Kael", role: "Mago / Bruxo", age: 19, color: "#62c9ff", accent: "#d7efff", maxHealth: 120, maxEnergy: 100, speed: 5.1,
    abilities: [
      { id: "blue-spark", name: "Centelha Azul", description: "Projétil arcano instável.", key: "Q", unlocked: true },
      { id: "impulse", name: "Impulso", description: "Onda curta que afasta inimigos.", key: "E", unlocked: true },
      { id: "arcane-flame", name: "Chama Azul", description: "Poder maior, ainda bloqueado pela história.", key: "R", unlocked: false },
    ],
  },
  dheren: {
    id: "dheren", name: "Dheren Varenn", role: "Guerreiro Místico", age: 39, color: "#e8b951", accent: "#fff0ae", maxHealth: 175, maxEnergy: 80, speed: 6.2,
    abilities: [
      { id: "astral-strike", name: "Golpe Astral", description: "Corte dourado energizado.", key: "Q", unlocked: true },
      { id: "ethereal-step", name: "Passo Etéreo", description: "Avanço veloz e preciso.", key: "E", unlocked: true },
      { id: "vigil", name: "Vigília", description: "Poder ainda não revelado.", key: "R", unlocked: false },
    ],
  },
  lyra: { id: "lyra", name: "Lyra", role: "Arqueira / Ranger", age: 0, color: "#98bf78", accent: "#ecffd6", maxHealth: 110, maxEnergy: 90, speed: 6.5, abilities: [] },
  mira: { id: "mira", name: "Mira", role: "Ladina", age: 0, color: "#bf7bd0", accent: "#f6d8ff", maxHealth: 105, maxEnergy: 100, speed: 7.2, abilities: [] },
};

export const ENEMIES: Record<string, EnemyDefinition> = {
  ashling: { id: "ashling", name: "Criatura das Cinzas", maxHealth: 42, speed: 2.3, damage: 8, color: "#7f4750" },
  "ash-soldier": { id: "ash-soldier", name: "Soldado das Cinzas", maxHealth: 68, speed: 1.9, damage: 11, color: "#73474b" },
  "blood-tracker": { id: "blood-tracker", name: "Rastreador de Sangue", maxHealth: 260, speed: 2.7, damage: 16, color: "#a23e36", isBoss: true },
};

export const QUESTS: Record<string, QuestDefinition> = {
  training: { id: "training", title: "A chama não obedece", objective: "Concentre a Centelha Azul duas vezes na clareira. [Q]" },
  "wood-for-darion": { id: "wood-for-darion", title: "Madeira para Darion", objective: "Encontre a lenha marcada perto da floresta. [F]" },
  "return-to-forge": { id: "return-to-forge", title: "Madeira para Darion", objective: "Leve a lenha de volta à ferraria. [F]" },
  "red-fox": { id: "red-fox", title: "A Raposa Vermelha", objective: "Encontre Dheren na Raposa Vermelha. [F]" },
  "ferrosul-under-attack": { id: "ferrosul-under-attack", title: "O sino e as cinzas", objective: "Assuma Dheren e proteja a praça." },
  "escape-ferrosul": { id: "escape-ferrosul", title: "Fuga para Elwen", objective: "Abra caminho até a saída da floresta. [F]" },
};

export const INITIAL_DIALOGUE = [
  { speaker: "Dheren", text: "Tenta novamente. Uma chama não respeita quem pede licença." },
  { speaker: "Kael", text: "Ela também não parece respeitar quem está tentando não incendiar as próprias botas." },
  { speaker: "Dheren", text: "Então hoje já aprendemos alguma coisa." },
];
