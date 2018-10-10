package ipanel.join.widget;

import java.lang.ref.SoftReference;

import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.Screen;
import ipanel.join.configuration.ViewInflater;

import org.json.JSONArray;
import org.json.JSONException;

import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

public class BasicPagerAdapter extends PagerAdapter {
	JSONArray mJsonArray;
	
	SparseArray<SoftReference<View>> mViewCache = new SparseArray<SoftReference<View>>();

	boolean mLoop = false;

	public BasicPagerAdapter(JSONArray jsa) {
		this(jsa, false);
	}

	public BasicPagerAdapter(JSONArray jsa, boolean loop) {
		this.mJsonArray = jsa;
		this.mLoop = loop;
	}
	
	public int getRealCount(){
		return mJsonArray.length();
	}

	@Override
	public int getCount() {
		if (mJsonArray.length() > 0 && mLoop)
			return Integer.MAX_VALUE;
		return mJsonArray.length();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		int realPosition = mLoop ? position % mJsonArray.length() : position;

		container.removeView((View) object);
		mViewCache.put(realPosition, new SoftReference<View>((View) object));
	}

	@Override
	public float getPageWidth(int position) {
		try {
			if (mLoop)
				position = position % mJsonArray.length();
			return (float) mJsonArray.getJSONObject(position).getDouble("width");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1f;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		int realPosition = mLoop ? position % mJsonArray.length() : position;
		if (mViewCache.get(realPosition) != null && mViewCache.get(realPosition).get() != null
				&& mViewCache.get(realPosition).get().getParent() == null) {
			View view = mViewCache.get(realPosition).get();
			view.setTag("_BasicPagerAdapter_" + position);
			container.addView(view);
			return view;
		}
		try {
			String id = mJsonArray.getJSONObject(realPosition).getString("id");
			Screen sc = ConfigState.getInstance().getConfiguration().findScreenById(id);
			if (sc != null) {
				View view = ViewInflater.inflateView(container.getContext(), null, sc.getView());
				if (container instanceof IConfigViewGroup) {
					view.setLayoutParams(((IConfigViewGroup) container).genConfLayoutParams(sc
							.getView()));
				}
				container.addView(view);
				view.setTag("_BasicPagerAdapter_" + position);
				return view;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
