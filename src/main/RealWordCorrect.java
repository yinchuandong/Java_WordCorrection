package main;

import java.util.HashMap;
import java.util.HashSet;

import util.CorpusUtil;

/**
 * 真词纠错
 * @author yinchuandong
 *
 */
public class RealWordCorrect {

	private HashSet<String> oxfordWordsSet;
	/**
	 * 每一项二元语法的map, eg:N(<BOS>|Brown)
	 */
	private HashMap<String, Integer> bigramMap;
	/**
	 * 二元语法的总数统计map, eg:N(<BOS>*)
	 */
	private HashMap<String, Integer> bigramSumMap;
	
	public RealWordCorrect(CorpusUtil corpusUtil){
		this.oxfordWordsSet = corpusUtil.getOxfordWordsSet();
		this.bigramMap = corpusUtil.getBigramMap();
		this.bigramSumMap = corpusUtil.getBigramSumMap();
	}
	
	private double test(String sentence){
		double frequency = 1.0;
		int numGram = 0; //分子
		int numWord = 0;//分母
		String key;
		String keySum;
		String[] wordArr = sentence.split(" ");
		for (int i = 0; i < wordArr.length - 1; i++) {
			key = wordArr[i] + "|" + wordArr[i+1];
			keySum = wordArr[i];
			if(bigramMap.containsKey(key)){
				numGram = bigramMap.get(key);
			}
			if(bigramSumMap.containsKey(keySum)){
				numWord = bigramSumMap.get(keySum);
			}
			//Laplace data smoothing
			frequency *= ((double)numGram + 1.0) / (numWord + oxfordWordsSet.size()*10);
		}
		return frequency;
	}
	
	
	public static void main(String[] args){
		RealWordCorrect model = new RealWordCorrect(CorpusUtil.getInstance());
//		double f1 = model.test("<BOS> I read a books <EOS>");
//		double f2 = model.test("<BOS> I read a book <EOS>");
		double f1 = model.test("<BOS> I read a books <EOS>");
		double f2 = model.test("<BOS> I read a book <EOS>");
		
		System.out.println("f1:" + f1);
		System.out.println("f2:" + f2);
	}
	
	
}
