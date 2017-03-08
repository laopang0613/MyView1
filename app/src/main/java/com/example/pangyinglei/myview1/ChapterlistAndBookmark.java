package com.example.pangyinglei.myview1;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class ChapterlistAndBookmark extends FragmentActivity implements View.OnClickListener{

    private static final String TAG = "ChapterlistAndBookmark";

    private Button chapterlistBtn;
    private Button bookmarkBtn;
    private ViewPager viewPager;
    private List<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapterlist_and_bookmark);
        initView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        initView();
    }

    private void initView(){
        chapterlistBtn = (Button)findViewById(R.id.chapterlistbtn);
        bookmarkBtn = (Button)findViewById(R.id.bookmarkbtn);
        chapterlistBtn.setOnClickListener(this);
        bookmarkBtn.setOnClickListener(this);

        ChapterListFragment chapterListFragment = ChapterListFragment.newInstance();
        BookmarkFragment bookmarkFragment = BookmarkFragment.newInstance();
        fragmentList = new ArrayList<Fragment>();
        fragmentList.add(chapterListFragment);
        fragmentList.add(bookmarkFragment);
        MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter(this.getSupportFragmentManager());
        viewPager = (ViewPager)findViewById(R.id.clbm_viewpager);
        viewPager.setAdapter(myViewPagerAdapter);
    }

    @Override
    public void onClick(View v) {

    }

    public class MyViewPagerAdapter extends FragmentPagerAdapter{

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG,"getItem");
            return fragmentList.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.d(TAG,"instantiateItem");
            return super.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.d(TAG,"destroyItem");
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onstop");
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        int currChapterIndx = mb.getCurrChapterIndx();
        int currPageIndx = mb.getCurrChapter().getCurrPageNumIndx();
        Log.d(TAG,"currChapterIndx = "+currChapterIndx+"currPageIndx="+currPageIndx);
        BookDBHelper.updateChapterIndxAndPageIndx(currChapterIndx,currPageIndx);
    }

}
