package com.meizu.testdevVideo.service;

import android.app.IntentService;
import android.content.Intent;

import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.util.PublicMethod;

public class TrySendResultAgainService extends IntentService {

    public static final String ACTION_TRY_SEND_RESULT = "action.try.send.result";

    public TrySendResultAgainService() {
        super("TrySendResultAgainService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TRY_SEND_RESULT.equals(action)) {



            }
        }
    }


    private void saveLog(String log){
        PublicMethod.saveStringToFileWithoutDeleteSrcFile("\n" + PublicMethod.getSystemTime() + log,
                "Performs_Log", iPublicConstants.LOCAL_MEMORY + "SuperTest/ApkLog/");
    }


}
