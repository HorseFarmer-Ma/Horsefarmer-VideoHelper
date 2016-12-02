package com.meizu.testdevVideo.util.download;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;

/**
 * Created by maxueming on 2016/10/22.
 */
public class DownloadHelper {
    private Context mContext;
    public static DownloadHelper mInstance;

    public DownloadHelper(Context context){
        mContext = context;
    }

    public static DownloadHelper getInstance(Context context){
        if(mInstance == null){
            mInstance = new DownloadHelper(context);
        }
        return mInstance;
    }

    public String download(String fileUrl, String path, String DownloadFileName){
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(fileUrl);
        Request request = new Request(uri);
        //设置允许使用的网络类型，这里是移动网络和wifi都可以

        request.setDestinationInExternalPublicDir(path, DownloadFileName);     // 设置下载路径及其文件名
        request.setTitle(DownloadFileName);       // 设置下载标题
        request.setDescription("Downloading..");    // 设置下载时描述
        request.setAllowedNetworkTypes(Request.NETWORK_MOBILE | Request.NETWORK_WIFI);
        request.setVisibleInDownloadsUi(true);
        request.allowScanningByMediaScanner();    // 设置可以被其他应用扫描到
        return String.valueOf(downloadManager.enqueue(request));
    }
}
