package com.mccartney0.release;

/** Snapshot imutável do progresso de um download. */
public final class DownloadProgress {
    public final long downloadedBytes;
    public final long totalBytes;
    public final long bytesPerSecond;

    public DownloadProgress(long downloadedBytes, long totalBytes, long bytesPerSecond) {
        this.downloadedBytes = Math.max(0L, downloadedBytes);
        this.totalBytes = totalBytes;
        this.bytesPerSecond = Math.max(0L, bytesPerSecond);
    }

    public boolean isIndeterminate() {
        return totalBytes <= 0L;
    }

    public int percent() {
        if (isIndeterminate()) return 0;
        return (int) Math.min(100L, downloadedBytes * 100L / totalBytes);
    }

    public long remainingSeconds() {
        if (isIndeterminate() || bytesPerSecond <= 0L) return -1L;
        return Math.max(0L, (totalBytes - downloadedBytes) / bytesPerSecond);
    }
}
