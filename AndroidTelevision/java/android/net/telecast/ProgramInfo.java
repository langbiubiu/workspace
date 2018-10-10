package android.net.telecast;

/**
 * 节目频道对象
 * <p>
 * 从数据库中的信息构建对象参考如下代码：<br>
 * 
 * <pre>
 * <code>
 * ProgramInfo getProgramInfo(Cursor c, long freq, int programNumber) {
 *   ProgramInfo pi = null;
 *   if (c.moveToFirst()) {
 *      pi = new ProgramInfo();
 *      pi.setProgramNumber(programNumber);
 *      pi.setCARequired(true);
 *      int ifreq = c.getColumnIndex(NetworkDatabase.Streams.FREQUENCY);
 *      int ispid = c.getColumnIndex(NetworkDatabase.Streams.STREAM_PID);			
 *      int ipn = c.getColumnIndex(NetworkDatabase.Streams.PROGRAM_NUMBER);
 *      int istname = c.getColumnIndex(NetworkDatabase.Streams.STREAM_TYPE_NAME);
 *      do {
 *      if (c.getLong(ifreq) == freq && c.getInt(ipn) == programNumber) {
 *        String stname = c.getString(istname);
 *        if (ProgramInfo.isAudioStream(stname)) {
 *          pi.setAudioStreamType(stname);
 *          pi.setAudioPID(c.getInt(ispid));
 *        } else if (ProgramInfo.isVideoStream(stname)) {
 *          pi.setVideoStreamType(stname);
 *          pi.setVideoPID(c.getInt(ispid));
 *        } else if (ProgramInfo.isPcrStream(stname)) {
 *          pi.setPcrPID(c.getInt(ispid));
 *        }
 *      }
 *    } while (c.moveToNext());
 *  }
 *  return pi;
 * }
 * </code>
 * </pre>
 */
public final class ProgramInfo {

	/** stream packet id 未定义 */
	public static final int PID_UNDEFINED = -1;

	/**
	 * 频道类型<br>
	 * 注意：具体标准(DVB,ATSC等)所定义的值与此可能并不相同
	 */
	public static interface ChannelTypeEnum {
		/** 数字电视 */
		public static final int DIGITAL_TV = 0xff1;
		/** 数字电台 */
		public static final int DIGITAL_RADIO = 0xff3;
		/** 数字电视 */
		public static final int ANALOG_TV = 0xff4;
		/** 数字电台 */
		public static final int ANALOG_RADIO = 0xff5;
		/** 其他 */
		public static final int OTHER = 0xffff;
	}

	/**
	 * 流类型名称枚举
	 */
	public static interface StreamTypeNameEnum {
		/** 音频 - MPEG1 */
		public static final String AUDIO_MPEG1 = "audio_mpeg1";
		/** 音频 - MPEG2 */
		public static final String AUDIO_MPEG2 = "audio_mpeg2";
		/** 音频 - AAC */
		public static final String AUDIO_AAC = "audio_aac";
		/** 音频 - AC3 */
		public static final String AUDIO_AC3 = "audio_ac3";
		/** 音频 - AC3 Plus */
		public static final String AUDIO_AC3_PLUS = "audio_ac3_plus";
		/** 音频 - DTS */
		public static final String AUDIO_DTS = "audio_dts";
		/** 视频 - MPEG1 */
		public static final String VIDEO_MPEG1 = "video_mpeg1";
		/** 视频 - MPEG2 */
		public static final String VIDEO_MPEG2 = "video_mpeg2";
		/** 视频 - H264 */
		public static final String VIDEO_H264 = "video_h264";
		/** 视频 - H265 */
		public static final String VIDEO_H265 = "video_h265";
		/** PCR */
		public static final String PCR = "pcr";
		/** 字幕 */
		public static final String SUBTITLE = "subtitle";
		/** 图文广播电视 */
		public static final String TELETEXT = "teletext";
		/** 其他 */
		public static final String OTHER = "other";
	}

	/**
	 * 视频源的呈现形式枚举
	 */
	public static interface VideoPictureFormEnum {
		/** 类型 - 2D */
		public static final int TYPE_2D = 0;
		/** 类型 - 3D 左右 */
		public static final int TYPE_3D_LEFT_RIGHT = 1;
		/** 类型 - 3D 上下 */
		public static final int TYPE_3D_TOP_BUTTOM = 2;
	}

