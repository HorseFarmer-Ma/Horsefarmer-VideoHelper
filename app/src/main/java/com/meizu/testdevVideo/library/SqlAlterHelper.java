package com.meizu.testdevVideo.library;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;

/**
 * Created by maxueming on 2016/9/6.
 */
public class SqlAlterHelper {

    public static SqlAlterHelper mSqlInstance;
    private DatabaseHelper mDatabaseHelper;
    public  SQLiteDatabase db;
    private Context mContext;

    public SqlAlterHelper(Context context){
        mContext = context;
        if(mDatabaseHelper == null){
            mDatabaseHelper = DatabaseHelper.getInstance(context);
            db = mDatabaseHelper.getReadableDatabase();
        }
    }

    public static synchronized SqlAlterHelper getInstance(Context context){
        if(mSqlInstance == null){
            mSqlInstance = new SqlAlterHelper(context);
        }
        return mSqlInstance;
    }

    /**
     * 插入数据
     * @param strMonkeyCommand：monkey指令
     * @param strStartTime：测试时间
     * @param bIsMute：静音
     * @param bIsWifiLock：锁定wifi
     * @param bIsFloating：悬浮按钮
     */
    public void addData(String strMonkeyType, String strMonkeyCommand, String strStartTime, boolean bIsMute, boolean bIsWifiLock, boolean bIsFloating){
        String strIsMute = String.valueOf(bIsMute);
        String strIsWifiLock = String.valueOf(bIsWifiLock);
        String strIsFloating = String.valueOf(bIsFloating);

        if(!db.isOpen()){
            db = mDatabaseHelper.getReadableDatabase();
        }
        db.execSQL("insert into MonkeyHistory(monkey_type, monkey_command, startTime, isMute, isWifiLock, isFloating) values(?,?,?,?,?,?)",
                new Object[]{strMonkeyType, strMonkeyCommand, strStartTime, strIsMute, strIsWifiLock, strIsFloating});
    }


    /**
     * 存储上传失败的性能测试文件相关信息
     */
    public void addPerformsFailUploadData(){
        if(!db.isOpen()){
            db = mDatabaseHelper.getReadableDatabase();
        }
        db.execSQL("insert into PerformsUploadFailCase("
                + iPerformsKey.deviceType + ", "
                + iPerformsKey.imei + ", "
                + iPerformsKey.testTime + ", "
                + iPerformsKey.testType + ", "
                + iPerformsKey.appType + ", "
                + iPerformsKey.appVersion + ", "
                + iPerformsKey.systemVersion + ", "
                + iPerformsKey.baseBand + ", "
                + iPerformsKey.kernel + ", "
                + iPerformsKey.stepValueFilePath
                + ") values(?,?,?,?,?,?,?,?,?,?)",
                new Object[]{PerformsData.getInstance(mContext).readStringData(iPerformsKey.deviceType),
                        PerformsData.getInstance(mContext).readStringData(iPerformsKey.imei),
                        PerformsData.getInstance(mContext).readStringData(iPerformsKey.testTime),
                        PerformsData.getInstance(mContext).readStringData(iPerformsKey.testType),
                        PerformsData.getInstance(mContext).readStringData(iPerformsKey.appType),
                        PerformsData.getInstance(mContext).readStringData(iPerformsKey.appVersion),
                        PerformsData.getInstance(mContext).readStringData(iPerformsKey.systemVersion),
                        PerformsData.getInstance(mContext).readStringData(iPerformsKey.baseBand),
                        PerformsData.getInstance(mContext).readStringData(iPerformsKey.kernel),
                        PerformsData.getInstance(mContext).readStringData(iPerformsKey.stepValueFilePath)});
    }


    /**
     * 获取查询monkey相关历史记录的表
     * @return：游标遍历 Cursor
     */
    public Cursor query(){
        //查询获得Cursor
        if(!db.isOpen()){
            db = mDatabaseHelper.getReadableDatabase();
        }
        return db.query("MonkeyHistory", null, null, null, null, null, null);
    }

    /**
     * 获取查询性能测试文件相关信息的表
     * @return：游标遍历 Cursor
     */
    public Cursor queryPerforms(){
        //查询获得Cursor
        if(!db.isOpen()){
            db = mDatabaseHelper.getReadableDatabase();
        }
        return db.query("PerformsUploadFailCase", null, null, null, null, null, null);
    }


    /**
     * 关闭数据库
     */
    public void close(){
        mDatabaseHelper.close();
    }

}
