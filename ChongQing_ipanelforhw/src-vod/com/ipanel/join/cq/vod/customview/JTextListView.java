package com.ipanel.join.cq.vod.customview;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.customview.JHListView.AnimInEndListener;
import com.ipanel.join.cq.vod.customview.JHListView.AnimInStartListener;
import com.ipanel.join.cq.vod.customview.JHListView.AnimOutEndListener;
import com.ipanel.join.cq.vod.customview.JHListView.AnimOutStartListener;
import com.ipanel.join.cq.vod.customview.JHListView.ListDataEmptyListener;
import com.ipanel.join.cq.vod.customview.JHListView.ListDataLoadTimeUp;
import com.ipanel.join.cq.vod.customview.JHListView.ListDataLoadingCallBack;
import com.ipanel.join.cq.vod.customview.JHListView.ListDataLoadingListener;
import com.ipanel.join.cq.vod.customview.JHListView.ListFocusChangeListener;
import com.ipanel.join.cq.vod.customview.JHListView.ListKeyPressgListener;
import com.ipanel.join.cq.vod.customview.JHListView.ListOnEnterListener;
import com.ipanel.join.cq.vod.customview.JHListView.ListPreFocusChangeListener;

public abstract class JTextListView extends RelativeLayout {
	public final static String TAG = "Navigation";
	public final static int STATE_OF_UN_READY = -1;
	public final static int STATE_OF_LOADING = -2;
	public final static int STATE_OF_EMPTY = -3;
	public final static int STATE_OF_NORMAL = 0;
	public final static int STATE_OF_VISIBLE = 4;
	public final static int STATE_OF_INVISIBLE = 1;
	public final static int STATE_OF_IN_ANIMING = 2;
	public final static int STATE_OF_OUT_ANIMING = 3;
	protected ArrayList<View> views = new ArrayList<View>();

	public int current_state = 1;
	public int list_item_height = 82;
	public int list_item_width = 185;
	public int spacing = 0;
	public int padding = 46;
	public int font_scale=28;

	public int show_count = 6;
	public int data_count;

	public int center_index;
	public int current_index = 0;
	public int selector_index;
	public int selector_res = R.color.transparent;
	public int anim_type = 0;
	public int anim_delay = 30;
	public int anim_time = 90;

	public int in_anim = 0;
	public int out_anim = 0;
	public int item_resourceId = 0;

	public int defaultAnimDelay = 500;
	public int defaultAnimTime = 60;// 上下动画时间
	public int defaultCenter = 0;

	public boolean move_flag = true;
	public boolean circle_flag = true;
	public boolean focus_flag = true;
	public boolean zoom_flag = false;
	public boolean layout_flag = false;

	public float density;
	public Context context;
	public View selector_view;
	public View tmpView;

