package android.media;

import android.net.Uri;

/**
 * 录制偏爱参数对象
 */
public class TeeveeRecordPreference {
	static final String TAG = "[java]TeeveeRecordPerference";
	String video_fmt = "";
	String audio_fmt = "";
	int video_width = 0, video_height = 0;
	int video_fps = 0;
	int video_bandwidth = 0;
	String encryption = "";

	public String toQueryString() {
		if (video_fmt.equals("") || audio_fmt.equals(""))
			return null;

		StringBuffer sb = new StringBuffer();
		sb.append("&");
		sb.append(PARAM_AUDIO_FMT).append("=").append(audio_fmt);
		sb.append("&");
		sb.append(PARAM_VIDEO_FMT).append("=").append(video_fmt);
		//
		if (video_width != 0 && video_height != 0) {
			sb.append("&");
			sb.append(PARAM_VIDEO_WIDTH).append("=").append(video_width);
			sb.append("&");
			sb.append(PARAM_VIDEO_HEIGHT).append("=").append(video_height);
		}
		if (video_fps > 0) {
			sb.append("&");
			sb.append(PARAM_VIDEO_FRAME_RATE).append("=").append(video_fps);
		}
		if (video_bandwidth > 0) {
			sb.append("&");
			sb.append(PARAM_VIDEO_BAND_WIDTH).append("=").append(video_bandwidth);
		}
		if (!encryption.equals("")) {
			sb.append("&");
			sb.append(PARAM_ENCRYPTION_FORMAT).append("=").append(encryption);
		}
		return sb.toString();
	}

	public static TeeveeRecordPreference fromQueryString(String s) {
		TeeveeRecordPreference tp = new TeeveeRecordPreference();
		Uri uri = Uri.parse(s);
		tp.video_fmt = uri.getQueryParameter(PARAM_VIDEO_FMT);
		tp.audio_fmt = uri.getQueryParameter(PARAM_AUDIO_FMT);
		tp.video_width = Integer.parseInt(uri.getQueryParameter(PARAM_VIDEO_WIDTH));
		tp.video_height = Integer.parseInt(uri.getQueryParameter(PARAM_VIDEO_HEIGHT));
		tp.video_fps = Integer.parseInt(uri.getQueryParameter(PARAM_VIDEO_FRAME_RATE));
		tp.video_bandwidth = Integer.parseInt(uri.getQueryParameter(PARAM_VIDEO_BAND_WIDTH));
		tp.encryption = uri.getQueryParameter(PARAM_ENCRYPTION_FORMAT);
		return tp;
	}

	/**
	 * 构造函数
	 */
	public TeeveeRecordPreference() {
	}

	/**
	 * 设置视频格式 <br>
	 * 比如：MPEG2 H.264
	 * 
	 * @param fmts
	 *            格式(可以将允许的格式进行或操作,比如 mpeg1|mpeg2|h264)
	 */
	public void setVideoFormat(String fmts) {
		String[] fmt_a = fmts.split("|");
		for (int i = 0; i < fmt_a.length; i++) {
			fmts = fmt_a[i];
			if (fmts.equalsIgnoreCase("mpeg1") || fmts.equalsIgnoreCase("mpeg-1")
					|| fmts.equalsIgnoreCase("mpeg_1")) {
			} else if (fmts.equalsIgnoreCase("mpeg2") || fmts.equalsIgnoreCase("mpeg-2")
					|| fmts.equalsIgnoreCase("mpeg_2")) {
			} else if (fmts.equalsIgnoreCase("mpeg4") || fmts.equalsIgnoreCase("mpeg-4")
					|| fmts.equalsIgnoreCase("mpeg_4")) {
			} else if (fmts.equalsIgnoreCase("H264") || fmts.equalsIgnoreCase("h.264")) {
			} else if (fmts.equalsIgnoreCase("h265") || fmts.equalsIgnoreCase("h.265")) {
			} else {
				throw new IllegalArgumentException("bad format:" + fmts);
			}
		}
		this.video_fmt = fmts;
	}

	/**
	 * 得到视频格式字符串
	 * 
	 * @return 字符串
	 */
	public String getVideoFormat() {
		return this.video_fmt;
	}

