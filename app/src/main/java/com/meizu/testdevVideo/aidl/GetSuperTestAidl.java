package com.meizu.testdevVideo.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.meizu.aidl.ISuperTestAidl;

/**
 * Aidl接口，对接ST应用
 * Created by maxueming on 2017/5/22.
 */
public class GetSuperTestAidl {

    private String TAG = "GetSuperTestAidl";
    private static GetSuperTestAidl mInstance;
    private ISuperTestAidl iSuperTestAidl;
    private Context mContext;

    public GetSuperTestAidl(Context context){
        mContext = context;
    }

    public synchronized static GetSuperTestAidl getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new GetSuperTestAidl(context);
        }
        return mInstance;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected");
            iSuperTestAidl = ISuperTestAidl.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected");
            iSuperTestAidl = null;
        }
    };

    /**
     * 绑定服务
     */
    public void bindService(){
        Intent intent = new Intent();
        intent.setAction("com.meizu.testdevVideo.service.SuperTestService");
        Intent mIntent = new Intent(AidlUtils.getExplicitIntent(mContext, intent));
        mContext.bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 解绑服务
     */
    public void unbindService(){
        mContext.unbindService(mServiceConnection);
    }



    /**
     * 一定要先绑定服务，即bindService后才可以调用iSuperTestAidl
     * 获取aidl
     * @return aidl
     */
    public ISuperTestAidl getISuperTestAidl() throws InterruptedException {
        if(null == iSuperTestAidl){
            bindService();
            Thread.sleep(2 * 1000);
        }
        return iSuperTestAidl;
    }
}
