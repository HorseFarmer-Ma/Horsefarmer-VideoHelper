package com.meizu.testdevVideo.constant;

import android.content.Context;

import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;

import net.tsz.afinal.http.AjaxParams;

/**
 * Created by maxueming on 2016/10/29.
 */
public class GetPerformsParams {

    public static GetPerformsParams mInstance;

    public static GetPerformsParams getInstance(){
        if(mInstance == null){
            mInstance = new GetPerformsParams();
        }
        return mInstance;
    }

    /**
     * 从pre获取params
     * @param context
     * @return
     */
    public AjaxParams getPerformsParamsByPre(Context context){
        AjaxParams params = new AjaxParams();
        params.put("\"" + iPerformsKey.deviceType + "\"", "\"" + PerformsData.getInstance(context).readStringData(iPerformsKey.deviceType) + "\"");
        params.put("\"" + iPerformsKey.imei + "\"", "\"" + PerformsData.getInstance(context).readStringData(iPerformsKey.imei) + "\"");
        params.put("\"" + iPerformsKey.testTime + "\"", "\"" + PerformsData.getInstance(context).readStringData(iPerformsKey.testTime) + "\"");
        params.put("\"" + iPerformsKey.testType + "\"", "\"" + PerformsData.getInstance(context).readStringData(iPerformsKey.testType) + "\"");
        params.put("\"" + iPerformsKey.appType + "\"", "\"" + PerformsData.getInstance(context).readStringData(iPerformsKey.appType) + "\"");
        params.put("\"" + iPerformsKey.appVersion + "\"", "\"" + PerformsData.getInstance(context).readStringData(iPerformsKey.appVersion) + "\"");
        params.put("\"" + iPerformsKey.systemVersion + "\"", "\"" + PerformsData.getInstance(context).readStringData(iPerformsKey.systemVersion) + "\"");
        params.put("\"" + iPerformsKey.baseBand + "\"", "\"" + PerformsData.getInstance(context).readStringData(iPerformsKey.baseBand) + "\"");
        params.put("\"" + iPerformsKey.kernel + "\"", "\"" + PerformsData.getInstance(context).readStringData(iPerformsKey.kernel) + "\"");
        return params;
    }


    /**
     * 从SQL获取params
     * @param context
     * @return
     */
    public AjaxParams getPerformsParamsBySql(Context context){
        AjaxParams params = new AjaxParams();
        return params;
    }
}
