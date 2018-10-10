package cn.ipanel.android.util;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.Enumeration;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.os.Build;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class DebugUtils {
	public static final String TAG = DebugUtils.class.getSimpleName();

	public static void printViewState(String desc, View v) {
		Rect r = new Rect();
		v.getGlobalVisibleRect(r);
		Log.d(TAG, desc + "globalVisibleRect: left=" + r.left + ", right=" + r.right + ", top="
				+ r.top + ", bottom=" + r.bottom + ", visibility = " + v.getVisibility()
				+ ", focusable=" + v.isFocusable() + ", isFocused=" + v.isFocused());
	}

	public static void printAllChildren(String tag, ViewGroup vg, boolean recursive) {
		int count = vg.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = vg.getChildAt(i);
			printViewState(tag + " " + i + " - 0x" + Integer.toHexString(v.getId()) + " ", v);
			if (v instanceof ViewGroup && recursive) {
				printAllChildren(tag, (ViewGroup) v, recursive);
			}
		}
	}
	
	public static void printAppInfo(Context ctx, PrintWriter writer) {
		try {
			PackageInfo pkg;
			pkg = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			ApplicationInfo appInfo = ctx.getApplicationInfo();
			writer.println("Application Info ---------");
			writer.println("appName: " + appInfo.loadLabel(ctx.getPackageManager()));
			writer.println("sourceDir: " + appInfo.sourceDir);
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
	
	public static void printDeviceInformation(Context ctx, PrintWriter out) {
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
	
	public static void printNetworkInformation(PrintWriter out) {
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

}
