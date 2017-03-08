package com.example.pangyinglei.myview1;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by pangyinglei on 2017/1/14.
 */

public class Chapter implements Parcelable {

    private static final String TAG = "Chapter";
    //title(包含两头的空白字符)
    private String name;
    //private String content = "";
    private StringBuffer content = new StringBuffer("");
    //title在全文中的位置。
    private int beginCharIndex;
    //内容在全文中的位置
    private int beginContentIndex;
    private int endCharIndex;

    //章节每页的结束索引。
    private List<Integer> pageNumList = new ArrayList<Integer>();

    //当前章节的页数索引。
    private int currPageNumIndx;

    public Chapter() {

    }
    public Chapter(Parcel in){
        name = in.readString();
        //content = in.readString();
        content = (StringBuffer)(in.readSerializable());
        beginCharIndex = in.readInt();
        beginContentIndex = in.readInt();
        endCharIndex = in.readInt();
        pageNumList = in.readArrayList(List.class.getClassLoader());
        currPageNumIndx = in.readInt();
    }

    public int getBeginCharIndex() {
        return beginCharIndex;
    }

    public void setBeginCharIndex(int beginCharIndex) {
        this.beginCharIndex = beginCharIndex;
    }

    public int getBeginContentIndex() {
        return beginContentIndex;
    }

    public void setBeginContentIndex(int beginContentIndex) {
        this.beginContentIndex = beginContentIndex;
    }

    public String getContent() {
        return content.toString();
    }

    public void setContent(String mContent) {
        this.content.setLength(0);
        this.content.append(mContent);
        if(this.content.length()>0){
            Log.d(TAG,"chapter content.len = "+this.content.length());
        }
        else{
            Log.d(TAG,"chapter content.len = 0");
        }
    }

    public boolean isEmpty(){
        if(this.getContent().length() == 0){
            return true;
        }
        return false;
    }

    public int getEndCharIndex() {
        return endCharIndex;
    }

    public void setEndCharIndex(int endCharIndex) {
        this.endCharIndex = endCharIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getPageNumList() {
        return pageNumList;
    }

    public void setPageNumList(List<Integer> pageNumList) {
        this.pageNumList = pageNumList;
    }

    public int getPageTotal() {
        return this.pageNumList.size();
    }


    public int getCurrPageNumIndx() {
        return currPageNumIndx;
    }

    public void setCurrPageNumIndx(int currPageNumIndx) {
        this.currPageNumIndx = currPageNumIndx;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        //dest.writeString(this.content);
        dest.writeSerializable(this.content);
        dest.writeInt(this.beginCharIndex);
        dest.writeInt(this.beginContentIndex);
        dest.writeInt(this.endCharIndex);
        dest.writeList(this.pageNumList);
        dest.writeInt(this.currPageNumIndx);
    }

    public static final Parcelable.Creator<Chapter> CREATOR = new Parcelable.Creator<Chapter>(){
        @Override
        public Chapter createFromParcel(Parcel source) {

            return new Chapter(source);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };
}
