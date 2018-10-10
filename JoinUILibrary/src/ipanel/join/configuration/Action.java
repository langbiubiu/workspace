//
// ���ļ����� JavaTM Architecture for XML Binding (JAXB) ����ʵ�� v2.2.7 ���ɵ�
// ����� <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �����±���Դģʽʱ, �Դ��ļ��������޸Ķ�����ʧ��
// ����ʱ��: 2013.08.15 ʱ�� 09:42:14 AM CST 
//


package ipanel.join.configuration;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 */
public class Action implements Serializable{
	
	public static final String ATT_CONTAINER = "container";

	public static final String ATT_TARGET = "target";

	public static final String ATT_OPERATION = "operation";

	public static final String ATT_EVENT = "event";

	/**
	 * 
	 */
	private static final long serialVersionUID = 7165300397942854180L;

	public static final String ITEM_TAG="action";
	
    public static final String EVENT_ONCLICK = "onClick";
	public static final String EVENT_ONFOCUS = "onFocus";
	public static final String EVENT_ONSELECT = "onSelect";
    public static final String EVENT_ONITEMCLICK = "onItemClick";
	
	public static final String OP_OPEN = "open";
	public static final String OP_CLOSE = "close";
	public static final String OP_ADD = "add";
	public static final String OP_REPLACE = "replace";
	public static final String OP_REMOVE = "remove";
	public static final String OP_INTENT = "intent";
	
    protected String event;
    protected String operation;
    protected String target;
    protected String containder;
    protected List<Bind> bind;
    protected Map<String, Bind> mBindMap = new HashMap<String, Bind>();
    
    /**
     * ��ȡevent���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvent() {
        if (event == null) {
            return "click";
        } else {
            return event;
        }
    }

    /**
     * ����event���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvent(String value) {
        this.event = value;
    }

    /**
     * ��ȡoperation���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperation() {
        return operation;
    }

    /**
     * ����operation���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperation(String value) {
        this.operation = value;
    }

    /**
     * ��ȡtarget���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public String getTarget() {
        return target;
    }

    /**
     * ����target���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setTarget(String value) {
        this.target = value;
    }


    /**
     * ��ȡcontainer���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public String getContainer() {
        return containder;
    }

    /**
     * ����container���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setContainer(String value) {
        this.containder = value;
    }

    /**
     * Gets the value of the bind property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bind property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBind().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Bind }
     * 
     * 
     */
    public List<Bind> getBind() {
        if (bind == null) {
            bind = new ArrayList<Bind>();
        }
        return this.bind;
    }
    
	public Bind getBindByName(String name) {
		return mBindMap.get(name);
	}

	public void loadData(XmlPullParser xpp) throws XmlPullParserException,IOException {
		int eventType = xpp.getEventType();
		if (eventType == XmlPullParser.START_TAG
				&& ITEM_TAG.equals(xpp.getName())) {
			event = xpp.getAttributeValue(null, ATT_EVENT);
			operation = xpp.getAttributeValue(null, ATT_OPERATION);
			target = xpp.getAttributeValue(null, ATT_TARGET);
			containder = xpp.getAttributeValue(null, ATT_CONTAINER);

			eventType = xpp.next();
			while (!(eventType == XmlPullParser.END_TAG && ITEM_TAG.equals(xpp
					.getName()))) {
    			if(eventType==XmlPullParser.START_TAG&&Bind.ITEM_TAG.equals(xpp.getName())){
        			Bind bind = new Bind();
        			bind.loadData(xpp);
        			this.getBind().add(bind);
        			mBindMap.put(bind.getProperty(), bind);
    			}
    			eventType = xpp.next();
			}
		}
	}
}
