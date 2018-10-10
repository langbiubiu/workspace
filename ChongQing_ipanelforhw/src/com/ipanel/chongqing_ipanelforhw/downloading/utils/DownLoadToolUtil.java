package com.ipanel.chongqing_ipanelforhw.downloading.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import android.util.Log;

public class DownLoadToolUtil implements Runnable {
	public static String TAG = "DownLoadToolUtil";
	private File updatefile;
	private String url;
	private String fileName;
	private boolean isReset;
	private HttpClient client = HttpClientHelper.getHttpClient();
	private DownLoadCallBack callBack;
	private boolean cancel_flag;
	private boolean pause_flag;

	public DownLoadToolUtil(String fileName, String url, boolean isReset,
			DownLoadCallBack callback) {
		this.url = url;
		this.fileName = fileName;
		this.isReset = isReset;
		this.callBack = callback;
	}

	public File getUpdatefile() {
		return updatefile;
	}

	public void setUpdatefile(File updatefile) {
		this.updatefile = updatefile;
	}

	public void cancelTask() {
		this.cancel_flag = true;
	}

	public void pauseTask(boolean flag) {
		this.pause_flag = flag;
	}

	public void isResetTask(boolean isReset) {
		this.isReset = isReset;
	}

	@Override
	public void run() {
		Log.d(TAG, "fileName:" + fileName + "\nurl:" + url);
		long remofilelength = 0;
		int msg = DownLoadCallBack.DOWNLOAD_FAILD;
		try {
			updatefile = new File(fileName);
			if (updatefile.exists()) {
				long curfilelength = updatefile.length();
				Log.v(TAG, "curfilelength=" + curfilelength
						+ ",remofilelength=" + remofilelength);
				if (isReset) {
					updatefile.delete();
					try {
						updatefile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Log.d(TAG, "重新下载");
					msg = downFile(fileName, url, 0);
				} else {
					Log.d(TAG, "断点续传进入");
					msg = downFile(fileName, url, curfilelength);
				}
			} else {
				new File(fileName).getParentFile().mkdirs();// 创建目录
				try {
					Log.d(TAG, "第一次下载");
					updatefile.createNewFile();// 创建文件
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				msg = downFile(fileName, url, 0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			msg = DownLoadCallBack.DOWNLOAD_FAILD;
		} finally {
			callBack.notifydownload(this, msg);
		}

	}

	public int downFile(String fileName, String url, long curfilelength) {
		InputStream input = null;
		RandomAccessFile file = null;
		int readSize = -1;
		byte[] buffer = new byte[8024];
		// Log.d(TAG, "downFile url = " + url);
		// Log.d(TAG, "downFile fileName = " + url);
		// Log.d(TAG, "downFile curfilelength = " + curfilelength);
		if (fileName.substring(fileName.length() - 4).equalsIgnoreCase(".apk")) {
			client = new DefaultHttpClient();
		}
		try {
			if (curfilelength != 0) {
				// url = FilePath.GET_INTERRUPT_APK + "?url=" + url +
				// "&fileSize="
				// + curfilelength;
				// Log.d(TAG, "after change" + url);
			}
			HttpGet request = new HttpGet();
			request.addHeader("Range", "bytes=" + curfilelength + "-");
			request.setURI(new URI(url));
			HttpResponse response = client.execute(request);
			// if (response.getStatusLine().getStatusCode () != 200) {
			Log.i(TAG, "downFile,http Status Code is:"
					+ response.getStatusLine().getStatusCode());
			// return DownLoadCallBack.DOWNLOAD_FAILD;
			// }
			if (response.getStatusLine().getStatusCode() == 404) {
				Log.i(TAG, "downFile,http download fail!");
				return DownLoadCallBack.DOWNLOAD_FAILD;
			}
			file = new RandomAccessFile(fileName, "rw");
			file.seek(curfilelength);
			input = response.getEntity().getContent();
			while ((readSize = input.read(buffer)) != -1) {
				file.write(buffer, 0, readSize);
				// Log.i(TAG, "####readSize:" + readSize + ",cancel_flag:"
				// + cancel_flag + ",pause_flag=" + pause_flag);
				if (cancel_flag) {
					File f = new File(fileName);
					if (f.exists()) {
						f.delete();
					}
					return DownLoadCallBack.DOWNLOAD_FAILD;
				}
				if (pause_flag) {
					/**
					 * 暂停下载
					 */
					return DownLoadCallBack.DOWNLOAD_PAUSE;
				}
			}
			Log.i(TAG, "down succ");
			return DownLoadCallBack.DOWNLOAD_SUCCESS;
		} catch (Exception e) {
			// downFile(fileName,url,curfilelength);
			e.printStackTrace();
			return DownLoadCallBack.DOWNLOAD_FAILD;
		} finally {
			if (input != null) {
				try {
					input.close();
					input = null;
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
			if (file != null) {
				try {
					file.close();
					file = null;
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
			// Log.i(TAG, "finally input close...");
		}
	}

	/**
	 * 断点续传
	 * 
	 * @param strPath
	 * @param filename
	 * @param size
	 * @return
	 */
	public static int doDownloadTheFile(String strPath, String filename,
			long size) {
		// 下载路径
		String url = strPath;
		HttpResponse response = null;
		// 用来获取下载文件的大小
		HttpResponse response_test = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpClient client_test = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			HttpGet request_test = new HttpGet(url);
			response_test = client_test.execute(request_test);
			long fileSize = response_test.getEntity().getContentLength();
			Log.v(TAG, "获取需要下载文件的大小fileSize=" + fileSize);
			Log.v(TAG, "验证下载文件的完整性size=" + size);
			if (fileSize != 0 && fileSize == size) {
				return DownLoadCallBack.DOWNLOAD_SUCCESS;
			}
			Log.v(TAG, "设置下载的数据位置XX字节到XX字节bytes=" + size + "-" + fileSize);
			Header header_size = new BasicHeader("Range", "bytes=" + size + "-"
					+ fileSize);
			request.addHeader(header_size);
			response = client.execute(request);
			InputStream is = response.getEntity().getContent();
			if (is == null) {
				throw new RuntimeException("stream is null");
			}
			Log.v(TAG, "获取文件对象，开始往文件里面写内容.filename=" + filename);
			File myTempFile = new File(filename);
			RandomAccessFile fos = new RandomAccessFile(myTempFile, "rw");
			Log.v(TAG, "从文件的size以后的位置开始写入，其实也不用，直接往后写就可以。有时候多线程下载需要用");
			fos.seek(size);
			byte buf[] = new byte[1024];
			do {
				int numread = is.read(buf);
				if (numread <= 0) {
					break;
				}
				fos.write(buf, 0, numread);
			} while (true);
			is.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return DownLoadCallBack.DOWNLOAD_FAILD;
		}
		return DownLoadCallBack.DOWNLOAD_SUCCESS;
	}

}
