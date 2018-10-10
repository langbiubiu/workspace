package com.ipanel.join.cqhome.view;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.Utils;
import ipanel.join.configuration.View;
import ipanel.join.widget.FrameLayout;
import ipanel.join.widget.IConfigViewGroup;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

public class ColumeLayout extends FrameLayout implements IConfigViewGroup {
	public static final String PROP_FOCUS_SCALE = "focusScale";

	public String mFocusScaleID="none";
	android.view.View scale;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@SuppressLint("NewApi")
	public ColumeLayout(Context context, View data) {
		super(context, data);
		Bind bind = data.getBindByName(PROP_FOCUS_SCALE);
		if(bind != null){
			mFocusScaleID=bind.getValue().getvalue();
		}
		setOnFocusChangeListener(new OnFocusChangeListener() {
			@SuppressLint("NewApi")
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
			@Override
			public void onFocusChange(android.view.View v, boolean hasFocus) {
				scale=null;
				if(!"none".equals(mFocusScaleID)){
					scale=Utils.findViewByConfigId(getRootView(), mFocusScaleID);
				}
				if(scale!=null){
					scale.bringToFront();
					if(hasFocus){
						scale.animate().scaleX(1.05f);
						scale.animate().scaleY(1.05f);
					}else{
						scale.animate().scaleX(1.0f);
						scale.animate().scaleY(1.0f);
					}
				}
			}
		});
	}


}
