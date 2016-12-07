package com.meizu.testdevVideo.library;

import net.tsz.afinal.FinalHttp;

/**
 * Created by maxueming on 2016/11/30.
 */
public class RegisterFinalHttpHelper {
    public static FinalHttp mRegisterInstance;

    public synchronized static FinalHttp getInstance(){
        if(mRegisterInstance == null){
            mRegisterInstance = new FinalHttp();
            mRegisterInstance.configTimeout(4 * 1000);
        }
        return mRegisterInstance;
    }

}
