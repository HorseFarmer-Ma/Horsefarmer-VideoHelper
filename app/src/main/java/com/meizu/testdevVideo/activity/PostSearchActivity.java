package com.meizu.testdevVideo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.meizu.common.widget.LoadingAnimotionView;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.adapter.PostSearchAdapter;
import com.meizu.testdevVideo.adapter.data.listview.PostSearchData;
import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.wifi.WifiUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostSearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener, View.OnClickListener{

    private SearchView search;
    private LoadingAnimotionView loading;
    private TextView notified;
    private ListView list;
    private Button choose_complete;
    private LinearLayout notifiedLayout;
    private String url;
    private static final int UPDATE_LIST = 1000;
    private static final int NO_NET = 1001;
    private static final int UPDATE_LIST_FAIL = 1002;
    private List<PostSearchData> listData;
    private List<PostSearchData> queryData;
    private List<PostSearchData> multiChoosedata;
    private PostSearchAdapter mAdapter;
    private String productList = "产品列表";
    private String appList = "应用列表";
    private String androidList = "安卓版本";
    private static Activity destActivity;
    private boolean isSingleChoose;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_search);
        url = getIntent().getStringExtra("URL");
        isSingleChoose = getIntent().getBooleanExtra("IS_SINGLE_CHOOSE", false);
        search = (SearchView) findViewById(R.id.search);
        choose_complete = (Button) findViewById(R.id.choose_complete);
        list = (ListView) findViewById(R.id.list);
        loading = (LoadingAnimotionView) findViewById(R.id.loading);
        notified = (TextView) findViewById(R.id.txt_notified);
        notifiedLayout = (LinearLayout) findViewById(R.id.notified_layout);

        list.setOnItemClickListener(this);
        search.setOnQueryTextListener(this);
        search.setSubmitButtonEnabled(false);
        simpleTask.executeInSerial();
        choose_complete.setOnClickListener(this);
    }

    public static void setDestActivity(Activity activity){
        destActivity = activity;
    }

    private SimpleTaskHelper simpleTask = new SimpleTaskHelper(){

        @Override
        protected void doInBackground() {
            if(WifiUtil.isWifiConnected(SuperTestApplication.getContext())){
                listData = new ArrayList<PostSearchData>();
                queryData = new ArrayList<PostSearchData>();
                multiChoosedata = new ArrayList<PostSearchData>();
                try {
                    Map<String, String> loadingParams = new HashMap<String, String>();
                    loadingParams.put("user", "appadmin");
                    PostUploadHelper.getInstance().submitPostData(url, loadingParams, new PostCallBack() {
                        @Override
                        public void resultCallBack(boolean isSuccess, int resultCode, String data) {
                            if(isSuccess && null != data){
                                JSONObject json = JSON.parseObject(data);
                                int status = json.getInteger("status");
                                if(0 == status){
                                    Logger.d("拉取列表成功");
                                    String message = json.getString("message");
                                    if(message.contains(productList)){
                                        JSONObject dataObject = json.getJSONObject("data");
                                        JSONArray childrenArray = dataObject.getJSONArray("children");
                                        int size = childrenArray.size();
                                        if(size > 0){
                                            getProductList(childrenArray);
                                        }
                                    }else if(message.contains(appList) || message.contains(androidList)){
                                        JSONArray jsonArray = json.getJSONArray("data");
                                        int size = jsonArray.size();
                                        for(int i = 0; i < size; i++){
                                            JSONObject jsonObject = JSONObject.parseObject(jsonArray.get(i).toString());
                                            PostSearchData postSearchData = new PostSearchData();
                                            postSearchData.setId(jsonObject.getString("value"));
                                            postSearchData.setName(jsonObject.getString("dis"));
                                            listData.add(postSearchData);
                                        }
                                    }

                                    Logger.d("列表总数为" + listData.size());
                                    queryData.addAll(listData);
                                    handler.sendEmptyMessage(UPDATE_LIST);
                                }else{
                                    Logger.d("拉取列表失败");
                                    handler.sendEmptyMessage(UPDATE_LIST_FAIL);
                                }
                            }else{
                                Logger.d("接口失败");
                                handler.sendEmptyMessage(UPDATE_LIST_FAIL);
                            }

                        }
                    });
                } catch (IOException e) {
                    Logger.d("接口请求失败");
                    handler.sendEmptyMessage(UPDATE_LIST_FAIL);
                    e.printStackTrace();
                }
            }else{
                handler.sendEmptyMessage(NO_NET);
            }
        }
    };

    /**
     * 获取产品雷彪
     * @param children
     */
    private void getProductList(JSONArray children){
        for(int i = 0; i < children.size(); i++){
            JSONObject childrenObject = JSONObject.parseObject(children.get(i).toString());
            PostSearchData postSearchData = new PostSearchData();
            postSearchData.setId(childrenObject.getString("id"));
            postSearchData.setName(childrenObject.getString("name"));
            listData.add(postSearchData);

            JSONArray jsonArray = childrenObject.getJSONArray("children");
            if(jsonArray.size() > 0){
                getProductList(jsonArray);
            }
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_LIST:
                    loading.setVisibility(View.GONE);
                    mAdapter = new PostSearchAdapter(PostSearchActivity.this, queryData, isSingleChoose);
                    list.setAdapter(mAdapter);
                    break;
                case UPDATE_LIST_FAIL:
                    notified.setText(getString(R.string.loading_fail));
                    loading.setVisibility(View.GONE);
                    notifiedLayout.setVisibility(View.VISIBLE);
                    break;
                case NO_NET:
                    notified.setText(getString(R.string.no_netword));
                    loading.setVisibility(View.GONE);
                    notifiedLayout.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(PostSearchActivity.this, destActivity.getClass());
        Bundle bundle = new Bundle();
        Logger.d(JSON.toJSONString(queryData.get(position)));
        PostSearchData postSearchData = new PostSearchData();
        postSearchData.setId(queryData.get(position).getId());
        postSearchData.setName(queryData.get(position).getName());
        multiChoosedata.add(postSearchData);
        bundle.putString("jsonData", JSON.toJSONString(multiChoosedata));
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        queryData.clear();
        if(TextUtils.isEmpty(newText)){
            queryData.addAll(listData);
        }else{
            for(PostSearchData data : listData){
                if(data.getName().contains(newText)){
                    queryData.add(data);
                }
            }
        }

        mAdapter.notifyDataSetChanged();
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != destActivity){
            destActivity = null;
        }

        if(null != handler){
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.choose_complete:
                Intent intent = new Intent(PostSearchActivity.this, destActivity.getClass());
                Bundle bundle = new Bundle();
                bundle.putString("jsonData", JSON.toJSONString(mAdapter.getChoose()));
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
                setResult(RESULT_OK, intent);
                finish();
                break;
        }

    }
}
