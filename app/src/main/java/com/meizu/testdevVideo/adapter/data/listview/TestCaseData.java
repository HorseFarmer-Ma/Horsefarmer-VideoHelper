package com.meizu.testdevVideo.adapter.data.listview;

/**
 * 测试案例数据保存
 * Created by maxueming on 2016/8/22.
 */
public class TestCaseData {
    public TestCaseData(){}

    private String testType;          // 测试类型
    private String caseName;          // 执行包名+方法名
    private String testAppType;       // 测试应用
    private String testAppVersion;    // 测试应用版本号
    private String testDescrition;    // 测试描述


    public void setCaseName(String str) {
        this.caseName = str;
    }

    public String getCaseName() {
        return this.caseName;
    }

    public void setTestType(String str) {
        this.testType = str;
    }

    public String getTestType() {
        return this.testType;
    }

    public void setTestAppType(String str) {
        this.testAppType = str;
    }

    public String getTestAppType() {
        return this.testAppType;
    }

    public void setTestAppVersion(String str) {
        this.testAppVersion = str;
    }

    public String getTestAppVersion() {
        return this.testAppVersion;
    }

    public void setTestDescrition(String str) {
        this.testDescrition = str;
    }

    public String getTestDescrition() {
        return this.testDescrition;
    }

}
