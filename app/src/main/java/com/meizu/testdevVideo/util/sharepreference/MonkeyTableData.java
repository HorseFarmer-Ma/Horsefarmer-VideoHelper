package com.meizu.testdevVideo.util.sharepreference;

import android.content.Context;

import com.meizu.testdevVideo.library.SharedPreferencesHelper;

/**
 * Created by maxueming on 2016/11/16.
 */
public class MonkeyTableData {
    private static SharedPreferencesHelper monkeyTableData;

    public synchronized static SharedPreferencesHelper getInstance(Context context){
        if(monkeyTableData == null){
            monkeyTableData = new SharedPreferencesHelper(context, "monkey_table");
        }
        return monkeyTableData;
    }
}
