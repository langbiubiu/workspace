package com.ipanel.join.chongqing.live.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.chongqing.live.base.BaseFragment;
import com.ipanel.join.chongqing.live.manager.UIManager;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWDataManagerImpl;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.portal.PortalDataManager;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class RecommendFragment extends BaseFragment {
	
	private final static int REQUEST_SERIES_DATA_COMPLETE = 0;
	
	private final static int REQUEST_SERIES_DATA_FAILED = 1;
	
//	SimpleAdapterView list;
	RelativeLayout mScrollView_layout;
	ImageFetcher mFetcher;
	ServiceHelper serviceHelper;
	
	List<Wiki> wikis = new ArrayList<Wiki>();
	
	Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case REQUEST_SERIES_DATA_COMPLETE:
//				list.setInitPos(100000, wikis.size() * 1000);
//				list.setItemSpace(-15);//设置间距
//				list.setAdapter(new RecommendsAdapter(getActivity(), wikis));
//				list.post(new Runnable() {
//
//					@Override
//					public void run() {
//						list.requestFocus();
//					}
//				});
				updateUI();
				break;
			case REQUEST_SERIES_DATA_FAILED:
				getLiveActivity().showMessage("获取推荐数据失败");
				getUIManager().hideUI(UIManager.ID_UI_RECOMEND);
				break;
			default:
				break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.back_recommend, root, false);
//		list = (SimpleAdapterView) v.findViewById(R.id.recommend_list);
		mScrollView_layout = (RelativeLayout)v.findViewById(R.id.back_scrollView_layout);
		mFetcher = SharedImageFetcher.getNewFetcher(root.getContext(), 3);
		serviceHelper = ServiceHelper.createOneHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		return v;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		LiveChannel channel = getLiveActivity().getStationManager().getPlayChannel();
//		String id = ((HWDataManagerImpl)getLiveActivity().getDataManager()).mHWMap.get(channel.getChannelKey().getProgram()+"");
		int id = channel.getChannelKey().getProgram();
		getRecommends(id);
	}

	private void getRecommends(int id) {
		// TODO Auto-generated method stub
		GetHwRequest req = new GetHwRequest();
		req.setAction("GetSimilarVodsByChannel");
		HWDataManager.fillCommonParams(req);
		req.getDeveloper().setApikey(HWDataManager.APIKEY);
		req.getDeveloper().setSecretkey(HWDataManager.SECRETKEY);
//		req.getParam().setChannelId(id);
		req.getParam().setServiceId(id);
		serviceHelper.setRootUrl(PortalDataManager.url);
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.callServiceAsync(getLiveActivity(), req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				// TODO Auto-generated method stub
				if (!success) {
					LogHelper.i("request detail data failed");
					mHandler.sendEmptyMessage(REQUEST_SERIES_DATA_FAILED);
					return;
				}
				if (result == null) {
					LogHelper.i("failed to parse JSON data");
					mHandler.sendEmptyMessage(REQUEST_SERIES_DATA_FAILED);
					return;
				}
				if (result.getError().getCode() != 0) {
					mHandler.sendEmptyMessage(REQUEST_SERIES_DATA_FAILED);
				} else {
					wikis = result.getWikis();
					mHandler.sendEmptyMessage(REQUEST_SERIES_DATA_COMPLETE);
				}
			}
		});
	}
	
	private void updateUI() {
		mScrollView_layout.removeAllViews();
		for (int i = 0; i < wikis.size(); i++) {
			View convertView = getLiveActivity().getLayoutInflater().inflate(R.layout.vod_item_recommend_list, mScrollView_layout,false);
			final Wiki item = wikis.get(i % wikis.size());
			final ViewHold hv = new ViewHold();
			hv.image = (ImageView) convertView.findViewById(R.id.recommend_poster);
			hv.text = (TextView) convertView.findViewById(R.id.recommend_name);
			
			if (item.getCover()!= null) {
				String adUrl = item.getCover();
				BaseImageFetchTask task = mFetcher.getBaseTask(adUrl);
				mFetcher.setLoadingImage(R.drawable.default_poster);
				mFetcher.loadImage(task, hv.image);
			}else{
				hv.image.setImageResource(R.drawable.default_poster);
			}
			hv.text.setText(item.getTitle()); 
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					startToDetail(item);
				}
			});

			if(convertView!=null){
				mScrollView_layout.addView(convertView);
				RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) convertView
						.getLayoutParams();
				para.leftMargin = getLiveActivity().getResources()
						.getDimensionPixelSize(R.dimen.back_left_lenth) * i;
				convertView.requestLayout();
			}
			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus){
						hv.text.setSelected(true);
					}else{
						hv.text.setSelected(false);
					}
				}
			});
		}
		mScrollView_layout.requestFocus();
	}
	
	class ViewHold {
		ImageView image;
		TextView text;
	}
	
	public void startToDetail(Wiki item) {
		HWDataManager.openDetail(getLiveActivity(), item.getId(), item.getTitle(), item);
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
	
//	class RecommendsAdapter extends ArrayAdapter<Wiki> implements OnClickListener {
//
//		public RecommendsAdapter(Context context, List<Wiki> objects) {
//			super(context, 0, objects);
//			// TODO Auto-generated constructor stub
//		}
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			if (v.getTag() instanceof Wiki) {
//				Wiki wiki = (Wiki) v.getTag();
////				Toast.makeText(getLiveActivity(), "onClick channel " + p.name, Toast.LENGTH_SHORT).show();
////				LiveChannel channel = getLiveActivity().getDataManager()
////						.getLiveChannelByChannelId(p.id + "");
////				getLiveActivity().getStationManager().switchTVChannel(channel);
////				getUIManager().hideFragment(RecommendFragment.this);
//			}
//		}
//		
//		@Override
//		public int getCount() {
//			// TODO Auto-generated method stub
//			int real = super.getCount();
//			if (real > 5)
//				return Integer.MAX_VALUE;
//			return real;
//		}
//		
//		@Override
//		public Wiki getItem(int position) {
//			int real = super.getCount();
//			position = position % real;
//			if (position < 0)
//				position += real;
//			return super.getItem(position % real);
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			if (convertView == null) {
//				convertView = LayoutInflater.from(parent.getContext()).inflate(
//						R.layout.vod_item_recommend_list, parent, false);
//				convertView.setOnClickListener(this);
//			}
//			ImageView poster = (ImageView) convertView
//					.findViewById(R.id.recommend_poster);
//			TextView name = (TextView) convertView
//					.findViewById(R.id.recommend_name);
//			
//			Wiki wiki = getItem(position);
//			String en = wiki.getTitle();
//			name.setText(en);
//			
//			String img_url = wiki.getCover();
//			if (img_url != null && !img_url.equals("")) {
//				BaseImageFetchTask task = mFetcher.getBaseTask(img_url);
//				mFetcher.setLoadingImage(R.drawable.default_poster);
//				mFetcher.loadImage(task, poster);
//			} else {
//				poster.setImageResource(R.drawable.default_poster);
//			}
//			convertView.setTag(wiki);
//			return convertView;
//		}
//	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
		case RcKeyEvent.KEYCODE_QUIT:
		case KeyEvent.KEYCODE_MENU:
		case RcKeyEvent.KEYCODE_TV_ADD:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
			return false;
		}
		return true;
	}

}
