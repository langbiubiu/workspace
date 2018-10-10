package ipaneltv.toolkit.wardship2;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import android.os.FileObserver;
import android.util.Log;

/**
 * 将已加锁的信息(目前使用program_number)保存在文件里，目前存放形式为program_number-program_number...
 * 非data目录下，主要用于恢复出厂设置时备份
 */
public class WardshipTool {

	private static final String TAG = WardshipTool.class.getSimpleName();
	public static String wardshipFilePath;
	public static String hideFilePath;
	public static String passwordFile;
	public static String numberedFile;
	public static String dir;
	static String root = "/otapackage";
	public static String PASSWORD = "password";
	public static String PWDSTATE = "pwdstate";
	public static final String INIT_PWD = "0000";
	//处理文件的访问权限问题
	public Runtime r = null;
	public static boolean is_wardship_exist = false;
	public static boolean is_password_exist = false;
	public static boolean is_hide_exist = false;
	public static boolean is_numberd_exist = false;
	MD5 md5;

	public static synchronized WardshipTool createWardshipTool() {
		return new WardshipTool(root);
	}

	public static synchronized WardshipTool createWardshipTool(String rootPath) {
		return new WardshipTool(rootPath);
	}

	protected WardshipTool(String rootPath) {
		if(rootPath == null){
			rootPath = root;
		}
		File roo = new File(rootPath);
		Log.i(TAG, "root canWrite = " + roo.canWrite());
		dir = rootPath + File.separator + "ipanelwardship";
		File file = new File(dir);
		if (!file.exists()) {
			boolean b = file.mkdirs();
			file.setReadable(true);
			file.setWritable(true);
			Log.i(TAG, "create dir " + b);
		}
		/**
		 * @explain
		 * 		给ipanelwardship加上对所有应用的可读可写权限
		 */
		r = Runtime.getRuntime( );
		try {
			Process proc = r.exec( "chmod -R 777 " + dir );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		wardshipFilePath = dir + File.separator + "warship.txt";
		hideFilePath = dir + File.separator + "hide.txt";
		passwordFile = dir + File.separator + "password.txt";
		numberedFile = dir + File.separator + "numbered.txt";
		Log.i(TAG, "wardshipFilePath =  " + wardshipFilePath + "; passwordFile = " + passwordFile);
		md5 = new MD5();
	}

	public void saveWarship(Set<ChannelKey> set) {
		OutputStreamWriter output = null;
		//更新枷锁数据
		if (set != null) {
			try {
				output = new OutputStreamWriter(new FileOutputStream(wardshipFilePath));
				for (ChannelKey ChannelKey : set) {
					output.write(ChannelKey.toString());
					output.write("\r\n");
				}
				//保证只跑一次
				if( !is_wardship_exist ){
					is_wardship_exist = true;
					try {
						Process proc = r.exec( "chmod -R 777 " + wardshipFilePath );
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}
				
				output.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (output != null) {
					try {
						output.close();
						output = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}else {
			//清空枷锁数据
			try {
				output = new OutputStreamWriter(new FileOutputStream(wardshipFilePath));
				output.write("");
				//保证只跑一次
				if( !is_wardship_exist ){
					is_wardship_exist = true;
					try {
						Process proc = r.exec( "chmod -R 777 " + wardshipFilePath );
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}
				
				output.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (output != null) {
					try {
						output.close();
						output = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void saveHideState(Set<ChannelKey> set) {
		OutputStreamWriter output = null;
		if (set != null) {
			try {
				output = new OutputStreamWriter(new FileOutputStream(hideFilePath));
				for (ChannelKey ChannelKey : set) {
					output.write(ChannelKey.toString());
					output.write("\r\n");
				}
				//保证只跑一次
				if( !is_hide_exist ){
					is_hide_exist = true;
					try {
						Process proc = r.exec( "chmod -R 777 " + hideFilePath );
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}
				
				output.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (output != null) {
					try {
						output.close();
						output = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void saveNumber(HashMap<ChannelKey, Integer> map) {
		OutputStreamWriter output = null;
		if (map != null) {
			try {
				output = new OutputStreamWriter(new FileOutputStream(numberedFile));
				Iterator<Entry<ChannelKey, Integer>> iter = map.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<ChannelKey, Integer> entry = iter.next();
					ChannelKey key = entry.getKey();
					Integer val = entry.getValue();
					output.write(key.toString() + ":" + val);
					output.write("\r\n");
				}
				//保证只跑一次
				if( !is_numberd_exist ){
					is_numberd_exist = true;
					try {
						Process proc = r.exec( "chmod -R 777 " + numberedFile );
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}
				
				output.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (output != null) {
					try {
						output.close();
						output = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public Set<ChannelKey> getWardshipList() {
		Set<ChannelKey> set = new HashSet<ChannelKey>();
		File f = new File(wardshipFilePath);
		if (f.exists()) {
			InputStreamReader read = null;
			BufferedReader bufferedReader = null;
			try {
				read = new InputStreamReader(new FileInputStream(wardshipFilePath));
				bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					set.add(ChannelKey.fromString(lineTxt));
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bufferedReader != null)
						bufferedReader.close();
					if (read != null)
						read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return set;
	}

	public HashMap<ChannelKey, Integer> getNumberedMap() {
		HashMap<ChannelKey, Integer> map = new HashMap<ChannelKey, Integer>();
		File f = new File(numberedFile);
		Log.d(TAG, "getNumberedMap f.exists() = "+ f.exists()+";numberedFile = "+ numberedFile);
		if (f.exists()) {
			InputStreamReader read = null;
			BufferedReader bufferedReader = null;
			try {
				read = new InputStreamReader(new FileInputStream(numberedFile));
				bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					Log.d(TAG, "getNumberedMap lineTxt = " + lineTxt);
					String[] s = lineTxt.split(":");
					if (s == null || s.length < 2) {
						continue;
					}
					Log.d(TAG, "s[0] = " + s[0] + ";s[1] = " + s[1]);
					map.put(ChannelKey.fromString(s[0]), Integer.valueOf(s[1]));
				}
				Log.d(TAG, "getNumberedMap 2 lineTxt = " + lineTxt);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bufferedReader != null)
						bufferedReader.close();
					if (read != null)
						read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return map;
	}

	public Set<ChannelKey> getHideList() {
		Set<ChannelKey> set = new HashSet<ChannelKey>();
		File f = new File(hideFilePath);
		Log.d(TAG, "getHideList f.exists() = "+ f.exists()+";hideFilePath = "+ hideFilePath);
		if (f.exists()) {
			InputStreamReader read = null;
			BufferedReader bufferedReader = null;
			try {
				read = new InputStreamReader(new FileInputStream(hideFilePath));
				bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					Log.d(TAG, "getHideList lineTxt = " + lineTxt);
					set.add(ChannelKey.fromString(lineTxt));
				}
				Log.d(TAG, "getHideList 2 lineTxt = " + lineTxt);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bufferedReader != null)
						bufferedReader.close();
					if (read != null)
						read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return set;
	}

	public synchronized void setPwdEnable(boolean isanable) {
		InputStream fis = null;
		OutputStream fos = null;
		try {
			File f = new File(passwordFile);
			if (!f.exists()) {
				boolean b = false;
				b = f.createNewFile();
				Log.d(TAG, "b = " + b);
			}
			fis = new FileInputStream(passwordFile);
			Properties properties = new Properties();
			properties.load(fis);
			fos = new FileOutputStream(passwordFile);
			String state = properties.getProperty(PWDSTATE, null);
			String password = properties.getProperty(PASSWORD);
			Log.d(TAG, "setPwdEnable 2 state = "+ state +";isanable = "+ isanable+";password = "+ password);
			properties.setProperty(PWDSTATE, isanable + "");
			if(password != null){
				properties.setProperty(PASSWORD, password);	
			}
			properties.store(fos, null);
			//保证只跑一次
			if( !is_password_exist ){
				is_password_exist = true;
				try {
					Process proc = r.exec( "chmod -R 777 " + passwordFile );
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
					fis = null;
				}
				if (fos != null) {
					fos.flush();
					fos.close();
					fos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized boolean setPassword(String s) {
		InputStream fis = null;
		OutputStream fos = null;
		try {
			if (s == null) {
				return false;
			}
			File f = new File(passwordFile);
			if (!f.exists()) {
				boolean b = false;
				b = f.createNewFile();
				Log.d(TAG, "b = " + b);
			}
			Log.d(TAG, "setPassword 2 s = "+ s);
			s = md5.getMD5ofStr(s);
			fis = new FileInputStream(passwordFile);
			Properties properties = new Properties();
			properties.load(fis);
			fos = new FileOutputStream(passwordFile);
			String state = properties.getProperty(PWDSTATE, null);
			String password = properties.getProperty(PASSWORD);
			Log.d(TAG, "setPassword 2 state = "+ state +";password = "+ password);
			properties.setProperty(PASSWORD, s);
			if(state != null){
				properties.setProperty(PWDSTATE, state);	
			}
			properties.store(fos, null);
			//保证只跑一次
			if( !is_password_exist ){
				is_password_exist = true;
				try {
					Process proc = r.exec( "chmod -R 777 " + passwordFile );
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
					fis = null;
				}
				if (fos != null) {
					fos.flush();
					fos.close();
					fos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	public String getPwd() {
		InputStream fis = null;
		String pwd = null;
		try {
			File f = new File(passwordFile);
			if (f.exists()) {
				Properties properties = new Properties();
				fis = new FileInputStream(passwordFile);
				properties.load(fis);
				pwd = properties.getProperty(PASSWORD, null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (pwd == null) {
			pwd = md5.getMD5ofStr(INIT_PWD);
		}
		return pwd;
	}

	public boolean isPwdEnable() {
		InputStream fis = null;
		try {
			File f = new File(passwordFile);
			if (f.exists()) {
				Properties properties = new Properties();
				fis = new FileInputStream(passwordFile);
				properties.load(fis);
				String str = properties.getProperty(PWDSTATE);
				if (str != null && str.equals("true")) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public Password getPassword() {
		InputStream fis = null;
		Password password = new Password();
		try {
			File f = new File(passwordFile);
			if (f.exists()) {
				Properties properties = new Properties();
				fis = new FileInputStream(passwordFile);
				properties.load(fis);
				password.setPassword(properties.getProperty(PASSWORD, null));
				String state = properties.getProperty(PWDSTATE, null);
				Log.d(TAG, "getPassword state = "+ state +";password = "+ password.getPassword());
				if (state != null && state.equals("true")) {
					password.setPwdState(true);
				} else {
					password.setPwdState(false);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return password;
	}

	public boolean checkPwd(String str) {
		if (str == null) {
			return false;
		}
		str = md5.getMD5ofStr(str);
		if (str.equals(getPwd())) {
			return true;
		}
		return false;
	}

	public boolean checkPwd(String pwd, String oldPwd) {
		if (pwd == null) {
			return false;
		}
		if (oldPwd == null) {
			oldPwd = md5.getMD5ofStr(INIT_PWD);
		}
		pwd = md5.getMD5ofStr(pwd);
		if (pwd.equals(oldPwd)) {
			return true;
		}
		return false;
	}

	MyFileObserver mObserver = null;
	WarshipListener wsl;

	public void startWatching() {
		mObserver = new MyFileObserver(dir);
		mObserver.startWatching();
	}

	public void stopWatching() {
		if (mObserver != null) {
			mObserver.stopWatching();
		}
	}

	public void setWardshipListener(WarshipListener wsl) {
		this.wsl = wsl;
		Log.d(TAG, "setWardshipListener wsl = "+ wsl +";this = "+ this);
	}

	public static interface WarshipListener {

		public void onWarshipChanged();

	}

	public class MyFileObserver extends FileObserver {

		/**
		 * path 是所监听的文件夹或者文件名。
		 */

		public MyFileObserver(String path) {
			super(path);

		}

		@Override
		public void onEvent(int event, String path) {
			Log.d(TAG, "onEvent event = " + event + ";path = " + path);
			switch (event) {
			case android.os.FileObserver.CLOSE_WRITE:
				WarshipListener listener = wsl;
				Log.d(TAG, "onEvent listener = "+ listener);
				if (listener != null) {
					listener.onWarshipChanged();
				}
				break;
			}

		}

	}

	public static class Password {
		private boolean pwdState;
		private String password;

		public boolean isPwdEnable() {
			return pwdState;
		}

		public void setPwdState(boolean pwdState) {
			this.pwdState = pwdState;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
}
