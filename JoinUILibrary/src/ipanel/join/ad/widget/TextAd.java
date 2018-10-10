package ipanel.join.ad.widget;

import java.util.List;

public class TextAd {
	public static final String TYPE_RIGHT_TO_LEFT = "left";
	public static final String TYPE_BOTTOM_TO_TOP = "up";
	
	public String x;
	public String y;
	public String width;
	public String height;
	
	public String id;
	public FlyAds flyads;
	
	public static class FlyAds {
		public List<FlyEntry> flyEntrys;
		public String extendsUrl;
		public int speed;
		public String fontColor;
		public String fontSize;
		/**
		 * "left", "up"
		 */
		public String runType;
		public String backgroundImage;
		public String backgroundColor;
		public String transparency;
		public int delay;
	}
	
	public static class FlyEntry{
		public String content;
		public int cycles;
	}
}
