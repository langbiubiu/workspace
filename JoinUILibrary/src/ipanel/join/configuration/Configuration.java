//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.7 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2013.08.15 时间 09:42:14 AM CST 
//

package ipanel.join.configuration;

import ipanel.join.widget.ImgView;
import ipanel.join.widget.PropertyUtils;
import ipanel.join.widget.TxtView;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import cn.ipanel.android.Logger;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.StateListImageFetchTask;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

/**
 * 
 */
public class Configuration implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8757129881610584243L;
//	private String TAG = "Configuration";
	public static final String ITEM_TAG = "configuration";
	public static final String ATT_VERSION = "version";
	public static final String ATT_SCALE = "scale";
	public static final String ATT_TARGET_WIDTH = "targetWidth";
	public static final String ATT_EXT_JAR = "extJar";
	public static final String ATT_EXT_RES = "extRes";
	public static final String TEMPLATE = "template";
	
	protected String version;
	protected List<Screen> screen;
	protected List<Style> styles;
	protected float scale;
	protected int targetWidth;
	protected String extJar;
	protected String extRes;

	protected Map<String, Screen> mScreenMap= new HashMap<String, Screen>();
	protected Map<String, Style> mStyleMap= new HashMap<String, Style>();
	
	protected Set<String> imgResources = new LinkedHashSet<String>();
	
	/**
	 * 获取version属性的值。
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * 设置version属性的值。
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVersion(String value) {
		this.version = value;
	}
	
	public float getScale(){
		if(scale <= 0)
			return 1.0f;
		return scale;
	}
	
	public void setScale(float scale){
		this.scale = scale;
	}
	
	public Screen findScreenById(String id){
		return mScreenMap.get(id);
	}
	
	public Style findStyleById(String id){
		return mStyleMap.get(id);
	}
	
	public void setExtJar(String extJar){
		this.extJar = extJar;
	}
	
	public String getExtJar(){
		return this.extJar;
	}
	
	public String getExtRes(){
		return this.extRes;
	}

	/**
	 * Gets the value of the screen property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the screen property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getScreen().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Screen }
	 * 
	 * 
	 */
	public List<Screen> getScreen() {
		if (screen == null) {
			screen = new ArrayList<Screen>();
		}
		return this.screen;
	}
	
	public List<Style> getStyles(){
		if(styles == null)
			styles = new ArrayList<Style>();
		return this.styles;
	}
	
	public void calculateScale(Context ctx) {
		if (targetWidth > 0) {
			DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
			float deviceWidth = Math.max(dm.widthPixels, dm.heightPixels);
			scale = deviceWidth / targetWidth;
		}
	}

	public void loadData(XmlPullParser xpp) throws XmlPullParserException,IOException {
		int eventType = xpp.getEventType();
		if (eventType == XmlPullParser.START_TAG&& ITEM_TAG.equals(xpp.getName())) {
			version = xpp.getAttributeValue(null, ATT_VERSION);
			extJar = xpp.getAttributeValue(null, ATT_EXT_JAR);
			extRes = xpp.getAttributeValue(null, ATT_EXT_RES);
			String sc = xpp.getAttributeValue(null, ATT_SCALE);
			String tw = xpp.getAttributeValue(null, ATT_TARGET_WIDTH);
			if(sc != null)
				scale = Float.parseFloat(sc);
			else if(tw != null){
				targetWidth = Integer.parseInt(tw);
			}
			if(scale <= 0)
				scale = 1.0f;
			eventType = xpp.next();
			while (!(eventType == XmlPullParser.END_TAG && ITEM_TAG.equals(xpp.getName()))) {
				if (eventType == XmlPullParser.START_TAG&& Screen.ITEM_TAG.equals(xpp.getName())) {
					Screen screen = new Screen();
					screen.loadData(xpp, Screen.ITEM_TAG);
					this.getScreen().add(screen);
					mScreenMap.put(screen.id, screen);
				}
				if (eventType == XmlPullParser.START_TAG&& TEMPLATE.equals(xpp.getName())) {
					Screen screen = new Screen();
					screen.loadData(xpp, TEMPLATE);
					this.getScreen().add(0, screen);
					//mScreenMap.put(screen.id, screen);
				}
				if (eventType == XmlPullParser.START_TAG&& Style.ITEM_TAG.equals(xpp.getName())) {
					Style style = new Style();
					style.loadData(xpp);
					this.getStyles().add(style);
					mStyleMap.put(style.id, style);
				}
				eventType = xpp.next();
			}
		}
	}
	
	public boolean isAllImageCached(Context ctx){
		ImageFetcher mFetcher = ConfigState.getInstance().getImageFetcher(ctx);
		for(String key : imgResources){
			if(!mFetcher.hasDiskCacheFor(key)){
				Logger.d("Image NOT cached: "+key);
				return false;
			}
		}
		Logger.d("All image cached for config version = "+version);
		return true;
	}
	
	public Set<String> getAllImages(){
		return imgResources;
	}
	
	public void findAllImages(Context ctx){
		if(screen != null){
			for(Screen sc : screen){
				findImageInView(ctx, sc.view);
			}
		}
		if(styles != null){
			for(Style stl : styles){
				findImageInStyle(ctx, stl);
			}
		}
	}

	private void findImageInStyle(Context ctx, Style style) {
		if(style.bind == null)
			return;
		for(Bind bd : style.bind){
			findImageInBind(ctx, bd, -1);
		}
		
	}

	private void findImageInBind(Context ctx, Bind bd, int maxSize) {
		ImageFetcher mFetcher = ConfigState.getInstance().getImageFetcher(ctx);

		if (PropertyUtils.PROP_BACKGROUND_DRAWABLE.equals(bd.property)
				|| ImgView.PROP_DRAWABLE.equals(bd.property)
				|| ImgView.PROP_DRAWABLE9.equals(bd.property)
				|| TxtView.PROP_DRAWABLE_LEFT.equals(bd.getProperty())
				|| TxtView.PROP_DRAWABLE_RIGHT.equals(bd.getProperty())
				|| TxtView.PROP_DRAWABLE_TOP.equals(bd.getProperty())
				|| TxtView.PROP_DRAWABLE_BOTTOM.equals(bd.getProperty())
				|| PropertyUtils.PROP_BACKGROUND_DRAWABLE9.equals(bd.getProperty())
				) {
			if (Value.TYPE_JSON.equals(bd.value.type)) {
				try {
					JSONObject jobj = bd.value.getJsonValue();
					StateListImageFetchTask task = maxSize > 0 ? new StateListImageFetchTask(
							maxSize, maxSize) : mFetcher.getTask();
					if (jobj.has(PropertyUtils.STATE_FOCUS)) {
						task.addStateUrl(new int[] { android.R.attr.state_focused },
								jobj.getString(PropertyUtils.STATE_FOCUS));
					}
					if (jobj.has(PropertyUtils.STATE_SELECTED)) {
						task.addStateUrl(new int[] { android.R.attr.state_selected },
								jobj.getString(PropertyUtils.STATE_SELECTED));
					}
					if (jobj.has(PropertyUtils.STATE_NORMAL)) {
						task.addStateUrl(new int[] {}, jobj.getString(PropertyUtils.STATE_NORMAL));
					}
					
					int count = task.getImageCount();
					for (int i = 0; i < count; i++) {
						String imgUrl = task.getImageUrl(i);
						if(TextUtils.isEmpty(imgUrl) || !imgUrl.startsWith("http"))
							continue;
						imgResources.add(imgUrl);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				BaseImageFetchTask task = maxSize > 0 ? new BaseImageFetchTask(bd.value.getvalue(),
						maxSize, maxSize) : mFetcher.getBaseTask(bd.value.getvalue());
				String imgUrl = task.getImageUrl(0);
				if(!TextUtils.isEmpty(imgUrl) && imgUrl.startsWith("http"))
					imgResources.add(imgUrl);
			}
		}

	}

	private void findImageInView(Context ctx, View view) {
		if(view == null){
			return;
		}
		if(view.bind != null){
			for(Bind bd : view.bind){
				findImageInBind(ctx, bd, PropertyUtils.getMaxBitmapSize(view));
			}
		}
		if(view.views != null){
			for(View v : view.views){
				findImageInView(ctx, v);
			}
		}
	}
}
