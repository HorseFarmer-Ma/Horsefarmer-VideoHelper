package com.meizu.testdevVideo.util.sharepreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.meizu.testdevVideo.constant.SettingPreferenceKey;

/**
 * 设置相关
 * Created by maxueming on 2017/5/16.
 */
public class SettingPreference {
    private static SettingPreference settingPreference;
    private SharedPreferences settingSharedPreferences = null;
    private SharedPreferences.Editor editor = null;
    public SettingPreference(Context context){
        settingSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settingSharedPreferences.edit();
    }

    public synchronized static SettingPreference getInstance(Context context){
        if(settingPreference == null){
            settingPreference = new SettingPreference(context);
        }
        return settingPreference;
    }

    public boolean setWakeUp(boolean isWakeUp){
        editor.remove(SettingPreferenceKey.KEEP_WAKEUP);
        editor.putBoolean(SettingPreferenceKey.KEEP_WAKEUP, isWakeUp);
        return editor.commit();
    }

    public boolean isWakeUp(){
        return settingSharedPreferences.getBoolean(SettingPreferenceKey.KEEP_WAKEUP, false);
    }

    public boolean setWifiLock(boolean isWifiLock){
        editor.remove(SettingPreferenceKey.LOCK_WIFI);
        editor.putBoolean(SettingPreferenceKey.LOCK_WIFI, isWifiLock);
        return editor.commit();
    }

    public boolean setWifiLockType(String wifiName){
        editor.remove(SettingPreferenceKey.LOCK_WIFI_TYPE);
        editor.putString(SettingPreferenceKey.LOCK_WIFI_TYPE, wifiName);
        return editor.commit();
    }

    public String getWifiLockType(){
        return settingSharedPreferences.getString(SettingPreferenceKey.LOCK_WIFI_TYPE, "MZ-Inweb-Test");
    }

    public boolean getNotifition(){
        return settingSharedPreferences.getBoolean(SettingPreferenceKey.NOTIFITION_SWITCH, false);
    }

    public boolean isWifiLock(){
        return settingSharedPreferences.getBoolean(SettingPreferenceKey.LOCK_WIFI, false);
    }

    public SharedPreferences getSettingSharedPreferences(){
        return settingSharedPreferences;
    }

}
