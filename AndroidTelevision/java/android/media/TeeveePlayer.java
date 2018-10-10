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
 * ���Ӳ�����
 * <p>
 * ��������������������صĲ�����
 * <p>
 * ���Ӧ�ó�����Ҫһ���������Ĳ�������(�������ż��Ž�Ŀ), һ�����ѡ��<br>
 * <li>����ʵ�֣�����ο�StreamDescrambler��CAManager,CAModuleManager<br>
 * <li>ʹ��Widget,�ο�TeeveeAppWidgetHost,(��ʵ����������Ӫ�̷���������ṩ)</li>
 * <p>
 * <p>
 * ������ͨӦ�ó���,�Ƽ�ֱ��ʹ��Widget��ʵ�ֵ��ӽ�Ŀ�Ĳ���Ƕ��.
 * 
 */
public class TeeveePlayer {
	static final String TAG = "[java]TeeveePlayer";

	/** ������־-Ĭ�� */
	public static final int CREATE_FLAG_DEFAULT = 0;
	/** ������־-��֧����Ƶ���� */
	public static final int CREATE_FLAG_NO_AUDIO = 0x1;
	/** ������־-��֧����Ƶ���� */
	public static final int CREATE_FLAG_NO_VIDEO = 0x2;
	/** ������־-֧����ȡ��Ļ���� */
	public static final int CREATE_FLAG_RECV_SUBTITLE = 0x4;
	/** ������־-��Ƶ���� */
	public static final int CREATE_FLAG_VIDEO_ANIMATION = 0x8;
	/** ������־-��������Ƶ��ͼ�� */
	public static final int CREATE_FLAG_VIDEO_OVERLAY = 0x10;
	/** ������־-������ͼ����ͼ�� */
	public static final int CREATE_FLAG_GRAPHICS_OVERLAY = 0x20;
	/** ������־-���ײ����������ڻ��л�ҵ��,��������CREATE_FLAG_NO_VIDEO */
	public static final int CREATE_FLAG_BASE_FOR_PIP = 0x40;
	/** ������־-��Ƶ����������ʹ��HD/FHD/UHD����ǿ�ĸ�ʽ */	
	public static final int CREATE_FLAG_VIDEO_BEST_HD = 0x80;
	/** ������־-����ʱû����Ƶ��� */
	public static final int CREATE_FLAG_NO_AUDIO_OUTPUT = 0x100;
	/** ������־-����ʱ��Ƶ֡������١��ﵽ���ٲ��ŵ�Ч�� */
	public static final int CREATE_FLAG_VIDEO_CACHE_LESS = 0x200;

	/** �龰-��׼ */
	public static final String PROFILE_STD = "std";
	/** �龰-���� */
	public static final String PROFILE_TV = "tv";
	/** �龰-��Ӱ */
	public static final String PROFILE_MOVIE = "movie";
	/** �龰-��̨ */
	public static final String PROFILE_RADIO = "radio";
	/** �龰-���� */
	public static final String PROFILE_MUSIC = "music";

