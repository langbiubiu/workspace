package ipaneltv.toolkit.ratingscollect;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class RatingsCollectorEvent {
	public static final int User_EvtPowerOn = 1;
	public static final int User_EvtStandBy = 2;
	public static final int User_EvtWakeUp = 3;
	public static final int User_EvtService = 4;
	public static final int User_EvtPowerOnAd = 5;
	public static final int User_EvtEPGAd = 6;
	public static final int User_EvtVOD = 7;
	public static final int User_EvtTimeShift = 8;
	public static final int User_EvtInteractiveApp = 9;
	public static final int User_EvtNativeApp = 10;

	public static final String SEPARATOR = ";";

	public static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String PATTERN = "yyyyMMddHHmmss";

	long ucTime;
	int event;
	String smcID;
	String msgid = "%nMsgId%";
	String stbid = "%stbid%";

	SimpleDateFormat utcSdf = new SimpleDateFormat(ISO8601_PATTERN);
	SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);

	RatingsCollectorEvent(int event, long ucTime) {
		this.event = event;
		this.ucTime = ucTime;
	}

	public String time2iso8601(long time) {
		return utcSdf.format(new Date(time));
	}

	public String time2yyyyMMddHHmmss(long time) {
		return sdf.format(new Date(time));
	}

	public abstract String toString();

	public static class PowerOnEvent extends RatingsCollectorEvent {
		String HW_ver;
		String SW_ver;
		/**
		 * 1 视频数字电视节目 2 音频数字电视节目 12 数据广播 226 双向交互主页 244 EPG节目指南 245 系统设置服务 247
		 * 升级服务 250 点播服务
		 */
		int startupService;

		public PowerOnEvent(long ucTime, String smcID, String HW_ver, String SW_ver,
				int startupService) {
			super(User_EvtPowerOn, ucTime);
			this.smcID = smcID;
			this.HW_ver = HW_ver;
			this.SW_ver = SW_ver;
			this.startupService = startupService;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(msgid).append(SEPARATOR);
			sb.append(time2iso8601(ucTime)).append(SEPARATOR);
			sb.append(event).append(SEPARATOR);
			sb.append(smcID).append(SEPARATOR);
			sb.append(stbid).append(SEPARATOR);
			sb.append(HW_ver).append(SEPARATOR);
			sb.append(SW_ver).append(SEPARATOR);
			sb.append(startupService).append(SEPARATOR);
			return sb.toString();
		}
	}

	public static class StandByEvent extends RatingsCollectorEvent {
		int standbyType;

		public StandByEvent(long ucTime, String smcID, int standbyType) {
			super(User_EvtStandBy, ucTime);
			this.smcID = smcID;
			this.standbyType = standbyType;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(msgid).append(SEPARATOR);
			sb.append(time2iso8601(ucTime)).append(SEPARATOR);
			sb.append(event).append(SEPARATOR);
			sb.append(smcID).append(SEPARATOR);
			sb.append(stbid).append(SEPARATOR);
			sb.append(standbyType).append(SEPARATOR);
			return sb.toString();
		}
	}

	public static class WeakUpEvent extends RatingsCollectorEvent {

		public WeakUpEvent(long ucTime, String smcID, int standbyType) {
			super(User_EvtWakeUp, ucTime);
			this.smcID = smcID;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(msgid).append(SEPARATOR);
			sb.append(time2iso8601(ucTime)).append(SEPARATOR);
			sb.append(event).append(SEPARATOR);
			sb.append(smcID).append(SEPARATOR);
			sb.append(stbid).append(SEPARATOR);
			return sb.toString();
		}
	}

	public static class EpgAdvEvent extends RatingsCollectorEvent {
		int serviceId;
		int adType;
		int groupID;
		String adID;
		long startTime;
		long endTime;

		public EpgAdvEvent(long ucTime, String smcID, int serviceId, int adType, int groupID,
				String adID, long startTime, long endTime) {
			super(User_EvtEPGAd, ucTime);
			this.smcID = smcID;
			this.serviceId = serviceId;
			this.adType = adType;
			this.groupID = groupID;
			this.adID = adID;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(msgid).append(SEPARATOR);
			sb.append(time2iso8601(ucTime)).append(SEPARATOR);
			sb.append(event).append(SEPARATOR);
			sb.append(smcID).append(SEPARATOR);
			sb.append(stbid).append(SEPARATOR);
			sb.append(serviceId).append(SEPARATOR);
			sb.append(adType).append(SEPARATOR);
			sb.append(groupID).append(SEPARATOR);
			sb.append(adID).append(SEPARATOR);
			sb.append(time2yyyyMMddHHmmss(startTime)).append(SEPARATOR);
			sb.append(time2yyyyMMddHHmmss(endTime));
			return sb.toString();
		}
	}

	public static class BootEpgAdvEvent extends RatingsCollectorEvent {
		int serviceId;
		int adType;
		int showPosition;
		String adID;
		long startTime;
		long endTime;

		public BootEpgAdvEvent(long ucTime, String smcID, int serviceId, int adType,
				int showPosition, String adID, long startTime, long endTime) {
			super(User_EvtPowerOnAd, ucTime);
			this.ucTime = ucTime;
			this.smcID = smcID;
			this.serviceId = serviceId;
			this.adType = adType;
			this.adID = adID;
			this.showPosition = showPosition;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(msgid).append(SEPARATOR);
			sb.append(time2iso8601(ucTime)).append(SEPARATOR);
			sb.append(event).append(SEPARATOR);
			sb.append(smcID).append(SEPARATOR);
			sb.append(stbid).append(SEPARATOR);
			sb.append(serviceId).append(SEPARATOR);
			sb.append(adType).append(SEPARATOR);
			sb.append(adID).append(SEPARATOR);
			sb.append(showPosition).append(SEPARATOR);
			sb.append(time2yyyyMMddHHmmss(startTime)).append(SEPARATOR);
			sb.append(time2yyyyMMddHHmmss(endTime));
			return sb.toString();
		}
	}

}
