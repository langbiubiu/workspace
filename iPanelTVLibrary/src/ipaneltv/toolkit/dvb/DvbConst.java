package ipaneltv.toolkit.dvb;

public class DvbConst {
	public static final int PID_PAT = 0x00;
	public static final int PID_CAT = 0x01;
	public static final int PID_TSDT = 0x02;
	public static final int PID_NIT = 0x10;
	public static final int PID_SDT = 0x11;
	public static final int PID_BAT = 0x11;
	public static final int PID_EIT = 0x12;
	public static final int PID_RST = 0x13;
	public static final int PID_TDT = 0x14;
	public static final int PID_TOT = 0x14;
	public static final int PID_DIT = 0x1e;
	public static final int PID_SIT = 0x1f;

	public static final int TID_PAT = 0x00;
	public static final int TID_CAT = 0x01;
	public static final int TID_PMT = 0x02;
	public static final int TID_TSDT = 0x03;
	public static final int TID_NIT_ACTUAL = 0x40;
	public static final int TID_NIT_OTHER = 0x41;
	public static final int TID_SDT_ACTUAL = 0x42;
	public static final int TID_SDT_OTHER = 0x46;
	public static final int TID_BAT = 0x4a;
	public static final int TID_EIT_ACTUAL_PF = 0x4e;
	public static final int TID_EIT_OTHER_PF = 0x4f;
	public static final int TID_EIT_ACTUAL_FIRST = 0x50;
	public static final int TID_EIT_ACTUAL_LAST = 0x5f;
	public static final int TID_EIT_OTHER_FIRST = 0x60;
	public static final int TID_EIT_OTHER_LAST = 0x6f;
	public static final int TID_TDT = 0x70;
	public static final int TID_RST = 0x71;
	public static final int TID_ST = 0x72;
	public static final int TID_TOT = 0x73;
	public static final int TID_DIT = 0x7e;
	public static final int TID_SIT = 0x7f;

	/** 电视节目 */
	public static final int SERVICE_TYPE_DIGITAL_TV = 0x01;
	/** 广播节目 */
	public static final int SERVICE_TYPE_DIGITAL_RADIO = 0x02;
	/** 字幕 */
	public static final int SERVICE_TYPE_TELETEXT = 0x03;
	/** NVOD参考 */
	public static final int SERVICE_TYPE_NVOD_REFERENCE = 0x04;
	/** NVOD时移 */
	public static final int SERVICE_TYPE_NVOD_TIME_SHIFTED = 0x05;
	/** 多画面 */
	public static final int SERVICE_TYPE_MOSAIC = 0x06;
	public static final int SERVICE_TYPE_ADVENCED_DIGITAL_RADIO = 0x0A;
	public static final int SERVICE_TYPE_ADVENCED_DIGITAL_MOSAIC = 0x0B;
	/** 数据广播 */
	public static final int SERVICE_TYPE_DATA_BROADCAST = 0x0C;
	public static final int SERVICE_TYPE_RCS_MAP = 0x0E;
	public static final int SERVICE_TYPE_RCS_FLS = 0x0F;
	public static final int SERVICE_TYPE_DVB_MHP_SERVICE = 0x10;
	public static final int SERVICE_TYPE_MPEG2_HD_DIGITAL_TV = 0x11;
	public static final int SERVICE_TYPE_ADVENCED_SD_DIGITAL_TV = 0x16;
	public static final int SERVICE_TYPE_ADVENCED_SD_NVOD_TIME_SHIFTED = 0x17;
	public static final int SERVICE_TYPE_ADVENCED_SD_NVOD_REFERENCE = 0x18;
	/** 高清节目 */
	public static final int SERVICE_TYPE_ADVENCED_HD_DIGITAL_TV = 0x19;
	public static final int SERVICE_TYPE_ADVENCED_HD_NVOD_TIME_SHIFTED = 0x1A;
	public static final int SERVICE_TYPE_ADVENCED_HD_NVOD_REFERENCE = 0x1B;

}