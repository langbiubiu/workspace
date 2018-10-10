package ipaneltv.toolkit.gl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.view.KeyEvent;

public class GlSurfaceWindow extends GLSurfaceView {

	protected static final String TAG = "GlSurfaceWindow";
	protected static GlSurfaceWindow mGlWindow;
	GlSurfaceWindowCallback mGlSurfaceWindowCallback;

	protected static float LEFT = -1.7777778f; // ͸�������߽�
	protected static float RIGHT = 1.7777778f; // ͸������ұ߽�
	protected static final float BOTTOM = -1.0f; // ͸������±߽�
	protected static final float TOP = 1.0f; // ͸������ϱ߽�
	protected static final float NEAR = 15.0f; // ͸����Ľ��߽�
	protected static final float FAR = 100.0f; // ͸�����Զ�߽�

	protected static int FULL_SCREEN_WIDTH = 1920; // �ӿڵĿ�
	protected static int FULL_SCREEN_HEIGHT = 1080; // �ӿڵĸ�

	protected static boolean BUSY = false; // ϵͳ��æ
	protected static final int SLEEP = 10; // ��Ⱦ�ӳ�

	protected GLRenderer mRenderer;
	protected GlFrame mGlFrame;

	protected boolean mInitFinish = false;

	protected Activity mActivity;
	protected Object mutex = new Object();

	public GlSurfaceWindow(Activity activity) {
		super(activity);
		mActivity = activity;

		if (mGlWindow != null) {
			mGlWindow = null;
			// throw new
			// IllegalArgumentException("not has more GlRoot Object!");
		}

		mGlWindow = this;

		setZOrderOnTop(true);
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		setRenderer(new GLRenderer());
		getHolder().setFormat(PixelFormat.RGBA_8888);

		setFocusable(true);
	}

	public Activity getActivity() {
		return mActivity;
	}

	public synchronized void onDestory() {
		if (mGlWindow != null) {
			mGlWindow = null;
		}
	}

	public static void setApplicationBusyNow(boolean b) {
		if (mGlWindow == null)
			return;
		BUSY = b;
		if (BUSY)
			mGlWindow.setRenderMode(RENDERMODE_WHEN_DIRTY); // ������Ⱦ
		else
			mGlWindow.setRenderMode(RENDERMODE_CONTINUOUSLY); // ������Ⱦ
	}

	public static int getFullScreenWidth() {
		return FULL_SCREEN_WIDTH;
	}

	public static int getFullScreenHeight() {
		return FULL_SCREEN_HEIGHT;
	}

