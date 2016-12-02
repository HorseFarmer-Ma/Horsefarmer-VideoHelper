package com.meizu.testdevVideo.util.register;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.testdevVideo.interports.iPublic;
import com.meizu.testdevVideo.library.GetFinalHttpHelper;
import com.meizu.testdevVideo.library.RegisterFinalHttpHelper;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by maxueming on 2016/11/29.
 */
public class RegisterPost {
    private Context mContext;
    private static RegisterPost instance = null;

    private RegisterResultCallBack registerCallBack;

    public synchronized static RegisterPost getInstance() {
        if (instance == null) {
            instance = new RegisterPost();
        }
        return instance;
    }

    public void setRegisterResultCallBack(RegisterResultCallBack CallBack){
        this.registerCallBack = CallBack;
    }

    public void registerPost(Context context, AjaxParams params) {
        if (mContext == null) {
            mContext = context;
        }

        Log.e("RegisterPost", params.toString());

        GetFinalHttpHelper.getInstance().post(iPublic.PERFORMS_POST_ID_TAG_ALIAS_URL, params, new AjaxCallBack<String>() {
            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                registerCallBack.isSendSuccess(false, true);
                Log.e("RegisterPost", "发送Failure");
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                //根据服务器返回的json数据，判断上传是否成功
                Log.e("RegisterPost", "返回的数据为：" + t);
                if (!TextUtils.isEmpty(t)) {
                    if ("200".equals(t)) {
                        Log.e("RegisterPost", "发送成功");
                        registerCallBack.isSendSuccess(true, true);
                    } else {
                        Log.e("RegisterPost", "服务器异常");
                        registerCallBack.isSendSuccess(false, true);
                    }
                }else{
                    registerCallBack.isSendSuccess(false, true);
                    Log.e("RegisterPost", "发送异常");
                }
            }
        });
    }
}
