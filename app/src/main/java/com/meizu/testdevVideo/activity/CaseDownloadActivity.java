package com.meizu.testdevVideo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.util.download.FileDownloadThread;
import com.meizu.testdevVideo.library.ToastHelper;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class CaseDownloadActivity extends Activity {

    private Button button_download_case;
    private Button button_cancel_download;
    private EditText edit_case;
    private EditText case_detail;
    /** 显示下载进度TextView */
    private TextView mMessageView;
    /** 显示下载进度ProgressBar */
    private ProgressBar mProgressbar;
    private LinearLayout download;
    private LinearLayout add_case_download;

    private static final String TAG = CaseDownloadActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String extra_message = getIntent().getExtras().getString("case_Detail");

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  //设置窗口显示模式为窗口方式
        setFinishOnTouchOutside(false);   // 取消点击外部区域消失
        setContentView(R.layout.activity_download);
        button_download_case = (Button) findViewById(R.id.button_download_case);
        button_cancel_download = (Button) findViewById(R.id.button_cancel_download);
        mMessageView = (TextView) findViewById(R.id.mMessageView);
        mProgressbar = (ProgressBar) findViewById(R.id.mProgressbar);
        edit_case = (EditText) findViewById(R.id.edit_case);
        case_detail = (EditText) findViewById(R.id.case_detail);
        download = (LinearLayout) findViewById(R.id.download);
        add_case_download = (LinearLayout) findViewById(R.id.add_case_download);

        download.setVisibility(View.GONE);   // 隐藏下载栏

        if(!extra_message.equals("")){
            edit_case.setText(extra_message);
        }

        button_cancel_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        button_download_case.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String download_case = edit_case.getText().toString();
                String download_detail = case_detail.getText().toString();
                ToastHelper.addToast(download_case, CaseDownloadActivity.this);
                System.out.print(download_case);
                if(download_case.equals("")){
                    ToastHelper.addToast("请输入JAR包名", CaseDownloadActivity.this);   // 输入框为空，提示输入案例名
                }else{
                    if(button_download_case.getText().equals("下载")){
                        add_case_download.setVisibility(View.GONE);
                        download.setVisibility(View.VISIBLE);   // 显示下载栏
                        doDownload("http://ats.meizu.com/static/upload/" +
                                        "user-resources/SuperTest/Case/" + download_case + ".jar",
                                        download_case + ".jar", 5);    // 开启5条线程
                        button_download_case.setText("完成");
                        button_download_case.setEnabled(false);
                        button_cancel_download.setEnabled(false);    // 不允许点击取消按钮
                    }else if(button_download_case.getText().equals("完成")){
                        if(download_detail.equals("")){
                            download_detail = "无";
                        }
                        Intent intent = new Intent(CaseDownloadActivity.this, CommonToolsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("caseName", download_case);
                        bundle.putString("caseDetail", download_detail);
                        intent.putExtras(bundle);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }
        });

    }

    /**
     * 使用Handler更新UI界面信息
     */
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            mProgressbar.setProgress(msg.getData().getInt("size"));

            float temp = (float) mProgressbar.getProgress()
                    / (float) mProgressbar.getMax();

            int progress = (int) (temp * 100);
            if (progress == 100) {
                button_download_case.setEnabled(true);   // 允许点击完成按钮
                ToastHelper.addToast("下载完成！", CaseDownloadActivity.this);
            }
            mMessageView.setText(progress + " %");
        }
    };

    /**
     * 下载准备工作，获取SD卡路径、开启线程
     * @param downloadUrl: 下载网址
     * @param fileName: 下载保存文件名
     * @param threadNum: 下载线程数
     */
    private void doDownload(String downloadUrl, String fileName, int threadNum) {
        // 获取SD卡路径
        String path = Environment.getExternalStorageDirectory()
                + "/SuperTest/JarDownload/";
        File file = new File(path);
        // 如果SD卡目录不存在创建
        if (!file.exists()) {
            file.mkdir();
        }
        // 设置progressBar初始化
        mProgressbar.setProgress(0);
        String filepath = path + fileName;
        Log.d(TAG, "Download file  path:" + filepath);
        downloadTask task = new downloadTask(downloadUrl, threadNum, filepath);
        task.start();
    }

    /**
     * 多线程文件下载
     */
    class downloadTask extends Thread {
        private String downloadUrl;// 下载链接地址
        private int threadNum;// 开启的线程数
        private String filePath;// 保存文件路径地址
        private int blockSize;// 每一个线程的下载量

        public downloadTask(String downloadUrl, int threadNum, String fileptah) {
            this.downloadUrl = downloadUrl;
            this.threadNum = threadNum;
            this.filePath = fileptah;
        }

        @Override
        public void run() {
            // 实例化threadNum个对象，进行多线程下载
            FileDownloadThread[] threads = new FileDownloadThread[threadNum];
            try {
                URL url = new URL(downloadUrl);
                Log.d(TAG, "download file http path:" + downloadUrl);
                URLConnection conn = url.openConnection();
                // 读取下载文件总大小
                int fileSize = conn.getContentLength();
                if (fileSize <= 0) {
                    return;
                }
                // 设置ProgressBar最大的长度为文件Size
                mProgressbar.setMax(fileSize);

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
                        }
                    }
                    // 通知handler去更新视图组件
                    Message msg = new Message();
                    msg.getData().putInt("size", downloadedAllSize);
                    mHandler.sendMessage(msg);
                    // Log.d(TAG, "current downloadSize:" + downloadedAllSize);
                    Thread.sleep(1000);// 休息1秒后再读取下载进度
                }
                Log.d(TAG, " all of downloadSize:" + downloadedAllSize);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
