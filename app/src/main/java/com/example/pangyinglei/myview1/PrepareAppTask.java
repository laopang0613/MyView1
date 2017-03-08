package com.example.pangyinglei.myview1;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pangyinglei on 2017/2/20.
 */

public class PrepareAppTask extends AsyncTask<Void,Void,Void> {

    private static final String TAG = "PrepareAppTask";

    private BaseAdapter adapter;

    public PrepareAppTask(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    protected Void doInBackground(Void... params) {
        readDB();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        this.adapter.notifyDataSetChanged();
    }

    private void readDB(){
        List<MyBook> books = BookshelfApp.getBookshelfApp().getBooks();
        books.clear();
        BookDBHelper bookDBHelper = new BookDBHelper(BookshelfApp.getBookshelfApp());
        SQLiteDatabase db = bookDBHelper.getReadableDatabase();
        String[] columns = new String[]{"bookId","bookName","bookPath","bookCurrChapterIndx","charTotalCount"};
        if(books.size() == 0){
            Cursor cursor = db.query("bookTable", columns, null,null,null,null,null);
            while(cursor.moveToNext()){
                int bookId = cursor.getInt(cursor.getColumnIndex("bookId"));
                Log.d(TAG,"bookId = "+bookId);
                String bookName = cursor.getString(cursor.getColumnIndex("bookName"));
                Log.d(TAG,"bookName ="+bookName);
                String bookPath = cursor.getString(cursor.getColumnIndex("bookPath"));
                Log.d(TAG,"bookPath ="+bookPath);
                int bookCurrChapterIndx = cursor.getInt(cursor.getColumnIndex("bookCurrChapterIndx"));
                Log.d(TAG,"bookCurrChapterIndx ="+bookCurrChapterIndx);
                int charTotalCount = cursor.getInt(cursor.getColumnIndex("charTotalCount"));
                MyBook mb = new MyBook();
                mb.setName(bookName);
                mb.setPath(bookPath);
                mb.setCurrChapterIndx(bookCurrChapterIndx);
                mb.setChapterList(this.getChapterList(db,bookId));
                mb.setCharTotalCount(charTotalCount);
                books.add(mb);
            }
            cursor.close();
            bookDBHelper.closeDB(db);
        }
        Log.d(TAG,"bookSize="+BookshelfApp.getBookshelfApp().getBooks().size());
    }

//    cv.put("bookId",bookId);
//    cv.put("chapterName",chapter.getName());
//    cv.put("beginCharIndex",chapter.getBeginCharIndex());
//    cv.put("beginContentIndex",chapter.getBeginContentIndex());
//    cv.put("currPageNumIndx",0);

    private List<Chapter> getChapterList(SQLiteDatabase db,int bookId){
        List<Chapter> chapterList = new ArrayList<Chapter>();
        String sBookId = String.valueOf(bookId);
        String[] columns = new String[]{"chapterName","beginCharIndex","beginContentIndex","currPageNumIndx"};
        Cursor cursor = db.query("chapterTable",columns,"bookId=?",new String[]{sBookId},null,null,null,null);
        while(cursor.moveToNext()){
            String chapterName = cursor.getString(cursor.getColumnIndex("chapterName"));
            int beginCharIndex = cursor.getInt(cursor.getColumnIndex("beginCharIndex"));
            int beginContentIndex = cursor.getInt(cursor.getColumnIndex("beginContentIndex"));
            int currPageNumIndx = cursor.getInt(cursor.getColumnIndex("currPageNumIndx"));
//            Log.d(TAG,"chaptename="+chapterName+"begincharindx="+beginCharIndex+"beginConindx="+beginContentIndex
//           +"currpagenumindx="+currPageNumIndx);
            Chapter chapter = new Chapter();
            chapter.setName(chapterName);
            chapter.setBeginCharIndex(beginCharIndex);
            chapter.setBeginContentIndex(beginContentIndex);
            chapter.setCurrPageNumIndx(currPageNumIndx);
            chapterList.add(chapter);
        }
        cursor.close();
        return chapterList;
    }

}
