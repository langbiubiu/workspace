package com.ipanel.join.cq.vod.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;

public class Tools {
	/**
	 * ��ȡ�����ļ���JSON�ַ���
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getJson(Context context, String fileName) {

		StringBuilder stringBuilder = new StringBuilder();
		String myString = null;
		try {
			/*
			 * BufferedReader bf = new BufferedReader(new
			 * InputStreamReader(context.getAssets().open(fileName))); String
			 * line; while ((line = bf.readLine()) != null) {
			 * stringBuilder.append(line); }
			 */
			InputStream is = context.getAssets().open(fileName);
			// BufferedInputStream bis = new BufferedInputStream(is);
			// ��ByteArrayBuffer����
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = is.read()) != -1) {
				baf.append((byte) current);
			}
			// �����������ת��ΪString,��UTF-8����
			myString = EncodingUtils.getString(baf.toByteArray(), "GBK");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// return stringBuilder.toString();
		return myString;
	}

	/**
	 * ��ȡĳһ����Ŀ�ܵ�ҳ��
	 * 
	 * @param totalNumber
	 * @return
	 */
//	public static int getHistoryPages(int totalNumber) {
//		int count = 0;
//		if (totalNumber % StaticData.HISTORYPERPAGENUMBER == 0) {
//			count = totalNumber / StaticData.HISTORYPERPAGENUMBER;
//		} else {
//			count = totalNumber / StaticData.HISTORYPERPAGENUMBER + 1;
//		}
//		return count;
//	}

	/**
	 * �ַ��� �û�����
	 */

	public static String replace(String strSource, String strFrom, String strTo) {
		if (strSource == null) {
			return null;
		}
		int i = 0;
		if ((i = strSource.indexOf(strFrom, i)) >= 0) {
			char[] cSrc = strSource.toCharArray();
			char[] cTo = strTo.toCharArray();
			int len = strFrom.length();
			StringBuffer buf = new StringBuffer(cSrc.length);
			buf.append(cSrc, 0, i).append(cTo);
			i += len;
			int j = i;
			while ((i = strSource.indexOf(strFrom, i)) > 0) {
				buf.append(cSrc, j, i - j).append(cTo);
				i += len;
				j = i;
			}
			buf.append(cSrc, j, cSrc.length - j);
			return buf.toString();
		}
		return strSource;
	}

	/**
	 * gbkתΪutf-8����
	 * 
	 * @param gbkStr
	 * @return
	 */
	public static String getUTF8StringFromGBKString(String gbkStr) {
		try {
			return new String(getUTF8BytesFromGBKString(gbkStr), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError();
		}
	}

	public static byte[] getUTF8BytesFromGBKString(String gbkStr) {
		int n = gbkStr.length();
		byte[] utfBytes = new byte[3 * n];
		int k = 0;
		for (int i = 0; i < n; i++) {
			int m = gbkStr.charAt(i);
			if (m < 128 && m >= 0) {
				utfBytes[k++] = (byte) m;
				continue;
			}
			utfBytes[k++] = (byte) (0xe0 | (m >> 12));
			utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));
			utfBytes[k++] = (byte) (0x80 | (m & 0x3f));
		}
		if (k < utfBytes.length) {
			byte[] tmp = new byte[k];
			System.arraycopy(utfBytes, 0, tmp, 0, k);
			return tmp;
		}
		return utfBytes;
	}

	/**
	 * �����ĵ�Ӱͼ��
	 * 
	 * @param src
	 * @return
	 */
	public static Bitmap createReflectionBitmapForSingle(Bitmap src) {
		final int w = src.getWidth();
		final int h = src.getHeight();
		// ���Ƹ�����32λͼ
		Bitmap bitmap = Bitmap.createBitmap(w, h / 2, Config.ARGB_8888);
		// ������X��ĵ�Ӱͼ��
		Matrix m = new Matrix();
		m.setScale(1, -1);
		Bitmap t_bitmap = Bitmap.createBitmap(src, 0, h / 2, w, h / 2, m, true);

		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		// ���Ƶ�Ӱͼ��
		canvas.drawBitmap(t_bitmap, 0, 0, paint);
		// ������Ⱦ-��Y��ߵ�����Ⱦ
		Shader shader = new LinearGradient(0, 0, 0, h / 2, 0x70ffffff, 0x00ffffff, Shader.TileMode.MIRROR);
		paint.setShader(shader);
		// ȡ������ƽ�������ʾ�²㡣
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// ������Ⱦ��Ӱ�ľ���
		canvas.drawRect(0, 0, w, h / 2, paint);
		return bitmap;
	}

	public static void showToastMessage(Context context, String msg) {
		View v = View.inflate(context, R.layout.vod_toast_view, null);
		TextView mShowInfo = (TextView) v.findViewById(R.id.showinfo);
		if (mShowInfo != null) {
			mShowInfo.setText(msg);
		}
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.setDuration(1000);
		toast.setView(v);
		toast.show();
	}

	public static String loadAssetText(Context ctx, String fileName) {
		try {
			InputStream is = ctx.getAssets().open(fileName);
			String content = new String(IS2ByteArray(is));
			is.close();
			return content;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] IS2ByteArray(InputStream is) throws IOException {
		byte[] buf = new byte[8192];
		int len;
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		while ((len = is.read(buf)) != -1) {
			bao.write(buf, 0, len);
		}
		byte[] result = bao.toByteArray();
		bao.close();
		return result;
	}

	public static InputStream loadAssetStream(Context ctx, String fileName) {
		try {
			InputStream is = ctx.getAssets().open(fileName);
			return is;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ��InputStreamת����ĳ���ַ������String
	 * 
	 * @param in
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static String InputStreamTOString(InputStream in) throws Exception {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[4096];
		int count = -1;
		while ((count = in.read(data, 0, 4096)) != -1)
			outStream.write(data, 0, count);

		data = null;
		return new String(outStream.toByteArray(), "UTF-8");
	}
	
	public static String picFile2String(String filePath) throws Exception{
		File file = new File(filePath);
		byte[] fileData = new byte[4096];
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		FileInputStream in;
		in = new FileInputStream(file);
		int count = -1;
		while ((count = in.read(fileData, 0, 4096)) != -1)
			outStream.write(fileData, 0, count);
		fileData = null;
		in.close();
		return new String(outStream.toByteArray(), "UTF-8");
		
	}
	
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}
	
	/**ת��Ƶ������*/
	public static String transformChannelName(String channelName){
		try {
			return URLEncoder.encode(channelName, "GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**��ת��Ƶ������ƴ�ӳ�ͼƬ��ȡ��ַ*/
	public static String getChannelUrl(String raw){
		String channelName=transformChannelName(raw);
		LogHelper.d( "lixby ��channel taibiao url : "+"asset:///channel/"+channelName+".png");
		return "asset:///channel/"+channelName+".png";
	}
	/**
	 * ��˹ģ��
	 * @param context
	 * @param bitmap
	 * @return
	 */
	public Bitmap blurBitmap(Context context,Bitmap bitmap) {

		// Let's create an empty bitmap with the same size of the bitmap we want
		// to blur
		Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);

		// Instantiate a new Renderscript
		RenderScript rs = RenderScript.create(context);

		// Create an Intrinsic Blur Script using the Renderscript
		ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs,
				Element.U8_4(rs));

		// Create the Allocations (in/out) with the Renderscript and the in/out
		// bitmaps
		Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
		Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

		// Set the radius of the blur
		blurScript.setRadius(25.f);

		// Perform the Renderscript
		blurScript.setInput(allIn);
		blurScript.forEach(allOut);

		// Copy the final bitmap created by the out Allocation to the outBitmap
		allOut.copyTo(outBitmap);

		// recycle the original bitmap
		bitmap.recycle();

		// After finishing everything, we destroy the Renderscript.
		rs.destroy();

		return outBitmap;
	}
}
