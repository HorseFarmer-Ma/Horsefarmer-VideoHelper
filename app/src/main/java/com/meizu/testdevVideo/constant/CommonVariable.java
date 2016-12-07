package com.meizu.testdevVideo.constant;

/**
 * Created by maxueming on 2016/4/29.
 */
public class CommonVariable {
    public static String snLabel = "";  // 手机序列号标志位
    public static String packet_choose = "";   // 应用packet选择
    //----------------------------- 关于手机信息 ------------------------------
    public static String about_phone_video_version = "";    // 视频版本号
    public static String about_phone_music_version = "";    // 音乐版本号
    public static String about_phone_ebook_version = "";    // 读书版本号
    public static String about_phone_gallery_version = "";  // 图库版本号
    public static String about_phone_reader_version = "";   // 资讯版本号
    public static String about_phone_vip_version = "";      // 会员版本号
    public static String about_phone_product_name = "";     // 设备名称
    public static String about_phone_internal_model= "";    // 设备内部名称
    public static String about_phone_product_push = "";    // 手机推送机型查询
    public static String about_phone_isLocked = "";     // 机器是否加密
    public static String about_phone_sn = "";               // 设备SN号
    public static String about_phone_imei = "";             // 设备IMEI号
    public static String about_phone_outside_version = "";  // 固件对外版本号
    public static String about_phone_inside_version = "";   // 固件对内版本号
    public static String about_phone_mask_id = "";          // 固件主干ID号
    public static String about_phone_baseband = "";         // 设备基带信息
    public static String about_phone_kernal = "";           // 设备Kernal信息
    public static String about_phone_earphone = "";         // 耳机阻抗
    public static String about_phone_simCardNumber = "";    // SIM卡号码
    public static String about_phone_cpu = "";              // CPU型号
    public static boolean isDataChange = false;     // 监听关于手机信息是否改变
    public static String screen_record_times = "";    // 屏幕录制时间

    // 应用升级
    public static String strVideoId = null;
    public static String strEbookId = null;
    public static String strReaderId = null;
    public static String strMusicId = null;
    public static String strGalleryId = null ;
    public static String strLogReportId = null ;

    // ATS平台WIFI公共账号密码
    public final static String WIFI_USERNAME ="atsms";
    public final static String WIFI_PWD = "autotest123.123";

    // 帧率值
    public static Double Fps = 0.0;

    // 开始抓log指令
    public static String startCatLogBroadcast = "am broadcast -a com.meizu.logreport.adb_cmd --ei action 1 --ei type %d --ez zip false";
    public static String stopCatLogBroadcast = "am broadcast -a com.meizu.logreport.adb_cmd --ei action 0 --ei type %d --ez zip false";

    // mtklog设置抓log大小
    public static String singleLogSizeBroadcast = "am broadcast -a com.mediatek.mtklogger.ADB_CMD -e cmd_name set_log_size_%s --ei cmd_target 7";
    public static String allLogSizeBroadcast = "am broadcast -a com.mediatek.mtklogger.ADB_CMD -e cmd_name set_total_log_size_%s --ei cmd_target 1";

    public static String performsMonkeyCommand = "monkey -p %s --pct-touch 50 --pct-motion 15 --pct-anyevent 5 --pct-majornav 12 --pct-trackball 1 " +
            "--pct-nav 0 --pct-syskeys 15 --pct-appswitch 2 --throttle 250 --ignore-crashes  -s 20 -v 999999999";

    public static String performsCommand = "/system/bin/sh /data/data/com.meizu.testdevVideo/files/uitest/a5/uiautomator runtest jarPath -c className";
    public static boolean isPerformsStart = false;

    public static String mtkLogBroadcast = "am broadcast -a com.mediatek.mtklogger.ADB_CMD -e cmd_name %s --ei cmd_target %d";
}
