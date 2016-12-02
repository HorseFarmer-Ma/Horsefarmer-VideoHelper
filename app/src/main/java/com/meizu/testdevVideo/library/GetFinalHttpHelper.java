package com.meizu.testdevVideo.library;

import net.tsz.afinal.FinalHttp;

/**
 * Created by maxueming on 2016/10/26.
 */
public class GetFinalHttpHelper {
    public static FinalHttp mInstance;
    private static int TimeOut = 8 * 1000;    // 定义超时时间

    public static FinalHttp getInstance(){
        if(mInstance == null){
            mInstance = new FinalHttp();
            mInstance.configTimeout(TimeOut);
        }
        return mInstance;
    }

}
