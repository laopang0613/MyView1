package com.example.pangyinglei.myview1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class FileScanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,View.OnClickListener{

    private static final String TAG = "FileScanActivity";
    private static final String fileSuffix = ".txt";

    private List<File> fileList = new ArrayList<File>();
    private ListView fileListView;
    private FileListAdapter fileListAdapter;
    private Stack<File> fileStack = new Stack<File>();
    private Button fileScanBtn;
    private Button addToBookShelfBtn;

    //checkbox选中的文件。
    private List<File> selectFiles = new ArrayList<File>();

    //实际加入到书架到文件
    private List<File> realSelectFiles = new ArrayList<File>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_scan);

        init();
    }

    private void init(){
        Log.d(TAG,"init6");
        getFiles();
        //fileStack.push(file);

        //MyFileUtils.listAvaliableStorage(BookshelfApp.getBookshelfApp());
        //MyFileUtils.getStoragePath(BookshelfApp.getBookshelfApp(),false);
        fileScanBtn = (Button)findViewById(R.id.file_scan_btn);
        fileScanBtn.setOnClickListener(this);
        fileListView  = (ListView)findViewById(R.id.filescan_filelist);
        fileListAdapter = new FileListAdapter();
        fileListView.setAdapter(fileListAdapter);
        //fileListView.setSelection();
        fileListView.setOnItemClickListener(this);
        fileStack.clear();
        addToBookShelfBtn = (Button)findViewById(R.id.file_scan_addtobookshelf);
        addToBookShelfBtn.setOnClickListener(this);
        selectFiles.clear();
        realSelectFiles.clear();
    }

    private class FileListAdapter extends BaseAdapter{

        private LayoutInflater inflater;

        public FileListAdapter(){
            inflater = LayoutInflater.from(BookshelfApp.getBookshelfApp());
        }

        @Override
        public int getCount() {
            return fileList.size();
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
            if(convertView == null){
                convertView = inflater.inflate(R.layout.filescan_filelistitem,null);
                vh = new ViewHolder();
                vh.iv = (ImageView)convertView.findViewById(R.id.filelistitem_iv);
                vh.tv = (TextView)convertView.findViewById(R.id.filelistitem_tv);
                vh.cb = (CheckBox)convertView.findViewById(R.id.filelistitem_cb);
                convertView.setTag(vh);
            }
            else{
                vh =(ViewHolder)convertView.getTag();
            }
            if(fileList.get(position).isDirectory()){
                vh.iv.setImageResource(R.mipmap.filefoldericon);
                vh.cb.setVisibility(View.GONE);
            }
            else {
                vh.iv.setImageResource(R.mipmap.txticon);
                if(isBookExist(fileList.get(position))){
                    vh.cb.setEnabled(false);
                    vh.cb.setClickable(false);
                    vh.cb.setFocusable(false);
                }
                else{
                    vh.cb.setEnabled(true);
                    vh.cb.setClickable(false);
                    vh.cb.setFocusable(false);
                    if(selectFiles.contains(fileList.get(position))){
                        vh.cb.setChecked(true);
                    }
                    else {
                        vh.cb.setChecked(false);
                    }
                }
                vh.cb.setVisibility(View.VISIBLE);
            }
            vh.tv.setText(fileList.get(position).getName());
            return convertView;
        }



        public class ViewHolder{
            ImageView iv;
            TextView tv;
            CheckBox cb;
        }
    }

    private void getStorageList(){
        StorageManager storageManager = (StorageManager)BookshelfApp.getBookshelfApp().getSystemService(Context.STORAGE_SERVICE);
    }


    //初始化显示的文件列表。
    private void getFiles(){
        String name = Environment.getExternalStorageDirectory().getAbsolutePath();
        //Log.d(TAG,"name = "+name);
        File file = new File(name);
        //写死华为真机目录，目前尚无法通过代码获取到目录storage/sdcard0;
        //File file = new File(HUAWEI_STORAGE_DIR);
        //fileStack.push(file);
        if(file.exists() == false){
            Log.d(TAG,"file isnot exists");
        }
        if(file.isDirectory()) {
            Log.d(TAG,"file is dir");
            getFileList(file);
        }
    }

    //获取要显示的文件列表
    private void getFileList(File file){
        this.fileList.clear();
        Log.d(TAG,"filelist clear done！ filename ="+file.getName());
        File[] files =  file.listFiles();
        if(files == null){
            Log.d(TAG,"files == null");
        }
        Log.d(TAG,"files.length = "+files.length);
        for(File f:files){
            if(f.isDirectory()||f.getName().endsWith(fileSuffix)) {
                fileList.add(f);
                //Log.d(TAG,"filename = "+ f.getName());
            }
        }

        sortFile(fileList);
    }

    //文件按名字排序
    private void sortFile(List fileList){
        int count = fileList.size();
        String[] fileNames = new String[count];
        File[] files = new File[count];
        fileList.toArray(files);
        Map<String,File> fileMap = new HashMap<String,File>();
        for(int i = 0;i<count;i++){
            fileNames[i] = files[i].getName();
            fileMap.put(fileNames[i],files[i]);
        }
        Arrays.sort(fileNames,new MyComparetor());
        fileList.clear();
        for(String s:fileNames){
            fileList.add(fileMap.get(s));
        }
    }

    //按文件名比较大小
    private class MyComparetor implements Comparator<String>{
        @Override
        public int compare(String o1, String o2) {
            if(o1.compareToIgnoreCase(o2) < 0){
                return  -1;
            }
            else if(o1.compareToIgnoreCase(o2) > 0){
                return 1;
            }
            return 0;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG,"pos ="+position);
        File file = this.fileList.get(position);
        //Log.d(TAG,"filename = "+file.getName());
       // Log.d(TAG,"filepath = "+file.getAbsolutePath());
        if(file.isDirectory()){
            fileStack.push(file);
            this.getFileList(file);
           // Log.d(TAG,"filelistsize = "+fileList.size());
            selectFiles.clear();
            fileListAdapter.notifyDataSetChanged();
        }
        else{
            //selectDone(position);
            if(view == null){
                Log.e(TAG,"filescan view == null");
            }
            FileListAdapter.ViewHolder vh = (FileListAdapter.ViewHolder) view.getTag();
            if(!isBookExist(fileList.get(position))) {
                if (selectFiles.contains(fileList.get(position))) {
                    vh.cb.setChecked(false);
                    selectFiles.remove(fileList.get(position));
                }
                else{
                    vh.cb.setChecked(true);
                    selectFiles.add(fileList.get(position));
                }
                //fileListAdapter.notifyDataSetChanged();
            }
        }

    }

    private boolean isBookExist(File file){
        List<MyBook> books = BookshelfApp.getBookshelfApp().getBooks();
        for(MyBook mb:books){
            if(mb.getPath().equals(file.getAbsolutePath())){
                Log.d(TAG,"book is exist");
                return true;
            }
        }
        return false;
    }

    //选择文件添加到书架
    private void selectDone(int position){
        File file = this.fileList.get(position);
        //Log.d(TAG,"file name = "+file.getName());
        //Log.d(TAG,"file path = "+file.getAbsolutePath());
        if(!isBookExist(file)) {
            MyBook mb = new MyBook();
            mb.setName(file.getName());
            mb.setPath(file.getAbsolutePath());
            BookshelfApp.getBookshelfApp().getBooks().add(mb);
            BookDBHelper.insertBook(file.getName(),file.getAbsolutePath());
            Intent home = new Intent(this,MainActivity.class);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(home);
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.file_scan_btn:
                Log.d(TAG, "button is click");
                if (fileStack.empty()) {
                    Log.d(TAG, "stack is empty");
                } else {
                    File subfile = fileStack.pop();
                    File file = subfile.getParentFile();
                    //Log.d(TAG, "file name is " + subfile.getName()+"parentFile ="+file.getName());
                    this.getFileList(file);
                    fileListAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.file_scan_addtobookshelf:
                addToBookShelf();
                break;
        }

    }

    private void addToBookShelf(){
        if(selectFiles.isEmpty()){
            return;
        }
        for(File file:selectFiles) {
            realSelectFiles.add(file);
            MyBook mb = new MyBook();
            mb.setName(file.getName());
            mb.setPath(file.getAbsolutePath());
            BookshelfApp.getBookshelfApp().getBooks().add(mb);
        }
        selectFiles.clear();
        this.fileListAdapter.notifyDataSetChanged();
//        for(File file:selectFiles){
//            BookDBHelper.insertBook(file.getName(),file.getAbsolutePath());
//        }
    }

    private void addDB(){
        BookDBHelper bookDBHelper = new BookDBHelper(BookshelfApp.getBookshelfApp());
        SQLiteDatabase db = bookDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        db.beginTransaction();
        try {
            for (File file : realSelectFiles) {
                cv.put("bookName", file.getName());
                cv.put("bookPath", file.getAbsolutePath());
                cv.put("bookCurrChapterIndx", 0);
                db.insert("bookTable", null, cv);
            }
            realSelectFiles.clear();
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        bookDBHelper.closeDB(db);
    }

    @Override
    protected void onStop() {
        super.onStop();
        final Handler handler = new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                addDB();
            }
        });

        if(fileList.size()!=0){
            fileList.clear();
        }
        this.fileList = null;
        if(!fileStack.empty()){
            fileStack.clear();
        }
        fileStack = null;
        if(!selectFiles.isEmpty()){
            selectFiles.clear();
        }
        selectFiles = null;
    }
}
