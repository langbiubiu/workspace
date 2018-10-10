package com.ipanel.join.cq.vod.customview;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

public class SimpleAdapterView extends ViewGroup {
	static final String TAG = SimpleAdapterView.class.getSimpleName();

	public static int HORIZONTAL = 0;
	public static int VERTICAL = 1;
	public boolean center_focus = false;

	public interface OnUnhandledFocusDirectionListener {
		/**
		 * 
		 * @param direction
		 *            focus direction
		 * @return true to consume this event, return false to ignore
		 */
		public boolean onUnhandledDirection(int direction);
	}

	private OnUnhandledFocusDirectionListener mFocusDirectionListener;

	public void setOnUnhandledFocusDirectionListener(OnUnhandledFocusDirectionListener l) {
		this.mFocusDirectionListener = l;
	}

	int orientation = HORIZONTAL;

	int itemSpace = 0;
	float fillFactor = 1.5f;

	public void setItemSpace(int space) {
		this.itemSpace = space;
		resetItems();
	}

	class ItemInfo {
		public ItemInfo(View v, int pos) {
			this.view = v;
			this.position = pos;
		}

		View view;
		int position;
	}

	List<ItemInfo> items = new ArrayList<ItemInfo>();

	SparseArray<List<SoftReference<View>>> viewCache = new SparseArray<List<SoftReference<View>>>();

	Adapter mAdapter;

	public SimpleAdapterView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SimpleAdapterView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SimpleAdapterView(Context context) {
		super(context);
	}

	@Override
	public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {

		if((orientation == HORIZONTAL && (direction == FOCUS_LEFT || direction == FOCUS_RIGHT))||
				(orientation == VERTICAL && (direction == FOCUS_DOWN || direction == FOCUS_UP))){
			super.addFocusables(views, direction, focusableMode);
		} else {
			float scroll = orientation == HORIZONTAL ? getScrollX() : getScaleY();
			float span = (orientation == HORIZONTAL ? getWidth() : getHeight());
			ArrayList<View> items = new ArrayList<View>();
			super.addFocusables(items, direction, focusableMode);
			Iterator<View> it = items.iterator();
			while(it.hasNext()){
				View v = it.next();
				if(v == this)
					continue;
				View view = getDirectChild(v);
				if(orientation == HORIZONTAL){
					if(view.getLeft()<scroll || view.getRight()>scroll+span)
						it.remove();
				} else {
					if(view.getTop()<scroll || view.getBottom()>scroll+span)
						it.remove();
				}
			}
			views.addAll(items);
		}
	}

	public int getFirstPosition() {
		if (items.size() > 0)
			return items.get(0).position;
		return -1;
	}
	
