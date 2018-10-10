package cn.ipanel.android.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Pre-inflate layout file to speed up activity start up later
 * 
 * @author Zexu
 *
 */
public class LayoutPreloader {
	private Map<Integer, View> viewMap = new HashMap<Integer, View>();

	LayoutInflater inflater;

	public LayoutPreloader(Context context) {
		context.getApplicationContext();
		inflater = LayoutInflater.from(context);
	}

	public LayoutPreloader add(int... ids) {
		synchronized (viewMap) {
			for (int id : ids) {
				if (!viewMap.containsKey(id))
					viewMap.put(id, null);
			}
		}
		return this;
	}

	public LayoutPreloader remove(int... ids) {
		synchronized (viewMap) {
			for (int id : ids) {
				viewMap.remove(id);
			}
		}
		return this;
	}

	public void clearAll() {
		synchronized (viewMap) {
			viewMap.clear();
		}
	}

	public void preloadAll() {
		synchronized (viewMap) {

			for (int id : viewMap.keySet()) {
				View view = viewMap.get(id);
				if (view == null) {
					view = inflater.inflate(id, null);
					viewMap.put(id, view);
				}
			}
		}
	}

	public View getView(int id) {
		synchronized (viewMap) {
			View view = viewMap.get(id);
			if (view == null) {
				view = inflater.inflate(id, null);
			} else if (view.getParent() instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup) view.getParent();
				vg.removeView(view);
				vg.endViewTransition(view);
			}
			return view;
		}
	}
}
