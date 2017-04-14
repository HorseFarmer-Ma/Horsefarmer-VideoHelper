package com.meizu.testdevVideo.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.activity.AppChooseActivity;
import com.meizu.testdevVideo.activity.HistoryAndTimeTaskActivity;

import com.meizu.testdevVideo.activity.SettingActivity;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.AnimationHelper;

import com.meizu.testdevVideo.service.MonkeyService;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.FailPostRecordData;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.sharepreference.PrefWidgetOnOff;

import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.shell.ShellUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonkeyFragment extends Fragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, TextWatcher, View.OnTouchListener,
        Animation.AnimationListener{
    private SharedPreferences sharedPreferences;
    private static List<String> packet_list;
    private ArrayAdapter<String> myAdapter;
    private static Context sApplicationContext = null;

    private int function_choose;

    private Button btn_choose_app, button_single_monkey, button_system_monkey,
            button_blacklist, button_blacklist_defined, button_save_blacklist, button_single_choose,
            button_system_choose, bt_timetask, bt_history, btn_open_mtkLog, btn_fail_and_send;

    private EditText edit_blacklist_defined, edit_seed, edit_click_time, edit_click_numbers;

    private CheckBox checkbox_defined_value;

    private TextView single_monkey_text, system_monkey_text, txt_send_fail_reason;
    private LinearLayout layout_single_monkey, layout_system_monkey, layout_tab_value, layout_fail_send;
    private boolean blacklist_flag = true;               // 黑名单选择标志位
    private boolean defined_monkey_value_flag = false;    // 自定义参数选择标志位

    private LinearLayout.LayoutParams params = null;
    private String monkeyApp = "";

    private ScaleAnimation openAnimation;
    private ScaleAnimation closeAnimation;

    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(null == rootView){
            rootView = inflater.inflate(R.layout.fragment_monkey, container, false);
            findView(rootView);
            sApplicationContext = SuperTestApplication.getContext();
            monkey_fragment_init();
            function_choose(1);
        }
        return rootView;
    }

    /**
     * 控件初始化
     * @param view
     */
    private void findView(View view) {
        button_single_monkey = (Button) view.findViewById(R.id.button_single_monkey);
        button_system_monkey = (Button) view.findViewById(R.id.button_system_monkey);
        button_blacklist = (Button) view.findViewById(R.id.button_blacklist);
        btn_fail_and_send = (Button) view.findViewById(R.id.btn_fail_and_send);
        button_blacklist_defined = (Button) view.findViewById(R.id.button_blacklist_defined);
        button_save_blacklist = (Button) view.findViewById(R.id.button_save_blacklist);
        button_single_choose = (Button) view.findViewById(R.id.button_single_choose);
        button_system_choose = (Button) view.findViewById(R.id.button_system_choose);
        btn_open_mtkLog = (Button) view.findViewById(R.id.btn_open_mtkLog);
        bt_history = (Button) view.findViewById(R.id.bt_history);
        bt_timetask = (Button) view.findViewById(R.id.bt_timetask);
        btn_choose_app = (Button) view.findViewById(R.id.btn_choose_app);
        single_monkey_text = (TextView) view.findViewById(R.id.single_monkey_text);
        system_monkey_text = (TextView) view.findViewById(R.id.system_monkey_text);
        txt_send_fail_reason = (TextView) view.findViewById(R.id.txt_send_fail_reason);
        checkbox_defined_value = (CheckBox) view.findViewById(R.id.checkbox_defined_value);
        layout_single_monkey = (LinearLayout) view.findViewById(R.id.layout_single_monkey);
        layout_system_monkey = (LinearLayout) view.findViewById(R.id.layout_system_monkey);
        layout_fail_send = (LinearLayout) view.findViewById(R.id.layout_fail_send);
        layout_tab_value = (LinearLayout) view.findViewById(R.id.layout_tab_value);
        edit_blacklist_defined = (EditText) view.findViewById(R.id.edit_blacklist_defined);
        edit_seed = (EditText) view.findViewById(R.id.edit_seed);
        edit_click_numbers = (EditText) view.findViewById(R.id.edit_click_numbers);
        edit_click_time = (EditText) view.findViewById(R.id.edit_click_time);

        button_save_blacklist.setVisibility(View.GONE);
        edit_blacklist_defined.setVisibility(View.GONE);
        layout_system_monkey.setVisibility(View.GONE);
        if(!CommonVariable.about_phone_cpu.contains("mt")){
            edit_seed.setVisibility(View.GONE);
            btn_open_mtkLog.setVisibility(View.GONE);
        }
    }

    /**
     * 页面按钮监听初始化
     */
    private void monkey_fragment_init() {
        // 监听设置
        button_single_monkey.setOnClickListener(this);
        button_system_monkey.setOnClickListener(this);
        button_blacklist.setOnClickListener(this);
        button_blacklist_defined.setOnClickListener(this);
        button_save_blacklist.setOnClickListener(this);
        button_single_choose.setOnClickListener(this);
        button_system_choose.setOnClickListener(this);
        bt_history.setOnClickListener(this);
        bt_timetask.setOnClickListener(this);
        btn_fail_and_send.setOnClickListener(this);
        btn_open_mtkLog.setOnClickListener(this);
        btn_choose_app.setOnClickListener(this);
        edit_seed.addTextChangedListener(this);
        edit_click_time.addTextChangedListener(this);
        edit_click_numbers.addTextChangedListener(this);
        checkbox_defined_value.setOnCheckedChangeListener(this);

        openAnimation = AnimationHelper.getInstance().getScaleAnimation(0f, 1.0f, 1.0f, 1.0f, 0, 0, 500, false, 0f, 0.5f);
        closeAnimation = AnimationHelper.getInstance().getScaleAnimation(1.0f, 0f, 1.0f, 1.0f, 0, 0, 500, false, 0f, 0.5f);
        layout_tab_value.setVisibility(View.GONE);
        closeAnimation.setAnimationListener(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(sApplicationContext);
        monkeyApp = BaseData.getInstance(sApplicationContext).readStringData(SettingPreferenceKey.MONKEY_CHOOSE_APP);

        params = (LinearLayout.LayoutParams) single_monkey_text.getLayoutParams();

        if(TextUtils.isEmpty(monkeyApp)){
            single_monkey_text.setText("您尚未选择业务");
            checkbox_defined_value.setVisibility(View.GONE);
        }else{
            checkbox_defined_value.setVisibility(View.VISIBLE);
            params.setMargins(16, 0, 16, 0);
            single_monkey_text.setLayoutParams(params);
            setAppMonkeyText(monkeyApp, "1000", "500", "1200000000");
        }


        // 读取保存的黑名单进编辑框
        edit_blacklist_defined.setText(PublicMethod.
                readFile(iPublicConstants.MEMORY_BACK_UP + "blacklist_save.txt"));
        edit_blacklist_defined.setOnTouchListener(this);

        // 系统monkey_text设置
        system_monkey_text_setting();

        //适配器
        myAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getPacketList());
        //设置样式
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }


    /**
     * 布局选择函数
     */
    private void function_choose(int layout_choose) {
        function_choose = layout_choose;
        switch (layout_choose) {
            case 1:
                button_single_choose.setBackground(getResources().getDrawable(R.drawable.my_function_choose_button1));
                button_system_choose.setBackground(getResources().getDrawable(R.drawable.my_function_choose_button2));
                button_single_choose.setTextColor(Color.WHITE);
                button_system_choose.setTextColor(Color.BLACK);
                layout_single_monkey.setVisibility(View.VISIBLE);
                layout_system_monkey.setVisibility(View.GONE);
                break;
            case 2:
                button_single_choose.setBackground(getResources().getDrawable(R.drawable.my_function_choose_button2));
                button_system_choose.setBackground(getResources().getDrawable(R.drawable.my_function_choose_button1));
                button_single_choose.setTextColor(Color.BLACK);
                button_system_choose.setTextColor(Color.WHITE);

                layout_single_monkey.setVisibility(View.GONE);
                layout_system_monkey.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    /**
     * 系统moneky黑名单
     * 按钮切换效果函数
     */
    private void blacklist_choose(boolean choose) {
        if (choose) {
            button_blacklist.setBackground(getResources().getDrawable(R.drawable.my_button_purple));
            button_blacklist_defined.setBackground(getResources().getDrawable(R.drawable.my_button_white));
            button_blacklist.setTextColor(Color.WHITE);
            button_blacklist_defined.setTextColor(Color.BLACK);
            edit_blacklist_defined.setVisibility(View.GONE);
            button_save_blacklist.setVisibility(View.GONE);
        } else {
            button_blacklist.setBackground(getResources().getDrawable(R.drawable.my_button_white));
            button_blacklist_defined.setBackground(getResources().getDrawable(R.drawable.my_button_purple));
            button_blacklist.setTextColor(Color.BLACK);
            button_blacklist_defined.setTextColor(Color.WHITE);
            edit_blacklist_defined.setVisibility(View.VISIBLE);
            button_save_blacklist.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 系统monkey指令设置
     */
    private void system_monkey_text_setting() {
        if (CommonVariable.about_phone_cpu.contains("mt")) {
            system_monkey_text.setText(Constants.MonkeyCommand.MTK_SYSTEM_MONKEY);
        } else {
            system_monkey_text.setText(Constants.MonkeyCommand.SAMSUNG_SYSTEM_MONKEY);
        }
    }

    /**
     * 应用列表生成
     *
     * @return
     */
    private List<String> getPacketList() {
        packet_list = new ArrayList<String>();
        packet_list.add("请选择应用名");
        packet_list.add("视频");
        packet_list.add("音乐");
        packet_list.add("读书");
        packet_list.add("图库");
        packet_list.add("资讯");
        packet_list.add("Defined");
        return packet_list;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();   // 读取按键值id
        switch (id){
            case R.id.button_single_monkey:
                if(TextUtils.isEmpty(BaseData.getInstance(sApplicationContext)
                        .readStringData(SettingPreferenceKey.APP_TYPE))){
                    ToastHelper.addToast("请设置目标业务和邮箱！",
                            sApplicationContext);
                    Intent intent = new Intent(getActivity(), SettingActivity.class);
                    startActivity(intent);
                }else if(single_monkey_text.getText().equals("您尚未选择业务")){
                    ToastHelper.addToast("请先选择要执行monkey的应用！",
                            sApplicationContext);
                }else{
                    if(defined_monkey_value_flag){
                        if(CommonVariable.about_phone_cpu.contains("mt")){
                            if(TextUtils.isEmpty(edit_click_numbers.getText())
                                    || TextUtils.isEmpty(edit_click_time.getText())
                                    || TextUtils.isEmpty(edit_seed.getText())){
                                ToastHelper.addToast("请先补全参数！",
                                        sApplicationContext);
                            }else{
                                monkey_dialog();
                            }
                        }else {
                            if(TextUtils.isEmpty(edit_click_numbers.getText())
                                    || TextUtils.isEmpty(edit_click_time.getText())){
                                ToastHelper.addToast("请先补全参数！",
                                        sApplicationContext);
                            }else{
                                monkey_dialog();
                            }
                        }
                    }else{
                        monkey_dialog();
                    }
                }
                break;
            case R.id.button_system_monkey:
                monkey_dialog();
                break;
            case R.id.button_blacklist:
                blacklist_flag = true;
                blacklist_choose(blacklist_flag);
                break;
            case R.id.button_blacklist_defined:
                blacklist_flag = false;
                blacklist_choose(blacklist_flag);
                break;
            case R.id.button_save_blacklist:
                PublicMethod.saveStringToFile(edit_blacklist_defined.getText().toString(),
                        "blacklist_save.txt", iPublicConstants.MEMORY_BACK_UP);      // 保存黑名单
                ToastHelper.addToast("已保存黑名单", sApplicationContext);
                break;
            case R.id.button_single_choose:
                function_choose(1);
                break;
            case R.id.button_system_choose:
                function_choose(2);
                break;
            case R.id.bt_history:
                jumpHistoryAndTimeTask("执行历史");
                break;
            case R.id.bt_timetask:
                jumpHistoryAndTimeTask("定时任务");
                break;
            case R.id.btn_open_mtkLog:
                try {
                    Runtime.getRuntime().exec("am start -n com.mediatek.mtklogger/com.mediatek.mtklogger.MainActivity");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_choose_app:
                Intent appChooseIntent = new Intent(getActivity(), AppChooseActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("title", getResources().getString(R.string.choose_monkey_app));
                appChooseIntent.putExtras(bundle);
                startActivityForResult(appChooseIntent, 0);
                break;
            case R.id.btn_fail_and_send:
                if(PublicMethod.isServiceWorked(getActivity().getApplicationContext(), "com.meizu.testdevVideo.service.MonkeyService")){
                    ToastHelper.addToast("发送正在执行中...\n耐心等待，稍后重试", getActivity().getApplicationContext());
                }else{
                    MonkeyService.startActionRetryPostReport(getActivity().getApplicationContext());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setAppMonkeyText(monkeyApp, edit_seed.getText().toString(),
                edit_click_time.getText().toString(), edit_click_numbers.getText().toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 取出字符串
        if (data != null){
            Bundle bundle = data.getExtras();
            Log.d("MonkeyFragment", bundle.getString(SettingPreferenceKey.MONKEY_CHOOSE_APP));
            if(1 == function_choose){
                monkeyApp = bundle.getString(SettingPreferenceKey.MONKEY_CHOOSE_APP);
                BaseData.getInstance(sApplicationContext)
                        .writeStringData(SettingPreferenceKey.MONKEY_CHOOSE_APP, monkeyApp);
                checkbox_defined_value.setVisibility(View.VISIBLE);
                params.setMargins(16, 0, 16, 0);
                single_monkey_text.setLayoutParams(params);
                if(defined_monkey_value_flag){
                    setAppMonkeyText(monkeyApp, edit_seed.getText().toString(),
                            edit_click_time.getText().toString(), edit_click_numbers.getText().toString());
                }else{
                    setAppMonkeyText(monkeyApp, "1000", "500", "1200000000");
                }

            }else if (3 == function_choose){
                BaseData.getInstance(sApplicationContext)
                        .writeStringData(SettingPreferenceKey.MONKEY_DEFINED_CHOOSE_APP,
                                bundle.getString(SettingPreferenceKey.MONKEY_CHOOSE_APP));
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 填充MONKEY Text
     * @param monkeyApp 业务类型
     * @param seed 种子值
     * @param time 点击时延
     * @param number 点击数量
     */
    private void setAppMonkeyText(String monkeyApp, String seed, String time, String number){
        if(null == monkeyApp){
            monkeyApp = "";
        }
        if(CommonVariable.about_phone_cpu.contains("mt")){
            single_monkey_text.setText(Constants.MonkeyCommand.MTK_APP_MONKEY.replace("%ss", seed)
                    .replace("%ps", monkeyApp).replace("%ts", time).replace("%ns", number));
        }else {
            single_monkey_text.setText(Constants.MonkeyCommand.SAMSUNG_APP_MONKEY
                    .replace("%ps", monkeyApp).replace("%ts", time).replace("%ns", number));
        }
    }

    /**
     * 跳转页面选择
     */
    private void jumpHistoryAndTimeTask(String str){
        Intent intent = new Intent(getActivity(), HistoryAndTimeTaskActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("object", str);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    /**
     * 跑monkey弹框
     * 弹出对话框，选择
     */
    private void monkey_dialog() {
        // 存在更新包，删除
        File a = new File(iPublicConstants.LOCAL_MEMORY + "update.zip");
        if(a.exists()){
            ToastHelper.addToast("删除update.zip中！",
                    sApplicationContext);
            a.delete();
            ToastHelper.addToast("删除update.zip完成！",
                    sApplicationContext);
        }

        String restMem = PublicMethod.getMemoryInfo(getActivity().getApplicationContext(), Environment.getExternalStorageDirectory());
        // 三星手机或MTK手机，但内存大于MONKEY_PHONE_SIZE_NEED GB允许执行monkey
        if(!CommonVariable.about_phone_cpu.contains("mt")
                || (CommonVariable.about_phone_cpu.contains("mt") && restMem.contains("GB")
                && Float.parseFloat(restMem.replace(" GB", "")) > Constants.Monkey.MONKEY_PHONE_SIZE_NEED)){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            if(1 == function_choose){
                builder.setTitle("应用monkey").setMessage("确定执行吗？");
            }else if (2 == function_choose){
                builder.setTitle("系统monkey").setMessage("确定执行吗？");
            }

            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String strMonkeyType = null;
                    dialog.dismiss();

                    if(1 == function_choose){
                        strMonkeyType = "应用Monkey";
                        MonkeyTableData.getInstance(sApplicationContext).writeStringData("monkey_command",
                                single_monkey_text.getText().toString());
                    }else if (2 == function_choose){
                        strMonkeyType = "系统Monkey";
                        writeBlacklist(blacklist_flag);   // 根目录填写黑名单
                        MonkeyTableData.getInstance(sApplicationContext).writeStringData("monkey_command",
                                system_monkey_text.getText().toString());
                    }

                    MonkeyTableData.getInstance(sApplicationContext).writeStringData("strMonkeyType", strMonkeyType);

                    BaseData.getInstance(sApplicationContext).writeStringData("monkey_start_time",
                            String.valueOf(System.currentTimeMillis()));

                    PrefWidgetOnOff.getInstance(sApplicationContext).writeBooleanData("isMonkeyFloating",
                            sharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_FLOAT_BTN, true));

//                // 保存数据库
//                SqlAlterHelper.getInstance(getActivity()).addData(strMonkeyType,
//                        MonkeyTableData.getInstance(getActivity()).readStringData("monkey_command"),
//                        PublicMethod.getSystemTime(), sharedPreferences.getBoolean(SettingPreferenceKey.MUTE, false),
//                        sharedPreferences.getBoolean(SettingPreferenceKey.LOCK_WIFI, true),
//                        sharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_FLOAT_BTN, true));

                    sendBroadcast();

                    MonkeyTableData.getInstance(sApplicationContext).writeBooleanData("isStart",
                            !sharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_FLOAT_BTN, true));
                    String cpu = ShellUtil.getProperty("ro.hardware");
                    MonkeyService.stopActionMonkeyReport(getActivity());
                    MonkeyService.startActionMonkeyReport(getActivity(), null != cpu && cpu.contains("mt"));
                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        }else{
            ToastHelper.addToast("MTK手机剩余内存不足2G\n请删除大文件后再执行monkey",
                    sApplicationContext);
        }
    }


    /**
     * 执行monkey前的操作
     * 发送广播
     */
    private void sendBroadcast(){
        new Thread(new Runnable() {
            @Override
            public void run() {
            try {

                // 清除日志
                if(sharedPreferences.getBoolean(SettingPreferenceKey.CLEAR_LOG, true)){
                    if(CommonVariable.about_phone_cpu.contains("mt")){
                        PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "/mtklog");
                        PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "/Android/log");
                    }else{
                        PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "/Android/log");
                    }
                }

                // 以下对MTK工具有效
                Runtime.getRuntime().exec(CommonVariable.singleLogSizeBroadcast.replace("%s",
                        sharedPreferences.getString(SettingPreferenceKey.SINGLE_LOG_SIZE, "4096")));
                Thread.sleep(500);
                Runtime.getRuntime().exec(CommonVariable.allLogSizeBroadcast.replace("%s",
                        sharedPreferences.getString(SettingPreferenceKey.ALL_LOG_SIZE, "10000")));

                // 是否开启自动开启/停止抓log
                if(sharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_MTK_SET, true)){

                    if(CommonVariable.about_phone_cpu.contains("mt")){
                        if(sharedPreferences.getBoolean(SettingPreferenceKey.CATCH_LOG_TYPE, true)){
                            Runtime.getRuntime().exec(CommonVariable.mtkLogBroadcast.replace("%s", "start").replace("%d", "1"));
                        }else{
                            Runtime.getRuntime().exec(CommonVariable.mtkLogBroadcast.replace("%s", "start").replace("%d", "7"));
                        }
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            }
        }).start();
    }

    /**
     * 跑系统monkey时写入黑名单
     */
    private void writeBlacklist(boolean choose){

        if(choose){
            PublicMethod.copyAssetFile(sApplicationContext, "blacklist.txt", iPublicConstants.LOCAL_MEMORY);
        }else{
            PublicMethod.saveStringToFile(edit_blacklist_defined.getText().toString(),
                    "blacklist.txt", iPublicConstants.LOCAL_MEMORY);      // 复制文件到根目录
        }
    }


    @Override
    public void onResume(){
        Log.d(MonkeyFragment.class.getSimpleName(), "onResume");
        if(TextUtils.isEmpty(monkeyApp)){
            single_monkey_text.setText("您尚未选择业务");
            checkbox_defined_value.setVisibility(View.GONE);
        }else{
            checkbox_defined_value.setVisibility(View.VISIBLE);
            params.setMargins(16, 0, 16, 0);
            single_monkey_text.setLayoutParams(params);
            if(defined_monkey_value_flag){
                setAppMonkeyText(monkeyApp, edit_seed.getText().toString(),
                        edit_click_time.getText().toString(), edit_click_numbers.getText().toString());
            }else{
                setAppMonkeyText(monkeyApp, "1000", "500", "1200000000");
            }
        }

        if(FailPostRecordData.getInstance(getActivity().getApplicationContext())
                .readBooleanDataDefTrue(SettingPreferenceKey.IS_MONKEY_REPORT_SEND_SUCCESS)){
            layout_fail_send.setVisibility(View.GONE);
        }else{
            layout_fail_send.setVisibility(View.VISIBLE);
            txt_send_fail_reason.setText("失败原因："
                    + FailPostRecordData.getInstance(getActivity()
                    .getApplicationContext()).readStringData(SettingPreferenceKey.MONKEY_REPORT_SEND_FAIL_REASON));
        }
        super.onResume();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if ((view.getId() == R.id.edit_blacklist_defined && PublicMethod.canVerticalScroll(edit_blacklist_defined))) {
            // 点击EditText控件且可滚动，则拦截父控件控制
            view.getParent().requestDisallowInterceptTouchEvent(true);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // 手抬起来，则恢复父控件的控制
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        defined_monkey_value_flag = isChecked;

        if(isChecked){
            layout_tab_value.setVisibility(View.VISIBLE);
            layout_tab_value.startAnimation(openAnimation);
            setAppMonkeyText(monkeyApp, edit_seed.getText().toString(),
                    edit_click_time.getText().toString(), edit_click_numbers.getText().toString());
        }else{
            layout_tab_value.startAnimation(closeAnimation);
            setAppMonkeyText(monkeyApp, "1000", "500", "1200000000");
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        layout_tab_value.setVisibility(View.GONE);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
