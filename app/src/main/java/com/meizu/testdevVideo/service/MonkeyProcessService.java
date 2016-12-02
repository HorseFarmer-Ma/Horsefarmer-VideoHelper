package com.meizu.testdevVideo.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.iPublic;
import com.meizu.testdevVideo.util.PublicMethod;

import java.io.IOException;

public class MonkeyProcessService extends Service {
    public static final String packageName = "packageName";
    private String pkgName = "";
    private String TAG = MonkeyProcessService.class.getSimpleName();

    public MonkeyProcessService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pkgName = intent.getStringExtra(packageName);
        Log.e(TAG, "启动Monkey服务");
        Log.e(TAG, "PackageName = " + pkgName);
        runMonkeyPerforms(pkgName);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 性能测试Monkey
     */
    private void runMonkeyPerforms(final String packageName){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                PublicMethod.saveStringToFileWithoutDeleteSrcFile(PublicMethod.getSystemTime() + "执行性能测试Monkey指令\n",
                        "Performs_Log", iPublic.LOCAL_MEMORY + "SuperTest/ApkLog/");
                try {
                    Runtime.getRuntime().exec(CommonVariable.performsMonkeyCommand.replace("%s", packageName));
                } catch (IOException e){
                    e.printStackTrace();
                    PublicMethod.saveStringToFileWithoutDeleteSrcFile(PublicMethod.getSystemTime() + "哎呀,性能测试Monkey意外出错\n",
                            "Performs_Log", iPublic.LOCAL_MEMORY + "SuperTest/ApkLog/");
                }

                return null;
            }
        }.execute();
    }
}
