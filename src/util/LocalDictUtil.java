package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.json.JSONObject;

/**
 * 
 * @author yinchuandong
 *
 */
public class LocalDictUtil {

	private final static String PATH_ORIGIN = "/Users/yinchuandong/PyCharmProjects/python_dict/out/newList.json";
	private final static String PATH_INDEX = "/Users/yinchuandong/cproject/cppmydict/cppmydict/index.dat";
	private final static String PATH_DICT = "/Users/yinchuandong/cproject/cppmydict/cppmydict/dict.dat";

	HashMap<String, JSONObject> dictMap;

	private HashMap<String, Addr> indexMap;
	private RandomAccessFile dictFile;

	public class Addr {
		long start;
		long end;
		long size;
	}

	LocalDictUtil() {
	}

	private void loadOrigin() {
		dictMap = new HashMap<String, JSONObject>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(PATH_ORIGIN)));
			String buff = null;
			while ((buff = reader.readLine()) != null) {
				JSONObject obj = JSONObject.fromObject(buff);
				String key = obj.optString("word");
				dictMap.put(key, obj);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void write() {
		loadOrigin();
		try {
			PrintWriter indexWriter = new PrintWriter(PATH_INDEX);
//			PrintWriter dictWriter = new PrintWriter(PATH_DICT);
			RandomAccessFile writer = new RandomAccessFile(PATH_DICT, "rw");

			int offset = 0;
			byte[] space = "\0".getBytes("utf-8");
			System.out.println("space len:" + space.length);
			for (Iterator<String> iter = dictMap.keySet().iterator(); iter.hasNext();) {
				String key = iter.next();
				String obj = dictMap.get(key).toString();
				byte[] objBytes = obj.getBytes("utf-8");
				int start = offset;
				int size = objBytes.length + space.length;
				int end = offset + size;

				indexWriter.println(key + "," + start + "," + size + "," + end);
//				dictWriter.println(obj);
				writer.write(space);
				writer.write(objBytes);

				offset = end;
			}

			indexWriter.flush();
//			dictWriter.flush();

			indexWriter.close();
//			dictWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadIndex() {
		indexMap = new HashMap<String, LocalDictUtil.Addr>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(PATH_INDEX)));
			String buff = null;
			while ((buff = reader.readLine()) != null) {
				String[] arr = buff.split(",");

				Addr addr = new Addr();
				addr.start = Long.parseLong(arr[1]);
				addr.size = Long.parseLong(arr[2]);
				addr.end = Long.parseLong(arr[3]);

				indexMap.put(arr[0], addr);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String find(String key) {
		String ret = "";

		Addr addr = indexMap.get(key);
		if (addr == null) {
			return ret;
		}

		try {
			dictFile = new RandomAccessFile(PATH_DICT, "r");
			byte[] buff = new byte[(int) addr.size];
			dictFile.seek(addr.start + 1);
			dictFile.read(buff, 0, (int) addr.size);
			ret = new String(buff);
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static void main(String[] args) {
		System.out.println("start");
		LocalDictUtil util = new LocalDictUtil();
		util.write();
		
//		util.loadIndex();
//		util.find("hello");
		System.out.println("end");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
