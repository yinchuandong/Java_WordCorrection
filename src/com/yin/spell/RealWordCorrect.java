package com.yin.spell;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 真词纠错
 * 
 * @author yinchuandong
 *
 */
public class RealWordCorrect {

	private CorpusUtil corpusUtil;

	private String[] words;
	private Node[][] matrix;
	private Node maxFinalNode = null;
	
	//结果句子记录矩阵
	/** 未纠错的句子矩阵 */
	String[][] oldWordMatrix = null;
	/** 纠错之后的句子矩阵 */
	String[][] newWordMatrix = null;
	/** 句子对应的标点符号 */
	String[] punctArr = null;
	
	public RealWordCorrect(){
		this(CorpusUtil.getInstance());
	}

	public RealWordCorrect(CorpusUtil corpusUtil) {
		this.corpusUtil = corpusUtil;
	}

	/**
	 * 初始化句子，每次只传入一句
	 * @param words
	 */
	public void init(String[] words) {
		this.words = words.clone();
		this.maxFinalNode = null;
		this.matrix = new Node[words.length][];
		this.initMatrix();
	}

	private void initMatrix() {
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].toLowerCase();
			ArrayList<Node> candidateList = corpusUtil.getCandidateList(words[i]);
			matrix[i] = new Node[candidateList.size()];
			for (int j = 0; j < candidateList.size(); j++) {
				Node node = candidateList.get(j).clone(); // 一定要加clone，否则无法display
				node.prob = 0.0;
				matrix[i][j] = node;
			}
		}

		// 创建初始维特比概率
		for (int j = 0; j < matrix[0].length; j++) {
			Node node = matrix[0][j];
			node.prob = corpusUtil.getInitProb(node.word);
		}
	}

	/**
	 * 采用二元语法计算
	 */
	private void calcBigram() {
		
		for (int t = 1; t < words.length; t++) {
			Node[] curNodes = matrix[t];
			Node[] preNodes = matrix[t - 1];
			for (int i = 0; i < curNodes.length; i++) {
				Node maxPreNode = preNodes[0];
				double maxProb = Integer.MIN_VALUE;

				for (int j = 0; j < preNodes.length; j++) {
					String tranKey = preNodes[j].word + "|" + curNodes[i].word;
					double preProb = corpusUtil.getTranProb(tranKey);
					double tmpProb = matrix[t - 1][j].prob * preProb;
					if (tmpProb > maxProb) {
						maxPreNode = preNodes[j];
						maxProb = tmpProb;
					}
				}
				
				curNodes[i].prob = maxProb;
				curNodes[i].preNode = maxPreNode;
			}
		}

		//find the maximum node in the last column
		Node[] finalRow = matrix[matrix.length - 1];
		for (int k = 0; k < finalRow.length; k++) {
			if (maxFinalNode == null || finalRow[k].prob > maxFinalNode.prob) {
				maxFinalNode = finalRow[k];
			}
		}

	}
	
	private String[] decode(){
		String[] result = new String[words.length];
		Node tmpNode = maxFinalNode;
		int i = words.length - 1;
		while (tmpNode != null) {
			result[i--] = tmpNode.word;
			tmpNode = tmpNode.preNode;
		}
		return result;
	}

	/**
	 * 具体运行算法
	 * @param article
	 * @return
	 */
	public boolean run(String article) {
		if (!article.endsWith(".")) {
			article += ".";
		}
		article = article.replaceAll("\\s+", " ");
		String[] oldSentences = article.split("[,.;]");
		
		if(oldSentences.length < 1){
			return false;
		}
		
		corpusUtil.initRedis();
		oldWordMatrix = new String[oldSentences.length][]; 
		newWordMatrix = new String[oldSentences.length][];
		punctArr = new String[oldSentences.length];

		int offset = 0;
		for (int i = 0; i < oldSentences.length; i++) {
			//获取每一句对应的标点符号
			offset += oldSentences[i].length() + 1;
			punctArr[i] = article.substring(offset - 1, offset);
			
			String oldSent = oldSentences[i].trim();
			oldWordMatrix[i] = oldSent.split("\\s");
			init(oldWordMatrix[i]);
			calcBigram();
			newWordMatrix[i] = decode();
			
			for (int j = 0; j < newWordMatrix[i].length; j++) {
				if (i == 0) {
					newWordMatrix[i][0] = CorpusUtil.toUpperCaseFirstChar(newWordMatrix[i][0]);
				}
				if (i > 0 && (punctArr[i - 1].equals(".") || punctArr[i - 1].equals(";"))) {
					newWordMatrix[i][0] = CorpusUtil.toUpperCaseFirstChar(newWordMatrix[i][0]);
				}
				if (newWordMatrix[i][j].equals("i")) {
					newWordMatrix[i][j] = newWordMatrix[i][j].toUpperCase();
				}
			}
		}
		//have to do this
		corpusUtil.closeRedis();
		display();
		System.out.println("----------end run------");
		return true;
	}
	
	
	/**
	 * 返回给外部调用的json
	 * @return
	 */
	public String ajaxRun(String article){
		
		JSONObject ret = JSONObject.fromObject("{}");
		if(run(article) == false){
			ret.put("status", "0");
			ret.put("info", "fail");
			return ret.toString();
		}
		JSONObject data = JSONObject.fromObject("{}");
		data.put("oldMatrix", oldWordMatrix);
		data.put("newMatrix", newWordMatrix);
		data.put("punct", punctArr);
		
		//候选集是没有纠错的候选集
		HashMap<String, String[]> candiMap = new HashMap<String, String[]>();
		for (int i = 0; i < oldWordMatrix.length; i++) {
			for (int j = 0; j < oldWordMatrix[i].length; j++) {
				String word = oldWordMatrix[i][j].toLowerCase();
				if(candiMap.containsKey(word)){
					continue;
				}
				ArrayList<Node> nodeList = corpusUtil.getCandidateList(word);
				String[] wordArr = new String[nodeList.size()];
				for(int k = 0; k < nodeList.size(); k++){
					wordArr[k] = nodeList.get(k).word;
				}
				candiMap.put(word, wordArr);
			}
		}
		
		ret.put("candidate", candiMap);
		ret.put("data", data);
		ret.put("status", "1");
		ret.put("info", "success");
		System.out.println(ret);
		return ret.toString();
	}

	public void display() {
		System.out.println();
		System.out.println("纠错句子：");
		for (int i = 0; i < newWordMatrix.length; i++) {
			for (int j = 0; j < newWordMatrix[i].length; j++) {
				System.out.print(oldWordMatrix[i][j] + "/");
				System.out.print(newWordMatrix[i][j] + " ");
			}
			System.out.println(punctArr[i]);
		}
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		System.out.println("start:");
		/*
		 * you is a boy 用来测试emit prob
		 * he do love you
		 * you parent do love you
		 * there is lots of apple which I like
		 */
		String sentence = "";
		sentence += "He am a boys,I has a apples. you is a boy.";
//		sentence += "there is lots of appe whih I like.";
//		sentence += "he do love you, She done love you.";
//		sentence += "my name as John.";
//		sentence += "I want too eat pizza this afternoon among my parent.";
//		sentence += "the weather is good today.";
//		sentence += "His favourite sports is basketball.";
//		sentence += "he like making faces or telling jokes.";
//		sentence += "he have play tuis thing.";
		System.out.println("原始句子：" + sentence);
		RealWordCorrect model = new RealWordCorrect(CorpusUtil.getInstance());
		model.ajaxRun(sentence);
		
		long end = System.currentTimeMillis();
		System.out.println("end;  delay: " + (end - start));
	}

}
