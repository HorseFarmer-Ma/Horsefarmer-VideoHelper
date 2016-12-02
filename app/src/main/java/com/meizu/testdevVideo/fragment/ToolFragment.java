package com.meizu.testdevVideo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.activity.CommonToolsActivity;
import com.meizu.testdevVideo.adapter.data.gridview.MyContent;

public class ToolFragment extends Fragment implements AbsListView.OnItemClickListener {

    private AbsListView mListView;
    private SimpleAdapter mAdapter;
    private MyContent mToolContent;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ToolFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_grid, container, false);
        mToolContent = new MyContent();
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mToolContent.addItem(new MyContent.DummyItem("录制视频", R.mipmap.ic_record));
        mToolContent.addItem(new MyContent.DummyItem("脚本执行", R.mipmap.ic_uiautomator));
        mToolContent.addItem(new MyContent.DummyItem("应用信息", R.mipmap.ic_applist));
        mToolContent.addItem(new MyContent.DummyItem("业务更新", R.drawable.ic_app_update));

        mAdapter = new SimpleAdapter(getActivity(), mToolContent.ITEMS, R.layout.tool_listview,
                new String[]{"text", "img"},
                new int[]{R.id.tool_text, R.id.tool_img});    // 生成列表数据

        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Bundle bundle = new Bundle();
        bundle.putString("object", mToolContent.ITEMS.get(position).get("text").toString());
        Intent intent = new Intent(getActivity(), CommonToolsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        // 设置动画效果
        getActivity().overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }




}
