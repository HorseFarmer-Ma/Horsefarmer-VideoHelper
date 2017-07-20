package com.meizu.aidl;

interface IU2AutoTestAidl {
    // caseName~案例名；className~测试案例类；steps~步骤；expectation~期望值；result~结果；testTime~测试时间；isSendOrSave~发送或保存参数；fromPage：来源
    void sendReport(String caseName, String className, String steps, String expectation, boolean result, String testTime, String fromPage, String exception);
    // 设置睡眠闹钟唤醒任务
    void setSleepAlarmTask(long alarmTime, boolean isWakeUp);

}
