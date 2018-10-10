package ipaneltv.toolkit.dsmcc;

import ipaneltv.toolkit.IPanelLog;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.SparseArray;
import android.util.SparseIntArray;

public abstract class MetaInfo {
	String uuid;
	long freq;
	int pid, version;

	public MetaInfo(String uuid, long freq, int pid) {
		this.uuid = uuid;
		this.freq = freq;
		this.pid = pid;
	}

	public String getUUID() {
		return uuid;
	}

	public long getFrequency() {
		return freq;
	}

	public int getPID() {
		return pid;
	}

	public int getVersion() {
		return version;
	}

	public static class Pmt extends MetaInfo {
		int ocid, pmtpid, dsipid, version;
		SparseIntArray tagsmap = new SparseIntArray();

		public Pmt(String uuid, long freq, int pid) {
			super(uuid, freq, pid);
		}

		public int getDsipid() {
			return dsipid;
		}

		public int tagSize() {
			return tagsmap.size();
		}

		public int tagAt(int index) {
			return tagsmap.keyAt(index);
		}

		public int getTagMappedPID(int tag) {
			return tagsmap.get(tag);
		}

		public void setAttr(int ocid, int dsiPid, int version) {
			this.ocid = ocid;
			this.dsipid = dsiPid;
			this.version = version;
		}

		public void setAttr(int pmtpid, int version) {
			this.version = version;
			this.pmtpid = pmtpid;
		}

		public void putTag(int tag, int dsipid) {
			this.dsipid = dsipid;
			tagsmap.put(tag, dsipid);
		}
	}

	public static class Dsi extends MetaInfo {
		int transactionId, transactionTag, moduleId, objectKey, version;

		public Dsi(String uuid, long freq, int pid) {
			super(uuid, freq, pid);
		}

		public int getRootTransactionId() {
			return transactionId;
		}

		public int getRootTransactionTag() {
			return transactionTag;
		}

		public int getRootMooduleId() {
			return moduleId;
		}

		public int getRootObjectKey() {
			return objectKey;
		}

		public void setAttr(int transId, int transTag, int modId, int objKey, int ver) {
			this.transactionId = transId;
			this.transactionTag = transTag;
			this.moduleId = modId;
			this.objectKey = objKey;
			this.version = ver;
		}
	}

	public static class Dii extends MetaInfo {
		int diiPid, transactionTag, version;
		SparseArray<Mod> mods = new SparseArray<Mod>();

		public Dii(String uuid, long freq, int pid) {
			super(uuid, freq, pid);
		}

		public int getDiiPid() {
			return diiPid;
		}

		public int getTransactionTag() {
			return transactionTag;
		}

		public int modSize() {
			return mods.size();
		}

		public Mod modAt(int i) {
			return mods.valueAt(i);
		}

		public Mod getModById(int id) {
			return mods.get(id);
		}

		public void setAttr(int diiPid, int transTag, int ver) {
			this.transactionTag = transTag;
			this.diiPid = diiPid;
			this.version = ver;
		}

		public void addMod(int tag, int id, int len, int ver, int link) {
			Mod m = new Mod(tag, id, len, ver, link);
			mods.put(id, m);
		}

		public class Mod {
			public final int tag, id, length, version, link;
			int pid = -1;
			Module mod;

			Mod(int tag, int id, int len, int ver, int link) {
				this.tag = tag;
				this.id = id;
				this.version = ver;
				this.length = len;
				this.link = link;
			}

			public Dii getDii() {
				return Dii.this;
			}

			public void setPID(int pid) {
				this.pid = pid;
			}

			public int getPID() {
				return pid;
			}

			public void setAttach(Module mod) {
				this.mod = mod;
			}

			public Module getAttach() {
				return mod;
			}
		}
	}

	public static class Module extends MetaInfo {
		int id, length;
		SparseArray<Buf> bufs = new SparseArray<Buf>();
		SparseArray<Dir> dirs = new SparseArray<Dir>();

		public Module(String uuid, long freq, int pid) {
			super(uuid, freq, pid);
		}

		public int getModuleId() {
			return id;
		}

		public int getModuleLength() {
			return length;
		}

		public int bufNum() {
			return bufs.size();
		}

		public Buf bufAt(int i) {
			return bufs.valueAt(i);
		}

		public Buf getBuf(int key) {
			return bufs.get(key);
		}

