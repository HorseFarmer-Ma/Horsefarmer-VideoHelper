package com.meizu.testdevVideo.broadcast;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.meizu.testdevVideo.activity.MainActivity;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.SharedPreferencesHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.service.PerformsTestService;
import com.meizu.testdevVideo.util.PublicMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class JpushReceiver extends BroadcastReceiver {
	private static final String TAG = "JpushReceiver";
	private SharedPreferencesHelper shared;
	private SharedPreferences settingSharedPreferences = null;
	private SharedPreferences.Editor editor = null;


	@Override
	public void onReceive(Context context, Intent intent) {
		settingSharedPreferences = ((settingSharedPreferences ==  null)?
				PreferenceManager.getDefaultSharedPreferences(context) : settingSharedPreferences);
		editor = (editor == null) ? settingSharedPreferences.edit() : editor;
        Bundle bundle = intent.getExtras();
		Log.d(TAG, "[JpushReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[JpushReceiver] 接收Registration Id : " + regId);
            //send the Registration Id to your server...
                        
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	Log.d(TAG, "[JpushReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
			if(shared == null){
				shared = new SharedPreferencesHelper(context, "jpush_message");
			}
			shared.writeStringData("message", "接收到的JPush消息为（时间为："
					+ PublicMethod.getSystemTime() +"）：" + bundle.toString());
        	processCustomMessage(context, bundle);
        
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            String msg = bundle.getString("cn.jpush.android.ALERT");
			int notificationId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "接收到的推送消息为" + msg);

			// 收到的为任务Json，则不显示通知栏
			if(msg.contains("testPackageName")){
				NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancel(notificationId);
				editor.remove(SettingPreferenceKey.KEEP_WAKEUP);
				editor.putBoolean(SettingPreferenceKey.KEEP_WAKEUP, true);    // 设置保持屏幕唤醒
				editor.apply();
				PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_RESULT);    // 清除测试结果
				PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_TESTCASE_PATH);   // 清除jar包
				PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_LOG);   // 清除打印的Log
				Intent mTaskIntent = new Intent(context, PerformsTestService.class);
				mTaskIntent.putExtra("taskType", 1);
				mTaskIntent.putExtra(iPerformsKey.taskPushJson, msg);
				if (PublicMethod.isServiceWorked(context, "com.meizu.testdevVideo.service.PerformsTestService")){
					context.stopService(mTaskIntent);
				}
				context.startService(mTaskIntent);
			}

			// 停止测试任务
			if(msg.equals("stopTask")){
				NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancel(notificationId);
				Intent mTaskIntent = new Intent(context, PerformsTestService.class);
				context.stopService(mTaskIntent);
				PublicMethod.killProcess("ps|grep uiautomator", "system    ", " ");
				PublicMethod.killProcess("ps |grep com.android.commands.monkey", "system    ", " ");
				ToastHelper.addToast("云端kill", context);
			}

			// 执行adb命令
			if(msg.contains("adb shell ")){
				NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancel(notificationId);
				try {
					Runtime.getRuntime().exec(msg.replace("adb shell ", ""));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// 留余地，自定义命令
			if(msg.contains("#defined command")){
				NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancel(notificationId);
			}
        	
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
			// 用于自定义点击通知栏通知的功能
            Log.d(TAG, "[JpushReceiver] 用户点击打开了通知");
        	// 打开自定义的Activity
        	Intent i = new Intent(context, MainActivity.class);
        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	context.startActivity(i);
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[JpushReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            // 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
        	
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
        	boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
        	Log.d(TAG, "[JpushReceiver]" + intent.getAction() +" connected state change to "+connected);
        } else {
        	Log.d(TAG, "[JpushReceiver] Unhandled intent - " + intent.getAction());
        }
	}

	// 打印所有的 intent extra 数据
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", EXTRA_NOTIFICATION_ID value:" + bundle.getInt(key));
			}else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
				sb.append("\nkey:" + key + ", EXTRA_CONNECTION_CHANGE value:" + bundle.getBoolean(key));
			} else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
				if (bundle.getString(JPushInterface.EXTRA_EXTRA).isEmpty()) {
					Log.i(TAG, "This message has no Extra data");
					continue;
				}

				try {
					JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
					Iterator<String> it =  json.keys();

					while (it.hasNext()) {
						String myKey = it.next().toString();
						sb.append("\nkey:" + key + ", EXTRA_EXTRA value: [" +
								myKey + " - " +json.optString(myKey) + "]");
					}
				} catch (JSONException e) {
					Log.d(TAG, "Get message extra JSON error!");
				}

			} else {
				sb.append("\nkey:" + key + ", else value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}
	
	//send msg to MainActivity
	private void processCustomMessage(Context context, Bundle bundle) {
//		if (MainActivity.isForeground) {
//			String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
//			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
//			Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
//			msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
//			if (!ExampleUtil.isEmpty(extras)) {
//				try {
//					JSONObject extraJson = new JSONObject(extras);
//					if (null != extraJson && extraJson.length() > 0) {
//						msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
//					}
//				} catch (JSONException e) {
//
//				}
//
//			}
//			context.sendBroadcast(msgIntent);
//		}
	}
}
