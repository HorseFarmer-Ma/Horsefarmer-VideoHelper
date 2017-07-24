package com.meizu.testdevVideo.fragment;

import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.activity.EmptyActivity;
import com.meizu.testdevVideo.activity.PostSearchActivity;
import com.meizu.testdevVideo.constant.FragmentUtils;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.library.apkController.ApkControllerUtils;
import com.meizu.testdevVideo.service.ScreenRecordService;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.shell.ShellUtils;


public class ScreenRecordFragment extends Fragment {

    private Button screenRecord, test;
    private EditText screen_record_times;
    public ScreenRecordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen_record, container, false);
        screenRecord = (Button) view.findViewById(R.id.button_screenRecord);
        test = (Button) view.findViewById(R.id.test);
        screen_record_times = (EditText) view.findViewById(R.id.screen_record_times);
        screenRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonVariable.screen_record_times = screen_record_times.getText().toString();
                Intent mIntent = new Intent(getActivity(), ScreenRecordService.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().stopService(mIntent);   // 开始服务
                getActivity().startService(mIntent);   // 开始服务
            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                ApkControllerUtils.clientInstall(iPublicConstants.LOCAL_MEMORY + "SuperTest.apk");


            }
        });
//        test.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(null != data){
            Bundle bundle = data.getExtras();
            Logger.d("值为:" + bundle.getString("jsonData"));
        }
    }
}
