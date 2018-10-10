package com.ipanel.chongqing_ipanelforhw.downloading.utils;

import java.util.HashMap;
import java.util.Map;

public class DownLoadManager {
	public static String TAG = "DownLoadManager";
	private static DownLoadManager dwloadmanger;
	public Map<String, DownLoadToolUtil> resouceTaskMap;
	public Map<String, DownLoadToolUtil> apkTaskMap;

	private DownLoadManager() {
		resouceTaskMap = new HashMap<String, DownLoadToolUtil>();
		apkTaskMap = new HashMap<String, DownLoadToolUtil>();
	}

	public static synchronized DownLoadManager getInstance() {
		if (dwloadmanger == null) {
			dwloadmanger = new DownLoadManager();
			return dwloadmanger;
		} else
			return dwloadmanger;
	}

	public synchronized void addDownLoad(String fileName,
			DownLoadToolUtil task, Map<String, DownLoadToolUtil> map) {
		// Log.d(TAG, "addDownLoad is in");
		if ("".equals(fileName) || null == fileName)
			return;
		DownLoadToolUtil isExitTask = map.get(fileName);
		if (null != isExitTask) {
			return;
		} else {
			map.put(fileName, task);
			task.isResetTask(true);
			ThreadPoolUtils.execute(task);
		}
	}

	/**
	 * 暂停或继续下载任务
	 * 
	 * @param fileName
	 * @param map
	 * @param flag
	 */
	public synchronized void pauseDowunLoad(String fileName,
			Map<String, DownLoadToolUtil> map, boolean flag) {
		if ("".equals(fileName) || null == fileName) {
			return;
		}
		DownLoadToolUtil task = map.get(fileName);
		if (null != task) {
			task.pauseTask(flag);
			if (!flag) {
				task.isResetTask(false);
				ThreadPoolUtils.execute(task);
			}
		}
	}

	public synchronized void removeDowunLoad(String fileName,
			Map<String, DownLoadToolUtil> map) {
		if ("".equals(fileName) || null == fileName)
			return;
		DownLoadToolUtil task = map.get(fileName);
		// Log.i(TAG, "#####task will set null ...task=" + task);
		if (null != task) {
			map.remove(fileName);
			task.cancelTask(); // 取消下载任务
			task = null;
		}
	}
}
