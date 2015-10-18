package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import main.Node;

public class CorpusUtil {

	private static CorpusUtil instance = null;

	private HashSet<String> oxfordWordsSet;
	
	/**
	 * 初始概率map
	 */
	private HashMap<String, Double> initProbMap;
	/**
	 * 转移概率map
	 */
	private HashMap<String, Double> tranProbMap;
	/**
	 * 发射概率map
	 */
	private HashMap<String, Double> emitProbMap;
	/**
	 * 计算混淆词集
	 */
	HashMap<String, String[]> confusingMap;
	/**
	 * 候选集合 
	 */
	private HashMap<String, ArrayList<Node>> candidateMap;

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
		initProbMap = new HashMap<String, Double>();
		tranProbMap = new HashMap<String, Double>();
		emitProbMap = new HashMap<String, Double>();
		confusingMap = new HashMap<String, String[]>();
		candidateMap = new HashMap<String, ArrayList<Node>>();

		loadOxfordWords();
		loadInitProb();
		loadTranProb();
//		loadEmitProb();
		loadConfusingWord();
		loadCandidateList();
	}

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
	
	private void loadInitProb(){
		try {
			File file = new File(C.PATH_INIT_PROB);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while ((buff = reader.readLine()) != null) {
				String[] lineArr = buff.split("\t");
				if (lineArr.length < 2) {
					continue;
				}
				String key = lineArr[0] ;
				double prob = Double.parseDouble(lineArr[1]);
				initProbMap.put(key, prob);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadTranProb(){
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
				tranProbMap.put(key, prob);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadEmitProb(){
		try {
			File file = new File(C.PATH_EMIT_PROB);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while ((buff = reader.readLine()) != null) {
				String[] lineArr = buff.split("\t");
				if (lineArr.length < 3) {
					continue;
				}
				String key = lineArr[0] + "|" + lineArr[1];
				double prob = Double.parseDouble(lineArr[2]);
				emitProbMap.put(key, prob);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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

	public HashSet<String> getOxfordWordsSet() {
		return oxfordWordsSet;
	}


	/**
	 * 根据编辑距离生成真词纠错的候选集合， 并写到文件中
	 * 
	 * @param words
	 */
	private void createCandidateList() {
		try {
			PrintWriter writer = new PrintWriter(new File(C.PATH_CANDIDATE_LIST));
			
			int count = 0;
			for (Iterator<String> iIter = this.oxfordWordsSet.iterator(); iIter.hasNext();) {
				count ++;
				String iWord = iIter.next();
				ArrayList<Node> tmpList = calcCandidateWords(iWord);
				
				int size = tmpList.size() > 10 ? 10 : tmpList.size();
				String tmpStr = iWord + " ";
				for (int i = 0; i < size; i++) {
					tmpStr += tmpList.get(i).word + "," + tmpList.get(i).distance + " ";
				}
				tmpStr = tmpStr.substring(0, tmpStr.length() - 1); // remove the last '\t'
//				System.out.println(tmpStr);
//				break;
				writer.println(tmpStr);
				if (count % 100 == 0) {
					System.out.println("正在处理第：" + count);
				}
			}
			
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 计算word
	 * @param iWord
	 * @return
	 */
	private ArrayList<Node> calcCandidateWords(String iWord){
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
		String[] confusingWords = confusingMap.get(iWord);
		if (confusingWords != null) {
			for (String confusingWord : confusingWords) {
				Node node = new Node(confusingWord, 0.1);
				if (!tmpList.contains(node)) {
					tmpList.add(node);
				}
			}
		}
		return tmpList;
	}
	
	
	public HashMap<String, Double> getInitProbMap() {
		return initProbMap;
	}

	public HashMap<String, Double> getTranProbMap() {
		return tranProbMap;
	}

	public HashMap<String, Double> getEmitProbMap() {
		return emitProbMap;
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
//		if (candidateMap.containsKey(word)) {
//			tmpList = candidateMap.get(word);
//			//由于生产一次candidate_set耗时太长，测试时就加上下面的代码
//			String[] confusingWords = confusingMap.get(word);
//			if (confusingWords != null) {
////				tmpList.clear();
//				for (String confusingWord : confusingWords) {
//					Node node = new Node(confusingWord, 0.1);
//					if (!tmpList.contains(node)) {
//						tmpList.add(node);
//					}
//				}
//			}
//			return tmpList;
//		}
		//在线计算
		tmpList = calcCandidateWords(word);
		return tmpList;
	}
	
	
	
	public static void main(String[] args) {
		System.out.println("start:");
		long start = System.currentTimeMillis();
		CorpusUtil corpusUtil = CorpusUtil.getInstance();
		
//		corpusUtil.loadConfusingWord();
		
//		corpusUtil.createCandidateList();
		
//		ArrayList<Node> list = corpusUtil.calcCandidateWords("are");
		ArrayList<Node> list = corpusUtil.getCandidateList("likes");
		for (Node node : list) {
			System.out.println(node);
		}

		long end = System.currentTimeMillis();
		long delay = (end - start);
		System.out.println("delay: " + delay + "ms");

	}

}
