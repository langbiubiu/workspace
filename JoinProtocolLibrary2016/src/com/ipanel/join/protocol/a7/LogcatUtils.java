package com.ipanel.join.protocol.a7;

import java.util.ArrayList;

import android.util.Log;

public class LogcatUtils {
	public static ArrayList<String> splitString(String text, int sliceSize) {
		ArrayList<String> textList = new ArrayList<String>();
		String aux;
		int left = -1, right = 0;
		int charsLeft = text.length();
		while (charsLeft != 0) {
			left = right;
			if (charsLeft >= sliceSize) {
				right += sliceSize;
				charsLeft -= sliceSize;
			} else {
				right = text.length();
				aux = text.substring(left, right);
				charsLeft = 0;
			}
			aux = text.substring(left, right);
			textList.add(aux);
		}
		return textList;
	}
	public static ArrayList<String> splitString(String text) {
		return splitString(text, 2000);
	}
	
	public static void splitAndLog(String tag, String text) {
		ArrayList<String> messageList = LogcatUtils.splitString(text);
		for (String message : messageList) {
			Log.d(tag, message);
		}
	}
}