/**
 * Gravura de Cinzas — HUD discreto de pergaminho e cinza; o mundo 3D continua dominante.
 */
import type { CharacterId, NarrativeStage } from "./types";

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
}

export interface UICallbacks {
  startNewGame: () => void;
  continueGame: () => void;
  saveGame: () => void;
  changeDifficulty: (value: "Historia" | "Aventura" | "Veterano" | "Lendario") => void;
  debugAction: (action: string) => void;
}

export class UIController {
  private readonly root: HTMLDivElement;
  private notificationTimer = 0;
  constructor(private readonly callbacks: UICallbacks) {
    this.root = document.createElement("div");
    this.root.className = "rpg-overlay";
    this.root.innerHTML = `
      <section class="rpg-title-screen" id="title-screen">
        <div class="title-ash"></div>
        <div class="title-content">
          <div class="rune-mark" aria-hidden="true"><span></span></div>
          <p class="eyebrow">LIVRO I · VERTICAL SLICE</p>
          <h1>A CHAMA<br/><em>DO ÚLTIMO</em> REINO</h1>
          <p class="title-hook">A chama não obedece. Ainda.</p>
          <div class="title-actions">
            <button class="rpg-button primary" id="new-game">Acender a chama</button>
            <button class="rpg-button" id="continue-game">Continuar crônica</button>
          </div>
          <p class="control-note">WASD mover · Mouse câmera · Q / E magia · F interagir · F1 debug</p>
        </div>
      </section>
      <section class="rpg-hud" id="rpg-hud" aria-live="polite">
        <div class="objective-scroll"><span class="small-label">JORNADA</span><strong id="objective-text">A chama não obedece</strong><span id="interaction-hint"></span></div>
        <div class="character-card">
          <div class="character-name"><span id="active-name">KAEL</span><small id="active-role">MAGO / BRUXO</small></div>
          <div class="meter"><i class="health" id="health-bar"></i></div>
          <div class="meter energy-meter"><i id="energy-bar"></i></div>
          <div class="magic-control" id="magic-control">CONTROLE 20 · INSTABILIDADE 0</div>
        </div>
        <div class="party-panel" id="party-panel"></div>
        <div class="skills-panel"><span><b>Q</b><i id="skill-q">Centelha</i></span><span><b>E</b><i id="skill-e">Impulso</i></span><span class="locked"><b>R</b><i>Selada</i></span></div>
        <div class="boss-card" id="boss-card"><span id="boss-name">RASTREADOR DE SANGUE</span><div class="boss-meter"><i id="boss-bar"></i></div></div>
        <div class="dialogue-card" id="dialogue-card"><span id="dialogue-speaker"></span><p id="dialogue-text"></p></div>
        <div class="rpg-notification" id="rpg-notification"></div>
        <button class="grimoire-toggle" id="grimoire-toggle" aria-label="Abrir grimório">✦</button>
        <button class="pause-toggle" id="pause-toggle" aria-label="Abrir pausa">Ⅱ</button>
      </section>
      <section class="parchment-panel grimoire" id="grimoire-panel" aria-hidden="true">
        <button class="close-panel" data-close="grimoire-panel">×</button>
        <p class="eyebrow">OBJETO VINCULADO</p><h2>O Grimório</h2>
        <nav><button class="tab-active">Jornada</button><button>Mapa</button><button>Personagens</button><button>Criaturas</button><button>Magia</button></nav>
        <article><h3>Ferrosul</h3><p>Uma vila de ferro, pinho e promessas que não duram. O grimório registra apenas o que foi vivido.</p><h3>Dheren Varenn</h3><p>Espada dourada, olhos atentos e respostas que sempre chegam um pouco tarde demais.</p><h3>Centelha Azul</h3><p>O primeiro gesto de Kael. Instável, mas seu.</p></article>
      </section>
      <section class="parchment-panel pause-menu" id="pause-panel" aria-hidden="true">
        <button class="close-panel" data-close="pause-panel">×</button><p class="eyebrow">CRÔNICA EM PAUSA</p><h2>Fogo guardado</h2>
        <button class="rpg-button primary" id="save-game">Salvar no grimório</button>
        <label>Dificuldade<select id="difficulty"><option>Historia</option><option selected>Aventura</option><option>Veterano</option><option>Lendario</option></select></label>
        <p>O jogo é salvo localmente neste navegador.</p>
      </section>
      <section class="debug-panel" id="debug-panel" aria-hidden="true"><strong>F1 · VIGÍLIA DO DESENVOLVEDOR</strong><div><button data-debug="heal">Curar</button><button data-debug="spawn">Invocar inimigo</button><button data-debug="kill">Eliminar inimigos</button><button data-debug="attack">Saltar para ataque</button><button data-debug="kael">Kael</button><button data-debug="dheren">Dheren</button></div></section>
      <section class="ending-screen" id="ending-screen" aria-hidden="true"><div><p class="eyebrow">FIM DO VERTICAL SLICE</p><h2>A CHAMA<br/>DO ÚLTIMO REINO</h2><p>Ferrosul arde atrás deles. Elwen aguarda à frente.</p><button class="rpg-button primary" id="ending-menu">Voltar ao grimório</button></div></section>
    `;
    document.body.appendChild(this.root);
    this.bind();
  }
  private bind() {
    this.root.querySelector<HTMLButtonElement>("#new-game")?.addEventListener("click", this.callbacks.startNewGame);
    this.root.querySelector<HTMLButtonElement>("#continue-game")?.addEventListener("click", this.callbacks.continueGame);
    this.root.querySelector<HTMLButtonElement>("#save-game")?.addEventListener("click", this.callbacks.saveGame);
    this.root.querySelector<HTMLButtonElement>("#grimoire-toggle")?.addEventListener("click", () => this.toggle("grimoire-panel"));
    this.root.querySelector<HTMLButtonElement>("#pause-toggle")?.addEventListener("click", () => this.toggle("pause-panel"));
    this.root.querySelector<HTMLButtonElement>("#ending-menu")?.addEventListener("click", () => this.showTitle());
    this.root.querySelectorAll<HTMLButtonElement>("[data-close]").forEach((button) => button.addEventListener("click", () => this.close(button.dataset.close || "")));
    this.root.querySelector<HTMLSelectElement>("#difficulty")?.addEventListener("change", (event) => this.callbacks.changeDifficulty((event.currentTarget as HTMLSelectElement).value as "Historia" | "Aventura" | "Veterano" | "Lendario"));
    this.root.querySelectorAll<HTMLButtonElement>("[data-debug]").forEach((button) => button.addEventListener("click", () => this.callbacks.debugAction(button.dataset.debug || "")));
  }
  private element<T extends HTMLElement>(id: string) { return this.root.querySelector<T>(`#${id}`); }
  showTitle() { this.element("title-screen")?.classList.remove("hidden"); this.element("ending-screen")?.setAttribute("aria-hidden", "true"); }
  beginGame() { this.element("title-screen")?.classList.add("hidden"); this.element("rpg-hud")?.classList.add("visible"); }
  showEnding() { this.element("ending-screen")?.setAttribute("aria-hidden", "false"); }
  update(state: HudState) {
    const health = Math.max(0, Math.round((state.health / state.maxHealth) * 100)); const energy = Math.max(0, Math.round((state.energy / state.maxEnergy) * 100));
    const healthBar = this.element("health-bar"); const energyBar = this.element("energy-bar");
    if (healthBar) healthBar.style.width = `${health}%`; if (energyBar) energyBar.style.width = `${energy}%`;
    if (this.element("active-name")) this.element("active-name")!.textContent = state.characterName.toUpperCase();
    if (this.element("active-role")) this.element("active-role")!.textContent = state.characterRole.toUpperCase();
    if (this.element("objective-text")) this.element("objective-text")!.textContent = state.objective;
    if (this.element("magic-control")) this.element("magic-control")!.textContent = state.active === "kael" ? `CONTROLE ${state.magicControl} · INSTABILIDADE ${state.magicInstability}` : "VIGÍLIA · ENERGIA DOURADA";
    const party = this.element("party-panel"); if (party) party.innerHTML = state.party.map((id, index) => `<span class="${id === state.active ? "active" : ""}"><b>${index + 1}</b>${id === "kael" ? "Kael" : id === "dheren" ? "Dheren" : id === "lyra" ? "Lyra" : "Mira"}</span>`).join("");
    const boss = this.element("boss-card"); if (boss) { boss.classList.toggle("visible", Boolean(state.boss)); if (state.boss) { this.element("boss-name")!.textContent = state.boss.name.toUpperCase(); this.element("boss-bar")!.style.width = `${(state.boss.health / state.boss.maxHealth) * 100}%`; } }
  }
  setSkills(q: string, e: string) { if (this.element("skill-q")) this.element("skill-q")!.textContent = q; if (this.element("skill-e")) this.element("skill-e")!.textContent = e; }
  setInteraction(text: string) { const element = this.element("interaction-hint"); if (element) element.textContent = text; }
  dialogue(speaker: string, text: string) { const card = this.element("dialogue-card"); if (!card) return; this.element("dialogue-speaker")!.textContent = speaker.toUpperCase(); this.element("dialogue-text")!.textContent = text; card.classList.add("visible"); }
  clearDialogue() { this.element("dialogue-card")?.classList.remove("visible"); }
  notify(text: string) { const notice = this.element("rpg-notification"); if (!notice) return; notice.textContent = text; notice.classList.add("visible"); window.clearTimeout(this.notificationTimer); this.notificationTimer = window.setTimeout(() => notice.classList.remove("visible"), 2800); }
  toggle(id: string) { const panel = this.element(id); if (!panel) return; panel.getAttribute("aria-hidden") === "false" ? this.close(id) : panel.setAttribute("aria-hidden", "false"); }
  close(id: string) { this.element(id)?.setAttribute("aria-hidden", "true"); }
  toggleDebug() { this.toggle("debug-panel"); }
  dispose() { this.root.remove(); }
}
