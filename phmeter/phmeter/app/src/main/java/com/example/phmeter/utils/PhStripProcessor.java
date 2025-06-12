package com.example.phmeter.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

public class PhStripProcessor {

    public static class ROIResult {
        public List<float[]> colors;
        public Bitmap overlay;

        public ROIResult(List<float[]> colors, Bitmap overlay) {
            this.colors = colors;
            this.overlay = overlay;
        }
    }

    public static ROIResult process(Bitmap bitmap) {
        int imageW = bitmap.getWidth();
        int imageH = bitmap.getHeight();

        int stripW = 80;
        int stripH = 300;
        int stripX = Math.max(0, imageW / 2 - stripW / 2);
        int stripY = Math.max(0, imageH / 2 - stripH / 2);

        if (stripX + stripW > imageW) stripW = imageW - stripX;
        if (stripY + stripH > imageH) stripH = imageH - stripY;


        Bitmap strip = Bitmap.createBitmap(bitmap, stripX, stripY, stripW, stripH);
        int regionH = stripH / 4;

        List<float[]> avgColors = new ArrayList<>();
        Bitmap overlay = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(overlay);

        Paint boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4);

        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.RED);
        dotPaint.setStyle(Paint.Style.FILL);

        // Draw bounding box of strip
        canvas.drawRect(stripX, stripY, stripX + stripW, stripY + stripH, boxPaint);

        for (int i = 0; i < 4; i++) {
            int centerX = stripX + stripW / 2;
            int centerY = stripY + i * regionH + regionH / 2;

            float sumR = 0, sumG = 0, sumB = 0;
            int count = 0;

            for (int dy = -5; dy < 5; dy++) {
                for (int dx = -5; dx < 5; dx++) {
                    int px = centerX + dx;
                    int py = centerY + dy;
                    if (px >= 0 && px < imageW && py >= 0 && py < imageH) {
                        int color = bitmap.getPixel(px, py);
                        sumR += Color.red(color);
                        sumG += Color.green(color);
                        sumB += Color.blue(color);
                        count++;
                    }
                }
            }

            avgColors.add(new float[]{ sumR / count, sumG / count, sumB / count });

            // Draw center dot on original image
            canvas.drawCircle(centerX, centerY, 8, dotPaint);
        }

        return new ROIResult(avgColors, overlay);
    }
}