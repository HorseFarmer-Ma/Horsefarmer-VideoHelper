package com.meizu.testdevVideo.task.performs;

/**
 * U2自动化测试接口
 * Created by maxueming on 2017/6/12.
 */
public interface U2AutoTestTaskCallBack {
    void runU2Task(String taskJson, int taskId);
    void installApkFinish(String id, boolean isSuccess);
    void alarmFinish();
    void stopTask();

}
