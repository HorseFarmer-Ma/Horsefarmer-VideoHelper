package com.meizu.testdevVideo.constant;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by maxueming on 2016/12/2.
 */
public class Constants {
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static class UpdateAppValue {
        // 全局下载记录值
        public static ArrayList<String> appUpdateStringlist = new ArrayList<String>();
    }

    // 时间常量
    public class TIME {
        public static final int MINUTE = 60 * 1000;         // 1分钟
        public static final int SECOND = 1000;              // 1秒

        // 转换相关
        public static final int HOURS_OF_DAY = 24;          // 1天24小时
        public static final int MINUTES_OF_HOUR = 60;       // 1小时60分钟
        public static final int SECONDS_OF_MINUTE = 60;      // 1分钟60秒
        public static final int MILLS_OF_SECOND = 1000;      // 1秒1000毫秒
    }

    // Monkey指令相关
    public class MonkeyCommand {
        // 单个应用monkey, 默认seed = 500, number = 1200000000
        public static final String MTK_APP_MONKEY = "monkey -s %s -p %s"
                + " --ignore-crashes --ignore-timeouts --kill-process"
                + "-after-error --ignore-security-exceptions --pct-trackball "
                + "0 --pct-nav 0 -v -v -v --throttle %s %s";

        public static final String SAMSUNG_APP_MONKEY = "monkey -p %s" +
                " --ignore-crashes --ignore-timeouts --ignore" +
                "-security-exceptions --kill-process-after-error " +
                "--pct-trackball 0 --pct-nav 0 --pct-majornav 0 " +
                "--pct-anyevent 0 -v -v -v --throttle %s %s";

        // 系统应用monkey
        public static final String MTK_SYSTEM_MONKEY = "monkey -s 1000 --pkg-blacklist-file /sdcard/blacklist.txt " +
                "--ignore-crashes --ignore-timeouts --kill-process-after-error " +
                "--ignore-security-exceptions --pct-trackball 0 --pct-nav 0 -v -v " +
                "-v --throttle 500 1200000000";

        public static final String SAMSUNG_SYSTEM_MONKEY = "monkey --pkg-blacklist-file " +
                "/sdcard/blacklist.txt --ignore-crashes --ignore-timeouts --ignore" +
                "-security-exceptions --kill-process-after-error --pct-trackball" +
                " 0 --pct-nav 0 --pct-majornav 0 --pct-anyevent 0 -v -v -v " +
                "--throttle 500 1200000000";
    }

    // 智能遍历测试
    public class SmartTest {
        public static final String SMART_COMMAND_BROADCAST = "{\"moduleId\":%s,\"msgType\":%s," +
                "\"taskId\":-%s,\"debug\":%s,\"smartConfInfo\":[{\"maxPageDepth\":%s," +
                "\"maxActivityTime\":%s,\"eventType\":[%s],\"accoutName\":%s,\"password\":%s," +
                "\"isNeedScreenshot\":%s,\"isWeightByArea\":%s}],\"appInfo\":[%s]," +
                "\"operate\":[{\"type\":%s,\"time\":%s,\"sloopTaskId\":%s,\"delay\":%s,\"ignoreException\":%s}] }";
    }

    // Monkey执行相关
    public static final class Monkey {
        // 数据库遗留
        public static final String START_TIME = "startTime";
        public static final String IS_WIFILOCK = "isWifiLock";
        public static final String IS_MUTE = "isMute";
        public static final String IS_FLOATING = "isFloating";

        public static final String TABLE_NAME = "MonkeyHistory";
        public static final String MONKEY_TYPE = "monkey_type";
        public static final String MONKEY_COMMAND = "monkey_command";
        public static final String MONKEY_RUN_TIME = "monkey_run_time";
        public static final String MONKEY_START_TIME = "monkey_start_time";
        public static final String MONKEY_LOG_ADDRESS = "monkey_log_address";
        public static final String MONKEY_LOG_RESULT_ADDRESS = "monkey_log_result_address";

        // 手机跑monkey需要的剩余内存量
        public static final float MONKEY_PHONE_SIZE_NEED = 2;

        /* -------------------- Monkey执行数据相关 -----------------------*/
        // MONKEY是否开始
        public static final String IS_START = "isStart";
        // MONKEY执行类型
        public static final String ALARM_MONKEY_TYPE = "alarm_monkey_type";
        // MONKEY定时开始时间
        public static final String MONKEY_ALARM_START_TIME = "monkey_alarm_start_time";
        // MONKEY定时执行时长
        public static final String ALARM_MONKEY_RUN_TIME = "alarm_monkey_run_time";
        // MONKEY定时指令
        public static final String ALARM_MONKEY_COMMAND = "alarm_monkey_command";
        // MONKEY_ID
        public static final String MONKEY_ID = "monkey_id";
        // 杀掉monkey的广播
        public static final String ACTION_KILL_MONKEY = "st.action.kill.monkey.service";
        // 定时任务的广播
        public static final String ACTION_SET_MONKEY_RUN_REPEAT_TASK = "st.action.set.monkey.task";
        // 静默安装APK并执行Monkey
        public static final String ACTION_SILENCE_INSTALL_APK = "st.action.silence.install.apk";
        // 应用级别Monkey
        public static final String SINGLE_MONKEY = "1";
        // 系统级别Monkey
        public static final String SYSTEM_MONKEY = "2";
        // Monkey动作类型
        public static final String MONKEY_ACTION = "monkey_aciton";
        // 动作类型：跑Monkey并上传报告
        public static final int LABEL_OF_ACTION_MONKEY_REPORT = 1;
        // 动作类型：仅跑Monkey
        public static final int LABEL_OF_ACTION_JUST_RUN_MONKEY = 2;

    }

