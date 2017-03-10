package com.meizu.testdevVideo.util.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;


public class WifiUtil {

    public static WifiConfiguration getEAPConfig(String ssid, String usr, String pwd){
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + ssid + "\"";
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
        enterpriseConfig.setIdentity(usr);
        enterpriseConfig.setPassword(pwd);
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
        wifiConfig.enterpriseConfig = enterpriseConfig;
        return wifiConfig;
    }

    public static WifiConfiguration getWPAConfig(String ssid, String pwd){
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.preSharedKey = "\"" + pwd + "\"";
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.status = WifiConfiguration.Status.ENABLED;
        return config;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null
                    && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }

}