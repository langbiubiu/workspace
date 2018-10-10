package com.ipanel.join.cqhome.view;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import ipanel.join.configuration.Bind;
import ipanel.join.configuration.View;
import ipanel.join.widget.FrameLayout;

public class TvFrameLayout extends FrameLayout {
	private static final String TAG = "TvFrameLayout";
	private String fre = null, program = null;

	public TvFrameLayout(Context context, View data) {
		super(context, data);
		Bind b=data.getBindByName("freUrl");
		String str=b.getValue().getvalue();
		if (str!=null) {
			try {
				JSONObject ob=new JSONObject(str);
				fre=ob.optString("fre");
				program=ob.optString("program");
//				fre  = "frequency://259000000?symbol_rate=6875000&delivery=cable&modulation=qam64&frequency=259000000";
//				program = "program://19?audio_stream_pid=402&audio_stream_type=audio_mpeg2&video_stream_pid=401&video_stream_type=video_mpeg2&pcr_stream_pid=401&ca_required=true";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		Log.d(TAG, "fre----"+fre);
		Log.d(TAG, "program----"+program);
	}
	
	public String getFre(){
		return fre;
	}
	public String getProgram(){
		return program;
	}

//	public boolean hasTVLib() {
//		try {
//			Class.forName("android.net.telecast.FrequencyInfo");
//		} catch (ClassNotFoundException e) {
//			return false;
//		}
//		return true;
//	}

	@Override
	protected void onAttachedToWindow() {
		Log.d(TAG, "onAttachedToWindow");
		super.onAttachedToWindow();
	}
  
	@Override
	protected void onDetachedFromWindow() {
		Log.d(TAG, "onDetachedFromWindow");
		super.onDetachedFromWindow();
	}

}
