package com.meizu.testdevVideo.util.sharepreference;

import android.content.Context;

import com.meizu.testdevVideo.library.SharedPreferencesHelper;

/**
 * Created by maxueming on 2017/3/30.
 */
public class FailPostRecordData {

    private static SharedPreferencesHelper failPostRecord;

    public synchronized static SharedPreferencesHelper getInstance(Context context){
        if(failPostRecord == null){
            failPostRecord = new SharedPreferencesHelper(context, "fail_post_record");
        }
        return failPostRecord;
    }
}
