package com.meizu.testdevVideo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.service.ScreenRecordService;
import com.meizu.testdevVideo.constant.CommonVariable;

public class ScreenRecordFragment extends Fragment {

    private Button screenRecord, btnZip, btnUnZip;
    private EditText screen_record_times;

    public ScreenRecordFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_screen_record, container, false);

        //点击录制屏幕，开启服务后台
        screenRecord = (Button) view.findViewById(R.id.button_screenRecord);
        btnZip = (Button) view.findViewById(R.id.button_zip);
        btnUnZip = (Button) view.findViewById(R.id.button_unzip);

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

        btnZip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnZip.setText("帧率采集测试");
            }
        });

        btnUnZip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });





        return view;
    }
}
