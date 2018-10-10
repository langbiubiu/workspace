package cn.ipanel.android.util;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Swallow system exception dialog, use with caution!
 * @author Zexu
 *
 */
public class CrashSwallow implements UncaughtExceptionHandler {
	public synchronized static void start() {
		if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CrashMonitor)) {
			Thread.setDefaultUncaughtExceptionHandler(new CrashSwallow());
		}
	}

	private CrashSwallow() {
		super();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		try{
			ex.printStackTrace();
		}catch(Throwable t){
			//ignore
		}
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(10);
	}

}
