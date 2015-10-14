package util;

public class EditDistanceUtil {
	
	private static EditDistanceUtil instance = null;
	
	private EditDistanceUtil(){
		
	}
	
	/**
	 * 获得EditDistance 的实例
	 * @return
	 */
	public static EditDistanceUtil getInstance(){
		if(instance == null){
			instance = new EditDistanceUtil();
		}
		return instance;
	}

	/**
	 * return the minimum of a, b and c
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	private int min(int a, int b, int c) {
		int t = a < b ? a : b;
		return t < c ? t : c;
	}

	/**
	 * 计算编辑距离
	 * 
	 * @param word1
	 * @param word2
	 * @return
	 */
	public int calculate(String word1, String word2) {
		int distance = 0;

		int len1 = word1.length();
		int len2 = word2.length();
		int[][] matrix = new int[len1 + 1][len2 + 1];

		// 初始化行
		for (int i = 0; i < len1 + 1; i++) {
			matrix[i][0] = i;
		}

		// 初始化列
		for (int j = 0; j < len2 + 1; j++) {
			matrix[0][j] = j;
		}

		for (int i = 1; i < len1 + 1; i++) {
			for (int j = 1; j < len2 + 1; j++) {
				char c1 = word1.charAt(i - 1);
				char c2 = word2.charAt(j - 1);

				int cost = c1 != c2 ? 2 : 0;
				int del = matrix[i - 1][j] + 1;
				int ins = matrix[i][j - 1] + 1;
				int sub = matrix[i - 1][j - 1] + cost;

				matrix[i][j] = min(del, ins, sub);

			}
		}

//		for (int i = 0; i <= len1; i++) {
//			for (int j = 0; j <= len2; j++) {
//				int val = matrix[i][j];
//				if (val < 10) {
//					System.out.print("  " + val + "  ");
//				} else {
//					System.out.print("  " + val + " ");
//				}
//
//			}
//			System.out.println();
//		}

		distance = matrix[len1][len2];
		return distance;
	}
	
	public void getCandidateList(String word){
		
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		EditDistanceUtil edited = EditDistanceUtil.getInstance();
		for (int i = 0; i < 50000; i++) {
			edited.calculate("intention", "execution");
		}
//		int distance = edited.calculate("intention", "execution");
		long end = System.currentTimeMillis();
		
		long delay = end - start;
//		System.out.println("edit distance is: " + distance);
		System.out.println("delay: " + delay + "ms");
	}
}
