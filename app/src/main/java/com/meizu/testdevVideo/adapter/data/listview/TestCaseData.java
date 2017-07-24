package com.meizu.testdevVideo.adapter.data.listview;

/**
 * 测试案例数据保存
 * Created by maxueming on 2016/8/22.
 */
public class TestCaseData {
    public TestCaseData(){}
    private String caseName;          // 执行包名+方法名
    private String testDescrition;    // 测试描述
    private boolean isChoose;         // 判断是否选中

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }

    public void setCaseName(String str) {
        this.caseName = str;
    }

    public String getCaseName() {
        return this.caseName;
    }

    public void setTestDescrition(String str) {
        this.testDescrition = str;
    }

    public String getTestDescrition() {
        return this.testDescrition;
    }

}
