/**
 * Gravura de Cinzas — conteúdo configurável do vertical slice.
 * Use estes dados para manter personagens e combate coerentes com o cânone disponível.
 */
import type { CharacterDefinition, ConsumableDefinition, CraftRecipeDefinition, EnemyDefinition, EquipmentDefinition, EquipmentSetDefinition, QuestDefinition, ShopOffer } from "./types";

export const CHARACTERS: Record<string, CharacterDefinition> = {
  kael: {
    id: "kael", name: "Kael", role: "Portador da Chama", age: 19, color: "#62c9ff", accent: "#d7efff", model: { url: "/manus-storage/kael-wanderer_bf0c29b7.glb", scale: 0.92, yaw: 0 }, maxHealth: 120, maxEnergy: 100, speed: 5.1,
    abilities: [
      { id: "blue-spark", name: "Centelha Azul", description: "Projétil arcano instável.", key: "Q", unlocked: true },
      { id: "impulse", name: "Impulso", description: "Onda curta que afasta inimigos.", key: "E", unlocked: true },
      { id: "arcane-flame", name: "Chama Azul", description: "Poder maior, ainda bloqueado pela história.", key: "R", unlocked: false },
    ],
  },
  dheren: {
    id: "dheren", name: "Dheren Varenn", role: "Guerreiro Místico", age: 39, color: "#e8b951", accent: "#fff0ae", model: { url: "/manus-storage/dheren-warrior_cd65ad32.glb", scale: 0.92, yaw: 0 }, maxHealth: 175, maxEnergy: 80, speed: 6.2,
    abilities: [
      { id: "astral-strike", name: "Golpe Astral", description: "Corte dourado energizado.", key: "Q", unlocked: true },
      { id: "ethereal-step", name: "Passo Etéreo", description: "Avanço veloz e preciso.", key: "E", unlocked: true },
      { id: "vigil", name: "Vigília", description: "Poder ainda não revelado.", key: "R", unlocked: false },
    ],
  },
  lyra: {
    id: "lyra", name: "Lyra", role: "Arqueira da Estrada Morta", age: 0, color: "#98bf78", accent: "#ecffd6", model: { url: "/manus-storage/lyra-archer-exclusive_3bb23d88.glb", scale: 1.12, yaw: 0, animations: { idle: "Idle", move: "Walk", attack: "Bow_Shot", special: "Thorn_Rain" } }, maxHealth: 108, maxEnergy: 110, speed: 6.7,
    abilities: [
      { id: "watch-arrow", name: "Flecha de Vigia", description: "Flecha veloz que marca a trilha.", key: "Q", unlocked: true },
      { id: "thorns-rain", name: "Chuva de Espinhos", description: "Três disparos sobre os inimigos próximos.", key: "E", unlocked: true },
      { id: "moon-shot", name: "Tiro da Lua", description: "Página selada da arqueira.", key: "R", unlocked: false },
    ],
  },
  mira: {
    id: "mira", name: "Mira", role: "Ladina de Veyra", age: 0, color: "#bf7bd0", accent: "#f6d8ff", model: { url: "/manus-storage/kael-wanderer_bf0c29b7.glb", scale: 0.88, yaw: 0 }, maxHealth: 105, maxEnergy: 120, speed: 7.35,
    abilities: [
      { id: "needle-cut", name: "Corte da Agulha", description: "Avanço curto entre duas adagas que abre a guarda inimiga.", key: "Q", unlocked: true },
      { id: "hidden-passage", name: "Passagem Oculta", description: "Salto lateral que deixa uma lâmina de sombra no caminho.", key: "E", unlocked: true },
      { id: "three-flames", name: "Três Chamas", description: "Um segredo do símbolo ainda não decifrado.", key: "R", unlocked: false },
    ],
  },
};

export const ENEMIES: Record<string, EnemyDefinition> = {
  ashling: { id: "ashling", name: "Criatura das Cinzas", maxHealth: 42, speed: 2.3, damage: 8, color: "#7f4750" },
  "ash-soldier": { id: "ash-soldier", name: "Soldado das Cinzas", maxHealth: 68, speed: 1.9, damage: 11, color: "#73474b" },
  "blood-tracker": { id: "blood-tracker", name: "Rastreador de Sangue", maxHealth: 260, speed: 2.7, damage: 16, color: "#a23e36", isBoss: true, model: { url: "/manus-storage/blood-tracker-demon_5c2ab469.glb", scale: 1.32, yaw: 0, offsetY: 0.02 } },
  "ash-scout": { id: "ash-scout", name: "Batedor das Cinzas", maxHealth: 58, speed: 3.2, damage: 10, color: "#805653" },
  "road-wraith": { id: "road-wraith", name: "Sussurrante da Estrada", maxHealth: 92, speed: 2.55, damage: 13, color: "#536e63" },
  "crown-agent": { id: "crown-agent", name: "Agente da Coroa", maxHealth: 105, speed: 2.85, damage: 15, color: "#813d49" },
  "masked-crown": { id: "masked-crown", name: "Arauto Mascarado", maxHealth: 385, speed: 3.45, damage: 21, color: "#4d2548", isBoss: true },
};

