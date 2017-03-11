package com.example.pangyinglei.myview1;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by pangyinglei on 2017/2/24.
 */

public class ChapterListFragment extends Fragment implements AdapterView.OnItemClickListener{

    private static final String TAG = "ChapterListFragment";
    private ListView lv;

    public static ChapterListFragment newInstance(){
        ChapterListFragment chapterListFragment = new ChapterListFragment();
        return chapterListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        View view = inflater.inflate(R.layout.chapterlist_main,container,false);
        lv = (ListView)view.findViewById(R.id.myList);
        ChapterListFragment.ListViewAdapter lva = new ChapterListFragment.ListViewAdapter(BookshelfApp.getBookshelfApp());
        lv.setAdapter(lva);
        lv.setOnItemClickListener(this);
        lv.setSelection(BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrChapterIndx());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG,"onActivityCreated");
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
        intent.setClass(this.getContext(),ChapterContentActivity.class);
        //Bundle bundle = new Bundle();
        //bundle.putParcelable("mybook",mb);
        //intent.putExtras(bundle);
        this.startActivity(intent);

        //同时缓存前后章节。
        CacheChapterContent cacheChapterContent = new CacheChapterContent(position);
        cacheChapterContent.execute();
    }

    private class ListViewAdapter extends BaseAdapter {
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
            ChapterListFragment.ViewHolder vh;

            if(convertView == null){
                vh = new ChapterListFragment.ViewHolder();
                convertView =  mInflater.inflate(R.layout.chapterlist_item,null);
                vh.tv = (TextView)convertView.findViewById(R.id.chapterTitle);
                convertView.setTag(vh);
            }
            else{
                vh = (ChapterListFragment.ViewHolder) convertView.getTag();
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
}
