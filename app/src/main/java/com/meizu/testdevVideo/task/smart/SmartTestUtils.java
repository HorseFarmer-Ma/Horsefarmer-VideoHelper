package com.meizu.testdevVideo.task.smart;

import com.meizu.testdevVideo.constant.Constants;

/**
 * 智能遍历操作类
 * Created by maxueming on 2017/6/2.
 */
public class SmartTestUtils {

    /**
     * 返回智能遍历的json串
     * moduleId: 模块id  (42 表示智能遍历)
     * msgType: 任务类型 (1 ：启动任务，3：停止任务)
     * taskId: 任务id (本地启动，均为-1)
     * debug: 是否为debug模式（开启后会打印智能遍历工具本身的日志）
     * appInfo：测试应用包名，可填写多个
     * maxPageDepth：最大页面深度
     * maxActivityTime：单个Activity最大测试时间(min)
     * eventType：支持事件操作类型，包括点击、滑动、长按等
     * accoutName：应用登录账号
     * password：应用登录密码
     * isNeedScreenshot：是否开启截图功能
     * isWeightByArea：是否按面积加权，权重大的优先遍历
     * packageWhiteList：应用白名单，白名单中的应用允许跨包运行
     * activityBlackList：页面黑名单，黑名单中的Activity将会被拦截启动
     * type ：智能遍历类型（0 启发式智能遍历 1 控件随机遍历 2 原生Monkey）
     * sloopTaskId: 子任务id (本地启动，均为-1)
     * time：测试时间
     * delay：操作延时
     * ignoreException ：是否忽略异常
     * @return 执行的json字符串
     */
    public static String getSmartCommand(boolean isStartTest, String appInfo, String type, String time){

        // 默认参数定义
        String moduleId = "42";
        String msgType = isStartTest? "1" : "3";
        String taskId = "-1";
        String debug = "false";
        String maxPageDepth = "9999";
        String maxActivityTime = "9999";
        String eventType = "0, 1, 2";
        String accoutName = "";
        String password = "";
        String isNeedScreenshot = "false";
        String isWeightByArea = "true";
        String sloopTaskId = "-1";
        String delay = "200";
        String ignoreException = "false";

        return String.format(Constants.SmartTest.SMART_COMMAND_BROADCAST, moduleId, msgType, taskId,
                debug, maxPageDepth, maxActivityTime, eventType, accoutName, password, isNeedScreenshot,
                isWeightByArea, appInfo, type, time, sloopTaskId, delay, ignoreException);
    }
}
