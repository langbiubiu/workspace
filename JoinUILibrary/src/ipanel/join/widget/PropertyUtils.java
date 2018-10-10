package ipanel.join.widget;

import ipanel.join.configuration.Action;
import ipanel.join.configuration.Bind;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.Value;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetchTask.TaskType;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.LevelListImageFetchTask;
import cn.ipanel.android.net.imgcache.StateListImageFetchTask;

public class PropertyUtils {

	public static final String PROP_CLIP_CHILDREN = "clipChildren";

	public static final String PROP_CLIP_TO_PADDING = "clipToPadding";

	public static final String PROP_FOCUS_CHANGE_LISTENER = "focusChangeListener";

	public static final String PROP_CLICK_LISTENER = "clickListener";

	public static final String PROP_NEXT_FOCUS_RIGHT = "nextFocusRight";

	public static final String PROP_NEXT_FOCUS_LEFT = "nextFocusLeft";

	public static final String PROP_NEXT_FOCUS_DOWN = "nextFocusDown";

	public static final String PROP_NEXT_FOCUS_UP = "nextFocusUp";

	public static final String PROP_ID = "id";

	public static final String PROP_TAG = "tag";

	public static final String GRAVITY_BOTTOM = "bottom";

	public static final String GRAVITY_TOP = "top";

	public static final String GRAVITY_CENTER_VERTICAL = "center_vertical";

	public static final String GRAVITY_CENTER_HORIZONTAL = "center_horizontal";

	public static final String GRAVITY_RIGHT = "right";

	public static final String GRAVITY_LEFT = "left";

	public static final String GRAVITY_CENTER = "center";

	public static final String STATE_NORMAL = "normal";

	public static final String STATE_SELECTED = "selected";

	public static final String STATE_FOCUS = "focus";

	public static final String PROP_PADDING = "padding";

	public static final String PROP_BACKGROUND_DRAWABLE = "backgroundDrawable";

	public static final String PROP_BACKGROUND_DRAWABLE9 = "backgroundDrawable9";

	public static final String PROP_BACKGROUND_COLOR = "backgroundColor";

	public static final String PROP_SHOW_FOCUS_FRAME = "showFocusFrame";

	public static final String PROP_FOCUSABLE = "focusable";

	public static final String PROP_DUPLICATE_PARENT_STATE = "duplicateParentState";

	public static final String PROP_VISIBILITY = "visibility";

	public static final String PROP_LEVEL_LOW = "low";

	public static final String PROP_LEVEL_HIGH = "high";

	public static final String PROP_LEVEL_URL = "url";

	public static final String PROP_ALPHA = "alpha";

	public static final String PROP_REQUEST_FOCUS = "requestFocus";

	public static final String PROP_BITMAP_MAX_SIZE = "bitmapMaxSize";

	public static final String PROP_CORNERS = "corners";

