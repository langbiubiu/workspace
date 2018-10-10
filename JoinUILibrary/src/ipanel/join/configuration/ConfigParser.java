package ipanel.join.configuration;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ConfigParser {
	public static ConfigParser sParser = new ConfigParser();
	
	XmlPullParser mParser;

	private ConfigParser() {
		try {
			mParser = XmlPullParserFactory.newInstance().newPullParser();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Configuration parse(InputStream is) throws XmlPullParserException,
			IOException {
		try {
			mParser.setInput(is, "UTF-8");
			int eventType = mParser.next();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG
						&& 0 == Configuration.ITEM_TAG.compareTo(mParser
								.getName())) {
					Configuration config = new Configuration();
					config.loadData(mParser);

					return config;
				}

				eventType = mParser.next();
			}
			return null;
		} finally {
			mParser.setInput(null);
			if (is != null)
				is.close();
		}
	}
}
