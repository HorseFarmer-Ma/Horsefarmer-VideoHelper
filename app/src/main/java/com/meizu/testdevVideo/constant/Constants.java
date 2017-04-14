package com.meizu.testdevVideo.constant;

/**
 * Created by maxueming on 2016/12/2.
 */
public class Constants {
    // 时间常量
    public class TIME{
        public static final int MINUTE = 60 * 1000;         // 1分钟
        public static final int SECOND = 1000;              // 1秒
    }

    public class MonkeyCommand{
        // 单个应用monkey, 默认%ts = 500, %ns = 1200000000
        public static final String MTK_APP_MONKEY = "monkey -s %ss -p %ps"
                + " --ignore-crashes --ignore-timeouts --kill-process"
                + "-after-error --ignore-security-exceptions --pct-trackball "
                + "0 --pct-nav 0 -v -v -v --throttle %ts %ns";

        public static final String SAMSUNG_APP_MONKEY = "monkey -p %ps" +
                " --ignore-crashes --ignore-timeouts --ignore" +
                "-security-exceptions --kill-process-after-error " +
                "--pct-trackball 0 --pct-nav 0 --pct-majornav 0 " +
                "--pct-anyevent 0 -v -v -v --throttle %ts %ns";

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

    public static final class Monkey{
        public static final String TABLE_NAME = "MonkeyHistory";
        public static final String MONKEY_TYPE = "monkey_type";
        public static final String MONKEY_COMMAND = "monkey_command";
        public static final String START_TIME = "startTime";
        public static final String IS_MUTE = "isMute";
        public static final String IS_WIFILOCK = "isWifiLock";
        public static final String IS_FLOATING = "isFloating";
        public static final String MONKEY_LOG_ADDRESS = "monkey_log_address";
        public static final String MONKEY_LOG_RESULT_ADDRESS = "monkey_log_result_address";
        public static final float MONKEY_PHONE_SIZE_NEED = 2;   // 手机跑monkey需要的剩余内存量


    }

}
