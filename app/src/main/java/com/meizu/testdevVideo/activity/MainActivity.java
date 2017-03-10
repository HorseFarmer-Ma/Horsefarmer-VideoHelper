package com.meizu.testdevVideo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import android.os.Bundle;

import android.os.Handler;
import android.os.Message;

import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.library.SharedPreferencesHelper;
import com.meizu.testdevVideo.fragment.AboutPhoneFragment;
import com.meizu.testdevVideo.fragment.MonkeyFragment;
import com.meizu.testdevVideo.fragment.PerformsTestFragment;
import com.meizu.testdevVideo.fragment.ToolFragment;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.SqlAlterHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.SaveLog;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.shell.ShellUtil;
import com.meizu.testdevVideo.util.shell.ShellUtils;
import com.meizu.testdevVideo.util.update.SoftwareUpdate;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.service.SuperTestService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jpush.android.api.JPushInterface;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    // 关于手机、增加测试数据、工具页面、跑monkey
    private AboutPhoneFragment about_phone;
    private PerformsTestFragment performsTestFragment;
    private ToolFragment toolFragment;
    private MonkeyFragment monkeyFragment;
    private static MainActivity mActivity;
    private Switch menu_switch;
    private TextView switch_textview;
    private boolean menu_function_choose = false;
    private NavigationView navigationView;
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private static NotifyToolAnimation notifyToolAnimation;
    private static NotifyPerformsAnimation notifyPerformsAnimation;

    private FragmentManager fm;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long i = SystemClock.currentThreadTimeMillis();
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   // 设置竖屏

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, 0, 0);
        drawer.setDrawerListener(toggle);
        mActivity = this;
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);    // 导航栏默认选中关于手机

        fm = getFragmentManager();

        menu_switch = (Switch) findViewById(R.id.menu_switch);
        switch_textview = (TextView) findViewById(R.id.switch_textview);
        navigationView.setNavigationItemSelectedListener(this);
        mSharedPreferencesHelper = new SharedPreferencesHelper(this, "monkey_table");

        new SoftwareUpdate(MainActivity.this).updateMyApp(false);

        loadData();
        if(menu_function_choose){
            switch_textview.setText("进入设置");
        }else{
            switch_textview.setText("清除数据");
        }

        menu_switch.setChecked(menu_function_choose);
        menu_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                menu_function_choose = !menu_function_choose;
                if(menu_function_choose){
                    switch_textview.setText("进入设置");
                }else{
                    switch_textview.setText("清除数据");
                }
                saveData();
            }
        });
        data_thread.start();    // 初始数据检测线程

        if(!PublicMethod.isServiceWorked(MainActivity.this, "com.meizu.testdevVideo.service.SuperTestService")){
            Intent mIntent = new Intent(MainActivity.this, SuperTestService.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(mIntent);   // 开始服务
        }

        // 设置默认的Fragment
        setDefaultFragment();
        Log.e("MainActivity", "初始化onCreate时间: " + (SystemClock.currentThreadTimeMillis() - i));
    }


    // 创建属于主线程的handler
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(1 == msg.what){
                ToastHelper.addToast("清除完毕！", MainActivity.this);
            }
        }
    };

    // 存储数据
    private boolean saveData() {
        // 实例化SharedPreferences对象
        SharedPreferences mySharedPreferences= getSharedPreferences("mainActivity_switch",
                Activity.MODE_PRIVATE);
        // 实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.remove("menu_function_choose");
        editor.putBoolean("menu_function_choose", menu_function_choose);
        // 提交当前数据
        return editor.commit();
    }

    // 加载表中数据
    private void loadData() {
        SharedPreferences mSharedPreference1 = getSharedPreferences("mainActivity_switch",
                Activity.MODE_PRIVATE);
        menu_function_choose = mSharedPreference1.getBoolean("menu_function_choose", false);
    }

    // 初始化数据线程
    Thread data_thread  = new Thread(new Runnable() {
        @Override
        public void run() {
            if(!new File(iPublicConstants.MEMORY_BACK_UP + "blacklist_save.txt").exists()){
                PublicMethod.copyAssetFile(MainActivity.this, "blacklist.txt", iPublicConstants.MEMORY_BACK_UP);
                try {
                    PublicMethod.copySingleFile("blacklist.txt", "blacklist_save.txt",
                            iPublicConstants.MEMORY_BACK_UP , iPublicConstants.MEMORY_BACK_UP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 删除更新包
            File mApk = new File(iPublicConstants.LOCAL_MEMORY + "/SuperTest/UpdateApk/");
            if(mApk.exists()){
                PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "/SuperTest/UpdateApk");
            }

            File uitest = new File(getFilesDir() + "/uitest/a5/uiautomator");
            if(!uitest.exists()){
                try {
                    PublicMethod.copyAssetDirToFiles(MainActivity.this, "uitest");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    // 获取Activity实例
    public static MainActivity getInstance() {
        return mActivity;
    }

    // 设置默认的Fragment
    private void setDefaultFragment() {
        setTitle("关于手机");
        transaction = fm.beginTransaction();
        if(about_phone == null){
            about_phone = new AboutPhoneFragment();
            if(about_phone.isAdded()){
                transaction.show(about_phone);
            }else{
                transaction.add(R.id.id_content, about_phone);
            }
        }else{
            if(about_phone.isAdded()){
                transaction.show(about_phone);
            }else{
                transaction.add(R.id.id_content, about_phone);
            }
        }
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mSharedPreferencesHelper.readBooleanData("isStart")
                || !TextUtils.isEmpty(ShellUtils.execCommand("ps |grep com.android.commands.monkey", false, true).successMsg)) {
            navigationView.getMenu().getItem(0).setChecked(true);    // 导航栏默认选中关于手机
            setTitle(R.string.about_phone);
            transaction = fm.beginTransaction();
            hideFragments(transaction);
            if(about_phone == null){
                about_phone = new AboutPhoneFragment();
                if(about_phone.isAdded()){
                    transaction.show(about_phone);
                }else{
                    transaction.add(R.id.id_content, about_phone);
                }
            }else{
                if(about_phone.isAdded()){
                    transaction.show(about_phone);
                }else{
                    transaction.add(R.id.id_content, about_phone);
                }
            }
            transaction.commit();
            onBackPressed();
        }

        JPushInterface.onResume(this);
    }

    // 重写销毁函数
    @Override
    public void onDestroy(){
        SqlAlterHelper.getInstance(MainActivity.this).close();
        Log.e("MainActivity", "onDestroy");
        super.onDestroy();
    }

    @Override
    public void finish(){
        moveTaskToBack(true);
//        super.finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String packet_choose = "";
        if (id == R.id.action_clear_video) {
            packet_choose = iPublicConstants.PACKET_VIDEO;
        }else if (id == R.id.action_clear_music) {
                packet_choose = iPublicConstants.PACKET_MUSIC;
        }else if (id == R.id.action_clear_ebook) {
            packet_choose = iPublicConstants.PACKET_EBOOK;
        }else if (id == R.id.action_clear_gallery) {
            packet_choose = iPublicConstants.PACKET_GALLERY;
        }else if (id == R.id.action_clear_reader) {
            packet_choose = iPublicConstants.PACKET_READER;
        }else if(id == R.id.action_clear_compaign){
            packet_choose = iPublicConstants.PACKET_COMPAIGN;
        }else if (id == R.id.action_clear_cloud) {
            packet_choose = iPublicConstants.PACKET_CLOUD;
        }

        // menu菜单功能选择
        if(!menu_function_choose){
            final String final_Packet_choose = packet_choose;
            new Thread(){
                public void run() {
                    ShellUtil.exec("pm clear " + final_Packet_choose);
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }.start();
        }else{
            PublicMethod.showInstalledAppDetails(MainActivity.this, packet_choose);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_about_phone) {
            transaction = fm.beginTransaction();
            hideFragments(transaction);
            setTitle(R.string.about_phone);
            if(about_phone == null){
                about_phone = new AboutPhoneFragment();
                if(about_phone.isAdded()){
                    transaction.show(about_phone);
                }else{
                    transaction.add(R.id.id_content, about_phone);
                }
            }else{
                if(about_phone.isAdded()){
                    transaction.show(about_phone);
                }else{
                    transaction.add(R.id.id_content, about_phone);
                }
            }

        } else if (id == R.id.nav_gallery) {
            transaction = fm.beginTransaction();
            hideFragments(transaction);
            setTitle(R.string.performs_test);

            if(performsTestFragment == null){
                performsTestFragment = new PerformsTestFragment();
                if(performsTestFragment.isAdded()){
                    transaction.show(performsTestFragment);
                }else{
                    transaction.add(R.id.id_content, performsTestFragment);
                }
                notifyPerformsAnimation.choosePerformsFragment(true);
            }else{
                if(performsTestFragment.isAdded()){
                    transaction.show(performsTestFragment);
                }else{
                    transaction.add(R.id.id_content, performsTestFragment);
                }
                notifyPerformsAnimation.choosePerformsFragment(false);
            }
        } else if (id == R.id.nav_slideshow) {
            transaction = fm.beginTransaction();
            hideFragments(transaction);
            setTitle(R.string.common_tool);

            if(toolFragment == null){
                toolFragment = new ToolFragment();
                if(toolFragment.isAdded()){
                    transaction.show(toolFragment);
                }else{
                    transaction.add(R.id.id_content, toolFragment);
                }
                notifyToolAnimation.chooseToolFragment(true);
            }else{
                if(toolFragment.isAdded()){
                    transaction.show(toolFragment);
                }else{
                    transaction.add(R.id.id_content, toolFragment);
                }
                notifyToolAnimation.chooseToolFragment(false);
            }

        } else if (id == R.id.nav_manage) {
            transaction = fm.beginTransaction();
            hideFragments(transaction);
            setTitle(R.string.monkey);

            if(monkeyFragment == null){
                monkeyFragment = new MonkeyFragment();
                if(monkeyFragment.isAdded()){
                    transaction.show(monkeyFragment);
                }else{
                    transaction.add(R.id.id_content, monkeyFragment);
                }
            }else{
                if(monkeyFragment.isAdded()){
                    transaction.show(monkeyFragment);
                }else{
                    transaction.add(R.id.id_content, monkeyFragment);
                }
            }

        } else if (id == R.id.nav_catchLog) {
            // 显示对话框
                ToastHelper.addToast("正在抓取Log，请稍后...", MainActivity.this);
            // 构建Runnable对象，在runnable中更新界面
            // 开启一个子线程，用于抓取Log
                new Thread(){
                    public void run() {
                        new SaveLog().saveAll();
                        handler.post(runnableUi);
                    }
                }.start();

        } else if(id == R.id.nav_catchTrace) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setIcon(R.mipmap.ic_app);
            builder.setTitle("选择导出的类型");
            //    指定下拉列表的显示数据
            final String[] cities = {"Trace文件", "DrodBox文件"};
            //    设置一个下拉的列表选择项
            builder.setItems(cities, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if(0 == which){
                        if(new File("/data/anr").exists()){
                            ToastHelper.addToast("正在导出Trace文件，请稍后...", MainActivity.this);
                            PublicMethod.copyFolder("/data/anr", iPublicConstants.LOCAL_MEMORY + "SuperTest/Trace/" +
                                    new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));   // 导出Trace文件
                            ToastHelper.addToast("Trace文件导出完毕，保存至/sdcard/SuperTest/Trace/",
                                    MainActivity.this);
                        }else{
                            ToastHelper.addToast("没有Trace文件，无需导出", getApplicationContext());
                        }
                    }else{
                        if(new File("/data/system/Dropbox").exists()){
                            ToastHelper.addToast("正在导出Dropbox文件，请稍后...", MainActivity.this);
                            PublicMethod.copyFolder("/data/system/dropbox", iPublicConstants.LOCAL_MEMORY + "SuperTest/Dropbox/" +
                                    new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()));   // 导出Trace文件
                            ToastHelper.addToast("Dropbox文件导出完毕，保存至/sdcard/SuperTest/Dropbox/",
                                    MainActivity.this);
                        }else{
                            ToastHelper.addToast("没有Dropbox文件，无需导出", MainActivity.this);
                        }
                    }
                }
            });
            builder.show();
        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);   // 跳转到帮助界面

        } else if(id == R.id.nav_update){   // 软件更新
            new SoftwareUpdate(MainActivity.this).updateMyApp(true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if(R.id.nav_about_phone == id || R.id.nav_gallery == id || R.id.nav_slideshow == id || R.id.nav_manage == id){
            transaction.commit();
        }

        return true;
    }

    Runnable runnableUi=new  Runnable(){
        @Override
        public void run() {
            //更新界面
            ToastHelper.addToast("Log抓取完毕，保存至/sdcard/SuperTest/LogReport/",
                    MainActivity.this);
        }
    };


    /**
     * 将所有的Fragment都置为隐藏状态。
     * @param transaction
     * 用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (about_phone != null) {
            transaction.hide(about_phone);
        }
        if (performsTestFragment != null) {
            transaction.hide(performsTestFragment);
        }
        if (toolFragment != null) {
            transaction.hide(toolFragment);
        }

        if (monkeyFragment != null) {
            transaction.hide(monkeyFragment);
        }
    }

    public static void setNotifyToolAnimation(NotifyToolAnimation notify){
        notifyToolAnimation = (notifyToolAnimation == null)? notify : notifyToolAnimation;
    }

    public static void setNotifyPerformsAnimation(NotifyPerformsAnimation notify){
        notifyPerformsAnimation = (notifyPerformsAnimation == null)? notify : notifyPerformsAnimation;
    }

    public interface NotifyToolAnimation{
        void chooseToolFragment(boolean isFirstTime);
    }

    public interface NotifyPerformsAnimation{
        void choosePerformsFragment(boolean isFirstTime);
    }
}
