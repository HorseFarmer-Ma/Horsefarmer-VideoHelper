package com.meizu.testdevVideo.util.sharepreference;

import android.content.Context;

import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.library.SharedPreferencesHelper;

/**
 * 推送相关保存数据
 * Created by maxueming on 2016/10/27.
 */
public class PushData {
    private static final String APP_ID = "appId";
    private static final String TASK_ID = "taskId";

    private static SharedPreferencesHelper mInstance;

    public static SharedPreferencesHelper getInstance(Context context){
        synchronized (PushData.class){
            if(mInstance == null){
                mInstance = new SharedPreferencesHelper(context, "push_data");
            }
            return mInstance;
        }
    }

    public static String getAppId(){
        return PushData.getInstance(SuperTestApplication.getContext()).readStringData(APP_ID);
    }


    public static boolean setAppId(String appId){
        return PushData.getInstance(SuperTestApplication.getContext()).writeStringData(APP_ID, appId);
    }

    public static String getTaskId(){
        return PushData.getInstance(SuperTestApplication.getContext()).readStringData(TASK_ID);
    }


    public static boolean setTaskId(String taskId){
        return PushData.getInstance(SuperTestApplication.getContext()).writeStringData(TASK_ID, taskId);
    }
}
