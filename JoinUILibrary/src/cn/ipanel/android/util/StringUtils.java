package cn.ipanel.android.util;

public class StringUtils {

	public static boolean isCJK(String str) {
		int length = str.length();
		for (int i = 0; i < length; i++) {
			char ch = str.charAt(i);
			Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
			if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)
					|| Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)
					|| Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)) {
				return true;
			}
		}
		return false;
	}

	public static String[] splitString(String str, int length) {
		return str.split("(?<=\\G.{" + length + "})");
	}
	
	public static String toString(Object obj){
		if(obj == null)
			return "null";
		return obj.toString();
	}
	
	public static String toHex(byte[] bytes) {
		StringBuffer buffer = new StringBuffer();
		for (byte b : bytes)
			buffer.append(String.format("%02x", b));
		return buffer.toString();
	}
}
