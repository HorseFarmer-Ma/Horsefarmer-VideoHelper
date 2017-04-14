package com.meizu.testdevVideo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.meizu.testdevVideo.interports.InstallCallBack;

/**
 * Created by maxueming on 2017/3/14.
 */
public class BootReceiver extends BroadcastReceiver{
    private InstallCallBack installCallBack;

    public void setOnInstallListener(InstallCallBack callBack){
        installCallBack = callBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //接收安装广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            installCallBack.installOrUninstall(true, packageName);
        }

        //接收卸载广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            installCallBack.installOrUninstall(false, packageName);
        }
    }
}
