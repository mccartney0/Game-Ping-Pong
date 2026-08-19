/**
 * Gravura de Cinzas — HUD discreto de pergaminho e cinza; o mundo 3D continua dominante.
 */
import { CONSUMABLES, CRAFT_RECIPES, EQUIPMENT, MATERIALS, VEYRA_SHOP_OFFERS } from "./data";
import { Localization, type GameLanguage } from "./Localization";
import type { CharacterId, ConsumableId, DailyMissionState, DynamicEventState, EquipmentSetProgress, EquipmentSlot, NarrativeStage } from "./types";

export interface HudState {
  characterName: string;
  characterRole: string;
  health: number;
  maxHealth: number;
  energy: number;
  maxEnergy: number;
  magicControl: number;
  magicInstability: number;
  objective: string;
  party: CharacterId[];
  active: CharacterId;
  boss?: { name: string; health: number; maxHealth: number };
  stage: NarrativeStage;
  faction?: { reputation: number; unlocked: boolean };
}

export interface UICallbacks {
  startNewGame: () => void;
  continueGame: () => void;
  saveGame: () => void;
  changeDifficulty: (value: "Historia" | "Aventura" | "Veterano" | "Lendario") => void;
  changeLanguage: (value: GameLanguage) => void;
  updateInvestigationLinks: (links: string[]) => void;
  chooseGuildClue: (choice: "mapas" | "nomes") => void;
  equipItem: (owner: CharacterId, itemId: string) => void;
  upgradeEquipment: (itemId: string) => void;
  craftRecipe: (recipeId: string) => void;
  equipQuickBelt: (slot: number, consumableId: ConsumableId) => void;
  buyConsumable: (offerId: string) => void;
  playDialogueAudio: (speaker: string, text: string, onComplete: () => void) => boolean;
  previewVoice: (speaker: string) => void;
  changePresentationSettings: (settings: { narrationEnabled: boolean; dialogueSubtitlesEnabled: boolean; actionSubtitlesEnabled: boolean }) => void;
  changeAudioSettings: (settings: { voiceVolume: number; sfxVolume: number; musicVolume: number }) => void;
  debugAction: (action: string) => void;
}

