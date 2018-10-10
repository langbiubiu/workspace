/**
 * GlTexture.java
 * TODO
 */
package ipaneltv.toolkit.gl;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public abstract class GlObject {

	private static final float[] COORDS = new float[] { 0.f, 0.f, 0.f, 1.f,
			1.f, 0.f, 1.f, 1.f };

	protected float width;
	protected float height;
	protected FloatBuffer mVertexBuf;
	protected FloatBuffer mTextureBuf;

	protected Object mutex = new Object();

	public GlObject(int w, int h) {
		this(GlToolkit.translateWidth(w), GlToolkit.translateHeight(h));
	}

	public GlObject(float w, float h) {
		width = w;
		height = h;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public abstract boolean onDraw(GL10 gl);

	protected void clear() {
		if (mVertexBuf == null)
			return;
		mVertexBuf.clear();
		mVertexBuf = null;
	}

	protected final FloatBuffer getVetreBuffer() {
		if (mVertexBuf != null)
			return mVertexBuf;
		float[] vertexs = new float[] { 0.f, 0.f, 0.f, // 左上点
				0.f, height, 0.f, // 左下点
				width, 0.f, 0.f, // 右上点
				width, height, 0.f // 右下点
		};
		mVertexBuf = GlToolkit.createFloatBuffer(vertexs.length << 2);
		mVertexBuf.put(vertexs);
		mVertexBuf.position(0);
		return mVertexBuf;
	}

	protected final FloatBuffer getTextureBuffer() {
		if (mTextureBuf != null)
			return mTextureBuf;
		mTextureBuf = GlToolkit.createFloatBuffer(COORDS.length << 2);
		mTextureBuf.put(COORDS);
		mTextureBuf.position(0);
		return mTextureBuf;
	}
}