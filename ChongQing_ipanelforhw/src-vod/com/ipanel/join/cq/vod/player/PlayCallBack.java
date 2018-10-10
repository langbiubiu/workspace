package com.ipanel.join.cq.vod.player;
/**
 * 逻辑层接口
 * @author Administrator
 *
 */
public interface PlayCallBack {
	/** 播放准备开始 */
	public void onPrepareStart();
	/** 播放准备成功*/ 
	public void onPrepareSuccess();
	/** 播放器已销毁  */
	public void onMediaDestroy();
	/** 播放状态改变 */
	public void onPlayStateChange(int state);
	/** 心跳回调 */
	public void onPlayTick();
	/** 播放到结尾 */
	public void onPlayEnd();
	/** 播放准备失败 */
	public void onPlayFailed(String msg);
	/** 设置音量 */
	public void setVolume(Object value);
	/** 获取音量设置的key */
	public String getVolomeKey();
	/** 静音状态变化回调 */
	public void onMuteStateChange(boolean mute);
	/** 停止播放且清除缓存 */
	public void onCleanCache();
	/** 快退到起点*/
	public void  fastReverseStart();
	/** 暂停视频*/
	public void  onPauseMedia();
	/** 恢复视频*/
	public void  onResumeMedia();
	/** 播放下当前视频，用于 刷新参数*/
	public void  onNeedPlayMedia(String freq, String prog,int flag);
	/**设置播放倍速*/
	public void onSpeedChange(int speed);

}