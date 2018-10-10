package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.View;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * 应用图标排列。<br/>
 * 设置显示几行几列<br/>
 * 设置应用图标之间的间距<br/>
 * 
 * @author liuf 20140516
 * 
 */
public class TileHLayout extends ViewGroup implements IConfigViewGroup {

	public static final String TAG = TileHLayout.class.getSimpleName();
	public static final String PROP_Y_SPACE = "ySpace";
	public static final String PROP_X_SPACE = "xSpace";
	public static final String PROP_Y_SIZE = "ySize";
	public static final String PROP_X_SIZE = "xSize";
	public static final String PROP_X_WIDTH = "xWidth";
	public static final String PROP_X_HEIGHT = "xHeight";
	public static final String PROP_DETAIL_APPID = "detail_appid_";
	private View mData;
	// 排列应用容器高宽
	private int layoutWidth, layoutHeight;
	// 应用图标高宽
	private int xWidth = 160, xHeight = 160;
	// 行列排列数
	private int xSize = 10, ySize = 2;
	// 应用图标的间距
	private int xSpace = 5, ySpace = 5;
	@SuppressWarnings("unused")
	private int pLeft, pRight, pTop, pBottom;
	// 渐变
	private int mAlphaStart = 0xAA000000;
	private int mAlphaEnd = 0x0;
	private int mAlphaGap = 0;
	// 画图
	private Paint paint;
	private Matrix vFlipMatrix;
	/** The maximum ratio of the height of the reflection to the source image. */
	private static final float MAX_REFLECTION_RATIO = 0.9F;

	public TileHLayout(Context context) {
		super(context);
		init();
	}

	/**
	 * 上下左右的距离
	 * 
	 * @param pLeft
	 * @param pTop
	 * @param pRight
	 * @param pBottom
	 */
	public void setLayoutPadding(int pLeft, int pTop, int pRight, int pBottom) {
		this.pLeft = pLeft;
		this.pTop = pTop;
		this.pRight = pRight;
		this.pBottom = pBottom;
	}

	/**
	 * 设置行列
	 * 
	 * @param xSize列
	 * @param ySize行
	 */
	public void setLayoutSize(int xSize, int ySize) {
		this.xSize = xSize;
		this.ySize = ySize;
	}

	/**
	 * 设置子控件高宽
	 * 
	 * @param width高
	 * @param height宽
	 */
	public void sethWidth(int width, int height) {
		this.xWidth = width;
		this.xHeight = height;
	}

	/**
	 * 设置子控件之间的间距
	 * 
	 * @param xSpace列间距
	 * @param ySpace行间距
	 */
	public void setSpace(int xSpace, int ySpace) {
		this.xSpace = xSpace;
		this.ySpace = ySpace;
	}

	/**
	 * 控件容器高宽
	 * 
	 * @param layoutWidth
	 * @param layoutHeight
	 */
	public void setLayoutHW(int layoutWidth, int layoutHeight) {
		this.layoutWidth = layoutWidth;
		this.layoutHeight = layoutHeight;
	}

	public void setAlphaStart(int mAlphaStart) {
		this.mAlphaStart = mAlphaStart;
		requestLayout();
	}

	public void setAlphaEnd(int mAlphaEnd) {
		this.mAlphaEnd = mAlphaEnd;
		requestLayout();
	}

	public void setAlphaGap(int mAlphaGap) {
		this.mAlphaGap = mAlphaGap;
		requestLayout();
	}

	/**
	 * 获取设置的一些参数
	 * 
	 * @param context
	 * @param data
	 */
	public TileHLayout(Context context, View data) {
		super(context);
		this.mData = data;
		PropertyUtils.setCommonProperties(this, data);

		Bind bind = data.getBindByName(PROP_X_SIZE);
		if (bind != null) {
			xSize = Integer.parseInt(bind.getValue().getvalue());
		}

		bind = data.getBindByName(PROP_Y_SIZE);
		if (bind != null) {
			ySize = Integer.parseInt(bind.getValue().getvalue());
		}

		bind = data.getBindByName(PROP_X_SPACE);
		if (bind != null) {
			xSpace = Integer.parseInt(bind.getValue().getvalue());
		}

		bind = data.getBindByName(PROP_Y_SPACE);
		if (bind != null) {
			ySpace = Integer.parseInt(bind.getValue().getvalue());
		}

		bind = data.getBindByName(PROP_X_WIDTH);
		if (bind != null) {
			xWidth = Integer.parseInt(bind.getValue().getvalue());
		}
		bind = data.getBindByName(PROP_X_HEIGHT);
		if (bind != null) {
			xHeight = Integer.parseInt(bind.getValue().getvalue());
		}
		init();
	}

