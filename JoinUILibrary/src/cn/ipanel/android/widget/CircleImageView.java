package cn.ipanel.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Bitmap will be displayed in a circle
 * @author Zexu
 *
 */
public class CircleImageView extends ImageView
{
    public CircleImageView(Context context)
    {
        this(context, null);
    }

    public CircleImageView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth()));
        updateShaderMatrix();
    }

    BitmapShader   shader;
    Paint          mPaint = new Paint();
    private Matrix mShaderMatrix;
    float          scale;
    float          dx;
    float          dy;
    private int    mBitmapWidth;
    private int    mBitmapHeight;

    @Override
    public void setImageDrawable(Drawable drawable)
    {
        super.setImageDrawable(drawable);
        setupShader(drawable);
    }

    @Override
    public void setImageResource(int resId)
    {
        super.setImageResource(resId);
        setupShader(getDrawable());
    }

    @Override
    public void setPressed(boolean pressed)
    {
        if (isClickable())
        {
            if (pressed)
            {
                mPaint.setColorFilter(new PorterDuffColorFilter(0x33000000, PorterDuff.Mode.LIGHTEN));
            } else
            {
                mPaint.setColorFilter(null);
            }
            invalidate();
        }
        super.setPressed(pressed);
    }
    
    private void setupShader(Drawable drawable)
    {
        if (drawable instanceof BitmapDrawable)
        {
            Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
            if (bmp != null)
            {
                shader = new BitmapShader(bmp, TileMode.CLAMP, TileMode.CLAMP);
                mPaint.setShader(shader);
                mPaint.setAntiAlias(true);
                mBitmapWidth = bmp.getWidth();
                mBitmapHeight = bmp.getHeight();
                updateShaderMatrix();
            }
        }
    }

    private void updateShaderMatrix()
    {
        if (mBitmapWidth == 0 || mBitmapHeight == 0 || getMeasuredWidth() == 0 || getMeasuredHeight() == 0)
            return;
        if (mShaderMatrix == null)
            mShaderMatrix = new Matrix();
        else
            mShaderMatrix.set(null);
        dx = 0;
        dy = 0;

        if (mBitmapWidth * getMeasuredHeight() > getMeasuredWidth() * mBitmapHeight)
        {
            scale = getMeasuredHeight() / (float) mBitmapHeight;
            dx = (getMeasuredWidth() - mBitmapWidth * scale) * 0.5f;
        } else
        {
            scale = getMeasuredWidth() / (float) mBitmapWidth;
            dy = (getMeasuredHeight() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        shader.setLocalMatrix(mShaderMatrix);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        Drawable maiDrawable = getDrawable();
        if (maiDrawable instanceof BitmapDrawable && shader != null)
        {
            Bitmap bmp = ((BitmapDrawable) maiDrawable).getBitmap();
            if (bmp != null)
            {
                float radius = getWidth() / 2f;
                canvas.drawCircle(radius, radius, radius, mPaint);
                if(isClickable() && isPressed()){
                    canvas.drawColor(0x33000000, PorterDuff.Mode.SRC_ATOP);
                }
            } else
            {
                super.onDraw(canvas);
            }

        } else
        {
            super.onDraw(canvas);
        }
    }
}