/** Equipamentos de jornada: cada melhoria aumenta o efeito base em 45%. */
export const EQUIPMENT: Record<string, EquipmentDefinition> = {
  "kael-focus": { id: "kael-focus", name: "Foco da Centelha Azul", owner: "kael", slot: "reliquia", rarity: "épico", collection: "cinza-fria", description: "Um fragmento de vidro azul que estabiliza a chama de Kael sem torná-la dócil.", bonuses: { energy: 18, abilityDamage: .16, instability: .22 }, upgradeLabel: "Lapidar a centelha" },
  "kael-mantle": { id: "kael-mantle", name: "Manto de Cinza Fria", owner: "kael", slot: "traje", rarity: "raro", collection: "cinza-fria", description: "Tecido carbonizado que absorve a primeira mordida do fogo azul.", bonuses: { health: 16, damage: .08 }, upgradeLabel: "Costurar runas frias" },
  "dheren-guard": { id: "dheren-guard", name: "Guarda Dourada de Dheren", owner: "dheren", slot: "traje", rarity: "épico", collection: "vigilia-dourada", description: "Proteção de campanha com canais de ouro preparados para receber impacto.", bonuses: { health: 30, parry: .16 }, upgradeLabel: "Reforçar os canais" },
  "dheren-blade": { id: "dheren-blade", name: "Lâmina da Vigília", owner: "dheren", slot: "arma", rarity: "raro", collection: "vigilia-dourada", description: "Uma lâmina de vigia marcada pela disciplina de Dheren Varenn.", bonuses: { damage: .16, speed: .12 }, upgradeLabel: "Temperar o fio" },
  "lyra-string": { id: "lyra-string", name: "Corda de Vigia", owner: "lyra", slot: "arma", rarity: "épico", collection: "estrada-morta", description: "Fibra de arco que responde ao pulso de Lyra e afia cada disparo.", bonuses: { damage: .14, abilityDamage: .14 }, upgradeLabel: "Tensionar a fibra" },
  "lyra-cloak": { id: "lyra-cloak", name: "Manto da Estrada Morta", owner: "lyra", slot: "traje", rarity: "raro", collection: "estrada-morta", description: "Manto leve para desaparecer entre pinheiros, ruínas e chuva.", bonuses: { energy: 16, speed: .28, stealth: .18 }, upgradeLabel: "Reforçar a bainha" },
  "mira-copper": { id: "mira-copper", name: "Agulha de Cobre da Passagem", owner: "mira", slot: "arma", rarity: "épico", collection: "lanterna-velada", description: "O cobre fino da passagem lateral abre a guarda antes que o inimigo perceba.", bonuses: { damage: .2, abilityDamage: .12 }, upgradeLabel: "Afiar a agulha" },
  "mira-obsidian": { id: "mira-obsidian", name: "Lâmina de Vigia Obsidiana", owner: "mira", slot: "reliquia", rarity: "lendário", collection: "lanterna-velada", description: "A lâmina rara da sala selada torna cada escape de Mira mais difícil de seguir.", bonuses: { damage: .14, dodge: .16, stealth: .24 }, upgradeLabel: "Polir a obsidiana" },
  "mira-mantle": { id: "mira-mantle", name: "Manto da Lanterna Velada", owner: "mira", slot: "traje", rarity: "épico", collection: "lanterna-velada", description: "Um manto concedido a quem cruzou a Senda Baixa sem entregar a própria sombra.", bonuses: { health: 12, speed: .34, stealth: .3 }, upgradeLabel: "Bordar o emblema" },
};

export const EQUIPMENT_SETS: Record<string, EquipmentSetDefinition> = {
  "cinza-fria": { id: "cinza-fria", name: "Cinza Fria", owner: "kael", pieces: 2, bonuses: { 2: { energy: 14, abilityDamage: .08, instability: .1 } } },
  "vigilia-dourada": { id: "vigilia-dourada", name: "Vigília Dourada", owner: "dheren", pieces: 2, bonuses: { 2: { health: 22, parry: .12 } } },
  "estrada-morta": { id: "estrada-morta", name: "Estrada Morta", owner: "lyra", pieces: 2, bonuses: { 2: { speed: .2, stealth: .2, abilityDamage: .08 } } },
  "lanterna-velada": { id: "lanterna-velada", name: "Lanterna Velada", owner: "mira", pieces: 3, bonuses: { 2: { dodge: .12, stealth: .1 }, 3: { speed: .2, abilityDamage: .18 } } },
};

