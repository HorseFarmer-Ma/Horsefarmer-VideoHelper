package com.meizu.testdevVideo.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.fragment.AppListFragment;
import com.meizu.testdevVideo.fragment.OtaPushFragment;
import com.meizu.testdevVideo.fragment.OtaHistoryFragment;
import com.meizu.testdevVideo.fragment.OtaTestFragment;
import com.meizu.testdevVideo.fragment.ScreenRecordFragment;
import com.meizu.testdevVideo.fragment.SchemaTestFragment;
import com.meizu.testdevVideo.fragment.UpdateSoftwareFtpFragment;

import flyme.support.v7.app.ActionBar;
import flyme.support.v7.app.AppCompatActivity;


public class OtaPushActivity extends AppCompatActivity {

    private ScreenRecordFragment screenRecordFragment;
    private OtaPushFragment otaPushFragment;
    private OtaHistoryFragment otaHistoryFragment;
    private AppListFragment appListFragment;
    private UpdateSoftwareFtpFragment updateSoftwareFtpFragment;
    private SchemaTestFragment schemaTestFragment;
    private OtaTestFragment otaTestFragment;
    private static Activity mActivity = null;
    String object;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_tools);
        mActivity = this;
        Intent intend = getIntent();    // 获取跳转数据
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   // 设置竖屏
        Bundle bundle=intend.getExtras();  // 从Intent中获得Bundle对象
        object=bundle.getString("object");   // 从Bundle中获得数据
        setFragment(object);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(object);
        }

    }


    /**
     * 设置Fragment
     */
    private void setFragment(String fragmentFlag)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if(fragmentFlag.equals("新增任务")){

            otaPushFragment = new OtaPushFragment();
            transaction.replace(R.id.id_common_tools, otaPushFragment);
        }
        transaction.commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }

    /**
     * 处理actionbar中menu的点击事件
     * @param item
     * @return
     */
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(null != mActivity){
            mActivity = null;
        }
    }
}
