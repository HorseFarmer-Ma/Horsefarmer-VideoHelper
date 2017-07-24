package com.meizu.testdevVideo.adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.meizu.testdevVideo.adapter.data.listview.TestCaseData;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.library.ViewHolderHelper;

import java.util.List;

/**
 * 显示适配类
 * Created by maxueming on 2016/8/22.
 */
public class PerformsAdapter extends BaseAdapter {
    private List<TestCaseData> mTestCaseData = null;
    private Context mContext;
    private LayoutInflater inflater = null;

    public PerformsAdapter(List<TestCaseData> CaseData, Context context){
        this.mTestCaseData = CaseData;
        this.mContext = context;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lv_performs, parent, false);
        }
        TextView item_description = ViewHolderHelper.get(convertView, R.id.item_description);
        TextView item_case_name = ViewHolderHelper.get(convertView, R.id.item_case_name);
        ImageView img_add_case = ViewHolderHelper.get(convertView, R.id.img_add_case);
        item_description.setText(mTestCaseData.get(position).getTestDescrition());
        item_case_name.setText(mTestCaseData.get(position).getCaseName());
        if(mTestCaseData.get(position).isChoose()){
            img_add_case.setImageResource(R.drawable.ic_yes);
        }else{
            img_add_case.setImageResource(R.drawable.ic_add_2);
        }

        return convertView;
    }
}