	/**
	 * 音频源声道组成形式枚举
	 */
	public static interface AudioTrackFormEnum {
		/** 类型 - 立体声 */
		public static final int TYPE_STEREO = 0;
		/** 类型 - 仅左声道 */
		public static final int TYPE_LEFT = 1;
		/** 类型 - 仅右声道 */
		public static final int TYPE_RIGHT = 2;
		/** 类型 - 双声道 */
		public static final int TYPE_MONO = 3;
	}

	static final String UNKNOWN = "unknown";
	int program_number = 0;
	int apid = PID_UNDEFINED, vpid = PID_UNDEFINED, pcrpid = PID_UNDEFINED,
			subtitlepid = PID_UNDEFINED;
	long freq = FrequencyInfo.INVALID_FREQUENCY;
	String ast = UNKNOWN, vst = UNKNOWN, sst = UNKNOWN;
	boolean needCA = false;
	float vrate = 1.0f;
	int audioEcmPID = -1, videoEcmPID = -1;
	byte[] pmtsection;

	/** uri schema */
	public static final String SCHEMA = "program://";
	/** uri 参数 pmt_section */
	public static final String PARAM_PMT_SECTION = "pmt_section";
	/** uri 参数 audio_stream_type */
	public static final String PARAM_ATYPE = "audio_stream_type";
	/** uri 参数 audio_stream_pid */
	public static final String PARAM_APID = "audio_stream_pid";
	/** uri 参数 video_stream_type */
	public static final String PARAM_VTYPE = "video_stream_type";
	/** uri 参数 video_stream_pid */
	public static final String PARAM_VPID = "video_stream_pid";
	/** uri 参数 pcr_stream_pid */
	public static final String PARAM_PCRPID = "pcr_stream_pid";
	/** uri 参数 subtitle_pid */
	public static final String PARAM_SUBTPID = "subtitle_stream_pid";
	/** uri 参数 subtitle_pid */
	public static final String PARAM_SUBTTYPE = "subtitle_stream_type";
	/** uri 参数 frequency */
	public static final String PARAM_FREQ = "frequency";
	/** uri 参数 ca_required */
	public static final String PARAM_CA = "ca_required";
	/** uri 参数 video_source_rate */
	public static final String PARAM_VRATE = "video_source_rate";
	/** uri 参数 audio_ecm_pid */
	public static final String PARAM_AECMPID = "audio_ecm_pid";
	/** uri 参数 video_ecm_pid */
	public static final String PARAM_VECMPID = "video_ecm_pid";

	/**
	 * 构造对象
	 */
	public ProgramInfo() {
	}

	/**
	 * 检查是否为音频
	 * 
	 * @param name
	 *            名称
	 * @return 是则返回true，否则返回false
	 */
	public static boolean isAudioStream(String name) {
		return name.startsWith("audio");
	}

	/**
	 * 检查是否为视频
	 * 
	 * @param name
	 *            名称
	 * @return 是则返回true，否则返回false
	 */
	public static boolean isVideoStream(String name) {
		return name.startsWith("video");
	}

	/**
	 * 检查是否为PCR流
	 * 
	 * @param name
	 *            名称
	 * @return 是则返回true，否则返回false
	 */
	public static boolean isPcrStream(String name) {
		return name.startsWith("pcr");
	}

	/**
	 * 检查是否为字幕流
	 * 
	 * @param name
	 *            名称
	 * @return 是则返回true，否则返回false
	 */
	public static boolean isSubtitleStream(String name) {
		return name.startsWith("subtitle");
	}

	/**
	 * 检查是否为图文电视广播业务流
	 * 
	 * @param name
	 *            名称
	 * @return 是则返回true，否则返回false
	 */
	public static boolean isTeletextStream(String name) {
		return name.startsWith("teletext");
	}

	/**
	 * 得到MPEG标准的音视频流类型名称
	 * <p>
	 * 如果参数值不在MPEG标准定义的范围,或者不是音视频流类型,将返回null
	 * 
	 * @param type
	 *            类型
	 * @return 名称
	 */
	public static String getMpegAVStreamTypeName(int type) {
		switch (type) {
		case 0x01:
			return StreamTypeNameEnum.VIDEO_MPEG1;
		case 0x02:
			return StreamTypeNameEnum.VIDEO_MPEG2;
		case 0x03:
			return StreamTypeNameEnum.AUDIO_MPEG1;
		case 0x04:
			return StreamTypeNameEnum.AUDIO_MPEG2;
		default:
			return null;
		}
	}

