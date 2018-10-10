package android.media;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.telecast.TransportManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * ý��������Ӳ�����
 * <p>
 * ����ʵ�ֲ��Ÿ�������ӿڵ���ʾ����,����PC��DVD,ģ����ӵ�
 * 
 * @hide
 */
public class InportPlayer {
	static final String TAG = "[java]InportPlayer";

	/** ����� - ������ */
	public static final long INPORT_INVALID = 0;
	/** ����� - ��Ƶ(ģ��) */
	public static final long INPORT_RF = 0x1;
	/** ����� - HDMI �ӿ� */
	public static final long INPORT_HDMI = 0x2;
	/** ����� - HDMI �ӿ� 2 */
	public static final long INPORT_HDMI2 = 0x4;
	/** ����� - HDMI �ӿ� 3 */
	public static final long INPORT_HDMI3 = 0x8;
	/** ����� - HDMI �ӿ� 4 */
	public static final long INPORT_HDMI4 = 0x10;
	/** ����� - RCA AV(��׻�) �ӿ� */
	public static final long INPORT_RCA_AV = 0x20;
	/** ����� - RCA AV(��׻�) �ӿ� 2 */
	public static final long INPORT_RCA_AV2 = 0x40;
	/** ����� - RCA ��Ƶ���� YPbPr (������) �ӿ� */
	public static final long INPORT_RCA_YPBPR = 0x80;
	/** ����� - RCA ��Ƶ���� YCbCr (������) �ӿ� */
	public static final long INPORT_RCA_YCBCR = 0x100;
	/** ����� - S ���ӽӿ� */
	public static final long INPORT_SVIDEO = 0x200;
	/** �����- BNC �ӿ� */
	public static final long INPORT_BNC = 0x400;
	/** ����� - PC VGA �ӿ� */
	public static final long INPORT_VGA = 0x800;
	/** ����� - PC DVI �ӿ� */
	public static final long INPORT_DVI = 0x1000;
	/** ����� - DisplayPort �ӿ� */
	public static final long INPORT_DP = 0x2000;
	/** ����� - ����ͬ��ӿ� */
	public static final long INPORT_COAXIAL = 0x4000;
	/** ����� - ������Ƶ�ӿ� */
	public static final long INPORT_SPDIF = 0x8000;
	/** ����� - RCA ��Ƶ(���) �ӿ� */
	public static final long INPORT_RCA_AUDIO = 0x10000;
	/** ����� - TRS 3.5 ���� �����ӿ� */
	public static final long INPORT_TRS_JACKS = 0x20000;
	/** ����� - ������Ƶ�ӿ� */
	public static final long INPORT_OPTICAL = 0x40000;

	/**
	 * �õ�������֧�ֵ�ý������ӿڵ�����(��ֵ)
	 * 
	 * @return ֵ
	 */
	public static long getMediaInportMask() {
		return 0;
	}

	/**
	 * ����������ʵ��
	 * 
	 * @param ctx
	 *            ������
	 * @return ����
	 */
	public static InportPlayer createInportPlayer(Context ctx, int flags) {
		Log.d(TAG, "create InportPlayer");
		return null;// TODO
	}

	private Object mutex = new Object();
	private int peer = 0;
	private int mx = 0, my = 0, mw = 0, mh = 0;
	private SurfaceHolder display;
	private final Object mHolderMutex = new Object();

	/**
	 * @hide
	 * @deprecated �������
	 */
	public InportPlayer(Context ctx, int flags) {
		boolean succ = native_open(new WeakReference<InportPlayer>(this), flags);
		if (!succ || peer == 0) {
			throw new RuntimeException();
		}
	}

