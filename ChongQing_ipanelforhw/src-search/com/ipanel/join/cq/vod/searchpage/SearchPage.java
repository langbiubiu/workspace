package com.ipanel.join.cq.vod.searchpage;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki.Info.Star;
import com.ipanel.join.chongqing.portal.VolumePanel;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.player.SimplePlayerActivity;
import com.ipanel.join.cq.vod.player.VodPlayerManager;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.cq.vod.vodhome.BaseActivity;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;

public class SearchPage extends BaseActivity implements OnClickListener {
	private LinearLayout linearLayout0;
	private LinearLayout linearLayout1;
	private LinearLayout linearLayout2;
	private LinearLayout linearLayout3;
	private LinearLayout linearLayout4;
	private LinearLayout linearLayout5;
	private LinearLayout linearLayout6;
	private LinearLayout linearLayout7;
	private LinearLayout linearLayout8;
	private LinearLayout linearLayout9;
	private LinearLayout linearLayoutDeleteAll;
	private LinearLayout linearLayoutDeleteOne;
	private PopupWindow searchInput;
	private View searchWindownView;
	private EditText tv_searchcontent;
	private RelativeLayout rl_search;
	private TextView tv_one_middle;
	private TextView tv_two_left;
	private TextView tv_two_middle;
	private TextView tv_two_right;
	private TextView tv_three_middle;
	private ListView search_type_view;
	
	private TextView search_result;//搜索个数
	private ListView search_list;
	private TextView search_type;
	
	private static final int FLAG_ONE_MIDDLE = 1;
	private static final int FLAG_TWO_LEFT = 2;
	private static final int FLAG_TWO_MIDDLE = 3;
	private static final int FLAG_TWO_RIGHT = 4;
	private static final int FLAG_THREE_MIDDLE = 5;
	
	public static final int SET_LISTVIEW = 0x01;
	public static final int NO_SEARCH_DATA= 0x02;
	public static final int UPDATE_SEARCH = 0x03;
	
