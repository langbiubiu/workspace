package com.ipanel.join.cq.vod.player;

import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.player.VodDataManager.HwResponseCallBack;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
/**
 * 退出挽留
 * @author wuhd
 *
 */
public class ExitFragment extends BaseFragment {
	private final static int MSG_RECOMMEND_LIVE_DATA_COMPLETE = 0;
	private final static int MSG_RECOMMEND_VOD_DATA_COMPLETE = 1;
	List<Wiki> channels;
	List<Wiki> vods;
	ServiceHelper serviceHelper;
	ImageFetcher mFetcher;
	WeightGridLayout live_grid, vod_grid;
	Button close;
	private SimplePlayerActivity mActivity;
	ImageView imageAd;//广告图
	Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_RECOMMEND_LIVE_DATA_COMPLETE:
				live_grid.setAdapter(new LiveGridAdapter());
				live_grid.requestFocus();
				break;
			case MSG_RECOMMEND_VOD_DATA_COMPLETE:
				vod_grid.setAdapter(new VodGridAdapter());
				break;
			default:
				break;
			}
			
		};
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.live_fragment_quit, root, false);
		live_grid = (WeightGridLayout) vg.findViewById(R.id.current_live);
		vod_grid = (WeightGridLayout) vg.findViewById(R.id.recommend_vod);
		imageAd = (ImageView)vg.findViewById(R.id.quit_ad);//广告
		
		mFetcher = SharedImageFetcher.getNewFetcher(root.getContext(), 1);
		serviceHelper = ServiceHelper.createOneHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		close = (Button) vg.findViewById(R.id.pop_close);
		close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_EMPTY, null);
			}
		});
		imageAd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				return;
			}
		});
		mActivity = (SimplePlayerActivity) getActivity();
		initControl();
		return vg;
	}

	private void initControl() {
		VodDataManager.getInstance(mActivity).setHwResponseCallBack(new HwResponseCallBack() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result, String action) {
				if(action.equals(HWDataManager.ACTION_GET_LIVE_PROGRAM_BY_RECOMMEND)){
					//直播推荐
					if(success && result!=null && result.getError().getCode()==0){
						channels = result.getWikis();
						if(channels!=null && channels.size()>0)
							mHandler.sendEmptyMessage(MSG_RECOMMEND_LIVE_DATA_COMPLETE);
					}
				}else if(action.equals(HWDataManager.ACTION_GET_SIMILAR_WIKIS)
						|| action.equals(HWDataManager.ACTION_GET_WIKIS_BY_HOT)){
					//点播推荐
					if(success && result!=null && result.getError().getCode()==0){
						vods = result.getWikis();
						if(vods!=null && vods.size()>0)
							mHandler.sendEmptyMessage(MSG_RECOMMEND_VOD_DATA_COMPLETE);
					}
				}
			}
		});
	}

	@Override
	public void showFragment() {
		requestLiveRecommend();
		String id = mActivity.getWikiId();
		if(TextUtils.isEmpty(id)){
			getWikisByHot();
		}else{
			requestVodRecommend(mActivity.getWikiId());
		}
	}
	private void getWikisByHot() {
		//获取大家都在看
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_WIKIS_BY_HOT);
		request.getParam().setPage(1);
		request.getParam().setPagesize(5);
		VodDataManager.getInstance(mActivity).getHwData(request);
	}
	/**
	 * 获取直播推荐
	 */
	private void requestLiveRecommend() {
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_LIVE_PROGRAM_BY_RECOMMEND);
		request.getParam().setPage(1);
		request.getParam().setPagesize(5);
		VodDataManager.getInstance(mActivity).getHwData(request);
	}
	
	private void requestVodRecommend(String vodId) {
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_SIMILAR_WIKIS);
		request.getParam().setWikiId(vodId);
		request.getParam().setPage(1);
		request.getParam().setPagesize(5);
		VodDataManager.getInstance(mActivity).getHwData(request);
	}
	
	private void getServiceId(long channelid) {/*
		Log.i("SearchPage", "getServiceId");
		if(StbApp.isLogin()){
			String url = HomedAPI.MEDIA_CHANNEL_GET_INFO;
			RequestParams req = new RequestParams();
			req.put("accesstoken", StbApp.currentLogin.access_token);
			req.put("verifycode", StbApp.currentLogin.device_id+"");
			req.put("chnlid", channelid+"");
			ServiceHelper helper = ServiceHelper.getHelper();
			helper.setRootUrl(url);
			helper.setSerializerType(SerializerType.JSON);
			helper.callServiceAsync(getActivity(), req, Channel.class, new ResponseHandlerT<Channel>(){

				@Override
				public void onResponse(boolean success, Channel result) {
					if(result != null){
						com.ipanel.join.homed.chongqing.HomedDataManager.goToLiveActivityVarServiceId(getActivity(),result.getServiceId());
					}
				}
			});
		}
	*/}
	
	class LiveGridAdapter extends WeightGridAdapter {

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public Object getItem(int positon) {
			return positon;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.live_item_recommend_live, null, false);
			}
			ImageView poster = (ImageView) convertView.findViewById(R.id.event_poster);
			ImageView icon = (ImageView) convertView.findViewById(R.id.live_icon);
			TextView event = (TextView) convertView.findViewById(R.id.event_name);
			ProgressBar bar = (ProgressBar) convertView.findViewById(R.id.progress);
			
			final Wiki channel = channels.get(position);
			String icon_url = channel.getCover();
			if (icon_url != null)
				mFetcher.loadImage(icon_url, icon);
			else
				icon.setImageDrawable(null);
