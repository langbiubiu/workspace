package ipanel.join.configuration;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Handle custom font
 * 
 * @author Zexu
 *
 */
public class CustomFont {
	private final String name;
	private final String path;

	/**
	 * 
	 * @param name
	 *            unique name for this font
	 * @param path
	 *            asset path or absolute file path to the ttf/otf font file
	 */
	public CustomFont(String name, String path) {
		this.name = name;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	private Typeface normal;
	
	public void applyToAll(android.view.View v) {
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				applyToAll(vg.getChildAt(i));
			}
		} else if (v instanceof TextView) {
			applyTypeface((TextView) v);
		}
	}

	public void applyTypeface(TextView v) {
		if (v.getTypeface() == null) {
			v.setTypeface(getNormal(v.getContext()));
			return;
		}
		switch (v.getTypeface().getStyle()) {
		case Typeface.BOLD:
			v.setTypeface(getNormal(v.getContext()));
			v.getPaint().setFakeBoldText(true);
			break;
		default:
			v.setTypeface(getNormal(v.getContext()));
			break;
		case Typeface.ITALIC:
			v.setTypeface(getNormal(v.getContext()));
			v.getPaint().setTextSkewX(-0.25f);
			break;
		case Typeface.BOLD_ITALIC:
			v.setTypeface(getNormal(v.getContext()));
			v.getPaint().setFakeBoldText(true);
			v.getPaint().setTextSkewX(-0.25f);
			break;
		}

	}

	public void applyTypeface(Context context, Paint v) {
		if (v.getTypeface() == null) {
			v.setTypeface(getNormal(context));
			return;
		}
		switch (v.getTypeface().getStyle()) {
		case Typeface.BOLD:
			v.setTypeface(getNormal(context));
			v.setFakeBoldText(true);
			break;
		default:
			v.setTypeface(getNormal(context));
			break;
		case Typeface.ITALIC:
			v.setTypeface(getNormal(context));
			v.setTextSkewX(-0.25f);
			break;
		case Typeface.BOLD_ITALIC:
			v.setTypeface(getNormal(context));
			v.setFakeBoldText(true);
			v.setTextSkewX(-0.25f);
			break;
		}
	}

	public synchronized Typeface getNormal(Context context) {
		if (normal == null)
			normal = loadFont(context.getAssets(), path);
		return normal;
	}

	public static Typeface loadFont(AssetManager am, String path) {
		try {
			if (path.startsWith("/"))
				return Typeface.createFromFile(path);
			Typeface tf = Typeface.createFromAsset(am, path);
			return tf;
		} catch (Exception e) {
			e.printStackTrace();
			return Typeface.DEFAULT;
		}
	}
}
