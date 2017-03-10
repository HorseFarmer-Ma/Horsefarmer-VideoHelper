package com.meizu.testdevVideo.util.ftp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

/**
 * Created by maxueming on 2016/12/30.
 */
public class FtpHelper {
    /**
     * 连接FTP服务器
     * @param host 地址
     * @param port 端口号
     */
    public static void connect(FTPClient client, String host, int port, String userName, String passWord){
        try {
            if(client.isConnected()){
                client.disconnect(true);
            }
            client.connect(host, port);
            client.login(userName, passWord);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FTPIllegalReplyException e) {
            e.printStackTrace();
        } catch (FTPException e) {
            e.printStackTrace();
        }
    }


    /**
     * 返回根目录
     */
    public static void goBackRootDir(FTPClient client, String dir){
        try {
            client.changeDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FTPIllegalReplyException e) {
            e.printStackTrace();
        } catch (FTPException e) {
            e.printStackTrace();
        }
    }


    /**
     * 断开FTP服务器
     */
    public static void disConnect(FTPClient client){
        if(client.isConnected()){
            try {
                client.disconnect(true);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
            } catch (FTPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return 返回当前列表下所有文件的信息
     * @throws FTPException
     * @throws IOException
     * @throws FTPDataTransferException
     * @throws FTPListParseException
     * @throws FTPIllegalReplyException
     * @throws FTPAbortedException
     */
    public static List<Map<String, Object>> getListName(FTPClient client, String key) throws
            FTPException, IOException, FTPDataTransferException, FTPListParseException,
            FTPIllegalReplyException, FTPAbortedException {
        List<Map<String, Object>> listName = new ArrayList<Map<String, Object>>();
        FTPFile[] list = client.list();
        for(FTPFile f : list){
            if(!f.getName().equals(".") && !f.getName().equals("..")){
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(key, f.getName());
//                data.put(MODIFIED_DATE, f.getModifiedDate());
                listName.add(data);
            }
        }
        return listName;
    }

    /**
     * @return 返回当前列表下所有文件夹
     * @throws FTPException
     * @throws IOException
     * @throws FTPDataTransferException
     * @throws FTPListParseException
     * @throws FTPIllegalReplyException
     * @throws FTPAbortedException
     */
    public static List<Map<String, Object>> getFolderName(FTPClient client, String key) throws
            FTPException, IOException, FTPDataTransferException, FTPListParseException,
            FTPIllegalReplyException, FTPAbortedException {
        List<Map<String, Object>> listName = new ArrayList<Map<String, Object>>();
        FTPFile[] list = client.list();
        for(FTPFile f : list){
            if(!f.getName().contains(".txt") && !f.getName().contains(".docx")
                    && !f.getName().contains(".xlsx") && !f.getName().contains(".bmp")
                    && !f.getName().contains(".zip") && !f.getName().contains(".java")
                    && !f.getName().contains(".jar") && !f.getName().contains(".pptx")){
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(key, f.getName());
                listName.add(data);
            }
        }
        return listName;
    }

    /**
     * @return 返回当前列表下所有文件夹
     * @throws FTPException
     * @throws IOException
     * @throws FTPDataTransferException
     * @throws FTPListParseException
     * @throws FTPIllegalReplyException
     * @throws FTPAbortedException
     */
    public static List<Map<String, Object>> getApkName(FTPClient client, String key) throws
            FTPException, IOException, FTPDataTransferException, FTPListParseException,
            FTPIllegalReplyException, FTPAbortedException {
        List<Map<String, Object>> listName = new ArrayList<Map<String, Object>>();
        FTPFile[] list = client.list("*.apk");
        for(FTPFile f : list){
            if(!f.getName().equals(".") && !f.getName().equals("..")){
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(key, f.getName());
                listName.add(data);
            }
        }
        return listName;
    }
}
