package ipaneltv.toolkit.ratingscollect;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class RatingsCollector2Event {
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

	int event;
	String caID;
	String msgid = "%nMsgId%";
	String stbid = "%stbid%";
	String platf = "%platform%";

	SimpleDateFormat utcSdf = new SimpleDateFormat(ISO8601_PATTERN);
	SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);

	RatingsCollector2Event() {
	}

	public String time2iso8601(long time) {
		return utcSdf.format(new Date(time));
	}

	public String time2yyyyMMddHHmmss(long time) {
		return sdf.format(new Date(time));
	}

	public abstract String toString();

	public static class EpgAdvEvent extends RatingsCollector2Event {
		int platform;
		int serviceId;
		String adID;
		long startTime;
		long endTime;
		int key;
		long time;
		String event_Type;
		int groupID;

		public EpgAdvEvent(int platform, String caID, int key, int serviceId, String adID,
				long startTime, long endTime, long time, String event_Type, int groupID) {
			super();
			this.platform = platform;
			this.caID = caID;
			this.key = key;
			this.time = time;
			this.event_Type = event_Type;
			this.serviceId = serviceId;
			this.groupID = groupID;
			this.adID = adID;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(msgid).append(SEPARATOR);
			if (platform == -1) {
				// 将platform定义为无效值，后续处理。
				sb.append(platf).append(SEPARATOR);
			} else {
				sb.append(platform).append(SEPARATOR);
			}
			sb.append(caID).append(SEPARATOR);
			sb.append(stbid).append(SEPARATOR);
			sb.append(key).append(SEPARATOR);
			sb.append(time2iso8601(time)).append(SEPARATOR);
			sb.append(event_Type).append(SEPARATOR);
			sb.append(serviceId).append(SEPARATOR);
			sb.append(groupID).append(SEPARATOR);
			sb.append(adID).append(SEPARATOR);
			sb.append(time2yyyyMMddHHmmss(startTime)).append(SEPARATOR);
			sb.append(time2yyyyMMddHHmmss(endTime));
			return sb.toString();
		}
	}

	public static class BootEpgAdvEvent extends RatingsCollector2Event {
		int platform;
		int serviceId;
		String adID;
		int key;
		long time;
		String event_Type;
		int groupID;
		long startTime;
		long endTime;

		public BootEpgAdvEvent(int platform, String caID, int key, int serviceId, String adID,
				long startTime, long endTime, long time, String event_Type, int groupID) {
			super();
			this.platform = platform;
			this.caID = caID;
			this.key = key;
			this.time = time;
			this.event_Type = event_Type;
			this.serviceId = serviceId;
			this.groupID = groupID;
			this.adID = adID;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(msgid).append(SEPARATOR);
			if (platform == -1) {
				sb.append(platf).append(SEPARATOR);
			} else {
				sb.append(platform).append(SEPARATOR);
			}
			sb.append(caID).append(SEPARATOR);
			sb.append(stbid).append(SEPARATOR);
			sb.append(key).append(SEPARATOR);
			sb.append(time2iso8601(time)).append(SEPARATOR);
			sb.append(event_Type).append(SEPARATOR);
			if (event_Type.equals("01101")) {// 开机视频广告
				sb.append("").append(SEPARATOR);
			} else if (event_Type.equals("01102")) {// 开机图片广告
				sb.append(serviceId).append(SEPARATOR);
			}
			sb.append("").append(SEPARATOR);// 对于所有开机广告，填null
			sb.append(adID).append(SEPARATOR);
			sb.append(time2yyyyMMddHHmmss(startTime)).append(SEPARATOR);
			sb.append(time2yyyyMMddHHmmss(endTime));
			return sb.toString();
		}
	}

	public static class PowerOnEvent extends RatingsCollector2Event {
		int platform;
		int serviceId;
		String adID;
		long startTime;
		long endTime;
		int key;
		long time;
		String event_Type;
		int groupID;

		public PowerOnEvent(int platform, String caID, int key, int serviceId, String adID,
				long startTime, long endTime, long time, String event_Type, int groupID) {
			this.platform = platform;
			this.caID = caID;
			this.key = key;
			this.time = time;
			this.event_Type = event_Type;
			this.serviceId = serviceId;
			this.groupID = groupID;
			this.adID = adID;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(msgid).append(SEPARATOR);
			sb.append(platform).append(SEPARATOR);
			sb.append(caID).append(SEPARATOR);
			sb.append(stbid).append(SEPARATOR);
			sb.append(key).append(SEPARATOR);
			sb.append(time2iso8601(time)).append(SEPARATOR);
			sb.append(event_Type).append(SEPARATOR);
			sb.append(serviceId).append(SEPARATOR);
			sb.append(groupID).append(SEPARATOR);
			sb.append(adID).append(SEPARATOR);
			sb.append(time2yyyyMMddHHmmss(startTime)).append(SEPARATOR);
			sb.append(time2yyyyMMddHHmmss(endTime));
			return sb.toString();
		}
	}
}
