package com.ipanel.join.chongqing.live.manager.impl;

import ipanel.join.ad.widget.ImageAdView;
import android.view.View;
import cn.ipanel.android.LogHelper;

import com.ipanel.join.chongqing.live.manager.ADManager;
import com.ipanel.join.chongqing.live.manager.IManager;

public class ADManagerImpl extends ADManager {
	private IManager activity;
	public ADManagerImpl(IManager activity){
		this.activity=activity;
	}
	@Override
	public void onShowAD(int type, View view) {
		// TODO Auto-generated method stub
		if(view!=null){
			int id=-1;
			switch(type){
			case AD_FOR_LIVE_INFO:
			case AD_FOR_VOLOME:
			case AD_FOR_SEEK:
			case AD_FOR_SHIFT_INFO:
				id=1;
				break;
			case AD_FOR_LIVE_RECOMEND:
				break;
			case AD_FOR_LIVE_EPG:
			case AD_FOR_SHIFT_EPG:
				id=2;
				break;
			case AD_FOR_SHIFT_LOADING:
			case AD_FOR_SHIFT_QUIT:
			case AD_FOR_SHIFT_PAUSE:
			case AD_FOR_SHIFT_ERROR:
				id=4;
				break;
			case AD_FOR_BROADCAST:
				id=3;
				break;
			}
			if(id<0){
				LogHelper.i("can't find id for type :"+type);
				return ;
			}
			if(view instanceof ImageAdView){
				String uri="content://com.ipanel.join.admanager.AdInfoProvider/ad/?adId=%s";
				ImageAdView ad=(ImageAdView) view;
				ad.setAdUri(String.format(uri, id));
			}
		}
	}

}
