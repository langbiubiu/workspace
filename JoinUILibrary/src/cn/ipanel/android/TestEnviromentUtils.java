package cn.ipanel.android;

import cn.ipanel.android.reflect.SysUtils;

/**
 * 
 * @author Zexu
 *
 */
public class TestEnviromentUtils {
	public static final String PROP_DTV_TEST = "persist.sys.dtv.test";
	public static final String PROP_DTV_UUID = "persist.sys.dtv.uuid";
	
	public static final String PROP_HOMED_HOST = "persist.sys.homed.host";
	public static final String PROP_HOMED_DEVICENO = "persist.sys.homed.deviceno";
	public static final String PROP_HOMED_DEVICETYPE = "persist.sys.homed.devicetype";
	
	public static boolean isDtvTest(){
		return "true".equals(SysUtils.getSystemProperty(PROP_DTV_TEST));
	}
	
	public static String getDtvUUID(){
		return SysUtils.getSystemProperty(PROP_DTV_UUID);
	}

	public static String getHomedHost(){
		return SysUtils.getSystemProperty(PROP_HOMED_HOST);
	}

	public static String getHomedDeviceNo(){
		return SysUtils.getSystemProperty(PROP_HOMED_DEVICENO);
	}

	public static String getHomedDeviceType(){
		return SysUtils.getSystemProperty(PROP_HOMED_DEVICETYPE);
	}
}
