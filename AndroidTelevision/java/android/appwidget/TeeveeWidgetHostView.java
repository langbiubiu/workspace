package android.appwidget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RemoteViews;

/**
 * �������������ͼ
 */
public class TeeveeWidgetHostView extends AppWidgetHostView {
	static final String TAG = "[java]TeeveeWidgetHostView";

	/** ��Ƶ�ɰ�-Ĭ�ϲ���ʾ */
	public static final int MASK_FLAG_DEFAULT = 0;
	/** ��Ƶ�ɰ�-����Ƶ���ʱ��ʾ(����Ƶ) */
	public static final int MASK_FLAG_NO_VIDEO = 0x1;
	/** ��Ƶ�ɰ�-���Ŵ���ʱ��ʾ */
	public static final int MASK_FLAG_PLAY_ERROR = 0x2;
	/** ��Ƶ�ɰ�-�л�ʱʱ��ʾ */
	public static final int MASK_FLAG_PLAY_SWITCH = 0x4;

	static final String EXTRA_TO_CH = "ch_ch";
	static final String EXTRA_VB = "set_vb";
	static final String EXTRA_VO = "set_vo";
	static final String EXTRA_STOP = "stop_play";
	static final String SCHEME_CH = "channel://";

	private String name = null;
	private SurfaceView mSurfaceView = null;
	private boolean surfaceAdded = false;
	volatile boolean autoVideoBounds = false;
	private Rect vbounds = new Rect();
	private Object intentMutex = new Object();
	private Intent toSend = null;
	private SparseArray<ImageView> mask = new SparseArray<ImageView>();
	private Handler handler = new InnerHandler(this);

	/**
	 * �������
	 * 
	 * @param context
	 *            ������
	 */
	public TeeveeWidgetHostView(Context context) {
		super(context);
	}

	/**
	 * ����ת����һ��Ƶ��
	 */
	public void toNextChannel() {
		toChannel("channel://next");
	}

	/**
	 * ����ת��ǰһ��Ƶ��
	 */
	public void toPrevChannel() {
		toChannel("channel://prev");
	}

	/**
	 * ����ת��ָ��Ƶ��
	 * 
	 * @param number
	 *            Ƶ����
	 */
	public void toChannel(int number) {
		if (number < 0)
			throw new IllegalArgumentException();
		toChannel("channel://" + number);
	}
	
	/**
	 * ����ת��ָ��Ƶ��
	 * 
	 * @param channelId
	 *            ȫ��Ƶ��id
	 * @hide ר�к���,����ʹ��
	 * @Deprecated
	 */
	public void toChannelById(String channelId) {
		if (channelId == null)
			throw new IllegalArgumentException();
		toChannel("channelId://" + channelId);
	}

	/**
	 * ����ת��ָ�����Ƶ�Ƶ��
	 * 
	 * @param uri
	 *            Ƶ��uri
	 */
	public void toChannel(String uri) {
		if (name == null)
			throw new NullPointerException();
		synchronized (intentMutex) {
			if (toSend == null)
				toSend = new Intent(name);
			toSend.putExtra(EXTRA_TO_CH, uri);
			handler.sendEmptyMessage(1);
		}
	}

	/**
	 * ������Ƶλ����view�Զ�����
	 */
	public void setAutoVideoBounds() {
		Rect r = new Rect();
		getGlobalVisibleRect(r);
		synchronized (intentMutex) {
			autoVideoBounds = true;
			// ȡ�������������Ļ�����ꡣ��Ҫ��Ϊ��Ƶ�Ŀ�͸�
			r.right = r.right - r.left + 1;
			r.bottom = r.bottom - r.top + 1;
			r.left++;
			r.top++;
			doSetVideoBoundsRect(r);
		}
	}

	/**
	 * ������Ƶλ��,�������Ļ��
	 * 
	 * @param r
	 *            ��������
	 */
	public void setVideoBounds(Rect r) {
		synchronized (intentMutex) {
			autoVideoBounds = false;
			r.right = r.right - r.left + 1;
			r.bottom = r.bottom - r.top + 1;
			r.left++;
			r.top++;
			doSetVideoBoundsRect(r);
		}
	}

	/**
	 * ֹͣ����
	 */
	public void stop() {
		synchronized (intentMutex) {
			if (toSend == null)
				toSend = new Intent(name);
			toSend.putExtra(EXTRA_STOP, "stop");
			handler.sendEmptyMessage(1);
		}
	}

	/**
	 * ��������
	 * 
	 * @param v
	 *            0-1.0f ֮��
	 */
	public void setVolume(float v) {
		synchronized (intentMutex) {
			if (toSend == null)
				toSend = new Intent(name);
			toSend.putExtra(EXTRA_VO, v);
			handler.sendEmptyMessage(1);
		}
	}

	/**
	 * ������Ƶ�ɰ�Ϊָ���ɻ������
	 * <p>
	 * Ĭ��ΪMASK_FLAG_NO_VIDEO
	 * 
	 * @param d
	 *            ͼƬ
	 */
	public void setVideoMask(Drawable d) {
		setVideoMask(d, MASK_FLAG_NO_VIDEO);
	}

