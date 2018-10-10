package ipanel.join.ad.widget;

import java.util.List;

public class StreamAd {
	public String x;
	public String y;
	public String width;
	public String height;
	
	public String id;
	public String groupId;
	public StreamAds streamAds;
	
	public static class StreamAds {
		public List<ImageEntry> imgEntrys;
		public String content;
	}
	
	public static class ImageEntry{
		public String src;
		public int pauseTime;
	}
}
