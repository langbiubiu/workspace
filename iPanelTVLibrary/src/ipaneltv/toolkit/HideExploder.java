package ipaneltv.toolkit;

import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.ActivityManager;
import android.content.Context;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;

/**
 * 调用隐藏方法的助手类
 */
public class HideExploder {
	static final String TAG = "HideExploder";
	private final static Object refLock = new Object();
	private static Method systemProperties_get = null;
	private static Method systemProperties_set = null;
	private static Method memoryFile_getFD = null;
	private static Field libcore_os = null;
	private static Method os_pipe = null;
	private static Method ioutils_close = null;
	private static Method serviceManager_getService = null;
	private static Method forceStopPackage = null;
	
	/**
	 * 设置系统属性
	 * <p>
	 * 等价于android.os.SystemProperties.set()
	 * 
	 * @param name
	 * @param def 默认值
	 * @return 属性字符串
	 */
	public static void setSystemProperty(String name, String value){
		try {
			synchronized (refLock) {
				if (systemProperties_set == null) {
					systemProperties_set = Class.forName("android.os.SystemProperties").getMethod(
							"set", String.class,String.class);
				}
			}
			systemProperties_set.invoke(name, value);
		} catch (Exception e) {
			IPanelLog.e(TAG, "call method android.os.SystemProperties.set(" + name +","+ value + ") erro:" + e);
		}
	}

	/**
	 * 获得系统属性
	 * <p>
	 * 等价于android.os.SystemProperties.get()
	 * 
	 * @param name
	 * @param def
	 * @return 属性字符串
	 */
	public static String getSystemProperty(String name, String def) {
		try {
			synchronized (refLock) {
				if (systemProperties_get == null) {
					systemProperties_get = Class.forName("android.os.SystemProperties").getMethod(
							"get", String.class);
				}
			}
			String ret = (String) systemProperties_get.invoke(null, name);
			return ret == null ? def : ret;
		} catch (Exception e) {
			IPanelLog.e(TAG, "call method android.os.SystemProperties.get(" + name + ") erro:" + e);
			return def;
		}
	}

	/**
	 * 获得系统属性
	 * <p>
	 * 等价于android.os.SystemProperties.get()
	 * 
	 * @param s
	 * @return 属性字符串
	 */
	public static String getSystemProperty(String s) {
		return getSystemProperty(s, null);
	}

	/**
	 * 得到属性文件的文件描述符对象
	 * <p>
	 * 等价于android.os.MemoryFile.getFileDescriptor()
	 * 
	 * @param file
	 * @return
	 */
	public static FileDescriptor getFileDescriptorFrom(MemoryFile file) {
		try {
			synchronized (refLock) {
				if (memoryFile_getFD == null)
					memoryFile_getFD = Class.forName("android.os.MemoryFile").getMethod(
							"getFileDescriptor");
			}
			return (FileDescriptor) memoryFile_getFD.invoke(file);
		} catch (Exception e) {
			IPanelLog.e(TAG, "call method android.os.MemoryFile.getFileDescriptor() error:" + e);
			return null;
		}
	}

	/**
	 * 得到属性文件的文件描述符对象
	 * <p>
	 * 等价于libcore.io.Libcore.os.pipe()
	 * 
	 * @return 管道read[0],write[1]
	 */
	public static ParcelFileDescriptor[] openPosixPipe() {
		try {
			synchronized (refLock) {
				if (libcore_os == null) {
					libcore_os = Class.forName("libcore.io.Libcore").getField("os");
					os_pipe = libcore_os.getClass().getMethod("pipe");
				}
			}
			FileDescriptor[] pipe = (FileDescriptor[]) os_pipe.invoke(libcore_os);
			if (pipe == null)
				return null;
			ParcelFileDescriptor[] ret = new ParcelFileDescriptor[2];
			ret[0] = ParcelFileDescriptor.adoptFd(Natives.getfd(pipe[0]));
			ret[1] = ParcelFileDescriptor.adoptFd(Natives.getfd(pipe[1]));
			return ret;
		} catch (Exception e) {
			IPanelLog.e(TAG, "call method libcore.io.Libcore.os.pipe() error:" + e);
			return null;
		}
	}

	/**
	 * 关闭文件描述符
	 * <p>
	 * 等价于libcore.io.IoUtils.closeQuietly()
	 * 
	 * @param fd
	 */
	public static void closeFileDescriptor(FileDescriptor fd) {
		try {
			synchronized (refLock) {
				if (ioutils_close == null) {
					ioutils_close = Class.forName("libcore.io.IoUtils").getMethod("closeQuietly",
							FileDescriptor.class);
				}
			}
			ioutils_close.invoke(null, fd);
		} catch (Exception e) {
			IPanelLog.e(TAG, "call method libcore.io.IoUtils.closeQuietly() error:" + e);
		}
	}

	/**
	 * 直接得到Service的Binder
	 * 
	 * @param name
	 *            名称
	 * @return 对象
	 */
	public static IBinder getServiceByServiceManager(String name) {
		synchronized (refLock) {
			try {
				if (serviceManager_getService == null)
					serviceManager_getService = Class.forName("android.os.ServiceManager")
							.getDeclaredMethod("getService", String.class);
				return (IBinder) serviceManager_getService.invoke(null, name);
			} catch (Exception e) {
				IPanelLog.w(TAG, "getServiceByServiceManager error:" + e);
				return null;
			}
		}
	}

	/**
	 * 结束进程的方法
	 */
	public static void forceStopPackage(Context context, String packageName) {
		try {
			synchronized (refLock) {
				ActivityManager am = (ActivityManager) context
						.getSystemService(Context.ACTIVITY_SERVICE);
				if (forceStopPackage == null)
					forceStopPackage = Class.forName("android.app.ActivityManager")
							.getDeclaredMethod("forceStopPackage", String.class);
				forceStopPackage.invoke(am, packageName);
			}
		} catch (Exception e) {
			IPanelLog.d(TAG, "forceStopPackage error:" + e);
			e.printStackTrace();
		}
	}
}