	/**
	 * 通过字符串构建对象
	 * 
	 * @param s
	 *            字符串,参考 {@link #toString()}
	 * @return 对象
	 * @throws IllegalArgumentException
	 * 
	 */
	public static ProgramInfo fromString(String s) {
		if (!s.startsWith(SCHEMA))
			throw new IllegalArgumentException("invalid starts:" + s);
		int x = s.indexOf('?', SCHEMA.length());
		if (x < 0)
			throw new IllegalArgumentException("invalid argument:" + s);
		ProgramInfo pi = new ProgramInfo();
		pi.program_number = Integer.parseInt(s.substring(SCHEMA.length(), x));

		String params[] = s.substring(x + 1).split("&");
		for (int i = 0; i < params.length; i++) {
			String p[] = params[i].split("=");
			if (p.length != 2)
				throw new IllegalArgumentException("invalid args :" + params[i]);
			if (!(p[0] = p[0].trim().toLowerCase()).equals(PARAM_PMT_SECTION))
				p[1] = p[1].trim().toLowerCase();
			if (PARAM_APID.equals(p[0])) {
				pi.apid = Integer.parseInt(p[1]);
			} else if (PARAM_VPID.equals(p[0])) {
				pi.vpid = Integer.parseInt(p[1]);
			} else if (PARAM_PCRPID.equals(p[0])) {
				pi.pcrpid = Integer.parseInt(p[1]);
			} else if (PARAM_SUBTPID.equals(p[0])) {
				pi.subtitlepid = Integer.parseInt(p[1]);
			} else if (PARAM_ATYPE.equals(p[0])) {
				pi.ast = p[1];
			} else if (PARAM_VTYPE.equals(p[0])) {
				pi.vst = p[1];
			} else if (PARAM_SUBTTYPE.equals(p[0])) {
				pi.sst = p[1];
			} else if (PARAM_CA.equals(p[0])) {
				pi.needCA = Boolean.parseBoolean(p[1]);
			} else if (PARAM_VRATE.equals(p[0])) {
				pi.vrate = Float.parseFloat(p[1]);
			} else if (PARAM_FREQ.equals(p[0])) {
				pi.freq = Long.parseLong(p[1]);
			} else if (PARAM_AECMPID.equals(p[0])) {
				pi.audioEcmPID = Integer.parseInt(p[1]);
			} else if (PARAM_VECMPID.equals(p[0])) {
				pi.videoEcmPID = Integer.parseInt(p[1]);
			} else if (PARAM_PMT_SECTION.equals(p[0])) {
				pi.pmtsection = decodeByteString(p[1]);
			} else {
				// ignore
			}
		}
		if (pi.apid < 0 || pi.apid > 0x1fff) {
			pi.apid = -1;
		}
		if (pi.vpid < 0 || pi.vpid > 0x1fff) {
			pi.vpid = -1;
		}
		if (pi.pcrpid < 0 || pi.pcrpid > 0x1fff)
			pi.pcrpid = -1;
		if (pi.audioEcmPID < 0 || pi.audioEcmPID > 0x1fff)
			pi.audioEcmPID = -1;
		if (pi.videoEcmPID < 0 || pi.videoEcmPID > 0x1fff)
			pi.videoEcmPID = -1;
		return pi;
	}

