package ngbj.ipanel.player;

interface iNgbPlayerListener{

	/**
	 * ý�岥����֪ͨ�¼��Ĵ��÷�����
	 * 
	 * @param eventType �����¼������ͣ�ȡֵΪ0~4�����У�
	 * 		0--���ųɹ�;
	 * 		1--��������;
	 * 		2--����ֹͣ;
	 * 		3--����ʧ��;
	 * 		4--SUMA:������ͣ
	 */
	void OnPlayerEvent(int eventType);

}