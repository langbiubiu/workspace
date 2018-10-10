package ipaneltv.toolkit.fragment;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.fragment.PlayActivityInterface.VodPlayBaseInterface;
import ipaneltv.toolkit.media.MediaSessionFragment;
import android.os.Bundle;

public class VodPlayFragment extends MediaSessionFragment {
	static final String TAG = VodPlayFragment.class.getSimpleName();
	private IpQamPlayer qamPlayer;
	private LocalSockPlayer localsockPlayer;
	private VodPlayManager manager;
	private VodPlaySource source;
	
	/**
	 * ����ʵ��
	 * 
	 * @param playServiceName
	 *            ���ŷ��������
	 * @param shiftServiceName
	 *            ʱ�Ʒ��������
	 * @return ����ʵ��
	 */
	public static VodPlayFragment createInstance(String uuid, String playServiceName,
			String sourceServiceName) {
		Bundle b = MediaSessionFragment.createArguments(uuid, playServiceName, sourceServiceName);
		VodPlayFragment f = new VodPlayFragment();
		f.setArguments(b);
		return f;
	}

	public VodPlayFragment() {
		manager = new VodPlayManager(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		qamPlayer = new IpQamPlayer(manager);
		localsockPlayer = new LocalSockPlayer(manager);
		source = new VodPlaySource(manager);
		// ���������йܸ�Fragment��������Դ���չ���
		// ����ͬһ��(play),��ѡ�񲥷�ʱ��ֻ��һ��������ʵ�����ڵ�ǰ���ﵽ�л���Դ��Ŀ��
		entrustSession("play", qamPlayer);
		entrustSession("play", localsockPlayer);
		entrustSession("source", source);
		manager.prepare();
	}

	@Override
	public void onStop() {
		super.onStop();
		IPanelLog.d(TAG, "onStop");
	}

	public void onDestroy() {
		manager.release();
		super.onDestroy();
	}

	LocalSockPlayer getLocalsockPlayer() {
		if (chooseSession(localsockPlayer))
			return localsockPlayer;
		return null;
	}

	IpQamPlayer getIpqamPlayer() {
		if (chooseSession(qamPlayer))
			return qamPlayer;
		return null;
	}

	VodPlaySource getVodSource() {
		if (chooseSession(source))
			return source;
		return null;
	}

	IpQamPlayer tryGetIpqamPlayer() {
		if (isSessionChoosed(qamPlayer))
			return qamPlayer;
		return null;
	}

	LocalSockPlayer tryGetLocalsockPlayer() {
		if (isSessionChoosed(localsockPlayer))
			return localsockPlayer;
		return null;
	}
	
	VodPlaySource tryGetVodSource() {
		if (isSessionChoosed(source))
			return source;
		return null;
	}

	public VodPlayBaseInterface getPlayInterface(VodPlayBaseInterface.Callback callback) {
		manager.setCallback(callback);
		return manager;
	}
	
	private boolean bSourceReady = false, bPlayerReady = false;

	@Override
	public void onAllEntrusteeConnected(String groug) {
		super.onAllEntrusteeConnected(groug);

		IPanelLog.d(TAG, "onAllEntrusteeConnected :" + groug);
		if ("source".equals(groug)) {
			bSourceReady = true;
		}

		if ("player".equals(groug)) {
			bPlayerReady = true;
		}

		if (bSourceReady && bPlayerReady) {
			manager.notifyPlayContextReady(groug);
		}
	}
}
