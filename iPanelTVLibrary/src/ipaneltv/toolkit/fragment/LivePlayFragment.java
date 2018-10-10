package ipaneltv.toolkit.fragment;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.fragment.PlayActivityInterface.LivePlayBaseInterface;
import ipaneltv.toolkit.media.MediaSessionFragment;
import android.os.Bundle;

public class LivePlayFragment extends MediaSessionFragment {
	private final static String TAG = LivePlayFragment.class.getSimpleName();
	private PlayLivePlayer live;
	private PlayShiftPlayer shift;
	private LivePlayManager manager;
	private PlayShiftSource source;

	/**
	 * ����ʵ��
	 * 
	 * @param playServiceName
	 *            ���ŷ��������
	 * @param shiftServiceName
	 *            ʱ�Ʒ��������
	 * @return ����ʵ��
	 */
	public static LivePlayFragment createInstance(String uuid, String playServiceName,
			String sourceServiceName) {
		Bundle b = MediaSessionFragment.createArguments(uuid, playServiceName, sourceServiceName);
		LivePlayFragment f = new LivePlayFragment();
		f.setArguments(b);
		return f;
	}

	public LivePlayFragment() {
		manager = new LivePlayManager(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		live = new PlayLivePlayer(manager);
		shift = new PlayShiftPlayer(manager);
		source = new PlayShiftSource(manager);
		// ���������йܸ�Fragment��������Դ���չ���
		// ����ͬһ��(play),��ѡ�񲥷�ʱ��ֻ��һ��������ʵ�����ڵ�ǰ���ﵽ�л���Դ��Ŀ��
		entrustSession("play", live);
		entrustSession("play", shift);
		entrustSession("source", source);
		manager.prepare();
	}

	public void onPause() {
		manager.suspend();
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		IPanelLog.d(TAG, "onStop");
	}

	public void onResume() {
		super.onResume();
		manager.resume();
	}

	public void onDestroy() {
		manager.release();
		super.onDestroy();
	}

	PlayLivePlayer getLivePlayer() {
		if (chooseSession(live))
			return live;
		return null;
	}

	PlayLivePlayer tryGetLivePlayer() {
		if (isSessionChoosed(live))
			return live;
		return null;
	}

	PlayShiftPlayer tryGetShiftPlayer() {
		if (isSessionChoosed(shift))
			return shift;
		return null;
	}

	PlayShiftSource tryGetShiftSource() {
		if (isSessionChoosed(source))
			return source;
		return null;
	}

	PlayShiftPlayer getShiftPlayer() {
		if (chooseSession(shift))
			return shift;
		return null;
	}

	PlayShiftSource getShiftSource() {
		if (chooseSession(source))
			return source;
		return null;
	}

	public LivePlayBaseInterface getPlayInterface(LivePlayBaseInterface.Callback callback) {
		manager.setCallback(callback);
		return manager;
	}

	@Override
	public void onAllEntrusteeConnected(String groug) {
		super.onAllEntrusteeConnected(groug);
		manager.notifyPlayContextReady(groug);
		IPanelLog.d(TAG, "onAllEntrusteeConnected :" + groug);
	}
}
