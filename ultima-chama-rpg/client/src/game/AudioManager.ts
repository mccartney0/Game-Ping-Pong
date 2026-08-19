/**
 * Gravura de Cinzas — efeitos sintetizados e vozes naturais PT-BR substituíveis.
 * Para trocar uma fala final, altere DIALOGUE_AUDIO_OVERRIDES para um novo arquivo em /manus-storage.
 */
export type VoiceSpeaker = "Kael" | "Dheren" | "Dheren Varenn" | "Lyra" | "Mira" | "Boticária" | "Tecelão" | "Darion" | "Escriba" | "Barqueiro" | "Arauto Mascarado";

const VOICE_PROFILES: Record<VoiceSpeaker, { sample: string }> = {
  Kael: { sample: "/manus-storage/kael-natural-forest-v2_0343a07b.wav" },
  Dheren: { sample: "/manus-storage/dheren-natural-guard_c87b4aae.wav" },
  "Dheren Varenn": { sample: "/manus-storage/dheren-natural-guard_c87b4aae.wav" },
  Lyra: { sample: "/manus-storage/lyra-natural-road_395faa0c.wav" },
  Mira: { sample: "/manus-storage/mira-natural-trail-v2_119e8bac.wav" },
  Boticária: { sample: "/manus-storage/mira-natural-trail-v2_119e8bac.wav" },
  Tecelão: { sample: "/manus-storage/dheren-natural-guard_c87b4aae.wav" },
  Darion: { sample: "/manus-storage/dheren-natural-guard_c87b4aae.wav" },
  Escriba: { sample: "/manus-storage/lyra-natural-road_395faa0c.wav" },
  Barqueiro: { sample: "/manus-storage/dheren-natural-guard_c87b4aae.wav" },
  "Arauto Mascarado": { sample: "/manus-storage/dheren-natural-guard_c87b4aae.wav" },
};

const DIALOGUE_AUDIO_OVERRIDES: Record<string, string> = {
  "Dheren::Fica atrás de mim, Kael. Pela primeira vez, tenta não queimar a vila inteira.": "/manus-storage/dheren-natural-guard_c87b4aae.wav",
  "Dheren Varenn::Fica atrás de mim, Kael. Pela primeira vez, tenta não queimar a vila inteira.": "/manus-storage/dheren-natural-guard_c87b4aae.wav",
  "Kael::A floresta. Agora.": "/manus-storage/kael-natural-forest-v2_0343a07b.wav",
  "Lyra::Não me façam repetir: sigam a estrada e mantenham a chama baixa.": "/manus-storage/lyra-natural-road_395faa0c.wav",
  "Mira::O livro fica com vocês. Eu fico com a trilha.": "/manus-storage/mira-natural-trail-v2_119e8bac.wav",
};

