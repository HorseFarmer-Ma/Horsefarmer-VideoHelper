package com.meizu.testdevVideo.fragment;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 展示本地应用列表
 * Created By MXM
 * 2016/6/29
 */
public class AppListFragment extends Fragment {

    private View mView;
    private SearchView searchView;  // 搜索
    private ListView listView2;  // 进程列表
    private LinearLayout mProgress;   // 加载进度圈
    private List<AppInfo> mlistAppInfo = null;    // 列表内容
    private List<AppInfo> searchListAppInfo = null;   // 搜索列表
    private PackageManager pm;
    private static final int FILTER_ALL_APP = 0; // 所有应用程序
    private static final int FILTER_SYSTEM_APP = 1; // 系统程序
    private static final int FILTER_THIRD_APP = 2; // 第三方应用程序
    private static final int FILTER_SDCARD_APP = 3; // 安装在SDCard的应用程序

    public AppListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_app_list, container, false);
        listView2 = (ListView) mView.findViewById(R.id.listviewApp);
        searchView = (SearchView) mView.findViewById(R.id.editTextApp);
        mProgress = (LinearLayout) mView.findViewById(R.id.mProgress);
        mAppListThread.start();      // 启动线程
        return mView;

    }

    /**
     * 初始化
     */
    private void initAppList(){
        mlistAppInfo = new ArrayList<AppInfo>();    // 初始化应用列表
        queryAppInfo(0); // 查询所有应用程序信息
        searchListAppInfo = mlistAppInfo;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextChange(String newText) {
                searchListAppInfo = searchItem(newText);
                updateLayout(searchListAppInfo);
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO Auto-generated method stub
                return false;
            }});
        searchView.setSubmitButtonEnabled(false);
    }

    // 读取手机信息线程
    Thread mAppListThread=new Thread(){
        public void run(){
            initAppList();
            if(handler != null){
                handler.sendMessage(handler.obtainMessage());
            }else {
                mView = null;
                listView2 = null;
                searchView = null;
                mProgress = null;
            }
        }
    };

    //消息处理队列
    Handler handler= new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            mProgress.setVisibility(View.GONE);
            // 调用AppListAdapter，生成列表信息
            AppListAdapter mAppAdapter = new AppListAdapter(
                    getActivity(), mlistAppInfo);
            listView2.setAdapter(mAppAdapter);

            // 创建监听器实现类对象
            ListListener listTextListener = new ListListener();
            listView2.setOnItemClickListener(listTextListener);
        }
    };

    /**
     * 搜索列表
     * @param name
     * @return
     */
    public List<AppInfo> searchItem(String name) {
        List<AppInfo> mSearchList = new ArrayList<AppInfo>();
        for (int i = 0; i < mlistAppInfo.size(); i++) {
            // 存在匹配的数据
            if (mlistAppInfo.get(i).getAppLabel().indexOf(name) != -1
                    || mlistAppInfo.get(i).getPkgName().indexOf(name) != -1) {
                mSearchList.add(mlistAppInfo.get(i));
            }
        }
        return mSearchList;
    }

    public void updateLayout(List<AppInfo> obj) {
        listView2.setAdapter(new AppListAdapter(getActivity(), obj));
    }

    //-------------------------------- 以下为《应用信息》 界面相关函数 -------------------------------------//

    // 获得所有启动Activity的信息，类似于Launch界面
    public void queryAppInfo(int filter) {
        // 获得PackageManager对象
        pm = getActivity().getPackageManager();

        // 查询所有已经安装的应用程序
        List<ApplicationInfo> pakageinfos = pm.getInstalledApplications(0);

        // 调用系统排序 ， 根据name排序
        Collections.sort(pakageinfos, new ApplicationInfo.DisplayNameComparator(pm));

        switch (filter) {

            // 所有应用程序
            case FILTER_ALL_APP:
                mlistAppInfo.clear();
                for (ApplicationInfo app : pakageinfos) {
                    mlistAppInfo.add(getAppInfo(app));
                }
                break;

            // 系统程序
            case FILTER_SYSTEM_APP:
                mlistAppInfo.clear();
                for (ApplicationInfo app : pakageinfos) {
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        mlistAppInfo.add(getAppInfo(app));
                    }
                }
                break;

            // 第三方应用程序
            case FILTER_THIRD_APP:
                mlistAppInfo.clear();
                for (ApplicationInfo app : pakageinfos) {
                    //非系统程序
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                        mlistAppInfo.add(getAppInfo(app));
                    }
                    //本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
                    else if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
                        mlistAppInfo.add(getAppInfo(app));
                    }
                }
                break;

            // 安装在SDCard的应用程序
            case FILTER_SDCARD_APP:
                mlistAppInfo.clear();
                for (ApplicationInfo app : pakageinfos) {
                    if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                        mlistAppInfo.add(getAppInfo(app));
                    }
                }
                break;
        }
    }

    // 构造一个AppInfo对象 ，并赋值
    private AppInfo getAppInfo(ApplicationInfo app) {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppLabel((String) app.loadLabel(pm));   // 应用名
        appInfo.setAppIcon(app.loadIcon(pm));    // 应用图片
        appInfo.setPkgName(app.packageName);    // 应用包名
        appInfo.setVersion(getVersion(app.packageName));  // 应用版本号
        return appInfo;
    }



    /**
     * 获取应用版本号
     * @return 当前应用的版本号
     */
    private String getVersion(String packName) {
        try {
            PackageInfo info = pm.getPackageInfo(packName, 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    // 列表点击监听实现类
    class ListListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            // TODO Auto-generated method stub
            new PublicMethod().showInstalledAppDetails(getActivity(), searchListAppInfo.get(arg2).getPkgName());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler = null;
    }
}
