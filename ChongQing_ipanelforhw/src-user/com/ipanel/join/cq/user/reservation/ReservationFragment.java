package com.ipanel.join.cq.user.reservation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.LiveApp;
import com.ipanel.join.chongqing.live.book.Alarms;
import com.ipanel.join.chongqing.live.data.BookData;
import com.ipanel.join.chongqing.live.manager.BookManager;
import com.ipanel.join.cq.user.BaseFragment;
import com.ipanel.join.cq.user.UserActivity;
import com.ipanel.join.cq.vod.utils.Tools;

@SuppressLint("SimpleDateFormat")
public class ReservationFragment extends BaseFragment{
	private List<BookData> recodingList = new ArrayList<BookData>();
	View view;
	ImageView del;
	UserActivity mActivity;
	BookManager manager;
	private boolean isUninstallState ;
	ListView book_list;
	BookAdapter adapter;
	Button del_cancel,del_sure;
	TextView user_book_curpage,user_book_totalpage;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 view = inflater.inflate(R.layout.user_reservation_fragment, container, false);	
		 return view;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity = (UserActivity) getActivity();
		manager = LiveApp.getInstance().getBookManager();
		//manager.init(getActivity());
		isUninstallState = false;
		user_book_curpage = (TextView) view.findViewById(R.id.user_book_curpage);
		user_book_curpage.setText("1/");
		user_book_totalpage = (TextView) view.findViewById(R.id.user_book_totalpage);
		user_book_totalpage.setText(manager.queryBookData().size()+"");
		book_list = (ListView) view.findViewById(R.id.reservation_list);
		book_list.setItemsCanFocus(true);
		book_list.setAdapter(adapter = new BookAdapter());
		book_list.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				if(hasFocus){
					book_list.setSelection(0);
				}
				
			}
			
		});
		if(adapter.getCount()==0){
			Tools.showToastMessage(getActivity(), getResources().getString(R.string.no_book));
		}		
	}
	class BookAdapter extends BaseAdapter {

		@Override
		public int getCount() {			
			recodingList = manager.queryBookData();
			return recodingList == null ? 0 :recodingList.size();
		}

		@Override
		public Object getItem(int position) {
			return recodingList == null ? null : recodingList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		public String getDateToTime(long time) {
		    SimpleDateFormat sf = null;
	        Date d = new Date(time);
	        sf = new SimpleDateFormat("HH:mm");
	        return sf.format(d);
	    }
		public String getDateToString(long time) {
		    SimpleDateFormat sf = null;
	        Date d = new Date(time);
	        sf = new SimpleDateFormat("yyyy/MM/dd");
	        return sf.format(d);
	    }

		@Override
		public View getView(final int position, View container, ViewGroup parent) {
			if (container == null)
				container = LayoutInflater.from(mActivity).inflate(R.layout.user_reservation_item, null, false);
			final BookData book = manager.queryBookData().get(position);
			LinearLayout rl = (LinearLayout) container.findViewById(R.id.user_reservation_item);
			TextView rese_name = (TextView) container.findViewById(R.id.rese_name);
			TextView rese_time = (TextView) container.findViewById(R.id.rese_time);
			TextView rese_tv = (TextView) container.findViewById(R.id.rese_tv);
			ImageView user_rese_del = (ImageView)container.findViewById(R.id.user_rese_del);
						
			final Long time1 = Long.parseLong(book.getStart_time());
			Long duration = Long.parseLong(book.getDuration());
			Long time2 = time1 + duration;
			String date = getDateToString(time1);
			String startTime = getDateToTime(time1);
			String endTime = getDateToTime(time2);
			rese_name.setText(book.getEvent_name());
			rese_time.setText(date+"   "+startTime+"-"+endTime);
			rese_tv.setText(book.getChannel_name());
			
			if(isUninstallState){
				user_rese_del.setVisibility(View.VISIBLE);
			}else{
				user_rese_del.setVisibility(View.GONE);
			}
			container.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					if(hasFocus){
						user_book_curpage.setText(position+1+"");
					}
					
				}
				
			});
			container.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(isUninstallState){
						manager.deleteBook(time1,book.getProgram_number());
						Alarms.setNextAlert(mActivity);
						isUninstallState = true;
						adapter.notifyDataSetChanged();
					}else{
						//TODO播放
					}
				}
			});
			
			return container;
		}
		
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View curView = getActivity().getCurrentFocus();
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU:
			showPopupWindow(curView);
			return true;
		case KeyEvent.KEYCODE_BACK:
			if(isUninstallState){
				isUninstallState = false;
				adapter.notifyDataSetChanged();
				return true;
			}
			getActivity().finish();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			book_list.requestFocus();
			return true;
		}
		return false;
	}			

	private void showPopupWindow(final View v) {
		Context mContext = getActivity().getApplicationContext();
		 View contentView = LayoutInflater.from(mContext).inflate(
	                R.layout.user_pop_window, null);		
		final PopupWindow popupWindow = new PopupWindow(contentView,
                LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, true);
		final View title = view.findViewById(R.id.user_reservation_title);
		title.setFocusable(true);
		title.requestFocus();
		popupWindow.setOnDismissListener(new OnDismissListener() {				
			@Override
			public void onDismiss() {
				v.requestFocus();
				title.setFocusable(false);
			}
		});
      // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
       popupWindow.setBackgroundDrawable(getResources().getDrawable(
              R.drawable.user_buttom));
      // 设置好参数之后再show
        popupWindow.showAtLocation(view,Gravity.BOTTOM,0,0);
        
        LinearLayout delAll = (LinearLayout) contentView.findViewById(R.id.del_all);
		delAll.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				popupWindow.dismiss();
				deleteAll(v);
				
			}
		});
		LinearLayout delOne = (LinearLayout) contentView.findViewById(R.id.del_one);
		delOne.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View mov) {												
					popupWindow.dismiss();
					isUninstallState = true;
					adapter.notifyDataSetChanged();
			}			
		});
    }
	
	protected  void deleteAll(final View v) {
		Context mContext = getActivity().getApplicationContext()  ;
		View contentView = LayoutInflater.from(mContext).inflate(
	                R.layout.user_delall_pop, null);
		final PopupWindow window = new PopupWindow(contentView,
	                LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
		window.setBackgroundDrawable(getResources().getDrawable(
          R.color.back_gray2));
		TextView user_del_tips = (TextView) contentView.findViewById(R.id.user_del_tips);
		user_del_tips.setText(R.string.del_tips_resevation);
		final View title = view.findViewById(R.id.user_reservation_title);
		title.setFocusable(true);
		title.requestFocus();
		window.setOnDismissListener(new OnDismissListener() {				
				@Override
				public void onDismiss() {
					v.requestFocus();
					title.setFocusable(false);
				}
			});
		del_cancel = (Button) contentView.findViewById(R.id.del_cancel);
		
		del_cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				window.dismiss();					
			}
		});
		del_sure = (Button) contentView.findViewById(R.id.del_sure);
		del_sure.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {								
				int size = manager.queryBookData().size();
				for(int i=size-1;i>=0;i--){
					BookData book = manager.queryBookData().get(i);
					final Long time1 = Long.parseLong(book.getStart_time());
					manager.deleteBook(time1,book.getProgram_number());				
			}
				adapter.notifyDataSetChanged();
				window.dismiss();
			}
		 });
		 window.showAtLocation(view,Gravity.TOP|Gravity.LEFT,613,300);
	}
	
}