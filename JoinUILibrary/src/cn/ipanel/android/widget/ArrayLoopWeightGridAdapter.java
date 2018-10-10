package cn.ipanel.android.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import cn.ipanel.android.util.PageLoadingHelper;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.OnKeyDispatchListener;
import cn.ipanel.android.widget.WeightGridLayout.OnUnhandledFocusDirectionListener;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

public abstract class ArrayLoopWeightGridAdapter<T> extends WeightGridAdapter implements
		OnUnhandledFocusDirectionListener, OnKeyDispatchListener {
	static final String TAG = ArrayLoopWeightGridAdapter.class.getSimpleName();
	
	public static int ORIENTATION_HORIZONTAL = 0;
	public static int ORIENTATION_VERTICAL = 1;

	protected List<T> items = new ArrayList<T>();

	protected int displaySize;
	protected int offset;

	protected int orientation = ORIENTATION_HORIZONTAL;

	protected int lineSize = 1;

	protected boolean loop = true;
	
	protected PageLoadingHelper<T> pagingLoader;

	public ArrayLoopWeightGridAdapter(int displaySize, WeightGridLayout layout) {
		this.displaySize = displaySize;
		//layout.setOnUnhandledFocusDirectionListener(this);
		//layout.setOnKeyDispatchListener(this);
	}
	
	public ArrayLoopWeightGridAdapter(int displaySize) {
		this.displaySize = displaySize;
	}
	
	public void setLineSize(int lineSize){
		this.lineSize = lineSize;
		notifyDataSetChanged();
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
		notifyDataSetChanged();
	}

	public void setEnableLoop(boolean loop) {
		this.loop = loop;
		notifyDataSetChanged();
	}

	public void setDisplaySize(int size) {
		this.displaySize = size;
		notifyDataSetChanged();
	}
	
	public int getDisplaySize(){
		return displaySize;
	}
	
	public int getLineSize(){
		return lineSize;
	}
	
	public int getOrientation() {
		return orientation;
	}
	
	public void setPageLoadingHelper(PageLoadingHelper<T> helper){
		this.pagingLoader = helper;
		helper.requestPage(0, 2);
	}

	@Override
	public int getCount() {
		if (items == null || items.size() < displaySize)
			return items.size();
		if (loop)
			return displaySize;
		else
			return Math.min(items.size() - offset, displaySize);
	}

	@Override
	public T getItem(int position) {
		if (items == null || items.size() == 0)
			return null;
		return items.get((position + offset) % items.size());
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setItems(Collection<T> items) {
		offset = 0;
		this.items = new ArrayList<T>(items);
		notifyDataSetChanged();
	}

	public void add(T... item) {
		if (item != null)
			addAll(Arrays.asList(item));
	}

	public void addAll(Collection<T> items) {
		this.items.addAll(items);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = createView(parent, position);
		}
		bindData(convertView, position);
		if(pagingLoader != null){
			pagingLoader.requestPage(items.size()/displaySize, 2);
		}
		return convertView;
	}

	public abstract void bindData(View convertView, int position);

	public abstract View createView(ViewGroup parent, int position);

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
		return orientation == ORIENTATION_HORIZONTAL ? displaySize / lineSize : lineSize;
	}

	@Override
	public int getYSize() {
		return orientation == ORIENTATION_VERTICAL ? displaySize / lineSize : lineSize;
	}

	public void setOffset(int offset) {
		this.offset = offset;
		notifyDataSetChanged();
	}

	public boolean changeOffset(int delta) {
		Log.d(TAG,
				"changeOffset delta = " + delta + ", items size ="
						+ (items != null ? items.size() : 0) + ", displaySize = " + displaySize
						+ ", offset=" + offset);
		if (items == null || items.size() < displaySize)
			return false;
		if (!loop) {
			if (offset + delta < 0 || offset + delta + displaySize > items.size())
				return false;
		}
		offset += delta;
		offset = (offset + items.size()) % items.size();
		notifyDataSetChanged();
		return true;
	}

	@Override
	public boolean onUnhandledDirection(int direction) {
		switch (direction) {
		case View.FOCUS_LEFT:
			if (orientation == ORIENTATION_HORIZONTAL) {
				return changeOffset(-1 * lineSize);
			}
			break;
		case View.FOCUS_RIGHT:
			if (orientation == ORIENTATION_HORIZONTAL) {
				return changeOffset(1 * lineSize);
			}
			break;
		case View.FOCUS_UP:
			if (orientation == ORIENTATION_VERTICAL) {
				return changeOffset(-1 * lineSize);
			}
			break;
		case View.FOCUS_DOWN:
			if (orientation == ORIENTATION_VERTICAL) {
				return changeOffset(1 * lineSize);
			}
			break;
		}
		return false;
	}

	@Override
	public boolean onDispatchKey(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (orientation == ORIENTATION_VERTICAL) {
				switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_PAGE_DOWN:
					return changeOffset(!loop ? Math.min(displaySize, items.size() - displaySize - offset) : displaySize);
				case KeyEvent.KEYCODE_PAGE_UP:
					return changeOffset(!loop ? Math.max(-offset, -displaySize) : -displaySize);
				}
			}
		}
		return false;
	}

	@Override
	public int getXSpace() {
		return 0;
	}

	@Override
	public int getYSpace() {
		return 0;
	}

}
