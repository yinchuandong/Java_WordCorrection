package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

public class FileUtil {

	/**
	 * 将 int 的 big-edian 转为 little-endian
	 * @param data
	 * @return
	 */
	public static int convertIntBigToLittle(int data) {
		int offset = 0;
		byte[] array = new byte[4];
		array[offset + 3] = (byte) ((data >>> 24) & 0xFF);
		array[offset + 2] = (byte) ((data >>> 16) & 0xFF);
		array[offset + 1] = (byte) ((data >>> 8) & 0xFF);
		array[offset] = (byte) ((data >>> 0) & 0xFF);
		int ch1 = array[offset];
		int ch2 = array[offset + 1];
		int ch3 = array[offset + 2];
		int ch4 = array[offset + 3];
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	/**
	 * 将 long 的 big-endian 转为 little-endian
	 * @param data
	 * @return
	 */
	public static long convertLongBigToLittle(long data) {
		int offset = 0;
		byte[] array = new byte[8];
		array[offset + 7] = (byte) (data >>> 56);
		array[offset + 6] = (byte) (data >>> 48);
		array[offset + 5] = (byte) (data >>> 40);
		array[offset + 4] = (byte) (data >>> 32);
		array[offset + 3] = (byte) (data >>> 24);
		array[offset + 2] = (byte) (data >>> 16);
		array[offset + 1] = (byte) (data >>> 8);
		array[offset + 0] = (byte) (data >>> 0);
		return (((long) array[offset] << 56) +
		((long) (array[offset + 1] & 255) << 48) +
		((long) (array[offset + 2] & 255) << 40) +
		((long) (array[offset + 3] & 255) << 32) +
		((long) (array[offset + 4] & 255) << 24) +
		((array[offset + 5] & 255) << 16) +
		((array[offset + 6] & 255) << 8) +
		((array[offset + 7] & 255) << 0));
	}
	
	/**
	 * int 转为byte数组，字节数组的低位是整型的低字节位, little-endian的形式
	 * @param iSource 要转换的整形
	 * @param iArrayLen 用来保存结果的数组的长度
	 * @return
	 */
	public static byte[] intToByteArray(int iSource, int iArrayLen) {
	    byte[] bLocalArr = new byte[iArrayLen];
	    for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
	        bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
	    }
	    return bLocalArr;
	}

	/**
	 * 将byte数组bRefArr转为一个整数,字节数组的低位是整型的低字节位, little-endian
	 * @param bRefArr
	 * @return
	 */
	public static int byteArrayToInt(byte[] bRefArr) {
	    int iOutcome = 0;
	    byte bLoop;

	    for (int i = 0; i < bRefArr.length; i++) {
	        bLoop = bRefArr[i];
	        iOutcome += (bLoop & 0xFF) << (8 * i);
	    }
	    return iOutcome;
	}

	public static void readBinFromC(String filename) {
		try {
			File srcFile = new File(filename);
			FileInputStream ins = new FileInputStream(srcFile);

			long fileLen = srcFile.length();
			System.out.println("filelen" + fileLen);

			byte[] countBuff = new byte[4];
			ins.read(countBuff, 0, 4);
			int count = byteArrayToInt(countBuff);

			int buffSize = 256 + 256 + 4;
			byte[] buff = new byte[buffSize];
			ins.read(buff, 0, 256);
			String f_name = new String(buff, 0, 256).replaceAll("\\x00", "");
			System.out.println(f_name.length());

			ins.read(buff, 256, 256);
			String l_name = new String(buff, 256, 256);
			
			ins.read(buff, 256 + 256, 4);
			int age = byteArrayToInt(Arrays.copyOfRange(buff, 512, 512 + 4));
			
			ins.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		readBinFromC("/Users/yinchuandong/cproject/stra/stra/data.bin");
	}
}
