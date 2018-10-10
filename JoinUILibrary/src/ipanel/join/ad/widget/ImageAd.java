package ipanel.join.ad.widget;

import java.util.List;

public class ImageAd {
	public String x;
	public String y;
	public String width;
	public String height;
	
	public String id;
	public String groupId;
	public ImageAds imgAds;
	
	public static class ImageAds {
		public List<ImageEntry> imgEntrys;
		public String extendsUrl;
	}
	
	public static class ImageEntry{
		public String src;
		public int pauseTime;
	}
}
