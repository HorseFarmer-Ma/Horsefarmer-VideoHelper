package com.meizu.testdevVideo.util.update;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.util.Xml;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.download.DownloadHelper;
import com.meizu.testdevVideo.util.download.DownloadIdCallback;
import com.meizu.testdevVideo.util.download.DownloadReceiver;
import com.meizu.testdevVideo.util.download.SoftUpdateCallBack;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 软件更新相关
 * Created by mxm on 2016/7/2.
 */
public class SoftwareUpdate {
    private Context mContext;
    private String downloadId;       // 下载文件id
    private UpdateInfo info;         // 网络端Http文件存储
    private boolean mInfo = false;    // 入口位，判断是点击检查更新按钮（true）还是刚打开界面的更新
//    ProgressDialog pd;
    private static final int NON_UPDATE_CLIENT = 0;   // 不需要更新
    private static final int UPDATE_CLIENT = 1;   // 需要更新
    private static final int GET_UNDATAINFO_ERROR = 2;   // 待处理
//    private static final int UPDATE_PROGRESS = 3;   // 更新进度


    /**
     * 构造函数，获取上下文
     * @param context
     */
    public SoftwareUpdate(Context context){
        this.mContext = context;
        DownloadReceiver.getInstance().setOnSoftUpdateListener(new SoftUpdateCallBack() {
            @Override
            public void onDownloadListener(String id, String filePath) {
                if(downloadId != null){
                    if(downloadId.equals(id)){
                        PublicMethod.installApp(mContext, new File(filePath));
                    }
                }
            }
        });
    }

    /**
     * 需要更新时，下载APP，更新ProgressDialog进度，并进行安装
     */
    public void updateMyApp(boolean info){
        this.mInfo = info;
        new Thread(new CheckVersionTask(){}).start();   // 执行更新线程
    }

    // 检测版本线程任务
    class CheckVersionTask implements Runnable{
        public void run() {
            try {
                //从资源文件获取服务器 地址
                String path = mContext.getResources().getString(R.string.serverXml);
                //包装成url的对象
                URL url = new URL(path);
                HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);   // 设置连接超时
                InputStream is =conn.getInputStream();
                info =  getUpdataInfo(is);
                Message msg = new Message();
                if(info.getVersion().equals(getVersionName())){
                    // 版本号相同，不需要升级
                    if(mInfo){
                        msg.what = NON_UPDATE_CLIENT;
                        mHandler.sendMessage(msg);
                    }
                }else{
                    // 版本号不同 ,提示用户升级
                    msg.what = UPDATE_CLIENT;
                    mHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                // 待处理
                if(mInfo){
                    Message msg = new Message();
                    msg.what = GET_UNDATAINFO_ERROR;
                    mHandler.sendMessage(msg);
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取当前程序的版本号
     * return 版本号
     */
    private String getVersionName() throws Exception{
        //获取packagemanager的实例
        PackageManager packageManager = mContext.getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager
                .getPackageInfo(mContext.getPackageName(), 0);
        return packInfo.versionName;
    }

    /*
    * 用pull解析器解析服务器返回的xml文件 (xml封装了版本号)
    */
    public static UpdateInfo getUpdataInfo(InputStream is) throws Exception{
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "utf-8");//设置解析的数据源
        int type = parser.getEventType();
        UpdateInfo info = new UpdateInfo();//实体
        while(type != XmlPullParser.END_DOCUMENT ){
            switch (type) {
                case XmlPullParser.START_TAG:
                    if("version".equals(parser.getName())){
                        info.setVersion(parser.nextText()); //获取版本号
                    }else if ("url".equals(parser.getName())){
                        info.setUrl(parser.nextText()); //获取要升级的APK文件
                    }else if ("description".equals(parser.getName())){
                        info.setDescription(parser.nextText()); //获取该文件的信息
                    }
                    break;
            }
            type = parser.next();
        }
        return info;
    }


    /**
     * 使用Handler更新UI界面信息
     */
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NON_UPDATE_CLIENT:
                    // Toast通知用户不需要升级程序
                        ToastHelper.addToast("当前为最新版本", mContext);
                    break;
                case UPDATE_CLIENT:
                    //对话框通知用户升级程序
                    showUpdataDialog();
                    break;
                case GET_UNDATAINFO_ERROR:
                    //服务器超时
                    ToastHelper.addToast("获取服务器更新信息失败", mContext);
                    break;
                default:
                    break;
            }
        }
    };



    /**
     * 弹出对话框通知用户更新程序
     * 弹出对话框的步骤：
     * 1.创建alertDialog的builder.
     * 2.要给builder设置属性, 对话框的内容,样式,按钮
     * 3.通过builder 创建一个对话框
     * 4.对话框show()出来
     */
    protected void showUpdataDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext) ;
        builder.setTitle("版本升级：" + info.getVersion());
        // 服务器下载下来后转义字符\n被转义成了n，转回来
        builder.setMessage(info.getDescription().replace("\\n", "\n"));
        //当点确定按钮时从服务器上下载 新的apk 然后安装
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                downloadId = DownloadHelper.getInstance(mContext).download(info.getUrl(),
                        "/SuperTest/UpdateApk/", "SuperTest.apk");
                Log.d("DownloadReceiver", "下载" + "id = " + downloadId);
            }
        });
        //当点取消按钮时进行登录
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();   // 关闭对话框
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

}
