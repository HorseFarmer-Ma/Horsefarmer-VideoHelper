package com.meizu.testdevVideo.push.android.bean;

import android.content.Context;

import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.library.SharedPreferencesHelper;

/**
 * 小型存储MPush值
 * Created by maxueming on 2017/5/27.
 */
public class MPushBindData {
    private static SharedPreferencesHelper mPushBindData;

    private synchronized static SharedPreferencesHelper getMPushBindData(Context context){
        if(mPushBindData == null){
            mPushBindData = new SharedPreferencesHelper(context, "mPushBindData");
        }
        return mPushBindData;
    }

    // 获取绑定状态
    public synchronized static int getBindStatus(Context context){
        return MPushBindData.getMPushBindData(context).readIntData(Constants.MpushBindUser.BIND_STATUS);
    }

    // 设置绑定状态
    public synchronized static void setBindStatus(Context context, int status){
        MPushBindData.getMPushBindData(context).writeIntData(Constants.MpushBindUser.BIND_STATUS, status);
    }

}
