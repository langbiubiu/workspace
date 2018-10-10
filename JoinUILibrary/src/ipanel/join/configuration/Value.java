//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.7 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2013.08.15 时间 09:42:14 AM CST 
//

package ipanel.join.configuration;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 */
public class Value implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4957602338209318816L;
	public static final String TYPE_STRING = "string";
	public static final String TYPE_JSON = "json";
	public static final String TYPE_FIELD = "field";
	public static final String TYPE_TAG = "tag";

	protected String type;
	protected String source;
	protected String value;

	protected transient JSONObject element;
	protected transient JSONArray array;

	protected String fieldName;

	/**
	 * 获取type属性的值。
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getType() {
		if (type == null) {
			return "string";
		} else {
			return type;
		}
	}

	/**
	 * 设置type属性的值。
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setType(String value) {
		this.type = value;
	}

	/**
	 * 获取source属性的值。
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSource() {
		return source;
	}

	/**
	 * 设置source属性的值。
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSource(String value) {
		this.source = value;
	}

	/**
	 * 获取value属性的值。
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getvalue() {
		if (value == null && element != null)
			value = element.toString();
		if (value == null && array != null)
			value = array.toString();
		return value;
	}

	public JSONObject getJsonValue() throws JSONException {
		if (element == null && value != null)
			element = new JSONObject(value);

		return element;
	}

	public void setJsonValue(JSONObject json) {
		this.element = json;
	}

	public JSONArray getArrayValue() throws JSONException {
		if (array == null && value != null)
			array = new JSONArray(value);

		return array;
	}

	public void setArrayValue(JSONArray array) {
		this.array = array;
	}

	/**
	 * 设置value属性的值。
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setvalue(String value) {
		this.value = value;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public Value clone() {
		Value v = new Value();
		v.type = type;
		v.source = source;
		v.value = value;
		return v;
	}

}
