package ipanel.join.configuration;

import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ConfigExporter {
	public enum Format {
		XML_JSON_VALUE, XML_ONLY
	}

	public Format format = Format.XML_ONLY;

	public boolean mergeStyle = true;

	public void saveTo(Configuration conf, OutputStream os) throws ParserConfigurationException,
			TransformerException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.newDocument();

		doc.appendChild(toConfigNode(doc, conf));

		TransformerFactory transformerFactory = TransformerFactory.newInstance();

		Transformer transformer = transformerFactory.newTransformer();

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(os);

		transformer.transform(source, result);
	}

	public Node toConfigNode(Document doc, Configuration conf) {
		Element root = doc.createElement(Configuration.ITEM_TAG);
		setAttribute(root, Configuration.ATT_VERSION, conf.version);
		setAttribute(root, Configuration.ATT_SCALE, String.valueOf(conf.scale));
		setAttribute(root, Configuration.ATT_EXT_JAR, conf.extJar);
		setAttribute(root, Configuration.ATT_EXT_RES, conf.extRes);
		boolean first = true;
		for (Screen sc : conf.getScreen()) {
			root.appendChild(toScreenNode(doc, conf, first, sc));
			first = false;
		}
		if(!mergeStyle){
			for(Style style : conf.getStyles()){
				root.appendChild(toStyleNode(doc, conf, style));
			}
		}
		return root;
	}

	private Node toStyleNode(Document doc, Configuration conf, Style style) {
		Element root = doc.createElement(Style.ITEM_TAG);
		setAttribute(root, Style.ATT_ID, style.id);
		setAttribute(root, Style.ATT_PARENT, style.parent);
		for(Bind bind : style.getBind()){
			root.appendChild(toBindNode(doc, conf, bind));
		}
		return root;
	}

	private void setAttribute(Element node, String key, String value) {
		if (value != null) {
			node.setAttribute(key, value);
		}
	}

	private Node toScreenNode(Document doc, Configuration conf, boolean first, Screen sc) {
		Element root = doc
				.createElement(first && format == Format.XML_ONLY ? Configuration.TEMPLATE
						: Screen.ITEM_TAG);
		setAttribute(root, first && format == Format.XML_ONLY ? "name" : Screen.ATT_ID, sc.id);
		setAttribute(root, Screen.ATT_TYPE, sc.type);
		root.appendChild(toViewNode(doc, conf, sc.view));
		return root;
	}

	private Node toViewNode(Document doc, Configuration conf, View view) {
		Element root = doc.createElement(View.ITEM_TAG);
		setAttribute(root, View.ATT_ID, view.id);
		setAttribute(root, View.ATT_CLAZZ, view.clazz);
		if (!mergeStyle)
			setAttribute(root, View.ATT_STYLE, view.style);
		if (mergeStyle) {
			Style style = conf.findStyleById(view.style);
			if (style != null) {
				mergeStyle(doc, conf, root, style);
			}
		}
		for (Bind bind : view.getBind()) {
			root.appendChild(toBindNode(doc, conf, bind));
		}
		for (Action action : view.getAction()) {
			root.appendChild(toActionNode(doc, conf, action));
		}
		for (View v : view.getView()) {
			root.appendChild(toViewNode(doc, conf, v));
		}
		return root;
	}
	
	private void mergeStyle(Document doc, Configuration conf, Element root, Style style){
		if(style == null)
			return;
		if(style.parent != null){
			Style pStyle = conf.findStyleById(style.parent);
			mergeStyle(doc, conf, root, pStyle);
		}
		for (Bind bind : style.getBind()) {
			root.appendChild(toBindNode(doc, conf, bind));
		}		
	}

	private Node toActionNode(Document doc, Configuration conf, Action action) {
		Element root = doc.createElement(Action.ITEM_TAG);
		setAttribute(root, Action.ATT_EVENT, action.getEvent());
		setAttribute(root, Action.ATT_OPERATION, action.getOperation());
		setAttribute(root, Action.ATT_TARGET, action.getTarget());
		setAttribute(root, Action.ATT_CONTAINER, action.getContainer());

		for (Bind bind : action.getBind()) {
			root.appendChild(toBindNode(doc, conf, bind));
		}
		return root;
	}

	private Node toBindNode(Document doc, Configuration conf, Bind bind) {
		if (format == Format.XML_JSON_VALUE)
			return toBindNodeJson(doc, conf, bind);
		Element root = doc.createElement(Bind.ITEM_TAG);
		setAttribute(root, Bind.ATT_ID, bind.property);
		setAttribute(root, Bind.ATT_TARGET, bind.target);
		JSONObject json = null;
		try {
			json = bind.getValue().getJsonValue();
		} catch (JSONException e) {
		}
		if (json != null) {
			try {
				root.appendChild(toElementNode(doc, null, json));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				JSONArray jsa = bind.getValue().getArrayValue();
				root.appendChild(toArrayNode(doc, null, jsa));
			} catch (JSONException e) {
				root.setTextContent(bind.getValue().getvalue());
			}
		}
		return root;
	}

	private Node toArrayNode(Document doc, String name, JSONArray jsa) throws JSONException {
		Element root = doc.createElement(Bind.ARRAY);
		if (name != null)
			setAttribute(root, Bind.NAME, name);
		for (int i = 0; i < jsa.length(); i++) {
			Object value = jsa.get(i);
			if (value instanceof JSONArray)
				root.appendChild(toArrayNode(doc, null, (JSONArray) value));
			else if (value instanceof JSONObject)
				root.appendChild(toElementNode(doc, null, (JSONObject) value));
			else
				root.appendChild(toPropertyNode(doc, null, value));
		}
		return root;
	}

	private Node toElementNode(Document doc, String name, JSONObject json) throws JSONException {
		Element root = doc.createElement(Bind.ELEMENT);
		if (name != null)
			setAttribute(root, Bind.NAME, name);
		Iterator<?> it = json.keys();
		while (it.hasNext()) {
			String key = it.next().toString();
			Object value = json.get(key);
			if (value instanceof JSONArray)
				root.appendChild(toArrayNode(doc, key, (JSONArray) value));
			else if (value instanceof JSONObject)
				root.appendChild(toElementNode(doc, key, (JSONObject) value));
			else
				root.appendChild(toPropertyNode(doc, key, value));
		}
		return root;
	}

	private Node toPropertyNode(Document doc, String key, Object value) {
		Element root = doc.createElement(Bind.PROPERTY);
		if (key != null)
			setAttribute(root, Bind.NAME, key);
		if (value instanceof Integer) {
			setAttribute(root, Bind.TYPE, "int");
		} else if (value instanceof Long) {
			setAttribute(root, Bind.TYPE, "long");
		} else if (value instanceof Boolean) {
			setAttribute(root, Bind.TYPE, "boolean");
		} else if (value instanceof Float) {
			setAttribute(root, Bind.TYPE, "float");
		} else if (value instanceof Double) {
			setAttribute(root, Bind.TYPE, "double");
		} else {
			setAttribute(root, Bind.TYPE, "string");
		}
		root.setTextContent(String.valueOf(value));
		return root;
	}

	private Node toBindNodeJson(Document doc, Configuration conf, Bind bind) {
		Element root = doc.createElement(Bind.ITEM_TAG);
		setAttribute(root, Bind.ATT_TARGET, bind.target);

		Element p = doc.createElement(Bind.PROPERTY);
		p.setTextContent(bind.property);
		root.appendChild(p);

		Element v = doc.createElement(Bind.VALUE);
		setAttribute(v, Bind.ATT_TYPE, bind.getValue().getType());
		setAttribute(v, Bind.ATT_FIELD_NAME, bind.getValue().getFieldName());
		v.setTextContent(bind.getValue().getvalue());
		root.appendChild(v);
		return root;
	}
}
