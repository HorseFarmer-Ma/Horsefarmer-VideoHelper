package com.meizu.testdevVideo.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.constant.FragmentUtils;
import com.meizu.testdevVideo.fragment.UpdateSoftwareFtpFragment;

import flyme.support.v7.app.ActionBar;
import flyme.support.v7.app.AppCompatActivity;

/**
 * 承载Fragment跳转的空Activity
 */
public class EmptyActivity extends AppCompatActivity {

    private FragmentManager fm;
    private UpdateSoftwareFtpFragment updateSoftwareFtpFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
        String fragment_type = getIntent().getStringExtra(FragmentUtils.FRAGMENT_TYPE);
        setFragment(fragment_type);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(fragment_type);
        }
    }

    private void setFragment(String fragment_type){
        fm = (null == fm)? getSupportFragmentManager() : fm;
        FragmentTransaction transaction = fm.beginTransaction();
        if(fragment_type.equals(FragmentUtils.FragmentType.UPDATE_APP.getFragmentName())){

        }else if(fragment_type.equals(FragmentUtils.FragmentType.CHOOSE_APP.getFragmentName())){
            updateSoftwareFtpFragment = new UpdateSoftwareFtpFragment();
            Bundle bundle = new Bundle();
            bundle.putString(FragmentUtils.FRAGMENT_TYPE, FragmentUtils.CHOOSE_APP_FRAGMENT);
            updateSoftwareFtpFragment.setArguments(bundle);
            transaction.replace(R.id.fragment_content, updateSoftwareFtpFragment);
        }
        transaction.commit();
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
}
