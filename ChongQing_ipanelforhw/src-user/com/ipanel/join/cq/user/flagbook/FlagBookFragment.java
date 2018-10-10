package com.ipanel.join.cq.user.flagbook;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import cn.ipanel.android.widget.WeightGridLayout;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.join.cq.user.BaseFragment;
import com.ipanel.join.cq.user.UserActivity;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.player.VodDataManager;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class FlagBookFragment extends BaseFragment{
	private View view;
	private TextView user_flag_curpage,user_flag_totalpage,flag;
	private ViewGroup container;
	private UserActivity mActivity;
	private int itemMargins = 34;//标签的距离
	private int lineMargins = 40;//行间距
	private boolean isUninstallState = false;
	private LinearLayout delAll,delOne;
	private View title;
	private Button del_cancel,del_sure;
	private List<String> wikiList;
	protected ServiceHelper serviceHelper;
	private int row = 0;//总行数
	private ImageView user_flag_del;
	private LinearLayout flagbook_linear;
	/**
	 * 刷新页面
	 */
	public static final int MSG_UPDATE_VIEW = 1;
	/**
	 * 刷新焦点
	 */
	public static final int MSG_REFRESH_FOCUS = 2;
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what){
			case MSG_UPDATE_VIEW:
				refreshView();
				break;
			case MSG_REFRESH_FOCUS:
				View focusView = (View) msg.obj;
				focusView.requestFocus();
				break;
			}
		}
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 view = inflater.inflate(R.layout.user_flagbook_fragment, container, false);	
		 return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity = (UserActivity) getActivity();
		serviceHelper = ServiceHelper.getHelper();
		initView();
		Thread mThread = new Thread(initThread);
		mThread.start();
    }
	private Runnable initThread = new Runnable() {

		@Override
		public void run() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// 获取数据
					initData();
				}
			});
		}
	};
	
	private void initData(){
		
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_SUBSCRIBE_TAGS);
		serviceHelper.setRootUrl(HWDataManager.ROOT_URL);
		serviceHelper.setSerializerType(SerializerType.JSON);
		
		serviceHelper.callServiceAsync(getActivity(), request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success && result.getError().getCode() == 0){
					int total = result.getTotal();
					wikiList = result.getTags();
					if(wikiList != null && wikiList.size() > 0){
						Log.i("","start refreshview--");
						mHandler.obtainMessage(MSG_UPDATE_VIEW).sendToTarget();
					}else{
						Tools.showToastMessage(getActivity(), getResources().getString(R.string.no_flagbook));
					}
				}else{
					Tools.showToastMessage(getActivity(), result.getError().getInfo());
				}
			}
		});

	}
	     																								

	private void refreshView() {
		Log.i("", "refresh view here");
		final int containerWidth = container.getMeasuredWidth() - container.getPaddingRight()  
                - container.getPaddingLeft();
		final LayoutInflater inflater = mActivity.getLayoutInflater();
		
		/** 用来测量字符的宽度 */  
        final Paint paint = new Paint();  
        final LinearLayout item = (LinearLayout) inflater.inflate(R.layout.user_flag_item, null);
        final TextView label_name = (TextView) item.findViewById(R.id.user_flag_name);
        final int itemPadding = label_name.getCompoundPaddingLeft() + label_name.getCompoundPaddingRight();
        final LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,  
                LayoutParams.WRAP_CONTENT); 
		tvParams.setMargins(0, 0, itemMargins, 0);
		
		paint.setTextSize(label_name.getTextSize()); 
		
		LinearLayout layout = new LinearLayout(mActivity);  
        layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));  
        layout.setOrientation(LinearLayout.HORIZONTAL);  
        container.addView(layout);
        
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,  
                LayoutParams.WRAP_CONTENT);  
        params.setMargins(0, lineMargins, 0, 0);
        		                         
        /** 一行剩下的空间 **/  
        //int remainWidth = containerWidth;  
    	int remainWidth = 1430;
        Log.i("remainWidth",remainWidth+"");
        for ( int i = 0; i < wikiList.size(); ++i) {  
            final String text = wikiList.get(i);  
            final float itemWidth = paint.measureText(text) + itemPadding;  
            if (remainWidth > itemWidth) {
                addItemView(inflater, layout, tvParams, text);  
            } else {  
            	resetFrameLayoutMarginsRight(layout);  
                layout = new LinearLayout(mActivity);
                layout.setLayoutParams(params);  
                layout.setOrientation(LinearLayout.HORIZONTAL);  
                /** 将前面那一个textview加入新的一行 */  
                addItemView(inflater, layout, tvParams, text);  
                container.addView(layout);  
                remainWidth = 1430;  
        }  
        remainWidth = (int) (remainWidth - itemWidth + 0.5f) - itemMargins;  
        }  
        resetFrameLayoutMarginsRight(layout);
        row = container.getChildCount();
        user_flag_totalpage.setText(row+"");
	}

	private void initView() {
		user_flag_curpage = (TextView) view.findViewById(R.id.user_flag_curpage);
		user_flag_curpage.setText("0/");
		user_flag_totalpage = (TextView) view.findViewById(R.id.user_flag_totalpage);
		user_flag_totalpage.setText(row+"");
		flag = (TextView) view.findViewById(R.id.flag);
		container = (ViewGroup) view.findViewById(R.id.container);		
		title = view.findViewById(R.id.user_flag_title);
		flagbook_linear = mActivity.flagbook_linear;
		//user_flag_del = (ImageView) view.findViewById(R.id.user_flag_del);
	}
	
	  /***************** 将每行最后一个textview的MarginsRight去掉 *********************************/  
    private void resetFrameLayoutMarginsRight(ViewGroup viewGroup) {
    	
        final LinearLayout tempTextView = (LinearLayout) viewGroup.getChildAt(viewGroup.getChildCount() - 1);

        tempTextView.setLayoutParams
        	(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));  
    }  
  
    private void addItemView(LayoutInflater inflater, final ViewGroup viewGroup, LayoutParams params, final String text) {  
        final LinearLayout item = (LinearLayout) inflater.inflate(R.layout.user_flag_item, null);
        TextView tvItem = (TextView) item.findViewById(R.id.user_flag_name);
        final ImageView tvDel = (ImageView) item.findViewById(R.id.user_flag_del);
        tvItem.setText(text);
        item.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(isUninstallState){
					//TODO删除
					final int row = findPosition(viewGroup,container);
					final int position = findPosition(item,viewGroup);
					//TODO
					mActivity = (UserActivity) getActivity();
					delSubscribeTag(text);					
					container.removeAllViews();
					Thread mThread = new Thread(initThread);
					mThread.start(); 
					final int count = viewGroup.getChildCount();
					final Timer timer = new Timer(true);
					timer.schedule(new TimerTask(){			
				      public void run(){
				    	  if(position != count-1){						
							LinearLayout ll = (LinearLayout)container.getChildAt(row);
							View nextView = ll.getChildAt(position);
							mHandler.obtainMessage(MSG_REFRESH_FOCUS, nextView).sendToTarget();
							}else if(position == count-1 && position == 0){
								LinearLayout nextLinearLayout1 = (LinearLayout)container.getChildAt(row-1);
								View nextView = nextLinearLayout1.getChildAt(0);
								mHandler.obtainMessage(MSG_REFRESH_FOCUS, nextView).sendToTarget();
							}else if(position == count-1 && position != 0){
								LinearLayout ll = (LinearLayout)container.getChildAt(row);
								View nextView = ll.getChildAt(position-1);
								mHandler.obtainMessage(MSG_REFRESH_FOCUS, nextView).sendToTarget();
							}						
						}		    	  					       
					   }, 500);									
					Toast.makeText(mActivity, "shanchu", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(mActivity, "dianji", Toast.LENGTH_SHORT).show();
				}				
			}        	
        });
        item.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {								
				if(hasFocus){
					int row = findPosition(viewGroup,container);
					user_flag_curpage.setText(row+1+"/");
					if(isUninstallState){
						tvDel.setVisibility(View.VISIBLE);
					}						
				}else{
					tvDel.setVisibility(View.GONE);
				}												
			}        	
        });
        item.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
						if(findPosition(viewGroup,container)==0){
							return true;
						}
					}else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
						if(findPosition(viewGroup,container)==(container.getChildCount()-1)){
							return true;
						}
					}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
						if(findPosition(item,viewGroup) == viewGroup.getChildCount()-1){
							return true;
						}
					}					
				}
				return false;
			}        	
        });
        viewGroup.addView(item, params);  
    }
    //TODO 删除订阅
	protected void delSubscribeTag(String tag) {
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_DEL_SUBSCRIBE_TAG);
		request.getParam().setTag(tag);
		VodDataManager.getInstance(getActivity()).getHwData(request);
	}

	
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	View curView = getActivity().getCurrentFocus();
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU:				
				showPopupWindow(curView);				
			return true;
		case KeyEvent.KEYCODE_BACK:
			if(isUninstallState){
				ImageView v = (ImageView)curView.findViewById(R.id.user_flag_del);
				v.setVisibility(View.GONE);
				isUninstallState = false;
				return true;
			}
			getActivity().finish();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if(flagbook_linear.hasFocus()){
				container.getChildAt(0).requestFocus();
				return true;
			}else{
				return false;
			}
		}		
		return false;
	}
    private void showPopupWindow(View view2) {
		final View v = view2;
		Context mContext = getActivity().getApplicationContext();
		 View contentView = LayoutInflater.from(mContext).inflate(
	                R.layout.user_pop_window, null);		
		final PopupWindow popupWindow = new PopupWindow(contentView,
                LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, true);		
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
        
        delAll = (LinearLayout) contentView.findViewById(R.id.del_all);
		delAll.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				popupWindow.dismiss();
				deleteAll(v);					
			}				
		});
		delOne = (LinearLayout) contentView.findViewById(R.id.del_one);
		delOne.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View mov) {												
				popupWindow.dismiss();
				isUninstallState = true;
				//TODO 
				ImageView user_flag_del = (ImageView) v.findViewById(R.id.user_flag_del);
				
				if(user_flag_del==null){
					return;
				}else{
					user_flag_del.setVisibility(View.VISIBLE);									
				}
			}
		});
    }
    protected  void deleteAll(final View v) {
		Context mContext = mActivity.getApplicationContext();
		View contentView = LayoutInflater.from(mContext).inflate(
	                R.layout.user_delall_pop, null);
		final PopupWindow window = new PopupWindow(contentView,
	                LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
		window.setBackgroundDrawable(getResources().getDrawable(
          R.color.back_gray2));
		TextView user_del_tips = (TextView) contentView.findViewById(R.id.user_del_tips);
		user_del_tips.setText(R.string.del_tips_flag);
		final View title = view.findViewById(R.id.user_flag_title);
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
				deleteAll();				
			}							 
		});
		window.showAtLocation(view,Gravity.TOP|Gravity.LEFT,613,300);
	}
    private void deleteAll() {
		for(int i=0 ; i<wikiList.size() ; i++){
			delSubscribeTag(wikiList.get(i));
		}
		refreshView();		
	}
    int position=0;
    private int findPosition(View view,ViewGroup parentView){
    	for(int i =0 ; i<parentView.getChildCount();i++){
    		if (view == parentView.getChildAt(i)){
    			position = i;
    			break;
    		}
    	}
    	return position;    	
    }
}
