package com.meizu.testdevVideo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.adapter.data.listview.PostSearchData;
import com.meizu.testdevVideo.adapter.data.listview.SchemaInfo;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.library.ViewHolderHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Schema列表类
 * Created by maxueming on 2017/7/3.
 */

public class PostSearchAdapter extends BaseAdapter{
    private List<PostSearchData> list;
    private List<PostSearchData> choose;
    private LayoutInflater inflater;
    private Context context;
    private boolean isSingleChoose;

    public PostSearchAdapter(Context context, List<PostSearchData> list, boolean isSingleChoose){
        this.list = list;
        this.context = context;
        this.isSingleChoose = isSingleChoose;
        choose = new ArrayList<PostSearchData>();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(null == convertView){
            convertView = inflater.inflate(R.layout.post_search_adapter, parent, false);
        }

        TextView name = ViewHolderHelper.get(convertView, R.id.name);
        CheckBox choose_box = ViewHolderHelper.get(convertView, R.id.choose_box);
        name.setText(list.get(position).getName());

        if(isSingleChoose){
            choose_box.setVisibility(View.GONE);
        }else{
            // 防止滑动时多次调用check刷新状态
            choose_box.setOnCheckedChangeListener(null);
            choose_box.setChecked(false);

            Logger.d("check box 大小为：" + choose.size());

            for(PostSearchData sb : choose){
                if(sb.getName().equals(list.get(position).getName())){
                    choose_box.setChecked(true);
                    break;
                }
            }

            choose_box.setOnCheckedChangeListener(new CheckBoxClickListener(position));
        }

        return convertView;
    }

    private class CheckBoxClickListener implements CompoundButton.OnCheckedChangeListener{
        private int position;

        CheckBoxClickListener(int position){
            this.position = position;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                PostSearchData postSearchData = new PostSearchData();
                postSearchData.setId(list.get(position).getId());
                postSearchData.setName(list.get(position).getName());
                choose.add(postSearchData);
            }else{
                for(PostSearchData postSearchData : choose){
                    if(postSearchData.getName().equals(list.get(position).getName())){
                        choose.remove(postSearchData);
                        break;
                    }
                }
            }
        }
    }

    public List<PostSearchData> getChoose(){
        return this.choose;
    }
}
