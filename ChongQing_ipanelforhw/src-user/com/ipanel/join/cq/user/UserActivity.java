package com.ipanel.join.cq.user;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.user.chasedrama.ChaseDramaFragment;
import com.ipanel.join.cq.user.collect.CollectFragment;
import com.ipanel.join.cq.user.flagbook.FlagBookFragment;
import com.ipanel.join.cq.user.history.HistoryFragment;
import com.ipanel.join.cq.user.recoding.MyRecordingsFragment;
import com.ipanel.join.cq.user.reservation.ReservationFragment;
import com.ipanel.join.cq.user.star.StarFragment;
import com.ipanel.join.cq.user.watchlist.WatchListFragment2;
public class UserActivity extends Activity {
	SimpleTab tab;
	int tabIndex ;
	TextView textView;
	FrameLayout content;
	Fragment fg;
	private HistoryFragment historyFragment;
	private WatchListFragment2 watchListFragment2;
	private ChaseDramaFragment chaseDramaFragment;
	private MyRecordingsFragment myRecordingsFragment;
	private ReservationFragment reservationFragment;
	private CollectFragment collectFragment;
	private FlagBookFragment flagBookFragment;
	private StarFragment starFragment;
	private TextView history,watchList,chaseDrama,myRecodings,reservation,collect,
		flagBook,star;
	public LinearLayout history_linear;
	public LinearLayout watch_list_linear;
	public LinearLayout chase_drama_linear;
	public LinearLayout collect_linear;
	public LinearLayout flagbook_linear;
	public LinearLayout star_linear;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_activity_main);		
		init();	
		tab = (SimpleTab) findViewById(R.id.tab);
		tab.setOnTabChangeListener(new SimpleTab.OnTabChangeListener() {
			@Override
			public void onTabChange(int index, View selectedView) {
				switchTab(index);
			}
		});
		tab.getCurrentIndex();		 
	}

	private void init() {

		history = (TextView) findViewById(R.id.history);
		watchList = (TextView) findViewById(R.id.watch_list);				
		chaseDrama = (TextView) findViewById(R.id.chase_drama);
		myRecodings = (TextView) findViewById(R.id.my_recordings);
		reservation = (TextView) findViewById(R.id.reservation);
		collect = (TextView) findViewById(R.id.collect);
		flagBook = (TextView) findViewById(R.id.flagbook);
		star = (TextView) findViewById(R.id.star);
		content = (FrameLayout) findViewById(R.id.content);
		history_linear = (LinearLayout) findViewById(R.id.history_linear);
		watch_list_linear = (LinearLayout) findViewById(R.id.watch_list_linear);
		chase_drama_linear = (LinearLayout) findViewById(R.id.chase_drama_linear);
		collect_linear = (LinearLayout) findViewById(R.id.collect_linear);
		star_linear = (LinearLayout) findViewById(R.id.star_linear);
		flagbook_linear = (LinearLayout) findViewById(R.id.flagbook_linear);
		
	}
	
	protected void switchTab(int index) {	
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment;
		ChangeView(index);		
	    switch(index){
	    case 0:
	    	//观看历史
//	    	if((fragment=fm.findFragmentByTag("" + index))!=null){
//	    		hideFragment(index,fm,ft);
//		    	ft.show(fm.findFragmentByTag("" + index)).commit();
//		    	fg = fm.findFragmentByTag("" + index);
//	    	
//	    	}else{
	    		historyFragment = new HistoryFragment();
	    		ft.add(R.id.content, historyFragment, "" + index);
		    	hideFragment(index,fm,ft);	
		    	ft.show(historyFragment).commit();
		    	fg = historyFragment;
//	    	}
			break;
	    case 1:
	    	//影片收藏

	    	if((fragment=fm.findFragmentByTag("" + index))!=null){
	    		hideFragment(index,fm,ft);
		    	ft.show(fm.findFragmentByTag("" + index)).commit();
		    	fg = fragment;
	    	}else{
	    		watchListFragment2 = new WatchListFragment2();
	    		ft.add(R.id.content, watchListFragment2, "" + index);
		    	hideFragment(index,fm,ft);	
		    	ft.show(watchListFragment2).commit();
		    	fg = watchListFragment2;
	    	}
			break;
	
	    case 2:
	    	//追剧

	    	if((fragment = fm.findFragmentByTag("" + index))!=null){
	    		hideFragment(index,fm,ft);	    	
		    	ft.show(fm.findFragmentByTag("" + index)).commit();
		    	fg = fragment;
	    	}else{
	    		chaseDramaFragment = new ChaseDramaFragment();
	    		ft.add(R.id.content, chaseDramaFragment, "" + index);
		    	hideFragment(index,fm,ft);	    	
		    	ft.show(chaseDramaFragment).commit();
		    	fg = chaseDramaFragment;
	    	}
	    	
			break;

	    case 3:
	    	//我的录制

	    	if(fm.findFragmentByTag("" + index)!=null){
	    		hideFragment(index,fm,ft);	    	
		    	ft.show(fm.findFragmentByTag("" + index)).commit();
		    	fg = fm.findFragmentByTag("" + index);
	    	}else{
	    		myRecordingsFragment = new MyRecordingsFragment();
	    		ft.add(R.id.content, myRecordingsFragment, "" + index);
		    	hideFragment(index,fm,ft);	    	
		    	ft.show(myRecordingsFragment).commit();	
		    	fg = myRecordingsFragment;
	    	}
			break;

	    case 4:
	    	//预约

	    	if(fm.findFragmentByTag("" + index)!=null){
	    		hideFragment(index,fm,ft);	    	
		    	ft.show(fm.findFragmentByTag("" + index)).commit();
		    	fg = fm.findFragmentByTag("" + index);
	    	}else{
	    		reservationFragment = new ReservationFragment();
	    		ft.add(R.id.content, reservationFragment, "" + index);
		    	hideFragment(index,fm,ft);	    	
		    	ft.show(reservationFragment).commit();
		    	fg = reservationFragment;
	    	}
			break;

	    case 5:
	    	//频道收藏

	    	if(fm.findFragmentByTag("" + index)!=null){
	    		hideFragment(index,fm,ft);	    	
		    	ft.show(fm.findFragmentByTag("" + index)).commit();
		    	fg = fm.findFragmentByTag("" + index);
	    	}else{
	    		collectFragment = new CollectFragment();
	    		ft.add(R.id.content, collectFragment, "" + index);
		    	hideFragment(index,fm,ft);	    	
		    	ft.show(collectFragment).commit();
		    	fg = collectFragment;
	    	}
			break;
	    case 6:
	    	//标签订阅
	    	if(fm.findFragmentByTag("" + index)!=null){
	    		hideFragment(index,fm,ft);	    	
		    	ft.show(fm.findFragmentByTag("" + index)).commit();
		    	fg = fm.findFragmentByTag("" + index);
	    	}else{
	    		flagBookFragment = new FlagBookFragment();
	    		ft.add(R.id.content, flagBookFragment, "" + index);
		    	hideFragment(index,fm,ft);	    	
		    	ft.show(flagBookFragment).commit();
		    	fg = flagBookFragment;
	    	}
	    	break;
	    case 7:
	    	//明星关注
	    	if(fm.findFragmentByTag("" + index)!=null){
	    		hideFragment(index,fm,ft);	    	
		    	ft.show(fm.findFragmentByTag("" + index)).commit();
		    	fg = fm.findFragmentByTag("" + index);
	    	}else{
	    		starFragment = new StarFragment();
	    		ft.add(R.id.content, starFragment, "" + index);
		    	hideFragment(index,fm,ft);	    	
		    	ft.show(starFragment).commit();
		    	fg = starFragment;
	    	}
	    	break;
	    }
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(fg instanceof HistoryFragment){		
			if(historyFragment!=null)
				return historyFragment.onKeyDown(keyCode, event);	
		}
		if(fg instanceof WatchListFragment2){
			if(watchListFragment2!=null)
				return watchListFragment2.onKeyDown(keyCode, event);
		}
		if(fg instanceof ChaseDramaFragment){
			if(chaseDramaFragment!=null){
				return chaseDramaFragment.onKeyDown(keyCode, event);
			}
		}
		if(fg instanceof CollectFragment){
			if(collectFragment!=null){
				return collectFragment.onKeyDown(keyCode, event);
			}
		}
		if(fg instanceof MyRecordingsFragment){
			if(myRecordingsFragment!=null){
				return myRecordingsFragment.onKeyDown(keyCode, event);
			}
		}
		if(fg instanceof ReservationFragment){
			if(reservationFragment!=null){
				return reservationFragment.onKeyDown(keyCode, event);
			}
		}
		if(fg instanceof FlagBookFragment){
			if(flagBookFragment!=null){
				return flagBookFragment.onKeyDown(keyCode, event);
			}
		}
		if(fg instanceof StarFragment){
			if(starFragment!=null){
				return starFragment.onKeyDown(keyCode, event);
			}
		}
		return super.onKeyDown(keyCode,event);
	}

	private void hideFragment(int index, FragmentManager fm, FragmentTransaction ft) {
		for(int i=0; i<8; i++){
			if(i!=index){
				BaseFragment fragment = (BaseFragment) fm.findFragmentByTag("" + i);
				if(fragment != null){
					ft.hide(fragment);
				}
			}
		}		
	}


	private void ChangeView(int index) {
		switch(index){
		case 0:
			initText();
			history.setTextSize(TypedValue.COMPLEX_UNIT_PX,44);	
			break;
		case 1:
			initText();
			watchList.setTextSize(TypedValue.COMPLEX_UNIT_PX,44);
			break;
		case 2:
			initText();
			chaseDrama.setTextSize(TypedValue.COMPLEX_UNIT_PX,44);
			break;
		case 3:
			initText();
			myRecodings.setTextSize(TypedValue.COMPLEX_UNIT_PX,44);
			break;
		case 4:
			initText();
			reservation.setTextSize(TypedValue.COMPLEX_UNIT_PX,44);
			break;
		case 5:
			initText();
			collect.setTextSize(TypedValue.COMPLEX_UNIT_PX,44);
			break;
		case 6:
			initText();
			flagBook.setTextSize(TypedValue.COMPLEX_UNIT_PX,44);
			break;
		case 7:
			initText();
			star.setTextSize(TypedValue.COMPLEX_UNIT_PX,44);
		}		
	}	
	private void initText() {
		history.setTextSize(TypedValue.COMPLEX_UNIT_PX,36);
		watchList.setTextSize(TypedValue.COMPLEX_UNIT_PX,36);		
		chaseDrama.setTextSize(TypedValue.COMPLEX_UNIT_PX,36);		
		myRecodings.setTextSize(TypedValue.COMPLEX_UNIT_PX,36);		
		reservation.setTextSize(TypedValue.COMPLEX_UNIT_PX,36);		
		collect.setTextSize(TypedValue.COMPLEX_UNIT_PX,36);
		flagBook.setTextSize(TypedValue.COMPLEX_UNIT_PX,36);
		star.setTextSize(TypedValue.COMPLEX_UNIT_PX,36);
	}
}
