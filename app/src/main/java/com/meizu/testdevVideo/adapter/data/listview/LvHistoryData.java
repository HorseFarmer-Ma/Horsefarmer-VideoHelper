package com.meizu.testdevVideo.adapter.data.listview;

/**
 * Created by mxm on 2016/9/4.
 */
public class LvHistoryData {

    private int id;
    private String monkeyType;
    private String startTime;
    private String isMute;
    private String isWifiLock;
    private String isFloating;
    private String monkeyCommand;

    public void setId(int str){
        this.id = str;
    }

    public void setMonkeyType(String str){
        this.monkeyType = str;
    }

    public void setStartTime(String str){
        this.startTime = str;
    }

    public void setMute(String str){
        this.isMute = str;
    }

    public void setWifiLock(String str){
        this.isWifiLock = str;
    }

    public void setFloating(String str){
        this.isFloating = str;
    }

    public void setMonkeyCommand(String str){
        this.monkeyCommand = str;
    }

    public int getId(){
        return this.id;
    }

    public String getMonkeyType(){
        return this.monkeyType;
    }

    public String getStartTime(){
        return this.startTime;
    }

    public String getMute(){
        return this.isMute;
    }

    public String getWifiLock(){
        return this.isWifiLock;
    }

    public String getFloating(){
        return this.isFloating;
    }

    public String getMonkeyCommand(){
        return this.monkeyCommand;
    }

}
