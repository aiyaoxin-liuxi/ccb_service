package com.zhl.ccb.service.Impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zhl.ccb.dao.NfcOrderWaterMapper;
import com.zhl.ccb.dao.NfcTransBillWaterMapper;
import com.zhl.ccb.model.NfcOrderWater;
import com.zhl.ccb.model.NfcTransBillWater;
import com.zhl.ccb.service.CcbService;
import com.zhl.ccb.service.CcbTransBillService;
import com.zhl.ccb.utils.Constants;
import com.zhl.ccb.utils.DateUtil;
import com.zhl.ccb.utils.JsonUtil;
import com.zhl.ccb.utils.MemcacheManager;

@Service("ccbTransBillService")
@Transactional
public class CcbTransBillServiceImpl implements CcbTransBillService{
	@Autowired
	private CcbService ccbService;
	@Autowired
	private NfcOrderWaterMapper nfcOrderWaterMapper;
	@Autowired
	private NfcTransBillWaterMapper nfcTransBillWaterMapper; 
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd");
	private static final Log logger = LogFactory.getLog(CcbTransBillServiceImpl.class);
	/**
	 * 定时和对账重导 公用方法
	 */
	public String status(String dateTime) {
		logger.info("==============================进入NFC中信银行对账下载定时器==============================");
		logger.info("dateTime="+dateTime);
		String returnString = "";
		String date = "";
		String mer_order_no = "";
		try{
			Date beginDate = new Date();
			if(dateTime.equals("quartz")){//说明这是定时任务执行传入的
				//计算需要查询的日期
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(beginDate);
				calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
				date = DateUtil.getDayForYYMMDD(calendar.getTime());
				logger.info("date="+date);
			}else{//人工调用传入
				date = dateTime; 
				logger.info("date="+date);
				//预防人工重导数据重复，先删除再插入
				nfcTransBillWaterMapper.deleteTransBillWater(sdf.parse(date));
			}
			/**
			 * 下载对账单(中信银行多商户号循环下载)
			 */
			List<Map<String,String>> merchList = Constants.getMerchList(date, Constants.statement_type_0);
			
			int no_transBill = 0;
			for(Map<String,String> mapParam : merchList){
				//组装json,准备下载数据
				String json = ccbService.ccbTransBill(mapParam);
				logger.info("(中信银行对账下载接口：),通道请求参数："+json);
				//生成的压缩包地址
				String strZipFile = Constants.getReadProperties("ccb","ccb.linux.checkUrl")+mapParam.get("date")+"_"+mapParam.get("userName")+".zip";
				String retJson = Constants.postReq(Constants.getReadProperties("ccb","ccb.check.url"), strZipFile, json, 120000, 120000);
				if(Constants.isGoodJson(retJson) == true){//返回为Json,说明下载失败
					logger.info("(中信银行对账下载接口：),通道返回结果："+retJson+",对账数据不是字符数据流，无对账数据！！！");
					Map<String,String> jsonMap = JsonUtil.getJsonToMapStr(retJson);
					no_transBill++;
				}else{//返回的为zip地址
					logger.info("(中信银行对账下载接口：),下载对账文件成功！");
					List<Map<String,String>> listMap = Constants.getCsvFileList(retJson, "");
					for(Map<String,String> map : listMap){
						//存入缓存
						mer_order_no = map.get("mer_order_no");
						boolean b = MemcacheManager.cachedClient.add(mer_order_no,map,new Date(System.currentTimeMillis()+6000));//设置60秒过期 
						if(b == true){//数据在缓存中未存在时，插入数据
							//取出缓存
							Map<String,String> cacheMap = (Map<String,String>)MemcacheManager.cachedClient.get(mer_order_no);
							if(cacheMap != null){
								logger.info("(中信银行对账下载接口：),缓存订单："+mer_order_no+"存在，进行导入！！！");
								//以中信对账文件中的商户订单号为准
						    	NfcTransBillWater transBillWater = new NfcTransBillWater();
								transBillWater.setId(Constants.getUUID());
								transBillWater.setOrderNo(null);
								transBillWater.setMerOrderNo(cacheMap.get("mer_order_no"));
								transBillWater.setMerchChannel("ccb");
								if(cacheMap.get("trans_amount") != null && !"".equals(cacheMap.get("trans_amount"))){//交易总金额不为空，暂时认为是支付交易
									transBillWater.setTotalFee(BigDecimal.valueOf(Double.parseDouble(cacheMap.get("trans_amount"))));
								}else{//认为是退货交易
									transBillWater.setTotalFee(BigDecimal.valueOf(Double.parseDouble(cacheMap.get("refund_amount"))));
								}
								transBillWater.setCreatedTime(sdf_.parse(cacheMap.get("trans_date")));
								transBillWater.setStatus("SUCCESS");
					        	//插入对账数据
								nfcTransBillWaterMapper.insertSelective(transBillWater);
							}
						}else{
							logger.info("(中信银行对账下载接口：),缓存订单："+mer_order_no+"已存在，不再放入缓存中,不再进行插入数据！！！");
						}
					}
					logger.info("(中信银行对账下载接口：),通用对账表插入数据成功!!!");
					//上游对账数据比对NFC交易流水数据
					List<NfcOrderWater> nfcOrderWaterList = nfcOrderWaterMapper.selRfOrderWater(date,"ccb", "1");
					System.out.println("nfcOrderWaterList="+nfcOrderWaterList.size());
					List<NfcTransBillWater> nfcTransBillWaterList = nfcTransBillWaterMapper.selectCcbTransBillWater(sdf.parse(date), "ccb", "SUCCESS");
					System.out.println("nfcTransBillWaterList="+nfcTransBillWaterList.size());
					for(NfcTransBillWater tbw : nfcTransBillWaterList){
						for(NfcOrderWater ow : nfcOrderWaterList){
							if(tbw.getMerOrderNo().equals(ow.getMerOrderNo())){
								//更新通用对账数据表
								tbw.setOrderNo(ow.getOrderNo());
								nfcTransBillWaterMapper.updateByPrimaryKeySelective(tbw);
							}
						}
					}
					logger.info("(融服支付对账查询接口：),通用对账表对比更新数据成功!!!");
					returnString = "{\"result_code\":\"1\",\"message\":\"导入成功\"}";
				}
			}
			if(no_transBill >= 3){
				returnString = "{\"result_code\":\"9\",\"message\":\"没有交易数据\"}";
			}
		}catch(Exception e){
			e.printStackTrace();
			logger.error("(中信银行对账下载接口：),对账失败");
			returnString = "{\"result_code\":\"9\",\"message\":\"对账失败\"}";
		}
		return returnString;
	}
}
