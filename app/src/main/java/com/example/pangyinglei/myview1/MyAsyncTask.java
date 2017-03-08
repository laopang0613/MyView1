package com.example.pangyinglei.myview1;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pangyinglei on 2017/1/6.
 */

public class MyAsyncTask extends AsyncTask<Void,Integer,String>{

    private static final String TAG = "MyAsyncTask";
    //private MyFileUtils myFileUtils;
    private MyCustomView myCustomView;
//    private ProgressBar progressBar;
//    private TextView textView;
    //private MyBook mb;

    public MyAsyncTask(MyCustomView myCustomView) {
        this.myCustomView = myCustomView;
//        this.progressBar = progressBar;
//        this.textView = textView;
    }

    @Override
    protected String doInBackground(Void... params) {
         return MyFileUtils.deal();
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG,"s = "+s);
//        LayoutInflater inflater = LayoutInflater.from(BookshelfApp.getBookshelfApp());
//        View view = inflater.inflate(R.layout.chapter_content,null);
//        ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
//        TextView textView = (TextView)view.findViewById(R.id.textView);
//        MyCustomView myCustomView = (MyCustomView)view.findViewById(R.id.chaptercontentview);
        ChapterContentActivity.getProgressBar().setVisibility(View.GONE);
        ChapterContentActivity.getTextView().setVisibility(View.GONE);

        myCustomView.setmText(s);
        final  Handler handler = new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                updateDBWithBook();
            }
        });
    }


    public void updateDBWithBook(){
        BookDBHelper bookDBHelper = new BookDBHelper(BookshelfApp.getBookshelfApp());
        SQLiteDatabase readDB = bookDBHelper.getReadableDatabase();
        int bookId = BookDBHelper.getCurrBookId(readDB);
        readDB.close();
        SQLiteDatabase writeDB = bookDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        writeDB.beginTransaction();
        List<Chapter> chapterList = BookshelfApp.getBookshelfApp().getCurrMyBook().getChapterList();
        for(Chapter chapter:chapterList){
            BookDBHelper.insertChapter(chapter,bookId,writeDB,cv);
        }
        BookDBHelper.insertCharTotalCount(BookshelfApp.getBookshelfApp().getCurrMyBook().getCharTotalCount(),
                cv,bookId,writeDB);
        writeDB.setTransactionSuccessful();
        writeDB.endTransaction();
        bookDBHelper.closeDB(writeDB);
    }

}
