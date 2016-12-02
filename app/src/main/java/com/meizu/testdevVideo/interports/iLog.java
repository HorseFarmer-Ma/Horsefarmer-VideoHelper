package com.meizu.testdevVideo.interports;

/**
 * Log相关
 * Created by maxueming on 2016/3/3.
 */
public interface iLog {
    /*------------------------------Log公共静态常量--------------------------------*/
    String LOG_LOCALTION = "/sdcard/SuperTest/LogReport/";  // 保存log位置
    String COMMAND_CLEAR_LOG = "logcat -c";   // 清除log信息指令

    // 参考钟科组“关于手机”的所有抓取log指令，感谢支持！
    String[] COMMAND_ALL_LOG = {"cat /proc/version","cat /proc/cpuinfo", "cat /proc/meminfo",
            "cat /proc/last_kmsg", "cat /proc/reset_reason", "cat /data/anr/traces.txt", "cat /data/ril.log",
            "cat /data/ril_miss.log", "cat /data/ril_sn.log", "cat /cache/recovery/last_install",
            "cat /cache/recovery/last_log", "getprop", "ps", "logcat -v threadtime -d", "logcat -v threadtime -b radio -d",
            "logcat -v threadtime -b events -d", "dmesg", "dumpsys power", "dumpsys alarm", "dumpsys battery", "dumpsys batteryinfo",
            "dumpsys cpuinfo", "dumpsys meminfo", "dumpsys netpolicy", "dumpsys netstats --full --uid", "dumpsys SurfaceFlinger",
            "dumpsys wifi", "dumpsys activity broadcasts", "dumpsys batterystats", "ps -t", "cat /sys/devices/platform/soc-audio.0/reg_program",
            "cat /sys/fs/pstore/console-ramoops", "cat /sys/fs/pstore/dmesg-ramoops-0", "cat /sys/fs/pstore/dmesg-ramoops-1",
            "cat /sys/fs/pstore/ftrace-ramoops", "cat /sys/class/charger_class/charger_device/dump_reg",
            "ping -c 2 61.147.106.32", "ping -c 2 "};
}
