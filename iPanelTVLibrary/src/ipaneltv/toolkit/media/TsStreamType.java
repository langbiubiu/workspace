package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import android.net.telecast.ProgramInfo;

public class TsStreamType {
	public static final int TS_STREAM_TYPE_AUDIO_MPEG1 = 0x3;
	public static final int TS_STREAM_TYPE_AUDIO_MPEG2 = 0x4;
	public static final int TS_STREAM_TYPE_AUDIO_AC3_PLUS = 0x6;
	public static final int TS_STREAM_TYPE_AUDIO_MPEG2_AAC = 0xf;
	public static final int TS_STREAM_TYPE_AUDIO_MPEG4_LATM_AAC = 0x11;
	public static final int TS_STREAM_TYPE_AUDIO_AC3 = 0x81;
	public static final int TS_STREAM_TYPE_AUDIO_AC3_E = 0x91;
	public static final int TS_STREAM_TYPE_AUDIO_HDMV_DTS = 0x82;
	public static final int TS_STREAM_TYPE_AUDIO_LPCM = 0x83;
	public static final int TS_STREAM_TYPE_AUDIO_DTS_HD = 0x86;
	public static final int TS_STREAM_TYPE_AUDIO_EAC3 = 0x87;
	public static final int TS_STREAM_TYPE_AUDIO_DTS = 0x8a;

	public static final int TS_STREAM_TYPE_VIDEO_MPEG1 = 0x1;
	public static final int TS_STREAM_TYPE_VIDEO_MPEG2 = 0x2;
	public static final int TS_STREAM_TYPE_VIDEO_MPEG4 = 0x10;
	public static final int TS_STREAM_TYPE_VIDEO_H264 = 0x1b;
	public static final int TS_STREAM_TYPE_VIDEO_AVS = 0x42;
	public static final int TS_STREAM_TYPE_VIDEO_VC1 = 0xEA;

	public static final int TS_STREAM_TYPE_PCR = 0xA0;

	/**
	 * 根据值获取播放字符串
	 */
	public String getMPEGStreamTypeName(int stream_type) {
		IPanelLog.d("TsStreamType", "getMPEGStreamTypeName stream_type = " + stream_type);
		switch (stream_type) {
		case TS_STREAM_TYPE_AUDIO_MPEG1:
			return "audio_mpeg1";
		case TS_STREAM_TYPE_AUDIO_MPEG2:
			return "audio_mpeg2";
		case TS_STREAM_TYPE_AUDIO_AC3_PLUS:
			return "audio_ac3_plus";
		case TS_STREAM_TYPE_AUDIO_MPEG2_AAC:
			return "audio_aac";
		case TS_STREAM_TYPE_AUDIO_MPEG4_LATM_AAC:
			return "audio_mpeg4_latm_aac";
		case TS_STREAM_TYPE_AUDIO_AC3:
		case TS_STREAM_TYPE_AUDIO_AC3_E:
			return "audio_ac3";
		case TS_STREAM_TYPE_AUDIO_HDMV_DTS:
			return "audio_hdvm_dts";
		case TS_STREAM_TYPE_AUDIO_LPCM:
			return "audio_lpcm";
		case TS_STREAM_TYPE_AUDIO_DTS_HD:
			return "audio_dts_hd";
		case TS_STREAM_TYPE_AUDIO_EAC3:
			return "audio_eac3";
		case TS_STREAM_TYPE_AUDIO_DTS:
			return "audio_dts";

		case TS_STREAM_TYPE_VIDEO_MPEG1:
			return "video_mpeg1";
		case TS_STREAM_TYPE_VIDEO_MPEG2:
			return "video_mpeg2";
		case TS_STREAM_TYPE_VIDEO_MPEG4:
			return "video_mpeg4";
		case TS_STREAM_TYPE_VIDEO_H264:
			return "video_h264";
		case TS_STREAM_TYPE_VIDEO_AVS:
			return "video_avs";
		case TS_STREAM_TYPE_VIDEO_VC1:
			return "video_vc1";
		case TS_STREAM_TYPE_PCR:
			return ProgramInfo.StreamTypeNameEnum.PCR;
		default:
			return "";
		}
	}

	public static int getMPEGStreamComponentType(String name) {
		IPanelLog.d("TsStreamType", "getMPEGStreamTypeName name=" + name);
		if (name.equals("audio_mpeg1")) {
			return TS_STREAM_TYPE_AUDIO_MPEG1;
		} else if (name.equals("audio_mpeg2")) {
			return TS_STREAM_TYPE_AUDIO_MPEG2;
		} else if (name.equals("audio_mpeg2_aac")) {
			return TS_STREAM_TYPE_AUDIO_MPEG2_AAC;
		} else if (name.equals("audio_ac3_plus")) {
			return TS_STREAM_TYPE_AUDIO_AC3_PLUS;
		} else if (name.equals("audio_mpeg4_latm_aac")) {
			return TS_STREAM_TYPE_AUDIO_MPEG4_LATM_AAC;
		} else if (name.equals("audio_ac3")) {
			return TS_STREAM_TYPE_AUDIO_AC3;
		} else if (name.equals("audio_hdmv_dts")) {
			return TS_STREAM_TYPE_AUDIO_HDMV_DTS;
		} else if (name.equals("audio_lpcm")) {
			return TS_STREAM_TYPE_AUDIO_LPCM;
		} else if (name.equals("audio_dts_hd")) {
			return TS_STREAM_TYPE_AUDIO_DTS_HD;
		} else if (name.equals("audio_eac3")) {
			return TS_STREAM_TYPE_AUDIO_EAC3;
		} else if (name.equals("audio_dts")) {
			return TS_STREAM_TYPE_AUDIO_DTS;
		} else if (name.equals("video_mpeg1")) {
			return TS_STREAM_TYPE_VIDEO_MPEG1;
		} else if (name.equals("video_mpeg2")) {
			return TS_STREAM_TYPE_VIDEO_MPEG2;
		} else if (name.equals("video_mpeg4")) {
			return TS_STREAM_TYPE_VIDEO_MPEG4;
		} else if (name.equals("video_h264")) {
			return TS_STREAM_TYPE_VIDEO_H264;
		} else if (name.equals("video_avs")) {
			return TS_STREAM_TYPE_VIDEO_AVS;
		} else if (name.equals("video_vc1")) {
			return TS_STREAM_TYPE_VIDEO_VC1;
		} else {
			return 0;
		}
	}
}
