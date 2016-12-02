package com.meizu.testdevVideo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.widget.checkbox.AnimateCheckBox;

import java.util.List;
import java.util.Map;

/**
 * Created by liuzipeng on 15/12/4.
 */
public class UiautomatorAdapter extends RecyclerView.Adapter<UiautomatorAdapter.MyViewHolder> {

    private List<Map<String, Object>> mList;        // 案例列表数据
    private Context context;
    private LayoutInflater mInflater;

    public UiautomatorAdapter(Context context, List<Map<String, Object>> mList) {
        this.context = context;
        this.mList = mList;
        mInflater = LayoutInflater.from(context);
    }

    private OnItemClickLitener mOnItemClickLitener;
    private OnCheckBoxChangeListener mOnCheckBoxChangeListener;

    // 服务接口
    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    // 服务接口
    public interface OnCheckBoxChangeListener {
        void onCheckBoxChange(View view, int position, boolean isChecked);
    }


    // 回调接口，接口方法在此类中实现并进行回调
    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    // 回调接口，接口方法在此类中实现并进行回调
    public void setOnCheckedChangeListener(OnCheckBoxChangeListener mOnCheckBoxChangeListener) {
        this.mOnCheckBoxChangeListener = mOnCheckBoxChangeListener;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType != 1) {
            view = mInflater.inflate(R.layout.item, parent, false);
        } else {
            view = mInflater.inflate(R.layout.last_item, parent, false);
        }

        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.case_name.setText(mList.get(position).get("caseName").toString());
        holder.case_detail.setText(mList.get(position).get("caseDetail").toString());
        holder.checkBox.setChecked((Boolean)mList.get(position).get("checkBox"));

        // 如果设置了回调，则设置列表点击事件
        if (mOnItemClickLitener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(holder.itemView, pos);
                    return true;
                }
            });
        }

        // 如果设置了回调，则设置CheckBox选中事件
        if(mOnCheckBoxChangeListener != null){
            holder.checkBox.setOnCheckedChangeListener(new AnimateCheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(View view, boolean isChecked) {
                    int pos = holder.getLayoutPosition();
                    mOnCheckBoxChangeListener.onCheckBoxChange(holder.checkBox, pos, isChecked);
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mList.size() - 1) {
            return 1;
        } else {
            return 0;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView case_name;
        TextView case_detail;
        AnimateCheckBox checkBox;

        public MyViewHolder(View itemView) {
            super(itemView);
            checkBox = (AnimateCheckBox) itemView.findViewById(R.id.checkbox);   //
            case_name = (TextView) itemView.findViewById(R.id.case_name);
            case_detail = (TextView) itemView.findViewById(R.id.case_detail);
        }
    }
}
