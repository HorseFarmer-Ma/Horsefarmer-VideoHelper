package com.meizu.testdevVideo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.service.WifiLockService;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.PrefWidgetOnOff;
import com.meizu.testdevVideo.util.wifi.WifiFunction;


/**
 * WIFI监听，开启WIFI
 * Created by maxueming on 2016/6/15.
 */
public class WifiReceiver extends BroadcastReceiver {

    public static WifiReceiver mInstance;
    private SharedPreferences settingSharedPreferences;
    private SharedPreferences.Editor editor;

    public synchronized static WifiReceiver getInstance(){
        if(mInstance == null){
            mInstance = new WifiReceiver();
        }
        return mInstance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String mAction = intent.getAction();
        // 这个监听wifi的打开与关闭，与wifi的连接无关

        if(settingSharedPreferences == null){
            settingSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            editor = settingSharedPreferences.edit();
        }

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(mAction)) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLING:     // 正在关闭wifi中
                    Log.e(WifiReceiver.class.getSimpleName(), "谁谁谁，关我WIFI");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:     // 已经关闭wifi
                    PublicMethod.lockWifi(settingSharedPreferences, context);
                    break;
                case WifiManager.WIFI_STATE_ENABLING:     // 正在开启wifi中
                    Log.e(WifiReceiver.class.getSimpleName(), "我正在开启WIFI");
                    break;
                case WifiManager.WIFI_STATE_ENABLED:     // 已经开启wifi
                    Log.e(WifiReceiver.class.getSimpleName(), "WIFI开启成功");
                    break;
            }
        }

        /**
         * 这个监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是
         * WifiManager.WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。
         * 在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，
         * 当然刚打开wifi肯定还没有连接到有效的无线
         */
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(mAction)) {
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = state == NetworkInfo.State.CONNECTED;// 当然，这边可以更精确的确定状态
                if (!isConnected) {
                    PublicMethod.lockWifi(settingSharedPreferences, context);
                }
            }
        }

        if("action.st.wifi.bind".equals(mAction)){
            Bundle mBundle = intent.getExtras();
            boolean isWifiOpen = mBundle.getBoolean("isWifiOpen");
            if(!isWifiOpen){
                writeBooleanData(SettingPreferenceKey.LOCK_WIFI, false);
                WifiFunction.getInstance(context).closeWifi();
            }else{
                WifiFunction.getInstance(context).openWifi();
                boolean isWifiLock = mBundle.getBoolean("isWifiLock");
                if(isWifiLock){
                    writeBooleanData(SettingPreferenceKey.LOCK_WIFI, true);
                    writeStringData(SettingPreferenceKey.LOCK_WIFI_TYPE, mBundle.getString("ssid").replace("\"", ""));
                    PublicMethod.lockWifi(settingSharedPreferences, context);
                }else{
                    writeBooleanData(SettingPreferenceKey.LOCK_WIFI, false);
                }
            }
        }
    }

    private boolean writeBooleanData(String key, boolean value){
        editor.remove(key);
        editor.putBoolean(key, value);
        return editor.commit();
    }

    private boolean writeStringData(String key, String value){
        editor.remove(key);
        editor.putString(key, value);
        return editor.commit();
    }
}
