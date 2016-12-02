package com.meizu.testdevVideo.util.sharepreference;

import android.content.Context;

import com.meizu.testdevVideo.library.SharedPreferencesHelper;


/**
 * Created by mxm on 2016/9/3.
 */
public class PrefWidgetOnOff {

    private static SharedPreferencesHelper mainWidgetOnOff;

    public synchronized static SharedPreferencesHelper getInstance(Context context){
        if(mainWidgetOnOff == null){
            mainWidgetOnOff = new SharedPreferencesHelper(context, "widget_switch");
        }
        return mainWidgetOnOff;
    }
}
