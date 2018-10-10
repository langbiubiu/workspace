package cn.ipanel.android.widget;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class WeightGridLayout extends ViewGroup {
	private static final String TAG = "WeightGridAdapter";
	private static final boolean DEBUG = false;
	
	public interface OnUnhandledFocusDirectionListener {
		/**
		 * 
		 * @param direction
		 *            focus direction
		 * @return true to consume this event, return false to ignore
		 */
		public boolean onUnhandledDirection(int direction);
	}
	
	public interface OnKeyDispatchListener {
		public boolean onDispatchKey(KeyEvent event);
	}

	private class ItemInfo {
		int position;
		View view;
		int[] xy;

		public ItemInfo(int position, View v) {
			this.position = position;
			this.view = v;
		}
	}

	public static abstract class WeightGridAdapter extends BaseAdapter {

		public abstract int getChildXSize(int position);

		public abstract int getChildYSize(int position);

		public abstract int getXSize();

		public abstract int getYSize();

		public int getXSpace() {
			return 10;
		}

		public int getYSpace() {
			return 10;
		}
		
		int[] edgeItemPadding = new int[]{0,0,0,0};
		
		/**
		 * Extra padding for edge item, in the order left, right, top, bottom
		 * 
		 * @return
		 */
		public int[] getEdgeItemPadding() {
			return edgeItemPadding;
		}
		
		public boolean autoSetEdgeItemPadding() {
			return true;
		}
	}

	private WeightGridAdapter mAdapter;

	private List<ItemInfo> mItemInfos = new ArrayList<WeightGridLayout.ItemInfo>();
	
	private OnUnhandledFocusDirectionListener mFocusDirectionListener;
	
	public void setOnUnhandledFocusDirectionListener(OnUnhandledFocusDirectionListener l){
		this.mFocusDirectionListener = l;
	}
	
	public OnUnhandledFocusDirectionListener getOnUnhandledFocusDirectionListener(){
		if(mFocusDirectionListener != null)
			return mFocusDirectionListener;
		if(mAdapter instanceof OnUnhandledFocusDirectionListener)
			return (OnUnhandledFocusDirectionListener) mAdapter;
		return null;
	}
	
	private OnKeyDispatchListener mKeyDispatchListener;
	
	public void setOnKeyDispatchListener(OnKeyDispatchListener l){
		this.mKeyDispatchListener = l;
	}
	
	public OnKeyDispatchListener getOnKeyDispatchListener(){
		if(mKeyDispatchListener != null)
			return mKeyDispatchListener;
		if(mAdapter instanceof OnKeyDispatchListener)
			return (OnKeyDispatchListener) mAdapter;
		return null;
	}
	
	private boolean layoutRequired = true;

	public WeightGridLayout(Context context) {
		this(context, null);
	}

	public WeightGridLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WeightGridLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setChildrenDrawingOrderEnabled(true);
	}

	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			populateItems();
			invalidate();
			
		}

	};
	
	public WeightGridAdapter getAdapter(){
		return mAdapter;
	}

	public void setAdapter(WeightGridAdapter adapter) {
		if (mAdapter != null)
			mAdapter.unregisterDataSetObserver(mDataObserver);
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataObserver);
		mItemInfos.clear();
		if (adapter != null) {
			populateItems();
		} else {
			removeAllViews();
		}
	}
	
	private int mPopulateFocusOffset = 0;
	
	public void setPopulateFocusOffset(int offset){
		this.mPopulateFocusOffset = offset;
	}

	private void populateItems() {
		int focusPos = -1;
		int focusId = View.NO_ID;
		for(ItemInfo ii : mItemInfos){
			if((ii.view.hasFocus())){
				focusPos = ii.position;
				if(ii.view instanceof ViewGroup){
					View focus = ((ViewGroup)ii.view).findFocus();
					if(focus != null && focus != ii.view)
						focusId = focus.getId();
				}
				break;
			}
		}
		if (mPopulateFocusOffset != 0) {
			if (focusPos == -1) {
				focusPos = mPopulateFocusOffset;
			} else {
				focusPos += mPopulateFocusOffset;
			}
			mPopulateFocusOffset = 0;
		}
		this.removeAllViewsInLayout();
		List<ItemInfo> prevInfos = mItemInfos;
		mItemInfos = new ArrayList<WeightGridLayout.ItemInfo>();
		int count = mAdapter.getCount();
		if(focusPos != -1 && focusPos >= count)
			focusPos = count -1;
		for (int i = 0; i < count; i++) {
			View convertView = null;
			if (prevInfos != null && prevInfos.size() > i)
				convertView = prevInfos.get(i).view;
			View v = mAdapter.getView(i, convertView, this);
			if (v != null) {
				LayoutParams lp = v.getLayoutParams();
				if (lp == null)
					lp = generateDefaultLayoutParams();
				addViewInLayout(v, -1, lp, true);
				
				mItemInfos.add(new ItemInfo(i, v));
				if(focusPos == i){
					View focus;
					if(focusId != View.NO_ID && (focus = v.findViewById(focusId))!=null)
						focus.requestFocus();
					else
						v.requestFocus();
				}
			}
		}
		layoutRequired = true;
		forceAllViewsLayout(this);
		requestLayout();
	}
	
	static void forceAllViewsLayout(View v){
		if(v instanceof ViewGroup){
			ViewGroup vg = (ViewGroup) v;
			for(int i=0; i<vg.getChildCount();i++){
				forceAllViewsLayout(vg.getChildAt(i));
			}
		}
		v.forceLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(DEBUG)
			Log.d(TAG, "onMeasure");
		if (mAdapter == null || mAdapter.getXSize() <= 0 || mAdapter.getYSize() <= 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		int dw = Math.max(getLayoutParams().width, 0);
		int dh = Math.max(getLayoutParams().height, 0);
		setMeasuredDimension(getDefaultSize(dw, widthMeasureSpec),
				getDefaultSize(dh, heightMeasureSpec));

		final int measuredWidth = getMeasuredWidth();
		final int measuredHeight = getMeasuredHeight();

		int xSize = mAdapter.getXSize();
		int xSpace = mAdapter.getXSpace();
		int ySize = mAdapter.getYSize();
		int ySpace = mAdapter.getYSpace();

		if(DEBUG)
			Log.d(TAG, String.format("Width %d, Height %d, xSzie %d, ySize %d", measuredWidth,
				measuredHeight, xSize, ySize));
		int[] edgePadding = mAdapter.getEdgeItemPadding();
		int cellWidth = (measuredWidth - edgePadding[0] - edgePadding[1] - getPaddingLeft()
				- getPaddingRight() - (xSize - 1) * xSpace)
				/ xSize;
		int cellHeight = (measuredHeight - edgePadding[2] - edgePadding[3] - getPaddingTop()
				- getPaddingBottom() - (ySize - 1) * ySpace)
				/ ySize;
		if(DEBUG)
			Log.d(TAG, String.format("cellWidth %d, cellHeight %d", cellWidth, cellHeight));

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int childX = mAdapter.getChildXSize(i);
			int childY = mAdapter.getChildYSize(i);

			child.measure(MeasureSpec.makeMeasureSpec(getSizeWithSpace(childX, cellWidth, xSpace),
					MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
					getSizeWithSpace(childY, cellHeight, ySpace), MeasureSpec.EXACTLY));
		}
	}

	private int getSizeWithSpace(int sizeCount, int sizeUnit, int space) {
		int sz = sizeCount * sizeUnit;
		if (sizeCount > 1)
			sz += (sizeCount - 1) * space;
		return sz;
	}

	@Override
	public void bringChildToFront(View child) {
		// TODO Auto-generated method stub
		super.bringChildToFront(child);
	}

	BitSet grid;
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if(DEBUG)
			Log.d(TAG, "onLayout changed="+changed);
		if (mAdapter == null || mAdapter.getXSize() <= 0 || mAdapter.getYSize() <= 0) {
			return;
		}
		
		int width = r - l;
		int height = b - t;
		int xSize = mAdapter.getXSize();
		int xSpace = mAdapter.getXSpace();
		int ySize = mAdapter.getYSize();
		int ySpace = mAdapter.getYSpace();

		int count = getChildCount();

		int pLeft = getPaddingLeft();
		int pTop = getPaddingTop();

		int[] edgePadding = mAdapter.getEdgeItemPadding();
		int cellWidth = (width - pLeft- edgePadding[0] - edgePadding[1] - getPaddingRight() - (xSize - 1) * xSpace) / xSize;
		int cellHeight = (height - pTop  - edgePadding[2] - edgePadding[3] - getPaddingBottom() - (ySize - 1) * ySpace) / ySize;

		// x * y matrix for greedy layout
		//byte[][] grid = new byte[ySize][xSize];
		if(layoutRequired){
			grid = new BitSet(xSize*ySize);
		}
		
		for (int i = 0; i < count; i++) {
			View child = getChildByOriginPosition(i);
			int childX = mAdapter.getChildXSize(i);
			int childY = mAdapter.getChildYSize(i);
			int childW = child.getMeasuredWidth();
			int childH = child.getMeasuredHeight();

			ItemInfo ii = mItemInfos.get(i);
			int[] xy = ii.xy;
			if(xy == null && grid != null){
				xy = findSpot(grid, xSize, ySize, childX, childY);
				ii.xy = xy;
			}
			if (xy != null) {
				int left = pLeft + xy[0] * (cellWidth + xSpace);
				if(xy[0] > 0 )
					left += edgePadding[0];
				int right = left + childW;
				int top = pTop + xy[1] * (cellHeight + ySpace);
				if(xy[1] > 0)
					top += edgePadding[2];
				int bottom = top + childH;
				
				//edge items
				int edgeW = 0, edgeH = 0;
				int cpl = child.getPaddingLeft();
				int cpr = child.getPaddingRight();
				int cpt = child.getPaddingTop();
				int cpb = child.getPaddingBottom();
				if(xy[0] == 0){
					edgeW += edgePadding[0];
					cpl = edgePadding[0];
				}
				if(xy[0] == xSize - 1){
					edgeW += edgePadding[1];
					cpr = edgePadding[1];
				}
				if(xy[1] == 0) {
					edgeH += edgePadding[2];
					cpt = edgePadding[2];
				}
				if( xy[1] == ySize - 1){
					edgeH += edgePadding[3];
					cpb = edgePadding[3];
				}
				if (edgeW > 0 || edgeH > 0) {
					right += edgeW;
					bottom += edgeH;
					if(mAdapter.autoSetEdgeItemPadding())
						child.setPadding(cpl, cpt, cpr, cpb);
					child.measure(MeasureSpec.makeMeasureSpec(childW + edgeW, MeasureSpec.EXACTLY),
							MeasureSpec.makeMeasureSpec(childH + edgeH, MeasureSpec.EXACTLY));
				}
				child.layout(left, top, right, bottom);
				if(DEBUG)
					Log.d(TAG, String.format("child %d at %d, %d; l %d, r %d, t %d, b %d, pl %d, pr %d, pt %d, pb %d", i, xy[0],
						xy[1], left, right, top, bottom, cpl, cpr, cpt, cpb));
			} else {
				Log.d(TAG, "can't find empty spot for child " + i);
			}
		}
		
		layoutRequired = false;
	}

	public View getChildByOriginPosition(int position) {
		if(position < mItemInfos.size()){
			return mItemInfos.get(position).view;
		}
		View v = getChildAt(position);
		return v;
	}
	
	public int getOriginPositionFor(View child){
		for (ItemInfo info : mItemInfos) {
			if (info.view == child)
				return info.position;
		}
		return -1;
	}

	private int[] findSpot(BitSet grid, int gridX, int gridY, int xSize, int ySize) {
		for (int r = 0; r <= gridY - ySize; r++) {
			for (int c = 0; c <= gridX - xSize; c++) {
			//	Log.v(TAG, "r="+r+",c="+c+",xSize="+xSize+",ySize="+ySize);
				if (spaceAvailable(grid, gridX, gridY, r, c, xSize, ySize)) {
					markSpace(grid, gridX, gridY, r, c, xSize, ySize);
					return new int[] { c, r };
				}
			}
		}

		return null;
	}

	private void markSpace(BitSet grid, int gridX, int gridY, int r, int c, int xSize, int ySize) {
		for (int i = r; i < r + ySize; i++) {
			for (int j = c; j < c + xSize; j++) {
				grid.set(i*gridX+j);
			}
		}
	}

	private boolean spaceAvailable(BitSet grid, int gridX, int gridY, int r, int c, int xSize, int ySize) {
		if (r + ySize > gridY || c + xSize > gridX) {
			return false;
		}
		int total = 0;
		for (int i = r; i < r + ySize; i++) {
			for (int j = c; j < c + xSize; j++) {
				total += grid.get(i*gridX+j)?1:0;
			}
		}

		return total == 0;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean handled = false;
		OnKeyDispatchListener kdl = getOnKeyDispatchListener();
		if (kdl != null && kdl.onDispatchKey(event)) {
			return true;
		}
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

	public boolean arrowScroll(int direction) {
		View currentFocused = findFocus();
		if (currentFocused == this)
			currentFocused = null;

		boolean handled = false;

		View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);
		if (nextFocused != null && nextFocused != currentFocused) {
			handled = nextFocused.requestFocus();
		}
		if (handled) {
			playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
		}
		if (!handled && getOnUnhandledFocusDirectionListener() != null)
			handled = getOnUnhandledFocusDirectionListener().onUnhandledDirection(direction);
		return handled;
	}
	
}
