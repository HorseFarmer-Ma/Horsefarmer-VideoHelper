package com.meizu.testdevVideo.library;

import android.util.Log;

import com.meizu.testdevVideo.util.PublicMethod;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
            Log.d("POST", "发送数据");
            if(response == HttpURLConnection.HTTP_OK) {
                Log.d("POST结果", "发送成功");
                InputStream inptStream = httpURLConnection.getInputStream();
                postCallBack.resultCallBack(true, response, dealResponseResult(inptStream));
            } else{
                postCallBack.resultCallBack(true, response, null);
                Log.d("POST结果", "发送失败");
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


    /**
     * Function  : POST文本和文件到服务器
     * Param     : params请求体内容
     */
    public void postFile(String actionUrl, Map<String, String> params,
                              Map<String, File> files, PostCallBack postCallBack) throws IOException {

        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";
        URL uri = new URL(actionUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setChunkedStreamingMode(128 * 1024);   // 设置块大小：128K，当内容达到这个值的时候就把流输出
        conn.setReadTimeout(5 * 1000);
        conn.setDoInput(true);   // 允许输入
        conn.setDoOutput(true);   // 允许输出
        conn.setUseCaches(false);
        conn.setRequestMethod("POST"); // Post方式
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
                + ";boundary=" + BOUNDARY);

        // 首先组拼文本类型的参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\""
                    + entry.getKey() + "\"" + LINEND);
            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            sb.append(LINEND);
            sb.append(entry.getValue());
            sb.append(LINEND);
        }

        DataOutputStream outStream = new DataOutputStream(conn
                .getOutputStream());
        outStream.write(sb.toString().getBytes());
        outStream.flush();

        // 发送文件数据
        if (files != null)
            for (Map.Entry<String, File> file : files.entrySet()) {
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""
                                + PublicMethod.getFileName(file.getValue().toString()) + "\"" + LINEND);
                sb1.append("Content-Type: application/octet-stream; charset="
                        + CHARSET + LINEND);
                sb1.append(LINEND);
                outStream.write(sb1.toString().getBytes());
                InputStream is = new FileInputStream(file.getValue());
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                    outStream.flush();
                }

                is.close();
                outStream.write(LINEND.getBytes());
                outStream.flush();
            }

        // 请求结束标志
        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
        outStream.write(end_data);
        outStream.flush();

        // 得到响应码
        int res = conn.getResponseCode();
        boolean isSendComplete = false;

        InputStream in = conn.getInputStream();
        StringBuilder sb2 = new StringBuilder();
//        InputStreamReader isReader = new InputStreamReader(in);
//        BufferedReader bufReader = new BufferedReader(isReader);
//        String line = null;
//        String data = "OK";

//        while ((line = bufReader.readLine()) == null)
//            data += line;

        if (200 == res) {
            int ch;
            while ((ch = in.read()) != -1) {
                sb2.append((char) ch);
            }
            isSendComplete = true;
        }

        postCallBack.resultCallBack(isSendComplete, res, sb2.toString());
        outStream.close();
        in.close();

        conn.disconnect();
//        return in.toString();
    }




}
