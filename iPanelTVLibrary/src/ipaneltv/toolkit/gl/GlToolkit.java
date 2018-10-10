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
			gl.glDeleteTextures(1, textures, 0); // �ͷ�����ID
		}
		gl.glGenTextures(1, textures, 0); // ��ϵͳ������õ�����ID
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]); // ������
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST); // ����Ŵ����ֵ
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR); // ������С����ֵ
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0); // ָ����������
		return textures;
	}

	/**
	 * ����Float������
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
	 * ����ʵ�ʵ�����λ��Xת���ɵ�ǰ3D͸����0,0����ϵ��Translate Xֵ
	 * 
	 * @param x
	 * @return
	 */
	public static float translateX(float x) {
		return (GlSurfaceWindow.RIGHT + GlSurfaceWindow.RIGHT)
				/ (GlSurfaceWindow.FULL_SCREEN_WIDTH / x);
	}

	/**
	 * ����ʵ�ʵ�����λ��Yת���ɵ�ǰ3D͸����0,0����ϵ��Translate Yֵ
	 * 
	 * @param y
	 * @return
	 */
	public static float translateY(float y) {
		return (GlSurfaceWindow.TOP + GlSurfaceWindow.TOP)
				/ (GlSurfaceWindow.FULL_SCREEN_HEIGHT / y);
	}

	/**
	 * ����ʵ�ʵ����ؿ��ת���ɶ�Ӧ��ǰ3D͸����Ŀ�
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
	 * ����ʵ�ʵ����ظ߶�ת���ɶ�Ӧ��ǰ3D͸����ĸ�
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