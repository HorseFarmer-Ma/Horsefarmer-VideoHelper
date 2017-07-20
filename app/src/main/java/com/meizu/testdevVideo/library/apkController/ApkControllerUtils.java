package com.meizu.testdevVideo.library.apkController;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.util.log.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 安装相关
 * Created by maxueming on 17-5-26.
 */
public class ApkControllerUtils {

    public static List<PackageInfo> getInstalledPackages(Context context) {
        return context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
    }

    /**
     * 获取包名
     * @param context 上下文
     * @param filePath APK文件路径
     * @return 包名
     */
    public static String getPackageName(Context context, String filePath) {
        PackageInfo info = context.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            return info.applicationInfo.packageName;
        }
        return null;
    }

    /**
     * 获取VersionCode
     * @param context 上下文
     * @param filePath APK文件路径
     * @return 版本号
     */
    public static int getVersionCode(Context context, String filePath) {
        PackageInfo info = context.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            return info.versionCode;
        }
        return -1;
    }


    public static String getPackageVersion(Context context, String pkg) {
        if(!TextUtils.isEmpty(pkg)){
            for (PackageInfo info : getInstalledPackages(context)) {
                if (pkg.equals(info.packageName)) {
                    return info.versionName;
                }
            }
            return null;
        }else {
            return null;
        }
    }

    // 返回versionCode
    public static int getPackageVersionCode(Context context, String pkg) {
        for (PackageInfo info : getInstalledPackages(context)) {
            if (pkg.equals(info.packageName)) {
                return info.versionCode;
            }
        }
        return -1;
    }

    // 判断文件是否安装
    public static boolean isPackageInstalled(Context context, String pkg) {
        for (PackageInfo info : getInstalledPackages(context)) {
            if (pkg != null && pkg.equals(info.packageName)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否系统app
    public static boolean isSystemApp(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo e = pm.getApplicationInfo(pkg, 0);
            if(e != null) {
                return (e.flags & 1) != 0 || (e.flags & 128) != 0;
            }
        } catch (PackageManager.NameNotFoundException var4) {
            var4.printStackTrace();
        }

        return false;
    }

    /**
     * 卸载软件
     * 系统级别应用：则只能恢复手机自带初始旧版本
     * 第三方应用：完全卸载
      * @param context 上下文
     * @param packageName 欲卸载的包名
     */
    public static void uninstall(Context context, String packageName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                context.getPackageManager().getPackageInstaller().uninstall(packageName,
                        PendingIntent.getActivity(context, 0, new Intent(context, context.getClass()), 0).getIntentSender());
                Logger.file("卸载APK ===> Android 5.0以上", Logger.MPUSH_TASK_SERVICE);
            }

            try {
                Runtime.getRuntime().exec("pm uninstall -k " + packageName);
            } catch (IOException e) {
                Logger.file("卸载APK ===> IOException" + e, Logger.MPUSH_TASK_SERVICE);
                e.printStackTrace();
            }

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 静默安装APK
     * @param path 路径
     * @return 执行结果
     */
    public static boolean installApk(String path) {
        return execute("pm install -r " + path);
    }

    /**
     * 非静默安装
     * 安装APP应用
     */
    public static void installApk(Context context, File file){
        Intent intent = new Intent(Intent.ACTION_VIEW );
        intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK) ;
        intent.addFlags (Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClassName ("com.android.packageinstaller" ,
                "com.android.packageinstaller.PackageInstallerActivity" );
        String type = "android/vnd.android.package-archive";
        intent.setDataAndType (Uri.fromFile(file), type );
        context.startActivity (intent );
    }

    public static boolean execute(String command) {
        try {
            Runtime.getRuntime().exec(command).waitFor();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 预判是否能够安装APK
     * @param context 上下文
     * @param pkg 包名
     * @return 能否安装
     */
    public static boolean preInstallForApk(Context context, String pkg, String filePath){
        if(getVersionCode(context, filePath) >= getPackageVersionCode(context, pkg)){
            Logger.file("INSTALL_APP:版本号大于等于当前应用，可以覆盖安装APK", Logger.MPUSH_TASK_SERVICE);
            return true;
        }

        Logger.file("INSTALL_APP_THEN_RUN_MONKEY:卸载APK", Logger.MPUSH_TASK_SERVICE);

        if(isPackageInstalled(context, pkg)){
            Logger.file("静默卸载APK" + (clientUninstall(pkg)? "成功" : "失败"), Logger.MPUSH_TASK_SERVICE);
        }

        if(isPackageInstalled(context, pkg)){
            if(isSystemApp(context, pkg)){
                Logger.file("INSTALL_APP:系统APP", Logger.MPUSH_TASK_SERVICE);
                if(getVersionCode(context, filePath) < getPackageVersionCode(context, pkg)){
                    Logger.file("INSTALL_APP:APK版" +
                            "本号小于系统自带的，无法覆盖安装", Logger.MPUSH_TASK_SERVICE);
                    return false;
                }
            }else{
                Logger.file("INSTALL_APP:非系统APK，但uninstall卸载失败", Logger.MPUSH_TASK_SERVICE);
                return false;
            }
        }
        Logger.file("INSTALL_APP:可以覆盖安装APK", Logger.MPUSH_TASK_SERVICE);
        return true;
    }

    /**
     * 静默安装apk
     */
    public static boolean clientInstall(String apkPath) {
        Logger.d("clientInstall apkPath = " + apkPath);
        InstallHelper.InstallStatus intent = InstallHelper.doInstall(SuperTestApplication.getContext(), apkPath);
        if(InstallHelper.InstallStatus.SUCCESS.equals(intent)) {
            Logger.d("clientInstall SUCCESS");
            return true;
        } else {
            Logger.d("clientInstall fail");
            return false;
        }
    }


    /**
     * 静默卸载apk
     */
    public static boolean clientUninstall(String packageName) {
        Logger.d("clientUninstall packageName = " + packageName);
        UnInstallHelper.UnInstallStatus intent = UnInstallHelper.doUninstall(SuperTestApplication.getContext(), packageName);
        if(UnInstallHelper.UnInstallStatus.SUCCESS.equals(intent)) {
            Logger.d("clientUninstall SUCCESS");
            return true;
        } else {
            Logger.d("clientUninstall fail");
            return false;
        }
    }

}
