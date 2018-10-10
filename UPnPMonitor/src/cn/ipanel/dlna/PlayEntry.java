package cn.ipanel.dlna;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.cybergarage.upnp.std.av.server.object.ContentNode;
import org.cybergarage.upnp.std.av.server.object.container.ContainerNode;
import org.cybergarage.upnp.std.av.server.object.item.ItemNode;
import org.cybergarage.xml.Node;

public class PlayEntry implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3779391722969828064L;

	public static String EXTRA_ENTRY = "entry";
	public static String EXTRA_ENTRY_LIST = "entryList";
	public static String EXTRA_POSITION = "position";

	public String title;
	public String url;
	public String format;

	public PlayEntry(String title, String url, String format) {
		this.title = title;
		this.url = url;
		this.format = format;
	}

	public static ArrayList<PlayEntry> getChildList(ContainerNode root) {
		ArrayList<PlayEntry> entries = new ArrayList<PlayEntry>();
		for (int i = 0; i < root.getNContentNodes(); i++) {
			ContentNode cn = root.getContentNode(i);
			if (cn instanceof ItemNode) {
				entries.add(new PlayEntry((ItemNode) cn));
			}
		}
		return entries;
	}

	public PlayEntry(ItemNode itemNode) {
		if (itemNode.getResourceNodeList().size() > 0) {
			String url = itemNode.getResourceNode(0).getURL();
			String info = itemNode.getResourceNode(0).getContentFormat();
			this.title = itemNode.getTitle();
			this.url = url;
			this.format = info;
		}
	}
}
