package org.cybergarage.upnp.std.av.renderer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.cybergarage.xml.Node;

public class Event{
	public static final String XML_NS = "urn:schemas-upnp-org:metadata-1-0/AVT/";
	public static final String NAME = "Event";
	
	public static final String VAL = "val";
	
	
	private Map<String, Map<String, String>> instances;
	
	public Event(){
		instances = new HashMap<String, Map<String, String>>();
	}
	
	public void addProperty(String name, String value){
		addProperty("0", name, value);
	}
	
	public void addProperty(String id, String name, String value){
		Map<String, String> pl = instances.get(id);
		if(pl == null){
			pl = new HashMap<String, String>();
			instances.put(id, pl);
		}
		pl.put(name, value);
	}
	
	public Node toNode(){
		Node root = new Node(NAME);
		root.setAttribute("xmlns", XML_NS);

		for(Entry<String, Map<String, String>> entry: instances.entrySet()){
			Node inst = new Node(AVTransport.INSTANCEID);
			inst.setAttribute(VAL, entry.getKey());
			for(Entry<String, String> p : entry.getValue().entrySet()){
				Node pn = new Node(p.getKey());
				pn.setAttribute(VAL, p.getValue());
				inst.addNode(pn);
			}
			root.addNode(inst);
		}
		return root;
	}
	
	public void set(Node node){
		int count = node.getNNodes();
		instances.clear();
		for(int i=0;i<count;i++){
			Node n = node.getNode(i);
			if(AVTransport.INSTANCEID.equals(n.getName())){
				String id = n.getAttributeValue(VAL);
				Map<String, String> pl = instances.get(id);
				if(pl == null){
					pl = new HashMap<String, String>();
					instances.put(id, pl);
				}
				int pcount = n.getNNodes();
				
				for(int k=0;k<pcount;k++){
					Node p = n.getNode(k);
					pl.put(p.getName(), p.getAttributeValue(VAL));
				}
			}
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Entry<String, Map<String, String>> entry: instances.entrySet()){
			sb.append(AVTransport.INSTANCEID);
			sb.append(": ");
			sb.append(entry.getKey());
			sb.append('\n');
			for(Entry<String, String> p : entry.getValue().entrySet()){
				sb.append("    ");
				sb.append(p.getKey());
				sb.append(": ");
				sb.append(p.getValue());
				sb.append('\n');
			}
		}
		return sb.toString();
	}
}
