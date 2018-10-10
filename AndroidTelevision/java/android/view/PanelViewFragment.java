package android.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Fragment;
import android.net.telecast.TransportManager;
import android.os.Bundle;
import android.util.Log;

/**
 * ���(ǰ���)��ʾ��ͼFragment
 * <p>
 * ��������豸,��������Ӧ�ó�����ʾǰ�������ݡ�����豸�����ڣ����������κ�Ч��.<br>
 * ͬһ��Activity�в�Ӧ��Ӷ��PanelViewFragment���Ա������.
 */
public class PanelViewFragment extends Fragment {

	/** �ַ���-Ĭ��8888 */
	public static final int FALGS_TEXT_DEFAULT = 0x00;
	/** �ַ���-ʱ��88:88 */
	public static final int FALGS_TEXT_TIME = 0x01;
	/** �ַ���-����8.8.8.8 */
	public static final int FALGS_TIME_NUMBER = 0x02;
	/** ��׼ƥ�������ʾλ */
	public static final int FALGS_TEXT_RAW = 0x04;
	/**
	 * �ַ���-ǿ����ʾ
	 * <p>
	 * �Ǳ�Ҫ������ʹ��.<br>
	 * ���ڷǻ̬��Fragment������ǿ��Ҫ����ʾ��������л̬��Fragment�������ȼ����ߡ� ��˲���ȷ��ǿ����ʾһֱ��Ч��
	 */
	public static final int FALGS_TEXT_FORCE_SHOW = 0x08;

	/** ��-Ĭ�� */
	public static final int FALGS_LIGHT_DEFAULT = 0x00;
	/** ��-������Ƿ���Ч,��ʵ�ʲ��� */
	public static final int FALGS_LIGHT_CHECK_ONLY = 0x01;
	/** @hide ��-ǿ������(��ҪϵͳȨ��) */
	public static final int FALGS_LIGHT_FORCE_SHOW = 0x02;

	/** ʱ��-Ĭ��(ʱ��,����˸) */
	public static final int FALGS_TIME_DEFAULT = 0;
	/** ʱ��-���� */
	public static final int FALGS_TIME_mmSS = 0x01;
	/** ʱ��-���� */
	public static final int FALGS_TIME_MMDD = 0x02;
	
	/**
	 * ����ʵ��
	 * 
	 * @return ����
	 */
	public static PanelViewFragment createInstance() {
		String clsname = TransportManager.getSystemProperty("android.view.PanelViewFragment");
		Log.i("PanelViewFragment","clsname = " + clsname);
		if (clsname != null && !"".equals(clsname)) {
			try {
				return (PanelViewFragment) Class.forName(clsname).newInstance();
			} catch (Exception e) {
				Log.e("PanelViewFragment", "class :" + clsname + " is not valid!");
			}
		}
		return new PanelViewFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return null;// no UI
	}

	@Override
	public void onResume() {
		Log.i("PanelViewFragment","onResume");
		super.onResume();
		showing = true;
		setText(text, 0);
	}

	@Override
	public void onPause() {
		Log.i("PanelViewFragment","onPause");
		super.onPause();
		setText("", 0);
		showing = false;
	}

	/**
	 * ������ʾ������
	 * 
	 * @param txt
	 *            �ַ���
	 */
	public void setText(String txt) {
		setText(txt, 0);
	}

	/**
	 * ������ʾ������
	 * 
	 * @param txt
	 *            �ַ��� int
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 */
	public void setText(String txt, int flags) {
		Log.i("PanelViewFragment","setText txt = "+txt + "flags = " + flags);
		if (txt == null)
			throw new NullPointerException();
		text = txt;
		if (!showing && (flags & FALGS_TEXT_FORCE_SHOW) == 0)
			return;
		nativeSetText(txt, flags & (~FALGS_TEXT_FORCE_SHOW));
	}

	/**
	 * ����Ϊ��ʾʱ��
	 * <p>
	 * ϵͳ��ǰʱ��
	 */
	public void setTime() {
		setTime(new Date(), 0);
	}

	/**
	 * ����ʱ����ʾ
	 * 
	 * @param time
	 *            ʱ��
	 * @param flags
	 *            ��־,Ĭ��0
	 */
	public void setTime(Date time, int flags) {
		setTime(time, 0, flags);
	}

	/**
	 * ����ʱ����ʾ
	 * 
	 * @param time
	 *            ʱ��
	 * @param updatePeriod
	 *            ����Ƶ��
	 * @param flags
	 *            ��־,Ĭ��0
	 */
	public void setTime(Date time, int updatePeriod, int flags) {
		SimpleDateFormat sdf = null;
		if ((flags & FALGS_TIME_mmSS) != 0) {
			sdf = new SimpleDateFormat("mm:SS");
		} else if ((flags & FALGS_TIME_MMDD) != 0) {
			sdf = new SimpleDateFormat("MM:DD");
		} else {
			sdf = new SimpleDateFormat("HH:MM");
		}
		setText(sdf.format(time), FALGS_TEXT_TIME);
		setTimeUpdate(sdf, time.getTime(), updatePeriod < 0 ? 0 : updatePeriod);
	}

	/**
	 * ���õ�
	 * 
	 * @param name
	 *            ����
	 * @param on
	 *            �Ƿ��
	 */
	public boolean setLight(String name, boolean on) {
		return setLight(name, on ? 0xffffff : 0, 0);
	}

	/**
	 * ���õ�
	 * 
	 * @param name
	 *            ����
	 * @param color
	 *            ��ɫ
	 * @param flags
	 *            ��־��Ĭ��0
	 */
	public boolean setLight(String name, int color, int flags) {
		if (!showing && (flags & FALGS_LIGHT_FORCE_SHOW) == 0)
			return false;
		if (name == null)
			throw new NullPointerException();
		return nativeSetLight(name, color, flags & (~FALGS_LIGHT_FORCE_SHOW)) == 0;
	}

	void setTimeUpdate(final SimpleDateFormat sdf, final long t, int interval) {
		synchronized (timer) {
			if (twinkle != null) {
				twinkle.cancel();
				twinkle = null;
			}
			if (interval > 0) {
				if (interval < 300)
					interval = 300;
				final long base = System.currentTimeMillis() - t;
				twinkle = new TimerTask() {
					@Override
					public void run() {
						Date d = new Date(System.currentTimeMillis() - base);
						setText(sdf.format(d), FALGS_TEXT_TIME);
					}
				};
				timer.schedule(twinkle, interval, interval);
			}
		}
	}

	static native int nativeSetText(String txt, int flags);

	static native int nativeSetLight(String name, int color, int flags);

	private volatile boolean showing = false;
	private String text = "";
	Timer timer = new Timer();
	TimerTask twinkle = null;
}