	/** ��־-Ĭ�� */
	public static final int FLAG_SELECT_DEFAULT = 0;
	/** ��־-��Ŀ�л�Ϊ��ɫ���� */
	public static final int FLAG_VIDEO_FRAME_BLACK = 0;
	/** ��־-��Ŀ�л�Ϊ��֡����(ѡ���Ŀʱ�Զ��ָ�) */
	public static final int FLAG_VIDEO_FRAME_FREEZE = 0x1;
	/** ��־-��Ŀ���Ų����PSI��Ϣ,���Ž�������PSI�ı仯,�Լ���Ӧ�Ĵ��� */
	public static final int FLAG_NOT_OBSERVE_PSI = 0x2;
	/** ��־-��ĿԴΪ��I֡ģʽ */
	public static final int FLAG_VIDEO_IFRAME_ONLY = 0x4;
	/** ��־-����2Dת3D */
	public static final int FLAG_VIDEO_2D_TO_3D = 0x8;
	/** ��־-����3D���� */
	public static final int FLAG_VIDEO_3D_LEFT_RIGHT = 0x10;
	/** ��־-����3D���� */
	public static final int FLAG_VIDEO_3D_TOP_BUTTOM = 0x20;
	/** ��־-����2D */
	public static final int FLAG_VIDEO_2D = 0x40;
	/** ��־-2D 3D����֮ǰ���趨 */
	public static final int FLAG_VIDEO_2D3D_KEEP = 0;
	/** ��־-��Ƶ��������ת˫���� */
	public static final int FLAG_AUDIO_LEFT_TO_MONO = 0x80;
	/** ��־-��Ƶ��������ת˫���� */
	public static final int FLAG_AUDIO_RIGHT_TO_MONO = 0x100;
	/** ��־-ȫ����ʾ��Ƶ����Ҫʱ������ݺ���� */
	public static final int FLAG_VIDEO_TRANSFORM_RATIO_ADAPTION = 0;
	/** ��־-ά����Ƶԭ���������ü�������䵽�����ʾ��Χ */
	public static final int FLAG_VIDEO_TRANSFORM_NOCLIP_FILL = 0x200;
	/** ��־-ά����Ƶԭ�������ü�������䵽ȫ����ʾ��Χ */
	public static final int FLAG_VIDEO_TRANSFORM_CLIP_FULL = 0x400;
	/** ��־-�л�ʱ������Ƶ���䣬����ǰ��ѡ��ʱ�Ĳ�����״̬ */
	public static final int FLAG_SELECT_KEEP_AUDIO = 0x800;	
	/** ��־-ǿ�����²��Ž�Ŀ */
	public static final int FLAG_SELECT_PROGRAM_FORCE = 0x1000;
	/** ��־-�Ƿ���Ҫ�����ʼ���pts */
	public static final int FLAG_DECODER_PLAY_FLAG_PTS_REFER_RATE = 0x2000;
	/** ��־-�л�ʱ������Ƶ���䣬����ǰ��ѡ��ʱ�Ĳ�����״̬ */
	public static final int FLAG_SELECT_KEEP_VIDEO = 0x4000;
	/** ��־-�л�ʱ������Ļ���䣬����ǰ��ѡ��ʱ�Ĳ�����״̬ */
	public static final int FLAG_SELECT_KEEP_SUBTITLE = 0x8000;
	/** ��־-�л�ʱ����PCR���䣬����ǰ��ѡ��ʱ�Ĳ�����״̬ */
	public static final int FLAG_SELECT_KEEP_PCR = 0x10000;
	/** ������־-�л�ʱ�������ɲ���ģʽ */
	public static final int FLAG_SELECT_SYNC_REF_NONE = 0x20000;

	/** ������־-Ĭ�� */
	public static final int ANIMATION_FLAG_ACTION_DEFAULE = 0;
	/** ������־-�������ӵ���󣬲����֮ǰ������Ķ��� */
	public static final int ANIMATION_FLAG_ACTION_APPEND = 0x01;

	/**
	 * ����������ʵ��
	 * 
	 * @param ctx
	 *            ������
	 * @return ����
	 */
	public static TeeveePlayer createTeeveePlayer(Context ctx) {
		return createTeeveePlayer(ctx, 1, 0);
	}

	/**
	 * ����������ʵ��
	 * 
	 * @param ctx
	 *            ������
	 * @param pipSize
	 *            ֧�ֵĻ��л�����( >= 0)
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return ����
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
	 * �������л�������ʵ��
	 * 
	 * @param base
	 *            ����(��ԴЭ����)
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return ����
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
			} else {// ���ƽ̨������ʵ����ʹ������ʵ��.
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

	// ֮���Ե�������һ��initPeer����,��Ϊ���ⷴ��ʽ���������ʧ��(���캯���������Ӳ���)��
	// Ϊ�˴������flags������Ŀ��,������Ϻ��ٵ��ô˺�����
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
	 * ׼��������������Դ
	 * 
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean prepare() {
		if (isPrepared())
			return true;
		return (prepared = native_prepare());
	}

	/**
	 * ������������
	 * 
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean clear() {
		return native_clear();
	}

	boolean prepared = false;

	/**
	 * �ѷ���׼������Դ
	 * <p>
	 * ���Ҫ�ж϶������Ƿ���û����ж�isWeakMode()
	 * 
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean isPrepared() {
		return !released && prepared;
	}

	/**
	 * ����Բ������ı���״̬,�ͷ������Դ
	 * <p>
	 * ���󽫲�����ʹ��
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
	 * ���õ�������
	 * <p>
	 * ��ȫ������, ���ڶԵ����������е�������.<br>
	 * ����, �л�����ͬƵ����������������Ϊ��ͬ��ֵ���Դ�����Ӧ�ź�Դ���������С������
	 * 
	 * @param leftVolume
	 *            ����������(0.0f-1.0f)
	 * @param rightVolume
	 *            ����������(0.0f-1.0f)
	 * @return �ɹ�����true��ʧ�ܷ���false
	 */
	public boolean setVolume(float leftVolume, float rightVolume) {
		return native_set_volume(leftVolume, rightVolume);
	}

