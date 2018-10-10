//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.7 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2013.08.15 时间 09:42:14 AM CST 
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
public class View implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3504615161367457465L;
	public static final String TAG="View";
    public static final String ITEM_TAG = "view";
    public static final String ATT_ID = "id";
    public static final String ATT_CLAZZ = "clazz";
    public static final String ATT_STYLE = "style";
    
	protected String id;
    protected String clazz;
    protected String style;
    
    protected List<Bind> bind;
    protected List<Action> actions;
    protected List<View> views;
    
    protected boolean hasFocusAction = false;
    
    protected boolean hasClickAction  = false;
    
    protected Map<String, Bind> mBindMap = new HashMap<String, Bind>();
    
    protected Map<String, Bind> mExtBindMap = new HashMap<String, Bind>();
   
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
     * 获取clazz属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }
    
    public String getStyle(){
    	return style;
    }

    /**
     * 设置clazz属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
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

    /**
     * Gets the value of the action property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the action property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Action }
     * 
     * 
     */
    public List<Action> getAction() {
        if (actions == null) {
        	actions = new ArrayList<Action>();
        }
        return this.actions;
    }
    
	public Action findActionBy(String event, String op) {
		for (Action action : getAction()) {
			if (event.equals(action.getEvent()) && op.equals(action.getOperation())) {
				return action;
			}
		}
		return null;
	}
    
	public List<View> getView() {
		if (views==null) {
			views=new ArrayList<View>();
		}
		return this.views;
	}

	public boolean hasFocusAction() {
		return hasFocusAction;
	}

	public boolean hasClickAction() {
		return hasClickAction;
	}

	public void loadData(XmlPullParser xpp) throws XmlPullParserException, IOException {
    	int eventType = xpp.getEventType();
    	if(eventType == XmlPullParser.START_TAG && ITEM_TAG.equals(xpp.getName())){
    		id = xpp.getAttributeValue(null, ATT_ID);
    		clazz = xpp.getAttributeValue(null, ATT_CLAZZ);
    		style = xpp.getAttributeValue(null, ATT_STYLE);
    		eventType = xpp.next();
    		while(!(eventType == XmlPullParser.END_TAG &&ITEM_TAG.equals(xpp.getName()))){
    			if(eventType==XmlPullParser.START_TAG&&Bind.ITEM_TAG.equals(xpp.getName())){
        			Bind bind = new Bind();
        			bind.loadData(xpp);
        			this.getBind().add(bind);
        			mBindMap.put(bind.getProperty(), bind);
    			}
    			if (eventType==XmlPullParser.START_TAG&&ITEM_TAG.equals(xpp.getName())) {
    				View view = new View();
    				view.loadData(xpp);
    				this.getView().add(view);
    			}
    			if (eventType==XmlPullParser.START_TAG&&Action.ITEM_TAG.equals(xpp.getName())) {
    				Action action=new Action();
    				action.loadData(xpp);
    				if(Action.EVENT_ONCLICK.equals(action.event)){
    					hasClickAction = true;
    				}
    				if(Action.EVENT_ONFOCUS.equals(action.event)){
    					hasFocusAction = true;
    				}
    			    this.getAction().add(action);
				}
        		   eventType = xpp.next();
    		}
    	}
	}

	public Bind getBindByName(String name) {
		if(mExtBindMap.containsKey(name))
			return mExtBindMap.get(name);
		if(mBindMap.containsKey(name))
			return mBindMap.get(name);
		if(style != null && ConfigState.getInstance().getConfiguration() != null){
			Style st = ConfigState.getInstance().getConfiguration().findStyleById(style);
			if(st != null)
				return st.getBindByName(name);
		}
		return null;
	}
	
	public int applyExtBinds(List<Bind> binds ){
		mExtBindMap.clear();
		if(binds != null){
			for(Bind bd : binds){
				if(id.equals(bd.getTarget())){
					mExtBindMap.put(bd.getProperty(), bd);
				}
			}
		}
		return mExtBindMap.size();
	}

}
