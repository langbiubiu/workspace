package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.Value;
import ipanel.join.configuration.View;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import cn.ipanel.android.net.imgcache.ImageFetchTask.TaskType;

@SuppressLint("DefaultLocale")
public class ImgView extends ImageView implements IConfigView, IConfigRebind {
	
	public static final String PROP_CORNERS = "corners";
	public static final String PROP_IMAGECORNERS = "drawableCorners";
	public static final String PROP_BACKGROUNDDRAWABLE = "backgroundDrawable";
	public static final String PROP_DRAWABLE = "drawable";
	public static final String PROP_SCALE_TYPE = "scaleType";
	public static final String PROP_DRAWABLE9 = "drawable9";
	public static final String PROP_LEVEL_LIST_DRAWABLe = "levelListDrawable";
	public static int corners = 10;
	private View mData;

	public ImgView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ImgView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImgView(Context context) {
		super(context);
	}

	public ImgView(Context ctx, View data) {
		super(ctx);
		this.mData = data;

		bindProperty(ctx, data);
	}

	private void bindProperty(Context ctx, View data) {
		PropertyUtils.setCommonProperties(this, data);

		Bind bd = data.getBindByName(PROP_SCALE_TYPE);
		if (bd != null) {
			setScaleType(ScaleType.valueOf(bd.getValue().getvalue().toUpperCase()));
		}

		bd = data.getBindByName(PROP_DRAWABLE);
		if (bd != null) {
			Value v = bd.getValue();
			PropertyUtils.loadDrawable(this, v, TaskType.IMAGE, PropertyUtils.getMaxBitmapSize(data));
		}

		bd = data.getBindByName(PROP_DRAWABLE9);
		if (bd != null) {
			Value v = bd.getValue();
			PropertyUtils.loadDrawable9(this, v, TaskType.IMAGE);
		}

		bd = data.getBindByName(PROP_LEVEL_LIST_DRAWABLe);
		if (bd != null) {
			PropertyUtils.loadLevelListDrawable(this, bd.getValue(), TaskType.IMAGE);
		}
				
		bd = data.getBindByName(PROP_BACKGROUNDDRAWABLE);
		if (bd != null) {
			Value v = bd.getValue();
			PropertyUtils.loadDrawable(this, v, TaskType.BACKGROUND, PropertyUtils.getMaxBitmapSize(data));			
		}	
		
		//圆角的弧度
		bd = data.getBindByName(PROP_CORNERS);
		if (bd != null) {
			corners = Integer.parseInt(bd.getValue().getvalue());
		}
				
		//需要改成圆角的图片
		bd = data.getBindByName(PROP_IMAGECORNERS);
		if (bd != null) {
			Value v = bd.getValue();			
			PropertyUtils.loadDrawable(this, v, TaskType.IMAGECORNER, PropertyUtils.getMaxBitmapSize(data),corners);
		}
	}

	@Override
	public View getViewData() {
		return mData;
	}

	@Override
	public void onAction(String type) {
		ActionUtils.handleAction(this, mData, type);

	}

	private boolean mShowFocusFrame = false;

	@Override
	public boolean showFocusFrame() {
		return mShowFocusFrame;
	}

	@Override
	public void setShowFocusFrame(boolean show) {
		mShowFocusFrame = show;
	}

	@Override
	public void rebind(List<Bind> extBinds) {
		int count = mData.applyExtBinds(extBinds);
		if (count > 0)
			bindProperty(getContext(), mData);
	}
}
