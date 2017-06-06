package com.example.gjen.taxibar.camera;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sammy on 2017/6/1.
 */

public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private Context mContext;

    public OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, Context context) {
        mGraphicOverlay = ocrGraphicOverlay;
        mContext = context;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {

        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);

            Text selectText = null;
            String selectPlateNum = "";
            boolean found = false;
            List<? extends Text> textComponents = item.getComponents();
            for(Text currentText : textComponents) {
                String num = currentText.getValue().trim();
                //num = num.replaceAll(" ","");
                Pattern pattern = Pattern.compile("[0-9A-Z]{2,3}-[0-9A-Z]{2,3}");
                Matcher matcher = pattern.matcher(num);

                while (matcher.find()) {
                    String matchNum = matcher.group();
                    if(matchNum.length() >= 6 && matchNum.length() <= 7) {
                        mGraphicOverlay.clear();
                        Log.v("Sammy", matcher.group());
                        found = true;
                        OcrGraphic graphic = new OcrGraphic(mContext, mGraphicOverlay, item, matchNum);
                        mGraphicOverlay.add(graphic);
                    }
                    break;
                }
            }

            if(found) {
                break;
            }
        }
    }

    public void clear() {
        mGraphicOverlay.clear();
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
