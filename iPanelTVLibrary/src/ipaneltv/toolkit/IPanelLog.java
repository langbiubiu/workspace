package ipaneltv.toolkit;

public final class IPanelLog {
	public final static String LOGTAG = "lib - ";
	
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int NONE = 100;

    /**
     * 设置打印级别
     */
	private static int PRINT_LEVEL = DEBUG;

	public static void v(String tag, String msg) {
		if (VERBOSE>=PRINT_LEVEL)
			android.util.Log.v(LOGTAG+tag, msg);
	}
	
	public static void d(String tag, String msg) {
		if (DEBUG>=PRINT_LEVEL)
			android.util.Log.i(LOGTAG+tag, msg);
	}
	
	public static void i(String tag, String msg) {
		if (INFO>=PRINT_LEVEL)
			android.util.Log.i(LOGTAG+tag, msg);
	}
	
	public static void w(String tag, String msg) {
		if (WARN>=PRINT_LEVEL)
			android.util.Log.w(LOGTAG+tag, msg);
	}
	
	public static void e(String tag, String msg) {
		if (ERROR>=PRINT_LEVEL)
			android.util.Log.e(LOGTAG+tag, msg);
	}

	public static void e(String tag, String msg, Exception ex) {
		if (ERROR>=PRINT_LEVEL)
			android.util.Log.e(LOGTAG+tag, msg, ex);
	}
	
	public static void setPrintLevel(int level){
		PRINT_LEVEL = level;
	}

}
