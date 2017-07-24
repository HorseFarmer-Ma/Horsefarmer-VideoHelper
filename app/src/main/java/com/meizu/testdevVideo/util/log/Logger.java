package com.meizu.testdevVideo.util.log;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志输出辅助类
 * Created by maxueming on 2017/6/1.
 */
public class Logger {

    private static String TAG = "SuperTest";
    private static String LOG_PATH = Environment.getExternalStorageDirectory().toString() + "/SuperTest/ApkLog/";

    // Log文件名
    public static String SUPER_TEST = "SuperTest";
    public static String MONKEY_SERVICE = "MonkeyService";
    public static String MPUSH_TASK_SERVICE = "MPushTaskService";
    public static String MPUSH = "MPush";
    public static String PERFORMS_SERVICE = "PerformsService";
    public static String U2TASK = "U2Task";

    public static void d(String msg){
        Log.d(TAG, msg);
    }

    public static void e(String msg){
        Log.e(TAG, msg);
    }

    public static void i(String msg){
        Log.i(TAG, msg);
    }

    public static void v(String msg){
        Log.v(TAG, msg);
    }

    public static void w(String msg){
        Log.w(TAG, msg);
    }

    public static void file(String msg, String fileName){
        saveStringToFileWithoutDeleteSrcFile(getSystemTime() + msg + "\n",
                fileName.contains(".txt")? fileName : fileName + ".txt", LOG_PATH);
    }

    private static void saveStringToFileWithoutDeleteSrcFile(String toSaveString, String fileName, String filePath) {
        try{
            File FileDir = new File(filePath);
            if (!FileDir.exists()) {
                FileDir.mkdirs();
            }
            FileWriter out;
            if(fileName.contains(".txt")){
                out = new FileWriter(filePath + fileName, true);
            }else{
                out = new FileWriter(filePath + fileName + ".txt", true);
            }

            out.write(toSaveString);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSystemTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   // 格式化当前时间
        String time = dateFormat.format(new Date());    // 获取当前时间
        return time + "   ";
    }
}
