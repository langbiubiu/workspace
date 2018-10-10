package cn.ipanel.android.widget;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import cn.ipanel.android.util.PageLoadingHelper;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Adapter for ViewPager, handles view cache and pagination.
 * 
 * @author Zexu
 *
 * @param <T>
 */
public abstract class ArrayPagerAdapter<T> extends PagerAdapter {
	public static class ItemInfo {
		public ItemInfo(Object obj, View v, int position) {
			this.item = obj;
			this.viewRef = new SoftReference<View>(v);
			this.position = position;
		}

		public Object item;
		public SoftReference<View> viewRef;
		public int position;
	}

	private SparseArray<ItemInfo> itemInfos = new SparseArray<ArrayPagerAdapter.ItemInfo>();

	protected List<T> items = new ArrayList<T>();
	private List<View> offscreenCache = new ArrayList<View>();

	private int offscreenCacheSize = 3;

	private int itemsPerPage = 1;

	private PageLoadingHelper<T> pageLoader;

	public void setPageLoader(PageLoadingHelper<T> loader) {
		this.pageLoader = loader;
		if (pageLoader != null)
			pageLoader.requestPage(0, offscreenCacheSize);
	}

	@Override
	public int getCount() {
		if (items == null)
			return 0;
		return (items.size() + itemsPerPage - 1) / itemsPerPage;
	}

	@Override
	public boolean isViewFromObject(View view, Object item) {
		if (item instanceof ItemInfo) {
			ItemInfo ii = (ItemInfo) item;
			return ii.viewRef.get() == view;
		}
		return false;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (object instanceof ItemInfo) {

			ItemInfo itemInfo = (ItemInfo) object;
			if (itemInfo.position >= getCount())
				itemInfos.remove(itemInfo.position);
			View view = itemInfo.viewRef.get();
			container.removeView(view);
			if (offscreenCache.size() < offscreenCacheSize) {
				itemInfo.viewRef.clear();
				offscreenCache.add(view);
			}
		}
	}

	@Override
	public int getItemPosition(Object object) {
		if (object instanceof ItemInfo) {
			ItemInfo itemInfo = (ItemInfo) object;
			if (itemInfos.get(itemInfo.position) == null)
				return POSITION_NONE;
			return itemInfo.position;
		}
		return super.getItemPosition(object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ItemInfo itemInfo = itemInfos.get(position);
		if (itemInfo == null || itemInfo.viewRef.get() == null) {
			View v = null;
			if (offscreenCache.size() > 0)
				v = offscreenCache.remove(0);
			else
				v = createView(container, position);
			if (itemInfo == null) {
				itemInfo = new ItemInfo(getItem(position), v, position);
			} else {
				itemInfo.position = position;
				itemInfo.item = getItem(position);
				itemInfo.viewRef = new SoftReference<View>(v);
			}
			itemInfos.put(position, itemInfo);
		}
		View view = itemInfo.viewRef.get();
		bindData(view, position);
		container.addView(view, 0);
		if (pageLoader != null) {
			pageLoader.requestPage(position, offscreenCacheSize);
		}
		return itemInfo;
	}

	public T getItem(int position) {
		return items.get(position % getItemCount());
	}

	public int getItemCount() {
		return items.size();
	}

	public void add(T... item) {
		if(item != null)
			addAll(Arrays.asList(item));
	}

	public void remove(T item) {
		items.remove(item);
		notifyDataSetChanged();
	}

	public void clear() {
		itemInfos.clear();
		items.clear();
		notifyDataSetChanged();
	}

	public void clearCachedView() {
		offscreenCache.clear();
	}

	public void setItems(Collection<T> items) {
		itemInfos.clear();
		this.items = new ArrayList<T>(items);
		notifyDataSetChanged();
	}

	public void addAll(Collection<T> items) {
		this.items.addAll(items);
		notifyDataSetChanged();
	}

	public List<T> getItemsForPage(int position) {
		int start = itemsPerPage * position;
		int end = Math.min(items.size(), start + itemsPerPage);
		return items.subList(start, end);
	}

	public abstract void bindData(View page, int position);

	public abstract View createView(ViewGroup container, int position);

	public int getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(int itemsPerPage) {
		if (itemsPerPage > 0) {
			this.itemsPerPage = itemsPerPage;
			notifyDataSetChanged();
		}
	}

	public void setOffscreenCacheSize(int size) {
		if (size >= 0)
			this.offscreenCacheSize = size;
	}
}
