package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;

/**
 * 是一个不可见的Fragment.用来管理播放的上下文环境
 */
public class MediaSessionBase{
	/** bundle 参数，字符串类型 */
	public static final String ARG_NETWORK_UUID = "network_uuid";
	/** bundle 参数，字符串类型 */
	public static final String ARG_PLAY_SERVICE_NAME = "play_service_name";
	/** bundle 参数，字符串类型 */
	public static final String ARG_SOURCE_SERVICE_NAME = "source_service_name";

	final static String TAG = MediaSessionBase.class.getSimpleName();
	/** 自动创建播放播放管理器 */
	public static final int FLAG_AUTO_CREATE = 0x1;
	private HashMap<String, MediaSessionClientGroup> sessions = new HashMap<String, MediaSessionClientGroup>();
	private boolean init = false;

	private class MediaSessionClientGroup implements MediaSessionClient.ServiceConnectionListener {
		int countConnected = 0;
		String name;
		List<MediaSessionClient> choosed = new LinkedList<MediaSessionClient>();
		List<MediaSessionClient> list = new LinkedList<MediaSessionClient>();

		void loosenChoosed(MediaSessionClient s,boolean clearState) {
			for (MediaSessionClient c : choosed) {
				if (c != s)
					c.loosen(clearState);
			}
			choosed.clear();
			if (s != null) {
				IPanelLog.d(TAG, "loosenChoosed add s = " + s);
				choosed.add(s);
			}
		}

		void closeAll() {
			loosenChoosed(null,true);
			for (MediaSessionClient c : list) {
				c.closeEnstructed();
			}
			list.clear();
		}

		boolean isChoosed(MediaSessionClient c) {
			boolean ret = choosed.contains(c);
			IPanelLog.d(TAG, "ret = " + ret);
			return ret;
		}

		boolean choose(MediaSessionClient c, boolean ex) {
			IPanelLog.d(TAG, "choose c = " + c + ";ex =" + ex);
			if (!c.isReserved()) {
				IPanelLog.d(TAG, "choose remove c = " + c);
				choosed.remove(c);
			}
			if (choosed.contains(c)) {
				if (ex)
					loosenChoosed(c,true);
				return true;
			}
			IPanelLog.d(TAG, "choose list.contains(c) = "+ list.contains(c));
			if (list.contains(c)) {
				if (ex)
					loosenChoosed(null,true);
				IPanelLog.d(TAG, "choose  c.reserveEntrusted ");
				if (c.reserveEntrusted()) {
					IPanelLog.d(TAG, "choose add c = " + c);
					choosed.add(c);
					return true;
				}
			}
			return false;
		}

		void add(MediaSessionClient c) {
			if (c.getTag() != null)
				throw new RuntimeException("MediaSessionClient can only entrust in one group("
						+ name + ") once");
			c.setTag(this);
			list.add(c);
			IPanelLog.d(TAG, "list add c = " + c);
			c.setEntrusted(true);
		}

		void remove(MediaSessionClient c) {
			IPanelLog.d(TAG, "list remove c = " + c);
			list.remove(c);
		}

		public void connectAll() {
			for (MediaSessionClient c : list) {
				if (!c.isShotted()) {
					c.connectToService();
				}
			}
		}

		@Override
		public void onServiceConnected(MediaSessionClient c) {
			++countConnected;
			IPanelLog.d(TAG, "onServiceConnected....... name = "+ name +";countConnected = "+ countConnected+" list.size() = "+ list.size());
			if (countConnected == list.size()) {
				onAllEntrusteeConnected(name);
			}
		}
	}

	public void onAllEntrusteeConnected(String groug) {

	}

	public static Bundle createArguments(String uuid, String playServiceName,
			String sourceServiceName) {
		Bundle b = new Bundle();
		b.putString(ARG_NETWORK_UUID, uuid);
		b.putString(ARG_PLAY_SERVICE_NAME, playServiceName);
		b.putString(ARG_SOURCE_SERVICE_NAME, sourceServiceName);
		return b;
	}

	public MediaSessionBase() {
	}

	public String getNetworkUUID() {
		return uuid;
	}

	public String getPlayServiceName() {
		return play_service_name;
	}

	public String getSourceServiceName() {
		return source_service_name;
	}

	/**
	 * 应该在子类的onCreate函数中创建Session后，调用此方法进行托管，以便session能被适当的启用和禁用
	 * 
	 * @param client
	 *            会话
	 * @param group
	 */
	public void entrustSession(String group, MediaSessionClient session) {
		if (!init)
			throw new RuntimeException("you should invoke onCreate()");
		synchronized (sessions) {
			MediaSessionClientGroup sg = sessions.get(group);
			if (sg == null) {
				sg = new MediaSessionClientGroup();
				sg.name = group;
				sessions.put(group, sg);
			}
			sg.add(session);
			session.setServiceConnectionListener(sg);
		}
	}

	public boolean chooseSession(MediaSessionClient c) {
		return chooseSession(c, true);
	}

	public void removeSession(String group, MediaSessionClient session) {
		if (!init)
			throw new RuntimeException("you should invoke onCreate()");
		synchronized (sessions) {
			MediaSessionClientGroup sg = sessions.get(group);
			if (sg != null) {
				sg.remove(session);
			}
		}
	}

	public void connectSeeeions() {
		for (MediaSessionClientGroup sg : sessions.values()) {
			sg.connectAll();
		}
	}

	public boolean chooseSession(MediaSessionClient c, boolean exclusiveInGroup) {
		synchronized (sessions) {
			IPanelLog.d(TAG, "chooseSession c = " + c + "; exclusiveInGroup = " + exclusiveInGroup);
			MediaSessionClientGroup sg = (MediaSessionClientGroup) c.getTag();
			if (sg == null)
				throw new RuntimeException("you can only choose session entrusted before!");
			return sg.choose(c, exclusiveInGroup);
		}
	}

	public boolean isSessionChoosed(MediaSessionClient c) {
		synchronized (sessions) {
			IPanelLog.d(TAG, "isSessionChoosed c = " + c);
			MediaSessionClientGroup sg = (MediaSessionClientGroup) c.getTag();
			if (sg == null)
				throw new RuntimeException("you can only choose session entrusted before!");
			return sg.isChoosed(c);
		}
	}

	public void loosenAllSession() {
		loosenAllSession(false);
	}

	void loosenAllSession(boolean close) {
		loosenAllSession(close,true);
	}
	
	public void loosenAllSession(boolean close,boolean clearState){
		synchronized (sessions) {
			for (MediaSessionClientGroup sg : sessions.values()) {
				sg.loosenChoosed(null,clearState);
				if (close)
					sg.closeAll();
			}
		}
	}

	String uuid,play_service_name,source_service_name;
	
	public MediaSessionBase(String uuid,String play_service_name,String source_service_name){
		this.uuid = uuid;
		this.play_service_name = play_service_name;
		this.source_service_name = source_service_name;
		init = true;
	}
	
	public boolean prepare(){
		synchronized (sessions) {
			for (MediaSessionClientGroup sg : sessions.values()) {
				sg.connectAll();
			}
		}
		return true;
	}

	public void suspend(boolean clearState){
		IPanelLog.d(TAG, "suspend in clearState = " + clearState);
		loosenAllSession(false,clearState);
		IPanelLog.d(TAG, "suspend out");
	}

    public void release(){
    	IPanelLog.d(TAG, "release in");
    	init = false;
    	loosenAllSession(true);
    	IPanelLog.d(TAG, "release out");
    }

}
