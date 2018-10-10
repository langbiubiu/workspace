package com.ipanel.join.cq.vod.detail;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.support.v4.view.ViewPager2;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.SimpleTab;
import cn.ipanel.android.widget.SimpleTab.OnTabChangeListener;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Vod;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki.Info.Star;
import com.ipanel.join.chongqing.portal.VolumePanel;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.customview.JHListView.ListFocusChangeListener;
import com.ipanel.join.cq.vod.customview.JiShuView;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.player.SimplePlayerActivity;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.cq.vod.vodhome.BaseActivity;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.huawei.cqvod.MovieDetailResponse;
import com.ipanel.join.protocol.huawei.cqvod.RTSPResponse;

public class TVDetailActivity extends BaseActivity implements OnClickListener,OnGlobalFocusChangeListener {
	protected static final String TAG = TVDetailActivity.class.getSimpleName();
	private RelativeLayout collect_rl;// �ղ�
	private RelativeLayout play_rl;// ����
	private RelativeLayout desc_rl;// �缯����
	private RelativeLayout alike_rl;// ����ӰƬ
//	private RelativeLayout comment_rl;// ����
	private RelativeLayout star_show;// ������
	private ImageView tv_poster;// ����
	private TextView tv_name;// ����
	private TextView play_times;//���Ŵ���
	private TextView douban_score;//��������
	private TextView total_nums;//�ܼ���
	private TextView update_to;//������
	private TextView year;//���
	private TextView region;//������
	private Button goodBt;//����
	private Button badBt;//��˥
	private TextView description;// �������
	private TextView series_name;//����ӰƬ����ĵ�Ӱ����
	private TextView collect_name;//ѡ������ĵ�Ӱ����
	
	private VerticalViewPager2 mViewPager;//�Ƽ�ӰƬ
	private RelativeLayout tv_content_layout;//�缯���ܽ���
	private RelativeLayout tv_recommend_layout;//����ӰƬ����
	private RelativeLayout collect_layout;//ѡ������
	
	private LinearLayout director_ll;//����
	private LinearLayout actor_ll;//����
	private LinearLayout label_ll;//��ǩ
	
	private WeightGridLayout gridview;
	private JiShuView tvCollect;//����view
	
//	private String seriesId;// �缯Id
	private String seriesName;// �缯����
	private String wikiId;//ID
	private String vodId;//��ΪId
	
	private Wiki seriesInfo;//���Ӿ���Ϣ��ͨ���ӿڻ�ȡ
	private ImageFetcher mImageFetcher;
	
	public static final int UPDATE_UI = 0;
	public static final int SET_RECOMMEND = 1;
	public static final int UPDATE_PAGE = 2; 
	public static final int SET_COMMENT_DATA = 3;
	public static final int NO_COMMENT_DATA = 4;//������
	public static final int UPDATE_STAR_LIST = 5;//������
	public static final int PERFORMTVPLAY = 6;//����
	private List<Wiki> similarWikis;//��Ŀ�б�
	private List<String> allList = new ArrayList<String>();//���Ӿ�缯
	private List<String> invertList = new ArrayList<String>();//������б�
//	private List<Comment> list = new ArrayList<Comment>();//����
//	private CommentListAdapter listAdapter;
//	private CollectAdapter collectAdapter;//ѡ����������
	private  CollectGridAdapter collectAdapter;
	private int currentPosition = 0;
	
