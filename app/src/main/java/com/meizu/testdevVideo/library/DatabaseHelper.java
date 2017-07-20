package com.meizu.testdevVideo.library;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.Constants.Monkey;
import com.meizu.testdevVideo.db.entity.MonkeyHistoryParam;
import com.meizu.testdevVideo.interports.iPerformsKey;

/**
 * 数据库建立维护协助函数
 * Created by maxueming on 2016/7/19.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    private static DatabaseHelper mInstance = null;
    private static final String DB_NAME = "SuperTest.db";
    private static final int DB_VERSION = 3;

    public static synchronized DatabaseHelper getInstance(Context context){
        if(mInstance == null){
            mInstance = new DatabaseHelper(context);
        }
        return mInstance;
    }

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Monkey.TABLE_NAME + " (id integer primary key autoincrement, "
                + MonkeyHistoryParam.MONKEY_TYPE + " varchar(50), " + MonkeyHistoryParam.MONKEY_COMMAND + " varchar(300), "
                + MonkeyHistoryParam.START_TIME + " varchar(50), " + MonkeyHistoryParam.IS_MUTE + " varchar(4), "
                + MonkeyHistoryParam.IS_WIFI_LOCK + " varchar(4), "
                + MonkeyHistoryParam.IS_FLOATING + " varchar(4))");

        db.execSQL("CREATE TABLE IF NOT EXISTS "+ iPerformsKey.TABLE_NAME
                + " (id integer primary key autoincrement, "
                + iPerformsKey.deviceType + " varchar(20), "
                + iPerformsKey.imei + " varchar(20), "
                + iPerformsKey.testTime + " varchar(20), "
                + iPerformsKey.testType + " varchar(20), "
                + iPerformsKey.appType + " varchar(40), "
                + iPerformsKey.appVersion + " varchar(20), "
                + iPerformsKey.systemVersion + " varchar(50), "
                + iPerformsKey.baseBand + " varchar(100), "
                + iPerformsKey.kernel + " varchar(200), "
                + iPerformsKey.stepValueFilePath + " varchar(100))");

        createU2TaskTable(db);
    }

    private void createU2TaskTable(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ Constants.U2TaskDB.TABLE_NAME
                + " (id integer primary key autoincrement, "
                + Constants.U2TaskDB.TASK_ID_STR + " TEXT,"
                + Constants.U2TaskDB.PERFORMS_TYPE_INT + " int,"
                + Constants.U2TaskDB.CASE_NAME_STR + " TEXT,"
                + Constants.U2TaskDB.CASE_STEP_STR + " varchar(200),"
                + Constants.U2TaskDB.STATUS_INT  + " int,"
                + Constants.U2TaskDB.RESULT_BOOLEAN  + " boolean,"
                + Constants.U2TaskDB.RESULT_FILE_STR  + " TEXT,"
                + Constants.U2TaskDB.EXCEPTION_STR  + " TEXT"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 1) {
            SuperTestDbUpgradeToV2(db);
        }

        if(oldVersion <= 2){
            SuperTestDbUpgradeToV3(db);
        }

    }

    /**
     * 将数据库从 1 --> 2 增加两个字段
     */
    private void SuperTestDbUpgradeToV2(SQLiteDatabase db){
        db.execSQL("alter table " + Monkey.TABLE_NAME + " add column " + Monkey.MONKEY_LOG_ADDRESS + " varchar(100)");// 增加一个字段
        db.execSQL("alter table " + Monkey.TABLE_NAME + " add column " + Monkey.MONKEY_LOG_RESULT_ADDRESS + " varchar(100)");// 增加一个字段
    }

    /**
     * 将数据库从 2 --> 3 增加一个表
     */
    private void SuperTestDbUpgradeToV3(SQLiteDatabase db){
        createU2TaskTable(db);
    }



    @Override
    public synchronized void close() {
        SQLiteDatabase db = mInstance.getReadableDatabase();
        if (null != db && db.isOpen()) {
            db.close();
        }
        super.close();
    }
}
