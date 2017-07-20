package com.meizu.widget.floatingwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.meizu.common.widget.LoadingView;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.db.bean.U2TaskBean;
import com.meizu.testdevVideo.db.util.U2TaskDBUtil;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.DensityUtil;
import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.task.performs.U2TaskListAdapter;
import com.meizu.testdevVideo.task.performs.U2TaskPreference;
import com.meizu.testdevVideo.task.performs.U2TaskUtils;
import com.meizu.testdevVideo.util.log.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 性能测试悬浮球定制页面
 * Created by maxueming on 2017/6/6.
 */
public class ViewManager implements View.OnClickListener{

    private static ViewManager manager;
    private Context context;
    private WindowManager windowManager = null;
    private FloatBall floatBall = null;
    private boolean isFloatBallHide = true;
    private boolean isFloatTaskListHide = true;
    private boolean isProgressTaskHide = true;
    private View taskListView = null;
    private View taskProgressView = null;
    private int screeenWidth, screeenHeight, edgeDistance;
    private WindowManager.LayoutParams floatBallParams = null;
    private WindowManager.LayoutParams floatTaskParams = null;
    private WindowManager.LayoutParams progressTaskParams = null;
    private ListView case_list_view;
    private TextView noTestcaseChoose, txtProgress;
    private RadioGroup type_choose;
    private ProgressBar barProgress;
    private LoadingView loading;
    private Button btnRunTestcase;
    private Button btnClearAllCase;
    private ImageView btnCloseMenu;
    private U2TaskListAdapter u2TaskListAdapter;
    private int taskType = -1;
    private static final int UPDATE_U2_TASK_LIST = 100;
    private static final int SHOW_U2_TASK_LIST = 200;
    private static final int SHOW_LOADING = 300;
    private static final int SET_RUNTEST_BUTTON = 400;

    private ViewManager(Context context) {
        init(context);
    }

    public static ViewManager getInstance(Context context){
        if (manager == null) {
            manager = new ViewManager(context);
        }
        return manager;
    }

    public FloatBall getFloatBallView(){
        return floatBall;
    }

    public View getTaskListView(){
        return taskListView;
    }

    public ListView getCaseListView(){
        return case_list_view;
    }

    /**
     * 设置进度
      * @param orignTxt 头
     * @param currentNumber 当前数字
     * @param totalNumber 全部
     */
    public void setProgressBar(String orignTxt, long currentNumber, long totalNumber){
        barProgress.setProgress((int)Math.floor((float)currentNumber/(float)totalNumber * 100));
        txtProgress.setText(orignTxt + "  "
                + "已完成: " + String.valueOf(Math.round((float)currentNumber/(float)totalNumber * 100)) + "%");
    }

