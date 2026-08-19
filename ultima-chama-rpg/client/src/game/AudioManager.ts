/**
 * Gravura de Cinzas — feedback sonoro sintetizado; não depende de assets externos.
 */
export class AudioManager {
  private context: AudioContext | null = null;
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
  dispose() { void this.context?.close(); this.context = null; }
}
