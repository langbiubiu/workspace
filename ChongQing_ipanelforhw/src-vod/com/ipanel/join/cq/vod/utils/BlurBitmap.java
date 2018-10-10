package com.ipanel.join.cq.vod.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
/**
 * 高斯模糊
 */
public class BlurBitmap {
	
	//模糊图片方法
	public static void blur(Bitmap bkg, View view) {
        long startMs = System.currentTimeMillis();
        float radius = 25;
        Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth()),
                (int) (view.getMeasuredHeight()), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft(), -view.getTop());
        Paint paint = new Paint();  
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        paint.setAntiAlias(true);
        canvas.drawBitmap(bkg, 0, 0, paint);
        //使用RenderScript
        RenderScript rs = RenderScript.create(view.getContext());
        Allocation overlayAlloc = Allocation.createFromBitmap(
                rs, overlay);
        
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(
                rs, overlayAlloc.getElement());
 
        blur.setInput(overlayAlloc);
 
        blur.setRadius(radius);
 
        blur.forEach(overlayAlloc);
 
        overlayAlloc.copyTo(overlay);
 
        view.setBackground(new BitmapDrawable(
        		view.getContext().getResources(), overlay));
 
        rs.destroy();
        Logger.d(System.currentTimeMillis() - startMs + "ms");
    }
	
	public static void blur(Bitmap bkg, View view,float scaleFactor) {
		if(bkg == null || view == null)
			return;
        long startMs = System.currentTimeMillis();
        float radius = 20;
        Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth()/scaleFactor),
                (int) (view.getMeasuredHeight()/scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft()/scaleFactor, -view.getTop()/scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();  
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);
        //使用RenderScript
        RenderScript rs = RenderScript.create(view.getContext());
        Allocation overlayAlloc = Allocation.createFromBitmap(
                rs, overlay);
        
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(
                rs, overlayAlloc.getElement());
 
        blur.setInput(overlayAlloc);
 
        blur.setRadius(radius);
 
        blur.forEach(overlayAlloc);
 
        overlayAlloc.copyTo(overlay);
 
        view.setBackground(new BitmapDrawable(
        		view.getContext().getResources(), overlay));
 
        rs.destroy();
        Logger.d(System.currentTimeMillis() - startMs + "ms");
    }
}
