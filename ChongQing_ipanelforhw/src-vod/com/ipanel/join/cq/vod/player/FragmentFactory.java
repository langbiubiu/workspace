package com.ipanel.join.cq.vod.player;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import cn.ipanel.android.LogHelper;

public class FragmentFactory {// 界面管理类
	/**
	 * 数据缓存界面
	 * */
	public static final int FRAGMENT_ID_LOADING=0;
	/**
	 * 频道列表界面
	 * */
	public static final int FRAGMENT_ID_CHANNEL=1;
	/**
	 * 节目列表界面
	 * */
	public static final int FRAGMENT_ID_PROGRAM=2;
	/**
	 * 播放信息界面
	 * */
	public static final int FRAGMENT_ID_PLAY=3;
	/**
	 * 空界面
	 * */
	public static final int FRAGMENT_ID_EMPTY=4;
	/**
	 * 播放准备界面
	 * */
	public static final int FRAGMENT_ID_READY=5;
	/**
	 * 播放输入界面
	 * */
	public static final int FRAGMENT_ID_INPUT=6;
	/**
	 * 声道设置存界面
	 * */
	public static final int FRAGMENT_ID_SOUND_TRACK=7;
	/**
	 * 输入比例设置界面
	 * */
	public static final int FRAGMENT_ID_SCALE=8;
	
	public static final int FRAGMENT_ID_EXIT=9;

	public static final int FRAGMENT_ID_HISTORY=10;
	
	public static final int FRAGMENT_ID_VOLUME=11;
	/**
	 * 推荐
	 */
	public static final int FRAGMENT_ID_RECOMMEND=12;
	/**
	 * 选集界面
	 */
	public static final int FRAGMENT_ID_Anthology=13;
	
	private Context mContext;
	private Activity mActivity;
	private ViewGroup mContainer;
	private FragmentManager fm;
	private int mContainerID;

	public FragmentFactory(ViewGroup container) {
		this.mContainer = container;
		this.mContainerID = container.getId();
		container.setBackgroundColor(Color.parseColor("#00ffffff"));
//		container.setBackgroundResource(R.drawable.broadcast_bg);
		this.mContext = container.getContext();
		this.mActivity = (Activity) mContext;
		fm = mActivity.getFragmentManager();
	}

	public synchronized void showFragment(int id, Object o) {// 显示界面

		BaseFragment current = getCurrentFragment();
		
		LogHelper.i(String.format("show fragment : %d at current fragment : %s", id,current==null?"null":current.getClass().getSimpleName()));

		if (current != null && current.getUID() != -1) {
			if (current.getUID() != id) {
				BaseFragment next = getFragmentByID(id, o);
				fm.beginTransaction().detach(current)
						.attach(next).commit();
			} else {
				current.setObject(o);
				current.refreshFragment();
			}
		} else {
			if (current != null) {
				fm.beginTransaction().remove(current);
			}
			BaseFragment next = getFragmentByID(id, o);
			fm.beginTransaction().attach(next).commit();
		}
		fm.executePendingTransactions();
	}
	
	public BaseFragment getCurrentFragment(){
		Fragment f = fm.findFragmentById(mContainerID);
		if(f instanceof BaseFragment){
			BaseFragment result=(BaseFragment) f;
			return result;
		}else{
			LogHelper.i(String.format("faild to find current fragment ."));
			return null;
		}
	}

	public BaseFragment getFragmentByID(int id, Object key) {
		Fragment f = fm.findFragmentByTag(changeTag(id));
		if (f == null) {
			f = createFragmentByID(id, key);
			LogHelper.d(String.format("create fragment : %s with %d", f.getClass().getSimpleName(),id));
			fm.beginTransaction().add(mContainerID, f, changeTag(id)).commit();
		}
		if(f instanceof BaseFragment){
			BaseFragment result=(BaseFragment) f;
			result.setUID(id);
			result.setObject(key);
			return result;
		}
		throw new RuntimeException(String.format("faild to create fragment by id :",id));
	}

	public String changeTag(int nav) {
		return "_" + nav;
	}

	public BaseFragment createFragmentByID(int id, Object key) {
		switch(id){
		case FRAGMENT_ID_LOADING:
			return new LoadingFragment();
//		case FRAGMENT_ID_CHANNEL:
//			return new ChannelFragment();
//		case FRAGMENT_ID_PROGRAM:
//			return new ProgramFragment();
		case FRAGMENT_ID_EMPTY:
			return new EmptyFragment();
		case FRAGMENT_ID_PLAY:
			return new PlayFragment();
//		case FRAGMENT_ID_READY:
//			return new PlayReadyFragment();
		case FRAGMENT_ID_INPUT:
			return new InputFragment();
		case FRAGMENT_ID_SOUND_TRACK:
			return new SoundTrackFragment();
		case FRAGMENT_ID_SCALE:
			return new ScaleFragment();
		case FRAGMENT_ID_EXIT:
			return new ExitFragment();
		case FRAGMENT_ID_HISTORY:
			return new HistoryFragment();
		case FRAGMENT_ID_VOLUME:
			return new VolumeFragment();
		case FRAGMENT_ID_RECOMMEND:
			return new RecommendFragment();
		case FRAGMENT_ID_Anthology:
			return new AnthologyFragment();
		}
		return null;
	}

}
