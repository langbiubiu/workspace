package android.media;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.net.telecast.ProgramInfo;
import android.net.telecast.StreamSelector;
import android.net.telecast.TransportManager;
import android.util.Log;

/**
 * 节目录制器
 */
public class TeeveeRecorder {
	static final String TAG = "[java]TeeveeRecorder";

	/** 创建标志-默认 */
	public static final int CREATE_FLAG_DEFAULT = 0;
	/** 创建标志-录制标清节目类型，参考720*576 */
	public static final int CREATE_FLAG_SD = 0x1;
	/** 创建标志-录制高清节目类型，参考1280*720 */
	public static final int CREATE_FLAG_HD = 0x2;
	/** 创建标志-录制全高清节目类型，参考1920*1080 */
	public static final int CREATE_FLAG_FHD = 0x4;
	/** 创建标志-录制超高清节目类型，参考3840*2160(4K×2K) */
	public static final int CREATE_FLAG_UHD = 0x8;

	/** 创建标志-是否需要进行转码 */
	public static final int CREATE_FLAG_TRANSCODE = 0x10;
	/** 标志-录制本地加密数据(如3DES/DES),系统将对音频或视频通道或二者同时加密 */
	public static final int CREATE_FLAG_NATIVE_ENCRYPTION = 0x20;
	/** 标志-录制加密流 */
	public static final int CREATE_FLAG_DESCRAMBLER = 0x40;

	/** 标志-默认 */
	public static final int SELECT_FLAG_DEFAULT = 0;
	/** 标志-节目播放监控PSI信息,播放默认将不会监测PSI的变化,以及相应的处理 */
	public static final int SELECT_FLAG_OBSERVE_PSI = 0x01;

	/** 标志-默认 */
	public static final int SET_FLAG_DEFAULT = 0;

	/** 用于选择一个空流来替换之前的选择(包括频率，文件以及流) {@link #select(FrequencyInfo, int)} */
	public static FileDescriptor EMPTY_STREAM = new FileDescriptor();
	
	Context context;
	private Object mutex = new Object();
	private int peer = 0;
	private boolean released = false;

	/**
	 * 得到录制器实例
	 * 
	 * @return 对象
	 */
	public static TeeveeRecorder createTeeveeRecorder(Context ctx) {
		return createTeeveeRecorder(ctx, 0);
	}

	/**
	 * 创建录制器实例
	 * 
	 * @param ctx
	 *            上下文
	 * @param flags
	 *            标志，默认为0
	 * @return 对象
	 */
	public static TeeveeRecorder createTeeveeRecorder(Context ctx, int flags) {
		TeeveeRecorder tr = createInner(ctx);
		if (tr != null)
			if (tr.initPeer(flags))
				return tr;
		return null;
	}

	static TeeveeRecorder createInner(Context ctx) {
		String className = null;
		try {
			if ((className = TransportManager.getSystemProperty("android.media.TeeveeRecorder"))
					.equals("")) {
				return new TeeveeRecorder(ctx);
			} else {// 如果平台有特殊实现则使用特殊实现.
				return (TeeveeRecorder) Class.forName(className)
						.getMethod("createTeeveeRecorder", Context.class).invoke(null, ctx);
			}
		} catch (Exception e) {
			Log.e(TAG, "create createTeeveeRecorder(" + className + ") failed:" + e);
		}
		return null;
	}

	protected TeeveeRecorder(Context ctx) {
		this.context = ctx;
	}

	// 之所以单独增加一个initPeer函数,是为避免反射式创建对象的失败(构造函数不能增加参数)。
	// 为了达成增加flags参数的目的,构造完毕后再调用此函数。
	private boolean initPeer(int flags) {
		if (!native_init(new WeakReference<TeeveeRecorder>(this), flags))
			throw new RuntimeException("native init failed");
		if (peer == 0)
			throw new RuntimeException("impl error");
		return true;
	}

	/**
	 * 准备录制器所需资源
	 * 
	 * @return 成功返回true,否则返回false
	 */
	public boolean prepare() {
		return native_prepare();
	}

	/**
	 * 释放资源，此后对象不再可用
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
	 * 开始，获取相关资源并准备录制
	 * <p>
	 * 如果已经处于start状态将直接返回
	 */
	public boolean start() {
		return native_start();
	}

	/**
	 * 停止，释放资源及停止录制
	 */
	public void stop() {
		native_stop();
	}

	/**
	 * 暂停录制
	 * <p>
	 * 
	 * @return 成则返回true，否则返回false
	 */
	public boolean pause() {
		return native_pause();
	}

