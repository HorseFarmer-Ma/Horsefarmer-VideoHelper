package com.meizu.testdevVideo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.PerformsCaseCompleteCallBack;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublic;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.service.PerformsTestService;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;

/**
 * 采集FPS频率：100ms采集一次，10Hz
 * Created by maxueming on 2016/8/8.
 */
public class PerformsReceiver extends BroadcastReceiver {
    private PerformsCaseCompleteCallBack mPerformsCaseCompleteCallBack;
    public static PerformsReceiver mInstance;

    public synchronized static PerformsReceiver getInstance(){
        if(mInstance == null){
            mInstance = new PerformsReceiver();
        }
        return mInstance;
    }


    public void setOnCaseCompleteListener(PerformsCaseCompleteCallBack performsCaseCompleteCallBack){
        this.mPerformsCaseCompleteCallBack = performsCaseCompleteCallBack;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String mAction = intent.getAction();

        if(mAction.equals("action.st.performs.test.over")){
            Log.e(PerformsReceiver.class.getSimpleName(), "收到案例测试结束的广播");
            String fileName = "";
            ToastHelper.addToast("上传报告中..", context);
            Bundle mBundle = intent.getExtras();
            fileName = mBundle.getString("fileName");
            if(!TextUtils.isEmpty(fileName)){
                if(fileName.contains(".txt")){
                    PerformsData.getInstance(context).writeStringData(iPerformsKey.stepValueFileName,
                            fileName);
                }else{
                    PerformsData.getInstance(context).writeStringData(iPerformsKey.stepValueFileName,
                            fileName + ".txt");
                }
            }

            if(!TextUtils.isEmpty(mBundle.getString("pkg"))){
                PerformsData.getInstance(context).writeStringData(iPerformsKey.packageName,
                        mBundle.getString("pkg"));
            }else{
                PerformsData.getInstance(context).writeStringData(iPerformsKey.packageName,
                        "貌似脚本没填包名，或非纯净后台测试");
            }

            PerformsData.getInstance(context).writeStringData(iPerformsKey.caseName, mBundle.getString("caseName"));
            PerformsData.getInstance(context).writeStringData(iPerformsKey.result, mBundle.getString("result"));

            PublicMethod.saveStringToFileWithoutDeleteSrcFile("\n\n" + PublicMethod.getSystemTime() + "收到结束测试的广播",
                    "Performs_Log", iPublic.LOCAL_MEMORY + "SuperTest/ApkLog/");
            if(this.mPerformsCaseCompleteCallBack != null){
                this.mPerformsCaseCompleteCallBack.onCaseComplete();
            }
        }

        if(mAction.equals("action.st.kill.performs")){
            Intent performsIntent = new Intent(context, PerformsTestService.class);
            context.stopService(performsIntent);
            PublicMethod.killProcess("ps|grep uiautomator", "system    ", " ");
            PublicMethod.killProcess("ps |grep com.android.commands.monkey", "system    ", " ");
        }
    }
}
