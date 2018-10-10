package ipaneltv.toolkit.mediaservice.components;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.ReserveStateInterface;
import ipaneltv.toolkit.mediaservice.LiveNetworkApplication;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.SparseArray;

public abstract class PlayWidgetManager extends LiveNetworkApplication.AppComponent {
	static final String TAG = PlayWidgetManager.class.getSimpleName();
	private SparseArray<String> descErr = new SparseArray<String>();
	private HandlerThread handlerThread = new HandlerThread(PlayWidgetManager.class.getName());
	private boolean relased = false;
	protected final Handler handler;

	@SuppressWarnings("rawtypes")
	public PlayWidgetManager(LiveNetworkApplication app) {
		super(app);
		handlerThread.start();
		// 为保证切台速度不受影响，异步处理所有消息
		handler = new Handler(handlerThread.getLooper());
	}

	private List<PlayWidgetControl> ctrls = new ArrayList<PlayWidgetControl>();
	protected PlayWidgetControl strong = null;
	private Object mutex = new Object();

	public PlayWidgetControl createControl(PlayWidgetControlCallback callback) {
		PlayWidgetControl c = new PlayWidgetControl(callback);
		synchronized (ctrls) {
			ctrls.add(c);
			return c;
		}
	}

	public synchronized void release() {
		if (!relased) {
			relased = true;
			handlerThread.quit();
		}
	}

	/**
	 * 此方法应该在每次Activity.onResume的时候检查系统默认语言,如果变化应予以设置
	 * <p>
	 * 参考L10n中定义的字符串
	 * 
	 * @param code
	 *            代码
	 * @param msg
	 *            消息
	 */
	protected void localizeErrorMessage(int code, String l10nString) {
		descErr.put(code, l10nString);
	}

	/** 重新显示 */
	protected void onShowWidget() {

	}

	protected abstract void onSetTeeveeWidget(int flags);

	protected abstract int onCkeckTeeveeWidget(int flags);

	/** 清除所有显示 */
	protected abstract void onClearWidget();

	/** 开始切换节目，告知是否只有音频 */
	protected abstract void onSwitchingStart(boolean audioOnly);

	/** 换台(更换节目)结束，如果有错，则err为错误消息 */
	protected abstract void onSwitchingEnd(String err);

	/** 是否有信号,是否有错误 */
	protected abstract void onTransportState(String err);

	/** error为null表示解扰已经正常，需清除之前所有错误消息显示 */
	protected abstract void onDescramblingState(String err);

	/** err为只能卡错误消息，若为null则表示正常 */
	protected abstract void onSmartCardState(String err);

	/** 底层解码错误消息，若为null则表示正常 */
	protected void onDecodeState(String error) {

	}

	/** error为null表示解扰已经正常，需清除之前所有错误消息显示 */
	protected void onDescramblingState(int code, String err, ChannelKey key) {

	}

	private String opGetL10NStr(String msg) {
		if (msg == null)
			return null;
		String ret = null;
		int i, code;
		if ((i = msg.indexOf(']')) > 1) {
			try {
				if ((code = Integer.parseInt(msg.substring(1, i))) > 0)
					ret = descErr.get(code, msg);
			} catch (Exception e) {
			}
		}
		return ret == null ? msg : ret;
	}

	/** 清除所有显示 */
	protected void onClearWidget(Integer... args) {

	}

	public class PlayWidgetControl implements ReserveStateInterface {
		PlayWidgetControlCallback cb;

		PlayWidgetControl(PlayWidgetControlCallback callback) {
			cb = callback;
		}

		public void close() {
			synchronized (mutex) {
				loosen(true);
			}
		}

		@Override
		public boolean reserve() {
			boolean ret = false;
			synchronized (mutex) {
				try {
					if (strong == this)
						return true;
					if (strong == null) {
						strong = this;
						ret = true;
						return true;
					}
				} finally {
					// if (ret)
					// clearWidgetMessage();
				}
			}
			return ret;
		}

		@Override
		public void loosen(boolean clearState) {
			synchronized (mutex) {
				if (strong == this) {
					strong = null;
					if (clearState)
						clearWidgetMessage();
				}
			}
		}

		@Override
		public boolean isReserved() {
			synchronized (mutex) {
				return (strong == this);
			}
		}

		public void setTeeveeWidget(int flags) {
			synchronized (mutex) {
				if (strong == this)
					onSetTeeveeWidget(flags);
			}
		}

		public void checkTeeveeWidget(int flags) {
			synchronized (mutex) {
				if (strong == this) {
					int ret = onCkeckTeeveeWidget(flags);
					cb.onWidgetChecked(ret);
				}
			}
		}

		public void clearWidgetMessage(final Integer... flags) {
			IPanelLog.d(TAG, "clearWidgetMessage strong = " + strong + ";this = " + this);
			synchronized (mutex) {
				if (strong == this) {
					handler.post(new Runnable() {

						@Override
						public void run() {
							onClearWidget(flags);
							onClearWidget();
						}
					});
				}
			}
		}

		public void showMessage() {
			synchronized (mutex) {
				if (strong == this) {
					handler.post(new Runnable() {

						@Override
						public void run() {
							onShowWidget();
						}
					});
				}
			}
		}

		public void notifySwitchingStart(final boolean audioOnly) {
			synchronized (mutex) {
				if (strong == this)
					handler.post(new Runnable() {

						@Override
						public void run() {
							onSwitchingStart(audioOnly);
						}
					});
			}
		}

		public void notifySwitchingEnd(int code, final String error) {
			synchronized (mutex) {
				if (strong == this)
					handler.post(new Runnable() {

						@Override
						public void run() {
							onSwitchingEnd(error);
						}
					});
			}
		}

		public void notifyTransportState(final String err) {
			synchronized (mutex) {
				if (strong == this)
					handler.post(new Runnable() {

						@Override
						public void run() {
							onTransportState(err);
						}
					});
			}
		}

		public void notifyDescramblingState(int code, final String err) {
			synchronized (mutex) {
				IPanelLog.d(TAG, "notifyDescramblingState code = " + code + ";err = " + err
						+ ";strong = " + strong + ";this = " + this);
				if (strong == this)
					handler.post(new Runnable() {

						@Override
						public void run() {
							onDescramblingState(opGetL10NStr(err));
						}
					});
			}
		}

		public void notifyDescramblingState(final int code, final String err, final ChannelKey key) {
			synchronized (mutex) {
				IPanelLog.d(TAG, "notifyDescramblingState 22 code = " + code + ";err = " + err
						+ ";strong = " + strong + ";this = " + this);
				if (strong == this)
					handler.post(new Runnable() {

						@Override
						public void run() {
							onDescramblingState(code, err, key);
						}
					});
			}
		}

		public void notifySmartcardState(int code, final String err) {
			IPanelLog.d(TAG, "notifySmartcardState err = " + err + ";code = " + code);
			synchronized (mutex) {
				if (strong == this)
					handler.post(new Runnable() {

						@Override
						public void run() {
							onSmartCardState(opGetL10NStr(err));
						}
					});
			}
		}

		public void notifyDecodeState(final String err) {
			IPanelLog.d(TAG, "notifyDecodeState err = " + err);
			synchronized (mutex) {
				if (strong == this)
					handler.post(new Runnable() {

						@Override
						public void run() {
							onDecodeState(err);
						}
					});
			}
		}
	}

	public static interface PlayWidgetControlCallback {
		void onWidgetChecked(int flags);
	}
}
