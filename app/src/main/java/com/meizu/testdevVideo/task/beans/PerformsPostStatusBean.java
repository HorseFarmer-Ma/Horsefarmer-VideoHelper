package com.meizu.testdevVideo.task.beans;

/**
 * 上报任务状态Bean
 * Created by maxueming on 2017/5/11.
 */
public class PerformsPostStatusBean {

    private String task;
    private String taskType;
    private String taskId;
    private String testType;
    private String status;
    private String allTaskState;


    public void clear(){
        taskId = null;
        testType = null;
        taskType = null;
        status = null;
        allTaskState = null;
    }


    public void setTask(String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAllTaskState(String allTaskState) {
        this.allTaskState = allTaskState;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTestType() {
        return testType;
    }

    public String getStatus() {
        return status;
    }

    public String getAllTaskState() {
        return allTaskState;
    }

}
