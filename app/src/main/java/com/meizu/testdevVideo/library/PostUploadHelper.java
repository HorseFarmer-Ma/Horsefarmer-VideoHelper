package com.meizu.testdevVideo.library;

import android.text.TextUtils;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONObject;


/**
 * Created by maxueming on 2016/10/21.
 */
public class PostUploadHelper {
    private FinalHttp finalHttp;

    // post接口
    public void upload(String[] key, String[] value, String url, int TimeOut){
        finalHttp = new FinalHttp();
        finalHttp.configTimeout(TimeOut);
        try {
            AjaxParams params = new AjaxParams();
            for(int i = 0; i < key.length; i++){
                params.put(key[i], value[i]);
            }

            // post请求，三个参数分别是请求地址、请求参数、请求的回调接口
            finalHttp.post(url, params, new AjaxCallBack<String>() {
                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {
                    super.onFailure(t, errorNo, strMsg);
                }

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onSuccess(String t) {
                    super.onSuccess(t);
                    //根据服务器返回的json数据，判断上传是否成功
                    if(!TextUtils.isEmpty(t)){
                        try {
                            JSONObject obj = new JSONObject(t);
                            if(obj.optInt("result") == 1){
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
