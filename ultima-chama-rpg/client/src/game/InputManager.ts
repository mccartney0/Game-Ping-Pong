/**
 * Gravura de Cinzas — entrada semântica para movimento, magia e câmera de terceira pessoa.
 */
export type GameAction = "forward" | "back" | "left" | "right" | "sprint" | "dodge" | "attack" | "special" | "ability1" | "ability2" | "ultimate" | "interact" | "lock" | "kael" | "dheren" | "lyra" | "mira" | "belt1" | "belt2" | "belt3" | "beltWheel" | "inventory" | "menu" | "debug";

const keyBindings: Record<string, GameAction> = {
  w: "forward", s: "back", a: "left", d: "right", shift: "sprint", " ": "dodge", q: "ability1", e: "ability2", r: "ultimate", f: "interact", tab: "lock", "1": "kael", "2": "dheren", "3": "lyra", "4": "mira", "5": "belt1", "6": "belt2", "7": "belt3", i: "inventory", escape: "menu", f1: "debug",
};

export class InputManager {
  private held = new Set<GameAction>();
  private pressed = new Set<GameAction>();
  private lookX = 0;
  private lookY = 0;
  private wheelX = 0;
  private wheelY = 0;
  private readonly onKeyDown: (event: KeyboardEvent) => void;
  private readonly onKeyUp: (event: KeyboardEvent) => void;
  private readonly onPointerDown: (event: PointerEvent) => void;
  private readonly onPointerUp: (event: PointerEvent) => void;
  private readonly onPointerMove: (event: PointerEvent) => void;
  private readonly onContextMenu: (event: MouseEvent) => void;

  constructor(private readonly canvas: HTMLCanvasElement) {
    this.onKeyDown = (event) => {
      const action = keyBindings[event.key.toLowerCase()];
      if (!action) return;
      if (["tab", " ", "f1"].includes(event.key.toLowerCase())) event.preventDefault();
      if (!this.held.has(action)) this.pressed.add(action);
      this.held.add(action);
    };
    this.onKeyUp = (event) => {
      const action = keyBindings[event.key.toLowerCase()];
      if (action) this.held.delete(action);
    };
    this.onPointerDown = (event) => {
      if (event.button === 0) this.pressed.add("attack");
      if (event.button === 2) this.pressed.add("special");
      if (event.button === 1) { event.preventDefault(); this.held.add("beltWheel"); this.wheelX = 0; this.wheelY = 0; }
      if (document.pointerLockElement !== canvas) canvas.requestPointerLock?.();
    };
    this.onPointerUp = (event) => { if (event.button === 1) this.held.delete("beltWheel"); };
    this.onPointerMove = (event) => {
      if (document.pointerLockElement === canvas) {
        if (this.held.has("beltWheel")) { this.wheelX += event.movementX; this.wheelY += event.movementY; }
        else { this.lookX += event.movementX; this.lookY += event.movementY; }
      }
    };
    this.onContextMenu = (event) => event.preventDefault();
    window.addEventListener("keydown", this.onKeyDown);
    window.addEventListener("keyup", this.onKeyUp);
    canvas.addEventListener("pointerdown", this.onPointerDown);
    canvas.addEventListener("pointerup", this.onPointerUp);
    canvas.addEventListener("pointermove", this.onPointerMove);
    canvas.addEventListener("contextmenu", this.onContextMenu);
  }

  isDown(action: GameAction) { return this.held.has(action); }
  consume(action: GameAction) { const active = this.pressed.has(action); this.pressed.delete(action); return active; }
  takeLookDelta() { const value = { x: this.lookX, y: this.lookY }; this.lookX = 0; this.lookY = 0; return value; }
  takeWheelDelta() { const value = { x: this.wheelX, y: this.wheelY }; this.wheelX = 0; this.wheelY = 0; return value; }
  dispose() {
    window.removeEventListener("keydown", this.onKeyDown);
    window.removeEventListener("keyup", this.onKeyUp);
    this.canvas.removeEventListener("pointerdown", this.onPointerDown);
    this.canvas.removeEventListener("pointerup", this.onPointerUp);
    this.canvas.removeEventListener("pointermove", this.onPointerMove);
    this.canvas.removeEventListener("contextmenu", this.onContextMenu);
  }
}
