package com.meizu.testdevVideo.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.meizu.common.app.LoadingDialog;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.adapter.SchemaAdapter;
import com.meizu.testdevVideo.adapter.data.listview.SchemaInfo;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.download.FileDownloadCallBack;
import com.meizu.testdevVideo.util.download.FileDownloadHelper;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;


/**
 * Scheme测试
 */
public class SchemaTestFragment extends Fragment {

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


    public SchemaTestFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if(null == view){
            view = inflater.inflate(R.layout.fragment_scheme_test, container, false);
            mActivity = getActivity();
            listView = (ListView) view.findViewById(R.id.list_schema);
            downloadUrl = getString(R.string.schemaXml) + getFileName();
            dialog = new LoadingDialog(mActivity);
            dialog.setMessage("正在加载");
            dialog.setCancelable(false);
            if(!new File(DEST_DOWNLOAD_PATH + fileName).exists()){
                updateFile();
            }else{
                handler.sendEmptyMessage(UPDATE_LIST);
            }
        }

        return view;
    }

    /**
     * 获取下载文件的名字
     */
    private String getFileName(){
        String packageName = BaseData.getInstance(SuperTestApplication.getContext())
                .readStringData(SettingPreferenceKey.MONKEY_PACKAGE);
        if(packageName.equals(iPublicConstants.PACKET_VIDEO)){
            fileName = "VideoScheme.xls";
        }else if(packageName.equals(iPublicConstants.PACKET_MUSIC)){
            fileName = "MusicScheme.xls";
        }else if(packageName.equals(iPublicConstants.PACKET_BROWSER)){
            fileName = "BrowserScheme.xls";
        }else if(packageName.equals(iPublicConstants.PACKET_THEME)){
            fileName = "CustomizecenterScheme.xls";
        }else if(packageName.equals(iPublicConstants.PACKET_READER)){
            fileName = "ReaderScheme.xls";
        }else if(packageName.equals(iPublicConstants.PACKET_EBOOK)){
            fileName = "EbookScheme.xls";
        }else{
            fileName = "OtherAppScheme.xls";
        }
        return fileName;
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case UPDATE_LIST:
                    dialog.show();
                    getData();     // 获取数据
                    break;
                case UPDATE_FAIL:
                    ToastHelper.addToast("下载文件失败\n" + msg.getData().getString("error"), SuperTestApplication.getContext());
                    dialog.dismiss();
                    break;
                case UPDATE_VIEW:
                    dialog.dismiss();
                    if(null == schemaAdapter){
                        schemaAdapter = new SchemaAdapter(mActivity, schemaInfos);
                        listView.setAdapter(schemaAdapter);
                    }else{
                        schemaAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 获取数据线程
     */
    private void getData(){
        new SimpleTaskHelper(){

            @Override
            protected void doInBackground() {
                getXlsData(DEST_DOWNLOAD_PATH + fileName);
                handler.sendEmptyMessage(UPDATE_VIEW);
            }
        }.executeInSerial();
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 0, 0, "更新列表").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case 0:
                updateFile();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }




    /**
     * 下载文件并更新列表
     */
    private void updateFile(){
        dialog.show();
        if(!isDownloading){
            File file = new File(DEST_DOWNLOAD_PATH);
            if(file.exists()){
                if(file.isDirectory()){
                    PublicMethod.deleteDirectory(DEST_DOWNLOAD_PATH);
                }else{
                    file.delete();
                }
            }

            file.mkdirs();    // 新建文件夹

            if(null != fileDownloadHelper){
                fileDownloadHelper.interrupt();
            }

            fileDownloadHelper = new FileDownloadHelper(downloadUrl, THREAD_NUM, DEST_DOWNLOAD_PATH + fileName, new FileDownloadCallBack() {
                @Override
                public void start() {
                    isDownloading = true;
                    Logger.d("下载任务开始");
                }

                @Override
                public void finish() {
                    isDownloading = false;
                    Logger.d("下载任务结束");
                    handler.sendEmptyMessage(UPDATE_LIST);
                }

                @Override
                public void error(String message) {
                    isDownloading = false;
                    Message errorMessage = new Message();
                    errorMessage.what = UPDATE_FAIL;
                    Bundle bundle = new Bundle();
                    bundle.putString("error", message);
                    errorMessage.setData(bundle);
                    handler.sendMessage(errorMessage);
                }
            });
            fileDownloadHelper.start();
        }else{
            ToastHelper.addToast("下载XML解析文件中，别着急", SuperTestApplication.getContext());
        }
    }


    /**
     * 获取表格数据
     * @param xlsPath xsl路径
     */
    private void getXlsData(String xlsPath){

        if(null != schemaInfos){
            schemaInfos.clear();
        }

        Logger.d(xlsPath);

        try {
            Workbook workbook = Workbook.getWorkbook(new File(xlsPath));
            Logger.d("一共有的表格数据为：" + workbook.getNumberOfSheets());
            Sheet sheet = workbook.getSheet(0);         // 默认获取第一张表格数据
            Logger.d("表格名称为：" + sheet.getName());
            int sheetRows = sheet.getRows();
            Logger.d("表格行数为：" + sheetRows);
            int sheetColumns = sheet.getColumns();
            Logger.d("表格列数为：" + sheetColumns);

            for(int i = 1; i < sheetRows; i++){
                SchemaInfo schemaInfo = new SchemaInfo();
                schemaInfo.setJumpType(Integer.parseInt(sheet.getCell(0, i).getContents()));
                schemaInfo.setDescription(sheet.getCell(1, i).getContents());
                schemaInfo.setAddress(sheet.getCell(2, i).getContents());
                schemaInfos.add(schemaInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Logger.d("解析xsl失败" + e.toString());
        }

    }

    @Override
    public void onDestroy() {
        if(null != handler){
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
