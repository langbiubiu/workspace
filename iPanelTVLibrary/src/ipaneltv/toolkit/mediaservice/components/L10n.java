package ipaneltv.toolkit.mediaservice.components;

public class L10n {
	/** ����ʧ */
	public static final String DESC_ERR_812 = "[812]ca card lost.";
	/** ������Ȩ������Ϣ��ʧ */
	public static final String DESC_ERR_813 = "[813]stream ecm info missing";
	/** ����Ȩ���ƹ�����ʧ�� */
	public static final String DESC_ERR_814 = "[814]open ecm filter failed";
	/** �޷���ȡ���ſ����� */
	public static final String DESC_ERR_815 = "[815]open keygen failed";
	/** ���ý��ſ�����ʧ�� */
	public static final String DESC_ERR_816 = "[816]set descrambler cw error";
	/** ��������Դ��ʧ */
	public static final String DESC_ERR_817 = "[817]descrambler hd res missing";
	/** ���ſ����ֶ�ʧ */
	public static final String DESC_ERR_818 = "[818]keygen is missing for ecm update";
	/** ������Ȩ������Ϣʧ�� */
	public static final String DESC_ERR_819 = "[819]update ecm for module failed";
	/** ��Ȩ������Ϣ��ʱ */
	public static final String DESC_ERR_820 = "[820]ecm filter timeout";
	/** ������Ȩ������Ϣ������ʧ�� */
	public static final String DESC_ERR_821 = "[821]start ecm filter failed!";
	/** ���������豸ʧ�� */
	public static final String DESC_ERR_822 = "[822]start descrambler device failed!";

	/** ������Ϣ��ʾ */
	public static final String TRANSPORT_ERR_401 = "[401]lock delivery failed";
	public static final String TRANSPORT_ERR_402 = "[402]transport signal lost";

	/** ��Ŀ��Ϣ��ʾ */
	public static final String PROGRAM_ERR_410 = "[410]select program failed";
	public static final String PROGRAM_ERR_411 = "[411]program discontinued";
	public static final String PROGRAM_ERR_412 = "[412]program redirect failed";
	public static final String PROGRAM_ERR_413 = "[413]reselect failed";
	
	/** ������Ϣ��ʾ */
	public static final String DESCR_ERR_420 = "[420]descrambling start failed";
	public static final String DESCR_ERR_421 = "[421] ecm pids not found";
	public static final String DESCR_ERR_422 = "[422] ca module lost or card lost";
	
	/** ��Ŀ��Ϣѡ����ʾ */
	public static final String SELECT_ERR_430 = "[430]invalid stream uri or flags";
	public static final String SELECT_ERR_431 = "[431]start program failed";
	public static final String SELECT_ERR_432 = "[432]start descrambling failed";
	public static final String SELECT_ERR_433 = "[433]no program";
	
	/** ���ܿ���Ϣ��ʾ */
	public static final String CARD_ERR_440 = "[440]ca card not inserted";
	public static final String CARD_ERR_441 = "[441]ca card was muted";
	public static final String CARD_ERR_442 = "[442]no matched mod for card";
	
	/** CAģ����Ϣ��ʾ*/
	public static final String CAMOD_ERR_450 = "[450]ca module removed";
	public static final String CAMOD_ERR_451 = "[451]ca module unbinded";
	public static final String CAMOD_ERR_452 = "[452]no more ca res to descramble";
	
	/** VOD������Ϣ��ʾ */
	public static final String VOD_ERR_460 = "[460]vod server connect failed";
	public static final String VOD_ERR_461 = "[461]vod server session shutdown";
	public static final String VOD_ERR_462 = "[462]vod server streaming abort";
	public static final String VOD_ERR_463 = "[463]vod server rtsp error";
	
	/**�������*/
	public static final String DECODE_ERR_470 = "[470]decode error";
	
	/** ʱ�Ʋ�����Ϣ��ʾ */

	/** �ؿ�������Ϣ��ʾ */
	
	/**������Ϣ��ʾ*/
	public static final String code_901 = "[901]frequency signal lock failed";
	public static final String code_902 = "[902]locked frequency signal loss";
	public static final String code_903 = "[903] smart card backwards or damaged";
	public static final String code_904 = "[904]Successfully locked";
	public static final String code_905 = "[905]frequency signal can not be locked";
	public static final String code_906 = "[906]frequency signal has been lost";
	public static final String code_907 = "[907]frequency search time is too long, stop!";
	public static final String code_908 = "[908]table data transport stream has been lost";
	public static final String code_909 = "[909]timeout not received any table data";
	public static final String code_910 = "[910]timeout not receive the full table data";
	public static final String code_911 = "[911]get_frequency_information_finashed";
	public static final String code_912 = "[912]start to get information:";
	public static final String code_913 = "[913]start search err";
	public static final String code_914 = "[914]program infomation has changed";
	public static final String code_915 = "[915]start search failed";
	public static final String code_916 = "[916]start search success";
	
	public static int getErrorCode(String err) {
		return getErrorCode(err, 0);
	}

	public static int getErrorCode(String err, int defaultValue) {
		if (err == null)
			return defaultValue;
		try {
			int i = err.indexOf(']');
			int s = err.indexOf('[');
			return Integer.parseInt(err.substring(s + 1, i));
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
