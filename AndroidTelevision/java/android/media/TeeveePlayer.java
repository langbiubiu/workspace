package android.media;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.telecast.ProgramInfo;
import android.net.telecast.StreamSelector;
import android.net.telecast.TransportManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * 电视播放器
 * <p>
 * 本播放器不包含解扰相关的操作。
 * <p>
 * 如果应用程序需要一整套完整的播放流程(包括播放加扰节目), 一般可以选择：<br>
 * <li>自行实现，另外参考StreamDescrambler和CAManager,CAModuleManager<br>
 * <li>使用Widget,参考TeeveeAppWidgetHost,(其实现由网络运营商服务包程序提供)</li>
 * <p>
 * <p>
 * 对于普通应用程序,推荐直接使用Widget来实现电视节目的播放嵌入.
 * 
 */
public class TeeveePlayer {
	static final String TAG = "[java]TeeveePlayer";

	/** 创建标志-默认 */
	public static final int CREATE_FLAG_DEFAULT = 0;
	/** 创建标志-不支持音频播放 */
	public static final int CREATE_FLAG_NO_AUDIO = 0x1;
	/** 创建标志-不支持视频播放 */
	public static final int CREATE_FLAG_NO_VIDEO = 0x2;
	/** 创建标志-支持收取字幕数据 */
	public static final int CREATE_FLAG_RECV_SUBTITLE = 0x4;
	/** 创建标志-视频动画 */
	public static final int CREATE_FLAG_VIDEO_ANIMATION = 0x8;
	/** 创建标志-工作在视频上图层 */
	public static final int CREATE_FLAG_VIDEO_OVERLAY = 0x10;
	/** 创建标志-工作在图形上图层 */
	public static final int CREATE_FLAG_GRAPHICS_OVERLAY = 0x20;
	/** 创建标志-基底播放器，用于画中画业务,不能设置CREATE_FLAG_NO_VIDEO */
	public static final int CREATE_FLAG_BASE_FOR_PIP = 0x40;
	/** 创建标志-视频解码器尽量使用HD/FHD/UHD尽量强的格式 */	
	public static final int CREATE_FLAG_VIDEO_BEST_HD = 0x80;
	/** 创建标志-创建时没有音频输出 */
	public static final int CREATE_FLAG_NO_AUDIO_OUTPUT = 0x100;
	/** 创建标志-创建时视频帧缓存更少、达到快速播放的效果 */
	public static final int CREATE_FLAG_VIDEO_CACHE_LESS = 0x200;

	/** 情景-标准 */
	public static final String PROFILE_STD = "std";
	/** 情景-电视 */
	public static final String PROFILE_TV = "tv";
	/** 情景-电影 */
	public static final String PROFILE_MOVIE = "movie";
	/** 情景-电台 */
	public static final String PROFILE_RADIO = "radio";
	/** 情景-音乐 */
	public static final String PROFILE_MUSIC = "music";

