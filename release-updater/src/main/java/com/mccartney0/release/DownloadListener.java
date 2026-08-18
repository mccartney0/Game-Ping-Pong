package com.mccartney0.release;

import java.io.File;

public interface DownloadListener {
    void onProgress(DownloadProgress progress);

    void onCompleted(File file);

    void onCancelled();

    void onError(Exception error);
}
