package com.meizu.testdevVideo.task.beans;

/**
 * 上报测试结果Bean
 * Created by maxueming on 2017/5/11.
 */
public class PerformsPostResultBean {
    private String task;
    private String type;
    private String taskId;
    private String testType;
    private String deviceType;
    private String imei;
    private String testTime;
    private String appType;
    private String appVersion;
    private String systemVersion;
    private String baseBand;
    private String kernel;
    private String pkg;
    private String resultFile;
    private String caseStep;
    private String result;
    private String expectation;
    private String exception;



    public void clear(){
        taskId = null;
        testType = null;
        deviceType = null;
        imei = null;
        testTime = null;
        appType = null;
        appVersion = null;
        systemVersion = null;
        baseBand = null;
        kernel = null;
        pkg = null;
        resultFile = null;
        caseStep = null;
        result = null;
        task = null;
        type = null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExpectation() {
        return expectation;
    }

    public void setExpectation(String expectation) {
        this.expectation = expectation;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTestType() {
        return testType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getImei() {
        return imei;
    }

    public String getTestTime() {
        return testTime;
    }

    public String getAppType() {
        return appType;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public String getBaseBand() {
        return baseBand;
    }

    public String getKernel() {
        return kernel;
    }

    public String getPkg() {
        return pkg;
    }

    public String getResultFile() {
        return resultFile;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setTestTime(String testTime) {
        this.testTime = testTime;
    }


    public void setAppType(String appType) {
        this.appType = appType;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public void setBaseBand(String baseBand) {
        this.baseBand = baseBand;
    }

    public void setKernel(String kernel) {
        this.kernel = kernel;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public void setResultFile(String stepValue) {
        this.resultFile = stepValue;
    }

    public void setCaseStep(String caseStep) {
        this.caseStep = caseStep;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCaseStep() {
        return caseStep;

    }

    public String getResult() {
        return result;
    }
}