	/** 标志-默认 */
	public static final int FLAG_SELECT_DEFAULT = 0;
	/** 标志-节目切换为黑色画面 */
	public static final int FLAG_VIDEO_FRAME_BLACK = 0;
	/** 标志-节目切换为静帧画面(选择节目时自动恢复) */
	public static final int FLAG_VIDEO_FRAME_FREEZE = 0x1;
	/** 标志-节目播放不监控PSI信息,播放将不会监测PSI的变化,以及相应的处理 */
	public static final int FLAG_NOT_OBSERVE_PSI = 0x2;
	/** 标志-节目源为纯I帧模式 */
	public static final int FLAG_VIDEO_IFRAME_ONLY = 0x4;
	/** 标志-播出2D转3D */
	public static final int FLAG_VIDEO_2D_TO_3D = 0x8;
	/** 标志-播出3D左右 */
	public static final int FLAG_VIDEO_3D_LEFT_RIGHT = 0x10;
	/** 标志-播出3D上下 */
	public static final int FLAG_VIDEO_3D_TOP_BUTTOM = 0x20;
	/** 标志-播出2D */
	public static final int FLAG_VIDEO_2D = 0x40;
	/** 标志-2D 3D保持之前的设定 */
	public static final int FLAG_VIDEO_2D3D_KEEP = 0;
	/** 标志-音频仅左声道转双声道 */
	public static final int FLAG_AUDIO_LEFT_TO_MONO = 0x80;
	/** 标志-音频仅右声道转双声道 */
	public static final int FLAG_AUDIO_RIGHT_TO_MONO = 0x100;
	/** 标志-全屏显示视频，必要时会更改纵横比例 */
	public static final int FLAG_VIDEO_TRANSFORM_RATIO_ADAPTION = 0;
	/** 标志-维持视频原比例，不裁剪情况扩充到最大显示范围 */
	public static final int FLAG_VIDEO_TRANSFORM_NOCLIP_FILL = 0x200;
	/** 标志-维持视频原比例，裁剪情况扩充到全屏显示范围 */
	public static final int FLAG_VIDEO_TRANSFORM_CLIP_FULL = 0x400;
	/** 标志-切换时保持音频不变，保留前次选择时的参数和状态 */
	public static final int FLAG_SELECT_KEEP_AUDIO = 0x800;	
	/** 标志-强制重新播放节目 */
	public static final int FLAG_SELECT_PROGRAM_FORCE = 0x1000;
	/** 标志-是否需要以速率计算pts */
	public static final int FLAG_DECODER_PLAY_FLAG_PTS_REFER_RATE = 0x2000;
	/** 标志-切换时保持视频不变，保留前次选择时的参数和状态 */
	public static final int FLAG_SELECT_KEEP_VIDEO = 0x4000;
	/** 标志-切换时保持字幕不变，保留前次选择时的参数和状态 */
	public static final int FLAG_SELECT_KEEP_SUBTITLE = 0x8000;
	/** 标志-切换时保持PCR不变，保留前次选择时的参数和状态 */
	public static final int FLAG_SELECT_KEEP_PCR = 0x10000;
	/** 创建标志-切换时保持自由播放模式 */
	public static final int FLAG_SELECT_SYNC_REF_NONE = 0x20000;

	/** 动画标志-默认 */
	public static final int ANIMATION_FLAG_ACTION_DEFAULE = 0;
	/** 动画标志-动作附加到最后，不清空之前待处理的动作 */
	public static final int ANIMATION_FLAG_ACTION_APPEND = 0x01;

	/**
	 * 创建播放器实例
	 * 
	 * @param ctx
	 *            上下文
	 * @return 对象
	 */
	public static TeeveePlayer createTeeveePlayer(Context ctx) {
		return createTeeveePlayer(ctx, 1, 0);
	}

	/**
	 * 创建播放器实例
	 * 
	 * @param ctx
	 *            上下文
	 * @param pipSize
	 *            支持的画中画数量( >= 0)
	 * @param flags
	 *            标志，默认为0
	 * @return 对象
	 */
	public static TeeveePlayer createTeeveePlayer(Context ctx, int pipSize, int flags) {
		if (pipSize < 0 || pipSize > 100)
			throw new IllegalArgumentException("bad pipSize:" + pipSize + ", available in[1,64]");
		TeeveePlayer tp = createInner(ctx);
		if (tp != null)
			if (tp.initPeer(null, pipSize, flags))
				return tp;
		return null;
	}

	/**
	 * 创建画中画播放器实例
	 * 
	 * @param base
	 *            基底(资源协助等)
	 * @param flags
	 *            标志，默认为0
	 * @return 对象
	 */
	public static TeeveePlayer createPipTeeveePlayer(TeeveePlayer base, int flags) {
		if (base == null)
			throw new NullPointerException("base is null");
		TeeveePlayer tp = createInner(base.context);
		if (tp != null)
			if (tp.initPeer(base, -1, flags))
				return tp;
		return null;
	}

	static TeeveePlayer createInner(Context ctx) {
		String className = null;
		try {
			if ((className = TransportManager.getSystemProperty("android.media.TeeveePlayer"))
					.equals("")) {
				return new TeeveePlayer(ctx);
			} else {// 如果平台有特殊实现则使用特殊实现.
				return (TeeveePlayer) Class.forName(className)
						.getMethod("createTeeveePlayer", Context.class).invoke(null, ctx);
			}
		} catch (Exception e) {
			Log.e(TAG, "create TeeveePlayer(" + className + ") failed:" + e);
		}
		return null;
	}

