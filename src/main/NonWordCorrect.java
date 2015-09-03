package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import util.CorpusUtil;
import util.EditDistanceUtil;

public class NonWordCorrect {
	
	/**
	 * 每个错误的单词最大推荐个数的推荐个数
	 */
	private int maxNumOfWords = 10;
	
	private HashSet<String> oxfordWordsSet;
	private HashMap<String, List<NonWordEntry>> oxfordDistanceMap;
	private EditDistanceUtil distanceUtil;
	
	public NonWordCorrect(CorpusUtil corpusUtil) {
		this.oxfordWordsSet = corpusUtil.getOxfordWordsSet();
		this.oxfordDistanceMap = new HashMap<String, List<NonWordEntry>>();
		this.distanceUtil = EditDistanceUtil.getInstance();
	}
	
	
	private void detectOne(String unknownWord){
		List<NonWordEntry> list = new ArrayList<NonWordEntry>();
		
		for (String word : oxfordWordsSet) {
			int distance = distanceUtil.calculate(unknownWord, word);
			NonWordEntry entry = new NonWordEntry();
			entry.word = word;
			entry.distance = distance;
			list.add(entry);
		}
		
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		Collections.sort(list, new Comparator<NonWordEntry>() {
			@Override
			public int compare(NonWordEntry o1, NonWordEntry o2) {
				//正序
				if(o1.distance > o2.distance){
					return 1;
				}
				return -1;
			}
		});
		
		int maxLen = list.size() > maxNumOfWords ? maxNumOfWords : list.size();
		List<NonWordEntry> newList = list.subList(0, maxLen - 1);
		oxfordDistanceMap.put(unknownWord, newList);
		for (NonWordEntry nonWordEntry : newList) {
			System.out.println(nonWordEntry.word + " " + nonWordEntry.distance);
		}
	}
	
	
	/**
	 * use as data structure
	 * @author yinchuandong
	 *
	 */
	public class NonWordEntry{
		public String word;
		public int distance;
	}
		
	
	public static void main(String[] args){
		NonWordCorrect model = new NonWordCorrect(CorpusUtil.getInstance());
		model.detectOne("plays");
		
	}
	
	
}