	public void setGlFrame(GlFrame frame) {
		synchronized (mutex) {
			if (!mInitFinish) {
				try {
					mutex.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		if (mGlFrame != null) {
			mGlFrame.mGlSurfaceWindow = null;
			mGlFrame = null;
		}
		mGlFrame = frame;
		mGlFrame.mGlSurfaceWindow = this;
		GlSurfaceWindow.setApplicationBusyNow(false);
	}

	public GlFrame getGlFrame() {
		return mGlFrame;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		queueEvent(new KeyEventRunnable(event));
		GlSurfaceWindow.setApplicationBusyNow(false);
		if (keyCode == 164) {
			return false;
		}
		if (keyCode == 178) {
			return true;
		}
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		queueEvent(new KeyEventRunnable(event));
		GlSurfaceWindow.setApplicationBusyNow(false);
		return true;
	}

	protected void finish() {
		if (mActivity == null)
			return;
		mGlWindow = null;
		setApplicationBusyNow(true);
		mActivity.finish();
	}

	class GLRenderer implements Renderer {

		long startTime = -1;
		int fps = 0;

		public GLRenderer() {
			mRenderer = this;
		}

		protected void onDrawGlFrame(GL10 gl, GlFrame glFrame) {
			if (glFrame == null || !glFrame.visible)
				return;
			if (!glFrame.onUpdate(gl))
				setRenderMode(RENDERMODE_WHEN_DIRTY);
		}

		public void onDrawFrame(GL10 gl) {
			// TODO Auto-generated method stub
			if (startTime == -1) {
				startTime = System.currentTimeMillis();
			}
			fps++;

			if (BUSY) {
				try {
					Thread.sleep(SLEEP);
				} catch (InterruptedException e) {
				}
			}

			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);// ����
			gl.glLoadIdentity(); // ����ģ�;���
			gl.glTranslatef(LEFT, TOP, -NEAR); // �����ϽǶ�λΪ0,0��
			gl.glRotatef(180, 1.f, 0.f, 0.f); // ��X����ת180��,��Yƽ̨���µ�����ϵ��Ϊ����
			onDrawGlFrame(gl, mGlFrame); // ���Ƴ���

			if (System.currentTimeMillis() - startTime >= 1000) {
				mGlSurfaceWindowCallback.surfaceDrawFrameFPS(fps);
				fps = 0;
				startTime = -1;
			}
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			gl.glDisable(GL10.GL_DITHER);
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST); // ��͸�ӽ�������
			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // ����������ɫ

			// gl.glClearDepthf(1.0f); // ������Ȼ���
			// gl.glEnable(GL10.GL_DEPTH_TEST); // ������Ȳ���
			// gl.glDepthFunc(GL10.GL_LEQUAL); // ������Ȳ��Ե�����
			// gl.glEnable(GL10.GL_CULL_FACE); // ���ñ���ü�

			gl.glEnable(GL10.GL_TEXTURE_2D); // ����2D����
			gl.glShadeModel(GL10.GL_SMOOTH); // ������Ӱƽ��
			gl.glFrontFace(GL10.GL_CCW); // ǰ��������ʱ�뷽ʽ

			gl.glEnable(GL10.GL_BLEND); // ��������ϳ�
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA); // �ϳɷ�ʽ��SRC_ALPHA�ķ�ʽ
		}

		public void onSurfaceChanged(GL10 gl, int width, int height) {
			FULL_SCREEN_WIDTH = width;
			FULL_SCREEN_HEIGHT = height;
			RIGHT = (float) FULL_SCREEN_WIDTH / FULL_SCREEN_HEIGHT;
			LEFT = -RIGHT;

			gl.glViewport(0, 0, FULL_SCREEN_WIDTH, FULL_SCREEN_HEIGHT); // ����OpenGL�ӿڵĴ�С
			gl.glMatrixMode(GL10.GL_PROJECTION); // ����ͶӰ����
			gl.glLoadIdentity(); // ����ͶӰ����
			gl.glFrustumf(LEFT, RIGHT, BOTTOM, TOP, NEAR, FAR); // �����Ӵ���͸���壩�Ĵ�С
			gl.glMatrixMode(GL10.GL_MODELVIEW); // ָ�����κ��µı任��Ӱ��ģ�͹۲����
			gl.glLoadIdentity(); // ����ͶӰ����
			synchronized (mutex) {
				mInitFinish = true;
				mutex.notifyAll();
			}
			mGlSurfaceWindowCallback.initGlSurfaceWindow();
		}
	}

	protected class KeyEventRunnable implements Runnable {

		KeyEvent mKeyEvent;

		KeyEventRunnable(KeyEvent event) {
			mKeyEvent = event;
		}

		public void run() {
			if (mGlFrame == null)
				return;
			mGlFrame.dispatchKeyEvent(mKeyEvent);
		}
	}

	public void setGlSurfaceWindowCallback(
			GlSurfaceWindowCallback mGlSurfaceWindowCallback) {
		// TODO Auto-generated method stub
		this.mGlSurfaceWindowCallback = mGlSurfaceWindowCallback;
	}

	public interface GlSurfaceWindowCallback {
		public void initGlSurfaceWindow();

		public void surfaceDrawFrameFPS(int fps);
	}

}