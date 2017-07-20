package com.meizu.testdevVideo.push.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.AppInfoHelper;
import com.meizu.testdevVideo.library.ServiceNotificationHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.push.android.MPush;
import com.meizu.testdevVideo.push.android.MPushService;
import com.meizu.testdevVideo.push.android.Notifications;
import com.meizu.testdevVideo.push.android.bean.MPushBindData;
import com.meizu.testdevVideo.push.android.bean.MPushBindUserBean;
import com.meizu.testdevVideo.service.U2AutoTestService;
import com.meizu.testdevVideo.task.monkey.SilenceAppMonkeyInfo;
import com.meizu.testdevVideo.task.performs.U2TaskPreference;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.download.DownloadHelper;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PushReceiver extends BroadcastReceiver {
    private MPushBindUserBean mPushBindUserBean;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (MPushService.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            String content = null;
            JSONObject messageDO = null;
            byte[] bytes = intent.getByteArrayExtra(MPushService.EXTRA_PUSH_MESSAGE);
            int messageId = intent.getIntExtra(MPushService.EXTRA_PUSH_MESSAGE_ID, 0);
            String message = new String(bytes, Constants.UTF_8);
            if (messageId > 0) MPush.I.ack(messageId);
            if (TextUtils.isEmpty(message)) return;

            try {
                messageDO = new JSONObject(message);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            try {
                content = new JSONObject(messageDO.optString("content")).optString("content");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            try {
                final JSONObject contentJson = new JSONObject(content);
                String task = contentJson.optString("task");

                if(null != content && task.equals(Constants.MpushTaskLabel.START_PERFORMS_TEST)){

                    if(contentJson.getString("type").equals(Constants.U2TaskConstants.U2_TASK_TYPE_OF_GET_ID)){
                        U2TaskPreference.getInstance(SuperTestApplication.getContext()).writeStringData(
                                Constants.U2TaskConstants.U2_TASK_TASKID, contentJson
                                        .getString(Constants.U2TaskConstants.U2_TASK_TASKID));
                    }else if(contentJson.getString("type").equals(Constants.U2TaskConstants.U2_TASK_TYPE_OF_U2_TASK)){

                        if(!TextUtils.isEmpty(BaseData.getInstance(SuperTestApplication.getContext())
                                .readStringData(SettingPreferenceKey.MONKEY_PACKAGE))){
                            if(!PublicMethod.isServiceWorked(SuperTestApplication.getContext(),
                                    "com.meizu.testdevVideo.service.U2AutoTestService")){
                                Intent taskStartIntent = new Intent(context, U2AutoTestService.class);
                                context.startService(taskStartIntent);
                            }

                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    String taskJson = null;
                                    int taskId = 0;
                                    try {
                                        taskJson = contentJson.getString("taskJson");
                                        taskId = contentJson.getInt("taskId");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    Intent taskIntent = new Intent(Constants.U2TaskConstants.U2_TASK_BROADCAST_ACTION);
                                    taskIntent.putExtra(Constants.U2TaskConstants.U2_TASK_TASKJSON, taskJson);
                                    taskIntent.putExtra(Constants.U2TaskConstants.U2_TASK_TASKID, taskId);
                                    context.sendBroadcast(taskIntent);
                                }
                            }, 5 * 1000);
                        }else{
                            ServiceNotificationHelper.getInstance(context)
                                    .notificationCanCancel("U2Task调度", "尚未在APK中选择业务类型，故不给跑", 1);
                        }
                    }
                }else if(null != content && task.equals(com.meizu
                        .testdevVideo.constant.Constants.MpushTaskLabel.STOP_PERFORMS_TEST)){
                    if(PublicMethod.isServiceWorked(SuperTestApplication.getContext(),
                            "com.meizu.testdevVideo.service.U2AutoTestService")){
                        Intent killU2TaskIntent = new Intent(Constants.U2TaskConstants.U2_TASK_STOP_TASK);
                        context.sendBroadcast(killU2TaskIntent);
                    }
                    ToastHelper.addToast("云端kill UiAutomator", context);

                }else if(null != content && task.equals(Constants.MpushTaskLabel.CHECK_PHONE_STATU)){
                    MPushBindUserBean mPushBindUserBean = new MPushBindUserBean();
                    if(PublicMethod.isServiceWorked(context, "com.meizu.testdevVideo.service.MonkeyService")){
                        mPushBindUserBean.setStatus("busy");
                        mPushBindUserBean.setReason("Monkey task running");
                    }else if(U2TaskPreference.isU2TaskRunning(context)){
                        mPushBindUserBean.setStatus("busy");
                        mPushBindUserBean.setReason("U2task task running");
                    } else{
                        mPushBindUserBean.setStatus("free");
                        mPushBindUserBean.setReason("null");
                    }

                    MPush.I.sendPush(JSON.toJSONString(mPushBindUserBean).getBytes(Constants.UTF_8));

                }else if(null != content && task.equals(com.meizu
                        .testdevVideo.constant.Constants.MpushTaskLabel.CHECK_PHONE_IMEI)){
                    // 上报MPush业务信息
                    String type = new JSONObject(content).optString("type");
                    if("2".equals(type)){
                        if(contentJson.optString("status").equals("0")){
                            MPushBindData.setBindStatus(context, Constants.MpushBindUser.PASS);
                        }else if(contentJson.optString("status").equals("-1")){
                            Log.d("Mpush", "邮箱信息错误");
                            MPushBindData.setBindStatus(context, Constants.MpushBindUser.FAIL);
                        }else{
                            MPushBindData.setBindStatus(context, Constants.MpushBindUser.FAIL);
                        }
                    }else{
                        mPushBindUserBean = (null == mPushBindUserBean)? new MPushBindUserBean() : mPushBindUserBean;
                        mPushBindUserBean.clear();
                        mPushBindUserBean.setTask(Constants.MpushTaskLabel.CHECK_PHONE_IMEI);
                        mPushBindUserBean.setVersion(AppInfoHelper.getInstance().getAppVersion(context.getPackageName()));
                        mPushBindUserBean.setImei(PerformsData.getInstance(context).readStringData(iPerformsKey.imei));
                        mPushBindUserBean.setModule(PerformsData.getInstance(context).readStringData(iPerformsKey.deviceType));
                        MPush.I.sendPush(JSON.toJSONString(mPushBindUserBean).getBytes(Constants.UTF_8));
                    }
                }else if(null != content && task.equals(com.meizu
                        .testdevVideo.constant.Constants.MpushTaskLabel.POST_MONKEY_TASK_AND_KILL_TASK)){
                    // 停止monkey
                    try {
                        Runtime.getRuntime().exec("am broadcast -a action.st.kill.monkey");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ToastHelper.addToast("云端kill Monkey", context);
                }else if(null != content && task.equals(com.meizu
                        .testdevVideo.constant.Constants.MpushTaskLabel.GET_MONKEY_TASK_ID)){
                    String monkey_id = new JSONObject(content).optString("task_id");
                    MonkeyTableData.setMonkeyId(context, monkey_id);
                    PublicMethod.saveLog("Mpush", "收到服务端monkeyId, MonkeyId为: " + monkey_id);
                }else if(null != content && task.equals(com.meizu
                        .testdevVideo.constant.Constants.MpushTaskLabel.INSTALL_APP_THEN_RUN_MONKEY)){
                    if(!TextUtils.isEmpty(BaseData.getInstance(SuperTestApplication.getContext())
                            .readStringData(SettingPreferenceKey.MONKEY_PACKAGE))){
                        SilenceAppMonkeyInfo.getInstance().setMonkeyType(contentJson.optString("monkey_type"));
                        String apkAddress = contentJson.optString("update_apk_address");
                        SilenceAppMonkeyInfo.getInstance().setUpdateApkAddress(apkAddress);
                        if(!TextUtils.isEmpty(apkAddress)){
                            ServiceNotificationHelper.getInstance(context)
                                    .notificationCanCancel("升级APK版本号",
                                            apkAddress.split("/")[apkAddress.split("/").length - 1].toString(), 2);
                        }

                        SilenceAppMonkeyInfo.getInstance().setRuntime(contentJson.optString("runtime"));

                        if(SilenceAppMonkeyInfo.getInstance().getMonkeyType().equals(com.meizu
                                .testdevVideo.constant.Constants.Monkey.SINGLE_MONKEY)){
                            SilenceAppMonkeyInfo.getInstance().setApp(contentJson.getString("app").replace(",", " -p "));
                            SilenceAppMonkeyInfo.getInstance().setSeed(contentJson.getString("seed"));
                            SilenceAppMonkeyInfo.getInstance().setTimes(contentJson.getString("times"));
                            SilenceAppMonkeyInfo.getInstance().setNumber(contentJson.getString("number"));
                        }else if(SilenceAppMonkeyInfo.getInstance().getMonkeyType().equals(com.meizu
                                .testdevVideo.constant.Constants.Monkey.SYSTEM_MONKEY)){
                            SilenceAppMonkeyInfo.getInstance().setPkgBlacklist(contentJson.getString("pkg_blacklist"));
                        }

                        PublicMethod.deleteFile(iPublicConstants.LOCAL_MEMORY + "SuperTest/SilenceTask/" + "SilenceTask.apk");
                        if(contentJson.getString("update_type").equals("1")){
                            SilenceAppMonkeyInfo.getInstance().setUpdateApkId(DownloadHelper
                                    .getInstance(context).download(SilenceAppMonkeyInfo.getInstance()
                                            .getUpdateApkAddress(), "/SuperTest/SilenceTask/", "SilenceTask.apk"));
                            ServiceNotificationHelper.getInstance(context)
                                    .notificationCanCancel("Monkey调度", "下载APK中..", 1);
                        }else{
                            Intent monkeyIntent = new Intent(Constants.Monkey.ACTION_SILENCE_INSTALL_APK);
                            intent.putExtra("filePath", "");
                            context.sendBroadcast(monkeyIntent);
                        }
                    }else{
                        ServiceNotificationHelper.getInstance(context)
                                .notificationCanCancel("Monkey调度", "尚未在APK中选择业务类型，故不给跑", 1);
                    }
                }else{
                    NotificationDO ndo = fromJson(message);
                    if (ndo != null) {
                        Intent it = new Intent(context, PushReceiver.class);
                        it.setAction(MPushService.ACTION_NOTIFICATION_OPENED);
                        if (ndo.getExtras() != null) it.putExtra("my_extra", ndo.getExtras().toString());
                        if (TextUtils.isEmpty(ndo.getTitle())) ndo.setTitle("SuperTest");
                        if (TextUtils.isEmpty(ndo.getTicker())) ndo.setTicker(ndo.getTitle());
                        if (TextUtils.isEmpty(ndo.getContent())) ndo.setContent(ndo.getTitle());
                        Notifications.I.notify(ndo, it);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (MPushService.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Notifications.I.clean(intent);
            String extras = intent.getStringExtra("my_extra");
//            Toast.makeText(context, "通知被点击了， extras=" + extras, Toast.LENGTH_SHORT).show();
        } else if (MPushService.ACTION_KICK_USER.equals(intent.getAction())) {
            Logger.file("用户被踢下线了", Logger.MPUSH);
        } else if (MPushService.ACTION_BIND_USER.equals(intent.getAction())) {
//                    , Toast.LENGTH_SHORT).show();
            Logger.file("绑定用户:" + intent.getStringExtra(MPushService.EXTRA_USER_ID)
                            + (intent.getBooleanExtra(MPushService.EXTRA_BIND_RET, false) ? "成功" : "失败"), Logger.MPUSH);
        } else if (MPushService.ACTION_UNBIND_USER.equals(intent.getAction())) {
            Logger.file("解绑用户:" + (intent.getBooleanExtra(MPushService.EXTRA_BIND_RET, false) ? "成功" : "失败"), Logger.MPUSH);
        } else if (MPushService.ACTION_CONNECTIVITY_CHANGE.equals(intent.getAction())) {

            boolean isBind = intent.getBooleanExtra(MPushService.EXTRA_CONNECT_STATE, false);
//            Toast.makeText(context, isBind ? "MPUSH连接建立成功" : "MPUSH连接断开"
//                    , Toast.LENGTH_SHORT).show();
            // 必加,修复断网后掉线问题
            Logger.d("重新连接MPush ==》" + (isBind? "成功": "失败"));
            if(isBind){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String userId = PerformsData.getInstance(SuperTestApplication.getContext()).readStringData(iPerformsKey.imei);
                            String tags = PerformsData.getInstance(SuperTestApplication.getContext()).readStringData(iPerformsKey.deviceType);
                            MPush.I.checkInit(SuperTestApplication.getContext()).unbindAccount();
                            Thread.sleep(2 * 1000);
                            MPush.I.checkInit(SuperTestApplication.getContext()).bindAccount(userId, tags);
                            MPush.I.checkInit(SuperTestApplication.getContext()).startPush();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        } else if (MPushService.ACTION_HANDSHAKE_OK.equals(intent.getAction())) {
//            Toast.makeText(context, "MPUSH握手成功, 心跳:" + intent.getIntExtra(MPushService.EXTRA_HEARTBEAT, 0)
//                    , Toast.LENGTH_SHORT).show();
        }
    }

    private NotificationDO fromJson(String message) {
        try {
            JSONObject messageDO = new JSONObject(message);
            if (messageDO != null) {
                JSONObject jo = new JSONObject(messageDO.optString("content"));
                NotificationDO ndo = new NotificationDO();
                ndo.setContent(jo.optString("content"));
                ndo.setTitle(jo.optString("title"));
                ndo.setTicker(jo.optString("ticker"));
                ndo.setNid(jo.optInt("nid", 1));
                ndo.setExtras(jo.optJSONObject("extras"));
                return ndo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
