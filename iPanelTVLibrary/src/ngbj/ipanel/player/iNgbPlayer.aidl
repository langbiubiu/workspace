package ngbj.ipanel.player;
import ngbj.ipanel.player.iNgbPlayerListener;
import ngbj.ipanel.player.NgbjRect;
interface iNgbPlayer{
	
	/**
	 * ���ò�������Դ
	 *
	 * @param onid ԭʼ����id
	 * @param tsid ��������ʶid
	 * @param serviceId ��Ŀid
	 * @param audioPid ��Ŀ����Ƶ��pid����Զ�·��Ƶ��Ĭ�ϴ�0				
	 * @param videoPid ��Ŀ����Ƶ��pid��Ĭ�ϴ�0
	 *
	 */
	int setDataSource(int onid, int tsid, int serviceId, int audioPid, int videoPid);

 	/**
	 * ����ý�岥������
	 */
	 void start();

	/**
	 * ֹͣý�岥�������š�
	 */
	void stop();
	
	/**
	 * ��ͣý�岥�������š����ڲ��Ź㲥����ý��ʱ������Ӧ������Ӧ�ñ�����
	 */
	void pause();
	
	/**
	 * �ָ�ý�岥�������š�
	 */
	void resume();

	/**
	 * �ͷ�ý�岥����ռ�õ�ϡȱ��Դ��
	 */
	void close(); 
	
	/**
	 * ע��һ�������������뵱ǰ��������ص��¼���
	 * 
	 * @param listener
	 *            iNgbPlayerListener;���󣬱�ʾ��ע���ý�岥�����¼���������
	 */
	void addListener(iNgbPlayerListener listener);
			
	/**
	 * ע���¼������������ָ���ļ�����Ŀǰû�б�ע�ᣬ��ִ���κζ�����
	 * 
	 * @param listener
	 *            iNgbPlayerListener���󣬱�ʾ��ע����ý�岥�����¼���������
	 */
	void removeListener(iNgbPlayerListener listener);

	/**
	 * ��ȡ������С��
	 * 
	 * @return volume int�ͣ���ʾ����������С��ȡֵ0��100��0��ʾ������100��ʾ���������
	 */
	int getVolume();

	/**
	 * ����������С��
	 * 
	 * @param volume
	 *            volume - int�ͣ���ʾ�����õ�������С��ȡֵ0��100��0��ʾ������100��ʾ���������
	 * @return int�ͣ���ʾ�����Ƿ�ɹ���ȡֵ0��ʾ���óɹ���1��ʾ����ʧ�ܡ�
	 */
	int setVolume(int volume); 

 	/**
	 * ������Ƶ��������Ĵ��ڴ�С��ʵ�����Ź��ܣ������������Ƶƽ������Ͻǣ�0,0�����ԡ�
	 * 
	 * @param rect
	 *            NgbjRect���󣬱�ʾ���ڵ���ʾ����
	 */
	 void setBounds(in NgbjRect rect);
	
	
	/**
	 * ��ȡ��Ƶ��������Ĵ��ڴ�С��ʵ�����Ź��ܣ������������Ƶƽ������Ͻǣ�0,0�����ԡ�
	 * 
	 * @return NgbjRect���󣬱�ʾ��Ƶ����������ڵ���ʾ����
	 */
	 NgbjRect getBounds();
	 
	 /**
	 * �趨���ڵļ����������ú�������Ƶ���ڽ���ʾ�����������Ƶ������ʵ�־ֲ��Ŵ���С�Ȳ����� �����ú� ���ƶ����������������������ʾ��
	 * 
	 * @param rect
	 *            NgbjRect���󣬱�ʾ���ڵļ�������
	 */
	void setClip(in NgbjRect rect);
	 
	/**
	 * ��ȡ���ڵļ�������
	 * 
	 * @return NgbjRect���󣬱�ʾ���ڵļ�������
	 */
	NgbjRect getClip();
	
	/**
	 * ���û�̨ʱ��Ƶ�Ĵ���Ч��������Ŀ�л�ʱ����Ƶ�л�Ч�����羲֡--1������--2, 3--���뵭��, 4--�ر���Ƶ�㡣
	 * 
	 * @param stopMode
	 *            
	 * @return int�ͣ���ʾ���ý����ȡֵ1��ʾ���óɹ���0��ʾ����ʧ�ܡ�
	 */
	int setStopMode(int stopMode);

	/**
	 * ��ȡ��̨ʱ��Ƶ�Ĵ���Ч�� ����Ŀ�л�ʱ����Ƶ�л�Ч�����羲֡--1������--2, 3--���뵭��, 4--�ر���Ƶ�㡣
	 * 
	 * @return int�� , ��֡--1������--2, 3--���뵭��, 4--�ر���Ƶ�㡣
	 */
	int getStopMode();
	
	/**
	 * ����Ƶ��������Ƶ����Ϊ������ʾ��Ƶ�������������
	 * 
	 * @return int�ͣ���ʾ�򿪽����ȡֵ1��ʾ�򿪳ɹ���0��ʾ��ʧ�ܡ�
	 */
	int openVideo();
	
	/**
	 * �ر���Ƶ�� �û����õ�ֹͣģʽ�����ر���Ƶ���Ǿ�֡���Ǻ����� ������Ƶ����Ϊ�ء�������Ƶ�����������
	 * �ر���Ƶ�󣬶���Ƶ�Ĳ�������������Ч�����ڲ���Ȼ���¼��Щ�����������ڴ���Ƶ����Ч��
	 * 
	 * @return int�ͣ���ʾ�رս����ȡֵ1��ʾ�رճɹ���0��ʾ�ر�ʧ�ܡ�
	 */
	int closeVideo();

	/**
	 * ��ȡ��Ƶ������״̬������Ƶ�����Ƿ������
	 * 
	 * @return int�ͣ�ȡֵ1��ʾ��Ƶ����򿪣�0��ʾ��Ƶ����رա�
	 */
	int isVideoOpen();

}