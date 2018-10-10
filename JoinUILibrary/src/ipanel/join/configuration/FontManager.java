package ipanel.join.configuration;

import java.util.HashMap;
import java.util.Map;

import android.widget.TextView;

/**
 * Manage loaded font
 * 
 * @author Zexu
 *
 */
public class FontManager {
	private Map<String, CustomFont> fontMap = new HashMap<String, CustomFont>();
	private static FontManager sMe = new FontManager();

	public static FontManager getMe() {
		return sMe;
	}

	/**
	 * 
	 * @param name
	 *            Unique Identifier
	 * @param path
	 *            asset path or file absolute path to ttf/otf
	 */
	public void registerFont(String name, String path) {
		if (name != null && path != null) {
			synchronized (fontMap) {
				fontMap.put(name, new CustomFont(name, path));
			}
		}
	}

	public void removeFont(String name) {
		synchronized (fontMap) {
			fontMap.remove(name);
		}
	}

	public void removeAllFonts() {
		synchronized (fontMap) {
			fontMap.clear();
		}
	}

	public CustomFont getFont(String name) {
		return fontMap.get(name);
	}

	public void applyFontTo(String fontName, android.view.View view) {
		CustomFont font = getFont(fontName);
		if (font != null) {
			if (view instanceof TextView)
				font.applyTypeface((TextView) view);
		}
	}

	/**
	 * apply font to all TextView in a layout
	 * 
	 * @param fontName
	 * @param view
	 */
	public void applyFontToAll(String fontName, android.view.View view) {
		CustomFont font = getFont(fontName);
		if (font != null) {
			font.applyToAll(view);
		}
	}
}
