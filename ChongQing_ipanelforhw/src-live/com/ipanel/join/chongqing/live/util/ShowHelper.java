package com.ipanel.join.chongqing.live.util;

import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import android.view.KeyEvent;
import android.view.RcKeyEvent;
import cn.ipanel.android.LogHelper;

import com.ipanel.join.chongqing.live.Constant;

public class ShowHelper {
	public static String getShowChannel(int channel) {
		channel=channel%1000;
		String result = "";
		if (channel > 99) {
			result = channel + "";
		} else if (channel > 9) {
			result = "0" + channel + "";
		} else {
			result = "00" + channel + "";
		}
		return result;
	}
	
	public static boolean isPFValid(Program present ,Program follow){
		LogHelper.i("isPFValid present = "+present+", follow="+follow);
		if (present != null && follow != null) {
			long now = System.currentTimeMillis();
			long start = present.getStart();
			long end = present.getEnd();
			long n_start = follow.getStart();

//			if (start <= now && now <= end && n_start >= end) {
//				LogHelper.i("check pf ,yes");
//				return true;
//			}
			if(TimeHelper.isPlaying(start, end, now)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 将按键转换成统一按键
	 * */
	public static int changeGlobalKeyCode(KeyEvent event){
		return Constant.DTV_LIB_EXIST?RcKeyEvent.getRcKeyCode(event):event.getKeyCode();
	}

	public static int getIntegerAtIndex(int value,int count,int index){
		if(index>count-1){
			return 0;
		}
		String s=channgeIntegerToString(value,count);		
		return Integer.parseInt(s.substring(index,index+1));
	}
	public static String channgeIntegerToString(int value,int count){
		String tmp=value+"";
		int add=count-tmp.length();
		for(int i=0;i<add;i++){
			tmp="0"+tmp;
		}
		return tmp;
	}
	public static int getArrayValue(int [] values){
		int length = values.length;
		if (length == 0) {
			return 0;
		} else {
			int value = 0;
			for (int i = 0; i < length; i++) {
				value += Math.pow(10, length -1- i)*values[i];
			}
			return value;
		}
	}
}