export const MATERIALS: Record<string, { name: string; accent: string; route: string }> = {
  "sal-da-senda": { name: "Sal da Senda", accent: "#bde7df", route: "Senda Baixa" },
  "cobre-afogado": { name: "Cobre Afogado", accent: "#d69b64", route: "Canal de Veyra" },
  "cinza-lunar": { name: "Cinza Lunar", accent: "#9f86cd", route: "Passagem lateral" },
  "semente-bruma": { name: "Semente de Bruma", accent: "#76c9cf", route: "Mercado noturno" },
};

export const CRAFT_RECIPES: Record<string, CraftRecipeDefinition> = {
  "bruma-guardada": { id: "bruma-guardada", name: "Ampola de Bruma Guardada", description: "Condensa a bruma do canal em uma esquiva restauradora.", materials: { "semente-bruma": 2, "cobre-afogado": 1 }, result: "Ampola de Bruma Guardada", resultDescription: "Restaura energia e reforça a próxima esquiva de Mira." },
  "sal-do-vigia": { id: "sal-do-vigia", name: "Sal do Vigia", description: "Uma mistura que torna a trilha menos legível aos inimigos.", materials: { "sal-da-senda": 2, "cinza-lunar": 1 }, result: "Sal do Vigia", resultDescription: "Concede um Fragmento de Cinza Lapidada para melhorias." },
  "fio-de-veyra": { id: "fio-de-veyra", name: "Fio de Veyra", description: "Cobre e cinza trançados para selar uma rota silenciosa.", materials: { "cobre-afogado": 2, "cinza-lunar": 2 }, result: "Fio de Veyra", resultDescription: "Eleva a reputação da Lanterna Velada." },
};

export const CONSUMABLES: Record<string, ConsumableDefinition> = {
  "bruma-guardada": { id: "bruma-guardada", name: "Ampola de Bruma Guardada", description: "Bruma condensada do canal que devolve fôlego e torna a próxima esquiva mais longa.", combatEffect: "Recupera energia e amplifica esquivas por alguns segundos.", accent: "#76c9cf", glyph: "◌" },
  "sal-do-vigia": { id: "sal-do-vigia", name: "Sal do Vigia", description: "Sal da Senda misturado a cinza lunar, espalhado como uma proteção discreta.", combatEffect: "Reduz o dano recebido durante uma breve vigília.", accent: "#e8c977", glyph: "✦" },
  "fio-de-veyra": { id: "fio-de-veyra", name: "Fio de Veyra", description: "Cobre e cinza trançados no silêncio das rotas veladas.", combatEffect: "Potencializa dano de habilidade por alguns segundos.", accent: "#bf7bd0", glyph: "⌁" },
};

export const VEYRA_SHOP_OFFERS: Record<string, ShopOffer> = {
  "apothecary-bruma": { id: "apothecary-bruma", merchantId: "merchant-apothecary", consumableId: "bruma-guardada", price: 3, note: "Bruma do canal, selada para combate." },
  "apothecary-sal": { id: "apothecary-sal", merchantId: "merchant-apothecary", consumableId: "sal-do-vigia", price: 4, note: "Sal fino para atravessar uma troca de golpes." },
  "thread-fio": { id: "thread-fio", merchantId: "merchant-thread", consumableId: "fio-de-veyra", price: 5, note: "Fio runado para uma habilidade decisiva." },
  "thread-bruma": { id: "thread-bruma", merchantId: "merchant-thread", consumableId: "bruma-guardada", price: 4, note: "Uma ampola extra para a rota mais longa." },
};

