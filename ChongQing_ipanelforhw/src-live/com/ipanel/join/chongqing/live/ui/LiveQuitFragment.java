package com.ipanel.join.chongqing.live.ui;

import java.text.ParseException;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
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
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Program;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.chongqing.live.base.BaseFragment;
import com.ipanel.join.chongqing.live.manager.UIManager;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWDataManagerImpl;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWDataManagerImpl.HWShiftChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.util.TimeHelper;
import com.ipanel.join.chongqing.portal.PortalDataManager;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class LiveQuitFragment extends BaseFragment {
	
	private final static int MSG_RECOMMEND_LIVE_DATA_COMPLETE = 0;
	private final static int MSG_RECOMMEND_VOD_DATA_COMPLETE = 1;
	
	List<Program> channels;
	List<Wiki> vods;
	ServiceHelper serviceHelper;
	ImageFetcher mFetcher;
	
	WeightGridLayout live_grid, vod_grid;
	Button close;
	
	Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_RECOMMEND_LIVE_DATA_COMPLETE:
				live_grid.setAdapter(new LiveGridAdapter());
//				live_grid.requestFocus();
				break;
			case MSG_RECOMMEND_VOD_DATA_COMPLETE:
				vod_grid.setAdapter(new VodGridAdapter());
//				close.requestFocus();
				break;
			default:
				break;
			}
			
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.live_fragment_quit, root, false);
		live_grid = (WeightGridLayout) vg.findViewById(R.id.current_live);
		vod_grid = (WeightGridLayout) vg.findViewById(R.id.recommend_vod);
		close = (Button) vg.findViewById(R.id.pop_close);
		close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getUIManager().hideFragment(LiveQuitFragment.this);
			}
		});
		
		mFetcher = SharedImageFetcher.getNewFetcher(root.getContext(), 1);
		serviceHelper = ServiceHelper.createOneHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		return vg;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		requestLiveRecommend();
		requestVodRecommend();
		close.requestFocus();
	}
	
	private void requestLiveRecommend() {
		GetHwRequest req = new GetHwRequest();
		req.setAction("GetLiveProgramsByRecommend");
		req.getDevice().setDnum("123");
		req.getUser().setUserid("123");
		req.getParam().setPage(1);
		req.getParam().setPagesize(10);
		serviceHelper.setRootUrl(PortalDataManager.url);
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.callServiceAsync(getLiveActivity(), req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				// TODO Auto-generated method stub
				if (!success) {
					LogHelper.i("request detail data failed");
					return;
				}
				if (result == null) {
					LogHelper.i("failed to parse JSON data");
					return;
				}
				channels = result.getPrograms();
				mHandler.sendEmptyMessage(MSG_RECOMMEND_LIVE_DATA_COMPLETE);
			}
		});
	}
	
	private void requestVodRecommend() {
		GetHwRequest req = new GetHwRequest();
		req.setAction("GetWikisByHot");
		req.getDevice().setDnum("123");
		req.getUser().setUserid("123");
		req.getParam().setPage(1);
		req.getParam().setPagesize(10);
		serviceHelper.setRootUrl(PortalDataManager.url);
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.callServiceAsync(getLiveActivity(), req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				// TODO Auto-generated method stub
				if (!success) {
					LogHelper.i("request detail data failed");
					return;
				}
				if (result == null) {
					LogHelper.i("failed to parse JSON data");
					return;
				}
				vods = result.getWikis();
				mHandler.sendEmptyMessage(MSG_RECOMMEND_VOD_DATA_COMPLETE);
			}
		});
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataChange(int type, Object data) {
		// TODO Auto-generated method stub

	}
	
	class LiveGridAdapter extends WeightGridAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return Math.min(5, channels.size());
		}

		@Override
		public Object getItem(int positon) {
			// TODO Auto-generated method stub
			return positon;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(getLiveActivity()).inflate(R.layout.live_item_recommend_live, null, false);
			}
			ImageView poster = (ImageView) convertView.findViewById(R.id.event_poster);
			ImageView icon = (ImageView) convertView.findViewById(R.id.live_icon);
			TextView event = (TextView) convertView.findViewById(R.id.event_name);
			ProgressBar bar = (ProgressBar) convertView.findViewById(R.id.progress);
			
			final Program channel = channels.get(position);
			String icon_url = channel.getChannelLogo();
			LogHelper.i("icon url is " + icon_url);
			if (icon_url != null)
				mFetcher.loadImage(icon_url, icon);
			else
				icon.setImageDrawable(null);
			event.setText(channel.getTitle());
			long now = System.currentTimeMillis();
			try {
				long start = TimeHelper.formatter_a.parse(channel.getStartTime()).getTime();
				long end = TimeHelper.formatter_a.parse(channel.getEndTime()).getTime();
				int progress = (int) ((now - start + 0.0f) / (end - start) * bar.getMax());
				bar.setProgress(progress);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String image_url = channel.getCover();
			if (image_url != null)
				mFetcher.loadImage(image_url, poster);
			else 
				poster.setImageDrawable(null);
			
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					LiveChannel next = getLiveActivity().getDataManager().getLiveChannelByService(Integer.parseInt(channel.getServiceId()));
					getLiveActivity().getStationManager().switchTVChannel(next);
					getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_PF, null);
				}
			});
			
			return convertView;
		}

		@Override
		public int getChildXSize(int position) {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public int getChildYSize(int position) {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public int getXSize() {
			// TODO Auto-generated method stub
			return 5;
		}

		@Override
		public int getYSize() {
			// TODO Auto-generated method stub
			return 1;
		}
		
		@Override
		public int getXSpace() {
			// TODO Auto-generated method stub
			return 5;
		}
		
		@Override
		public int getYSpace() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	class VodGridAdapter extends WeightGridAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return Math.min(5, vods.size());
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(getLiveActivity()).inflate(R.layout.live_item_recommend_vod, null, false);
			}
			ImageView poster = (ImageView) convertView.findViewById(R.id.vod_poster);
			TextView name = (TextView) convertView.findViewById(R.id.vod_name);
			
			final Wiki vod = vods.get(position);
			String image_url = vod.getCover();
			LogHelper.i("vod: " + vod.getTitle() + ", poster url: " + image_url);
			if (image_url != null)
				mFetcher.loadImage(image_url, poster);
			else {
				poster.setImageDrawable(null);
			}
			name.setText(vod.getTitle());
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			return convertView;
		}

		@Override
		public int getChildXSize(int position) {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public int getChildYSize(int position) {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public int getXSize() {
			// TODO Auto-generated method stub
			return 5;
		}

		@Override
		public int getYSize() {
			// TODO Auto-generated method stub
			return 1;
		}
		
		@Override
		public int getXSpace() {
			// TODO Auto-generated method stub
			return 5;
		}
		
		@Override
		public int getYSpace() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
		case RcKeyEvent.KEYCODE_QUIT:
			LiveChannel last = getLiveActivity().getStationManager().getLastCannel();
			if (last == null) {
//				last = getLiveActivity().getDataManager().getAllShiftChannel().get(0);
				last = getLiveActivity().getDataManager().getAllChannel().get(0);
			}
			getLiveActivity().getStationManager().switchTVChannel(last);
			getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_PF, null);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_LEFT:
			changeFocus(keyCode);
			break;
		}
		return true;
	}

}
