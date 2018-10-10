package ipanel.join.widget;

import org.json.JSONArray;
import org.json.JSONException;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.View;
import android.content.Context;

public class ViewPager2 extends android.support.v4.view.ViewPager2 implements IConfigView{
	public static final String PROP_PAGE_SCROLL_DURATION = "pageScrollDuration";

	public static final String PROP_FOCUS_ITEM_OFFSET = "focusItemOffset";
	
	public static final String PROP_PAGE_MARGIN = "pageMargin";

	private View mData;
	
	private OnPageChangeListener mPageChangeListener;
	
	private boolean mControlFrame = true;
	
	public ViewPager2(Context context, View data) {
		super(context);
		this.mData = data;
		
		PropertyUtils.setCommonProperties(this, data);
		Bind bind = data.getBindByName(ViewPager.PROP_OFF_SCREEN_PAGE_LIMIT);
		if(bind != null){
			this.setOffscreenPageLimit(Integer.parseInt(bind.getValue().getvalue()));
		}
		
		bind = data.getBindByName(PROP_FOCUS_ITEM_OFFSET);
		if(bind != null){
			this.setFocusItemOffset(Integer.parseInt(bind.getValue().getvalue()));
		}
		
		bind = data.getBindByName(PROP_PAGE_MARGIN);
		if(bind != null){
			this.setPageMargin(Integer.parseInt(bind.getValue().getvalue()));
		}
		
		bind = data.getBindByName(PROP_PAGE_SCROLL_DURATION);
		if(bind != null){
			setPageScrollDuration(Integer.parseInt(bind.getValue().getvalue()));
		}
		
		super.setOnPageChangeListener(new OnPageChangeListener() {
			android.view.View mLastSelected;

			@Override
			public void onPageSelected(int position) {
				if (mLastSelected != null) {
					mLastSelected.setSelected(false);
					mLastSelected = null;
				}
				android.view.View v = getViewByPosition(position);
				if (v != null) {
					v.setSelected(true);
					mLastSelected = v;
				}
				if (mPageChangeListener != null)
					mPageChangeListener.onPageSelected(position);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				if(mPageChangeListener != null)
					mPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				if (mControlFrame) {
					if (state == SCROLL_STATE_SETTLING || state == SCROLL_STATE_DRAGGING) {
						ConfigState.getInstance().getFrameListener().freezeFrame();
					} else if (state == SCROLL_STATE_IDLE) {
						post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								ConfigState.getInstance().getFrameListener().updateFrame();
							}
						});
					}
				}
				if(mPageChangeListener != null)
					mPageChangeListener.onPageScrollStateChanged(state);
				
			}
		});
		bind = data.getBindByName(ViewPager.PROP_FRAGMENT_PAGER_ADAPTER);
		if(bind != null){
			try {
				JSONArray jsa = bind.getValue().getArrayValue();
				setAdapter(new BasicPagerAdapter(jsa));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		bind = data.getBindByName(ViewPager.PROP_LOOP_PAGER_ADAPTER);
		if(bind != null){
			try {
				JSONArray jsa = new JSONArray(bind.getValue().getvalue());
				setAdapter(new BasicPagerAdapter(jsa, true));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void setControlFrame(boolean control){
		this.mControlFrame = control;
	}
	
	
	@Override
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		this.mPageChangeListener = listener;
	}


	public android.view.View getViewByPosition(int pos){
		return findViewWithTag("_BasicPagerAdapter_"+pos);
	}
	
	@Override
	public View getViewData() {
		return mData;
	}

	@Override
	public void onAction(String type) {
		ActionUtils.handleAction(this, mData, type);
		
	}

	private boolean mShowFocusFrame = false;

	@Override
	public boolean showFocusFrame() {
		return mShowFocusFrame;
	}

	@Override
	public void setShowFocusFrame(boolean show) {
		mShowFocusFrame = show;
	}
}
