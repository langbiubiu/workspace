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
					Log.d(TAG, "��������");
					msg = downFile(fileName, url, 0);
				} else {
					Log.d(TAG, "�ϵ���������");
					msg = downFile(fileName, url, curfilelength);
				}
			} else {
				new File(fileName).getParentFile().mkdirs();// ����Ŀ¼
				try {
					Log.d(TAG, "��һ������");
					updatefile.createNewFile();// �����ļ�
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
					 * ��ͣ����
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
	 * �ϵ�����
	 * 
	 * @param strPath
	 * @param filename
	 * @param size
	 * @return
	 */
	public static int doDownloadTheFile(String strPath, String filename,
			long size) {
		// ����·��
		String url = strPath;
		HttpResponse response = null;
		// ������ȡ�����ļ��Ĵ�С
		HttpResponse response_test = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpClient client_test = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			HttpGet request_test = new HttpGet(url);
			response_test = client_test.execute(request_test);
			long fileSize = response_test.getEntity().getContentLength();
			Log.v(TAG, "��ȡ��Ҫ�����ļ��Ĵ�СfileSize=" + fileSize);
			Log.v(TAG, "��֤�����ļ���������size=" + size);
			if (fileSize != 0 && fileSize == size) {
				return DownLoadCallBack.DOWNLOAD_SUCCESS;
			}
			Log.v(TAG, "�������ص�����λ��XX�ֽڵ�XX�ֽ�bytes=" + size + "-" + fileSize);
			Header header_size = new BasicHeader("Range", "bytes=" + size + "-"
					+ fileSize);
			request.addHeader(header_size);
			response = client.execute(request);
			InputStream is = response.getEntity().getContent();
			if (is == null) {
				throw new RuntimeException("stream is null");
			}
			Log.v(TAG, "��ȡ�ļ����󣬿�ʼ���ļ�����д����.filename=" + filename);
			File myTempFile = new File(filename);
			RandomAccessFile fos = new RandomAccessFile(myTempFile, "rw");
			Log.v(TAG, "���ļ���size�Ժ��λ�ÿ�ʼд�룬��ʵҲ���ã�ֱ������д�Ϳ��ԡ���ʱ����߳�������Ҫ��");
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
