package com.meizu.testdevVideo.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

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

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meizu.common.widget.TimePicker;
import com.meizu.common.widget.TimePickerDialog;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.activity.AppChooseActivity;
import com.meizu.testdevVideo.activity.HistoryAndTimeTaskActivity;

import com.meizu.testdevVideo.activity.SettingActivity;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.AlarmSetting;
import com.meizu.testdevVideo.library.AnimationHelper;

import com.meizu.testdevVideo.service.MonkeyService;
import com.meizu.testdevVideo.task.monkey.MonkeyUtils;
import com.meizu.testdevVideo.util.PublicMethodConstant;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.FailPostRecordData;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;

import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.library.ToastHelper;

import java.io.File;
import java.io.IOException;

import java.util.Calendar;
import java.util.Map;

import flyme.support.v7.app.AlertDialog;

public class MonkeyFragment extends Fragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, TextWatcher, View.OnTouchListener,
        Animation.AnimationListener{

    private static final String TAG = "MonkeyFragment";
    private static final int UPDATE_LOGREPORT_ATTENTION = 100;
    private static final int UPDATE_MONKEY_RETRY_SEND_PASS= 200;
    private static final int UPDATE_MONKEY_RETRY_SEND_FAIL = 400;
    private static final String SEND_FINISH = "action.st.send.monkey.over";
    private SharedPreferences sharedPreferences;
    private static Context sApplicationContext = null;
    private static final String ALARM_TEST_KEY = "alarm_text_key";
    private int function_choose;
    private Button button_single_monkey, button_save_blacklist, button_single_choose,
            button_system_choose, btn_open_mtkLog, btn_fail_and_send;
//    private Spinner smart_type_spinner;
    private ImageView alarm_minus_btn, alarm_add_btn, btn_question;
    private RelativeLayout btn_choose_app;
    private EditText edit_blacklist_defined, edit_seed, edit_click_time, edit_click_numbers, txt_alarm_time;
    private CheckBox checkbox_defined_value;
    private TextView single_monkey_text, system_monkey_text, txt_send_fail_reason, button_blacklist,
            button_blacklist_defined, btn_add_alarm_run_monkey, btn_detail_of_alarm_task, btn_clear_alarm_task;
    private LinearLayout layout_single_monkey, layout_system_monkey, layout_tab_value, layout_fail_send;
    private boolean blacklist_flag = true;               // 黑名单选择标志位
    private boolean defined_monkey_value_flag = false;    // 自定义参数选择标志位
    private LinearLayout.LayoutParams params = null;
    private String monkeyApp = "";
    private ScaleAnimation openAnimation;
    private ScaleAnimation closeAnimation;
    private View rootView;
    private String cpu;


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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Map<String, Object> apkMessage = PublicMethod.getApkMessage(sApplicationContext,
                            "com.meizu.logreport");
                    if(apkMessage == null || Integer.valueOf(apkMessage
                            .get(PublicMethodConstant.VERSION_CODE).toString()) < 3000000){
                        handler.sendEmptyMessage(UPDATE_LOGREPORT_ATTENTION);
                    }
                }
            }).start();
        }

        IntentFilter sendReportResultFilter = new IntentFilter();
        sendReportResultFilter.addAction(SEND_FINISH);
        getActivity().getApplicationContext()
                .registerReceiver(sendResultReceiver, sendReportResultFilter);

        return rootView;
    }

    /**
     * 控件初始化
     * @param view
     */
    private void findView(View view) {
        button_single_monkey = (Button) view.findViewById(R.id.button_single_monkey);
        button_blacklist = (TextView) view.findViewById(R.id.button_blacklist);
        btn_fail_and_send = (Button) view.findViewById(R.id.btn_fail_and_send);
        button_blacklist_defined = (TextView) view.findViewById(R.id.button_blacklist_defined);
        button_save_blacklist = (Button) view.findViewById(R.id.button_save_blacklist);
        button_single_choose = (Button) view.findViewById(R.id.button_single_choose);
        button_system_choose = (Button) view.findViewById(R.id.button_system_choose);
        btn_open_mtkLog = (Button) view.findViewById(R.id.btn_open_mtkLog);
        btn_choose_app = (RelativeLayout) view.findViewById(R.id.btn_choose_app);
        btn_add_alarm_run_monkey = (TextView) view.findViewById(R.id.btn_add_alarm_run_monkey);
        btn_detail_of_alarm_task = (TextView) view.findViewById(R.id.btn_detail_of_alarm_task);
        btn_clear_alarm_task = (TextView) view.findViewById(R.id.btn_clear_alarm_task);
        single_monkey_text = (TextView) view.findViewById(R.id.single_monkey_text);
        system_monkey_text = (TextView) view.findViewById(R.id.system_monkey_text);
        txt_send_fail_reason = (TextView) view.findViewById(R.id.txt_send_fail_reason);
        checkbox_defined_value = (CheckBox) view.findViewById(R.id.checkbox_defined_value);
        layout_single_monkey = (LinearLayout) view.findViewById(R.id.layout_single_monkey);
        layout_system_monkey = (LinearLayout) view.findViewById(R.id.layout_system_monkey);
        layout_fail_send = (LinearLayout) view.findViewById(R.id.layout_fail_send);
        layout_tab_value = (LinearLayout) view.findViewById(R.id.layout_tab_value);
        alarm_minus_btn = (ImageView) view.findViewById(R.id.alarm_minus_btn);
        alarm_add_btn = (ImageView) view.findViewById(R.id.alarm_add_btn);
        btn_question = (ImageView) view.findViewById(R.id.btn_question);
        edit_blacklist_defined = (EditText) view.findViewById(R.id.edit_blacklist_defined);
        edit_seed = (EditText) view.findViewById(R.id.edit_seed);
        txt_alarm_time = (EditText) view.findViewById(R.id.txt_alarm_time);
        edit_click_numbers = (EditText) view.findViewById(R.id.edit_click_numbers);
        edit_click_time = (EditText) view.findViewById(R.id.edit_click_time);

        button_save_blacklist.setVisibility(View.GONE);
        edit_blacklist_defined.setVisibility(View.GONE);
        layout_system_monkey.setVisibility(View.GONE);
        cpu = BaseData.getInstance(sApplicationContext).readStringData("CPU");

        if(!cpu.contains("mt")){
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
        button_blacklist.setOnClickListener(this);
        button_blacklist_defined.setOnClickListener(this);
        button_save_blacklist.setOnClickListener(this);
        button_single_choose.setOnClickListener(this);
        button_system_choose.setOnClickListener(this);
        btn_fail_and_send.setOnClickListener(this);
        btn_open_mtkLog.setOnClickListener(this);
        btn_choose_app.setOnClickListener(this);
        alarm_minus_btn.setOnClickListener(this);
        alarm_add_btn.setOnClickListener(this);
        btn_question.setOnClickListener(this);
        btn_add_alarm_run_monkey.setOnClickListener(this);
        btn_detail_of_alarm_task.setOnClickListener(this);
        btn_clear_alarm_task.setOnClickListener(this);
        edit_seed.addTextChangedListener(this);
        edit_click_time.addTextChangedListener(this);
        edit_click_numbers.addTextChangedListener(this);
        checkbox_defined_value.setOnCheckedChangeListener(this);
//        String[] smartTypeItem = getResources().getStringArray(R.array.smart_type);
//        ArrayAdapter<String> smartTypeAdapter=new ArrayAdapter<String>(sApplicationContext, android.R.layout.simple_spinner_item, smartTypeItem);
//        smart_type_spinner.setAdapter(smartTypeAdapter);

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

        String alarm_txt = BaseData.getInstance(getActivity()
                .getApplicationContext()).readStringData(ALARM_TEST_KEY);

        if(null != alarm_txt){
            txt_alarm_time.setText(alarm_txt);
        }else{
            BaseData.getInstance(sApplicationContext).writeStringData(ALARM_TEST_KEY, "");
        }

        txt_alarm_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String alarmText = BaseData.getInstance(sApplicationContext).readStringData(ALARM_TEST_KEY);

                try {
                    if(null != alarmText && !alarmText.equals(s.toString())){
                        String stop_date = txt_alarm_time.getText().toString();
                        if(!TextUtils.isEmpty(stop_date) && PublicMethod.isNumeric(stop_date)){
                            BaseData.getInstance(sApplicationContext).writeStringData(ALARM_TEST_KEY, txt_alarm_time.getText().toString());
                            ToastHelper.addToast("预计Monkey停止日期\n" + PublicMethod.dateFormatTimes(System.currentTimeMillis()
                                    + (long)Math.round(Float.parseFloat(stop_date) * Constants.TIME.MINUTES_OF_HOUR
                                    * Constants.TIME.SECONDS_OF_MINUTE * Constants.TIME.MILLS_OF_SECOND)), sApplicationContext);
                        }else{
                            ToastHelper.addToast("请输入有效数字", sApplicationContext);
                        }
                    }
                }catch (NumberFormatException e){
                    ToastHelper.addToast("数字格式不正确，请检查\n" + e, sApplicationContext);
                }

            }
        });

        // 读取保存的黑名单进编辑框
        edit_blacklist_defined.setText(PublicMethod.
                readFile(iPublicConstants.MEMORY_BACK_UP + "blacklist_save.txt"));
        edit_blacklist_defined.setOnTouchListener(this);

        // 系统monkey_text设置
        system_monkey_text_setting();
    }

    private BroadcastReceiver sendResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean result = intent.getBooleanExtra("result", true);
            if (null != action && action.equals(SEND_FINISH)) {
                if(result){
                    handler.sendEmptyMessage(UPDATE_MONKEY_RETRY_SEND_PASS);
                }else{
                    handler.sendEmptyMessage(UPDATE_MONKEY_RETRY_SEND_FAIL);
                }
            }
        }
    };

    /**
     * 布局选择函数
     */
    private void function_choose(int layout_choose) {
        function_choose = layout_choose;
        switch (layout_choose) {
            case 1:
                button_single_choose.setTextColor(getResources().getColor(R.color.mz_theme_color_limegreen));
                button_system_choose.setTextColor(Color.BLACK);
                button_single_choose.setTextSize(16);
                button_system_choose.setTextSize(13);
                layout_single_monkey.setVisibility(View.VISIBLE);
                layout_system_monkey.setVisibility(View.GONE);
                break;
            case 2:
                button_single_choose.setTextColor(Color.BLACK);
                button_system_choose.setTextColor(getResources().getColor(R.color.mz_theme_color_limegreen));
                button_single_choose.setTextSize(13);
                button_system_choose.setTextSize(16);
                layout_single_monkey.setVisibility(View.GONE);
                layout_system_monkey.setVisibility(View.VISIBLE);
//                break;
//            case 3:
//                button_single_choose.setTextColor(Color.BLACK);
//                button_system_choose.setTextColor(Color.BLACK);
//                button_smartTest_choose.setTextColor(getResources().getColor(R.color.mz_theme_color_limegreen));
//                button_single_choose.setTextSize(13);
//                button_system_choose.setTextSize(13);
//                button_smartTest_choose.setTextSize(16);
//                layout_single_monkey.setVisibility(View.GONE);
//                layout_system_monkey.setVisibility(View.GONE);
//                break;
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
            button_blacklist.setTextColor(getResources().getColor(R.color.ThemeColor));
            button_blacklist_defined.setTextColor(Color.BLACK);
            edit_blacklist_defined.setVisibility(View.GONE);
            button_save_blacklist.setVisibility(View.GONE);
        } else {
            button_blacklist.setTextColor(Color.BLACK);
            button_blacklist_defined.setTextColor(getResources().getColor(R.color.ThemeColor));
            edit_blacklist_defined.setVisibility(View.VISIBLE);
            button_save_blacklist.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 系统monkey指令设置
     */
    private void system_monkey_text_setting() {
        if (cpu.contains("mt")) {
            system_monkey_text.setText(Constants.MonkeyCommand.MTK_SYSTEM_MONKEY);
        } else {
            system_monkey_text.setText(Constants.MonkeyCommand.SAMSUNG_SYSTEM_MONKEY);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();   // 读取按键值id
        if(MonkeyTableData.getInstance(sApplicationContext).readBooleanData(Constants.Monkey.IS_START)){
            ToastHelper.addToast("Monkey执行中，按钮失效！", getActivity().getApplicationContext());
        }else {
            switch (id){
                case R.id.button_single_monkey:
                    if(1 == function_choose){
                        if(TextUtils.isEmpty(BaseData.getInstance(sApplicationContext)
                                .readStringData(SettingPreferenceKey.APP_TYPE))){
                            ToastHelper.addToast("请设置目标业务和邮箱！",
                                    sApplicationContext);
                            Intent intent = new Intent(getActivity(), SettingActivity.class);
                            startActivity(intent);
                        }else if(single_monkey_text.getText().equals("您尚未选择业务")){
                            ToastHelper.addToast("请先选择要执行monkey的应用！",
                                    sApplicationContext);
                        }else {
                            if (defined_monkey_value_flag) {
                                if (cpu.contains("mt")) {
                                    if (TextUtils.isEmpty(edit_click_numbers.getText())
                                            || TextUtils.isEmpty(edit_click_time.getText())
                                            || TextUtils.isEmpty(edit_seed.getText())) {
                                        ToastHelper.addToast("请先补全参数！",
                                                sApplicationContext);
                                    } else {
                                        monkey_dialog();
                                    }
                                } else {
                                    if (TextUtils.isEmpty(edit_click_numbers.getText())
                                            || TextUtils.isEmpty(edit_click_time.getText())) {
                                        ToastHelper.addToast("请先补全参数！",
                                                sApplicationContext);
                                    } else {
                                        monkey_dialog();
                                    }
                                }
                            } else {
                                monkey_dialog();
                            }
                        }
                    }else{
                        monkey_dialog();
                    }
                    break;
                case R.id.button_blacklist:
                    blacklist_choose(blacklist_flag = true);
                    break;
                case R.id.button_blacklist_defined:
                    blacklist_choose(blacklist_flag = false);
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
//                case R.id.button_smartTest_choose:
//                    function_choose(3);
//                    break;
//                case R.id.bt_history:
//                    jumpHistoryAndTimeTask("执行历史");
//                    break;
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
                        MonkeyService.startActionRetryPostReport(sApplicationContext);
                        btn_fail_and_send.setEnabled(false);
                        btn_fail_and_send.setText("发送中..");
                    }
                    break;
                case R.id.alarm_add_btn:
                    txt_alarm_time.setText(String.valueOf(Float.parseFloat(txt_alarm_time.getText()
                            .toString().equals("")? "0" : txt_alarm_time.getText().toString()) + 1F));
                    break;
                case R.id.alarm_minus_btn:
                    float currentNumber = Float.parseFloat(txt_alarm_time.getText()
                            .toString().equals("")? "0" : txt_alarm_time.getText().toString());
                    if(currentNumber < 1F ){
                        ToastHelper.addToast("时长不得小于0", getActivity().getApplicationContext());
                    }else{
                        txt_alarm_time.setText(String.valueOf(currentNumber - 1F));
                    }
                    break;
                case R.id.btn_add_alarm_run_monkey:

                    if(TextUtils.isEmpty(BaseData.getInstance(sApplicationContext)
                            .readStringData(SettingPreferenceKey.APP_TYPE))){
                        ToastHelper.addToast("请设置目标业务和邮箱！",
                                sApplicationContext);
                        Intent intent = new Intent(getActivity(), SettingActivity.class);
                        startActivity(intent);
                    }else if(1 == function_choose && single_monkey_text.getText().equals("您尚未选择业务")){
                        ToastHelper.addToast("请先选择要执行monkey的应用！",
                                sApplicationContext);
                    }else if(1== function_choose && cpu.contains("mt")
                            && defined_monkey_value_flag
                            && (TextUtils.isEmpty(edit_click_numbers.getText())
                            || TextUtils.isEmpty(edit_click_time.getText())
                            || TextUtils.isEmpty(edit_seed.getText()))) {
                        ToastHelper.addToast("请先补全参数！", sApplicationContext);
                    }else if(1== function_choose && !cpu.contains("mt")
                            && defined_monkey_value_flag
                            && (TextUtils.isEmpty(edit_click_numbers.getText())
                            || TextUtils.isEmpty(edit_click_time.getText()))) {
                        ToastHelper.addToast("请先补全参数！", sApplicationContext);
                    } else{
                        final String monkeyType = (1 == function_choose)? "应用级" : "系统级";
                        final String monkeyCommand = (1 == function_choose)? single_monkey_text.getText().toString()
                                : system_monkey_text.getText().toString();
                        final long runMonkeyTime = (long)Math.round(Float.parseFloat(txt_alarm_time.getText().toString())
                                * Constants.TIME.MINUTES_OF_HOUR * Constants.TIME.SECONDS_OF_MINUTE * Constants.TIME.MILLS_OF_SECOND);

                        StringBuilder dialog_txt = new StringBuilder();
                        dialog_txt.append("\n").append("任务类型: ").append(monkeyType).append("\n").append("执行指令: ")
                                .append(monkeyCommand).append("\n").append("执行时长: ")
                                .append(txt_alarm_time.getText().toString()).append("小时").append("\n");

                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("定时任务内容").setMessage(dialog_txt.toString())
                                .setPositiveButton("下一步", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Log.d(TAG, "下一步，选中定时时间");
                                        // 选择定时时间段
                                        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                ToastHelper.addToast("设置成功", getActivity().getApplicationContext());
                                                // 获取到小时和分钟,转化为毫秒存进内存
                                                MonkeyTableData.setAlarmStartTime(sApplicationContext,
                                                        (hourOfDay * Constants.TIME.MINUTES_OF_HOUR + minute)
                                                                * Constants.TIME.SECONDS_OF_MINUTE * Constants.TIME.MILLS_OF_SECOND);

                                                MonkeyTableData.setAlarmMonkeyType(sApplicationContext, monkeyType);
                                                MonkeyTableData.setAlarmMonkeyCommand(sApplicationContext, monkeyCommand);
                                                MonkeyTableData.setAlarmRunTime(sApplicationContext, runMonkeyTime);

                                                // 设置定时任务
                                                Calendar c = Calendar.getInstance();
                                                long currentHourMinuteToMills = (c.get(Calendar.HOUR_OF_DAY)
                                                        * Constants.TIME.MINUTES_OF_HOUR + c.get(Calendar.MINUTE))
                                                        * Constants.TIME.SECONDS_OF_MINUTE * Constants.TIME.MILLS_OF_SECOND;

                                                long alarmStartTime = MonkeyTableData.getAlarmStartTime(sApplicationContext);
                                                long leaveTime = alarmStartTime - currentHourMinuteToMills;
                                                long setAlarmStartTime = leaveTime > 0 ? (System.currentTimeMillis() + leaveTime)
                                                        : (System.currentTimeMillis() + leaveTime + PublicMethod.getDayMills());

                                                AlarmSetting.getInstance().setRepeatAlarm(sApplicationContext,
                                                        Constants.Monkey.ACTION_SET_MONKEY_RUN_REPEAT_TASK, setAlarmStartTime);
                                                ToastHelper.addToast("定时任务设置完毕\n不需点击执行按钮\n时间到达，自动执行", sApplicationContext);
                                            }
                                        }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);
                                        timePickerDialog.setTitle("选择定时时间");
                                        timePickerDialog.show();
                                    }
                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                 });
                            builder.create().show();
                        }
                    break;

                case R.id.btn_clear_alarm_task:
                    // 取消定时闹钟
                    AlarmSetting.getInstance().canalAlarm(sApplicationContext, Constants.Monkey.ACTION_SET_MONKEY_RUN_REPEAT_TASK);
                    MonkeyTableData.setAlarmStartTime(sApplicationContext, 0);
                    MonkeyTableData.setAlarmMonkeyType(sApplicationContext, "");
                    MonkeyTableData.setAlarmMonkeyCommand(sApplicationContext, "");
                    MonkeyTableData.setAlarmRunTime(sApplicationContext, 0);
                    ToastHelper.addToast("取消定时任务成功", sApplicationContext);
                    break;

                case R.id.btn_detail_of_alarm_task:
                    long startTime = MonkeyTableData.getAlarmStartTime(sApplicationContext)
                            / (Constants.TIME.SECONDS_OF_MINUTE * Constants.TIME.MILLS_OF_SECOND);
                    int hours = (int) Math.floor(startTime / Constants.TIME.MINUTES_OF_HOUR);
                    int minutes = (int) startTime % Constants.TIME.MINUTES_OF_HOUR;
                    long runTime = MonkeyTableData.getAlarmRunTime(sApplicationContext);
                    float time = ((float)runTime) / (Constants.TIME.MINUTES_OF_HOUR
                            * Constants.TIME.SECONDS_OF_MINUTE * Constants.TIME.MILLS_OF_SECOND);

                    StringBuilder toast_detail = new StringBuilder();
                    toast_detail.append("任务类型: ").append(MonkeyTableData.getAlarmMonkeyType(sApplicationContext))
                            .append("\n").append("任务开始时间: ").append("每天").append(hours).append("点")
                            .append(minutes).append("分").append("\n").append("执行指令: ")
                            .append(MonkeyTableData.getAlarmMonkeyCommand(sApplicationContext))
                            .append("\n").append("执行时长: ").append(String.valueOf(time)).append("小时");
                    ToastHelper.addToast(toast_detail.toString(), sApplicationContext);

                    break;

                case R.id.btn_question:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.question_info))
                            .setMessage(getString(R.string.question_message_monkey))
                            .setNeutralButton("我知道了", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        try{
            if(checkbox_defined_value.isChecked() && (1 == function_choose)){
                setAppMonkeyText(monkeyApp, edit_seed.getText().toString(),
                        edit_click_time.getText().toString(), edit_click_numbers.getText().toString());
            }
        }catch (NumberFormatException e){
            ToastHelper.addToast("数字格式不正确，请检查\n" + e, sApplicationContext);
        }
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
        if(cpu.contains("mt")){
            single_monkey_text.setText(String.format(Constants.MonkeyCommand
                    .MTK_APP_MONKEY, seed, monkeyApp, time, number));
        }else {
            single_monkey_text.setText(String.format(Constants.MonkeyCommand
                    .SAMSUNG_APP_MONKEY, monkeyApp, time, number));
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
        if(!cpu.contains("mt") || (cpu.contains("mt") && restMem.contains("GB")
                && Float.parseFloat(restMem.replace(" GB", "")) > Constants.Monkey.MONKEY_PHONE_SIZE_NEED)){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            String appendString = "";
            String taskType = "";

            if(!sharedPreferences.getBoolean(SettingPreferenceKey.LOCK_WIFI, false)){
                appendString = "还未进行WiFi绑定，可能造成Monkey过程WiFi断开，后期报告没有上传的BUG\n";
            }

            taskType = (1 == function_choose)? "应用级" : "系统级";
            builder.setTitle(taskType).setMessage(appendString + "确定执行吗？");
            final String finalTaskType = taskType;

            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String monkeyCommand = null;
                    long stopMonkeyTime = 0;
                    dialog.dismiss();

                    if(1 == function_choose){
                        monkeyCommand = single_monkey_text.getText().toString();
                    }else if (2 == function_choose){
                        writeBlacklist(blacklist_flag);   // 根目录填写黑名单
                        monkeyCommand = system_monkey_text.getText().toString();
                    }

                    stopMonkeyTime = System.currentTimeMillis()
                            + (long)Math.round(Float.parseFloat(txt_alarm_time.getText().toString())
                            * Constants.TIME.MINUTES_OF_HOUR * Constants.TIME.SECONDS_OF_MINUTE * Constants.TIME.MILLS_OF_SECOND);

                    // Monkey设置执行相关
                    MonkeyUtils.setStartMonkeyParams(finalTaskType, monkeyCommand,
                            stopMonkeyTime, Constants.Monkey.LABEL_OF_ACTION_MONKEY_REPORT);
                    // 获取Monkey任务id
                    MonkeyUtils.getMonkeyId(finalTaskType);
                    // 跑Monkey前初始化
                    MonkeyUtils.runMonkeyInit(sApplicationContext);
                    // 开始Monkey服务
                    MonkeyUtils.startMonkeyService(getActivity());
                    // 设置Monkey停止时间
                    AlarmSetting.getInstance().setOnceAlarm(sApplicationContext, Constants.Monkey.ACTION_KILL_MONKEY, stopMonkeyTime);
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

        if(FailPostRecordData.getInstance(getActivity().getApplicationContext())
                .readBooleanDataDefTrue(SettingPreferenceKey.IS_MONKEY_REPORT_SEND_SUCCESS)){
            layout_fail_send.setVisibility(View.GONE);
        }else{
            if(!PublicMethod.isServiceWorked(sApplicationContext, "com.meizu.testdevVideo.service.MonkeyService")){
                btn_fail_and_send.setText("重新发送");
                btn_fail_and_send.setEnabled(true);
            }
            layout_fail_send.setVisibility(View.VISIBLE);
            txt_send_fail_reason.setText("失败原因："
                    + FailPostRecordData.getInstance(getActivity()
                    .getApplicationContext()).readStringData(SettingPreferenceKey.MONKEY_REPORT_SEND_FAIL_REASON));
        }
        super.onResume();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case UPDATE_LOGREPORT_ATTENTION:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("警告").setMessage("\n注意: 此机型ST静默安" +
                            "装LogReport失败，请手动升级！\n\n" +
                            "否则会导致发送报告无法采集Log，最终发送失败，" +
                            "下载路径：常用→常用工具→业务更新→LogReport")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();

                    break;

                case UPDATE_MONKEY_RETRY_SEND_PASS:
                    layout_fail_send.setVisibility(View.GONE);
                    break;

                case UPDATE_MONKEY_RETRY_SEND_FAIL:
                    btn_fail_and_send.setEnabled(true);
                    btn_fail_and_send.setText("重新发送");
                    txt_send_fail_reason.setText("失败原因："
                            + FailPostRecordData.getInstance(getActivity()
                            .getApplicationContext()).readStringData(SettingPreferenceKey.MONKEY_REPORT_SEND_FAIL_REASON));
                    break;
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().getApplicationContext().unregisterReceiver(sendResultReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
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
