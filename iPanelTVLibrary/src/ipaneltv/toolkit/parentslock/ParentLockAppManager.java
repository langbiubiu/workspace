package ipaneltv.toolkit.parentslock;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.net.telecast.NetworkManager;
import android.net.telecast.dvb.DvbNetworkDatabase.Services;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ParentLockAppManager {
	private final static String TAG = "ParentLockAppManager";

	static final String SERVICE_NAME = "ipaneltv.teevee.playmanager.CommonParentLockAppManager";
	private final static String AUTHORITY = "ipaneltv.chongqing.networksidb";
	public static final String PARENT_TABLE_NAME = "parentLock";
	public static final String PROGRAM_WARDSHIP_TABLE_NAME = "program_wardship";

	static final int R_UNBIND = 0;
	static final int R_BINDING = 1;
	static final int R_BIND = 2;
	int bindFlag = R_UNBIND;

	IRemoteService remoteService;
	private Context context;
	public List<ParentLockChannel> parentLockChannels;
	private String serviceName;
	ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			Log.v(TAG, "bindFlag=" + bindFlag);
			if (bindFlag != R_BINDING)
				throw new RuntimeException("bindFlag flag impl err!");
			bindFlag = R_BIND;
			remoteService = IRemoteService.Stub.asInterface(service);
			Log.i(TAG, "IRemoteService.Stub.asInterface bindFlag:"
					+ bindFlag + " curTime:" + System.currentTimeMillis());
			try {
				remoteService.setLockDateListener(pldl);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (remoteService != null) {
				if (bindFlag == R_BIND)
				remoteService = null;
			}
			bindFlag = R_UNBIND;
		}
	};

	public static ParentLockAppManager createInstance(Context c, String uuid) {
		Log.i(TAG, "ParentLockAppManager createInstance!! uuid:" + uuid);
		NetworkManager nm = NetworkManager.getInstance(c);
		String sname = nm.getNetworkProperty(uuid,
				"ipaneltv.teevee.playmanager.ParentLockAPPManager");
		if (sname == null)
			sname = SERVICE_NAME;
		return new ParentLockAppManager(c, sname);
	}

	ParentLockAppManager() {}

	ParentLockAppManager(Context ctx, String sname) {
		Log.i(TAG, "ParentLockAppManager 构造方法!!!");
		this.context = ctx;
		this.serviceName = sname;
		this.parentLockChannels = getLockedData(context);
		Log.i(TAG, "ParentLockAppManager parentLockChannels size:"
				+ this.parentLockChannels.size());
		doBind();
	}

	private boolean doBind() {
		if (bindFlag != R_UNBIND) {
			return true;
		}
		boolean ret = false;
		if (context != null && remoteService == null) {
			ret = context.bindService(new Intent(serviceName), mConnection,
					Context.BIND_AUTO_CREATE);
			Log.i(TAG,"context.bindService curTime:" + System.currentTimeMillis());
		}
		Log.d(TAG, "bind service : " + serviceName
				+ (ret ? " success " : " failed"));
		if (ret)
			bindFlag = R_BINDING;
		return ret;
	}

	/**
	 * 验证密码是否正确 param password 需要验证的字符串密码 return true表示密码正确，false密码不正确
	 */
	public boolean validatePwd(String password) {
		Log.i(TAG, "validatePwd curTime:" + System.currentTimeMillis());
		try {
			Log.i(TAG, "validatePwd bindFlag:" + bindFlag);
			if (bindFlag == R_BIND)
				return remoteService.validatePwd(password);
		} catch (Exception e) {
			Log.e(TAG, "validatePwd error:" + e);
		}
		return false;
	}

	/**
	 * 重置密码
	 * 
	 * return true表示重置成功，false重置失败
	 */
	public boolean resetPassword() {
		try {
			Log.i(TAG, "resetPassword bindFlag:" + bindFlag);
			if (bindFlag == R_BIND)
				return remoteService.resetPassword();
		} catch (Exception e) {
			Log.e(TAG, "resetPassword error:" + e);
		}
		return false;
	}

	/**
	 * 清空所有频道的锁
	 * 
	 * return true表示清空成功，false清空失败
	 */
	public boolean clearChannelLock() {
		try {
			Log.i(TAG, "clearChannelLock bindFlag:" + bindFlag);
			if (bindFlag == R_BIND)
				return remoteService.clearChannelLock();
		} catch (Exception e) {
			Log.e(TAG, "clearChannelLock error:" + e);
		}
		return false;
	}

	public boolean checkChannelLocked(int program_number) {
		Log.i(TAG,"checkChannelLocked in!! program_number: " + program_number + " parentLockChannels size:" + parentLockChannels.size()+";parentLockChannels = "+ parentLockChannels);
		boolean checkChannelLockedFlag = false;
		if(parentLockChannels.size() == 0){
			parentLockChannels = getLockedData(context);
		}
		Log.i(TAG,"checkChannelLocked in!! parentLockChannels size: " + parentLockChannels.size());
		for (ParentLockChannel channel : parentLockChannels) {
			if (program_number == channel.getProgram_number()) {
				checkChannelLockedFlag = true;
			}
		}
		Log.d(TAG, "checkChannelLocked checkChannelLockedFlag = "+ checkChannelLockedFlag);
		return checkChannelLockedFlag;
	}

	List<ParentLockChannel> getLockedData(Context cxt) {
		Log.i(TAG, "getLockedData!!!!");
		Log.i(TAG,"zzq 2014年12月11日16:46:59");
		List<ParentLockChannel> lockedChannel = new ArrayList<ParentLockChannel>();
		Uri uriParentLock = Uri.parse("content://" + AUTHORITY + "/"
				+ PROGRAM_WARDSHIP_TABLE_NAME);
		
		Log.i(TAG,"uriParentLock = " + uriParentLock.toString());
		ContentResolver resolver = cxt.getContentResolver();
		Cursor cursorParentLock = resolver.query(uriParentLock, null, null,
				null, "_id asc");
		if (cursorParentLock != null) {
			if (cursorParentLock.getCount() > 0)
				cursorParentLock.moveToFirst();
			while (cursorParentLock.moveToNext()) {
				if (cursorParentLock.getInt(cursorParentLock
						.getColumnIndex("wardship")) != 1) {
					continue;
				}
				if (cursorParentLock.getInt(cursorParentLock
						.getColumnIndex("wardship")) == 1) {
					ParentLockChannel channel = new ParentLockChannel();
					int frequency = cursorParentLock.getInt(cursorParentLock
							.getColumnIndex(Services.FREQUENCY));
					int channel_number = cursorParentLock
							.getInt(cursorParentLock
									.getColumnIndex(Services.CHANNEL_NUMBER));
					int program_number = cursorParentLock
							.getInt(cursorParentLock
									.getColumnIndex(Services.PROGRAM_NUMBER));
					String channel_name = cursorParentLock
							.getString(cursorParentLock
									.getColumnIndex(Services.CHANNEL_NAME));
					channel.setFrequency(frequency);
					channel.setChannel_name(channel_name);
					channel.setChannel_number(channel_number);
					channel.setProgram_number(program_number);
					lockedChannel.add(channel);
				}
			}
			cursorParentLock.close();
		}
		Log.i(TAG, "getLockedData!!!!!lockedChannel size:" + lockedChannel.size());
		return lockedChannel;
	}

	public boolean setParentLockChannels(Context cxt) {
		Log.i(TAG,"ParentLockAppManager setParentLockChannels in this.parentLockChannels.size: " + this.parentLockChannels.size()+";parentLockChannels = "+ parentLockChannels);
		boolean flag = false;
		try {
			if(this.parentLockChannels.size()!=0){
				this.parentLockChannels.clear();
				Log.i(TAG,"ParentLockAppManager setParentLockChannels in this.parentLockChannels.size: " + this.parentLockChannels.size());
			}
			List<ParentLockChannel> Channels = getLockedData(cxt);
			Log.d(TAG, "setParentLockChannels Channels ="+ Channels.size());
			this.parentLockChannels = Channels;
			Log.d(TAG, "this.parentLockChannels = "+ this.parentLockChannels+";this.parentLockChannels = "+ this.parentLockChannels+";this.parentLockChannels.size()= "+ this.parentLockChannels.size());
			flag = true;
		} catch (Exception e) {
			Log.i(TAG,"ParentLockAppManager setParentLockChannels end flag: " + flag);
			return flag;
		}
		Log.i(TAG,"ParentLockAppManager setParentLockChannels end flag: " + flag);
		return flag;
	}
	
	IParentLockDateListener pldl = new IParentLockDateListener.Stub() {
		
		@Override
		public void onParentLockChange() throws RemoteException {
			
			boolean setFlag = setParentLockChannels(context);
			Log.i(TAG,"IParentLockDateListener setFlag: " + setFlag);
		}
	};
	
}
