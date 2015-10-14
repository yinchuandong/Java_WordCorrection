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
	 * 每一项二元语法的map, eg:N(<BOS>|Brown)
	 */
	private HashMap<String, Integer> bigramMap;
	/**
	 * 二元语法的总数统计map, eg:N(<BOS>*)
	 */
	private HashMap<String, Integer> bigramSumMap;
	/**
	 * 候选集合 
	 */
	private HashMap<String, ArrayList<Node>> candidateList;

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
		bigramMap = new HashMap<String, Integer>();
		bigramSumMap = new HashMap<String, Integer>();
		candidateList = new HashMap<String, ArrayList<Node>>();

		loadOxfordWords();
		loadBigram();
//		loadCandidateList();
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

	private void loadBigram() {
		try {
			File file = new File(C.PATH_BIGRAM);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while ((buff = reader.readLine()) != null) {
				if (buff.equals("")) {
					continue;
				}
				String[] lineArr = buff.split("\t");
				if (lineArr.length < 3) {
					continue;
				}
				String key = lineArr[1] + "|" + lineArr[2];// key of bigramMap
				String keySum = lineArr[1];// key of bigramSumMap
				int count = Integer.parseInt(lineArr[0]);// word frequency

				bigramMap.put(key, count);
				if (!bigramSumMap.containsKey(keySum)) {
					bigramSumMap.put(keySum, count);
				} else {
					bigramSumMap.put(keySum, bigramSumMap.get(keySum) + count);
				}

			}
			reader.close();
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
			buff = reader.readLine();
//			buff = reader.readLine();
			System.out.println(buff);
//			while ((buff = reader.readLine()) != null) {
//				
//				int index = buff.indexOf("\t");
//				String key = buff.substring(0, index);
//				String[] lineArr = buff.substring(index + 1).split("\t");
//				System.out.println(key);
//				ArrayList<Node> list = new ArrayList<Node>();
//				for (String item : lineArr) {
//					String[] arr = item.split(",");
//					String word = arr[0];
//					int distance = Integer.parseInt(arr[1]);
//					Node node = new Node(word, distance);
//					list.add(node);
//				}
//				candidateList.put(key, list);
//			}
//			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashSet<String> getOxfordWordsSet() {
		return oxfordWordsSet;
	}

	public HashMap<String, Integer> getBigramMap() {
		return bigramMap;
	}

	public HashMap<String, Integer> getBigramSumMap() {
		return bigramSumMap;
	}

	/**
	 * 根据编辑距离生成真词纠错的候选集合， 并写到文件中
	 * 
	 * @param words
	 */
	private void createCandidateList() {
		try {
			PrintWriter writer = new PrintWriter(new File(C.PATH_CANDIDATE_LIST));
			EditDistanceUtil editUtil = EditDistanceUtil.getInstance();
			int count = 0;
			for (Iterator<String> iIter = this.oxfordWordsSet.iterator(); iIter.hasNext();) {
				count ++;
				String iWord = iIter.next();
				ArrayList<Node> tmpList = new ArrayList<Node>();
				for (Iterator<String> jIter = this.oxfordWordsSet.iterator(); jIter.hasNext();) {
					String jWord = jIter.next();
					int distance = editUtil.calculate(iWord, jWord);
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
	
	public HashMap<String, ArrayList<Node>> getCandidateList(){
		return null;
	}

	public static void main(String[] args) {
		System.out.println("start:");
		long start = System.currentTimeMillis();
		CorpusUtil corpusUtil = CorpusUtil.getInstance();
		corpusUtil.createCandidateList();

		long end = System.currentTimeMillis();
		long delay = (end - start);
		System.out.println("delay: " + delay + "ms");

	}

}
