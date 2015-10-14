package main;

/**
 * 维特比节点的概率
 * 
 * @author yinchuandong
 *
 */
public class Node {
	/** 词的内容 */
	public String word;
	/** 当前词的edit distance */
	public int distance = 0;
	/** 当前的维特比概率 */
	public double prob = 0.0;
	/** 前驱节点 */
	public Node preNode = null;

	
	public Node(String word, int distance) {
		super();
		this.word = word;
		this.distance = distance;
	}

	/**
	 * 
	 * @param word
	 *            当前词的 edit distance
	 * @param prob
	 *            当前的维特比概率
	 * @param word
	 *            词的内容
	 * @param preNode
	 *            前驱节点
	 */
	public Node(String word, int distance, double prob, Node preNode) {
		super();
		this.word = word;
		this.distance = distance;
		this.prob = prob;
		this.preNode = preNode;
	}

	@Override
	public String toString() {
		return "Node [word=" + word + ", distance=" + distance + ", prob=" + prob + "]";
	}


	
}