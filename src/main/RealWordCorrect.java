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
	 * 初始概率
	 */
	private HashMap<String, Double> initProbMap;
	/**
	 * 转移概率
	 */
	private HashMap<String, Double> tranProbMap;
	/**
	 * 发射概率map
	 */
	private HashMap<String, Double> emitProbMap;

	private static double MIN_PROB = 0.0000000000000001;

	private String[] words;
	private Node[][] matrix;
	private Node maxFinalNode = null;

	public RealWordCorrect(CorpusUtil corpusUtil) {
		this.corpusUtil = corpusUtil;
		this.oxfordWordsSet = corpusUtil.getOxfordWordsSet();
		this.initProbMap = corpusUtil.getInitProbMap();
		this.tranProbMap = corpusUtil.getTranProbMap();
		this.emitProbMap = corpusUtil.getEmitProbMap();
	}

	public void init() {
	}

	private void initMatrix() {
		matrix = new Node[words.length][];
		for (int i = 0; i < words.length; i++) {
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
			double initProb = initProbMap.containsKey(node.word) ? initProbMap.get(node.word) : MIN_PROB;
			String emitKey = words[0] + "|" + node.word;
			double emitProb = emitProbMap.containsKey(emitKey) ? emitProbMap.get(emitKey) : MIN_PROB;
			node.prob = initProb * emitProb;
		}
	}

	/**
	 * 采用二元语法计算
	 */
	private void runBigram() {
		for (int t = 1; t < words.length; t++) {
			Node[] curNodes = matrix[t];
			Node[] preNodes = matrix[t - 1];
			for (int i = 0; i < curNodes.length; i++) {
				Node maxPreNode = preNodes[0];
				double maxProb = Integer.MIN_VALUE;

				for (int j = 0; j < preNodes.length; j++) {
					double preProb = MIN_PROB;
					String tranKey = preNodes[j].word + "|" + curNodes[i].word;
					if (tranProbMap.containsKey(tranKey)) {
						preProb = tranProbMap.get(tranKey);
					}
					double tmpProb = matrix[t - 1][j].prob * preProb;
					if (tmpProb > maxProb) {
						maxPreNode = preNodes[j];
						maxProb = tmpProb;
					}
				}

				String emitKey = words[t] + "|" + curNodes[i].word;
				double emitProb = emitProbMap.containsKey(emitKey) ? emitProbMap.get(emitKey) : MIN_PROB;
				maxProb = maxProb * emitProb;
				curNodes[i].prob = maxProb;
				curNodes[i].preNode = maxPreNode;
			}
		}

		Node[] finalRow = matrix[matrix.length - 1];
		for (int k = 0; k < finalRow.length; k++) {
			if (maxFinalNode == null || finalRow[k].prob > maxFinalNode.prob) {
				maxFinalNode = finalRow[k];
			}
		}

	}

	public void run(String sentence) {
		words = sentence.split(" ");
		initMatrix();
		runBigram();

		System.out.println("end run");
	}

	public void display() {
		Node tmpNode = maxFinalNode;
		String str = "";
		while (tmpNode != null) {
			str = tmpNode.word + " " + str;
			tmpNode = tmpNode.preNode;
		}
		System.out.println("纠错句子：" + str);
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		System.out.println("start:");
		/*
		 * you is a boy 用来测试emit prob
		 * he do love you
		 * you parent do love you
		 */
		String sentence = "you parent do love you";
		System.out.println("原始句子：" + sentence);
		RealWordCorrect model = new RealWordCorrect(CorpusUtil.getInstance());
		model.init();
		model.run(sentence);
		model.display();
		long end = System.currentTimeMillis();
		System.out.println("end;  delay: " + (end - start));
	}

}
