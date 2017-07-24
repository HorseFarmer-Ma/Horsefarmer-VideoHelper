package com.meizu.testdevVideo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import com.meizu.common.preference.ListPreference;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.BaseData;

import flyme.support.v7.app.ActionBar;
import flyme.support.v7.app.AppCompatPreferenceActivity;


public class SettingActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener{

    private ListPreference preference_lock_wifi_type;
    private EditTextPreference preference_defined_wifi_ssid, preference_defined_wifi_psw,
            preference_single_log_size, preference_all_log_size, preference_clear_cache;
    private SharedPreferences sharedPreferences;
    private CheckBoxPreference preference_catch_log_type, preference_mute_run_task;
    private Preference preference_app_type_choose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(getResources().getString(R.string.setting));
        }

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
        muteSettingInit();
        appTypeInit();
        clearCacheInit();
    }


    /**
     * 处理actionbar中menu的点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void findView(){
        preference_lock_wifi_type = (ListPreference) findPreference(SettingPreferenceKey.LOCK_WIFI_TYPE);
        preference_defined_wifi_ssid = (EditTextPreference) findPreference(SettingPreferenceKey.DEFINED_WIFI_SSID);
        preference_defined_wifi_psw = (EditTextPreference) findPreference(SettingPreferenceKey.DEFINED_WIFI_PSW);
        preference_single_log_size = (EditTextPreference) findPreference(SettingPreferenceKey.SINGLE_LOG_SIZE);
        preference_all_log_size = (EditTextPreference) findPreference(SettingPreferenceKey.ALL_LOG_SIZE);
        preference_clear_cache = (EditTextPreference) findPreference(SettingPreferenceKey.CLEAR_CACHE);
        preference_catch_log_type = (CheckBoxPreference) findPreference(SettingPreferenceKey.CATCH_LOG_TYPE);
        preference_mute_run_task = (CheckBoxPreference) findPreference(SettingPreferenceKey.MUTE_RUN_TASK);
        preference_app_type_choose = findPreference(SettingPreferenceKey.APP_TYPE);
        preference_app_type_choose.setOnPreferenceClickListener(this);
    }


        @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(R.anim.mz_activity_extra_to_next_open_enter, R.anim.mz_activity_extra_to_next_close_exit);
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
        }else if(key.equals(SettingPreferenceKey.LOCK_WIFI_TYPE)){
            wifiLockTypeSetInit();
            PublicMethod.lockWifi(sharedPreferences, this);
        }else if(key.equals(SettingPreferenceKey.DEFINED_WIFI_SSID)){
            ssidInit();
            PublicMethod.lockWifi(sharedPreferences, this);
        }else if(key.equals(SettingPreferenceKey.DEFINED_WIFI_PSW)){
            PublicMethod.lockWifi(sharedPreferences, this);
        }else if(key.equals(SettingPreferenceKey.MUTE)){
            PublicMethod.mute(this);
        }else if(key.equals(SettingPreferenceKey.SINGLE_LOG_SIZE)){
            singleLogSizeSetInit();
        }else if(key.equals(SettingPreferenceKey.ALL_LOG_SIZE)){
            allLogSizeSetInit();
        }else if(key.equals(SettingPreferenceKey.CLEAR_CACHE)){
            clearCacheInit();
        }else if(key.equals(SettingPreferenceKey.MUTE)){
            muteSettingInit();
        }else if(key.equals(SettingPreferenceKey.NOTIFITION_SWITCH)){
            Intent intent = new Intent();
            intent.setAction("st.action.control.notification");
            if(sharedPreferences.getBoolean(SettingPreferenceKey.NOTIFITION_SWITCH, true)){
                intent.putExtra("isOpen", true);
            }else{
                intent.putExtra("isOpen", false);
            }
            sendBroadcast(intent);
        }
        else{
            Log.d(SettingActivity.class.getSimpleName(), "未知点击项");
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals(SettingPreferenceKey.APP_TYPE)){
            Log.d("onPreferenceClick----->", "preference_app_type_choose");
            Intent appChooseIntent = new Intent(this, AppChooseActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("title", getResources().getString(R.string.choose_app_type));
            appChooseIntent.putExtras(bundle);
            startActivityForResult(appChooseIntent, 0);
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 取出字符串
        if (data != null){
            Bundle bundle = data.getExtras();
            BaseData.getInstance(getApplicationContext()).writeStringData(SettingPreferenceKey.APP_TYPE,
                    bundle.getString(SettingPreferenceKey.APP_TYPE));
            BaseData.getInstance(getApplicationContext()).writeStringData(SettingPreferenceKey.EMAIL_ADDRESS,
                    bundle.getString(SettingPreferenceKey.EMAIL_ADDRESS));
            BaseData.getInstance(getApplicationContext()).writeStringData(SettingPreferenceKey.MONKEY_PACKAGE,
                    bundle.getString(SettingPreferenceKey.MONKEY_PACKAGE));
            appTypeInit();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void wifiLockSetInit(){
        if(sharedPreferences.getBoolean(SettingPreferenceKey.LOCK_WIFI, false)){
            preference_lock_wifi_type.setEnabled(true);
            String lockWifiType = sharedPreferences.getString(SettingPreferenceKey.LOCK_WIFI_TYPE, null);
            if(null != lockWifiType && lockWifiType.equals("自定义")){
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
        String lockWifiType = sharedPreferences.getString(SettingPreferenceKey.LOCK_WIFI_TYPE, null);
        if(sharedPreferences.getBoolean(SettingPreferenceKey.LOCK_WIFI, false)
                && null != lockWifiType && lockWifiType.equals("自定义")){
            preference_defined_wifi_ssid.setEnabled(true);
            preference_defined_wifi_psw.setEnabled(true);
        }else{
            preference_defined_wifi_ssid.setEnabled(false);
            preference_defined_wifi_psw.setEnabled(false);
        }
        preference_lock_wifi_type.setSummary(sharedPreferences.getString(SettingPreferenceKey.LOCK_WIFI_TYPE, null));
    }


    private void ssidInit(){
        preference_defined_wifi_ssid.setSummary(sharedPreferences.getString(SettingPreferenceKey.DEFINED_WIFI_SSID, null));
    }

    private void pswInit(){
        preference_defined_wifi_psw.setSummary("·········");
    }

    private void singleLogSizeSetInit(){
        preference_single_log_size.setSummary(sharedPreferences.getString(SettingPreferenceKey.SINGLE_LOG_SIZE, "4096") + " MB");
    }

    private void allLogSizeSetInit(){
        preference_all_log_size.setSummary(sharedPreferences.getString(SettingPreferenceKey.ALL_LOG_SIZE, "10000") + " MB");
    }

    private void clearCacheInit(){
        String last_clear_time = BaseData.getInstance(getApplicationContext())
                .readStringData(SettingPreferenceKey.LSAT_CLEAR_CACHE_TIME);
        if(null != last_clear_time){
            preference_clear_cache.setTitle("清除应用缓存周期/" + sharedPreferences
                    .getString(SettingPreferenceKey.CLEAR_CACHE, getString(R.string.clear_cache_orign_times)) + "天");
            preference_clear_cache.setSummary("应用日志缓存相关，抓取的log和trace/drodbox文件缓存"
                    + "\n上次清理时间: " + PublicMethod.dateFormatTimes(Long.parseLong(last_clear_time)));
        }else{
            preference_clear_cache.setTitle("清除应用缓存周期/" + sharedPreferences
                    .getString(SettingPreferenceKey.CLEAR_CACHE, getString(R.string.clear_cache_orign_times)) + "天");
            preference_clear_cache.setSummary("应用日志缓存相关，抓取的log和trace/drodbox文件缓存");
        }
    }

    private void muteSettingInit(){
        preference_mute_run_task.setEnabled(sharedPreferences.getBoolean(SettingPreferenceKey.MUTE, true));
    }

    private void appTypeInit(){
        String appType = BaseData.getInstance(getApplicationContext()).readStringData(SettingPreferenceKey.APP_TYPE);
        String email = BaseData.getInstance(getApplicationContext()).readStringData(SettingPreferenceKey.EMAIL_ADDRESS);
        if(null != appType){
            preference_app_type_choose.setSummary(appType + " ~ " + email);
        }
    }

}
