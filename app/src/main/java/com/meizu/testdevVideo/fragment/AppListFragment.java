package com.meizu.testdevVideo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.data.listview.AppInfo;
import com.meizu.testdevVideo.adapter.AppListAdapter;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.query.QueryApp;

import java.util.List;

/**
 * 展示本地应用列表
 * Created By MXM
 * 2016/6/29
 */
public class AppListFragment extends Fragment {

    private View mView;
    private SearchView searchView;  // 搜索
    private ListView appListView;  // 进程列表
    private LinearLayout mProgress;   // 加载进度圈
    private QueryApp queryApp;
    private List<AppInfo> mlistAppInfo = null;    // 列表内容
    private AppListAdapter mAppAdapter = null;

    public AppListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_app_list, container, false);
        appListView = (ListView) mView.findViewById(R.id.listviewApp);
        searchView = (SearchView) mView.findViewById(R.id.editTextApp);
        mProgress = (LinearLayout) mView.findViewById(R.id.mProgress);
        queryApp = new QueryApp(getActivity().getApplicationContext());
        mAppListThread.start();      // 启动线程
        return mView;

    }

    private Thread mAppListThread = new Thread(){
        public void run(){
            mlistAppInfo = queryApp.getListAppInfo(QueryApp.Query.FILTER_ALL_APP);
            if(null != handler){
                handler.sendEmptyMessage(100);
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler= new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 100:
                    try {
                        mProgress.setVisibility(View.GONE);
                        // 调用AppListAdapter，生成列表信息
                        mAppAdapter = new AppListAdapter(
                                getActivity().getApplicationContext(), mlistAppInfo, AppListAdapter.Choose.APP_LIST_VIEW);
                        appListView.setAdapter(mAppAdapter);
                        // 创建监听器实现类对象
                        ListListener listTextListener = new ListListener();
                        appListView.setOnItemClickListener(listTextListener);
                    }catch (NullPointerException e){
                        System.out.print(e.toString());
                    }

                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
                        @Override
                        public boolean onQueryTextChange(String query) {
                            mlistAppInfo = queryApp.searchListAppInfo(query);
                            mAppAdapter.notifyDataSetChanged();
                            return false;
                        }

                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }});
                    searchView.setSubmitButtonEnabled(false);

                    break;
            }
        }
    };
    
    class ListListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            PublicMethod.showInstalledAppDetails(getActivity(), mlistAppInfo.get(arg2).getPkgName());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
