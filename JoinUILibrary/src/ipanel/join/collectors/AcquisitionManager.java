package ipanel.join.collectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;
import cn.ipanel.android.net.cache.JSONApiHelper;

/**
 * 主要线程 1、文件上传线程，会一直监测压缩包目录下的文件，定期遍历目录下文件，如果文件过期就删除。
 * 并且选择离现在时间最近的压缩包来上传。删除已上传的压缩包,保留上传失败的压缩包。
 * 
 * 2、数据打包线程：将消息目录下文件，压缩成一个以当前时间命名的压缩包，然后清空消息目录，最后保存到压缩包目录。
 * 
 * 3、时间间隔线程：当时间间隔大约5分钟时，会触发数据打包线程。
 * 
 * 4、消息收集线程：会一直检测外界传入的消息，然后将消息写入消息目录下对应的文件，如果消息文件目录大于10M，会触发数据打包线程。
 * 
 * */

public class AcquisitionManager {
	private static final String TAG = AcquisitionManager.class.getSimpleName();
	private static AcquisitionManager mInstance;
	private IAcquisitionConfig config = null;
	private Context cxt;
	/** 上一次打包消息文件的时间 */
	private long lastCompressTime = -1l;
	private long lastWriteTime = -1l;

	private Object file_lock = new Object();
	private Object compress_lock = new Object();
	private Object upload_lock = new Object();

	private final LinkedBlockingQueue<Collector> msgQueue = new LinkedBlockingQueue<Collector>();
	private final LinkedList<Collector> msgs = new LinkedList<Collector>();
	/** 内存中最大存储消息数 */
	private final int MAX_MEMERY_MSG_COUNT = 10;
	private final String ZIP_FILE_NAME_SPLIT = "__";

	public static synchronized AcquisitionManager getInstance() {
		if (mInstance == null) {
			mInstance = new AcquisitionManager();
		}
		return mInstance;
	}

	private AcquisitionManager() {
		lastWriteTime = lastCompressTime = System.currentTimeMillis();
		new Thread(new AcquisitionCompressRunnable()).start();
		new Thread(new AcquisitionTimeRunnable()).start();
		new Thread(new AcquisitionSaveRunnable()).start();
		new Thread(new AcquisitionUploadRunnable()).start();

	}

	/***
	 * 数据本地存储线程
	 * */
	class AcquisitionSaveRunnable implements Runnable {

		@Override
		public void run() {
			while (true) {
				if (config == null) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					try {
						// 从队列中获取收集来的消息
						msgs.add(msgQueue.take());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 时间检测线程
	 * */
	class AcquisitionTimeRunnable implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (config != null && checkCOnfigTimeUploadCondition()) {
					Log.i(TAG, "noticy compress for time");
					nocityCompressThread();
				}
				// 如果内存中消息数达到上限，就转存到文件中
				if (msgs.size() > MAX_MEMERY_MSG_COUNT) {
					appendAllMsgToFile(true);
				} else {
					if (System.currentTimeMillis() - lastWriteTime > 60 * 1000) {
						if (msgs.size() > 0) {
							appendAllMsgToFile(true);
						}
					}
				}

			}
		}
	}

