package com.meizu.testdevVideo.task.monkey;

/**
 * 静默安装APK后执行monkey的相关参数
 * Created by maxueming on 2017/5/30.
 */
public class SilenceAppMonkeyInfo {
    private static SilenceAppMonkeyInfo mInstance;
    private String monkeyType;          // monkey类型
    private String updateApkAddress;    // 下载地址
    private String updateApkId;         // 下载应用ID
    private String runtime;             // 执行monkey时间
    private String app;                 // 执行的应用
    private String seed;                // 种子数
    private String times;               // 点击时间间隔
    private String number;              // 事件数量
    private String pkgBlacklist;        // 执行黑名单

    public static synchronized SilenceAppMonkeyInfo getInstance(){
        if(null == mInstance){
            mInstance = new SilenceAppMonkeyInfo();
        }
        return mInstance;
    }

    public void clearParma(){
        monkeyType = null;
        updateApkAddress = null;
        runtime = null;
        app = null;
        seed = null;
        times = null;
        number = null;
        pkgBlacklist = null;
    }


    public String getMonkeyType() {
        return monkeyType;
    }

    public void setMonkeyType(String monkeyType) {
        this.monkeyType = monkeyType;
    }

    public String getUpdateApkAddress() {
        return updateApkAddress;
    }

    public void setUpdateApkAddress(String update_apk_address) {
        this.updateApkAddress = update_apk_address;
    }

    public String getUpdateApkId() {
        return updateApkId;
    }

    public void setUpdateApkId(String update_apk_id) {
        this.updateApkId = update_apk_id;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPkgBlacklist() {
        return pkgBlacklist;
    }

    public void setPkgBlacklist(String pkg_blacklist) {
        this.pkgBlacklist = pkg_blacklist;
    }
}
