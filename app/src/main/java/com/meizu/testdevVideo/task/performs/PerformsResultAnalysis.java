package com.meizu.testdevVideo.task.performs;

import com.alibaba.fastjson.JSONObject;
import com.meizu.testdevVideo.util.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析性能测试文件
 * Created by maxueming on 2017/5/9.
 */
public class PerformsResultAnalysis {
    private static final String CPU = "cpu";
    private static final String MODEM = "Modem";
    private static final String MRA = "MRA";
    private static final String WIFI = "WiFi";
    private static final String ALARM = "alarm";
    private static final String WAKELOCK = "WakeLock";

    private static final String CPU_KEY_WORD = "Total cpu time: ";
    private static final String MODEM_KEY_WORD = "Mobile network: ";
    private static final String MRA_KEY_WORD = "Mobile radio active: ";
    private static final String WIFI_KEY_WORD = "Wi-Fi network: ";
    private static final String ALARM_KEY_WORD = "Wake lock alarm";
    private static final String WAKELOCK_KEY_WORD = "TOTAL wake: ";

    private static final String LOGVALUE_RESULT = "logValue_result";

    // 解析纯净后台日志
    public static final String getClearBackGround(String log){
        Map<String, String> result = new HashMap<String, String>();
        String keyValue = null;

        try {
            // MODEM
            if(null != log && log.contains(MODEM_KEY_WORD)){
                keyValue = log.split(MODEM_KEY_WORD)[1].split("\n")[0];
                float modemResult = 0;
                if(null != keyValue && keyValue.contains("KB received")){
                    modemResult += Float.parseFloat(keyValue.split("KB received")[0]);
                }
                if(null != keyValue && keyValue.contains("KB sent ")){
                    modemResult += Float.parseFloat(keyValue.split("KB sent ")[0].split(" ")[keyValue.split("KB sent ")[0].split(" ").length - 1]);
                }
                result.put(MODEM, String.valueOf(modemResult));
            }else{
                result.put(MODEM, "-");
            }

            // MRA
            if(null != log && log.contains(MRA_KEY_WORD)){
                keyValue = log.split(MRA_KEY_WORD)[1].split("\n")[0];
                result.put(MRA, String.valueOf(getTimeResult(keyValue, " ")/1000));
            }else{
                result.put(MRA, "-");
            }

            // WIFI
            if(null != log && log.contains(WIFI_KEY_WORD)){
                keyValue = log.split(WIFI_KEY_WORD)[1].split("\n")[0];
                float wifiResult = 0;
                if(null != keyValue && keyValue.contains("KB received")){
                    wifiResult += Float.parseFloat(keyValue.split("KB received")[0]);
                }
                if(null != keyValue && keyValue.contains("KB sent ")){
                    wifiResult += Float.parseFloat(keyValue.split("KB sent ")[0]
                            .split(" ")[keyValue.split("KB sent ")[0].split(" ").length - 1]);
                }

                result.put(WIFI, String.valueOf(wifiResult));

            }else{
                result.put(WIFI, "-");
            }

            // WAKELOCK
            if(null != log && log.contains(WAKELOCK_KEY_WORD)){
                keyValue = log.split(WAKELOCK_KEY_WORD)[1].split("\n")[0];
                result.put(WAKELOCK, String.valueOf(getTimeResult(keyValue, " ")/1000));
            }else{
                result.put(WAKELOCK, "-");
            }

            // CPU
            if(null != log && log.contains(CPU_KEY_WORD)){
                keyValue = log.split(CPU_KEY_WORD)[1].split("\n")[0];
                result.put(CPU, String.valueOf(getTimeResult(keyValue, "=")));
            }else{
                result.put(CPU, "-");
            }

            // 适配出现“*”的问题
            if(null != log){
                log = log.replace("*", "");
            }

            // ALARM
            if(null != log && log.contains(ALARM_KEY_WORD)){
                keyValue = log.split(ALARM_KEY_WORD)[1].split("\n")[0];
                int times = 0;
                if(null != keyValue && keyValue.contains(" times")){
                    String[] value = keyValue.split(" times")[0].split("\\(");
                    times += Integer.parseInt(value[value.length - 1]);
                }
                result.put(ALARM, String.valueOf(times));
            }else{
                result.put(ALARM, "-");
            }
        }catch (Exception e){
            e.printStackTrace();
            Logger.file("解析性能测试文件报错==>" + e.toString(), Logger.U2TASK);
            return "";
        }

        return JSONObject.toJSONString(result);
    }


    /**
     * 返回字符串
     * @param value 输入值，例： Mobile radio active: 3m 16s 620ms (2.9%) 13x @ 667 mspp
     * 以上例子，可获取3、16、620、13、667
     * @param regrexStart 匹配的首部字符串
     * @param regrexLast 匹配的尾部字符串
     * @return 结果，float类型
     */
    private static float getNumber(String value, String regrexStart, String regrexLast){
        float result = 0;
        int compensate = value.endsWith(regrexLast)? 0 : 1;    // 判断regrexLast是否为结尾字符，补偿作用
        String[] valueContent = value.split(regrexLast);

        for(int i = 0; i < valueContent.length - compensate; i++){
            // 适配获取CPU出现" "的情况
            if(regrexStart.equals("=")){
                valueContent[i] = valueContent[i].replace(" ", "=");
            }
            result += Float.parseFloat(valueContent[i].split(regrexStart)[valueContent[i].split(regrexStart).length - 1]);
        }
        return result;
    }

    /**
     * 筛选查找并计算出包含时间的关键
     * @param keyValue 关键词
     * @return 结果
     */
    private static float getTimeResult(String keyValue, String regrexStart){
        float result = 0;
        if(null != keyValue && keyValue.contains("h ")){
            result += getNumber(keyValue, regrexStart, "h ") * 60 * 60 * 1000;
        }

        if(null != keyValue && keyValue.contains("m ")){
            result += getNumber(keyValue, regrexStart, "m ") * 60 * 1000;
        }

        if(null != keyValue && keyValue.contains("ms ")){
            result += getNumber(keyValue, regrexStart, "ms ");
            keyValue = keyValue.replace("ms ", " ");
        }

        if(null != keyValue && keyValue.contains("s ")){
            result += getNumber(keyValue, regrexStart, "s ") * 1000;
        }

        if(null != keyValue && keyValue.contains("us ")){
            result += getNumber(keyValue, regrexStart, "us ")/1000;
        }

        if(null != keyValue && keyValue.contains("mAh")){
            result += getNumber(keyValue, regrexStart, "mAh");
        }

        return result;
    }
}