    public void init(final Context context) {
        this.context = context;
        floatBall = new FloatBall(this.context);
        taskListView = LayoutInflater.from(this.context).inflate(R.layout.u2task_list_float, null);
        taskProgressView = LayoutInflater.from(this.context).inflate(R.layout.u2task_progress, null);
        type_choose = (RadioGroup) taskListView.findViewById(R.id.type_choose);
        btnCloseMenu = (ImageView) taskListView.findViewById(R.id.btn_close_menu);
        loading = (LoadingView) taskListView.findViewById(R.id.loading);
        case_list_view = (ListView) taskListView.findViewById(R.id.case_list_view);
        noTestcaseChoose = (TextView) taskListView.findViewById(R.id.no_testcase_choose);
        btnRunTestcase = (Button) taskListView.findViewById(R.id.btn_run_testcase);
        btnClearAllCase = (Button) taskListView.findViewById(R.id.clear_all_case);

        txtProgress = (TextView) taskProgressView.findViewById(R.id.txt_progress);
        barProgress = (ProgressBar) taskProgressView.findViewById(R.id.bar_progress);

        windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        screeenWidth = windowManager.getDefaultDisplay().getWidth();
        screeenHeight = windowManager.getDefaultDisplay().getHeight();
        edgeDistance = DensityUtil.dip2px(this.context, 15);
        floatBall.setOnClickListener(onClickListener);
        floatBall.setOnTouchListener(touchListener);
        type_choose.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.type_choose_1:
                        taskType = -1;
                        break;
                    case R.id.type_choose_2:
                        taskType = 2;
                        break;
                    case R.id.type_choose_3:
                        taskType = 3;
                        break;
                    case R.id.type_choose_4:
                        taskType = 1;
                        break;
                    case R.id.type_choose_5:
                        taskType = 4;
                        break;
                    default:
                        taskType = -1;
                        break;
                }
                updateU2TaskList(true);
            }
        });
        btnCloseMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideTaskList();
                hideProgressBar();
                showFloatBall();
            }
        });
        btnRunTestcase.setOnClickListener(this);
        btnClearAllCase.setOnClickListener(this);
        updateU2TaskList(false);
    }


    /**
     * 执行案例
     */
    private void runTestCase(int taskId){
        List<U2TaskBean> listU2Task = U2TaskDBUtil.getInstance().getListU2Task();
        String taskJson = "[";
        for(int i = 0; i < listU2Task.size(); i++){
            if(i == (listU2Task.size()-1)){
                taskJson = taskJson + "\"" + listU2Task.get(i).getCaseName() + "\"" + "]";
            }else{
                taskJson = taskJson + "\"" + listU2Task.get(i).getCaseName() + "\"" + ",";
            }
        }

        Intent intent = new Intent(Constants.U2TaskConstants.U2_TASK_BROADCAST_ACTION);
        intent.putExtra(Constants.U2TaskConstants.U2_TASK_TASKJSON, taskJson);
        intent.putExtra(Constants.U2TaskConstants.U2_TASK_TASKID, taskId);
        context.sendBroadcast(intent);
    }

    private void updateU2TaskList(boolean isUpdate){
        handler.sendEmptyMessage(SHOW_LOADING);
        if(!isUpdate){
            handler.sendEmptyMessage(SHOW_U2_TASK_LIST);
        }else{
            handler.sendEmptyMessage(UPDATE_U2_TASK_LIST);
        }
    }


    // 手势监听
    View.OnTouchListener touchListener = new View.OnTouchListener() {
        float startX;
        float startY;
        float tempX;
        float tempY;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    startY = event.getRawY();
                    tempX = event.getRawX();
                    tempY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getRawX() - startX;
                    float y = event.getRawY() - startY;
                    floatBallParams.x += x;
                    floatBallParams.y += y;
                    if(Math.abs(x) > 6 || Math.abs(y) > 6){
                        floatBall.setDragState(true);
                    }
                    windowManager.updateViewLayout(floatBall, floatBallParams);
                    startX = event.getRawX();
                    startY = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    float endX = event.getRawX();
                    float endY = event.getRawY();

                    if (Math.abs(endX - tempX) > 6 || Math.abs(endY - tempY) > 6) {
                        if (endX < screeenWidth / 2) {
                            endX = edgeDistance;
                        } else {
                            endX = screeenWidth - floatBall.width - edgeDistance;
                        }
                        floatBallParams.x = (int) endX;
                        windowManager.updateViewLayout(floatBall, floatBallParams);
                        floatBall.setDragState(false);
                        return true;
                    }
                    break;
            }
            return false;
        }
    };


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideFloatBall();
            hideProgressBar();
            // 恢复按钮文本
            btnRunTestcase.setText(context.getResources().getStringArray(R.array.u2task_get_id_status)[3]);
            showU2TaskList();
            // 半小时更新一次
            if(System.currentTimeMillis() - U2TaskPreference
                    .getLastUpdateTime(SuperTestApplication.getContext()) > 5 * 60 * 1000){
                U2TaskPreference.setLastUpdateTime(SuperTestApplication.getContext(),
                        System.currentTimeMillis());
                new SimpleTaskHelper(){
                    @Override
                    protected void doInBackground() {
                        handler.sendEmptyMessage(SHOW_LOADING);
                        Map<String, String> getListDataParams = new HashMap<String, String>();
                        getListDataParams.put("m_package_st", "");
                        getListDataParams.put("m_class_st", "");
                        try {
                            PostUploadHelper.getInstance().submitPostData(iPublicConstants
                                    .PPERFORMS_PULL_TESTCASE_URL, getListDataParams, new PostCallBack() {
                                @Override
                                public void resultCallBack(boolean isSuccess, int resultCode, String data) {
                                    if(isSuccess && null != data){
                                        JSONObject jsonObject = JSON.parseObject(data);
                                        int status = jsonObject.getInteger("status");
                                        if(0 == status){
                                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                                            if(jsonArray.size() > 0){
                                                U2TaskDBUtil.getInstance().clearNonExitsCase(jsonArray);
                                                Logger.d("存在案例数据更新，更新完毕！");
                                            }
                                            updateU2TaskList(true);

                                        }else{
                                            updateU2TaskList(true);
                                        }
                                    }else{
                                        updateU2TaskList(true);
                                    }
                                }
                            });
                        } catch (IOException e) {
                            updateU2TaskList(true);
                        }
                    }
                }.executeInSerial();
            }else{
                updateU2TaskList(true);
            }
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn_run_testcase:
                String btnStatus = btnRunTestcase.getText().toString();
                if(btnStatus.equals(context.getResources().getStringArray(R.array.u2task_get_id_status)[3])){
                    boolean isGet = U2TaskUtils.getU2TaskId();
                    if(isGet){
                        btnRunTestcase.setText(context.getResources().getStringArray(R.array.u2task_get_id_status)[2]);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                long lastTime = System.currentTimeMillis();
                                while (System.currentTimeMillis() - lastTime < 3 * 1000){
                                    if(!TextUtils.isEmpty(U2TaskPreference.getInstance(SuperTestApplication.getContext())
                                            .readStringData(Constants.U2TaskConstants.U2_TASK_TASKID))){
                                        runTestCase(Integer.parseInt(U2TaskPreference.getInstance(SuperTestApplication.getContext())
                                                .readStringData(Constants.U2TaskConstants.U2_TASK_TASKID)));
                                        U2TaskPreference.getInstance(SuperTestApplication.getContext())
                                                .writeStringData(Constants.U2TaskConstants.U2_TASK_TASKID, null);   // 清空存储的Id值
                                        return;
                                    }
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                handler.sendEmptyMessage(SET_RUNTEST_BUTTON);
                            }
                        }).start();
                    }else{
                        btnRunTestcase.setText(context.getResources().getStringArray(R.array.u2task_get_id_status)[1]);
                    }
                }else if(btnStatus.equals(context.getResources().getStringArray(R.array.u2task_get_id_status)[2])){
                    ToastHelper.addToast("别着急，慢慢来!", SuperTestApplication.getContext());
                }else if(btnStatus.equals(context.getResources().getStringArray(R.array.u2task_get_id_status)[0])){
                    runTestCase(0);
                }else if(btnStatus.equals(context.getResources().getStringArray(R.array.u2task_get_id_status)[1])){
                    runTestCase(0);
                }
                break;
            case R.id.clear_all_case:
                U2TaskDBUtil.getInstance().clearDataById("0");
                updateU2TaskList(true);
                break;
        }
    }


    // 处理刷新界面
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case UPDATE_U2_TASK_LIST:
                    U2TaskDBUtil.getInstance().queryAllCaseByTaskId("0", taskType);
                    loading.setVisibility(View.GONE);
                    u2TaskListAdapter.notifyDataSetChanged();
                    if(U2TaskDBUtil.getInstance().getListU2TaskBean().size() == 0){
                        noTestcaseChoose.setVisibility(View.VISIBLE);
                        btnRunTestcase.setEnabled(false);
                    }else{
                        noTestcaseChoose.setVisibility(View.GONE);
                        btnRunTestcase.setEnabled(true);
                    }
                    break;
                case SHOW_U2_TASK_LIST:
                    u2TaskListAdapter = new U2TaskListAdapter(context, U2TaskDBUtil.getInstance().queryAllCaseByTaskId("0", taskType));
                    loading.setVisibility(View.GONE);
                    case_list_view.setAdapter(u2TaskListAdapter);

                    u2TaskListAdapter.setOnItemDeleteListener(new U2TaskListAdapter.OnItemDeleteListener() {
                        @Override
                        public void onClick(int position) {
                            U2TaskDBUtil.getInstance().clearDataByIdAndCaseName(U2TaskDBUtil.getInstance()
                                            .getListU2TaskBean().get(position).getTaskId(),
                                    U2TaskDBUtil.getInstance().getListU2TaskBean().get(position).getCaseName());
                            updateU2TaskList(true);
                        }
                    });

                    if(U2TaskDBUtil.getInstance().getListU2TaskBean().size() == 0){
                        noTestcaseChoose.setVisibility(View.VISIBLE);
                        btnRunTestcase.setEnabled(false);
                    }else{
                        noTestcaseChoose.setVisibility(View.GONE);
                        btnRunTestcase.setEnabled(true);
                    }
                    break;
                case SHOW_LOADING:
                    noTestcaseChoose.setVisibility(View.GONE);
                    loading.setVisibility(View.VISIBLE);
                    break;

                case SET_RUNTEST_BUTTON:
                    btnRunTestcase.setText(context.getResources().getStringArray(R.array.u2task_get_id_status)[0]);
                    break;
            }
        }
    };


    /**
     * 展示悬浮球
     */
    public void showFloatBall(){
        if (floatBallParams == null) {
            floatBallParams = new WindowManager.LayoutParams();
            floatBallParams.x = Math.round(screeenWidth - floatBall.width - edgeDistance);
            floatBallParams.y = Math.round(screeenHeight - floatBall.height
                    - edgeDistance - DensityUtil.getStatusBarHeight(context));
            floatBallParams.width = floatBall.width;
            floatBallParams.height = floatBall.height;
//            floatBallParams.windowAnimations = android.R.style.Animation_Translucent;
            floatBallParams.gravity = Gravity.TOP | Gravity.START;
            floatBallParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            floatBallParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            floatBallParams.format = PixelFormat.RGBA_8888;
        }

        if(isFloatBallHide){
            windowManager.addView(floatBall, floatBallParams);
            isFloatBallHide = false;
        }

    }

    /**
     * 展示U2任务列表
     */
    public void showU2TaskList(){
        if (floatTaskParams == null) {
            int width = DensityUtil.getViewWidthHeight(taskListView, false);
            int height = DensityUtil.getViewWidthHeight(taskListView, true);
            floatTaskParams = new WindowManager.LayoutParams();
            floatTaskParams.x = (screeenWidth - width)/2;
            floatTaskParams.y = (screeenHeight - height)/2;
//            floatTaskParams.windowAnimations = android.R.style.Animation_Translucent;
            floatTaskParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            floatTaskParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            floatTaskParams.gravity = Gravity.TOP | Gravity.START;
            floatTaskParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            floatTaskParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            floatTaskParams.format = PixelFormat.RGBA_8888;
        }
        if(isFloatTaskListHide){
            windowManager.addView(taskListView, floatTaskParams);
            isFloatTaskListHide = false;
        }

    }

    public void showTaskProgressBar(){
        if (progressTaskParams == null) {
            int width = DensityUtil.getViewWidthHeight(taskProgressView, false);
            progressTaskParams = new WindowManager.LayoutParams();
            progressTaskParams.x = (screeenWidth - width)/2;
            progressTaskParams.y = screeenHeight * 4 / 5;
            progressTaskParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            progressTaskParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            progressTaskParams.gravity = Gravity.TOP | Gravity.START;
            progressTaskParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            progressTaskParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            progressTaskParams.format = PixelFormat.RGBA_8888;
        }
        if(isProgressTaskHide){
            windowManager.addView(taskProgressView, progressTaskParams);
            isProgressTaskHide = false;
        }
    }

    public void hideFloatBall() {
        if (floatBall != null && !isFloatBallHide) {
            windowManager.removeView(floatBall);
            isFloatBallHide = true;
        }
    }

    public void hideProgressBar() {
        if (taskProgressView != null && !isProgressTaskHide) {
            windowManager.removeView(taskProgressView);
            isProgressTaskHide = true;
        }
    }


    public void hideTaskList() {
        if (taskListView != null && !isFloatTaskListHide) {
            windowManager.removeView(taskListView);
            isFloatTaskListHide = true;
        }
    }

    public void cancelCallBackHandler(){
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
        }
    }
}
