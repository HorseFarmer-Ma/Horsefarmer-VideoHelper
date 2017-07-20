package com.meizu.testdevVideo.push.android.bean;

/**
 * MPush上报
 * Created by maxueming on 2017/4/27.
 */
public class MPushBindUserBean {
    private String taskType;   // 任务类型
    private String appType;
    private String Imei;
    private String module;
    private String status;
    private String reason;
    private String task;
    private String email;
    private String version;   // ST版本号
    private String packageName;



    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;

    }

    public String getReason() {
        return reason;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getModule() {
        return module;
    }

    public void clear(){
        taskType = null;
        appType = null;
        Imei = null;
        module = null;
        status = null;
        reason = null;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getImei() {
        return Imei;
    }

    public void setImei(String imei) {
        Imei = imei;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