	protected void finalize() throws Throwable {
		try {
			release();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.finalize();
	};

	/**
	 * �ͷŶ�����Դ
	 */
	public void release() {
		native_close();
	}

	void checkPeer() {
		if (peer == 0)
			throw new IllegalStateException("not reserve!");
	}

	/**
	 * ����
	 */
	public boolean start() {
		synchronized (mutex) {
			checkPeer();
			return native_start();
		}
	}

	/**
	 * ֹͣ
	 */
	public void stop() {
		synchronized (mutex) {
			checkPeer();
			native_stop();
		}
	}

	/**
	 * ���ð󶨵�Surface ��ʼλ��Ϊ(0,0)
	 * 
	 * @param holder
	 *            һ���SurfaceView�л�ȡ
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean setDisplay(SurfaceHolder holder) {
		synchronized (mutex) {
			checkPeer();
			return setDisplay(holder, 0, 0);
		}
	}

	/**
	 * ���ð󶨵�Surface
	 * 
	 * @param holder
	 *            һ���SurfaceView�л�ȡ
	 * @param x
	 *            ��ʼλ��X
	 * @param y
	 *            ��ʼλ��Y
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean setDisplay(SurfaceHolder holder, int x, int y) {
		synchronized (mHolderMutex) {
			if (holder == null) { // holderΪnullʱִ��release
				if (display != null)
					display.removeCallback(mSurfaceCbk);
				native_set_display(0, 0, 0, 0); // hide
				display = null;
				return true;
			}

			if (display == null) {
				display = holder;
				display.addCallback(mSurfaceCbk);
			} else if (display != holder) {
				display.removeCallback(mSurfaceCbk);
				display = holder;
				display.addCallback(mSurfaceCbk);
			}

			Surface surface = display.getSurface();
			if (surface == null)
				throw new IllegalStateException("surface not created");

			Rect r = display.getSurfaceFrame();
			mx = x;
			my = y;
			int w = r.right - r.left;
			int h = r.bottom - r.top;
			if (native_set_display(x, y, w < 0 ? 0 : w, h < 0 ? 0 : h)) {
				return true;
			}
			return false;
		}
	}

	/**
	 * ����λ��
	 * <p>
	 * ������ù�SurfaceHolder��ΪDisplay,��ô�˲���������ʱ�Եģ�<br>
	 * ����ΪSurfaceHolder��λ�úʹ�С�ı仯���ٴ�����
	 * 
	 * @param x
	 *            ��ʼλ��X
	 * @param y
	 *            ��ʼλ��Y
	 * @param width
	 *            ��Ƶ���ſ��
	 * @param height
	 *            ��Ƶ���Ÿ߶�
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean setDisplay(int x, int y, int width, int height) {
		synchronized (mHolderMutex) {
			mx = x;
			my = y;
			return native_set_display(x, y, width < 0 ? 0 : width, height < 0 ? 0 : height);
		}
	}

	/**
	 * ������Դ
	 * 
	 * @param id
	 *            ��ԴID ���ο�{@link #INPORT_RF}�ȵ�
	 * @param flags
	 *            ��־ ��Ĭ��Ϊ0
	 */
	public boolean setSource(int id) {
		synchronized (mutex) {
			checkPeer();
			return setSource(id, null, 0);
		}
	}

	/**
	 * ������Դ
	 * 
	 * @param id
	 *            ��ԴID ���ο�{@link #INPORT_RF}�ȵ�
	 * @param uri
	 *            ��Դ����uri
	 * @param flags
	 *            ��־ ��Ĭ��Ϊ0
	 */
	public boolean setSource(int id, String uri, int flags) {
		synchronized (mutex) {
			checkPeer();
			return native_setsrc(id, uri, flags);
		}
	}

	@SuppressWarnings("unused")
	private SurfaceHolder.Callback mSurfaceCbk = new SurfaceHolder.Callback() {// for
		// 2.2
		public void surfaceDestroyed(SurfaceHolder holder) {
			synchronized (mHolderMutex) {
				if (holder == display)
					display = null;
				holder.removeCallback(mSurfaceCbk);
			}
		}

		public void surfaceRedrawNeeded(SurfaceHolder holder) {
			android.graphics.Rect r = holder.getSurfaceFrame();
			int x = mx; // r.left;
			int y = my; // r.top;
			int w = r.right - r.left;
			int h = r.bottom - r.top;
			native_set_display(x, y, w, h);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			Canvas canvas = holder.lockCanvas();
			Paint painter = new Paint();
			painter.setColor(Color.TRANSPARENT);
			painter.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
			canvas.drawPaint(painter);
			holder.unlockCanvasAndPost(canvas);
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			android.graphics.Rect r = holder.getSurfaceFrame();
			int x = mx; // r.left;
			int y = my; // r.top;
			int w = r.right - r.left;
			int h = r.bottom - r.top;
			native_set_display(x, y, w, h);
		}
	};

	native boolean native_open(WeakReference<InportPlayer> wo, int flags);

	native void native_close();

	native boolean native_set_display(int x, int t, int w, int h);

	native boolean native_start();

	native void native_stop();

	native boolean native_setsrc(int id, String uri, int flags);

	@SuppressWarnings("deprecation")
	static void init() {
		TransportManager.ensure();
	}

	static {
		init();
	}
}