	/**
	 * ��������
	 * <p>
	 * �ȼ���ͬ����ֵ����{@link #setVolume(float, float)}
	 * 
	 * @param volume
	 *            ����ֵ(0.0f-1.0f)
	 * @return �ɹ�����true��ʧ�ܷ���false
	 */
	public boolean setVolume(float volume) {
		return native_set_volume(volume, volume);
	}

	/**
	 * ��������Դ
	 * 
	 * @param selector
	 *            ѡ����
	 * @return �ɹ��򷵻�true,���򷵻�false
	 */
	public boolean setDataSource(StreamSelector selector) {
		if ((src != null && selector == null) || (src == null && selector != null)) {
			src = selector; // StreamSelector�������ͷ���native_set_data_sourceʧ�ܣ���֤��ֵ��ȷ
			if (native_set_data_source(selector)) {
				return true;
			}
		}
		return false;
	}

	StreamSelector src = null;

	/**
	 * �õ�֮ǰ���õ�����Դ
	 * 
	 * @return ����,û���򷵻�null
	 */
	public StreamSelector getDataSource() {
		return src;
	}

	/**
	 * ���ð󶨵�Surface ��ʼλ��Ϊ(0,0)
	 * 
	 * @param holder
	 *            һ���SurfaceView�л�ȡ
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean setDisplay(SurfaceHolder holder) {
		return setDisplay(holder, 0, 0);
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
			Log.i(TAG, "setDisplay holder = " + holder + " x=" + x + " y=" + y);
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
			Log.i(TAG, "setDisplay x=" + x + " y=" + y + " w=" + w + " h=" + h);
			if (native_set_display(x, y, w < 0 ? 0 : w, h < 0 ? 0 : h)) {
				Log.v(TAG, "call native_set_display---");
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
	 * ���ض����ű�
	 * 
	 * @param fd
	 *            �����ű�,���fdΪ-1��ȡ��֮ǰ���صĽű�
	 * @return �ɹ�����true�����򷵻�false
	 */
	public boolean loadAnimation(FileDescriptor fd) {
		return native_load_a(fd) == 0;
	}

	/**
	 * ʵʩ����
	 * 
	 * @param id
	 *            ����ID
	 * @param p1
	 *            ����1
	 * @param p2
	 *            ����2
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 */
	public void actAnimation(int id, int p1, int p2, int flags) {
		if (id <= 0 || id > 256)
			throw new IndexOutOfBoundsException();
		native_act_a(id, p1, p2, flags);
	}

	/**
	 * ��������豸�Ĺ���������Ҫ����selectProgram���ܲ��ž���Ľ�Ŀ.
	 * <p>
	 * ����Ѿ�����start״̬��ֱ�ӷ���true
	 * 
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean start() {
		return native_start();
	}

	/**
	 * ֹͣ����
	 * <p>
	 * ����ջ��������棬ֹͣ����豸�Ĺ�����������ǰһ�εĶ��᷽ʽ���ử����߲��足�ᡣ
	 * <p>
	 * ������ʵ�ֵ㲥��seek��������Ҫ�建�����Ĳ�����
	 */
	public void stop() {
		native_stop();
		last_process_pts_time = 0;
	}

	/**
	 * ��ͣ����
	 * <p>
	 * ���ử��,����,������������
	 * 
	 * @return ���򷵻�true�����򷵻�false
	 */
	public boolean pause() {
		return native_pause();
	}

