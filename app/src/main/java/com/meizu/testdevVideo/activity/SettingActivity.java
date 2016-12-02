package com.meizu.testdevVideo.activity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.util.PublicMethod;

public class SettingActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private Toolbar mActionBar;
    private ListPreference preference_lock_wifi_type;
    private EditTextPreference preference_defined_wifi_ssid, preference_defined_wifi_psw, preference_single_log_size, preference_all_log_size;
    private SharedPreferences sharedPreferences;
    private CheckBoxPreference preference_monkey_mtk_set, preference_catch_log_type, preference_mute_run_task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
        mActionBar.setTitle("设置");
        mActionBar.setNavigationIcon(R.drawable.ic_back);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   // 设置竖屏
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        findView();
        wifiLockSetInit();
        wifiLockTypeSetInit();
        ssidInit();
        pswInit();
        singleLogSizeSetInit();
        allLogSizeSetInit();
        monkeyMtkSetInit();
        muteSettingInit();
    }

    private void findView(){
        preference_lock_wifi_type = (ListPreference)findPreference(SettingPreferenceKey.LOCK_WIFI_TYPE);
        preference_defined_wifi_ssid = (EditTextPreference)findPreference(SettingPreferenceKey.DEFINED_WIFI_SSID);
        preference_defined_wifi_psw = (EditTextPreference)findPreference(SettingPreferenceKey.DEFINED_WIFI_PSW);
        preference_single_log_size = (EditTextPreference)findPreference(SettingPreferenceKey.SINGLE_LOG_SIZE);
        preference_all_log_size = (EditTextPreference)findPreference(SettingPreferenceKey.ALL_LOG_SIZE);
        preference_monkey_mtk_set = (CheckBoxPreference)findPreference(SettingPreferenceKey.MONKEY_MTK_SET);
        preference_catch_log_type = (CheckBoxPreference)findPreference(SettingPreferenceKey.CATCH_LOG_TYPE);
        preference_mute_run_task = (CheckBoxPreference)findPreference(SettingPreferenceKey.MUTE_RUN_TASK);
    }


        @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(SettingPreferenceKey.LOCK_WIFI)) {
            wifiLockSetInit();
            PublicMethod.lockWifi(sharedPreferences, this);
        }

        if(key.equals(SettingPreferenceKey.LOCK_WIFI_TYPE)){
            wifiLockTypeSetInit();
            PublicMethod.lockWifi(sharedPreferences, this);
        }

        if(key.equals(SettingPreferenceKey.DEFINED_WIFI_SSID)){
            ssidInit();
            PublicMethod.lockWifi(sharedPreferences, this);
        }

        if(key.equals(SettingPreferenceKey.DEFINED_WIFI_PSW)){
            pswInit();
            PublicMethod.lockWifi(sharedPreferences, this);
        }

        if(key.equals(SettingPreferenceKey.MUTE)){
            PublicMethod.mute(this);
        }

        if(key.equals(SettingPreferenceKey.SINGLE_LOG_SIZE)){
            singleLogSizeSetInit();
        }

        if(key.equals(SettingPreferenceKey.ALL_LOG_SIZE)){
            allLogSizeSetInit();
        }

        if(key.equals(SettingPreferenceKey.MONKEY_MTK_SET)){
            monkeyMtkSetInit();
        }

        if(key.equals(SettingPreferenceKey.MUTE)){
            muteSettingInit();
        }

    }


    private void wifiLockSetInit(){
        if(sharedPreferences.getBoolean(SettingPreferenceKey.LOCK_WIFI, false)){
            preference_lock_wifi_type.setEnabled(true);
            if(sharedPreferences.getString(SettingPreferenceKey.LOCK_WIFI_TYPE, null).equals("自定义")){
                preference_defined_wifi_ssid.setEnabled(true);
                preference_defined_wifi_psw.setEnabled(true);
            }else{
                preference_defined_wifi_ssid.setEnabled(false);
                preference_defined_wifi_psw.setEnabled(false);
            }
        }else{
            preference_defined_wifi_ssid.setEnabled(false);
            preference_defined_wifi_psw.setEnabled(false);
            preference_lock_wifi_type.setEnabled(false);
        }
    }

    private void wifiLockTypeSetInit(){
        if(sharedPreferences.getBoolean(SettingPreferenceKey.LOCK_WIFI, true)
                && sharedPreferences.getString(SettingPreferenceKey.LOCK_WIFI_TYPE, null).equals("自定义")){
            preference_defined_wifi_ssid.setEnabled(true);
            preference_defined_wifi_psw.setEnabled(true);
        }else{
            preference_defined_wifi_ssid.setEnabled(false);
            preference_defined_wifi_psw.setEnabled(false);
        }
        preference_lock_wifi_type.setSummary(sharedPreferences.getString(SettingPreferenceKey.LOCK_WIFI_TYPE, null));
    }

    private void monkeyMtkSetInit(){
        boolean isEnable = sharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_MTK_SET, true);
        preference_single_log_size.setEnabled(isEnable);
        preference_all_log_size.setEnabled(isEnable);
        preference_catch_log_type.setEnabled(isEnable);
    }

    private void ssidInit(){
        preference_defined_wifi_ssid.setSummary(sharedPreferences.getString(SettingPreferenceKey.DEFINED_WIFI_SSID, null));
    }

    private void pswInit(){
        preference_defined_wifi_psw.setSummary(sharedPreferences.getString(SettingPreferenceKey.DEFINED_WIFI_PSW, null));
    }

    private void singleLogSizeSetInit(){
        preference_single_log_size.setSummary(sharedPreferences.getString(SettingPreferenceKey.SINGLE_LOG_SIZE, "4096") + " MB");
    }

    private void allLogSizeSetInit(){
        preference_all_log_size.setSummary(sharedPreferences.getString(SettingPreferenceKey.ALL_LOG_SIZE, "10000") + " MB");
    }

    private void muteSettingInit(){
        preference_mute_run_task.setEnabled(sharedPreferences.getBoolean(SettingPreferenceKey.MUTE, true));
    }


    @Override
    public void setContentView(int layoutResID) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.activity_settings, new LinearLayout(this), false);

        mActionBar = (Toolbar) contentView.findViewById(R.id.action_bar);
        mActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById(R.id.content_wrapper);
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);
        getWindow().setContentView(contentView);
    }
}
