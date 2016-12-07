package com.meizu.testdevVideo.util.sendReport;

import android.content.Context;
import android.text.TextUtils;

import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.GetFinalHttpHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.PublicMethod;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by maxueming on 2016/11/18.
 */
public class PerformsReportSend {
    private Context mContext;
    private static PerformsReportSend instance = null;

    private ReportSendCallBack reportSendCallBack;

    public synchronized static PerformsReportSend getInstance() {
        if (instance == null) {
            instance = new PerformsReportSend();
        }
        return instance;
    }

    public void sendReport(Context context, AjaxParams params, ReportSendCallBack CallBack){
        this.reportSendCallBack = CallBack;
        if (mContext == null){
            mContext = context;
        }

        GetFinalHttpHelper.getInstance().post(iPublicConstants.PERFORMS_POST_TESTRESULT_URL, params, new AjaxCallBack<String>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                reportSendCallBack.isSendComplete(true);
                if(500 == errorNo){

                }
                saveLogLocal("发送失败");
                saveLogLocal(String.valueOf(errorNo));
                saveLogLocal(strMsg);
            }

            @Override
            public void onStart() {
                reportSendCallBack.isSendComplete(false);
                super.onStart();
                saveLogLocal("开始发送报告");
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                reportSendCallBack.isSendComplete(true);
                //根据服务器返回的json数据，判断上传是否成功
                if(!TextUtils.isEmpty(t)){
                    if("200".equals(t)){
                        saveLogLocal("发送报告成功");
                    }else{
                        saveLogLocal("服务器异常500");
                        ToastHelper.addToast("服务器异常", mContext);
                    }
                }
            }
        });
    }

    /**
     * 保存服务LOG到本地
     * @param log
     */
    private void saveLogLocal(String log){
        PublicMethod.saveStringToFileWithoutDeleteSrcFile("\n" + PublicMethod.getSystemTime() + log,
                "Performs_Log", iPublicConstants.LOCAL_MEMORY + "SuperTest/ApkLog/");
    }


}
