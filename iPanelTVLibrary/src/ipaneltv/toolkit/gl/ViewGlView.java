package ipaneltv.toolkit.gl;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.view.View;

public class ViewGlView extends GlView {
	View view;
	GlImage image;

	public ViewGlView(Context ctx, View v, int width, int height) {
		image = new GlImage(ctx, width, height, Config.ARGB_8888);
	}

	public View getView() {
		return view;
	}

	public void updateView() {
		synchronized (image.getMutex()) {
			view.draw(image.getCanvas());
			image.relead();
		}
	}

	public void setLocation(int x, int y) {
		setBound(x, y, image.width, image.height);
	}

}
