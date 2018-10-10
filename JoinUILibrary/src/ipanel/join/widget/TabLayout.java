package ipanel.join.widget;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import cn.ipanel.android.Logger;

import ipanel.join.configuration.Action;
import ipanel.join.configuration.Bind;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.View;
import ipanel.join.widget.ActionUtils.FragmentAnimationTag;
import android.content.Context;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;

public class TabLayout extends LinearLayout {
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
		 * @param child
		 *            selected item view
		 */
		public void onTabChange(int index, android.view.View child);
	}
	
	private OnTabChangeListener mTabChangeListener;

	public void setOnTabChangeListener(OnTabChangeListener listener) {
		this.mTabChangeListener = listener;
	}
	
	public int mIndex = -1;
	
	FragmentAnimationTag left;
	FragmentAnimationTag right;
	public TabLayout(Context context, View data) {
		super(context, data);
		Bind bind = data.getBindByName("animationLeft");
		if (bind != null) {
			try {
				JSONObject json = new JSONObject(bind.getValue().getvalue());
				left = new FragmentAnimationTag(
						getResources().getIdentifier(json.getString("in"),
								"anim", context.getPackageName()),
						getResources().getIdentifier(json.getString("out"),
								"anim", context.getPackageName()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		bind = data.getBindByName("animationRight");
		if (bind != null) {
			try {
				JSONObject json = new JSONObject(bind.getValue().getvalue());
				right = new FragmentAnimationTag(
						getResources().getIdentifier(json.getString("in"),
								"anim", context.getPackageName()),
						getResources().getIdentifier(json.getString("out"),
								"anim", context.getPackageName()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		bind = data.getBindByName("onTabChangeListener");
		if(bind != null && bind.matchTarget(data.getId())){
			try{
				ClassLoader classLoader = ConfigState.getInstance().getClassLoader();
				if(classLoader == null)
					classLoader = getContext().getClassLoader();
				Class<?> clazz = classLoader.loadClass(bind.getValue().getvalue());
				setOnTabChangeListener((OnTabChangeListener) clazz.newInstance());
			}catch(Exception e){
				e.printStackTrace();
			}
		}		
		setFocusable(true);
		setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
		getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {
			
			@Override
			public void onGlobalFocusChanged(android.view.View oldFocus,
					android.view.View newFocus) {
				Logger.d("oldFoucs "+oldFocus+" newFocus "+newFocus);
				if(newFocus == TabLayout.this){
					android.view.View child = getChildAt(mIndex == -1? 0:mIndex);
					if(child != null)
						child.requestFocus();
				}else if(newFocus != null && newFocus.getParent() == TabLayout.this){
					switchIndex(newFocus);
				}
				
			}
		});
	}
	
	public boolean isSelectedView(android.view.View view){
		if(view==null){
			return false;
		}
		return mIndex==indexOfChild(view);
		
	}
	
	public int getSelectIndex(){
		return mIndex;
	}
	
	public void switchIndex(android.view.View child){
		int idx = this.indexOfChild(child);
		if(idx != -1 && idx != mIndex){
			getChildAt(mIndex).setSelected(false);
			child.setSelected(true);
			//save old tag
			Object tag = child.getTag();
			if(idx > mIndex){
				child.setTag(right);
			} else {
				child.setTag(left);
			}
			if(child instanceof IConfigView){
				((IConfigView) child).onAction(Action.EVENT_ONSELECT);
			}
			//resume old tag
			child.setTag(tag);
			mIndex = idx;
			if (mTabChangeListener != null)
				mTabChangeListener.onTabChange(mIndex, child);

		}
	}
	
	@Override
	public void addView(android.view.View child) {
		super.addView(child);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Logger.d("TabLayout.onAttachedToWindow childCount:"+getChildCount());
		if (mIndex == -1 && getChildCount() > 0)
			mIndex = 0;
		if(mIndex != -1 && mIndex<getChildCount()){
			android.view.View child = getChildAt(mIndex);
			child.setSelected(true);
			if(child instanceof IConfigView){
				((IConfigView) child).onAction(Action.EVENT_ONSELECT);
			}
			if (mTabChangeListener != null)
				mTabChangeListener.onTabChange(mIndex, child);
		}
		if(getChildCount() >0){
			android.view.View child = getChildAt(0);
			if(getOrientation() == HORIZONTAL)
				child.setNextFocusLeftId(child.getId());
		}
	}

	@Override
	public void addFocusables(ArrayList<android.view.View> views, int direction, int focusMode) {
		if(getFocusedChild() == null){
			android.view.View child = getChildAt(mIndex);
			if(child != null && child.hasFocusable()){
				views.add(child);
				return;
			}
		}
		super.addFocusables(views, direction, focusMode);
	}
	

}
