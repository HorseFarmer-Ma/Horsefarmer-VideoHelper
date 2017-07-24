package com.meizu.testdevVideo.db.bean;

import com.meizu.testdevVideo.constant.Constants;

/**
 * U2自动化函数bean
 * Created by maxueming on 2017/6/9.
 */
public class U2TaskBean {
    private String taskId = Constants.U2TaskDB.TASK_ID_STR;
    private String performsType = Constants.U2TaskDB.PERFORMS_TYPE_INT;
    private String caseStep = Constants.U2TaskDB.CASE_STEP_STR;
    private String caseName = Constants.U2TaskDB.CASE_NAME_STR;
    private String status = Constants.U2TaskDB.STATUS_INT;
    private String result = Constants.U2TaskDB.RESULT_BOOLEAN;
    private String resultFile = Constants.U2TaskDB.RESULT_FILE_STR;
    private String exception = Constants.U2TaskDB.EXCEPTION_STR;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getPerformsType() {
        return performsType;
    }

    public void setPerformsType(String performsType) {
        this.performsType = performsType;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultFile() {
        return resultFile;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getCaseStep() {
        return caseStep;
    }

    public void setCaseStep(String caseStep) {
        this.caseStep = caseStep;
    }
}
