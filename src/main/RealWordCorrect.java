package main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import util.CorpusUtil;

/**
 * 真词纠错
 * 
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

	/**
	 * 转移概率
	 */
	private HashMap<String, Double> tranMap;

	public RealWordCorrect(CorpusUtil corpusUtil) {
		this.oxfordWordsSet = corpusUtil.getOxfordWordsSet();
		this.bigramMap = corpusUtil.getBigramMap();
		this.bigramSumMap = corpusUtil.getBigramSumMap();
		this.tranMap = new HashMap<String, Double>();
	}

	public void init() {
		calcTranProb();
	}
	
	private void initMatrix(){
		
	}

	private void calcTranProb() {
		for (Iterator<String> iter = bigramMap.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			String[] keyArr = key.split("\\|");
			String iWord = keyArr[0];
			double prob = (double) bigramMap.get(key) / bigramSumMap.get(iWord);
			tranMap.put(key, prob);
		}
		System.out.println();
	}
	
	public void run(String sentence){
		
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		System.out.println("start:");
		RealWordCorrect model = new RealWordCorrect(CorpusUtil.getInstance());
		model.init();

		long end = System.currentTimeMillis();
		System.out.println("end;  delay: " + (end - start));
	}

}
