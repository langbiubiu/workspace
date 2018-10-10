package com.ipanel.join.chongqing.live.manager.impl.hw;

import java.util.HashMap;

import com.ipanel.join.chongqing.live.manager.IManager;
import com.ipanel.join.chongqing.live.manager.impl.BaseUIManagerImpl;
import com.ipanel.join.chongqing.live.ui.ChannelGroupFragment;
import com.ipanel.join.chongqing.live.ui.ChannelListFragment;
import com.ipanel.join.chongqing.live.ui.EPGListFragment;
import com.ipanel.join.chongqing.live.ui.InformationFragment;
import com.ipanel.join.chongqing.live.ui.LiveQuitFragment;
import com.ipanel.join.chongqing.live.ui.SeriesFragment;
import com.ipanel.join.chongqing.live.ui.ShiftFragment;
import com.ipanel.join.chongqing.live.ui.ShiftLoadingFragment;
import com.ipanel.join.chongqing.live.ui.ShiftPauseFragment;
import com.ipanel.join.chongqing.live.ui.TVADDFragment;
import com.ipanel.join.chongqing.live.ui.WatchFragment;

public class HWUIManagerImpl extends BaseUIManagerImpl {

	public HWUIManagerImpl(IManager cxt, int root, int mask) {
		super(cxt, root, mask);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initConfig(HashMap<Integer, UIConfig> config) {
		// TODO Auto-generated method stub
		config.put(ID_UI_CQ_LIVE_CHANNEL_GROUP, new UIConfig(ID_UI_CQ_LIVE_CHANNEL_GROUP, ChannelGroupFragment.class, true, FOREVER_DURATION_OF_SHOW_UI));
		config.put(ID_UI_CQ_LIVE_CHANNEL_LIST, new UIConfig(ID_UI_CQ_LIVE_CHANNEL_LIST, ChannelListFragment.class, true, FOREVER_DURATION_OF_SHOW_UI));
		config.put(ID_UI_CQ_LIVE_EVENT_LIST, new UIConfig(ID_UI_CQ_LIVE_EVENT_LIST, EPGListFragment.class, true, FOREVER_DURATION_OF_SHOW_UI));
		config.put(ID_UI_CQ_LIVE_PF, new UIConfig(ID_UI_CQ_LIVE_PF, InformationFragment.class, true, DEFAULT_DURATION_OF_SHOW_UI));
		config.put(ID_UI_CQ_TV_ADD, new UIConfig(ID_UI_CQ_TV_ADD, TVADDFragment.class, true, DEFAULT_DURATION_OF_SHOW_UI));
		config.put(ID_UI_SHIFT_LOADING, new UIConfig(ID_UI_SHIFT_LOADING, ShiftLoadingFragment.class, true, DEFAULT_DURATION_OF_SHOW_UI));
		config.put(ID_UI_WATCH_STATE, new UIConfig(ID_UI_WATCH_STATE, WatchFragment.class, false, FOREVER_DURATION_OF_SHOW_UI));
		config.put(ID_UI_CQ_SHIFT_INFO, new UIConfig(ID_UI_CQ_SHIFT_INFO, ShiftFragment.class, true, DEFAULT_DURATION_OF_SHOW_UI));
		config.put(ID_UI_SHIFT_QUIT, new UIConfig(ID_UI_SHIFT_QUIT, ShiftPauseFragment.class, true, FOREVER_DURATION_OF_SHOW_UI));
		config.put(ID_UI_CQ_SERIES, new UIConfig(ID_UI_CQ_SERIES, SeriesFragment.class, true, FOREVER_DURATION_OF_SHOW_UI));
		config.put(ID_UI_CQ_LIVE_QUIT, new UIConfig(ID_UI_CQ_LIVE_QUIT, LiveQuitFragment.class, true, FOREVER_DURATION_OF_SHOW_UI));
	}

}