	private int currentPositonFlag = 0;
	private StringBuffer searchText = new StringBuffer();
//	private List<Program> allList;//记录搜索结果
	private SearchAdapter searchAdapter;
	private ImageFetcher mImageFetcher;
	boolean flag = false; // true表示不隐藏
	private List<Wiki> searchResult;//搜索结果
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case SET_LISTVIEW:
				search_list.setVisibility(View.VISIBLE);
				search_type.setText(getResources().getString(R.string.search_all));
//				search_result.setText(String.format(getResources().getString(R.string.search_total), allList.size()));
				searchAdapter = new SearchAdapter(SearchPage.this, searchResult);
				search_list.setAdapter(searchAdapter);
				search_type_view.setVisibility(View.VISIBLE);
				search_type_view.setAdapter(new SearchTypeAdapter(getTypeList(), SearchPage.this));
				break;
			case NO_SEARCH_DATA:
				search_list.setVisibility(View.INVISIBLE);
				search_result.setText(getResources().getString(R.string.no_search_data));
				search_type_view.setVisibility(View.INVISIBLE);
				break;
				
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_page);
		findViews();
		setListener();
		mImageFetcher = SharedImageFetcher.getNewFetcher(SearchPage.this, 3);
		volPanel = new VolumePanel(this);
		searchInput = new PopupWindow(searchWindownView, 200, 200, true);
		getHotKeyWord();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}
	
	private void getHotKeyWord() {
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_SEARCH_KEYWORD);
		request.getParam().setPage(1);
		request.getParam().setPage(20);
		
		serviceHelper.callServiceAsync(SearchPage.this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success && result!=null){
					List<String> hotKeyWord = result.getSearch_keyword();
					if(hotKeyWord!=null && hotKeyWord.size()>0){
						search_list.setVisibility(View.VISIBLE);
						search_list.setAdapter(new TopSearchAdapter(hotKeyWord));
						search_list.setFocusable(true);
					}
				}
			}
		});
	}

	private void setListener() {
		linearLayout0.setOnClickListener(this);
		linearLayout1.setOnClickListener(this);
		linearLayout2.setOnClickListener(this);
		linearLayout3.setOnClickListener(this);
		linearLayout4.setOnClickListener(this);
		linearLayout5.setOnClickListener(this);
		linearLayout6.setOnClickListener(this);
		linearLayout7.setOnClickListener(this);
		linearLayout8.setOnClickListener(this);
		linearLayout9.setOnClickListener(this);
		linearLayoutDeleteAll.setOnClickListener(this);
		linearLayoutDeleteOne.setOnClickListener(this);
		search_type_view.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {/*
//				typeIndex = position;
				if(position == 0){
					if(searchAdapter != null){
						searchAdapter = new SearchAdapter(SearchPage.this, allList);
						search_list.setAdapter(searchAdapter);
						search_type.setText(getResources().getString(R.string.search_all));
						search_result.setText(String.format(getResources().getString(R.string.search_total), allList.size()));
					}
				}else{
					String tab = (String) view.getTag();
					Logger.d("wuhd", "tag:"+tab);
					List<Program> tempList = sortProgramByType().get(tab);
					searchAdapter = new SearchAdapter(SearchPage.this, tempList);
					search_list.setAdapter(searchAdapter);
					search_type.setText(tab);
					search_result.setText(String.format(getResources().getString(R.string.search_total), tempList.size()));
				}
			*/}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		search_list.setItemsCanFocus(true);
		search_type_view.setItemsCanFocus(true);
	}

	private void findViews() {
		linearLayout0 = (LinearLayout) findViewById(R.id.ll_sercah0);
		linearLayout1 = (LinearLayout) findViewById(R.id.ll_sercah1);
		linearLayout2 = (LinearLayout) findViewById(R.id.ll_sercah2);
		linearLayout3 = (LinearLayout) findViewById(R.id.ll_sercah3);
		linearLayout4 = (LinearLayout) findViewById(R.id.ll_sercah4);
		linearLayout5 = (LinearLayout) findViewById(R.id.ll_sercah5);
		linearLayout6 = (LinearLayout) findViewById(R.id.ll_sercah6);
		linearLayout7 = (LinearLayout) findViewById(R.id.ll_sercah7);
		linearLayout8 = (LinearLayout) findViewById(R.id.ll_sercah8);
		linearLayout9 = (LinearLayout) findViewById(R.id.ll_sercah9);
		tv_searchcontent = (EditText) findViewById(R.id.search_content);
		linearLayoutDeleteAll = (LinearLayout) findViewById(R.id.ll_delete_all);
		linearLayoutDeleteOne = (LinearLayout) findViewById(R.id.ll_delete_one);
		searchWindownView = getLayoutInflater().inflate(R.layout.search_input,
				null);
		rl_search = (RelativeLayout) searchWindownView.findViewById(R.id.rl_search_input);
		
		search_result = (TextView)this.findViewById(R.id.search_result);
		search_type = (TextView)this.findViewById(R.id.movie_tv);
		search_list = (ListView)this.findViewById(R.id.search_list);
		search_type_view = (ListView)this.findViewById(R.id.search_type_list);
		//以下是对焦点进行代码控制
		linearLayout3.setOnKeyListener(new OnKeyListener() {
			
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(event.getAction() == KeyEvent.ACTION_DOWN){
						if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
							moveFocus();
							return true;
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
							return true;
						}
					}
					return false;
				}
			});
		linearLayout6.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
						moveFocus();
						return true;
					}
				}
				return false;
			}
		});
		linearLayout9.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
						moveFocus();
						return true;
					}
				}
				return false;
			}
		});
		linearLayoutDeleteAll.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
						moveFocus();
						return true;
					}
				}
				return false;
			}

		});
		
	}
	
	private void moveFocus() {
		if(search_list!=null && search_list.isShown()&&search_list.getSelectedView()!=null){
			search_list.getSelectedView().requestFocus();
		}
	}
	/**
	 * 搜索
	 * @param string
	 */
	private void excuteSearch(String model,String letter,String title) {/*
		Logger.d("wuhd", "keyword:"+keyword);
		String url = HomedAPI.SEARCH_SEARCH_BY_KEYWORD;
		RequestParams req = new RequestParams();
		HomedDataManager.fillCommonParams(req);
		req.put("keyword", keyword.toLowerCase());
		req.put("label", "0");//可选 分类类型，多个分类，用“|”分割,取值“0”表示所有。
		req.put("pageidx", "1");
		req.put("pagenum", "200");
		req.put("postersize", "320x400|100x100");
		req.put("matchingtype", matchingtype+"");
		JSONApiHelper.callJSONAPI(this, CallbackType.ForceUpdate, url, req, new StringResponseListener() {
			
			@Override
			public void onResponse(String content) {
				Logger.d("wuhd", "search content-->"+content);
				RespProgramList resp = HomedDataManager.gson.fromJson(content, RespProgramList.class);
				if(resp.ret==0){
					if(resp.total > 0){
						allList = resp.list;
						mHandler.sendEmptyMessage(SET_LISTVIEW);
					}else{
						//没有搜索结果
						mHandler.sendEmptyMessage(NO_SEARCH_DATA);
					}
				}else{
					Toast.makeText(SearchPage.this, resp.ret_msg, Toast.LENGTH_SHORT).show();
					mHandler.sendEmptyMessage(NO_SEARCH_DATA);
				}
			}
		});
	*/
		serviceHelper.cancelAllTasks();
		Logger.d("excute search", "mode:"+model+",letter:"+",title"+title);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_SEARCH_WIKIS);
		request.getDeveloper().setApikey(HWDataManager.APIKEY);
		request.getDeveloper().setSecretkey(HWDataManager.SECRETKEY);
		//设置搜索参数
		request.getParam().setModel(model);
		request.getParam().setLetter(letter);
		request.getParam().setTitle(title);
		request.getParam().setPage(1);
		request.getParam().setPagesize(30);
		
		serviceHelper.callServiceAsync(SearchPage.this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success && result!=null){
					searchResult = result.getWikis();
					if(searchResult!=null && searchResult.size() > 0){
						mHandler.sendEmptyMessage(SET_LISTVIEW);
					}else{
						//没有搜索结果
						Tools.showToastMessage(getBaseContext(), "没有搜索到相关数据");
						mHandler.sendEmptyMessage(NO_SEARCH_DATA);
					}
				}else{
					Tools.showToastMessage(getBaseContext(), "获取数据失败");
					mHandler.sendEmptyMessage(NO_SEARCH_DATA);
				}
			}
		});
	}
	//按类型分类
