package com.meizu.testdevVideo.library;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast服务
 * Created by maxueming on 2016/4/28.
 */
public class ToastHelper {
    public static Toast toast = null;
    public static void addToast(String message, Context context){
        if (toast != null){
            toast.setText(message);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        } else{
            toast = Toast.makeText(context, message,
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static void addLongToast(String message, Context context){
        if (toast != null){
            toast.setText(message);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        } else{
            toast = Toast.makeText(context, message,
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
