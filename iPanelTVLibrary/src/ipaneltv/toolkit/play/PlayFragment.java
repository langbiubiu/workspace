package ipaneltv.toolkit.play;

import ipaneltv.toolkit.media.MediaSessionBase;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface.Provider;
import android.content.Context;
import android.util.Log;

public class PlayFragment extends MediaSessionBase {
	private final static String TAG = PlayFragment.class.getName();
	public static String PLAY_SERVICE_NAME = "cn.ipanel.tvapps.network.NcPlayService";;
	public static String SRC_SERVICE_NAME = "com.ipanel.apps.common.tsvodsrcservice";
	private DvbLivePlayer live;
	private LocalShiftPlayer shift;
	private IpqamShiftPlayer qamshift;
	private PlayManager manager;
	private NgodShiftSource source;
	Context context;
	boolean prepared = false;
	Object mutex = new Object();
	String provider;

	public PlayFragment(String uuid, Context context) {
		super(uuid, PLAY_SERVICE_NAME, SRC_SERVICE_NAME);
		this.context = context;
		provider = Provider.Ngod.getName();
		manager = new PlayManager(this);
		live = new DvbLivePlayer(manager);
		shift = new LocalShiftPlayer(manager);
		qamshift = new IpqamShiftPlayer(manager);
		source = new NgodShiftSource(manager);
		entrustSession("play", live);
		entrustSession("play", shift);
		entrustSession("play", qamshift);
		entrustSession("source", source);
	}

	@Override
	public boolean prepare() {
		synchronized (mutex) {
			if (!prepared) {
				prepared = true;
				// 将播放器托管给Fragment，进行资源回收管理
				// 加入同一组(play),当选择播放时，只有一个播放器实例处于当前，达到切换资源的目的
				manager.prepare();
				return super.prepare();
			}
			return true;
		}
	}

	@Override
	public void suspend(boolean clearState) {
		synchronized (mutex) {
			if (prepared) {
				manager.suspend();
				super.suspend(clearState);
			}
		}
	}

	@Override
	public void release() {
		synchronized (mutex) {
			if (prepared) {
				prepared = false;
				manager.release();
				super.release();
			}
		}
	}

	DvbLivePlayer getLivePlayer() {
		synchronized (mutex) {
			if (prepared) {
				if (chooseSession(live))
					return live;
			}
			return null;
		}
	}

	DvbLivePlayer tryGetLivePlayer() {
		if (isSessionChoosed(live))
			return live;
		return null;
	}

	LocalShiftPlayer tryGetShiftPlayer() {
		if (isSessionChoosed(shift))
			return shift;
		return null;
	}

	IpqamShiftPlayer tryGetQamShiftPlayer() {
		if (isSessionChoosed(qamshift))
			return qamshift;
		return null;
	}

	NgodShiftSource tryGetShiftSource() {
		if (isSessionChoosed(source))
			return source;
		return null;
	}

	LocalShiftPlayer getShiftPlayer() {
		synchronized (mutex) {
			if (prepare()) {
				if (chooseSession(shift))
					return shift;
			}
			return null;
		}
	}

	IpqamShiftPlayer getQamShiftPlayer() {
		synchronized (mutex) {
			if (prepared) {
				if (chooseSession(qamshift))
					return qamshift;
			}
			return null;
		}
	}

	NgodShiftSource getShiftSource() {
		synchronized (mutex) {
			if(prepared){
				if (chooseSession(source))
					return source;	
			}
			return null;
		}
	}

	public PlayInterface getPlayInterface(PlayCallback callback) {
		manager.setCallback(callback);
		return manager;
	}

	@Override
	public void onAllEntrusteeConnected(String group) {
		super.onAllEntrusteeConnected(group);

		synchronized (mutex) {
			if (prepared) {
				manager.notifyPlayContextReady(group);
			}
		}
		Log.d(TAG, "onAllEntrusteeConnected :" + group);
	}
}