	/**
	 * 恢复录制
	 */
	public void resume() {
		native_resume();
	}

	/**
	 * 设置数据源
	 * 
	 * @param selector
	 *            数据源对象
	 * @return 成功返回true,否则返回false
	 */
	public boolean setDataSource(StreamSelector selector) {
		return setDataSource(selector, SET_FLAG_DEFAULT);
	}

	/**
	 * 设置数据源
	 * 
	 * @param selector
	 *            数据源对象
	 * @param flags
	 *            标志，默认为0
	 * @return 成功返回true,否则返回false
	 */
	public boolean setDataSource(StreamSelector selector, int flags) {
		if ((src != null && selector == null) || (src == null && selector != null)) {
			src = selector; // StreamSelector对象先释放则native_set_data_source失败，保证赋值正确
			if (native_set_data_source(selector, flags)) {
				return true;
			}
		}
		return false;
	}

	StreamSelector src = null;

	/**
	 * 得到之前设置的数据源
	 * 
	 * @return 对象,没有则返回null
	 */
	public StreamSelector getDataSource() {
		return src;
	}

	void checkPeer() {
		if (peer == 0)
			throw new IllegalStateException("not reserve!");
	}

	/**
	 * 录制指定的节目信息到流文件中
	 * 
	 * @param fd
	 *            流文件句柄(管道或套接字)
	 * @param pi
	 *            节目信息
	 * @param flags
	 *            标志，默认为0
	 * @return 成功则返回true,否则返回false
	 */
	public boolean selectProgram(FileDescriptor fd, ProgramInfo pi, int flags) {
		synchronized (mutex) {
			checkPeer();
			if (fd == EMPTY_STREAM || fd == null) {
				uri = "fd://" + (-1);
				fd = EMPTY_STREAM;
			} else {
				uri = "fd://" + fd.toString();// 顺便做了null pointer检查
			}
			return selectProgram(fd, pi, null, flags);
		}
	}

	/**
	 * 设置录制的偏好参数
	 * 
	 * @param ti
	 *            偏好参数
	 * @param flags
	 *            默认为0
	 * @return
	 */
	public void setPreference(TeeveeRecordPreference ti) {
		synchronized (mutex) {
			checkPeer();
			mPreference = ti;
		}
	}

	TeeveeRecordPreference mPreference = null;
	String preference = null;

	/**
	 * 获取录制的偏好参数
	 */
	public TeeveeRecordPreference getPreference() {
		if (preference != null)
			return TeeveeRecordPreference.fromQueryString(preference);
		return null;
	}

	/**
	 * 录制一组PID所对应的流到流文件中
	 * 
	 * @param fd
	 *            流文件句柄(管道或套接字)
	 * @param pi
	 *            节目信息
	 * @param pids
	 *            pid数组
	 * @param flags
	 *            标志，默认为0
	 * @return 成功则返回true,否则返回false
	 */
	public boolean selectProgram(FileDescriptor fd, ProgramInfo pi, int[] pids, int flags) {
		synchronized (mutex) {
			checkPeer();
			if (pi == null && pids.length <= 0)
				throw new RuntimeException("selectProgram11>pids is null pointer");
			if (fd == EMPTY_STREAM || fd == null) {
				uri = "fd://" + (-1);
				fd = EMPTY_STREAM;
			} else {
				uri = "fd://" + fd.toString();// 顺便做了null pointer检查
			}
			String s = uri = pi.toString();
			s += mPreference != null ? mPreference.toQueryString() : "";
			return native_select(fd, s, pids, flags);
		}
	}

	/**
	 * 录制指定的节目信息到流句柄中
	 * 
	 * @param fd
	 *            流文件句柄(管道或套接字)
	 * @param pi
	 *            节目信息
	 * @param flags
	 *            标志，默认为0
	 * @return 成功则返回true,否则返回false
	 */
	public boolean selectProgram(FileDescriptor fd, int off, int len, ProgramInfo pi, int flags) {
		synchronized (mutex) {
			checkPeer();
			return selectProgram(fd, off, len, pi, null, flags);
		}
	}

