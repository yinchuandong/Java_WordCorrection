package com.yin.spell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import redis.clients.jedis.Jedis;

public class RedisUtil {

	static Jedis redis = null;

	private RedisUtil() {

	}

	public static Jedis getInstance() {
		return getInstance("127.0.0.1", 6379);
	}

	public static Jedis getInstance(String ip, int port) {
		if (redis == null) {
			redis = new Jedis(ip, port);
		}
		return redis;
	}

	public static void close() {
		if (redis != null) {
			redis.disconnect();
			redis = null;
		}
	}

	/**
	 * 加载初始概率
	 */
	private static void loadInitProb() {
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
		RedisUtil.getInstance();
		RedisUtil.loadInitProb();
		RedisUtil.loadTranProb();
		
		long end = System.currentTimeMillis();
		long delay = (end - start);
		System.out.println("delay: " + delay + "ms");

	}
	
}
