package ipanel.join.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.View;
import android.content.Context;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.ViewGroup;

/**
 * Cell weight based grid layout
 * 
 * @author Zexu
 * 
 */
public class TileLayout extends ViewGroup implements IConfigViewGroup {
	
	public static final String TAG = TileLayout.class.getSimpleName();

	private class ItemInfo {
		int position;
		android.view.View view;

		public ItemInfo(int position, android.view.View v) {
			this.position = position;
			this.view = v;
		}
	}

	public static final String PROP_Y_SPACE = "ySpace";

	public static final String PROP_X_SPACE = "xSpace";

	public static final String PROP_Y_SIZE = "ySize";

	public static final String PROP_X_SIZE = "xSize";

	private View mData;

	/**
	 * Total cell count horizontally
	 */
	private int xSize = 4;

	/**
	 * Total cell count vertically
	 */
	private int ySize = 4;

	/**
	 * Horizontal space between cells
	 */
	private int xSpace = 10;

	/**
	 * Vertical space between cells
	 */
	private int ySpace = 10;
	
	/**
	 *
	 */
	private int hWidth = 0;
	
	private boolean layoutRequired = true;
	

	public TileLayout(Context context, View data) {
		super(context);
		this.mData = data;
		PropertyUtils.setCommonProperties(this, data);

		Bind bind = data.getBindByName(PROP_X_SIZE);
		if (bind != null) {
			xSize = Integer.parseInt(bind.getValue().getvalue());
		}

		bind = data.getBindByName(PROP_Y_SIZE);
		if (bind != null) {
			ySize = Integer.parseInt(bind.getValue().getvalue());
		}

		bind = data.getBindByName(PROP_X_SPACE);
		if (bind != null) {
			xSpace = PropertyUtils.getScaledSize(Integer.parseInt(bind.getValue().getvalue()));
		}

		bind = data.getBindByName(PROP_Y_SPACE);
		if (bind != null) {
			ySpace = PropertyUtils.getScaledSize(Integer.parseInt(bind.getValue().getvalue()));
		}
	}
	
	public void setLayoutSize(int xSize,int ySize){
		this.xSize=xSize;
		this.ySize=ySize;
		requestLayout();
	}

	private List<ItemInfo> mItemInfos = new ArrayList<ItemInfo>();

	@Override
	public void removeAllViews() {
		layoutRequired = true;
		super.removeAllViews();
		mItemInfos.clear();
	}

	@Override
	public void addView(android.view.View child, int index, LayoutParams params) {
		if (index >= 0 && index < getChildCount()) {
			Iterator<ItemInfo> it = mItemInfos.iterator();
			while (it.hasNext()) {
				ItemInfo itemInfo = it.next();
				if (itemInfo.position >= index)
					itemInfo.position++;
			}
		}
		mItemInfos.add(new ItemInfo(index < 0 ? getChildCount() : index, child));
		layoutRequired = true;
		super.addView(child, index, params);
	}

	@Override
	public void removeViewAt(int index) {
		layoutRequired = true;
		super.removeViewAt(index);
		Iterator<ItemInfo> it = mItemInfos.iterator();
		while (it.hasNext()) {
			ItemInfo itemInfo = it.next();
			if (itemInfo.position == index)
				it.remove();
			else if (itemInfo.position > index)
				itemInfo.position--;
		}
	}

	@Override
	public void removeViews(int start, int count) {
		layoutRequired = true;
		Iterator<ItemInfo> it = mItemInfos.iterator();
		while (it.hasNext()) {
			ItemInfo itemInfo = it.next();
			if (itemInfo.position >= start && itemInfo.position < start + count) {
				it.remove();
				super.removeView(itemInfo.view);
			} else if (itemInfo.position >= start + count)
				itemInfo.position -= count;
		}
	}

