//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.7 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2013.08.15 时间 09:42:14 AM CST 
//

package ipanel.join.configuration;

import java.io.IOException;
import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.text.TextUtils;

/**
 * 
 */
public class Bind implements Serializable {

	public static final String ATT_FIELD_NAME = "fieldName";
	public static final String ATT_TYPE = "type";
	/**
	 * 
	 */
	private static final long serialVersionUID = -2497638143250699231L;
	public static final String TAG = "Bind";
	public static final String ITEM_TAG = "bind";
	public static final String ATT_TARGET = "target";
	public static final String PROPERTY = "property";
	public static final String VALUE = "value";
	public static final String ATT_ID = "id";
	public static final String ARRAY = "array";
	public static final String ELEMENT = "element";
	public static final String NAME = "name";
	public static final String TYPE = ATT_TYPE;

	protected String target;
	protected String property;
	protected Value value;

	/**
	 * 获取target属性的值。
	 * 
	 * @return possible object is {@link Object }
	 * 
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * 设置target属性的值。
	 * 
	 * @param value
	 *            allowed object is {@link Object }
	 * 
	 */
	public void setTarget(String value) {
		this.target = value;
	}

	/**
	 * 获取property属性的值。
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * 设置property属性的值。
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setProperty(String value) {
		this.property = value;
	}

	/**
	 * 获取value属性的值。
	 * 
	 * @return possible object is {@link Value }
	 * 
	 */
	public Value getValue() {
		return value;
	}

	/**
	 * 设置value属性的值。
	 * 
	 * @param value
	 *            allowed object is {@link Value }
	 * 
	 */
	public void setValue(Value value) {
		this.value = value;
	}

	public boolean matchTarget(String id) {
		if (target != null)
			return target.equals(id);
		return true;
	}

	public void loadData(XmlPullParser xpp) throws XmlPullParserException, IOException {
		int eventType = xpp.getEventType();
		if (eventType == XmlPullParser.START_TAG && ITEM_TAG.equals(xpp.getName())) {
			target = xpp.getAttributeValue(null, ATT_TARGET);
			String id = xpp.getAttributeValue(null, ATT_ID);
			eventType = xpp.next();
			while (!(eventType == XmlPullParser.END_TAG && ITEM_TAG.equals(xpp.getName()))) {
				if (id != null && eventType == XmlPullParser.TEXT) {
					String text = xpp.getText().trim();
					if (value == null) {
						//Log.d(TAG, "TEXT: " + id + " = " + text);
						Value v = new Value();
						v.setvalue(text.trim());
						v.setType(Value.TYPE_STRING);
						this.setProperty(id);
						this.setValue(v);
					}
				}
				if (eventType == XmlPullParser.START_TAG && ARRAY.equals(xpp.getName())) {
					loadJsonArray(xpp);
					this.setProperty(id);
					//Log.d(TAG, "ARRAY: "+id+" = "+getValue().getvalue());
				}
				if (eventType == XmlPullParser.START_TAG && ELEMENT.equals(xpp.getName())) {
					loadJsonObject(xpp);
					this.setProperty(id);
					//Log.d(TAG, "ELEMENT: "+id+" = "+getValue().getvalue());
				}
				if (eventType == XmlPullParser.START_TAG && PROPERTY.equals(xpp.getName())) {
					String property = xpp.nextText();
					this.setProperty(property);
				}
				if (eventType == XmlPullParser.START_TAG && VALUE.equals(xpp.getName())) {
					String type = xpp.getAttributeValue(null, ATT_TYPE);
					String fieldName = xpp.getAttributeValue(null, ATT_FIELD_NAME);
					String value = xpp.nextText().trim();
					Value v = new Value();
					v.setType(type);
					v.setvalue(value);
					v.setFieldName(fieldName);
					this.setValue(v);
				}
				eventType = xpp.next();
			}
			//Log.d(TAG, property+" = "+getValue().getvalue());
		}
	}

	private void loadJsonObject(XmlPullParser xpp) throws XmlPullParserException, IOException {
		JSONObject root = parseElement(xpp, null);
		Value v = new Value();
		v.setType(Value.TYPE_JSON);
		v.setJsonValue(root);
		this.setValue(v);
	}

