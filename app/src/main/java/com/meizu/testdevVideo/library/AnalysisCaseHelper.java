package com.meizu.testdevVideo.library;

import android.content.Context;

import com.meizu.automation.Expectation;
import com.meizu.automation.Steps;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

/**
 * 解析JAR/APK中类名+方法+注释，采用DexFile和反射原理
 * Created by maxueming on 2017/3/24.
 */
public class AnalysisCaseHelper {

    private Context context = null;
    private String filePath = null;                 // JAR/APK路径
    private String type = null;                     // 分析的包类型
    private ArrayList<String> clazz = null;
    private Enumeration enumeration = null;
    private DexClassLoader localDexClassLoader = null;

    // DexClassLoader依赖JAR
    private static final String TEST_RUNNER_JAR = "/system/framework/android.test.runner.jar";
    private static final String UIAUTOMATOR_JAR = "/system/framework/uiautomator.jar";


    public class AnalysisType{
        public static final String U1_JAR = "U1_JAR";   // Uiautomator JAR
        public static final String JAR = "JAR";         // 普通JAR
        public static final String APK = "APK";         // APK
    }

    public AnalysisCaseHelper(Context context, String filePath, String type){
        this.context = context;
        this.filePath = filePath;
        this.type = type;
    }

    /**
     * @return JAR/APK包中元素内容
     */
    private synchronized Enumeration getEnumeration(){
        if(null != enumeration){
            return enumeration;
        }
        try {
            enumeration = DexFile.loadDex(this.filePath,
                    File.createTempFile("opt", "dex", this.context.getCacheDir()).getPath(), 0).entries();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return enumeration;
    }

    /**
     * @return 返回类名列表
     */
    public ArrayList<String> getJarOrApkClass(){
        clazz = new ArrayList<String>();
        getEnumeration();
        while (enumeration.hasMoreElements()){
            String className = (String) enumeration.nextElement();
            clazz.add(className);
        }

        return clazz;
    }

    /**
     * @return 返回测试类名列表
     */
    public ArrayList<String> getJarOrApkTestClass(){
        clazz = new ArrayList<String>();
        getEnumeration();
        while (enumeration.hasMoreElements()){
            String className = (String) enumeration.nextElement();
            if(isTestCase(className)){
                clazz.add(className);
            }
        }

        return clazz;
    }

    /**
     * @return 返回DexClassLoader
     */
    private synchronized DexClassLoader getDexClassLoader(){
        if(null != localDexClassLoader){
            return localDexClassLoader;
        }
        if(type.equals(AnalysisType.U1_JAR)){
            localDexClassLoader = new DexClassLoader(filePath
                    + File.pathSeparator + TEST_RUNNER_JAR + File.pathSeparator
                    + UIAUTOMATOR_JAR, context.getApplicationInfo().dataDir,
                    null, context.getClass().getClassLoader());
        }else if(type.equals(AnalysisType.APK)){
            localDexClassLoader = new DexClassLoader(filePath, context.getApplicationInfo().dataDir,
                    null, context.getClass().getClassLoader());
        }else if(type.equals(AnalysisType.JAR)){
            localDexClassLoader = new DexClassLoader(filePath, context.getApplicationInfo().dataDir,
                    null, context.getClass().getClassLoader());
        }else{
            return null;
        }
        return localDexClassLoader;
    }

    /**
     * @param className 类名
     * @return 方法名
     */
    public ArrayList<String> getJarOrApkTestMethod(String className){
        ArrayList<String> methodName = new ArrayList<String>();
        getDexClassLoader();
        try {
            for (Method localMethod : localDexClassLoader.loadClass(className.trim()).getMethods()){
                if (localMethod.getName().startsWith("test")){
                    methodName.add(localMethod.getName());
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return methodName;
    }

    /**
     * @param className 类名
     * @return 测试步骤
     */
    public ArrayList<String> getJarOrApkTestStep(String className){
        ArrayList<String> step = new ArrayList<String>();
        getDexClassLoader();
        try {
            for (Method localMethod : localDexClassLoader.loadClass(className.trim()).getMethods()){
                if (localMethod.getName().startsWith("test")){
                    step.add(localMethod.getAnnotation(Steps.class).value());
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return step;
    }

    /**
     * @param className 类名
     * @return 期望结果
     */
    public ArrayList<String> getJarOrApkTestExpectation(String className){
        ArrayList<String> expectation = new ArrayList<String>();
        getDexClassLoader();
        try {
            for (Method localMethod : localDexClassLoader.loadClass(className.trim()).getMethods()){
                if (localMethod.getName().startsWith("test")){
                    expectation.add(localMethod.getAnnotation(Expectation.class).value());
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return expectation;
    }

    /**
     * @param className 类名
     * @return 方法名
     */
    public ArrayList<String> getJarOrApkMethod(String className){
        ArrayList<String> method = new ArrayList<String>();
        getDexClassLoader();
        try {
            for (Method localMethod : localDexClassLoader.loadClass(className.trim()).getMethods()){
                method.add(localMethod.getName());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return method;
    }

    /**
     * 判定是否为测试案例
     * @param paramString 案例名
     * @return 结果
     */
    private boolean isTestCase(String paramString) {
        boolean bool;
        try {
            bool = Pattern.compile("((com.meizu.+(sanity|test|multilang))(|\\w+))").matcher(paramString.toLowerCase()).matches();
            return bool;
        } catch (Exception ignored) {}
        return false;
    }

}
