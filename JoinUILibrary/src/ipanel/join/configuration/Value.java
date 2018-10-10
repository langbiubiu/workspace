//
// ���ļ����� JavaTM Architecture for XML Binding (JAXB) ����ʵ�� v2.2.7 ���ɵ�
// ����� <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �����±���Դģʽʱ, �Դ��ļ��������޸Ķ�����ʧ��
// ����ʱ��: 2013.08.15 ʱ�� 09:42:14 AM CST 
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
	 * ��ȡtype���Ե�ֵ��
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
	 * ����type���Ե�ֵ��
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setType(String value) {
		this.type = value;
	}

	/**
	 * ��ȡsource���Ե�ֵ��
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSource() {
		return source;
	}

	/**
	 * ����source���Ե�ֵ��
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSource(String value) {
		this.source = value;
	}

	/**
	 * ��ȡvalue���Ե�ֵ��
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
	 * ����value���Ե�ֵ��
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
