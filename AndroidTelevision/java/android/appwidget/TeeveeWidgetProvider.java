package android.appwidget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;

/**
 * ��������ṩ��
 */
public class TeeveeWidgetProvider extends AppWidgetProvider {
	static final String TAG = "[java]TeeveeWidgetProvider";

	/** ��Ƶ�ɰ�FrameLayout��tag��ʶ(android:tag)-����Ƶ(����Ƶ) */
	public static final String MASK_LAYOUT_TAG_NO_VIDEO = "mask_tag_no_video";
	/** ��Ƶ�ɰ�FrameLayout��tag��ʶ(android:tag)-���� */
	public static final String MASK_LAYOUT_TAG_PLAY_ERROR = "mask_tag_play_error";
	/** ��Ƶ�ɰ�FrameLayout��tag��ʶ(android:tag)-�л� */
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
	 * ����һ��Ƶ��
	 * 
	 * @param context
	 *            ������
	 */
	public void onToNextChannel(Context context) {
	}

	/**
	 * ��ǰһ��Ƶ��
	 * 
	 * @param context
	 *            ������
	 */
	public void onToPrevChannel(Context context) {
	}

	/**
	 * ��ָ����Ƶ��
	 * 
	 * @param context
	 *            ������
	 * @param number
	 *            Ƶ����
	 */
	public void onToChannel(Context context, int number) {
	}

	/**
	 * ��ָ�����Ƶ�Ƶ��
	 * 
	 * @param context
	 *            ������
	 * @param name
	 *            ����
	 */
	public void onToChannel(Context context, String name) {
	}

	/**
	 * ��������Ƶλ��
	 * 
	 * @param context
	 *            ������
	 * @param r
	 *            �����Ļ�ľ�������
	 */
	public void onSetVideoBounds(Context context, Rect r) {
	}

	/**
	 * ����������
	 * 
	 * @param context
	 *            ������
	 * @param v
	 *            0-1.0֮��
	 */
	public void onSetVolume(Context context, float v) {
	}
	
	/**
	 * ֹͣ����
	 */
	public void stop(Context context){
		
	}
}
