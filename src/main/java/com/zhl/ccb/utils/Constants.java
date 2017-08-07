package com.zhl.ccb.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;

import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.jnewsdk.connection.client.HttpClient;
import com.jnewsdk.connection.client.HttpSSLClient;
import com.jnewsdk.tools.log.LogFactory;

/**
 * 中信银行服务——常量类
 * @author pie
 */
public class Constants {
	private static Logger logger = Logger.getLogger(Constants.class);
	private static Properties props = new Properties();
	private static final String URL_PARAM_CONNECT_FLAG = "&";
	
	/**
	 * 编码方式
	 */
	public static final String charset_UTF_8 = "UTF-8";
	/**
	 * 签名方式
	 */
	public static final String sing_method_MD5 = "02";
	public static final String sing_method_RSA = "03";
	/**
	 * 交易类型
	 * 消费
	 * 查询交易状态
	 * 退货
	 * 子商户入驻
	 * 子账户查询
	 */
	public static final String trade_type_01 = "01";
	public static final String trade_type_38 = "38";
	public static final String trade_type_48 = "48";
	public static final String trade_type_04 = "04";
	public static final String trade_type_09 = "09";
	public static final String trade_type_10 = "10";
	/**
	 * 交易子类型
	 * 二维码支付
	 * 支付宝二清主扫码消费
	 * 交易状态查询
	 * 支付宝二清交易查询接口
	 * 微信消费
	 * 支付宝二清消费
	 * 微信查询交易状态
	 * 支付宝二清查询交易状态
	 * 线上微信退货
	 * 线上支付宝退货
	 * 线下微信退货
	 * 线下支付宝退货
	 * 分账子商户入驻/修改
	 * 分账子商户查询
	 * 公众号支付
	 * H5支付
	 * 支付宝二清交易创建
	 */
	public static final String trade_sub_type_010130 = "010130";
	public static final String trade_sub_type_010302 = "010302";
	public static final String trade_sub_type_383000 = "383000";
	public static final String trade_sub_type_381004 = "381004";
	public static final String trade_sub_type_481000 = "481000";
	public static final String trade_sub_type_481003 = "481003";
	public static final String trade_sub_type_381000 = "381000";
	public static final String trade_sub_type_381003 = "381003";
	public static final String trade_sub_type_040441 = "040441";
	public static final String trade_sub_type_040303 = "040303";
	public static final String trade_sub_type_483000 = "483000";
	public static final String trade_sub_type_483003 = "483003";
	public static final String trade_sub_type_900030 = "900030";
	public static final String trade_sub_type_900040 = "900040";
	public static final String trade_sub_type_010131 = "010131";
	public static final String trade_sub_type_010133 = "010133";
	public static final String trade_sub_type_010303 = "010303";
	
	/**
	 * 接入渠道
	 * 商户互联网渠道
	 * 持牌机构MIS接入
	 */
	public static final String channel_type_6002 = "6002";
	public static final String channel_type_6005 = "6005";
	/**
	 * 接入支付类型
	 * 接口支付
	 */
	public static final String pay_access_type_02 = "02";
	/**
	 * 对账单类型
	 * 下载微信和支付宝对账单
	 * 下载冻结对账单
	 */
	public static final String statement_type_0 = "0";
	public static final String statement_type_3 = "3";
	
