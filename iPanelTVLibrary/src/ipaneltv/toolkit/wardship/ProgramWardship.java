package ipaneltv.toolkit.wardship;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonChannel;
import ipaneltv.toolkit.JsonParcelable;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseBooleanArray;

/**
 * 节目观看监护
 * <p>
 * 设计用来实现网络应用与CA间完成家长控制等功能
 */
public abstract class ProgramWardship {

	/** 用作NetworkManager.getNetworkPeoperty()的参数 */
	public static final String NETWORK_PROP_WARDSHIP_MANAGER = ProgramWardship.class.getCanonicalName();
	public static final String TAG = "ProgramWardship";
	/** 检查通过-可以播放 */
	public static final int CHECK_PROGRAM_PASS = 0;
	/** 检查未通过-需要发起对话 {@link #popPasswordDialog()} */
	public static final int CHECK_PROGRAM_DIALOG = 1;
	/** 检查未通过-不予播放 */
	public static final int CHECK_PROGRAM_REFUSED = -1;
	public static final int NOTIFY_UPDATE = 3;

	static final int CODE_DATABASE_URI = 1;
	static final int CODE_POP_PWD_DIALOG = 2;
	static final int CMD_DATABASE_CHANGE = 111;// cb
	static final int CMD_PWD_DIALOG_RESULT = 112;// cb
	static final int CMD_DATABASE_ORIGIN_LOAD = 113;// cb

	JsonChannel jsonChannel = null;
	private Object mutex = new Object();
	Map<Long, SparseBooleanArray> wardshipList = new HashMap<Long, SparseBooleanArray>();
	boolean programCheckResult = true;

	public ProgramWardship(Context context, String name) {
		jsonChannel = new JsonChannel(context, name) {
			@Override
			public void onCallback(int cmd, String json, JsonParcelable p, Bundle b) {
				switch (cmd) {
				case CMD_PWD_DIALOG_RESULT:
					synchronized (mutex) {
						onPasswordDialogResult(Boolean.parseBoolean(json));
					}
					break;
				case CMD_DATABASE_CHANGE:
					IPanelLog.i(TAG, "CMD_DATABASE_CHANGE get");
					if (onDatabaseChanged()) {
						synchronized (wardshipList) {
							reloadCheckingList();
						}
					}
					break;
				}
			}

			public void onChannelConnected() {
				synchronized (wardshipList) {
					onServiceConnected();
				}
			}
		};
		jsonChannel.connect();//
	}

	public void release() {
		synchronized (mutex) {
			if (jsonChannel != null)
				jsonChannel.disconnect();
			jsonChannel = null;
		}
	}

	public Uri getProgramWardshipDatabaseUri() {
		String s = jsonChannel.transmit(CODE_DATABASE_URI, null);
		System.out.println("getProgramWardshipDatabaseUri s = " + s);
		if (s != null)
			return Uri.parse(s);
		return null;
	}

	public void loadCheckingList() {
		synchronized (wardshipList) {
			reloadCheckingList();
		}
	}

	/**
	 * 根据方绘制做具体反应
	 * <p>
	 * <li>发起对话 {@link #CHECK_PROGRAM_DIALOG}
	 * <li>可以播放 {@link #CHECK_PROGRAM_PASS}
	 * <li>不予播放 {@link #CHECK_PROGRAM_REFUSED}
	 * 
	 * @param freq
	 *            节目所在频率
	 * @param programNumber
	 *            节目号
	 * @return 检查的值
	 */
	public int checkProgram(long freq, int programNumber) {
		loadCheckingList();
		IPanelLog.i(TAG, "checkProgram...freq..." + freq + "...programNumber..." + programNumber + " class = "
				+ wardshipList);
		synchronized (wardshipList) {
			if (!jsonChannel.isConnected())
				return CHECK_PROGRAM_PASS;
			IPanelLog.i(TAG, "checkProgram...2..wardshipList.size:" + wardshipList.size());
			SparseBooleanArray ps = wardshipList.get(freq);
			
			if (ps != null) {
				for(int i =0;i<ps.size();i++){
					IPanelLog.i(TAG, "key = "+ ps.keyAt(i) + ";value = " + ps.valueAt(i));
				}
				IPanelLog.i(TAG, "checkProgram...default true.." + ps.get(programNumber, true));
				if (programCheckResult = ps.get(programNumber, true)) {// 无programNumber认为true
					IPanelLog.i(TAG, "checkProgram...3..");
					return CHECK_PROGRAM_PASS;
				} else {
					IPanelLog.i(TAG, "checkProgram...4..");
					return CHECK_PROGRAM_DIALOG;
				}
			} else {// 频道列表里无此频率
				return CHECK_PROGRAM_PASS;
			}
			// return CHECK_PROGRAM_REFUSED;
		}
	}

