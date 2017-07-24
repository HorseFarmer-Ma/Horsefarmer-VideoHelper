package com.meizu.testdevVideo.library.apkController;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;


public class InstallHelper {
    private static final String TAG = "InstallHelper";

    public static enum InstallStatus{
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
    public static InstallStatus doInstall(Context context, String apkPath){
        final Object LOCK = new Object();
        final InstallStatus result = InstallStatus.SUCCESS;
        try{
            final int INSTALL_REPLACE_EXISTING = (Integer) ReflectHelper.getStaticField("android.content.pm.PackageManager", "INSTALL_REPLACE_EXISTING");
            final int INSTALL_SUCCEEDED = (Integer) ReflectHelper.getStaticField("android.content.pm.PackageManager", "INSTALL_SUCCEEDED");
            PackageManager pm = context.getPackageManager();
            ReflectHelper.invoke(pm, "installPackage",
                    new Class<?>[]{Uri.class, IPackageInstallObserver.class, int.class, String.class},
                    new Object[]{Uri.parse("file://" + apkPath), new IPackageInstallObserver.Stub() {
                        public void packageInstalled(String packageName, int returnCode)
                                throws RemoteException {
                            if(returnCode != INSTALL_SUCCEEDED){
                                Log.w(TAG, "install return code : " + returnCode);
                            }
                            result.setErrorCode(returnCode);
                            synchronized (LOCK) {
                                LOCK.notify();
                            }
                        }
                    }, INSTALL_REPLACE_EXISTING, null});
            synchronized (LOCK) {
                try{
                    LOCK.wait(120000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                    return InstallStatus.FAILED;
                }
            }
            if(result.getErrorCode() != INSTALL_SUCCEEDED){
                InstallStatus error = InstallStatus.FAILED;
                error.setErrorCode(result.getErrorCode());
                return error;
            }else{
                return result;
            }
        }catch(Exception e){
//            Loger.writeFileLog(context, "background install error :" + e.getMessage());
            e.printStackTrace();
        }
        return InstallStatus.NOT_SUPPORT;
    }

//	public static final Intent getSystemInstallIntent(String path){
//		Intent intent = new Intent(Intent.ACTION_VIEW);
//		intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
//		return intent;
//	}
//
//	public static final void startSystemInstallActivity(Context context, String path, UpdateInfo info){
//        AppRestartManager.sendRestartIntent(context, info);
//		Intent intent = getSystemInstallIntent(path);
//		if(!(context instanceof Activity)){
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		}
//		context.startActivity(intent);
//	}
}
