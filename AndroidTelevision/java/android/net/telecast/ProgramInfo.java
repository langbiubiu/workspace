package android.net.telecast;

/**
 * ��ĿƵ������
 * <p>
 * �����ݿ��е���Ϣ��������ο����´��룺<br>
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

	/** stream packet id δ���� */
	public static final int PID_UNDEFINED = -1;

	/**
	 * Ƶ������<br>
	 * ע�⣺�����׼(DVB,ATSC��)�������ֵ��˿��ܲ�����ͬ
	 */
	public static interface ChannelTypeEnum {
		/** ���ֵ��� */
		public static final int DIGITAL_TV = 0xff1;
		/** ���ֵ�̨ */
		public static final int DIGITAL_RADIO = 0xff3;
		/** ���ֵ��� */
		public static final int ANALOG_TV = 0xff4;
		/** ���ֵ�̨ */
		public static final int ANALOG_RADIO = 0xff5;
		/** ���� */
		public static final int OTHER = 0xffff;
	}

	/**
	 * ����������ö��
	 */
	public static interface StreamTypeNameEnum {
		/** ��Ƶ - MPEG1 */
		public static final String AUDIO_MPEG1 = "audio_mpeg1";
		/** ��Ƶ - MPEG2 */
		public static final String AUDIO_MPEG2 = "audio_mpeg2";
		/** ��Ƶ - AAC */
		public static final String AUDIO_AAC = "audio_aac";
		/** ��Ƶ - AC3 */
		public static final String AUDIO_AC3 = "audio_ac3";
		/** ��Ƶ - AC3 Plus */
		public static final String AUDIO_AC3_PLUS = "audio_ac3_plus";
		/** ��Ƶ - DTS */
		public static final String AUDIO_DTS = "audio_dts";
		/** ��Ƶ - MPEG1 */
		public static final String VIDEO_MPEG1 = "video_mpeg1";
		/** ��Ƶ - MPEG2 */
		public static final String VIDEO_MPEG2 = "video_mpeg2";
		/** ��Ƶ - H264 */
		public static final String VIDEO_H264 = "video_h264";
		/** ��Ƶ - H265 */
		public static final String VIDEO_H265 = "video_h265";
		/** PCR */
		public static final String PCR = "pcr";
		/** ��Ļ */
		public static final String SUBTITLE = "subtitle";
		/** ͼ�Ĺ㲥���� */
		public static final String TELETEXT = "teletext";
		/** ���� */
		public static final String OTHER = "other";
	}

	/**
	 * ��ƵԴ�ĳ�����ʽö��
	 */
	public static interface VideoPictureFormEnum {
		/** ���� - 2D */
		public static final int TYPE_2D = 0;
		/** ���� - 3D ���� */
		public static final int TYPE_3D_LEFT_RIGHT = 1;
		/** ���� - 3D ���� */
		public static final int TYPE_3D_TOP_BUTTOM = 2;
	}

	/**
	 * ��ƵԴ���������ʽö��
	 */
	public static interface AudioTrackFormEnum {
		/** ���� - ������ */
		public static final int TYPE_STEREO = 0;
		/** ���� - �������� */
		public static final int TYPE_LEFT = 1;
		/** ���� - �������� */
		public static final int TYPE_RIGHT = 2;
		/** ���� - ˫���� */
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
	/** uri ���� pmt_section */
	public static final String PARAM_PMT_SECTION = "pmt_section";
	/** uri ���� audio_stream_type */
	public static final String PARAM_ATYPE = "audio_stream_type";
	/** uri ���� audio_stream_pid */
	public static final String PARAM_APID = "audio_stream_pid";
	/** uri ���� video_stream_type */
	public static final String PARAM_VTYPE = "video_stream_type";
	/** uri ���� video_stream_pid */
	public static final String PARAM_VPID = "video_stream_pid";
	/** uri ���� pcr_stream_pid */
	public static final String PARAM_PCRPID = "pcr_stream_pid";
	/** uri ���� subtitle_pid */
	public static final String PARAM_SUBTPID = "subtitle_stream_pid";
	/** uri ���� subtitle_pid */
	public static final String PARAM_SUBTTYPE = "subtitle_stream_type";
	/** uri ���� frequency */
	public static final String PARAM_FREQ = "frequency";
	/** uri ���� ca_required */
	public static final String PARAM_CA = "ca_required";
	/** uri ���� video_source_rate */
	public static final String PARAM_VRATE = "video_source_rate";
	/** uri ���� audio_ecm_pid */
	public static final String PARAM_AECMPID = "audio_ecm_pid";
	/** uri ���� video_ecm_pid */
	public static final String PARAM_VECMPID = "video_ecm_pid";

	/**
	 * �������
	 */
	public ProgramInfo() {
	}

	/**
	 * ����Ƿ�Ϊ��Ƶ
	 * 
	 * @param name
	 *            ����
	 * @return ���򷵻�true�����򷵻�false
	 */
	public static boolean isAudioStream(String name) {
		return name.startsWith("audio");
	}

	/**
	 * ����Ƿ�Ϊ��Ƶ
	 * 
	 * @param name
	 *            ����
	 * @return ���򷵻�true�����򷵻�false
	 */
	public static boolean isVideoStream(String name) {
		return name.startsWith("video");
	}

	/**
	 * ����Ƿ�ΪPCR��
	 * 
	 * @param name
	 *            ����
	 * @return ���򷵻�true�����򷵻�false
	 */
	public static boolean isPcrStream(String name) {
		return name.startsWith("pcr");
	}

	/**
	 * ����Ƿ�Ϊ��Ļ��
	 * 
	 * @param name
	 *            ����
	 * @return ���򷵻�true�����򷵻�false
	 */
	public static boolean isSubtitleStream(String name) {
		return name.startsWith("subtitle");
	}

	/**
	 * ����Ƿ�Ϊͼ�ĵ��ӹ㲥ҵ����
	 * 
	 * @param name
	 *            ����
	 * @return ���򷵻�true�����򷵻�false
	 */
	public static boolean isTeletextStream(String name) {
		return name.startsWith("teletext");
	}

	/**
	 * �õ�MPEG��׼������Ƶ����������
	 * <p>
	 * �������ֵ����MPEG��׼����ķ�Χ,���߲�������Ƶ������,������null
	 * 
	 * @param type
	 *            ����
	 * @return ����
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
	 * ͨ���ַ�����������
	 * 
	 * @param s
	 *            �ַ���,�ο� {@link #toString()}
	 * @return ����
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
	 * �õ��ַ�����ʽ
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
	 * ���ý�Ŀ��
	 * 
	 * @param pn
	 *            ��Ŀ��ֵ
	 */
	public void setProgramNumber(int pn) {
		program_number = pn;
	}

	/**
	 * �õ���Ŀ��
	 * 
	 * @return ֵ
	 */
	public int getProgramNumber() {
		return program_number;
	}

	/**
	 * �Ƿ���Ҫ���ڽ���
	 * 
	 * @return �Ƿ���true�����򷵻�false
	 */
	public boolean isCARequired() {
		return needCA;
	}

	/**
	 * �����Ƿ�����������
	 * 
	 * @param b
	 *            trueΪ��Ҫ��falseΪ����Ҫ
	 */
	public void setCARequired(boolean b) {
		needCA = b;
	}

	/**
	 * �õ���Ƶ��PID
	 * 
	 * @return ��Ƶ��PIDֵ
	 */
	public int getAudioPID() {
		return apid;
	}

	/**
	 * ������Ƶ��PID
	 * 
	 * @param pid
	 *            ֵ
	 */
	public void setAudioPID(int pid) {
		this.apid = pid < 0 || pid > 0x1fff ? -1 : pid;
	}

	/**
	 * �Ƿ������Ч����ƵPID
	 * 
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean hasValidAudioPID() {
		return apid > 0 && apid <= 0x1fff;
	}

	/**
	 * �Ƿ������Ч����ƵPID
	 * 
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean hasValidVideoPID() {
		return vpid > 0 && vpid <= 0x1fff;
	}

	/**
	 * �õ���Ƶ������
	 * 
	 * @return ֵ
	 */
	public String getAudioStreamType() {
		return ast;
	}

	/**
	 * ������Ƶ������
	 * 
	 * @param type
	 *            ��Ƶ����������
	 */
	public void setAudioStreamType(String type) {
		this.ast = type;
	}

	/**
	 * �õ���ƵECM PID
	 * 
	 * @return ֵ
	 */
	public int getAudioEcmPID() {
		return audioEcmPID;
	}

	/**
	 * ������ƵECM PID
	 * 
	 * @param audioEcmPID
	 *            ��ƵECM PIDֵ
	 */
	public void setAudioEcmPID(int audioEcmPID) {
		this.audioEcmPID = audioEcmPID;
	}

	/**
	 * �õ���Ƶ��PID
	 * 
	 * @return ֵ
	 */
	public int getVideoPID() {
		return vpid;
	}

	/**
	 * ������Ƶ��PID
	 * 
	 * @param pid
	 *            ��Ƶ��PIDֵ
	 */
	public void setVideoPID(int pid) {
		this.vpid = pid < 0 || pid > 0x1fff ? -1 : pid;
	}

	/**
	 * �õ���Ƶ������
	 * <p>
	 * 
	 * @return ֵ
	 */
	public String getVideoStreamType() {
		return vst;
	}

	/**
	 * ������Ƶ������
	 * 
	 * @param type
	 *            ��Ƶ������ֵ
	 */
	public void setVideoStreamType(String type) {
		this.vst = type;
	}

	/**
	 * �õ���ƵECM PID
	 * 
	 * @return ֵ
	 */
	public int getVideoEcmPID() {
		return videoEcmPID;
	}

	/**
	 * ������ƵECM PID
	 * 
	 * @param videoEcmPID
	 *            ��ƵECM PIDֵ
	 */
	public void setVideoEcmPID(int videoEcmPID) {
		this.videoEcmPID = videoEcmPID;
	}

	/**
	 * ������ƵԴ��������
	 * 
	 * @param r
	 *            ��ƵԴ��������ֵ
	 */
	public void setVideoSourceRate(float r) {
		if (r >= 0.0f)
			vrate = r < 0.01f ? 0.01f : r;
		else
			vrate = r > -0.01f ? -0.01f : r;
	}

	/**
	 * �õ���ƵԴ��������
	 * 
	 * @return ֵ
	 */
	public float getVideoSourceRate() {
		return vrate;
	}

	/**
	 * �õ���Ļ��PID
	 * 
	 * @return ֵ
	 */
	public int getSubtitlePID() {
		return subtitlepid;
	}

	/**
	 * ������Ļ��PID
	 * 
	 * @param pid
	 *            ��Ļ��PIDֵ
	 */
	public void setSubtitlePID(int pid) {
		this.subtitlepid = pid < 0 || pid > 0x1fff ? -1 : pid;
	}

	/**
	 * �Ƿ������Ч����Ļ(Subtitle) PID
	 * 
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean hasValidSubtitlePID() {
		return subtitlepid > 0 && subtitlepid <= 0x1fff;
	}

	/**
	 * �õ���Ƶ������
	 * 
	 * @return ֵ
	 */
	public String getSubtitleStreamType() {
		return sst;
	}

	/**
	 * ������Ļ������
	 * 
	 * @param type
	 *            ��Ļ����������
	 */
	public void setSubtitleStreamType(String type) {
		this.sst = type;
	}

	/**
	 * ������Ƶ������
	 * 
	 * @param v
	 *            ��Ļ������ֵ
	 */
	public void setSubtitleStreamType(int v) {
		this.sst = String.valueOf(v);
	}

	/**
	 * �õ�PCR��PID
	 * 
	 * @return ֵ
	 */
	public int getPcrPID() {
		return pcrpid;
	}

	/**
	 * ����PCR��PID
	 * 
	 * @param pid
	 *            PCR��PIDֵ
	 */
	public void setPcrPID(int pid) {
		this.pcrpid = pid < 0 || pid > 0x1fff ? -1 : pid;
	}

	/**
	 * �Ƿ������Ч��PCR PID
	 * 
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean hasValidPcrPID() {
		return pcrpid > 0 && pcrpid <= 0x1fff;
	}

	/**
	 * �õ���Ŀ���ڵ�Ƶ��
	 * <p>
	 * Ĭ��Ϊ��ָ��
	 * 
	 * @return ֵ
	 */
	public long getFrequency() {
		return freq;
	}

	/**
	 * �趨��Ŀ���ڵ�Ƶ��
	 * 
	 * @param f
	 *            ��Ŀ���ڵ�Ƶ��ֵ
	 */
	public void setFrequency(long f) {
		freq = f;
	}

	/**
	 * �õ�PMT Section���ݵ��ֽ�����
	 * <p>
	 * Ĭ��Ϊ��ָ��
	 * 
	 * @return ����
	 */
	public byte[] getPmtSection() {
		return pmtsection;
	}

	/**
	 * �趨PMT Section���ݵ��ֽ�����
	 * 
	 * @param b
	 *            ����
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