	public View getItemView(int pos){
		return items.get(pos).view;
	}

	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			refreshItems();
		}

	};

	public void setAdapter(Adapter adapter) {
		if (mAdapter != null)
			mAdapter.unregisterDataSetObserver(mDataObserver);

		this.mAdapter = adapter;
		if (mAdapter != null)
			mAdapter.registerDataSetObserver(mDataObserver);
		viewCache.clear();
		resetItems();
	}

	public int focusPosToResume = -1;

	protected void refreshItems() {
		if (items.size() > 0) {
			ItemInfo first = items.get(0);
			if (mAdapter.getCount() < first.position) {
				resetItems();
				return;
			}

			for (ItemInfo ii : items) {
				if (ii.view.hasFocus()) {
					focusPosToResume = ii.position;
				}
				cacheView(ii);
			}
			removeAllViewsInLayout();

			// User previous first item as anchor to update items;
			int lastPos = orientation == HORIZONTAL ? first.view.getRight() : first.view
					.getBottom();
			if (first.position > 0)
				lastPos += itemSpace;
			first.view = makeAndAddView(true, first.position, lastPos);
			items.clear();
			items.add(first);
			requestLayout();
		} else {
			refreshItems();
		}

	}

	public int getLastPosition() {
		if (items.size() > 0)
			return items.get(items.size() - 1).position;
		return -1;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d(TAG, "onLayout width =" + getWidth() + ", height = " + getHeight());
		if (mAdapter != null && mAdapter.getCount() > 0 && getWidth() > 0 && getHeight() > 0) {
			removeOffScreenItems();
			fillViews();
		}
		invalidate();
	}

	private void removeOffScreenItems() {
		if (orientation == HORIZONTAL) {
			float scrollX = getScrollX();
			float min = scrollX - getWidth() * (fillFactor - 1);
			float max = scrollX + getWidth() * fillFactor;
			Iterator<ItemInfo> it = items.iterator();
			while (it.hasNext()) {
				ItemInfo ii = it.next();
				if (ii.view.getLeft() > max || ii.view.getRight() < min) {
					removeViewInLayout(ii.view);
					cacheView(ii);
					it.remove();
					Log.d(TAG, "remove view position = " + ii.position);
				}
			}
		} else {
			float scrollY = getScrollY();
			float min = scrollY - getHeight() * (fillFactor - 1);
			float max = scrollY + getHeight() * fillFactor;
			Iterator<ItemInfo> it = items.iterator();
			while (it.hasNext()) {
				ItemInfo ii = it.next();
				if (ii.view.getBottom() > max || ii.view.getTop() < min) {
					removeViewInLayout(ii.view);
					cacheView(ii);
					it.remove();
					Log.d(TAG, "remove view position = " + ii.position);
				}
			}
		}
	}

	private void fillViews() {
		int first = getFirstPosition();
		int firstPosPX = initialScrollX;
		int lastPosPX = initialScrollX;
		float scroll = orientation == HORIZONTAL ? getScrollX() : getScaleY();
		float span = (orientation == HORIZONTAL ? getWidth() : getHeight());
		float min = scroll - (fillFactor - 1) * span;
		float max = scroll + fillFactor * span;
		int last = getLastPosition();
		if (first >= 0) {
			if (orientation == HORIZONTAL) {
				firstPosPX = items.get(0).view.getLeft();
				lastPosPX = items.get(items.size() - 1).view.getRight();
			} else {
				firstPosPX = items.get(0).view.getTop();
				lastPosPX = items.get(items.size() - 1).view.getBottom();
			}
		} else if (initialPosition > 0) {
			first = initialPosition;
			last = initialPosition - 1;
		}

		while (last < mAdapter.getCount() - 1 && lastPosPX < max) {
			last++;
			View v = makeAndAddView(false, last, lastPosPX);
			items.add(new ItemInfo(v, last));
			lastPosPX = orientation == HORIZONTAL ? v.getRight() : v.getBottom();
		}
		
		while (first > 0 && firstPosPX > min) {
			first--;
			View v = makeAndAddView(true, first, firstPosPX);
			items.add(0, new ItemInfo(v, first));
			firstPosPX = orientation == HORIZONTAL ? v.getLeft() : v.getTop();
		}
	}

	int begin_position = -1;
	
	private View makeAndAddView(boolean isBegin, int position, int lastPos) {
		View v = mAdapter.getView(position, getConvertView(position), this);
		LayoutParams lp = v.getLayoutParams();
		if (lp == null)
			lp = generateDefaultLayoutParams();

		addViewInLayout(v, -1, lp, true);
		v.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
		int w = v.getMeasuredWidth();
		int h = v.getMeasuredHeight();
		int space = position == initialPosition ? 0 : itemSpace;
		Log.d(TAG,
				position + " measure width = " + v.getMeasuredWidth() + ", height="
						+ v.getMeasuredHeight());
		if(isBegin){
			begin_position = position;
		}
		if (isBegin) {
			if (orientation == HORIZONTAL) {
				v.layout(lastPos - space - w, 0, lastPos - space, h);
			} else {
				v.layout(0, lastPos - space - h, w, lastPos - space);
			}
		} else {
			if (orientation == HORIZONTAL) {
				v.layout(lastPos + space, 0, lastPos + space + w, h);
			} else {
				v.layout(0, lastPos + space, w, lastPos + space + h);
			}
		}
		Log.d(TAG, "focusPosToResume = " + focusPosToResume + ", position = " + position
				+ ", initialPosition=" + initialPosition + ", isFocus=" + isFocused());
		
		Log.d(TAG, "center_focus:"+center_focus);
		
		if(!center_focus){
			if (focusPosToResume != -1 && position == focusPosToResume) {
				v.requestFocus();
			} else if(isFocused() && position == initialPosition){
				v.requestFocus();
			}
		}else{
//			int left = v.getLeft();
//			Log.d(TAG, "left:"+left+" min:"+(v.getWidth() * 3+ 3 * itemSpace)+" max:"+(v.getWidth() * 4+ 3 * itemSpace));
//			if(v.getLeft() >=  (v.getWidth() * 3+ 3 * itemSpace) && 
//					v.getLeft() <=  (v.getWidth() * 4+ 3 * itemSpace)){
//				v.requestFocus();
//				Log.i(TAG,"has focus v:");
//			}
			
			if(3 == (position - getFirstPosition())){
				v.requestFocus();
			}
		}
		
		Log.d(TAG, "v left=" + v.getLeft() + ", right=" + v.getRight() + ", top=" + v.getTop()
				+ ", bottom=" + v.getBottom());
		return v;
	}

	private void cacheView(ItemInfo ii) {
		int type = mAdapter.getItemViewType(ii.position);
		List<SoftReference<View>> cache = viewCache.get(type);
		if (cache == null) {
			cache = new ArrayList<SoftReference<View>>();
			viewCache.put(type, cache);
		}
		cache.add(new SoftReference<View>(ii.view));

	}

	private View getConvertView(int position) {
		int type = mAdapter.getItemViewType(position);
		List<SoftReference<View>> cache = viewCache.get(type);
		if (cache != null) {
			Iterator<SoftReference<View>> it = cache.iterator();
			while (it.hasNext()) {
				SoftReference<View> sr = it.next();
				it.remove();
				View view = sr.get();
				if (view != null) {
					forceAllViewsLayout(view);
					return view;
				}
			}
		}
		return null;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean handled = false;
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				handled = arrowScroll(FOCUS_LEFT);
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				handled = arrowScroll(FOCUS_RIGHT);
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				handled = arrowScroll(FOCUS_UP);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				handled = arrowScroll(FOCUS_DOWN);
				break;
			}
		}
		if (handled)
			return true;
		return super.dispatchKeyEvent(event);
	}

	static void forceAllViewsLayout(View v) {
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				forceAllViewsLayout(vg.getChildAt(i));
			}
		}
		v.forceLayout();
	}

	public View getDirectChild(View v) {
		if (v.getParent() == null)
			return null;
		if (v.getParent() == this)
			return v;
		return getDirectChild((View) v.getParent());
	}

	public boolean arrowScroll(int direction) {
		View currentFocused = findFocus();
		if (currentFocused == this)
			currentFocused = null;

		boolean handled = false;

		View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);
		if (nextFocused != null && nextFocused != currentFocused) {
			handled = nextFocused.requestFocus();
			if (handled) {
				if(mListener!=null){
					mListener.beforeScroll();
				}
				View directChild = getDirectChild(nextFocused);
				forceAllViewsLayout(directChild);
				if (orientation == HORIZONTAL) {
					int scroll = getScrollX();
					int max = scroll + getWidth();
					Log.d(TAG,
							"scroll = " + scroll + ", max = " + max + ", v.left="
									+ directChild.getLeft() + ", v.right=" + directChild.getRight());
					if (directChild.getLeft() < scroll) {
						scrollTo(directChild.getLeft(), getScrollY());
						requestLayout();
					}
					if (directChild.getRight() > max) {
						scrollTo(directChild.getRight() - getWidth(), getScrollY());
						requestLayout();
					}
				} else {
					int scroll = getScrollY();
					int max = scroll + getHeight();
					Log.d(TAG,
							"scroll = " + scroll + ", max = " + max + ", v.top="
									+ directChild.getTop() + ", v.bottom="
									+ directChild.getBottom());
					if (nextFocused.getTop() < scroll) {
						scrollTo(getScrollX(), directChild.getTop());
						requestLayout();
					}
					if (directChild.getBottom() > max) {
						scrollTo(getScrollX(), directChild.getBottom() - getHeight());
						requestLayout();
					}
				}
				if(mListener!=null){
//					postDelayed(action, delayMillis)
					mListener.afterScroll();
				}
			}
		}
		// if (handled) {
		// playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
		// }
		if (!handled && mFocusDirectionListener != null)
			handled = mFocusDirectionListener.onUnhandledDirection(direction);
		return handled;
	}

	protected int initialScrollX = 0;
	protected int initialPosition = 0;

	public void setInitPos(int initialScrollX, int initialPosition) {
		this.initialScrollX = initialScrollX;
		this.initialPosition = initialPosition;
	}

	
	
	public void resetItems() {
		items.clear();
		removeAllViewsInLayout();
		scrollTo(initialScrollX, 0);
		requestLayout();
	}
	
	ScrollStateListener mListener;
	
	public void setScrollStateListener(ScrollStateListener l){
		mListener = l;
	}
	
	public interface ScrollStateListener{
		public void beforeScroll();
		public void afterScroll();
	}

}
