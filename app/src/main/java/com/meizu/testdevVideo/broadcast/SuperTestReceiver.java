package com.meizu.testdevVideo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.service.SuperTestService;
import com.meizu.testdevVideo.util.PublicMethod;

/**
 * Created by mxm on 2016/8/30.
 */
public class SuperTestReceiver extends BroadcastReceiver {
    private static String TAG = SuperTestReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "触发SuperTestService后台监听器！!");
        if(!PublicMethod.isServiceWorked(context, "com.meizu.testdevVideo.service.SuperTestService")){
            PublicMethod.saveStringToFileWithoutDeleteSrcFile(PublicMethod.getSystemTime()
                    + "服务被销毁了，触发监听器，启动服务\n",
                    "SuperTestServiceLog", iPublicConstants.LOCAL_MEMORY + "SuperTest/ApkLog/");
            Intent service=new Intent(context, SuperTestService.class);
            //启动服务
            context.startService(service);
            Log.d(TAG, "别睡了老大，请重新启动服务！！!");
        }
    }
}
