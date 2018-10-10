package com.ipanel.join.cq.vod.player;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;

import com.ipanel.chongqing_ipanelforhw.R;
/**
 * 播放下一集
 * @author wuhd
 *
 */
public class NextTeleplayFragment extends BaseFragment {
	TextView yes_btn,no_btn;
	ImageView lastview;//上次视图
	TextView tvName;//电视名称
	TextView tv;//下一集
	SimplePlayerActivity activity;
	int currentNum;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		activity = (SimplePlayerActivity) root.getContext();
		ViewGroup container = (ViewGroup) (inflater.inflate(
				R.layout.vod_next_tele_fragment_layout, root, false));
		
		yes_btn = (TextView) container.findViewById(R.id.btn_continue);
		no_btn = (TextView) container.findViewById(R.id.btn_restart);
		tvName = (TextView) container.findViewById(R.id.vod_tvname);
		tv = (TextView) container.findViewById(R.id.vod_continue_text);
		lastview = (ImageView)container.findViewById(R.id.img);//上次画面
		
		yes_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				VodDataManager.getInstance(activity).requestTelevisionPlayUrl(mActivity.getHwResponse(), currentNum + 1);
			}
		});
		
		no_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.finish();
			}
		});
		return container;
	}

	@Override
	public void onResume() {
		super.onResume();
		currentNum = Integer.parseInt(activity.getNum()) - 1;//当前下标，从0开始
		Log.i("wuhd", "currentNum="+currentNum);
		if(activity.getHwResponse()!=null&&activity.getHwResponse().getVod()!=null&&activity.getHwResponse().getVod().size()>0){
			//已经播放结束当前集
			//tvName.setText(activity.getHwResponse().getVod().get(currentNum).getName()+getString(R.string.play_over));
			tvName.setText(activity.getString(R.string.vod_next_one));
			if(currentNum < activity.getHwResponse().getVod().size() - 1){//不超过最后一集
				//是否开始播放下一集
				tv.setText(getString(R.string.play_next) + activity.getHwResponse().getVod().get(currentNum + 1).getName());
			}
		}
		
		if(activity.getHwResponse()!=null && activity.getHwResponse().getWiki()!=null){
			String url = "";
			if(activity.getHwResponse().getWiki().getPosters()!=null&&activity.getHwResponse().getWiki().getPosters().size()>0){
				int size = activity.getHwResponse().getWiki().getPosters().size();
				int d = (int) (Math.random() * size);
				url = activity.getHwResponse().getWiki().getPosters().get(d);
			}
			SharedImageFetcher.getSharedFetcher(activity).loadImage(url, lastview);
		}
	}

	@Override
	public void showFragment() {
		yes_btn.requestFocus();
		lastview.setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE){
			getActivity().finish();
			return true;
		}
		if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT||keyCode==KeyEvent.KEYCODE_DPAD_RIGHT
				||keyCode==KeyEvent.KEYCODE_DPAD_DOWN||keyCode==KeyEvent.KEYCODE_DPAD_UP){
			changeFocus(keyCode);
			return true;
		}
		return super.onKeyDown(keyCode,event);
	}

}
