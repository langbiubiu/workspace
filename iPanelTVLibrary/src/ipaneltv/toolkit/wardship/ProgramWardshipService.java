package ipaneltv.toolkit.wardship;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonChannelService;
import ipaneltv.toolkit.JsonParcelable;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

public abstract class ProgramWardshipService extends JsonChannelService {
	static final String TAG = "ProgramWardshipService";

	protected List<Session> mSessions = new ArrayList<Session>();

	Session curSession;

	public abstract String onGetDatabaseUriString();

	public abstract String onPopPasswordDialog(Session session);

	public void notifyPasswordDialogResult(Session session, boolean b) {
		IPanelLog.d(TAG, "call back dialogresult session callingId= " + session.getCallingUID());
		synchronized (mSessions) {
			if (mSessions.contains(session)) {
				session.notifyJson(ProgramWardship.CMD_PWD_DIALOG_RESULT, b + "");
			}
		}
	}

	/**
	 * 通知回调重新加载数据，充program_wardship表中
	 * 
	 * @param session
	 * @param b
	 */
	public void notifyreloadCheckingList(Session session, boolean b) {
		IPanelLog.i(TAG, "notifyreloadCheckingList get");
		synchronized (mSessions) {
			IPanelLog.i("tjx0000", "lvby-->mSessions=" + mSessions);
			if (mSessions.contains(session)) {
				IPanelLog.i("tjx0000", "notifyreloadCheckingList get 1");
				session.notifyJson(ProgramWardship.CMD_DATABASE_CHANGE, b + "");
			}
		}
	}

	/**
	 * 通知回调重新加载数据，充program_wardship表中
	 * 
	 * @param b
	 */
	public void notifyReloadCheckingList(boolean b) {
		IPanelLog.i(TAG, "notifyreloadCheckingList get");
		synchronized (mSessions) {
			for (Session s : mSessions) {
				s.notifyJson(ProgramWardship.CMD_DATABASE_CHANGE, b + "");
			}
		}
	}

	/**
	 * 通知回调加载原始数据，从channels表中
	 * 
	 * @param session
	 * @param b
	 */
	public void notifyreloadOriginalData(Session session, boolean b) {
		synchronized (mSessions) {
			if (mSessions.remove(session)) {
				session.notifyJson(ProgramWardship.CMD_DATABASE_ORIGIN_LOAD, b + "");
			}
		}
	}

	@Override
	public Session createSession() {

		return new Session() {

			@Override
			public String onTransmit(int code, String json, JsonParcelable p, Bundle b) {
				IPanelLog.i(TAG, "session onTransmit code = " + code);

				switch (code) {
				case ProgramWardship.CODE_DATABASE_URI:
					return onGetDatabaseUriString();
				case ProgramWardship.CODE_POP_PWD_DIALOG:
					synchronized (mSessions) {
						if (!mSessions.contains(this))
							mSessions.add(this);
						try {
							return onPopPasswordDialog(this);
						} catch (Exception e) {
							mSessions.remove(this);
							IPanelLog.d(TAG, "popPasswordDialog error:" + e);
						}
					}
					break;
				case ProgramWardship.NOTIFY_UPDATE:
					break;
				default:
					break;
				}
				return null;
			}

			@Override
			public void onCreate() {
				IPanelLog.d(TAG, "(UID=" + getCallingUID() + ")");
				curSession = this;
				mSessions.add(this);
			}

			@Override
			public void onClose() {
			}
		};
	}

	public Session getCurSession() {
		return curSession;
	}

}
