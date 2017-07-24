package com.meizu.testdevVideo.push.android.bean;

import java.util.Map;

/**
 * 上报Monkey任务等
 * Created by maxueming on 2017/5/16.
 */
public class MPushMonkeyBean {
    private String task;
    private String m_meid;
    private String type;
    private Map<String, String> data;

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getM_meid() {
        return m_meid;
    }

    public void setM_meid(String m_meid) {
        this.m_meid = m_meid;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
