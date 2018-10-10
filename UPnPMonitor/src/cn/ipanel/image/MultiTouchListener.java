package cn.ipanel.image;

import cn.ipanel.dlna.Logger;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class MultiTouchListener implements OnTouchListener {   
    Matrix matrix = new Matrix();   
    Matrix savedMatrix = new Matrix();   
    static final int NONE = 0;   
    static final int DRAG = 1;   
    static final int ZOOM = 2;   
    int mode = NONE;   
    PointF start = new PointF();   
    PointF mid = new PointF();   
    float oldDist = 1f;   
    @Override   
    public boolean onTouch(View v, MotionEvent event) {   
            ImageView view = (ImageView) v;   
            switch (event.getAction() & MotionEvent.ACTION_MASK) {   
            case MotionEvent.ACTION_DOWN:   
                    matrix.set(view.getImageMatrix());   
                    savedMatrix.set(matrix);   
                    start.set(event.getX(), event.getY());   
                    mode = DRAG;   
                     
                    break;   
            case MotionEvent.ACTION_POINTER_DOWN:   
                    oldDist = spacing(event);   
                    if (oldDist > 10f) {   
                            savedMatrix.set(matrix);   
                            midPoint(mid, event);   
                            mode = ZOOM;   
                    }   
                    break;   
            case MotionEvent.ACTION_UP:   
            case MotionEvent.ACTION_POINTER_UP:   
                    mode = NONE;   
                    break;   
            case MotionEvent.ACTION_MOVE:   
                    if (mode == DRAG) {   
                            matrix.set(savedMatrix);   
                            matrix.postTranslate(event.getX() - start.x, event.getY()   
                                            - start.y);   
                    } else if (mode == ZOOM) {   
                            float newDist = spacing(event);   
                            if (newDist > 10f) {   
                                    matrix.set(savedMatrix);   
                                    float scale = normalizeScale(matrix, newDist / oldDist, view);   
                                    matrix.postScale(scale, scale, mid.x, mid.y);
                                    
                            }   
                    }   
                    break;   
            }
            view.setImageMatrix(matrix);   
            return true;   
    }
    
    private float normalizeScale(Matrix matrix, float rawF, ImageView iv){
    	Drawable d = iv.getDrawable();
    	
    	if(d == null || !(d instanceof BitmapDrawable))
    		return rawF;
    	int dw = d.getIntrinsicWidth();
    	int dh = d.getIntrinsicHeight();
    	
    	int vw = iv.getWidth();
    	int vh = iv.getHeight();
    	
    	float max = 4f*(vw+vh)/(dw+dh);
    	float min = Math.min(1, max/10);

    	float[] f = new float[9];
    	matrix.getValues(f);
    	float scale = f[Matrix.MSCALE_X];
    	
//    	Logger.d(String.format("%f %f %f", scale, min, max));
    	rawF = Math.min(max/scale, rawF);
    	rawF = Math.max(min/scale, rawF);
    	
    	
    	return rawF;
    }
      
     
    private float spacing(MotionEvent event) {   
            float x = event.getX(0) - event.getX(1);   
            float y = event.getY(0) - event.getY(1);   
            return FloatMath.sqrt(x * x + y * y);   
    }   
     
    private void midPoint(PointF point, MotionEvent event) {   
            float x = event.getX(0) + event.getX(1);   
            float y = event.getY(0) + event.getY(1);   
            point.set(x / 2, y / 2);   
    }   
}   