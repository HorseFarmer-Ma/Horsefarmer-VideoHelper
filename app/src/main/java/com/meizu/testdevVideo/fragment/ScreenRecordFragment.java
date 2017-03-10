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
import com.meizu.testdevVideo.service.ScreenRecordService;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.util.wifi.DnsSettingLollipopDown;

public class ScreenRecordFragment extends Fragment {

    private Button screenRecord, btnZip;
    //    private EditText editText;
    private EditText screen_record_times;


//    private Spinner package_choose;
//    private ArrayAdapter<String> myAdapter;
//    private String choose;

    public ScreenRecordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_screen_record, container, false);

        //点击录制屏幕，开启服务后台
        screenRecord = (Button) view.findViewById(R.id.button_screenRecord);
//        btnZip = (Button) view.findViewById(R.id.button_zip);
//        editText = (EditText) view.findViewById(R.id.edt_result);
        screen_record_times = (EditText) view.findViewById(R.id.screen_record_times);


//        package_choose = (Spinner) view.findViewById(R.id.package_choose);
//        myAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getPacketList());
//        package_choose.setAdapter(myAdapter);
//        package_choose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                choose = parent.getItemAtPosition(position).toString();
//                Log.e("TAG", choose);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });


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

//        btnZip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new DnsSettingLollipopDown(getActivity().getApplicationContext()).setIpWithStaticIp();
//            }
//        });

//        btnZip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AjaxParams params = new AjaxParams();
//                params.put("\"" + iPerformsKey.deviceType + "\"", "\"" + "m1611" + "\"");
//                params.put("\"" + iPerformsKey.imei + "\"", "\"" + "862484030016805" + "\"");
//                params.put("\"" + iPerformsKey.testTime + "\"", "\"" + "1482154452950" + "\"");
//                params.put("\"" + iPerformsKey.testType + "\"", "\"" + "purebackstage" + "\"");
//
//                params.put("\"" + iPerformsKey.appType + "\"", "\"" + "读书" + "\"");
//                params.put("\"" + iPerformsKey.appVersion + "\"", "\"" + "2.0.125" + "\"");
//                params.put("\"" + iPerformsKey.systemVersion + "\"", "\"" + "m1611-user 6.0 MRA58K 1481595663 test-keys | 6.0-20161213095620_I" + "\"");
//                params.put("\"" + iPerformsKey.baseBand + "\"", "\"" + "MOLY.LR11.W1539.MD.MP.V25.2.P30, 2016/11/16 17:55" + "\"");
//                params.put("\"" + iPerformsKey.kernel + "\"", "\"" + "Linux version 3.18.22+ (flyme@mz-builder-2) (gcc version 4.9.x-google 20140827 (prerelease)" +
//                        " (GCC) ) #1 SMP PREEMPT Tue Dec 13 10:31:57 CST 2016" + "\"");
//                String i = PublicMethod.readFile(Environment.getExternalStorageDirectory()
//                        .toString() + "/" + "Android/log/dumpsys_batterystats").replace("\"", "");
//                if(i.contains("Proc " + choose)){
//                    Log.e("TAG", choose);
//                    int startNum= i.indexOf("Proc " + choose);
//                    i = i.substring(startNum-280, startNum+230);
//                }else{
//                    i = "日志中找不到Proc + packageName";
//                }
//
//                params.put("\"" + iPerformsKey.stepValue + "\"", "\"" + i + "\"");
//                params.put("\"" + iPerformsKey.packageName + "\"", "\"" + "com.meizu.media.ebook" + "\"");
//                params.put("\"" + iPerformsKey.caseName + "\"", "\"" + "WIFI灭屏5小时" + "\"");
//                params.put("\"" + iPerformsKey.result + "\"", "\"" + "true" + "\"");
//                params.put("\"" + iPerformsKey.taskId + "\"", "180");
//                // 发送报告，回调接口，判断是否发送报告结束，方便停止测试
//
//                GetFinalHttpHelper.getInstance().post("http://172.17.132.211:8080/saury2/TR_PerformAnalysis_DeviceIFCtrl/Performance.do",
//                        params, new AjaxCallBack<String>() {
//
//                    @Override
//                    public void onFailure(Throwable t, int errorNo, String strMsg) {
//                        super.onFailure(t, errorNo, strMsg);
//                        if(500 == errorNo){
//
//                        }
//                        Log.e(ScreenRecordFragment.class.getSimpleName(), "发送失败");
//                        Log.e(ScreenRecordFragment.class.getSimpleName(), String.valueOf(errorNo));
//                        Log.e(ScreenRecordFragment.class.getSimpleName(), strMsg);
//                    }
//
//                    @Override
//                    public void onStart() {
//                        super.onStart();
//
//                    }
//
//                    @Override
//                    public void onSuccess(String t) {
//                        super.onSuccess(t);
//                        //根据服务器返回的json数据，判断上传是否成功
//                        if(!TextUtils.isEmpty(t)){
//                            if("200".equals(t)){
//                                Log.e(ScreenRecordFragment.class.getSimpleName(), "发送报告成功");
//                            }else{
//                                Log.e(ScreenRecordFragment.class.getSimpleName(), "服务器异常500");
//                            }
//                        }
//                    }
//                });
//
//            }
//        });
//
//
        return view;
    }
//
//    private List<String> getPacketList() {
//        List<String> packet_list;
//        packet_list = new ArrayList<String>();
//        packet_list.add("请选择包名");
//        packet_list.add(iPublicConstants.PACKET_EBOOK);
//        packet_list.add(iPublicConstants.PACKET_GALLERY);
//        packet_list.add(iPublicConstants.PACKET_MUSIC);
//        packet_list.add(iPublicConstants.PACKET_VIDEO);
//        packet_list.add(iPublicConstants.PACKET_READER);
//        packet_list.add(iPublicConstants.PACKET_COMPAIGN);
//        return packet_list;
//    }
}