	/**
	 * 文件压缩线程
	 * */
	class AcquisitionCompressRunnable implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					synchronized (compress_lock) {
						compress_lock.wait();
					}
					lastCompressTime = System.currentTimeMillis();
					boolean success = false;
					synchronized (file_lock) {
						success = fileToZip();
					}
					if (success) {
						Log.i(TAG, "success compress a file");
						nocityUploadThread();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 上传检测线程
	 * */
	class AcquisitionUploadRunnable implements Runnable {
		@Override
		public void run() {
			while (true) {
				if (config == null) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					try {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						File file = null;
						synchronized (file_lock) {
							file = getUploadFile();
						}
						if (file != null) {
							if (upload(file)) {
								Log.i(TAG,
										"successfully,upload a file to server");
								file.delete();
							}
						} else {
							synchronized (upload_lock) {
								upload_lock.wait();
							}
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}
	}

	/**
	 * 通知压缩线程开始压缩文件
	 * */
	private void nocityCompressThread() {
		if (config != null) {
			synchronized (compress_lock) {
				compress_lock.notify();
			}
		}
	}

	/**
	 * 通知上传线程开始上传文件
	 * */
	private void nocityUploadThread() {
		if (config != null && JSONApiHelper.isOnline(cxt)) {
			synchronized (upload_lock) {
				upload_lock.notify();
			}
		}
	}

	/**
	 * 上传文件是否达到大小限制
	 * */
	private boolean checkCOnfigSizeUploadCondition() {
		boolean result = false;
		File dir = new File(getSaveDirPath());
		if (!dir.exists()) {
			dir.mkdir();
		}
		try {
			long size = getFileSize(dir);
			long max = getAcquisitionConfig().getMaxUploadFileSize();
			Log.i(TAG, String.format(
					"%s .  compare file size max %s : current %s ",
					(size > max) + "", max, size));
			return size > max;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 上传文件是否达到时间限制
	 * */
	private boolean checkCOnfigTimeUploadCondition() {
		long now = System.currentTimeMillis();
		return now - lastCompressTime > getAcquisitionConfig()
				.getUploadHearter();
	}

	private File getUploadFile() {
		String zipFilePath = getZipDirPath();
		File dir = new File(zipFilePath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		File[] zips = dir.listFiles();
		int length = zips == null ? 0 : zips.length;
		long now = System.currentTimeMillis();
		int index = -1;
		long min = Long.MAX_VALUE;
		long duration = getAcquisitionConfig().getZipSaveDuration();
		for (int i = 0; i < length; i++) {
			File file = zips[i];
			String name = file.getName().split("\\.")[0];
			long save = 0;
			try {
				if (name.contains(ZIP_FILE_NAME_SPLIT)) {
					save = Long.parseLong(name.split(ZIP_FILE_NAME_SPLIT)[1]);
				} else {
					save = 0;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				save = 0;
			}
			long time = now - save;
			if (time > duration) {
				file.deleteOnExit();
				Log.i(TAG,
						String.format("delete a zip file %s", file.getName()));
			} else {
				if (time < min) {
					min = time;
					index = i;
				}
			}
		}

		if (index >= 0) {
			return zips[index];
		} else {
			return null;
		}
	}

	/**
	 * 根据策略获得不同的配置
	 * */
	public void setAcquisitionConfig(IAcquisitionConfig config) {
		this.config = config;
	}

	public IAcquisitionConfig getAcquisitionConfig() {
		if (config == null) {
			throw new IllegalStateException(
					"before call getAcquisitionConfig ,you should call setAcquisitionConfig firstly");
		}
		return config;
	}

	public void initContext(Context context) {
		this.cxt = context.getApplicationContext();
		this.cxt.registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if (JSONApiHelper.isOnline(cxt)) {
					nocityUploadThread();
				}
			}
		}, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	public void handleAcquisitionIntent(Intent intent, Context cxt) {
		Collector msg = getAcquisitionConfig().parseAcquistitionMessage(intent);
		Log.i(TAG, String.format("parse msg %s", msg));
		if (msg.isValid()) {
			msgQueue.add(msg);
		}
	}

	public void appendSpecialMessage(Collector msg) {
		Log.i(TAG, String.format("parse special msg %s", msg));
		if (msg.isValid()) {
			msgQueue.add(msg);
		}
	}

	/**
	 * 将消息保存到本地文件 noticy：是否有必要通知上传线程
	 * */
	@SuppressLint("NewApi")
	private void appendAllMsgToFile(boolean noticy) {
		Log.i(TAG, String.format("save all msg size %s", msgs.size()));
		lastWriteTime = System.currentTimeMillis();
		synchronized (file_lock) {
			File dir = new File(getSaveDirPath());
			if (!dir.exists()) {
				dir.mkdir();
			}
			int lenght = msgs.size();
			for (int i = 0; i < lenght; i++) {
				appendMsgToFile(msgs.get(i));
			}
		}
		msgs.clear();
		if (noticy) {
			if (config != null && checkCOnfigSizeUploadCondition()) {
				Log.i(TAG, "noticy compress for size");
				nocityCompressThread();
			}
		}

	}

	private String getSaveDirPath() {
		return cxt.getFilesDir().getPath() + "/collector";
	}

	private String getZipDirPath() {
		return cxt.getFilesDir().getPath() + "/zip";
	}

	/**
	 * 将消息保存到本地文件
	 * */
	@SuppressLint("NewApi")
	private void appendMsgToFile(Collector msg) {
		Log.i(TAG, String.format("save msg %s", msg));
		synchronized (file_lock) {
			try {
				String path = getSaveDirPath() + "/" + msg.getOwner() + ".dat";
				File file = new File(path);
				if (!file.exists()) {
					file.createNewFile();
				}
				FileOutputStream out = new FileOutputStream(file, true);
				out.write((msg.getMsg() + "\n").getBytes());
				out.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private long getFileSize(File file) {
		long size = 0;
		try {
			if (file.exists()) {
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					int length = files == null ? 0 : files.length;
					for (int i = 0; i < length; i++) {
						size += getFileSize(files[i]);
					}
				} else {
					FileInputStream fis = null;
					fis = new FileInputStream(file);
					size = fis.available();
					fis.close();
				}
			} else {
				if (file.isDirectory()) {
					file.mkdir();
				} else {
					file.createNewFile();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return size;
	}

	/**
	 * 将消息保存到FTP服务器
	 * */
	private boolean upload(File file) {
		Log.i(TAG, "upload file to ftp");
		if (!JSONApiHelper.isOnline(cxt)) {
			return false;
		}
		if (config == null) {
			return false;
		}
		try {
			return uploadFile(file, getAcquisitionConfig().getServerAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean uploadFile(File file, String url) {
		if (!file.exists()) {
			return false;
		}
		boolean result = false;
		PostMethod postMethod = new PostMethod(url);
		try {
			// FilePart：用来上传文件的类
			FilePart fp = new FilePart("filedata", file);
			Part[] parts = { fp };

			// 对于MIME类型的请求，httpclient建议全用MulitPartRequestEntity进行包装
			MultipartRequestEntity mre = new MultipartRequestEntity(parts,
					postMethod.getParams());
			postMethod.setRequestEntity(mre);
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams()
					.setConnectionTimeout(50000);// 设置连接时间
			int status = client.executeMethod(postMethod);
			result = status == HttpStatus.SC_OK;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			// 释放连接
			postMethod.releaseConnection();
		}
		return result;
	}

	/**
	 * 打包需要上传的文件
	 * */
	private boolean fileToZip() {
		String sourceFilePath = getSaveDirPath();
		String zipFilePath = getZipDirPath();
		File dir = new File(zipFilePath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		String fileName = getAcquisitionConfig().getUserToken()
				+ ZIP_FILE_NAME_SPLIT + System.currentTimeMillis() + "";
		boolean flag = false;
		File sourceFile = new File(sourceFilePath);
		if (!sourceFile.exists()) {
			sourceFile.mkdir();
		}
		if (sourceFile.exists() == false) {
			return false;
		} else {
			File[] sourceFiles = sourceFile.listFiles();
			if (null == sourceFiles || sourceFiles.length < 1) {
				return false;
			} else {
				synchronized (file_lock) {
					try {
						ZipFile zipFile = new ZipFile(zipFilePath + "/"
								+ fileName + ".zip");

						ArrayList<File> filesToAdd = new ArrayList<File>();
						for (int i = 0; i < sourceFiles.length; i++) {
							filesToAdd.add(sourceFiles[i]);
						}
						ZipParameters parameters = new ZipParameters();
						parameters
								.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

						parameters
								.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
						parameters.setEncryptFiles(true);

						parameters
								.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
						parameters
								.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
						String password =getAcquisitionConfig().getZipPassword();
						if(!TextUtils.isEmpty(password)){
							parameters.setPassword(password);
						}
						zipFile.addFiles(filesToAdd, parameters);
						flag = true;
					} catch (ZipException e) {
						e.printStackTrace();
						flag = false;
					}
				}
			}

		}
		if (flag) {
			boolean d = deleteDir(sourceFile);
			Log.i(TAG, "after compressed,delete source file " + d);
		}
		return flag;
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * 
	 * @param dir
	 *            将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	private boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	// //////////////////////////////////////////////////////////
}
