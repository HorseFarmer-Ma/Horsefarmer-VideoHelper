package com.meizu.testdevVideo.util.sharepreference;

import android.content.Context;

import com.meizu.testdevVideo.library.SharedPreferencesHelper;

/**
 * 性能测试数据存储
 * Created by maxueming on 2016/10/25.
 */
public class PerformsData {

    private static SharedPreferencesHelper performsDataInstance;

    public synchronized static SharedPreferencesHelper getInstance(Context context){
        if(performsDataInstance == null){
            performsDataInstance = new SharedPreferencesHelper(context, "performs_data");
        }
        return performsDataInstance;
    }
}
