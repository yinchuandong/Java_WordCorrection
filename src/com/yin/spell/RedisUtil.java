package com.yin.spell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

	 //Redis服务器IP
    private static String ADDR = "127.0.0.1";
    
    //Redis的端口号
    private static int PORT = 6379;
    
  //访问密码
    private static String AUTH = "admin";
	
	//如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
    private static int MAX_ACTIVE = 1024;
    
    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static int MAX_IDLE = 200;
    
    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    private static int MAX_WAIT = 10000;
    
    private static int TIMEOUT = 10000;
    
    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static boolean TEST_ON_BORROW = true;
    
    private static JedisPool jedisPool = null;
    

	private RedisUtil() {

	}
	
	public static JedisPool getPool(){
		if (jedisPool == null){
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxActive(MAX_ACTIVE);
            config.setMaxIdle(MAX_IDLE);
            config.setTestOnBorrow(TEST_ON_BORROW);
            jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT);
		}
		return jedisPool;
	}

	/** 
     * 返还到连接池 
     *  
     * @param pool  
     * @param redis 
     */  
    public static void returnResource(JedisPool pool, Jedis redis) {  
        if (redis != null) {  
            pool.returnResource(redis);  
        }  
    }  
    /** 
     * 返还到连接池 
     *  
     * @param pool  
     * @param redis 
     */  
    public static void returnResource(Jedis redis) {  
    	returnResource(jedisPool, redis);
    }  
	
	public static Jedis getInstance() {
        Jedis redis = null;
        try {
        	if(jedisPool == null){
        		jedisPool = getPool();
        	}
        	redis = jedisPool.getResource();
		} catch (Exception e) {
			jedisPool.returnBrokenResource(redis);
			e.printStackTrace();
		}
        return redis;
	}


	/**
	 * 加载初始概率
	 */
	private static void loadInitProb() {
		Jedis redis = getInstance();
		try {
			File file = new File(C.PATH_INIT_PROB);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while ((buff = reader.readLine()) != null) {
				String[] lineArr = buff.split("\t");
				if (lineArr.length < 2) {
					continue;
				}
				String key = lineArr[0];
				double prob = Double.parseDouble(lineArr[1]);
				redis.set("init_prob:" + key, prob + "");
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 加载转移概率
	 */
	private static void loadTranProb() {
		Jedis redis = getInstance();
		try {
			File file = new File(C.PATH_TRAN_PROB);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while ((buff = reader.readLine()) != null) {
				String[] lineArr = buff.split("\t");
				if (lineArr.length < 3) {
					continue;
				}
				String key = lineArr[0] + "|" + lineArr[1];
				double prob = Double.parseDouble(lineArr[2]);
				redis.set("tran_prob:" + key, prob + "");
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("start:");
		long start = System.currentTimeMillis();
		
		//将要用的数据加载到数据库
		RedisUtil.loadInitProb();
		RedisUtil.loadTranProb();
		
		long end = System.currentTimeMillis();
		long delay = (end - start);
		System.out.println("delay: " + delay + "ms");

	}
	
}
