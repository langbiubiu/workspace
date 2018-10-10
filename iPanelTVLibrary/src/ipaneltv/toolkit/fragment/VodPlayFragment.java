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
	 * 创建实例
	 * 
	 * @param playServiceName
	 *            播放服务的名称
	 * @param shiftServiceName
	 *            时移服务的名称
	 * @return 对象实例
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
		// 将播放器托管给Fragment，进行资源回收管理
		// 加入同一组(play),当选择播放时，只有一个播放器实例处于当前，达到切换资源的目的
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
