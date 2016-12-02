package com.meizu.testdevVideo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.util.PublicMethod;

/**
 * Created by maxueming on 2016/12/1.
 */
public class ScreenWakeUpReceiver extends BroadcastReceiver{
    public static ScreenWakeUpReceiver mInstance;

    public ScreenWakeUpReceiver(){}

    public synchronized static ScreenWakeUpReceiver getInstance(){
        if(mInstance == null){
            mInstance = new ScreenWakeUpReceiver();
        }
        return mInstance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String mAction = intent.getAction();
        Log.d("ScreenWakeUpReceiver", "收到灭屏广播");
        if (mAction.equals(Intent.ACTION_SCREEN_OFF)){
            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingPreferenceKey.KEEP_WAKEUP, false)){
                Log.d("ScreenWakeUpReceiver", "屏幕灭屏，自动唤醒");
                PublicMethod.wakeUpAndUnlock(context);
            }
        }
    }
}