	/**
	 * 得到字符串形式
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(SCHEMA).append(program_number).append('?');
		if (apid > 0) {
			sb.append(PARAM_APID).append('=').append(apid).append('&');
			sb.append(PARAM_ATYPE).append('=').append(ast).append('&');
		}

		if (vpid > 0) {
			sb.append(PARAM_VPID).append('=').append(vpid).append('&');
			sb.append(PARAM_VTYPE).append('=').append(vst).append('&');
			if (vrate != 1.0f)
				sb.append(PARAM_VRATE).append('=').append(vrate).append('&');
		}
		if (subtitlepid > 0) {
			sb.append(PARAM_SUBTPID).append('=').append(subtitlepid).append('&');
			sb.append(PARAM_SUBTTYPE).append('=').append(sst).append('&');
		}
		if (pcrpid > 0) {
			sb.append(PARAM_PCRPID).append('=').append(pcrpid).append('&');
		}
		if (freq != FrequencyInfo.INVALID_FREQUENCY) {
			sb.append(PARAM_FREQ).append('=').append(freq).append('&');
		}
		if (audioEcmPID > 0) {
			sb.append(PARAM_AECMPID).append('=').append(audioEcmPID).append('&');
		}
		if (videoEcmPID > 0) {
			sb.append(PARAM_VECMPID).append('=').append(videoEcmPID).append('&');
		}
		sb.append(PARAM_CA).append('=').append(needCA);
		if (pmtsection != null) {
			sb.append('&').append(PARAM_PMT_SECTION).append('=');
			encodeByteString(pmtsection, sb);
		}
		return sb.toString();
	}

	/**
	 * 设置节目号
	 * 
	 * @param pn
	 *            节目号值
	 */
	public void setProgramNumber(int pn) {
		program_number = pn;
	}

	/**
	 * 得到节目号
	 * 
	 * @return 值
	 */
	public int getProgramNumber() {
		return program_number;
	}

	/**
	 * 是否需要调节接收
	 * 
	 * @return 是返回true，否则返回false
	 */
	public boolean isCARequired() {
		return needCA;
	}

	/**
	 * 设置是否需条件接收
	 * 
	 * @param b
	 *            true为需要，false为不需要
	 */
	public void setCARequired(boolean b) {
		needCA = b;
	}

	/**
	 * 得到音频流PID
	 * 
	 * @return 音频流PID值
	 */
	public int getAudioPID() {
		return apid;
	}

	/**
	 * 设置音频流PID
	 * 
	 * @param pid
	 *            值
	 */
	public void setAudioPID(int pid) {
		this.apid = pid < 0 || pid > 0x1fff ? -1 : pid;
	}

	/**
	 * 是否存在有效的音频PID
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean hasValidAudioPID() {
		return apid > 0 && apid <= 0x1fff;
	}

	/**
	 * 是否存在有效的视频PID
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean hasValidVideoPID() {
		return vpid > 0 && vpid <= 0x1fff;
	}

	/**
	 * 得到音频流类型
	 * 
	 * @return 值
	 */
	public String getAudioStreamType() {
		return ast;
	}

	/**
	 * 设置音频流类型
	 * 
	 * @param type
	 *            音频流类型名称
	 */
	public void setAudioStreamType(String type) {
		this.ast = type;
	}

	/**
	 * 得到音频ECM PID
	 * 
	 * @return 值
	 */
	public int getAudioEcmPID() {
		return audioEcmPID;
	}

	/**
	 * 设置音频ECM PID
	 * 
	 * @param audioEcmPID
	 *            视频ECM PID值
	 */
	public void setAudioEcmPID(int audioEcmPID) {
		this.audioEcmPID = audioEcmPID;
	}

	/**
	 * 得到视频流PID
	 * 
	 * @return 值
	 */
	public int getVideoPID() {
		return vpid;
	}

	/**
	 * 设置音频流PID
	 * 
	 * @param pid
	 *            音频流PID值
	 */
	public void setVideoPID(int pid) {
		this.vpid = pid < 0 || pid > 0x1fff ? -1 : pid;
	}

	/**
	 * 得到视频流类型
	 * <p>
	 * 
	 * @return 值
	 */
	public String getVideoStreamType() {
		return vst;
	}

	/**
	 * 设置视频流类型
	 * 
	 * @param type
	 *            视频流类型值
	 */
	public void setVideoStreamType(String type) {
		this.vst = type;
	}

	/**
	 * 得到视频ECM PID
	 * 
	 * @return 值
	 */
	public int getVideoEcmPID() {
		return videoEcmPID;
	}

	/**
	 * 设置视频ECM PID
	 * 
	 * @param videoEcmPID
	 *            视频ECM PID值
	 */
	public void setVideoEcmPID(int videoEcmPID) {
		this.videoEcmPID = videoEcmPID;
	}

	/**
	 * 设置视频源播放速率
	 * 
	 * @param r
	 *            视频源播放速率值
	 */
	public void setVideoSourceRate(float r) {
		if (r >= 0.0f)
			vrate = r < 0.01f ? 0.01f : r;
		else
			vrate = r > -0.01f ? -0.01f : r;
	}

