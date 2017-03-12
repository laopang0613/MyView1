package com.example.pangyinglei.myview1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.storage.StorageManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pangyinglei on 2017/1/4.
 */

public class MyFileUtils {

    private static final String TAG = "MyFileUtils";
    //private static final String REGEX = "\u7b2c{1}.+\u7ae0{1}.+[\\r\\n]{1}?";

    // 零 \u96f6 一 \u4e00  二 \u4e8c 三 \u4e09 四 \u56db 五 \u4e94 六 \u516d 七 \u4e03
    // 八 \u516b 九 \u4e5d 十 \u5341  百 \u767e 千 \u5343 万 \u4e07 卷 \u5377

    public static final String FILESUFFIX = ".txt";
    public static final int SFILECHAPTERCOUNT = 50;
    private static final String NUMREGEX1 = "[\u96f6\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\\d]";
    private static final String NUMREGEX2 = "[\u96f6\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\\d]";
    private static final String VOLUMEREGEX = "(\u7b2c"+NUMREGEX1+"{1,7}\\u5377)?(?>\\s*)";
    private static final String CHAPTERREGEX = "(\u7b2c"+NUMREGEX2+"{1,9}\u7ae0)(?>.+)$";
    private static final String REGEX = VOLUMEREGEX+CHAPTERREGEX;

    //每次读取buff，都选取buff最后SHARESTRLEN个字符添加到下次buff的开头中。
    private static final int SHARESTRLEN = 50;

    //存储章节。
    //private List<Chapter> chapterList = new ArrayList<Chapter>();
    //存储章节名以及章节名在文本中的索引位置。
    //private Map<String,Long> chapterIndxMap = new HashMap<String,Long>();
    private static Matcher matcher;
    private static Pattern pattern = Pattern.compile(REGEX,Pattern.MULTILINE);

    private MyCustomView myCustomView;
    //String prevStr = "";

    private static Paint mPaint = new Paint();

    private static float lineWidth = 960f;
    private static float lineHeight = 55f;
    //private static float txtTopXStart = 60f;
    private static float txtTopYStart = 150f;
    //private int lineNumInPage = 26;
    private static float txtSize = 55f;
    private static float lineSpace = 27f;
    private static float paraSpace = 55f;
    private static float firstPageYStart = 500f;
    private static float pageIndxSize = 30f;

    //private static List<String> smallFileList = new ArrayList<String>();

    public MyFileUtils() {
        BookshelfApp.getBookshelfApp().getCurrMyBook().getChapterList().clear();
        initPaint();
    }


    private void initPaint(){
        mPaint.setTextSize(txtSize);
    }

    public static Paint getPaint(){
        return mPaint;
    }

    public static DisplayMetrics getDisplayMetrics(){
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager)BookshelfApp.getBookshelfApp().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    public void createNewFile(String fileName){
        File file = new File(fileName);
        if(file.exists() == false){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //删除目录/storage/emulate/0/PPReader/books/下的bookfile.
    public void deleteBookFile(String fileName){
        //BookshelfApp.getBookshelfApp().deleteFile(fileName);
        File file = new File(AppFoldUtils.getFoldPath("books")+"/"+fileName);
        if(file.exists()){
            file.delete();
        }
    }

    public void deleteAllBooksFile(){
        File file = new File(AppFoldUtils.getFoldPath("books"));
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File f:files){
                f.delete();
            }
        }
    }

    public String[] getAllFileName(){
        String[] fileNames = BookshelfApp.getBookshelfApp().fileList();
        for(String fileName:fileNames){
            Log.d(TAG,"fileName = "+fileName);
        }
        return fileNames;
    }

    public static String getChapterTitleTrim(String title){
        return title.trim().replaceFirst("\\s*\u7b2c","\u7b2c");
    }

