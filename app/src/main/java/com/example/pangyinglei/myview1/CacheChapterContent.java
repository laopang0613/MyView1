package com.example.pangyinglei.myview1;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by pangyinglei on 2017/3/11.
 */

public class CacheChapterContent extends AsyncTask<Void,Void,Void> {

    private static final String TAG = "CacheChapterContent";
    private int currChapterIndx;
    private boolean isCachePrevChapter = false;
    private boolean isCacheNextChapter = false;

    public CacheChapterContent(int currChapterIndx) {
        this.currChapterIndx = currChapterIndx;
    }

    @Override
    protected Void doInBackground(Void... params) {
//        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
//        int cachePreChapterIndx = mb.getCachePreChapterIndx();
//
//        if(currChapterIndx!=0){
//            //判断前一章的内容是否为空，不为空，直接将cachePreChapterIndx设为前一章的索引，同时清空之前缓存的前一章内容。
//            if(!mb.getChapterList().get(currChapterIndx-1).getContent().isEmpty()){
//                mb.setCachePreChapterIndx(currChapterIndx - 1);
//                //如果之前的前一章索引，不等于当前章节，下一章和前一章时，才能清空之前的缓存。
//                if(cachePreChapterIndx != -1 && cachePreChapterIndx!= currChapterIndx && cachePreChapterIndx!=currChapterIndx+1 &&
//                        cachePreChapterIndx != currChapterIndx - 1){
//                    mb.getChapterList().get(cachePreChapterIndx).setContent("");
//                }
//            }
//            //如果缓存的前一章索引不是当前章节的前一章索引。
//            else if(cachePreChapterIndx != currChapterIndx - 1){
//                    //如果前一章缓存章节索引不等于-1，则前一章缓存内容不为空。
//                    if(cachePreChapterIndx!= -1){
//                        //缓存当前章节的前一章内容
//                        String preContent = MyFileUtils.getChapterContent(currChapterIndx - 1, mb);
//                        mb.getChapterList().get(currChapterIndx - 1).setContent(preContent);
//                        MyFileUtils.setPageNumList(preContent, currChapterIndx - 1, mb, MyFileUtils.getPaint());
//                        //设置缓存前一章节的索引。
//                        mb.setCachePreChapterIndx(currChapterIndx - 1);
//
//                    }
//            }
//        }
//
//        int cacheNextChapterIndx = mb.getCacheNextChapterIndx();
//        if(currChapterIndx != mb.getChapterList().size() - 1){
//            if(!mb.getChapterList().get(currChapterIndx+1).getContent().isEmpty()){
//                mb.setCacheNextChapterIndx(currChapterIndx+1);
//                if(cacheNextChapterIndx != -1&& cacheNextChapterIndx!= currChapterIndx && cacheNextChapterIndx!=currChapterIndx+1 &&
//                        cacheNextChapterIndx != currChapterIndx - 1){
//                    mb.getChapterList().get(cacheNextChapterIndx).setContent("");
//                }
//            }
//            else if(cacheNextChapterIndx != currChapterIndx + 1){
//                if(cacheNextChapterIndx != -1){
//                    mb.getChapterList().get(cacheNextChapterIndx).setContent("");
//                    String nextContent = MyFileUtils.getChapterContent(currChapterIndx + 1, mb);
//                    mb.getChapterList().get(currChapterIndx + 1).setContent(nextContent);
//                    MyFileUtils.setPageNumList(nextContent, currChapterIndx + 1, mb, MyFileUtils.getPaint());
//                }
//            }
//        }
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        Set<Integer> cacheChapterIndxs = mb.getCacheChapterIndxs();

        synchronized (mb.getCacheChapterIndxs()) {
            MyFileUtils.addCacheChapterIndx(currChapterIndx);
        }
        synchronized (mb.getCacheChapterIndxs()) {
            if (currChapterIndx != 0) {
                if (cacheChapterIndxs.contains(currChapterIndx - 1)) {
                    Log.d(TAG, "contain previndx=" + (currChapterIndx - 1));
//                    mb.setCachePreChapterIndx(currChapterIndx - 1);

                    Iterator<Integer> iterator = cacheChapterIndxs.iterator();
                    while (iterator.hasNext()) {
                        Log.d(TAG, "contain previndx,iterator.next=" + iterator.next());
                    }

                } else {
                    Log.d(TAG, "not contain previndx,prevlen=" + mb.getChapterList().get(currChapterIndx - 1).getContent().length());
                    mb.getChapterList().get(currChapterIndx - 1).setContent(MyFileUtils.getPrevChapterContent());

                    Log.d(TAG, "have contain previndx,prevlen=" + mb.getChapterList().get(currChapterIndx - 1).getContent().length());

                    Iterator<Integer> iterator = cacheChapterIndxs.iterator();
                    while (iterator.hasNext()) {
                        Log.d(TAG, "have contain previndx,iterator.next=" + iterator.next());
                    }
                }
                MyFileUtils.addCacheChapterIndx(currChapterIndx - 1);
            }
        }
        synchronized (mb.getCacheChapterIndxs()) {
            if (currChapterIndx != mb.getChapterList().size() - 1) {
                if (cacheChapterIndxs.contains(currChapterIndx + 1)) {
                    Log.d(TAG,"contain nextindx="+(currChapterIndx +1));
//                    mb.setCacheNextChapterIndx(currChapterIndx + 1);

                    Iterator<Integer> iterator = cacheChapterIndxs.iterator();
                    while(iterator.hasNext()){
                        Log.d(TAG,"contain nextindx,iterator.next="+iterator.next());
                    }

                } else {
                    Log.d(TAG,"not contain nextindx,nextlen="+mb.getChapterList().get(currChapterIndx+1).getContent().length());
                    mb.getChapterList().get(currChapterIndx + 1).setContent(MyFileUtils.getNextChapterContent());

                    Log.d(TAG,"have contain nextindx,nextlen="+mb.getChapterList().get(currChapterIndx+1).getContent().length());
                    Log.d(TAG,"cacheChapterindxs.size="+cacheChapterIndxs.size());

                    Iterator<Integer> iterator = cacheChapterIndxs.iterator();
                    while(iterator.hasNext()){
                        Log.d(TAG,"have contain nextindx,iterator.next="+iterator.next());
                    }
                }
                MyFileUtils.addCacheChapterIndx(currChapterIndx + 1);
            }
        }

        for(Chapter c:BookshelfApp.getBookshelfApp().getCurrMyBook().getChapterList()){
            if(!c.isEmpty()){
                // Log.d(TAG,"c.len ="+c.getContent().length());
                Log.d(TAG,"c.name="+c.getName()+"c.len="+c.getContent().length());
            }
        }
        return null;
    }
}
