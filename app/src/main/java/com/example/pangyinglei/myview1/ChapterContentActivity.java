package com.example.pangyinglei.myview1;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class ChapterContentActivity extends AppCompatActivity {

    private static final String TAG = "ChapterContentActivity";
    private MyCustomView mCustomView;
    private static ProgressBar progressBar;
    private static TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window window = this.getWindow();
        window.setFlags(flag,flag);

        //隐藏底部navigationbar。(虚拟按键)
//        WindowManager.LayoutParams layoutParams = window.getAttributes();
//        layoutParams.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        window.setAttributes(layoutParams);
        setContentView(R.layout.chapter_content);
        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG,"onNewIntent");
        init();
    }

    private void init(){
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);
        //progressBar.setMax(100);
        tv = (TextView)findViewById(R.id.textView);
        mCustomView = (MyCustomView)this.findViewById(R.id.chaptercontentview);
        //Intent intent = this.getIntent();
        //mb = this.getIntent().getExtras().getParcelable("mybook");
        //int position = this.getIntent().getExtras().getInt("position");
        List<MyBook> books = BookshelfApp.getBookshelfApp().getBooks();
        if(books.size() ==0) {
            Log.e(TAG, "books.size == 0");
        }
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        //启动线程去读取章节内容，耗时操作不能在ui线程处理。
        if(mb.getChapterList().size() == 0){
            //书第一次加载。提取章节信息并且缓存第一章的内容并显示。
            Log.d(TAG,"chapter.size =" + mb.getChapterList().size());
            progressBar.setVisibility(View.VISIBLE);
            //progressBar.setProgress(0);
            tv.setVisibility(View.VISIBLE);
            getChapterList();
        }
        else {

//            if(mb.getCurrChapter().isEmpty() == false) {
//                Log.d(TAG, "chapter is not empty" + "currindx= " + mb.getCurrChapterIndx());
//                Chapter chapter = mb.getCurrChapter();
//                int pageIndx = chapter.getCurrPageNumIndx();
//                Log.d(TAG,"pageIndx ="+pageIndx);
//                List<Integer> pageNumList = chapter.getPageNumList();
//                if(pageIndx == 0){
//                    this.mCustomView.setmText(chapter.getContent().substring(0,pageNumList.get(0)));
//                }
//                else{
//                    this.mCustomView.setmText(chapter.getContent().substring(pageNumList.get(pageIndx - 1),pageNumList.get(pageIndx)));
//                }
//                //this.mCustomView.setmText(mb.getCurrChapter().getContent());
//            } else {
                //从文件中读取。文件操作属于耗时操作。
                Log.d(TAG, "chapter is empty" + "currindx= " + mb.getCurrChapterIndx());
                progressBar.setVisibility(View.VISIBLE);
                //progressBar.setProgress(0);
                tv.setVisibility(View.VISIBLE);
                ShowChapterTask sct = new ShowChapterTask(mCustomView,false,false);
                sct.execute();
//            }
        }
    }

    public static ProgressBar getProgressBar(){
        return progressBar;
    }

    public static TextView getTextView(){
        return tv;
    }

    private void getChapterList(){
        MyAsyncTask mtask = new MyAsyncTask(mCustomView);
        mtask.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        int currChapterIndx = mb.getCurrChapterIndx();
        int currPageIndx = mb.getCurrChapter().getCurrPageNumIndx();
        Log.d(TAG,"currChapterIndx = "+currChapterIndx+"currPageIndx="+currPageIndx);
        BookDBHelper.updateChapterIndxAndPageIndx(currChapterIndx,currPageIndx);
        //mCustomView.setmText("");
        mCustomView = null;
        progressBar = null;
        tv = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onresume");
    }
}
