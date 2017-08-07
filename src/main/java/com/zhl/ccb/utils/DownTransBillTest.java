package com.zhl.ccb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;






import org.apache.commons.lang.StringUtils;








import com.google.common.collect.Maps;

public class DownTransBillTest {
	/** 
     * 解析zip文件中的csv文件 
     * @param strZipFile
     *        生成的zip文件路径
     * @param strCsvFileName 
     *        zip文件保存的csv文件的名字
     * @return List 
     * @throws IOException 
     */  
    public static List<Map<String,String>> getCsvFileList(String strZipFile,String strCsvFileName)throws IOException {  
        //Zip文件中的文件对象 
        ZipEntry zipEntry;  
        int lines = 0;
        List<Map<String,String>> list = new ArrayList<Map<String,String>>();
        //文件读入用  
        InputStream inStream = null;  
        ZipInputStream zipInStream = null;  
        //Zip文件  
        ZipFile zipNotSendFile = null;  
        try{  
			zipNotSendFile = new ZipFile(strZipFile);  
			inStream = new BufferedInputStream(new FileInputStream(strZipFile));  
			zipInStream = new ZipInputStream(inStream);  
			//Zip文件解压  
			while((zipEntry = zipInStream.getNextEntry()) != null){  
//			    if (!zipEntry.isDirectory() && zipEntry.getName().indexOf(strCsvFileName.replace(".csv", "").replace(".zip", "").substring(0, 6)) >= 0){
				if (!zipEntry.isDirectory()){
			        //读取csv文件内容
			        BufferedReader bufReader = new BufferedReader(new InputStreamReader(zipNotSendFile.getInputStream(zipEntry),"GBK"));  
			        //csv文件行内容 
			        String strCsvline = null;  
			        while ((strCsvline = bufReader.readLine()) != null){
			        	lines++;
			        	if(lines > 1){
			        		String item[] = strCsvline.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
			        		Map<String, String> txtMap = new HashMap<String, String>();
			             	txtMap.put("aa", item[0]);
			             	txtMap.put("bb", item[1]);
			             	txtMap.put("cc", item[2]);
			             	txtMap.put("dd", item[3]);
			             	txtMap.put("ee", item[4]);
			             	txtMap.put("ff", item[5]);
			             	list.add(txtMap);
			        	}
			        }  
			        bufReader.close();  
			    }  
			}  
        }catch(IOException e){  
            throw e;  
        }finally{  
            //colse文件 及数据流
			try {  
			    if (null != zipNotSendFile) {  
			        zipNotSendFile.close();  
			    }  
			    if (null != inStream) {  
			        inStream.close();  
			    }  
			    if (null != zipInStream) {  
			        zipInStream.closeEntry();  
			    }  
			} catch (IOException e) {  
			    throw e;  
			}  
        }  
        return list;  
    } 
	
