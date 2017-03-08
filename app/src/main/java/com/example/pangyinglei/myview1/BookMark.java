package com.example.pangyinglei.myview1;

/**
 * Created by pangyinglei on 2017/2/24.
 */

public class BookMark {

    private int chapterIndx;
    private int pageNumIndx;
    private String content = "";
    private String percent = "";
    private String time = "";

    public BookMark() {
    }

    public int getChapterIndx() {
        return chapterIndx;
    }

    public void setChapterIndx(int chapterIndx) {
        this.chapterIndx = chapterIndx;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPageNumIndx() {
        return pageNumIndx;
    }

    public void setPageNumIndx(int pageNumIndx) {
        this.pageNumIndx = pageNumIndx;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
