package com.ipanel.join.chongqing.portal;

import com.ipanel.chongqing_ipanelforhw.CQApplication;

import android.view.View;
import android.view.View.OnClickListener;

public class RefreshClickListener implements OnClickListener{

	@Override
	public void onClick(View arg0) {
		CQApplication.getInstance().getRecsPage2Data();
	}
}
