package com.example.pangyinglei.myview1;

import android.content.SharedPreferences;

/**
 * Created by pangyinglei on 2017/2/18.
 */

public class BookSetting {
    private float LINEWIDTH = 960f;
    private float LINEHEIGHT = 55f;
    private float TXTTOP_XSTART = 40f;
    private float TXTTOP_YSTART = 80f;
    private int LINENUMINPAGE = 26;
    private float txtSize = 55f;


    public BookSetting() {

    }

    public float getLINEHEIGHT() {
        return LINEHEIGHT;
    }

    public void setLINEHEIGHT(float LINEHEIGHT) {
        this.LINEHEIGHT = LINEHEIGHT;
    }

    public int getLINENUMINPAGE() {
        return LINENUMINPAGE;
    }

    public void setLINENUMINPAGE(int LINENUMINPAGE) {
        this.LINENUMINPAGE = LINENUMINPAGE;
    }

    public float getLINEWIDTH() {
        return LINEWIDTH;
    }

    public void setLINEWIDTH(float LINEWIDTH) {
        this.LINEWIDTH = LINEWIDTH;
    }

    public float getTxtSize() {
        return txtSize;
    }

    public void setTxtSize(float txtSize) {
        this.txtSize = txtSize;
    }

    public float getTXTTOP_XSTART() {
        return TXTTOP_XSTART;
    }

    public void setTXTTOP_XSTART(float TXTTOP_XSTART) {
        this.TXTTOP_XSTART = TXTTOP_XSTART;
    }

    public float getTXTTOP_YSTART() {
        return TXTTOP_YSTART;
    }

    public void setTXTTOP_YSTART(float TXTTOP_YSTART) {
        this.TXTTOP_YSTART = TXTTOP_YSTART;
    }
}