	/**
	 * 交易币种
	 */
	public static final String trade_currency_type_CNY = "156";
	/**
	 * 通讯服务商
	 */
	public static final String wechat_nfc_merch = "wechat";
	public static final String alipay_nfc_merch = "alipay";
	/**
	 * 通讯类型
	 * 被扫
	 * 主扫
	 * 冲正
	 * 退货
	 * 查询订单（内部区分支付和退货）
	 */
	public static final String nfc_passive = "nfc_passive";
	public static final String nfc_active = "nfc_active";
	public static final String nfc_reversal = "nfc_reversal";
	public static final String nfc_refund = "nfc_refund";
	public static final String nfc_query = "nfc_query";
	public static final String nfc_channelPay = "nfc_channelPay";
	/**
	 * 业务状态
	 * 被扫业务先生成二维码，支付后变成成功或失败 
	 */
	public static final String nfc_pay_status_0 = "0";
	public static final String nfc_pay_status_0_context = "交易中";
	public static final String nfc_pay_status_1 = "1";
	public static final String nfc_pay_status_1_context = "交易完成";
	public static final String nfc_pay_status_1_context_refund_RP = "交易完成，部分退款";
	public static final String nfc_pay_status_1_context_refund_RF = "交易完成，全部退款";
	public static final String nfc_pay_status_1_context_refund_RE = "交易完成，订单退款";
	public static final String nfc_pay_status_1_context_refund = "交易完成，转入退款";
	public static final String nfc_pay_status_1_context_close = "交易完成，已关闭";
	public static final String nfc_pay_status_9_context_close = "交易失败，已关闭";
	public static final String nfc_pay_status_1_context_reversal = "交易完成，已冲正";
	public static final String nfc_pay_status_2 = "2";
	public static final String nfc_pay_status_2_context = "退货中";
	public static final String nfc_pay_status_3 = "3";
	public static final String nfc_pay_status_3_context = "未支付";
	public static final String nfc_pay_status_4 = "4";
	public static final String nfc_pay_status_4_context = "冲正申请提交成功";
	public static final String nfc_pay_status_5 = "5";
	public static final String nfc_pay_status_5_context = "退货订单转入代发";
	public static final String nfc_pay_status_9 = "9";
	public static final String nfc_pay_status_9_context = "交易失败";
	public static final String nfc_pay_status_19 = "19";
	public static final String nfc_pay_status_19_context = "超时失效";
	public static final String nfc_pay_status_19_context_ZHL = "ZHL_超时失效";
	public static final String nfc_pay_status_29 = "29";
	public static final String nfc_pay_status_29_context = "退货未确定";
	
	
	