	/**
	 * 设置音频格式 <br>
	 * 比如：MPEG1 MPEG2
	 * 
	 * @param fmts
	 *            格式(可以将允许的格式进行或操作,比如 mpeg1|mpeg2|aac)
	 */
	public void setAudioFormat(String fmts) {
		String[] fmt_a = fmts.split("|");
		for (int i = 0; i < fmt_a.length; i++) {
			fmts = fmt_a[i];
			if (fmts.equalsIgnoreCase("mpeg1") || fmts.equalsIgnoreCase("mpeg-1")
					|| fmts.equalsIgnoreCase("mpeg_1")) {
			} else if (fmts.equalsIgnoreCase("mpeg2") || fmts.equalsIgnoreCase("mpeg-2")
					|| fmts.equalsIgnoreCase("mpeg_2")) {
			} else if (fmts.equalsIgnoreCase("aac")) {
			} else if (fmts.equalsIgnoreCase("ac3")) {
			} else if (fmts.equalsIgnoreCase("dts")) {
			} else if (fmts.equalsIgnoreCase("pcm")) {
			} else {
				throw new IllegalArgumentException("bad format:" + fmts);
			}
		}
		this.audio_fmt = fmts;
	}

	/**
	 * 得到音频格式字符串
	 * 
	 * @return 字符串
	 */
	public String getAudioFormat() {
		return this.audio_fmt;
	}

	/**
	 * 设置最佳的分辨率、单位为像素
	 * 
	 * @param w
	 *            宽度
	 * @param h
	 *            高度
	 */
	public void setBestResuluation(int w, int h) {
		this.video_width = w;
		this.video_height = h;
	}

	/**
	 * 返回分辨率宽度
	 * 
	 * @return 值,如未设置则返回0,此时系统将自动选择平台最优参数
	 */
	public int getResuluationWidth() {
		return video_width;
	}

	/**
	 * 返回分辨率高度
	 * 
	 * @return 值,如未设置则返回0,此时系统将自动选择平台最优参数
	 */
	public int getResuluationHeight() {
		return video_height;
	}

	/**
	 * 设置偏好的帧率
	 * 
	 * @param fps
	 *            帧率
	 */
	public void setPreferredFrameRate(int fps) {
		this.video_fps = fps;
	}

	/**
	 * 得到当前帧率
	 * 
	 * @return 值,如未设置则返回0,此时系统将自动选择平台最优参数
	 */
	public int getFrameRate() {
		return video_fps;
	}

	/**
	 * 设置偏好的带宽
	 * 
	 * @param fps
	 *            帧率
	 */
	public void setPreferredBandwidth(int bps) {
		this.video_bandwidth = bps;
	}

	/**
	 * 返回当前设置的带宽
	 * 
	 * @return 值,如未设置则返回0,此时系统将自动选择平台最优参数
	 */
	public int getBandwidth() {
		return video_bandwidth;
	}

	/**
	 * 设置加密格式
	 * 
	 * @param fmt
	 *            格式字符串，比如"des"
	 */
	public void setEncryptionFormat(String fmt) {
		this.encryption = fmt;
	}

	/**
	 * 得到加密格式字符串
	 * 
	 * @return 值，如未设置则不加密
	 */
	public String getEncryptionFormat() {
		return encryption;
	}

	/** @hide 对端支持的视频格式列表 */
	public static final String PARAM_VIDEO_FMT = "preferred_video_format";
	/** @hide 对端支持的音频格式列表 */
	public static final String PARAM_AUDIO_FMT = "preferred_audio_format";
	/** @hide 对端支持的视频宽度 */
	public static final String PARAM_VIDEO_WIDTH = "preferred_video_width";
	/** @hide 对端支持的视频高度 */
	public static final String PARAM_VIDEO_HEIGHT = "preferred_video_height";
	/** @hide 对端支持的优先选择帧率 */
	public static final String PARAM_VIDEO_FRAME_RATE = "preferred_video_frame_rate";
	/** @hide 播放带宽 */
	public static final String PARAM_VIDEO_BAND_WIDTH = "preferred_video_band_width";
	/** @hide 使用本地加密格式 */
	public static final String PARAM_ENCRYPTION_FORMAT = "preferred_encryption_format";
}
