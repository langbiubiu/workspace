package android.appwidget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;

/**
 * 电视组件提供者
 */
public class TeeveeWidgetProvider extends AppWidgetProvider {
	static final String TAG = "[java]TeeveeWidgetProvider";

	/** 视频蒙版FrameLayout的tag标识(android:tag)-无视频(仅音频) */
	public static final String MASK_LAYOUT_TAG_NO_VIDEO = "mask_tag_no_video";
	/** 视频蒙版FrameLayout的tag标识(android:tag)-出错 */
	public static final String MASK_LAYOUT_TAG_PLAY_ERROR = "mask_tag_play_error";
	/** 视频蒙版FrameLayout的tag标识(android:tag)-切换 */
	public static final String MASK_LAYOUT_TAG_PLAY_SWITCH = "mask_tag_play_switch";

	@Override
	public void onReceive(Context context, Intent intent) {
		String name = intent.getStringExtra(TeeveeWidgetHostView.EXTRA_TO_CH);
		Log.d(TAG, "onReceive name = " + name);
		if (name != null) {
			if (name.startsWith(TeeveeWidgetHostView.SCHEME_CH)) {
				String sub = name.substring(TeeveeWidgetHostView.SCHEME_CH.length());
				if (sub.equals("prev")) {
					onToPrevChannel(context);
				} else if (sub.equals("next")) {
					onToNextChannel(context);
				} else {
					try {
						onToChannel(context, Integer.parseInt(sub));
					} catch (Exception e) {
						Log.d(TAG, "to channel:" + sub + ", failed");
					}
				}
			} else {
				onToChannel(context, name);
			}
		}
		Rect r = (Rect) intent.getParcelableExtra(TeeveeWidgetHostView.EXTRA_VB);
		float v = intent.getFloatExtra(TeeveeWidgetHostView.EXTRA_VO, -1f);
		String stop = intent.getStringExtra(TeeveeWidgetHostView.EXTRA_STOP);
		if (r != null)
			onSetVideoBounds(context, r);
		if (v >= 0)
			onSetVolume(context, v);
		if(stop != null){
			stop(context);
		}
		super.onReceive(context, intent);
	}

	/**
	 * 到后一个频道
	 * 
	 * @param context
	 *            上下文
	 */
	public void onToNextChannel(Context context) {
	}

	/**
	 * 到前一个频道
	 * 
	 * @param context
	 *            上下文
	 */
	public void onToPrevChannel(Context context) {
	}

	/**
	 * 到指定的频道
	 * 
	 * @param context
	 *            上下文
	 * @param number
	 *            频道号
	 */
	public void onToChannel(Context context, int number) {
	}

	/**
	 * 到指定名称的频道
	 * 
	 * @param context
	 *            上下文
	 * @param name
	 *            名称
	 */
	public void onToChannel(Context context, String name) {
	}

	/**
	 * 当设置视频位置
	 * 
	 * @param context
	 *            上下文
	 * @param r
	 *            相对屏幕的矩形区域
	 */
	public void onSetVideoBounds(Context context, Rect r) {
	}

	/**
	 * 当调整音量
	 * 
	 * @param context
	 *            上下文
	 * @param v
	 *            0-1.0之间
	 */
	public void onSetVolume(Context context, float v) {
	}
	
	/**
	 * 停止播放
	 */
	public void stop(Context context){
		
	}
}