	/**
	 * ������Ƶ�ɰ�Ϊָ���ɻ������
	 * 
	 * @param d
	 *            ͼƬ
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 */
	public void setVideoMask(Drawable d, int flags) {
		synchronized (mask) {
			ImageView v = null;
			if ((flags & MASK_FLAG_NO_VIDEO) != 0)
				if ((v = mask.get(MASK_FLAG_NO_VIDEO)) != null) {
					v.setImageDrawable(d);
					v.setVisibility(View.VISIBLE);
					v.setScaleType(ImageView.ScaleType.FIT_XY);
				}
			if ((flags & MASK_FLAG_PLAY_ERROR) != 0)
				if ((v = mask.get(MASK_FLAG_PLAY_ERROR)) != null) {
					v.setImageDrawable(d);
					v.setScaleType(ImageView.ScaleType.FIT_XY);
				}
			if ((flags & MASK_FLAG_PLAY_SWITCH) != 0)
				if ((v = mask.get(MASK_FLAG_PLAY_SWITCH)) != null) {
					v.setImageDrawable(d);
					v.setScaleType(ImageView.ScaleType.FIT_XY);
				}
		}
	}

	/**
	 * ���������Ƶ�ɰ�
	 */
	public void clearVideoMask() {
		synchronized (mask) {
			int n = mask.size();
			for (int i = 0; i < n; i++) {
				ImageView v = mask.valueAt(i);
				v.setImageDrawable(null);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed && autoVideoBounds) {
			getGlobalVisibleRect(vbounds);
			synchronized (intentMutex) {
				doSetVideoBoundsRect(vbounds);
			}
		}
	}

	@Override
	public void addView(View child) {
		if (!surfaceAdded) {
			surfaceAdded = true;
			mSurfaceView = new SurfaceView(getContext());
			super.addView(mSurfaceView);
		}
		if (child instanceof ViewGroup)
			initVideoMask((ViewGroup) child);
		super.addView(child);
	}

	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		super.setPadding(0, 0, 0, 0);
	}

	@Override
	public void updateAppWidget(RemoteViews remoteViews) {
		super.updateAppWidget(remoteViews);
		Log.d(TAG, "updateAppWidget");
		ImageView v = mask.get(MASK_FLAG_NO_VIDEO);
		Log.d(TAG, "updateAppWidget v = " + v);
		if (v != null) {
			int i = v.getVisibility();
			Log.d(TAG, "updateAppWidget i = " + i);
		}
		if (l != null) {
			Log.d(TAG, "updateAppWidget l.getVisibility() = " + l.getVisibility());
		}
	}

	/**
	 * �ǹ��������������
	 * 
	 * @hide
	 */
	protected SurfaceHolder getSurfaceHolder() {
		if (mSurfaceView == null)
			return null;
		return mSurfaceView.getHolder();
	}

	void setProviderName(String name) {
		this.name = name;
	}

	FrameLayout l = null;

	void initVideoMask(ViewGroup root) {
		int n = root.getChildCount();
		for (int i = 0; i < n; i++) {
			View v = root.getChildAt(i);
			if (v instanceof FrameLayout) {
				FrameLayout layout = (FrameLayout) v;
				Object otag = layout.getTag();
				if (otag != null) {
					String tag = otag.toString();
					ImageView imgv = null;
					if (tag.equals(TeeveeWidgetProvider.MASK_LAYOUT_TAG_NO_VIDEO)) {
						imgv = initMaskImageView(MASK_FLAG_NO_VIDEO);
					} else if (tag.equals(TeeveeWidgetProvider.MASK_LAYOUT_TAG_PLAY_ERROR)) {
						imgv = initMaskImageView(MASK_FLAG_PLAY_ERROR);
					} else if (tag.equals(TeeveeWidgetProvider.MASK_LAYOUT_TAG_PLAY_SWITCH)) {
						imgv = initMaskImageView(MASK_FLAG_PLAY_SWITCH);
					}
					l = layout;
					if (imgv != null)
						layout.addView(imgv);
				}
			}
		}
	}

	ImageView initMaskImageView(int v) {
		ImageView ret = new ImageView(getContext());
		switch (v) {
		case MASK_FLAG_NO_VIDEO:
		case MASK_FLAG_PLAY_ERROR:
		case MASK_FLAG_PLAY_SWITCH:
			break;
		default:
			return null;
		}
		mask.put(v, ret);
		return ret;
	}

	void doSetVideoBoundsRect(Rect r) {
		if (toSend == null)
			toSend = new Intent(name);
		toSend.putExtra(EXTRA_VB, new Rect(r));
		handler.sendEmptyMessage(1);
	}

	void sendIntent() {
		Intent i = null;
		synchronized (intentMutex) {
			i = toSend;
			toSend = null;
		}
		if (i != null)
			getContext().sendBroadcast(i);
	}

	static class InnerHandler extends Handler {
		WeakReference<TeeveeWidgetHostView> o;

		InnerHandler(TeeveeWidgetHostView h) {
			super();
			o = new WeakReference<TeeveeWidgetHostView>(h);
		}

		public void dispatchMessage(android.os.Message msg) {
			TeeveeWidgetHostView v = o.get();
			if (v != null) {
				switch (msg.what) {
				case 1:
					v.sendIntent();
					break;
				default:
					break;
				}
			}
		}
	}

}
