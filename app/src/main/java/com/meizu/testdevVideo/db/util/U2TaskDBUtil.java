package com.meizu.testdevVideo.db.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alibaba.fastjson.JSONArray;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.db.bean.U2TaskBean;
import com.meizu.testdevVideo.library.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * U2Task
 * Created by maxueming on 2017/6/9.
 */
public class U2TaskDBUtil {
    private static U2TaskDBUtil u2TaskDBUtil;
    public SQLiteDatabase dataBase;
    private DatabaseHelper mDatabaseHelper;

    public U2TaskDBUtil(){
        mDatabaseHelper = new DatabaseHelper(SuperTestApplication.getContext());
        dataBase = mDatabaseHelper.getReadableDatabase();
    }

    public synchronized static U2TaskDBUtil getInstance(){
        if(null == u2TaskDBUtil){
            u2TaskDBUtil = new U2TaskDBUtil();
        }
        return u2TaskDBUtil;
    }

    // 关闭数据库
    public void closeU2TaskDB(){
        if(null != dataBase){
            dataBase.close();
            u2TaskDBUtil = null;
        }
    }

    public List<U2TaskBean> getListU2Task(){
        return listU2Task;
    }

    /**
     * 根据Id查询数据库所有任务信息
     * @param taskId 任务主ID
     * @param type 全部（-1），启动（1），帧率（2），内存（3），纯净（4）
     * @return 返回list列表，存任务列表
     */
    private List<U2TaskBean> listU2Task = new ArrayList<U2TaskBean>();
    public List<U2TaskBean> queryAllCaseByTaskId(String taskId, int type){
        listU2Task.clear();
        Cursor cursor = null;
        String command;
        try {
            if(-1 == type){
                command = "SELECT * FROM " + Constants.U2TaskDB.TABLE_NAME
                        + " WHERE " + Constants.U2TaskDB.TASK_ID_STR + " = " + taskId;
            }else{
                command = "SELECT * FROM " + Constants.U2TaskDB.TABLE_NAME
                        + " WHERE " + Constants.U2TaskDB.TASK_ID_STR + " = " + taskId
                        + " AND " + Constants.U2TaskDB.PERFORMS_TYPE_INT + " = " + type;
            }
            cursor = dataBase.rawQuery(command, null);
            if(null != cursor && cursor.getCount() != 0){
                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++){
                    U2TaskBean u2TaskBean = new U2TaskBean();
                    u2TaskBean.setTaskId(taskId);
                    u2TaskBean.setCaseName(cursor.getString(cursor.getColumnIndex(Constants.U2TaskDB.CASE_NAME_STR)));
                    u2TaskBean.setPerformsType(cursor.getString(cursor.getColumnIndex(Constants.U2TaskDB.PERFORMS_TYPE_INT)));
                    u2TaskBean.setResult(cursor.getString(cursor.getColumnIndex(Constants.U2TaskDB.RESULT_BOOLEAN)));
                    u2TaskBean.setResultFile(cursor.getString(cursor.getColumnIndex(Constants.U2TaskDB.RESULT_FILE_STR)));
                    u2TaskBean.setStatus(cursor.getString(cursor.getColumnIndex(Constants.U2TaskDB.STATUS_INT)));
                    u2TaskBean.setException(cursor.getString(cursor.getColumnIndex(Constants.U2TaskDB.EXCEPTION_STR)));
                    u2TaskBean.setCaseStep(cursor.getString(cursor.getColumnIndex(Constants.U2TaskDB.CASE_STEP_STR)));
                    listU2Task.add(u2TaskBean);
                    cursor.moveToNext();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor != null) {
                cursor.close();
            }
        }

        return listU2Task;
    }


