package ipaneltv.toolkit.wardship2;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.QueryHandler;
import ipaneltv.toolkit.db.WardshipDatabaseCursorHandler;
import ipaneltv.toolkit.db.WardshipProgram;
import ipaneltv.uuids.db.ExtendDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.Vector;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.telecast.dvb.DvbNetworkDatabase.Services;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseIntArray;

/**
 * for settings
 * 
 * @author 235
 * 
 */
public class WardshipManager {
	private static final String TAG = WardshipManager.class.getSimpleName();
	Context ctx;
	Uri uri;
	private boolean loaded = false;
	private boolean loadFinisheded = false;
	private HandlerThread loadThread = new HandlerThread("navi-proc");
	private Handler loadHandler;
	private Handler mainHandler;
	Vector<WardshipProgram> loadedPrograms;
	ProgramDataListener l;
	private ContentObserver programObserver;
	MD5 md5;
	WardshipTool wardshipTool;
	Set<ChannelKey> wardshipSet;
	Set<ChannelKey> hideSet;
//	HashMap<ChannelKey, Integer> numberMap;

	public WardshipManager(Context ctx, Uri uri) {
		this.ctx = ctx;
		this.uri = uri;
		md5 = new MD5();
		wardshipTool = WardshipTool.createWardshipTool();
		mainHandler = new Handler(Looper.getMainLooper());
	}
	
	public WardshipManager(Context ctx, Uri uri,String root) {
		this.ctx = ctx;
		this.uri = uri;
		md5 = new MD5();
		wardshipTool = WardshipTool.createWardshipTool(root);
		mainHandler = new Handler(Looper.getMainLooper());
	}

	
	/*
	 * @explain
	 * 		1.获取频道列表
	 * 		2.将获取的节目列表与加锁set进行对比，
	 * 		    包含在set里的设置枷锁，不包含的设置为0
	 * 		3.将处理后的节目信息返回
	 * 
	 */
	public Vector<WardshipProgram> getWardshipPrograms() {
		Vector<WardshipProgram> wardshipPrograms = loadedPrograms;
		Vector<WardshipProgram> programs = new Vector<WardshipProgram>();
		if (wardshipPrograms != null) {
			programs.addAll(wardshipPrograms);
			if (wardshipSet != null) {
				for (WardshipProgram program : programs) {
					if (wardshipSet != null) {
						if (wardshipSet.contains(ChannelKey.obten(program.getFrequency(),
								program.getProgram_number()))) {
							program.setLocked(1);
						} else {
							program.setLocked(0);
						}
					}
					Log.d(TAG, "getWardshipPrograms program = "+ program);
				}
			}
		}
		return programs;
	}

	/**
	 * 设置是否启用密码
	 * 
	 * @param isEnable
	 */
	public void setPwdState(boolean isEnable) {
		Log.i(TAG, "setPwdState isEnable = " + isEnable);
		wardshipTool.setPwdEnable(isEnable);
	}

	/**
	 * 检查密码状态
	 * 
	 * @return
	 */
	public boolean isPwdEnable() {
		return wardshipTool.isPwdEnable();
	}

	/**
	 * 重置密码，默认为0000
	 * 
	 * @return
	 */
	public boolean resetPwd() {
		return updatePwd(WardshipTool.INIT_PWD);
	}

	protected String getPwd() {
		return wardshipTool.getPwd();
	}

	public boolean checkPwd(String pwd) {
		return wardshipTool.checkPwd(pwd);
	}
	
	/**
	 * 修改密码
	 * 
	 * @param s
	 * @return
	 */
	public boolean updatePwd(String s) {
		return wardshipTool.setPassword(s);
	}

