package com.example.phmeter.utils;

import android.graphics.Bitmap;

import java.util.Arrays;
import java.util.List;

class CalibrationPoint {
    final float pH, r, g, b;
    CalibrationPoint(float pH, float r, float g, float b) {
        this.pH = pH; this.r = r; this.g = g; this.b = b;
    }
}

public class ImageProcessor {
    private static final List<CalibrationPoint> CALIB = Arrays.asList(
            new CalibrationPoint(4.0f, 190,  30,  40),
            new CalibrationPoint(6.0f, 160,  80,  60),
            new CalibrationPoint(7.0f, 120, 180,  90),
            new CalibrationPoint(8.0f,  80, 200, 120),
            new CalibrationPoint(10.0f,  60,  90, 200)
            // …add calibration points here
    );

    public static Bitmap cropROI(Bitmap src, int index) {
        int w = src.getWidth() / 4;
        return Bitmap.createBitmap(src, index * w, 0, w, src.getHeight());
    }

    public static float[] avgColor(Bitmap bmp) {
        int w = bmp.getWidth(), h = bmp.getHeight();
        long sumR=0, sumG=0, sumB=0;
        int[] pixels = new int[w*h];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int p : pixels) {
            sumR += (p >> 16) & 0xFF;
            sumG += (p >>  8) & 0xFF;
            sumB +=  p        & 0xFF;
        }
        float total = w * h;
        return new float[]{ sumR/total, sumG/total, sumB/total };
    }

    public static float mapColorToPH(float r, float g, float b) {
        CalibrationPoint best = null;
        double bestDist = Double.MAX_VALUE;
        for (CalibrationPoint cp : CALIB) {
            double dx = r - cp.r;
            double dy = g - cp.g;
            double dz = b - cp.b;
            double d  = Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (d < bestDist) {
                bestDist = d;
                best = cp;
            }
        }
        return best != null ? best.pH : Float.NaN;
    }
}
