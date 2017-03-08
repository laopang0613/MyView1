package com.example.pangyinglei.myview1;

import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pangyinglei on 2017/1/16.
 */

public class ShowChapterTask extends AsyncTask<Void,Void,String> {
    private static final String TAG = "ShowChapterTask";

    private MyCustomView mcv;
//    private ProgressBar progressBar;
//    private TextView textView;

    //是否翻页；
    private boolean isTurnPage;

    //true往前翻页;false往后翻页
    private boolean isGotoPrev;

//    public ShowChapterTask(MyCustomView mcv,boolean isTurnPage,boolean isGotoPrev) {
//        this.mcv = mcv;
//        this.isTurnPage = isTurnPage;
//        this.isGotoPrev = isGotoPrev;
//        this.progressBar = progressBar;
//        this.textView = textView;
//    }

    public ShowChapterTask(MyCustomView mcv,boolean isTurnPage,boolean isGotoPrev) {
        this.mcv = mcv;
        this.isTurnPage = isTurnPage;
        this.isGotoPrev = isGotoPrev;
    }

    @Override
    protected String doInBackground(Void... params) {
        int prevPageEndIndx =0,currPageEndIndx;
        BookshelfApp bookapp = BookshelfApp.getBookshelfApp();
        MyBook mb = bookapp.getBooks().get(bookapp.getCurrMyBookIndx());
        int currChapterIndx = mb.getCurrChapterIndx();
        //Log.d(TAG,"currChapterIndx ="+currChapterIndx);
        //Log.d(TAG,"chaptersize = "+mb.getChapterList().size());
        String currChapterContent = MyFileUtils.getChapterContent(currChapterIndx,mb);
        //Log.d(TAG,"currchapter content = "+currChapterContent);
        //不保存章节内容，避免当前章节内容过长。
        mb.getCurrChapter().setContent(currChapterContent);
        MyFileUtils.setPageNumList(currChapterContent,currChapterIndx,mb,MyFileUtils.getPaint());
        //this.setCacheChapter(currChapterIndx);

        Chapter currChapter = mb.getCurrChapter();
        //Log.d(TAG,"currchaptername = "+currChapter.getName());
        List<Integer> pageNumList = currChapter.getPageNumList();
        //Log.d(TAG,"pageNumList.size ="+pageNumList.size());
        //Log.d(TAG,"isgotoprev = "+isGotoPrev);
        //跳转前一章显示最后一页，跳转后一章显示第一页。
        if(isTurnPage) {
            if (isGotoPrev) {
                int currPageIndx = currChapter.getPageNumList().size() - 1;
                currChapter.setCurrPageNumIndx(currPageIndx);
                Log.d(TAG, "currPageIndx = " + currPageIndx);
                prevPageEndIndx = pageNumList.get(currPageIndx - 1);
                currPageEndIndx = pageNumList.get(currPageIndx);
            } else {
                mb.getCurrChapter().setCurrPageNumIndx(0);
                prevPageEndIndx = 0;
                currPageEndIndx = pageNumList.get(0);
            }
        }
        else{
            int currPageIndx = currChapter.getCurrPageNumIndx();
            Log.d(TAG,"currPageIndx="+currPageIndx+"isTurnPage="+isTurnPage);
            if(currPageIndx == 0){
                prevPageEndIndx = 0;
                currPageEndIndx = pageNumList.get(0);
            }else {
                int prev = pageNumList.get(currPageIndx - 1);
                int next = pageNumList.get(currPageIndx);
                Log.d(TAG,"prev="+prev+"next="+next+"currChapterContent.len="+currChapterContent.length());
                prevPageEndIndx = pageNumList.get(currPageIndx - 1);
                currPageEndIndx = pageNumList.get(currPageIndx);
            }
        }
        return currChapterContent.substring(prevPageEndIndx,currPageEndIndx);
    }

    @Override
    protected void onPostExecute(String s) {
        //Log.d(TAG,"s="+s);
//        LayoutInflater inflater = LayoutInflater.from(BookshelfApp.getBookshelfApp());
//        View view = inflater.inflate(R.layout.chapter_content,null);
//        ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
//        TextView textView = (TextView)view.findViewById(R.id.textView);
//        MyCustomView mcv = (MyCustomView)view.findViewById(R.id.chaptercontentview);
        ChapterContentActivity.getProgressBar().setVisibility(View.GONE);
        ChapterContentActivity.getTextView().setVisibility(View.GONE);
        mcv.setmText(s);
    }
}
