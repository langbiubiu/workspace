package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.Utils;
import ipanel.join.configuration.View;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

public class ColumeLayout extends FrameLayout implements IConfigViewGroup {
	public static final String PROP_FOCUS_SCALE = "focusScale";
	public static final String WORD_TAG = "word";

	public String mFocusScaleID="none";
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@SuppressLint("NewApi")
	public ColumeLayout(Context context, View data) {
		super(context, data);
		Bind bind = data.getBindByName(PROP_FOCUS_SCALE);
		if(bind != null){
			mFocusScaleID=bind.getValue().getvalue();
		}
		setOnFocusChangeListener(new OnFocusChangeListener() {
			@SuppressLint("NewApi")
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
			@Override
			public void onFocusChange(android.view.View v, boolean hasFocus) {
				android.view.View word = findViewWithTag(WORD_TAG);
				android.view.View scale=null;
				if(!"none".equals(mFocusScaleID)){
					scale=Utils.findViewByConfigId(getRootView(), mFocusScaleID);
				}
				if(scale!=null){
					if(hasFocus){
						scale.animate().scaleX(1.1f);
						scale.animate().scaleY(1.1f);
					}else{
						scale.animate().scaleX(1.0f);
						scale.animate().scaleY(1.0f);
					}
				}
				if (word != null) {
					if (hasFocus) {
						word.animate().alpha(1.0f);
					} else {
						word.animate().alpha(0.0f);
					}
				}
			}
		});
	}


}
