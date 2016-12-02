package com.meizu.testdevVideo.interports;

/**
 * Created by maxueming on 2016/10/26.
 */
public interface iPerformsKey {
    String isStart = "isStart";
    String isRegister = "isRegister";                   // 标志设备是否已注册
    String deviceType = "deviceType";                   // 设备名
    String imei = "imei";                               // 手机IMEI
    String testTime = "testTime";                       // 测试时间
    String testType = "testType";                       // 测试类型
    String appType = "appType";                         // 应用类型
    String appVersion = "appVersion";                   // 应用版本
    String systemVersion = "systemVersion";             // 系统版本
    String baseBand = "baseBand";                       // 基带
    String kernel = "kernel";                           // 内核
    String stepValue = "stepValue";                     // 性能测试文件
    String doPackageName = "doPackageName";             // 后台配置，执行案例
    String stepValueFileName = "stepValueFileName";     // 性能测试文件名
    String stepValueFilePath = "stepValueFilePath";     // 性能测试文件名
    String packageName = "pkg";                         // 包名，由脚本传过来
    String caseName = "caseName";                       // 案例名，由脚本传过来执行了哪条案例
    String result = "result";                           // 案例执行结果，由脚本传过来

    String ServiceTaskJson = "serviceTaskJson";         // 服务端任务Json数据
}