	/**
	 * 得到视频源播放速率
	 * 
	 * @return 值
	 */
	public float getVideoSourceRate() {
		return vrate;
	}

	/**
	 * 得到字幕流PID
	 * 
	 * @return 值
	 */
	public int getSubtitlePID() {
		return subtitlepid;
	}

	/**
	 * 设置字幕流PID
	 * 
	 * @param pid
	 *            字幕流PID值
	 */
	public void setSubtitlePID(int pid) {
		this.subtitlepid = pid < 0 || pid > 0x1fff ? -1 : pid;
	}

	/**
	 * 是否存在有效的字幕(Subtitle) PID
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean hasValidSubtitlePID() {
		return subtitlepid > 0 && subtitlepid <= 0x1fff;
	}

	/**
	 * 得到音频流类型
	 * 
	 * @return 值
	 */
	public String getSubtitleStreamType() {
		return sst;
	}

	/**
	 * 设置字幕流类型
	 * 
	 * @param type
	 *            字幕流类型名称
	 */
	public void setSubtitleStreamType(String type) {
		this.sst = type;
	}

	/**
	 * 设置音频流类型
	 * 
	 * @param v
	 *            字幕流类型值
	 */
	public void setSubtitleStreamType(int v) {
		this.sst = String.valueOf(v);
	}

	/**
	 * 得到PCR流PID
	 * 
	 * @return 值
	 */
	public int getPcrPID() {
		return pcrpid;
	}

	/**
	 * 设置PCR流PID
	 * 
	 * @param pid
	 *            PCR流PID值
	 */
	public void setPcrPID(int pid) {
		this.pcrpid = pid < 0 || pid > 0x1fff ? -1 : pid;
	}

	/**
	 * 是否存在有效的PCR PID
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean hasValidPcrPID() {
		return pcrpid > 0 && pcrpid <= 0x1fff;
	}

	/**
	 * 得到节目所在的频率
	 * <p>
	 * 默认为不指定
	 * 
	 * @return 值
	 */
	public long getFrequency() {
		return freq;
	}

	/**
	 * 设定节目所在的频率
	 * 
	 * @param f
	 *            节目所在的频率值
	 */
	public void setFrequency(long f) {
		freq = f;
	}

	/**
	 * 得到PMT Section数据的字节数组
	 * <p>
	 * 默认为不指定
	 * 
	 * @return 数组
	 */
	public byte[] getPmtSection() {
		return pmtsection;
	}

	/**
	 * 设定PMT Section数据的字节数组
	 * 
	 * @param b
	 *            数组
	 */
	public void setPmtSection(byte[] b) {
		pmtsection = b;
	}

	static final StringBuffer encodeByteString(byte[] b, StringBuffer sb) {
		for (int i = 0; i < b.length; i++)
			sb.append(mapb2c[b[i]]);
		return sb;
	}

	static final byte[] decodeByteString(String s) {
		if (s.length() % 2 != 0)
			throw new IllegalArgumentException("length % 2 != 0");
		int n = s.length() / 2;
		byte[] ret = new byte[n];
		for (int i = 0; i < n; i++) {
			int b1 = mapc2b[s.charAt(i * 2)];
			int b2 = mapc2b[s.charAt(i * 2 + 1)];
			if (b1 == 0xff || b2 == 0xff)
				throw new IllegalArgumentException("invalid char(" + s.charAt(i * 2) + ", "
						+ s.charAt(i * 2 + 1) + ")");
			ret[i] = (byte) ((b1 << 4) | b2);
		}
		return ret;
	}

	static byte[] mapc2b = new byte[128];
	static char[] mapb2c = new char[16];
	static {
		for (int i = 0; i < mapc2b.length; i++)
			mapc2b[i] = (byte) 0xff;
		for (int i = '0'; i <= '9'; i++)
			mapc2b[i] = (byte) i;
		for (int i = 'a'; i <= 'f'; i++)
			mapc2b[i] = (byte) (0xa + (i - 'a'));
		for (int i = 'A'; i <= 'F'; i++)
			mapc2b[i] = (byte) (0xa + (i - 'A'));
		for (int i = 0; i < 9; i++)
			mapb2c[i] = (char) ('0' + i);
		for (int i = 10; i < 16; i++)
			mapb2c[i] = (char) ('A' + i);
	}
}
