//
// ���ļ����� JavaTM Architecture for XML Binding (JAXB) ����ʵ�� v2.2.7 ���ɵ�
// ����� <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �����±���Դģʽʱ, �Դ��ļ��������޸Ķ�����ʧ��
// ����ʱ��: 2013.08.15 ʱ�� 09:42:14 AM CST 
//

package ipanel.join.configuration;

import java.io.IOException;
import java.io.Serializable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 */
public class Screen implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5399594269603385096L;
	public static final String TAG="Screen";
	public static final String ITEM_TAG = "screen";
	public static final String ATT_ID = "id";
	public static final String ATT_TYPE = "type";
    protected String id;
    protected String type;
    protected View view;

    /**
     * ��ȡid���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * ����id���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * ��ȡtype���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        if (type == null) {
            return "fullscreen";
        } else {
            return type;
        }
    }

    /**
     * ����type���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * ��ȡview���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link View }
     *     
     */
    public View getView() {
        return view;
    }

    /**
     * ����view���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link View }
     *     
     */
    public void setViewGroup(View value) {
        this.view = value;
    }
   
	
	public void loadData(XmlPullParser xpp, String tag) throws XmlPullParserException, IOException {
    	int eventType = xpp.getEventType();
    	if(eventType == XmlPullParser.START_TAG && tag.equals(xpp.getName())){
    		id = xpp.getAttributeValue(null, ATT_ID);
    		type = xpp.getAttributeValue(null, ATT_TYPE);
    		eventType = xpp.next();
    		while (!(eventType == XmlPullParser.END_TAG && tag.equals(xpp.getName()))) {
    			if(eventType == XmlPullParser.START_TAG && View.ITEM_TAG.equals(xpp.getName())){
        			view = new View();
        			view.loadData(xpp);
        			this.setViewGroup(view);
        		}
    			eventType = xpp.next();
			}
    		
    	}
	}

}
