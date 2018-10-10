package ipaneltv.toolkit;

import java.util.Set;

import android.net.Uri;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.ProgramInfo;

public class UriToolkit {
	public static final int FREQUENCY_INFO_SCHEMA_ID = 1;
	public static final int PROGRAM_INFO_SCHEMA_ID = 2;
	public static final int VFREQ_SCHEMA_ID = 3;
	public static final int DVB_SERVICE_SCHEMA_ID = 4;
	public static final int PMT_SCHEMA_ID = 5;
	public static final int TEEVEEPLAY_SCHEMA_ID = 6;
	public static final int LOCALSOCK_SCHEMA_ID = 7;
	public static final int FILE_SCHEMA_ID = 8;
	public static final int VFREQ_PARAM_STREAM_FD_ID = 9;

	public static final String FREQUENCY_INFO_SCHEMA = "frequency://";
	public static final String PROGRAM_INFO_SCHEMA = "program://";
	public static final String VFREQ_SCHEMA = "vfrequency://";
	public static final String VFREQ_PARAM_STREAM_FD = "stream_fd://";
	public static final String DVB_SERVICE_SCHEMA = "service://";
	public static final String PMT_SCHEMA = "pmt://";
	public static final String TEEVEEPLAY_SCHEMA = "teeveeplay://";
	public static final String LOCALSOCK_SCHEMA = "localsock://";
	public static final String FILE_SCHEMA = "file://";

	public static int getSchemaId(String uri) {
		if (uri == null)
			return -1;
		if (uri.startsWith(FREQUENCY_INFO_SCHEMA)) {
			return FREQUENCY_INFO_SCHEMA_ID;
		} else if (uri.startsWith(PROGRAM_INFO_SCHEMA)) {
			return PROGRAM_INFO_SCHEMA_ID;
		} else if (uri.startsWith(VFREQ_SCHEMA)) {
			return VFREQ_SCHEMA_ID;
		} else if (uri.startsWith(DVB_SERVICE_SCHEMA)) {
			return DVB_SERVICE_SCHEMA_ID;
		} else if (uri.startsWith(PMT_SCHEMA)) {
			return PMT_SCHEMA_ID;
		} else if (uri.startsWith(TEEVEEPLAY_SCHEMA)) {
			return TEEVEEPLAY_SCHEMA_ID;
		} else if (uri.startsWith(LOCALSOCK_SCHEMA)) {
			return LOCALSOCK_SCHEMA_ID;
		}else if(uri.startsWith(FILE_SCHEMA)){
			return FILE_SCHEMA_ID;
		}else if(uri.startsWith(VFREQ_PARAM_STREAM_FD)){
			return VFREQ_PARAM_STREAM_FD_ID;
		}
		return -1;
	}

	public static String makeVirtualFrequencyUri(long vf, int fd) {
		return VFREQ_SCHEMA + vf + "?stream_fd=" + fd;
	}

	public static String makeDvbServiceUri(int serviceid) {
		return DVB_SERVICE_SCHEMA + serviceid;
	}

	public static String makeLocalSockUri(String name, String queried, long vf) {
		return LOCALSOCK_SCHEMA + name + "?" + (queried == null ? "" : queried + "&")
				+ "vfrequency=" + vf;
	}

	/**
	 * 将ProgramInfo的参数部分附加到uri里
	 * 
	 * @param serviceid
	 * @param pi
	 * @return
	 */
	public static String makeDvbServiceUri(int serviceid, ProgramInfo pi) {
		Uri uri = Uri.parse(pi.toString());
		return DVB_SERVICE_SCHEMA + serviceid + "?" + uri.getQuery();
	}

	public static String makePmtUri(int pmtpid) {
		return PMT_SCHEMA + pmtpid;
	}

	public static String makeLocalSockUri(String name) {
		return LOCALSOCK_SCHEMA + name;
	}

	private static void makeTeeveePlayUri2(String apx, StringBuffer sb, String suri, int flags) {
		Uri uri = Uri.parse(suri);
		Set<String> names = uri.getQueryParameterNames();
		sb.append(apx).append("flags=").append(flags);
		for (String n : names) {
			String v = uri.getQueryParameter(n);
			sb.append('&').append(apx).append(n).append('=').append(v);
		}
	}

	static final String _f_ = "_f_", _p_ = "_p_";

	public static String makeTeeveePlayUri(FrequencyInfo fi, int fflags, ProgramInfo pi, int pflags) {
		StringBuffer sb = new StringBuffer();
		sb.append(TEEVEEPLAY_SCHEMA);
		sb.append(fi.getFrequency()).append('.').append(pi.getProgramNumber()).append('?');
		makeTeeveePlayUri2(_f_, sb, fi.toString(), fflags);
		sb.append("&");
		makeTeeveePlayUri2(_p_, sb, pi.toString(), pflags);
		return sb.toString();
	}

	public static String makeTeeveePlayUri(FrequencyInfo fi, ProgramInfo pi) {
		return makeTeeveePlayUri(fi, 0, pi, 0);
	}

	private static String splitInfoUriInTeeveePlayUri(Uri uri, String apx, int index) {
		boolean firstIn = true;
		StringBuffer sb = new StringBuffer();
		Set<String> names = uri.getQueryParameterNames();
		if(apx.startsWith(_f_))
			sb.append(FrequencyInfo.SCHEMA);
		else
			sb.append(ProgramInfo.SCHEMA);
		sb.append(uri.getAuthority().split("\\.")[index]).append('?');
		boolean b = false;
		for (String n : names) {
			if (n.startsWith(apx)) {
				if (b) {
					b = !b;
				} else {
					if(!firstIn){
						sb.append('&');
					}else{
						firstIn = false;
					}
				}
				String v = uri.getQueryParameter(n);
				sb.append(n.substring(apx.length())).append('=').append(v);
			}
		}
		return sb.toString();
	}

	public static String splitFrequencyInfoUriInTeeveePlayUri(Uri uri) {
		return splitInfoUriInTeeveePlayUri(uri, _f_, 0);
	}

	public static String splitProgramInfoUriInTeeveePlayUri(Uri uri) {
		return splitInfoUriInTeeveePlayUri(uri, _p_, 1);
	}

	public static int getFrequencyFlagsInTeeveePlayUri(Uri uri) {
		return Integer.parseInt(uri.getQueryParameter(_f_ + "flags"));
	}

	public static int getProgramFlagsInTeeveePlayUri(Uri uri) {
		return Integer.parseInt(uri.getQueryParameter(_p_ + "flags"));
	}

	public static int getProgramServiceId(String puri) {
		return Integer.valueOf(puri.replaceAll(DVB_SERVICE_SCHEMA, ""));
	}

	public static int getProgramPmtId(String puri) {
		return Integer.valueOf(puri.replaceAll(PMT_SCHEMA, ""));
	}
}