    public static void getChapterNameAndIndx(String destStr,int mCharCount,int shareStrlen,StringBuffer prevStr){
       // Log.d(TAG,"prevstr = "+prevStr);
        matcher = pattern.matcher(destStr);

        while(matcher.find()){
            int start0  = matcher.start(0);
            int end0 = matcher.end(0);
            String str0 = matcher.group(0);
            String str2 = matcher.group(2);
            //Log.d(TAG,"str2 = "+str2+"str0 = "+str0+"prevStr ="+prevStr.toString());
            //如果有相同的章节名，保留前面的章节名。
            if(str2.equals(prevStr.toString()) == false) {
                //chapterList.add(str0.trim());
                Chapter chapter = new Chapter();
                //chapter.setName(str0.trim());
                str0 = getChapterTitleTrim(str0);
                chapter.setName(str0);
                int beginTitleIndex = mCharCount+start0 - shareStrlen;
                chapter.setBeginCharIndex(beginTitleIndex);
                int beginContentIndex = mCharCount+end0 - shareStrlen;
                chapter.setBeginContentIndex(beginContentIndex);
                chapter.setCurrPageNumIndx(0);
                BookshelfApp.getBookshelfApp().getCurrMyBook().getChapterList().add(chapter);
                //Log.d(TAG,"chaptename = "+chapter.getName());
                //chapterIndxMap.put(str0,beginIndex);
                //Log.d(TAG,"str0 = "+ str0 + " beginIndex = "+beginTitleIndex +" end0 = "+end0 + "str2 = "+str2);
                //Log.d(TAG,"str0.len = "+str0.length());
            }
            else{
                //Log.d(TAG,"prev = next str = "+prevStr);
            }
            prevStr.setLength(0);
            prevStr.append(str2);
        }

    }

