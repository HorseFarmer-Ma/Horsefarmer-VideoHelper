package com.meizu.testdevVideo.fragment;


import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.activity.CommonToolsActivity;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.data.gridview.MyContent;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.util.sharepreference.BaseData;

import flyme.support.v7.app.AlertDialog;


/**
 * 性能测试Fragment
 */
public class CommonFragment extends Fragment {
    private AbsListView mToolListView;
    private SimpleAdapter mToolAdapter;
    private MyContent mToolContent;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(null == rootView){
            rootView = inflater.inflate(R.layout.fragment_item_grid, container, false);
            mToolListView = (AbsListView) rootView.findViewById(R.id.list_tool);
//            mPerformsContent.addItem(new MyContent.DummyItem(uiautomator2, R.drawable.ic_time));

            mToolContent = new MyContent();
            mToolContent.addItem(new MyContent.DummyItem("录制视频", R.drawable.ic_record));
            mToolContent.addItem(new MyContent.DummyItem("脚本执行", R.drawable.ic_uiautomator));
            mToolContent.addItem(new MyContent.DummyItem("应用信息", R.drawable.ic_applist));
            mToolContent.addItem(new MyContent.DummyItem("业务更新", R.drawable.ic_app_update));
            mToolContent.addItem(new MyContent.DummyItem("Schema测试", R.drawable.ic_schema));
            mToolContent.addItem(new MyContent.DummyItem("OTA推送", R.drawable.ic_schema));
            mToolAdapter = new SimpleAdapter(getActivity(), mToolContent.ITEMS, R.layout.tool_listview,
                    new String[]{"text", "img"},
                    new int[]{R.id.tool_text, R.id.tool_img});    // 生成列表数据

            mToolListView.setAdapter(mToolAdapter);
            mToolListView.setOnItemClickListener(new OnItemClickListener());
        }

        return rootView;
    }

    class OnItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(4 == position && TextUtils.isEmpty(BaseData.getInstance(SuperTestApplication.getContext())
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
                bundle.putString("object", mToolContent.ITEMS.get(position).get("text").toString());
                Intent intent = new Intent(getActivity(), CommonToolsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                // 设置动画效果
                getActivity().overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
