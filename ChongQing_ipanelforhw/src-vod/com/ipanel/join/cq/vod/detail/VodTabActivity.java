package com.ipanel.join.cq.vod.detail;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.chongqing.portal.VolumePanel;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.player.VodDataManager;
import com.ipanel.join.cq.vod.player.VodDataManager.HwResponseCallBack;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.cq.vod.vodhome.BaseActivity;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;

public class VodTabActivity extends BaseActivity {
	private static final String TAG = "VodTabActivity";
	public static final int PAGE_SIZE = 10;//ÿҳ��ȡ������
	private RelativeLayout label_layout;
	private TextView vod_tab_name;//��ǩ��
	private TextView vod_total_number;//��Ӱ����
	private TextView vod_page;//viewpager��ǰҳ��
	private TextView vod_total_page;//����ҳ��
	private VerticalViewPager2 mViewPager;
	private ImageView vod_follow;//����ͼ��
	private TextView subscribe;//����
	private ImageFetcher mImageFetcher;
	private String tag;
	private int subscribe_tag = 0;//�Ƿ��ģ�0��ʾδ���ģ�1��ʾ�Ѷ���
	private List<Wiki> wikiList;
	public static final int SET_DATA = 0;
	public static final int UPDATE_SUB_TAG = 1;
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case SET_DATA:
				mViewPager.setAdapter(new VODPagerAdapter(wikiList));
				updateUI();
				break;
			case UPDATE_SUB_TAG:
				refreshSubTag();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vod_tag_activity);
		mImageFetcher = SharedImageFetcher.getNewFetcher(this, 3);
		volPanel = new VolumePanel(this);
		initViews();
		initControl();
	}

	protected void updateUI() {
		vod_tab_name.setText(tag);
		vod_total_number.setText("��"+wikiList.size()+"��");
	}

	@Override
	protected void onResume() {
		super.onResume();
		getIntentData();
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}
	@Override
	protected void onPause() {
		super.onPause();
		setIntent(null);
	}
	
	private void getIntentData() {
		if(getIntent()!=null){
			tag = getIntent().getStringExtra("tag");
			getWikisByCategory(tag);
		}
	}
	private void initViews() {
		vod_tab_name = (TextView)findViewById(R.id.vod_tab_name);
		vod_total_number = (TextView)findViewById(R.id.vod_total);
		vod_page = (TextView)findViewById(R.id.vod_tab_page);
		vod_total_page = (TextView)findViewById(R.id.vod_tab_totalpage);
		vod_follow = (ImageView)findViewById(R.id.vod_tab_follow);
		subscribe = (TextView)findViewById(R.id.subscribe);
		label_layout = (RelativeLayout)findViewById(R.id.vod_tab_rl);
		mViewPager = (VerticalViewPager2)this.findViewById(R.id.vod_tab_viewpager);
	}
	
	private void initControl() {
		label_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(subscribe_tag == 1){
					//�Ѷ��ģ���ȡ������
					delSubscribeTag(tag);
				}else{
					//δ��������Ӷ���
					setSubscribeTag(tag);
				}
			}
		});
		VodDataManager.getInstance(this).setHwResponseCallBack(new HwResponseCallBack() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result, String action) {
				if(action.equals(HWDataManager.ACTION_DEL_SUBSCRIBE_TAG)){
					//ɾ����ǩ
					if(success && result!=null && result.getError().getCode() == 0){
						 Tools.showToastMessage(getBaseContext(), "ɾ����ǩ�ɹ�");
						 subscribe_tag = 0;
						 refreshSubTag();
					}else{
						Tools.showToastMessage(getBaseContext(), "ɾ����ǩʧ��");
					}
				}else if(action.equals(HWDataManager.ACTION_SET_SUBSCIRBE_TAG)){
					//��Ӷ���
					if(success && result!=null && result.getError().getCode() == 0){
						 Tools.showToastMessage(getBaseContext(), "���ĳɹ�");
						 subscribe_tag = 1;
						 refreshSubTag();
					}else{
						Tools.showToastMessage(getBaseContext(), "����ʧ��");
					}
				}
			}
		});
		
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				refreshPage(position+1);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				
			}
		});
	}
	// TODO ��Ӷ���
	protected void setSubscribeTag(String tag) {
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_SET_SUBSCIRBE_TAG);
		request.getParam().setTag(tag);
		VodDataManager.getInstance(this).getHwData(request);
	}

	//TODO ɾ������
	protected void delSubscribeTag(String tag) {
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_DEL_SUBSCRIBE_TAG);
		request.getParam().setTag(tag);
		VodDataManager.getInstance(this).getHwData(request);
	}

	//ˢ�±��
	private void refreshSubTag() {
		switch (subscribe_tag) {
		case 1:
			subscribe.setText("�Ѷ���");
			vod_follow.setImageResource(R.drawable.vod_movie_like02);
			break;
		case 0:
			subscribe.setText("���ı�ǩ");
			vod_follow.setImageResource(R.drawable.vod_movielike_sl);
			break;
		default:
			break;
		}
	}
	
	protected void refreshPage(int i) {
		vod_page.setText(i+"");
		vod_total_page.setText("/"+getTotalPageSize()+"ҳ");
	}

	private void getWikisByCategory(String category) {
		Logger.d(TAG, "request-->"+category);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_WIKIS_BY_CATEGORY);
		request.getParam().setPage(1);
		request.getParam().setPagesize(PAGE_SIZE);
		request.getParam().setTag(category);
		request.getParam().setSort(1);//����ʽ��1����   2����   3����
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success && result.getError().getCode() == 0){
					subscribe_tag = result.getSubscribe_tag();
					mHandler.sendEmptyMessage(UPDATE_SUB_TAG);
					int total = result.getTotal();
					wikiList = result.getWikis();
					if(wikiList !=null && wikiList.size()>0){
						mHandler.obtainMessage(SET_DATA).sendToTarget();
					}else{
						Tools.showToastMessage(getBaseContext(), "�ñ�ǩ��û��ӰƬ");
					}
					Logger.d(TAG, "subscribe_tag:"+subscribe_tag+",total"+total);
				}else{
					Tools.showToastMessage(VodTabActivity.this, result.getError().getInfo());
				}
			}
		});
	}
	
	/**
	 * ���ViewPager����ҳ��
	 * */
	private int getTotalPageSize() {
		int count = wikiList.size();
		return count / 5 + (count % 5 == 0 ? 0 : 1);
	}
	
	/**
	 * ���һҳ��ʾ��ӰƬ��
	 * @param pager 
	 * */
	private int getPageDataCount(int pager) {
		if(pager == getTotalPageSize() - 1){
			return wikiList.size() % 5 == 0 ? 5 : wikiList.size() % 5;//���һҳ����
		}else{
			return 5;
		}
	}
	
	class VODPagerAdapter extends PagerAdapter {
		private List<Wiki> list;
		
		public void setList(List<Wiki> list) {
			this.list = list;
			notifyDataSetChanged();
		}

		public VODPagerAdapter(List<Wiki> list) {
			this.list = list;
		}

		@Override
		public float getPageWidth(int position) {
			return (1 + 0.0f) / 2;  
		}

		@Override
		public int getCount() {
			return list.size() % 5 == 0 ? list.size() / 5 : list.size() / 5 + 1;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View view = (View) object;
			container.removeView(view);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			long time=System.currentTimeMillis();
			View itemView = null;
			if (itemView == null) {
				itemView = View.inflate(getBaseContext(), R.layout.vod_hotpageritem, null);
			}
			WeightGridLayout mWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.hotgrid);
			mWeightLayout.setClipToPadding(false);
			mWeightLayout.setTag(position);
			mWeightLayout.setAdapter(new VODWeightGridAdapter(position,list));
			itemView.setTag(position);
			container.addView(itemView);
			return itemView;
		}
	}
	
	class VODWeightGridAdapter extends WeightGridAdapter {

		private int row;
		private List<Wiki> list;
		
		public VODWeightGridAdapter(int row,List<Wiki> list) {
			this.row = row;
			this.list = list;
		}

		@Override
		public int getCount() {
			return getPageDataCount(row);
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int index = 5 * row + position;
			final Wiki filmItem = list.get(index);
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplication()).inflate(R.layout.vod_hot_element3, parent, false);
			}
			final ImageView image = (ImageView) convertView.findViewById(R.id.film_img);
			final TextView name = (TextView) convertView.findViewById(R.id.film_name);
			final RelativeLayout score_layout = (RelativeLayout)convertView.findViewById(R.id.score_layout);
			final TextView score = (TextView) convertView.findViewById(R.id.score_text);
			final RatingBar ratingbar = (RatingBar)convertView.findViewById(R.id.score_ratingbar);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					//TODO �����ת
					HWDataManager.openDetail(getBaseContext(), filmItem.getId(), filmItem.getTitle(), filmItem);
				}
			});
			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						name.setSelected(true);
						score_layout.setVisibility(View.VISIBLE);
					} else {
						name.setSelected(false);
						score_layout.setVisibility(View.INVISIBLE);
					}
				}
			});
			convertView.setTag(row + "");
			if (row * 5 + position >= list.size()) {
				convertView.setVisibility(View.GONE);
			} else {
				convertView.setVisibility(View.VISIBLE);
			}
			if (filmItem.getCover() != null
					&& image.getVisibility() == View.VISIBLE) {
				BaseImageFetchTask task = mImageFetcher.getBaseTask(filmItem.getCover());
				mImageFetcher.setImageSize(241, 300);
				mImageFetcher.setLoadingImage(R.drawable.default_poster);
				mImageFetcher.loadImage(task, image);
			} else {
				image.setBackgroundResource(R.drawable.default_poster);
			}
			name.setText(filmItem.getTitle());
			score.setText(filmItem.getInfo().getAverage());
			ratingbar.setRating(Float.parseFloat(filmItem.getInfo().getAverage())/2.0f);//��������
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
		public int getYSpace() {
			return 35;
		}

		@Override
		public int getXSpace() {
			return 35;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(volPanel.onKeyDown(keyCode, event)){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
