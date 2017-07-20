package com.meizu.testdevVideo.task.performs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.db.bean.U2TaskBean;
import com.meizu.testdevVideo.library.ViewHolderHelper;

import java.util.List;


/**
 * 已添加的U2任务列表
 * Created by maxueming on 2017/6/12.
 */
public class U2TaskListAdapter extends BaseAdapter{
    private Context context;
    private List<U2TaskBean> u2TaskListData;
    private OnItemDeleteListener onItemDeleteListener;

    public U2TaskListAdapter(Context context, List<U2TaskBean> u2TaskListData){
        this.context = context;
        this.u2TaskListData = u2TaskListData;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener onItemDeleteListener){
        this.onItemDeleteListener = onItemDeleteListener;
    }

    public interface OnItemDeleteListener{
        void onClick(int position);
    }

    @Override
    public int getCount() {
        return u2TaskListData.size();
    }

    @Override
    public Object getItem(int position) {
        return u2TaskListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (null == convertView){
            convertView = LayoutInflater.from(context).inflate(R.layout.u2_task_list_adapter, parent, false);
        }
        TextView caseName = ViewHolderHelper.get(convertView, R.id.case_name);
        ImageView btnDeleteCase = ViewHolderHelper.get(convertView, R.id.btn_delete_case);
        ImageView imgPerformsType = ViewHolderHelper.get(convertView, R.id.img_performs_type);
        caseName.setText(u2TaskListData.get(position).getCaseStep());
        btnDeleteCase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != onItemDeleteListener){
                    onItemDeleteListener.onClick(position);
                }
            }
        });

        imgPerformsType.setImageResource(getCaseImgResourceId(position));

        return convertView;
    }

    private int getCaseImgResourceId(int position){
        int id = -1;
        if(u2TaskListData.get(position).getPerformsType()
                .equals(Constants.U2TaskConstants.PERFORMS_TYPE_STARTTIME)){
            id = R.drawable.ic_point_starttime;
        }else if(u2TaskListData.get(position).getPerformsType()
                .equals(Constants.U2TaskConstants.PERFORMS_TYPE_FRAGMENT)){
            id = R.drawable.ic_point_fps;
        }else if(u2TaskListData.get(position).getPerformsType()
                .equals(Constants.U2TaskConstants.PERFORMS_TYPE_MEMORY)){
            id = R.drawable.ic_point_memory;
        }else{
            id = R.drawable.ic_point_pure;
        }

        return id;
    }

}
