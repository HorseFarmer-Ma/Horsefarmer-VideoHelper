package com.meizu.testdevVideo.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipboardManager;
import android.content.Context;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.broadcast.BootReceiver;
import com.meizu.testdevVideo.interports.InstallCallBack;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.widget.floatingbutton.FabTagLayout;
import com.meizu.widget.floatingbutton.FloatingActionButtonPlus;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.shell.ShellUtil;
import com.meizu.testdevVideo.library.ToastHelper;

public class AboutPhoneFragment extends Fragment implements InstallCallBack{
    private TextView textView1, textView2, textView3, textView4, textView5;
    private LinearLayout mAboutPhoneProgress;
    private FloatingActionButtonPlus mActionButtonPlus;
    private static Context sApplicationContext = null;

    private StringBuilder Str1 = new StringBuilder();
    private StringBuilder Str2 = new StringBuilder();
    private StringBuilder Str3 = new StringBuilder();
    private StringBuilder Str4 = new StringBuilder();
    private StringBuilder Str5 = new StringBuilder();
    private String sn = "";
    private String internalModel = "";
    private PackageManager pm;

    private static final int UPDATE_MSG = 100;
    private static final int UPDATE_MSG_SOON = 200;
    private View rootView;     // 缓存Fragment view
    private IntentFilter bootFilter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(null == rootView){
            rootView = inflater.inflate(R.layout.fragment_about_phone, container, false);
            sApplicationContext = SuperTestApplication.getContext();
            findView(rootView);
            pm = sApplicationContext.getPackageManager();
            BootReceiver.setOnInstallListener(AboutPhoneFragment.this);
            readPhoneMsgThread.start();
        }
        return rootView;
    }

    private void findView(View view){
        textView1 = (TextView) view.findViewById(R.id.textView1);
        textView2 = (TextView) view.findViewById(R.id.textView2);
        textView3 = (TextView) view.findViewById(R.id.textView3);
        textView4 = (TextView) view.findViewById(R.id.textView4);
        textView5 = (TextView) view.findViewById(R.id.textView5);
        mAboutPhoneProgress = (LinearLayout) view.findViewById(R.id.mAboutPhoneProgress);
        mActionButtonPlus = (FloatingActionButtonPlus) view.findViewById(R.id.FabPlus);
    }


    // 读取手机信息线程
    Thread readPhoneMsgThread = new Thread(){
        public void run(){
            aboutPhone();     // 关于手机信息填充
            /**---------------------------- 浮动按钮 ---------------------------*/
            // 悬浮按钮监听
            mActionButtonPlus.setOnItemClickListener(new FloatingActionButtonPlus.OnItemClickListener() {
                @Override
                public void onItemClick(FabTagLayout tagView, int position) {
                    switch (position) {
                        case 0:
                            Snackbar.make(tagView, "                          已将内容复制至剪贴板", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            ClipboardManager cmb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            cmb.setText(Str1.toString() + Str2.toString() + Str3.toString() + Str4.toString() + Str5.toString());   // 复制内容至剪贴板
                            break;
                        case 1:
                            PublicMethod.saveStringToFile(Str1.toString() + Str2.toString() + Str3.toString() + Str4.toString() + Str5.toString(),
                                    "about_phone.txt", iPublicConstants.LOCAL_MEMORY);
                            ToastHelper.addToast("已保存至内存根目录", getActivity());
                            break;
                        default:
                            break;
                    }
                }
            });

            if(handler != null){
                handler.sendEmptyMessage(UPDATE_MSG);
            }
        }
    };

    // 消息处理队列
    @SuppressLint("HandlerLeak")
    Handler handler= new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_MSG:
                    mAboutPhoneProgress.setVisibility(View.GONE);
                    textView1.setVisibility(View.VISIBLE);
                    textView1.setText(Str1.toString());
                    textView2.setVisibility(View.VISIBLE);
                    textView2.setText(Str2.toString());
                    textView3.setVisibility(View.VISIBLE);
                    textView3.setText(Str3.toString());
                    textView4.setVisibility(View.VISIBLE);
                    textView4.setText(Str4.toString());
                    textView5.setVisibility(View.VISIBLE);
                    textView5.setText(Str5.toString().split("产品名称")[0]);
                    break;
                case UPDATE_MSG_SOON:
                    CommonVariable.about_phone_video_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_VIDEO);
                    CommonVariable.about_phone_music_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_MUSIC);
                    CommonVariable.about_phone_ebook_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_EBOOK);
                    CommonVariable.about_phone_browser_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_BROWSER);
                    CommonVariable.about_phone_reader_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_READER);
                    CommonVariable.about_phone_theme_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_THEME);
                    Str1.delete(0, Str1.length());
                    testAdd("应用版本号:\n", 1);
                    testAdd("视频：" + CommonVariable.about_phone_video_version +
                            "                    " + "音乐："
                            + CommonVariable.about_phone_music_version + "\n", 1);    // 产品对外名称
                    testAdd("读书：" + CommonVariable.about_phone_ebook_version +
                            "                " + "浏览器："
                            + CommonVariable.about_phone_browser_version + "\n", 1);    // 产品对外名称
                    testAdd("资讯：" + CommonVariable.about_phone_reader_version +
                            "                    " + "主题："
                            + CommonVariable.about_phone_theme_version, 1);    // 产品对外名称

                    CommonVariable.isDataChange = false;
                    textView1.setText(Str1.toString());
            }
        }
    };

    /**
     * 添加关于手机信息
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void aboutPhone(){
        getMyPhoneMes();
        connectMyPhoneMes();
    }

    /**
     * 连接手机信息成一个StringBuilder
     */
    private void connectMyPhoneMes(){
        // ---------------------------添加信息----------------------
        Str1.delete(0, Str1.length());
        testAdd("应用版本号:\n", 1);
        testAdd("视频：" + CommonVariable.about_phone_video_version +
                "                    " + "音乐："
                + CommonVariable.about_phone_music_version + "\n", 1);    // 产品对外名称
        testAdd("读书：" + CommonVariable.about_phone_ebook_version +
                "                " + "浏览器："
                + CommonVariable.about_phone_browser_version + "\n", 1);    // 产品对外名称
        testAdd("资讯：" + CommonVariable.about_phone_reader_version +
                "                    " + "主题："
                + CommonVariable.about_phone_theme_version, 1);    // 产品对外名称
        testAdd("产品名称：" + PerformsData.getInstance(getActivity()).readStringData(iPerformsKey.deviceType) + "\n", 2);    // 产品对外名称
        testAdd("内部机型：" + CommonVariable.about_phone_internal_model + "\n", 2);   // 产品对内名称
        testAdd("推送机型查询：" + CommonVariable.about_phone_product_push + "\n", 2);   // 推送机型查询
        testAdd("机器是否加密：" + CommonVariable.about_phone_isLocked + "\n", 2);   // 推送机型查询
        testAdd("SN：" + CommonVariable.about_phone_sn + "\n", 2);  // 添加SN号
        testAdd("设备ID：" + PerformsData.getInstance(getActivity()).readStringData(iPerformsKey.imei), 2);  // 添加IMEI号
        testAdd("固件信息：\n" + "外部版本号 " + CommonVariable.about_phone_outside_version + "\n", 3);  // 添加固件版本
        testAdd( "内部版本号 " + CommonVariable.about_phone_inside_version + "\n", 3);  // 添加内部版本号
        PerformsData.getInstance(SuperTestApplication.getContext()).writeStringData(iPerformsKey.systemVersion,
                CommonVariable.about_phone_outside_version + " | " + CommonVariable.about_phone_inside_version);
        testAdd("主干ID " + CommonVariable.about_phone_mask_id, 3);  // 添加主干固件版本号
        testAdd("基带信息：" + "\n", 4);
        testAdd(CommonVariable.about_phone_baseband + "\n", 4);  // 添加基带信息
        PerformsData.getInstance(getActivity()).writeStringData(iPerformsKey.baseBand,
                CommonVariable.about_phone_baseband);
        testAdd("Kernel信息：" + "\n", 4);
        testAdd(CommonVariable.about_phone_kernal, 4);  // 添加Kernel信息
        PerformsData.getInstance(getActivity()).writeStringData(iPerformsKey.kernel,
                CommonVariable.about_phone_kernal);
        testAdd("耳机阻抗：" + CommonVariable.about_phone_earphone + "\n", 5);     // 获取耳机阻抗
//        testAdd("本机号码：" + CommonVariable.about_phone_simCardNumber + "\n", 5);     // 获取手机号码
        testAdd("CPU信息：" + BaseData.getInstance(SuperTestApplication.getContext()).readStringData("CPU"), 5);// CPU信息
    }


    /**
     * 获取手机信息
     */
    private void getMyPhoneMes(){
        CommonVariable.about_phone_video_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_VIDEO);
        CommonVariable.about_phone_music_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_MUSIC);
        CommonVariable.about_phone_ebook_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_EBOOK);
        CommonVariable.about_phone_browser_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_BROWSER);
        CommonVariable.about_phone_reader_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_READER);
        CommonVariable.about_phone_theme_version = PublicMethod.getVersion(pm, iPublicConstants.PACKET_THEME);
        sn = ShellUtil.getProperty("ro.serialno");   // 手机SN号
        productAndModel();   // 设备内部外部型号获取
        CommonVariable.about_phone_internal_model = internalModel;
        CommonVariable.about_phone_product_push = ShellUtil.getProperty("ro.meizu.product.model");  // 推送机型查询
        // 三星平台手机
        if(!BaseData.getInstance(SuperTestApplication.getContext()).readStringData("CPU").contains("mt")){
            CommonVariable.about_phone_isLocked = ShellUtil.exec("cat /proc/bootloader_unlock");
            if(CommonVariable.about_phone_isLocked == null){
                CommonVariable.about_phone_isLocked = "";
            }
            if(CommonVariable.about_phone_isLocked.contains("1")){
                CommonVariable.about_phone_isLocked = "已解锁";
            }else if(CommonVariable.about_phone_isLocked.contains("0")){
                CommonVariable.about_phone_isLocked = "未解锁";
            }else{
                CommonVariable.about_phone_isLocked = "未识别";
            }
        }else{
            CommonVariable.about_phone_isLocked = ShellUtil.exec("cat /proc/lk_info/sec");
            if(CommonVariable.about_phone_isLocked == null || CommonVariable.about_phone_isLocked.equals("")){
                CommonVariable.about_phone_isLocked = "未知";
            }else if(CommonVariable.about_phone_isLocked.equals("Non-Secure Chip")) {
                CommonVariable.about_phone_isLocked = CommonVariable.about_phone_isLocked + " (非加密)";
            }else{
                CommonVariable.about_phone_isLocked = CommonVariable.about_phone_isLocked + " (加密)";
            }
        }


        CommonVariable.about_phone_sn = sn;
        CommonVariable.about_phone_outside_version = ShellUtil.getProperty("ro.build.display.id");
        CommonVariable.about_phone_inside_version = ShellUtil.getProperty("ro.build.inside.id");
        CommonVariable.about_phone_mask_id = ShellUtil.getProperty("ro.build.mask.id");
        CommonVariable.about_phone_baseband = ShellUtil.getProperty("gsm.version.baseband");
        CommonVariable.about_phone_kernal = ShellUtil.exec("cat /proc/version");
        CommonVariable.about_phone_earphone = earphoneImpedance();

    }

    /**
     * 获取SIM卡信息
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    private String simCardNumber(){
        TelephonyManager tmc = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);   // 获取手机号码信息
        if(tmc.getLine1Number() != null){    // 有SIM卡
            if(tmc.getSimOperatorName().equals("CMCC")){
                if(tmc.getLine1Number().equals("")){
                    return  "中国移动"; // 中国移动卡处理
                }else{
                    return  "中国移动" +"("+ tmc.getLine1Number() + ")"; // 中国移动卡处理
                }
            }else{
                return  tmc.getSimOperatorName() +"("+ tmc.getLine1Number() + ")";  // 其他情况
            }

        }else{
            return "未插入SIM卡";
        }
    }


    /**
     * 获取产品名称和内部型号
     */
    private void productAndModel(){
        if(sn.length() > 0)
            CommonVariable.snLabel = sn.substring(0, 3).trim();  // 截取前3位字
        if(CommonVariable.snLabel.contains("80")){
//            CommonVariable.about_phone_product_name = "Pro6";
            internalModel = "M80";
        }else if(CommonVariable.snLabel.contains("A1")){
//            CommonVariable.about_phone_product_name = "魅蓝metal";
            internalModel = "MA01";
        }else if(CommonVariable.snLabel.contains("A02")){
            internalModel = "A02";
        }else if(CommonVariable.snLabel.contains("95")){
//            CommonVariable.about_phone_product_name = "MX6";
            internalModel = "M95";
        }else if(CommonVariable.snLabel.contains("86")){
//            CommonVariable.about_phone_product_name = "Pro5";
            internalModel = "M86";
        }else if(CommonVariable.snLabel.contains("88") || CommonVariable.snLabel.contains("8C")){
//            CommonVariable.about_phone_product_name = "魅蓝2";
            internalModel = "M88";
        }else if(CommonVariable.snLabel.contains("85")){
//            CommonVariable.about_phone_product_name = "MX5";
            internalModel = "M85";
        }else if(CommonVariable.snLabel.contains("81")){
//            CommonVariable.about_phone_product_name = "魅蓝note2";
            internalModel = "M81";
        }else if(CommonVariable.snLabel.contains("79")){
//            CommonVariable.about_phone_product_name = "魅蓝";
            internalModel = "M79";
        }else if(CommonVariable.snLabel.contains("71")){
//            CommonVariable.about_phone_product_name = "魅蓝note";
            internalModel = "M71";
        }else if(CommonVariable.snLabel.contains("76")){
//            CommonVariable.about_phone_product_name = "MX4Pro";
            internalModel = "M71";
        }else if(CommonVariable.snLabel.contains("75")){
//            CommonVariable.about_phone_product_name = "MX4";
            internalModel = "M75";
        }else if(CommonVariable.snLabel.contains("35")){
//            CommonVariable.about_phone_product_name = "MX3";
            if(sn.substring(0, 3).equals("356"))
            {
                internalModel = "M069";
            }else if(sn.substring(0, 3).equals("355"))
            {
                internalModel = "M068";
            }else if(sn.substring(0, 3).equals("353"))
            {
                internalModel = "M065";
            }else if(sn.substring(0, 3).equals("351"))
            {
                internalModel = "M064";
            }
        }else if(CommonVariable.snLabel.contains("04")){
//            CommonVariable.about_phone_product_name = "MX2";
            internalModel = "M" + sn.substring(0, 3);
        }else if(CommonVariable.snLabel.contains("MX")){
//            CommonVariable.about_phone_product_name = "MX";
            if(sn.substring(0, 3).equals("MX3"))
            {
                internalModel = "M031";
            }else if(sn.substring(0, 3).equals("MX2"))
            {
                internalModel = "M032";
            }else if(sn.substring(0, 3).equals("MX1"))
            {
                internalModel = "M030";
            }

        }else if(CommonVariable.snLabel.contains("Y15")){
            internalModel = "Y15";
        }else if(CommonVariable.snLabel.contains("U10")){
            internalModel = "U10";
        }else if(CommonVariable.snLabel.contains("U20")){
            internalModel = "U15";
        }else{
            internalModel = CommonVariable.snLabel;
        }
    }


    /**
     * 添加TextView信息
     * @param Text
     */
    private void testAdd(String Text, int i){
        switch (i){
            case 1:
                Str1.append(Text);
                break;
            case 2:
                Str2.append(Text);
                break;
            case 3:
                Str3.append(Text);
                break;
            case 4:
                Str4.append(Text);
                break;
            case 5:
                Str5.append(Text);
                break;
        }

    }


    /**
     * 查询耳机阻抗
     * @return
     */
    private String earphoneImpedance(){
        String earphone = null;
        earphone = ShellUtil.exec("cat /sys/class/arizona/wm8998_hp_impedance/hp_impedance");   // M86
        if(earphone == null){
            return "欲获取阻抗，请将耳机插入三星平台手机";
        }else{
            if(earphone.contains("0")){
                return "未插入耳机";
            }else{
                return earphone + "欧姆";
            }
        }
    }


    @Override
    public void onResume(){
        Log.d("AboutPhoneFragment", "执行onResume()");
        handler.sendEmptyMessage(UPDATE_MSG_SOON);
        super.onResume();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void installOrUninstall(boolean isInstall, String packageName) {
        if(packageName.equals("package:" + iPublicConstants.PACKET_VIDEO) || packageName.equals("package:" + iPublicConstants.PACKET_MUSIC)
                || packageName.equals("package:" + iPublicConstants.PACKET_EBOOK) || packageName.equals("package:" + iPublicConstants.PACKET_BROWSER)
                || packageName.equals("package:" + iPublicConstants.PACKET_READER) || packageName.equals("package:" + iPublicConstants.PACKET_THEME)){
            CommonVariable.isDataChange = true;
            handler.sendEmptyMessage(UPDATE_MSG_SOON);
        }
    }
}
