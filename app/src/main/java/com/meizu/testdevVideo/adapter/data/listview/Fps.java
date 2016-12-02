package com.meizu.testdevVideo.adapter.data.listview;

/**
 * 存储Fps相关值
 * Created by maxueming on 2016/8/9.
 */
public class Fps {
    private String strHexFps;   // 上一次的Fps值
    private long time;           // 时间

    public Fps(){
    }

    public void setHexFps(String strHexFps, boolean isUpdateTime){
        this.strHexFps = strHexFps;
        this.time = isUpdateTime? System.currentTimeMillis() : this.time;
    }

    public String getHexFps(){
        return strHexFps;
    }

    public long getTime(){
        return this.time;
    }

    public void setTime(Long time){
        this.time = time;
    }
}