export class UIController {
  private readonly root: HTMLDivElement;
  private notificationTimer = 0;
  private dialogueTimer = 0;
  private bossWarningTimer = 0;
  private selectedEvidence = "";
  private investigationLinks: string[] = [];
  private investigationFingerprint = "";
  private partyReactionTimers: number[] = [];
  private equipmentFingerprint = "";
  private completedSetKeys = new Set<string>();
  private setActivationTimer = 0;
  private craftingFingerprint = "";
  private beltFingerprint = "";
  private activeMerchantId = "";
  private merchantMarks = 0;
  private activePauseTab = "game";
  private presentationSettings = { narrationEnabled: true, dialogueSubtitlesEnabled: true, actionSubtitlesEnabled: true };
  private readonly l10n = new Localization();
  private readonly evidence = [
    { id: "market", title: "Mercado de Veyra", type: "LOCAL", description: "Bancas fechadas às pressas e vendedores que só falam quando a multidão muda de rumo." },
    { id: "contract", title: "Contrato das Três Chamas", type: "DOCUMENTO", description: "A marca liga compra de relíquias, nomes riscados e pagamentos sem origem." },
    { id: "coin", title: "Moeda Marcada", type: "OBJETO", description: "Uma moeda de Veyra com a mesma coroa negra cercada por três chamas." },
    { id: "crown", title: "Coroa Negra", type: "SÍMBOLO", description: "O emblema visto nos soldados de Ferrosul reapareceu nos becos da cidade." },
    { id: "needle", title: "Casa da Agulha", type: "LOCAL", description: "Uma hospedaria de portas discretas e saídas demais para ser inocente." },
    { id: "ledger", title: "Registro da Escriba", type: "TESTEMUNHO", description: "Uma lista de cargas que foi alterada na mesma noite em que o comprador chegou." },
    { id: "canal", title: "Rota do Canal", type: "PASSAGEM", description: "O barqueiro conhece uma travessia que evita os olhos do mercado." },
    { id: "hidden-route", title: "Recibo da Passagem", type: "PISTA", description: "Um recibo de rota escondida aponta para a vigilância dos agentes." },
    { id: "mask-seal", title: "Selo da Máscara", type: "PROVA", description: "Cera marcada pelo arauto. A Guilda dos Caminhos reconhece selos que a cidade preferia esquecer." },
    { id: "guild-map", title: "Mapa de Rotas da Guilda", type: "DOCUMENTO", description: "Rotas oficiais e atalhos que não aparecem nas placas de Veyra." },
    { id: "runner", title: "Relato do Corredor", type: "TESTEMUNHO", description: "Um corredor da Guilda viu uma carga sem nome seguir para além das portas internas." },
    { id: "ripped-route", title: "Ficha de Rota Rasgada", type: "CHAVE", description: "A parte restante desenha uma entrada que só existe sob as fundações da Guilda." },
    { id: "underways-sigil", title: "Sinal da Senda Baixa", type: "SÍMBOLO", description: "Um risco de sal e cobre confirma que a passagem subterrânea foi usada por mensageiros da coroa." },
    { id: "courier-knot", title: "Nó do Mensageiro", type: "PROVA", description: "Um nó de fios cinzentos e cera negra guarda a próxima direção sem entregar um nome." },
  ];
  constructor(private readonly callbacks: UICallbacks) {
    this.root = document.createElement("div");
    this.root.className = "rpg-overlay";
    this.root.innerHTML = `
      <section class="rpg-title-screen" id="title-screen">
        <div class="title-ash"></div>
        <div class="title-content">
          <div class="title-crown" aria-hidden="true"><i></i><i></i><i></i></div>
          <div class="rune-mark" aria-hidden="true"><span></span></div>
          <p class="eyebrow" data-i18n="bookOne">LIVRO I · VERTICAL SLICE</p>
          <h1>A CHAMA<br/><em>DO ÚLTIMO</em> REINO</h1>
          <p class="title-chronicle">FERROSUL · CINZA · MEMÓRIA</p>
          <p class="title-hook">A chama não obedece. Ainda.</p>
          <div class="title-actions">
            <button class="rpg-button primary" id="new-game" data-i18n="start">Acender a chama</button>
            <button class="rpg-button" id="continue-game" data-i18n="continue">Continuar crônica</button>
          </div>
          <p class="control-note" data-i18n="controls">WASD mover · Mouse câmera · botão do meio: roda · 5 / 6 / 7 cinto · I inventário · F interagir · F1 debug</p>
        </div>
      </section>
      <section class="rpg-hud" id="rpg-hud" aria-live="polite">
        <div class="objective-scroll"><span class="small-label" data-i18n="journey">JORNADA</span><strong id="objective-text">A chama não obedece</strong><span id="interaction-hint"></span></div>
        <div class="character-card">
          <div class="character-name"><span id="active-name">KAEL</span><small id="active-role">MAGO / BRUXO</small></div>
          <div class="meter"><i class="health" id="health-bar"></i></div>
          <div class="meter energy-meter"><i id="energy-bar"></i></div>
          <div class="magic-control" id="magic-control">CONTROLE 20 · INSTABILIDADE 0</div>
        </div>
        <div class="party-panel" id="party-panel"></div>
        <div class="skills-panel"><span><b>Q</b><i id="skill-q">Centelha</i></span><span><b>E</b><i id="skill-e">Impulso</i></span><span class="locked"><b>R</b><i>Selada</i></span></div><div class="quick-belt" id="quick-belt" aria-label="Cinto rápido de consumíveis"></div><div class="consumable-wheel" id="consumable-wheel" aria-hidden="true"></div>
        <div class="boss-card" id="boss-card"><span id="boss-name">RASTREADOR DE SANGUE</span><div class="boss-meter"><i id="boss-bar"></i></div></div>
        <div class="dialogue-card" id="dialogue-card"><div class="dialogue-portrait portrait-kael" id="dialogue-portrait" aria-hidden="true"><i></i><b></b></div><div class="dialogue-copy"><span id="dialogue-speaker"></span><p id="dialogue-text"></p><div class="dialogue-choices" id="dialogue-choices"></div></div></div>
        <div class="boss-warning" id="boss-warning" aria-live="assertive"><span id="boss-warning-kicker">TÉCNICA IMINENTE</span><strong id="boss-warning-title"></strong><p id="boss-warning-text"></p></div>
        <div class="stealth-card" id="stealth-card"><span>VIGIAS DA SENDA</span><strong id="stealth-state">OCULTO</strong><i id="stealth-detail">A luz ainda não encontrou a party.</i></div>
        <div class="faction-card" id="faction-card"><span>LANTERNA VELADA</span><strong id="faction-rank">REPUTAÇÃO 0</strong><div><i id="faction-bar"></i></div><small id="faction-status">A prova ainda não foi reconhecida.</small></div>
        <div class="rpg-notification" id="rpg-notification"></div><div class="set-activation" id="set-activation" aria-live="assertive"></div>
        <button class="grimoire-toggle" id="grimoire-toggle" aria-label="Abrir grimório">✦</button>
        <button class="inventory-toggle" id="inventory-toggle" aria-label="Abrir inventário">▣</button>
        <button class="pause-toggle" id="pause-toggle" aria-label="Abrir pausa">Ⅱ</button>
      </section>
      <section class="parchment-panel grimoire" id="grimoire-panel" aria-hidden="true">
        <button class="close-panel" data-close="grimoire-panel">×</button>
        <p class="eyebrow">OBJETO VINCULADO</p><h2>O Grimório</h2>
        <nav><button class="tab-active" data-grimoire-tab="journey">Jornada</button><button data-grimoire-tab="investigation">Investigação</button><button>Mapa</button><button>Personagens</button><button>Magia</button></nav>
        <article id="grimoire-journey"><h3>Ferrosul</h3><p>Cinquenta casas, uma ferraria, a Raposa Vermelha, a capela e uma clareira distante o bastante para guardar segredos. O grimório registra apenas o que foi vivido.</p><h3>Dheren Varenn</h3><p>Guerreiro Místico de energia dourada. Não é mestre nem pai de Kael: é o amigo que provoca, fica e luta quando o medo chega antes das palavras.</p><h3>Darion</h3><p>Ferreiro, pai adotivo e guardião do Grimório. Ele guarda respostas que Kael ainda não sabe como pedir.</p><h3>Centelha Azul</h3><p>O primeiro gesto de Kael. Instável, mas seu — e perigoso demais para Ferrosul ignorar.</p><div class="sealed-pages"><span>✦ PÁGINAS SELADAS</span><p>Visão do Fluxo, Laço Astral e runas ainda não podem ser nomeados. O Grimório só abre uma página quando Kael sobrevive para vivê-la.</p></div></article>
        <article class="investigation-page" id="grimoire-investigation" aria-hidden="true"><div class="investigation-heading"><div><span class="small-label">PÁGINAS CONECTADAS</span><h3>Trilha da Coroa</h3></div><span id="investigation-status">0 conexões</span></div><p class="investigation-hint">Selecione uma pista e depois outra para traçar um fio no Grimório. Três conexões revelam quem está observando a investigação.</p><div class="evidence-board" id="evidence-board"></div><div class="investigation-threads" id="investigation-threads"></div><article class="investigation-detail" id="investigation-detail"><span>✦</span><h3>Escolha uma pista</h3><p>O Grimório só liga aquilo que a jornada tornou visível.</p></article></article>
      </section>
      <section class="parchment-panel pause-menu" id="pause-panel" aria-hidden="true">
        <button class="close-panel" data-close="pause-panel">×</button><p class="eyebrow" data-i18n="pause">CRÔNICA EM PAUSA</p><h2 data-i18n="pauseTitle">Fogo guardado</h2>
        <nav class="pause-tabs" aria-label="Categorias de configuração" role="tablist"><button class="tab-active" data-pause-tab="game" role="tab" aria-selected="true">JOGO</button><button data-pause-tab="audio" role="tab" aria-selected="false">ÁUDIO</button><button data-pause-tab="accessibility" role="tab" aria-selected="false">ACESSO</button></nav>
        <section class="pause-tab-panel" data-pause-pane="game"><button class="rpg-button primary" id="save-game" data-i18n="save">Salvar no grimório</button><label><span data-i18n="difficulty">Dificuldade</span><select id="difficulty"><option>Historia</option><option selected>Aventura</option><option>Veterano</option><option>Lendario</option></select></label><label><span data-i18n="language">Idioma</span><select id="language"><option value="pt-BR">Português (Brasil)</option><option value="en">English</option></select></label><p data-i18n="localSave">O jogo é salvo localmente neste navegador.</p></section>
        <section class="pause-tab-panel" data-pause-pane="audio" aria-hidden="true"><section class="audio-settings"><header><span>MIXAGEM DE CAMPO</span><b>LOCAL</b></header><label for="voice-volume"><span>Voz e narração</span><output id="voice-volume-value">86%</output><input id="voice-volume" type="range" min="0" max="100" value="86"/></label><label for="sfx-volume"><span>Efeitos sonoros</span><output id="sfx-volume-value">78%</output><input id="sfx-volume" type="range" min="0" max="100" value="78"/></label><label for="music-volume"><span>Música ambiente</span><output id="music-volume-value">38%</output><input id="music-volume" type="range" min="0" max="100" value="38"/></label><p>Os três canais são guardados neste navegador e atualizados enquanto você ajusta os controles.</p></section><section class="voice-settings"><header><span data-i18n="voices">VOZES DE CENA · PT-BR</span><b data-i18n="replaceable">SUBSTITUÍVEIS</b></header><p>As falas são narradas por arquivos naturais. Use as prévias para ouvir cada assinatura de voz.</p><div><button data-preview-voice="Kael">Kael</button><button data-preview-voice="Dheren Varenn">Dheren Varenn</button><button data-preview-voice="Lyra">Lyra</button><button data-preview-voice="Mira">Mira</button></div></section></section>
        <section class="pause-tab-panel" data-pause-pane="accessibility" aria-hidden="true"><section class="accessibility-settings"><header><span>APRESENTAÇÃO E ACESSIBILIDADE</span><b>CONFIGURAÇÃO LOCAL</b></header><label><input id="narration-enabled" type="checkbox" checked/> <span>Narração natural</span><small>Reproduz somente arquivos de voz finalizados; não usa leitura robótica.</small></label><label><input id="dialogue-subtitles-enabled" type="checkbox" checked/> <span>Legendas de diálogo</span><small>Exibe e fecha cada fala ao fim do áudio ou da leitura.</small></label><label><input id="action-subtitles-enabled" type="checkbox" checked/> <span>Legendas de ações e alertas</span><small>Mostra técnicas, compras e notificações breves de combate.</small></label></section></section>
      </section>
      <section class="parchment-panel inventory-panel" id="inventory-panel" aria-hidden="true">
        <button class="close-panel" data-close="inventory-panel">×</button><p class="eyebrow">BOLSA DE JORNADA</p><h2>Inventário</h2>
        <p class="inventory-hint">Selecione um item para registrar sua utilidade na crônica.</p><section class="crafting-board" id="crafting-board"></section><div class="equipment-header"><span>ARMAS E MELHORIAS</span><b id="upgrade-tokens">FRAGMENTOS 0</b></div><div class="equipment-slots" id="equipment-slots"></div><div class="equipment-sets" id="equipment-sets"></div><div class="equipment-catalog" id="equipment-catalog"></div><div class="inventory-grid" id="inventory-grid"></div><article class="inventory-detail" id="inventory-detail"><span>✦</span><h3>Nenhum item selecionado</h3><p>As Marcas da Estrada Morta aparecem aqui depois de recolhidas.</p></article>
      </section>
      <section class="parchment-panel merchant-panel" id="merchant-panel" aria-hidden="true">
        <button class="close-panel" data-close="merchant-panel">×</button><p class="eyebrow" data-i18n="shop">COMÉRCIO DE VEYRA</p><h2 id="merchant-name">Banca de Rotas</h2><p class="merchant-intro" id="merchant-intro">Cargas preparadas para quem prefere uma rota curta a uma despedida longa.</p><div id="merchant-contents"></div>
      </section>
      <section class="debug-panel" id="debug-panel" aria-hidden="true"><strong>F1 · VIGÍLIA DO DESENVOLVEDOR</strong><div><button data-debug="heal">Curar</button><button data-debug="spawn">Invocar inimigo</button><button data-debug="kill">Eliminar inimigos</button><button data-debug="attack">Saltar para ataque</button><button data-debug="kael">Kael</button><button data-debug="dheren">Dheren</button></div></section>
      <section class="ending-screen" id="ending-screen" aria-hidden="true"><div><p class="eyebrow">CAPÍTULO SEGUINTE LIBERADO</p><h2>A CHAMA<br/>DO ÚLTIMO REINO</h2><p>Ferrosul arde atrás deles. Em Elwen, uma arqueira observa a estrada morta.</p><button class="rpg-button primary" id="ending-menu">Voltar ao grimório</button></div></section>
    `;
    document.body.appendChild(this.root);
    this.bind();
  }
  private bind() {
    this.root.querySelector<HTMLButtonElement>("#new-game")?.addEventListener("click", this.callbacks.startNewGame);
    this.root.querySelector<HTMLButtonElement>("#continue-game")?.addEventListener("click", this.callbacks.continueGame);
    this.root.querySelector<HTMLButtonElement>("#save-game")?.addEventListener("click", this.callbacks.saveGame);
    this.root.querySelector<HTMLButtonElement>("#grimoire-toggle")?.addEventListener("click", () => this.toggle("grimoire-panel"));
    this.root.querySelector<HTMLButtonElement>("#inventory-toggle")?.addEventListener("click", () => this.toggleInventory());
    this.root.querySelector<HTMLButtonElement>("#pause-toggle")?.addEventListener("click", () => this.toggle("pause-panel"));
    this.root.querySelector<HTMLButtonElement>("#ending-menu")?.addEventListener("click", () => this.showTitle());
    this.root.querySelectorAll<HTMLButtonElement>("[data-close]").forEach((button) => button.addEventListener("click", () => this.close(button.dataset.close || "")));
    this.root.querySelector<HTMLSelectElement>("#difficulty")?.addEventListener("change", (event) => this.callbacks.changeDifficulty((event.currentTarget as HTMLSelectElement).value as "Historia" | "Aventura" | "Veterano" | "Lendario"));
    this.root.querySelector<HTMLSelectElement>("#language")?.addEventListener("change", (event) => this.callbacks.changeLanguage((event.currentTarget as HTMLSelectElement).value as GameLanguage));
    ["narration-enabled", "dialogue-subtitles-enabled", "action-subtitles-enabled"].forEach((id) => this.root.querySelector<HTMLInputElement>(`#${id}`)?.addEventListener("change", () => this.emitPresentationSettings()));
    ["voice-volume", "sfx-volume", "music-volume"].forEach((id) => this.root.querySelector<HTMLInputElement>(`#${id}`)?.addEventListener("input", () => this.emitAudioSettings()));
    this.root.querySelectorAll<HTMLButtonElement>("[data-debug]").forEach((button) => button.addEventListener("click", () => this.callbacks.debugAction(button.dataset.debug || "")));
    this.root.querySelectorAll<HTMLButtonElement>("[data-preview-voice]").forEach((button) => button.addEventListener("click", () => this.callbacks.previewVoice(button.dataset.previewVoice || "Kael")));
    this.root.querySelectorAll<HTMLButtonElement>("[data-pause-tab]").forEach((button) => button.addEventListener("click", () => this.showPauseTab(button.dataset.pauseTab || "game")));
    this.root.querySelectorAll<HTMLButtonElement>("[data-grimoire-tab]").forEach((button) => button.addEventListener("click", () => this.showGrimoireTab(button.dataset.grimoireTab || "journey")));
  }
  private element<T extends HTMLElement>(id: string) { return this.root.querySelector<T>(`#${id}`); }
  showTitle() { this.element("title-screen")?.classList.remove("hidden"); this.element("ending-screen")?.setAttribute("aria-hidden", "true"); }
  beginGame() { this.element("title-screen")?.classList.add("hidden"); this.element("rpg-hud")?.classList.add("visible"); }
  setPresentationSettings(settings: { narrationEnabled: boolean; dialogueSubtitlesEnabled: boolean; actionSubtitlesEnabled: boolean }) { this.presentationSettings = settings; const narration = this.element<HTMLInputElement>("narration-enabled"); const dialogue = this.element<HTMLInputElement>("dialogue-subtitles-enabled"); const action = this.element<HTMLInputElement>("action-subtitles-enabled"); if (narration) narration.checked = settings.narrationEnabled; if (dialogue) dialogue.checked = settings.dialogueSubtitlesEnabled; if (action) action.checked = settings.actionSubtitlesEnabled; if (!settings.dialogueSubtitlesEnabled) this.clearDialogue(); if (!settings.actionSubtitlesEnabled) { this.element("rpg-notification")?.classList.remove("visible"); this.clearBossWarning(); } }
  private emitPresentationSettings() { this.callbacks.changePresentationSettings({ narrationEnabled: this.element<HTMLInputElement>("narration-enabled")?.checked ?? true, dialogueSubtitlesEnabled: this.element<HTMLInputElement>("dialogue-subtitles-enabled")?.checked ?? true, actionSubtitlesEnabled: this.element<HTMLInputElement>("action-subtitles-enabled")?.checked ?? true }); }
  setAudioSettings(settings: { voiceVolume: number; sfxVolume: number; musicVolume: number }) { (["voice", "sfx", "music"] as const).forEach((channel) => { const value = Math.round(settings[`${channel}Volume`]); const input = this.element<HTMLInputElement>(`${channel}-volume`); const output = this.element<HTMLOutputElement>(`${channel}-volume-value`); if (input) input.value = String(value); if (output) output.value = `${value}%`; }); }
  private emitAudioSettings() { const read = (id: string) => Number(this.element<HTMLInputElement>(id)?.value || 0) / 100; const settings = { voiceVolume: read("voice-volume"), sfxVolume: read("sfx-volume"), musicVolume: read("music-volume") }; this.setAudioSettings({ voiceVolume: settings.voiceVolume * 100, sfxVolume: settings.sfxVolume * 100, musicVolume: settings.musicVolume * 100 }); this.callbacks.changeAudioSettings(settings); }
  showPauseTab(tab: string) { this.activePauseTab = tab; this.root.querySelectorAll<HTMLElement>("[data-pause-pane]").forEach((panel) => panel.setAttribute("aria-hidden", String(panel.dataset.pausePane !== tab))); this.root.querySelectorAll<HTMLButtonElement>("[data-pause-tab]").forEach((button) => { const active = button.dataset.pauseTab === tab; button.classList.toggle("tab-active", active); button.setAttribute("aria-selected", String(active)); }); }
  setLanguage(language: GameLanguage) { this.l10n.setLanguage(language); this.root.querySelectorAll<HTMLElement>("[data-i18n]").forEach((element) => { const key = element.dataset.i18n; if (key) element.textContent = this.l10n.ui(key); }); const select = this.root.querySelector<HTMLSelectElement>("#language"); if (select) select.value = language; if (this.activeMerchantId) this.renderShop(this.merchantMarks); }
  openShop(merchantId: string, marks: number) { this.activeMerchantId = merchantId; this.merchantMarks = marks; this.element("merchant-panel")?.setAttribute("aria-hidden", "false"); this.renderShop(marks); }
  updateShop(marks: number) { this.merchantMarks = marks; if (this.activeMerchantId) this.renderShop(marks); }
  private renderShop(marks: number) { const name = this.element("merchant-name"); const intro = this.element("merchant-intro"); const body = this.element("merchant-contents"); if (!body || !this.activeMerchantId) return; const merchantName = this.activeMerchantId === "merchant-apothecary" ? "Boticária da Bruma" : "Tecelão de Rotas"; if (name) name.textContent = this.l10n.text(merchantName); if (intro) intro.textContent = this.l10n.text("Cargas preparadas para quem prefere uma rota curta a uma despedida longa."); const offers = Object.values(VEYRA_SHOP_OFFERS).filter((offer) => offer.merchantId === this.activeMerchantId); body.innerHTML = `<header class="merchant-wallet"><span>${this.l10n.ui("marks")}</span><b>${marks}</b></header><div class="merchant-offers">${offers.map((offer) => { const consumable = CONSUMABLES[offer.consumableId]; const canBuy = marks >= offer.price; return `<article style="--offer:${consumable.accent}"><span>${consumable.glyph}</span><div><strong>${this.l10n.text(consumable.name)}</strong><p>${this.l10n.text(offer.note)}</p><small>${this.l10n.text(consumable.combatEffect)}</small></div><button data-buy-offer="${offer.id}" ${canBuy ? "" : "disabled"}>${offer.price} ${this.l10n.ui("buy")}</button></article>`; }).join("")}</div>`; body.querySelectorAll<HTMLButtonElement>("[data-buy-offer]").forEach((button) => button.addEventListener("click", () => this.callbacks.buyConsumable(button.dataset.buyOffer || ""))); }
  showEnding() { this.element("ending-screen")?.setAttribute("aria-hidden", "false"); }
  update(state: HudState) {
    const health = Math.min(100, Math.max(0, Math.round((state.health / state.maxHealth) * 100))); const energy = Math.min(100, Math.max(0, Math.round((state.energy / state.maxEnergy) * 100)));
    const healthBar = this.element("health-bar"); const energyBar = this.element("energy-bar");
    if (healthBar) healthBar.style.width = `${health}%`; if (energyBar) energyBar.style.width = `${energy}%`;
    if (this.element("active-name")) this.element("active-name")!.textContent = state.characterName.toUpperCase();
    if (this.element("active-role")) this.element("active-role")!.textContent = state.characterRole.toUpperCase();
    if (this.element("objective-text")) this.element("objective-text")!.textContent = this.l10n.text(state.objective);
    if (this.element("magic-control")) this.element("magic-control")!.textContent = state.active === "kael" ? `CONTROLE ${state.magicControl} · INSTABILIDADE ${state.magicInstability}` : "VIGÍLIA · ENERGIA DOURADA";
    const party = this.element("party-panel"); if (party) party.innerHTML = state.party.map((id, index) => `<span class="${id === state.active ? "active" : ""}"><b>${index + 1}</b>${id === "kael" ? "Kael" : id === "dheren" ? "Dheren" : id === "lyra" ? "Lyra" : "Mira"}</span>`).join("");
    const boss = this.element("boss-card"); if (boss) { boss.classList.toggle("visible", Boolean(state.boss)); if (state.boss) { this.element("boss-name")!.textContent = state.boss.name.toUpperCase(); this.element("boss-bar")!.style.width = `${(state.boss.health / state.boss.maxHealth) * 100}%`; } }
  }
  setSkills(q: string, e: string) { if (this.element("skill-q")) this.element("skill-q")!.textContent = q; if (this.element("skill-e")) this.element("skill-e")!.textContent = e; }
  updateFaction(reputation: number, unlocked: boolean) { const faction = this.element("faction-card"); if (!faction) return; faction.classList.toggle("visible", unlocked); if (!unlocked) return; this.element("faction-rank")!.textContent = `REPUTAÇÃO ${reputation}`; this.element("faction-bar")!.style.width = `${Math.min(100, reputation)}%`; this.element("faction-status")!.textContent = reputation >= 40 ? "Aliado dos caminhos velados · itens de vigia liberados." : "Contato reconhecido · prove que sabe guardar segredos."; }
  updateInvestigation(items: string[], links: string[]) {
    const owned = new Set(items);
    const unlocked = new Set<string>();
    if (owned.has("Passe do Portão Sul de Veyra") || owned.has("Grimório Recuperado")) unlocked.add("market");
    if (owned.has("Contrato das Três Chamas")) { unlocked.add("contract"); unlocked.add("crown"); }
    if (owned.has("Moeda de Veyra Marcada")) unlocked.add("coin");
    if (owned.has("Chave de Agulha")) unlocked.add("needle");
    if (owned.has("Registro da Escriba")) unlocked.add("ledger");
    if (owned.has("Rota do Canal")) unlocked.add("canal");
    if (owned.has("Recibo da Passagem")) unlocked.add("hidden-route");
    if (owned.has("Selo da Máscara")) unlocked.add("mask-seal");
    if (owned.has("Mapa de Rotas da Guilda")) unlocked.add("guild-map");
    if (owned.has("Relato do Corredor")) unlocked.add("runner");
    if (owned.has("Ficha de Rota Rasgada")) unlocked.add("ripped-route");
    if (owned.has("Sinal da Senda Baixa")) unlocked.add("underways-sigil");
    if (owned.has("Nó de Mensageiro das Três Chamas")) unlocked.add("courier-knot");
    const fingerprint = `${items.slice().sort().join("|")}::${links.slice().sort().join("|")}`;
    if (fingerprint === this.investigationFingerprint) return;
    this.investigationFingerprint = fingerprint;
    this.investigationLinks = links;
    this.renderInvestigation(unlocked);
  }
  openInvestigation() { this.element("grimoire-panel")?.setAttribute("aria-hidden", "false"); this.showGrimoireTab("investigation"); }
  private showGrimoireTab(tab: string) {
    const investigation = tab === "investigation";
    this.element("grimoire-journey")?.setAttribute("aria-hidden", String(investigation));
    this.element("grimoire-investigation")?.setAttribute("aria-hidden", String(!investigation));
    this.root.querySelectorAll<HTMLButtonElement>("[data-grimoire-tab]").forEach((button) => button.classList.toggle("tab-active", button.dataset.grimoireTab === tab));
  }
  private renderInvestigation(unlocked: Set<string>) {
    const board = this.element("evidence-board"); if (!board) return;
    board.innerHTML = this.evidence.map((clue) => `<button class="evidence-card ${unlocked.has(clue.id) ? "unlocked" : "locked"} ${this.selectedEvidence === clue.id ? "selected" : ""}" data-evidence="${clue.id}" ${unlocked.has(clue.id) ? "" : "disabled"}><span>${clue.type}</span><strong>${clue.title}</strong><i>${unlocked.has(clue.id) ? "✦" : "SELADA"}</i></button>`).join("");
    board.querySelectorAll<HTMLButtonElement>("[data-evidence]").forEach((button) => button.addEventListener("click", () => this.selectEvidence(button.dataset.evidence || "", unlocked)));
    const status = this.element("investigation-status"); if (status) status.textContent = `${this.investigationLinks.length} ${this.investigationLinks.length === 1 ? "conexão" : "conexões"}`;
    const threads = this.element("investigation-threads"); if (threads) threads.innerHTML = this.investigationLinks.length ? this.investigationLinks.map((link) => { const [left, right] = link.split("::"); const from = this.evidence.find((clue) => clue.id === left)?.title || left; const to = this.evidence.find((clue) => clue.id === right)?.title || right; return `<span><b>✦</b>${from}<i>↝</i>${to}</span>`; }).join("") : "<p>Nenhum fio foi traçado entre estas páginas.</p>";
  }
  private selectEvidence(id: string, unlocked: Set<string>) {
    const detail = this.element("investigation-detail"); const clue = this.evidence.find((entry) => entry.id === id); if (!clue || !detail) return;
    if (!this.selectedEvidence) { this.selectedEvidence = id; detail.innerHTML = `<span>✦</span><h3>${clue.title}</h3><p>${clue.description} Agora selecione uma segunda pista para conectá-la.</p>`; this.renderInvestigation(unlocked); return; }
    if (this.selectedEvidence === id) { this.selectedEvidence = ""; detail.innerHTML = `<span>✦</span><h3>Fio desfeito</h3><p>Escolha uma pista e depois outra para registrar uma relação.</p>`; this.renderInvestigation(unlocked); return; }
    const link = [this.selectedEvidence, id].sort().join("::"); const from = this.evidence.find((entry) => entry.id === this.selectedEvidence);
    this.selectedEvidence = "";
    if (!this.investigationLinks.includes(link)) { this.investigationLinks = [...this.investigationLinks, link]; this.callbacks.updateInvestigationLinks(this.investigationLinks); }
    detail.innerHTML = `<span>✦</span><h3>Fio registrado</h3><p>${from?.title || "Pista"} agora toca ${clue.title}. A página reage à relação.</p>`;
    this.renderInvestigation(unlocked);
  }
  updateInventory(items: string[]) { const grid = this.element("inventory-grid"); if (!grid) return; const unique = Array.from(new Set(items)); grid.innerHTML = unique.length ? unique.map((item) => `<button class="inventory-item" data-item="${item.replaceAll('"', '&quot;')}"><b>${item.includes("Marca") ? "✦" : item.includes("Lenha") ? "▰" : "◈"}</b><span>${item}</span></button>`).join("") : `<p class="inventory-empty">Nenhum item de jornada foi recolhido.</p>`; grid.querySelectorAll<HTMLButtonElement>("[data-item]").forEach((button) => button.addEventListener("click", () => this.selectInventory(button.dataset.item || "")));
  }
  updateEquipment(items: string[], equipped: Record<CharacterId, Partial<Record<EquipmentSlot, string>>>, upgrades: Record<string, number>, tokens: number, active: CharacterId, sets: EquipmentSetProgress[]) { const slots = this.element("equipment-slots"); const catalog = this.element("equipment-catalog"); const setPanel = this.element("equipment-sets"); if (!slots || !catalog || !setPanel) return; const fingerprint = `${items.slice().sort().join("|")}::${JSON.stringify(equipped)}::${JSON.stringify(upgrades)}::${tokens}::${active}::${JSON.stringify(sets)}`; if (fingerprint === this.equipmentFingerprint) return; this.equipmentFingerprint = fingerprint; const owner = active; const ownerName = owner === "kael" ? "KAEL" : owner === "dheren" ? "DHEREN VARENN" : owner === "lyra" ? "LYRA" : "MIRA"; const equippedForOwner = equipped[owner] || {}; this.element("upgrade-tokens")!.textContent = `FRAGMENTOS ${tokens}`; slots.innerHTML = (["arma", "traje", "reliquia"] as EquipmentSlot[]).map((slot) => { const id = equippedForOwner[slot]; const item = id ? EQUIPMENT[id] : undefined; return `<article class="equipment-slot ${item ? `rarity-${item.rarity}` : "empty"}"><span>${slot.toUpperCase()}</span><strong>${item?.name || "Slot vazio"}</strong><small>${item ? `+${upgrades[item.id] || 0} · ${item.rarity.toUpperCase()}` : "Sem vínculo"}</small></article>`; }).join(""); setPanel.innerHTML = sets.map((set) => { const bonus = Object.entries(set.bonuses).map(([key, value]) => `${key === "health" ? "VIDA" : key === "energy" ? "ENERGIA" : key === "speed" ? "VELOCIDADE" : key === "damage" ? "DANO" : key === "abilityDamage" ? "HABILIDADE" : key === "dodge" ? "ESQUIVA" : key === "parry" ? "CONTRA" : key === "instability" ? "CONTROLE" : "FURTIVIDADE"} ${typeof value === "number" && value < 1 ? `+${Math.round(value * 100)}%` : `+${value}`}`).join(" · "); return `<article class="equipment-set ${set.activeTiers.length ? "active" : ""}"><span>COLEÇÃO ATIVA</span><strong>${set.name}</strong><b>${set.pieces}/${set.required} PEÇAS</b><p>${set.activeTiers.length ? `Bônus ${set.activeTiers.map((tier) => `${tier}P`).join(" + ")} · ${bonus}` : `Equipe ${set.required - set.pieces} peça${set.required - set.pieces === 1 ? "" : "s"} para despertar o conjunto.`}</p></article>`; }).join(""); const entries = Object.values(EQUIPMENT).filter((item) => item.owner === owner); catalog.innerHTML = `<p class="equipment-owner">${ownerName} · equipamentos vinculados</p>${entries.map((item) => { const owned = items.includes(item.name); const isEquipped = equippedForOwner[item.slot] === item.id; const level = upgrades[item.id] || 0; const modifier = Object.entries(item.bonuses).map(([key, value]) => `${key === "health" ? "VIDA" : key === "energy" ? "ENERGIA" : key === "speed" ? "VELOCIDADE" : key === "damage" ? "DANO" : key === "abilityDamage" ? "HABILIDADE" : key === "dodge" ? "ESQUIVA" : key === "parry" ? "CONTRA" : key === "instability" ? "CONTROLE" : "FURTIVIDADE"} ${typeof value === "number" && value < 1 ? `+${Math.round(value * 100)}%` : `+${value}`}`).join(" · "); return `<article class="equipment-entry rarity-${item.rarity} ${owned ? "owned" : "locked"}"><div><span>${item.collection.toUpperCase()} · ${item.rarity.toUpperCase()} · ${item.slot.toUpperCase()}</span><strong>${item.name}</strong><p>${item.description}</p><small>${modifier}</small></div><footer>${owned ? `<button data-equip-item="${item.id}" ${isEquipped ? "disabled" : ""}>${isEquipped ? "EQUIPADO" : "EQUIPAR"}</button>${isEquipped ? `<button class="upgrade" data-upgrade-item="${item.id}" ${tokens <= 0 ? "disabled" : ""}>${item.upgradeLabel} · +${level}</button>` : ""}` : "<i>AINDA NÃO OBTIDO</i>"}</footer></article>`; }).join("")}`; catalog.querySelectorAll<HTMLButtonElement>("[data-equip-item]").forEach((button) => button.addEventListener("click", () => this.callbacks.equipItem(owner, button.dataset.equipItem || ""))); catalog.querySelectorAll<HTMLButtonElement>("[data-upgrade-item]").forEach((button) => button.addEventListener("click", () => this.callbacks.upgradeEquipment(button.dataset.upgradeItem || "")));
  }
  updateCrafting(materials: Record<string, number>, crafted: string[], dailyMissions: DailyMissionState[], event: DynamicEventState, consumables: Record<ConsumableId, number>, belt: Array<ConsumableId | null>) { const board = this.element("crafting-board"); if (!board) return; const fingerprint = `${JSON.stringify(materials)}::${crafted.join("|")}::${JSON.stringify(dailyMissions)}::${JSON.stringify(event)}::${JSON.stringify(consumables)}::${belt.join("|")}`; if (fingerprint === this.craftingFingerprint) return; this.craftingFingerprint = fingerprint; const materialRows = Object.entries(MATERIALS).map(([id, material]) => `<span style="--material:${material.accent}"><b>${materials[id] || 0}</b>${material.name}<small>${material.route}</small></span>`).join(""); const beltSlots = belt.map((equipped, index) => `<label class="belt-loadout-slot"><span>SLOT ${index + 1} · ${index + 5}</span><select data-belt-slot="${index}">${Object.values(CONSUMABLES).map((consumable) => `<option value="${consumable.id}" ${equipped === consumable.id ? "selected" : ""}>${consumable.glyph} ${consumable.name} · ${consumables[consumable.id]}</option>`).join("")}</select></label>`).join(""); const recipes = Object.values(CRAFT_RECIPES).map((recipe) => { const ready = Object.entries(recipe.materials).every(([id, amount]) => (materials[id] || 0) >= amount); const cost = Object.entries(recipe.materials).map(([id, amount]) => `${MATERIALS[id]?.name || id} ×${amount}`).join(" · "); return `<article class="craft-recipe ${ready ? "ready" : ""}"><span>RECEITA ${crafted.includes(recipe.id) ? "DOMINADA" : "DESCUBERTA"}</span><strong>${recipe.name}</strong><p>${recipe.description}</p><small>${cost}</small><button data-craft="${recipe.id}" ${ready ? "" : "disabled"}>${ready ? "CRIAR" : "MATERIAIS INSUFICIENTES"}</button></article>`; }).join(""); const dailies = dailyMissions.map((mission) => `<article class="daily-mission ${mission.completed ? "done" : ""}"><b>${mission.completed ? "✓" : `${mission.progress}/${mission.target}`}</b><div><strong>${mission.title}</strong><p>${mission.objective}</p><small>RECOMPENSA · ${mission.reward}</small></div></article>`).join(""); board.innerHTML = `<header><span>OFICINA DE ROTA</span><b>MATERIAIS RAROS</b></header><div class="material-pouch">${materialRows}</div><section class="belt-loadout"><header><span>CINTO RÁPIDO</span><b>5 · 6 · 7 EM COMBATE</b></header><div>${beltSlots}</div><p>Equipada a mistura, use o atalho correspondente durante qualquer combate ou evento dinâmico.</p></section><div class="craft-recipes">${recipes}</div><header><span>CICLO DE VEYRA</span><b>RENOVA AO ABRIR A CRÔNICA</b></header><div class="daily-list">${dailies}</div><article class="dynamic-event ${event.completed ? "done" : event.active ? "active" : ""}"><span>EVENTO DINÂMICO</span><strong>${event.title || "A cidade aguarda a próxima mudança de turno."}</strong><p>${event.objective || "Explore Veyra para encontrar uma ocorrência nova."}</p></article>`; board.querySelectorAll<HTMLButtonElement>("[data-craft]").forEach((button) => button.addEventListener("click", () => this.callbacks.craftRecipe(button.dataset.craft || ""))); board.querySelectorAll<HTMLSelectElement>("[data-belt-slot]").forEach((select) => select.addEventListener("change", () => this.callbacks.equipQuickBelt(Number(select.dataset.beltSlot || 0), select.value as ConsumableId))); }
  updateQuickBelt(belt: Array<ConsumableId | null>, consumables: Record<ConsumableId, number>) { const panel = this.element("quick-belt"); if (!panel) return; const fingerprint = `${belt.join("|")}::${JSON.stringify(consumables)}`; if (fingerprint === this.beltFingerprint) return; this.beltFingerprint = fingerprint; panel.innerHTML = belt.map((id, index) => { const item = id ? CONSUMABLES[id] : undefined; return `<article class="quick-belt-slot ${item && consumables[item.id] > 0 ? "ready" : "empty"}" style="--belt:${item?.accent || "#6e7569"}"><b>${index + 5}</b><span>${item?.glyph || "·"}</span><small>${item ? consumables[item.id] : 0}</small><i>${item?.name || "Vazio"}</i></article>`; }).join(""); }
  updateConsumableWheel(belt: Array<ConsumableId | null>, consumables: Record<ConsumableId, number>, selected: number, visible: boolean) { const wheel = this.element("consumable-wheel"); if (!wheel) return; wheel.setAttribute("aria-hidden", String(!visible)); if (!visible) return; wheel.innerHTML = `<span class="wheel-hint">SOLTE O BOTÃO<br/>PARA USAR</span>${belt.map((id, index) => { const item = id ? CONSUMABLES[id] : undefined; return `<article class="wheel-option ${index === selected ? "selected" : ""} ${item && consumables[item.id] > 0 ? "ready" : "empty"}" style="--wheel:${item?.accent || "#6e7569"}; --slot:${index}"><b>${item?.glyph || "·"}</b><strong>${item?.name || "Slot vazio"}</strong><small>${item ? `${consumables[item.id]} carga${consumables[item.id] === 1 ? "" : "s"}` : "sem mistura"}</small></article>`; }).join("")}`; }
  highlightCompleteSets(sets: EquipmentSetProgress[]) { const panel = this.element("equipment-sets"); if (!panel) return; const complete = new Set(sets.filter((set) => set.pieces >= set.required).map((set) => set.id)); panel.querySelectorAll<HTMLElement>(".equipment-set").forEach((node) => { const set = sets.find((entry) => node.textContent?.includes(entry.name)); if (!set) return; const isComplete = complete.has(set.id); node.classList.toggle("complete", isComplete); if (!isComplete) return; const key = `${set.id}:${set.pieces}`; if (this.completedSetKeys.has(key)) return; this.completedSetKeys.add(key); node.classList.remove("awakening"); void node.offsetWidth; node.classList.add("awakening"); const activation = this.element("set-activation"); if (activation) { activation.innerHTML = `<span>✦ CONJUNTO COMPLETO ✦</span><strong>${set.name}</strong><p>${set.pieces} peças sincronizadas · bônus despertados</p>`; activation.classList.add("visible"); window.clearTimeout(this.setActivationTimer); this.setActivationTimer = window.setTimeout(() => activation.classList.remove("visible"), 2600); } this.notify(`Conjunto completo: ${set.name}. Os bônus da coleção despertaram.`); }); this.completedSetKeys.forEach((key) => { if (!Array.from(complete).some((id) => key.startsWith(`${id}:`))) this.completedSetKeys.delete(key); }); }
  private selectInventory(item: string) { const detail = this.element("inventory-detail"); if (!detail) return; const description = item.includes("Marca") ? "Uma marca verde-pálida deixada na Estrada Morta. Lyra a usa para confirmar que a party ainda segue a rota segura." : item.includes("Lenha") ? "Lenha marcada para Darion. O cheiro de resina lembra Ferrosul, mesmo longe da ferraria." : item.includes("Ampola de Bruma") ? "Recompensa do canal. A bruma restaura energia e alonga a esquiva de Mira." : item.includes("Agulha de Cobre") ? "Recompensa da passagem lateral. O cobre fino reforça os golpes básicos de Mira." : item.includes("Selo da Máscara") ? "Prova tomada do Arauto Mascarado. A Guilda dos Caminhos pode reconhecer sua rota de origem." : item.includes("Mapa de Rotas") ? "Registro oficial com atalhos que não constam nas placas de Veyra." : item.includes("Relato do Corredor") ? "Testemunho de uma carga sem nome que atravessou as portas internas da Guilda." : item.includes("Ficha de Rota") ? "Papel rasgado que aponta para uma descida selada sob a Guilda dos Caminhos." : item.includes("Senda Baixa") ? "Sinal de sal e cobre usado por mensageiros para marcar portas subterrâneas." : item.includes("Nó de Mensageiro") ? "Prova que a rede das três chamas atravessa os subterrâneos de Veyra." : "Registro ligado à jornada atual."; detail.innerHTML = `<span>✦</span><h3>${item}</h3><p>${description}</p>`; this.root.querySelectorAll(".inventory-item").forEach((button) => button.classList.toggle("selected", (button as HTMLElement).dataset.item === item)); }
  toggleInventory() { this.toggle("inventory-panel"); }
  setInteraction(text: string) { const element = this.element("interaction-hint"); if (element) element.textContent = this.l10n.text(text); }
  dialogue(speaker: string, text: string) { const card = this.element("dialogue-card"); if (!card) return; window.clearTimeout(this.dialogueTimer); const portrait = this.element("dialogue-portrait"); const key = speaker.toLocaleLowerCase("pt-BR").normalize("NFD").replace(/[\u0300-\u036f]/g, "").split(" ")[0]; if (portrait) portrait.className = `dialogue-portrait portrait-${["kael", "dheren", "darion", "lyra", "mira"].includes(key) ? key : "kael"}`; this.element("dialogue-speaker")!.textContent = speaker.toUpperCase(); const localized = this.l10n.text(text); this.element("dialogue-text")!.textContent = localized; const choices = this.element("dialogue-choices"); if (choices) choices.innerHTML = ""; card.classList.toggle("visible", this.presentationSettings.dialogueSubtitlesEnabled); const finish = () => this.clearDialogue(); const played = this.l10n.getLanguage() === "pt-BR" && this.presentationSettings.narrationEnabled ? this.callbacks.playDialogueAudio(speaker, text, finish) : false; if (this.presentationSettings.dialogueSubtitlesEnabled) this.dialogueTimer = window.setTimeout(finish, played ? 12000 : Math.max(2200, Math.min(7000, localized.length * 55 + 1200))); }
  guildChoice(speaker: string, prompt: string) { this.dialogue(speaker, prompt); const choices = this.element("dialogue-choices"); if (!choices) return; choices.innerHTML = `<button data-guild-choice="mapas">Perguntar pelas rotas apagadas</button><button data-guild-choice="nomes">Perguntar por quem pagou</button>`; choices.querySelectorAll<HTMLButtonElement>("[data-guild-choice]").forEach((button) => button.addEventListener("click", () => { choices.innerHTML = ""; this.callbacks.chooseGuildClue(button.dataset.guildChoice as "mapas" | "nomes"); })); }
  bossWarning(title: string, text: string, tone: "ash" | "mirror") { if (!this.presentationSettings.actionSubtitlesEnabled) return; const warning = this.element("boss-warning"); if (!warning) return; window.clearTimeout(this.bossWarningTimer); warning.className = `boss-warning visible ${tone}`; this.element("boss-warning-title")!.textContent = title; this.element("boss-warning-text")!.textContent = text; this.bossWarningTimer = window.setTimeout(() => this.clearBossWarning(), 3600); }
  clearBossWarning() { window.clearTimeout(this.bossWarningTimer); this.element("boss-warning")?.classList.remove("visible"); }
  setStealthState(visible: boolean, active: boolean) { const card = this.element("stealth-card"); if (!card) return; card.classList.toggle("visible", active); card.classList.toggle("alert", visible); if (active) { this.element("stealth-state")!.textContent = visible ? "VISTO" : "OCULTO"; this.element("stealth-detail")!.textContent = visible ? "Os vigias soaram o corredor." : "Fique fora dos cones de luz."; } }
  partyReactions(lines: Array<{ speaker: string; text: string }>) { this.partyReactionTimers.forEach((timer) => window.clearTimeout(timer)); this.partyReactionTimers = lines.map((line, index) => window.setTimeout(() => this.dialogue(line.speaker, line.text), 650 + index * 1250)); }
  clearDialogue() { window.clearTimeout(this.dialogueTimer); this.element("dialogue-card")?.classList.remove("visible"); }
  notify(text: string) { if (!this.presentationSettings.actionSubtitlesEnabled) return; const notice = this.element("rpg-notification"); if (!notice) return; notice.textContent = this.l10n.text(text); notice.classList.add("visible"); window.clearTimeout(this.notificationTimer); this.notificationTimer = window.setTimeout(() => notice.classList.remove("visible"), 2800); }
  toggle(id: string) { const panel = this.element(id); if (!panel) return; panel.getAttribute("aria-hidden") === "false" ? this.close(id) : panel.setAttribute("aria-hidden", "false"); }
  close(id: string) { this.element(id)?.setAttribute("aria-hidden", "true"); }
  toggleDebug() { this.toggle("debug-panel"); }
  dispose() { this.partyReactionTimers.forEach((timer) => window.clearTimeout(timer)); window.clearTimeout(this.dialogueTimer); window.clearTimeout(this.bossWarningTimer); this.root.remove(); }
}
