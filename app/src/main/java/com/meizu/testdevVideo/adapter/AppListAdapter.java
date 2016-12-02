package com.meizu.testdevVideo.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.data.listview.AppInfo;

//自定义适配器类，提供给listView的自定义view
public class AppListAdapter extends BaseAdapter {

	private List<AppInfo> mlistAppInfo = null;
	LayoutInflater infater = null;
	public AppListAdapter(Context context,  List<AppInfo> apps) {
		infater = LayoutInflater.from(context);
		mlistAppInfo = apps ;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		System.out.println("size" + mlistAppInfo.size());
		return mlistAppInfo.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mlistAppInfo.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertview, ViewGroup arg2) {
		System.out.println("getView at " + position);
		View view = null;
		ViewHolder holder = null;
		if (convertview == null || convertview.getTag() == null) {
			view = infater.inflate(R.layout.applistview, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		}
		else{
			view = convertview ;
			holder = (ViewHolder) convertview.getTag() ;
		}
		AppInfo appInfo = (AppInfo) getItem(position);
		holder.appIcon.setImageDrawable(appInfo.getAppIcon());     // 设置图像
		holder.PkgName.setText(appInfo.getPkgName());   // 设置包名
		holder.title.setText(appInfo.getAppLabel());   // 设置标题
		holder.versionName.setText(appInfo.getVersion());   // 设置版本号

		return view;
	}

	class ViewHolder {
		ImageView appIcon;
		TextView title;
		TextView PkgName;
		TextView versionName;

		public ViewHolder(View view) {
			this.appIcon = (ImageView) view.findViewById(R.id.img);
			this.title = (TextView) view.findViewById(R.id.title);
			this.PkgName = (TextView) view.findViewById(R.id.packet);
			this.versionName = (TextView) view.findViewById(R.id.versionName);
		}
	}
}  