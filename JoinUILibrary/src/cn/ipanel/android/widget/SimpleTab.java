package cn.ipanel.android.widget;

import java.util.ArrayList;

import cn.ipanel.android.Logger;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.widget.LinearLayout;

/**
 * Simple linear layout wrapper behaves as a tab
 * 
 * @author Zexu
 * 
 */
public class SimpleTab extends LinearLayout {

	public SimpleTab(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);
		setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
		getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {

			@Override
			public void onGlobalFocusChanged(android.view.View oldFocus, android.view.View newFocus) {
				Logger.d("oldFoucs " + oldFocus + " newFocus " + newFocus);
				if (newFocus == SimpleTab.this) {
					android.view.View child = getChildAt(mIndex);
					if (child != null)
						child.requestFocus();
				} else if (newFocus != null && newFocus.getParent() == SimpleTab.this) {
					switchIndex(newFocus);
				}

			}
		});
	}

	/**
	 * Listener for tab switch
	 * 
	 * @author Zexu
	 * 
	 */
	public interface OnTabChangeListener {
		/**
		 * 
		 * @param index
		 *            index of the newly selected item
		 * @param selectedView
		 *            selected item view
		 */
		public void onTabChange(int index, View selectedView);
	}

	int mIndex = 0;
	
	public int getCurrentIndex(){
		return mIndex;
	}
	public void setCurrentIndex(int index){
		mIndex = index;
	}

	private OnTabChangeListener mTabChangeListener;

	public void setOnTabChangeListener(OnTabChangeListener listener) {
		this.mTabChangeListener = listener;
	}

	public SimpleTab(Context context) {
		super(context);
		setFocusable(true);
		setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
		getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {

			@Override
			public void onGlobalFocusChanged(android.view.View oldFocus, android.view.View newFocus) {
				Logger.d("oldFoucs " + oldFocus + " newFocus " + newFocus);
				if (newFocus == SimpleTab.this) {
					android.view.View child = getChildAt(mIndex);
					if (child != null)
						child.requestFocus();
				} else if (newFocus != null && newFocus.getParent() == SimpleTab.this) {
					switchIndex(newFocus);
				}

			}
		});
	}

	public void switchIndex(android.view.View child) {
		int idx = this.indexOfChild(child);
		if (idx != -1 && idx != mIndex) {
			getChildAt(mIndex).setSelected(false);
			child.setSelected(true);
			mIndex = idx;
			if (mTabChangeListener != null)
				mTabChangeListener.onTabChange(mIndex, child);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mIndex < getChildCount()) {
			android.view.View child = getChildAt(mIndex);
			child.setSelected(true);
			if (mTabChangeListener != null)
				mTabChangeListener.onTabChange(mIndex, child);
		}
		if (getChildCount() > 0) {
			android.view.View child = getChildAt(0);
			if (getOrientation() == HORIZONTAL)
				child.setNextFocusLeftId(child.getId());
		}
	}

	@Override
	public void addFocusables(ArrayList<android.view.View> views, int direction, int focusMode) {
		if (getFocusedChild() == null) {
			android.view.View child = getChildAt(mIndex);
			if (child != null && child.hasFocusable()) {
				views.add(child);
				return;
			}
		}
		super.addFocusables(views, direction, focusMode);
	}

}
