package ipaneltv.toolkit.gl;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class GlImage extends GlObject {

	protected int[] textures = new int[] { -1 };

	Context mContext;
	volatile Bitmap mToLoad = null;
	Bitmap mBitmap;

	public GlImage(Context ctx, int w, int h, Config config) {
		super(w, h);
		mContext = ctx;
		mBitmap = Bitmap.createBitmap(w, h, config);
		if (mBitmap == null)
			throw new OutOfMemoryError();
	}

	public Object getMutex() {
		return mutex;
	}

	public Canvas getCanvas() {
		return new Canvas(mBitmap);
	}

	private void zoom(Bitmap bmp) {
		// TODO linyi getCanvas().drawBitmap(bmp);
	}

	public void setImage(int rsid) {
		Bitmap bmp = BitmapFactory
				.decodeResource(mContext.getResources(), rsid);
		setImage(bmp);
	}

	public void relead() {
		mToLoad = mBitmap;
	}

	public void setImage(Bitmap bmp) {
		synchronized (mutex) {
			if (bmp.getWidth() != mBitmap.getWidth()
					|| bmp.getHeight() != mBitmap.getHeight()) {
				zoom(bmp);
			} else {
				mBitmap.recycle();
				mBitmap = bmp;
			}
		}
	}

	public void recycle(GL10 gl) {
		synchronized (mutex) {
			if (textures[0] >= 0)
				gl.glDeleteTextures(1, textures, 0);
			textures[0] = -1;
			if (mBitmap != null) {
				mBitmap.recycle();
				mBitmap = null;
			}
		}
	}

	public boolean onDraw(GL10 gl) {
		synchronized (mutex) {
			if (mToLoad != null) {
				textures = GlToolkit.bindTexture(gl, mToLoad, textures);
				mToLoad = null;
			}
			if (textures[0] == -1)
				return false;
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, getVetreBuffer());
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, getTextureBuffer());
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
			return false;
		}
	}
}