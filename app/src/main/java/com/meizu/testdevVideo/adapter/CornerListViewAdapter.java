package com.meizu.testdevVideo.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.data.listview.LvHistoryData;
import com.meizu.testdevVideo.library.ViewHolderHelper;

import java.util.List;

/**
 * Created by mxm on 2016/9/3.
 */
public class CornerListViewAdapter extends BaseAdapter{

    private List<LvHistoryData> listData;
    private LayoutInflater inflater = null;
    private Context mContext;

    public CornerListViewAdapter(List<LvHistoryData> data, Context context){
        this.listData = data;
        inflater = LayoutInflater.from(context);
        mContext = context;
    }

    private onDeleteClickListener mOnDeleteClickListener;
    private onReviewClickListener mOnReviewClickListener;
    private onReportClickListener mOnReportClickListener;

    public interface onDeleteClickListener{
        void onDeleteClick(View v, int position);
    }

    public interface onReviewClickListener{
        void onReviewClick(View v, int position);
    }

    public interface onReportClickListener{
        void onReportClick(View v, int position);
    }

    public void setOnDeleteClickListener(onDeleteClickListener onDeleteClickListener){
        this.mOnDeleteClickListener = onDeleteClickListener;
    }

    public void setOnReviewClickListener(onReviewClickListener onReviewClickListener){
        this.mOnReviewClickListener = onReviewClickListener;
    }

    public void setOnReportClickListener(onReportClickListener onReportClickListener){
        this.mOnReportClickListener = onReportClickListener;
    }


    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = inflater.inflate(R.layout.content_list, parent, false);
        }

        TextView title = ViewHolderHelper.get(convertView, R.id.tv_title);
        TextView startTime = ViewHolderHelper.get(convertView, R.id.tv_start_time);
        ImageView isMute = ViewHolderHelper.get(convertView, R.id.iv_mute);
        ImageView isWifiLock = ViewHolderHelper.get(convertView, R.id.iv_wifi);
        ImageView isFloating = ViewHolderHelper.get(convertView, R.id.iv_floating);
        ImageView iv_delete = ViewHolderHelper.get(convertView, R.id.iv_delete);
        ImageView iv_review = ViewHolderHelper.get(convertView, R.id.iv_review);
        ImageView iv_report = ViewHolderHelper.get(convertView, R.id.iv_report);

        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnDeleteClickListener.onDeleteClick(v, position);
            }
        });

        iv_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnReviewClickListener.onReviewClick(v, position);
            }
        });

        iv_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnReportClickListener.onReportClick(v, position);
            }
        });

        title.setText(listData.get(position).getMonkeyType());
        startTime.setText(listData.get(position).getStartTime());
        if(listData.get(position).getMute().equals("true")){
            isMute.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_monkey_point_use));
        }else {
            isMute.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_monkey_point_no_use));
        }

        if(listData.get(position).getWifiLock().equals("true")){
            isWifiLock.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_monkey_point_use));
        }else {
            isWifiLock.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_monkey_point_no_use));
        }

        if(listData.get(position).getFloating().equals("true")){
            isFloating.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_monkey_point_use));
        }else {
            isFloating.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_monkey_point_no_use));
        }

        return convertView;
    }
}
