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
 * 面板(前面板)显示视图Fragment
 * <p>
 * 如果存在设备,用来帮助应用程序显示前面板的内容。如果设备不存在，将不产生任何效果.<br>
 * 同一个Activity中不应添加多个PanelViewFragment，以避免混乱.
 */
public class PanelViewFragment extends Fragment {

	/** 字符串-默认8888 */
	public static final int FALGS_TEXT_DEFAULT = 0x00;
	/** 字符串-时间88:88 */
	public static final int FALGS_TEXT_TIME = 0x01;
	/** 字符串-数字8.8.8.8 */
	public static final int FALGS_TIME_NUMBER = 0x02;
	/** 精准匹配面板显示位 */
	public static final int FALGS_TEXT_RAW = 0x04;
	/**
	 * 字符串-强制显示
	 * <p>
	 * 非必要不建议使用.<br>
	 * 对于非活动态的Fragment，可以强制要求显示。但如果有活动态的Fragment，其优先级更高。 因此不能确保强制显示一直有效。
	 */
	public static final int FALGS_TEXT_FORCE_SHOW = 0x08;

	/** 灯-默认 */
	public static final int FALGS_LIGHT_DEFAULT = 0x00;
	/** 灯-仅检查是否有效,不实际操作 */
	public static final int FALGS_LIGHT_CHECK_ONLY = 0x01;
	/** @hide 灯-强制设置(需要系统权限) */
	public static final int FALGS_LIGHT_FORCE_SHOW = 0x02;

	/** 时间-默认(时分,不闪烁) */
	public static final int FALGS_TIME_DEFAULT = 0;
	/** 时间-分秒 */
	public static final int FALGS_TIME_mmSS = 0x01;
	/** 时间-月日 */
	public static final int FALGS_TIME_MMDD = 0x02;
	
	/**
	 * 创建实例
	 * 
	 * @return 对象
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
	 * 设置显示的文字
	 * 
	 * @param txt
	 *            字符串
	 */
	public void setText(String txt) {
		setText(txt, 0);
	}

	/**
	 * 设置显示的文字
	 * 
	 * @param txt
	 *            字符串 int
	 * @param flags
	 *            标志，默认为0
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
	 * 设置为显示时间
	 * <p>
	 * 系统当前时间
	 */
	public void setTime() {
		setTime(new Date(), 0);
	}

	/**
	 * 设置时间显示
	 * 
	 * @param time
	 *            时间
	 * @param flags
	 *            标志,默认0
	 */
	public void setTime(Date time, int flags) {
		setTime(time, 0, flags);
	}

	/**
	 * 设置时间显示
	 * 
	 * @param time
	 *            时间
	 * @param updatePeriod
	 *            更新频度
	 * @param flags
	 *            标志,默认0
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
	 * 设置灯
	 * 
	 * @param name
	 *            名称
	 * @param on
	 *            是否打开
	 */
	public boolean setLight(String name, boolean on) {
		return setLight(name, on ? 0xffffff : 0, 0);
	}

	/**
	 * 设置灯
	 * 
	 * @param name
	 *            名称
	 * @param color
	 *            颜色
	 * @param flags
	 *            标志，默认0
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
