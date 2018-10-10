package ngbj.ipaneltv.dvb;

import ngbj.ipaneltv.dvb.NgbJSITransportStream;
import ngbj.ipaneltv.dvb.NgbJSIService;

interface INgbJScanListener {
	
	/**
	 * ��̨�ɹ�����NIT(�Զ�����ʱ?)
	 * 
	 * @param niid
	 * @param siTSs--��ʾNIT���а����Ĵ�����NgbJSITransportStream�������顣
	 */
	void onScanNITSuccess(int niid, in NgbJSITransportStream[] siTSs);
	
	/**
	 * �����ɹ���������һ��Ƶ�㣩
	 * 
	 * @param niid
	 * @param siSs --��Ŀ��Ϣ����NgbJSIService���顣
	 */
	void onScanSuccess(int niid, in NgbJSIService[] siSs);
	
	/**
	 * ����ʧ��
	 * 
	 * @param niid 
	 * @param reason ��̨ʧ��ԭ��ȡֵΪ0~5�����У�
	 * 		0--δ֪ԭ��;
	 * 		1--��Ƶʧ��;
	 * 		2--NIT����ʧ��;
	 * 		3--BAT����ʧ��;
	 * 		4--PAT����ʧ��;
	 * 		5--PMT����ʧ�ܡ�
	 */
	void onScanFailure(int niid, int reason);
	
	/**
	 * �������
	 * @param niid
	 * @param transportStreamCount--��ʾ������������
	 * @param serviceCount --��ʾҵ�����
	 */
	void onScanFinish(int niid, int transportStreamCount, int serviceCount);
}
