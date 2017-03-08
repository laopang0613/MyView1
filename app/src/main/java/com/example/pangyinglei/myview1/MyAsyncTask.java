package com.example.pangyinglei.myview1;

import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    }

}
