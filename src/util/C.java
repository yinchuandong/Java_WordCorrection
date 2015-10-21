package util;

public class C {

	/**
	 * 二元语法
	 */
//	public final static String PATH_BIGRAM = "data/w2_.txt";
	public final static String PATH_BIGRAM = "data/w2_new.txt";
	/**
	 * 三元语法
	 */
	public final static String PATH_TRIGRAM = "data/trigram.txt";
	/**
	 * 牛津词典
	 */
	public final static String PATH_OXFORD_WORDS = "data_new/oxford-words.dat";
	/**
	 * 初始概率
	 */
	public final static String PATH_INIT_PROB = "data_new/init_prob.hmm";
	/**
	 * 转移概率
	 */
	public final static String PATH_TRAN_PROB = "data_new/tran_prob.hmm";
	/**
	 * 发射概率
	 */
	public final static String PATH_EMIT_PROB = "data_new/emit_prob.hmm";
	
	public final static String PATH_CONFUSING_WORD = "data_new/confusing_word.dat";
	
	/**
	 * 真词纠错的候选集
	 */
//	public final static String PATH_CANDIDATE_LIST = "data/candidate_set.txt";
	public final static String PATH_CANDIDATE_LIST = "data_new/candidate_set_new.dat";
	
	/**
	 * 词典集合
	 */
	public final static String PATH_DICT_MAP = "data_new/dict.json";
	
	public final static String[] PATH_ARR = {
		C.PATH_OXFORD_WORDS,
		C.PATH_INIT_PROB,
		C.PATH_TRAN_PROB,
		C.PATH_CONFUSING_WORD,
		C.PATH_CANDIDATE_LIST,
		C.PATH_DICT_MAP
	};
	/**
	 * 语料库
	 */
	public final static String PATH_SENTENCE= "data/sentences.txt";
	
}
