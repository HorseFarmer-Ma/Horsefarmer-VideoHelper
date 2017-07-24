package com.meizu.testdevVideo.util.shell;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;


/**
 * Created by maxueming on 2016/3/3.
 */

public class ShellUtil {
    // 跑monkey的话不太建议采用
    public ShellUtil() {
    }

    // Creat by MXM 根据包名跳转系统应用信息界面
    public static void showInstalledAppDetails(Context context, String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        context.startActivity(intent);
    }
    
    public static String exec(String command) {   //
        Process p = null;
        String result = "";

        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            result = reader.readLine();
            reader.close();
        } catch (IOException var9) {
            var9.printStackTrace();
        } catch (InterruptedException var10) {
            var10.printStackTrace();
        } finally {
            if(p != null) {
                p.destroy();
            }

        }
        return result;
    }
    

    public static void sendBroadcast(String actionName, Object... args) {
        String command = "am broadcast -a " + actionName;
        Object[] arr$ = args;
        int len$ = args.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Object arg = arr$[i$];
            command = command + " " + arg;
        }

        exec(command);
    }

    public static void installApp(String path) {
        exec("pm install -r " + path);
    }

    public static void uninstallApp(String packageName) {
        exec("pm uninstall -k " + packageName);
    }

    public static void exitApp(String packageName) {
        exec("am force-stop " + packageName);
    }

    public static void startApp(String packageName, String launchActivity) {
        String command = "";
        if(launchActivity.startsWith(".")) {
            command = "am start -n " + packageName + "/" + packageName + launchActivity;
        } else {
            command = "am start -n " + packageName + "/" + launchActivity;
        }

        System.out.println(command);
        exec(command);
    }

    public static void clearCache(String packageName) {
        exec("pm clear " + packageName);
    }

    public static void move(String srcPath, String destPath) {
        exec("mv " + srcPath + " " + destPath);
    }

    public static void remove(String filePath) {
        exec("rm  -rf " + filePath);
    }

    public static String getVersion(String packageName) {
        String lineStr = null;
        String version = null;
        Process process = null;

        try {
            process = Runtime.getRuntime().exec("pm dump " + packageName);
            InputStreamReader e = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(e);

            while((lineStr = input.readLine()) != null) {
                if(lineStr.contains("versionName=")) {
                    version = lineStr.split("versionName=")[1].toString();
                    break;
                }
            }

            e.close();
            input.close();
        } catch (IOException var9) {
            var9.printStackTrace();
        } finally {
            if(null != process){
                process.destroy();
            }
        }

        System.out.println(version);
        return version;
    }
    
    
    // ͨ�������ȡ�ֻ���Ϣ����ӹؼ��ַ���˻�ȡ����Ϣ�������ϢΪwant���ַ���Ϣ��write by mxm, ����Ϊadb shell+ command
    public static String getWantInformation(String command, String keyword, String want) {    // ���
    	 String information = null;
         Process process = null;

         try {
             process = Runtime.getRuntime().exec(command);
             InputStreamReader e = new InputStreamReader(process.getInputStream());
             BufferedReader input = new BufferedReader(e);

             String lineStr;
             while((lineStr = input.readLine()) != null) {
                 if(lineStr.contains(keyword)) {
                	 information = lineStr.split(want)[1].toString();
                     break;
                 }
             }

             e.close();
             input.close();
         } catch (IOException var9) {
             var9.printStackTrace();
         } finally {
             if(null != process){
                 process.destroy();
             }
         }

       //  System.out.println(information);
         return information;
    }
    
    
    
    
    // ��ȡ�ֻ�APP�汾�ţ�write by mxm  ERROR
    public static String getAppVersion(String packageName) {
    	String AppVersion = null;
    	String AppMessage = null;
    	AppMessage = exec("dumpsys package " + packageName);
    	if(AppMessage.contains("versionName=")) {
    		AppVersion = AppMessage.split("versionName=")[1].toString();
        }   	
    	return AppVersion;
    }

    public static String getBattery() {
        String lineStr = null;
        String level = null;
        Process process = null;

        try {
            process = Runtime.getRuntime().exec("dumpsys battery");
            InputStreamReader e = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(e);

            while((lineStr = input.readLine()) != null) {
                if(lineStr.contains("level:")) {
                    level = lineStr.split("level:")[1].toString();
                    break;
                }
            }

            e.close();
            input.close();
        } catch (IOException var8) {
            var8.printStackTrace();
        } finally {
            if(null != process){
                process.destroy();
            }
        }

        System.out.println(level);
        return level;
    }

    public static String getProperty(String property) {
        Process p = null;
        String result = "";

        try {
            p = Runtime.getRuntime().exec("getprop " + property);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            result = reader.readLine();
            reader.close();
        } catch (IOException var9) {
            var9.printStackTrace();
        } catch (InterruptedException var10) {
            var10.printStackTrace();
        } finally {
            if(p != null) {
                p.destroy();
            }

        }

        return result;
    }

    public static String getHardware() {
        return getProperty("ro.hardware");
    }

    public static String getSnCode() {
        return getProperty("ro.serialno");
    }

    public static String getImei() {
        return getProperty("gsm.sim.imei");
    }

    public static String getDisplayId() {
        return getProperty("ro.build.display.id");
    }

    public static String getModel() {
        return getProperty("ro.product.model");
    }

    public static String getPackageName(String keyword) {
        String name = null;
        Process process = null;

        try {
            process = Runtime.getRuntime().exec("pm list package");
            InputStreamReader e = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(e);

            String lineStr;
            while((lineStr = input.readLine()) != null) {
                if(lineStr.contains(keyword)) {
                    name = lineStr.split("package:")[1].toString();
                    break;
                }
            }

            e.close();
            input.close();
        } catch (IOException var9) {
            var9.printStackTrace();
        } finally {
            if(null != process){
                process.destroy();
            }
        }

        System.out.println(name);
        return name;
    }

    public static boolean isWifiOn() {
        String result = getProperty("wlan.driver.status");
        return result != null && "ok".equals(result.trim());
    }

    public static boolean isWifiConnected() {
        String result = getProperty("dhcp.wlan0.result");
        return result != null && "ok".equals(result.trim());
    }

    public static void sendResult(String caseName, boolean result) {
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        String extendString = "";
        System.out.println("INSTRUMENTATION_STATUS: language=" + language + '_' + country);
        if(!TextUtils.isEmpty(language)) {
            if(!TextUtils.isEmpty(country)) {
                extendString = language.trim() + "_" + country.trim() + "-";
            } else {
                extendString = language.trim() + "-";
            }
        }

        caseName = extendString + caseName;
        System.out.println("caseName is : " + caseName);
        sendBroadcast("sk.action.RESULT ", new Object[]{"--es NAME " + caseName + " --ez RESULT " + result});
    }

    public static void sendScreenShot(String name) {
        sendBroadcast("sk.action.SCREENSHOT ", new Object[]{"--es NAME " + name});
    }

    public static void sendWakeup() {
        sendBroadcast("sk.action.WAKEUP", new Object[0]);
    }

    public static void sendWifiLock(boolean status) {
        sendBroadcast(status?"sk.action.WIFILOCK_ON ":"sk.action.WIFILOCK_OFF", new Object[0]);
    }

    public static void sendWifiSwitch(boolean status) {
        sendBroadcast("sk.action.SET_NETWORK ", new Object[]{"--ei TYPE 1 --ez NETWORK_STATUS " + status});
    }

    public static void sendDataNetSwitch(boolean status) {
        sendBroadcast("sk.action.SET_NETWORK ", new Object[]{"--ei TYPE 2 --ez NETWORK_STATUS " + status});
    }

    public static void sendWifiRebind(String ssid, String pwd) {
        sendBroadcast("sk.action.WIFILOCK_REBIND --es SSID " + ssid + " --es PWD " + pwd, new Object[0]);
    }

    public static void sendGPS(boolean status) {
        sendBroadcast("sk.action.CHANGE_GPS --ez STATUS " + status, new Object[0]);
    }

    public static void sendTime(int year, int month, int day, int hour, int minute, int second, boolean auto) {
        sendBroadcast("sk.action.SET_TIME ", new Object[]{"--ei YEAR " + year + " --ei MONTH " + month + " --ei DAY " + day + " --ei HOUR " + hour + " --ei MINUTE " + minute + " --ei SECOND " + second + " --ez AUTO " + false});
    }

    public static void sendRecoverTime() {
        sendBroadcast("sk.action.SET_TIME ", new Object[]{"--ez AUTO true"});
    }

    public static void sendFloatSwitch(boolean status) {
        sendBroadcast(status?"sk.action.FLOAT_ON":"sk.action.FLOAT_OFF", new Object[0]);
    }

    public static void sendTimeZone(String timeZone) {
        sendBroadcast("sk.action.SET_TIME ", new Object[]{"--es TIME_ZONE " + timeZone});
    }

    public static void sendTimeZoneRestore() {
        sendBroadcast("sk.action.SET_TIME", new Object[0]);
    }

    public static void sendTimeUse24H(boolean isUse24H) {
        sendBroadcast("sk.action.SET_TIME ", new Object[]{"--ez USE_24H " + isUse24H});
    }

    public static void sendScreenOff(int timeOut) {
        sendBroadcast("sk.action.SCREEN_OFF --ei TIME_OUT " + timeOut, new Object[0]);
    }

    public static void sendScreenRestore() {
        sendBroadcast("sk.action.SCREEN_OFF --ez ALWAYS_ON true", new Object[0]);
    }

    public static void setUtf7Input() {
        exec("ime set com.meizu.scriptkeeper/.services.Utf7ImeService");
    }

    public static void setSystemInput() {
        exec("ime set com.meizu.flyme.input/com.meizu.input.MzInputService");
    }

    public static void inputTap(int x, int y) {
        exec("input tap " + x + " " + y);
    }

    public static void inputSwipe(int startX, int startY, int endX, int endY, int setps) {
        exec("input swipe " + startX + " " + startY + " " + endX + " " + endY + " " + setps);
    }

    public static void inputText(String text) {
        exec("input text " + text);
    }
}
