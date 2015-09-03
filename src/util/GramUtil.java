package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GramUtil {

	public GramUtil(){
		
	}

	/**
	 * 生成二元语法
	 */
	public void generateBigram(){
		
		HashMap<String, Integer> bigramMap = new HashMap<String, Integer>();
		
		//从文件中读取二元语法格式
		try {
			File file = new File(C.PATH_SENTENCE);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while((buff = reader.readLine()) != null){
				if(buff.equals("")){
					continue;
				}
				
				String[] lineArr = buff.split(" ");
				if(lineArr.length < 2){
					continue;
				}
				
				String bWord = filterKey(lineArr[0]);
				if(!bWord.equals("")){
					String bos = "<BOS>\t" + bWord;
					if(!bigramMap.containsKey(bos)){
						bigramMap.put(bos, 1);
					}else{
						bigramMap.put(bos, bigramMap.get(bos) + 1);
					}
				}
				
				for (int i = 0; i < lineArr.length - 1; i++) {
					String w1 = filterKey(lineArr[i]);
					String w2 = filterKey(lineArr[i + 1]);
					if(w1.equals("") || w2.equals("")){
						continue;
					}
					
					String key = w1 + "\t" + w2;
					if(!bigramMap.containsKey(key)){
						bigramMap.put(key, 1);
					}else{
						bigramMap.put(key, bigramMap.get(key) + 1);
					}
				}
				
				String eWord = filterKey(lineArr[lineArr.length - 1]);
				if(!eWord.equals("")){
					String eos = eWord + "\t<EOS>";
					if(!bigramMap.containsKey(eos)){
						bigramMap.put(eos, 1);
					}else{
						bigramMap.put(eos, bigramMap.get(eos) + 1);
					}
				}
				
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//将二元语法写入到文件中
		try {
			List<String> keyList = new ArrayList<String>(bigramMap.keySet());
			Collections.sort(keyList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return o1.toLowerCase().compareTo(o2.toLowerCase());
				}
				
			});
			
			File file = new File(C.PATH_BIGRAM);
			PrintWriter writer = new PrintWriter(file);
			for (String key : keyList) {
				int count = bigramMap.get(key);
				writer.println(key + "\t" + count);
			}
			
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * 生成三元语法
	 */
	public void generateTrigram(){
		
		HashMap<String, Integer> trigramMap = new HashMap<String, Integer>();
		
		//从文件中读取三元语法格式
		try {
			File file = new File(C.PATH_SENTENCE);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while((buff = reader.readLine()) != null){
				if(buff.equals("")){
					continue;
				}
				
				String[] lineArr = buff.split(" ");
				if(lineArr.length < 3){
					continue;
				}
				
				String bWord0 = filterKey(lineArr[0]);
				String bWord1 = filterKey(lineArr[1]);
				if(!bWord0.equals("") && !bWord0.equals("")){
					String bos = "<BOS>\t" + bWord0 + "\t" + bWord1;
					if(!trigramMap.containsKey(bos)){
						trigramMap.put(bos, 1);
					}else{
						trigramMap.put(bos, trigramMap.get(bos) + 1);
					}
				}
				
				for (int i = 0; i < lineArr.length - 2; i++) {
					String w1 = filterKey(lineArr[i]);
					String w2 = filterKey(lineArr[i + 1]);
					String w3 = filterKey(lineArr[i + 2]);
					if(w1.equals("") || w2.equals("") || w3.equals("")){
						continue;
					}
					
					String key = w1 + "\t" + w2 + "\t" + w3;
					if(!trigramMap.containsKey(key)){
						trigramMap.put(key, 1);
					}else{
						trigramMap.put(key, trigramMap.get(key) + 1);
					}
				}
				
				String eWord0 = filterKey(lineArr[lineArr.length - 2]);
				String eWord1 = filterKey(lineArr[lineArr.length - 1]);
				if(!eWord0.equals("") && !eWord1.equals("")){
					String eos = eWord0 + "\t" + eWord1 + "\t<EOS>";
					if(!trigramMap.containsKey(eos)){
						trigramMap.put(eos, 1);
					}else{
						trigramMap.put(eos, trigramMap.get(eos) + 1);
					}
				}
				
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//将二元语法写入到文件中
		try {
			List<String> keyList = new ArrayList<String>(trigramMap.keySet());
			Collections.sort(keyList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return o1.toLowerCase().compareTo(o2.toLowerCase());
				}
				
			});
			
			File file = new File(C.PATH_TRIGRAM);
			PrintWriter writer = new PrintWriter(file);
			for (String key : keyList) {
				int count = trigramMap.get(key);
				writer.println(key + "\t" + count);
			}
			
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 过滤掉非英语字符以及一些标点符号
	 * @param oldKey
	 * @return
	 */
	private String filterKey(String oldKey){
		String newKey = "";
		newKey = oldKey.replaceAll("[\"();,{}@%\\[\\]\\|\\+]|((\\*)+[a-zA-Z])|(/-)|(/)|(\\\\)", "");
		if(newKey.equals("") | newKey.equals("'") || newKey.equals("''")){
			return "";
		}
		while(newKey.startsWith("'") || newKey.startsWith("-")){
			newKey = newKey.substring(1);
		}
		return newKey;
	}
	
	public static void main(String[] args){
		System.out.println("start");
		long start = System.currentTimeMillis();
		
		GramUtil util = new GramUtil();
//		util.generateBigram();
		util.generateTrigram();
		
		long end = System.currentTimeMillis();
		long delay = (end - start);
		System.out.println("delay: " + delay + "ms");
	}
}
