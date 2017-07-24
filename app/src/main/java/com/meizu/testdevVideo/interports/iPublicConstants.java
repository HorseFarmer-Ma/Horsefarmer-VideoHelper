package com.meizu.testdevVideo.interports;

import android.os.Environment;

/**
 * 公共常量
 * Created by maxueming on 2016/4/28.
 */
public interface iPublicConstants {
    String PACKET_VIDEO = "com.meizu.media.video";
    String PACKET_MUSIC = "com.meizu.media.music";
    String PACKET_EBOOK = "com.meizu.media.ebook";
    String PACKET_GALLERY = "com.meizu.media.gallery";
    String PACKET_READER = "com.meizu.media.reader";
    String PACKET_COMPAIGN = "com.meizu.compaign";
    String PACKET_CLOUD = "com.meizu.cloud";
    String PACKET_THEME = "com.meizu.customizecenter";
    String PACKET_BROWSER = "com.android.browser";
    String LOCAL_MEMORY = Environment.getExternalStorageDirectory()
            .toString() + "/";
    String MEMORY_BACK_UP = Environment.getExternalStorageDirectory()
            .toString() + "/SuperTest/Backup/";

    // 性能测试相关
    String PERFORMS_LOG = LOCAL_MEMORY + "SuperTest/ApkLog/";
    // 测试JAR包存放路径
    String PERFORMS_TESTCASE_PATH = LOCAL_MEMORY + "SuperTest/PerformsTest/TestCase/";
    // 下载的JAR包名
    String PERFORMS_JAR_NAME = "multimedia.jar";    // 下载的jar包名
//    String PERFORMS_JAR_NAME = "multimedia.jar";    // 下载的jar包名
    // 测试结果文件存放路径
    String PERFORMS_RESULT = LOCAL_MEMORY + "SuperTest/PerformsTest/Result/";
    // 帧率测试结果文件存放路径
    String PERFORMS_FPS_RESULT = LOCAL_MEMORY + "SuperTest/PerformsTest/Result/framerate/";
    // 内存测试结果文件存放路径
    String PERFORMS_MEMORY_RESULT = LOCAL_MEMORY + "SuperTest/PerformsTest/Result/memory/";
    // 纯净后台测试结果文件存放路径
    String PERFORMS_PURE_BACKGROUND_RESULT = LOCAL_MEMORY + "Android/log/";
    // 启动时间测试结果文件存放路径
    String PERFORMS_TIME_RESULT = LOCAL_MEMORY + "SuperTest/PerformsTest/Result/starttime/";



    /** -------------------------------------- 性能测试接口定义 -------------------------------------------------*/

    /**
     * 上报测试结果接口
     */
    String PERFORMS_POST_TESTRESULT_URL = "http://multimedia.meizu.com/TR_PerformAnalysis_DeviceIFCtrl/Performance.do";

    /**
     * 拉取测试列表接口,POST
     */
    String PPERFORMS_PULL_TESTCASE_URL = "http://multimedia.meizu.com/autotest/taskdispatch/apk_analysis";

    /**
     * 下载Jar包网址
     */
    String PERFORMS_TESTCASE_DOWNLOAD_URL = "http://172.17.53.51/static/upload/user-resources/SuperTest/Jar/multimedia_mt.jar";
    /**
     * 本地上传ALIAS和TAG和注册ID
     */
    String PERFORMS_POST_ID_TAG_ALIAS_URL = "http://multimedia.meizu.com/DevicePoolIFCtrl/addDevicePool.do";

    /**
     * 上报任务状态接口
     */
    String PERFORMS_POST_TASK_STATUS_URL = "http://multimedia.meizu.com/SubTaskIFCtrl/SubTask.do";

    /**
     * 上报monkey日志接口
     */
    String MONKEY_RESULT_POST_URL = "http://ats.meizu.com/other/log/upload";

    /**
     * 拉取APK更新接口
     */
    String LOADING_APP_UPDATE = "http://multimedia.meizu.com/autotest/taskdispatch/apk_update";




    /** -------------------------------------- OTA推送接口定义 -------------------------------------------------*/

    /**
     * #一键入库
     */
    String OTA_SIP_DB_URL = "http://172.16.177.99/ota/flyme/cgi_test_env/auth";
    /**
     * #检测在线
     */
    String OTA_SIP_ONLINE_URL = "http://172.16.177.99/ota/flyme/cgi_test_env/check_online";
    /**
     * #检测订阅
     */
    String OTA_SIP_SUB_URL = "http://172.16.177.99/ota/flyme/cgi_test_env/check_sub";


    /**
     * #任务生效/失效 POST #状态1为生效,2为失效
     */
    String OTA_TASK_ChangeStatus_URL = "http://172.16.177.99/ota/uPush/customApp/changeStatus";







    /**----------------------------- FTP服务器 ---------------------------------*/
    String USERNAME = "SuperTest";
    String PASSWORD = "mxm##mxm666";
    String HOST = "172.17.132.85";
    int PORT = 2121;
    String FILENAME = "FileName";
}
