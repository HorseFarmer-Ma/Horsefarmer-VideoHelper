package com.meizu.testdevVideo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.data.listview.GetPushHistoryData;
import com.meizu.testdevVideo.library.ViewHolderHelper;

import java.util.List;

/**
 * OTA推送适配器
 * Created by maxueming on 2017/7/20.
 */

public class OtaHistoryAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {

    private LayoutInflater inflater;
    private List<GetPushHistoryData> listOfOtaHistory;

    public OtaHistoryAdapter(Context context, List<GetPushHistoryData> listOfOtaHistory){
        inflater = LayoutInflater.from(context);
        this.listOfOtaHistory = listOfOtaHistory;
    }

    @Override
    public int getCount() {
        return listOfOtaHistory.size();
    }

    @Override
    public Object getItem(int position) {
        return listOfOtaHistory.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(listOfOtaHistory.get(position).getId());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(null == convertView){
            convertView = inflater.inflate(R.layout.ota_history_adapter, parent, false);
        }

        TextView app_name = ViewHolderHelper.get(convertView, R.id.app_name);
        TextView app_version = ViewHolderHelper.get(convertView, R.id.app_version);
        TextView product = ViewHolderHelper.get(convertView, R.id.product);
        TextView android_version = ViewHolderHelper.get(convertView, R.id.android_version);
        TextView check_area = ViewHolderHelper.get(convertView, R.id.check_area);
        TextView status = ViewHolderHelper.get(convertView, R.id.status);
        CheckBox isChoose = ViewHolderHelper.get(convertView, R.id.isChoose);

        app_name.setText(listOfOtaHistory.get(position).getAppName());
        app_version.setText(listOfOtaHistory.get(position).getVersionNum());
        product.setText(listOfOtaHistory.get(position).getProduct());
        android_version.setText(listOfOtaHistory.get(position).getSysVersion());
        check_area.setText(listOfOtaHistory.get(position).getCheckArea());
        status.setText(listOfOtaHistory.get(position).getStatus());
        isChoose.setChecked(listOfOtaHistory.get(position).isChoose());

        if(listOfOtaHistory.get(position).isChoose()){
            setPositionChoose(position);
        }

        return convertView;
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }


    private int positionChoose;

    public void setPositionChoose(int position){
        positionChoose = position;
    }

    public int getPositionChoose(){
        return positionChoose;
    }
}
