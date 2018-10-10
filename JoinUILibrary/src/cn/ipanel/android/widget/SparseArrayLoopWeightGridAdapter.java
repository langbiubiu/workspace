package cn.ipanel.android.widget;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import cn.ipanel.android.util.PageLoadingHelper;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.OnKeyDispatchListener;
import cn.ipanel.android.widget.WeightGridLayout.OnUnhandledFocusDirectionListener;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

public abstract class SparseArrayLoopWeightGridAdapter<T> extends WeightGridAdapter implements
		OnUnhandledFocusDirectionListener, OnKeyDispatchListener {
	static final String TAG = SparseArrayLoopWeightGridAdapter.class.getSimpleName();
	
	public static int ORIENTATION_HORIZONTAL = 0;
	public static int ORIENTATION_VERTICAL = 1;

	protected SparseArray<T> items = new SparseArray<T>();

	protected int displaySize;
	protected int virtualDisplaySize = -1;
	
	protected int offset;

	protected int orientation = ORIENTATION_HORIZONTAL;

	protected int lineSize = 1;
	
	protected int virtualSize = -1;

	protected boolean loop = true;
	
	protected PageLoadingHelper<T> pagingLoader;
	
	public SparseArrayLoopWeightGridAdapter(int displaySize, WeightGridLayout layout) {
		this.displaySize = displaySize;
		//layout.setOnUnhandledFocusDirectionListener(this);
		//layout.setOnKeyDispatchListener(this);
	}
	
	public SparseArrayLoopWeightGridAdapter(int displaySize) {
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
		if (items == null || getVirtualSize() < getVirtualDisplaySize())
			return getVirtualSize();
		if (loop)
			return displaySize;
		else
			return Math.min(getVirtualSize() - offset, displaySize);
	}
	
	public void addAll(int index, Collection<T> items) {
		boolean shouldReload = this.items.size() < offset + displaySize;
		addFrom(index, items);
		if (shouldReload)
			notifyDataSetChanged();
	}

	/**
	 * 
	 * @param pageIndex starts from 0
	 * @param items
	 */
	public void addPageItems(int pageIndex, Collection<T> items) {
		pageIndex = Math.max(0, pageIndex);

		int from = pageIndex * displaySize;
		int to = from + displaySize;
		boolean shouldReload = (from >= offset && from < offset + displaySize)
				|| (to >= offset && to < offset + displaySize);
		addFrom(from, to, items);
		
		if (shouldReload)
			notifyDataSetChanged();
	}
	
	protected void addFrom(int index, Collection<T> items){
		addFrom(index, index+items.size(), items);
	}
	
	protected void addFrom(int index, int to, Collection<T> items){
		Iterator<T> it = items.iterator();
		while(it.hasNext()){
			if (index >= to)
				break;
			this.items.append(index++, it.next());
		}
	}

	@Override
	public T getItem(int position) {
		if (items == null || getVirtualSize() == 0)
			return null;
		return items.get((position + offset) % getVirtualSize());
	}
	
	public void removeItemAt(int realPosition) {
		if (realPosition >= getVirtualSize())
			return;
		SparseArray<T> nArray = new SparseArray<T>();
		for (int i = 0; i < items.size(); i++) {
			int key = items.keyAt(i);
			if (key == realPosition)
				continue;
			if (key > realPosition)
				nArray.append(key - 1, items.valueAt(i));
			else
				nArray.append(key, items.valueAt(i));
		}
		this.items = nArray;
		if (virtualSize > 0)
			virtualSize--;
		int maxOffset = Math.max(0, getVirtualSize() - getVirtualDisplaySize());
		offset = Math.min(offset, maxOffset);
		notifyDataSetChanged();
	}
	
	public T getItemBy(int realPosition){
		if(items == null)
			return null;
		return items.get(realPosition);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setItems(Collection<T> items) {
		setItems(items, 0);
	}
	
	public void setItems(Collection<T> items, int offset) {
		this.offset = offset;
		this.items = new SparseArray<T>();
		addFrom(0, items);
		notifyDataSetChanged();
	}

	public void add(T... item) {
		if (item != null)
			addAll(Arrays.asList(item));
	}

	public void addAll(Collection<T> items) {
		boolean shouldReload = this.items.size() < offset + displaySize;
		addFrom(this.items.size(), items);
		if (shouldReload)
			notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = createView(parent, position);
		}
		if(getItem(position) == null){
			convertView.setVisibility(View.INVISIBLE);
			if(pagingLoader != null){
				int cp = getCurrentPage(false) - 1;
				if (cp >= 0)
					pagingLoader.requestPage(cp);
				if (cp > 1)
					pagingLoader.requestPage(cp - 1);
				if (cp > 2)
					pagingLoader.requestPage(cp - 2);
			}
		} else {
			convertView.setVisibility(View.VISIBLE);
			bindData(convertView, position);
		}
		if (pagingLoader != null && (items.size() - offset) / displaySize < 2) {
			int cp = getCurrentPage(false) - 1;
			if (cp >= 0)
				pagingLoader.requestPage(cp, 2);
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
		return orientation == ORIENTATION_HORIZONTAL ? (displaySize+lineSize-1) / lineSize : lineSize;
	}

	@Override
	public int getYSize() {
		return orientation == ORIENTATION_VERTICAL ? (displaySize+lineSize-1) / lineSize : lineSize;
	}

	public void setOffset(int offset) {
		if (!loop) {
			int maxOffset = Math.max(0, getVirtualSize() - getVirtualDisplaySize());
			offset = Math.min(maxOffset, Math.max(0, offset));
		}
		this.offset = offset;
		notifyDataSetChanged();
	}

	public boolean changeOffset(int delta) {
		Log.d(TAG,
				"changeOffset delta = " + delta + ", items size ="
						+ (items != null ? getVirtualSize() : 0) + ", displaySize = " + displaySize
						+ ", offset=" + offset);
		if (items == null || getVirtualSize() < getVirtualDisplaySize())
			return false;
		if (!loop) {
			if (delta == 0)
				return false;
			int direction = delta / Math.abs(delta);
			if (direction < 0 && offset - 1 < 0)
				return false;
			if (direction > 0 && offset + 1 + getVirtualDisplaySize() > getVirtualSize())
				return false;
		}
		offset += delta;
		offset = (offset + getVirtualSize()) % getVirtualSize();
		notifyDataSetChanged();
		return true;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		onPageChange(getCurrentPage(), getTotalPage());
	}

	protected void onPageChange(int currentPage, int totalPage) {
		// TODO Auto-generated method stub
		
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
					return pageDown();
				case KeyEvent.KEYCODE_PAGE_UP:
					return pageUp();
				}
			}
		}
		return false;
	}

	public boolean pageUp() {
		return changeOffset(!loop ? Math.max(-offset, -getVirtualDisplaySize()) : -getVirtualDisplaySize());
	}

	public boolean pageDown() {
		int maxOffset = Math.max(0, getVirtualSize() - getVirtualDisplaySize());
		if (lineSize > 1 && getVirtualSize() % lineSize != 0) {
			maxOffset += lineSize - getVirtualSize() % lineSize;
		}
		return changeOffset(!loop ? Math.min(getVirtualDisplaySize(), maxOffset - offset) : getVirtualDisplaySize());
	}
	
	public int getTotalPage() {
		return getTotalPage(true);
	}
	
	public int getTotalPage(boolean virtual) {
		if (displaySize == 0)
			return 0;
		if (virtual)
			return (getVirtualSize() + getVirtualDisplaySize() - 1) / getVirtualDisplaySize();
		return (getVirtualSize() + displaySize - 1) / displaySize;
	}
	
	public void setVirtualSize(int size){
		this.virtualSize = size;
	}

	public int getVirtualSize() {
		if(virtualSize > -1)
			return virtualSize;
		if (items.size() > 0)
			return items.keyAt(items.size() - 1) + 1;
		return 0;
	}
	
	public int getVirtualDisplaySize() {
		if(virtualDisplaySize > 0)
			return virtualDisplaySize;
		return displaySize;
	}
	
	/**
	 * 
	 * @return page number, starting from 1
	 */
	public int getCurrentPage() {
		return getCurrentPage(true);
	}
	
	protected int getCurrentPage(boolean virtual) {
		int maxOffset = Math.max(0, getVirtualSize()
				- (virtual ? getVirtualDisplaySize() : displaySize));
		int temp = Math.min(offset, maxOffset);
		temp = Math.max(0, temp);
		int total = getTotalPage(virtual) - 1;
		if (total < 0)
			return 0;
		if (maxOffset == 0)
			return 1;
		return (temp * total / maxOffset) + 1;
	}
	
	/**
	 * 
	 * @param page page number, starting from 1
	 */
	public void setCurrentPage(int page) {
		page--;
		int maxOffset = Math.max(0, getVirtualSize() - getVirtualDisplaySize());
		if (lineSize > 1 && getVirtualSize() % lineSize != 0) {
			maxOffset += lineSize - getVirtualSize() % lineSize;
		}
		int pageOffset = Math.min(getVirtualDisplaySize() * page, maxOffset);
		Log.d(TAG, "setCurrentPage page = " + page + ", items size ="
				+ (items != null ? getVirtualSize() : 0) + ", virtualDisplaySize = "
				+ getVirtualDisplaySize() + ", offset=" + pageOffset);

		setOffset(pageOffset);
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
