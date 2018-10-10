package ipanel.join.configuration;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Style implements Serializable{

	public static final String ATT_PARENT = "parent";

	public static final String ATT_ID = "id";

	/**
	 * 
	 */
	private static final long serialVersionUID = -1794150674841420248L;

	public static final String ITEM_TAG="style";
	
	protected String id;
	protected String parent;
    protected List<Bind> bind;
    protected Map<String, Bind> mBindMap = new HashMap<String, Bind>();

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
		if(mBindMap.containsKey(name))
			return mBindMap.get(name);
		if(parent != null){
			Style pt = ConfigState.getInstance().getConfiguration().findStyleById(parent);
			if(pt != null)
				return pt.getBindByName(name);
		}
		return null;
	}

	public void loadData(XmlPullParser xpp) throws XmlPullParserException,IOException {
		int eventType = xpp.getEventType();
		if (eventType == XmlPullParser.START_TAG
				&& ITEM_TAG.equals(xpp.getName())) {
			id = xpp.getAttributeValue(null, ATT_ID);
			parent = xpp.getAttributeValue(null, ATT_PARENT);

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