	@Override
	public View getViewData() {
		return mData;
	}

	@Override
	public void onAction(String type) {
		ActionUtils.handleAction(this, mData, type);
	}

	private boolean mShowFocusFrame = false;

	@Override
	public boolean showFocusFrame() {
		return mShowFocusFrame;
	}

	@Override
	public void setShowFocusFrame(boolean show) {
		mShowFocusFrame = show;
	}

	@Override
	public LayoutParams genConfLayoutParams(View data) {
		Bind bd = data.getBindByName(PROPERTY_LAYOUT_PARAMS);
		if (bd != null) {
			try {
				JSONObject jobj = bd.getValue().getJsonValue();
				LayoutParams lp = new LayoutParams(
						PropertyUtils.getScaledSize(jobj.optInt("width",
								LayoutParams.WRAP_CONTENT)),
						PropertyUtils.getScaledSize(jobj.optInt("height",
								LayoutParams.WRAP_CONTENT)));
				return lp;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return generateDefaultLayoutParams();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (xSize <= 0 || ySize <= 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		int dw = 0;
		int dh = 0;
		if (layoutWidth != 0) {
			dw = Math.max(layoutWidth, 0);
		} else {
			dw = Math.max(getLayoutParams().width, 0);
		}
		if (layoutHeight != 0) {
			dh = Math.max(layoutHeight, 0);
		} else {
			dh = Math.max(getLayoutParams().height, 0);
		}
		setMeasuredDimension(dw, dh);
		Log.d(TAG, String.format("dw %d, dh %d", dw, dh));

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			android.view.View child = getChildAt(i);
			int mode = MeasureSpec.EXACTLY;
			int w = MeasureSpec.makeMeasureSpec(xWidth, mode);
			int h = MeasureSpec.makeMeasureSpec(xHeight, mode);
			child.measure(w, h);
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d(TAG, "onLayout("+changed+","+l+","+t+","+r+","+b+")");
		if (xSize <= 0 || ySize <= 0) {
			return;
		}

		// x * y matrix for greedy layout
		int[][] grid = new int[ySize][xSize];
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				int index = xSize * i + j;
				android.view.View child = getChildAt(index);
				if (child != null) {
					int left = pLeft + j * (xWidth + xSpace);
					int right = left + xWidth;
					int top = pTop + i * (xHeight + ySpace);
					int bottom = top + xHeight;
					child.layout(left, top, right, bottom);
					Log.d(TAG, String.format(
							"index %d,left %d, right %d,top %d, bottom %d",
							index, left, right, top, bottom));
				}
			}
		}
		setFlipMatrix(l, t, r, b);
	}

	/**
	 * 控件的高度与宽度
	 * 
	 * @param myView
	 */
	public void setUIControlWidthHeight(final android.view.View myView) {
		ViewTreeObserver vto = myView.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			public boolean onPreDraw() {
				myView.getViewTreeObserver().removeOnPreDrawListener(this);
				xHeight = myView.getMeasuredHeight();
				xWidth = myView.getMeasuredWidth();
				Rect rect = new Rect();
				myView.getHitRect(rect);
				Log.v(TAG, "height:" + xHeight + ",width:" + xWidth + ",rect:"
						+ rect);
				return true;
			}
		});
	}

	/**
	 * Initialises the layout.
	 */
	private void init() {
		// Ensures that we redraw when our children are redrawn.
		setAddStatesFromChildren(true);

		// Important to ensure onDraw gets called.
		setWillNotDraw(false);
		setDrawingCacheEnabled(true);

		// Create the paint object which we'll use to create the reflection
		// gradient
		paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		// Create a matrix which can flip images vertically
		vFlipMatrix = new Matrix();
	}

	private void setFlipMatrix(int l, int t, int r, int b) {
		Log.v(TAG, "setFlipMatrix("+l+","+t+","+r+","+b+")...");
		reflectionHeight = getReflectionHeight();
		int width = r - l;
		if (reflectionHeight > 0 && width > 0) {
			if (sourceBitmap == null
					|| sourceBitmap.getWidth() != getMeasuredWidth()
					|| sourceBitmap.getHeight() != reflectionHeight) {
				sourceBitmap = Bitmap.createBitmap(width, reflectionHeight,
						Bitmap.Config.ARGB_8888);
				tempCanvas = new Canvas(sourceBitmap);

				LinearGradient gradient = new LinearGradient(0, 0, 0,
						reflectionHeight, mAlphaStart, mAlphaEnd,
						TileMode.CLAMP);
				paint.setShader(gradient);
			}

			vFlipMatrix.reset();
			vFlipMatrix.postScale(1, -1);
			vFlipMatrix.postTranslate(0, getMaxChildBottom());
		}
	}

	int reflectionHeight;
	Bitmap sourceBitmap;
	Canvas tempCanvas;

	@Override
	protected void onDraw(Canvas canvas) {		
		super.onDraw(canvas);		
		Log.v(TAG, "onDraw()...");
		// Only actually do anything if there is space to actually draw a
		// reflection
		if (reflectionHeight > 0 && tempCanvas != null) {

			// Create a bitmap to hold the drawing of child views and pass this
			// to a temp canvas
			tempCanvas.drawColor(0, Mode.CLEAR);

			int count = tempCanvas.saveLayer(0, 0, canvas.getWidth(),
					canvas.getHeight(), null, Canvas.MATRIX_SAVE_FLAG
							| Canvas.CLIP_SAVE_FLAG
							| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
							| Canvas.FULL_COLOR_LAYER_SAVE_FLAG
							| Canvas.CLIP_TO_LAYER_SAVE_FLAG);
			// Draw the content of this layout onto our temporary canvas.
			tempCanvas.concat(vFlipMatrix);
			super.dispatchDraw(tempCanvas);
			tempCanvas.setMatrix(null);
			tempCanvas.drawPaint(paint);
			tempCanvas.restoreToCount(count);

			int childBottom = getMaxChildBottom();

			// Draw our image onto the canvas
			
			canvas.drawBitmap(sourceBitmap, 0, childBottom + mAlphaGap,
						null);
		}
	}

	/**
	 * Finds the bottom of the lowest view contained by this layout.
	 * 
	 * @return the bottom of the lowest view
	 */
	private int getMaxChildBottom() {
		int maxBottom = 0;
		for (int i = 0; i < getChildCount(); i++) {
			int bottom = getChildAt(i).getBottom();
			if (bottom > maxBottom)
				maxBottom = bottom;
		}
		return maxBottom;
	}

	/**
	 * Gets the height of the reflection to be drawn.
	 * 
	 * <p>
	 * This is the minimum of either:
	 * <ul>
	 * <li>The remaining height between the bottom of this layout and the
	 * bottom-most child view</li>
	 * <li>The maximum reflection ratio</li>
	 * </p>
	 * 
	 * @return the reflection height
	 */
	private int getReflectionHeight() {
		return (int) Math.min(getMeasuredHeight() - getMaxChildBottom(),
				getTotalChildHeight() * MAX_REFLECTION_RATIO);
	}

	/**
	 * Gets the height of the space covered by all children.
	 * 
	 * @return the total child height
	 */
	private int getTotalChildHeight() {
		// The max value of any child's "bottom" minus the minimum of any "top"
		return getMaxChildBottom() - getMinChildTop();
	}

	/**
	 * Gets the highest top edge of all contained views.
	 * 
	 * @return the min child top
	 */
	private int getMinChildTop() {
		int minTop = Integer.MAX_VALUE;
		for (int i = 0; i < getChildCount(); i++) {
			int top = getChildAt(i).getTop();
			if (top < minTop)
				minTop = top;
		}
		return minTop;
	}
}
