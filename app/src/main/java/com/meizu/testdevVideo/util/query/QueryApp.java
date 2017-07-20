package com.meizu.testdevVideo.util.query;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.meizu.testdevVideo.adapter.data.listview.AppInfo;
import com.meizu.testdevVideo.util.PublicMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by maxueming on 2017/1/22.
 */
public class QueryApp {

    private Context mContext;
    private PackageManager pm = null;
    private List<AppInfo> mListAppInfo = null;    // 列表内容
    private List<AppInfo> mSearchList = null;    // 列表内容

    public QueryApp(Context context){
        mContext = context;
    }

    public enum Query{
        FILTER_ALL_APP, FILTER_SYSTEM_APP, FILTER_THIRD_APP, FILTER_SDCARD_APP
    }


    public List<AppInfo> getListAppInfo(Query filter){
        if(null == mListAppInfo){
            mListAppInfo = new ArrayList<AppInfo>();
        }

        if(null == mSearchList){
            mSearchList = new ArrayList<AppInfo>();
        }

        // 获得PackageManager对象
        pm = mContext.getPackageManager();
        // 查询所有已经安装的应用程序
        List<ApplicationInfo> pakageinfos = pm.getInstalledApplications(0);
        // 调用系统排序 ， 根据name排序
        Collections.sort(pakageinfos, new ApplicationInfo.DisplayNameComparator(pm));
        mListAppInfo.clear();
        mSearchList.clear();
        switch (filter) {
            // 所有应用程序
            case FILTER_ALL_APP:
                for (ApplicationInfo app : pakageinfos) {
                    mListAppInfo.add(getAppInfo(app));
                    mSearchList.add(getAppInfo(app));
                }
                break;

            // 系统程序
            case FILTER_SYSTEM_APP:
                for (ApplicationInfo app : pakageinfos) {
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        mListAppInfo.add(getAppInfo(app));
                        mSearchList.add(getAppInfo(app));
                    }
                }
                break;

            // 第三方应用程序
            case FILTER_THIRD_APP:
                for (ApplicationInfo app : pakageinfos) {
                    //非系统程序
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                        mListAppInfo.add(getAppInfo(app));
                        mSearchList.add(getAppInfo(app));
                    } else if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
                        //本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
                        mListAppInfo.add(getAppInfo(app));
                        mSearchList.add(getAppInfo(app));
                    }
                }
                break;

            // 安装在SDCard的应用程序
            case FILTER_SDCARD_APP:
                for (ApplicationInfo app : pakageinfos) {
                    if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                        mListAppInfo.add(getAppInfo(app));
                        mSearchList.add(getAppInfo(app));
                    }
                }
                break;
        }

        return mListAppInfo;
    }

    /**
     * 搜索列表
     * @param name
     * @return
     */
    public List<AppInfo> searchListAppInfo(String name) {
        mListAppInfo.clear();
        if(null == name || name.equals("")){
            for (int i = 0; i < mSearchList.size(); i++) {
                mListAppInfo.add(mSearchList.get(i));
            }
        }else{
            for (int i = 0; i < mSearchList.size(); i++) {
                // 存在匹配的数据
                if (mSearchList.get(i).getAppLabel().contains(name)
                        || mSearchList.get(i).getPkgName().contains(name)) {
                    mListAppInfo.add(mSearchList.get(i));
                }
            }
        }
        return mListAppInfo;
    }

    // 构造一个AppInfo对象 ，并赋值
    private AppInfo getAppInfo(ApplicationInfo app) {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppLabel((String) app.loadLabel(pm));   // 应用名
        appInfo.setAppIcon(app.loadIcon(pm));    // 应用图片
        appInfo.setPkgName(app.packageName);    // 应用包名
        appInfo.setVersion(PublicMethod.getVersion(pm, app.packageName));  // 应用版本号
        return appInfo;
    }



}
