package com.ipanel.join.cq.user.recoding;

import android.content.Context;
import android.content.Intent;
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
import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.chongqing.live.util.TimeHelper;
import com.ipanel.join.cq.user.BaseFragment;
import com.ipanel.join.cq.user.UserActivity;
import com.ipanel.join.cq.vod.player.SimplePlayerActivity;
import com.ipanel.join.cq.vod.utils.Tools;

public class MyRecordingsFragment extends BaseFragment{
	View view;
	ImageView del;
	UserActivity mActivity;
//	RecordDataBaseHelper mDBHelper;
	private boolean isUninstallState = false;
	ListView record_list;
	RecordAdapter adapter;
	Button del_cancel,del_sure;
	TextView user_record_curpage,user_record_totalpage;
	TextView record;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 view = inflater.inflate(R.layout.user_recodings_fragment, container, false);	
		 return view;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity = (UserActivity) getActivity();
//		mDBHelper = RecordDataBaseHelper.getInstance();
//		mDBHelper.init(getActivity());
		record_list = (ListView) view.findViewById(R.id.recoding_list);
		record_list.setItemsCanFocus(true);
		user_record_curpage = (TextView) view.findViewById(R.id.user_record_curpage);
		user_record_curpage.setText("0/");
		user_record_totalpage = (TextView) view.findViewById(R.id.user_record_totalpage);
//		user_record_totalpage.setText(mDBHelper.getEvents().size()+"");
		record = (TextView) view.findViewById(R.id.record);
//		if(mDBHelper.getEvents().size() !=0){
//			record.setVisibility(View.VISIBLE);
//		}
		adapter = new RecordAdapter();
		record_list.setAdapter(adapter);
		record_list.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				if(hasFocus){
				record_list.setSelection(0);	
				}
			}
			
		});

		if(adapter.getCount()==0){
			Tools.showToastMessage(getActivity(), getResources().getString(R.string.no_record));
			
		}
			

	}
	class RecordAdapter extends BaseAdapter {

		@Override
		public int getCount() {
//			return mDBHelper.getEvents() == null ? 0 : mDBHelper.getEvents().size();
			return 10;
		}

		@Override
		public Object getItem(int position) {
//			return mDBHelper.getEvents() == null ? null : mDBHelper.getEvents().get(position);
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View container, ViewGroup parent) {
			if (container == null)
				container = LayoutInflater.from(mActivity).inflate(R.layout.user_recoding_item, null, false);
//			final Wiki event = mDBHelper.getEvents().get(position);
//			LogHelper.i("RecordFragment event name = " + event.getEvent_name() + ", start time = " + event.getStart_time());
//			LinearLayout rl = (LinearLayout) container.findViewById(R.id.user_recod_item);
//			TextView recording_name = (TextView) container.findViewById(R.id.recd_name);
//			TextView recd_tv = (TextView) container.findViewById(R.id.recd_tv);
//			TextView has_recorded = (TextView) container.findViewById(R.id.has_recorded);
//			ImageView user_recode_del = (ImageView)container.findViewById(R.id.user_recode_del);
//			
//			recording_name.setText(event.getEvent_name());
//			recd_tv.setText(event.getChannel_name());
//			if (TimeHelper.isPlayed(event.getStart_time(), 0, System.currentTimeMillis()/1000)) {
//				has_recorded.setText("已录制");
//			} else {
//				has_recorded.setText("已订录");
//			}			
//			if(isUninstallState){
//				user_recode_del.setVisibility(View.VISIBLE);
//			}else{
//				user_recode_del.setVisibility(View.GONE);
//			}
			container.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					if(hasFocus){
						user_record_curpage.setText(position+1+"/");
					}
					
				}
				
			});
			container.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {/*
					
					if(isUninstallState){
						mDBHelper.delete(event);		
						adapter.notifyDataSetChanged();
					}else{
						//TODO播放
						Intent intent = new Intent(getActivity(), SimplePlayerActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("vodId", event.event_id+"");
						intent.putExtra("playType", "4");
						intent.putExtra("name", event.event_name);
						startActivity(intent);
					}
				*/}
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
			record_list.requestFocus();
			return true;
		}
		return false;
	}			

	private void showPopupWindow(final View v) {
		final View curView = v;
		Context mContext = getActivity().getApplicationContext()  ;
		 View contentView = LayoutInflater.from(mContext).inflate(
	                R.layout.user_pop_window, null);		
		final PopupWindow popupWindow = new PopupWindow(contentView,
                LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, true);
		final View title = view.findViewById(R.id.user_record_title);
		title.setFocusable(true);
		title.requestFocus();
		  popupWindow.setOnDismissListener(new OnDismissListener() {				
				@Override
				public void onDismiss() {
					curView.requestFocus();
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
				deleteAll(curView);				
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
		user_del_tips.setText(R.string.del_tips_recode);
		final View title = view.findViewById(R.id.user_record_title);
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
			public void onClick(View arg0) {/*								
				int size = mDBHelper.getEvents().size();
				for(int i=size-1;i>=0;i--){
					mDBHelper.delete(mDBHelper.getEvents().get(i));				
			}
				adapter.notifyDataSetChanged();
				window.dismiss();
			*/}
		 });
		 window.showAtLocation(view,Gravity.TOP|Gravity.LEFT,613,300);
	}
	
}