    public static String deal()  {
        //Log.d(TAG,"start deal");
        //pattern = Pattern.compile(regex,Pattern.MULTILINE);
        FileInputStream fis = null;
        BufferedReader br = null;
        //StringBuilder sb = new StringBuilder();
        StringBuffer sb = new StringBuffer();
        StringBuffer sb2 = new StringBuffer();
        char[] buff = new char[1024*4];
        int charCount = 0;

        //String shareStr = "";
        StringBuffer shareStr = new StringBuffer("");
        StringBuffer prevStr = new StringBuffer("");
        BookshelfApp bookshelfApp = BookshelfApp.getBookshelfApp();

        MyBook mb = bookshelfApp.getCurrMyBook();
        try {
            //Log.d(TAG,"name = "+mb.getChapterList());
            //Log.d(TAG,"path = "+mb.getPath()+" name = "+mb.getName());
            //fis = bookshelfApp.openFileInput(mb.getName());
            //fis = bookshelfApp.openFileInput(mb.getPath());
            //openFileInput的参数只能是/data/data/<package name>/files目录下应用的私有文件名字，且不能带分隔符；
            //如果是别的目录下的文件，还是要用FileInputStream


            fis = new FileInputStream(new File(mb.getPath()));
            if(fis != null){
                Log.d(TAG,"fis!= null");
            }
            br = new BufferedReader(new InputStreamReader(fis,"gb2312"));

            while(br.read(buff,0,buff.length)!=-1){
               int length = buff.length;
                //String tmpStr = String.valueOf(buff,0,length);
                sb2.setLength(0);
                sb2.append(buff,0,length);

                sb.setLength(0);
                sb.append(shareStr.toString()).append(buff,0,length);

                //把共用的字串添加到下次buff字串开头。避免后续匹配章节名时，章节名被读取的buff截断而导致丢失。
                //String sumStr = shareStr + tmpStr;
                getChapterNameAndIndx(sb.toString(),charCount,shareStr.length(),prevStr);
                //每读一次buff，都截取最后一部分字符。
                shareStr.setLength(0);
               // shareStr = tmpStr.substring(length - SHARESTRLEN,length);

                //shareStr.append(buff,0,length).substring(length - SHARESTRLEN,length);
                shareStr.append(buff,length - SHARESTRLEN,SHARESTRLEN);

                charCount += length;
            }
            mb.setCharTotalCount(charCount);
            //Log.d(TAG,"chartotalcount = "+charCount+"chaptercount = "+mb.getChapterList().size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } finally{
            try {
                closeStream(br);
                closeStream(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String fileNamePrefix = mb.getName().substring(0,mb.getName().length() - 4);
        //mContext.deleteFile("sishen.txt");

        //deleteAllBooksFile();
        //去掉分割文件，分割文件会多占用户一倍存储空间。
        //cutFile(fileNamePrefix);
        Log.d(TAG,"cut done");

        //预加载后一章,即第二章的内容。
        String nextChapterContent = MyFileUtils.getNextChapterContent();
        Log.d(TAG,"nextchaptercontent="+nextChapterContent);
        mb.getNextChapter().setContent(nextChapterContent);
        setPageNumList(nextChapterContent,1,mb,mPaint);
//        mb.setCacheNextChapterIndx(1);


        //获取当前章节内容。
        String currChapterContent = getChapterContent(mb.getCurrChapterIndx(),mb);
        //Log.d(TAG,"currChaterContent = "+currChapterContent);
        mb.getCurrChapter().setContent(currChapterContent);
        //获取当前章节的每页索引。
        setPageNumList(currChapterContent,mb.getCurrChapterIndx(),mb,mPaint);

        //缓存第1，2章
        Set<Integer> cacheChapterIndxs = mb.getCacheChapterIndxs();
        Log.d(TAG,"cacheChapterIndxs.size="+cacheChapterIndxs.size());
        synchronized (mb.getCacheChapterIndxs()){
            MyFileUtils.addCacheChapterIndx(0);
        }
        synchronized (mb.getCacheChapterIndxs()){
            MyFileUtils.addCacheChapterIndx(1);
        }

        if(mb.getCurrChapter().getPageNumList().size() == 0){
            Log.e(TAG,"pageNumList.size == 0");
        }
        //这里page第一页开头会有中文全角空格\u3000。
        //currChapterContent = currChapterContent.trim().replaceFirst("[\\s\\u3000]*？"," ");
        Chapter chapter = mb.getCurrChapter();
        int pageNumIndx = chapter.getCurrPageNumIndx();
        if(pageNumIndx == 0){
            return currChapterContent.substring(0,chapter.getPageNumList().get(0));
        }
        else{
            return currChapterContent.substring(chapter.getPageNumList().get(pageNumIndx - 1 ),chapter.getPageNumList().get(pageNumIndx));
        }
        //getStr("sishen");
    }


    private static void cutFile(String fileNamePrefix){
        BookshelfApp bookshelfApp = BookshelfApp.getBookshelfApp();
        MyBook mb = bookshelfApp.getCurrMyBook();
        int smallFileTotal = 0;
        int smallFileIndx = 0;
        String smallFileName = fileNamePrefix + smallFileIndx+ FILESUFFIX;
        //Log.d(TAG, "smallFile =" + smallFileName + "smallfile.len =" + smallFileName.length());
        int chapterSize = mb.getChapterList().size();
        Log.d(TAG,"chapterSize = "+chapterSize);
        //按50章一个文件计算。
        if(chapterSize % SFILECHAPTERCOUNT == 0)
        {
            smallFileTotal = chapterSize/SFILECHAPTERCOUNT;
            //Log.d(TAG,"up smallFileTotal ="+smallFileTotal);
        }
        else{
            smallFileTotal = chapterSize/SFILECHAPTERCOUNT + 1;
            //Log.d(TAG,"down smallFileTotal ="+smallFileTotal);
        }

        FileOutputStream fos = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        char[] buff = new char[1024*4];
        //long beginCharIndx = 0;
        int currCharCount = 0;
        StringBuffer overflowCharBuff = new StringBuffer();
        int everyCount = 0;
        try {
            //fos = bookshelfApp.openFileOutput(smallFileName,Context.MODE_APPEND);
            //分割后的books写入目录/storage/emulate/0/PPReader/books
            fos = new FileOutputStream(new File(AppFoldUtils.getFoldPath("books")+"/"+smallFileName),true);
            //smallFileList.add(smallFileName);
            bw = new BufferedWriter(new OutputStreamWriter(fos,"gb2312"));
            //br = new BufferedReader(new InputStreamReader(bookshelfApp.openFileInput(mb.getName()),"gb2312"));
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(mb.getPath())),"gb2312"));
            //br.skip(mb.getChapterList().get(0).getBeginCharIndex());
            while((everyCount = br.read(buff,0,buff.length))!=-1){
                //Log.d(TAG,"everyCount = "+everyCount+" buff.len ="+buff.length);
                currCharCount += everyCount;
                //获得第51，101，151，。。。的章节开头字符索引。
                int fileCharCount;
                //如果是最后一个文件，字节数应该是总字数。
                if(smallFileIndx == smallFileTotal - 1){
                    fileCharCount = mb.getCharTotalCount();
                    //Log.d(TAG,"1   smallfileindx ="+smallFileIndx+"     filetotal = "+smallFileTotal);
                }
                else {
                    //Log.d(TAG, "2    smallfileindx =" + smallFileIndx + " smallFileTotal = " + smallFileTotal);
                    fileCharCount = mb.getChapterList().get((smallFileIndx + 1) * SFILECHAPTERCOUNT).getBeginCharIndex();
                }
                //Log.d(TAG,"fileCharCount = "+ fileCharCount+" currCharCount = "+currCharCount);
                if(currCharCount<= fileCharCount){
                    bw.write(buff,0,everyCount);
                    //bw.flush();
                }
                else{
                    //Log.d(TAG,"smallFileIndx = "+smallFileIndx);
                    //计算超过的字符数。
                    int overflowCharNum = (int)(currCharCount - fileCharCount);
                    overflowCharBuff.setLength(0);
                    //Log.d(TAG,"overflowcharnum ="+overflowCharNum);
                    overflowCharBuff.append(buff,everyCount - overflowCharNum,overflowCharNum);
                    //Log.d(TAG,"overstr_len = "+overflowCharBuff.length());
                    //由于每次分割文件，都会把下一个文件的开头标题写入上一个文件末尾，所以将该标题去掉。
                    //原因是读取流之前br.skip(mb.getChapterList().get(0).getBeginCharIndex()); 造成的。
                    //overflowCharNum += mb.getChapterList().get((smallFileIndx + 1) * SFILECHAPTERCOUNT).getName().length();
                    //超出部分写入上一个文件。
                    bw.write(buff,0,everyCount - overflowCharNum);
                    bw.flush();
                    bw.close();
                    //重新打开到新文件的输出流。
                    smallFileIndx++;
                    smallFileName = fileNamePrefix+ smallFileIndx+ FILESUFFIX;
                    //fos = bookshelfApp.openFileOutput(smallFileName,Context.MODE_APPEND);
                    fos = new FileOutputStream(new File(AppFoldUtils.getFoldPath("books")+"/"+smallFileName),true);
                    bw = new BufferedWriter(new OutputStreamWriter(fos,"gb2312"));
                    bw.write(overflowCharBuff.toString());

                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                closeStream(br);
                //流bw依赖fos,先关闭外层流bw,再关闭内层流fos,否则报错：
                closeStream(bw);
                closeStream(fos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getStr(String fileNamePrefix){
        BookshelfApp bookshelfApp = BookshelfApp.getBookshelfApp();
        MyBook mb = bookshelfApp.getCurrMyBook();
        String smallFileName = fileNamePrefix + FILESUFFIX;



        BufferedReader br = null;

        char[] buff = new char[1024*4];
        //long beginCharIndx = 0;
        int currCharCount = 0;

        int count  = 0;
        try {

            br = new BufferedReader(new InputStreamReader(bookshelfApp.openFileInput(mb.getName()),"gb2312"));

            br.skip(834896);
            int charnum = 0;
            while((charnum = br.read(buff,0,buff.length))!=-1&&count<1){
                //currCharCount += buff.length;

                //int   fileCharCount = mb.getChapterList().get(49).getBeginCharIndex();

                //Log.d(TAG,"filecCount = "+ fileCharCount+" currCharCount = "+currCharCount);
                Log.d(TAG,"buff.str ="+String.valueOf(buff,0,charnum));
                Log.d(TAG,"buff.len ="+charnum);
                count++;

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                closeStream(br);
                //流bw依赖fos,先关闭外层流bw,再关闭内层流fos,否则报错：
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void closeStream(Object obj) throws IOException {
        if(obj!= null){
            if(obj instanceof FileOutputStream){
                FileOutputStream fos = (FileOutputStream)obj;
                fos.close();
                fos = null;
            }
            else if(obj instanceof FileInputStream){
                FileInputStream fis = (FileInputStream)obj;
                fis.close();
                fis = null;
            }
            else if(obj instanceof BufferedReader){
                BufferedReader br = (BufferedReader)obj;
                br.close();
                br = null;
            }
            else if(obj instanceof BufferedWriter){
                BufferedWriter bw = (BufferedWriter)obj;
                bw.close();
                bw = null;
            }

        }
    }

    public static String getChapterContent(int chapterIndx, MyBook mb){
        //获取章节属于小文件的文件索引，即第几个文件。
        //Log.d(TAG,"chapterIndx = "+chapterIndx);
        //int fileIndx = chapterIndx/MyFileUtils.SFILECHAPTERCOUNT;
        //获取文件名前缀。
        //String fileNamePrefix = mb.getName().substring(0,mb.getName().length() - 4);
        //获取章节所属小文件的文件名。
        //String smallFileName = fileNamePrefix+fileIndx+MyFileUtils.FILESUFFIX;
        //Log.d(TAG,"smallFileName = "+smallFileName);
        //获取小文件的起始第一章在原文件中的标题起始位置。
//        int smallFileBeginIndx;
//        if(fileIndx == 0){
//            smallFileBeginIndx = 0;
//        }
//        else {
//            smallFileBeginIndx = mb.getChapterList().get(fileIndx * MyFileUtils.SFILECHAPTERCOUNT).getBeginCharIndex();
//        }
        //Log.d(TAG,"smallfilebeginindx ="+smallFileBeginIndx);
        //获取当前章节的内容在原文件的内容起始位置。
        //Log.d(TAG,"size = "+ mb.getChapterList().size());
        int currChapterBeginIndx = mb.getChapterList().get(chapterIndx).getBeginContentIndex();
        Log.d(TAG,"currchapterBeginIndx = "+currChapterBeginIndx);
        //计算当前章节在小文件的的内容起始位置。
        //int skipCharNum = currChapterBeginIndx - smallFileBeginIndx;
        int skipCharNum = currChapterBeginIndx;
        Log.d(TAG,"skipCharNum = "+skipCharNum);
        int currChapterCharTotal = 0;
        //如果章节是原文最后一章，那么当前章节的字数应为 原文总字数减去当前章节在原文件中的内容起始位置。
        if(chapterIndx == mb.getChapterList().size()-1) {
            currChapterCharTotal = mb.getCharTotalCount() - currChapterBeginIndx;
            Log.d(TAG," up currchapterchartotal = "+currChapterCharTotal);
        }
        else{
            //否则，当前章节字数为下一章标题起始位置减去当前章节内容起始位置。
            currChapterCharTotal = mb.getChapterList().get(chapterIndx + 1).getBeginCharIndex() - currChapterBeginIndx;
            //Log.d(TAG,"curr chapterName ="+mb.getChapterList().get(chapterIndx).getName());
            //Log.d(TAG,"next chaptername ="+mb.getChapterList().get(chapterIndx+1).getName());
            //Log.d(TAG,"chapterindx="+chapterIndx+" next chaptercontent.len ="+mb.getChapterList().get(chapterIndx + 1).getBeginCharIndex());
            Log.d(TAG,"down currchapterchartotal = "+currChapterCharTotal);
        }

        BufferedReader br = null;
        FileInputStream fis = null;
        StringBuffer chapterContent = new StringBuffer("");
        try {
            //fis = BookshelfApp.getBookshelfApp().openFileInput(smallFileName);
            //分割后的文件写入storage/emulate/0/PPReader/books/
            //fis = new FileInputStream(new File(AppFoldUtils.getFoldPath("books")+"/"+smallFileName));
            fis = new FileInputStream(new File(BookshelfApp.getBookshelfApp().getCurrMyBook().getPath()));
            br = new BufferedReader(new InputStreamReader(fis,"gb2312"));
            br.skip(skipCharNum);
            int currCharCount = 0;
            int charCount = 0;
            char[] buff = new char[1024*4];
            while((charCount = br.read(buff,0,buff.length))!=-1){
                currCharCount += charCount;
                Log.d(TAG,"currCharcount = "+currCharCount);
                //如果当前读取字符总数小于当前章节总数，就存入stringbuff;
                if(currCharCount <= currChapterCharTotal){
                    chapterContent.append(buff,0,charCount);
                    //Log.d(TAG,"charCount = "+charCount);
                }
                else{
                    //否则，计算超出的字符数
                    int overCharCount = currCharCount - currChapterCharTotal;
                    //Log.d(TAG,"overCharcount = "+overCharCount+"charCount = "+charCount);
                    //从当前buff中减去超出的字符数，然后存入stringbuff.
                    chapterContent.append(buff,0,charCount - overCharCount);
                    break;
                }
            }
            Log.d(TAG,"chapterContent ="+chapterContent.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                MyFileUtils.closeStream(br);
                MyFileUtils.closeStream(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //去掉每章节的两头空白。
        //Log.d(TAG,"chaptecontent.len = "+chapterContent.toString().length());
        //Log.d(TAG,"chapterContent = "+chapterContent.toString().trim());
//        for(int i = 0;i<10;i++){
//            Log.d(TAG,"char[] ="+Integer.toHexString(chapterContent.toString().charAt(i)));
//        }
        //去掉每行开头的空白字符，包括中文全角空格\u3000.
        return chapterContent.toString().trim();
    }

    public static  void setPageNumList(String destStr,int chapterIndx,MyBook mb,Paint mPaint){
        mPaint.setTextSize(txtSize);
        int totalChar = destStr.length();
        //Log.d(TAG,"totalchar = "+totalChar);
        int pageEndIndx = 0;
        mb.getChapterList().get(chapterIndx).getPageNumList().clear();
        while(pageEndIndx < totalChar) {
            int pageCharCount = justifyedText(destStr,mPaint,mb,chapterIndx);
            if(pageCharCount <= 0){
                //Log.d(TAG,"deststr ="+destStr);
                return;
            }
            pageEndIndx += pageCharCount;
//            Log.d(TAG,"pageEndIndx = "+pageEndIndx+"pagecharcount = "+pageCharCount);
//            Log.d(TAG,"totalchar ="+totalChar);
            mb.getChapterList().get(chapterIndx).getPageNumList().add(pageEndIndx);
            destStr = destStr.substring(pageCharCount,destStr.length());
//            Log.d(TAG,"destStr ="+destStr.toString());
        }
        //Log.d(TAG,"end while pageEndIndx="+pageEndIndx+"totalChar ="+totalChar);
        //mb.getChapterList().get(chapterIndx).getPageNumList().add(totalChar);
    }
    //获取章节每页都最后一个字符索引。
    public static int justifyedText(String pageStr,Paint mPaint,MyBook mb,int chapterIndx){
        //Log.d(TAG,"pageStr ="+pageStr);
        float lastLineHeight;
        if(mb.getChapterList().get(chapterIndx).getPageNumList().size() == 0){
            lastLineHeight = firstPageYStart;
        }
        else{
            lastLineHeight = txtTopYStart;
        }
        //float maxLineHeight = getDisplayMetrics().heightPixels;// - lineHeight;
        float maxLineHeight = MyFileUtils.getDisplayMetrics().heightPixels - pageIndxSize - 20;
//        Log.d(TAG,"maxLineHeight ="+maxLineHeight);
        int pageEndCharIndx = 0;
        int lenth = pageStr.length();
        //Log.d(TAG,"lenth ="+lenth);
        float[] tmpOneWidth = new float[lenth];
        float tmpTotalWidth = 0;
        int lineBeginIndx = 0;
        int lineEndIndx = 0;
        int lineNum = 0;
        //String lineStr = "";
        StringBuffer lineStr = new StringBuffer("");
        //StringBuffer everyWord = new StringBuffer("");
        for(int i = 0; i< lenth;i++){
            //测量单个字符到字宽。
            tmpOneWidth[i] = mPaint.measureText(pageStr,i,i+1);
            //Log.d(TAG,"oneWidth ="+ tmpOneWidth[i]);
            //字符宽度累加
            tmpTotalWidth += tmpOneWidth[i];
            //如果累加到字符宽度大于行宽。
            if(tmpTotalWidth> lineWidth){
//                Log.d(TAG,"tmptotalwidth = "+tmpTotalWidth);
                //获取超过当前行宽的字符的索引。
                lineEndIndx = i;
                //计算实际显示的行宽（要减去超过的字符宽度）
                tmpTotalWidth -= tmpOneWidth[i];
                //Log.d(TAG,"960 tmptotalwidth = "+tmpTotalWidth);
                //获取当前行实际要显示的字符串
                lineStr.setLength(0);
                lineStr.append(pageStr.substring(lineBeginIndx,lineEndIndx));
                //Log.d(TAG,"line = "+lineStr.toString()+ " linebeginindx = "+lineBeginIndx+ " lineendindx = "+ lineEndIndx);
                //Log.d(TAG,"i = "+i+" lenth ="+lenth);
                //如果当前行字串宽度不占满行宽，计算当前行每个字符需要均分的宽度，即字间距。
                //float averageWidth = (lineWidth - tmpTotalWidth)/(lineEndIndx - lineBeginIndx);
                //Log.d(TAG,"avewidth = "+averageWidth);
                if(lastLineHeight + lineHeight + lineSpace > maxLineHeight){
//                    Log.d(TAG,"break1");
                    break;
                }
                else {
                    lastLineHeight = lastLineHeight + lineHeight + lineSpace;
//                    Log.d(TAG,">linewidth ;lastLineHeight ="+lastLineHeight);
                    pageEndCharIndx = i;
                    //lineNum++;
                    //由于下一行最后一个字符的字宽被舍去，重置当前累加行宽为下一行第一个字符的字宽。
                    tmpTotalWidth = tmpOneWidth[i];
                    //下一行字符串的起始字符索引。
                    lineBeginIndx = i;
                    //i恰好等于最后一个字符，需要再循环一次。
                    if(i == lenth - 1){
                        i = lenth - 2;
                    }
                }

            }
            else {
                if (i != lenth - 1 && pageStr.charAt(i) == '\n' && lineBeginIndx != i) {
                    //如果该行中有换行符，同时换行符不能为行首字符，获取当前行的字串。
                    //如果为行首字符，按照空格字宽计算。
                    lineStr.setLength(0);
                    lineStr.append(pageStr.substring(lineBeginIndx, i));
//                canvas.drawText(lineStr.toString(),TXTTOP_XSTART,TXTTOP_YSTART+lineNum*LINEHEIGHT,mPaint);
//                    Log.d(TAG,"linestr = "+lineStr.toString()+" linebeginindx = "+lineBeginIndx+" i = "+i);
                    lineBeginIndx = i;
                    //重置累加字符宽度。
                    tmpTotalWidth = tmpOneWidth[i];
                    if(lastLineHeight + lineHeight + paraSpace > maxLineHeight){
//                        Log.d(TAG,"break2");
                        break;
                    }
                    else {
                        lastLineHeight = lastLineHeight + lineHeight + paraSpace;
//                        Log.d(TAG,"huanhang lastlineheight ="+lastLineHeight);
                        pageEndCharIndx = i;
                    }
                    //lineNum++;
                }else if(i == lenth - 1){
                    //获取总字串的最后一行字符串。
                    lineStr.setLength(0);
                    lineStr.append(pageStr.substring(lineBeginIndx, lenth));
//                    Log.d(TAG, "i = lenth -1  line ="+lineStr);
                    if(lastLineHeight + lineHeight + lineSpace > maxLineHeight){
//                        Log.d(TAG,"break3");
                        break;
                    }
                    else {
                        lastLineHeight = lastLineHeight + lineHeight + lineSpace;
                        pageEndCharIndx = lenth;
//                        Log.d(TAG,"i = len -1 lastlineh ="+lastLineHeight);
                        return pageEndCharIndx;
                    }

                }
            }
            //记录下一页的起始字符索引。
//            if(lineNum >= lineNumInPage){
//                Log.d(TAG,"pageendstr = "+pageStr.charAt(i)+" linebeginindx = "+i);
//                pageEndCharIndx = i;
//                break;
//            }
//            if(lastLineHeight > getDisplayMetrics().heightPixels - lineHeight){
//                Log.d(TAG,"dm.h = "+getDisplayMetrics().heightPixels+"lastlineh = "+lastLineHeight);
//                pageEndCharIndx = i;
//                break;
//            }
        }
        //Log.d(TAG,"pageEndCharIndx ="+pageEndCharIndx);
        return pageEndCharIndx;
    }

    public static List listAvaliableStorage(Context context) {
        ArrayList<StorageInfo> storagges = new ArrayList<StorageInfo>();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod("getVolumeList", paramClasses);
            Log.d(TAG,"getVolumeList = "+getVolumeList);
            getVolumeList.setAccessible(true);
            Object[] params = {};
            Object[] invokes = (Object[]) getVolumeList.invoke(storageManager, params);
            Log.d(TAG,"invokes.len = "+invokes.length);
            if (invokes != null) {
                StorageInfo info = null;
                for (int i = 0; i < invokes.length; i++) {
                    Object obj = invokes[i];
                    Method getPath = obj.getClass().getMethod("getPath", new Class[i]);
                    String path = (String) getPath.invoke(obj, new Object[i]);
                    Log.d(TAG,"path = "+path);
                    info = new StorageInfo(path);
                    File file = new File(info.path);
                    if ((file.exists()) && (file.isDirectory()) && (file.canWrite())) {
                        Method isRemovable = obj.getClass().getMethod("isRemovable", new Class[i]);
                        String state = null;
                        try {
                            Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
                            state = (String) getVolumeState.invoke(storageManager, info.path);
                            Log.d(TAG,"state = "+state);
                            info.state = state;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (info.isMounted()) {
                            info.isRemoveable = ((Boolean) isRemovable.invoke(obj, new Object[i]));
                            Log.d(TAG,"isRemoveable = "+info.isRemoveable);
                            storagges.add(info);
                        }
                    }
                }
            }
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
        storagges.trimToSize();

        return storagges;
    }

    public static String getStoragePath(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            Log.d(TAG,"getStoragePath length = "+length);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                Log.d(TAG,"getStoragePath1 path ="+path);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    Log.d(TAG,"getStoragePath2 path ="+path);
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPrevChapterContent(){
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        int currChapterIndx = mb.getCurrChapterIndx();
        if(currChapterIndx == 0){
            return "";
        }
        else{
            String content = getChapterContent(currChapterIndx - 1,mb);
            setPageNumList(content,currChapterIndx - 1,mb,mPaint);
            return content;
        }
    }

    public static String getNextChapterContent(){
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        int currChapterIndx = mb.getCurrChapterIndx();
        if(currChapterIndx == mb.getChapterList().size() - 1){
            return "";
        }
        else{
            String content = getChapterContent(currChapterIndx + 1,mb);
            setPageNumList(content,currChapterIndx + 1,mb,mPaint);
            return content;
        }
    }


    public static void addCacheChapterIndx(int indx){
        Log.d(TAG,"addCacheChapterIndx");
        MyBook mb = BookshelfApp.getBookshelfApp().getCurrMyBook();
        Set<Integer> cacheChapterIndxs = mb.getCacheChapterIndxs();
        Integer integer;

        if (!cacheChapterIndxs.contains(indx)) {
            if (cacheChapterIndxs.size() >= 5) {
                Iterator<Integer> iterator = cacheChapterIndxs.iterator();
                if (iterator.hasNext()) {
                    integer = iterator.next();
                    Log.d(TAG, "integer = " + integer);
                    cacheChapterIndxs.remove(integer);
                    cacheChapterIndxs.add(indx);
                    mb.getChapterList().get(integer.intValue()).setContent("");
                }
            } else {
                cacheChapterIndxs.add(indx);
            }

        } else {
            Log.d(TAG, "addCacheChapterIndx contain indx=" + indx);
            //如果已缓存，将该索引放到最后。
            cacheChapterIndxs.remove(indx);
            cacheChapterIndxs.add(indx);
        }



    }
}
