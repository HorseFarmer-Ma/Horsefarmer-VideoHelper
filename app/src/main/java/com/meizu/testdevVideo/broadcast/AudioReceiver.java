package com.meizu.testdevVideo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.meizu.testdevVideo.util.PublicMethod;


/**
 * 监听音量，静音
 * Created by maxueming on 2016/9/2.
 */
public class AudioReceiver extends BroadcastReceiver {

    public static AudioReceiver mInstance;

    public synchronized static AudioReceiver getInstance(){
        if(mInstance == null){
            mInstance = new AudioReceiver();
        }
        return mInstance;
    }


    @Override
    public void onReceive(Context context, Intent intent){
        PublicMethod.mute(context);
    }

}
