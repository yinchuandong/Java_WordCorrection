package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

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
	
	private CorpusUtil(){
		init();
	}
	
	public static CorpusUtil getInstance(){
		if(instance == null){
			instance = new CorpusUtil();
		}
		return instance;
	}
	
	private void init(){
		oxfordWordsSet = new HashSet<String>();
		bigramMap = new HashMap<String, Integer>();
		bigramSumMap = new HashMap<String, Integer>();
		
		loadOxfordWords();
		loadBigram();
	}
	
	private void loadOxfordWords(){
		try {
			File file = new File(C.PATH_OXFORD_WORDS);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while((buff = reader.readLine()) != null){
				if(buff.equals("")){
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
	
	private void loadBigram(){
		try {
			File file = new File(C.PATH_BIGRAM);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while((buff = reader.readLine()) != null){
				if(buff.equals("")){
					continue;
				}
				String[] lineArr = buff.split("\t");
				if(lineArr.length < 3){
					continue;
				}
				String key = lineArr[0] + "|" + lineArr[1];//key of bigramMap
				String keySum = lineArr[0];//key of bigramSumMap
				int count = Integer.parseInt(lineArr[2]);//word frequency
				
				bigramMap.put(key, count);
				if(!bigramSumMap.containsKey(keySum)){
					bigramSumMap.put(keySum, count);
				}else{
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
	
	
	public HashSet<String> getOxfordWordsSet() {
		return oxfordWordsSet;
	}
	
	public HashMap<String, Integer> getBigramMap() {
		return bigramMap;
	}

	public HashMap<String, Integer> getBigramSumMap() {
		return bigramSumMap;
	}

	public static void main(String[] args){
		System.out.println("start");
		long start = System.currentTimeMillis();
		CorpusUtil corpusUtil = CorpusUtil.getInstance();
		
		long end = System.currentTimeMillis();
		long delay = (end - start);
		System.out.println("delay: " + delay + "ms");
		
	}
	
	
	
	
	
	
	
	
}
