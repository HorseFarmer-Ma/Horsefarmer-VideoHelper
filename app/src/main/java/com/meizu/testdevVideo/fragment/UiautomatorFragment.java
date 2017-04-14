package com.meizu.testdevVideo.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

import com.meizu.widget.floatingbutton.FabTagLayout;
import com.meizu.widget.floatingbutton.FloatingActionButtonPlus;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.activity.CaseDownloadActivity;
import com.meizu.testdevVideo.adapter.UiautomatorAdapter;
import com.meizu.testdevVideo.service.UiautomatorService;
import com.meizu.testdevVideo.library.ToastHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UiautomatorFragment extends Fragment {

    RecyclerView mRecyclerView;
    private FloatingActionButtonPlus mActionButtonPlus;
    private Button cancel;
    private Button delete;
    private Button choose_all;

    private UiautomatorAdapter myadapter;
    private List<Map<String, Object>> mList;        // 案例列表数据
    public View mView;
    PopupWindow window;   // 弹出框

    public static UiautomatorFragment newInstance(String param1, String param2) {
        UiautomatorFragment fragment = new UiautomatorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_uiautomator, container, false);
        mView = view;
        init(view);
        events();
        return view;
    }



    // 设计点击事件
    private void events() {
        // 列表点击
        myadapter.setOnItemClickLitener(new UiautomatorAdapter.OnItemClickLitener() {
            String fileName;   // 获取当前文件名
            @Override
            public void onItemClick(View view, final int position) {
                fileName = mList.get(position).get("caseName").toString();

                // 判断当前手机是否下载了该条案例.Yes
                if(new File(Environment.getExternalStorageDirectory()
                        + "/SuperTest/JarDownload/" + fileName + ".jar").exists()){

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("确认执行脚本" + fileName + ".jar : " +
                            mList.get(position).get("caseDetail").toString());
                    builder.setTitle("执行脚本");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle mBundle = new Bundle();
                            mBundle.putString("fileName", fileName);
                            Intent mIntent = new Intent(getActivity(), UiautomatorService.class);
                            mIntent.putExtras(mBundle);
                            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getActivity().stopService(mIntent);   // 开始服务
                            getActivity().startService(mIntent);   // 开始服务
                        }
                    });

                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();

                } else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("找不到该条脚本，有可能误删，是否重新下载？");
                    builder.setTitle("Waring!!!");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            mList.remove(position);   // 移除当前案例
                            Intent i = new Intent(getActivity(), CaseDownloadActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("case_Detail", fileName);
                            i.putExtras(bundle);
                            startActivityForResult(i, 0);
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                ToastHelper.addToast("未定义长按功能", getActivity());
            }
        });

        // 选中监听
        myadapter.setOnCheckedChangeListener(new UiautomatorAdapter.OnCheckBoxChangeListener() {
            @Override
            public void onCheckBoxChange(View view, int position, boolean isChecked) {
                ((Map) mList.get(position)).put("checkBox", isChecked);//修改值
                if (isChoose()) {
                    mRecyclerView.setPaddingRelative(0, 0, 0, 170);
                    window.showAtLocation(mView.findViewById(R.id.list),
                            Gravity.BOTTOM, 0, 0);   // 显示底栏
                    mActionButtonPlus.hideFab();   // 隐藏悬浮按钮
                }
                if (!isChoose()) {
                    mRecyclerView.setPaddingRelative(0, 0, 0, 0);
                    mActionButtonPlus.showFab();  // 展示悬浮按钮
                    window.dismiss();
                }
            }
        });

        // 悬浮按钮监听
        mActionButtonPlus.setOnItemClickListener(new FloatingActionButtonPlus.OnItemClickListener() {
            @Override
            public void onItemClick(FabTagLayout tagView, int position) {
                switch (position) {
                    case 0:     // 下载案例,跳转Acitivity Dialog
                        Intent i = new Intent(getActivity(), CaseDownloadActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("case_Detail", "");
                        i.putExtras(bundle);
                        startActivityForResult(i, 0);
                        break;
                    case 1:
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("使用说明");
                        builder.setMessage("1. 提供下载脚本功能，脚本存放地址：" +
                                "\\\\ats.meizu.com\\user-resources\\SuperTest\\Case\\" +
                                "\n\n2. 短按列表执行当前脚本\n\n" +
                                "3. 点击右侧小" + "圆点选中删除列表\n\n" +
                                "Waring：新功能板块，简单实现跑脚本功能，要求脚本用uiautomator1.0" +
                                "进行编写，包名为:com.meizu.test，脚本编写类名为:Sanity，后期完善优化。");

                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();   // 消除弹框
                            }
                        });
                        builder.create().show();

                        break;
                    case 3:
                        break;
                    default:
                        break;
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 取出字符串
        if (data != null){
            Bundle bundle = data.getExtras();
            String caseName = bundle.getString("caseName");
            String caseDetail = bundle.getString("caseDetail");
            addData(caseName, caseDetail, false);
            myadapter.notifyItemInserted(mList.size() - 1);
        }
        saveArray();   // 存储列表
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 初始化数据
    private void init(View view) {
        /**------------------------------ ListView设置 --------------------------------*/
        mList = new ArrayList<Map<String, Object>>();
        loadArray();   // 加载记录的列表数据
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());    // 设置默认添加和删除的动画效果
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);   // 设置垂直排布
        mRecyclerView.setLayoutManager(layoutManager);   // 载入布局设置
        myadapter = new UiautomatorAdapter(getActivity().getApplicationContext(), mList);
        mRecyclerView.setAdapter(myadapter);
        showPopwindow();    // 初始化底栏

        /**---------------------------- 浮动按钮 ---------------------------*/
        mActionButtonPlus = (FloatingActionButtonPlus) view.findViewById(R.id.FabPlus);

    }


    // 生成数组列表
    private void addData(String caseName, String caseDetail, boolean checkBox) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("caseName", caseName);
        map.put("caseDetail", caseDetail);
        map.put("checkBox", checkBox);
        mList.add(map);
    }

    // 生成数组列表
    private void addDataByLocation(int location, String caseName, String caseDetail, boolean checkBox) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("caseName", caseName);
        map.put("caseDetail", caseDetail);
        map.put("checkBox", checkBox);
        mList.add(location, map);
    }

    // 判断是否选中
    private boolean isChoose(){
        for (int i = 0; i < mList.size(); i++){
            if((Boolean)mList.get(i).get("checkBox")){
                return true;
            }
        }
        return false;
    }


    // 取消选中
    private void cancelAllCheckBox(){
        for (int i = 0; i < mList.size(); i++){
            if((Boolean)mList.get(i).get("checkBox")){
                mList.get(i).put("checkBox", false);
            }
        }
        myadapter.notifyDataSetChanged();  // 监听列表变化，不知道加进去为什么不可以
    }

    // 选中全部
    private void chooseAllCheckBox(){
        for (int i = 0; i < mList.size(); i++){
            if(!(Boolean)mList.get(i).get("checkBox")){
                mList.get(i).put("checkBox", true);
            }
        }
        myadapter.notifyDataSetChanged();  // 监听列表变化
    }

    // 取消选中全部
    private void cancelChooseAllCheckBox(){
        for (int i = 0; i < mList.size(); i++){
            if((Boolean)mList.get(i).get("checkBox")){
                mList.get(i).put("checkBox", false);
            }
        }
        myadapter.notifyDataSetChanged();  // 监听列表变化
    }

    // 删除选中的视频
    private void deleteBeChoose(){
        Log.d("列表大小", String.valueOf(mList.size()));
        for (int i = 0; i < mList.size(); ){
            Log.d("选中状态", String.valueOf(i) + mList.get(i).get("checkBox").toString());
            if((Boolean)mList.get(i).get("checkBox")){
                Log.d("选中删除", String.valueOf(i));
                mList.remove(i);
                myadapter.notifyItemRemoved(i);  // 监听列表变化
            }else{
                i++;
            }
        }
        saveArray();  // 删除完毕，保存列表
    }



    // 显示popupWindow
    private void showPopwindow() {
        // 利用layoutInflater获得View
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_uiautomator_toolbar, null);

        // 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()

        window = new PopupWindow(view,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true

        window.setBackgroundDrawable(new BitmapDrawable());

        // 设置popWindow的显示和消失动画
        window.setAnimationStyle(R.style.mypopwindow_anim_style);

        // 监听按钮
        cancel = (Button) view.findViewById(R.id.cancel);
        delete = (Button) view.findViewById(R.id.delete);
        choose_all = (Button) view.findViewById(R.id.choose_all);

        // 取消按钮
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击取消按钮，checkbox取消选中，弹出框消失
                mRecyclerView.setPaddingRelative(0, 0, 0, 0);
                window.dismiss();
                mActionButtonPlus.showFab();  // 展示悬浮按钮
                cancelAllCheckBox();
            }
        });

        // 删除按钮
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("确定删除选中的列表？");
                builder.setTitle("删除脚本");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBeChoose();   // 删除选中的脚本
                        mRecyclerView.setPaddingRelative(0, 0, 0, 0);
                        window.dismiss();  // 底栏消失
                        mActionButtonPlus.showFab();  // 展示悬浮按钮
                        dialog.dismiss();   // 对话框消失
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        // 全选按钮
        choose_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(choose_all.getText().equals("全选")){
                    choose_all.setText("全不选");
                    chooseAllCheckBox();   // 选中全部
                }else{
                    choose_all.setText("全选");
                    cancelChooseAllCheckBox();   // 取消选中全部
                }
            }
        });
    }

    // 存储列表数据
    private boolean saveArray() {
        // 实例化SharedPreferences对象
        SharedPreferences mySharedPreferences= getActivity().getSharedPreferences("save_uiautomator_list",
                Activity.MODE_PRIVATE);
        // 实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putInt("mListSize", mList.size()); /*sKey is an array*/

        for(int i = 0; i < mList.size(); i++) {
            editor.remove("caseName_" + i);
            editor.putString("caseName_" + i, mList.get(i).get("caseName").toString());
            editor.remove("caseDetail_" + i);
            editor.putString("caseDetail_" + i, mList.get(i).get("caseDetail").toString());
            editor.remove("checkBox_" + i);
            editor.putBoolean("checkBox_" + i, (Boolean)mList.get(i).get("checkBox"));
        }
        // 提交当前数据
        return editor.commit();
    }

    // 加载表中数据
    private void loadArray() {
        SharedPreferences mSharedPreference1 = getActivity().getSharedPreferences("save_uiautomator_list",
                Activity.MODE_PRIVATE);
        int size = mSharedPreference1.getInt("mListSize", 0);

        for(int i = 0; i < size; i++){
            addData(mSharedPreference1.getString("caseName_" + i, null),
                    mSharedPreference1.getString("caseDetail_" + i, null),
                    mSharedPreference1.getBoolean("checkBox_" + i, false));
        }
    }
}