	@Override
	public void removeView(android.view.View view) {
		int index = indexOfChild(view);
		if (index != -1)
			removeViewAt(index);
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

	@Override
	public LayoutParams genConfLayoutParams(View data) {
		Bind bd = data.getBindByName(PROPERTY_LAYOUT_PARAMS);
		if (bd != null) {
			try {
				JSONObject jobj = bd.getValue().getJsonValue();
				LayoutParams lp = new LayoutParams(jobj.optInt("width",
						LayoutParams.WRAP_CONTENT), jobj.optInt(
						"height", LayoutParams.WRAP_CONTENT));
				return lp;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return generateDefaultLayoutParams();
	}

	public android.view.View getChildByOriginPosition(int position) {
		for (ItemInfo info : mItemInfos) {
			if (info.position == position)
				return info.view;
		}
		android.view.View v = getChildAt(position);
		return v;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(TAG, "onMeasure");
		if (xSize <= 0 || ySize <= 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
        
		int dw=0;
		if (hWidth!=0) {
			dw = Math.max(hWidth, 0);
		}else{
			dw = Math.max(getLayoutParams().width, 0);
		}
//		int dw = Math.max(getLayoutParams().width, 0);
		int dh = Math.max(getLayoutParams().height, 0);
		setMeasuredDimension(getDefaultSize(dw, widthMeasureSpec),
				getDefaultSize(dh, heightMeasureSpec));

		final int measuredWidth = getMeasuredWidth();
		final int measuredHeight = getMeasuredHeight();

		Log.d(TAG, String.format("Width %d, Height %d, xSzie %d, ySize %d", measuredWidth,
				measuredHeight, xSize, ySize));

		int cellWidth = (measuredWidth - getPaddingLeft() - getPaddingRight() - (xSize - 1)
				* xSpace)
				/ xSize;
		int cellHeight = (measuredHeight - getPaddingTop() - getPaddingBottom() - (ySize - 1)
				* ySpace)
				/ ySize;
		Log.d(TAG, String.format("cellWidth %d, cellHeight %d", cellWidth, cellHeight));

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			android.view.View child = getChildByOriginPosition(i);
			int childX = 1;
			int childY = 1;
			if (child.getLayoutParams() != null) {
				if (child.getLayoutParams().width >= 0)
					childX = child.getLayoutParams().width;
				if (child.getLayoutParams().height >= 0)
					childY = child.getLayoutParams().height;
			}

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
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d(TAG, "onLayout changed = " + changed);
		if (xSize <= 0 || ySize <= 0) {
			return;
		}
		
		if(!changed && !layoutRequired)
			return;

		int width = r - l;
		int height = b - t;

		int count = getChildCount();

		int pLeft = getPaddingLeft();
		int pTop = getPaddingTop();

		int cellWidth = (width - pLeft - getPaddingRight() - (xSize - 1) * xSpace) / xSize;
		int cellHeight = (height - pTop - getPaddingBottom() - (ySize - 1) * ySpace) / ySize;

		// x * y matrix for greedy layout
		int[][] grid = new int[ySize][xSize];
		for (int i = 0; i < count; i++) {
			android.view.View child = getChildByOriginPosition(i);
			int childX = 1;
			int childY = 1;
			if (child.getLayoutParams() != null) {
				if (child.getLayoutParams().width >= 0)
					childX = child.getLayoutParams().width;
				if (child.getLayoutParams().height >= 0)
					childY = child.getLayoutParams().height;
			}
			int childW = child.getMeasuredWidth();
			int childH = child.getMeasuredHeight();

			int[] xy = findSpot(grid, childX, childY);
			if (xy != null) {
				int left = pLeft + xy[0] * (cellWidth + xSpace);
				int right = left + childW;
				int top = pTop + xy[1] * (cellHeight + ySpace);
				int bottom = top + childH;
				child.layout(left, top, right, bottom);
				Log.d(TAG, String.format("child %d at %d, %d; l %d, r %d, t %d, b %d", i, xy[0],
						xy[1], left, right, top, bottom));
			} else {
				Log.d(TAG, "can't find empty spot for child " + i);
			}
		}
		
		layoutRequired = false;
	}

	private int[] findSpot(int[][] grid, int xSize, int ySize) {
		for (int r = 0; r <= grid.length - ySize; r++) {
			for (int c = 0; c <= grid[r].length - xSize; c++) {
				if (spaceAvailable(grid, r, c, xSize, ySize)) {
					markSpace(grid, r, c, xSize, ySize);
					return new int[] { c, r };
				}
			}
		}

		return null;
	}

	private void markSpace(int[][] grid, int r, int c, int xSize, int ySize) {
		for (int i = r; i < r + ySize; i++) {
			for (int j = c; j < c + xSize; j++) {
				grid[i][j] = 1;
			}
		}
	}

	private boolean spaceAvailable(int[][] grid, int r, int c, int xSize, int ySize) {
		if (r + ySize > grid.length || c + xSize > grid[0].length) {
			return false;
		}
		int total = 0;
		for (int i = r; i < r + ySize; i++) {
			for (int j = c; j < c + xSize; j++) {
				total += grid[i][j];
			}
		}

		return total == 0;
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

	public boolean arrowScroll(int direction) {
		android.view.View currentFocused = findFocus();
		if (currentFocused == this)
			currentFocused = null;

		boolean handled = false;

		android.view.View nextFocused = FocusFinder.getInstance().findNextFocus(this,
				currentFocused, direction);
		if (nextFocused != null && nextFocused != currentFocused) {
			handled = nextFocused.requestFocus();
		}
		if (handled) {
			playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
		}
		return handled;
	}

	public void debugPrint(){
		for(ItemInfo info : mItemInfos){
			Log.d(TAG, info.position+" "+info.view);
		}
	}
	
//	public void setLayoutSize(int xSize,int ySize){
//		this.xSize=xSize;
//		this.ySize=ySize;
//	}
	
	public void sethWidth(int width){
		this.hWidth=width;
	}
}
