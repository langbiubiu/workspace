package cn.ipanel.smackui;

public class Utils {

	public static String getUidInJID(String jidString) {
		int idx = jidString.indexOf('@');
		if (idx != -1)
			return jidString.substring(0, idx);
		idx = jidString.indexOf('/');
		if (idx != -1)
			return jidString.substring(0, idx);
		return jidString;
	}

}
