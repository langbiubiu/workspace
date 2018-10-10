package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.View;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.support.v4.view.VerticalViewPager;

public class VerticalPager extends VerticalViewPager implements IConfigView{

	public static final String PROP_LOOP_PAGER_ADAPTER = "LoopPagerAdapter";

	public static final String PROP_FRAGMENT_PAGER_ADAPTER = "FragmentPagerAdapter";

	public static final String PROP_OFF_SCREEN_PAGE_LIMIT = "offScreenPageLimit";
	
	public static final String PROP_PAGE_MARGIN = "pageMargin";

	private View mData;
	
	private OnPageChangeListener mPageChangeListener;
	
	public VerticalPager(Context context, View data) {
		super(context);
		this.mData = data;
		
		PropertyUtils.setCommonProperties(this, data);
		Bind bind = data.getBindByName(PROP_OFF_SCREEN_PAGE_LIMIT);
		if(bind != null){
			this.setOffscreenPageLimit(Integer.parseInt(bind.getValue().getvalue()));
		}
		
		bind = data.getBindByName(PROP_PAGE_MARGIN);
		if(bind != null){
			this.setPageMargin(Integer.parseInt(bind.getValue().getvalue()));
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
				if(state == SCROLL_STATE_SETTLING || state == SCROLL_STATE_DRAGGING){
					ConfigState.getInstance().getFrameListener().freezeFrame();
				} else if(state == SCROLL_STATE_IDLE) {
					post(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							ConfigState.getInstance().getFrameListener().updateFrame();
						}
					});
				}
				
				if(mPageChangeListener != null)
					mPageChangeListener.onPageScrollStateChanged(state);
				
			}
		});
		bind = data.getBindByName(PROP_FRAGMENT_PAGER_ADAPTER);
		if(bind != null){
			try {
				JSONArray jsa = bind.getValue().getArrayValue();
				setAdapter(new BasicPagerAdapter(jsa));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		bind = data.getBindByName(PROP_LOOP_PAGER_ADAPTER);
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