export const QUESTS: Record<string, QuestDefinition> = {
  training: { id: "training", title: "Coisas que um ferreiro não deveria fazer", objective: "Concentre a Centelha Azul duas vezes na clareira. [Q]" },
  "wood-for-darion": { id: "wood-for-darion", title: "Madeira para Darion", objective: "Encontre a lenha marcada perto da floresta. [F]" },
  "return-to-forge": { id: "return-to-forge", title: "Madeira para Darion", objective: "Leve a lenha de volta à ferraria. [F]" },
  "red-fox": { id: "red-fox", title: "A Raposa Vermelha", objective: "Encontre Dheren na Raposa Vermelha. [F]" },
  "ferrosul-under-attack": { id: "ferrosul-under-attack", title: "A Noite das Cinzas", objective: "Assuma Dheren e proteja a praça de Ferrosul." },
  "escape-ferrosul": { id: "escape-ferrosul", title: "Fuga para Elwen", objective: "Abra caminho até a saída da floresta. [F]" },
  "elwen-threshold": { id: "elwen-threshold", title: "A Estrada Morta", objective: "Siga Dheren até a primeira fogueira de Elwen. [F]" },
  "elwen-trail": { id: "elwen-trail", title: "A Arqueira e o Caminho Morto", objective: "Sobreviva à emboscada na trilha e encontre a voz entre os pinheiros." },
  "dead-road-gathering": { id: "dead-road-gathering", title: "Marcas na Estrada Morta", objective: "Recolha marcas de trilha e alcance a ponte quebrada." },
  "veyra-arrival": { id: "veyra-arrival", title: "Cidade de Mil Portas", objective: "Acompanhe Lyra até o portão sul de Veyra. [F]" },
  "mira-and-the-grimoire": { id: "mira-and-the-grimoire", title: "O Grimório Desaparecido", objective: "Encontre a ladina de capuz escuro que levou o Grimório." },
  "market-whispers": { id: "market-whispers", title: "Sussurros do Mercado", objective: "Siga Mira até o mercado e pergunte pelo comprador do Grimório. [F]" },
  "three-flames-trail": { id: "three-flames-trail", title: "A Marca das Três Chamas", objective: "Examine o contrato marcado pela coroa e pelas três chamas. [F]" },
  "needle-house": { id: "needle-house", title: "A Casa da Agulha", objective: "Encontre Mira na Casa da Agulha e recupere o Grimório. [F]" },
  "market-witnesses": { id: "market-witnesses", title: "Olhos do Mercado", objective: "Converse com a escriba e o barqueiro de Veyra. [F]" },
  "investigation-board": { id: "investigation-board", title: "Páginas Conectadas", objective: "No Grimório, conecte três pistas da rede da coroa. [✦]" },
  "crown-agents": { id: "crown-agents", title: "A Coroa Responde", objective: "Sobreviva à emboscada dos agentes das três chamas." },
  "after-the-ambush": { id: "after-the-ambush", title: "Fio Exposto", objective: "Registre a prova dos agentes no Grimório." },
  "masked-herald": { id: "masked-herald", title: "O Arauto sob a Máscara", objective: "Derrote o Arauto Mascarado antes que ele recupere as pistas." },
  "guild-of-paths": { id: "guild-of-paths", title: "A Guilda dos Caminhos", objective: "Leve o selo da máscara até a Guilda dos Caminhos. [F]" },
  "guild-archive": { id: "guild-archive", title: "Rotas que Não Constam", objective: "Converse com a cartógrafa e o corredor da Guilda. [F]" },
  "underways-entry": { id: "underways-entry", title: "Sob as Mil Portas", objective: "Use a ficha rasgada para encontrar a entrada sob a Guilda. [F]" },
  "underways-trials": { id: "underways-trials", title: "A Passagem que Escuta", objective: "Desvende os três desafios dos subterrâneos de Veyra. [F]" },
  "underways-stealth": { id: "underways-stealth", title: "Olhos na Senda Baixa", objective: "Cruze o corredor sem entrar no campo de visão dos vigias." },
  "underways-proof": { id: "underways-proof", title: "O Nó do Mensageiro", objective: "Examine a prova deixada na câmara final. [F]" },
};

export const INITIAL_DIALOGUE = [
  { speaker: "Dheren", text: "De novo. Hoje já castigamos a bota; não precisa declarar guerra à outra." },
  { speaker: "Kael", text: "Eu só queria esquentar uma caneca de chá." },
  { speaker: "Dheren", text: "E conseguiu fazer fogo. Isso conta mais do que você imagina." },
];

/** Páginas futuras, registradas no Grimório mas não liberadas antes de sua progressão narrativa. */
export const SEALED_GRIMOIRE_ENTRIES = [
  { id: "flow-vision", title: "Visão do Fluxo", hint: "Algumas correntes não se veem com os olhos comuns.", state: "selada" },
  { id: "astral-bond", title: "Laço Astral", hint: "Há perigos que chegam ao outro antes de chegar à palavra.", state: "selada" },
  { id: "golden-channels", title: "Canais Dourados", hint: "A disciplina pode dar forma a uma força que ainda não compreendemos.", state: "selada" },
  { id: "runic-craft", title: "Runas de Equilíbrio", hint: "Nenhuma estrutura deve tomar de alguém a própria escolha.", state: "selada" },
] as const;