		public int dirNum() {
			return dirs.size();
		}

		public Dir dirAt(int i) {
			return dirs.valueAt(i);
		}

		public Dir getDir(int key) {
			return dirs.get(key);
		}

		public void setAttr(int id, int len, int version) {
			this.id = id;
			this.length = len;
			this.version = version;
		}

		Dir addDir(int key) {
			Dir d = new Dir(key);
			dirs.put(key, d);
			return d;
		}

		void addBuf(int type, int key, int off, int len) {
			Buf b = new Buf(type, key, off, len);
			bufs.put(key, b);
		}

		public class Buf {
			public final int type, key, off, len;

			public Buf(int type, int key, int off, int len) {
				this.type = type;
				this.key = key;
				this.off = off;
				this.len = len;
			}
		}

		public class Dir {
			int key;
			String name;
			SparseArray<Node> nodes = new SparseArray<Node>();

			Dir(int key) {
				this.key = key;
			}

			public int getKey() {
				return key;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public int nodeSize() {
				return nodes.size();
			}

			public Node nodeAt(int i) {
				return nodes.valueAt(i);
			}

			public Node findNodeByName(String name) {
				int s = nodes.size();
				for (int i = 0; i < s; i++) {
					Node n = nodes.valueAt(i);
					if (n.name.equals(name))
						return n;
				}
				return null;
			}

			public void addNode(String name, int key, int type, int tag, int tid, int mid) {
				Node n = new Node(name, key, type, tag, tid, mid);
				nodes.put(key, n);
			}

			public Module getModule() {
				return Module.this;
			}

			public class Node extends PathNode {
				public final int key, transactionTag, transactionId, moduleId;

				Node(String name, int key, int type, int tag, int tid, int mid) {
					super(type, name);
					this.key = key;
					this.transactionTag = tag;
					this.transactionId = tid;
					this.moduleId = mid;

				}

				public Dir getDir() {
					return Dir.this;
				}
			}
		}
	}

	public static class PathNode {
		public static final int TYPE_UNKNOWN = 0;
		public static final int TYPE_FILE = 1;
		public static final int TYPE_DIR = 2;
		public static final int TYPE_STREAM = 3;
		String name;
		int type;
		List<PathNode> list = null;

		public PathNode(int type, String name) {
			this.type = type;
			this.name = name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public int getType() {
			return type;
		}

		public boolean isFile() {
			return type == TYPE_FILE;
		}

		public boolean isDir() {
			return type == TYPE_DIR;
		}

		public boolean isStream() {
			return type == TYPE_STREAM;
		}

		public int childSize() {
			if (list != null)
				return list.size();
			return 0;
		}

		public PathNode childAt(int i) {
			return list.get(i);
		}

		public void addChild(int type, String name) {
			if (list == null)
				list = new ArrayList<PathNode>();
			list.add(new PathNode(type, name));
		}
	}
}

class PmtMetaHandler extends MetaXmlHandler {
	static final String TAG = "PmtMetaHandler";
	MetaInfo.Pmt pmt;

	public PmtMetaHandler() {
	}

	public boolean parse(XMLReader xreader, InputSource is, MetaInfo.Pmt out) {
		try {
			pmt = out;
			xreader.setContentHandler(this);
			xreader.parse(is);
			return true;
		} catch (Exception e) {
			IPanelLog.e(TAG, "parse error:" + e);
		} finally {
			pmt = null;
		}
		return false;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
			throws SAXException {

		if (localName.equals("tag")) {
			int tag = Integer.parseInt(atts.getValue("id"));
			int pid = Integer.parseInt(atts.getValue("pid"));
			pmt.putTag(tag, pid);
		} else if (localName.equals("pmt")) {
			int version = Integer.parseInt(atts.getValue("version"));
			int pid = Integer.parseInt(atts.getValue("pid"));
			pmt.setAttr(pid, version);
		}
	}
}

class DsiMetaHandler extends MetaXmlHandler {
	static final String TAG = "DsiMetaHandler";
	MetaInfo.Dsi dsi;

	public DsiMetaHandler() {
	}

