package ngbj.ipaneltv.dvb;

import ngbj.ipaneltv.dvb.INgbJScanListener;
import ngbj.ipaneltv.dvb.INgbJScanEitListener;
import ngbj.ipaneltv.dvb.IDvbSearchListener;
import ngbj.ipaneltv.dvb.ISelectListener;
import ngbj.ipanel.player.iNgbPlayer;

interface INgbJScanManager{
	/**
	 * @return iNgbPlayer���󣬱�ʾ��ý�岥������
	 */
	iNgbPlayer createLivePlayer() ;	
	
	/**
	 *
	 * @param searchType--Ƶ��������ʽ��ȡֵ0~2�����У�
	 * 		  0--�ֶ�������
	 * 		  1--�Զ�������
	 * 		  2--����������
	 * @param  deliveryType ���Ʒ�ʽ
	 *        0--cable
	 *        1--satellite
	 *        2--terrestrial
	 *
	 * @param  flags 
	 *        0-----��ͨ���������������ݿ⡣
	 *        0x01--ngb��������ص�ngb�Ļص�
	 *        0x04--�����ļ��С�
	 *        0x08--�������ݿ⡣
	 * ��ʼ������
	 */
	void initScan(int searchType, int deliveryType, int flags);
	
	/**
	 * �����Ƶ�����
	 *
	 * @param  mainFrequency ��Ƶ��Ƶ��
	 * @param  modulation ���Ʒ�ʽ
	 * @param  symbolRate ������
	 */
	boolean setMainFrequency(long mainFrequency,int modulation,int symbolRate);
	
	/**
	 * ����ֶ���������
	 *
	 * @param  start ��ʼƵ��
	 * @param  end   ��ֹƵ��
	 * @param  modulation ���Ʒ�ʽ
	 * @param  symbolRate ������
	 * @param  polarization ������ʽ��δ��������Ϊ0
	 */
	 void addScanManualParam(long start, long end, int modulation, int symbolRate, int polarization);
	
	/**
	*
	*��Ƶ������������ѯƵ����Ϣ����ӦISelectListener�ص�
	*
	*/
	void lockFreqency(long freq,int modulation,int symbolRate);
	
	/**
	 * ��ʼƵ��������������Ϣ�첽�ص���
	 */
	void startScan();
	
	/**
	* ����������������浽NVM�С�
 	*
 	* @param  flags
 	*
 	*        0--�����ǰ�����ݣ����±��档
 	*        1--���ʽ����
 	*
	* @return boolean�ͣ�true��ʾ����ɹ���false��ʾ����ʧ�ܡ�
	*/
	boolean saveScanResult(int flags);

	/**
	* ȡ��Ƶ��������
	* <p>
	* Ӧ���ڲ�������ʧ��(<code>ChannelScanFailureEvent</code>)����������(
	* <code>ChannelScanFinishEvent</code>) �¼�֮ǰ������ͨ�����ø÷���ȡ������������
	*/
	void cancel();

	/**
	* �ͷ�����ʹ�õ���Դ��
	*/
	void release();
	
	/**
	* ������ͨdvb����������
	*
	*/
	void setDvbSearchListener(IDvbSearchListener listener);

	/**
	* ������ͨdvb����������
	*
	*/
	void setSelectListener(ISelectListener listener);

	/**
	* ע����������״̬������(ngb�淶)��
	* 
	* @param listener
	*            NgbScanListener���󣬱�ʾ��ע����������̼���������
	*/
	void setChannelScanListener(INgbJScanListener listener) ;

	/**
	* �Ƴ���������״̬������(ngb�淶)��
	* 
	* @param listener
	*            NgbScanListener���󣬱�ʾ��ע�����������̼���������
	*/
	void removeChannelScanListener(INgbJScanListener listener);
	
	/**
	 * ���UTCʱ�䣬��TDT��TOT�л�ȡ
	 */
	long getUTCTime(long frequence);	
	
	/**
	 * ����eit�����Ļص�����(ngb�淶)��
	 */
	void setScanEitListener(INgbJScanEitListener pflistener);
	
	/**
	 * ������Ƶ���ȫ��EPG��Ϣ������Eit_Actual����Ϣͨ��eit�Ļص������첽�ص���
	 * ������Ϣ�����SectionStorage��(ngb�淶)��
	 * 
	 * @param frequence  Ƶ��
	 */
	void scanEitAll(long frequence);
	
	/**
	 * ����Ƶ���Eit_Actual����EPG PF��Ϣ(ngb�淶)��
	 * 
	 * @param frequence ��ǰ��Ŀ��Ƶ����Ϣ
	 */
	void scanEitActual(long frequence);	
	
	/**
	 * �ͷ�tune��Դ���������ʹ��tune���ͷ�ʧ�ܡ�
	 * �ɹ�����1��ʧ�ܷ���0��
	 */
	int releaseTune();
	
}
