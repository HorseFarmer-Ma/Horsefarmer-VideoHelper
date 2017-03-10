package com.meizu.testdevVideo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.data.gridview.MyContent;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.AnimationHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.download.DownloadHelper;
import com.meizu.testdevVideo.util.sharepreference.PrefWidgetOnOff;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppUpdateFragment extends Fragment implements AbsListView.OnItemClickListener {
    private MyContent mContent;
    private AbsListView mListView;
    private SimpleAdapter mAdapter;
    private ScaleAnimation animation;
    private TextView txt_last_update_time, txt_last_update_app;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_app_update, container, false);
        mContent = new MyContent();
        txt_last_update_time = (TextView)mView.findViewById(R.id.txt_last_update_time);
        txt_last_update_app = (TextView)mView.findViewById(R.id.txt_last_update_app);

        mListView = (AbsListView) mView.findViewById(android.R.id.list);
        mContent.addItem(new MyContent.DummyItem("视频", R.drawable.ic_video));
        mContent.addItem(new MyContent.DummyItem("音乐", R.drawable.ic_music));
        mContent.addItem(new MyContent.DummyItem("资讯", R.drawable.ic_reader));
        mContent.addItem(new MyContent.DummyItem("读书", R.drawable.ic_ebook));
        mContent.addItem(new MyContent.DummyItem("图库", R.drawable.ic_gallery));
        mContent.addItem(new MyContent.DummyItem("会员", R.drawable.ic_account));

        mAdapter = new SimpleAdapter(getActivity(), mContent.ITEMS, R.layout.tool_listview,
                new String[]{"text", "img"},
                new int[]{R.id.tool_text, R.id.tool_img});    // 生成列表数据

        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        if(TextUtils.isEmpty(PrefWidgetOnOff.getInstance(getActivity()).readStringData("txt_last_update_time"))){
            txt_last_update_time.setText("还未用该软件进行过更新应用操作");
            txt_last_update_app.setVisibility(View.GONE);
        }else{
            txt_last_update_time.setText("上次更新时间：" + PrefWidgetOnOff.getInstance(getActivity()).readStringData("txt_last_update_time"));
            txt_last_update_app.setText("上次更新应用：" + PrefWidgetOnOff.getInstance(getActivity()).readStringData("txt_last_update_app"));
        }
        animation = AnimationHelper.getInstance().getScaleAnimation(0f, 1.0f, 1.0f, 1.0f, 300, 0, 400, true, 0.5f, 0.5f);
        mListView.setAnimation(animation);
        txt_last_update_app.setAnimation(animation);
        txt_last_update_time.setAnimation(animation);
        animation.startNow();

        return mView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                CommonVariable.strVideoId = DownloadHelper.getInstance(getActivity()).download("http://ats.meizu.com/static/upload/user-resources/SuperTest/MediaAppUpdate/Video/video.apk",
                        "/SuperTest/UpdateApk/", "video.apk");
                PrefWidgetOnOff.getInstance(getActivity()).writeStringData("txt_last_update_app", "视频");
                break;
            case 1:
                CommonVariable.strMusicId = DownloadHelper.getInstance(getActivity()).download("http://ats.meizu.com/static/upload/user-resources/SuperTest/MediaAppUpdate/Music/music.apk",
                        "/SuperTest/UpdateApk/", "music.apk");
                PrefWidgetOnOff.getInstance(getActivity()).writeStringData("txt_last_update_app", "音乐");
                break;
            case 2:
                CommonVariable.strReaderId = DownloadHelper.getInstance(getActivity()).download("http://ats.meizu.com/static/upload/user-resources/SuperTest/MediaAppUpdate/Reader/reader.apk",
                        "/SuperTest/UpdateApk/", "reader.apk");
                PrefWidgetOnOff.getInstance(getActivity()).writeStringData("txt_last_update_app", "资讯");
                break;
            case 3:
                CommonVariable.strEbookId = DownloadHelper.getInstance(getActivity()).download("http://ats.meizu.com/static/upload/user-resources/SuperTest/MediaAppUpdate/Ebook/ebook.apk",
                        "/SuperTest/UpdateApk/", "ebook.apk");
                PrefWidgetOnOff.getInstance(getActivity()).writeStringData("txt_last_update_app", "读书");
                break;
            case 4:
                CommonVariable.strGalleryId = DownloadHelper.getInstance(getActivity()).download("http://ats.meizu.com/static/upload/user-resources/SuperTest/MediaAppUpdate/Gallery/gallery.apk",
                        "/SuperTest/UpdateApk/", "gallery.apk");
                PrefWidgetOnOff.getInstance(getActivity()).writeStringData("txt_last_update_app", "图库");
                break;
            case 5:
                CommonVariable.strVipId = DownloadHelper.getInstance(getActivity()).download("http://ats.meizu.com/static/upload/user-resources/SuperTest/MediaAppUpdate/Vip/vip.apk",
                        "/SuperTest/UpdateApk/", "vip.apk");
                PrefWidgetOnOff.getInstance(getActivity()).writeStringData("txt_last_update_app", "会员");
                break;
        }

        PrefWidgetOnOff.getInstance(getActivity()).writeStringData("txt_last_update_time", PublicMethod.getSystemTime());
        txt_last_update_app.setVisibility(View.VISIBLE);
        txt_last_update_time.setText("上次更新时间：" + PrefWidgetOnOff.getInstance(getActivity()).readStringData("txt_last_update_time"));
        txt_last_update_app.setText("上次更新应用：" + PrefWidgetOnOff.getInstance(getActivity()).readStringData("txt_last_update_app"));
        ToastHelper.addToast("通知栏下载中，请勿重复点击！", getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        // 删除更新包
        File mApk = new File(iPublicConstants.LOCAL_MEMORY + "/SuperTest/UpdateApk/");
        if(mApk.exists()){
            PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "/SuperTest/UpdateApk");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
