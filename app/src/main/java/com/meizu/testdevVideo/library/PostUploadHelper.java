package com.meizu.testdevVideo.library;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maxueming on 2016/10/21.
 */
public class PostUploadHelper {

    public static PostUploadHelper mInstance;

    public synchronized static PostUploadHelper getInstance(){
        if(mInstance == null){
            mInstance = new PostUploadHelper();
        }
        return mInstance;
    }

    /**
     * Function  : 发送Post请求到服务器
     * Param     : params请求体内容，encode编码格式
     */
    public void submitPostData(String urlAdress, Map<String, String> params, PostCallBack postCallBack) throws MalformedURLException {
        URL url = new URL(urlAdress);
        byte[] data = getRequestData(params, "utf-8").toString().getBytes();    //获得请求体
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);           // 设置连接超时时间
            httpURLConnection.setDoInput(true);                  // 打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 // 打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");          // 设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               // 使用Post方式不能使用缓存
            // 设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            // 获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            Log.e("POST", "发送数据");
            if(response == HttpURLConnection.HTTP_OK) {
                Log.e("POST结果", "发送成功");
                InputStream inptStream = httpURLConnection.getInputStream();
                postCallBack.resultCallBack(true, response, dealResponseResult(inptStream));
            } else{
                postCallBack.resultCallBack(true, response, null);
                Log.e("POST结果", "发送失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 定义超时为
            postCallBack.resultCallBack(false, 503, e.toString());
        }
    }


    /**
     * Function : 封装请求体信息
     * Param    : params请求体内容，encode编码格式
     */
    public StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    /**
     * Function  :   处理服务器的响应结果（将输入流转化成字符串）
     * Param     :   inputStream服务器的响应输入流
     */
    public String dealResponseResult(InputStream inputStream) {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }
}
