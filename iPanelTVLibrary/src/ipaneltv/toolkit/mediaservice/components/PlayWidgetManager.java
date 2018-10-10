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
		// Ϊ��֤��̨�ٶȲ���Ӱ�죬�첽����������Ϣ
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
	 * �˷���Ӧ����ÿ��Activity.onResume��ʱ����ϵͳĬ������,����仯Ӧ��������
	 * <p>
	 * �ο�L10n�ж�����ַ���
	 * 
	 * @param code
	 *            ����
	 * @param msg
	 *            ��Ϣ
	 */
	protected void localizeErrorMessage(int code, String l10nString) {
		descErr.put(code, l10nString);
	}

	/** ������ʾ */
	protected void onShowWidget() {

	}

	protected abstract void onSetTeeveeWidget(int flags);

	protected abstract int onCkeckTeeveeWidget(int flags);

	/** ���������ʾ */
	protected abstract void onClearWidget();

	/** ��ʼ�л���Ŀ����֪�Ƿ�ֻ����Ƶ */
	protected abstract void onSwitchingStart(boolean audioOnly);

	/** ��̨(������Ŀ)����������д���errΪ������Ϣ */
	protected abstract void onSwitchingEnd(String err);

	/** �Ƿ����ź�,�Ƿ��д��� */
	protected abstract void onTransportState(String err);

	/** errorΪnull��ʾ�����Ѿ������������֮ǰ���д�����Ϣ��ʾ */
	protected abstract void onDescramblingState(String err);

	/** errΪֻ�ܿ�������Ϣ����Ϊnull���ʾ���� */
	protected abstract void onSmartCardState(String err);

	/** �ײ���������Ϣ����Ϊnull���ʾ���� */
	protected void onDecodeState(String error) {

	}

	/** errorΪnull��ʾ�����Ѿ������������֮ǰ���д�����Ϣ��ʾ */
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

	/** ���������ʾ */
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