	public boolean parse(XMLReader xreader, InputSource is, MetaInfo.Dsi out) {
		try {
			dsi = out;

			xreader.setContentHandler(this);
			xreader.parse(is);
			return true;
		} catch (Exception e) {
			IPanelLog.e(TAG, "parse error:" + e);
		} finally {
			dsi = null;
		}
		return false;
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
			throws SAXException {
		if (localName.equals("gateway")) {
			int tid = (int) Long.parseLong(atts.getValue("dii_id"));
			int tag = Integer.parseInt(atts.getValue("dii_tag"));
			int key = Integer.parseInt(atts.getValue("object_key"));
			int mid = Integer.parseInt(atts.getValue("module_id"));
			int version = Integer.parseInt(atts.getValue("version"));

			dsi.setAttr(tid, tag, mid, key, version);
		}
	}
}

class DiiMetaHandler extends MetaXmlHandler {
	static final String TAG = "DiiMetaHandler";
	MetaInfo.Dii dii;

	public DiiMetaHandler() {
	}

	public boolean parse(XMLReader xreader, InputSource is, MetaInfo.Dii out) {
		try {
			dii = out;
			xreader.setContentHandler(this);
			xreader.parse(is);
			return true;
		} catch (Exception e) {
			IPanelLog.e(TAG, "parse error:" + e);
		} finally {
			dii = null;
		}
		return false;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
			throws SAXException {
		if (localName.equals("mod")) {
			int tag = Integer.parseInt(atts.getValue("tag"));
			int id = Integer.parseInt(atts.getValue("id"));
			int len = Integer.parseInt(atts.getValue("len"));
			int version = Integer.parseInt(atts.getValue("ver"));
			int link = Integer.parseInt(atts.getValue("link"));
			dii.addMod(tag, id, len, version, link);
		} else if (localName.equals("dii")) {
			int pid = Integer.parseInt(atts.getValue("pid"));
			int tag = (int) Long.parseLong(atts.getValue("tag"));
			int version = Integer.parseInt(atts.getValue("version"));
			dii.setAttr(pid, tag, version);
		}
	}
}

class ModuleMetaHandler extends MetaXmlHandler {
	static final String TAG = "ModuleMetaHandler";
	MetaInfo.Module mod;
	MetaInfo.Module.Dir dir;

	public ModuleMetaHandler() {
	}

	public boolean parse(XMLReader xreader, InputSource is, MetaInfo.Module out) {
		try {
			mod = out;
			xreader.setContentHandler(this);
			xreader.parse(is);
			return true;
		} catch (Exception e) {
			IPanelLog.e(TAG, "parse error:" + e);
		} finally {
			mod = null;
			dir = null;
		}
		return false;
	}

	static int stypeTo(String s) {
		if (s == null ? true : s.length() == 0)
			return MetaInfo.PathNode.TYPE_UNKNOWN;
		switch (s.charAt(0)) {
		case 'f':
			return MetaInfo.PathNode.TYPE_FILE;
		case 'd':
			return MetaInfo.PathNode.TYPE_DIR;
		case 's':
			return MetaInfo.PathNode.TYPE_STREAM;
		default:
			return MetaInfo.PathNode.TYPE_UNKNOWN;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
			throws SAXException {
		if (localName.equals("node")) {
			String name = atts.getValue("name");
			int type = stypeTo(atts.getValue("type"));
			int tag = Integer.parseInt(atts.getValue("dii_tag"));
			int tid = (int) Long.parseLong(atts.getValue("dii_tid"));
			int mid = Integer.parseInt(atts.getValue("mod_id"));
			int key = Integer.parseInt(atts.getValue("key"));
			dir.addNode(name, key, type, tag, tid, mid);
		} else if (localName.equals("dir")) {
			int key = Integer.parseInt(atts.getValue("key"));
			dir = mod.addDir(key);
		} else if (localName.equals("fragment")) {
			int type = stypeTo(atts.getValue("type"));
			int key = Integer.parseInt(atts.getValue("key"));
			int off = Integer.parseInt(atts.getValue("off"));
			int len = Integer.parseInt(atts.getValue("len"));
			mod.addBuf(type, key, off, len);
		} else if (localName.equals("mod")) {
			int id = Integer.parseInt(atts.getValue("id"));
			int len = Integer.parseInt(atts.getValue("len"));
			int version = Integer.parseInt(atts.getValue("version"));
			mod.setAttr(id, len, version);
		}
	}
}

class MetaXmlHandler implements ContentHandler {

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
	}

	@Override
	public void setDocumentLocator(Locator locator) {
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts)
			throws SAXException {
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
	}

}
