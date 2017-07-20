package com.meizu.testdevVideo.push.android;

import android.graphics.BitmapFactory;

import com.meizu.testdevVideo.BuildConfig;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.library.AlarmSetting;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.mpush.client.ClientConfig;

/**
 * MPush启动协助
 * Created by maxueming on 2017/6/27.
 */
public class MPushStart {

    public static void startMush(){
        Notifications.I.init(SuperTestApplication.getContext());
        Notifications.I.setSmallIcon(R.mipmap.ic_app);
        Notifications.I.setLargeIcon(BitmapFactory.decodeResource(SuperTestApplication.getContext().getResources(), R.mipmap.ic_app));
        String userId = PerformsData.getInstance(SuperTestApplication.getContext()).readStringData(iPerformsKey.imei);
        String tags = PerformsData.getInstance(SuperTestApplication.getContext()).readStringData(iPerformsKey.deviceType);
        bindUser(userId, tags);
        startPush(Constants.Push.ALLOC_SERVER_ADDRESS, userId);
    }

    public static void stopMpush(){
        unBindUser();
        MPush.I.checkInit(SuperTestApplication.getContext()).stopPush();
    }

    private static void bindUser(String userId, String tags){
        // bindUser
        MPush.I.checkInit(SuperTestApplication.getContext()).bindAccount(userId, tags);
    }

    private static void unBindUser(){
        // bindUser
        MPush.I.checkInit(SuperTestApplication.getContext()).unbindAccount();
    }

    private static void startPush(String allocServer, String userId){
        initPush(allocServer, userId);
        MPush.I.checkInit(SuperTestApplication.getContext()).startPush();
    }

    private static void initPush(String allocServer, String userId) {
        //公钥有服务端提供和私钥对应
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";

        ClientConfig cc = ClientConfig.build()
                .setPublicKey(publicKey)
                .setAllotServer(allocServer)
                .setDeviceId(PerformsData.getInstance(SuperTestApplication.getContext()).readStringData(iPerformsKey.imei))
                .setClientVersion(BuildConfig.VERSION_NAME)
                .setEnableHttpProxy(true)
                .setUserId(userId);
        MPush.I.checkInit(SuperTestApplication.getContext()).setClientConfig(cc);
    }

}
