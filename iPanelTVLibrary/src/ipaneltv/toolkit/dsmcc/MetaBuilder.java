package ipaneltv.toolkit.dsmcc;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.dsmcc.MetaInfo.Dii;
import ipaneltv.toolkit.dsmcc.MetaInfo.Dsi;
import ipaneltv.toolkit.dsmcc.MetaInfo.Module;
import ipaneltv.toolkit.dsmcc.MetaInfo.Pmt;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class MetaBuilder {
	static final String TAG = "[cb]MetaBuilder";
	static SAXParserFactory factory;
	static {
		factory = SAXParserFactory.newInstance();
	}
	String uuid;
	long freq;
	SAXParser parser = null;
	XMLReader reader;

	public MetaBuilder(String uuid, long freq) {
		this.uuid = uuid;
		this.freq = freq;
	}

	public XMLReader getXmlReader() {
		synchronized (TAG) {
			if (parser == null) {
				try {
					parser = factory.newSAXParser();
					reader = parser.getXMLReader();
				} catch (Exception e) {
					IPanelLog.e(TAG, "getXmlReader error:" + e);
				}
			}
			return reader;
		}
	}

	public MetaInfo.Pmt parsePmt(int pid, XMLReader reader, InputSource is) {
		if (reader == null || is == null)
			throw new NullPointerException();
		if (pid < 0 || pid > 8192)
			throw new IllegalArgumentException("bad pid");
		Pmt pmt = new Pmt(uuid, freq, pid);
		PmtMetaHandler h = new PmtMetaHandler();
		if (h.parse(reader, is, pmt))
			return pmt;
		return null;
	}

	public MetaInfo.Dsi parseDsi(int pid, XMLReader reader, InputSource is) {
		if (reader == null || is == null)
			throw new NullPointerException();
		if (pid < 0 || pid > 8192)
			throw new IllegalArgumentException("bad pid");
		Dsi dsi = new Dsi(uuid, freq, pid);
		DsiMetaHandler h = new DsiMetaHandler();
		if (h.parse(reader, is, dsi))
			return dsi;
		return null;
	}

	public MetaInfo.Dii parseDii(int pid, XMLReader reader, InputSource is) {
		if (reader == null || is == null)
			throw new NullPointerException();
		if (pid < 0 || pid > 8192)
			throw new IllegalArgumentException("bad pid");
		Dii dii = new Dii(uuid, freq, pid);
		DiiMetaHandler h = new DiiMetaHandler();
		if (h.parse(reader, is, dii))
			return dii;
		return null;
	}

	public MetaInfo.Module parseModule(int pid, XMLReader reader, InputSource is) {
		if (reader == null || is == null)
			throw new NullPointerException();
		if (pid < 0 || pid > 8192)
			throw new IllegalArgumentException("bad pid");
		Module mod = new Module(uuid, freq, pid);
		ModuleMetaHandler h = new ModuleMetaHandler();
		if (h.parse(reader, is, mod))
			return mod;
		return null;
	}

	public MetaInfo.Pmt parsePmt(int pid, InputSource is, int len) {

		return parsePmt(pid, getXmlReader(), is);
	}

	public MetaInfo.Dsi parseDsi(int pid, InputSource is) {
		return parseDsi(pid, reader, is);
	}

	public MetaInfo.Dii parseDii(int pid, InputSource is) {
		return parseDii(pid, reader, is);
	}

	public MetaInfo.Module parseModule(int pid, InputSource is) {
		return parseModule(pid, reader, is);
	}
}
