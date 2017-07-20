package com.meizu.testdevVideo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.constant.FragmentUtils;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.ViewHolderHelper;

import java.util.List;
import java.util.Map;

/**
 * Created by maxueming on 2016/12/28.
 */
public class UpdateAppListAdapter extends BaseAdapter {
    private Context context;
    private List<Map<String, Object>> fileList;
    private LayoutInflater inflater;
    private String type;
    private DownloadClickListener downloadClickListener;

    public UpdateAppListAdapter(Context context, List<Map<String, Object>> fileList, String type){
        this.context = context;
        this.fileList = fileList;
        this.type = type;
        inflater = LayoutInflater.from(context);
    }

    public interface DownloadClickListener{
        void onItemClick(int position);
    }

    public void setOnDownloadClickListener(DownloadClickListener downloadClickListener){
        this.downloadClickListener = downloadClickListener;
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(null == convertView){
            convertView = inflater.inflate(R.layout.update_app_list_content, parent, false);
        }

        TextView appName = ViewHolderHelper.get(convertView, R.id.update_app_name);
        Button ftpDownload = ViewHolderHelper.get(convertView, R.id.ftp_download);
        appName.setText(fileList.get(position).get(iPublicConstants.FILENAME).toString());
        if(null != type && type.equals(FragmentUtils.CHOOSE_APP_FRAGMENT)){
            ftpDownload.setText(SuperTestApplication.getContext().getString(R.string.add));
        }
        ftpDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadClickListener.onItemClick(position);
            }
        });

        return convertView;
    }
}
