//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.7 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2013.08.15 时间 09:42:14 AM CST 
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
     * 获取id属性的值。
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
     * 设置id属性的值。
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
     * 获取type属性的值。
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
     * 设置type属性的值。
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
     * 获取view属性的值。
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
     * 设置view属性的值。
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
