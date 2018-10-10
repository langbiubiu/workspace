package com.ipanel.join.cq.user;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.ipanel.chongqing_ipanelforhw.R;

public class UserTitle extends LinearLayout{
	public UserTitle (Context context,AttributeSet attrs){
		super(context,attrs);
		LayoutInflater.from(context).inflate(R.layout.back_tip_layout, this);
	}
	
}
