package cn.ipanel.android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Monitor app crash and generate report
 * 
 * @author Zexu
 *
 */
public final class CrashMonitor implements UncaughtExceptionHandler {

	public synchronized static void start(Context ctx){
		start(ctx, false);
	}
	
	public synchronized static void start(Context ctx, boolean silent) {
		if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CrashMonitor)) {
			Thread.setDefaultUncaughtExceptionHandler(new CrashMonitor(ctx, silent));
		}
	}

	private Context ctx;
	UncaughtExceptionHandler defaultHandler;
	private CharSequence appName;
	boolean silent = false;
	
	private static volatile boolean mCrashing = false;
	
	private CrashMonitor(Context ctx, boolean silent) {
		this.ctx = ctx.getApplicationContext();
		this.silent = silent;
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		ApplicationInfo app = ctx.getApplicationInfo();
		appName = app.loadLabel(ctx.getPackageManager());
		
		PrintWriter writer = new PrintWriter(System.out);
		printAppInfo(writer);
		writer.flush();
		writer.close();
	}

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	String generateCrashName(String exceptionMsg) {
		String name = "RP_" + sdf.format(new Date());
		if (exceptionMsg != null && exceptionMsg.length() > 0) {
			if (exceptionMsg.length() > 16)
				exceptionMsg = exceptionMsg.substring(0, 16);
			name += "-" + URLEncoder.encode(exceptionMsg);
		}
		return name;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		try {
			if(mCrashing)
				return;
			mCrashing = true;
			
			File dir = ctx.getExternalCacheDir();
			if (dir == null || !dir.canWrite())
				dir = ctx.getCacheDir();
			String fileName = generateCrashName(ex.getMessage());
			File log = new File(dir, fileName + ".log");
			log.setReadable(true, false);

			PrintWriter writer = new PrintWriter(log);
			printAppInfo(writer);

			writer.println();
			writer.println("Exception for crash --------");
			writer.println("Exception in thread- " + thread.getName() + "-" + thread.getId());
			ex.printStackTrace(writer);

			printDeviceInformation(writer);
			printNetworkInformation(writer);
			printThreadStack(writer);
			printLogcat(writer);
			writer.close();

			File hdump = null;
			if (ex instanceof OutOfMemoryError) {
				hdump = new File(dir, fileName + ".hprof");
				hdump.setReadable(true, false);
				try {
					Debug.dumpHprofData(hdump.getAbsolutePath());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					hdump = null;
				}
			}
			sendCrashReport(log, hdump);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// hand over to the default handler
		if (defaultHandler != null && !silent)
			defaultHandler.uncaughtException(thread, ex);
		else {
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(10);
		}
	}

	public void printThreadStack(PrintWriter writer) {
		writer.println();
		writer.println("Thread stack ---------");
		Map<Thread, StackTraceElement[]> traceMap = Thread.getAllStackTraces();
		for (Map.Entry<Thread, StackTraceElement[]> entry : traceMap.entrySet()) {
			writer.println("thread - " + entry.getKey().getName() + "-" + entry.getKey().getId());
			for (StackTraceElement e : entry.getValue()) {
				writer.println("    " + e.toString());
			}
		}
	}

	public void printAppInfo(PrintWriter writer) {
		try {
			PackageInfo pkg;
			pkg = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			writer.println("Application Info ---------");
			writer.println("appName: " + appName);
			writer.println("package: " + ctx.getPackageName());
			writer.println("versionName: " + pkg.versionName);
			writer.println("versionCode: " + pkg.versionCode);
			writer.println("sharedUserId: " + pkg.sharedUserId);
			writer.println("firstInstallTime: " + new Date(pkg.firstInstallTime).toString());
			writer.println("lastUpdateTime: " + new Date(pkg.lastUpdateTime).toString());
		} catch (NameNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
	private void printNetworkInformation(PrintWriter out) {
		out.println();
		out.println("Network Information --------");
		try {
			Enumeration<NetworkInterface> enu = NetworkInterface.getNetworkInterfaces();
			while(enu.hasMoreElements()){
				NetworkInterface ni = enu.nextElement();
				out.println("Interface "+ni.getName());
				byte[] mac = ni.getHardwareAddress();
				String macStr = null;
                if (mac!=null){ 
	                StringBuilder buf = new StringBuilder();
	                for (int idx=0; idx<mac.length; idx++)
	                    buf.append(String.format("%02X:", mac[idx]));       
	                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
	                
	                macStr = buf.toString();
	                out.println("\tMAC:"+macStr);
                }
                Enumeration<InetAddress> aenu = ni.getInetAddresses();
                while(aenu.hasMoreElements()){
                	InetAddress addr = aenu.nextElement();
                	if(!addr.isLoopbackAddress()){
                		String sAddr = addr.getHostAddress().toUpperCase();

                        out.println("\tIP:"+sAddr);
                	}
                }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void printDeviceInformation(PrintWriter out) {
		out.println();
		out.println("Memory Information --------");
		MemoryInfo mem = new MemoryInfo();
		Debug.getMemoryInfo(mem);
		out.println("TotalPss: " + mem.getTotalPss() + "KB");
		out.println("TotalPrivateDirty: " + mem.getTotalPrivateDirty() + "KB");
		out.println("TotalSharedDirty: " + mem.getTotalSharedDirty() + "KB");
		out.println("HeapAllocatedSize: " + Debug.getNativeHeapAllocatedSize() / 1024 + "KB");
		out.println("HeapSize: " + Debug.getNativeHeapSize() / 1024 + "KB");

		out.println();
		out.println("Device Information ---------");
		out.println("manufactor: " + Build.MANUFACTURER);
		out.println("model: " + Build.MODEL);
		out.println("version: " + Build.VERSION.RELEASE);
		out.println("product: " + Build.PRODUCT);
		out.println("hardware: " + Build.HARDWARE);
		out.println("board: " + Build.BOARD);
		out.println("device: " + Build.DEVICE);
		out.println("CPU_ABI: " + Build.CPU_ABI);
		out.println("CPU_ABI2: " + Build.CPU_ABI2);

		out.println();
		out.println("Display Information --------");
		DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
		out.println("Width: " + dm.widthPixels);
		out.println("Height: " + dm.heightPixels);
		out.println("Density: " + dm.density);
		out.println("DPI: " + dm.densityDpi);
		out.println("ScaledDensity: " + dm.scaledDensity);

	}

	private void printLogcat(PrintWriter ps) {
		try {
			ps.println();
			ps.println("Logcat Information --------");
			ArrayList<String> commandLine = new ArrayList<String>();
			commandLine.add("logcat");
			commandLine.add("-d");
			commandLine.add("-v");
			commandLine.add("time");
			commandLine.add("*:V");

			Process process = Runtime.getRuntime().exec(
					commandLine.toArray(new String[commandLine.size()]));
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					ps.println(line);
				}
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendCrashReport(File log, File hdump) {
		if(silent)
			return;
		
		final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Crash report for " + appName);
		// has to be an ArrayList
		ArrayList<Uri> uris = new ArrayList<Uri>();
		uris.add(Uri.fromFile(log));
		if (hdump != null) {
			uris.add(Uri.fromFile(hdump));
		}

		emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		Intent chooser = Intent.createChooser(emailIntent, "Send Crash Report for " + appName);

		PendingIntent pi = PendingIntent.getActivity(ctx, 0, chooser, 0);
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 4000, pi);
	}
}
