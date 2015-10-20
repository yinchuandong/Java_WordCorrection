package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import main.Node;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;



public class DictUtil {

	HashMap<String, Integer> bigramMap;
	HashMap<String, JSONObject> wordsMap;
	HashSet<String> oxfordWordsSet;
	
	public DictUtil() {
		
	}

	
	private void init(){
		bigramMap = new HashMap<String, Integer>();
		wordsMap = new HashMap<String, JSONObject>();
		loadFile();
	}
	
	private void loadFile(){
		try {
			//加载旧的二元语法
			File file = new File("data/w2_.txt");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String buff = null;
			while ((buff = reader.readLine()) != null) {
				if (buff.equals("")) {
					continue;
				}
				String[] lineArr = buff.split("\t");
				if (lineArr.length < 3) {
					continue;
				}
				String key = lineArr[1] + "|" + lineArr[2];// key of bigramMap
				int count = Integer.parseInt(lineArr[0]);// word frequency
				bigramMap.put(key, count);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			//加载词库
			String filename = "/Users/yinchuandong/PycharmProjects/python_dict/out/newList.json";
			BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
			String buff = null;
			while((buff = reader.readLine()) != null){
				JSONObject obj = JSONObject.fromObject(buff);
				String word = obj.getString("word");
				wordsMap.put(word, obj);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(wordsMap.get("low"));
	}
	
	/**
	 * 为二元语法原始文件的动词搭配增加过去式，现在分词，三人称单数等搭配形式
	 * 如：like making 290
	 * 则增加: 
	 * likes making 290
	 * liked making 290
	 * liking making 290
	 */
	private void addNewItems(){
		System.out.println(bigramMap.size());
		HashSet<String> set = new HashSet<String>(bigramMap.keySet());
		for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
			String key = iter.next();
			String[] arr = key.split("\\|");
			String iWord = arr[0];
			String jWord = arr[1];
			int count = bigramMap.get(key);
			JSONObject obj = wordsMap.get(iWord);
			if (obj == null) {
				continue;
			}
			String pt = obj.optString("pt"); //过去式
			String pp = obj.optString("p.p"); //过去分词
			String ppr = obj.optString("p.pr"); //现在分词
			String ps = obj.optString("3ps"); //三单
			if (pt.length() > 0 && pp.length() > 0 && pt.equals(pp)) {
				//如果过去式和过去分词相同
				if (bigramMap.containsKey(pt + "|" + jWord)) {
					//如果存在改元组，则将该元组*0.8，降低频率，防止 liked 被过去式和过去分词的频率相加
					bigramMap.put(pt + "|" + jWord, (int)(bigramMap.get(pt + "|" + jWord) * 0.3));
				}else{
					//如果不存在，则以原型的0.8赋值
					bigramMap.put(pt + "|" + jWord, (int)(count * 0.1));
				}
			}
			
			if (ppr.length() > 0 && !bigramMap.containsKey(ppr + "|" + jWord)) {
				//如果原始bigramMap里不存在 (现在分词 + jWord) 的搭配形式，则将原型的频率复制给现在分词
				bigramMap.put(ppr + "|" + jWord, count);
			}
			
			if (ps.length() > 0) {
				//如果原始bigramMap里不存在 (三人称单数 + jWord) 的搭配形式，则将原型的频率复制给三人称单数
				int oldCount = bigramMap.containsKey(ps + "|" + jWord) ? bigramMap.get(ps + "|" + jWord) : 0;
				bigramMap.put(ps + "|" + jWord, Math.max(count, oldCount));
			}
		}
		System.out.println(bigramMap.size());
	}
	
	
	/**
	 * 重新生成语料库
	 */
	private void resaveBigram(){
		addNewItems();
		try {
			Set<String> set = new TreeSet<>(bigramMap.keySet());
			PrintWriter writer = new PrintWriter(new File("data/w2_new.txt"));
			for(Iterator<String> iter = set.iterator(); iter.hasNext(); ){
				String key = iter.next();
				String[] arr = key.split("\\|");
				String iWord = arr[0];
				String jWord = arr[1];
				int count = bigramMap.get(key);
				writer.println(count + "\t" + iWord + "\t" + jWord);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/*-------------候选集函数-----------------------------------------------*/
	
	/**
	 * 创建候选同义词
	 */
	private void createCandidateThesaurus(){
		try {
			PrintWriter writer = new PrintWriter(new File("data_new/candidate_set_new.txt"));
			for (Iterator<String> iter = wordsMap.keySet().iterator(); iter.hasNext();) {
				String key = iter.next();
				JSONObject obj = wordsMap.get(key);
				String pt = obj.optString("pt"); // 过去式
				String pp = obj.optString("p.p"); // 过去分词
				String ppr = obj.optString("p.pr"); // 现在分词
				String ps = obj.optString("3ps"); // 三单
				String plural = obj.optString("plural"); //复数形式

				HashSet<String> set = new HashSet<String>();
				set.add(pt);
				set.add(pp);
				set.add(ppr);
				set.add(ps);
				set.add(plural);
				String lineStr = key + " " + key + ",0" + " ";
				for (String candi : set) {
					if (candi.length() <= 0) {
						continue;
					}
					lineStr += candi.split(" ")[0] + ",0.5 ";
				}
				lineStr = lineStr.substring(0, lineStr.length() - 1);
				writer.println(lineStr);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据编辑距离生成真词纠错的候选集合， 并写到文件中
	 * 
	 * @param words
	 */
	private void createCandidateList() {
		CorpusUtil util = CorpusUtil.getInstance();
		oxfordWordsSet = util.getOxfordWordsSet(); 
		try {
			PrintWriter writer = new PrintWriter(new File(C.PATH_CANDIDATE_LIST));
			
			int count = 0;
			for (Iterator<String> iIter = this.oxfordWordsSet.iterator(); iIter.hasNext();) {
				count ++;
				String iWord = iIter.next();
				ArrayList<Node> tmpList = util.calcCandidateWords(iWord);
				
				int size = tmpList.size() > 10 ? 10 : tmpList.size();
				String tmpStr = iWord + " ";
				for (int i = 0; i < size; i++) {
					tmpStr += tmpList.get(i).word + "," + tmpList.get(i).distance + " ";
				}
				tmpStr = tmpStr.substring(0, tmpStr.length() - 1); // remove the last '\t'
//				System.out.println(tmpStr);
//				break;
				writer.println(tmpStr);
				if (count % 100 == 0) {
					System.out.println("正在处理第：" + count);
				}
			}
			
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		System.out.println("start:");
		long start = System.currentTimeMillis();
		
		DictUtil util = new DictUtil();
		util.init();
		//根据w2_.txt重新生成二元语法库
		util.resaveBigram();
		
		//根据词性变换，生成候选语料集
//		util.createCandidateThesaurus();
		
		long end = System.currentTimeMillis();
		long delay = end - start;
		System.out.println("end, delay: " + delay);
	}
	
	
}
