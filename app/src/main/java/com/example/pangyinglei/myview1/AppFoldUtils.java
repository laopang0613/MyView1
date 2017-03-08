package com.example.pangyinglei.myview1;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by pangyinglei on 2017/2/16.
 */

public class AppFoldUtils {

    private static final String TAG = "AppFoldUtils";

    private static final String APPNAME = "PPReader";

    public static String getSDCardPath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    }

    public static void createOneFold(String name){
        File file = new File(getSDCardPath()+APPNAME);
        Log.d(TAG,"file.path ="+file.getAbsolutePath());
        if(!file.exists()){
            file.mkdir();
            File sFile = new File(getSDCardPath()+APPNAME+"/"+name);
            Log.d(TAG,"sFile.path ="+sFile.getAbsolutePath());
            if(!sFile.exists()){
                sFile.mkdir();
            }
        }
        else{
            File sFile = new File(getSDCardPath()+APPNAME+"/"+name);
            if(!sFile.exists()){
                sFile.mkdir();
            }
        }
    }

    public static String getFoldPath(String name){
        return getSDCardPath()+APPNAME+"/"+name;
    }

}
