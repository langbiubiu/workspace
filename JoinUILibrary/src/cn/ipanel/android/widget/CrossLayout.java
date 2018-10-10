package cn.ipanel.android.widget;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.support.v4.view.ViewPager2;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

/**
 * 
 * @author Zexu
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CrossLayout extends FrameLayout {

	public interface OnSelectionChangeListener {
		public void onSelectionChange(ListAdapter mAdapter, int position);
	}

	VerticalViewPager2 vPager;
	ViewPager2 hPager;

	OnSelectionChangeListener mListener;

	public CrossLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CrossLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CrossLayout(Context context) {
		super(context);
		init();
	}

	private void init() {
		setClipChildren(false);
		vPager = new VerticalViewPager2(getContext());
		vPager.setClipToPadding(false);
		vPager.setClipChildren(false);
		vPager.setFocusItemOffset(-2);
		vPager.setAdapter(mVAdapter = new VAdapter());
		vPager.setPageTransformer(false, new VerticalViewPager2.PageTransformer() {

			@Override
			public void transformPage(View page, float position) {
				position = Math.abs(position);
				if (position < 1f / vsize) {
					float scale = 1f + centerOverScale * (1 - position * vsize);
					page.setScaleX(scale);
					page.setScaleY(scale);
				} else {
					page.setScaleX(1f);
					page.setScaleY(1f);
				}
			}
		});
		vPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				mHAdapter.notifyDataSetChanged();
				if (mListener != null)
					mListener.onSelectionChange(mAdapter, getCurrentPosition());
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub

			}
		});
		addView(vPager);

		hPager = new ViewPager2(getContext());
		hPager.setClipToPadding(false);
		hPager.setClipChildren(false);
		hPager.setFocusItemOffset(-2);
		hPager.setAdapter(mHAdapter = new HAdapter());
		hPager.setPageTransformer(false, new ViewPager2.PageTransformer() {

			@Override
			public void transformPage(View page, float position) {
				position = Math.abs(position);
				if (position < 1f / 3) {
					float scale = 1 + centerOverScale * (1 - position * 3);
					page.setScaleX(scale);
					page.setScaleY(scale);
				} else {
					page.setScaleX(1f);
					page.setScaleY(1f);
				}
			}

		});
		hPager.setOnPageChangeListener(new ViewPager2.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				mVAdapter.notifyDataSetChanged();
				if (mListener != null)
					mListener.onSelectionChange(mAdapter, getCurrentPosition());

			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub

			}
		});
		addView(hPager);
	}
	
	/**
	 * 设置竖直滚动的时间
	 * 
	 * */
	public void setVPageScrollDuration(int time){
		vPager.setPageScrollDuration(time);
	}
	//设置水平滚动的时间
	public void setHPageScrollDuration(int time){
		hPager.setPageScrollDuration(time);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		int width = right - left;
		int height = bottom - top;
		int w3 = width / hsize;
		int h3 = height / vsize;
		hPager.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(h3, MeasureSpec.EXACTLY));
		vPager.measure(MeasureSpec.makeMeasureSpec(w3, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
		hPager.layout(0, (height - h3) / 2, width, height - (height - h3) / 2);
		vPager.layout((width - w3) / 2, 0, width - (width - w3) / 2, height);
	}

	long lastEventTime = 0;
	final static long KEY_REPEAT_LIMIT = 20;

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (SystemClock.currentThreadTimeMillis() - lastEventTime < KEY_REPEAT_LIMIT)
					return true;
				moveHorizontal(-1);
				lastEventTime = SystemClock.currentThreadTimeMillis();
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (SystemClock.currentThreadTimeMillis() - lastEventTime < KEY_REPEAT_LIMIT)
					return true;
				moveHorizontal(1);
				lastEventTime = SystemClock.currentThreadTimeMillis();
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				if (SystemClock.currentThreadTimeMillis() - lastEventTime < KEY_REPEAT_LIMIT)
					return true;
				moveVertical(-1);
				lastEventTime = SystemClock.currentThreadTimeMillis();
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (SystemClock.currentThreadTimeMillis() - lastEventTime < KEY_REPEAT_LIMIT)
					return true;
				moveVertical(1);
				lastEventTime = SystemClock.currentThreadTimeMillis();
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	private void moveVertical(int delta) {
		if (vIndex + delta < 0)
			return;
		vIndex += delta;
		vPager.findViewWithTag(vPager.getCurrentItem()).setVisibility(VISIBLE);
		vPager.requestFocus();
		vPager.bringToFront();
		vPager.setCurrentItem(vIndex);
		showHValue = false;
		mHAdapter.notifyDataSetChanged();
	}

	private void moveHorizontal(int delta) {
		if (hIndex + delta < 0)
			return;
		hIndex += delta;
		hPager.findViewWithTag(hPager.getCurrentItem()).setVisibility(VISIBLE);
		hPager.requestFocus();
		hPager.bringToFront();
		hPager.setCurrentItem(hIndex);
		showHValue = true;
		mVAdapter.notifyDataSetChanged();
	}

	VAdapter mVAdapter;
	HAdapter mHAdapter;

	boolean showHValue = true;

	ListAdapter mAdapter;
	int wrap = 10;

	int vsize = 3;
	int hsize = 3;

	float centerOverScale = 0.8f;

	private int hIndex = 0;
	private int vIndex = 0;

	public int getCurrentPosition() {
		return getPosition(hIndex, vIndex);
	}

	public int getPosition(int h, int v) {
		return (wrap * (h % getHcount()) + v % mAdapter.getCount()) % mAdapter.getCount();
	}

	public void setCurrentPosition(int position) {
		int v = position % wrap;
		int h = position / wrap;
		hIndex = 100000 * getHcount() + h;
		vIndex = 100000 * mAdapter.getCount() + v;

		vPager.setCurrentItem(vIndex, false);
		hPager.setCurrentItem(hIndex, false);
	}

	public ListAdapter getAdapter() {
		return mAdapter;
	}

	public void setAdapter(ListAdapter mAdapter) {
		setAdapter(mAdapter, 0);
	}

	public void setAdapter(ListAdapter mAdapter, int initPosition) {
		mCache.clear();
		this.mAdapter = mAdapter;
		setCurrentPosition(initPosition);
	}

	public int getWrap() {
		return wrap;
	}

	public void setWrap(int wrap) {
		this.wrap = wrap;
	}

	public int getVsize() {
		return vsize;
	}

	public void setVsize(int vsize) {
		this.vsize = vsize;
	}

	public int getHsize() {
		return hsize;
	}

	public void setHsize(int hsize) {
		this.hsize = hsize;
	}

	protected int getHcount() {
		int c = mAdapter.getCount();
		int row = c / wrap;
		if (c % wrap != 0) {
			row += 1;
		}
		return row;
	}

	public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
		this.mListener = listener;
	}

	public void setCenterOverScale(float scale) {
		this.centerOverScale = scale;
	}

	public ViewPager2 getHorizontalPager() {
		return hPager;
	}

	public VerticalViewPager2 getVerticalPager() {
		return vPager;
	}

	private List<SoftReference<View>> mCache = new ArrayList<SoftReference<View>>();

	View getConvertView() {
		Iterator<SoftReference<View>> it = mCache.iterator();
		while (it.hasNext()) {
			SoftReference<View> sr = it.next();
			it.remove();
			View v = sr.get();
			if (v != null)
				return v;
		}
		return null;
	}

	class VAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			if (mAdapter != null)
				return Integer.MAX_VALUE;
			return 0;
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
			mCache.add(new SoftReference<View>((View) object));
		}

		@Override
		public float getPageWidth(int position) {
			return 1f / vsize;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View v = mAdapter.getView(getPosition(hIndex, position), getConvertView(), container);
			v.setVisibility(showHValue && position == vPager.getCurrentItem() ? View.INVISIBLE
					: View.VISIBLE);
			v.setTag(position);
			container.addView(v, 0);
			return v;
		}
	}

	class HAdapter extends PagerAdapter {

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}

		@Override
		public int getCount() {
			if (mAdapter != null && getHcount() > 0) {
				return Integer.MAX_VALUE;
			}

			return 0;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
			mCache.add(new SoftReference<View>((View) object));
		}

		@Override
		public float getPageWidth(int position) {
			return 1f / hsize;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View v = mAdapter.getView(getPosition(position, vIndex), getConvertView(), container);

			v.setVisibility(!showHValue && position == hPager.getCurrentItem() ? View.INVISIBLE
					: View.VISIBLE);
			v.setTag(position);
			container.addView(v, 0);
			return v;
		}
	}

}
