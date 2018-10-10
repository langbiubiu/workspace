package com.ipanel.join.chongqing.live;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ipanel.chongqing_ipanelforhw.R;

public class InfoManager {
	public static final int DIRECTION_CENTER_AND_CENTER = 0;
	public static final int DIRECTION_CENTER_AND_BOTTOM = 1;
	public static final int DIRECTION_RIGHT_AND_BOTTOM = 2;
	public static final int DIRECTION_DEFAULT = 3;
	private static InfoManager mInstance;
	private Context mContext;
	private Toast toast;
	private Field field;
	private Object obj;
	private float mDensity;
	private Method showMethod, hideMethod;
	private Handler mhandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				toast.show();
				mhandler.sendEmptyMessageDelayed(1, Constant.DEFAULT_TOAST_SHOW_TIME);
				break;
			case 1:
				// if
				// (!Configuration.getInstance(mContext).isWidgetTipsEnable()) {
				// Configuration.getInstance(mContext).setWidgetTipsEnable(
				// true);
				// Log.i("show search info ");
				// }
				toast.cancel();
				break;
			default:
				break;
			}
		}
	};

	public static synchronized InfoManager getInstance(Context mContext) {
		if (mInstance == null)
			mInstance = new InfoManager(mContext);
		return mInstance;
	}

	private InfoManager(Context mContext) {
		this.mContext = mContext;
		if (mContext.getResources().getDisplayMetrics().heightPixels > 720) {
			mDensity = 1.5f;
		} else {
			mDensity = 1.0f;
		}
		toast = new Toast(mContext);
		toast.setDuration(Constant.DEFAULT_TOAST_SHOW_TIME);
		toast.setDuration(Toast.LENGTH_LONG);
//		reflectionTN();
//		hide();
	}

	public void showDefaultDialog(int msgRec) {
		showDialog(R.layout.live_toast_view, msgRec, 0);
	}

	public void showDefaultDialog(int msgRec, int direction) {
		showDialog(R.layout.live_toast_view, msgRec, direction);
	}

	public void showDialog(int viewRec, int msgRec, int direction) {
		View v = View.inflate(mContext, viewRec, null);
		TextView mShowInfo = (TextView) v.findViewWithTag("msg");
		if (mShowInfo != null && msgRec > 0) {
			mShowInfo.setText(msgRec);
		}
		toast.setView(v);
		switch (direction) {
		case 0:// center center
			toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, (int) (193*mDensity));
			break;
		case 1:// center botton
			toast.setGravity(Gravity.FILL_HORIZONTAL, 0, (int) (295*mDensity));
			break;
		case 2:// right botton
			toast.setGravity(Gravity.FILL_HORIZONTAL, (int)(500*mDensity), (int) (295*mDensity));
			break;
		default:
			toast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
			break;
		}
		sendMessage();
	}

	public void showDefaultDialog(String msg) {
		showDialog(R.layout.live_toast_view, msg, 0);
	}

	public void showDefaultDialog(String msg, int direction) {
		showDialog(R.layout.live_toast_view, msg, direction);
	}

	@SuppressLint("NewApi")
	public void showDialog(int viewRec, String msg, int direction) {
		View v = View.inflate(mContext, viewRec, null);
		TextView mShowInfo = (TextView) v.findViewWithTag("msg");
		if (mShowInfo != null && !msg.isEmpty()) {
			mShowInfo.setText(msg);
		}
		toast.setView(v);
		switch (direction) {
		case 0:// center center
			toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, (int) (193*mDensity));
			break;
		case 1:// center botton
			toast.setGravity(Gravity.FILL_HORIZONTAL, 0, (int) (295*mDensity));
			break;
		case 2:// right botton
			toast.setGravity(Gravity.FILL_HORIZONTAL, (int)(500*mDensity), (int) (295*mDensity));
			break;
		default:
			toast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
			break;
		}
		sendMessage();
	}

	public void sendMessage() {
		mhandler.removeMessages(1);
		// if (Configuration.getInstance(mContext).isWidgetTipsEnable()) {
		// Configuration.getInstance(mContext).setWidgetTipsEnable(false);
		// Log.i("hide search info ");
		// }
		toast.show();
		mhandler.sendEmptyMessageDelayed(1, 1500);
	}

//	private void reflectionTN() {
//		try {
//			field = toast.getClass().getDeclaredField("mTN");
//			field.setAccessible(true);
//			obj = field.get(toast);
//			showMethod = obj.getClass().getDeclaredMethod("show", null);
//			hideMethod = obj.getClass().getDeclaredMethod("hide", null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void show() {
//		try {
//			showMethod.invoke(obj, null);
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}// 调用TN对象的show()方法，显示toast
//	}
//
//	public void hide() {
//		try {
//			hideMethod.invoke(obj, null);
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}// 调用TN对象的hide()方法，关闭toast
//	}
}
