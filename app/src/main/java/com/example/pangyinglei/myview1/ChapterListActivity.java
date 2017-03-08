package com.example.pangyinglei.myview1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


/**
 * Created by pangyinglei on 2017/1/10.
 */

public class ChapterListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private static final String TAG = "ChapterListActivity";
    private ListView lv;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chapterlist_main);
        //Intent intent = this.getIntent();
        //Bundle bundle = intent.getExtras();
        //mb = (MyBook) bundle.getParcelable("mybook");
        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        init();
    }

    private void init(){
        lv = (ListView)findViewById(R.id.myList);
        ListViewAdapter lva = new ListViewAdapter(this);
        //ListViewAdapter lva = new ListViewAdapter(BookshelfApp.getBookshelfApp());
        lv.setAdapter(lva);
        lv.setOnItemClickListener(this);
        lv.setSelection(BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrChapterIndx());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG,"position = "+position+ "id = "+id);
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        mb.setCurrChapterIndx(position);
        mb.getCurrChapter().setCurrPageNumIndx(0);
        //ViewHolder vh = (ViewHolder) view.getTag();
        //String str = vh.tv.getText().toString();
        //Log.d(TAG,"str = "+str);
        //不需要实时写数据库。
        //BookDBHelper.writeDB(position,0);
        Intent intent = new Intent();
        intent.setClass(this,ChapterContentActivity.class);
        //Bundle bundle = new Bundle();
        //bundle.putParcelable("mybook",mb);
        //intent.putExtras(bundle);
        this.startActivity(intent);


    }

    private class ListViewAdapter extends BaseAdapter{
        private LayoutInflater mInflater;

        public ListViewAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            //Log.d(TAG,"size = "+chapterList.size());
            return BookshelfApp.getBookshelfApp().getCurrMyBook().getChapterList().size();
        }

        @Override
        public Object getItem(int position) {
            //Log.d(TAG,"item = "+chapterList.get(position));
            return null;
        }

        @Override
        public long getItemId(int position) {
           // Log.d(TAG,"itemid = "+position);
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;

            if(convertView == null){
                vh = new ViewHolder();
                convertView =  mInflater.inflate(R.layout.chapterlist_item,null);
                vh.tv = (TextView)convertView.findViewById(R.id.chapterTitle);
                convertView.setTag(vh);
            }
            else{
                vh = (ViewHolder) convertView.getTag();
            }
            //Log.d(TAG,"position = "+position);
            String titleStr = BookshelfApp.getBookshelfApp().getCurrMyBook().getChapterList().get(position).getName().trim();
            //Log.d(TAG,"titleStr ="+titleStr+"end");
            if(position == BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrChapterIndx()){
                vh.tv.setTextColor(Color.RED); //设置当前章节标题颜色
            }
            else{
                vh.tv.setTextColor(Color.DKGRAY);
            }
            vh.tv.setText(titleStr);
            return convertView;
        }


    }

    public final class ViewHolder{
        public  TextView tv;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onstart");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onresume");
        lv.setSelection(BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrChapterIndx());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"ondestroy");

    }



}
