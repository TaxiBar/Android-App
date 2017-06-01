package com.google.android.gms.samples.vision.ocrreader;

/**
 * Created by LokHim on 2017/6/1.
 */

public interface TextListener {
    public void notify(String text);
    public void refresh();
    public boolean isWritten();
}
