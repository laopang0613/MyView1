package com.example.pangyinglei.myview1;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by pangyinglei on 2017/2/24.
 */

public class BookmarkFragment extends Fragment implements AdapterView.OnItemClickListener{

    private static final String TAG = "BookmarkFragment";
    private ListView lv;

    public static BookmarkFragment newInstance(){
        BookmarkFragment bookmarkFragment = new BookmarkFragment();
        return bookmarkFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark_list,container,false);
        lv = (ListView)view.findViewById(R.id.bookmarklist);
        BookmarkFragment.ListViewAdapter lva = new BookmarkFragment.ListViewAdapter(BookshelfApp.getBookshelfApp());
        lv.setAdapter(lva);
        lv.setOnItemClickListener(this);
        lv.setSelection(BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrBookMarkIndx());
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private class ListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ListViewAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            //Log.d(TAG,"size = "+chapterList.size());
            return BookshelfApp.getBookshelfApp().getCurrMyBook().getBookMarkList().size();
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
            BookmarkFragment.ViewHolder vh;

            if(convertView == null){
                vh = new BookmarkFragment.ViewHolder();
                convertView =  mInflater.inflate(R.layout.fragment_bookmark_item,null);
                vh.content = (TextView)convertView.findViewById(R.id.bookmark_content);
                vh.percent = (TextView)convertView.findViewById(R.id.bookmark_percent);
                vh.time = (TextView)convertView.findViewById(R.id.bookmark_time);
                convertView.setTag(vh);
            }
            else{
                vh = (BookmarkFragment.ViewHolder) convertView.getTag();
            }
            Log.d(TAG,"position="+position);
            BookMark bookMark = BookshelfApp.getBookshelfApp().getCurrMyBook().getBookMarkList().get(position);
//            if(position == BookshelfApp.getBookshelfApp().getCurrMyBook().getCurrBookMarkIndx()){
//                vh.content.setTextColor(Color.RED); //设置当前章节标题颜色
//            }
//            else{
//                vh.content.setTextColor(Color.DKGRAY);
//            }
            vh.content.setTextColor(Color.DKGRAY);
            vh.content.setText(bookMark.getContent());
            Log.d(TAG,"percent="+bookMark.getPercent());
            vh.percent.setTextColor(Color.DKGRAY);
            vh.percent.setText(bookMark.getPercent());
            Log.d(TAG,"time ="+bookMark.getTime());
            vh.time.setTextColor(Color.DKGRAY);
            vh.time.setText(bookMark.getTime());
            return convertView;
        }


    }

    public final class ViewHolder{
        public TextView content;
        public TextView percent;
        public TextView time;
    }
}
