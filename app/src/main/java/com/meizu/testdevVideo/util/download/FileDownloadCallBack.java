package com.meizu.testdevVideo.util.download;

/**
 * 文件下载回调
 * Created by maxueming on 2017/7/3.
 */

public interface FileDownloadCallBack {
    void start();
    void finish();
    void error(String message);
}