	/**
	 * �ָ�����
	 * <p>
	 * �˺���������Ҫ��{@link #pause()}��Ӧ
	 */
	public void resume() {
		native_resume();
	}

	/**
	 * �趨��������Ƶֹͣʱ��ʾģʽ
	 * 
	 * @param b
	 *            false��ʾ�����֡ģʽ��trueʹ�ܾ�֡ģʽ
	 * @param flags
	 *            ֻ�е�bΪtrueʱ��Ч �ο�<br> {@link #FLAG_VIDEO_FRAME_BLACK}<br>
	 *            {@link #FLAG_VIDEO_FRAME_FREEZE}<br>
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean setFreeze(boolean b, int flags) {
		return native_set_freeze(b, flags);
	}

	/**
	 * �õ���ǰ��Ŀ�Ĳ���ʱ��
	 * 
	 * @return ����ʱ��ֵ
	 */
	public long getPlayTime() {
		return native_play_time();
	}

	/**
	 * ��ȡ�����������е�ptsʱ��
	 * 
	 * @return ����ʱ��ֵ
	 */
	public long getPlayProcessPtsTime() {
		return last_process_pts_time;
	}

	long last_process_pts_time = 0;

	/**
	 * �õ�ѡ��Ľ�Ŀ��uri
	 * 
	 * @return �ַ���
	 */
	public String getSelectUri() {
		return uri;
	}

	String uri = null;

	boolean invalidProgramInfo(ProgramInfo p) {
		return !((p.hasValidAudioPID()) || p.hasValidVideoPID());
	}

	/**
	 * ѡ���Ŀ
	 * 
	 * @param p
	 *            ��Ŀ��Ϣ
	 * @return ʧ���򷵻�false,���򷵻�true
	 */
	public boolean selectProgram(ProgramInfo p) {
		if (invalidProgramInfo(p))
			return false;
		return native_select_program((uri = p.toString()), null, 0);
	}

	/**
	 * ѡ���Ŀ
	 * 
	 * @param p
	 *            ��Ŀ
	 * @param flags
	 *            ��־����, Ĭ���봫��0<br>
	 *            �ο�<br> {@link #FLAG_NOT_OBSERVE_PSI}<br>
	 *            {@link #FLAG_VIDEO_FRAME_BLACK}<br>
	 *            {@link #FLAG_VIDEO_FRAME_FREEZE}
	 * @return ʧ���򷵻�false,���򷵻�true
	 */
	public boolean selectProgram(ProgramInfo p, int flags) {
		if (invalidProgramInfo(p))
			return false;
		return native_select_program((uri = p.toString()), null, flags);
	}

	/**
	 * �����龰ģʽ
	 * <p>
	 * ϵͳ�����ݵ�ǰ�趨�������ܵ��Ż�
	 * <p>
	 * �����Զ�����ƽ��,����趨ΪPROFILE_TV����ϵͳ֧�ֵ�����½����ô˹���,���⻻̨���µ����������С��������PROFILE_MOVIE,
	 * �����ã����Ӱ��������̬��Χһ��ϴ���������Ѳ����á����ϵͳ֧�֣���ʱ��򿪸�����Чϵͳ�� <br>
	 * �ο�:<br>
	 * ��׼:{@link #PROFILE_STD}<br>
	 * ����:{@link #PROFILE_TV}<br>
	 * ��Ӱ:{@link #PROFILE_MOVIE}<br>
	 * ����:{@link #PROFILE_MUSIC}<br>
	 * ��̨:{@link #PROFILE_RADIO}<br>
	 * 
	 * @param type
	 *            ����
	 * @return ʧ���򷵻�false,���򷵻�true
	 */
	public boolean setProfile(String type) {
		return native_set_profile(type);
	}

	/**
	 * ��Ƶ֡ץ��
	 * <p>
	 * �ο�TeeveeCapturer����.
	 * 
	 * @param id
	 *            ץ��id��ʶֵ����TeeveeCapturer�������
	 * @return ʧ���򷵻�false,���򷵻�true
	 */
	public boolean captureVideoFrame(int id) {
		return native_capture_video(id);
	}

