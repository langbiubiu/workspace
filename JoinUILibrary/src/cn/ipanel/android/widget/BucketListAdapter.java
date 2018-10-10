package cn.ipanel.android.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * A bucket adapter presenting rows of buckets.
 * 
 * @author Scythe
 * 
 * @param <T>
 */
public abstract class BucketListAdapter<T> extends BaseAdapter
{

    protected List<T>  elements = new ArrayList<T>();
    protected Activity ctx;
    protected Integer  bucketSize;

    /**
     * Basic constructor, takes an Activity context and the list of elements.
     * Assumes a 1 column view by default.
     * 
     * @param ctx
     *            The Activity context.
     * @param elements
     *            The list of elements to present.
     */
    public BucketListAdapter(Activity ctx)
    {
        this(ctx, 1);
    }

    /**
     * Extended constructor, takes an Activity context, the list of elements and
     * the exact number of columns.
     * 
     * @param ctx
     *            The Activity context.
     * @param elements
     *            The list of elements to present.
     * @param bucketSize
     *            The exact number of columns.
     * 
     */
    public BucketListAdapter(Activity ctx, Integer bucketSize)
    {
        this.ctx = ctx;
        this.bucketSize = bucketSize;
    }
    
    public void addItems(Collection<T> items){
        this.elements.addAll(items);
    }

    public void setItems(List<T> items)
    {
        this.elements = items;
        notifyDataSetChanged();
    }
    
    public void clear(){
        this.elements.clear();
        notifyDataSetChanged();
    }
    
    public int getRealCount(){
        return elements.size();
    }
    
    public T getLastElement(){
        if(elements.size() == 0)
            return null;
        return elements.get(elements.size()-1);
    }

    public void setBucketSize(int i)
    {
        bucketSize = Math.max(1, i);
        notifyDataSetChanged();
    }

    /**
     * Calculates the required number of columns based on the actual screen
     * width (in DIP) and the given minimum element width (in DIP).
     * 
     * @param minBucketElementWidthDip
     *            The minimum width in DIP of an element.
     */
    public void enableAutoMeasure(float minBucketElementWidthDip)
    {
        float screenWidth = getScreenWidthInDip();

        if (minBucketElementWidthDip >= screenWidth)
        {
            bucketSize = 1;
        } else
        {
            bucketSize = (int) (screenWidth / minBucketElementWidthDip);
        }
    }

    @Override
    public int getCount()
    {
        return (elements.size() + bucketSize - 1) / bucketSize;
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int bucketPosition, View convertView, ViewGroup parent)
    {
        ViewGroup bucket = (ViewGroup) convertView;
        boolean createView = false;

        if (bucket == null || bucket.getChildCount() != bucketSize || !"bucket-layout".equals(bucket.getTag()))
        {
            if(bucketSize != 1){
                bucket = new LinearLayout(ctx);
                bucket.setTag("bucket-layout");
            }else
                bucket = null;
            createView = true;
        }
        
        if(bucketSize == 1)
            return getBucketElement(bucket, bucketPosition, elements.get(bucketPosition), bucket);
        
        for (int i = 0; i < bucketSize; i++)
        {
            FrameLayout bucketElementFrame;
            if (createView)
            {
                bucketElementFrame = new FrameLayout(ctx);
                bucketElementFrame.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            } else
            {
                bucketElementFrame = (FrameLayout) bucket.getChildAt(i);
            }
            int position = bucketPosition * bucketSize + i;
            if (position < elements.size())
            {
                View cv = bucketElementFrame.getChildAt(0);
                View current = getBucketElement(cv, position, elements.get(position), bucketElementFrame);
                if (cv != null)
                {
                    bucketElementFrame.removeAllViews();
                }
                bucketElementFrame.addView(current);
            } else
            {
                bucketElementFrame.removeAllViews();
            }
            if (createView)
                bucket.addView(bucketElementFrame);
        }

        return bucket;
    }

    /**
     * Extending classes should return a bucket-element with this method. Each
     * row in the list contains bucketSize total elements.
     * 
     * @param position
     *            The absolute, global position of the current item.
     * @param currentElement
     *            The current element for which the View should be constructed
     * @return The View that should be presented in the corresponding bucket.
     */
    protected abstract View getBucketElement(View convertView, final int position, T currentElement, ViewGroup parent);

    protected float getScreenWidthInDip()
    {
        WindowManager wm = ctx.getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int screenWidth_in_pixel = dm.widthPixels;
        float screenWidth_in_dip = screenWidth_in_pixel / dm.density;

        return screenWidth_in_dip;
    }
}
