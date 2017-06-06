package com.example.gjen.taxibar.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.android.gms.vision.text.TextBlock;

import com.example.gjen.taxibar.R;
import com.example.gjen.taxibar.TaxiReviewList;

/**
 * Created by sammy on 2017/6/1.
 */

public class OcrGraphic extends GraphicOverlay.Graphic {

    private int mId;

    private static final int TEXT_COLOR = Color.WHITE;

    private static Paint sRectPaint;
    private static Paint sTakeRectPaint;
    private static Paint sTextPaint;
    private static Paint sIconPaint;
    private final TextBlock mText;
    private final String mPlateNumber;
    private Context mContext;

    OcrGraphic(Context context, GraphicOverlay overlay, TextBlock text, String plateNumber) {
        super(overlay);
        mText = text;
        mPlateNumber = plateNumber;
        mContext = context;

        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(Color.BLACK);
            sRectPaint.setAlpha(125);
            sRectPaint.setStyle(Paint.Style.FILL);

        }

        if (sTakeRectPaint == null) {
            sTakeRectPaint = new Paint();
            sTakeRectPaint.setColor(Color.MAGENTA);
            sTakeRectPaint.setAlpha(175);
            sTakeRectPaint.setStyle(Paint.Style.FILL);

        }

        if (sIconPaint == null) {
            sIconPaint = new Paint();
            sTakeRectPaint.setAlpha(80);

        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(100.0f);
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public TextBlock getTextBlock() {
        return mText;
    }

    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     *
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */
    public boolean contains(float x, float y) {
        TextBlock text = mText;
        if (text == null) {
            return false;
        }
        RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        return (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y);
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {

        if (mPlateNumber == null || mText == null) {
            return;
        }

        RectF rect = new RectF(mText.getBoundingBox());
        rect.bottom = translateY(rect.bottom) - 130;
        rect.top = rect.bottom - 120;
        rect.left = translateX(rect.left) - 10;
        rect.right = rect.left + mPlateNumber.length() * 60;
        canvas.drawRect(rect, sRectPaint);

        float left = translateX(mText.getBoundingBox().left);
        float bottom = translateY(mText.getBoundingBox().bottom) - 150;
        canvas.drawText(mPlateNumber, left, bottom, sTextPaint);

        RectF takeRect = new RectF(mText.getBoundingBox());
        takeRect.bottom = translateY(takeRect.bottom) - 260;
        takeRect.top = takeRect.bottom - 120;
        takeRect.left = translateX(takeRect.left) - 10;
        takeRect.right = takeRect.left + 250;
        canvas.drawRoundRect(takeRect, 25, 25, sTakeRectPaint);

        left = takeRect.left + 20;
        bottom = takeRect.bottom - 20;
        canvas.drawText("搭乘", left, bottom, sTextPaint);

        TaxiReviewList.ReviewType rt= TaxiReviewList.getInstance().getReviewe(mPlateNumber);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.neutral);
        if (TaxiReviewList.ReviewType.Good == rt) {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.good);
        } else if (TaxiReviewList.ReviewType.Bad == rt) {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bad);
        }

        canvas.drawBitmap(bitmap, takeRect.left -180, takeRect.top , sIconPaint);


    }

    public String getPlateNumber() {
        return mPlateNumber;
    }
}
