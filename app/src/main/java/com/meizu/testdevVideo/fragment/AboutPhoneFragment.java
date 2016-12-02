package com.meizu.testdevVideo.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.telephony.TelephonyManager;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.widget.floatingbutton.FabTagLayout;
import com.meizu.widget.floatingbutton.FloatingActionButtonPlus;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.activity.MainActivity;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.iPublic;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.shell.ShellUtil;
import com.meizu.testdevVideo.library.ToastHelper;

public class AboutPhoneFragment extends Fragment {
    private TextView textView1, textView2, textView3, textView4, textView5;   // 定义变量
    private LinearLayout mAboutPhoneProgress;   // 加载进度圈
    private FloatingActionButtonPlus mActionButtonPlus;
    StringBuilder Str1 = new StringBuilder();
    StringBuilder Str2 = new StringBuilder();
    StringBuilder Str3 = new StringBuilder();
    StringBuilder Str4 = new StringBuilder();
    StringBuilder Str5 = new StringBuilder();
    // 关于手机界面元素
    String sn = "";    // 手机SN信息
    String internalModel = "";   // 手机内部型号
    private PackageManager pm;    // 包名管理


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_phone, container, false);
        pm = getActivity().getApplication().getPackageManager();
        textView1 = (TextView) view.findViewById(R.id.textView1);
        textView2 = (TextView) view.findViewById(R.id.textView2);
        textView3 = (TextView) view.findViewById(R.id.textView3);
        textView4 = (TextView) view.findViewById(R.id.textView4);
        textView5 = (TextView) view.findViewById(R.id.textView5);
        mAboutPhoneProgress = (LinearLayout) view.findViewById(R.id.mAboutPhoneProgress);
        mActionButtonPlus = (FloatingActionButtonPlus) view.findViewById(R.id.FabPlus);
        ReadThread.start();
        handler.post(myRunnable);
        return view;
    }

    // 更新APK包信息任务
    private Runnable myRunnable= new Runnable() {
        public void run() {
            handler.postDelayed(this, 1000);
            if(isDataChange()){
                CommonVariable.about_phone_video_version = getVersion(iPublic.PACKET_VIDEO);
                CommonVariable.about_phone_music_version = getVersion(iPublic.PACKET_MUSIC);
                CommonVariable.about_phone_ebook_version = getVersion(iPublic.PACKET_EBOOK);
                CommonVariable.about_phone_gallery_version = getVersion(iPublic.PACKET_GALLERY);
                CommonVariable.about_phone_reader_version = getVersion(iPublic.PACKET_READER);
                CommonVariable.about_phone_vip_version = getVersion(iPublic.PACKET_COMPAIGN);
                Str1.delete(0, Str1.length());
                testAdd("应用版本号:\n", 1);
                testAdd("视频：" + CommonVariable.about_phone_video_version +
                        "                    " + "音乐："
                        + CommonVariable.about_phone_music_version + "\n", 1);    // 产品对外名称
                testAdd("读书：" + CommonVariable.about_phone_ebook_version +
                        "                " + "图库："
                        + CommonVariable.about_phone_gallery_version + "\n", 1);    // 产品对外名称
                testAdd("资讯：" + CommonVariable.about_phone_reader_version +
                        "                    " + "会员："
                        + CommonVariable.about_phone_vip_version, 1);    // 产品对外名称
                CommonVariable.isDataChange = true;
            }
        }
    };

    // 读取手机信息线程
    Thread ReadThread=new Thread(){
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
                                    "about_phone.txt", iPublic.LOCAL_MEMORY);
                            ToastHelper.addToast("已保存至内存根目录", getActivity());
                            break;
                        default:
                            break;
                    }
                }
            });

            if(handler != null){
                handler.sendMessage(handler.obtainMessage());
            }
        }
    };

    //消息处理队列
    @SuppressLint("HandlerLeak")
    Handler handler= new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
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
        testAdd("应用版本号:\n", 1);
        testAdd("视频：" + CommonVariable.about_phone_video_version +
                "                    " + "音乐："
                + CommonVariable.about_phone_music_version + "\n", 1);    // 产品对外名称
        testAdd("读书：" + CommonVariable.about_phone_ebook_version +
                "                " + "图库："
                + CommonVariable.about_phone_gallery_version + "\n", 1);    // 产品对外名称
        testAdd("资讯：" + CommonVariable.about_phone_reader_version +
                "                    " + "会员："
                + CommonVariable.about_phone_vip_version, 1);    // 产品对外名称
        testAdd("产品名称：" + PerformsData.getInstance(getActivity()).readStringData(iPerformsKey.deviceType) + "\n", 2);    // 产品对外名称
        testAdd("内部机型：" + CommonVariable.about_phone_internal_model + "\n", 2);   // 产品对内名称
        testAdd("推送机型查询：" + CommonVariable.about_phone_product_push + "\n", 2);   // 推送机型查询
        testAdd("机器是否加密：" + CommonVariable.about_phone_isLocked + "\n", 2);   // 推送机型查询
        testAdd("SN：" + CommonVariable.about_phone_sn + "\n", 2);  // 添加SN号
        testAdd("IMEI：" + PerformsData.getInstance(getActivity()).readStringData(iPerformsKey.imei), 2);  // 添加IMEI号
        testAdd("固件信息：\n" + "外部版本号 " + CommonVariable.about_phone_outside_version + "\n", 3);  // 添加固件版本
        testAdd( "内部版本号 " + CommonVariable.about_phone_inside_version + "\n", 3);  // 添加内部版本号
        PerformsData.getInstance(getActivity()).writeStringData(iPerformsKey.systemVersion,
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
        testAdd("CPU信息：" + CommonVariable.about_phone_cpu, 5);// CPU信息
    }

    // 判断信息是否更改
    private boolean isDataChange(){
        if(!CommonVariable.about_phone_video_version.equals(getVersion(iPublic.PACKET_VIDEO)) |
                !CommonVariable.about_phone_music_version.equals(getVersion(iPublic.PACKET_MUSIC)) |
                !CommonVariable.about_phone_ebook_version.equals(getVersion(iPublic.PACKET_EBOOK)) |
                !CommonVariable.about_phone_gallery_version.equals(getVersion(iPublic.PACKET_GALLERY)) |
                !CommonVariable.about_phone_reader_version.equals(getVersion(iPublic.PACKET_READER)) |
                !CommonVariable.about_phone_vip_version.equals(getVersion(iPublic.PACKET_COMPAIGN))) {
            return true;
        }
        return false;
    }

    /**
     * 获取手机信息
     */
    private void getMyPhoneMes(){
        CommonVariable.about_phone_video_version = getVersion(iPublic.PACKET_VIDEO);
        CommonVariable.about_phone_music_version = getVersion(iPublic.PACKET_MUSIC);
        CommonVariable.about_phone_ebook_version = getVersion(iPublic.PACKET_EBOOK);
        CommonVariable.about_phone_gallery_version = getVersion(iPublic.PACKET_GALLERY);
        CommonVariable.about_phone_reader_version = getVersion(iPublic.PACKET_READER);
        CommonVariable.about_phone_vip_version = getVersion(iPublic.PACKET_COMPAIGN);
        sn = ShellUtil.getProperty("ro.serialno");   // 手机SN号
        productAndModel();   // 设备内部外部型号获取
        CommonVariable.about_phone_internal_model = internalModel;
        CommonVariable.about_phone_product_push = ShellUtil.getProperty("ro.meizu.product.model");  // 推送机型查询
        // 三星平台手机
        if(CommonVariable.snLabel.contains("71") || CommonVariable.snLabel.contains("76") || CommonVariable.snLabel.contains("86")){
            CommonVariable.about_phone_isLocked = ShellUtil.exec("cat /proc/bootloader_unlock");
            if(CommonVariable.about_phone_isLocked == null){
                CommonVariable.about_phone_isLocked = "";
            }
            System.out.print("输出的值是" + CommonVariable.about_phone_isLocked .toString());
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
//        CommonVariable.about_phone_simCardNumber = simCardNumber();
        if(ShellUtil.getProperty("ro.meizu.hardware.soc").equals("")){
            CommonVariable.about_phone_cpu = ShellUtil.getProperty("ro.hardware");
        }else{
            CommonVariable.about_phone_cpu = ShellUtil.getProperty("ro.meizu.hardware.soc");
        }
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

    /**
     * 获取应用版本号
     * @param packName
     * @return
     */
    private String getVersion(String packName) {
        try {
            PackageInfo info = this.pm.getPackageInfo(packName, 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    @Override
    public void onResume(){
        Log.e("AboutPhoneFragment", "执行onResume()");
        if(CommonVariable.isDataChange){
            CommonVariable.isDataChange = false;
            textView1.setText(Str1);
        }
        super.onResume();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!TextUtils.isEmpty(Str1)){
            Str1 = null;
            Str2 = null;
            Str3 = null;
            Str4 = null;
            Str5 = null;
        }
    }
}
