package com.zhl.ccb.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {
	public static Map<String, Object> getJsonToMap(String jsonStr){
	    Map<String, Object> map = new LinkedHashMap<String, Object>();
	    //最外层解析
	    JSONObject json = JSONObject.fromObject(jsonStr);
	    for(Object k : json.keySet()){
			Object v = json.get(k); 
			//如果内层还是数组的话，继续解析
			if(v instanceof JSONArray){
				List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
				Iterator<JSONObject> it = ((JSONArray)v).iterator();
				while(it.hasNext()){
					JSONObject json2 = it.next();
					list.add(getJsonToMap(json2.toString()));
				}
				map.put(k.toString(), list);
			} else {
				map.put(k.toString(), v);
			}
		}
	    return map;
	}
	public static Map<String,String>  getJsonToMapStr(String jsonStr){
//		String str = jsonStr.substring(1, jsonStr.length()-1);
//		 String[] key =  str.split(",");
//		 String[] value =null;
		 Map<String,String> map = new LinkedHashMap<String,String>();
//		 for(int i= 0;i<key.length; i++){
//			 String key_=key[i].toString().replace("\"", "");
//			 value = key_.split(":");
//			 map.put(value[0], value[1]);
//		 }
		// 将json字符串转换成jsonObject  
       JSONObject jsonObject = JSONObject.fromObject(jsonStr); 
       Iterator it = jsonObject.keys();
    // 遍历jsonObject数据，添加到Map对象  
       while (it.hasNext())  
       {  
           String key = String.valueOf(it.next());  
           String value = (String) jsonObject.get(key);  
           map.put(key, value);  
       }  
		return map;
	}
	public static String getMapToJson(Map<String,Object> map){
//		Gson g = new Gson();
//		String json= g.toJson(map);
		GsonBuilder gb =new GsonBuilder();
		gb.disableHtmlEscaping();
		String json = gb.create().toJson(map);
		return json;
	}
	public static String getMapToJsonStr(Map<String,String> map){
		Gson g = new Gson();
		String json= g.toJson(map);
		return json;
	}
	/**
     * 构建返回_报文头
     */
    public static Map<String,Object> getReturnMessageHead(String tranNo,String code,String message){
    	Map<String,Object> map = new HashMap<String, Object>();
    	map.put("tranNo", tranNo);
    	map.put("code", code);
    	map.put("message", message);
    	return map;
    }
    /**
     * 构建NFC返回_报文头
     */
    public static Map<String,Object> getReturnNFCMessageHead(String order_no,String result_code,String message){
    	Map<String,Object> map = new HashMap<String, Object>();
    	map.put("order_no", order_no);
    	map.put("result_code", result_code);
    	map.put("message", message);
    	return map;
    }
    public static void main(String[] args) {
//    	Map<String,String> map = new HashMap<String, String>();
//    	map.put("tranNo", "111");
//    	map.put("code", "222");
//    	map.put("message", "333");
//    	String json = JsonUtil.getMapToJsonStr(map);
//    	System.out.println(json);
    	
    	
    	String json = "{\"result_code\":\"0000\",\"result_msg\":\"\",\"subContractId\":\"5841\"}";
    	Map<String,Object> m = JsonUtil.getJsonToMap(json);
    	//System.out.println(m.get("result_msg"));
    	System.out.println(m.get("result_code"));
    	
	}
}
