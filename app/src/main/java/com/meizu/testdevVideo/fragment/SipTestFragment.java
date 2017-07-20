package com.meizu.testdevVideo.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.meizu.common.app.LoadingDialog;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.SchemaAdapter;
import com.meizu.testdevVideo.adapter.data.listview.SchemaInfo;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.util.download.FileDownloadHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Scheme测试
 */
public class SipTestFragment extends Fragment {

    private View view;
    private static final int UPDATE_LIST = 1000;
    private static final int UPDATE_FAIL = 1001;
    private static final int UPDATE_VIEW = 1002;
    private String downloadUrl;
    private static final String DEST_DOWNLOAD_PATH =
            iPublicConstants.LOCAL_MEMORY + "SuperTest/SchemaResource/";
    private String fileName;
    private static final int THREAD_NUM = 2;
    private boolean isDownloading = false;
    private List<SchemaInfo> schemaInfos = new ArrayList<SchemaInfo>();
    private SchemaAdapter schemaAdapter;
    private ListView listView;
    private FileDownloadHelper fileDownloadHelper;
    private Activity mActivity;
    private LoadingDialog dialog;
    private TextView textView;



    public SipTestFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if(null == view){
            view = inflater.inflate(R.layout.fragment_ota_test, container, false);
            mActivity = getActivity();
            listView = (ListView) view.findViewById(R.id.list_schema);
        }

        return view;
    }

}