//	private Map<String, List<Program>> sortProgramByType(){
//		Map<String, List<Program>> mData = new HashMap<String, List<Program>>();
//		List<Program> list1 = new ArrayList<Program>();
//		List<Program> list2 = new ArrayList<Program>();
//		List<Program> list3 = new ArrayList<Program>();
//		List<Program> list4 = new ArrayList<Program>();
//		List<Program> list5 = new ArrayList<Program>();
//		List<Program> list6 = new ArrayList<Program>();
//		List<Program> list7 = new ArrayList<Program>();
//		List<Program> list8 = new ArrayList<Program>();
//		List<Program> list9 = new ArrayList<Program>();
//		for (int i = 0; i < allList.size(); i++) {
//			Program item = allList.get(i);
//			switch (item.type) {
//			case 1:
//				list1.add(item);
//				break;
//			case 2:
//				list2.add(item);
//				break;
//			case 3:
//				list3.add(item);
//				break;
//			case 4:
//				list4.add(item);
//				break;
//			case 5:
//				list5.add(item);
//				break;
//			case 6:
//				list6.add(item);
//				break;
//			case 7:
//				list7.add(item);
//				break;
//			case 8:
//				list8.add(item);
//				break;
//			case 9:
//				list9.add(item);
//				break;
//			default:
//				break;
//			}
//		}
//		if(list2.size()>0){
//			mData.put("点播", list2);
//		}
//		if(list4.size()>0){
//			mData.put("回看", list4);
//		}
//		if(list1.size()>0){
//			mData.put("频道直播", list1);
//		}
//		if(list3.size()>0){
//			mData.put("应用", list3);
//		}
//		if(list5.size()>0){
//			mData.put("音乐", list5);
//		}
//		if(list6.size()>0){
//			mData.put("歌手", list6);
//		}
//		if(list7.size()>0){
//			mData.put("专辑", list7);
//		}
//		if(list8.size()>0){
//			mData.put("资讯", list8);
//		}
//		if(list9.size()>0){
//			mData.put("监控频道", list9);
//		}
//		return mData;
//	}
	
	private List<String> getTypeList(){
		List<String> type = new ArrayList<String>();
		/*Map<String, List<Program>> mData = sortProgramByType();
		type.add(String.format(getResources().getString(R.string.search_type_all), allList.size()));
//		Iterator<String> it = mData.keySet().iterator();
//		while (it.hasNext()) {
//			String str = it.next();
//			type.add(str + "("+mData.get(str).size()+")");
//		}
		for (int i = 1; i <= 9; i++) {
			String str = null;
			switch (i) {
			case 1:
				str = "点播";
				break;
			case 2:
				str = "回看";
				break;
			case 3:
				str = "频道直播";
				break;
			case 4:
				str = "应用";
				break;
			case 5:
				str = "音乐";
				break;
			case 6:
				str = "歌手";
				break;
			case 7:
				str = "专辑";
				break;
			case 8:
				str = "资讯";
				break;
			case 9:
				str = "监控频道";
				break;
			}
			if(mData.get(str) !=null && mData.get(str).size() > 0 ){
				type.add(str + "("+mData.get(str).size()+")");
			}
		}*/
		return type;
		}
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.ll_sercah1:
			searchText.append("1");
			tv_searchcontent.setText(searchText.toString());
			excuteSearch("",searchText.toString(),"");
			break;
		case R.id.ll_sercah2:
			initSearchControl();
			setSearchWindownText("2", "A", "B", "C","");
			searchInput.showAsDropDown(linearLayout2, -35, -175);
			searchWindownView.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_TWO_LEFT
									|| currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_up);
								currentPositonFlag = FLAG_ONE_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchInput.dismiss();
								linearLayout5.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_left);
								currentPositonFlag = FLAG_TWO_LEFT;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchInput.dismiss();
								linearLayout1.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}

							break;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_right);
								currentPositonFlag = FLAG_TWO_RIGHT;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchInput.dismiss();
								linearLayout3.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchText.append("B");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchText.append("A");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchText.append("2");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchText.append("C");
								tv_searchcontent.setText(searchText.toString());
							}
							tv_searchcontent.setSelection(searchText.length()-1);
							excuteSearch("",searchText.toString(),"");
							//progressBar.setVisibility(View.VISIBLE);
							break;
						}
					}
					return true;
				}
			});
			break;
		case R.id.ll_sercah3:
			initSearchControl();
			setSearchWindownText("3", "D", "E", "F","");
			searchInput.showAsDropDown(linearLayout3, -35, -175);
			searchWindownView.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_TWO_LEFT
									|| currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_up);
								currentPositonFlag = FLAG_ONE_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchInput.dismiss();
								linearLayout6.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_left);
								currentPositonFlag = FLAG_TWO_LEFT;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchInput.dismiss();
								linearLayout2.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}

							break;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_right);
								currentPositonFlag = FLAG_TWO_RIGHT;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchText.append("E");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchText.append("D");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchText.append("3");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchText.append("F");
								tv_searchcontent.setText(searchText.toString());
							}
							tv_searchcontent.setSelection(searchText.length()-1);
							excuteSearch("",searchText.toString(),"");
							//progressBar.setVisibility(View.VISIBLE);
							break;
						}
					}
					return true;
				}
			});
			break;
		case R.id.ll_sercah4:
			initSearchControl();
			setSearchWindownText("4", "G", "H", "I","");
			searchInput.showAsDropDown(linearLayout4, -35, -175);
			searchWindownView.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_TWO_LEFT
									|| currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_up);
								currentPositonFlag = FLAG_ONE_MIDDLE;
							} else if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchInput.dismiss();
								linearLayout1.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchInput.dismiss();
								linearLayout7.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_left);
								currentPositonFlag = FLAG_TWO_LEFT;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}

							break;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_right);
								currentPositonFlag = FLAG_TWO_RIGHT;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchInput.dismiss();
								linearLayout5.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchText.append("H");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchText.append("G");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchText.append("4");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchText.append("I");
								tv_searchcontent.setText(searchText.toString());
							}
							tv_searchcontent.setSelection(searchText.length()-1);
							excuteSearch("",searchText.toString(),"");
							//progressBar.setVisibility(View.VISIBLE);
							break;
						}
					}
					return true;
				}
			});
			break;
		case R.id.ll_sercah5:
			initSearchControl();
			setSearchWindownText("5", "J", "K", "L","");
			searchInput.showAsDropDown(linearLayout5, -35, -175);
			searchWindownView.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_TWO_LEFT
									|| currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_up);
								currentPositonFlag = FLAG_ONE_MIDDLE;
							} else if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchInput.dismiss();
								linearLayout2.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchInput.dismiss();
								linearLayout8.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_left);
								currentPositonFlag = FLAG_TWO_LEFT;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchInput.dismiss();
								linearLayout4.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}

							break;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_right);
								currentPositonFlag = FLAG_TWO_RIGHT;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchInput.dismiss();
								linearLayout6.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_ENTER:
						case KeyEvent.KEYCODE_DPAD_CENTER:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchText.append("K");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchText.append("J");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchText.append("5");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchText.append("L");
								tv_searchcontent.setText(searchText.toString());
							}
							tv_searchcontent.setSelection(searchText.length()-1);
							excuteSearch("",searchText.toString(),"");
							//progressBar.setVisibility(View.VISIBLE);
							break;
						}
					}
					return true;
				}
			});
			break;
		case R.id.ll_sercah6:
			initSearchControl();
			setSearchWindownText("6", "M", "N", "O","");
			searchInput.showAsDropDown(linearLayout6, -35, -175);
			searchWindownView.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_TWO_LEFT
									|| currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_up);
								currentPositonFlag = FLAG_ONE_MIDDLE;
							} else if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchInput.dismiss();
								linearLayout3.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchInput.dismiss();
								linearLayout9.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_left);
								currentPositonFlag = FLAG_TWO_LEFT;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchInput.dismiss();
								linearLayout5.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}

							break;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_right);
								currentPositonFlag = FLAG_TWO_RIGHT;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_ENTER:
						case KeyEvent.KEYCODE_DPAD_CENTER:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchText.append("N");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchText.append("M");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchText.append("6");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchText.append("O");
								tv_searchcontent.setText(searchText.toString());
							}
							tv_searchcontent.setSelection(searchText.length()-1);
							excuteSearch("",searchText.toString(),"");
							break;
						}
					}
					return true;
				}
			});
			break;
		case R.id.ll_sercah7:
			initSearchControl();
			setSearchWindownText("7", "P", "Q", "R","S");
			searchInput.showAsDropDown(linearLayout7, -35, -175);
			searchWindownView.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_TWO_LEFT
									|| currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_up);
								currentPositonFlag = FLAG_ONE_MIDDLE;
							} else if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchInput.dismiss();
								linearLayout4.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}else if(currentPositonFlag == FLAG_THREE_MIDDLE){
								rl_search
								.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE||currentPositonFlag == FLAG_TWO_LEFT
									||currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
								.setBackgroundResource(R.drawable.search_select04);
								currentPositonFlag = FLAG_THREE_MIDDLE;
							}else if(currentPositonFlag == FLAG_THREE_MIDDLE){
								searchInput.dismiss();
								linearLayoutDeleteOne.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_left);
								currentPositonFlag = FLAG_TWO_LEFT;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}

							break;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_right);
								currentPositonFlag = FLAG_TWO_RIGHT;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchInput.dismiss();
								linearLayout8.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_ENTER:
						case KeyEvent.KEYCODE_DPAD_CENTER:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchText.append("Q");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchText.append("P");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchText.append("7");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchText.append("R");
								tv_searchcontent.setText(searchText.toString());
							}else if(currentPositonFlag == FLAG_THREE_MIDDLE){
								searchText.append("S");
								tv_searchcontent.setText(searchText.toString());
							}
							tv_searchcontent.setSelection(searchText.length()-1);
							excuteSearch("",searchText.toString(),"");
							//progressBar.setVisibility(View.VISIBLE);
							break;
						}
					}
					return true;
				}
			});
			break;
		case R.id.ll_sercah8:
			initSearchControl();
			setSearchWindownText("8", "T", "U", "V","");
			searchInput.showAsDropDown(linearLayout8, -35, -175);
			searchWindownView.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_TWO_LEFT
									|| currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_up);
								currentPositonFlag = FLAG_ONE_MIDDLE;
							} else if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchInput.dismiss();
								linearLayout5.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchInput.dismiss();
								linearLayout0.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE
									|| currentPositonFlag == FLAG_THREE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_left);
								currentPositonFlag = FLAG_TWO_LEFT;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchInput.dismiss();
								linearLayout7.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}

							break;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE
									|| currentPositonFlag == FLAG_THREE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_right);
								currentPositonFlag = FLAG_TWO_RIGHT;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchInput.dismiss();
								linearLayout9.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchText.append("U");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchText.append("T");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchText.append("8");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchText.append("V");
								tv_searchcontent.setText(searchText.toString());
							}
							tv_searchcontent.setSelection(searchText.length()-1);
							excuteSearch("",searchText.toString(),"");
							//progressBar.setVisibility(View.VISIBLE);
							break;
						}
					}
					return true;
				}
			});
			break;
		case R.id.ll_sercah9:
			initSearchControl();
			setSearchWindownText("9", "W", "X"," Y", "Z");
			searchInput.showAsDropDown(linearLayout9, -35, -175);
			searchWindownView.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							if (currentPositonFlag == FLAG_TWO_MIDDLE || currentPositonFlag == FLAG_TWO_LEFT
									|| currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
								.setBackgroundResource(R.drawable.vod_focus_keyboard_press_up);
								currentPositonFlag = FLAG_ONE_MIDDLE;
							}else if(currentPositonFlag == FLAG_THREE_MIDDLE){
								rl_search
								.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}else if(currentPositonFlag == FLAG_ONE_MIDDLE){
								searchInput.dismiss();
								linearLayout6.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (currentPositonFlag == FLAG_THREE_MIDDLE) {
								searchInput.dismiss();
								linearLayoutDeleteAll.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}else if(currentPositonFlag == FLAG_ONE_MIDDLE){
								rl_search
								.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}else if(currentPositonFlag == FLAG_TWO_MIDDLE 
									|| currentPositonFlag == FLAG_TWO_LEFT
									|| currentPositonFlag == FLAG_TWO_RIGHT){
								rl_search
								.setBackgroundResource(R.drawable.search_select04);
								currentPositonFlag = FLAG_THREE_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE
									|| currentPositonFlag == FLAG_THREE_MIDDLE ) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_left);
								currentPositonFlag = FLAG_TWO_LEFT;
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchInput.dismiss();
								linearLayout8.requestFocus();
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}

							break;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if (currentPositonFlag == FLAG_TWO_MIDDLE
									|| currentPositonFlag == FLAG_ONE_MIDDLE
									|| currentPositonFlag == FLAG_THREE_MIDDLE) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_right);
								currentPositonFlag = FLAG_TWO_RIGHT;
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								rl_search
										.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
								currentPositonFlag = FLAG_TWO_MIDDLE;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:
							if (currentPositonFlag == FLAG_ONE_MIDDLE) {
								searchText.append("X");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_LEFT) {
								searchText.append("W");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_MIDDLE) {
								searchText.append("9");
								tv_searchcontent.setText(searchText.toString());
							} else if (currentPositonFlag == FLAG_TWO_RIGHT) {
								searchText.append("Y");
								tv_searchcontent.setText(searchText.toString());
							}else if(currentPositonFlag == FLAG_THREE_MIDDLE){
								searchText.append("Z");
								tv_searchcontent.setText(searchText.toString());
							}
							tv_searchcontent.setSelection(searchText.length()-1);
							excuteSearch("",searchText.toString(),"");
							//progressBar.setVisibility(View.VISIBLE);
							break;
						}
					}
					return true;
				}
			});
			break;
		case R.id.ll_sercah0:
			searchText.append("0");
			tv_searchcontent.setText(searchText.toString());
			tv_searchcontent.setSelection(searchText.length()-1);
			excuteSearch("",searchText.toString(),"");
			//progressBar.setVisibility(View.VISIBLE);
			break;
		case R.id.ll_delete_all:// 删除所有
			if (searchText.length() <= 0) {
				return;
			} else {
				searchText.delete(0, searchText.length());
				tv_searchcontent.setText(searchText.toString());
				if (searchText.length() <= 0) {
					//清空数据
					mHandler.sendEmptyMessage(NO_SEARCH_DATA);
				} 
			}

			break;
		case R.id.ll_delete_one:// 删除一个字母
			if (searchText.length() <= 0) {
				return;
			} else {
				searchText.delete(searchText.length() - 1, searchText.length());
				tv_searchcontent.setText(searchText.toString());
				if (searchText.length() <= 0) {
					//清空数据
					mHandler.sendEmptyMessage(NO_SEARCH_DATA);
				} else {
					tv_searchcontent.setSelection(searchText.length()-1);
					excuteSearch("",searchText.toString(),"");
				}
			}
			break;
		default:
			break;
		}
	}

	private void initSearchControl() {
		searchInput.setBackgroundDrawable(new BitmapDrawable());
		searchInput.setFocusable(true);
		rl_search = (RelativeLayout) searchWindownView
				.findViewById(R.id.rl_search_input);
		tv_one_middle = (TextView) searchWindownView
				.findViewById(R.id.tv_one_middle);
		tv_two_left = (TextView) searchWindownView
				.findViewById(R.id.tv_two_left);
		tv_two_middle = (TextView) searchWindownView
				.findViewById(R.id.tv_two_middle);
		tv_two_right = (TextView) searchWindownView
				.findViewById(R.id.tv_two_right);
		tv_three_middle = (TextView)searchWindownView
				.findViewById(R.id.tv_three_middle);
		searchWindownView.setFocusable(true);
		currentPositonFlag = FLAG_TWO_MIDDLE;
		rl_search.setBackgroundResource(R.drawable.vod_focus_keyboard_press_mid);
	}
	
	private void setSearchWindownText(String twoMiddle, String twoLeft,
			String oneMiddle, String rightdigital,String threeMiddle) {
		tv_two_middle.setText(twoMiddle);
		tv_two_left.setText(twoLeft);
		tv_two_right.setText(rightdigital);
		tv_one_middle.setText(oneMiddle);
		tv_three_middle.setText(threeMiddle);
	}
	
	class SearchAdapter extends BaseAdapter {
		private Context context;
		private List<Wiki> list;
		
		public List<Wiki> getList() {
			return list;
		}

		public SearchAdapter(Context context,List<Wiki> list){
			this.context = context;
			this.list = list;
		}
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup container) {
			final Wiki item = list.get(position);
			convertView = LayoutInflater.from(context).inflate(R.layout.search_list_item, null);
			convertView.setTag(position);
			convertView.clearFocus();
			final RelativeLayout search_rl = (RelativeLayout)convertView.findViewById(R.id.search_rl);
			ImageView icon = (ImageView)convertView.findViewById(R.id.search_icon);
			final TextView name= (TextView)convertView.findViewById(R.id.search_name);
			
			final RelativeLayout search_rl2= (RelativeLayout)convertView.findViewById(R.id.search_rl2);
			final TextView tv_name = (TextView)convertView.findViewById(R.id.search_video_name);
			final ImageView poster = (ImageView)convertView.findViewById(R.id.search_poster);
			TextView actors = (TextView)convertView.findViewById(R.id.search_actor_name);
			TextView diretor = (TextView)convertView.findViewById(R.id.search_director_name);
			TextView region = (TextView)convertView.findViewById(R.id.search_region_name);
			TextView score = (TextView)convertView.findViewById(R.id.search_douban_score);
			
			name.setText(item.getTitle());
			tv_name.setText(item.getTitle());
			score.setText(item.getInfo().getAverage());
			//演员列表
			List<Star> actorList = item.getInfo().getStarring();
			if(actorList != null && actorList.size() > 0){
				StringBuffer actorBuffer = new StringBuffer();
				for (int i = 0; i < actorList.size(); i++) {
					if(i != (actorList.size() - 1))
						actorBuffer.append(actorList.get(i).getTitle()+"/");
					else
						actorBuffer.append(actorList.get(i).getTitle());
				}
				actors.setText(actorBuffer.toString());
			}
			//导演列表
			List<Star> diretList = item.getInfo().getDirector();
			if(diretList !=null && diretList.size() > 0){
				StringBuffer diretBuffer = new StringBuffer();
				for (int i = 0; i < diretList.size(); i++) {
					if(i != (diretList.size() - 1))
						diretBuffer.append(diretList.get(i).getTitle()+"/");
					else
						diretBuffer.append(diretList.get(i).getTitle());
				}
				diretor.setText(diretBuffer.toString());
			}
			region.setText(item.getInfo().getCountry());
			//TODO 匹配关键字，改变字体颜色
//			Logger.d("wuhd", "match word:"+item.matching_word);
//			if(item.name.contains(item.matching_word)){
//				int index = item.name.indexOf(item.matching_word);
//				int len = item.matching_word.length();
//				formatTextColor(name, index, len);
//				formatTextColor2(tv_name, index, len);
//			}
			if(item.getModel().equals("film")||item.getModel().equals("teleplay")){
				//电影、电视剧
				icon.setImageResource(R.drawable.search_icon02);
			}else {
				//直播
				icon.setImageResource(R.drawable.search_icon06);
			}
			if(position==0){
				search_rl.setVisibility(View.GONE);
				search_rl2.setVisibility(View.VISIBLE);
				if(item.getCover()!=null){
					BaseImageFetchTask task = mImageFetcher.getBaseTask(item.getCover());
					mImageFetcher.setLoadingImage(R.drawable.default_poster);
					mImageFetcher.setImageSize(160, 200);
					mImageFetcher.loadImage(task, poster);
				}else{
					poster.setImageResource(R.drawable.default_poster);
				}
			}
			convertView.setTag(position);
			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus){
						Logger.d("wuhd", "onFocusChange11111");
						search_rl.setVisibility(View.GONE);
						search_rl2.setVisibility(View.VISIBLE);
						if(item.getCover()!=null){
							BaseImageFetchTask task = mImageFetcher.getBaseTask(item.getCover());
							mImageFetcher.setLoadingImage(R.drawable.default_poster);
							mImageFetcher.setImageSize(160, 200);
							mImageFetcher.loadImage(task, poster);
						}else{
							poster.setImageResource(R.drawable.default_poster);
						}
						tv_name.setSelected(true);
					}else{
						Logger.d("wuhd", "onFocusChange22222");
						Logger.d("wuhd", "position"+position+";flag = "+flag);
						if (!flag) {
							search_rl.setVisibility(View.VISIBLE);
							search_rl2.setVisibility(View.GONE);	
						}
						tv_name.setSelected(false);
					}
				}
			});
			
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(item.getModel().equals("film")||item.getModel().equals("teleplay")){
						HWDataManager.openDetail(getBaseContext(), item.getId(), item.getTitle(), item);
					}else{
						Tools.showToastMessage(getBaseContext(), ""+item.getModel());
					}
				}
			});
			convertView.setOnKeyListener(new OnKeyListener() {
				
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(event.getAction() == KeyEvent.ACTION_DOWN){
						if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
							if(search_type_view!=null && search_type_view.isShown()&&search_type_view.getSelectedView()!=null){
//								if(search_type_view.getChildAt(typeIndex)!=null)
//									search_type_view.getChildAt(typeIndex).requestFocus();
								flag = true;
								search_type_view.getSelectedView().requestFocus();
								return true;
							}
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
							flag = false;
							if(position == 0){
								return true;
							}
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
							flag = false;
//							if(position==(list.size()-1)){
//								return true;
//							}
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
							flag = true;
						}
					}
					return false;
				}
			});
			return convertView;
		}
		
		class ViewHolder{
			RelativeLayout rly1;
			RelativeLayout rly2;
			ImageView icon;
			ImageView poster;
			TextView tv_name;
			TextView actors;
			TextView diretor;
			TextView region;
			TextView score;
			TextView name;
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
				helper.callServiceAsync(SearchPage.this, req, Channel.class, new ResponseHandlerT<Channel>(){

					@Override
					public void onResponse(boolean success, Channel result) {
						if(result != null){
							com.ipanel.join.homed.chongqing.HomedDataManager.goToLiveActivityVarServiceId(SearchPage.this,result.getServiceId());
						}
					}
				});
			}
		*/}
		
		private void formatTextColor(TextView tv,int index,int len) {
			SpannableString styledText = new SpannableString(tv.getText().toString());  
	        styledText.setSpan(new TextAppearanceSpan(context,  R.style.search_text_style1), index, index+len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  
	        tv.setText(styledText, TextView.BufferType.SPANNABLE);
		}
		private void formatTextColor2(TextView tv,int index,int len) {
			SpannableString styledText = new SpannableString(tv.getText().toString());  
	        styledText.setSpan(new TextAppearanceSpan(context,  R.style.search_text_style2), index, index+len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  
	        tv.setText(styledText, TextView.BufferType.SPANNABLE);
		}
	}
	
	
	class SearchTypeAdapter extends BaseAdapter{
		List<String> list;
		Context context;
		
		public SearchTypeAdapter(List<String> list, Context context) {
			this.list = list;
			this.context = context;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = LayoutInflater.from(context).inflate(R.layout.search_type_item, parent,false);
			}
			TextView text = (TextView)convertView.findViewById(R.id.search_type);
			text.setText(list.get(position));
			String tag = list.get(position).split("\\(")[0];
			convertView.setOnKeyListener(new OnKeyListener() {
				
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(event.getAction() == KeyEvent.ACTION_DOWN){
						if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
//							typeIndex = position;
							moveFocus();
							return true;
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
							return true;
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
							if(position==0){
								return true;
							}
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
							if(position==(list.size()-1)){
								return true;
							}
						}
					}
					return false;
				}
			});
			convertView.setTag(tag);
			return convertView;
		}
	}
	
	class TopSearchAdapter extends BaseAdapter{

		public List<String> list;
		
		public TopSearchAdapter(List<String> list){
			this.list = list;
		} 
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.search_hotkeyword_item, parent,false);
			}
			TextView text = (TextView)convertView.findViewById(R.id.search_item);
			text.setText(list.get(position));
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					excuteSearch("","",list.get(position));
				}
			});
			return convertView;
		}
	}
	
	@Override
	public void onBackPressed() {
		if(!tv_searchcontent.getText().toString().equals("")){
			onClick(linearLayoutDeleteOne);
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(volPanel.onKeyDown(keyCode, event)){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