	/**
	 * ������Դ������ģʽ
	 * <p>
	 * <li>������Ϊtrue,������Ϊ������ģʽ����ʱ��Դ���ܻᱻϵͳ�ջأ���ʱ���˹رն���������¸Ļ�ǿ����ģʽ,����������ֱ��ʧ�ܣ�
	 * <li>����Ϊfalse,��û�ǿ���ã���������ʧ��.��ʧ�ܣ���Ӧ�ùرն���.
	 * 
	 * @return �ɹ�����true,���򷵻�false
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
	 * �Ƿ�Ϊ������ģʽ
	 * 
	 * @return ���򷵻�true,���򷵻�false
	 */
	public boolean isWeakMode() {
		return weak;
	}

	/**
	 * ������Ļ������
	 * 
	 * @param fd
	 *            ���(Ӧ��Ϊ��ʽ)
	 */
	public boolean recvSubtitle(FileDescriptor fd) {
		return native_recv_subt(fd);
	}
	
	/**
	 * ���ò���״̬������
	 * 
	 * @param l
	 *            ����
	 */
	public void setPlayStateListener(PlayStateListener l) {
		playsl = l;
	}

	private ProgramStateListener psl;

	/**
	 * ��ӽ�Ŀ״̬������
	 * 
	 * @param l
	 *            ����
	 */
	public void setProgramStateListener(ProgramStateListener l) {
		psl = l;
	}

	/**
	 * ��Ŀ״̬������
	 * <p>
	 * �����ϣ��ϵͳ���Ĳ��ŵ������߽��ô���Ϣ,�ο�
	 * {@link android.media.TeeveePlayer#FLAG_NOT_OBSERVE_PSI}
	 */
	public static interface ProgramStateListener {

		/**
		 * Ҫ������ѡ���Ŀ
		 * <p>
		 * ��һ������Ϊ��Ŀ��������仯�����<br>
		 * uri�ο�{@link android.net.telecast.ProgramInfo#fromString(String)}
		 * <p>
		 * ��uri����PMT Section���ݣ��������ѡ��Ϣ����ȷ��.<br>
		 * �ο�{@link android.net.telecast.ProgramInfo#getPmtSection()}
		 * 
		 * @param program_number
		 *            ��Ŀ��
		 * @param newuri
		 *            ����ѡ���uri
		 */
		void onProgramReselect(int program_number, String newuri);

		/**
		 * ��Ŀ�Ѿ��ж�
		 * <p>
		 * ���������Ϊ��Ŀ���Ƴ�,���߽�Ŀ������������仯��ԭ���µ�
		 * 
		 * @param program_number
		 *            ��Ŀ��
		 */
		void onProgramDiscontinued(int program_number);
	}

	/**
	 * ����״̬������
	 */
	public static interface PlayStateListener {
		/**
		 * ��ѡ�񱻽��ܺ���
		 * 
		 * @param player
		 *            ����������
		 * @param program_number
		 *            ��Ŀ��
		 */
		void onSelectionStart(TeeveePlayer player, int program_number);

		/**
		 * ���Ŵ���ʼ����
		 * <p>
		 * �յ�����Ϣ������Ϊ�����Ѿ���ʼ���������л���Ŀʱ����һ����Ŀ�ѿ�ʼ����
		 * 
		 * @param program_number
		 *            ��selectProgram�еĲ�����ָ��,���δָ����Ϊ0
		 */
		void onPlayProcessing(int program_number);

		/**
		 * ���Ŵ������
		 * <p>
		 * ���Ŵ����Ѿ�����,��ͨ������Ϊ�л���Ŀ���߶�����������ϵ��µ�
		 * 
		 * @param program_number
		 *            ��selectProgram�еĲ�����ָ��,���δָ����Ϊ0
		 */
		void onPlaySuspending(int program_number);

		/**
		 * ���Ŵ���
		 * <p>
		 * ֮�󲥷Ž���ֹͣ�����Ҫȷ������player�ٹ�����Ӧ�ó����Ҫ����ִ��prepare��һϵ�в���.
		 * 
		 * @param program_number
		 *            ��selectProgram�еĲ�����ָ��,���δָ����Ϊ0
		 * @param msg
		 *            ������Ϣ�ַ���
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
