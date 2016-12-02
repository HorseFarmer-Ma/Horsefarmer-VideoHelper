package com.meizu.testdevVideo.util.log;

import com.meizu.testdevVideo.interports.iLog;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by maxueming on 2016/3/3.
 */
public class SaveLog implements iLog {

    /**
     * 保存所有log信息
     * module：从公共类中传进来获取的当前的类名
     */
    public String saveAll(){
        File file = new File(LOG_LOCALTION + "/");    // 在SDcard中创建文件夹
        if (!file.exists()){
            file.mkdirs();// 当不存在这个问题时，新创建一个目录
        }

        // 获取当前时间
        String currenttime = getCurrentSystemTime();

        // 保存所有log信息
        saveAllLog(currenttime);

        // 清除缓存中的log信息
        process(COMMAND_CLEAR_LOG);
        return currenttime;
    }

    /**
     * 参考钟科组“关于手机”指令，保存log信息
     */
    public String saveAllLog(String currenttime) {
        StringBuilder sb = new StringBuilder();   // 设置一个缓冲区，用于保存log信息
        String line;   // 读写行暂存函数
        OutputStreamWriter osw = null;   // 初始化输出流文件

        try {
            // 创建.log文件
            osw = new OutputStreamWriter(new FileOutputStream(LOG_LOCALTION + "/" + "log_" + currenttime + ".txt"));
        } catch (FileNotFoundException e1) {
            System.out.println("have error at savedump method" + e1);
        }
        // 列出要执行的指令
        try {
            osw.write("***********************************************************" + "\n\n");
            osw.write("感谢钟科组“关于手机”的抓取log指令支持，执行以下指令：" + "\n");
            for (int i = 0; i < COMMAND_ALL_LOG.length; i++) {
                osw.write(COMMAND_ALL_LOG[i] + "\n");   // 输出要执行的指令
            }
            osw.write("\n\n" + "***********************************************************");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 循环输入指令，输出并写入log信息至“loglocaltion”路径
        for (int i = 0; i < COMMAND_ALL_LOG.length; i++) {
            try {
                osw.write("\n\n\n" + "**************************" + COMMAND_ALL_LOG[i] + "**************************" + "\n\n\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 获得进程缓冲信息
            BufferedReader brdupm = shellOut(process(COMMAND_ALL_LOG[i]));
            // 读取并写入数据
            try {
                while ((line = brdupm.readLine()) != null) {  // 从一个有若干行的文件中依次读取各行
                    osw.write(line + "\n", 0, line.length() + 1);// 将 line字符组的内容
                    // 从下标0处开始，输出length个字符到该流初始化的文件
                    sb.append(line+"\n");// 将Line字符串连接起来，相当于+
                }
            } catch (IOException e) {
                System.out.println("Write log have error at savedump method" + e);
            }

            // 强制请求清空缓冲区，让i/o系统立马完成它应该完成的输入、输出动作
            try {
                osw.flush();
            } catch (IOException e) {
                System.out.println("Save loging appear eroor at flushing at savedump method" + e);
            }

            // 关闭该流并释放与之关联的所有系统资源
            try {
                brdupm.close();
            } catch (IOException e) {
                System.out.println("Save loging appear eroor at flushing at close method" + e);
            }
        }
        return sb.toString();    // 返回Log信息
    }

    /**
     * logClass：log指令
     * currenttime:当前时间
     * module：当前执行案例
     * errorinfo：错误类型
     * 保存dumpsys信息,查询所有service的状态
     */
    private String saveLog(String logCommand, String currenttime, String module, String errorinfo){
        // 设置一个缓冲区，用于保存log信息
        StringBuilder sb = new StringBuilder();
        // 获得进程缓冲信息
        BufferedReader brdupm = shellOut(process(logCommand));
        String line;
        OutputStreamWriter osw = null;
        try{
            // 创建.log文件
            osw = new OutputStreamWriter(new FileOutputStream(LOG_LOCALTION + module + "/" + currenttime + "/" + errorinfo + ".txt"));
        }catch (FileNotFoundException e1) {
            System.out.println("have error at savedump method" + e1);
        }
        // 读取并写入数据
        try{
            while ((line = brdupm.readLine()) != null){  // 从一个有若干行的文件中依次读取各行
                osw.write(line+"\n", 0, line.length()+1);// 将 line字符组的内容
                // 从下标0处开始，输出length个字符到该流初始化的文件
                sb.append(line);// 将Line字符串连接起来，相当于+
            }
        }catch (IOException e) {
            System.out.println("Write log have error at savedump method" + e);
        }

        // 强制请求清空缓冲区，让i/o系统立马完成它应该完成的输入、输出动作
        try{
            osw.flush();
        }catch (IOException e){
            System.out.println("Save loging appear eroor at flushing at savedump method"   + e);
        }

        // 关闭该流并释放与之关联的所有系统资源
        try{
            brdupm.close();
        }catch (IOException e) {
            System.out.println("Save loging appear eroor at flushing at close method"  + e);
        }

        return sb.toString();    // 返回Log信息
    }

    /**
     * 打开某个进程
     */
    private Process process(String command) {
        Process ps = null;
        try {
            ps = Runtime.getRuntime().exec(command);
        }catch (IOException e){
            System.out.println("执行命令错误！" + e);
        }
        return ps;
    }

    /**
     * 读取进程信息，并保在缓冲中
     */
    private BufferedReader shellOut(Process ps){
        BufferedInputStream in = new BufferedInputStream(ps.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        return br;
    }

    /**
     * 获取当前时间
     * @return time
     */
    private String getCurrentSystemTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");   // 格式化当前时间
        String time = dateFormat.format(new Date());    // 获取当前时间
        return time;
    }
}