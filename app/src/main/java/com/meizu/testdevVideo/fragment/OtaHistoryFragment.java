package com.meizu.testdevVideo.fragment;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.meizu.common.app.LoadingDialog;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.adapter.OtaHistoryAdapter;
import com.meizu.testdevVideo.adapter.SchemaAdapter;
import com.meizu.testdevVideo.adapter.data.listview.GetPushHistoryData;
import com.meizu.testdevVideo.adapter.data.listview.SchemaInfo;
import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.PushData;
import com.meizu.widget.listview.PullUpLoadMoreListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Ota历史记录测试
 */
public class OtaHistoryFragment extends Fragment implements DialogInterface.OnKeyListener,
        PullUpLoadMoreListView.OnLoadMoreListener, AdapterView.OnItemClickListener {

    private View view;
    private LoadingDialog dialog;
    private PullUpLoadMoreListView list_history;
    private List<GetPushHistoryData> listOfPushHistory;
    private boolean hasNextPage = true;
    private int index = 1;
    private OtaHistoryAdapter otaHistoryAdapter;
    private static final String GET_HISTORY_URL = "http://172.16.177.99/ota/uPush/customApp/search";
    private String chooseId;

    private static final int UPDATE_VIEW = 1000;
    private static final int REQUEST_FAIL = 1001;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if(null == view){
            view = inflater.inflate(R.layout.fragment_ota_history, container, false);
            list_history = (PullUpLoadMoreListView) view.findViewById(R.id.list_history);
            init();
        }

        return view;
    }


    private void init(){
        dialog = new LoadingDialog(getActivity());
        dialog.setMessage("正在加载");
        dialog.setCancelable(false);
        dialog.setOnKeyListener(this);
        dialog.show();
        listOfPushHistory = new ArrayList<GetPushHistoryData>();
        list_history.setOnLoadMoreListener(this);
        list_history.setOnItemClickListener(this);
        chooseId = PushData.getTaskId();
        new GetHistoryThreadTask().executeInSerial();
    }

    @Override
    public void loadMore() {
        if(hasNextPage){
            new GetHistoryThreadTask().executeInSerial();
        }else{
            list_history.setLoadState(false);
            ToastHelper.addLongToast("没有更多数据", SuperTestApplication.getContext());
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(listOfPushHistory.get(position).isChoose()){
            ToastHelper.addToast("至少选中一项，不可取消", SuperTestApplication.getContext());
        }else{
            listOfPushHistory.get(otaHistoryAdapter.getPositionChoose()).setChoose(false);
            listOfPushHistory.get(position).setChoose(true);
            PushData.setTaskId(listOfPushHistory.get(position).getId());
            otaHistoryAdapter.notifyDataSetChanged();
        }
    }


    private class GetHistoryThreadTask extends SimpleTaskHelper{

        @Override
        protected void doInBackground() {
            try {
                Map<String, String> getHistoryParams = new HashMap<String, String>();

                getHistoryParams.put("appId", PushData.getAppId());
                getHistoryParams.put("index", String.valueOf(index));

                PostUploadHelper.getInstance().submitPostData(GET_HISTORY_URL,
                        getHistoryParams, new PostCallBack() {
                            @Override
                            public void resultCallBack(boolean isSuccess, int resultCode, String data) {
                                if(isSuccess && null != data){
                                    JSONObject jsonObject = JSON.parseObject(data);
                                    int status = jsonObject.getInteger("status");
                                    if(status == 0){
                                        JSONArray dataJsonArray = jsonObject.getJSONArray("data");
                                        if(dataJsonArray == null){
                                            hasNextPage = false;
                                            return;
                                        }

                                        JSONObject taskDetail = dataJsonArray.getJSONObject(0);
                                        JSONArray result = taskDetail.getJSONArray("result");
                                        int size = result.size();

                                        if(size >= 10){
                                            hasNextPage = true;
                                            ++index;
                                        }else{
                                            hasNextPage = false;
                                        }

                                        for(int i = 0; i < size; i++){
                                            JSONObject history = result.getJSONObject(i);
                                            String id = history.getString("id");
                                            GetPushHistoryData getPushHistoryData = new GetPushHistoryData();
                                            if(null != id && chooseId != null && chooseId.equals(id)){
                                                getPushHistoryData.setChoose(true);
                                            }else{
                                                getPushHistoryData.setChoose(false);
                                            }
                                            getPushHistoryData.setStatus(history.getString("status"));
                                            getPushHistoryData.setId(id);
                                            getPushHistoryData.setMaxCheckVerNum(history.getString("maxCheckVerNum"));
                                            getPushHistoryData.setProduct(history.getString("product"));
                                            getPushHistoryData.setCheckAreaValue(history.getString("checkAreaValue"));
                                            getPushHistoryData.setAppName(history.getString("appName"));
                                            getPushHistoryData.setSysVersion(history.getString("sysVersion"));
                                            getPushHistoryData.setVersionNum(history.getString("versionNum"));
                                            getPushHistoryData.setCheckArea(history.getString("checkArea"));
                                            getPushHistoryData.setFileSize(history.getString("fileSize"));
                                            getPushHistoryData.setAppId(history.getString("appId"));
                                            getPushHistoryData.setVersionCode(history.getString("versionCode"));
                                            getPushHistoryData.setMinCheckVerNum(history.getString("minCheckVerNum"));
                                            getPushHistoryData.setVerRange(history.getString("verRange"));
                                            listOfPushHistory.add(getPushHistoryData);
                                        }
                                        handler.sendEmptyMessage(UPDATE_VIEW);
                                    }

                                }else{
                                    handler.sendEmptyMessage(REQUEST_FAIL);
                                }
                            }
                        });
            } catch (Exception e) {
                Logger.d("请求异常");
                e.printStackTrace();
                handler.sendEmptyMessage(REQUEST_FAIL);
            }
        }
    }



    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_VIEW:
                    dialog.dismiss();
                    if(otaHistoryAdapter == null){
                        otaHistoryAdapter = new OtaHistoryAdapter(getActivity(), listOfPushHistory);
                        list_history.setAdapter(otaHistoryAdapter);
                    }else{
                        otaHistoryAdapter.notifyDataSetChanged();
                    }
                    // 加载完毕
                    list_history.setLoadState(false);
                    break;
                case REQUEST_FAIL:
                    dialog.dismiss();
                    list_history.setLoadState(false);
                    break;
                default:
                    break;
            }
        }
    };




    @Override
    public void onDestroy() {
        if(null != handler){
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dialog.dismiss();
            getActivity().finish();
        }
        return false;
    }
}
