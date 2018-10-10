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
 * 媒体输入端子播放器
 * <p>
 * 用于实现播放各种输入接口的显示播放,比如PC，DVD,模拟电视等
 * 
 * @hide
 */
public class InportPlayer {
	static final String TAG = "[java]InportPlayer";

	/** 输入端 - 不可用 */
	public static final long INPORT_INVALID = 0;
	/** 输入端 - 射频(模拟) */
	public static final long INPORT_RF = 0x1;
	/** 输入端 - HDMI 接口 */
	public static final long INPORT_HDMI = 0x2;
	/** 输入端 - HDMI 接口 2 */
	public static final long INPORT_HDMI2 = 0x4;
	/** 输入端 - HDMI 接口 3 */
	public static final long INPORT_HDMI3 = 0x8;
	/** 输入端 - HDMI 接口 4 */
	public static final long INPORT_HDMI4 = 0x10;
	/** 输入端 - RCA AV(红白黄) 接口 */
	public static final long INPORT_RCA_AV = 0x20;
	/** 输入端 - RCA AV(红白黄) 接口 2 */
	public static final long INPORT_RCA_AV2 = 0x40;
	/** 输入端 - RCA 视频分量 YPbPr (红绿蓝) 接口 */
	public static final long INPORT_RCA_YPBPR = 0x80;
	/** 输入端 - RCA 视频分量 YCbCr (红绿蓝) 接口 */
	public static final long INPORT_RCA_YCBCR = 0x100;
	/** 输入端 - S 端子接口 */
	public static final long INPORT_SVIDEO = 0x200;
	/** 输入端- BNC 接口 */
	public static final long INPORT_BNC = 0x400;
	/** 输入端 - PC VGA 接口 */
	public static final long INPORT_VGA = 0x800;
	/** 输入端 - PC DVI 接口 */
	public static final long INPORT_DVI = 0x1000;
	/** 输入端 - DisplayPort 接口 */
	public static final long INPORT_DP = 0x2000;
	/** 输入端 - 数码同轴接口 */
	public static final long INPORT_COAXIAL = 0x4000;
	/** 输入端 - 数字音频接口 */
	public static final long INPORT_SPDIF = 0x8000;
	/** 输入端 - RCA 音频(红白) 接口 */
	public static final long INPORT_RCA_AUDIO = 0x10000;
	/** 输入端 - TRS 3.5 毫米 耳机接口 */
	public static final long INPORT_TRS_JACKS = 0x20000;
	/** 输入端 - 光纤音频接口 */
	public static final long INPORT_OPTICAL = 0x40000;

	/**
	 * 得到本机器支持的媒体输入接口的掩码(或值)
	 * 
	 * @return 值
	 */
	public static long getMediaInportMask() {
		return 0;
	}

	/**
	 * 创建播放器实例
	 * 
	 * @param ctx
	 *            上下文
	 * @return 对象
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
	 * @deprecated 请勿调用
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
	 * 释放对象资源
	 */
	public void release() {
		native_close();
	}

	void checkPeer() {
		if (peer == 0)
			throw new IllegalStateException("not reserve!");
	}

	/**
	 * 启动
	 */
	public boolean start() {
		synchronized (mutex) {
			checkPeer();
			return native_start();
		}
	}

	/**
	 * 停止
	 */
	public void stop() {
		synchronized (mutex) {
			checkPeer();
			native_stop();
		}
	}

	/**
	 * 设置绑定的Surface 起始位置为(0,0)
	 * 
	 * @param holder
	 *            一般从SurfaceView中获取
	 * @return 成功返回true,否则返回false
	 */
	public boolean setDisplay(SurfaceHolder holder) {
		synchronized (mutex) {
			checkPeer();
			return setDisplay(holder, 0, 0);
		}
	}

	/**
	 * 设置绑定的Surface
	 * 
	 * @param holder
	 *            一般从SurfaceView中获取
	 * @param x
	 *            起始位置X
	 * @param y
	 *            起始位置Y
	 * @return 成功返回true,否则返回false
	 */
	public boolean setDisplay(SurfaceHolder holder, int x, int y) {
		synchronized (mHolderMutex) {
			if (holder == null) { // holder为null时执行release
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
	 * 设置位置
	 * <p>
	 * 如果设置过SurfaceHolder作为Display,那么此操作将是临时性的，<br>
	 * 会因为SurfaceHolder的位置和大小的变化被再次设置
	 * 
	 * @param x
	 *            起始位置X
	 * @param y
	 *            起始位置Y
	 * @param width
	 *            视频播放宽度
	 * @param height
	 *            视频播放高度
	 * @return 成功返回true,否则返回false
	 */
	public boolean setDisplay(int x, int y, int width, int height) {
		synchronized (mHolderMutex) {
			mx = x;
			my = y;
			return native_set_display(x, y, width < 0 ? 0 : width, height < 0 ? 0 : height);
		}
	}

	/**
	 * 设置信源
	 * 
	 * @param id
	 *            信源ID ，参考{@link #INPORT_RF}等等
	 * @param flags
	 *            标志 ，默认为0
	 */
	public boolean setSource(int id) {
		synchronized (mutex) {
			checkPeer();
			return setSource(id, null, 0);
		}
	}

	/**
	 * 设置信源
	 * 
	 * @param id
	 *            信源ID ，参考{@link #INPORT_RF}等等
	 * @param uri
	 *            信源参数uri
	 * @param flags
	 *            标志 ，默认为0
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
