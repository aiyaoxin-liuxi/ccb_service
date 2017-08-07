//package test;
//
//import java.util.Date;
//import java.util.List;
//
//import org.apache.log4j.Logger;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import com.alibaba.fastjson.JSON;
//import com.zhl.ccb.dao.NfcOrderWaterMapper;
//import com.zhl.ccb.model.NfcOrderWater;
//import com.zhl.ccb.service.Impl.CcbTestService;
//
//@RunWith(SpringJUnit4ClassRunner.class)  
//@ContextConfiguration(locations = { "classpath:spring.xml",  
//        "classpath:spring-mybatis.xml" })  
//public class JunitTest {
//	private static final Logger LOGGER = Logger.getLogger(JunitTest.class);  
//	
//	@Autowired  
//    private CcbTestService ccbTestService;
//	@Autowired
//	private NfcOrderWaterMapper nfcOrderWaterMapper;
//	
//	
////	@Test  
////    public void testQueryById() {  
////		NfcOrderWater nfcOrderWater = nfcOrderWaterService.getNfcOrderWaterById("44e60745c3cf4facaaa2d5a6cfdf963e");
////        LOGGER.info("!!!!!!!!!!!!!!!!!!!!====="+JSON.toJSON(nfcOrderWater));  
////    }  
//	
//	@Test  
//    public void testQueryAll() {  
////        List<NfcOrderWater> nfcOrderWaters = nfcOrderWaterService.getNfcOrderWaters();  
////        LOGGER.info("!!!!!!!!!!!!!!!!!!!!====="+JSON.toJSON(nfcOrderWaters)); 
//		 List<NfcOrderWater> nfcOrderWaters = nfcOrderWaterMapper.selectAll();
//		 LOGGER.info("!!!!!!!!!!!!!!!!!!!!====="+JSON.toJSON(nfcOrderWaters));
//    }  
//
////	@Test  
////    public void testInsert() {  
////		NfcOrderWater nfcOrderWater = new NfcOrderWater();  
////		nfcOrderWater.setMerOrderNo("");  
////		nfcOrderWater.setOrderNo("");  
////        int result = nfcOrderWaterService.insert(nfcOrderWater);  
////        System.out.println(result);  
////    } 
////	@Test
////	public void zhlNfcPassive(){
////		ccbTestService.zhlNfcPassive();
////	}
//}
