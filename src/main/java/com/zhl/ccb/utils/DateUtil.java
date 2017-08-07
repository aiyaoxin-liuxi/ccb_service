package com.zhl.ccb.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class DateUtil {
	
	private static final Logger logger = Logger.getLogger(DateUtil.class);
	public static String format(Date date){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}
	
	public static String getDayForYYMMDD(){
		SimpleDateFormat time = new SimpleDateFormat("yyyyMMdd");
		time.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return time.format(new Date());
	}
	public static String getDayForYYMMDD(Date date){
		if(date==null){
			return null;
		}
		SimpleDateFormat time = new SimpleDateFormat("yyyyMMdd");
		time.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return time.format(date);
	}
	public static String getTimeForHHmmss(Date date){
		if(date==null){
			return null;
		}
		SimpleDateFormat time = new SimpleDateFormat("HHmmss");
		time.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return time.format(date);
	}
	public static Date strToDate(String strDate) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			//解析此类数据：2001-12-17T09:30:47.0Z
			if(strDate == null || "".equals(strDate)){
				return new Date();
			}
			if(strDate.contains("T")){
				strDate = strDate.replace("T", " ").substring(0,19);
			}
			Date strtodate = format.parse(strDate);
			return strtodate;
		} catch (Exception e) {
			logger.error("Input strDate:"+strDate);
			throw new Exception();
		}
	}

	/**
	 * 得到一个时间延后或前移几天的时间,nowdate为时间,delay为前移或后延的分钟数
	 */
	public static String getDatePlus(String nowdate, int delay) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			String mdate = "";
			Date d = strToDate(nowdate);
			long myTime = (d.getTime() / 1000) + delay * 60;
			d.setTime(myTime * 1000);
			mdate = format.format(d);
			return mdate;
		} catch (Exception e) {
			logger.error("PlusDate failed,nowdate:"+nowdate);
			return "";
		}
	}
	 /** 
	  * 将短时间格式时间转换为字符串 yyyy-MM-dd 
	  * 
	  * @param dateDate 
	  * @param k 
	  * @return 
	  */  
	 public static String dateToStr(java.util.Date dateDate) {  
		 SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		 String dateString = format.format(dateDate);  
		 return dateString;  
	 }
	 
	 public static boolean isLessMinutes(Date date,int minutes){
		 Date now = new Date();
		 if(now.after(date)){
			 DateTime dateTime = new DateTime(now);
			 Date lessMinutes= dateTime.minusMinutes(minutes).toDate();
			 if(date.after(lessMinutes)){
				 return true;
			 }
		 }
		 return false;
	 }
	 public static String formatYYYYMMDDHHMMSS(Date date){
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			return format.format(date);
		}
	public static void main(String[] args) throws Exception {
		//System.out.println(DateUtil.dateToStr(DateUtil.strToDate("2014-09-14T10:34:00")));
		Date beginDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(beginDate);
		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
		String date = DateUtil.getDayForYYMMDD(calendar.getTime());
		System.out.println(date);
	}
}
