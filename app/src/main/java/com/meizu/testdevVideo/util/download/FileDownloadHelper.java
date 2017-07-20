package com.meizu.testdevVideo.util.download;

import android.util.Log;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

/**
 * 文件下载线程
 * Created by maxueming on 2017/7/3.
 */

public class FileDownloadHelper extends Thread {
    private static final String TAG = "FileDownloadHelper";
    private String downloadUrl;       // 下载链接地址
    private int threadNum;            // 开启的线程数
    private String filePath;          // 保存文件路径地址
    private int blockSize;            // 每一个线程的下载量
    private FileDownloadCallBack fileDownloadCallBack;
    private boolean isFail = false;
    private FileDownloadThread[] threads;

    public FileDownloadHelper(String downloadUrl, int threadNum, String fileptah, FileDownloadCallBack fileDownloadCallBack) {
        this.downloadUrl = downloadUrl;
        this.threadNum = threadNum;
        this.filePath = fileptah;
        this.fileDownloadCallBack = fileDownloadCallBack;
    }

    @Override
    public void run() {
        // 实例化threadNum个对象，进行多线程下载
        fileDownloadCallBack.start();
        threads = new FileDownloadThread[threadNum];
        try {
            URL url = new URL(downloadUrl);
            Log.d(TAG, "download file http path:" + downloadUrl);
            URLConnection conn = url.openConnection();
            // 读取下载文件总大小
            int fileSize = conn.getContentLength();
            if (fileSize <= 0) {
                fileDownloadCallBack.error("无网络或地址错误");
                return;
            }

            // 计算每条线程下载的数据长度
            blockSize = (fileSize % threadNum) == 0 ? fileSize / threadNum
                    : fileSize / threadNum + 1;

            Log.d(TAG, "fileSize:" + fileSize + "  blockSize:");

            File file = new File(filePath);
            for (int i = 0; i < threads.length; i++) {
                // 启动线程，分别下载每个线程需要下载的部分
                threads[i] = new FileDownloadThread(url, file, blockSize,
                        (i + 1));
                threads[i].setName("Thread:" + i);
                threads[i].start();
            }

            boolean isfinished = false;
            int downloadedAllSize = 0;
            while (!isfinished) {
                isfinished = true;
                // 当前所有线程下载总量
                downloadedAllSize = 0;
                for (int i = 0; i < threads.length; i++) {
                    downloadedAllSize += threads[i].getDownloadLength();
                    if (!threads[i].isCompleted()) {
                        isfinished = false;
                    }else{
                        if(isFail = threads[i].isFail()){
                            fileDownloadCallBack.error("Thread download fail");
                            for(int j = 0; j < threads.length; j++){
                                threads[i].interrupt();
                            }
                            isfinished = true;
                            break;
                        }
                    }
                }
                Thread.sleep(1000);// 休息1秒后再读取下载进度
            }

            if(!isFail){
                fileDownloadCallBack.finish();
            }
            Log.d(TAG, " all of downloadSize:" + downloadedAllSize);

        } catch (Exception e) {
            fileDownloadCallBack.error(e.toString());
            e.printStackTrace();
        }

    }

    @Override
    public void interrupt() {
        super.interrupt();
        if(null != threads){
            for (FileDownloadThread thread : threads){
                if(null != thread){
                    thread.interrupt();
                }
            }
        }
    }
}