    public static final class Push {
        // MPush接口
        public static final String ALLOC_SERVER_ADDRESS = "http://172.16.177.67:9999";
    }

    public static final class MpushTaskLabel {
        // 启动性能测试任务  对应上传测试报告
        public static final String START_PERFORMS_TEST = "1";
        // 停止性能测试任务
        public static final String STOP_PERFORMS_TEST = "2";
        // 查看设备繁忙
        public static final String CHECK_PHONE_STATU = "3";
        // 检查设备IMEI
        public static final String CHECK_PHONE_IMEI = "4";
        // 上报测试状态，正在进行，已完成，失败
        public static final String POST_TASK_RUNNING_STATU = "5";
        // 上报monkey任务或杀monkey任务
        public static final String POST_MONKEY_TASK_AND_KILL_TASK = "6";
        // 服务端下发monkey任务ID
        public static final String GET_MONKEY_TASK_ID = "7";
        // 静默安装后跑monkey任务
        public static final String INSTALL_APP_THEN_RUN_MONKEY = "8";
    }

    public static final class MpushBindUser {
        // 绑定状态key名
        public static final String BIND_STATUS = "bindStatus";
        // 无状态
        public static final int NO_STATUS = 0;
        // 绑定成功
        public static final int PASS = 1;
        // 绑定失败
        public static final int FAIL = 2;

    }

    public static final class HttpCode {
        // 成功
        public static final int NORMAL = 200;
        // 无网络
        public static final int NONET = 1000;
        // 请求超时
        public static final int REQUEST_TIME_OUT = 504;
        // 服务不可用
        public static final int SERVICE_UN_USER = 503;
    }

    // 业务更新相关
    public static final class AppUpdate {
        // Http下载地址
        public static final String DOWNLOAD_ADRESS = "http://ats.meizu.com/static/upload/user-resources/SuperTest/MediaAppUpdate/";
    }

    public static final class U2TaskDB {
        public static final String TABLE_NAME = "u2Task";
        // 任务ID
        public static final String TASK_ID_STR = "taskId";
        // 测试类型：启动（1），帧率（2），内存（3），纯净（4）
        public static final String PERFORMS_TYPE_INT = "performsType";
        // 测试案例名
        public static final String CASE_NAME_STR = "caseName";
        // 测试案例
        public static final String CASE_STEP_STR = "caseStep";
        // 案例执行状态
        public static final String STATUS_INT = "status";
        // 测试结果
        public static final String RESULT_BOOLEAN = "result";
        // 测试结果文件
        public static final String RESULT_FILE_STR = "resultFile";
        // 异常
        public static final String EXCEPTION_STR = "exception";
    }

    public static final class U2TaskConstants {
        // 任务状态
        public static final int STATUS_TYPE_NO_STATU = 0;
        public static final int STATUS_TYPE_RUNNING = 1;
        public static final int STATUS_TYPE_FINISH = 2;
        public static final String PERFORMS_TYPE_STARTTIME = "1";
        public static final String PERFORMS_TYPE_FRAGMENT = "2";
        public static final String PERFORMS_TYPE_MEMORY = "3";
        public static final String PERFORMS_TYPE_PURE = "4";

        public static final String U2_TASK_BROADCAST_ACTION = "st.action.u2.task.ready";
        public static final String U2_TASK_APK_INSTALL_SUCCESS_BROADCAST_ACTION = "st.action.u2.task.apk.Install.ready";
        public static final String U2_TASK_STOP_TASK= "st.action.u2.task.stop";
        public static final String U2_TASK_ALARM_TIME_FINISH = "android.intent.action.ALARM_RECEIVER";
        public static final String U2_TASK_TASKJSON = "taskJson";
        // U2任务ID
        public static final String U2_TASK_TASKID = "task_id";
        public static final String U2_TASK_INSTALLED_APK_ID = "id";
        public static final String U2_TASK_TESTTYPE = "testType";
        public static final String U2_TASK_INSTALLED_APK_PASS_OR_FAIL = "isSuccess";
        // 请求ID
        public static final String U2_TASK_TYPE_OF_GET_ID_REQUEST = "1";
        // 获取ID
        public static final String U2_TASK_TYPE_OF_GET_ID = "2";
        // 接受任务
        public static final String U2_TASK_TYPE_OF_U2_TASK = "3";
        // 发送报告
        public static final String U2_TASK_TYPE_OF_SEND_REPORT = "4";



        // U2指令
        public static final String U2_TASK_COMMAND = "am instrument -w -r   -e debug false -e " +
                "class %s com.meizu.testdev.multimedia.test/com.meizu.u2.runner.BaseRunner";

    }
}