	/**
	 * 订单生成的机器IP
	 * @param 
	 * @throws DocumentException
	 */
	public static String getNetAddressIp(){
		String ip = "";
		try{
			//获取计算机名称
			//String name = InetAddress.getLocalHost().getHostName(); 
			// 获取IP地址 
			ip = InetAddress.getLocalHost().getHostAddress(); 
		}catch(UnknownHostException e){
			System.out.println("异常：" + e);            
			e.printStackTrace();
		}
		return ip;
	}
	/**  
     * 功能描述：去除字符串首部为"0"字符  
     * @param str 传入需要转换的字符串  
     * @return 转换后的字符串  
     */  
    public static String removeZero(String str){     
        char  ch;    
        String result = "";  
        if(str != null && str.trim().length()>0 && !str.trim().equalsIgnoreCase("null")){                  
            try{              
                for(int i=0;i<str.length();i++){  
                    ch = str.charAt(i);  
                    if(ch != '0'){                        
                        result = str.substring(i);  
                        break;  
                    }  
                }  
            }catch(Exception e){  
                result = "";  
            }     
        }else{  
            result = "";  
        }  
        return result;  
    }  
    /**  
     * 功能描述：金额字符串转换：单位元转成单分  
     * @param str 传入需要转换的金额字符串  
     * @return 转换后的金额字符串  
     */       
    public static String fromYuanToFen(String s) {  
        int posIndex = -1;  
        String str = "";  
        StringBuilder sb = new StringBuilder();  
        if (s != null && s.trim().length()>0 && !s.equalsIgnoreCase("null")){  
            posIndex = s.indexOf(".");  
            if(posIndex>0){  
                int len = s.length();  
                if(len == posIndex+1){  
                    str = s.substring(0,posIndex);  
                    if(str == "0"){  
                        str = "";  
                    }  
                    sb.append(str).append("00");  
                }else if(len == posIndex+2){  
                    str = s.substring(0,posIndex);  
                    if(str == "0"){  
                        str = "";  
                    }  
                    sb.append(str).append(s.substring(posIndex+1,posIndex+2)).append("0");  
                }else if(len == posIndex+3){  
                    str = s.substring(0,posIndex);  
                    if(str == "0"){  
                        str = "";  
                    }  
                    sb.append(str).append(s.substring(posIndex+1,posIndex+3));  
                }else{  
                    str = s.substring(0,posIndex);  
                    if(str == "0"){  
                        str = "";  
                    }  
                    sb.append(str).append(s.substring(posIndex+1,posIndex+3));  
                }  
            }else{  
                sb.append(s).append("00");  
            }  
        }else{  
            sb.append("0");  
        }  
        str = removeZero(sb.toString());  
        if(str != null && str.trim().length()>0 && !str.trim().equalsIgnoreCase("null")){  
            return str;  
        }else{  
            return "0";  
        }  
    }
	/**
	 * 读取配置文件方法（通用）
	 * @param fileName
	 * @param key
	 * @return value
	 */
	public static String getReadProperties(String fileName,String key){
		InputStream in = Constants.class.getResourceAsStream("/"+fileName+".properties");
		String value = "";
		try{
			props.load(in);
			if (null == key) {
				return null;
			}
			value = props.getProperty(key);
		}catch(IOException e){
			logger.error("PayCutServer:获取"+fileName+".properties文件时异常：",e);
		}finally{
			try{
				if(in != null){
					in.close();
				}
			}catch(IOException e){
				logger.error("PayCutServer:关闭文件流："+fileName+".properties文件时异常：",e);
			}
		}
		return value;
	}
	/**
	 * 将map转化为形如key1=value1&key2=value2...
	 * @param map
	 * @return
	 */
	public static String getWebForm(Map<String, String> map) {
		if (null == map || map.keySet().size() == 0) {
			return "";
		}
		StringBuffer url = new StringBuffer();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String value = entry.getValue();
			String str = (value != null ? value : "");
			try {
				str = URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			url.append(entry.getKey()).append("=").append(str).append(URL_PARAM_CONNECT_FLAG);
		}
		// 最后一个键值对后面的“&”需要去掉。
		String strURL = "";
		strURL = url.toString();
		if (URL_PARAM_CONNECT_FLAG.equals("" + strURL.charAt(strURL.length() - 1))) {
			strURL = strURL.substring(0, strURL.length() - 1);
		}
		return (strURL);
	}
	/**
	 * 往渠道发送数据
	 * @param url 通讯地址
	 * @param map 发送参数
	 * @return 应答消息
	 */
	public static String sendMsg(String url, Map<String, String> map) {
		try {
			HttpClient http = new HttpSSLClient(url, "60000");
			http.setRequestMethod("POST");
			http.connect();
			// 转换参数格式
			String webForm = getWebForm(map);
			http.send(webForm.getBytes());
			byte[] rspMsg = http.getRcvData();
			String msg = new String(rspMsg, "utf-8");
			LogFactory.getLog().info(Constants.class, msg);
			return msg;
		} catch (Exception e) {
			LogFactory.getLog().error(e, e.getMessage());
		}
		return null;
	}
	/**
	 * 获得一个UUID
	 * @return String UUID
	 */
	public static String getUUID() {
		String s = UUID.randomUUID().toString();
		return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)
				+ s.substring(19, 23) + s.substring(24);
	}
	/**
	 * String转BigDecimal
	 * @param str
	 * @return
	 */
	public static BigDecimal getStrToBigDecimal(String str){
		BigDecimal bigDecimal = new BigDecimal(str);
		return bigDecimal; 
	}
	/**
	 * 参数规则排序
	 * @param map
	 */
	public static String getBuildPayParams(Map<String,Object> map){
		List<String> keys = new ArrayList<String>(map.keySet());
		Collections.sort(keys);
		String s = "";
		for(String key : keys){
			if(map.get(key) != null && !map.get(key).equals("")){
				if(!key.equals("sign")&&!key.equals("serverCert")&&!key.equals("serverSign")){
					s += key + "=" + map.get(key) + "&";
				}
			}
        }
		return s.substring(0, s.length()-1);
	}
	/**
	 * 发送异步通知给商户
	 * @param 目前机制只发送一次
	 */
	public static void getSendNotifyUrl(String notifyUrl,Map<String,Object> map){
		HttpRequestParam http = new HttpRequestParam();
		http.setUrl(notifyUrl);
		Map<String,String> heads = Maps.newHashMap();
		heads.put("Content-Type", "application/json;charset=UTF-8");
		http.setContext(JsonUtil.getMapToJson(map));
		logger.info("异步通知发送商户的报文："+JsonUtil.getMapToJson(map));
		http.setHeads(heads);
		HttpResponser resp=HttpHelp.postParamByHttpClient(http);
		logger.info(map.get("order_no")+"异步通知发送商户结果："+resp.getContent());
	}
	/**
	 * 判断是否为json
	 * @param json
	 * @return
	 */
	public static boolean isGoodJson(String json) {    
	   try {    
	       new JsonParser().parse(json);  
	       return true;    
	   } catch (JsonParseException e) {    
		   logger.info("bad json: " + json);    
	       return false;    
	   }    
	}
	
	public static List<Map<String,String>> getMerchList(String date,String statementType){
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String,String> merchMap1 = new HashMap<String,String>();
		merchMap1.put("userName", Constants.getReadProperties("ccb", "ccb.wechat.active.passive.merId"));
		merchMap1.put("userPwd", Constants.getReadProperties("ccb", "ccb.wechat.active.passive.checkApi.key"));
		merchMap1.put("date", date);
		merchMap1.put("statementType", statementType);
		Map<String,String> merchMap2 = new HashMap<String,String>();
		merchMap2.put("userName", Constants.getReadProperties("ccb", "ccb.wechat.public.merId"));
		merchMap2.put("userPwd", Constants.getReadProperties("ccb", "ccb.wechat.public.checkApi.key"));
		merchMap2.put("date", date);
		merchMap2.put("statementType", statementType);
		Map<String,String> merchMap3 = new HashMap<String,String>();
		merchMap3.put("userName", Constants.getReadProperties("ccb", "ccb.alipay.merId"));
		merchMap3.put("userPwd", Constants.getReadProperties("ccb", "ccb.alipay.checkApi.key"));
		merchMap3.put("date", date);
		merchMap3.put("statementType", statementType);
		list.add(merchMap1);
		list.add(merchMap2);
		list.add(merchMap3);
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
	/** 
     * 解析zip文件中的csv文件 
     * @param strZipFile
     *        生成的zip文件路径
     * @param strCsvFileName 
     *        zip文件保存的csv文件的名字
     * @return List 
     * @throws IOException 
     */  
    public static List<Map<String,String>> getCsvFileList(String strZipFile,String strCsvFileName) throws IOException{  
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
			             	txtMap.put("trans_date", item[0]);//交易时间
			             	txtMap.put("mer_order_no", item[6]);//商户订单号
			             	txtMap.put("cc_order_no", item[7]);//中信订单号
			             	txtMap.put("cc_old_order_no", item[8]);//原交易订单号
			             	txtMap.put("trans_amount", item[14]);//交易总金额
			             	txtMap.put("trans_status", item[13]);//交易状态
			             	txtMap.put("refund_amount", item[15]);//退款总金额
			             	txtMap.put("refund_status", item[16]);//退款状态
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
     * 中信示例方法
     */
    /**
	 * http post
	 * @param requrl 请求地址
	 * @param savePath 保存文件地址
	 * @param req 请求json数据
	 * @param connTimeOut
	 * @param readTimeOut
	 * @return
	 */
	public static String postReq(String requrl,String savePath,String req,int connTimeOut,int readTimeOut){
		try {
			HttpURLConnection conn = null;
			try {
				URL url = new URL(requrl);
				conn = (HttpURLConnection)url.openConnection();
				conn.setDoInput(true);
				conn.setDoOutput(true);	//POST
				conn.setRequestMethod("POST");
				conn.setUseCaches(false);
				conn.setConnectTimeout(connTimeOut);
				conn.setReadTimeout(readTimeOut);
				conn.connect();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(),"utf-8");
			out.write(req);
			out.flush();
			out.close();
			int respCode=conn.getResponseCode();
			System.out.println("respCode:"+respCode);
			if (respCode==200){
				BufferedInputStream in=new BufferedInputStream(conn.getInputStream());
		        try {
		         File f = new File(savePath);
//		         if (f.exists()) {
//		          System.out.println("文件存在");
//		         } else {
//		          System.out.println("文件不存在，正在创建...");
//		          if (f.createNewFile()) {
//		           System.out.println("文件创建成功！");
//		          } else {
//		           System.out.println("文件创建失败！");
//		          }
//		         }
	                File outFile=new File(savePath);
	                if(!outFile.exists()){
	                    outFile.createNewFile();
	                }
	                BufferedOutputStream bufferedOutputStream=new BufferedOutputStream(new FileOutputStream(savePath));
	                byte[] line=new byte[200];
	                while(true){
	                    int len=in.read(line);
	                    if(len==-1){
	                        break;
	                    }
	                    bufferedOutputStream.write(line,0,len);
	                }
	                bufferedOutputStream.close();
		         /*
		         FileWriter ff = new FileWriter(f);
		         java.io.FileOutputStream writerStream = new java.io.FileOutputStream(savePath);    
		         utput = new BufferedWriter(new java.io.OutputStreamWriter(writerStream, "GBK")); 
					StringBuilder sb = new StringBuilder();
					char[] buff = new char[2048];
					int cnt = 0;
					while((cnt = in.read(buff))!=-1)
						utput.write(buff,0,cnt);
					in.close();
		         utput.close();*/
		         return savePath;
		        } catch (Exception e) {
		         e.printStackTrace();
		         return null;
		        }
			}else {
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
				StringBuilder sb = new StringBuilder();
				char[] buff = new char[2048];
				int cnt = 0;
				while((cnt = in.read(buff))!=-1)
					sb.append(buff,0,cnt);
				in.close();
				String rtStr = sb.toString();
				return rtStr;
			}
		} catch (IOException e) {
			System.out.println(e);
			throw new RuntimeException(e);
		}
	}
}
