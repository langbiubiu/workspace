package cn.ipanel.dlna;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

public class IconFont {
	private static final String FONT_FILE = "FontAwesome.otf";
	
	private static Typeface sFontFace;
	
	public static void applyTo(View tv){
		if(tv instanceof TextView)
			applyTo((TextView)tv);
	}
	
	public static void applyTo(TextView tv){
		if(tv == null)
			return;
		if(sFontFace == null)
			sFontFace = Typeface.createFromAsset(tv.getResources().getAssets(), FONT_FILE);
		
		tv.setTypeface(sFontFace);
	}

}
