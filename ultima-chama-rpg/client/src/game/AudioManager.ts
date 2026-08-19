/**
 * Gravura de Cinzas — efeitos sintetizados e direção de voz PT-BR substituível.
 * Para trocar vozes no futuro, altere VOICE_ASSET_OVERRIDES para novos arquivos em /manus-storage.
 */
export type VoiceSpeaker = "Kael" | "Dheren" | "Dheren Varenn" | "Lyra" | "Mira" | "Boticária" | "Tecelão" | "Darion" | "Escriba" | "Barqueiro" | "Arauto Mascarado";

const VOICE_PROFILES: Record<VoiceSpeaker, { rate: number; pitch: number; sample: string }> = {
  Kael: { rate: .98, pitch: 1.08, sample: "/manus-storage/kael-ptbr-voice-sample_3f10b14f.wav" },
  Dheren: { rate: .92, pitch: .78, sample: "/manus-storage/dheren-ptbr-voice-sample_f56da05b.wav" },
  "Dheren Varenn": { rate: .92, pitch: .78, sample: "/manus-storage/dheren-ptbr-voice-sample_f56da05b.wav" },
  Lyra: { rate: .96, pitch: 1.13, sample: "/manus-storage/lyra-ptbr-voice-sample_34250906.wav" },
  Mira: { rate: 1.05, pitch: 1.03, sample: "/manus-storage/mira-ptbr-voice-sample_3603e475.wav" },
  Boticária: { rate: .94, pitch: 1.0, sample: "/manus-storage/mira-ptbr-voice-sample_3603e475.wav" },
  Tecelão: { rate: .9, pitch: .85, sample: "/manus-storage/dheren-ptbr-voice-sample_f56da05b.wav" },
  Darion: { rate: .88, pitch: .75, sample: "/manus-storage/dheren-ptbr-voice-sample_f56da05b.wav" },
  Escriba: { rate: .91, pitch: .98, sample: "/manus-storage/lyra-ptbr-voice-sample_34250906.wav" },
  Barqueiro: { rate: .86, pitch: .8, sample: "/manus-storage/dheren-ptbr-voice-sample_f56da05b.wav" },
  "Arauto Mascarado": { rate: .82, pitch: .65, sample: "/manus-storage/dheren-ptbr-voice-sample_f56da05b.wav" },
};

const VOICE_ASSET_OVERRIDES: Partial<Record<VoiceSpeaker, string>> = {};

export class AudioManager {
  private context: AudioContext | null = null;
  private voicesEnabled = true;
  private voiceQueue: Array<{ speaker: string; text: string }> = [];
  private voiceBusy = false;
  async unlock() {
    if (!this.context) this.context = new AudioContext();
    if (this.context.state === "suspended") await this.context.resume();
  }
  tone(frequency: number, duration: number, type: OscillatorType = "sine", volume = 0.035) {
    if (!this.context || this.context.state !== "running") return;
    const oscillator = this.context.createOscillator();
    const gain = this.context.createGain();
    oscillator.type = type; oscillator.frequency.setValueAtTime(frequency, this.context.currentTime);
    gain.gain.setValueAtTime(volume, this.context.currentTime); gain.gain.exponentialRampToValueAtTime(0.001, this.context.currentTime + duration);
    oscillator.connect(gain).connect(this.context.destination); oscillator.start(); oscillator.stop(this.context.currentTime + duration);
  }
  impact(weight = 1, bright = false) {
    if (!this.context || this.context.state !== "running") return;
    const now = this.context.currentTime;
    const oscillator = this.context.createOscillator(); const gain = this.context.createGain();
    oscillator.type = bright ? "triangle" : "sawtooth"; oscillator.frequency.setValueAtTime(bright ? 510 : 150, now); oscillator.frequency.exponentialRampToValueAtTime(bright ? 180 : 48, now + 0.13 * weight);
    gain.gain.setValueAtTime(0.045 * weight, now); gain.gain.exponentialRampToValueAtTime(0.001, now + 0.15 * weight);
    oscillator.connect(gain).connect(this.context.destination); oscillator.start(now); oscillator.stop(now + 0.16 * weight);
  }
  magic(kind: "arcane" | "arrow" | "needle" | "gold") { const notes = { arcane: [420, 620], arrow: [720, 980], needle: [840, 1140], gold: [360, 620] }[kind]; this.tone(notes[0], .12, "triangle", .045); window.setTimeout(() => this.tone(notes[1], .16, kind === "arcane" ? "sine" : "triangle", .03), 45); }
  consumable(kind: "bruma" | "sal" | "fio") { const notes = { bruma: [560, 760], sal: [230, 380], fio: [760, 1030] }[kind]; this.tone(notes[0], .13, "triangle", .06); window.setTimeout(() => this.tone(notes[1], .18, "sine", .045), 65); }
  purchase() { this.tone(620, .09, "triangle", .05); window.setTimeout(() => this.tone(880, .15, "sine", .04), 70); }
  setVoicesEnabled(enabled: boolean) { this.voicesEnabled = enabled; if (!enabled && "speechSynthesis" in window) { this.voiceQueue = []; this.voiceBusy = false; window.speechSynthesis.cancel(); } }
  speak(speaker: string, text: string) { if (!this.voicesEnabled || !("speechSynthesis" in window) || !text.trim()) return; this.voiceQueue.push({ speaker, text }); this.playQueuedDialogue(); }
  private playQueuedDialogue() { if (this.voiceBusy || !this.voiceQueue.length || !("speechSynthesis" in window)) return; const next = this.voiceQueue.shift()!; const profile = VOICE_PROFILES[next.speaker as VoiceSpeaker] ?? VOICE_PROFILES.Kael; const utterance = new SpeechSynthesisUtterance(next.text.replace(/\[[^\]]+\]/g, "")); utterance.lang = "pt-BR"; utterance.rate = profile.rate; utterance.pitch = profile.pitch; utterance.volume = .86; const voice = window.speechSynthesis.getVoices().find((candidate) => candidate.lang.toLowerCase().startsWith("pt-br")); if (voice) utterance.voice = voice; this.voiceBusy = true; utterance.onend = () => { this.voiceBusy = false; this.playQueuedDialogue(); }; utterance.onerror = () => { this.voiceBusy = false; this.playQueuedDialogue(); }; window.speechSynthesis.speak(utterance); }
  previewVoice(speaker: string) { const profile = VOICE_PROFILES[speaker as VoiceSpeaker] ?? VOICE_PROFILES.Kael; const sample = VOICE_ASSET_OVERRIDES[speaker as VoiceSpeaker] ?? profile.sample; const audio = new Audio(sample); audio.volume = .72; void audio.play().catch(() => undefined); }
  dispose() { this.voiceQueue = []; if ("speechSynthesis" in window) window.speechSynthesis.cancel(); void this.context?.close(); this.context = null; }
}
