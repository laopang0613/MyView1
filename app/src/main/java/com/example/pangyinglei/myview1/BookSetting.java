package com.example.pangyinglei.myview1;

import android.content.SharedPreferences;

/**
 * Created by pangyinglei on 2017/2/18.
 */

public class BookSetting {
    private float lineWidth = 960f;
    private float lineHeight = 55f;
    private float TXTTOP_XSTART = 40f;
    private float TXTTOP_YSTART = 80f;
    private int LINENUMINPAGE = 26;
    private float txtSize = 55f;

    private static float brightness;
    private static boolean isNightMode = false;

    public BookSetting() {

    }

    public static float getBrightness() {
        return brightness;
    }

    public static void setBrightness(float brightness) {
        BookSetting.brightness = brightness;
    }

    public static boolean isNightMode() {
        return isNightMode;
    }

    public static void setNightMode(boolean nightMode) {
        isNightMode = nightMode;
    }

}
