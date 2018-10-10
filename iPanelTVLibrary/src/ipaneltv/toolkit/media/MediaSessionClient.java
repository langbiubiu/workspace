package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonChannel;
import ipaneltv.toolkit.JsonParcelable;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;

public abstract class MediaSessionClient implements MediaSessionInterface {
	final static String TAG = MediaSessionClient.class.getSimpleName();
	protected final JsonChannel channel;
	private Context context;
	private volatile boolean reserved = false, closed = false, conn = false;

	/**
	 * ��������
	 * 
	 * @param context
	 *            ������
	 * @param serviceName
	 *            ��������
	 * @param sessionName
	 *            �Ự����
	 */
	public MediaSessionClient(Context context, String serviceName, String sessionName) {
		this.context = context;
		channel = new JsonChannel(context, serviceName, sessionName) {

			public void onChannelConnected() {
				onServiceConnected();
				ServiceConnectionListener l = lis;
				conn = true;
				if (l != null) {
					l.onServiceConnected(MediaSessionClient.this);
				}
			}

			public void onChannelDisconnectted() {
				conn = false;
				onServiceLost();
			}

			@Override
			public void onCallback(int code, String json, JsonParcelable p, Bundle b)
					throws JSONException {
				IPanelLog.d(this.toString(), "onCallback code = "+ code +";json = "+ json);
				MediaSessionClient.this.onCallback(code, json, p, b);
			}
		};
	}

	private ServiceConnectionListener lis;

	void setServiceConnectionListener(ServiceConnectionListener lis) {
		this.lis = lis;
	}

	static interface ServiceConnectionListener {
		void onServiceConnected(MediaSessionClient c);
	}

	public final Context getContext() {
		return context;
	}

	public void setArguments(Bundle b){
		channel.setArguments(b);
	}
	
	protected void setLowPriority(boolean soft) {
		Bundle b = channel.getArguments();
		if (b == null) {
			b = new Bundle();
		}
		b.putInt("priority", 4);
		b.putBoolean("soft", soft);
		channel.setArguments(b);
	}

	protected void setLowPriority() {
		setLowPriority(true);
	}

	protected final void connectToService() {
		channel.connect();
	}

	/**
	 * �����Դ�Ŀ���Ȩ
	 * 
	 * @return �ɹ�����true,���򷵻�false
	 */
	@Override
	public final boolean reserve() {
		if (entrusted)// �ѱ��йܶ���Ҫ���ô˷���
			throw new RuntimeException("do not invoke when object has been entrusted!");
		return reserveEntrusted();
	}

	final boolean reserveEntrusted() {
		synchronized (channel) {
			if (reserved)
				return true;
			String ret = channel.transmit(__ID_reserve);
			if (reserved = (ret != null ? "true".equals(ret) : false))
				onReserved();
			return reserved;
		}
	}

	protected void onReserved() {
	}

	protected void onLoosened() {
	}

	/**
	 * ������Դ����.
	 * <p>
	 * ϵͳ�ڲ���ͻ������½�����������Դ.<br>
	 * !!�����״̬!!
	 */
	public final void loosen() {
		loosen(true);
		onLoosened();
	}

	/**
	 * ������Դ����.
	 * <p>
	 * ϵͳ�ڲ���ͻ������½�����������Դ
	 * 
	 * @param keepState
	 *            �Ƿ����״̬
	 */
	@Override
	public final void loosen(boolean clearState) {
		IPanelLog.d(TAG, "loosen###########client channel = "+ channel +";reserved = "+ reserved);
		synchronized (channel) {
			IPanelLog.d(TAG, "loosen###########client 11");
			if (reserved) {
				reserved = false;
				channel.transmit(__ID_loosen,clearState+"");
			}
		}
		IPanelLog.d(TAG, "loosen###########client end channel = "+ channel);
	}

	public boolean isShotted() {
		return channel.isShotted();
	}

	public final boolean isReserved() {
		return reserved;
	}

	public final boolean isClosed() {
		return closed;
	}

	public final boolean isConnected() {
		return conn;
	}

	/**
	 * �رն���������
	 */
	public final void close() {
		if (entrusted)// �ѱ��йܶ���Ҫ���ô˷���
			throw new RuntimeException("do not invoke when object has been entrusted!");
		closeEnstructed();
	}

	final void closeEnstructed() {
		synchronized (channel) {
			if (!closed) {
				IPanelLog.d(TAG, "channel.disconnect()");
				closed = true;
				channel.disconnect();
			}
		}
	}

	/**
	 * ������������ʱ���ã�����ɸ�д�η���
	 */
	public void onServiceConnected() {
	}

	public void onServiceLost() {
	}

	protected abstract void onCallback(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException;

	private Object tag;

	Object getTag() {
		return tag;
	}

	void setTag(Object tag) {
		this.tag = tag;
	}

	private boolean entrusted = false;

	void setEntrusted(boolean entrusted) {
		this.entrusted = entrusted;
	}
}