	public AnimInStartListener in_start_listener;
	public AnimInEndListener in_end_listener;
	public AnimOutStartListener out_start_listener;
	public AnimOutEndListener out_end_listener;
	public ListFocusChangeListener focus_change_listener;
	public ListOnEnterListener enter_listener;
	public ListDataEmptyListener empty_listener;
	public ListDataLoadingListener loading_listener;
	public ListKeyPressgListener press_listener;
	public ListDataLoadingCallBack loading_callback;
	public ListDataLoadTimeUp time_up_listener;
	public ListPreFocusChangeListener pre_focus_change_listener;
	protected Handler mhandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 1:
				if (focus_change_listener != null) {
					focus_change_listener.onFocusChange(getCurrentIndex());
				}
				break;
			case 2:
				if (in_anim == 0) {
					if (in_start_listener != null) {
						in_start_listener.onAnimInStart();
					}
				}
				if (in_anim == show_count - 1) {
					mhandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							selector_view.setVisibility(View.VISIBLE);
							current_state = STATE_OF_VISIBLE;
							if (in_end_listener != null) {
								in_end_listener.onAnimInEnd();
							}
						}
					}, anim_delay);
				}
				if (anim_type == 0) {
					views.get(in_anim).setVisibility(View.VISIBLE);

				} else {
					views.get(in_anim).setVisibility(View.VISIBLE);
					View vv = views.get(in_anim);
					vv.setPivotX(0);
					vv.setRotationY(-90);
					vv.animate().setDuration(anim_time);
					vv.animate().rotationY(0);
				}
				in_anim++;
				if (in_anim < show_count) {
					mhandler.sendEmptyMessageDelayed(2, anim_delay);
				} else {
					in_anim = 0;
				}
				break;
			case 3:
				if (out_anim == 0) {
					if (out_start_listener != null) {
						out_start_listener.onAnimOutStart();
					}
				}
				if (out_anim == show_count - 1) {
					current_state = STATE_OF_INVISIBLE;
					if (out_end_listener != null) {
						out_end_listener.onAnimOutEnd();
					}
				}
				if (anim_type == 0) {
					views.get(show_count - out_anim - 1).setVisibility(
							View.INVISIBLE);
				} else {
					View vv = views.get(in_anim);
					vv.setPivotX(0);
					vv.setRotationY(0);
					vv.animate().setDuration(anim_time);
					vv.animate().rotationY(90);
				}

				out_anim++;
				if (out_anim < show_count) {
					mhandler.sendEmptyMessageDelayed(3, anim_delay);
				} else {
					out_anim = 0;
				}
				break;
			case 4:
				if (loading_callback != null) {
					loading_callback.onLoadingComplete(msg.obj);
				}
				mhandler.removeMessages(5);
				break;
			case 5:
				if (time_up_listener != null) {
					time_up_listener.onTimeUp();
				}
				break;
			default:
				break;
			}
		}
	};

	public abstract View getListView(int position, View convertView);

	public abstract int getDataCount();

	public JTextListView(Context context) {
		super(context,null);
	}

	public JTextListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		current_state = STATE_OF_UN_READY;
		this.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					return onKeyDown(keyCode, event);
				}
				return false;
			}
		});
		this.setFocusable(true);
	}

	public void init() {
		circle_flag = false;
		move_flag = true;
		zoom_flag = false;
		current_state = STATE_OF_UN_READY;
		selector_index = 0;
		current_index = 0;
		data_count = getDataCount();
		if (data_count <= 0) {
			current_state = STATE_OF_EMPTY;

			if (empty_listener != null) {
				empty_listener.onEmpty();
			}
			return;
		}
		if (show_count > data_count) {
			show_count = data_count;
		}
		if (defaultCenter < 0)
			center_index = show_count % 2 == 0 ? (show_count / 2 - 1)
					: (show_count / 2);
		else
			center_index = defaultCenter;
		selector_index = center_index;
		int child_count = this.getChildCount();
		if (show_count != child_count - 2) {
			this.removeAllViews();
			views.clear();
			for (int i = 0; i < show_count; i++) {
				View view = getConvertView();
				views.add(view);
				this.addViewInLayout(view, this.getChildCount() - 1,
						view.getLayoutParams());
			}
			tmpView = getConvertView();
			tmpView.setVisibility(View.INVISIBLE);
			this.addViewInLayout(tmpView, this.getChildCount() - 1,
					tmpView.getLayoutParams());
			selector_view = new View(context);
//			selector_view.setBackgroundResource(R.drawable.selector);
			this.addView(selector_view, getLayoutParams(null, selector_index));
		} else {
			this.moveSelector(selector_index);
		}
		invalidateAll();
		current_state = STATE_OF_VISIBLE;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = 0;
		int width = 0;
		width = View.MeasureSpec.getSize(widthMeasureSpec);
		height = View.MeasureSpec.getSize(heightMeasureSpec);

		widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width,
				View.MeasureSpec.EXACTLY);
		heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height,
				View.MeasureSpec.EXACTLY);
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG, "list view press key: " + keyCode);
		Log.i(TAG, this.getClass().getName());
		if (press_listener != null) {
			boolean result = press_listener.onKeyPress(keyCode);
			if (result) {
				return true;
			}
		}
		if (!isValid()) {
			return true;
		}
		switch (keyCode) {
//		case KeyEvent.KEYCODE_DPAD_RIGHT:
//			if (!circle_flag) {
//				if (this.getCurrentIndex() == data_count - 1) {
//					return true;
//				}
//			}
//			if (data_count > 1 && isMoveAble()) {
//				this.clearAnim();
//				if (move_flag) {
//					if (selector_index < show_count - 1) {
//						moveSelector(false);
//						listLayout();
//					} else {
//						doSpecialAnim(true);
//						current_index = calCurrentIndex(false, false);
//						moveSelector(false);
//						listLayout();
//						animUp(-1);
//					}
//				} else {
//					doSpecialAnim(true);
//					current_index = calCurrentIndex(false, false);
//					moveSelector(false);
//					listLayout();
//					animUp(-1);
//				}
//				current_state = STATE_OF_VISIBLE;
//				if (focus_change_listener != null)
//					doChangeOperation();
//			}
//			return true;
//		case KeyEvent.KEYCODE_DPAD_LEFT:
//			if (!circle_flag) {
//				if (this.getCurrentIndex() == 0) {
//					return true;
//				}
//			}
//			if (data_count > 1 && isMoveAble()) {
//				this.clearAnim();
//				if (move_flag) {
//					if (selector_index > 0) {
//						moveSelector(true);
//						listLayout();
//					} else {
//						doSpecialAnim(false);
//						current_index = calCurrentIndex(true, false);
//						moveSelector(true);
//						listLayout();
//						animDown(-1);
//					}
//				} else {
//					doSpecialAnim(false);
//					current_index = calCurrentIndex(true, false);
//					moveSelector(true);
//					listLayout();
//					animDown(-1);
//				}
//				current_state = STATE_OF_VISIBLE;
//				if (focus_change_listener != null)
//					doChangeOperation();
//			}
//			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (!circle_flag) {
				if (this.getCurrentIndex() == data_count - 1) {
					return true;
				}
			}
			if (data_count > 1 && isMoveAble()) {
				this.clearAnim();
				if (move_flag) {
					if (selector_index < show_count - 1) {
						moveSelector(false);
						listLayout();
					} else {
						doSpecialAnim(true);
						current_index = calCurrentIndex(false, false);
						moveSelector(false);
						listLayout();
						animUp(-1);
					}
				} else {
					doSpecialAnim(true);
					current_index = calCurrentIndex(false, false);
					moveSelector(false);
					listLayout();
					animUp(-1);
				}
				current_state = STATE_OF_VISIBLE;
				if (focus_change_listener != null)
					doChangeOperation();
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_UP:
			if (!circle_flag) {
				if (this.getCurrentIndex() == 0) {
					return true;
				}
			}
			if (data_count > 1 && isMoveAble()) {
				this.clearAnim();
				if (move_flag) {
					if (selector_index > 0) {
						moveSelector(true);
						listLayout();
					} else {
						doSpecialAnim(false);
						current_index = calCurrentIndex(true, false);
						moveSelector(true);
						listLayout();
						animDown(-1);
					}
				} else {
					doSpecialAnim(false);
					current_index = calCurrentIndex(true, false);
					moveSelector(true);
					listLayout();
					animDown(-1);
				}
				current_state = STATE_OF_VISIBLE;
				if (focus_change_listener != null)
					doChangeOperation();
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			
			break;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if (enter_listener != null) {
				enter_listener.onEnter(this.getCurrentIndex());
				return true;
			}
			break;
		case 92:
			if (circle_flag) {
				int tmp = calCurrentIndex(true, true);
				if (current_index != tmp) {
					setCurrentIndex(tmp);
					if (focus_change_listener != null)
						doChangeOperation();
				}
			}
			return true;
		case 93:
			if (circle_flag) {
				int tmp = calCurrentIndex(false, true);
				if (current_index != tmp) {
					setCurrentIndex(tmp);
					if (focus_change_listener != null)
						doChangeOperation();
				}
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

//	public boolean onKeyDown(int keyCode) {
//		if (!isValid()) {
//			return true;
//		}
//		switch (keyCode) {
//		case KeyEvent.KEYCODE_DPAD_DOWN:
//			if (!circle_flag) {
//				if (this.getCurrentIndex() == data_count - 1) {
//					return true;
//				}
//			}
//			if (data_count > 1 && isMoveAble()) {
//				this.clearAnim();
//				if (move_flag) {
//					if (selector_index < show_count - 1) {
//						moveSelector(false);
//						listLayout();
//					} else {
//						doSpecialAnim(true);
//						current_index = calCurrentIndex(false, false);
//						moveSelector(false);
//						listLayout();
//						animUp(-1);
//					}
//				} else {
//					doSpecialAnim(true);
//					current_index = calCurrentIndex(false, false);
//					moveSelector(false);
//					listLayout();
//					animUp(-1);
//				}
//				current_state = STATE_OF_VISIBLE;
//				if (focus_change_listener != null)
//					doChangeOperation();
//			}
//			return true;
//		case KeyEvent.KEYCODE_DPAD_UP:
//			if (!circle_flag) {
//				if (this.getCurrentIndex() == 0) {
//					return true;
//				}
//			}
//			if (data_count > 1 && isMoveAble()) {
//				this.clearAnim();
//				if (move_flag) {
//					if (selector_index > 0) {
//						moveSelector(true);
//						listLayout();
//					} else {
//						doSpecialAnim(false);
//						current_index = calCurrentIndex(true, false);
//						moveSelector(true);
//						listLayout();
//						animDown(-1);
//					}
//				} else {
//					doSpecialAnim(false);
//					current_index = calCurrentIndex(true, false);
//					moveSelector(true);
//					listLayout();
//					animDown(-1);
//				}
//				current_state = STATE_OF_VISIBLE;
//				if (focus_change_listener != null)
//					doChangeOperation();
//			}
//			return true;
//
//		case KeyEvent.KEYCODE_DPAD_CENTER:
//			if (enter_listener != null) {
//				enter_listener.onEnter(this.getCurrentIndex());
//				return true;
//			}
//			break;
//		case 92:
//			setCurrentIndex(calCurrentIndex(true, true));
//			if (focus_change_listener != null)
//				doChangeOperation();
//			return true;
//		case 93:
//			setCurrentIndex(calCurrentIndex(false, true));
//			if (focus_change_listener != null)
//				doChangeOperation();
//			return true;
//		}
//		return true;
//	}

	public void testAnimUp() {
		if (data_count > 1) {
			if (move_flag && selector_index > 0) {
				moveSelector(true);
				listLayout();
			} else {
				doSpecialAnim(true);
				current_index = calCurrentIndex(false, false);
				moveSelector(true);
				listLayout();
				animUp(-1);
			}
			if (focus_change_listener != null)
				doChangeOperation();
		}
	}

	public void testAnimDown() {

		if (data_count > 1) {
			if (move_flag && (selector_index < show_count - 1)) {
				moveSelector(false);
				listLayout();
			} else {
				doSpecialAnim(false);
				current_index = calCurrentIndex(true, false);
				moveSelector(false);
				listLayout();
				animDown(-1);
			}

			if (focus_change_listener != null)
				doChangeOperation();
		}
	}

	public void doSpecialAnim(boolean isFirst) {
		selector_view.setVisibility(View.VISIBLE);

		AnimationSet sset = new AnimationSet(true);
		int si = 0;
		int sj = 0;
		if (!isFirst) {
			si = show_count - 1;
		} else {
			si = 0;
		}
		if ((current_index + si - center_index) < 0) {
			sj = data_count + current_index + si - center_index;
		} else {
			sj = (current_index + si - center_index) % data_count;
		}
		tmpView = getListView(sj, null);
		tmpView.setVisibility(View.VISIBLE);
		tmpView.setLayoutParams(getLayoutParams(tmpView, si));
		Animation animation = new AlphaAnimation(1.0f, 0.0f);
		animation.setDuration(defaultAnimTime);
		sset.addAnimation(animation);
		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				isFirst ? -0.05f : 0.05f, Animation.RELATIVE_TO_SELF,
				isFirst ? -1.0f : 1.0f, Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f);
		animation.setDuration(defaultAnimTime);
		sset.addAnimation(animation);
		sset.setInterpolator(new DecelerateInterpolator());
		sset.setFillAfter(true);
		tmpView.startAnimation(sset);
	}

	public void animIn() {
		if (!isValid()) {
			Log.e(TAG, "invalid");
			return;
		}
		clearAnim();
		selector_view.setVisibility(View.INVISIBLE);
		for (int i = 0; i < show_count; i++) {
			views.get(i).setVisibility(View.INVISIBLE);
		}
		current_state = STATE_OF_IN_ANIMING;
		mhandler.sendEmptyMessage(2);
	}

	public void animOut() {
		if (!isValid()) {
			Log.e(TAG, "invalid");
			return;
		}
		clearAnim();
		if (selector_view != null)
			selector_view.setVisibility(View.INVISIBLE);
		for (int i = 0; i < show_count; i++) {
			views.get(i).setVisibility(View.VISIBLE);
		}
		current_state = STATE_OF_OUT_ANIMING;

		mhandler.sendEmptyMessage(3);
	}

	public void setInVisible() {
		clearAnim();
		selector_view.setVisibility(View.INVISIBLE);
		for (int i = 0; i < show_count; i++) {
			views.get(i).setVisibility(View.INVISIBLE);
		}
	}

	public void animUp(int mils) {
		int animTime = mils < 0 ? defaultAnimTime : mils;

		for (int i = 0; i < show_count; i++) {
			AnimationSet set = new AnimationSet(true);
			Animation animation = null;
			if (i == show_count - 1) {
				animation = new AlphaAnimation(0.5f, 1.0f);
				animation.setDuration(animTime);
				set.addAnimation(animation);
			} else {
				animation = new AlphaAnimation(1.0f, 1.0f);
				animation.setDuration(animTime);
				set.addAnimation(animation);
			}
			if (zoom_flag) {
				if (i == selector_index) {
					animation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1,
							Animation.RELATIVE_TO_SELF, 0f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					animation.setDuration(animTime);
					set.addAnimation(animation);
				} else if (i == selector_index + 1) {
					animation = new ScaleAnimation(1.2f, 1.0f, 1.2f, 1,
							Animation.RELATIVE_TO_SELF, 0f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					animation.setDuration(animTime);
					set.addAnimation(animation);
				} else if (i == selector_index + 1) {

				} else {

				}
			}
			animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
					1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f);
			animation.setDuration(animTime);
			set.addAnimation(animation);
			set.setInterpolator(new DecelerateInterpolator());

			set.setFillAfter(true);
			views.get(i).startAnimation(set);
		}
	}

	public void animDown(int mils) {
		int animTime = mils < 0 ? defaultAnimTime : mils;

		for (int i = 0; i < show_count; i++) {
			AnimationSet set = new AnimationSet(true);
			Animation animation = null;
			if (i == 0) {
				animation = new AlphaAnimation(0.5f, 1.0f);
				animation.setDuration(animTime);
				set.addAnimation(animation);
			} else {
				animation = new AlphaAnimation(1.0f, 1.0f);
				animation.setDuration(animTime);
				set.addAnimation(animation);
			}
			if (zoom_flag) {
				if (i == selector_index) {
					animation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1,
							Animation.RELATIVE_TO_SELF, 0f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					animation.setDuration(animTime);
					set.addAnimation(animation);
				} else if (i == selector_index + 1) {
				} else if (i == selector_index + 1) {
					animation = new ScaleAnimation(1.2f, 1.0f, 1.2f, 1,
							Animation.RELATIVE_TO_SELF, 0f,
							Animation.RELATIVE_TO_SELF, 0.5f);
					animation.setDuration(animTime);
					set.addAnimation(animation);
				} else {

				}
			}
			animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
					-1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f);
			animation.setDuration(animTime);
			set.addAnimation(animation);
			set.setInterpolator(new DecelerateInterpolator());

			set.setFillAfter(true);
			views.get(i).startAnimation(set);
		}
	}

	public void focus() {
		focus_flag = true;
		invalidateSelector();
		this.requestFocus();
	}

	public void blur() {
		focus_flag = false;
		invalidateSelector();
	}

	public void moveSelector(boolean up) {
		if (move_flag) {
			if (up) {
				selector_index--;
			} else {
				selector_index++;
			}
			if (selector_index < 0) {
				selector_index = 0;
			}
			if (selector_index > show_count - 1) {
				selector_index = show_count - 1;
			}
			selector_view.setLayoutParams(getLayoutParams(selector_view,
					selector_index));
		}
	}

	public void moveSelector(int value) {
		if (move_flag) {
			selector_index = value;
			while (selector_index < 0) {
				selector_index += show_count;
			}
			selector_index = selector_index % show_count;
			selector_view.setLayoutParams(getLayoutParams(selector_view,
					selector_index));
		}
	}

	public void clearAnim() {
		in_anim = 0;
		mhandler.removeMessages(2);
		out_anim = 0;
		mhandler.removeMessages(3);
		if (tmpView != null) {
			tmpView.clearAnimation();
			tmpView.setVisibility(View.INVISIBLE);
		}
		for (View view : views) {
			view.clearAnimation();
		}
		this.clearAnimation();
		current_state = STATE_OF_NORMAL;
	}

	public void listLayout() {
		for (int i = 0; i < show_count; i++) {
			int j = 0;
			if ((current_index + i - center_index) < 0) {
				j = data_count + current_index + i - center_index;
			} else {
				j = (current_index + i - center_index) % data_count;
			}
			View tmp = getListView(j, views.get(i));
			this.requestLayout();
			tmp.setLayoutParams(getLayoutParams(tmp, i, j));

		}
	}

	public int calCurrentIndex(boolean up, boolean page) {
		int result = 0;
		if (page) {
			if (up) {
				result = (current_index - show_count);
			} else {
				result = (current_index + show_count);
			}
		} else {
			if (up) {
				result = (current_index - 1);
			} else {
				result = (current_index + 1);
			}
		}
		while (result < 0) {
			result += data_count;
		}
		result = result % data_count;
		return result;
	}

	public void setCurrentIndex(int current) {
		if (current > data_count - 1) {
			current %= data_count;
		}
		if (current_index != current) {
			current_index = current;
		}
		invalidateAll();
	}
	public void setCurrentItem(int current) {
		if (current > data_count - 1) {
			current %= data_count;
		}
		selector_index=current;
		invalidateAll();
		if (focus_change_listener != null)
			doChangeOperation();
	}
	public void setCurrentIndexAndSelector(int current) {
		if (current > data_count - 1) {
			current %= data_count;
		}
		if (current_index != current) {
			current_index = current;
		}
		selector_index = center_index = current;
		this.moveSelector(current_index % show_count);

		invalidateAll();
	}

	public void invalidateAll() {
		listLayout();
		invalidateSelector();
	}

	public void invalidateSelector() {
		if (selector_view == null)
			return;
		if (focus_flag) {
			selector_view.setBackgroundResource(selector_res);
		} else
			selector_view.setBackgroundDrawable(null);
	}

	public int getCurrentIndex() {
		if (move_flag) {
			int tmp = current_index + selector_index - center_index;
			while (tmp < 0) {
				tmp += data_count;
			}
			return tmp % data_count;
		} else {
			return current_index;
		}
	}

	public void doChangeOperation() {
		mhandler.removeMessages(1);
		mhandler.sendEmptyMessageDelayed(1, defaultAnimDelay);
	}

	public View getConvertView() {
		if (item_resourceId > 0)
			return LayoutInflater.from(context).inflate(item_resourceId, null);
		else {
			TextView tv = new TextView(context);
			tv.setTextSize(font_scale);
			tv.setGravity(Gravity.CENTER);
			tv.setPadding(padding, 0, padding, 0);
			if(layout_flag){
				tv.setLayoutParams(new LayoutParams(list_item_width, list_item_height));	
			}else{
				tv.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, list_item_height));
			}
			return tv;
		}
	}

	public RelativeLayout.LayoutParams getLayoutParams(View view, int index) {
		RelativeLayout.LayoutParams p = null;
		if (view == null) {
			p = new RelativeLayout.LayoutParams(list_item_width,
					list_item_height);
		} else {
			p = (LayoutParams) view.getLayoutParams();
		}
		p.leftMargin = getLeftMargin(index);
		p.topMargin = 0;
		return p;
	}

	public RelativeLayout.LayoutParams getLayoutParams(View view, int index,
			int location) {
		RelativeLayout.LayoutParams p = null;
		if (view == null) {
			p = new RelativeLayout.LayoutParams(list_item_width,
					list_item_height);
		} else {
			p = (LayoutParams) view.getLayoutParams();
		}
		//p.leftMargin = getLeftMargin(index, location);
		p.leftMargin = 70;
		p.topMargin = getTopMargin(index, location);
		return p;
	}

	private int getLeftMargin(int index) {
		int left_margin = 0;
		Paint p = new Paint();
		p.setTextSize(font_scale);
		for (int i = 0; i < index; i++) {
			left_margin += 0;
		}
		return left_margin;
	}

	private int getLeftMargin(int index, int location) {
		if(layout_flag){
			return index * list_item_width + index * spacing;
		}
		int left_margin = 0;
		Paint p = new Paint();
		p.setTextSize(font_scale);

		for (int i = 0; i < index; i++) {
			int l = location - index + i;
			if (l < 0) {
				l += data_count;
			}
			l %= data_count;
			TextView tv=(TextView)(views.get(i));
			int text_width=(int) p.measureText(tv.getText()+"");
			left_margin += text_width+ padding * 2 + spacing;
		}
		return left_margin;
	}
	
	private int getTopMargin(int index, int location) {
		return index * list_item_width + index * padding;
		
	}

	public boolean isValid() {
		return current_state >= 0;
	}

	public boolean isMoveAble() {
		return current_state == STATE_OF_VISIBLE;
	}

	public boolean isAniming() {
		return current_state == STATE_OF_IN_ANIMING
				|| current_state == STATE_OF_OUT_ANIMING;
	}

	public int getCurrentState() {
		return current_state;
	}

	public void loadingData() {
		current_state = STATE_OF_LOADING;
		this.setVisibility(View.INVISIBLE);
		mhandler.removeMessages(5);
		mhandler.sendEmptyMessageDelayed(5, 10000);
		if (loading_listener != null) {
			loading_listener.onLoading();
		}
	}

	public void noticyLoadingComplete(Object o) {
		Message msg = mhandler.obtainMessage();
		msg.obj = o;
		msg.what = 4;
		mhandler.sendMessage(msg);
	}

	public void resetList() {
		selector_index = 0;
		current_index = 0;
		if (defaultCenter < 0)
			center_index = show_count % 2 == 0 ? (show_count / 2 - 1)
					: (show_count / 2);
		else
			center_index = defaultCenter;
		selector_index = center_index;
		selector_view.setLayoutParams(getLayoutParams(selector_view,
				selector_index));
		this.invalidateAll();
	}

	public void setListFocusChangeListener(ListFocusChangeListener listener) {
		this.focus_change_listener = listener;
	}

	public void setListPreFocusChangeListener(
			ListPreFocusChangeListener listener) {
		this.pre_focus_change_listener = listener;
	}

	public void setEnteristener(ListOnEnterListener listener) {
		this.enter_listener = listener;
	}

	public void setAnimInStartListener(AnimInStartListener listener) {
		this.in_start_listener = listener;
	}

	public void setAnimOutStartListener(AnimOutStartListener listener) {
		this.out_start_listener = listener;
	}

	public void setAnimInEndListener(AnimInEndListener listener) {
		this.in_end_listener = listener;
	}

	public void setAnimOutEndListener(AnimOutEndListener listener) {
		this.out_end_listener = listener;
	}

	public void SetListDataEmptyListener(ListDataEmptyListener listener) {
		this.empty_listener = listener;
	}

	public void setListDataLoadingListener(ListDataLoadingListener listener) {
		this.loading_listener = listener;
	}

	public void setListKeyPressgListener(
			ListKeyPressgListener listKeyPressgListener) {
		this.press_listener = listKeyPressgListener;
	}

	public void setListDataLoadingCallBack(ListDataLoadingCallBack listener) {
		this.loading_callback = listener;
	}

	public void setListDataLoadTimeUp(ListDataLoadTimeUp listener) {
		this.time_up_listener = listener;
	}

}
