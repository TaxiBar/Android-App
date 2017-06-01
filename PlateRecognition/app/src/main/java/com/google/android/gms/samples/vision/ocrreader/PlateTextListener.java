package com.google.android.gms.samples.vision.ocrreader;

import android.widget.EditText;

/**
 * Created by LokHim on 2017/6/1.
 */

public class PlateTextListener implements TextListener {
    private EditText mET;
    private boolean writtenFlag;

    public PlateTextListener(EditText et){
        mET = et;
        writtenFlag = false;
    }
    @Override
    public void notify(String text) {
        final String ftext = text;
        if (!writtenFlag){
            mET.post(new Runnable() {
                @Override
                public void run() {
                    mET.setText(ftext);
                }
            });
            writtenFlag = true;
        }
    }

    @Override
    public void refresh() {
        writtenFlag = false;
        mET.post(new Runnable() {
            @Override
            public void run() {
                mET.getText().clear();
            }
        });
    }

    @Override
    public boolean isWritten() {
        return writtenFlag;
    }
}
