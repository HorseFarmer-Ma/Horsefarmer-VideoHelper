package com.meizu.testdevVideo.adapter.data.gridview;

/**
 * Created by maxueming on 2016/9/5.
 */
public class MonkeyWidgetList {
    private String function;
    private boolean btSwitch;

    public void setFunction(String str){
        this.function = str;
    }

    public void setSwitch(boolean str){
        this.btSwitch = str;
    }

    public String getFunction(){
        return this.function;
    }

    public boolean getSwitch(){
        return this.btSwitch;
    }

}
