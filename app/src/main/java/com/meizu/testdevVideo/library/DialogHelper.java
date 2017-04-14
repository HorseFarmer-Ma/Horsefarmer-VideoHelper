package com.meizu.testdevVideo.library;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by maxueming on 2016/9/12.
 */
public class DialogHelper {
    private static DialogHelper mInstance;
    public DialogHelper(){
    }

    public static DialogHelper getInstance(){
        if(mInstance == null){
            mInstance = new DialogHelper();
        }
        return mInstance;
    }

    /**
     * 中间一颗确认按钮
     * @param title：标题
     * @param content：内容
     */
    public void createdDialogWithDismissButton(Context context, String title, String content){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

}
