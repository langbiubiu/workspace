package ipaneltv.toolkit.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

public final class GlToolkit {

	static final String TAG = "GlToolkit";

	// ------------------------------------------------------------------------------------------------------
	// //
	// ----------------------------------|
	// |----------------------------------------- //
	// ----------------------------------| Public Function
	// |----------------------------------------- //
	// ----------------------------------|
	// |----------------------------------------- //
	// ------------------------------------------------------------------------------------------------------
	// //


	public static int[] bindTexture(GL10 gl, Bitmap bitmap, int[] textures) {
		if (textures == null) {
			textures = new int[] { -1 };
		}
		if (textures[0] > 0) {
			gl.glDeleteTextures(1, textures, 0); // 释放纹理ID
		}
		gl.glGenTextures(1, textures, 0); // 向系统申请可用的纹理ID
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]); // 绑定纹理
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST); // 纹理放大采样值
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR); // 纹理缩小采样值
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0); // 指定纹理数据
		return textures;
	}

	/**
	 * 创建Float缓存区
	 * 
	 * @param length
	 * @return
	 */
	public static FloatBuffer createFloatBuffer(int length) {
		ByteBuffer vBuffer = ByteBuffer.allocateDirect(length);
		vBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer buf = vBuffer.asFloatBuffer();
		return buf;
	}

	// ------------------------------------------------------------------------------------------------------
	// //
	// --------------------------------|
	// |---------------------------------------- //
	// --------------------------------| Protected Function
	// |---------------------------------------- //
	// --------------------------------|
	// |---------------------------------------- //
	// ------------------------------------------------------------------------------------------------------
	// //
	/**
	 * 根据实际的像素位置X转换成当前3D透视体0,0坐标系的Translate X值
	 * 
	 * @param x
	 * @return
	 */
	public static float translateX(float x) {
		return (GlSurfaceWindow.RIGHT + GlSurfaceWindow.RIGHT)
				/ (GlSurfaceWindow.FULL_SCREEN_WIDTH / x);
	}

	/**
	 * 根据实际的像素位置Y转换成当前3D透视体0,0坐标系的Translate Y值
	 * 
	 * @param y
	 * @return
	 */
	public static float translateY(float y) {
		return (GlSurfaceWindow.TOP + GlSurfaceWindow.TOP)
				/ (GlSurfaceWindow.FULL_SCREEN_HEIGHT / y);
	}

	/**
	 * 根据实际的像素宽度转换成对应当前3D透视体的宽
	 * 
	 * @param width
	 * @return
	 */
	protected static float translateWidth(float width) {
		return width
				/ (GlSurfaceWindow.FULL_SCREEN_WIDTH / (Math
						.abs(GlSurfaceWindow.LEFT) + GlSurfaceWindow.RIGHT));
	}

	/**
	 * 根据实际的像素高度转换成对应当前3D透视体的高
	 * 
	 * @param height
	 * @return
	 */
	protected static float translateHeight(float height) {
		return height
				/ (GlSurfaceWindow.FULL_SCREEN_HEIGHT / (GlSurfaceWindow.TOP + Math
						.abs(GlSurfaceWindow.BOTTOM)));
	}
	// ------------------------------------------------------------------------------------------------------
	// //
}