	private JSONObject parseElement(XmlPullParser xpp, Object parent)
			throws XmlPullParserException, IOException {
		JSONObject root = new JSONObject();
		String name = xpp.getAttributeValue(null, NAME);
		int eventType = xpp.next();
		while (!(eventType == XmlPullParser.END_TAG && ELEMENT.equals(xpp.getName()))) {
			if (eventType == XmlPullParser.START_TAG && ARRAY.equals(xpp.getName())) {
				parseArray(xpp, root);
			}
			if (eventType == XmlPullParser.START_TAG && ELEMENT.equals(xpp.getName())) {
				parseElement(xpp, root);
			}
			if (eventType == XmlPullParser.START_TAG && PROPERTY.equals(xpp.getName())) {
				try {
					parseProperty(xpp, root);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			eventType = xpp.next();
		}
		if (parent != null) {
			if (parent instanceof JSONObject && name != null) {
				try {
					((JSONObject) parent).put(name, root);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (parent instanceof JSONArray) {
				((JSONArray) parent).put(root);
			}
		}
		return root;
	}

	private void parseProperty(XmlPullParser xpp, Object root) throws XmlPullParserException,
			IOException, JSONException {
		String name = xpp.getAttributeValue(null, NAME);
		String type = xpp.getAttributeValue(null, TYPE);
		String text = xpp.nextText().trim();
		JSONArray jsa = null;
		JSONObject jobj = null;
		if (root instanceof JSONArray)
			jsa = (JSONArray) root;
		if (root instanceof JSONObject)
			jobj = (JSONObject) root;
		if ("int".equals(type)) {
			int v = TextUtils.isEmpty(text) ? 0 : Integer.parseInt(text);
			if (jsa != null)
				jsa.put(v);
			if (jobj != null && name != null)
				try {
					jobj.put(name, v);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		} else if ("long".equals(type)) {
			long v = TextUtils.isEmpty(text) ? 0 : Long.parseLong(text);
			if (jsa != null)
				jsa.put(v);
			if (jobj != null && name != null)
				jobj.put(name, v);
		} else if ("float".equals(type) || "double".equals(type)) {
			double v = TextUtils.isEmpty(text) ? 0 : Double.parseDouble(text);
			if (jsa != null)
				jsa.put(v);
			if (jobj != null && name != null)
				jobj.put(name, v);
		} else if ("boolean".equals(type)) {
			boolean v = Boolean.parseBoolean(text);
			if (jsa != null)
				jsa.put(v);
			if (jobj != null && name != null)
				jobj.put(name, v);
		} else {
			if (jsa != null)
				jsa.put(text);
			if (jobj != null && name != null)
				jobj.put(name, text);
		}
	}

	private JSONArray parseArray(XmlPullParser xpp, Object parent) throws XmlPullParserException,
			IOException {
		JSONArray root = new JSONArray();
		String name = xpp.getAttributeValue(null, NAME);
		int eventType = xpp.next();
		while (!(eventType == XmlPullParser.END_TAG && ARRAY.equals(xpp.getName()))) {
			if (eventType == XmlPullParser.START_TAG && ARRAY.equals(xpp.getName())) {
				parseArray(xpp, root);
			}
			if (eventType == XmlPullParser.START_TAG && ELEMENT.equals(xpp.getName())) {
				parseElement(xpp, root);
			}
			if (eventType == XmlPullParser.START_TAG && PROPERTY.equals(xpp.getName())) {
				try {
					parseProperty(xpp, root);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			eventType = xpp.next();
		}
		if (parent != null) {
			if (parent instanceof JSONObject && name != null) {
				try {
					((JSONObject) parent).put(name, root);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (parent instanceof JSONArray) {
				((JSONArray) parent).put(root);
			}
		}
		return root;
	}

	private void loadJsonArray(XmlPullParser xpp) throws XmlPullParserException, IOException {
		JSONArray root = parseArray(xpp, null);
		Value v = new Value();
		v.setType(Value.TYPE_JSON);
		v.setArrayValue(root);
		this.setValue(v);
	}

	@Override
	public Bind clone() {
		Bind b = new Bind();
		b.property = property;
		b.target = target;
		if (value != null)
			b.value = value.clone();
		return b;
	}

}
