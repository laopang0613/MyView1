package com.example.pangyinglei.myview1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by pangyinglei on 2017/2/6.
 */

public class BookDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "BookDBHelper";
    private static final int VERSION = 1;
    private static final String DBName = "book";
    private static final int ERRBOOKID = -100;
    private static final int ERRCHAPTERID = -101;

    private String bookTable;
    private String chapterTable;

    public BookDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public BookDBHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public BookDBHelper(Context context, String name) {
        this(context, name, VERSION);
    }

    public BookDBHelper(Context context) {
        this(context, DBName);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //db.openOrCreateDatabase("/storage/emulate/0/PPReader/database/bookshelf.db",null);
        //CREATE TABLE IF NOT EXISTS person (personid integer primary key autoincrement, name varchar(20), age INTEGER)"
        bookTable = "CREATE TABLE IF NOT EXISTS bookTable (bookId integer primary key autoincrement," +
                "bookName text," +
                "bookPath text," +
                "bookCurrChapterIndx INTEGER," +
                "charTotalCount INTEGER)";
        chapterTable ="CREATE TABLE IF NOT EXISTS chapterTable(chapterId integer primary key autoincrement," +
                "bookId INTEGER," +
                "chapterName text," +
                "beginCharIndex INTEGER," +
                "beginContentIndex INTEGER," +
                "endCharIndex INTEGER," +
                "currPageNumIndx INTEGER)";
//        pageListTable = "CREATE TABLE IF NOT EXISTS pageTable(pageId integer primary key autoincrement," +
//                "bookId INTEGER," +
//                "chapterId INTEGER," +
//                "pageCharEndIndx INTEGER)";
        Log.d(TAG,"db create table");
        if(db==null){
            Log.d(TAG,"db == null");
        }
        db.execSQL(bookTable);
        db.execSQL(chapterTable);
//        db.execSQL(pageListTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void closeDB(SQLiteDatabase db) {
        db.close();
        this.close();
    }

    public static void updateChapterIndxAndPageIndx(int chapterIndx,int pageNumIndx){
        Log.d(TAG,"updateChapterIndxAndPageIndx");
        BookDBHelper bookDBHelper = new BookDBHelper(BookshelfApp.getBookshelfApp());
        SQLiteDatabase readDB = bookDBHelper.getReadableDatabase();
        int bookId = getCurrBookId(readDB);
        String sBookId = String.valueOf(bookId);
        int chapterId = getChapterId(chapterIndx,readDB,bookId);
        String sChapterId = String.valueOf(chapterId);
        Log.d(TAG,"chapterId="+chapterId);
        Log.d(TAG,"bookId="+bookId);
        readDB.close();
        SQLiteDatabase writeDB = bookDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put("bookCurrChapterIndx",chapterIndx);
        writeDB.update("bookTable",cv,"bookId=?",new String[]{sBookId});
        cv.clear();//必须要先Clear
        cv.put("currPageNumIndx",pageNumIndx);
        writeDB.update("chapterTable",cv,"chapterId=? and bookId=?",new String[]{sChapterId,sBookId});
        writeDB.close();
    }

    public static void updateChapterIndx(int chapterIndx){
        BookDBHelper bookDBHelper = new BookDBHelper(BookshelfApp.getBookshelfApp());
        SQLiteDatabase readDB = bookDBHelper.getReadableDatabase();
        int bookId = getCurrBookId(readDB);
        String sBookId = String.valueOf(bookId);
        Log.d(TAG,"bookId="+bookId);
        readDB.close();
        SQLiteDatabase writeDB = bookDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put("bookCurrChapterIndx",chapterIndx);
        writeDB.update("bookTable",cv,"bookId=?",new String[]{sBookId});
        writeDB.close();
    }

    public static  void updatePageIndx(int PageNumIndx){
        BookDBHelper bookDBHelper = new BookDBHelper(BookshelfApp.getBookshelfApp());
        SQLiteDatabase readDB = bookDBHelper.getReadableDatabase();
        int bookId = getCurrBookId(readDB);
        String sBookId = String.valueOf(bookId);
        int currChapterIndx = BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrChapterIndx();
        int chapterId = getChapterId(currChapterIndx,readDB,bookId);
        String sChapterId = String.valueOf(chapterId);
        SQLiteDatabase writeDB = bookDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put("currPageNumIndx",PageNumIndx);
        writeDB.update("chapterTable",cv,"bookId=? and chapterId=?",new String[]{sBookId,sChapterId});
        writeDB.close();
    }

    public static void updateCharTotalCount(int charTotalCount){
        BookDBHelper bookDBHelper = new BookDBHelper(BookshelfApp.getBookshelfApp());
        SQLiteDatabase readDB = bookDBHelper.getReadableDatabase();
        int bookId = getCurrBookId(readDB);
        String sBookId = String.valueOf(bookId);
        Log.d(TAG,"bookId="+bookId);
        readDB.close();
        SQLiteDatabase writeDB = bookDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.clear();
        cv.put("charTotalCount",charTotalCount);
        writeDB.update("bookTable",cv,"bookId=?",new String[]{sBookId});
        writeDB.close();
    }

    public static int getChapterId(int chapterIndx,SQLiteDatabase db,int bookId){
        int chapterId = ERRCHAPTERID;
        if(bookId == ERRBOOKID){
            Log.e(TAG,"bookId = -100 error!");
            return chapterId;
        }
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        String chapterName = mb.getChapterList().get(chapterIndx).getName();
        int beginCharIndex = mb.getChapterList().get(chapterIndx).getBeginCharIndex();
        Log.d(TAG,"chapterName="+chapterName+"begincharIndx="+beginCharIndex);
        String sBookId = String.valueOf(bookId);
        String sBeginCharIndex = String.valueOf(beginCharIndex);
        Cursor cusor = db.query("chapterTable",new String[]{"chapterId"},
                "beginCharIndex=? and bookId=?",new String[]{sBeginCharIndex,sBookId},null,null,null,null);
        while(cusor.moveToNext()){
            chapterId = cusor.getInt(cusor.getColumnIndex("chapterId"));
            Log.d(TAG,"chapterId ="+chapterId);
        }
        cusor.close();
        return chapterId;
    }

    public static int getCurrBookId(SQLiteDatabase db){
        int bookId = ERRBOOKID;
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        String bookName = mb.getName();
        String bookPath = mb.getPath();
        Log.d(TAG,"bookName ="+bookName+"bookPath="+bookPath);
        //BookDBHelper bookDBHelper = new BookDBHelper(BookshelfApp.getBookshelfApp());
        //SQLiteDatabase readDB = bookDBHelper.getReadableDatabase();
        Cursor cursor = db.query("bookTable",new String[]{"bookId"},"bookPath=?",
                new String[]{bookPath},null,null,null,null);
        while(cursor.moveToNext()){
            bookId = cursor.getInt(cursor.getColumnIndex("bookId"));
            Log.d(TAG,"bookId="+bookId);
        }
        cursor.close();
        if(bookId == ERRBOOKID){
            Log.e(TAG,"bookId = -100 error!");
        }
        return bookId;
    }

    public static void insertChapter(Chapter chapter,int bookId,SQLiteDatabase db,ContentValues cv){
        if(bookId == ERRBOOKID){
            Log.e(TAG,"bookId = -100 error!");
            return;
        }
        //BookDBHelper bookDBHelper = new BookDBHelper(BookshelfApp.getBookshelfApp());
        //SQLiteDatabase db = bookDBHelper.getWritableDatabase();
        //ContentValues cv = new ContentValues();
        cv.clear();
        cv.put("bookId",bookId);
        cv.put("chapterName",chapter.getName());
        cv.put("beginCharIndex",chapter.getBeginCharIndex());
        cv.put("beginContentIndex",chapter.getBeginContentIndex());
        cv.put("currPageNumIndx",0);
        db.insert("chapterTable",null,cv);
        //bookDBHelper.closeDB(db);
    }

    public static void insertBook(String name,String path){
        BookDBHelper bookDBHelper = new BookDBHelper(BookshelfApp.getBookshelfApp());
        SQLiteDatabase db = bookDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        //这里数据一次用cv存完，再insert，不能cv put一条数据就插入一次，插入一次就会生成一条bookid.
//            bookDBHelper.insertString(db,cv,"bookName",file.getName(),"bookTable");
//            bookDBHelper.insertString(db,cv,"bookPath",file.getAbsolutePath(),"bookTable");
//            bookDBHelper.insertInt(db,cv,"bookCurrChapterIndx",0,"bookTable");
        cv.put("bookName",name);
        cv.put("bookPath",path);
        cv.put("bookCurrChapterIndx",0);
        db.insert("bookTable",null,cv);
        bookDBHelper.closeDB(db);
    }


}
