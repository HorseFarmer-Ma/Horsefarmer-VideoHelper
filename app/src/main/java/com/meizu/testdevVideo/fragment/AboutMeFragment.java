package com.meizu.testdevVideo.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meizu.common.widget.Switch;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.activity.AppChooseActivity;
import com.meizu.testdevVideo.activity.SettingActivity;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.AppInfoHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.SaveLog;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.update.SoftwareUpdate;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import flyme.support.v7.app.AlertDialog;

public class AboutMeFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    private RelativeLayout personal_center_setting_btn, personal_center_update_btn,
            ic_user_account, cat_log_btn, export_trace_drodbox_btn, clear_data_btn;
    private SoftwareUpdate softwareUpdate = null;
    private TextView ic_user_email, version_text, clear_data_txt;
    private ImageView ic_app_image, ic_clear_data;
    private Switch function_choose;
    private Handler mHandler = null;
    private SaveLog saveLog;
    private DefinedRunnable definedRunnable;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        softwareUpdate = (null == softwareUpdate)? new SoftwareUpdate(getActivity(), getActivity()) : softwareUpdate;
        mHandler = (null == mHandler)? new Handler() : mHandler;
        saveLog = (null == saveLog)? new SaveLog() : saveLog;
        definedRunnable = (null == definedRunnable)? new DefinedRunnable() : definedRunnable;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_me, container, false);
        findView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mHandler.postDelayed(definedRunnable.setWhat(DefinedRunnable.UPDATE_USER_MSG), 100);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        softwareUpdate.clear();
    }

    private void findView(View view){
        personal_center_setting_btn = (RelativeLayout) view.findViewById(R.id.personal_center_setting_btn);
        personal_center_update_btn = (RelativeLayout) view.findViewById(R.id.personal_center_update_btn);
        ic_user_account = (RelativeLayout) view.findViewById(R.id.ic_user_account);
        cat_log_btn = (RelativeLayout) view.findViewById(R.id.cat_log_btn);
        export_trace_drodbox_btn = (RelativeLayout) view.findViewById(R.id.export_trace_drodbox_btn);
        clear_data_btn = (RelativeLayout) view.findViewById(R.id.clear_data_btn);
        ic_user_email = (TextView) view.findViewById(R.id.ic_user_email);
        version_text = (TextView) view.findViewById(R.id.version_text);
        clear_data_txt = (TextView) view.findViewById(R.id.clear_data_txt);
        ic_app_image = (ImageView) view.findViewById(R.id.ic_app_image);
        ic_clear_data = (ImageView) view.findViewById(R.id.ic_clear_data);
        function_choose = (Switch) view.findViewById(R.id.function_choose);
        personal_center_setting_btn.setOnClickListener(this);
        personal_center_update_btn.setOnClickListener(this);
        ic_user_account.setOnClickListener(this);
        cat_log_btn.setOnClickListener(this);
        clear_data_btn.setOnClickListener(this);
        export_trace_drodbox_btn.setOnClickListener(this);
        function_choose.setOnCheckedChangeListener(this);
        version_text.setText("当前版本 : " + AppInfoHelper.getInstance().getAppVersion(getActivity().getPackageName()));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.personal_center_setting_btn:
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.mz_activity_extra_to_next_open_enter,
                        R.anim.mz_activity_extra_to_next_close_exit);
            break;
            case R.id.personal_center_update_btn:
                softwareUpdate.updateMyApp(true);
                break;
            case R.id.ic_user_account:
                Intent appChooseIntent = new Intent(getActivity(), AppChooseActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("title", getResources().getString(R.string.choose_app_type));
                appChooseIntent.putExtras(bundle);
                startActivityForResult(appChooseIntent, 0);
                break;
            case R.id.cat_log_btn:
                ToastHelper.addToast("正在抓取Log，请稍后...",
                        getActivity().getApplicationContext());
                new Thread(){
                    public void run() {
                        saveLog.saveAll();
                        mHandler.post(definedRunnable.setWhat(DefinedRunnable.UPDATE_CAT_LOG_MSG));
                    }
                }.start();
                break;
            case R.id.export_trace_drodbox_btn:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("导出类型");
                //    指定下拉列表的显示数据
                final String[] cities = {"Trace文件", "DrodBox文件"};
                //    设置一个下拉的列表选择项
                builder.setItems(cities, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(0 == which){
                            if(new File("/data/anr").exists()){
                                ToastHelper.addToast("正在导出Trace文件，请稍后...", getActivity().getApplicationContext());
                                PublicMethod.copyFolder("/data/anr", iPublicConstants.LOCAL_MEMORY + "SuperTest/Trace/" +
                                        new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));   // 导出Trace文件
                                ToastHelper.addToast("Trace文件导出完毕，保存至/sdcard/SuperTest/Trace/",
                                        getActivity().getApplicationContext());
                            }else{
                                ToastHelper.addToast("没有Trace文件，无需导出", getActivity().getApplicationContext());
                            }
                        }else{
                            if(new File("/data/system/Dropbox").exists()){
                                ToastHelper.addToast("正在导出Dropbox文件，请稍后...", getActivity().getApplicationContext());
                                PublicMethod.copyFolder("/data/system/dropbox", iPublicConstants.LOCAL_MEMORY + "SuperTest/Dropbox/" +
                                        new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));   // 导出Trace文件
                                ToastHelper.addToast("Dropbox文件导出完毕，保存至/sdcard/SuperTest/Dropbox/",
                                        getActivity().getApplicationContext());
                            }else{
                                ToastHelper.addToast("没有Dropbox文件，无需导出", getActivity().getApplicationContext());
                            }
                        }
                    }
                });
                builder.show();
                break;

            case R.id.clear_data_btn:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity()).setTitle("选择应用");
                //    指定下拉列表的显示数据
                final String[] app = {"视频", "音乐", "读书", "资讯", "浏览器", "主题美化", "推送服务"};
                final String[] package_name = {iPublicConstants.PACKET_VIDEO, iPublicConstants.PACKET_MUSIC,
                        iPublicConstants.PACKET_EBOOK, iPublicConstants.PACKET_READER, iPublicConstants.PACKET_BROWSER,
                        iPublicConstants.PACKET_THEME, iPublicConstants.PACKET_CLOUD};
                //    设置一个下拉的列表选择项
                builder2.setItems(app, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        if(function_choose.isChecked()){
                            PublicMethod.showInstalledAppDetails(getActivity(), package_name[which]);
                        }else{
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Runtime.getRuntime().exec("pm clear " + package_name[which]);
                                        mHandler.post(definedRunnable.setWhat(DefinedRunnable.UPDATE_CLEAR_MSG));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    }
                });
                builder2.show();

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 取出字符串
        if (data != null){
            Bundle bundle = data.getExtras();
            BaseData.getInstance(getActivity().getApplicationContext())
                    .writeStringData(SettingPreferenceKey.APP_TYPE,
                    bundle.getString(SettingPreferenceKey.APP_TYPE));
            BaseData.getInstance(getActivity().getApplicationContext())
                    .writeStringData(SettingPreferenceKey.EMAIL_ADDRESS,
                    bundle.getString(SettingPreferenceKey.EMAIL_ADDRESS));
            BaseData.getInstance(getActivity().getApplicationContext())
                    .writeStringData(SettingPreferenceKey.MONKEY_PACKAGE,
                    bundle.getString(SettingPreferenceKey.MONKEY_PACKAGE));
            mHandler.postDelayed(definedRunnable.setWhat(DefinedRunnable.UPDATE_USER_MSG), 100);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    class DefinedRunnable implements Runnable{

        public static final int UPDATE_USER_MSG = 100;
        public static final int UPDATE_CLEAR_MSG = 200;
        public static final int UPDATE_CAT_LOG_MSG = 300;
        private int what;

        public DefinedRunnable setWhat(int what){
            this.what = what;
            return this;
        }

        @Override
        public void run() {
            switch (what){
                case UPDATE_USER_MSG:
                    String email = BaseData.getInstance(getActivity().getApplicationContext())
                            .readStringData(SettingPreferenceKey.EMAIL_ADDRESS);
                    String package_name = BaseData.getInstance(getActivity().getApplicationContext())
                            .readStringData(SettingPreferenceKey.MONKEY_PACKAGE);
                    if(null != email){
                        ic_user_email.setText(email);
                    }

                    if (null != package_name){
                        ic_app_image.setImageDrawable(AppInfoHelper.getInstance().getAppIcon(package_name));
                    }
                    break;
                case UPDATE_CLEAR_MSG:
                    ToastHelper.addToast("清除完毕", getActivity().getApplicationContext());
                    break;
                case UPDATE_CAT_LOG_MSG:
                    ToastHelper.addToast("Log抓取完毕，保存至/sdcard/SuperTest/LogReport/",
                            getActivity().getApplicationContext());
                    break;
            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            clear_data_txt.setText(getString(R.string.action_into_setting));
            ic_clear_data.setImageResource(R.drawable.ic_info_setting);
        }else{
            clear_data_txt.setText(getString(R.string.action_clear_data));
            ic_clear_data.setImageResource(R.drawable.ic_clear_data);
        }
    }
}
