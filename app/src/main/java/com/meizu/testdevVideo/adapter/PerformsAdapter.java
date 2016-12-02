package com.meizu.testdevVideo.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meizu.testdevVideo.adapter.data.listview.TestCaseData;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.library.ViewHolderHelper;

import java.util.List;

/**
 * Created by maxueming on 2016/8/22.
 */
public class PerformsAdapter extends BaseAdapter {
    private List<TestCaseData> mTestCaseData = null;
    private Context mContext;
    private LayoutInflater inflater = null;

    public PerformsAdapter(List<TestCaseData> CaseData, Context mContext){
        this.mTestCaseData = CaseData;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
    }


    @Override
    public int getCount() {
        return mTestCaseData.size();
    }

    @Override
    public Object getItem(int position) {
        return mTestCaseData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lv_performs, parent, false);
        }
        RelativeLayout background_color = ViewHolderHelper.get(convertView, R.id.background_color);
        TextView item_app_type = ViewHolderHelper.get(convertView, R.id.item_app_type);
        TextView item_description = ViewHolderHelper.get(convertView, R.id.item_description);
        TextView item_case_name = ViewHolderHelper.get(convertView, R.id.item_case_name);
        ImageView img_performs = ViewHolderHelper.get(convertView, R.id.img_performs);

        item_app_type.setText(mTestCaseData.get(position).getTestAppType());
        TextPaint paint = item_app_type.getPaint();
        paint.setFakeBoldText(true);
        item_description.setText(mTestCaseData.get(position).getTestDescrition());
        item_case_name.setText(mTestCaseData.get(position).getCaseName());

        if("视频".equals(mTestCaseData.get(position).getTestAppType())){
            background_color.setBackgroundResource(R.drawable.performs_video_background_color);
            img_performs.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_video));
        }else if("音乐".equals(mTestCaseData.get(position).getTestAppType())){
            background_color.setBackgroundResource(R.drawable.performs_music_background_color);
            img_performs.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_music));
        }else if("读书".equals(mTestCaseData.get(position).getTestAppType())){
            background_color.setBackgroundResource(R.drawable.performs_ebook_background_color);
            img_performs.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_ebook));
        }else if("图库".equals(mTestCaseData.get(position).getTestAppType())){
            background_color.setBackgroundResource(R.drawable.performs_gallery_background_color);
            img_performs.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_gallery));
        }else if("资讯".equals(mTestCaseData.get(position).getTestAppType())){
            background_color.setBackgroundResource(R.drawable.performs_reader_background_color);
            img_performs.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_reader));
        }else if("会员".equals(mTestCaseData.get(position).getTestAppType())){
            background_color.setBackgroundResource(R.drawable.performs_account_background_color);
            img_performs.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_account));
        }
        return convertView;
    }
}
