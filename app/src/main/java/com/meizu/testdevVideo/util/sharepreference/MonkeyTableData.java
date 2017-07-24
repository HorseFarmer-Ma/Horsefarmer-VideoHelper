package com.meizu.testdevVideo.util.sharepreference;

import android.content.Context;

import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.library.SharedPreferencesHelper;

/**
 * Monkey执行相关Preferences
 * Created by maxueming on 2016/11/16.
 */
public class MonkeyTableData {
    private static SharedPreferencesHelper monkeyTableData;

    public synchronized static SharedPreferencesHelper getInstance(Context context){
        if(monkeyTableData == null){
            monkeyTableData = new SharedPreferencesHelper(context, "monkey_table");
        }
        return monkeyTableData;
    }


    /**
     * 获取monkey运行状态
     * @param context
     * @return
     */
    public synchronized static boolean isMonkeyStart(Context context){
        return MonkeyTableData.getInstance(context).readBooleanData(Constants.Monkey.IS_START);
    }

    /**
     * 设置Monkey执行类型
     * @param context
     * @param monkeyType
     */
    public synchronized static void setMonkeyType(Context context, String monkeyType){
        MonkeyTableData.getInstance(context).writeStringData(Constants.Monkey.MONKEY_TYPE, monkeyType);
    }

    /**
     * 获取Monkey执行类型
     * @param context
     */
    public synchronized static String getMonkeyType(Context context){
        return MonkeyTableData.getInstance(context).readStringData(Constants.Monkey.MONKEY_TYPE);
    }



    /**
     * 设置monkey开始或停止
     * @param context
     * @param isStart
     */
    public synchronized static void setMonkeyStart(Context context, boolean isStart){
        MonkeyTableData.getInstance(context).writeBooleanData(Constants.Monkey.IS_START, isStart);
    }

    /**
     * Monkey执行开始时间
     * @param context
     * @param startTime
     */
    public synchronized static void setMonkeyStartTime(Context context, String startTime){
        MonkeyTableData.getInstance(context).writeStringData(Constants.Monkey.MONKEY_START_TIME, startTime);
    }

    /**
     * Monkey执行开始时间
     * @param context
     * @return
     */
    public synchronized static String getMonkeyStartTime(Context context){
        return MonkeyTableData.getInstance(context).readStringData(Constants.Monkey.MONKEY_START_TIME);
    }

    /**
     * 获取执行的Monkey指令
     * @param context
     * @return
     */
    public synchronized static String getMonkeyCommand(Context context){
        return MonkeyTableData.getInstance(context).readStringData(Constants.Monkey.MONKEY_COMMAND);
    }

    /**
     * 设置执行的Monkey指令
     * @param context
     * @param monkeyCommand
     */
    public synchronized static void setMonkeyCommand(Context context, String monkeyCommand){
        MonkeyTableData.getInstance(context).writeStringData(Constants.Monkey.MONKEY_COMMAND, monkeyCommand);
    }

    /**
     * 获取Monkey停止时间
     * @param context
     * @return
     */
    public synchronized static long getMonkeyStopTime(Context context){
        return MonkeyTableData.getInstance(context).readLongData(Constants.Monkey.MONKEY_RUN_TIME);
    }

    /**
     * 设置Monkey停止时间
     * @param context
     * @param stopTime
     */
    public synchronized static void setMonkeyStopTime(Context context, Long stopTime){
        MonkeyTableData.getInstance(context).writeLongData(Constants.Monkey.MONKEY_RUN_TIME, stopTime);
    }

    /**
     * 获取Monkey动作
     * @param context
     * @return
     */
    public synchronized static int getMonkeyAction(Context context){
        return MonkeyTableData.getInstance(context).readIntData(Constants.Monkey.MONKEY_ACTION);
    }

    /**
     * 设置Monkey动作
     * @param context
     * @param monkeyAction
     */
    public synchronized static void setMonkeyAction(Context context, int monkeyAction){
        MonkeyTableData.getInstance(context).writeIntData(Constants.Monkey.MONKEY_ACTION, monkeyAction);
    }




    /**----------------------------------- 闹钟定时执行MONKEY相关--------------------------------**/
    /**
     * 获取闹钟开始时间，即当天开始跑monkey时间
     * @param context
     * @return
     */
    public synchronized static long getAlarmStartTime(Context context){
        return MonkeyTableData.getInstance(context).readLongData(Constants.Monkey.MONKEY_ALARM_START_TIME);
    }

    /**
     * 设置闹钟定时开始时间
     * @param context
     * @param startTime
     */
    public synchronized static void setAlarmStartTime(Context context, long startTime){
        MonkeyTableData.getInstance(context).writeLongData(Constants.Monkey.MONKEY_ALARM_START_TIME, startTime);
    }

    /**
     * 获取Monkey执行时间
     * @param context
     * @return
     */
    public synchronized static long getAlarmRunTime(Context context){
        return MonkeyTableData.getInstance(context).readLongData(Constants.Monkey.ALARM_MONKEY_RUN_TIME);
    }

    /**
     * 设置Monkey执行时间
     * @param context
     * @param startTime
     */
    public synchronized static void setAlarmRunTime(Context context, long startTime){
        MonkeyTableData.getInstance(context).writeLongData(Constants.Monkey.ALARM_MONKEY_RUN_TIME, startTime);
    }

    /**
     * 获取定时执行的Monkey指令
     * @param context
     * @return
     */
    public synchronized static String getAlarmMonkeyCommand(Context context){
        return MonkeyTableData.getInstance(context).readStringData(Constants.Monkey.ALARM_MONKEY_COMMAND);
    }

    /**
     * 设置定时执行的Monkey指令
     * @param context
     * @param alarmMonkeyCommand
     */
    public synchronized static void setAlarmMonkeyCommand(Context context, String alarmMonkeyCommand){
        MonkeyTableData.getInstance(context).writeStringData(Constants.Monkey.ALARM_MONKEY_COMMAND, alarmMonkeyCommand);
    }

    /**
     * 设置定时Monkey执行类型
     * @param context
     * @param alarmMonkeyType
     */
    public synchronized static void setAlarmMonkeyType(Context context, String alarmMonkeyType){
        MonkeyTableData.getInstance(context).writeStringData(Constants.Monkey.ALARM_MONKEY_TYPE, alarmMonkeyType);
    }

    /**
     * 获取定时Monkey执行类型
     * @param context
     */
    public synchronized static String getAlarmMonkeyType(Context context){
        return MonkeyTableData.getInstance(context).readStringData(Constants.Monkey.ALARM_MONKEY_TYPE);
    }


    /**
     * 设置MonkeyId
     * @param context
     * @param monkeyId
     */
    public synchronized static void setMonkeyId(Context context, String monkeyId){
        MonkeyTableData.getInstance(context).writeStringData(Constants.Monkey.MONKEY_ID, monkeyId);
    }

    /**
     * 获取MonkeyId
     * @param context
     */
    public synchronized static String getMonkeyId(Context context){
        return MonkeyTableData.getInstance(context).readStringData(Constants.Monkey.MONKEY_ID);
    }

}