	/**
     * 发起http请求获取文件流并下载，返回结果true/false
     * @param req_url 请求地址
     * @param req_method 请求方式（GET、POST）
     * @param out_str 提交的数据
     * @param charset 字符集
     * @param fileUrl 生成文件目录
     * @return 返回数据boolean
     */
    public static Boolean http_request(String req_url, String req_method, String out_str, String charset,String fileUrl){
    	boolean b = true;
    	try{
            if (charset == null || charset.trim().equals("")) {
                charset = "UTF-8";
            }
            URL url = new URL(req_url);
            HttpURLConnection http_conn = (HttpURLConnection) url.openConnection();
            //tp正文内，因此需要设为true, 默认情况下是false;
            http_conn.setDoOutput(true);
            //设置是否从httpUrlConnection读入，默认情况下是true;
            http_conn.setDoInput(true);
            //Post请求不能使用缓存
            http_conn.setUseCaches(false);
            //设定请求的方法为"POST"，默认是GET
            http_conn.setRequestMethod(req_method);
            //连接主机的超时时间（单位：毫秒）
            http_conn.setConnectTimeout(60 * 1000);
            //从主机读取数据的超时时间（单位：毫秒）
            http_conn.setReadTimeout(60 * 1000);
            //设置通用的请求属性
            http_conn.setRequestProperty("Accept", "*/*");
            http_conn.setRequestProperty("Connection", "Keep-Alive");
            http_conn.setRequestProperty("Cache-Control", "no-cache");
            http_conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http_conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Foxy/1; .NET CLR 2.0.50727; MEGAUPLOAD 1.0)");
//            if (REQ_METHOD_GET.equalsIgnoreCase(req_method)) {
            http_conn.connect();
//            }
            // 当有数据需要提交时
            if (null != out_str) {
                OutputStreamWriter out = new OutputStreamWriter(http_conn.getOutputStream());
                out.write(out_str);
                out.flush();
                out.close();
            }
            // 将返回的输入流转换成字符串
            InputStream is = http_conn.getInputStream();
            File file = new File(fileUrl);// 要写入的文本地址
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
             os.write(buffer, 0, bytesRead);
            }
            os.close();
            is.close();
        }catch(Exception e){
        	e.printStackTrace();
        	b = false;
        }
        return b;
    }

	
	public static void main(String[] args) throws Exception{
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("userName",Constants.getReadProperties("ccb","ccb.wechat.active.merId"));//用户账号
		reqMap.put("userPwd",MD5.MD5Encode("91DfWQ"));//API密码
		reqMap.put("date","20170510");//对账单日期
		reqMap.put("statementType",Constants.statement_type_0);//对账单类型
		String json = JsonUtil.getMapToJsonStr(reqMap);
		System.out.println("json="+json);
		
//		String requrl = "https://120.27.165.177:8099/MerWeb/StatementQueryApi";
//		String savePath = "F:/CCB_CHECK/"+reqMap.get("date")+".zip";
//		/**
//		 * 默认的http连接超时时间
//		 */
//		int connTimeOut = 120000;	//10s
//		/**
//		 * 默认的http read超时时间
//		 */
//		int readTimeOut = 120000;	//120s
//		String ret = Constants.postReq(requrl, savePath, json, connTimeOut, readTimeOut);
//		System.out.println(ret);
		
		
		HttpRequestParam param = new HttpRequestParam();
		param.setUrl(Constants.getReadProperties("ccb","ccb.check.url"));
		Map<String,String> heads = Maps.newHashMap();
		heads.put("Content-Type", "application/json;charset=UTF-8");
		param.setContext(json);
		param.setHeads(heads);
		HttpResponser resp=HttpHelp.postParamByHttpClient(param);
		
		if(Constants.isGoodJson(resp.getContent()) == true){
			System.out.println(resp.getContent());
		}else{
			//生成zip文件
			String zipname="F:/CCB_CHECK/"+reqMap.get("date")+".zip";
			boolean downLoadStatus = Constants.http_request(Constants.getReadProperties("ccb","ccb.check.url"), "POST", json, "UTF-8", zipname);
			if(downLoadStatus = true){
				System.out.println("下载成功");
//				Thread thread = Thread.currentThread();
//			    thread.sleep(20000);//暂停20秒后程序继续执行
			    System.out.println("准备读取");
//			    List<Map<String,String>> map = getCsvFileList(zipname,"");
			    List<Map<String,String>> map = Constants.getCsvFileList(zipname, "");
			    for(Map<String,String> str : map){
//			    	System.out.println("1="+str.get("aa"));
//			    	System.out.println("2="+str.get("bb"));
//			    	System.out.println("3="+str.get("cc"));
//			    	System.out.println("4="+str.get("dd"));
//			    	System.out.println("5="+str.get("ee"));
//			    	System.out.println("6="+str.get("ff"));
			    	System.out.println("------------------------------------------------------");
			    	System.out.println("1="+str.get("trans_date"));
			    	System.out.println("2="+str.get("mer_order_no"));
			    	System.out.println("3="+str.get("cc_order_no"));
			    	System.out.println("4="+str.get("cc_old_order_no"));
			    	System.out.println("5="+str.get("trans_amount"));
			    	System.out.println("6="+str.get("trans_status"));
			    	System.out.println("7="+str.get("refund_amount"));
			    	System.out.println("8="+str.get("refund_status"));
			    	
			    }
			}else{
				System.out.println("下载失败");
			}
		}
//		String strZipFile = "F:/CCB_CHECK/20170510_997700000000243.zip";
//		ZipFile zipNotSendFile = new ZipFile(strZipFile); 
		
	}
}
