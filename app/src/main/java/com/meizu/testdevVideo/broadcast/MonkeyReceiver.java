package com.meizu.testdevVideo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.PerformsMonkeyCallBack;
import com.meizu.testdevVideo.util.PublicMethod;

import java.io.IOException;


/**
 * Created by maxueming on 2016/11/18.
 */
public class MonkeyReceiver extends BroadcastReceiver {

    public static MonkeyReceiver mInstance;
    private Context mContext;
    private Bundle bundle;
    public synchronized static MonkeyReceiver getInstance(){
        if(mInstance == null){
            mInstance = new MonkeyReceiver();
        }
        return mInstance;
    }

    private static PerformsMonkeyCallBack performsMonkeyCallBack;

    public void setPerformsMonkeyListener(PerformsMonkeyCallBack performsCallBack){
        performsMonkeyCallBack = performsCallBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("MonkeyReceiver", "收到执行monkey的广播");
        if (mContext == null){
            mContext = context;
        }

        String mAction = intent.getAction();
        bundle = intent.getExtras();

        if(mAction.equals("action.st.performs.monkey.start")){
            String packageName = bundle.getString("packageName");
            if(!TextUtils.isEmpty(packageName)){
                performsMonkeyCallBack.startMonkey(packageName);
            }
        }
    }
}
