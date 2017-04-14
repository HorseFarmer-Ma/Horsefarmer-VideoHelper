package com.meizu.testdevVideo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.activity.CommonToolsActivity;
import com.meizu.testdevVideo.activity.MainActivity;
import com.meizu.testdevVideo.adapter.data.gridview.MyContent;
import com.meizu.testdevVideo.library.AnimationHelper;

public class ToolFragment extends Fragment implements AbsListView.OnItemClickListener{

    private AbsListView mListView;
    private SimpleAdapter mAdapter;
    private MyContent mToolContent;
    private ScaleAnimation animation;
    private View rootView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(null == rootView){
            rootView = inflater.inflate(R.layout.fragment_item_grid, container, false);

            mToolContent = new MyContent();
            mListView = (AbsListView) rootView.findViewById(android.R.id.list);
            mToolContent.addItem(new MyContent.DummyItem("录制视频", R.mipmap.ic_record));
            mToolContent.addItem(new MyContent.DummyItem("脚本执行", R.mipmap.ic_uiautomator));
            mToolContent.addItem(new MyContent.DummyItem("应用信息", R.mipmap.ic_applist));
            mToolContent.addItem(new MyContent.DummyItem("业务更新", R.drawable.ic_app_update));
//            mToolContent.addItem(new MyContent.DummyItem("Scheme Test", R.drawable.ic_app_update));
//        mToolContent.addItem(new MyContent.DummyItem("业务Test", R.drawable.ic_app_update));

            mAdapter = new SimpleAdapter(getActivity(), mToolContent.ITEMS, R.layout.tool_listview,
                    new String[]{"text", "img"},
                    new int[]{R.id.tool_text, R.id.tool_img});    // 生成列表数据

            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);
            animation = AnimationHelper.getInstance().getScaleAnimation(1.0f, 1.0f, 0f, 1.0f, 0, 0, 500, true, 0.5f, 0f);
        }

        mListView.setAnimation(animation);
        animation.cancel();
        animation.startNow();

        return rootView;
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


    @Override
    public void onResume() {
        super.onResume();
        Log.d(ToolFragment.class.getSimpleName(), "执行onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        animation.cancel();
    }
}
