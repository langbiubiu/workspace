package ipaneltv.toolkit.http;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.http.HttpObjectification.DVBMenuBouquet;
import ipaneltv.toolkit.http.HttpObjectification.DVBMenuBouquet.BBouquet;
import ipaneltv.toolkit.http.HttpObjectification.DVBMenuBouquet.BBouquet.BChannel;
import ipaneltv.toolkit.http.HttpObjectification.VersionRegister;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import android.content.Context;

public class HttpRequestToolkit {

	final String TAG = "JsonFromServer";
	SAXParserFactory factory;
	SAXParser parser;
	SaxHandler dh;
	EITSaxHandler edh;
	private Object mutex = new Object();
	private boolean prepared = false, running = false;
	Properties properties;
	public VersionRegister version;
	final int TAG_XML_FILE = 1;
	final int TAG_ZIP_FILE = 2;
	final int TAG_PRO_FILE = 3;
	final int TAG_BINARY_FILE = 4;
	final int TAG_HTML_FILE = 5;
	final int ERROR_FILE = 99;
	int tag_file;
	boolean isTableFull;
	int appointVersion;
	public boolean isTableFull() {
		return isTableFull;
	}

	public void setTableFull(boolean isTableFull) {
		this.isTableFull = isTableFull;
	}

	HttpRequestToolkit() {
		try {
			factory = SAXParserFactory.newInstance();
			parser = factory.newSAXParser();
			dh = new SaxHandler();
			properties = new Properties();
			version = new VersionRegister();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final int REQUEST_TIMEOUT = 5 * 1000;// 设置请求超时10秒钟
	private static final int SO_TIMEOUT = 10 * 1000; // 设置等待数据超时时间10秒钟

	public SaxHandler getDh() {
		return dh;
	}
	
	public EITSaxHandler getEDh() {
		return edh;
	}

	private static HttpRequestToolkit toolkit = new HttpRequestToolkit();

	public static HttpRequestToolkit getInstance() {
		return toolkit;
	}

	public boolean isGoodInternet(String basicUrl) {
		HttpURLConnection conn = null;
		boolean isConnect = false;
		synchronized (mutex) {
			try {
				URL url = new URL(basicUrl);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(REQUEST_TIMEOUT);
				conn.setReadTimeout(SO_TIMEOUT);
				conn.setRequestMethod("POST");
				int code = conn.getResponseCode();
				String type = conn.getContentType();
				IPanelLog.i(TAG, "code=" + code + ";type=" + type);
				if (code == 200) {
					isConnect = true;
					IPanelLog.i(TAG, "connection is OK ");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return isConnect;
	}
	
	public HttpURLConnection getConnection(String uri) {
		HttpURLConnection conn = null;
		boolean isConnect = false;
		synchronized (mutex) {
			try {
				URL url = new URL(uri);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(REQUEST_TIMEOUT);
				conn.setReadTimeout(SO_TIMEOUT);
				conn.setRequestMethod("POST");
				int code = conn.getResponseCode();
				String type = conn.getContentType();
				IPanelLog.i(TAG, "code=" + code + ";type=" + type);
				if (code == 200) {
					isConnect = true;
					IPanelLog.i(TAG, "connection is OK ");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (isConnect) {
				return conn;
			}
		}
		return null;
	}

	public boolean prepare(HttpURLConnection conn) {
		synchronized (mutex) {
			try {
				if (!prepared) {
					prepared = true;
					String type = getContentType(conn);
					type = ( type != null ? (type.contains(";") ? type.substring(0, type.indexOf(';')) : type) : null);
					if ("application/xml".equals(type)) {
						IPanelLog.i(TAG, "go in common parser");
						tag_file = TAG_XML_FILE;
					} else if ("application/zip".equals(type)) {
						IPanelLog.i(TAG, "go in zip parser");
						tag_file = TAG_ZIP_FILE;
					} else if ("text/plain".equals(type)) {
						tag_file = TAG_PRO_FILE;
					} else if ("application/octet-stream".equals(type)) {
						IPanelLog.i(TAG, "go in zip parser");
						tag_file = TAG_BINARY_FILE;
					} else if ("text/html".equals(type)) {
						tag_file = TAG_HTML_FILE;
					} else {
						IPanelLog.i(TAG, "lvby-->test Json contenttype = "+getContentType(conn));
						tag_file = ERROR_FILE;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return prepared;
	}

	public boolean start(InputStream in, HttpURLConnection conn) {

		synchronized (mutex) {
			if (prepared && !running) {
				try {
					running = true;
					switch (tag_file) {
					case TAG_PRO_FILE:
						IPanelLog.i(TAG, "go in pro");
						parseProperties(in);
						break;
					case TAG_XML_FILE:
						IPanelLog.i(TAG, "go in common parser");
						parser.parse(in, dh);
						break;
					case TAG_ZIP_FILE:
						IPanelLog.i(TAG, "go in zip parser");
						readZIPStream(in);
						break;
					case TAG_BINARY_FILE:
						readBinaryStream(in);
						IPanelLog.i(TAG, "go in binary parser");
						break;
					case TAG_HTML_FILE:
						readHtmlStream(in);
						IPanelLog.i(TAG, "go in binary parser html");
						break;
					default:
						IPanelLog.i(TAG, "there is not sutiable");
						readHtmlStream(in);
						break;
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return false;
		}
	}

	private void readBinaryStream(InputStream in) {
		IPanelLog.i(TAG, "lvby-->go in readBinaryStream method");
		InputStream iis = null;
		try {
			if (!isTableFull) {
				iis = new InflaterInputStream(in);
				IPanelLog.i(TAG, "available size="+iis.available());
				InputSource ins = new InputSource(iis);
				ins.setEncoding("UTF-8");			
				parser.parse(ins, dh);
			}else{
				parseProperties(in);				
			}
			IPanelLog.i(TAG, "it's OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean readBinaryStream(InputStream in, boolean isIpSearching) {
		IPanelLog.i(TAG, "lvby-->go in readBinaryStream method");
		InputStream iis = null;
		try {
			if (isIpSearching) {
				edh = new EITSaxHandler();
				iis = new InflaterInputStream(in);
				IPanelLog.i(TAG, "available size=" + iis.available());
				InputSource ins = new InputSource(iis);
				ins.setEncoding("UTF-8");
				parser.parse(ins, edh);
				IPanelLog.i(TAG, "it's OK------");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				in.close();
				iis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private void readHtmlStream(InputStream in) {
		try {
			int len = 0;
			byte[]buf = new byte[1024];
			ByteArrayOutputStream outstr = new ByteArrayOutputStream();
			while((len = in.read(buf)) != -1){
				outstr.write(buf, 0, len);
			}
			IPanelLog.i(TAG, "bytearray size="+outstr.toByteArray().length);
			IPanelLog.i(TAG, "lvby-->readHtmlStream is over");
			IPanelLog.i(TAG, "congteng-->"+new String (outstr.toByteArray(),"gb2312"));
		    String version = new String (outstr.toByteArray());
			appointVersion = Integer.parseInt("".equals(version) ? 0+"" : version);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getContentType(HttpURLConnection conn) {
		return conn.getContentType();
	}

	public boolean startSearch(String uri) {
		IPanelLog.i(TAG, "startSearch uri=" + uri);
		HttpURLConnection conn = null;
		InputStream in = null;
		synchronized (mutex) {
			if ((conn = getConnection(uri)) != null) {
				try {
					in = conn.getInputStream();
					prepare(conn);
					IPanelLog.i(TAG, "papared=" + prepared);
					return start(in, conn);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						prepared = false;
						running = false;
						in.close();
						conn.disconnect();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				IPanelLog.i(TAG, "conn is null");
			}
			return false;
		}
	}
	
	public boolean startSearch(String uri, boolean isSearching) {
		IPanelLog.i(TAG, "startSearch uri=" + uri);
		HttpURLConnection conn = null;
		InputStream in = null;
		synchronized (mutex) {
			if ((conn = getConnection(uri)) != null) {
				try {
					in = conn.getInputStream();
					return readBinaryStream(in, isSearching);
				} catch (IOException e) {
					e.printStackTrace();
				} 
			} else {
				IPanelLog.i(TAG, "conn is null");
			}
			return false;
		}
	}
	
	public void closeParser(){
		IPanelLog.i(TAG, "lvby-->close parser");
		dh = null;
		dh = new SaxHandler();
		IPanelLog.i(TAG ,"closeParser-->"+dh.ipNetwork);
	}

	private void readZIPStream(InputStream in) {
		ZipInputStream zin = null;
		try {
			zin = new ZipInputStream(in);
			ZipEntry entry = null;
			while ((entry = zin.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					zin.closeEntry();
					continue;
				}
				parser.parse(zin, dh);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (zin != null) {
					zin.closeEntry();
					zin.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void parseProperties(InputStream in) {
		try {
			properties.load(in);
			version.network = HttpObjectification.parseString2Int(properties
					.getProperty("network"));
			version.eit_today = HttpObjectification.parseString2Int(properties
					.getProperty("eit-today"));
			version.bouquet = HttpObjectification.parseString2Int(properties
					.getProperty("bouquet"));
			version.eit_tsid_sid = HttpObjectification
					.parseString2Int(properties.getProperty("eit-10-101"));
			// version.addVersions(tsid, sid, version)
			IPanelLog.i(TAG, "version1=" + properties.get("eit-10-101"));
			IPanelLog.i(TAG, "version2=" + properties.get("eit-today"));
			IPanelLog.i(TAG, "version3=" + properties.get("bouquet"));
			IPanelLog.i(TAG, "version4=" + properties.get("network"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			isTableFull = false;
		}
	}
	public DVBMenuBouquet parserDvbMenuJsons(String uri, Context context) {
		DVBMenuBouquet bouquet = null;
		synchronized (mutex) {
			if(bouquet == null)
				bouquet = new DVBMenuBouquet();
			try {
				String json = getDvbMenuJsons(uri, context);
				if(json != null && !"".equals(json)){
					JSONObject root = new JSONObject(json);
					JSONObject bodyObject = root.getJSONObject("body");
					if(bodyObject != null){
						JSONArray folders = bodyObject.getJSONArray("folders");
						if(folders == null)
							return null;
						int folders_len = folders.length();
						IPanelLog.i(TAG, "folder length=" + folders_len);
						if (folders_len < 0) {
							return null;
						}
						for (int i = 0; i < folders_len; i++) {
							BBouquet bbouquet = bouquet.addBouquet();
							
							JSONObject folder = (JSONObject) folders.opt(i);
							String folderId = folder.optString("folderId");
							String liveCircleTag = folder.optString("liveCircleTag");
							String folderName = folder.optString("folderName");
							int level = folder.optInt("level");
							bbouquet.b_id = SaxHandlerUtil.parseString2Int(liveCircleTag);
							bbouquet.b_name = folderName;
							bbouquet.level = level;
							JSONArray contents = folder.optJSONArray("contents");
							if(contents == null){
								return null;
							}
							int contents_len = contents.length();
							IPanelLog.i(TAG, "value:folderId=" + folderId + ";folderName="
									+ folderName + ";level=" + level+";liveCircleTag="+liveCircleTag);
							IPanelLog.i(TAG, "contents length = " + contents_len);
							
							for (int j = 0; j < contents_len; j++) {
								BChannel bchannel = bbouquet.addChannel();
								
								JSONObject content = (JSONObject) contents.opt(j);
								String contentName = content.optString("conentName");
								String contentId = content.optString("contentId");
								String serviceId = content.optString("serviceId");
								String tsId = content.optString("tsId");
								IPanelLog.i(TAG, "content value conentName=" + contentName
										+ ";contentId=" + contentId + ";serviceId="
										+ serviceId + ";tsId=" + tsId);
								bchannel.contentId = contentId;
								bchannel.serviceId = SaxHandlerUtil.parseString2Int(serviceId);
								bchannel.contentName = contentName;
								bchannel.tsId = SaxHandlerUtil.parseString2Int(tsId);
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return bouquet;
		}
	}
	
	public String getDvbMenuJsons(String uri, Context context){
		try {
			HttpPost httpPost = new HttpPost(uri);
			String xml = getDvbMenuRequestJSON();
			IPanelLog.i(TAG, "send xml:" + xml);
			String contentType = "text/xml; charset=UTF-8";
			final HttpEntity entity = new StringEntity(xml, "GBK");
			Header[] headers = getDvbMenuJsonHeader(context);
			HttpEntityEnclosingRequestBase request = addEntityToRequestBase(
					httpPost, entity);
			if (headers != null) {
				request.setHeaders(headers);
			}
			if (contentType != null) {
				request.addHeader("Content-Type", contentType);
			}
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(request);
			int code = httpResponse.getStatusLine().getStatusCode();
			IPanelLog.i(TAG, "lvby-->code = " + code);
			if (200 == code) {
				String value = EntityUtils.toString(httpResponse
						.getEntity());
				IPanelLog.i(TAG, "value:" + value);
				return value;
			}
			return "";
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getDvbMenuRequestJSON(){
		StringBuffer sb = new StringBuffer();
		sb.append("{'command': 'WASU_DVBMENU_QUERY','version': {'version': '1.0','componentId':'vodApk'}, 'body':{'siteCode': 'alisite', 'folderCode':'1012475'}}");
		return sb.toString();
	}
	
	public Header[] getDvbMenuJsonHeader(Context context) {
		
		String userId = "97034970001";
		String userProfile = "";
		String terminalId = "74833terminal";
		String region="HZ";
		/*Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://com.join.wasu.authenticator.AuthenticationInfoProvider"),
				null, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {
			userId = cursor.getString(cursor.getColumnIndex("UserId"));
			userProfile = cursor.getString(cursor.getColumnIndex("UserProfile"));
			terminalId = cursor.getString(cursor.getColumnIndex("TerminalId"));
			region = cursor.getString(cursor.getColumnIndex("Region"));
			cursor.close();
		}*/
		Header[] headers = new Header[] {
				new BasicHeader("X-device-type", "4"),
				new BasicHeader("X-device-id", "00034c840001"),
				new BasicHeader("X-USER-ID", userId),
				new BasicHeader("X-REGION", region),
				new BasicHeader("X-TERMINAL-ID", terminalId),
				new BasicHeader("X-USERPROFILE", userProfile),
				new BasicHeader("Connection", "close")};
		for (Header h : headers) {
			IPanelLog.i(TAG, h.getName() + " : " + h.getValue());
		}
		
		return headers;
	}
	
	private HttpEntityEnclosingRequestBase addEntityToRequestBase(
			HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
		if (entity != null) {
			requestBase.setEntity(entity);
		}
		return requestBase;
	}

	public int getAppointVersion() {
		return appointVersion;
	}
}
