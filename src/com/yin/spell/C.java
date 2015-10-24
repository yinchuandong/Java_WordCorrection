package com.yin.spell;

public class C {

	/**
	 * 二元语法
	 */
//	public static String PATH_BIGRAM = "data/w2_.txt";
	public static String PATH_BIGRAM = "data/w2_new.txt";
	/**
	 * 三元语法
	 */
	public static String PATH_TRIGRAM = "data/trigram.txt";
	/**
	 * 语料库
	 */
	public static String PATH_SENTENCE= "data/sentences.txt";
	/**
	 * 牛津词典
	 */
	public static String PATH_OXFORD_WORDS = "data_new/oxford-words.dat";
	/**
	 * 初始概率
	 */
	public static String PATH_INIT_PROB = "data_new/init_prob.hmm";
	/**
	 * 转移概率
	 */
	public static String PATH_TRAN_PROB = "data_new/tran_prob.hmm";
	/**
	 * 发射概率
	 */
	public static String PATH_EMIT_PROB = "data_new/emit_prob.hmm";
	
	public static String PATH_CONFUSING_WORD = "data_new/confusing_word.dat";
	
	/**
	 * 真词纠错的候选集
	 */
	public static String PATH_CANDIDATE_LIST = "data_new/candidate_set_new.dat";
	
	/**
	 * 词典集合
	 */
	public static String PATH_DICT_MAP = "data_new/dict.json";
	
	private static boolean IS_LOADED = false;
	
	/**
	 * 重置路径
	 * @param base
	 */
	public static void resetPath(String base){
		if (IS_LOADED) {
			return;
		}
		IS_LOADED = true;
		PATH_OXFORD_WORDS = base + PATH_OXFORD_WORDS;
		PATH_CONFUSING_WORD = base + PATH_CONFUSING_WORD;
		PATH_CANDIDATE_LIST = base + PATH_CANDIDATE_LIST;
	}
	
	
}
