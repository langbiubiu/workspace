package ipaneltv.toolkit.parentslock;

import ipaneltv.toolkit.parentslock.IParentLockDateListener;

interface IRemoteService {
	/**
	*验证密码是否正确
	*param password 需要验证的字符串密码
	*return true表示密码正确，false密码不正确
	*/
	boolean validatePwd(String password);
	
	
	/**
	*根据频道号检查频道是否加锁
	*param ctx 所在环境中的上下文对象
	*param channel_number 整形频道号
	*return true表示频道已加锁，false频道未加锁
	*/
	boolean checkChannelLocked(int channel_number);
	
	
	/**
	*重置密码
	*
	*return true表示重置成功，false重置失败
	*/
	boolean resetPassword();
	
	
	/**
	*清空所有频道的锁
	*
	*return true表示清空成功，false清空失败
	*/
	boolean clearChannelLock();

	/**
	*监听数据库中的数据变动
	*
	*
	*/
	void setLockDateListener(IParentLockDateListener lockDataListener);

	/**
	*数据库中的数据变动
	*
	*
	*/
	boolean onParentLockDataChange();
}