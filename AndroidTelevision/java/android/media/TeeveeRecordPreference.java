package android.media;

import android.net.Uri;

/**
 * ¼��ƫ����������
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
	 * ���캯��
	 */
	public TeeveeRecordPreference() {
	}

	/**
	 * ������Ƶ��ʽ <br>
	 * ���磺MPEG2 H.264
	 * 
	 * @param fmts
	 *            ��ʽ(���Խ�����ĸ�ʽ���л����,���� mpeg1|mpeg2|h264)
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
	 * �õ���Ƶ��ʽ�ַ���
	 * 
	 * @return �ַ���
	 */
	public String getVideoFormat() {
		return this.video_fmt;
	}

	/**
	 * ������Ƶ��ʽ <br>
	 * ���磺MPEG1 MPEG2
	 * 
	 * @param fmts
	 *            ��ʽ(���Խ�����ĸ�ʽ���л����,���� mpeg1|mpeg2|aac)
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
	 * �õ���Ƶ��ʽ�ַ���
	 * 
	 * @return �ַ���
	 */
	public String getAudioFormat() {
		return this.audio_fmt;
	}

	/**
	 * ������ѵķֱ��ʡ���λΪ����
	 * 
	 * @param w
	 *            ���
	 * @param h
	 *            �߶�
	 */
	public void setBestResuluation(int w, int h) {
		this.video_width = w;
		this.video_height = h;
	}

	/**
	 * ���طֱ��ʿ��
	 * 
	 * @return ֵ,��δ�����򷵻�0,��ʱϵͳ���Զ�ѡ��ƽ̨���Ų���
	 */
	public int getResuluationWidth() {
		return video_width;
	}

	/**
	 * ���طֱ��ʸ߶�
	 * 
	 * @return ֵ,��δ�����򷵻�0,��ʱϵͳ���Զ�ѡ��ƽ̨���Ų���
	 */
	public int getResuluationHeight() {
		return video_height;
	}

	/**
	 * ����ƫ�õ�֡��
	 * 
	 * @param fps
	 *            ֡��
	 */
	public void setPreferredFrameRate(int fps) {
		this.video_fps = fps;
	}

	/**
	 * �õ���ǰ֡��
	 * 
	 * @return ֵ,��δ�����򷵻�0,��ʱϵͳ���Զ�ѡ��ƽ̨���Ų���
	 */
	public int getFrameRate() {
		return video_fps;
	}

	/**
	 * ����ƫ�õĴ���
	 * 
	 * @param fps
	 *            ֡��
	 */
	public void setPreferredBandwidth(int bps) {
		this.video_bandwidth = bps;
	}

	/**
	 * ���ص�ǰ���õĴ���
	 * 
	 * @return ֵ,��δ�����򷵻�0,��ʱϵͳ���Զ�ѡ��ƽ̨���Ų���
	 */
	public int getBandwidth() {
		return video_bandwidth;
	}

	/**
	 * ���ü��ܸ�ʽ
	 * 
	 * @param fmt
	 *            ��ʽ�ַ���������"des"
	 */
	public void setEncryptionFormat(String fmt) {
		this.encryption = fmt;
	}

	/**
	 * �õ����ܸ�ʽ�ַ���
	 * 
	 * @return ֵ����δ�����򲻼���
	 */
	public String getEncryptionFormat() {
		return encryption;
	}

	/** @hide �Զ�֧�ֵ���Ƶ��ʽ�б� */
	public static final String PARAM_VIDEO_FMT = "preferred_video_format";
	/** @hide �Զ�֧�ֵ���Ƶ��ʽ�б� */
	public static final String PARAM_AUDIO_FMT = "preferred_audio_format";
	/** @hide �Զ�֧�ֵ���Ƶ��� */
	public static final String PARAM_VIDEO_WIDTH = "preferred_video_width";
	/** @hide �Զ�֧�ֵ���Ƶ�߶� */
	public static final String PARAM_VIDEO_HEIGHT = "preferred_video_height";
	/** @hide �Զ�֧�ֵ�����ѡ��֡�� */
	public static final String PARAM_VIDEO_FRAME_RATE = "preferred_video_frame_rate";
	/** @hide ���Ŵ��� */
	public static final String PARAM_VIDEO_BAND_WIDTH = "preferred_video_band_width";
	/** @hide ʹ�ñ��ؼ��ܸ�ʽ */
	public static final String PARAM_ENCRYPTION_FORMAT = "preferred_encryption_format";
}
