package com.yin.spell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redis.clients.jedis.Jedis;

public class CorpusUtil {

	private static CorpusUtil instance = null;
	
	private Jedis redis = null;
	
	private static double MIN_PROB = 0.0000000000000001;


	private HashSet<String> oxfordWordsSet;
	
	/**
	 * 计算混淆词集
	 */
	private HashMap<String, String[]> confusingMap;
	/**
	 * 候选集合 
	 */
	private HashMap<String, ArrayList<Node>> candidateMap;
	/**
	 * 词典的集合，包括词典的解释
	 */
	private HashMap<String, String> dictMap;
	

	private CorpusUtil() {
		init();
	}

	public static CorpusUtil getInstance() {
		if (instance == null) {
			instance = new CorpusUtil();
		}
		return instance;
	}

	private void init() {
		
		oxfordWordsSet = new HashSet<String>();
		confusingMap = new HashMap<String, String[]>();
		candidateMap = new HashMap<String, ArrayList<Node>>();
		dictMap = new HashMap<String, String>();

		loadOxfordWords();
		loadConfusingWord();
		loadCandidateList();
	}
	
	public void initRedis(){
		redis = RedisUtil.getInstance();
	}
	
	public void closeRedis(){
		RedisUtil.returnResource(redis);
	}
	
	

	/**
	 * 加载牛津词典，只有单词
	 */
	private void loadOxfordWords() {
		try {
			File file = new File(C.PATH_OXFORD_WORDS);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while ((buff = reader.readLine()) != null) {
				if (buff.equals("")) {
					continue;
				}
				oxfordWordsSet.add(buff);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	
	/**
	 * 加载易混淆词
	 */
	private void loadConfusingWord(){
		try {
			File file = new File(C.PATH_CONFUSING_WORD);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			ArrayList<String[]> list = new ArrayList<String[]>();
			while ((buff = reader.readLine()) != null) {
				if (buff.startsWith("#")) {
					continue;
				}
				String[] wordArr = buff.split(" ");
				list.add(wordArr);
			}
			reader.close();
			
			for (String[] wordArr : list) {
				for (String word : wordArr) {
					if (!confusingMap.containsKey(word)) {
						confusingMap.put(word, wordArr);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 加载候选集列表
	 */
	private void loadCandidateList() {
		try {
			File file = new File(C.PATH_CANDIDATE_LIST);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while ((buff = reader.readLine()) != null) {
				
				int index = buff.indexOf(" ");
				String key = buff.substring(0, index);
				String[] lineArr = buff.substring(index + 1).split(" ");
				ArrayList<Node> list = new ArrayList<Node>();
				for (String item : lineArr) {
					String[] arr = item.split(",");
					String word = arr[0];
					double distance = Double.parseDouble(arr[1]);
					Node node = new Node(word, distance);
					list.add(node);
				}
				candidateMap.put(key, list);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 加载词典，包含单词的翻译，需要加载，之后才能调用其相应的get方法
	 */
	public void loadDictMap() {
		try {
			// 加载词库
			BufferedReader reader = new BufferedReader(new FileReader(new File(C.PATH_DICT_MAP)));
			String buff = null;
			while ((buff = reader.readLine()) != null) {
				Pattern pattern = Pattern.compile("\"word\":\\s+\"(\\w+)\"");
				Matcher matcher = pattern.matcher(buff);
				if (!matcher.find()) {
					continue;
				}
				String word = matcher.group(1);
				dictMap.put(word, buff);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 首字母大写
	 * @param name
	 * @return
	 */
	public static String toUpperCaseFirstChar(String name) {
		char[] cs = name.toCharArray();
		if(Character.isUpperCase(cs[0])){
			return name;
		}
		cs[0] -= 32;
		return String.valueOf(cs);
	}
	
	
	/**
	 * 计算word
	 * @param iWord
	 * @return
	 */
	public ArrayList<Node> calcCandidateWords(String iWord){
		ArrayList<Node> tmpList = new ArrayList<Node>();
		EditDistanceUtil editUtil = EditDistanceUtil.getInstance();
		for (Iterator<String> jIter = this.oxfordWordsSet.iterator(); jIter.hasNext();) {
			String jWord = jIter.next();
			double distance = editUtil.calculate(iWord, jWord);
			if (distance > 1) {
				continue;
			}
			Node node = new Node(jWord, distance);
			tmpList.add(node);
		}
		Collections.sort(tmpList, new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				if (o1.distance < o2.distance) {
					return -1;
				} else if (o1.distance > o2.distance) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		int len = tmpList.size() > 10 ? 10 : tmpList.size();
		tmpList  = new ArrayList<Node>(tmpList.subList(0, len));
		return tmpList;
	}
	
	/**
	 * 获得牛津词典
	 * @return
	 */
	public HashSet<String> getOxfordWordsSet() {
		return oxfordWordsSet;
	}
	
	/**
	 * 获得初始概率
	 * @param key
	 * @return
	 */
	public double getInitProb(String key){
		key = "init_prob:" + key;
		if(!redis.exists(key)){
			return MIN_PROB;
		}else{
			return Double.parseDouble(redis.get(key));
		}
	}
	
	/**
	 * 获得转移概率
	 * @param key
	 * @return
	 */
	public double getTranProb(String key){
		key = "tran_prob:" + key;
		if(!redis.exists(key)){
			return MIN_PROB;
		}else{
			return Double.parseDouble(redis.get(key));
		}
	}
	

	public HashMap<String, ArrayList<Node>> getCandidateMap() {
		return candidateMap;
	}

	/**
	 * 获得word的候选集合，word也会被包含在里面
	 * @param word
	 * @return
	 */
	public ArrayList<Node> getCandidateList(String word){
		ArrayList<Node> tmpList = null;
		//从文件中直接读取
		if (candidateMap.containsKey(word)) {
			tmpList = candidateMap.get(word);
			//由于生产一次candidate_set耗时太长，测试时就加上下面的代码
			String[] confusingWords = confusingMap.get(word);
			if (confusingWords != null) {
				for (String confusingWord : confusingWords) {
					Node node = new Node(confusingWord, 0.1);
					if (!tmpList.contains(node)) {
						tmpList.add(node);
					}
				}
			}
		}else{
			//在线计算
			tmpList = calcCandidateWords(word);
		}
		//让候选集按照编辑距离排序
		Collections.sort(tmpList, new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				if(o1.distance < o2.distance){
					return -1;
				}else if(o1.distance > o2.distance){
					return 1;
				}else{
					return 0;
				}
				
			}
		});
		return tmpList;
	}
	
	/**
	 * 获得词典解释
	 * @return
	 */
	public HashMap<String, String> getDictMap() {
		return dictMap;
	}

	public static void main(String[] args) {
		System.out.println("start:");
		long start = System.currentTimeMillis();
		
		CorpusUtil corpusUtil = CorpusUtil.getInstance();
		System.out.println(corpusUtil.getInitProb("like"));
		System.out.println(corpusUtil.getTranProb("i|like"));
		System.out.println(corpusUtil.getTranProb("i|liked"));
		
//		ArrayList<Node> list = corpusUtil.calcCandidateWords("are");
//		ArrayList<Node> list = corpusUtil.getCandidateList("like");
//		for (Node node : list) {
//			System.out.println(node);
//		}
//		corpusUtil.loadDictMap();
		long end = System.currentTimeMillis();
		long delay = (end - start);
		System.out.println("delay: " + delay + "ms");

	}

}
