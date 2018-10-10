package com.ipanel.join.chongqing.live.manager.impl.hw;

import ipanel.join.ad.widget.ImageAdView;
import ipanel.join.ad.widget.TextAdView;
import android.view.View;
import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.manager.ADManager;
import com.ipanel.join.chongqing.live.manager.IManager;

public class HWADManagerImpl extends ADManager {
	
	int [] AD_RES={R.drawable.live_pausead_01,R.drawable.live_pausead_02,R.drawable.live_pausead_03,R.drawable.live_pausead_04,R.drawable.live_pausead_05};

	private IManager activity;
	public HWADManagerImpl(IManager activity){
		this.activity=activity;
	}
	@Override
	public void onShowAD(int type, View view) {
		// TODO Auto-generated method stub
//		if(view!=null){
//			int index = (int) (Math.random() * AD_RES.length);
//
//			view.setBackgroundResource(AD_RES[index]);
//		}
		if(view!=null){
			int id=-1;
			switch(type){
			case AD_FOR_LIVE_INFO:
				id=110;
				break;
			case AD_FOR_VOLOME:
				id=112;
				break;
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
			case AD_FOR_LIVE_CHANNEL_LIST:
				id=111;
				break;
			case AD_FOR_LIVE_TXT:
				id = 5;
				break;
			case AD_FOR_LIVE_CHANNEL_NUMBER:
				id = 113;
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
			if(view instanceof TextAdView){
				String uri="content://com.ipanel.join.admanager.AdInfoProvider/ad/?adId=%s";
				TextAdView txt_ad=(TextAdView) view;
				txt_ad.setAdUri(String.format(uri, id));
			}
		}
	}
}
