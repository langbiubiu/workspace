package ngbj.ipanel.player;
import ngbj.ipanel.player.iNgbPlayerListener;
import ngbj.ipanel.player.NgbjRect;
interface iNgbPlayer{
	
	/**
	 * 设置播放器资源
	 *
	 * @param onid 原始网络id
	 * @param tsid 传输流标识id
	 * @param serviceId 节目id
	 * @param audioPid 节目中音频的pid，针对多路音频，默认传0				
	 * @param videoPid 节目中视频的pid，默认传0
	 *
	 */
	int setDataSource(int onid, int tsid, int serviceId, int audioPid, int videoPid);

 	/**
	 * 启动媒体播放器。
	 */
	 void start();

	/**
	 * 停止媒体播放器播放。
	 */
	void stop();
	
	/**
	 * 暂停媒体播放器播放。当在播放广播的流媒体时，其相应的设置应该保留。
	 */
	void pause();
	
	/**
	 * 恢复媒体播放器播放。
	 */
	void resume();

	/**
	 * 释放媒体播放器占用的稀缺资源。
	 */
	void close(); 
	
	/**
	 * 注册一个监听器接收与当前播放器相关的事件。
	 * 
	 * @param listener
	 *            iNgbPlayerListener;对象，表示待注册的媒体播放器事件监听器。
	 */
	void addListener(iNgbPlayerListener listener);
			
	/**
	 * 注销事件监听器。如果指定的监听器目前没有被注册，则不执行任何动作。
	 * 
	 * @param listener
	 *            iNgbPlayerListener对象，表示待注销的媒体播放器事件监听器。
	 */
	void removeListener(iNgbPlayerListener listener);

	/**
	 * 获取声音大小。
	 * 
	 * @return volume int型，表示设置音量大小，取值0～100，0表示静音，100表示最大音量。
	 */
	int getVolume();

	/**
	 * 设置声音大小。
	 * 
	 * @param volume
	 *            volume - int型，表示待设置的音量大小，取值0～100，0表示静音，100表示最大音量。
	 * @return int型，表示设置是否成功，取值0表示设置成功，1表示设置失败。
	 */
	int setVolume(int volume); 

 	/**
	 * 设置视频解码输出的窗口大小，实现缩放功能，坐标相对于视频平面的左上角（0,0）而言。
	 * 
	 * @param rect
	 *            NgbjRect对象，表示窗口的显示区域。
	 */
	 void setBounds(in NgbjRect rect);
	
	
	/**
	 * 获取视频解码输出的窗口大小，实现缩放功能，坐标相对于视频平面的左上角（0,0）而言。
	 * 
	 * @return NgbjRect对象，表示视频解码输出窗口的显示区域。
	 */
	 NgbjRect getBounds();
	 
	 /**
	 * 设定窗口的剪切区域。设置后整个视频窗口仅显示设置区域的视频，用于实现局部放大缩小等操做。 即设置后 将制定区域在整个输出窗口上显示。
	 * 
	 * @param rect
	 *            NgbjRect对象，表示窗口的剪切区域。
	 */
	void setClip(in NgbjRect rect);
	 
	/**
	 * 获取窗口的剪切区域
	 * 
	 * @return NgbjRect对象，表示窗口的剪切区域。
	 */
	NgbjRect getClip();
	
	/**
	 * 设置换台时视频的处理效果，即节目切换时的视频切换效果，如静帧--1、黑屏--2, 3--淡入淡出, 4--关闭视频层。
	 * 
	 * @param stopMode
	 *            
	 * @return int型，表示设置结果，取值1表示设置成功，0表示设置失败。
	 */
	int setStopMode(int stopMode);

	/**
	 * 获取换台时视频的处理效果 即节目切换时，视频切换效果，如静帧--1、黑屏--2, 3--淡入淡出, 4--关闭视频层。
	 * 
	 * @return int型 , 静帧--1、黑屏--2, 3--淡入淡出, 4--关闭视频层。
	 */
	int getStopMode();
	
	/**
	 * 打开视频，设置视频属性为开。显示视频解码器的输出。
	 * 
	 * @return int型，表示打开结果，取值1表示打开成功，0表示打开失败。
	 */
	int openVideo();
	
	/**
	 * 关闭视频。 用户设置的停止模式决定关闭视频后是静帧还是黑屏。 设置视频属性为关。隐藏视频解码器的输出
	 * 关闭视频后，对视频的操作不再生立刻效。但内部仍然会记录这些操作。并且在打开视频后生效。
	 * 
	 * @return int型，表示关闭结果，取值1表示关闭成功，0表示关闭失败。
	 */
	int closeVideo();

	/**
	 * 获取视频打开属性状态，即视频解码是否输出。
	 * 
	 * @return int型，取值1表示视频输出打开，0表示视频输出关闭。
	 */
	int isVideoOpen();

}