	public static ColorStateList genColorList(JSONObject json) {
		try {
			List<int[]> sl = new ArrayList<int[]>();
			List<Integer> cl = new ArrayList<Integer>();
			if (json.has(STATE_FOCUS)) {
				sl.add(new int[] { android.R.attr.state_focused });
				cl.add(parseColor(json.optString(STATE_FOCUS)));
			}
			if (json.has(STATE_SELECTED)) {
				sl.add(new int[] { android.R.attr.state_selected });
				cl.add(parseColor(json.optString(STATE_SELECTED)));
			}
			if (json.has(STATE_NORMAL)) {
				sl.add(new int[] {});
				cl.add(parseColor(json.optString(STATE_NORMAL)));
			}
			int[][] states = new int[sl.size()][];
			int[] colors = new int[sl.size()];
			for (int i = 0; i < sl.size(); i++) {
				states[i] = sl.get(i);
				colors[i] = cl.get(i);
			}
			return new ColorStateList(states, colors);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int parseColor(String str) {
		try {
			BigInteger bi = new BigInteger(str, 16);
			return bi.intValue();
		} catch (NumberFormatException e) {

		}
		return Color.parseColor(str);
	}

	public static int getScaledWidth(JSONObject json) {
		return getScaledSize(json, "width");
	}

	public static int getScaledHeight(JSONObject json) {
		return getScaledSize(json, "height");
	}

	public static int getScaledLeft(JSONObject json) {
		return getScaledSize(json, "left");
	}

	public static int getScaledRight(JSONObject json) {
		return getScaledSize(json, "right");
	}

	public static int getScaledTop(JSONObject json) {
		return getScaledSize(json, "top");
	}

	public static int getScaledBottom(JSONObject json) {
		return getScaledSize(json, "bottom");
	}

	public static int getScaledSize(JSONObject json, String key) {
		return getScaledSize(json.optInt(key));
	}

	public static int getScaledSize(int size) {
		if (size <= 0) {
			return size;
		}
		float scale = ConfigState.getInstance().getConfiguration().getScale();
		return Math.round(scale * size);
	}

	public static float getScaledSize(float size) {
		if (size <= 0) {
			return size;
		}
		float scale = ConfigState.getInstance().getConfiguration().getScale();
		return scale * size;
	}

	public static int parseGravity(String gNames) {
		int gravity = Gravity.NO_GRAVITY;
		if (gNames != null && gNames.length() > 0) {
			String[] secs = gNames.split("\\|");
			for (String gName : secs) {
				if (GRAVITY_CENTER.equals(gName)) {
					gravity |= Gravity.CENTER;
				}
				if (GRAVITY_LEFT.equals(gName))
					gravity |= Gravity.LEFT;
				if (GRAVITY_RIGHT.equals(gName))
					gravity |= Gravity.RIGHT;
				if (GRAVITY_CENTER_HORIZONTAL.equals(gName))
					gravity |= Gravity.CENTER_HORIZONTAL;
				if (GRAVITY_CENTER_VERTICAL.equals(gName))
					gravity |= Gravity.CENTER_VERTICAL;
				if (GRAVITY_TOP.equals(gName))
					gravity |= Gravity.TOP;
				if (GRAVITY_BOTTOM.equals(gName))
					gravity |= Gravity.BOTTOM;
			}
		}
		return gravity;
	}

	public static int getMaxBitmapSize(ipanel.join.configuration.View data) {
		Bind bind = data.getBindByName(PROP_BITMAP_MAX_SIZE);
		if (bind != null) {
			return Integer.parseInt(bind.getValue().getvalue());
		}
		return -1;
	}

	public static void loadDrawable(android.view.View view, Value v,
			TaskType type, int maxSize) {
		loadDrawable(view, v, type, maxSize, 10);
	}

	public static void loadDrawable(android.view.View view, Value v,
			TaskType type) {
		loadDrawable(view, v, type, -1);
	}

	public static void loadDrawable(android.view.View view, Value v,
			TaskType type, int maxSize, int corners) {
		if (v != null) {
			ImageFetcher mFetcher = ConfigState.getInstance().getImageFetcher(
					view.getContext());
			if (Value.TYPE_JSON.equals(v.getType())) {
				try {
					JSONObject jobj = v.getJsonValue();
					StateListImageFetchTask task = maxSize > 0 ? new StateListImageFetchTask(
							maxSize, maxSize) : mFetcher.getTask();
					task.setCorners(corners);
					task.setTaskType(type);
					if (jobj.has(STATE_FOCUS)) {
						task.addStateUrl(
								new int[] { android.R.attr.state_focused },
								jobj.getString(STATE_FOCUS));
					}
					if (jobj.has(STATE_SELECTED)) {
						task.addStateUrl(
								new int[] { android.R.attr.state_selected },
								jobj.getString(STATE_SELECTED));
					}
					if (jobj.has(STATE_NORMAL)) {
						task.addStateUrl(new int[] {},
								jobj.getString(STATE_NORMAL));
					}
					mFetcher.loadImage(task, view);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (Value.TYPE_STRING.equals(v.getType())) {
				BaseImageFetchTask task = maxSize > 0 ? new BaseImageFetchTask(
						v.getvalue(), maxSize, maxSize) : mFetcher
						.getBaseTask(v.getvalue());
				task.setCorners(corners);
				task.setTaskType(type);
				mFetcher.loadImage(task, view);
			}
		}

	}

	public static void loadLevelListDrawable(android.view.View view, Value v,
			TaskType type) {
		if (v != null) {
			ImageFetcher mFetcher = ConfigState.getInstance().getImageFetcher(
					view.getContext());
			if (Value.TYPE_JSON.equals(v.getType())) {
				try {
					JSONArray jsa = v.getArrayValue();
					LevelListImageFetchTask task = mFetcher.getLevelListTask();
					for (int i = 0; i < jsa.length(); i++) {
						JSONObject jobj = jsa.getJSONObject(i);
						int[] levels = new int[] { jobj.optInt(PROP_LEVEL_LOW),
								jobj.getInt(PROP_LEVEL_HIGH) };
						task.addLevelUrl(levels, jobj.getString(PROP_LEVEL_URL));
					}
					mFetcher.loadImage(task, view);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static void loadDrawable9(android.view.View view, Value v,
			TaskType type) {
		if (v != null) {
			ImageFetcher mFetcher = ConfigState.getInstance().getImageFetcher(
					view.getContext());
			if (Value.TYPE_JSON.equals(v.getType())) {
				try {
					JSONObject jobj = v.getJsonValue();
					BaseImageFetchTask task = mFetcher.getBaseTask(jobj
							.getString("url"));
					task.setTaskType(type);
					Rect patch = new Rect();
					Rect padding = new Rect();
					if (jobj.has("patch")) {
						JSONObject p = jobj.getJSONObject("patch");
						patch.left = getScaledLeft(p);
						patch.right = getScaledRight(p);
						patch.top = getScaledTop(p);
						patch.bottom = getScaledBottom(p);
					}
					if (jobj.has("padding")) {
						JSONObject p = jobj.getJSONObject("patch");
						padding.left = getScaledLeft(p);
						padding.right = getScaledRight(p);
						padding.top = getScaledTop(p);
						padding.bottom = getScaledBottom(p);

					}
					task.setNinePatch(patch, padding);
					mFetcher.loadImage(task, view);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (Value.TYPE_STRING.equals(v.getType())) {
				BaseImageFetchTask task = mFetcher.getBaseTask(v.getvalue());
				task.setTaskType(type);
				mFetcher.loadImage(task, view);
			}
		}

	}

	@SuppressLint("NewApi")
	public static void setCommonProperties(android.view.View view,
			ipanel.join.configuration.View data) {
		Bind bind = data.getBindByName(PROP_DUPLICATE_PARENT_STATE);
		if (bind != null && bind.matchTarget(data.getId())) {
			view.setDuplicateParentStateEnabled(Boolean.parseBoolean(bind
					.getValue().getvalue()));
		}

		bind = data.getBindByName(PROP_ID);
		if (bind != null && bind.matchTarget(data.getId())) {
			view.setId(getIdFromBind(view, bind));
		}
		bind = data.getBindByName(PROP_ALPHA);
		if (bind != null && bind.matchTarget(data.getId())) {
			view.setAlpha(Float.parseFloat(bind.getValue().getvalue()));
		}
		bind = data.getBindByName(PROP_NEXT_FOCUS_UP);
		if (bind != null && bind.matchTarget(data.getId())) {
			view.setNextFocusUpId(getIdFromBind(view, bind));
		}

		bind = data.getBindByName(PROP_NEXT_FOCUS_DOWN);
		if (bind != null && bind.matchTarget(data.getId())) {
			view.setNextFocusDownId(getIdFromBind(view, bind));
		}

		bind = data.getBindByName(PROP_NEXT_FOCUS_LEFT);
		if (bind != null && bind.matchTarget(data.getId())) {
			view.setNextFocusLeftId(getIdFromBind(view, bind));
		}

		bind = data.getBindByName(PROP_NEXT_FOCUS_RIGHT);
		if (bind != null && bind.matchTarget(data.getId())) {
			view.setNextFocusRightId(getIdFromBind(view, bind));
		}

		bind = data.getBindByName(PROP_FOCUSABLE);
		if (bind != null && bind.matchTarget(data.getId())) {
			view.setFocusable(Boolean.parseBoolean(bind.getValue().getvalue()));
			view.setFocusableInTouchMode(Boolean.parseBoolean(bind.getValue()
					.getvalue()));
		}

		bind = data.getBindByName(PROP_VISIBILITY);
		if (bind != null && bind.matchTarget(data.getId())) {
			view.setVisibility(Integer.parseInt(bind.getValue().getvalue()));
		}

		bind = data.getBindByName(PROP_SHOW_FOCUS_FRAME);
		if (bind != null && view instanceof IConfigView
				&& bind.matchTarget(data.getId())) {
			((IConfigView) view).setShowFocusFrame(Boolean.parseBoolean(bind
					.getValue().getvalue()));
		}

		bind = data.getBindByName(PROP_BACKGROUND_COLOR);
		if (bind != null && bind.matchTarget(data.getId())) {
			view.setBackgroundColor(parseColor(bind.getValue().getvalue()));
		}

		bind = data.getBindByName(PROP_BACKGROUND_DRAWABLE);
		if (bind != null && bind.matchTarget(data.getId())) {
			Value v = bind.getValue();
			loadDrawable(view, v, TaskType.BACKGROUND, getMaxBitmapSize(data));
		}

		bind = data.getBindByName(PROP_BACKGROUND_DRAWABLE9);
		if (bind != null && bind.matchTarget(data.getId())) {
			Value v = bind.getValue();
			loadDrawable9(view, v, TaskType.BACKGROUND);
		}

		bind = data.getBindByName(PROP_PADDING);
		if (bind != null && bind.matchTarget(data.getId())) {
			Value value = bind.getValue();
			if (Value.TYPE_JSON.equals(value.getType())) {
				try {
					JSONObject json = value.getJsonValue();
					view.setPadding(getScaledLeft(json), getScaledTop(json),
							getScaledRight(json), getScaledBottom(json));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (Value.TYPE_STRING.equals(value.getType())) {
				int pad = Integer.parseInt(value.getvalue());
				view.setPadding(pad, pad, pad, pad);
			}
		}

		bind = data.getBindByName(PROP_TAG);
		if (bind != null && bind.matchTarget(data.getId())) {
			view.setTag(bind.getValue().getvalue());
		}

		bind = data.getBindByName(PROP_REQUEST_FOCUS);
		if (bind != null && bind.matchTarget(data.getId())) {
			if (Boolean.parseBoolean(bind.getValue().getvalue()))
				view.requestFocus();
		}

		if (view instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) view;
			bind = data.getBindByName(PROP_CLIP_TO_PADDING);
			if (bind != null && bind.matchTarget(data.getId())) {
				vg.setClipToPadding(Boolean.parseBoolean(bind.getValue()
						.getvalue()));
			}
			bind = data.getBindByName(PROP_CLIP_CHILDREN);
			if (bind != null && bind.matchTarget(data.getId())) {
				vg.setClipChildren(Boolean.parseBoolean(bind.getValue()
						.getvalue()));
			}
		}

		if (data.hasClickAction() || ((IConfigView) view).showFocusFrame()) {
			bind = data.getBindByName(PROP_CLICK_LISTENER);
			if (bind != null && bind.matchTarget(data.getId())) {
				try {
					ClassLoader classLoader = ConfigState.getInstance()
							.getClassLoader();
					if (classLoader == null)
						classLoader = view.getContext().getClassLoader();
					Class<?> clazz = classLoader.loadClass(bind.getValue()
							.getvalue());
					view.setOnClickListener((OnClickListener) clazz
							.newInstance());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (!(view instanceof AdapterView)) {
				view.setOnClickListener(sClickListener);
			}
		}

		if (data.hasFocusAction()) {
			bind = data.getBindByName(PROP_FOCUS_CHANGE_LISTENER);
			if (bind != null && bind.matchTarget(data.getId())) {
				try {
					ClassLoader classLoader = ConfigState.getInstance()
							.getClassLoader();
					if (classLoader == null)
						classLoader = view.getContext().getClassLoader();
					Class<?> clazz = classLoader.loadClass(bind.getValue()
							.getvalue());
					view.setOnFocusChangeListener((OnFocusChangeListener) clazz
							.newInstance());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				view.setOnFocusChangeListener(sFocusChangeListener);
			}
		}

	}

	public static int getIdFromBind(android.view.View view, Bind bind) {
		return view.getResources().getIdentifier(bind.getValue().getvalue(),
				PROP_ID, view.getContext().getPackageName());
	}

	private static OnFocusChangeListener sFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus && v instanceof IConfigView) {
				IConfigView configView = (IConfigView) v;
				configView.onAction(Action.EVENT_ONFOCUS);
			}
		}
	};

	private static OnClickListener sClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v instanceof IConfigView) {
				IConfigView configView = (IConfigView) v;
				configView.onAction(Action.EVENT_ONCLICK);
			}

		}
	};
}
