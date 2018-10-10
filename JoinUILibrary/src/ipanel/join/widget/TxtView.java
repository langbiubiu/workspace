package ipanel.join.widget;

import java.util.List;

import org.json.JSONException;

import cn.ipanel.android.net.imgcache.ImageFetchTask.TaskType;
import ipanel.join.configuration.Bind;
import ipanel.join.configuration.Value;
import ipanel.join.configuration.View;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.util.TypedValue;
import android.widget.TextView;

public class TxtView extends TextView implements IConfigView, IConfigRebind{

	public static final String PROP_DRAWABLE_BOTTOM = "drawableBottom";
	public static final String PROP_DRAWABLE_TOP = "drawableTop";
	public static final String PROP_DRAWABLE_RIGHT = "drawableRight";
	public static final String PROP_DRAWABLE_LEFT = "drawableLeft";
	public static final String PROP_DRAWABLE_PADDING = "drawablePadding";
	public static final String PROP_GRAVITY = "gravity";
	public static final String PROP_TEXT_COLOR = "textColor";
	public static final String PROP_TEXT_SIZE = "textSize";
	public static final String PROP_TEXT_STYLE = "textStyle";
	public static final String PROP_TEXT = "text";
	public static final String PROP_SIGNLE_LINE = "singleLine";
	public static final String PROP_SELECTED = "selected";
	public static final String PROP_LINES = "lines";
	
	public static final String TYPEFACE_BOLD = "bold";
	public static final String TYPEFACE_ITALIC = "italic";
	
	private View mData;
	public TxtView(Context context){
		super(context);		
	}
	public TxtView(Context context, View data) {
		super(context);
		this.mData = data;
		bindProperty(data);
	}

	private void bindProperty(View data) {
		PropertyUtils.setCommonProperties(this,data);
		
		Bind bind = data.getBindByName(PROP_TEXT);
		if(bind != null){
			setText(Html.fromHtml(bind.getValue().getvalue()));
		}
		
		bind = data.getBindByName(PROP_TEXT_SIZE);
		if(bind != null){
			setTextSize(TypedValue.COMPLEX_UNIT_PX,
					PropertyUtils.getScaledSize(Float.parseFloat(bind
							.getValue().getvalue())));
		}
		
		bind = data.getBindByName(PROP_TEXT_COLOR);
		if(bind != null){
			Value v = bind.getValue();
			if(Value.TYPE_JSON.equals(v.getType())){
				try {
					setTextColor(PropertyUtils.genColorList(v.getJsonValue()));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(Value.TYPE_STRING.equals(v.getType())){
				setTextColor(PropertyUtils.parseColor(v.getvalue()));
			}
		}
		
		bind = data.getBindByName(PROP_GRAVITY);
		if(bind != null){
			setGravity(PropertyUtils.parseGravity(bind.getValue().getvalue()));
		}
		
		bind = data.getBindByName(PROP_DRAWABLE_PADDING);
		if(bind != null){
			setCompoundDrawablePadding(Integer.parseInt(bind.getValue().getvalue()));
		}
		int maxSize = PropertyUtils.getMaxBitmapSize(data);
		bind = data.getBindByName(PROP_DRAWABLE_LEFT);
		if(bind != null){
			PropertyUtils.loadDrawable(this, bind.getValue(), TaskType.TEXT_LEFT, maxSize);
		}
		bind = data.getBindByName(PROP_DRAWABLE_RIGHT);
		if(bind != null){
			PropertyUtils.loadDrawable(this, bind.getValue(), TaskType.TEXT_RIGHT, maxSize);
		}
		bind = data.getBindByName(PROP_DRAWABLE_TOP);
		if(bind != null){
			PropertyUtils.loadDrawable(this, bind.getValue(), TaskType.TEXT_TOP, maxSize);
		}
		bind = data.getBindByName(PROP_DRAWABLE_BOTTOM);
		if(bind != null){
			PropertyUtils.loadDrawable(this, bind.getValue(), TaskType.TEXT_BOTTOM, maxSize);
		}
		bind = data.getBindByName(PROP_SIGNLE_LINE);
		if(bind != null){
			if(Boolean.parseBoolean(bind.getValue().getvalue()))
				setSingleLine();
		}	
		bind = data.getBindByName(PROP_LINES);
		if(bind != null){
			setLines(Integer.parseInt(bind.getValue().getvalue()));
		}
		bind = data.getBindByName(PROP_SELECTED);
		if(bind != null){
			setSelected(Boolean.parseBoolean(bind.getValue().getvalue()));
		}	
		bind = data.getBindByName(PROP_TEXT_STYLE);
		if(bind != null){
			String style = bind.getValue().getvalue().toLowerCase();
			boolean hasBold = style.contains(TYPEFACE_BOLD);
			boolean hasItalic = style.contains(TYPEFACE_ITALIC);
			if(hasBold && hasItalic)
				setTypeface(null, Typeface.BOLD_ITALIC);
			else if(hasBold)
				setTypeface(null, Typeface.BOLD);
			else if(hasItalic)
				setTypeface(null, Typeface.ITALIC);
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
		if(count > 0)
			bindProperty(mData);
	}
}
