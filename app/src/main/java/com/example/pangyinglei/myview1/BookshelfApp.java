package com.example.pangyinglei.myview1;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by pangyinglei on 2017/1/22.
 */

public class BookshelfApp extends Application{

    private static final String TAG = "BookshelfApp";
    private static BookshelfApp bookshelfApp;

    private List<MyBook> books = new ArrayList<MyBook>();
    private int currMyBookIndx;

    public static BookshelfApp getBookshelfApp(){
        if(bookshelfApp == null){
            Log.d(TAG,"bookshelfapp = null");
        }
        return bookshelfApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"bookshelfapp oncreate");
        bookshelfApp = this;
        Log.d(TAG,"create database");
    }

    public List<MyBook> getBooks() {
        return books;
    }

    public void setBooks(List<MyBook> books) {
        this.books = books;
    }

    public int getCurrMyBookIndx() {
        return currMyBookIndx;
    }

    public void setCurrMyBookIndx(int currMyBookIndx) {
        this.currMyBookIndx = currMyBookIndx;
    }

    public MyBook getCurrMyBook(){
        return this.getBooks().get(currMyBookIndx);
    }

}
