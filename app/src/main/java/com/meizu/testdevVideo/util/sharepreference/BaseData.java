package com.meizu.testdevVideo.util.sharepreference;

import android.content.Context;

import com.meizu.testdevVideo.library.SharedPreferencesHelper;

/**
 * 基本存储数据；wifi等
 * Created by maxueming on 2016/10/27.
 */
public class BaseData {

    private static SharedPreferencesHelper baseDataInstance;

    public synchronized static SharedPreferencesHelper getInstance(Context context){
        if(baseDataInstance == null){
            baseDataInstance = new SharedPreferencesHelper(context, "base_data");
        }
        return baseDataInstance;
    }
}
