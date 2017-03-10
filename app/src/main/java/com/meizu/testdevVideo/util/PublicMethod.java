package com.meizu.testdevVideo.util;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;

import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.service.WifiLockService;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.shell.ShellUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mxm on 2016/5/5.
 */
public class PublicMethod {

    /**
     * 获取assets资源文件夹中的文件，写入到手机中
     * @return
     */
    public static File copyAssetFile(Context context, String fileName, String director) {
        File PRO_DIR = new File(director);
        File f = null;
        try {
            if(!PRO_DIR.exists()){
                PRO_DIR .mkdirs();  // 创建文件夹
            }
            InputStream is = context.getClass().getClassLoader().getResourceAsStream("assets/" + fileName);   // 打开文件
            f = new File(PRO_DIR, fileName) ;
            f .createNewFile ();
            FileOutputStream fOut = new FileOutputStream(f);
            byte [] buffer = new byte[ 1024] ;
            int len = 0 ;
            while (( len = is.read(buffer)) != - 1) {
                fOut .write (buffer , 0, len) ;
                fOut .flush();
            }
            is .close ();
            fOut .close ();
            return f;
        } catch ( IOException e) {
            e .printStackTrace ();
        }
        return null ;
    }

    /**
     * 从asset中复制文件夹进/data/data/packagename/files中
     */
    public static void copyAssetDirToFiles(Context context, String dirname)
            throws IOException {
        File dir = new File(context.getFilesDir() + "/" + dirname);
        dir.mkdir();

        AssetManager assetManager = context.getAssets();
        String[] children = assetManager.list(dirname);
        for (String child : children) {
            child = dirname + '/' + child;
            String[] grandChildren = assetManager.list(child);
            if (0 == grandChildren.length)
                copyAssetFileToFiles(context, child);
            else
                copyAssetDirToFiles(context, child);
        }
    }

    /**
     * 从asset中复制文件进/data/data/packagename/files中
     */
    public static void copyAssetFileToFiles(Context context, String filename)
            throws IOException {
        InputStream is = context.getAssets().open(filename);
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();

        File of = new File(context.getFilesDir() + "/" + filename);
        of.createNewFile();
        FileOutputStream os = new FileOutputStream(of);
        os.write(buffer);
        os.close();
    }


    /**
     * 检测本机是否安装了某APK
     * @param context
     * @param PacketName：APK包名
     * @return
     */
    public static boolean isInstallApk(Context context, String PacketName){
        boolean isinstall = false;
        PackageManager pManager = context.getPackageManager();

        List<PackageInfo> appList = pManager. getInstalledPackages( 0) ;
        for ( int i = 0 ; i < appList .size (); i++ ) {
            PackageInfo pak = (PackageInfo) appList .get (i );
            String mPak = pak .packageName ;
            if (mPak.contains (PacketName)) {
                isinstall = true;
                break;
            }
        }
        return isinstall;
    }


    /**
     * 检测本机是否安装了某APK
     * @param context
     * @param PacketName：APK包名
     * @return
     */
    public static Map<String, Object> getApkMessage(Context context, String PacketName){
        Map<String, Object> map = new HashMap<String, Object>();
        PackageManager pManager = context.getPackageManager();
        List<PackageInfo> appList = pManager.getInstalledPackages( 0) ;
        for ( int i = 0 ; i < appList .size (); i++ ) {
            PackageInfo pak = (PackageInfo) appList .get (i);
            String mPak = pak .packageName ;
            if (mPak .contains (PacketName)) {
                map.put(PublicMethodConstant.VERSION_CODE, pak.versionCode);
                map.put(PublicMethodConstant.VERSION_NAME, pak.versionName);
                map.put(PublicMethodConstant.FIRST_INSTALL_TIME, pak.firstInstallTime);
                map.put(PublicMethodConstant.INSTALL_LOCATION, pak.installLocation);
                return map;
            }
        }
        return null;
    }


