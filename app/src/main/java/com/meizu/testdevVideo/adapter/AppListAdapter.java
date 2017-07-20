package com.meizu.testdevVideo.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.data.listview.AppInfo;
import com.meizu.testdevVideo.library.ViewHolderHelper;

//自定义适配器类，提供给listView的自定义view
public class AppListAdapter extends BaseAdapter {

	public enum Choose{
		APP_LIST_VIEW, APP_CHOOSE, MONKEY_APP_CHOOSE
	}

	private Choose choose;
	private List<AppInfo> mlistAppInfo = null;
	private LayoutInflater infater = null;
	private OnCheckListener onCheckListener = null;
	private Map<String, String> appCheck;

	public interface OnCheckListener{
		void onCheck(int position, boolean isChecked);
	}

	public void setOnCheckListener(OnCheckListener listener){
		onCheckListener = listener;
	}


	public AppListAdapter(Context context,  List<AppInfo> apps, Choose choose) {
		infater = LayoutInflater.from(context);
		mlistAppInfo = apps ;
		this.choose = choose;
		appCheck = new HashMap<String, String>();
	}

	@Override
	public int getCount() {
		return mlistAppInfo.size();
	}
	@Override
	public Object getItem(int position) {
		return mlistAppInfo.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	public Map<String, String> getCheckBoxMap(){
		return appCheck;
	}

	public int getCheckBoxMapNumber(){
		return appCheck.size();
	}

	@Override
	public View getView(final int position, View convertview, ViewGroup container) {

		if(null == convertview){
			convertview = infater.inflate(R.layout.applistview, container, false);
		}

		ImageView appIcon = ViewHolderHelper.get(convertview, R.id.img);
		TextView pkgName = ViewHolderHelper.get(convertview, R.id.packet);
		TextView title = ViewHolderHelper.get(convertview, R.id.title);
		TextView versionName = ViewHolderHelper.get(convertview, R.id.versionName);
		CheckBox app_choose = ViewHolderHelper.get(convertview, R.id.app_choose);
		TextView version = ViewHolderHelper.get(convertview, R.id.version);
		TextView packetName = ViewHolderHelper.get(convertview, R.id.packetName);

		appIcon.setImageDrawable(mlistAppInfo.get(position).getAppIcon());     // 设置图像
		pkgName.setText(mlistAppInfo.get(position).getPkgName());              // 设置包名
		title.setText(mlistAppInfo.get(position).getAppLabel());               // 设置标题

		switch (choose){
			case APP_LIST_VIEW:
				app_choose.setVisibility(View.GONE);
				versionName.setText(mlistAppInfo.get(position).getVersion());   // 设置版本号
				break;
			case APP_CHOOSE:
				title.setTextColor(Color.WHITE);
				pkgName.setTextColor(Color.WHITE);
				version.setTextColor(Color.WHITE);
				packetName.setTextColor(Color.WHITE);
				versionName.setVisibility(View.GONE);
				version.setVisibility(View.GONE);
				app_choose.setVisibility(View.GONE);
				break;
			case MONKEY_APP_CHOOSE:
				title.setTextColor(Color.WHITE);
				pkgName.setTextColor(Color.WHITE);
				version.setTextColor(Color.WHITE);
				packetName.setTextColor(Color.WHITE);
				versionName.setVisibility(View.GONE);
				version.setVisibility(View.GONE);
				app_choose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(null != onCheckListener){
							onCheckListener.onCheck(position, isChecked);
							if(appCheck.containsKey(mlistAppInfo.get(position).getPkgName())){
								if(!isChecked){
									appCheck.remove(mlistAppInfo.get(position).getPkgName());
								}
							}else{
								if(isChecked){
									appCheck.put(mlistAppInfo.get(position).getPkgName(), mlistAppInfo.get(position).getPkgName());
								}
							}
						}
						if(onCheckListener != null){
							onCheckListener.onCheck(position, isChecked);
						}
					}
				});
				if(appCheck.containsKey(mlistAppInfo.get(position).getPkgName())){
					app_choose.setChecked(true);
				}else{
					app_choose.setChecked(false);   // 默认不勾选
				}
				break;
			default:break;
		}

		return convertview;
	}

}  