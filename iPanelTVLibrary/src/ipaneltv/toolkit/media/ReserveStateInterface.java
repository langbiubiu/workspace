package ipaneltv.toolkit.media;

/**
 * lyre 20:58:31<br>
 * ��ܵ�����<br>
 * �ͻ��� player�������playerContext��Service�����𴴽�playerContext,���ṩ���߸�playerContextʹ�ã�
 * ���͵ı���PlayResourceScheduler��Widgetcontroller,<br>
 * ��ô����ԭ���ǣ�ÿ��PlayerContext������Ϊ���������Ե�Player���ڵģ������������������Player�����仰˵���ᴴ���ܶ��Player<br>
 * lyre 20:58:41<br>
 * ���˼·����Χ�����չ����<br>
 * lyre 20:59:45<br>
 * �������߽����ŵ�Context����Щ��Ҫ�������Դ�ֿ��ˣ�Context����ֻ��ʹ����Դ������������Դ��Context��
 * ������Դ����Service�����¼�ţ�����DataHolding����ѯ�������ݿ��¼ɶ��<br>
 * lyre 21:00:51<br>
 * ContextҪ��Ϊ��ǰ��ʱ�򣬾Ͷ�����Ҫʹ�õ���Դ����reserve�����ɹ��ˣ��Ϳ���ʹ�ã�
 * ���͵ľ���PlayResourceScheduler���洴����LivePlayState<br>
 * lyre 21:01:01<br>
 * �Լ�ǰ���ᵽDataHoldings<br>
 * lyre 21:01:08<br>
 * Widgetcontroll<br>
 * lyre 21:01:34<br>
 * ������Ҫ�ڲ�ͬ��Player֮�乲�����Դ����Ҫ������ʹ��<br>
 * lyre 21:02:00<br>
 * ������ϵͳ�ж��tuner����player����ca���ţ��Ϳ��Ժ���Ȼ����������<br>
 */
public interface ReserveStateInterface {
	boolean reserve();

	void loosen(boolean clearState);

	boolean isReserved();
	
	//void close();
}
