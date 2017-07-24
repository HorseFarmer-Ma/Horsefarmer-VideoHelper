package com.meizu.testdevVideo.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.meizu.common.widget.GuidePopupWindow;
import com.meizu.common.widget.Switch;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.activity.PerformsActivity;
import com.meizu.testdevVideo.adapter.data.gridview.MyContent;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.service.U2AutoTestService;
import com.meizu.testdevVideo.task.performs.U2TaskPreference;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.BaseData;

import java.io.File;

import flyme.support.v7.app.AlertDialog;

/**
 * 性能测试Fragment
 * Created by maxueming on 2017/5/25.
 */
public class PerformsFragment extends Fragment {
    private AbsListView mListView;
    private SimpleAdapter mAdapter;
    private Switch icU2Test;
    private MyContent mPerformsContent;
    private String memory_test = "内存测试";
    private String fps_test = "帧率测试";
    private String water_test = "纯净后台";
    private String time_test = "启动时间";
    private String stop_performs_test = "中止任务";
    private View rootView;
    private TextView txtPerforms;
    private GuidePopupWindow guideU2Task;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(null == rootView) {
            rootView = inflater.inflate(R.layout.fragment_performs, container, false);
            mListView = (AbsListView) rootView.findViewById(R.id.list);
            icU2Test = (Switch) rootView.findViewById(R.id.ic_u2_test);
            txtPerforms = (TextView) rootView.findViewById(R.id.txt_performs);
            mPerformsContent = new MyContent();
            mPerformsContent.addItem(new MyContent.DummyItem(memory_test, R.drawable.ic_memory));
            mPerformsContent.addItem(new MyContent.DummyItem(fps_test, R.drawable.ic_fps));
            mPerformsContent.addItem(new MyContent.DummyItem(water_test, R.drawable.ic_water));
            mPerformsContent.addItem(new MyContent.DummyItem(time_test, R.drawable.ic_time));
            mPerformsContent.addItem(new MyContent.DummyItem(stop_performs_test, R.drawable.ic_stop_performs));

            mAdapter = new SimpleAdapter(getActivity(), mPerformsContent.ITEMS, R.layout.tool_listview,
                    new String[]{"text", "img"},
                    new int[]{R.id.tool_text, R.id.tool_img});    // 生成列表数据

            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new OnItemClickListener());
            icU2Test = (Switch) rootView.findViewById(R.id.ic_u2_test);

            if(!U2TaskPreference.getInstance(SuperTestApplication.getContext()).readBooleanData("isGuide")){
                guideU2Task = new GuidePopupWindow(getActivity());
                guideU2Task.setMessage("请点右边开关，开启测试任务篮子");
                guideU2Task.setOutsideTouchable(false);
                guideU2Task.setLayoutMode(GuidePopupWindow.GUIDE_LAYOUT_MODE_DOWN);
                guideU2Task.setArrowPosition(txtPerforms.getWidth()/2);
                guideU2Task.show(txtPerforms, 0, 0);
                U2TaskPreference.getInstance(SuperTestApplication.getContext()).writeBooleanData("isGuide", true);
            }

            icU2Test.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(guideU2Task != null){
                        guideU2Task.dismiss();
                        guideU2Task = null;
                    }
                    Intent intent = new Intent(getActivity(), U2AutoTestService.class);
                    if (isChecked) {
                        getActivity().startService(intent);
                    } else {
                        getActivity().stopService(intent);
                    }
                }
            });
            mThread.start();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(PublicMethod.isServiceWorked(SuperTestApplication.getContext(),
                "com.meizu.testdevVideo.service.U2AutoTestService")){
            icU2Test.setChecked(true);
        }else{
            icU2Test.setChecked(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(guideU2Task != null){
            guideU2Task.dismiss();
            guideU2Task = null;
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



    class OnItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position < 4){
                if(TextUtils.isEmpty(BaseData.getInstance(SuperTestApplication.getContext())
                        .readStringData(SettingPreferenceKey.MONKEY_PACKAGE))){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("还未选择业务，请选择后重试！").setNeutralButton("我知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }else{
                    Bundle bundle = new Bundle();
                    bundle.putString("Project", mPerformsContent.ITEMS.get(position).get("text").toString());
                    Intent mIntent = new Intent(getActivity(), PerformsActivity.class);
                    mIntent.putExtras(bundle);
                    startActivity(mIntent);
                    // 设置动画效果
                    getActivity().overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
                }
            }else if(4 == position){
                if(PublicMethod.isServiceWorked(SuperTestApplication.getContext(),
                        "com.meizu.testdevVideo.service.U2AutoTestService")){
                    Intent intent = new Intent(Constants.U2TaskConstants.U2_TASK_STOP_TASK);
                    getActivity().sendBroadcast(intent);
                }else{
                    ToastHelper.addToast("性能测试服务没有开启，不存在任务", SuperTestApplication.getContext());
                }
            }
        }
    }

}
