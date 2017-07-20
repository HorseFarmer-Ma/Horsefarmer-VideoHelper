package com.meizu.testdevVideo.library.apkController;

import android.content.Context;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by maxueming on 2017/7/5.
 */

public class UnInstallHelper {

    private static final String TAG = "UnInstallHelper";

    public static enum UnInstallStatus{
        NOT_SUPPORT,
        SUCCESS,
        FAILED;
        private int mErrorCode = -10000;
        protected void setErrorCode(int errorCode){
            mErrorCode = errorCode;
        }
        public int getErrorCode(){
            return mErrorCode;
        }
    }

    public static UnInstallStatus doUninstall(Context context, String packageName){
        final Object LOCK = new Object();
        final UnInstallStatus result = UnInstallStatus.SUCCESS;
        try{
            final int DELETE_SUCCEEDED = (Integer) ReflectHelper.getStaticField("android.content.pm.PackageManager", "DELETE_SUCCEEDED");
            final int DELETE_ALL_USERS = (Integer) ReflectHelper.getStaticField("android.content.pm.PackageManager", "DELETE_ALL_USERS");
            PackageManager pm = context.getPackageManager();
            ReflectHelper.invoke(pm, "deletePackage",
                    new Class<?>[]{String.class, IPackageDeleteObserver.class, int.class},
                    new Object[]{packageName, new IPackageDeleteObserver.Stub() {
                        @Override
                        public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                            Log.d(TAG, "video doUninstall return code : " + returnCode);
                            result.setErrorCode(returnCode);
                            synchronized (LOCK) {
                                LOCK.notify();
                            }
                        }
                    }, DELETE_ALL_USERS});
            synchronized (LOCK) {
                try{
                    LOCK.wait(120000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                    return UnInstallStatus.FAILED;
                }
            }
            if(result.getErrorCode() != DELETE_SUCCEEDED){
                UnInstallStatus error = UnInstallStatus.FAILED;
                error.setErrorCode(result.getErrorCode());
                return error;
            }else{
                return result;
            }
        }catch(Exception e){
            Log.d(TAG, "video doUninstall error :" + e.getMessage());
            e.printStackTrace();
        }
        return UnInstallStatus.NOT_SUPPORT;
    }
}
