package com.meizu.testdevVideo.task.performs;

import android.content.Context;

import com.meizu.testdevVideo.library.SharedPreferencesHelper;

/**
 * U2Task小型存储
 * Created by maxueming on 2017/6/7.
 */
public class U2TaskPreference {
    private static SharedPreferencesHelper mInstance;
    private static final String U2_TASK_STATUS = "u2TaskStatus";
    private static final String U2_TASK_LAST_UPDATE_TIME = "u2TaskLastUpdateTime";

    public synchronized static SharedPreferencesHelper getInstance(Context context){
        if(mInstance == null){
            mInstance = new SharedPreferencesHelper(context, "U2Task");
        }
        return mInstance;
    }

    public synchronized static boolean isU2TaskRunning(Context context){
        return U2TaskPreference.getInstance(context).readBooleanData(U2_TASK_STATUS);
    }

    public synchronized static void setU2TaskStatus(Context context, boolean isRunning){
        U2TaskPreference.getInstance(context).writeBooleanData(U2_TASK_STATUS, isRunning);
    }

    public synchronized static long getLastUpdateTime(Context context){
        return U2TaskPreference.getInstance(context).readLongData(U2_TASK_LAST_UPDATE_TIME);
    }

    public synchronized static void setLastUpdateTime(Context context, long lastUpdateTime){
        U2TaskPreference.getInstance(context).writeLongData(U2_TASK_LAST_UPDATE_TIME, lastUpdateTime);
    }


}
