package com.meizu.testdevVideo.task.performs;

import android.support.v4.util.ArrayMap;

import com.alibaba.fastjson.JSON;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.db.bean.U2TaskBean;
import com.meizu.testdevVideo.db.util.U2TaskDBUtil;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.push.android.MPush;
import com.meizu.testdevVideo.push.android.bean.MPushMonkeyBean;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.wifi.WifiUtil;

import java.util.List;
import java.util.Map;

/**
 * U2Task公共方法类
 * Created by maxueming on 2017/6/14.
 */
public class U2TaskUtils {

    // 获取测试类型
    public static String getTestType(String className){
        if(className.contains(iPerformsKey.starttime)){
            return iPerformsKey.starttime;
        }else if(className.contains(iPerformsKey.framerate)){
            return iPerformsKey.framerate;
        }else if(className.contains(iPerformsKey.memory)){
            return iPerformsKey.memory;
        }else{
            return iPerformsKey.purebackstage;
        }
    }

    // 获取测试类型，仅支持启动时间、FPS、内存
    public static String getTestTypeResultPath(String className){
        if(className.contains(iPerformsKey.starttime)){
            return iPublicConstants.PERFORMS_TIME_RESULT;
        }else if(className.contains(iPerformsKey.framerate)){
            return iPublicConstants.PERFORMS_FPS_RESULT;
        }else if(className.contains(iPerformsKey.memory)){
            return iPublicConstants.PERFORMS_MEMORY_RESULT;
        }
        return null;
    }

    /**
     * 清除测试数据
     */
    public static void clearTestData(){
        PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_FPS_RESULT);
        PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_MEMORY_RESULT);
        PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_TIME_RESULT);
        PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_PURE_BACKGROUND_RESULT);
    }

    private static String getTestType(){
        List<U2TaskBean> listU2Task = U2TaskDBUtil.getInstance().getListU2Task();
        String testType = "";
        for(int i = 0; i < listU2Task.size(); i++){
            if(listU2Task.get(i).getCaseName().contains("starttime") && !testType.contains("starttime")){
                testType = testType + "starttime" + ",";
            }
            if(listU2Task.get(i).getCaseName().contains("framerate") && !testType.contains("framerate")){
                testType = testType + "framerate" + ",";
            }
            if(listU2Task.get(i).getCaseName().contains("memory") && !testType.contains("memory")){
                testType = testType + "memory" + ",";
            }
            if(listU2Task.get(i).getCaseName().contains("purebackstage") && !testType.contains("purebackstage")){
                testType = testType + "purebackstage" + ",";
            }
        }
        return testType.substring(0, testType.length() - 1);
    }

    /**
     * 获取U2TaskId
     */
    public static boolean getU2TaskId(){
        if(WifiUtil.isWifiConnected(SuperTestApplication.getContext())){
            MPushMonkeyBean mPushMonkeyBean = new MPushMonkeyBean();
            mPushMonkeyBean.setTask(Constants.MpushTaskLabel.START_PERFORMS_TEST);
            mPushMonkeyBean.setM_meid(PerformsData.getInstance(SuperTestApplication
                    .getContext()).readStringData(iPerformsKey.imei));
            mPushMonkeyBean.setType(Constants.U2TaskConstants.U2_TASK_TYPE_OF_GET_ID_REQUEST);

            Map<String, String> data = new ArrayMap<String, String>();
            data.put("testType", getTestType());
            mPushMonkeyBean.setData(data);
            Logger.d("获取U2Task任务的JSON串==>" + JSON.toJSONString(mPushMonkeyBean));
            MPush.I.sendPush(JSON.toJSONString(mPushMonkeyBean).getBytes(Constants.UTF_8));
            return true;
        }else{
            return false;
        }
    }
}
