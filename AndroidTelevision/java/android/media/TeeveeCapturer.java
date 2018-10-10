package android.media;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * 视频画面抓取器
 */
public class TeeveeCapturer {
	static final String TAG = "[java]TeeveeCapturer";

	/** 类型标志-默认(平台实际抓屏的数据大小) */
	public static final int TYPE_DEFAULT = 0;
	/** 类型标志-小画幅 */
	public static final int TYPE_SMALL = 1;
	/** 类型标志-中画幅 */
	public static final int TYPE_MEDIUM = 2;
	/** 类型标志-大画幅 */
	public static final int TYPE_LARGE = 3;
	/** 类型标志-全画幅 */
	public static final int TYPE_FULL = 4;

	/** 创建标志-默认(平台默认抓屏的数据格式) */
	public static final int FLAG_DEFAULT = 0;
	/** 创建标志-偏好RGB数据 */
	public static final int FLAG_PREPERRED_RGB = 0x01;
	/** 创建标志-偏好YUV数据 */
	public static final int FLAG_PREPERRED_YUV = 0x02;
	/** 创建标志-偏好JPG编码数据 */
	public static final int FLAG_PREPERRED_JPG = 0x04;
	/** 创建标志-偏好PNG编码数据 */
	public static final int FLAG_PREPERRED_PNG = 0x08;

	/**
	 * 创建视频帧抓取器
	 * 
	 * @param context
	 *            上下文
	 * @param type
	 *            类型
	 * @return 对象，如果失败返回null
	 */
	public static TeeveeCapturer createInstance(Context context, int type) {
		return createInstance(context, type, 0);
	}

	/**
	 * 创建视频帧抓取器
	 * 
	 * @param context
	 *            上下文
	 * @param type
	 *            类型
	 * @param flags
	 *            标志，默认为0
	 * @return 对象，如果失败返回null
	 */
	public synchronized static TeeveeCapturer createInstance(Context context, int type, int flags) {
		return new TeeveeCapturer(context, type, flags);
	}

	private Context context;
	private int type = 0, flags = 0;
	private CaptureStateListener csl;
	private Object mutex = new Object();
	private int id = -1, peer = 0;
	private boolean released = false;

	/**
	 * 得到framer抓屏实例
	 * 
	 * @return 对象
	 */
	TeeveeCapturer(Context context, int type, int flags) {
		this.context = context;
		this.type = type;
		this.flags = flags;
		if (!native_init(new WeakReference<TeeveeCapturer>(this), type, flags))
			throw new RuntimeException("native init failed");
		if (peer == 0)
			throw new RuntimeException("impl error");
	}

	protected void finalize() throws Throwable {
		try {
			release();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.finalize();
	}

	/**
	 * 释放资源
	 */
	public void release() {
		synchronized (mutex) {
			if (!released) {
				released = true;
				native_release();
			}
		}
	}

	/**
	 * 是否已释放
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean isReleased() {
		return !released;
	}

	/**
	 * 分配可抓取frame的id值
	 * <p>
	 * 
	 * @return 返回唯一标识id值, 如果为0则表示操作失败
	 */
	public int allocId() {
		if(id != 0)
			return id;
		return (id = native_alloc_id());
	}



	/**
	 * 释放可抓取frame的id值
	 */
	public void cancelId() {
		native_cancel_id();
		id = 0;
	}

	/**
	 * 获取可抓取frame的id值
	 * <p>
	 * 
	 * @return 返回唯一标识id值
	 */
	public int getAllocedId() {
		return id;
	}

	/**
	 * 将抓取的frame屏数据转换成bitmap位图
	 * <p>
	 * 
	 * @param bmp
	 *            位图对象
	 * @return 是否转换成功
	 */
	public boolean dup(Bitmap bmp) {
		return native_dup_bitmap(bmp);
	}

	/**
	 * 将当前抓图保存为图片
	 * 
	 * @param dir
	 *            目录
	 * @param name
	 *            文件名称，不包括扩展名
	 * @return 文件的全名称
	 * @throws IOException
	 *             如果发生O异常
	 */
	public String save(String dir, String name) throws IOException {
		String ret = native_save_file(dir + "/" + name);
		if (ret == null)
			throw new IOException("save error!");
		return name + "." + ret;
	}

	/**
	 * 设置抓取的frame屏状态监听器
	 * 
	 * @param l
	 *            对象
	 */
	public void setCaptureStateListener(CaptureStateListener l) {
		csl = l;
	}

	/**
	 * 状态监听器
	 */
	public static interface CaptureStateListener {
		/**
		 * 开始时回调
		 * 
		 * @param id
		 *            所涉及的ID
		 */
		void onCaptureStart(int id);

		/**
		 * 超时时回调
		 * 
		 * @param id
		 *            ID值
		 */
		void onCaptureIdTimeout(int id);

		/**
		 * 结束时回调
		 * 
		 * @param id
		 *            ID值
		 * @param err
		 *            错误信息
		 */
		void onCaptureOver(int id, String err);
	}

	static final int MSG_START = 1;
	static final int MSG_TIMEOUT = 2;
	static final int MSG_END = 3;

	void onMessage(int msg, int p1, String p2) {
		CaptureStateListener l = csl;
		Log.d(TAG, "onMessage msg=" + msg + ",p1=" + p1 + ",p2=" + p2);
		if (l == null)
			return;
		switch (msg) {
		case MSG_START:
			l.onCaptureStart((int) p1);
			break;
		case MSG_TIMEOUT:
			l.onCaptureIdTimeout((int) p1);
			break;
		case MSG_END:
			l.onCaptureOver(p1, p2);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	static void native_proc(Object o, int msg, int p1, String p2) {
		WeakReference<TeeveeCapturer> wo = null;
		TeeveeCapturer m = null;
		if (o == null)
			return;
		try {
			wo = (WeakReference<TeeveeCapturer>) o;
			if ((m = wo.get()) == null)
				return;
			m.onMessage(msg, p1, p2);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
	}

	private native boolean native_init(WeakReference<TeeveeCapturer> wo, int type, int flags);

	private native void native_release();

	private native int native_alloc_id();

	private native void native_cancel_id();

	private native boolean native_dup_bitmap(Bitmap b);

	private native String native_save_file(String pathWithoutExtName);
}
