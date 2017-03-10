package com.meizu.testdevVideo.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.data.gridview.MonkeyWidgetList;
import com.meizu.testdevVideo.library.ViewHolderHelper;

public class MonkeyAdapter extends BaseAdapter{
	private List<MonkeyWidgetList> mList;
	private LayoutInflater inflater = null;

	public MonkeyAdapter(List<MonkeyWidgetList> list, Context context) {
		this.mList = list;
		inflater = LayoutInflater.from(context);
	}


	private onCheckedChangedListener mOnCheckedChangedListener;

	// 服务接口
	public interface onCheckedChangedListener
	{
		void onCheckedChange(CompoundButton buttonView, boolean isChecked, int position);
	}

	public void setOnCheckedChangedLitener(onCheckedChangedListener onCheckedChangedListener){
		this.mOnCheckedChangedListener = onCheckedChangedListener;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Log.e("AppChooseActivity", "渲染刷新行数：" + position);
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.listviewitem, parent, false);
		}

		Switch widget_bt = ViewHolderHelper.get(convertView, R.id.widget_bt);
		widget_bt.setChecked(mList.get(position).getSwitch());
		widget_bt.setText(mList.get(position).getFunction());
		widget_bt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mList.get(position).setSwitch(isChecked);   // 放在前面，回调的时候先赋值
				mOnCheckedChangedListener.onCheckedChange(buttonView, isChecked, position);
			}
		});

		return convertView;
	}
}  