package com.meizu.testdevVideo.fragment;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.CornerListViewAdapter;
import com.meizu.testdevVideo.adapter.data.listview.LvHistoryData;
import com.meizu.testdevVideo.db.entity.MonkeyHistoryParam;
import com.meizu.testdevVideo.library.DialogHelper;
import com.meizu.testdevVideo.library.SqlAlterHelper;
import com.meizu.widget.listview.CornerListView;

import java.util.ArrayList;
import java.util.List;


public class HistoryFragment extends Fragment {

    private CornerListView mHistoryLv;
    private List<LvHistoryData> mHistoryLvData;
    private Cursor mCursor;
    private CornerListViewAdapter mCornerListViewAdapter;

    public HistoryFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        mCursor = SqlAlterHelper.getInstance(getActivity()).query();
        mHistoryLv = (CornerListView) view.findViewById(R.id.lv_test);
        mHistoryLvData = new ArrayList<LvHistoryData>();
        mHistoryLvData = getListData();
        mCornerListViewAdapter = new CornerListViewAdapter(mHistoryLvData, getActivity());
        mHistoryLv.setAdapter(mCornerListViewAdapter);
        setClickListener();
        return view;
    }


    private void setClickListener(){
        mCornerListViewAdapter.setOnDeleteClickListener(new CornerListViewAdapter.onDeleteClickListener() {
            @Override
            public void onDeleteClick(View v, int position) {
                Log.e(HistoryFragment.class.getSimpleName(), "删除" + String.valueOf(position));
                SqlAlterHelper.getInstance(getActivity()).db.delete(MonkeyHistoryParam.TABLE_NAME,
                        "id=?", new String[]{String.valueOf(mHistoryLvData.get(position).getId())});
                mHistoryLvData.remove(position);
                mCornerListViewAdapter.notifyDataSetChanged();
            }
        });

        mCornerListViewAdapter.setOnReviewClickListener(new CornerListViewAdapter.onReviewClickListener() {
            @Override
            public void onReviewClick(View v, int position) {
                Log.e(HistoryFragment.class.getSimpleName(), "预览" + String.valueOf(position));
                DialogHelper.getInstance().createdDialogWithDismissButton(getActivity(), "预览指令",
                        mHistoryLvData.get(position).getMonkeyCommand());
            }
        });

        mCornerListViewAdapter.setOnReportClickListener(new CornerListViewAdapter.onReportClickListener() {
            @Override
            public void onReportClick(View v, int position) {
                Log.e(HistoryFragment.class.getSimpleName(), "报告" + String.valueOf(position));
                DialogHelper.getInstance().createdDialogWithDismissButton(getActivity(), "报告",
                        "实现中。。。");
            }
        });
    }


    /**
     * 读取数据库数据，添加到列表
     * @return：返回列表数据
     */
    private List<LvHistoryData> getListData(){
        List<LvHistoryData> listData = new ArrayList<LvHistoryData>();
        int number = mCursor.getCount();
        mCursor.moveToLast();
        if(0 != number){
            for(int i = 0; i < number; i++){
                listData.add(addListData(mCursor.getInt(mCursor.getColumnIndex(MonkeyHistoryParam.ID)),
                        mCursor.getString(mCursor.getColumnIndex(MonkeyHistoryParam.MONKEY_TYPE)),
                        mCursor.getString(mCursor.getColumnIndex(MonkeyHistoryParam.MONKEY_COMMAND)),
                        mCursor.getString(mCursor.getColumnIndex(MonkeyHistoryParam.START_TIME)),
                        mCursor.getString(mCursor.getColumnIndex(MonkeyHistoryParam.IS_MUTE)),
                        mCursor.getString(mCursor.getColumnIndex(MonkeyHistoryParam.IS_WIFI_LOCK)),
                        mCursor.getString(mCursor.getColumnIndex(MonkeyHistoryParam.IS_FLOATING))
                        ));
                mCursor.moveToPrevious();
            }
        }
        mCursor.close();
        return listData;
    }

    /**
     * 添加列表数据
     * @param strMonkeyType：monkey名称
     * @param strStartTime：开始时间
     * @param strMute：是否静音
     * @param strWifiLock：是否锁定wifi
     * @param strFloating：是否悬浮
     * @return：数据
     */
    private LvHistoryData addListData(int id, String strMonkeyType, String strMonkeyCommand, String strStartTime, String strMute, String strWifiLock, String strFloating){
        LvHistoryData testLvData = new LvHistoryData();
        testLvData.setId(id);
        testLvData.setMonkeyType(strMonkeyType);
        testLvData.setMonkeyCommand(strMonkeyCommand);
        testLvData.setStartTime(strStartTime);
        testLvData.setMute(strMute);
        testLvData.setWifiLock(strWifiLock);
        testLvData.setFloating(strFloating);
        return testLvData;
    }

}
