package com.zhl.ccb.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

public class XmlUtil {
//	public static String getMapToXml(Map<String,String> map){
//		StringBuffer sb = new StringBuffer();
//		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//		Set es = map.entrySet();
//		Iterator it = es.iterator();
//        while (it.hasNext()) {
//            Map.Entry entry = (Map.Entry)it.next();
//            String k = (String)entry.getKey();
//            String v = (String)entry.getValue();
//            if (null != v && !"".equals(v) && !"appkey".equals(k)) {
//                sb.append("<" + k + ">" + map.get(k) + "</" + k + ">\n");
//            }
//        }
//        sb.append("</xml>");
//        return sb.toString();
//	}
	/**
	 * 将Map转成Xml
	 * @param map
	 * @return
	 */
	public static String getMapToXml(Map<String,Object> map){
		System.out.println("将Map转成Xml, Map：" + map.toString());  
        StringBuffer sb = new StringBuffer();  
//        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");  
        sb.append("<xml>");
        mapToXML(map, sb);  
        System.out.println("将Map转成Xml, Xml：" + sb.toString()); 
        sb.append("</xml>");
        try {  
            return sb.toString();  
        } catch (Exception e) {  
        	System.out.println(e);  
        }  
		return null;
	}
	private static void mapToXML(Map map, StringBuffer sb){
		 Set set = map.keySet();  
        for (Iterator it = set.iterator(); it.hasNext();) {  
            String key = (String) it.next();  
            Object value = map.get(key);  
            if (null == value)  
                value = "";  
            if (value.getClass().getName().equals("java.util.ArrayList")) {  
                ArrayList list = (ArrayList) map.get(key);  
                sb.append("<" + key + ">");  
                for (int i = 0; i < list.size(); i++) {  
                    HashMap hm = (HashMap) list.get(i);  
                    mapToXML(hm, sb);  
                }  
                sb.append("</" + key + ">");  
            } else {  
                if (value instanceof HashMap) {  
                    sb.append("<" + key + ">");  
                    mapToXML((HashMap) value, sb);  
                    sb.append("</" + key + ">");  
                } else {  
                    sb.append("<" + key + ">" + value + "</" + key + ">");  
                }  
            }  
        }  
	}  
	
	/**
	 * xml转换成map
	 * @param element
	 * @return
	 */
	public static Map<String,Object> getXmlToMap(String xml){
		Map<String, Object> map = null;
		try {
			Document doc = DocumentHelper.parseText(xml);
			map = (Map<String, Object>) xml2map(doc.getRootElement());
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	public static Object xml2map(Element element) {
	    System.out.println(element);
	    Map<String, Object> map = new HashMap<String, Object>();
	    List<Element> elements = element.elements();
	    if (elements.size() == 0) {
	      map.put(element.getName(), element.getText());
	      if (!element.isRootElement()) {
	        return element.getText();
	      }
	    } else if (elements.size() == 1) {
	      map.put(elements.get(0).getName(), xml2map(elements.get(0)));
	    } else if (elements.size() > 1) {
	      // 多个子节点的话就得考虑list的情况了，比如多个子节点有节点名称相同的
	      // 构造一个map用来去重
	      Map<String, Element> tempMap = new HashMap<String, Element>();
	      for (Element ele : elements) {
	        tempMap.put(ele.getName(), ele);
	      }
	      Set<String> keySet = tempMap.keySet();
	      for (String string : keySet) {
	        Namespace namespace = tempMap.get(string).getNamespace();
	        List<Element> elements2 = element.elements(new QName(string, namespace));
	        // 如果同名的数目大于1则表示要构建list
	        if (elements2.size() > 1) {
	          List<Object> list = new ArrayList<Object>();
	          for (Element ele : elements2) {
	            list.add(xml2map(ele));
	          }
	          map.put(string, list);
	        } else {
	          // 同名的数量不大于1则直接递归去
	          map.put(string, xml2map(elements2.get(0)));
	        }
	      }
	    }
	    return map;
	  }
    
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Map<String,Object> map = new LinkedHashMap<String,Object>();
		map.put("name", "aaa");
		map.put("age", "23");
		map.put("address", "bbb");
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Map<String,Object> map1 = new LinkedHashMap<String, Object>();
		Map<String,Object> map11 = new LinkedHashMap<String, Object>();
		map1.put("name1", "name1");
		map1.put("age1", "23");
		map1.put("address1", "bbb1");
		map11.put("listlist", map1);
		list.add(map11);
		Map<String,Object> map2 = new LinkedHashMap<String, Object>();
		Map<String,Object> map22 = new LinkedHashMap<String, Object>();
		map2.put("name2", "name2");
		map2.put("age2", "23");
		map2.put("address2", "bbb2");
		map22.put("listlist", map2);
		list.add(map22);
		map.put("list", list);
		System.out.println(getMapToXml(map));
	}

}
