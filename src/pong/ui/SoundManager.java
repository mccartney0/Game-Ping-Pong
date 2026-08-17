package pong.ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class SoundManager {

    private volatile boolean enabled = true;
    private volatile double masterVolume = 1.0;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setVolumePercent(int percent) {
        masterVolume = Math.max(0.0, Math.min(1.0, percent / 100.0));
    }

    public int getVolumePercent() {
        return (int) Math.round(masterVolume * 100.0);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void paddle() {
        playTone(520, 55, 0.16);
    }

    public void wall() {
        playTone(300, 35, 0.11);
    }

    public void point(boolean playerScored) {
        playTone(playerScored ? 720 : 180, 150, 0.22);
    }

    public void power() {
        playTone(900, 130, 0.2);
    }

    public void event() {
        playTone(260, 220, 0.18);
    }

    public void menu() {
        playTone(440, 45, 0.1);
    }

    private void playTone(final int frequency, final int durationMs, final double volume) {
        if (!enabled) {
            return;
        }
        Thread soundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                AudioFormat format = new AudioFormat(22050, 8, 1, true, false);
                try {
                    if (!AudioSystem.isLineSupported(new javax.sound.sampled.DataLine.Info(SourceDataLine.class, format))) {
                        return;
                    }
                    SourceDataLine line = AudioSystem.getSourceDataLine(format);
                    line.open(format);
                    line.start();
                    byte[] buffer = new byte[Math.max(1, durationMs * 22)];
                    for (int i = 0; i < buffer.length; i++) {
                        double envelope = 1.0 - (i / (double) buffer.length);
                        double wave = Math.sin(2.0 * Math.PI * frequency * i / 22050.0);
                        buffer[i] = (byte) (wave * envelope * 127.0 * volume * masterVolume);
                    }
                    line.write(buffer, 0, buffer.length);
                    line.drain();
                    line.stop();
                    line.close();
                } catch (Exception ignored) {
                    // Áudio é opcional e não pode quebrar o loop do jogo.
                }
            }
        }, "neon-ping-pong-sound");
        soundThread.setDaemon(true);
        soundThread.start();
    }
}