    /**
     * 清除不存在的案例
     * @param taskJson 案例json串
     */
    public void clearNonExitsCase(JSONArray taskJson){
        Cursor cursor = null;
        String caseName;
        int a = 0;
        try {
            cursor = dataBase.rawQuery("SELECT * FROM " + Constants.U2TaskDB.TABLE_NAME
                    + " WHERE " + Constants.U2TaskDB.TASK_ID_STR + " = 0", null);
            if(null != cursor && cursor.getCount() != 0){
                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++){
                    caseName = cursor.getString(cursor.getColumnIndex(Constants.U2TaskDB.CASE_NAME_STR));
                    a = 0;
                    for(int j = 0; j < taskJson.size(); j++){
                        if(caseName.equals(taskJson.getJSONObject(j).getString("m_name"))){
                            break;
                        }
                        ++a;
                    }
                    // 清除不存在的案例
                    if(a == taskJson.size()){
                        clearDataByIdAndCaseName("0", caseName);
                    }
                    cursor.moveToNext();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 判断案例是否已添加
     * @param taskId 任务ID
     * @param caseName 案例名
     * @return 结果
     */
    public boolean isCaseNameExits(String taskId, String caseName){
        Cursor cursor = null;
        try {
            cursor = dataBase.rawQuery("SELECT * FROM " + Constants.U2TaskDB.TABLE_NAME
                    + " WHERE " + Constants.U2TaskDB.TASK_ID_STR + " = " + taskId
                    + " AND " + Constants.U2TaskDB.CASE_NAME_STR + " = " + "\"" + caseName + "\"", null);
            if(null != cursor && cursor.getCount() != 0){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public boolean isExistPureCase(String id){
        queryAllCaseByTaskId(id, 4);
        return !(0 == listU2Task.size());

//        for(int i = 0; i < listU2Task.size(); i++){
//            if(listU2Task.get(i).getCaseName().contains("purebackstage")){
//                return true;
//            }
//        }
//        return false;
    }

    // 返回数组
    public List<U2TaskBean> getListU2TaskBean(){
        return listU2Task;
    }

    /**
     * 新增数据到数据库，当数据存在的时候，仅做修改
     * @param taskId 任务ID
     * @param performsType 测试类型 启动（1），帧率（2），内存（3），纯净（4）
     * @param caseName 案例名
     * @param status 状态 0：未执行 1：执行中 2：执行完毕
     */
    public void saveData(String taskId, int performsType, String caseStep, String caseName, int status,
                         boolean result, String resultFile, String exception){
        ContentValues values = new ContentValues();
        values.put(Constants.U2TaskDB.TASK_ID_STR, taskId);
        values.put(Constants.U2TaskDB.PERFORMS_TYPE_INT, performsType);
        values.put(Constants.U2TaskDB.CASE_STEP_STR, caseStep);
        values.put(Constants.U2TaskDB.CASE_NAME_STR, caseName);
        values.put(Constants.U2TaskDB.STATUS_INT, status);
        values.put(Constants.U2TaskDB.RESULT_BOOLEAN, result);
        values.put(Constants.U2TaskDB.RESULT_FILE_STR, resultFile);
        values.put(Constants.U2TaskDB.EXCEPTION_STR, exception);
        values.put(Constants.U2TaskDB.EXCEPTION_STR, exception);
        int resultCode = dataBase.update(Constants.U2TaskDB.TABLE_NAME, values,
                " " + Constants.U2TaskDB.TASK_ID_STR + " = ? " +
                "AND " + Constants.U2TaskDB.CASE_NAME_STR +
                " = ? ", new String[]{taskId, caseName});
        if (resultCode == 0){
            dataBase.insert(Constants.U2TaskDB.TABLE_NAME, "caseName", values);
        }
    }

    /**
     * 根据taskId和caseName唯一确定清除案例
     * @param taskId 主任务ID
     * @param caseName 案例名
     * @return 删除结果
     */
    public boolean clearDataByIdAndCaseName(String taskId, String caseName){
        return dataBase.delete(Constants.U2TaskDB.TABLE_NAME,
                " " + Constants.U2TaskDB.TASK_ID_STR + " = ? " +
                        "AND " + Constants.U2TaskDB.CASE_NAME_STR +
                        " = ? ", new String[]{taskId, caseName}) != 0;
    }

    /**
     * 根据taskId和caseName唯一确定清除案例
     * @param taskId 主任务ID
     * @return 删除结果
     */
    public boolean clearDataById(String taskId){
        return dataBase.delete(Constants.U2TaskDB.TABLE_NAME,
                " " + Constants.U2TaskDB.TASK_ID_STR + " = ? ", new String[]{taskId}) != 0;
    }




}
