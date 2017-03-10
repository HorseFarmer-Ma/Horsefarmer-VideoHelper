package com.meizu.testdevVideo.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import com.meizu.testdevVideo.activity.MainActivity;
import com.meizu.testdevVideo.activity.PerformsActivity;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.data.gridview.MyContent;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.AnimationHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.service.PerformsTestService;
import com.meizu.testdevVideo.service.RegisterAppService;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;

import java.io.File;

/**
 * 性能测试Fragment
 */
public class PerformsTestFragment extends Fragment implements AdapterView.OnItemClickListener, MainActivity.NotifyPerformsAnimation {
    private AbsListView mListView;
    private SimpleAdapter mAdapter;
    private MyContent mPerformsContent;
    private String memory_test = "内存测试";
    private String fps_test = "帧率测试";
    private String water_test = "纯净后台";
    private String time_test = "启动时间";
    private String stop_performs_test = "中止任务";
    private String persion_register = "手动注册";
    private ScaleAnimation animation;

    public PerformsTestFragment(){
        MainActivity.setNotifyPerformsAnimation(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_grid, container, false);

        mPerformsContent = new MyContent();

        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mPerformsContent.addItem(new MyContent.DummyItem(memory_test, R.mipmap.ic_sdcard));
        mPerformsContent.addItem(new MyContent.DummyItem(fps_test, R.mipmap.ic_fps));
        mPerformsContent.addItem(new MyContent.DummyItem(water_test, R.drawable.ic_water));
        mPerformsContent.addItem(new MyContent.DummyItem(time_test, R.mipmap.ic_time));
        mPerformsContent.addItem(new MyContent.DummyItem(stop_performs_test, R.drawable.ic_stop_performs));
        mPerformsContent.addItem(new MyContent.DummyItem(persion_register, R.drawable.ic_register));

        mAdapter = new SimpleAdapter(getActivity(), mPerformsContent.ITEMS, R.layout.tool_listview,
                new String[]{"text", "img"},
                new int[]{R.id.tool_text, R.id.tool_img});    // 生成列表数据

        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        animation = AnimationHelper.getInstance().getScaleAnimation(1.0f, 1.0f, 0f, 1.0f, 0, 0, 500, true, 0.5f, 0f);
        mListView.setAnimation(animation);
        mThread.start();

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position < 4){
            Bundle bundle = new Bundle();
            bundle.putString("Project", mPerformsContent.ITEMS.get(position).get("text").toString());
            Intent mIntent = new Intent(getActivity(), PerformsActivity.class);
            mIntent.putExtras(bundle);
            startActivity(mIntent);
            // 设置动画效果
            getActivity().overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
        }

        switch (position){
            case 4:
                if(PublicMethod.isServiceWorked(getActivity(), "com.meizu.testdevVideo.service.PerformsTestService")){
                    Intent intent = new Intent(getActivity(), PerformsTestService.class);
                    getActivity().stopService(intent);
                    PublicMethod.killProcess("ps|grep uiautomator", "system    ", " ");
                    PublicMethod.killProcess("ps |grep com.android.commands.monkey", "system    ", " ");
                    ToastHelper.addToast("已终止", getActivity());
                }else{
                    ToastHelper.addToast("当前没有运行中的任务", getActivity());
                }

                break;
            case 5:
                PerformsData.getInstance(getActivity()).writeBooleanData(iPerformsKey.isRegister, false);
                Intent registerIntent = new Intent(getActivity(), RegisterAppService.class);
                getActivity().startService(registerIntent);
                break;
            default:
                break;
        }


    }

    /**
     * Activity 初始化线程
     */
    Thread mThread = new Thread(new Runnable(){
        @Override
        public void run() {
            // 文件夹不存在，则新建文件夹
            File mProjectPath = new File(iPublicConstants.PERFORMS_TESTCASE_PATH);
            File mResultPath1 = new File(iPublicConstants.PERFORMS_FPS_RESULT);
            File mResultPath2 = new File(iPublicConstants.PERFORMS_MEMORY_RESULT);
            File mResultPath3 = new File(iPublicConstants.PERFORMS_PURE_BACKGROUND_RESULT);
            File mResultPath4 = new File(iPublicConstants.PERFORMS_TIME_RESULT);

            if(!mProjectPath.exists()){
                mProjectPath.mkdirs();
            }

            if(!mResultPath1.exists()){
                mResultPath1.mkdirs();
            }

            if(!mResultPath2.exists()){
                mResultPath2.mkdirs();
            }

            if(!mResultPath3.exists()){
                mResultPath3.mkdirs();
            }

            if(!mResultPath4.exists()){
                mResultPath4.mkdirs();
            }
        }
    });

    @Override
    public void choosePerformsFragment(boolean isFirstTime) {
        if (!isFirstTime){
            animation.cancel();
            animation.startNow();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        animation.cancel();
    }
}
