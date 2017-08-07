package com.zhl.ccb.utils;

import java.util.Date;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

public class MemcacheManager {
	
	// 构建缓存客户端  
    public static MemCachedClient cachedClient;  
    // 单例模式实现客户端管理类  
    public static MemcacheManager INSTANCE = new MemcacheManager();  
    private MemcacheManager() {  
        cachedClient = new MemCachedClient();  
        // 初始化SockIOPool，管理memcached的连接池  
        SockIOPool pool = SockIOPool.getInstance();  
        // 设置缓存服务器列表，当使用分布式缓存的时，可以指定多个缓存服务器。（这里应该设置为多个不同的服务器）  
        String[] servers = {"192.168.20.63:11211"
        // 测试环境不需要连接VPN 119.90.53.150:11211		
        // "192.168.163.10:11211", "192.168.163.11:11211"		
        // 也可以使用域名 "server3.mydomain.com:1624"  
        };  
        pool.setServers(servers);  
        pool.setFailover(true);  
        pool.setInitConn(10); // 设置初始连接  
        pool.setMinConn(5);// 设置最小连接  
        pool.setMaxConn(250); // 设置最大连接  
        pool.setMaxIdle(1000 * 60 * 60 * 3); // 设置每个连接最大空闲时间3个小时  
        pool.setMaintSleep(30);  
        pool.setNagle(false);  
        pool.setSocketTO(3000);  
        pool.setAliveCheck(true);  
        pool.initialize();  
    }
    /** 
     * 获取缓存管理器唯一实例 
     *  
     * @return 
     */  
    public static MemcacheManager getInstance() {  
        return INSTANCE;  
    }  
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//将对象加入到memcached缓存  
		cachedClient.add("keke","哈哈123",new Date(System.currentTimeMillis()+6000));//设置60秒过期 
		//cachedClient.set("aaabbb", "This is a test String12333");
        //从memcached缓存中按key值取对象  
        String result  = (String) cachedClient.get("keke");  
        System.out.println(result); 
	}

}
