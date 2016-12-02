package com.meizu.testdevVideo.util.download;

/**
 * 回调接口
 * Created by maxueming on 2016/10/22.
 */
public interface DownloadIdCallback {
    // 返回下载的id值
    void onDownloadListener(String id, String filePath);
}
