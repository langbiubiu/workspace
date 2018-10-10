package com.ipanel.join.cq.vod.player;
/**
 * �߼���ӿ�
 * @author Administrator
 *
 */
public interface PlayCallBack {
	/** ����׼����ʼ */
	public void onPrepareStart();
	/** ����׼���ɹ�*/ 
	public void onPrepareSuccess();
	/** ������������  */
	public void onMediaDestroy();
	/** ����״̬�ı� */
	public void onPlayStateChange(int state);
	/** �����ص� */
	public void onPlayTick();
	/** ���ŵ���β */
	public void onPlayEnd();
	/** ����׼��ʧ�� */
	public void onPlayFailed(String msg);
	/** �������� */
	public void setVolume(Object value);
	/** ��ȡ�������õ�key */
	public String getVolomeKey();
	/** ����״̬�仯�ص� */
	public void onMuteStateChange(boolean mute);
	/** ֹͣ������������� */
	public void onCleanCache();
	/** ���˵����*/
	public void  fastReverseStart();
	/** ��ͣ��Ƶ*/
	public void  onPauseMedia();
	/** �ָ���Ƶ*/
	public void  onResumeMedia();
	/** �����µ�ǰ��Ƶ������ ˢ�²���*/
	public void  onNeedPlayMedia(String freq, String prog,int flag);
	/**���ò��ű���*/
	public void onSpeedChange(int speed);

}