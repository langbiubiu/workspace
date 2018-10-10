package ngbj.ipanel.player;

interface iNgbPlayerListener{

	/**
	 * 媒体播放器通知事件的处置方法。
	 * 
	 * @param eventType 播放事件的类型，取值为0~4，其中：
	 * 		0--播放成功;
	 * 		1--启动播放;
	 * 		2--播放停止;
	 * 		3--播放失败;
	 * 		4--SUMA:播放暂停
	 */
	void OnPlayerEvent(int eventType);

}