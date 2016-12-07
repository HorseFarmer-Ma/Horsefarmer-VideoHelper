package com.meizu.testdevVideo.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;

import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.activity.HistoryAndTimeTaskActivity;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.SqlAlterHelper;

import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.sharepreference.PrefWidgetOnOff;
import com.meizu.testdevVideo.service.SuperTestService;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.library.ToastHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonkeyFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private static List<String> packet_list;
    private ArrayAdapter<String> myAdapter;

    private int function_choose;

    private Button button_single_monkey, button_system_monkey, button_defined_monkey, button_copy_single_monkey,
            button_copy_system_monkey, button_blacklist, button_blacklist_defined, button_save_blacklist, button_single_choose,
            button_system_choose, button_defined_choose, bt_timetask, bt_history, btn_open_mtkLog;

    private Spinner packet_spinner;
    private TextView single_monkey_text, system_monkey_text;
    private EditText defined_monkey_text, edit_blacklist_defined, edit_monkey_defined, edit_package_name,
            edit_seed_number, edit_click_time, edit_click_number;
    private LinearLayout layout_single_monkey, layout_system_monkey, layout_defined_monkey;
    private boolean single_monkey_text_flag = false;
    private boolean system_monkey_text_flag = false;
    private boolean blacklist_flag = true;   // 黑名单选择标志位

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        long i = SystemClock.currentThreadTimeMillis();
        View view = inflater.inflate(R.layout.fragment_monkey, container, false);
        widget_init(view);   // 控件初始化
        loadData();   // 加载保存的数据
        monkey_fragment_init();     // 页面按钮监听初始化
        function_choose(1);         // 功能选择初始化
        Log.e("MonkeyFragment", "初始化时间为:" + (SystemClock.currentThreadTimeMillis() - i));
        return view;
    }

    /**
     * 控件初始化
     * @param view
     */
    private void widget_init(View view) {
        button_single_monkey = (Button) view.findViewById(R.id.button_single_monkey);
        button_system_monkey = (Button) view.findViewById(R.id.button_system_monkey);
        button_defined_monkey = (Button) view.findViewById(R.id.button_defined_monkey);
        button_copy_single_monkey = (Button) view.findViewById(R.id.button_copy_single_monkey);
        button_copy_system_monkey = (Button) view.findViewById(R.id.button_copy_system_monkey);
        button_blacklist = (Button) view.findViewById(R.id.button_blacklist);
        button_blacklist_defined = (Button) view.findViewById(R.id.button_blacklist_defined);
        button_save_blacklist = (Button) view.findViewById(R.id.button_save_blacklist);
        button_single_choose = (Button) view.findViewById(R.id.button_single_choose);
        button_system_choose = (Button) view.findViewById(R.id.button_system_choose);
        button_defined_choose = (Button) view.findViewById(R.id.button_defined_choose);
        btn_open_mtkLog = (Button) view.findViewById(R.id.btn_open_mtkLog);
        bt_history = (Button) view.findViewById(R.id.bt_history);
        bt_timetask = (Button) view.findViewById(R.id.bt_timetask);

        edit_blacklist_defined = (EditText) view.findViewById(R.id.edit_blacklist_defined);
        edit_monkey_defined = (EditText) view.findViewById(R.id.edit_monkey_defined);
        // 自定义monkey输入框
        edit_package_name = (EditText) view.findViewById(R.id.edit_package_name);
        edit_seed_number = (EditText) view.findViewById(R.id.edit_seed_number);
        edit_click_time = (EditText) view.findViewById(R.id.edit_click_time);
        edit_click_number = (EditText) view.findViewById(R.id.edit_click_number);

        packet_spinner = (Spinner) view.findViewById(R.id.spinner_packet_choose);
        single_monkey_text = (TextView) view.findViewById(R.id.single_monkey_text);
        system_monkey_text = (TextView) view.findViewById(R.id.system_monkey_text);
        defined_monkey_text = (EditText) view.findViewById(R.id.defined_monkey_text);
        layout_single_monkey = (LinearLayout) view.findViewById(R.id.layout_single_monkey);
        layout_system_monkey = (LinearLayout) view.findViewById(R.id.layout_system_monkey);
        layout_defined_monkey = (LinearLayout) view.findViewById(R.id.layout_defined_monkey);
        single_monkey_text.setVisibility(View.GONE);
        button_copy_single_monkey.setVisibility(View.GONE);
        button_copy_system_monkey.setVisibility(View.GONE);
        button_save_blacklist.setVisibility(View.GONE);
        edit_blacklist_defined.setVisibility(View.GONE);
        layout_system_monkey.setVisibility(View.GONE);
        layout_defined_monkey.setVisibility(View.GONE);
        edit_monkey_defined.setVisibility(View.GONE);
        single_monkey_text.setMaxLines(1);
        system_monkey_text.setMaxLines(1);
    }

    /**
     * 页面按钮监听初始化
     */
    private void monkey_fragment_init() {
        // 监听设置
        ClickListener clickListener = new ClickListener();
        button_single_monkey.setOnClickListener(clickListener);
        button_system_monkey.setOnClickListener(clickListener);
        button_defined_monkey.setOnClickListener(clickListener);
        button_copy_single_monkey.setOnClickListener(clickListener);
        button_copy_system_monkey.setOnClickListener(clickListener);
        button_blacklist.setOnClickListener(clickListener);
        button_blacklist_defined.setOnClickListener(clickListener);
        button_save_blacklist.setOnClickListener(clickListener);
        button_single_choose.setOnClickListener(clickListener);
        button_system_choose.setOnClickListener(clickListener);
        button_defined_choose.setOnClickListener(clickListener);
        bt_history.setOnClickListener(clickListener);
        bt_timetask.setOnClickListener(clickListener);
        btn_open_mtkLog.setOnClickListener(clickListener);


        // 读取保存的黑名单进编辑框
        edit_blacklist_defined.setText(PublicMethod.
                readFile(iPublicConstants.MEMORY_BACK_UP + "blacklist_save.txt"));

        // 单个
        single_monkey_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonVariable.packet_choose.equals("")) {
                    single_monkey_text_flag = true;
                }
                single_monkey_text_flag = !single_monkey_text_flag;

                if (single_monkey_text_flag) {
                    single_monkey_text.setMaxLines(8);
                    button_copy_single_monkey.setVisibility(View.VISIBLE);
                } else {
                    single_monkey_text.setMaxLines(1);
                    button_copy_single_monkey.setVisibility(View.GONE);
                }
            }
        });

        // 系统monkey
        system_monkey_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                system_monkey_text_flag = !system_monkey_text_flag;
                if (system_monkey_text_flag) {
                    system_monkey_text.setMaxLines(9);
                    button_copy_system_monkey.setVisibility(View.VISIBLE);
                } else {
                    system_monkey_text.setMaxLines(1);
                    button_copy_system_monkey.setVisibility(View.GONE);
                }
            }
        });

        edit_monkey_defined.addTextChangedListener(textWatcher);
        edit_package_name.addTextChangedListener(textWatcher);
        edit_seed_number.addTextChangedListener(textWatcher);
        edit_click_time.addTextChangedListener(textWatcher);
        edit_click_number.addTextChangedListener(textWatcher);

        // 系统monkey_text设置
        system_monkey_text_setting();

        //适配器
        myAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getPacketList());
        //设置样式
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        packet_spinner.setAdapter(myAdapter);
        packet_spinner.setOnItemSelectedListener(new SpinnerOnSelectedListener());
    }

    /**
     * 监听自定义monkey包名变化
     */
    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (CommonVariable.snLabel.contains("71") || CommonVariable.snLabel.contains("76") || CommonVariable.snLabel.contains("86")
                    || CommonVariable.snLabel.contains("96")) {
                if(1 == function_choose){
                    single_monkey_text.setText("指令：" + "monkey -p " +
                            edit_monkey_defined.getText() +
                            "  --ignore-crashes --ignore-timeouts --ignore" +
                            "-security-exceptions --kill-process-after-error " +
                            "--pct-trackball 0 --pct-nav 0 --pct-majornav 0 " +
                            "--pct-anyevent 0 -v -v -v --throttle 500 " +
                            "1200000000 > /sdcard/monkeytest.log 2>&1 &");
                }else if(3 == function_choose){
                    defined_monkey_text.setText("monkey -p " +
                            edit_package_name.getText() +
                            "  --ignore-crashes --ignore-timeouts --ignore" +
                            "-security-exceptions --kill-process-after-error " +
                            "--pct-trackball 0 --pct-nav 0 --pct-majornav 0 " +
                            "--pct-anyevent 0 -v -v -v --throttle " + edit_click_time.getText()
                            + " " + edit_click_number.getText() + " > /sdcard/monkeytest.log 2>&1 &");

                }

            } else {
                if(1 == function_choose){
                    single_monkey_text.setText("指令：" + "monkey -s 1000 -p "
                            + edit_monkey_defined.getText()
                            + " --ignore-crashes --ignore-timeouts --kill-process"
                            + "-after-error --ignore-security-exceptions --pct-trackball "
                            + "0 --pct-nav 0 -v -v -v --throttle 500 1200000000"
                            + " > /sdcard/monkeytest.log 2>&1 &");
                }else if(3 == function_choose){
                    defined_monkey_text.setText("monkey -s " + edit_seed_number.getText() + " -p "
                            + edit_package_name.getText()
                            + " --ignore-crashes --ignore-timeouts --kill-process"
                            + "-after-error --ignore-security-exceptions --pct-trackball "
                            + "0 --pct-nav 0 -v -v -v --throttle " + edit_click_time.getText()
                            + " " + edit_click_number.getText() +" > /sdcard/monkeytest.log 2>&1 &");
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    // 存储数据
    private boolean saveData() {
        // 实例化SharedPreferences对象
        SharedPreferences mySharedPreferences= getActivity().getSharedPreferences("defined_monkey",
                Activity.MODE_PRIVATE);
        // 实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.remove("edit_click_number");
        editor.remove("edit_click_time");
        editor.remove("edit_seed_number");
        editor.remove("edit_package_name");
        editor.putString("edit_click_number", edit_click_number.getText().toString());
        editor.putString("edit_click_time", edit_click_time.getText().toString());
        editor.putString("edit_seed_number", edit_seed_number.getText().toString());
        editor.putString("edit_package_name", edit_package_name.getText().toString());
        // 提交当前数据
        return editor.commit();
    }

    // 加载表中数据
    private void loadData() {
        SharedPreferences mSharedPreference1 = getActivity().getSharedPreferences("defined_monkey",
                Activity.MODE_PRIVATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        edit_seed_number.setText(mSharedPreference1.getString("edit_seed_number", null));
        edit_package_name.setText(mSharedPreference1.getString("edit_package_name", null));
        edit_click_time.setText(mSharedPreference1.getString("edit_click_time", null));
        edit_click_number.setText(mSharedPreference1.getString("edit_click_number", null));

        if (CommonVariable.snLabel.contains("71") || CommonVariable.snLabel.contains("76") || CommonVariable.snLabel.contains("86")
                || CommonVariable.snLabel.contains("96")) {
            defined_monkey_text.setText("monkey -p " +
                    mSharedPreference1.getString("edit_package_name", null) +
                    "  --ignore-crashes --ignore-timeouts --ignore" +
                    "-security-exceptions --kill-process-after-error " +
                    "--pct-trackball 0 --pct-nav 0 --pct-majornav 0 " +
                    "--pct-anyevent 0 -v -v -v --throttle " + mSharedPreference1.getString("edit_click_time", null)
                    + " " + mSharedPreference1.getString("edit_click_number", null) + " > /sdcard/monkeytest.log 2>&1 &");
        } else {
            defined_monkey_text.setText("monkey -s " + mSharedPreference1.getString("edit_seed_number", null) + " -p "
                    + mSharedPreference1.getString("edit_package_name", null)
                    + " --ignore-crashes --ignore-timeouts --kill-process"
                    + "-after-error --ignore-security-exceptions --pct-trackball "
                    + "0 --pct-nav 0 -v -v -v --throttle " + mSharedPreference1.getString("edit_click_time", null)
                    + " " + mSharedPreference1.getString("edit_click_number", null) +" > /sdcard/monkeytest.log 2>&1 &");

        }
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
                button_defined_choose.setBackground(getResources().getDrawable(R.drawable.my_function_choose_button2));
                button_single_choose.setTextColor(Color.WHITE);
                button_system_choose.setTextColor(Color.BLACK);
                button_defined_choose.setTextColor(Color.BLACK);
                layout_single_monkey.setVisibility(View.VISIBLE);
                layout_system_monkey.setVisibility(View.GONE);
                layout_defined_monkey.setVisibility(View.GONE);
                break;
            case 2:
                button_single_choose.setBackground(getResources().getDrawable(R.drawable.my_function_choose_button2));
                button_system_choose.setBackground(getResources().getDrawable(R.drawable.my_function_choose_button1));
                button_defined_choose.setBackground(getResources().getDrawable(R.drawable.my_function_choose_button2));
                button_single_choose.setTextColor(Color.BLACK);
                button_system_choose.setTextColor(Color.WHITE);
                button_defined_choose.setTextColor(Color.BLACK);

                layout_single_monkey.setVisibility(View.GONE);
                layout_system_monkey.setVisibility(View.VISIBLE);
                layout_defined_monkey.setVisibility(View.GONE);
                break;
            case 3:
                button_single_choose.setBackground(getResources().getDrawable(R.drawable.my_function_choose_button2));
                button_system_choose.setBackground(getResources().getDrawable(R.drawable.my_function_choose_button2));
                button_defined_choose.setBackground(getResources().getDrawable(R.drawable.my_function_choose_button1));
                button_single_choose.setTextColor(Color.BLACK);
                button_system_choose.setTextColor(Color.BLACK);
                button_defined_choose.setTextColor(Color.WHITE);

                layout_single_monkey.setVisibility(View.GONE);
                layout_system_monkey.setVisibility(View.GONE);
                layout_defined_monkey.setVisibility(View.VISIBLE);
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
        // 三星手机
        if (CommonVariable.snLabel.contains("71") || CommonVariable.snLabel.contains("76") || CommonVariable.snLabel.contains("86")
                || CommonVariable.snLabel.contains("96")) {
            system_monkey_text.setText("指令：" + "monkey --pkg-blacklist-file " +
                    "/sdcard/blacklist.txt --ignore-crashes --ignore-timeouts --ignore" +
                    "-security-exceptions --kill-process-after-error --pct-trackball" +
                    " 0 --pct-nav 0 --pct-majornav 0 --pct-anyevent 0 -v -v -v " +
                    "--throttle 500 1200000000 > /sdcard/monkeytest.log 2>&1 &");
        } else {
            system_monkey_text.setText("指令：" + "monkey -s 1000 --pkg-blacklist-file /sdcard/blacklist.txt " +
                    "--ignore-crashes --ignore-timeouts --kill-process-after-error " +
                    "--ignore-security-exceptions --pct-trackball 0 --pct-nav 0 -v -v " +
                    "-v --throttle 500 1200000000 > /sdcard/monkeytest.log 2>&1 &");
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


    /**
     * 单个应用monkey指令包名选择
     */
    class SpinnerOnSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                   long id) {
            // TODO Auto-generated method stub
            String choose = adapterView.getItemAtPosition(position).toString();

            edit_monkey_defined.setVisibility(View.GONE);
            if(choose.equals("请选择应用名")) {
                button_copy_single_monkey.setVisibility(View.GONE);
                CommonVariable.packet_choose = "";
            }else if(choose.equals("视频")){
                CommonVariable.packet_choose = iPublicConstants.PACKET_VIDEO;
            }else if(choose.equals("音乐")){
                CommonVariable.packet_choose = iPublicConstants.PACKET_MUSIC;
            }else if(choose.equals("读书")){
                CommonVariable.packet_choose = iPublicConstants.PACKET_EBOOK;
            }else if(choose.equals("图库")){
                CommonVariable.packet_choose = iPublicConstants.PACKET_GALLERY;
            }else if(choose.equals("资讯")){
                CommonVariable.packet_choose = iPublicConstants.PACKET_READER;
            }else if(choose.equals("自定义")){
                CommonVariable.packet_choose = " ";
                edit_monkey_defined.setVisibility(View.VISIBLE);
                edit_monkey_defined.setWidth(800);
                edit_monkey_defined.setMaxLines(1);
            }

            if(CommonVariable.packet_choose.equals("")){
                single_monkey_text.setVisibility(View.GONE);   // 隐藏text
            }else{
                if(CommonVariable.snLabel.contains("71") || CommonVariable.snLabel.contains("76") || CommonVariable.snLabel.contains("86")
                        || CommonVariable.snLabel.contains("96")){
                    single_monkey_text.setText("指令：" + "monkey -p " +
                            CommonVariable.packet_choose +
                            "  --ignore-crashes --ignore-timeouts --ignore" +
                            "-security-exceptions --kill-process-after-error " +
                            "--pct-trackball 0 --pct-nav 0 --pct-majornav 0 " +
                            "--pct-anyevent 0 -v -v -v --throttle 500 " +
                            "1200000000 > /sdcard/monkeytest.log 2>&1 &");
                }else{
                    single_monkey_text.setText("指令：" + "monkey -s 1000 -p "
                            + CommonVariable.packet_choose
                            + " --ignore-crashes --ignore-timeouts --kill-process"
                            + "-after-error --ignore-security-exceptions --pct-trackball "
                            + "0 --pct-nav 0 -v -v -v --throttle 500 1200000000"
                            + " > /sdcard/monkeytest.log 2>&1 &");
                }
                single_monkey_text.setVisibility(View.VISIBLE);   // 使text可见
                if(single_monkey_text_flag){
                    button_copy_single_monkey.setVisibility(View.VISIBLE);
                }else{
                    button_copy_single_monkey.setVisibility(View.GONE);
                }
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * 按钮监听实现类
     */
    class ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int id = v.getId();   // 读取按键值id
            ClipboardManager cmb;
            switch (id){
                case R.id.button_single_monkey:
                    if(!CommonVariable.packet_choose.equals("")){
                        monkey_dialog();    // 应用monkey
                    }else{
                        ToastHelper.addToast("请先选择要执行monkey的应用！！！",
                                getActivity().getApplicationContext());
                    }
                    break;
                case R.id.button_system_monkey:
                case R.id.button_defined_monkey:
                    monkey_dialog();
                    break;
                case R.id.button_copy_single_monkey:
                    cmb = (ClipboardManager) getActivity().getApplicationContext()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(single_monkey_text.getText().toString());   // 复制内容至剪贴板
                    ToastHelper.addToast("已复制内容", getActivity().getApplicationContext());
                    break;
                case R.id.button_copy_system_monkey:
                    cmb = (ClipboardManager) getActivity().getApplicationContext()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(system_monkey_text.getText().toString());   // 复制内容至剪贴板
                    ToastHelper.addToast("已复制内容", getActivity().getApplicationContext());
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
                    ToastHelper.addToast("已保存黑名单", getActivity().getApplicationContext());
                    break;
                case R.id.button_single_choose:
                    function_choose(1);
                    break;
                case R.id.button_system_choose:
                    function_choose(2);
                    break;
                case R.id.button_defined_choose:
                    function_choose(3);
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
                default:
                    break;
            }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(1 == function_choose){
            builder.setTitle("应用monkey");
        }else if (2 == function_choose){
            builder.setTitle("系统monkey");
        }else if (3 == function_choose){
            saveData(); // 保存自定义monkey数据
            builder.setTitle("自定义monkey");
        }

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strMonkeyType = null;
                dialog.dismiss();

                if(1 == function_choose){
                    strMonkeyType = "应用Monkey";
                    MonkeyTableData.getInstance(getActivity()).writeStringData("monkey_command", single_monkey_text.getText().toString().substring(3));
                }else if (2 == function_choose){
                    strMonkeyType = "系统Monkey";
                    writeBlacklist(blacklist_flag);   // 根目录填写黑名单
                    MonkeyTableData.getInstance(getActivity()).writeStringData("monkey_command", system_monkey_text.getText().toString().substring(3));
                }else if (3 == function_choose){
                    strMonkeyType = "自定义Monkey";
                    MonkeyTableData.getInstance(getActivity()).writeStringData("monkey_command", defined_monkey_text.getText().toString());
                }

                MonkeyTableData.getInstance(getActivity()).writeBooleanData("isStart", !sharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_FLOAT_BTN, true));
                PrefWidgetOnOff.getInstance(getActivity()).writeBooleanData("isMonkeyFloating", sharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_FLOAT_BTN, true));

                // 保存数据库
                SqlAlterHelper.getInstance(getActivity()).addData(strMonkeyType, MonkeyTableData.getInstance(getActivity()).readStringData("monkey_command"),
                        PublicMethod.getSystemTime(), sharedPreferences.getBoolean(SettingPreferenceKey.MUTE, false),
                        sharedPreferences.getBoolean(SettingPreferenceKey.LOCK_WIFI, true), sharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_FLOAT_BTN, true));

                sendBroadcast();
                Intent mIntent = new Intent(getActivity(), SuperTestService.class);
                getActivity().startService(mIntent);   // 开始服务
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
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
                    if(CommonVariable.snLabel.contains("71") || CommonVariable.snLabel.contains("76") || CommonVariable.snLabel.contains("86")
                            || CommonVariable.snLabel.contains("96")){
                        PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "/Android/log");
                    }else{
                        PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "/mtklog");
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
                    // 启动抓log工具
                    if(CommonVariable.snLabel.contains("71") || CommonVariable.snLabel.contains("76") || CommonVariable.snLabel.contains("86")
                            || CommonVariable.snLabel.contains("96")){
                        Runtime.getRuntime().exec("am start -n com.meizu.logreport/com.meizu.logreport.activity.MainActivity");
                        Thread.sleep(5 * 1000);
                        // 只抓Main Log或其他
                        if(sharedPreferences.getBoolean(SettingPreferenceKey.CATCH_LOG_TYPE, true)){
                            Runtime.getRuntime().exec(CommonVariable.startCatLogBroadcast.replace("%d", "1"));
                        }else{
                            Runtime.getRuntime().exec(CommonVariable.startCatLogBroadcast.replace("%d", "7"));
                        }
                    }else{
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
            PublicMethod.copyAssetFile(getActivity(), "blacklist.txt", iPublicConstants.LOCAL_MEMORY);
        }else{
            PublicMethod.saveStringToFile(edit_blacklist_defined.getText().toString(),
                    "blacklist.txt", iPublicConstants.LOCAL_MEMORY);      // 复制文件到根目录
        }
    }


    @Override
    public void onResume(){
        Log.e(MonkeyFragment.class.getSimpleName(), "onResume");
        super.onResume();
    }
}