//			PFInfo pf = channel.pf_info.get(0);
//			if (pf != null)
//				event.setText(pf.name);
			long now = System.currentTimeMillis() / 1000;
//			int progress = (int) ((now - pf.start_time + 1.0f) / (pf.end_time - pf.start_time) * bar.getMax());
//			bar.setProgress(progress);
			
			String image_url = channel.getCover();
//			LogHelper.i("event: " + pf.name + ", poster url: " + image_url);
			if (image_url != null)
				mFetcher.loadImage(image_url, poster);
			else
				poster.setImageDrawable(null);
			
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
//					getServiceId(channel.id);
//					mActivity.finish();
				}
			});
			return convertView;
		}

		@Override
		public int getChildXSize(int position) {
			return 1;
		}

		@Override
		public int getChildYSize(int position) {
			return 1;
		}

		@Override
		public int getXSize() {
			return 5;
		}

		@Override
		public int getYSize() {
			return 1;
		}
		
		@Override
		public int getXSpace() {
			return 5;
		}
		
		@Override
		public int getYSpace() {
			return 0;
		}
	}
	
	class VodGridAdapter extends WeightGridAdapter {

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.live_item_recommend_vod, null, false);
			}
			ImageView poster = (ImageView) convertView.findViewById(R.id.vod_poster);
			TextView name = (TextView) convertView.findViewById(R.id.vod_name);
			final Wiki p = vods.get(position);
			String image_url = p.getCover();
			LogHelper.i("vod: " + vods.get(position).getTitle() + ", poster url: " + image_url);
			if (image_url != null)
				mFetcher.loadImage(image_url, poster);
			else
				poster.setImageDrawable(null);
			name.setText(vods.get(position).getTitle());
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
//					if (p.series_total > 1 || p.type == 4) 
//						com.ipanel.join.cq.vod.HomedDataManager.openTVDetail(getActivity(), p.id+"", p.name, p.series_id+"", p);
//					else
//						com.ipanel.join.cq.vod.HomedDataManager.openMovieDetail(getActivity(), p.id+"", p.name, p);
					mActivity.finish();
				}
			});
			return convertView;
		}

		@Override
		public int getChildXSize(int position) {
			return 1;
		}

		@Override
		public int getChildYSize(int position) {
			return 1;
		}

		@Override
		public int getXSize() {
			return 5;
		}

		@Override
		public int getYSize() {
			return 1;
		}
		
		@Override
		public int getXSpace() {
			return 5;
		}
		@Override
		public int getYSpace() {
			return 0;
		}
		
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT||keyCode==KeyEvent.KEYCODE_DPAD_RIGHT
				|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN||keyCode == KeyEvent.KEYCODE_DPAD_UP){
			changeFocus(keyCode);
			return true;
		}
		if(keyCode==KeyEvent.KEYCODE_BACK||keyCode == KeyEvent.KEYCODE_ESCAPE){
			mActivity.finish();
			return true;
		}
		return super.onKeyDown(keyCode,event);
	}

}
