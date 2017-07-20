package com.meizu.testdevVideo.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.adapter.SuperTestFragmentPagerAdapter;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.fragment.AboutMeFragment;
import com.meizu.testdevVideo.fragment.AboutPhoneFragment;
import com.meizu.testdevVideo.fragment.MonkeyFragment;
import com.meizu.testdevVideo.fragment.CommonFragment;
import com.meizu.testdevVideo.fragment.PerformsFragment;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.update.SoftwareUpdate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import flyme.support.v4.view.ViewPager;
import flyme.support.v7.app.ActionBar;
import flyme.support.v7.app.AppCompatActivity;

public class SuperTestActivity extends AppCompatActivity implements ActionBar.TabListener, ViewPager.OnPageChangeListener{

    private ActionBar mActionBar = null;
    private ViewPager mViewPager = null;
    private List<Fragment> fragmentList = null;
    private SoftwareUpdate softwareUpdate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_test);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   // 设置竖屏
        softwareUpdate = (null == softwareUpdate)? new SoftwareUpdate(this, this) : softwareUpdate;
        init_ActionBar();
        init_ViewPager();
        data_thread.start();    // 初始数据检测线程
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    private void gotoMeizuPermission() {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", getPackageName());
        try {
            startActivityForResult(intent, 0);
        } catch (Exception e) {
            e.printStackTrace();
            ToastHelper.addToast("跳转权限详情失败\n" +
                    "请去手机管家手动开启悬浮窗权限", SuperTestApplication.getContext());
        }
    }

    // 初始化数据线程
    Thread data_thread  = new Thread(new Runnable() {
        @Override
        public void run() {
            if(!new File(iPublicConstants.MEMORY_BACK_UP + "blacklist_save.txt").exists()){
                PublicMethod.copyAssetFile(SuperTestActivity.this, "blacklist.txt", iPublicConstants.MEMORY_BACK_UP);
                try {
                    PublicMethod.copySingleFile("blacklist.txt", "blacklist_save.txt",
                            iPublicConstants.MEMORY_BACK_UP , iPublicConstants.MEMORY_BACK_UP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            File uitest = new File(getFilesDir() + "/uitest/a5/uiautomator");
            if(!uitest.exists()){
                try {
                    PublicMethod.copyAssetDirToFiles(SuperTestActivity.this, "uitest");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    /**
     * 初始化ActionBar
     */
    private void init_ActionBar(){
        mActionBar = getSupportActionBar();
        if(null != mActionBar){
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowTabEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS); // 设置标题栏为Tab模式

            int tab_number = getResources().getStringArray(R.array.tab_fragment).length;
            String[] tabList = getResources().getStringArray(R.array.tab_fragment);
            for(int i = 0; i < tab_number; i++){
                mActionBar.addTab(mActionBar.newTab().setText(tabList[i]).setTag(i).setTabListener(this));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        softwareUpdate.updateMyApp(false);
        if(!PublicMethod.checkOp(SuperTestApplication.getContext(), 24)){
            ToastHelper.addLongToast("未获取悬浮窗权限，请开启后再启动ST", SuperTestApplication.getContext());
            gotoMeizuPermission();
        }
        if (MonkeyTableData.getInstance(getApplicationContext()).readBooleanData(Constants.Monkey.IS_START)) {
            onBackPressed();
        }

        JPushInterface.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }

    /**
     * 初始化ViewPager
     */
    private void init_ViewPager(){
        mViewPager = (ViewPager) findViewById(R.id.vp_fragment);
        fragmentList = new ArrayList<>();
        fragmentList.add(new AboutPhoneFragment());
        fragmentList.add(new CommonFragment());
        fragmentList.add(new PerformsFragment());
        fragmentList.add(new MonkeyFragment());
        fragmentList.add(new AboutMeFragment());
        mViewPager.setAdapter(new SuperTestFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        mViewPager.addOnPageChangeListener(this);
    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position < mActionBar.getAllTabs().size()) {
            mActionBar.setTabScrolled(position, positionOffset, ViewPager.SCROLL_STATE_DRAGGING);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (position < mActionBar.getAllTabs().size()) {
            mActionBar.selectTab(mActionBar.getTabAt(position));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void finish(){
        moveTaskToBack(true);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        int tag = (int) tab.getTag();
        if(null != mViewPager){
            mViewPager.setCurrentItem(tag);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        softwareUpdate.clear();
    }
}
