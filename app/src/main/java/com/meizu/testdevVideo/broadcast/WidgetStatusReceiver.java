package com.meizu.testdevVideo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.util.PublicMethod;

/**
 * Created by maxueming on 2016/12/14.
 */
public class WidgetStatusReceiver extends BroadcastReceiver{

    private SharedPreferences settingSharedPreferences = null;
    private SharedPreferences.Editor editor = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        settingSharedPreferences = ((settingSharedPreferences ==  null)?
                PreferenceManager.getDefaultSharedPreferences(context) : settingSharedPreferences);
        editor = (editor == null) ? settingSharedPreferences.edit() : editor;
        if(action.equals("st.action.widget.wakeup")){
            editor.remove(SettingPreferenceKey.KEEP_WAKEUP);
            editor.putBoolean(SettingPreferenceKey.KEEP_WAKEUP, bundle.getBoolean("isOpen"));
            editor.apply();
            // 收到亮屏通知，则强制亮屏
            if(bundle.getBoolean("isOpen")){
                PublicMethod.wakeUpAndUnlock(context);
            }
        }
    }
}