	/**
	 * 修改加锁状态并备份
	 * 
	 * @param wardshipPrograms
	 * @explain
	 * 		更新节目列表：
	 * 			1.将整个的节目列表进行处理
	 * 			2.将枷锁的加在set里，未加锁的移除
	 * 			3.最后将整个加锁set写进文档保存
	 */
	public void updateWardship(Vector<WardshipProgram> wardshipPrograms) {
		Log.d( TAG, "updateWardship_CWJ" );
		if (wardshipPrograms == null || wardshipPrograms.size() <= 0) {
			return;
		}
		for (WardshipProgram wardshipProgram : wardshipPrograms) {
			Log.d(TAG, "updateWardship wardshipProgram = " + wardshipProgram.getFrequency() + "/"
					+ wardshipProgram.getProgram_number() + "/" + wardshipProgram.getLocked());
			if (wardshipProgram.getLocked() == 0) {
				wardshipSet.remove(ChannelKey.obten(wardshipProgram.getFrequency(),
						wardshipProgram.getProgram_number()));
			} else {
				wardshipSet.add(ChannelKey.obten(wardshipProgram.getFrequency(),
						wardshipProgram.getProgram_number()));
			}
		}
		wardshipTool.saveWarship(wardshipSet);
	}
	public void resetWardship( ){
		wardshipTool.saveWarship( null );
	}
	/**
	 * 修改隐藏状态
	 * 
	 * @param wardshipPrograms
	 *            隐藏状态有变化的频道集合
	 */
	public void updateHideState(Vector<WardshipProgram> wardshipPrograms) {
		if (wardshipPrograms == null || wardshipPrograms.size() <= 0) {
			return;
		}
		Uri channelUri = Uri.withAppendedPath(uri, Services.TABLE_NAME);
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		SparseIntArray array = new SparseIntArray();

		for (WardshipProgram wardshipProgram : wardshipPrograms) {
			Log.d(TAG, "updateHideState wardshipProgram = " + wardshipProgram.getFrequency() + "/"
					+ wardshipProgram.getProgram_number() + "/" + wardshipProgram.getHide());
			if (wardshipProgram.getHide() == 0) {
				hideSet.remove(ChannelKey.obten(wardshipProgram.getFrequency(),
						wardshipProgram.getProgram_number()));
			} else {
				hideSet.add(ChannelKey.obten(wardshipProgram.getFrequency(),
						wardshipProgram.getProgram_number()));
			}
			ContentProviderOperation operation = ContentProviderOperation
					.newUpdate(channelUri)
					.withSelection(Services.PROGRAM_NUMBER + "=?",
							new String[] { String.valueOf(wardshipProgram.getProgram_number()) })
					.withValue(ExtendDatabase.ExtendServices.HIDED, wardshipProgram.getHide())
					.build();
			ops.add(operation);
			array.put(wardshipProgram.getProgram_number(), wardshipProgram.getLocked());
		}
		wardshipTool.saveHideState(hideSet);
		try {

			ContentProviderResult rs[] = ctx.getContentResolver().applyBatch(uri.getAuthority(),
					ops);

			for (ContentProviderResult s : rs) {
				System.out.println(s.toString());
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 修改排序
	 * 
	 * @param ward排序有变化的频道集合
	 */
	public void updatenumbered(Vector<WardshipProgram> wardshipPrograms) {
		if (wardshipPrograms == null || wardshipPrograms.size() <= 0) {
			return;
		}
		Uri channelUri = Uri.withAppendedPath(uri, Services.TABLE_NAME);
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		for (WardshipProgram wardshipProgram : wardshipPrograms) {
			Log.d(TAG, "updatenumbered wardshipProgram = " + wardshipProgram.getFrequency() + "/"
					+ wardshipProgram.getProgram_number() + "/" + wardshipProgram.getChannel_number());
//			numberMap.put(
//					ChannelKey.obten(wardshipProgram.getFrequency(),
//							wardshipProgram.getProgram_number()),
//					wardshipProgram.getChannel_number());
			ContentProviderOperation operation = ContentProviderOperation
					.newUpdate(channelUri)
					.withSelection(Services.PROGRAM_NUMBER + "=?",
							new String[] { String.valueOf(wardshipProgram.getProgram_number()) })
					.withValue(Services.CHANNEL_NUMBER, wardshipProgram.getChannel_number())
					.build();
			ops.add(operation);
		}
//		wardshipTool.saveNumber(numberMap);
		try {

			ContentProviderResult rs[] = ctx.getContentResolver().applyBatch(uri.getAuthority(),
					ops);

			for (ContentProviderResult s : rs) {
				System.out.println(s.toString());
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void ensureOnload() {
		if (!loaded) {
			loaded = true;
			onLoad();
		}
	}
	
	protected void onLoad() {
		loadThread.start();
		loadHandler = new Handler(loadThread.getLooper());
		postLoadWarship();
	}

	protected void close() {
		Looper lp;
		if ((lp = loadThread.getLooper()) != null)
			lp.quit();
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	/*
	 * @explain
	 * 	1.	获取枷锁列表
	 * 	2.	获取隐藏列表
	 * 	3.	获取节目数据
	 * 
	 */
	private void postLoadWarship() {
		loadHandler.post(new Runnable() {

			@Override
			public void run() {
				wardshipSet = wardshipTool.getWardshipList();
				hideSet = wardshipTool.getHideList();
//				numberMap = wardshipTool.getNumberedMap();
				postLoadPrograms();
			}
		});
	}

	private void postLoadPrograms() {
		Uri channelUri = Uri.withAppendedPath(uri, Services.TABLE_NAME);
		WardshipDatabaseCursorHandler h = getWardshipCursorHandler(ctx, channelUri, loadHandler);
		final WardshipDatabaseCursorHandler lf = h;

		lf.setQueryHandler(new QueryHandler() {
			@Override
			public void onQueryStart() {
				if (programObserver == null)
					ctx.getContentResolver().registerContentObserver(uri, false,
							setProgramObserver());
			}

			@Override
			public void onQueryEnd() {
				Log.d(TAG, "postLoadPrograms 2 onCursorEnd");
				loadedPrograms = lf.getPrograms();
				if(loadedPrograms != null){
					Collections.sort(loadedPrograms, new ComparatorProgram());	
				}
				mainHandler.post(new Runnable() {
					
					@Override
					public void run() {
						ProgramDataListener lis = l;
						loadFinisheded = true;
						if (lis != null) {
							lis.onProgramInfoUpdated();
						}
					}
				});
			}
		});
		lf.postQuery();
	}
	
	class ComparatorProgram implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			WardshipProgram program0 = (WardshipProgram) arg0;
			WardshipProgram program1 = (WardshipProgram) arg1;
			return program0.getChannel_number() - program1.getChannel_number();
		}
	}
/*
 * @explain
 *		设置监听数据集	
 * 
 */
	private ContentObserver setProgramObserver() {
		programObserver = new ContentObserver(loadHandler) {
			public void onChange(boolean selfChange) {
				Log.i(TAG, "--------------go in Observe  setProgramObserver");
				postLoadPrograms();
			};
		};
		return programObserver;
	}

	public void addProgramDataListener(ProgramDataListener lis) {
		l = lis;
		if (loadFinisheded && l != null) {
			l.onProgramInfoUpdated();
		}
	}

	public static interface ProgramDataListener {
		void onProgramInfoUpdated();
	}

	protected WardshipDatabaseCursorHandler getWardshipCursorHandler(Context context, Uri uri,
			Handler handler) {
		return new SxWardshipCursorHandler(context, uri, new String[] { Services._ID,
				Services.FREQUENCY, Services.PROGRAM_NUMBER, Services.CHANNEL_NUMBER,
				Services.CHANNEL_NAME, ExtendDatabase.ExtendServices.HIDED,Services.SERVICE_TYPE}, Services.CHANNEL_NUMBER + ">=?", new String[] { 0 + "" },
				Services.CHANNEL_NUMBER + " ASC", handler);
	}

	public static class SxWardshipCursorHandler extends WardshipDatabaseCursorHandler {

		public SxWardshipCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String order, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, order, handler);
		}

		@Override
		public void onCursorStart(Cursor c) {
			super.onCursorStart(c);
		}

		@Override
		public void onRecordFound(Cursor c) {
			super.onRecordFound(c);
			appendRecord();// 做过滤策略
		}
	}
}