	private ListView comment_listview;//����
	private String playType;
	private TextView page;//ҳ��
	private TextView totalPage;//��ҳ��
	private ImageView collect_icon;//�ղػ���ȡ���ղص�ͼ��
	private TextView collect_text;//�ղػ���ȡ���ղ�
	private SimpleTab simpleTab;
	private int is_fav = 0;//�Ƿ��ղ�,0��ʾδ�ղأ�1���ղ�
	private RelativeLayout star_show_layout;//������
	private VerticalViewPager2 star_pager;
	private TextView star_movie_name;
	private boolean isInverted = false;//˳���������
	private ImageView[] vod_line = new ImageView[4];
	private int[] lines = {R.id.vod_line1,R.id.vod_line2,R.id.vod_line3,R.id.vod_line4};
	private ImageView sort_icon;//����ͼ��
	private Wiki bean;//ͨ��Intent����
	private List<Star> starList = new ArrayList<Star>();//�����б�
	private int is_inter = 0;//������0��1����2��
	private boolean hasVodId = false;//ͨ����ΪId
	private List<Vod> vodList;//���Ӿ�缯
	private GetHwResponse hwResponse;
	private BroadcastReceiver receiver;
	private int number;//���ŵļ���,��1��ʼ
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case UPDATE_UI:
				updateUI(seriesInfo);
				getSimilarWikis();
				break;
			case SET_RECOMMEND:
				mViewPager.setAdapter(new VODPagerAdapter());
				refreshPage(1);
				break;
			case SET_COMMENT_DATA:
				break;
			case NO_COMMENT_DATA:
				comment_listview.setVisibility(View.INVISIBLE);
				break;
			case UPDATE_STAR_LIST:
				if(starList != null && starList.size() > 0){
					star_pager.setAdapter(new StarPagerAdapter(getBaseContext(), starList));
				}
				break;
			case PERFORMTVPLAY:
				if ("0".equals(rtspResponse.getPlayFlag())) {
					Tools.showToastMessage(TVDetailActivity.this,rtspResponse.getMessage());
				} else {
					startToTVPlay();
				}
				break;
			default:
				break;
			}
			
		}

	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vod_tv_detail_layout);
		mImageFetcher = SharedImageFetcher.getNewFetcher(this, 3);
		volPanel = new VolumePanel(this);
		findViewById(R.id.vod_tv_detail_layout).getViewTreeObserver().
			addOnGlobalFocusChangeListener(this);
		this.registerReceiver(receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Logger.d("�Ѿ����յ����¹㲥CookieString = " + intent.getExtras().getString("CookieString").toString());
				GlobalFilmData.getInstance().setCookieString(intent.getExtras().getString("CookieString"));
				String epg = intent.getExtras().getString("EPG");
				String serviceGroupId = "" + intent.getExtras().getLong("ServiceGroupId");
				String smartcard = intent.getExtras().getString("smartcard");
				String authToken = intent.getStringExtra("authToken");

				GlobalFilmData.getInstance().setEpgUrl(epg);
				GlobalFilmData.getInstance().setGroupServiceId(serviceGroupId);
				GlobalFilmData.getInstance().setCardID(smartcard);
				GlobalFilmData.getInstance().setIcState(intent.getExtras().getString("icState"));
				GlobalFilmData.getInstance().cardID=intent.getStringExtra("smartcard");
				GlobalFilmData.getInstance().uid=intent.getStringExtra("unitUserId");
				
				GlobalFilmData.getInstance().setAaa_state(intent.getStringExtra("aaa_state"));
				Logger.d("�Ѿ����յ����¹㲥intent.getStringExtra(unitUserId) = " + intent.getStringExtra("unitUserId"));
				GlobalFilmData.getInstance().setAuthToken(authToken);
				Logger.d("�Ѿ����յ����¹㲥intent.getStringExtra(authToken) = " + intent.getStringExtra("authToken"));

				GlobalFilmData.getInstance().setServicegroup(intent.getLongExtra("ServiceGroupId",0)+"");
			}
			
		}, new IntentFilter("com.ipanel.join.cq.vodauth.EPG_URL"));
		initViews();
		initControl();
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	//��ȡ����
	private void getTVDetail() {
		GetHwRequest req = HWDataManager.getHwRequest();
		req.setAction(HWDataManager.ACTION_GET_WIKI_INFO);
		req.getParam().setWikiId(wikiId);
		
		serviceHelper.callServiceAsync(this, req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if (!success) {
					Log.i(TAG, "request detail data failed");
					return;
				}
				if (result == null) {
					Log.i(TAG, "failed to parse JSON data");
					showMessage(R.string.dataerror);
					return;
				}
				if(result.getError().getCode() == 0){
					hwResponse = result;
					seriesInfo = result.getWiki();//���Ӿ�����
					updateInterInfo(seriesInfo);//���»�����Ϣ���ղء�����);
					vodList = reSort(result.getVod());
					if(vodList!=null && vodList.size()>0){
						setCollectLayout(vodList);
					}
					if(bean == null && seriesInfo != null){
						mHandler.sendEmptyMessage(UPDATE_UI);
					}
				}else{
					Tools.showToastMessage(getBaseContext(), result.getError().getInfo());
				}
			}

		});
	}
	//��ȡ����ͨ����ΪId
	private void getTVDetail(String hwId) {
		Log.d(TAG, "hwid"+hwId);
		GetHwRequest req = HWDataManager.getHwRequest();
		req.setAction(HWDataManager.ACTION_GET_WIKI_BY_VODID);
		req.getParam().setId(hwId);
		req.getDeveloper().setApikey(HWDataManager.APIKEY);
		req.getDeveloper().setSecretkey(HWDataManager.SECRETKEY);
		
		serviceHelper.callServiceAsync(this, req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if (!success) {
					Log.i(TAG, "request detail data failed");
					return;
				}
				if (result == null) {
					Log.i(TAG, "failed to parse JSON data");
					showMessage(R.string.dataerror);
					return;
				}
				if(result.getError().getCode()==0){
					hwResponse = result;
					seriesInfo = result.getWiki();//���Ӿ�����
					if(seriesInfo!=null)
						updateInterInfo(seriesInfo);//���»�����Ϣ���ղء�����);
					vodList = reSort(result.getVod());
					wikiId = result.getWiki().getId();
					if(vodList != null && vodList.size() > 0){
						setCollectLayout(vodList);
					}
					if(bean == null && seriesInfo != null){
						mHandler.sendEmptyMessage(UPDATE_UI);
					}
				}else{
					Tools.showToastMessage(getBaseContext(), result.getError().getInfo());
				}
			}

		});
	}
	
	protected List<Vod> reSort(List<Vod> vod) {
		//�����Ѹ�����������ݷŵ���һ�������
		if(vod == null || vod.size() == 0){
			return null;
		}
		List<Vod> mViedoList = new ArrayList<Vod>();
		mViedoList.add(vod.get(0));
		for (int i = 1; i < vod.size(); i++) {
			if(!vod.get(i).getMark().equals(mViedoList.get(mViedoList.size()-1).getMark())){
				mViedoList.add(vod.get(i));
			}
		}
		return mViedoList;
	}

	//���»�����Ϣ
	private void updateInterInfo(Wiki wiki) {
		is_fav = wiki.getInfo().getIs_fav();
		setCollectIcon();
		is_inter = wiki.getInfo().getIs_inter();
		Logger.d(TAG, "is_fav:"+is_fav+"is_inter:"+is_inter);
	}
	
	private void getIntentData() {
		if(getIntent() != null){
			seriesName = getIntent().getStringExtra("name");
			setSeriesName();
			wikiId = getIntent().getStringExtra("id");
			bean = (Wiki)getIntent().getSerializableExtra("data");
			hasVodId = getIntent().getBooleanExtra("isVodId", false);
			Logger.d(TAG, "seriesName:"+seriesName+",id:"+wikiId+",hasVodId"+hasVodId);
			if(bean != null){
				updateUI(bean);
				getSimilarWikis();//��ȡ�Ƽ�����
			}
			if(hasVodId){//����ǻ�ΪId��
				vodId = wikiId;
				getTVDetail(vodId);
			}else{
				getTVDetail();
			}
		}
	}
	//ˢ�½���
	private void updateUI(Wiki wiki) {
		starList.clear();
		showLayoutNeeded(2);
		desc_rl.requestFocus();
		simpleTab.setCurrentIndex(1);//�����ƶ����缯����
		setSeriesName();
		if(wiki.getCover()!=null){//���ú���
			BaseImageFetchTask task = mImageFetcher.getBaseTask(wiki.getCover());
			mImageFetcher.setImageSize(321, 400);
			mImageFetcher.setLoadingImage(R.drawable.default_poster);
			mImageFetcher.loadImage(task, tv_poster);
		}else{
			tv_poster.setImageResource(R.drawable.default_poster);
		}
		total_nums.setText(wiki.getInfo().getEpisodes()+"��");
		description.setText(wiki.getDesc());
		play_times.setText(wiki.getInfo().getVod_num()+"��");
		douban_score.setText(wiki.getInfo().getAverage()+"");
		region.setText(wiki.getInfo().getCountry());
		year.setText(wiki.getInfo().getReleased());
		//���õ���
		List<Star> diretorList = wiki.getInfo().getDirector();
		director_ll.removeAllViews();
		starList.addAll(diretorList);
		for(int i = 0; i < diretorList.size(); i++) {
			String direct = diretorList.get(i).getTitle();
			final View convertView = LayoutInflater.from(TVDetailActivity.this).inflate(R.layout.vod_label_item, null,false);
			TextView direct_name = (TextView)convertView.findViewById(R.id.vod_label_name);
			direct_name.setText(direct);
			if(convertView!=null){
				director_ll.addView(convertView);
				LinearLayout.LayoutParams para = (LinearLayout.LayoutParams) convertView
						.getLayoutParams();
				para.leftMargin = 28;
				convertView.requestLayout();
				convertView.setTag(diretorList.get(i).getId());
			}
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					HWDataManager.openStarShowActivity(getBaseContext(), convertView.getTag().toString());
				}
			});
		}
		//��������
		actor_ll.removeAllViews();
		List<Star> actorList = wiki.getInfo().getStarring();
		starList.addAll(actorList);
		int width = 0;
		for(int i = 0 ; i < actorList.size(); i++){
			String actorName = actorList.get(i).getTitle();
			final View convertView = LayoutInflater.from(TVDetailActivity.this).inflate(R.layout.vod_label_item, null,false);
			TextView label_name = (TextView)convertView.findViewById(R.id.vod_label_name);
			label_name.setText(actorName);
			if(convertView!=null){
				int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
				int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
				convertView.measure(w, h);
				width += (convertView.getMeasuredWidth() + 28);
				Logger.d(TAG, "width:"+width+",actor_ll:"+actor_ll.getWidth());
				if(width >= 981){
					break;
				}
				actor_ll.addView(convertView);
				convertView.setTag(actorList.get(i).getId());
				LinearLayout.LayoutParams para = (LinearLayout.LayoutParams) convertView
						.getLayoutParams();
				para.leftMargin = 28;
				convertView.requestLayout();
			}
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					HWDataManager.openStarShowActivity(getBaseContext(), convertView.getTag().toString());
				}
			});
		}
		//���ñ�ǩ
		label_ll.removeAllViews();
		List<String> tagList = wiki.getTags();
		for(int i = 0 ; i < tagList.size(); i++){
			final String labelName = tagList.get(i);
			View convertView = LayoutInflater.from(TVDetailActivity.this).inflate(R.layout.vod_label_item, null,false);
			TextView label_name = (TextView)convertView.findViewById(R.id.vod_label_name);
			label_name.setText(labelName);
			if(convertView != null){
				label_ll.addView(convertView);
				LinearLayout.LayoutParams para = (LinearLayout.LayoutParams) convertView
						.getLayoutParams();
				para.leftMargin = 28;
				convertView.requestLayout();
			}
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					HWDataManager.openVodTabActivity(getBaseContext(), labelName);
				}
			});
		}
		mHandler.sendEmptyMessage(UPDATE_STAR_LIST);
	}

	private void setSeriesName() {
		tv_name.setText(seriesName);
		collect_name.setText(seriesName);
		star_movie_name.setText(seriesName);
		series_name.setText(seriesName);
	}
	//��ʾ�ղػ����ղ�
	private void setCollectIcon() {
		if(is_fav==1){
			collect_icon.setImageResource(R.drawable.vod_movie_like02);
			collect_text.setText(getString(R.string.collected));
		}else{
			collect_icon.setImageResource(R.drawable.vod_movielike_sl);
			collect_text.setText(getString(R.string.vod_collect));
		}
	}
	
	//��ʼ���ؼ�
	private void initControl() {
		collect_rl.setOnClickListener(this);
		play_rl.setOnClickListener(this);
		desc_rl.setOnClickListener(this);
		alike_rl.setOnClickListener(this);
		star_show.setOnClickListener(this);
		goodBt.setOnClickListener(this);
		badBt.setOnClickListener(this);
		
		tvCollect.setListFocusChangeListener(new ListFocusChangeListener() {
			
			@Override
			public void onFocusChange(int focus) {
				currentPosition = focus;
				int mod = allList.size() % 30;
				if (mod == 0) {
					if(isInverted){
						collectAdapter.setList(invertList.subList(focus * 30, (focus + 1) * 30));
					}else{
						collectAdapter.setList(allList.subList(focus * 30, (focus + 1) * 30));
					}
				} else {
					if (focus < allList.size() / 30) {
						if(isInverted){
							collectAdapter.setList(invertList.subList(focus * 30, (focus + 1) * 30));
						}else{
							collectAdapter.setList(allList.subList(focus * 30, (focus + 1) * 30));
						}
					} else {
						if(isInverted){
							collectAdapter.setList(invertList.subList(focus * 30, focus * 30 + mod));
						}else{
							collectAdapter.setList(allList.subList(focus * 30, focus * 30 + mod));
						}
					}
				}
			}
		});
		tvCollect.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				tvCollect.invalidateAll();
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
				if (state == ViewPager2.SCROLL_STATE_IDLE) {
					moveIndicatorToFocus();
					mImageFetcher.setPauseWork(false);
				} else {
					mImageFetcher.setPauseWork(true);
				}
				
			}
		});
		mViewPager.getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {
			@Override
			public void onGlobalFocusChanged(View oldFocus, View newFocus) {
				if (mViewPager.getScrollState() == VerticalViewPager2.SCROLL_STATE_IDLE) {
					if (newFocus instanceof FrameLayout) {
						moveIndicatorToFocus();
					}
				}
			}
		});
		simpleTab.setOnTabChangeListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChange(int index, View selectedView) {
				switch (index) {
				case 0:
					showLayoutNeeded(1);//ѡ��
					break;
				case 1:
					//�缯����
					showLayoutNeeded(2);
					break;
				case 2:
					//����ӰƬ
					showLayoutNeeded(3);
					break;
				case 3:
					//������
					showLayoutNeeded(4);
					break;
				default:
					break;
				}
			}
		});
		//������sortIcon��ȡ����
		tvCollect.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction()==KeyEvent.ACTION_DOWN){
					if(keyCode==KeyEvent.KEYCODE_DPAD_UP && currentPosition == 0){
						sort_icon.requestFocus();
					}
				}
				return false;
			}
		});
	}
	
	//ˢ��ҳ��
	private void refreshPage(int position) {
		page.setText(position+"");
		totalPage.setText("/"+getTotalPage()+"ҳ");
	}
	//�ж��Ƿ�����
	private void getPlayNum(int index) {
		int size = allList.size();
		//�ж��Ƿ�����
		if(isInverted){
			number = size - index;
		}else{
			number = index + 1;
		}
		getPlayUrl(number-1);
	}
	
	RTSPResponse rtspResponse;
	
	//��ȡ���ŵ�ַ
	private void getPlayUrl(int i) {
		Logger.d("getPlayUrl:"+i);
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie",
				GlobalFilmData.getInstance().getCookieString()) });
		serviceHelper.setRootUrl(GlobalFilmData.getInstance().getEPGBaseURL()
				+ "/go_authorization.jsp");
		serviceHelper.setSerializerType(SerializerType.JSON);
		RequestParams requestParams = new RequestParams();
		requestParams.put("typeId", "-1");
		requestParams.put("playType", "11");
		requestParams.put(
				"progId",
				vodList.get(i).getId());
		requestParams.put("parentVodId", vodList.get(i).getParent_id()+"");
		requestParams.put("contentType", "0");
		requestParams.put("business", "1");
		serviceHelper.callServiceAsync(TVDetailActivity.this, requestParams,
				RTSPResponse.class, new ResponseHandlerT<RTSPResponse>() {

					@Override
					public void onResponse(boolean success,
							RTSPResponse result) {
						if (success) {
							rtspResponse = result;
							Logger.d("wuhd", String
									.format("rtspResponse: %s",
											rtspResponse.toString()));
							mHandler.sendEmptyMessage(PERFORMTVPLAY);
						}
					}
				});
	}
	
	//��ת������
	private void startToTVPlay() {
		Intent intent = new Intent();
		intent.putExtra("name", vodList.get(number-1).getName());
		intent.putExtra("params",
				rtspResponse.getPlayUrl().substring(
						rtspResponse.getPlayUrl().indexOf("rtsp"),
						rtspResponse.getPlayUrl().length()));
		intent.putExtra("playType", "1");
		intent.putExtra("historyTime", 0);
		intent.putExtra("hwResponse", hwResponse);
		intent.setClass(TVDetailActivity.this, SimplePlayerActivity.class);
		TVDetailActivity.this.startActivity(intent);
	}
	
	/**
	 * ����������˵��
	 */
	private void jumpToMediaPlayer(){
		ComponentName componentName = new ComponentName("com.ipanel.chongqing_ipanelforhw",
				"com.ipanel.join.cq.vod.player.SimplePlayerActivity");//ǰ��Ϊ����������Ϊ������������������
		Intent intent = new Intent();
		intent.setComponent(componentName);
		intent.putExtra("params", "");//���벥�ŵ�ַ������Ϊ�ַ���
		intent.putExtra("name", "");//ӰƬ���ƣ��ַ��������Ӿ����뵥�������磺�����5��
		intent.putExtra("playType", "RTSP");//�������ͣ�����RTSP��ע���д��
		startActivity(intent);
	}
	//��ʼ����ͼ
	private void initViews() {
		collect_rl = (RelativeLayout) this.findViewById(R.id.tv_line1);
		play_rl = (RelativeLayout) this.findViewById(R.id.tv_line2);
		desc_rl = (RelativeLayout) this.findViewById(R.id.tv_line3);
		desc_rl.requestFocus();//��һ���ɾ缯���ܻ�ȡ����
		alike_rl = (RelativeLayout) this.findViewById(R.id.tv_line4);
//		comment_rl = (RelativeLayout) this.findViewById(R.id.tv_line5);
		star_show = (RelativeLayout) this.findViewById(R.id.tv_line6);
		
		tv_poster = (ImageView)this.findViewById(R.id.tv_poster);
		tv_name = (TextView)this.findViewById(R.id.tv_name);
		play_times = (TextView)this.findViewById(R.id.play_times);//���Ŵ���
		douban_score = (TextView)this.findViewById(R.id.douban_score);//��������
		total_nums = (TextView)this.findViewById(R.id.total_nums);//�ܼ���
		update_to = (TextView)this.findViewById(R.id.update_to);//������
		year = (TextView)this.findViewById(R.id.year);//���
		region = (TextView)this.findViewById(R.id.region);//������
		goodBt = (Button)this.findViewById(R.id.good_bt);//����
		badBt = (Button)this.findViewById(R.id.bad_bt);;//��˥
		description = (TextView)this.findViewById(R.id.description);// �������
		
		director_ll = (LinearLayout)this.findViewById(R.id.director_ll);//����
		actor_ll = (LinearLayout)this.findViewById(R.id.actor_ll);//����
		label_ll = (LinearLayout)this.findViewById(R.id.tag_ll);//��ǩ
		
		tv_content_layout = (RelativeLayout)this.findViewById(R.id.tv_intro);
		tv_recommend_layout = (RelativeLayout)this.findViewById(R.id.tv_recommend_layout);
		collect_layout = (RelativeLayout)this.findViewById(R.id.collect_layout);
		
		series_name = (TextView)this.findViewById(R.id.series_name);
		collect_name =(TextView)this.findViewById(R.id.collect_name);
		gridview = (WeightGridLayout)this.findViewById(R.id.collect_grid);
		tvCollect = (JiShuView)this.findViewById(R.id.tvcollect);
		comment_listview = (ListView)this.findViewById(R.id.comment_list);
		mViewPager = (VerticalViewPager2)this.findViewById(R.id.tv_viewpager);
		page = (TextView)findViewById(R.id.series_page);
		totalPage = (TextView)findViewById(R.id.series_totalpage);
		
		simpleTab = (SimpleTab)findViewById(R.id.tv_simple_tab);
		simpleTab.setCurrentIndex(1);//�缯���ܻ�ȡ����
		collect_icon = (ImageView)findViewById(R.id.tv_collect);
		collect_text = (TextView)findViewById(R.id.tv_collect_text);
		
		star_show_layout = (RelativeLayout)findViewById(R.id.star_layout);
		star_movie_name = (TextView)findViewById(R.id.vod_movie_name);
		star_pager = (VerticalViewPager2)findViewById(R.id.star_viewpager);
		for (int i = 0; i < vod_line.length; i++) {
			vod_line[i] = (ImageView)findViewById(lines[i]);
		}
		sort_icon = (ImageView)findViewById(R.id.vod_sort_icon);
	}
	/**
	 * ��ȡ����ӰƬ
	 */
	private void getSimilarWikis() {
		Log.d(TAG, "getSimilarWikis--"+wikiId);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_SIMILAR_WIKIS);
		request.getParam().setWikiId(wikiId);
		request.getParam().setPage(1);
		request.getParam().setPagesize(10);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if (!success) {
					Log.i(TAG, "request detail data failed");
					return;
				}
				if (result == null) {
					Log.i(TAG, "failed to parse JSON data");
					showMessage(R.string.dataerror);
					return;
				}
				if(result.getError().getCode()==0){
					similarWikis = result.getWikis();
					mHandler.obtainMessage(SET_RECOMMEND).sendToTarget();
				}
			}
		});
	}
	//����ѡ������
	private void setCollectLayout(List<Vod> video_list){
		update_to.setText(String.format(getResources().getString(R.string.update), video_list.size()));//������
		if(seriesInfo != null && video_list.size() > 0){
			allList.clear();
			invertList.clear();
			for (int i = 0; i < video_list.size(); i++) {
				allList.add(""+(i+1));
				invertList.add((video_list.size()-i)+"");//����
			}
			if(collectAdapter == null){
				collectAdapter = new CollectGridAdapter(TVDetailActivity.this);
			}
			if(allList.size() >= 30){
				collectAdapter.setList(allList.subList(0, 30));
			}else{
				collectAdapter.setList(allList);
			}
			tvCollect.setShow(allList.size(),30,isInverted);
			gridview.setAdapter(collectAdapter);
			showLines();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_line1:
			if(is_fav==0){
				//�ղ�
				setFavorite();
			}else{
				//ȡ���ղ�
				cancelFavorite();
			}
			break;
		case R.id.good_bt:
			if(is_inter==0){
				setWikiInteractive(1);
			}else{
				showMessage("�ظ�����");
			}
			break;
		case R.id.bad_bt:
			if(is_inter==0){
				setWikiInteractive(2);
			}else{
				showMessage("�ظ�����");
			}
			break;
		default:
			break;
		}
	}

	private void showLayoutNeeded(int id) {
		//������ʾ����
		if(id == 1){
			collect_layout.setVisibility(View.VISIBLE);
		}else{
			collect_layout.setVisibility(View.INVISIBLE);
		}
		if(id == 2){
			tv_content_layout.setVisibility(View.VISIBLE);
		}else{
			tv_content_layout.setVisibility(View.INVISIBLE);
		}
		if(id == 3){
			tv_recommend_layout.setVisibility(View.VISIBLE);
		}else{
			tv_recommend_layout.setVisibility(View.INVISIBLE);
		}
		if(id == 4){
			star_show_layout.setVisibility(View.VISIBLE);
		}else{
			star_show_layout.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * ȡ���ղ�
	 */
	private void cancelFavorite() {
		Logger.d("wuhd", "cancelFavorite-->"+is_fav+","+wikiId);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_DEL_FAV_WIKI);
		request.getParam().setWikiId(wikiId);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success&&result.getError().getCode()==0){
					//ȡ���ղسɹ�
					showMessage(R.string.cancel_fav_succeed);
					is_fav = 0;
					setCollectIcon();
				}else{
					showMessage(R.string.cancel_fav_failed);
				}
			}
		});
	}

	/**
	 * �ղ�
	 */
	private void setFavorite() {
		Logger.d("wuhd", "setFavorite-->"+is_fav+","+wikiId);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_SET_FAV_WIKI_BY_USER);
		request.getParam().setWikiId(wikiId);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success&&result.getError().getCode()==0){
					//�ղسɹ�
					showMessage(R.string.fav_succeed);
					is_fav = 1;
					setCollectIcon();
				}else{
					showMessage(R.string.fav_failed);
				}
			}
		});
	}
	
	//���޻��ߵ�˥:1���� 2��
	private void setWikiInteractive(final int act) {
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_SET_WIKI_INTERACTIVE);
		request.getParam().setAct(act);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(result.getError().getCode()==0){
					is_inter = act;
					if(act == 1){
						showMessage(getString(R.string.good_succeed));
					}else{
						showMessage(getString(R.string.bad_succeed));
					}
				}else{
					if(act == 1){
						showMessage(getString(R.string.good_failed));
					}else{
						showMessage(getString(R.string.bad_failed));
					}
				}
			}
		});
	}
	
	private int getTotalPage(){
		int total = similarWikis == null ? 0 : similarWikis.size();
		int totalPage = total % 5 == 0 ? total / 5 : total / 5 + 1;
		return totalPage;
	}
	
	private int getPageCount(int row){
		int total = similarWikis == null ? 0 : similarWikis.size();
		int totalPage = getTotalPage();
		if(row == totalPage - 1){
			return total % 5 == 0 ? 5 : total % 5;
		}else{
			return 5;
		}
	}
	//����Ƽ�ӰƬ
	private void reFreshCurrentPage(Wiki item) {
		wikiId = item.getId();
		seriesName = item.getTitle();
		bean = item;
		updateUI(bean);
		getSimilarWikis();//��ȡ�Ƽ�����
		getTVDetail();//��ȡ����
	}
	
	class VODPagerAdapter extends PagerAdapter {

		@Override
		public float getPageWidth(int position) {
			return (1 + 0.0f) / 2;  
		}

		@Override
		public int getCount() {
			return similarWikis.size() % 5 == 0 ? similarWikis.size() / 5 : similarWikis.size() / 5 + 1;
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
			ViewGroup v = (ViewGroup) object;
			container.removeView(v);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			long time=System.currentTimeMillis();
			View itemView = null;//getViewFromSoftReference();
			if (itemView == null) {
				LogHelper.e("inflate from layout ");
				itemView = View.inflate(getBaseContext(), R.layout.vod_hotpageritem, null);
			}
			WeightGridLayout mWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.hotgrid);
			mWeightLayout.setClipToPadding(false);
			mWeightLayout.setTag(position);
			mWeightLayout.setAdapter(new VODWeightGridAdapter(position));
			itemView.setTag(position);
			container.addView(itemView);
			LogHelper.e("instantiateItem view take time : "+(System.currentTimeMillis()-time));
			return itemView;
		}
	}
	
	class VODWeightGridAdapter extends WeightGridAdapter {
		
		int row;
		public VODWeightGridAdapter(int row) {
			this.row = row;
		}

		@Override
		public int getCount() {
			return getPageCount(row);
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
			final Wiki item = similarWikis.get(index);
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
					 reFreshCurrentPage(item);
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
			if (row * 5 + position >= similarWikis.size()) {
				convertView.setVisibility(View.GONE);
			} else {
				convertView.setVisibility(View.VISIBLE);
			}
			if (item.getCover() != null
					&& image.getVisibility() == View.VISIBLE) {
				BaseImageFetchTask task = mImageFetcher.getBaseTask(item.getCover());
				mImageFetcher.setImageSize(241, 300);
				mImageFetcher.setLoadingImage(R.drawable.default_poster);
				mImageFetcher.loadImage(task, image);
			} else {
				image.setBackgroundResource(R.drawable.default_poster);
			}
			name.setText(item.getTitle());
			float rate = 0f;
			if(item.getInfo()!=null && item.getInfo().getAverage()!=null){
				if(!TextUtils.isEmpty(item.getInfo().getAverage())){
					rate = Float.parseFloat(item.getInfo().getAverage());
				}
				score.setText(item.getInfo().getAverage());
			}else{
				score.setText("0.0");
			}
			ratingbar.setRating(rate/2.0f);//��������
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
	
	class CollectGridAdapter extends WeightGridAdapter {
		private List<String> list;
		private LayoutInflater mInflater;
		private Context context;
		
		public CollectGridAdapter(Context context) {
			this.context = context;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		public void setList(List<String> list){
			this.list = list;
			notifyDataSetChanged();
		}
		@Override
		public int getCount() {
			return list.size();
			
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			Holder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.vod_collect_grid_item, null);
				holder = new Holder();
				holder.tvNumber = (TextView) convertView.findViewById(R.id.tv_number);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					int index = position + 30 * currentPosition;
					getPlayNum(index);
				}
			});
			String info = list.get(position);
			if (info != null) {
				holder.tvNumber.setText(info);
			}
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
			return 6;
		}

		@Override
		public int getYSpace() {
			return 0;
		}

		@Override
		public int getXSpace() {
			return 0;
		}
		class Holder {
			TextView tvNumber;
		}
	}
	@Override
	public void onGlobalFocusChanged(View oldFocus, View newFocus) {
		if(newFocus.getId() == R.id.tv_line2){
			collect_rl.setFocusable(true);
		}else if(newFocus.getId() != R.id.tv_line1){
			collect_rl.setFocusable(false);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(volPanel.onKeyDown(keyCode, event)){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	//����ѡ��
	public void reSort(View v){
		//TODOѡ��
		if(!isInverted){
			//���֮ǰΪ����������
			isInverted = true;
			sort_icon.setImageResource(R.drawable.vod_sort_down_sl);
			if(allList.size() >= 30){
				collectAdapter.setList(invertList.subList(0, 30));
			}else{
				collectAdapter.setList(invertList);
			}
			tvCollect.setShow(invertList.size(), 30,isInverted);
			gridview.setAdapter(collectAdapter);
		}else{
			//���֮ǰΪ����������
			isInverted = false;
			sort_icon.setImageResource(R.drawable.vod_sort_up_sl);
			if(allList.size() >= 30){
				collectAdapter.setList(allList.subList(0, 30));
			}else{
				collectAdapter.setList(allList);
			}
			tvCollect.setShow(allList.size(), 30,isInverted);
			gridview.setAdapter(collectAdapter);
		}
	}
	//�ֶ��������ߵ���ʾ������
	private void showLines() {
		int tag = allList.size() % 30 == 0 ? allList.size() / 30 : allList.size() / 30 + 1;
		if(tag > 5)
			tag = 5;
		if(tag <= 1){
			for(int i = 0; i < 4;i++ ){
				vod_line[i].setVisibility(View.INVISIBLE);
			}
		}
		if(tag > 1 && tag <= 5){
			for (int i = 0; i < tag - 1; i++) {
				vod_line[i].setVisibility(View.VISIBLE);
			}
			for(int i = tag - 1; i < 4;i++ ){
				vod_line[i].setVisibility(View.INVISIBLE);
			}
		}
	}
}