    /**
     * 从assets获取资源安装APK
     * @param context
     * @param apkName
     */
    public static void installApkFromAsset(Context context, String apkName){
        File file = copyAssetFile(context, apkName, iPublicConstants.LOCAL_MEMORY + "SuperTest/");   // 拷贝APK文件
        Intent intent = new Intent( Intent. ACTION_VIEW );
        intent .addFlags (Intent . FLAG_ACTIVITY_NEW_TASK) ;
        intent .addFlags (Intent . FLAG_GRANT_READ_URI_PERMISSION);
        intent .setClassName ("com.android.packageinstaller" ,
                "com.android.packageinstaller.PackageInstallerActivity" );
        String type = "android/vnd.android.package-archive";
        intent .setDataAndType (Uri. fromFile(file) , type );
        context.startActivity (intent );
    }


    /**
     * Creat by MXM 根据包名跳转系统应用信息界面
     * @param context：上下文资源
     * @param packageName：包名
     */
    public static void showInstalledAppDetails(Context context, String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        context.startActivity(intent);
    }

    /**
 　　* 将程序中字符串写入到文本文件
 　　* @param toSaveString
 　　* @param filePath
 　　*/
    public static void saveStringToFile(String toSaveString, String fileName, String filePath) {
        File f = null;
        try{
            File FileDir = new File(filePath);
            if (!FileDir.exists()) {
                FileDir.mkdirs();
            }
            f = new File(FileDir, fileName);
            f.createNewFile();

            FileOutputStream outStream = new FileOutputStream(f);
            outStream.write(toSaveString.getBytes());
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     　　* 将程序中字符串写入到文本文件
     　　* @param toSaveString
     　　* @param filePath
     　　*/
    public static void saveStringToFileWithoutDeleteSrcFile(String toSaveString, String fileName, String filePath) {
        try{
            File FileDir = new File(filePath);
            if (!FileDir.exists()) {
                FileDir.mkdirs();
            }
            FileWriter out;
            if(fileName.contains(".txt")){
                out = new FileWriter(filePath + fileName, true);
            }else{
                out = new FileWriter(filePath + fileName + ".txt", true);
            }

            out.write(toSaveString);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 复制同一目录下单个文件
     * @param srcFileName：源文件名称
     * @param destFileName：目标文件名称
     * @param srcPath：源文件路径
     * @param destPath：目标文件路径
     * @throws IOException
     */
    public static void copySingleFile(String srcFileName, String destFileName, String srcPath, String destPath) throws IOException {
        File fos = null;
        InputStream is = null;
        byte[] bits = new byte[1024];
        File destpath = new File(destPath);

        // 文件夹不存在则新建目录
        if(!destpath.exists()){
            destpath.mkdirs();
        }
        fos = new File(destpath, destFileName);
        fos.createNewFile();   // 新增文件
        try {
            is = new FileInputStream(srcPath + srcFileName);
            FileOutputStream outStream = new FileOutputStream(fos);
            int length;
            while((length = is.read(bits)) != -1) {
                outStream.write(bits, 0, length);
                outStream.flush();
            }
            is.close();
            outStream.close();
        } catch ( IOException e) {
            e .printStackTrace();
        }
    }

    /**
　　* 从手机中读取文件内容，输出字符串
　　* @param filePath
　　* @return 文件内容
　　*/
    public static String readFile(String filePath) {
        String str = "";
        try {
            File readFile = new File(filePath);
            if(!readFile.exists()) {
                return null;
            }

            FileInputStream inStream = new FileInputStream(readFile);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = -1;

            while ((length = inStream.read(buffer)) != -1) {
                stream.write(buffer, 0, length);
            }

            str = stream.toString();
            stream.close();
            inStream.close();
            return str;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除单个文件
     * @param   filePath    被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }


    /**
     * 复制整个文件夹内容
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static boolean copyFolder(String oldPath, String newPath) {
        boolean isok = true;

        try {
            new File(newPath).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a=new File(oldPath);
            String[] file=a.list();
            File temp=null;
            for (int i = 0; i < file.length; i++) {
                if(oldPath.endsWith(File.separator)){
                    temp=new File(oldPath+file[i]);
                }else {
                    temp=new File(oldPath+File.separator+file[i]);
                }

                if(temp.isFile()){
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ( (len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if(temp.isDirectory()){//如果是子文件夹
                    copyFolder(oldPath+"/"+file[i], newPath+"/"+file[i]);
                }
            }
        } catch (Exception e) {
            isok = false;
        }
        return isok;
    }

    /**
     * 复制整个文件夹内容
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static boolean copyLogReportFolder(String oldPath, String newPath) {
        boolean isok = true;

        try {
            new File(newPath).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a=new File(oldPath);
            String[] file=a.list();
            File temp=null;
            for (int i = 0; i < file.length; i++) {
                if(oldPath.endsWith(File.separator)){
                    temp=new File(oldPath+file[i]);
                }else {
                    temp=new File(oldPath+File.separator+file[i]);
                }

                if(temp.isFile()){
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = null;
                    if (temp.getName().contains("logSnapshot")){
                        output = new FileOutputStream(newPath + "/" +
                                "logSnapshot.txt");
                    }else{
                        output = new FileOutputStream(newPath + "/" +
                                (temp.getName()).toString());
                    }

                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ( (len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if(temp.isDirectory()){//如果是子文件夹
                    copyLogReportFolder(oldPath+"/"+file[i], newPath+"/"+file[i]);
                }
            }
        } catch (Exception e) {
            isok = false;
        }
        return isok;
    }

    /**
     * 判断当前是否有指定服务在运行
     * @param context：上下文资源
     * @param serviceName：服务名
     * @return
     */
    public static boolean isServiceWorked(Context context, String serviceName) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(Integer.MAX_VALUE);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前时间
     * @return time
     */
    public static String getSystemTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   // 格式化当前时间
        String time = dateFormat.format(new Date());    // 获取当前时间
        return time + "   ";
    }

    /**
     * 可靠性有待验证
     * 检查手机是否具有某个权限
     * @param context：上下文
     * @param permissionName：权限名，例：android.permission.SYSTEM_ALERT_WINDOW
     * @param packageName：应用包名
     * @return：返回结果
     */
    public static boolean checkSystemWindowManagePermiss(Context context, String permissionName, String packageName){
        PackageManager pm = context.getPackageManager();
        return (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(permissionName, packageName));
    }

    /**
     * 安装APP应用
     */
    public static void installApp(Context context, File file){
        Intent intent = new Intent( Intent. ACTION_VIEW );
        intent .addFlags (Intent . FLAG_ACTIVITY_NEW_TASK) ;
        intent .addFlags (Intent . FLAG_GRANT_READ_URI_PERMISSION);
        intent .setClassName ("com.android.packageinstaller" ,
                "com.android.packageinstaller.PackageInstallerActivity" );
        String type = "android/vnd.android.package-archive";
        intent .setDataAndType (Uri. fromFile(file) , type );
        context.startActivity (intent );
    }

    /**
     * @param path: 文件路径
     * @return: 返回日期最新的文件夹
     */
    public static String getFileDirNewDataPath(String path){
        String filePath = "";
        Long iNewFileNumber = 0L;
        Long iFileNumber = 0L;
        DateFormat df = new SimpleDateFormat("yy-MM-dd--HH-mm-ss");
        File SDFile = new File(path);
        File sdPath = new File(SDFile.getAbsolutePath());
        if(sdPath.listFiles() == null){
            return null;
        }

        if(sdPath.listFiles().length > 0) {
            for(File file : sdPath.listFiles()) {
                String strFileNumber = file.getName().replace(path, "");
                try {
                    iFileNumber = df.parse(strFileNumber).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(iFileNumber > iNewFileNumber){
                    iNewFileNumber = iFileNumber;
                    filePath = path + file.getName();
                }
            }
        }

        return filePath;
    }


    /**
     * @param path: 文件路径
     * @return: 返回日期最新的文件夹
     */
    public static String getNewMtkLogPath(String path){
        String filePath = "";
        Long iNewFileNumber = 0L;
        Long iFileNumber = 0L;
        DateFormat df = new SimpleDateFormat("yy_MMdd_HHmmss");
        File SDFile = new File(path);
        File sdPath = new File(SDFile.getAbsolutePath());
        if(sdPath.listFiles() == null){
            return null;
        }

        if(sdPath.listFiles().length > 0) {
            for(File file : sdPath.listFiles()) {
                String strFileNumber = file.getName().replace(path, "").replace("APLog_", "");
                try {
                    iFileNumber = df.parse(strFileNumber).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(iFileNumber > iNewFileNumber){
                    iNewFileNumber = iFileNumber;
                    filePath = path + file.getName();
                }
            }
        }

        return filePath;
    }

    public static void mute(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences.getBoolean(SettingPreferenceKey.MUTE, true)){
            if(sharedPreferences.getBoolean(SettingPreferenceKey.MUTE_RUN_TASK, true)){
                if(PerformsData.getInstance(context).readBooleanData(iPerformsKey.isStart)
                        || MonkeyTableData.getInstance(context).readBooleanData("isStart")){
                    muteTask(context);
                }
            }else{
                muteTask(context);
            }
        }
    }

    public static void muteTask(Context context){
        AudioManager audioManager;  // 系统声音管理类
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0){
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
        }
        if (audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) != 0){
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_SHOW_UI);
        }
        if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0){
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_SHOW_UI);
        }
        if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0){
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_SHOW_UI);
        }
    }

    /**
     * 杀掉进程
     * @param command：查找进程的指令
     * @param start：进程数前面字段
     * @param end：进程数后面字段
     */
    public static void killProcess(String command, String start, String end){
        String content = ShellUtils.execCommand(command, false, true).successMsg;
        if(!TextUtils.isEmpty(content)){
            String regex = start + "([^" + end + "]*)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(content);
            if (m.find()) {
                try {
                    Runtime.getRuntime().exec("kill process " + m.group(1));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 锁定wifi
     * @param settingSharedPreferences
     * @param context
     */
    public static void lockWifi(SharedPreferences settingSharedPreferences, Context context){
        if(settingSharedPreferences.getBoolean(SettingPreferenceKey.LOCK_WIFI, true)){
            Intent wifiIntent = new Intent(context, WifiLockService.class);
            context.startService(wifiIntent);
        }
    }

    /**
     * 获取外置SD卡路径
     * 需要root，不建议使用
     * @return
     */
    public static List<String> getExtSDCardPaths() {
        List<String> paths = new ArrayList<String>();
        String extFileStatus = Environment.getExternalStorageState();
        File extFile = Environment.getExternalStorageDirectory();
        if (extFileStatus.endsWith(Environment.MEDIA_UNMOUNTED)
                && extFile.exists() && extFile.isDirectory()
                && extFile.canWrite()) {
            paths.add(extFile.getAbsolutePath());
        }
        try {
            // obtain executed result of command line code of 'mount', to judge
            // whether tfCard exists by the result
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                // format of sdcard file system: vfat/fuse
                if ((!line.contains("fat") && !line.contains("fuse") && !line
                        .contains("storage"))
                        || line.contains("secure")
                        || line.contains("asec")
                        || line.contains("firmware")
                        || line.contains("shell")
                        || line.contains("obb")
                        || line.contains("legacy") || line.contains("data")) {
                    continue;
                }
                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String mountPath = parts[mountPathIndex];
                if (!mountPath.contains("/") || mountPath.contains("data")
                        || mountPath.contains("Data")) {
                    continue;
                }
                File mountRoot = new File(mountPath);
                if (!mountRoot.exists() || !mountRoot.isDirectory()
                        || !mountRoot.canWrite()) {
                    continue;
                }
                boolean equalsToPrimarySD = mountPath.equals(extFile
                        .getAbsolutePath());
                if (equalsToPrimarySD) {
                    continue;
                }
                paths.add(mountPath);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return paths;
    }

    public static void saveLog(String log){
        PublicMethod.saveStringToFileWithoutDeleteSrcFile("\n" + PublicMethod.getSystemTime() + log,
                "PerformsLog", iPublicConstants.LOCAL_MEMORY + "SuperTest/ApkLog/");
    }

    /**
     * 判断WIFI是否已连接
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * 屏幕唤醒并解锁
     * @param context
     */
    public static void wakeUpAndUnlock(Context context){
        //获取电源管理器对象
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);

        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();

        // disableKeyguard方法的作用是关闭掉了系统锁屏服务，只需要调用一次就行了
        // 调用多次反而出现问题（还会造成关于关闭定制锁屏、恢复系统锁屏服务功能的bug）
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        int width = getScreenWidthHeight(context, true);
        int height = getScreenWidthHeight(context, false);
        if (km.inKeyguardRestrictedInputMode()) {
            Log.e("PublicMethod", "键盘锁已锁，需要解锁");
//            kl.disableKeyguard();
            // 滑动屏幕，解锁键盘
            try {
                Runtime.getRuntime().exec("input swipe "
                        + String.valueOf(width/2) + " "
                        + String.valueOf(height - height/4) + " "
                        + String.valueOf(width/2) + " "
                        + String.valueOf(height/4));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取手机宽高
     * @param context
     * @param choose true获取宽度； false获取高度
     */
    public static int getScreenWidthHeight(Context context, boolean choose){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return choose? wm.getDefaultDisplay().getWidth() : wm.getDefaultDisplay().getHeight();
    }

    /**
     * 根据应用名返回应用版本号
     */
    public static String getAppVersion(Context context,String apptype){
        PackageManager pm = context.getPackageManager();
        if(apptype.contains("视频")){
            return getVersion(pm, iPublicConstants.PACKET_VIDEO);
        }else if(apptype.contains("音乐")){
            return getVersion(pm, iPublicConstants.PACKET_MUSIC);
        }else if(apptype.contains("读书")){
            return getVersion(pm, iPublicConstants.PACKET_EBOOK);
        }else if(apptype.contains("图库")){
            return getVersion(pm, iPublicConstants.PACKET_GALLERY);
        }else if(apptype.contains("资讯")){
            return getVersion(pm, iPublicConstants.PACKET_READER);
        }else if(apptype.contains("会员")){
            return getVersion(pm, iPublicConstants.PACKET_COMPAIGN);
        }
        return "null";
    }

    /**
     * Returns whether the SDK is KitKat or later
     */
    public static boolean isKitKatOrLater() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    /**
     * 判断是否包含SIM卡
     * @return 状态
     */
    public static boolean hasSimCard(Context context) {
        TelephonyManager telMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // 没有SIM卡
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        return result;
    }

    /**
     * 获取应用版本号
     * @param packName
     * @return
     */
    public static String getVersion(PackageManager pm, String packName) {
        try {
            PackageInfo info = pm.getPackageInfo(packName, 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    /**
     * dp转成px
     * @param context 上下文对象
     * @param dp dp数值
     * @return px数值
     *
     */
    public static int dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**px转成dp
     * @param context 上下文对象
     * @param px 像素数值
     * @return dp数值
     *
     */
    public static int px2Dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * 保存Log日志
     * @param log
     */
    public static void saveLog(String TAG, String log){
        PublicMethod.saveStringToFileWithoutDeleteSrcFile(PublicMethod.getSystemTime() + log + "\n",
                TAG, iPublicConstants.LOCAL_MEMORY + "SuperTest/ApkLog/");
    }

    /**
     * EditText竖直方向是否可以滚动
     * @param editText 需要判断的EditText
     * @return true：可以滚动  false：不可以滚动
     */
    public static boolean canVerticalScroll(EditText editText) {
        //滚动的距离
        int scrollY = editText.getScrollY();
        //控件内容的总高度
        int scrollRange = editText.getLayout().getHeight();
        //控件实际显示的高度
        int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() -editText.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        int scrollDifference = scrollRange - scrollExtent;

        if(scrollDifference == 0) {
            return false;
        }

        return (scrollY > 0) || (scrollY < scrollDifference - 1);
    }

    /**
     * 获取绝对路径下的文件名
     * @param path 绝对路径
     * @return 文件名
     */
    public static String getFileName(String path){
        int start=path.lastIndexOf("/");
        int end=path.length();
        if (start!=-1 && end!=-1) {
            return path.substring(start+1, end);
        } else {
            return null;
        }
    }

}
