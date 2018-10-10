package ipaneltv.toolkit;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

public class ProcessToolkit {
	static final String TAG = ProcessToolkit.class.getSimpleName();
	private Context mContext = null;
	private static ActivityManager mActivityManager = null;
	private int pid, uid;

	public ProcessToolkit(Context context) {
		this.mContext = context;
		mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
	}

	private void ensureRunOnce(String processName) {
		synchronized (TAG) {
			if (processName == null || "".equals(processName))
				return;

			List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager
					.getRunningAppProcesses();

			for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
				int pid = appProcess.pid; // pid
				int uid = appProcess.uid; // uid
				String procName = appProcess.processName; // 进程名
				IPanelLog.i(TAG, "processName: " + processName + "  pid: " + pid);
				if (processName.equals(procName)) {
					this.pid = pid;
					this.uid = uid;
					this.brunget = true;
					IPanelLog.d(TAG, "process pid=" + pid + ",uid=" + uid);
					break;
				}
			}
		}
	}

	private boolean brunget = false;
	private String currprocessName = null;

	public int getPid(String processName) {
		if (processName == null || "".equals(processName))
			return 0;

		if (currprocessName == null && !processName.equals(currprocessName)) {
			this.currprocessName = processName;
			this.brunget = false;
		}

		if (!brunget)
			ensureRunOnce(processName);
		return this.pid;
	}

	public int getUid(String processName) {
		if (processName == null || "".equals(processName))
			return 0;

		if (currprocessName == null && !processName.equals(currprocessName)) {
			this.currprocessName = processName;
			this.brunget = false;
		}

		if (!brunget)
			ensureRunOnce(processName);
		return this.uid;
	}

	public Debug.MemoryInfo getMemInfo(String processName) {
		if (processName == null || "".equals(processName))
			return null;

		List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager
				.getRunningAppProcesses();

		for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
			int pid = appProcess.pid; // pid
			int uid = appProcess.uid; // uid
			String procName = appProcess.processName; // 进程名
			IPanelLog.i(TAG, "processName: " + processName + "  pid: " + pid);
			if (processName.equals(procName)) {
				int[] myMempid = new int[] { pid };
				Debug.MemoryInfo[] memoryInfo = mActivityManager.getProcessMemoryInfo(myMempid);
				int memSize = memoryInfo[0].dalvikPrivateDirty;
				IPanelLog.i(TAG, "processName: " + processName + "  pid: " + pid + " uid:" + uid
						+ " memorySize is -->" + memSize + "kb");
				return memoryInfo[0];
			}
		}
		return null;
	}
	
	
}
