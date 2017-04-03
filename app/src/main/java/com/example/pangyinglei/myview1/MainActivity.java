package com.example.pangyinglei.myview1;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final static String TAG = "MainActivity";

    //图片文件夹
    private static final String FOLD_IMG = "image";
    private static final String FOLD_BOOKS = "books";
    private static final String FOLD_CACHE = "cache";
    private static final String FOLD_USER = "user";
    private static final String FOLD_DB = "database";
    private static final int FILEPERMISSION = 124;

    //private MyBook mBook;
    private GridView gv;
    private List<MyBook> books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BookDBHelper bookDBHelper2 = new BookDBHelper(this);
        SQLiteDatabase db = bookDBHelper2.getWritableDatabase();
        if(bookDBHelper2 == null){
            Log.d(TAG,"bookDBHelp2 == null");
        }
        else{
            Log.d(TAG,"bookDBHelp2 != null");
        }
        initView();
        initData();
    }

    private void initView(){
        gv = (GridView)findViewById(R.id.bookgridview);
        BookshelfGridAdapter bAdapter = new BookshelfGridAdapter();
        if(books == null) {
            books = BookshelfApp.getBookshelfApp().getBooks();
        }
        gv.setAdapter(bAdapter);
        gv.setOnItemClickListener(this);
    }

    private void initData(){
        checkPermission();
    }

    //检查权限创建app的文件夹。
    private void checkPermission(){
        int currSDKVersion = android.os.Build.VERSION.SDK_INT;
        Log.d(TAG,"currSDKVersion = "+currSDKVersion);
        //安卓6.0以前的版本不需要检查权限。
        if(currSDKVersion < 23){
            createAppFold();
            //startReadDBTask();
            Message msg = handler.obtainMessage();
            handler.sendMessage(msg);
        }
        else {
            int permissionCheck1 = ContextCompat.checkSelfPermission(BookshelfApp.getBookshelfApp(), Manifest.permission.READ_EXTERNAL_STORAGE);
            int permissionCheck2 = ContextCompat.checkSelfPermission(BookshelfApp.getBookshelfApp(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck1 != PackageManager.PERMISSION_GRANTED || permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permisson!");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        FILEPERMISSION);
            } else {
                Log.d(TAG, "getFileList(file)");
                createAppFold();
                //startReadDBTask();
                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
            }
        }
    }

    private void startReadDBTask(){
        if(books.size() == 0) {
            PrepareAppTask prepareAppTask = new PrepareAppTask((BookshelfGridAdapter) gv.getAdapter());
            prepareAppTask.execute();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FILEPERMISSION) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d(TAG,"onRequestPermissionsResult");
                createAppFold();
                //startReadDBTask();
                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
            }
            else{
                //如果不授权权限，则应该退出。
                this.finish();
            }
        }
    }

    private void createAppFold(){
        AppFoldUtils.createOneFold(FOLD_CACHE);
        AppFoldUtils.createOneFold(FOLD_BOOKS);
        AppFoldUtils.createOneFold(FOLD_IMG);
        AppFoldUtils.createOneFold(FOLD_USER);
        AppFoldUtils.createOneFold(FOLD_DB);
    }

    private class BookshelfGridAdapter extends BaseAdapter{

        LayoutInflater inflater;
        public BookshelfGridAdapter() {
            inflater = LayoutInflater.from(BookshelfApp.getBookshelfApp());

        }

        @Override
        public int getCount() {
            return books.size()+1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if(convertView==null){
                convertView = inflater.inflate(R.layout.bookshelf_item,null);
                vh = new ViewHolder();
                vh.iv = (ImageView)convertView.findViewById(R.id.bookiv);
                vh.tv = (TextView)convertView.findViewById(R.id.bookname);
                convertView.setTag(vh);
            }
            else{
                vh = (ViewHolder)convertView.getTag();
            }
            if(position == books.size())
            {
                vh.iv.setImageResource(R.mipmap.addbook);
                vh.tv.setText("");
            }
            else {
                vh.iv.setImageResource(R.mipmap.bookcover);

                String name = books.get(position).getName();
                if(name == null){
                    Log.d(TAG,"name is null");
                    vh.tv.setText("");
                }
                else{
                    Log.d(TAG,"pos = "+position+" name ="+name);
                    vh.tv.setText(name);
                }
            }

            return convertView;
        }

        public class ViewHolder{
            public ImageView iv;
            public TextView tv;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position == books.size()){
            Log.d(TAG,"add new book");
            addNewBook();
        }
        else{
            Log.d(TAG,"open book! position = "+position);
            openBook(position);

        }
    }

    private void addNewBook(){
        Intent intent = new Intent();
        intent.setClass(this,AddNewBookActivity.class);
        this.startActivity(intent);
    }

    private void openBook(int position){
        BookshelfApp bookApp = BookshelfApp.getBookshelfApp();
        String path = bookApp.getBooks().get(position).getPath();
        Log.d(TAG,"path = "+path);
        File file = new File(path);
        if(!file.exists()){
            Log.d(TAG,"file not exists!");
            return;
        }
        else{
            Log.d(TAG,"file exists");
        }
        bookApp.setCurrMyBookIndx(position);
        Log.d(TAG,"position = "+position);
        Intent intent = new Intent(this,ChapterContentActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void finish() {
        super.finish();
        books = null;
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

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            readDB();
            ((BookshelfGridAdapter)gv.getAdapter()).notifyDataSetChanged();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        if(books == null){
            books = BookshelfApp.getBookshelfApp().getBooks();
            //this.startReadDBTask();
            Message msg = handler.obtainMessage();
            handler.sendMessage(msg);
        }
        else{
            ((BookshelfGridAdapter)gv.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //books = null;
        Log.d(TAG,"onStop");
    }

}