	TeeveePlayer base;
	Context context;
	private PlayStateListener playsl;
	private boolean released = false, weak = false;
	private int user_flags = 0;

	protected TeeveePlayer(Context ctx) {
		this.context = ctx;
	}

	// 之所以单独增加一个initPeer函数,是为避免反射式创建对象的失败(构造函数不能增加参数)。
	// 为了达成增加flags参数的目的,构造完毕后再调用此函数。
	private boolean initPeer(TeeveePlayer base, int pipSize, int flags) {
		if (!native_init(new WeakReference<TeeveePlayer>(this), base, pipSize, flags))
			throw new RuntimeException("native init failed");
		if ((peer != 0)) {
			this.base = base;
			user_flags = flags;
			return true;
		}
		return false;
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
	 * 准备播放器所需资源
	 * 
	 * @return 成功返回true,否则返回false
	 */
	public boolean prepare() {
		if (isPrepared())
			return true;
		return (prepared = native_prepare());
	}

	/**
	 * 清理播放器缓存
	 * 
	 * @return 成功返回true,否则返回false
	 */
	public boolean clear() {
		return native_clear();
	}

	boolean prepared = false;

	/**
	 * 已否已准备过资源
	 * <p>
	 * 如果要判断对象想是否可用还需判断isWeakMode()
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean isPrepared() {
		return !released && prepared;
	}

	/**
	 * 解除对播放器的保留状态,释放相关资源
	 * <p>
	 * 对象将不可再使用
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
	 * 设置电视音量
	 * <p>
	 * 非全局音量, 用于对电视音量进行单独调整.<br>
	 * 比如, 切换到不同频道，其音量都可设为不同的值，以此来适应信号源声音忽大忽小的问题
	 * 
	 * @param leftVolume
	 *            左声道音量(0.0f-1.0f)
	 * @param rightVolume
	 *            右声道音量(0.0f-1.0f)
	 * @return 成功返回true，失败返回false
	 */
	public boolean setVolume(float leftVolume, float rightVolume) {
		return native_set_volume(leftVolume, rightVolume);
	}

	/**
	 * 设置音量
	 * <p>
	 * 等价于同音量值调用{@link #setVolume(float, float)}
	 * 
	 * @param volume
	 *            音量值(0.0f-1.0f)
	 * @return 成功返回true，失败返回false
	 */
	public boolean setVolume(float volume) {
		return native_set_volume(volume, volume);
	}

	/**
	 * 设置数据源
	 * 
	 * @param selector
	 *            选择器
	 * @return 成功则返回true,否则返回false
	 */
	public boolean setDataSource(StreamSelector selector) {
		if ((src != null && selector == null) || (src == null && selector != null)) {
			src = selector; // StreamSelector对象先释放则native_set_data_source失败，保证赋值正确
			if (native_set_data_source(selector)) {
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

	/**
	 * 设置绑定的Surface 起始位置为(0,0)
	 * 
	 * @param holder
	 *            一般从SurfaceView中获取
	 * @return 成功返回true,否则返回false
	 */
	public boolean setDisplay(SurfaceHolder holder) {
		return setDisplay(holder, 0, 0);
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
			Log.i(TAG, "setDisplay holder = " + holder + " x=" + x + " y=" + y);
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
			Log.i(TAG, "setDisplay x=" + x + " y=" + y + " w=" + w + " h=" + h);
			if (native_set_display(x, y, w < 0 ? 0 : w, h < 0 ? 0 : h)) {
				Log.v(TAG, "call native_set_display---");
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
	 * 加载动画脚本
	 * 
	 * @param fd
	 *            动画脚本,如果fd为-1则取消之前加载的脚本
	 * @return 成功返回true，否则返回false
	 */
	public boolean loadAnimation(FileDescriptor fd) {
		return native_load_a(fd) == 0;
	}

	/**
	 * 实施动画
	 * 
	 * @param id
	 *            动作ID
	 * @param p1
	 *            参数1
	 * @param p2
	 *            参数2
	 * @param flags
	 *            标志，默认为0
	 */
	public void actAnimation(int id, int p1, int p2, int flags) {
		if (id <= 0 || id > 256)
			throw new IndexOutOfBoundsException();
		native_act_a(id, p1, p2, flags);
	}

	/**
	 * 开启相关设备的工作。还需要调用selectProgram才能播放具体的节目.
	 * <p>
	 * 如果已经处于start状态将直接返回true
	 * 
	 * @return 成功返回true,否则返回false
	 */
	public boolean start() {
		return native_start();
	}

	/**
	 * 停止播放
	 * <p>
	 * 会清空缓冲区缓存，停止相关设备的工作，并按照前一次的冻结方式冻结画面或者不予冻结。
	 * <p>
	 * 可用于实现点播的seek操作等需要清缓冲区的操作。
	 */
	public void stop() {
		native_stop();
		last_process_pts_time = 0;
	}

	/**
	 * 暂停播放
	 * <p>
	 * 冻结画面,静音,不会清理缓冲区
	 * 
	 * @return 成则返回true，否则返回false
	 */
	public boolean pause() {
		return native_pause();
	}

	/**
	 * 恢复播放
	 * <p>
	 * 此函数调用需要与{@link #pause()}对应
	 */
	public void resume() {
		native_resume();
	}

	/**
	 * 设定播放器视频停止时显示模式
	 * 
	 * @param b
	 *            false表示解除静帧模式，true使能静帧模式
	 * @param flags
	 *            只有当b为true时有效 参考<br> {@link #FLAG_VIDEO_FRAME_BLACK}<br>
	 *            {@link #FLAG_VIDEO_FRAME_FREEZE}<br>
	 * @return 成功返回true,否则返回false
	 */
	public boolean setFreeze(boolean b, int flags) {
		return native_set_freeze(b, flags);
	}

	/**
	 * 得到当前节目的播放时间
	 * 
	 * @return 毫秒时间值
	 */
	public long getPlayTime() {
		return native_play_time();
	}

	/**
	 * 获取解码器处理中的pts时间
	 * 
	 * @return 毫秒时间值
	 */
	public long getPlayProcessPtsTime() {
		return last_process_pts_time;
	}

	long last_process_pts_time = 0;

	/**
	 * 得到选择的节目的uri
	 * 
	 * @return 字符串
	 */
	public String getSelectUri() {
		return uri;
	}

	String uri = null;

	boolean invalidProgramInfo(ProgramInfo p) {
		return !((p.hasValidAudioPID()) || p.hasValidVideoPID());
	}

	/**
	 * 选择节目
	 * 
	 * @param p
	 *            节目信息
	 * @return 失败则返回false,否则返回true
	 */
	public boolean selectProgram(ProgramInfo p) {
		if (invalidProgramInfo(p))
			return false;
		return native_select_program((uri = p.toString()), null, 0);
	}

	/**
	 * 选择节目
	 * 
	 * @param p
	 *            节目
	 * @param flags
	 *            标志参数, 默认请传入0<br>
	 *            参考<br> {@link #FLAG_NOT_OBSERVE_PSI}<br>
	 *            {@link #FLAG_VIDEO_FRAME_BLACK}<br>
	 *            {@link #FLAG_VIDEO_FRAME_FREEZE}
	 * @return 失败则返回false,否则返回true
	 */
	public boolean selectProgram(ProgramInfo p, int flags) {
		if (invalidProgramInfo(p))
			return false;
		return native_select_program((uri = p.toString()), null, flags);
	}

	/**
	 * 设置情景模式
	 * <p>
	 * 系统将根据当前设定做尽可能的优化
	 * <p>
	 * 比如自动音量平衡,如果设定为PROFILE_TV，在系统支持的情况下将启用此功能,避免换台导致的声音忽大忽小。而对于PROFILE_MOVIE,
	 * 则不启用，因电影的声音动态范围一般较大，这个策略已不适用。如果系统支持，此时会打开更好音效系统。 <br>
	 * 参考:<br>
	 * 标准:{@link #PROFILE_STD}<br>
	 * 电视:{@link #PROFILE_TV}<br>
	 * 电影:{@link #PROFILE_MOVIE}<br>
	 * 音乐:{@link #PROFILE_MUSIC}<br>
	 * 电台:{@link #PROFILE_RADIO}<br>
	 * 
	 * @param type
	 *            类型
	 * @return 失败则返回false,否则返回true
	 */
	public boolean setProfile(String type) {
		return native_set_profile(type);
	}

	/**
	 * 视频帧抓屏
	 * <p>
	 * 参考TeeveeCapturer对象.
	 * 
	 * @param id
	 *            抓屏id标识值，由TeeveeCapturer对象给定
	 * @return 失败则返回false,否则返回true
	 */
	public boolean captureVideoFrame(int id) {
		return native_capture_video(id);
	}

	/**
	 * 设置资源弱引用模式
	 * <p>
	 * <li>当参数为true,则设置为弱引用模式，此时资源可能会被系统收回，此时除了关闭对象或者重新改回强引用模式,其他操作将直接失败，
	 * <li>参数为false,则该会强引用，操作可能失败.若失败，则应该关闭对象.
	 * 
	 * @return 成功返回true,否则返回false
	 */
	public boolean setWeakMode(boolean b) {
		synchronized (mutex) {
			if (native_set_weak_mode(b)) {
				weak = b;
				return true;
			}
			return false;
		}
	}

	/**
	 * 是否为弱引用模式
	 * 
	 * @return 是则返回true,否则返回false
	 */
	public boolean isWeakMode() {
		return weak;
	}

	/**
	 * 接收字幕数据流
	 * 
	 * @param fd
	 *            句柄(应该为流式)
	 */
	public boolean recvSubtitle(FileDescriptor fd) {
		return native_recv_subt(fd);
	}
	
	/**
	 * 设置播放状态监听器
	 * 
	 * @param l
	 *            对象
	 */
	public void setPlayStateListener(PlayStateListener l) {
		playsl = l;
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
		 * <p>
		 * 若uri包含PMT Section数据，则表明重选信息不能确定.<br>
		 * 参考{@link android.net.telecast.ProgramInfo#getPmtSection()}
		 * 
		 * @param program_number
		 *            节目号
		 * @param newuri
		 *            建议选择的uri
		 */
		void onProgramReselect(int program_number, String newuri);

		/**
		 * 节目已经中断
		 * <p>
		 * 这可能是因为节目被移除,或者节目的组成流发生变化等原因导致的
		 * 
		 * @param program_number
		 *            节目号
		 */
		void onProgramDiscontinued(int program_number);
	}

	/**
	 * 播放状态监听器
	 */
	public static interface PlayStateListener {
		/**
		 * 当选择被接受后发送
		 * 
		 * @param player
		 *            播放器对象
		 * @param program_number
		 *            节目号
		 */
		void onSelectionStart(TeeveePlayer player, int program_number);

		/**
		 * 播放处理开始进行
		 * <p>
		 * 收到此消息，可认为播放已经开始，比如再切换节目时，下一个节目已开始播放
		 * 
		 * @param program_number
		 *            由selectProgram中的参数所指定,如果未指定将为0
		 */
		void onPlayProcessing(int program_number);

		/**
		 * 播放处理挂起
		 * <p>
		 * 播放处理已经挂起,这通常是因为切换节目或者额数据消耗完毕导致的
		 * 
		 * @param program_number
		 *            由selectProgram中的参数所指定,如果未指定将为0
		 */
		void onPlaySuspending(int program_number);

		/**
		 * 播放错误
		 * <p>
		 * 之后播放将被停止。如果要确保后续player再工作，应用程序就要重新执行prepare等一系列操作.
		 * 
		 * @param program_number
		 *            由selectProgram中的参数所指定,如果未指定将为0
		 * @param msg
		 *            错误信息字符串
		 */
		void onPlayError(int program_number, String msg);
	}

	private Object mutex = new Object();
	private int peer = 0;
	private int mx = 0, my = 0, mw = 0, mh = 0;
	private SurfaceHolder display;
	private final Object mHolderMutex = new Object();

	// private SurfaceHolder.Callback2 mSurfaceCbk = new
	// SurfaceHolder.Callback2() {//for above 2.3
	@SuppressWarnings("unused")
	private SurfaceHolder.Callback mSurfaceCbk = new SurfaceHolder.Callback() {// for
																				// 2.2
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i(TAG, "call surfaceDestroyed");
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
			Log.i(TAG, "call surfaceRedrawNeeded x = " + x + " y = " + y + " width = " + w
					+ " height = " + h);
			native_set_display(x, y, w, h);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			Log.i(TAG, "surfaceCreated holder = " + holder);
			Canvas canvas = holder.lockCanvas();
			Paint painter = new Paint();
			painter.setColor(Color.TRANSPARENT);
			painter.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
			canvas.drawPaint(painter);
			holder.unlockCanvasAndPost(canvas);
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Log.i(TAG, "call surfaceChanged x = " + mx + " y = " + my + " width = " + width
					+ " height = " + height);
			android.graphics.Rect r = holder.getSurfaceFrame();
			int x = mx; // r.left;
			int y = my; // r.top;
			int w = r.right - r.left;
			int h = r.bottom - r.top;
			native_set_display(x, y, w, h);
		}
	};

	static final int PLAY_PROC = 1;
	static final int PLAY_SUSP = 2;
	static final int PLAY_ERRO = 3;
	static final int PLAY_START = 4;
	static final int PLAY_P_SEL = 8;
	static final int PLAY_P_BRK = 9;

	void onMessage(int msg, int p1, String s, long pts_time) {
		PlayStateListener l = playsl;
		ProgramStateListener l2 = psl;
		if (l == null && l2 == null)
			return;
		switch (msg) {
		case PLAY_PROC:
			if (l != null) {
				last_process_pts_time = pts_time;
				l.onPlayProcessing(p1);

			}
			break;
		case PLAY_SUSP:
			if (l != null)
				l.onPlaySuspending(p1);
			break;
		case PLAY_ERRO:
			if (l != null)
				l.onPlayError(p1, s);
			break;
		case PLAY_START:
			if (l != null)
				l.onSelectionStart(this, p1);
			break;
		case PLAY_P_SEL:
			if (l2 != null)
				l2.onProgramReselect(p1, s);
			break;
		case PLAY_P_BRK:
			if (l2 != null)
				l2.onProgramDiscontinued(p1);
			break;
		default:
			return;
		}
	}

	@SuppressWarnings("unchecked")
	static void native_proc(Object o, int msg, int p1, String s, long pts_time) {
		WeakReference<TeeveePlayer> wo = null;
		TeeveePlayer m = null;
		if (o == null)
			return;
		try {
			wo = (WeakReference<TeeveePlayer>) o;
			if ((m = wo.get()) == null)
				return;
			m.onMessage(msg, p1, s, pts_time);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
	}

	private native boolean native_init(WeakReference<TeeveePlayer> wo, Object b, int ps, int f);

	private native boolean native_prepare();
	
	private native boolean native_clear();
	
	private native void native_release();

	private native boolean native_set_weak_mode(boolean b);

	private native boolean native_set_volume(float leftVolume, float rightVolume);

	private native boolean native_set_data_source(StreamSelector selector);

	private native boolean native_set_display(int x, int y, int w, int h);

	private native int native_load_a(FileDescriptor fd);

	private native void native_act_a(int action, int p1, int p2, int flags);

	private native boolean native_set_freeze(boolean b, int flags);

	private native boolean native_start();

	private native boolean native_stop();

	private native boolean native_pause();

	private native boolean native_resume();

	private native long native_play_time();

	private native boolean native_select_program(String params, FileDescriptor fd, int flags);

	private native boolean native_set_profile(String type);

	private native boolean native_capture_video(int id);

	private native boolean native_recv_subt(FileDescriptor fd);

	@SuppressWarnings("deprecation")
	static void init() {
		TransportManager.ensure();
	}

	static {
		init();
	}
}