	/**
	 * 发起监护密码输入对话
	 */
	public void popPasswordDialog() {
		synchronized (mutex) {
			programCheckResult = false;
			if (!programCheckResult) {
				IPanelLog.i(TAG, "ProgramWardship popPasswordDialog");
				String ret = jsonChannel.transmit(CODE_POP_PWD_DIALOG, null);
				IPanelLog.i(TAG, "ret==should be create dialog==" + ret);
			}
		}
	}

	/**
	 * overwrite it if you need
	 */
	public boolean onDatabaseChanged() {
		return true;
	}

	/**
	 * overwrite it if you need
	 */
	public void onServiceConnected() {
		reloadCheckingList();
	}

	boolean reloadCheckingList() {
		IPanelLog.i(TAG, "reloadCheckingList .....");
		Uri baseUri = getProgramWardshipDatabaseUri();
		if (baseUri == null)
			return false;
		IPanelLog.i(TAG, "baseUri....." + baseUri.toString());
		Uri furi = Uri.withAppendedPath(baseUri, ProgramWardshipDatebase.ProgramWardships.TABLE_NAME);
		IPanelLog.i(TAG, "furi = " + furi);
		Cursor c = jsonChannel.getContext().getContentResolver().query(furi, null, null, null, null);
		IPanelLog.i(TAG, "c is null....." + (c == null));
		if (c != null) {
			IPanelLog.i(TAG, "c.size = " + c.getCount());
			if (c.moveToFirst()) {
				int index[] = new int[] { c.getColumnIndex(ProgramWardshipDatebase.ProgramWardships.WARDSHIP),// 0
						c.getColumnIndex(ProgramWardshipDatebase.ProgramWardships.FREQUENCY),// 1
						c.getColumnIndex(ProgramWardshipDatebase.ProgramWardships.PROGRAM_NUMBER),// 2
				};
				IPanelLog.d(TAG, "reloadCheckingList is in");
				int size = 0;
				IPanelLog.d(TAG, "reloadCheckingList while in class = " + wardshipList);
				wardshipList.clear();
				do {
					int st;
					switch (c.getInt(index[0])) {
					case ProgramWardshipDatebase.WardshipType.TYPE_OPEN:
						st = 1;
						break;
					case ProgramWardshipDatebase.WardshipType.TYPE_LOCKED:
						st = 0;
						break;
					default:
						st = -1;
						break;
					}
					IPanelLog.i(TAG, "freq=" + c.getLong(index[1]) + "----programnumber=" + c.getInt(index[2])
							+ "----wardship" + c.getInt(index[0]));
					if (st >= 0) {
						size++;
						addWardships(c.getLong(index[1]), c.getInt(index[2]), st == 1, wardshipList);
					}
				} while (c.moveToNext());
				// IPanelLog.d(TAG,
				// "reloadCheckingList while out class = "+wardshipList);
				// Set<Entry<Long, SparseBooleanArray>> set =
				// wardshipList.entrySet();
				// for (Entry<Long, SparseBooleanArray> entry : set) {
				// long frequency = entry.getKey();
				// SparseBooleanArray sb = entry.getValue();
				// int len = sb.size();
				// for (int i = 0; i < len; i++) {
				// IPanelLog.d(TAG, "check programNumber=" + sb.keyAt(i));
				// IPanelLog.d(TAG, "check wardship = " + sb.valueAt(i));
				// }
				// }
				IPanelLog.d(TAG, "reloadCheckingList void size = " + size);
			}
			c.close();
			return true;
		}
		return false;
	}

	void addWardships(long freq, int pn, boolean b, Map<Long, SparseBooleanArray> wardshipList) {
		SparseBooleanArray ba = wardshipList.get(freq);
		if (ba == null) {
			ba = new SparseBooleanArray();
			wardshipList.put(freq, ba);
		}
		ba.put(pn, b);
	}

	/**
	 * 子类需要实现此方法
	 * <p>
	 * 当对话框操作有结果时调用
	 * 
	 * @param b
	 */
	public abstract void onPasswordDialogResult(boolean b);

}