export class AudioManager {
  private context: AudioContext | null = null;
  private sfxGain: GainNode | null = null;
  private musicGain: GainNode | null = null;
  private musicSources: OscillatorNode[] = [];
  private narrationEnabled = true;
  private activeNarration: HTMLAudioElement | null = null;
  private voiceVolume = .86;
  private sfxVolume = .78;
  private musicVolume = .38;
  async unlock() {
    if (!this.context) this.context = new AudioContext();
    if (this.context.state === "suspended") await this.context.resume();
    this.ensureMixers(); this.startAmbientMusic();
  }
  private ensureMixers() { if (!this.context || this.sfxGain || this.musicGain) return; this.sfxGain = this.context.createGain(); this.musicGain = this.context.createGain(); this.sfxGain.connect(this.context.destination); this.musicGain.connect(this.context.destination); this.sfxGain.gain.value = this.sfxVolume; this.musicGain.gain.value = this.musicVolume * .055; }
  private startAmbientMusic() { if (!this.context || this.musicSources.length) return; const frequencies = [73.42, 110]; this.musicSources = frequencies.map((frequency, index) => { const oscillator = this.context!.createOscillator(); const gain = this.context!.createGain(); oscillator.type = index ? "sine" : "triangle"; oscillator.frequency.value = frequency; gain.gain.value = index ? .42 : .25; oscillator.connect(gain).connect(this.musicGain!); oscillator.start(); return oscillator; }); }
  tone(frequency: number, duration: number, type: OscillatorType = "sine", volume = 0.035) {
    if (!this.context || this.context.state !== "running") return;
    const oscillator = this.context.createOscillator();
    const gain = this.context.createGain();
    oscillator.type = type; oscillator.frequency.setValueAtTime(frequency, this.context.currentTime);
    gain.gain.setValueAtTime(volume, this.context.currentTime); gain.gain.exponentialRampToValueAtTime(0.001, this.context.currentTime + duration);
    oscillator.connect(gain).connect(this.sfxGain ?? this.context.destination); oscillator.start(); oscillator.stop(this.context.currentTime + duration);
  }
  impact(weight = 1, bright = false) {
    if (!this.context || this.context.state !== "running") return;
    const now = this.context.currentTime;
    const oscillator = this.context.createOscillator(); const gain = this.context.createGain();
    oscillator.type = bright ? "triangle" : "sawtooth"; oscillator.frequency.setValueAtTime(bright ? 510 : 150, now); oscillator.frequency.exponentialRampToValueAtTime(bright ? 180 : 48, now + 0.13 * weight);
    gain.gain.setValueAtTime(0.045 * weight, now); gain.gain.exponentialRampToValueAtTime(0.001, now + 0.15 * weight);
    oscillator.connect(gain).connect(this.sfxGain ?? this.context.destination); oscillator.start(now); oscillator.stop(now + 0.16 * weight);
  }
  magic(kind: "arcane" | "arrow" | "needle" | "gold") { const notes = { arcane: [420, 620], arrow: [720, 980], needle: [840, 1140], gold: [360, 620] }[kind]; this.tone(notes[0], .12, "triangle", .045); window.setTimeout(() => this.tone(notes[1], .16, kind === "arcane" ? "sine" : "triangle", .03), 45); }
  consumable(kind: "bruma" | "sal" | "fio") { const notes = { bruma: [560, 760], sal: [230, 380], fio: [760, 1030] }[kind]; this.tone(notes[0], .13, "triangle", .06); window.setTimeout(() => this.tone(notes[1], .18, "sine", .045), 65); }
  purchase() { this.tone(620, .09, "triangle", .05); window.setTimeout(() => this.tone(880, .15, "sine", .04), 70); }
  setNarrationEnabled(enabled: boolean) { this.narrationEnabled = enabled; if (!enabled) this.stopNarration(); }
  setMixVolumes(settings: { voiceVolume: number; sfxVolume: number; musicVolume: number }) { this.voiceVolume = Math.min(1, Math.max(0, settings.voiceVolume)); this.sfxVolume = Math.min(1, Math.max(0, settings.sfxVolume)); this.musicVolume = Math.min(1, Math.max(0, settings.musicVolume)); if (this.context) { this.sfxGain?.gain.setTargetAtTime(this.sfxVolume, this.context.currentTime, .03); this.musicGain?.gain.setTargetAtTime(this.musicVolume * .055, this.context.currentTime, .06); } if (this.activeNarration) this.activeNarration.volume = this.voiceVolume; }
  speak(speaker: string, text: string, onComplete: () => void) { if (!this.narrationEnabled || !text.trim()) return false; const source = DIALOGUE_AUDIO_OVERRIDES[`${speaker}::${text}`]; if (!source) return false; this.stopNarration(); const audio = new Audio(source); this.activeNarration = audio; audio.volume = this.voiceVolume; audio.onended = () => { if (this.activeNarration === audio) this.activeNarration = null; onComplete(); }; audio.onerror = () => { if (this.activeNarration === audio) this.activeNarration = null; onComplete(); }; void audio.play().catch(() => onComplete()); return true; }
  previewVoice(speaker: string) { const profile = VOICE_PROFILES[speaker as VoiceSpeaker] ?? VOICE_PROFILES.Kael; this.stopNarration(); const audio = new Audio(profile.sample); this.activeNarration = audio; audio.volume = this.voiceVolume; audio.onended = () => { if (this.activeNarration === audio) this.activeNarration = null; }; void audio.play().catch(() => undefined); }
  private stopNarration() { if (!this.activeNarration) return; this.activeNarration.pause(); this.activeNarration.currentTime = 0; this.activeNarration = null; }
  dispose() { this.stopNarration(); this.musicSources.forEach((source) => source.stop()); this.musicSources = []; void this.context?.close(); this.context = null; this.sfxGain = null; this.musicGain = null; }
}
