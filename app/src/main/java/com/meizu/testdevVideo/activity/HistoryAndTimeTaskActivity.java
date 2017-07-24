package com.meizu.testdevVideo.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.fragment.HistoryFragment;
import com.meizu.testdevVideo.fragment.TimeTaskFragment;

import flyme.support.v7.app.ActionBar;
import flyme.support.v7.app.AppCompatActivity;

public class HistoryAndTimeTaskActivity extends AppCompatActivity {

    private HistoryFragment historyFragment;
    private TimeTaskFragment timeTaskFragment;
    String object;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intend=getIntent();    // 获取跳转数据
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   // 设置竖屏
        Bundle bundle=intend.getExtras();  // 从Intent中获得Bundle对象
        object=bundle.getString("object");   // 从Bundle中获得数据
        setContentView(R.layout.activity_history_timetask);
        setFragment(object);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(object);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // 设置Fragment
    private void setFragment(String fragmentFlag)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if(fragmentFlag.equals("执行历史")){
            historyFragment = new HistoryFragment();
            transaction.replace(R.id.id_history_timetask, historyFragment);
        }else if(fragmentFlag.equals("定时任务")){
            timeTaskFragment = new TimeTaskFragment();
            transaction.replace(R.id.id_history_timetask, timeTaskFragment);
        }
        transaction.commit();
    }
}
