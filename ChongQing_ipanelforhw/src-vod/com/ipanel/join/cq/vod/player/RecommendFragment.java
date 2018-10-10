package com.ipanel.join.cq.vod.player;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.ArrayAdapter;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.customview.SimpleAdapterView;
import com.ipanel.join.cq.vod.player.VodDataManager.HwResponseCallBack;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class RecommendFragment extends BaseFragment {
	SimpleAdapterView list;
	ImageFetcher mFetcher;
	ServiceHelper serviceHelper;
	SimplePlayerActivity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container) {
		View v = inflater.inflate(R.layout.vod_fragment_recommend, container, false);
		list = (SimpleAdapterView) v.findViewById(R.id.recommend_list);
		mFetcher = SharedImageFetcher.getNewFetcher(container.getContext(), 3);
		serviceHelper = ServiceHelper.createOneHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		activity = (SimplePlayerActivity) getActivity();
		VodDataManager.getInstance(activity).setHwResponseCallBack(new HwResponseCallBack() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result,String action) {
				if(success && result != null && result.getError().getCode()==0){
					showRecommends(result);
				}else{
					Tools.showToastMessage(mActivity, "暂无相似影片");
					activity.mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_EMPTY, null);
				}
			}
		});
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		initData();
	}
	
	
	private void initData() {
		String playType = activity.getPlayType();
		String id = null;
		if(playType.equals("0")){
			//电影
			id = activity.getWikiId();
		}else if(playType.equals("1")){
			//电视剧。其他均为空
			if(activity.getHwResponse()!=null && activity.getHwResponse().getWiki()!=null)
				id = activity.getHwResponse().getWiki().getId();
		}
		if(TextUtils.isEmpty(id)){
			getWikisByHot();
		}else{
			getSimilarWikis(id);
		}
	}
	
	private void getSimilarWikis(String id) {
		//获取相似影片
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_SIMILAR_WIKIS);
		request.getParam().setWikiId(id);
		request.getParam().setPage(1);
		request.getParam().setPagesize(15);
		VodDataManager.getInstance(activity).getHwData(request);
	}

	private void getWikisByHot() {
		//获取大家都在看
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_WIKIS_BY_HOT);
		request.getParam().setPage(1);
		request.getParam().setPagesize(15);
		VodDataManager.getInstance(activity).getHwData(request);
	}
	
	protected void showRecommends(GetHwResponse result) {
		List<Wiki> items = result.getWikis();
		list.setInitPos(100000, items.size() * 1000);
		list.setAdapter(new RecommendAdapter(getActivity(), items));
		list.setItemSpace(-15);//设置间距
		list.post(new Runnable() {
			
			@Override
			public void run() {
				list.requestFocus();
			}
		});
	}
	
	class RecommendAdapter extends ArrayAdapter<Wiki> implements OnClickListener {

		public RecommendAdapter(Context context, List<Wiki> items) {
			super(context, 0, items);
		}

		@Override
		public int getCount() {
			int real = super.getCount();
			if (real > 5)
				return Integer.MAX_VALUE;
			return real;
		}

		@Override
		public Wiki getItem(int position) {
			int real = super.getCount();
			position = position % real;
			if (position < 0)
				position += real;
			return super.getItem(position % real);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(
						R.layout.vod_item_recommend_list, parent, false);
				convertView.setOnClickListener(this);
			}
			ImageView poster = (ImageView) convertView.findViewById(R.id.recommend_poster);
			final TextView name = (TextView) convertView.findViewById(R.id.recommend_name);
			
			Wiki item = getItem(position);
			String en = item.getTitle();
			name.setText(en);
			if(item.getCover() != null){
				BaseImageFetchTask task = mFetcher.getBaseTask(item.getCover());
				mFetcher.setLoadingImage(R.drawable.default_poster);
				mFetcher.loadImage(task, poster);
			} else {
				poster.setImageResource(R.drawable.default_poster);
			}
			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					if(hasFocus){
						name.setSelected(true);
					}else{
						name.setSelected(false);
					}
				}
			});
			convertView.setTag(item);
			return convertView;
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() instanceof Wiki) {
				Wiki item = (Wiki) v.getTag();
				startToDetail(item);//跳转到详情
			}
		}
	}
	
	public void startToDetail(Wiki item) {
		HWDataManager.openDetail(activity, item.getId(), item.getTitle(), item);
		activity.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT||keyCode==KeyEvent.KEYCODE_DPAD_RIGHT
				|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN||keyCode == KeyEvent.KEYCODE_DPAD_UP){
			changeFocus(keyCode);
			return true;
		}
		return super.onKeyDown(keyCode,event);
	}
}
