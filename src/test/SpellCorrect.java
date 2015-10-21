package test;

import java.util.*;
import java.io.*;

public class SpellCorrect {

	private static String readText(File file) {
		String text = null;
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					file));
			BufferedReader br = new BufferedReader(read);
			StringBuffer buff = new StringBuffer();
			while ((text = br.readLine()) != null) {
				buff.append(text + "\r\n");
			}
			br.close();
			text = buff.toString();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
		return text;
	}

	public static void tokenizeAndLowerCase(String line,
			ArrayList<String> tokens) {
		// TODO Auto-generated method stub
		StringTokenizer strTok = new StringTokenizer(line,
				"\r\n\t/\\\':\" ()[]{};.,#-_=!@$%^&*+1234567890");
		while (strTok.hasMoreTokens()) {
			String token = strTok.nextToken();
			tokens.add(token.toLowerCase().trim());
		}
	}

	public static void trainPrior(ArrayList<String> str,
			Map<String, Integer> map) {
		for (int i = 0; i < str.size(); i++) {
			if (map.containsKey(str.get(i))) {
				int tmp = map.get(str.get(i));
				map.put(str.get(i), 1 + tmp);
			} else
				map.put(str.get(i), 1);
		}
	}

	public static Set<String> Edit1(String str) {
		Set<String> array = new HashSet<String>();
		for (int i = 0; i < str.length(); i++)// delete
		{
			String tmpstr = str.substring(0, i)
					+ str.substring(i + 1, str.length());
			array.add(tmpstr);
		}

		for (int i = 0; i < str.length(); i++)// insert
		{
			for (char x = 'a'; x <= 'z'; x++) {
				String tmpstr = str.substring(0, i) + x
						+ str.substring(i, str.length());
				array.add(tmpstr);
			}
		}
		for (int i = 0; i < str.length() - 1; i++)// trans
		{
			String tmpstr = str.substring(0, i) + str.charAt(i + 1)
					+ str.charAt(i) + str.substring(i + 2, str.length());
			array.add(tmpstr);
		}
		for (int i = 0; i < str.length(); i++)// convert
		{
			for (char x = 'a'; x <= 'z'; x++) {
				String tmpstr = str.substring(0, i) + x
						+ str.substring(i + 1, str.length());
				array.add(tmpstr);
			}
		}
		return array;
	}

	public static Set<String> Edit2(String str) {
		Set<String> array = new HashSet<String>();
		array = Edit1(str);
		Set<String> array2 = new HashSet<String>();
		Iterator<String> iter = array.iterator();
		while (iter.hasNext()) {
			String str1 = iter.next();
			array2.addAll(Edit1(str1));
		}
		return array2;
	}

	public static boolean kowns(Set<String> checkset, Set<String> wordset) {
		Iterator<String> iter = checkset.iterator();
		while (iter.hasNext()) {
			String str = iter.next();
			if (!wordset.contains(str))
				iter.remove();
		}
		return checkset.size() > 0;
	}

	public static void main(String[] args) {
		String text = readText(new File("big.txt"));
		ArrayList<String> s = new ArrayList<String>();
		tokenizeAndLowerCase(text, s);
		Map<String, Integer> map = new HashMap<String, Integer>();
		trainPrior(s, map);
		Set<String> keys = map.keySet();
		// System.out.println(map.size());
		Scanner scan = new Scanner(System.in);
		System.out.println("spell correct starting");
		while (true) {
			System.out.println("please input a term:");
			String str = scan.next();
			if ("q".equals(str))
				break;
			Set<String> edit1 = Edit1(str);
			Set<String> edit2 = Edit2(str);
			boolean flag = kowns(new HashSet<String>(Arrays.asList(str)), keys);
			if (flag)
				return;
			Set<String> edit = edit1;
			flag = kowns(edit, keys);
			if (!flag) {
				edit = edit2;
				flag = kowns(edit, keys);
			}
			Iterator<String> iter = edit.iterator();
			int max = 0;
			int tmp = 1;
			String maxStr = null;
			while (iter.hasNext()) {
				String tmpStr = iter.next();
				// System.out.println(tmpStr);
				tmp = map.get(tmpStr);

				if (max < tmp) {
					maxStr = tmpStr;
					max = tmp;
				}
			}
			System.out.println(maxStr);
		}
		System.out.println("spell correct ending");
		// Set<Map.Entry<String,Integer>> allSet=null;
		// allSet=map.entrySet();
		// for(Map.Entry<String,Integer> me : allSet)
		// System.out.println(me.getKey()+"-->"+me.getValue());
	}
}
