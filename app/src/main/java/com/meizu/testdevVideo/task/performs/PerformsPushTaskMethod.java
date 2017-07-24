package com.meizu.testdevVideo.task.performs;

import com.meizu.testdevVideo.interports.iPerformsKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 返回推送Json数据中的字段
 * Created by maxueming on 2016/12/6.
 */
public class PerformsPushTaskMethod {

    // 返回测试主Id
    public static String getTaskIdFromJson(JSONObject taskJson){
        return taskJson.optString(iPerformsKey.taskId);
    }

    // 返回测试应用
    public static String getAppType(JSONObject taskJson){
        return taskJson.optString(iPerformsKey.appType);
    }

    // 返回测试类型数目
    public static int getTypeNumber(JSONObject taskJson){
        return taskJson.optInt("typeNumber");
    }

    // 返回启动时间数目
    public static int getStarttimeNumber(JSONObject taskJson) throws JSONException {
        JSONObject startTimeJson = getJsonFromData(taskJson, 0);
        return startTimeJson.optInt("testNumber");
    }

    // 返回启动时间案例
    public static String[] getStarttimeCase(JSONObject taskJson) throws JSONException {
        JSONObject startTimeJson = getJsonFromData(taskJson, 0);
        return startTimeJson.optString("testPackageName").split(",");
    }

    // 返回帧率测试数目
    public static int getFramerateNumber(JSONObject taskJson) throws JSONException {
        JSONObject framerateJson = getJsonFromData(taskJson, 1);
        return framerateJson.optInt("testNumber");
    }

    // 返回帧率测试案例
    public static String[] getFramerateCase(JSONObject taskJson) throws JSONException {
        JSONObject framerateJson = getJsonFromData(taskJson, 1);
        return framerateJson.optString("testPackageName").split(",");
    }

    // 返回内存测试数目
    public static int getMemoryNumber(JSONObject taskJson) throws JSONException {
        JSONObject memoryJson = getJsonFromData(taskJson, 2);
        return memoryJson.optInt("testNumber");
    }

    // 返回内存测试案例
    public static String[] getMemoryCase(JSONObject taskJson) throws JSONException {
        JSONObject memoryJson = getJsonFromData(taskJson, 2);
        return memoryJson.optString("testPackageName").split(",");
    }

    // 返回纯净后台测试数目
    public static int getPurebackstageNumber(JSONObject taskJson) throws JSONException {
        JSONObject purebackstageJson = getJsonFromData(taskJson, 3);
        return purebackstageJson.optInt("testNumber");
    }

    // 返回纯净后台测试案例
    public static String[] getPurebackstageCase(JSONObject taskJson) throws JSONException {
        JSONObject purebackstageJson = getJsonFromData(taskJson, 3);
        return purebackstageJson.optString("testPackageName").split(",");
    }

    // 返回一列的data中Json数据
    private static JSONObject getJsonFromData(JSONObject taskJson, int i) throws JSONException {
        JSONArray data = taskJson.optJSONArray("data");
        return (JSONObject) data.get(i);
    }
}