	/**
	 * 录制一组PID所对应的流到句柄中
	 * 
	 * @param fd
	 *            流文件句柄(管道或套接字)
	 * @param pi
	 *            节目信息
	 * @param flags
	 *            标志，默认为0
	 * @return 成功则返回true,否则返回false
	 */
	public boolean selectProgram(FileDescriptor fd, long off, long len, ProgramInfo pi, int[] pids,
			int flags) {
		synchronized (mutex) {
			checkPeer();
			if (pi == null && pids.length <= 0)
				throw new RuntimeException("selectProgram22>pids is null pointer");
			if (fd == EMPTY_STREAM || fd == null) {
				uri = "fd://" + (-1);
				fd = EMPTY_STREAM;
			} else {
				uri = "fd://" + fd.toString();// 顺便做了null pointer检查
			}
			String s = uri = pi.toString();
			s += mPreference != null ? mPreference.toString() : "";
			return native_select(fd, off, len, s, pids, flags);
		}
	}

	public String getRecordUri() {
		return uri;
	}

	String uri;

	OnRecordStateListener rsl = null;

	public void setOnRecordStateListener(OnRecordStateListener l) {
		rsl = l;
	}

	/**
	 * 录制状态监听器
	 */
	public static interface OnRecordStateListener {
		/**
		 * 录制开始
		 * 
		 * @param program_number
		 */
		void onRecordStart(int program_number);

		/**
		 * 录制错误
		 * <p>
		 * 之后录制将被停止。如果要确保后续player再工作，应用程序就要重新执行prepare等一系列操作.
		 * 
		 * @param program_number
		 *            由selectProgram中的参数所指定,如果未指定将为0
		 * @param msg
		 *            错误信息字符串
		 */
		void onRecordError(int program_number, String msg);

		/**
		 * 录制完成
		 * <p>
		 * 之后录制将被停止。如果要确保后续player再工作，应用程序就要重新执行prepare等一系列操作.
		 * 
		 * @param program_number
		 *            由selectProgram中的参数所指定,如果未指定将为0
		 */
		void onRecordEnd(int program_number);
	}

	private ProgramStateListener psl;

	/**
	 * 添加节目状态监听器
	 * 
	 * @param l
	 *            对象
	 */
	public void setProgramStateListener(ProgramStateListener l) {
		psl = l;
	}

	/**
	 * 节目状态监听器
	 * <p>
	 * 如果不希望系统更改播放的流或者禁用此消息,参考
	 * {@link android.media.TeeveePlayer#FLAG_NOT_OBSERVE_PSI}
	 */
	public static interface ProgramStateListener {
		/**
		 * 要求重新选择节目
		 * <p>
		 * 这一般是因为节目组件发生变化引起的<br>
		 * uri参考{@link android.net.telecast.ProgramInfo#fromString(String)}
		 * 
		 * @param program_number
		 *            节目号
		 * @param newuri
		 *            建议选择的uri
		 */
		void onProgramReselect(int program_number, String newuri);

		/**
		 * 节目已经移除
		 * 
		 * @param program_number
		 *            节目号
		 */
		void onProgramRemoved(int program_number);
	}

	static final int REC_ERROR = 1;
	static final int REC_END = 2;
	static final int REC_START = 3;
	static final int P_RESELECT = 8;
	static final int P_REMOVED = 9;

	void onMessage(int msg, int p1, String s) {
		OnRecordStateListener l = rsl;
		ProgramStateListener l2 = psl;

		switch (msg) {
		case REC_START:
			preference = s;
			if (l != null)
				l.onRecordStart(p1);
			break;
		case REC_ERROR:
			if (l != null)
				l.onRecordError(p1, s);
			break;
		case REC_END:
			if (l != null)
				l.onRecordEnd(p1);
			break;
		case P_RESELECT:
			if (l2 != null)
				l2.onProgramReselect(p1, s);
			break;
		case P_REMOVED:
			if (l2 != null)
				l2.onProgramRemoved(p1);
			break;
		default:
			return;
		}
	}

	@SuppressWarnings("unchecked")
	static void native_proc(Object o, int msg, int p1, String info) {
		WeakReference<TeeveeRecorder> wo = null;
		TeeveeRecorder m = null;
		if (o == null)
			return;
		try {
			wo = (WeakReference<TeeveeRecorder>) o;
			if ((m = wo.get()) == null)
				return;
			m.onMessage(msg, p1, info);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
	}

	native boolean native_init(WeakReference<TeeveeRecorder> wo, int flags);

	native void native_release();

	native boolean native_prepare();

	native boolean native_start();

	native void native_stop();

	native boolean native_pause();

	native void native_resume();

	native boolean native_set_data_source(StreamSelector src, int flags);

	native boolean native_select(FileDescriptor fd, long off, long len, String s, int[] pids,
			int flags);

	native boolean native_select(FileDescriptor fd, String s, int[] pids, int flags);
}
