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
                + "0 --pct-nav 0 -v -v -v --throttle %ts %ns"
                + " > /sdcard/monkeytest.log 2>&1 &";

        public static final String SAMSUNG_APP_MONKEY = "monkey -p %ps" +
                " --ignore-crashes --ignore-timeouts --ignore" +
                "-security-exceptions --kill-process-after-error " +
                "--pct-trackball 0 --pct-nav 0 --pct-majornav 0 " +
                "--pct-anyevent 0 -v -v -v --throttle %ts " +
                "%ns > /sdcard/monkeytest.log 2>&1 &";

        // 系统应用monkey
        public static final String MTK_SYSTEM_MONKEY = "monkey -s 1000 --pkg-blacklist-file /sdcard/blacklist.txt " +
                "--ignore-crashes --ignore-timeouts --kill-process-after-error " +
                "--ignore-security-exceptions --pct-trackball 0 --pct-nav 0 -v -v " +
                "-v --throttle 500 1200000000 > /sdcard/monkeytest.log 2>&1 &";

        public static final String SAMSUNG_SYSTEM_MONKEY = "monkey --pkg-blacklist-file " +
                "/sdcard/blacklist.txt --ignore-crashes --ignore-timeouts --ignore" +
                "-security-exceptions --kill-process-after-error --pct-trackball" +
                " 0 --pct-nav 0 --pct-majornav 0 --pct-anyevent 0 -v -v -v " +
                "--throttle 500 1200000000 > /sdcard/monkeytest.log 2>&1 &";
    }

}
