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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener,View.OnClickListener{

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

    private boolean isDeleteMode = false;
    private List<String> selectDeleteBooks = new ArrayList<String>();

    private Button deleteBtn;
    private Button finishBtn;
    private Button selectallBtn;
    private boolean isSelectAllStatus = false;
    private static final String DELETEBOOKS = "deleteBooksFromDB";
    private static final String READBOOKS = "readBooksFromDB";
    private static final String BOOKKEY = "msgs";

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
        gv.setOnItemLongClickListener(this);
        deleteBtn = (Button)findViewById(R.id.bookgrid_delete);
        finishBtn = (Button)findViewById(R.id.bookgrid_finish);
        selectallBtn = (Button)findViewById(R.id.bookgrid_selectall);
        deleteBtn.setOnClickListener(this);
        finishBtn.setOnClickListener(this);
        selectallBtn.setOnClickListener(this);
        deleteBtn.setVisibility(View.GONE);
        selectallBtn.setVisibility(View.GONE);
        finishBtn.setVisibility(View.GONE);

    }

    private void initData(){
        checkPermission();
        selectDeleteBooks.clear();
    }

    //检查权限创建app的文件夹。
    private void checkPermission(){
        int currSDKVersion = android.os.Build.VERSION.SDK_INT;
        Log.d(TAG,"currSDKVersion = "+currSDKVersion);
        //安卓6.0以前的版本不需要检查权限。
        if(currSDKVersion < 23){
            createAppFold();
            //startReadDBTask();
            sendDealDBMsg(READBOOKS);
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
                sendDealDBMsg(READBOOKS);
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
                sendDealDBMsg(READBOOKS);
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

    public String dealName(String name){
        name = name.substring(0,name.length() - 4);
        if(name.length() > 20){
            return name.substring(0,20)+"...";
        }
        return name;
    }

    private class BookshelfGridAdapter extends BaseAdapter{

        LayoutInflater inflater;
        public BookshelfGridAdapter() {
            inflater = LayoutInflater.from(BookshelfApp.getBookshelfApp());

        }

        @Override
        public int getCount() {
            if(isDeleteMode){
                return books.size();
            }
            else {
                return books.size() + 1;
            }
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
                vh.cb = (CheckBox)convertView.findViewById(R.id.bookcheckbox);
                vh.cb.setFocusable(false);
                vh.cb.setClickable(false);
                convertView.setTag(vh);
            }
            else{
                vh = (ViewHolder)convertView.getTag();
            }
            if(isDeleteMode){
                vh.iv.setImageResource(R.mipmap.bookcover);

                String name = books.get(position).getName();
                if (name == null) {
                    Log.d(TAG, "name is null");
                    vh.tv.setText("");
                } else {
                    Log.d(TAG, "pos = " + position + " name =" + name);
                    vh.tv.setText(dealName(name));
                }
                BookshelfApp bookApp = BookshelfApp.getBookshelfApp();
                String path = bookApp.getBooks().get(position).getPath();
                vh.cb.setVisibility(View.VISIBLE);
                if(selectDeleteBooks.contains(path)){
                    vh.cb.setChecked(true);
                }
                else{
                    vh.cb.setChecked(false);
                }
            }
            else {
                vh.cb.setVisibility(View.GONE);
                if (position == books.size()) {
                    vh.iv.setImageResource(R.mipmap.addbook);
                    vh.tv.setText("");
                } else {
                    vh.iv.setImageResource(R.mipmap.bookcover);

                    String name = books.get(position).getName();
                    if (name == null) {
                        Log.d(TAG, "name is null");
                        vh.tv.setText("");
                    } else {
                        Log.d(TAG, "pos = " + position + " name =" + name);
                        vh.tv.setText(dealName(name));
                    }
                }
            }

            return convertView;
        }

        public class ViewHolder{
            public ImageView iv;
            public TextView tv;
            public CheckBox cb;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(isDeleteMode){
            BookshelfApp bookApp = BookshelfApp.getBookshelfApp();
            String path = bookApp.getBooks().get(position).getPath();
            BookshelfGridAdapter.ViewHolder vh = (BookshelfGridAdapter.ViewHolder)view.getTag();
            Log.d(TAG,"onItemClick selectDeletebooks.size="+selectDeleteBooks.size());
            if(selectDeleteBooks.contains(path)){
                vh.cb.setChecked(false);
                selectDeleteBooks.remove(path);
                Log.d(TAG,"onItemClick remove path="+path);
            }
            else{
                vh.cb.setChecked(true);
                selectDeleteBooks.add(path);
                Log.d(TAG,"onItemClick add path="+path);
            }
        }
        else {
            if (position == books.size()) {
                Log.d(TAG, "add new book");
                addNewBook();
            } else {
                Log.d(TAG, "open book! position = " + position);
                openBook(position);

            }
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
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        BookshelfApp bookApp = BookshelfApp.getBookshelfApp();
        String path = bookApp.getBooks().get(position).getPath();
        BookshelfGridAdapter.ViewHolder vh = (BookshelfGridAdapter.ViewHolder)view.getTag();
        Log.d(TAG,"longclick path ="+path);
        if(!isDeleteMode){
            isDeleteMode = true;
            selectDeleteBooks.add(path);
            vh.cb.setChecked(true);
            ((BookshelfGridAdapter)gv.getAdapter()).notifyDataSetChanged();
            deleteBtn.setVisibility(View.VISIBLE);
            finishBtn.setVisibility(View.VISIBLE);
            selectallBtn.setVisibility(View.VISIBLE);
        }
        if(!selectDeleteBooks.contains(path)) {
            selectDeleteBooks.add(path);
            vh.cb.setChecked(true);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bookgrid_delete:
                deletebooks();
                break;
            case R.id.bookgrid_finish:
                exitForDelete();
                ((BookshelfGridAdapter) gv.getAdapter()).notifyDataSetChanged();
                break;
            case R.id.bookgrid_selectall:
                selectAll();
                break;
        }
    }

    private void deletebooks(){
        if(selectDeleteBooks.isEmpty()){
            return;
        }
        List<MyBook> books = BookshelfApp.getBookshelfApp().getBooks();
//        Iterator<MyBook> iterator = books.iterator();
//        while(iterator.hasNext()){
//            MyBook book = iterator.next();
//            Log.d(TAG,"book.path="+book.getPath());
//            if(selectDeleteBooks.contains(book.getPath())){
//                iterator.remove();
//                Log.d(TAG,"iterator remove book.path ="+book.getPath());
//            }
//        }
        for(int i= 0;i<selectDeleteBooks.size();i++){
            Log.d(TAG,"selectbooks["+i+"]="+selectDeleteBooks.get(i));
        }

        for(Iterator<MyBook> iterator = books.iterator();iterator.hasNext();){
            MyBook book = iterator.next();
            Log.d(TAG,"book.path="+book.getPath());
            if(selectDeleteBooks.contains(book.getPath())){
                iterator.remove();
                Log.d(TAG,"iterator remove book.path ="+book.getPath());
            }
        }
        sendDealDBMsg(DELETEBOOKS);
    }



    private void deleteBooksFromDB(){
        BookDBHelper bookDBHelper = new BookDBHelper(BookshelfApp.getBookshelfApp());
        SQLiteDatabase readDB = bookDBHelper.getReadableDatabase();
        SQLiteDatabase writeDB = bookDBHelper.getWritableDatabase();
        int len = selectDeleteBooks.size();
        writeDB.beginTransaction();
        Cursor cursor = null;
        try{
            for(int i = 0;i < len;i++){
                String path = selectDeleteBooks.get(i);
                cursor = readDB.query("bookTable",new String[]{"bookId"},"bookPath=?",new String[]{path},null,null,null);
                int bookId;
                while(cursor.moveToNext()){
                    bookId = cursor.getInt(cursor.getColumnIndex("bookId"));
                    Log.d(TAG,"delete bookId="+bookId+"path ="+path);
                    writeDB.delete("chapterTable","bookId=?",new String[]{String.valueOf(bookId)});
                }

                writeDB.delete("bookTable","bookPath=?",new String[]{path});
            }
            if(cursor != null){
                cursor.close();
            }
            writeDB.setTransactionSuccessful();
        }finally{
            writeDB.endTransaction();
        }
        readDB.close();
        writeDB.close();
        selectDeleteBooks.clear();
        exitForDelete();
        ((BookshelfGridAdapter) gv.getAdapter()).notifyDataSetChanged();
    }


    private void exitForDelete(){
        selectDeleteBooks.clear();
        isDeleteMode = false;
        isSelectAllStatus = false;
        deleteBtn.setVisibility(View.GONE);
        selectallBtn.setVisibility(View.GONE);
        finishBtn.setVisibility(View.GONE);
    }

    private void selectAll(){
        if(isSelectAllStatus) {
            selectallBtn.setText("全选");
            selectDeleteBooks.clear();
            ((BookshelfGridAdapter) gv.getAdapter()).notifyDataSetChanged();
            isSelectAllStatus = false;
        }
        else
        {
            selectallBtn.setText("取消全选");
            List<MyBook> books = BookshelfApp.getBookshelfApp().getBooks();
            int len = books.size();
            for (MyBook book : books) {
                if (!selectDeleteBooks.contains(book.getPath())) {
                    selectDeleteBooks.add(book.getPath());
                }
            }
            ((BookshelfGridAdapter) gv.getAdapter()).notifyDataSetChanged();
            isSelectAllStatus = true;
        }
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

    private void sendDealDBMsg(String str){
        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString(BOOKKEY,str);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if(bundle.get(BOOKKEY) == READBOOKS) {
                Log.d(TAG,"handler readBooks");
                readDB();
                ((BookshelfGridAdapter) gv.getAdapter()).notifyDataSetChanged();
            }
            else if(bundle.get(BOOKKEY) == DELETEBOOKS){
                Log.d(TAG,"handler deletebooks");
                deleteBooksFromDB();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        if(books == null){
            books = BookshelfApp.getBookshelfApp().getBooks();
            //this.startReadDBTask();
            this.sendDealDBMsg(READBOOKS);
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
        exitForDelete();
    }

}
