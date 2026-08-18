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
  dispose() { void this.context?.close(); this.context = null; }
}
