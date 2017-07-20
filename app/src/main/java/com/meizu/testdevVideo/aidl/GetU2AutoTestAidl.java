package com.meizu.testdevVideo.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.meizu.aidl.IU2AutoTestAidl;

/**
 * Aidl接口，对接ST应用
 * Created by maxueming on 2017/5/22.
 */
public class GetU2AutoTestAidl {

    private String TAG = "GetSuperTestAidl";
    private static GetU2AutoTestAidl mInstance;
    private IU2AutoTestAidl iu2AutoTestAidl;
    private Context mContext;

    public GetU2AutoTestAidl(Context context){
        mContext = context;
    }

    public synchronized static GetU2AutoTestAidl getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new GetU2AutoTestAidl(context);
        }
        return mInstance;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected");
            iu2AutoTestAidl = IU2AutoTestAidl.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected");
            iu2AutoTestAidl = null;
        }
    };

    /**
     * 绑定服务
     */
    public void bindService(){
        Intent intent = new Intent();
        intent.setAction("com.meizu.testdevVideo.service.U2AutoTestService");
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
    public IU2AutoTestAidl getIU2AutoTestAidl() throws InterruptedException {
        if(null == iu2AutoTestAidl){
            bindService();
            Thread.sleep(1000);
        }
        return iu2AutoTestAidl;
    }
}
