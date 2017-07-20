package com.meizu.testdevVideo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.meizu.automation.Expectation;
import com.meizu.automation.Steps;
import com.meizu.testdevVideo.interports.iPublicConstants;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.regex.Pattern;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class TestService extends Service {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        analysisCase(iPublicConstants.LOCAL_MEMORY + "SuperTest/Video.jar");
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 解析案例
     * @param filePath .jar/.apk文件地址
     */
    private void analysisCase(String filePath){
        try {
            Enumeration enumeration = DexFile.loadDex(filePath,
                    File.createTempFile("opt", "dex", getCacheDir()).getPath(), 0).entries();

            while (enumeration.hasMoreElements()){
                String className = (String) enumeration.nextElement();
                Log.d("TestService", "class: " + className);

                if (isTestCase(className)) {
                    Log.d("TestService", "class: " + className);
                    DexClassLoader localDexClassLoader = new DexClassLoader(filePath
                            + File.pathSeparator + "/system/framework/android.test.runner.jar"
                            + File.pathSeparator + "/system/framework/uiautomator.jar", getApplicationInfo().dataDir,
                            null, getClass().getClassLoader());

                    for (Method localMethod : localDexClassLoader.loadClass(className.trim()).getMethods()){
                        if (localMethod.getName().startsWith("test")){
                            Log.d("TestService", "method: " + localMethod.getName());
                            Log.d("TestService", "StepsAnnotation: " + localMethod.getAnnotation(Steps.class).value());
                            Log.d("TestService", "ExpectationAnnotation: " + localMethod.getAnnotation(Expectation.class).value());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static boolean isTestCase(String paramString) {
        boolean bool;
        try {
            bool = Pattern.compile("((com.meizu.+(sanity|test|multilang))(|\\w+))").matcher(paramString.toLowerCase()).matches();
            return bool;
        } catch (Exception ignored) {}
        return false;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
