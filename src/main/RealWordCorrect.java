package main;

import java.util.ArrayList;
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

	private CorpusUtil corpusUtil;

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
	
	private String[] words;
	private Node[][] matrix;
	

	public RealWordCorrect(CorpusUtil corpusUtil) {
		this.corpusUtil = corpusUtil;
		this.oxfordWordsSet = corpusUtil.getOxfordWordsSet();
		this.bigramMap = corpusUtil.getBigramMap();
		this.bigramSumMap = corpusUtil.getBigramSumMap();
		this.tranMap = new HashMap<String, Double>();
	}

	public void init() {
		calcTranProb();
	}
	
	private void initMatrix(){
		matrix = new Node[words.length][];
		for(int i = 0; i < words.length; i++){
			ArrayList<Node> candidateList = corpusUtil.getCandidateList(words[i]);
			matrix[i] = new Node[candidateList.size()];
			for(int j = 0; j < candidateList.size(); j++){
				Node node = candidateList.get(j);
				matrix[i][j] = node;
			}
		}
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
		words = sentence.split(" ");
		initMatrix();
		
		System.out.println("end run");
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		System.out.println("start:");
		RealWordCorrect model = new RealWordCorrect(CorpusUtil.getInstance());
		model.init();
		model.run("i am a boy");
		long end = System.currentTimeMillis();
		System.out.println("end;  delay: " + (end - start));
	}

}
