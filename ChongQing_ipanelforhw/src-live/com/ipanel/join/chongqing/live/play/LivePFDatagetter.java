package com.ipanel.join.chongqing.live.play;

import android.content.Context;
import android.util.Log;
public class LivePFDatagetter extends AllPFDataGetter{
	private final static String TAG=LivePFDatagetter.class.getName();
	PFListener l;
	public LivePFDatagetter(Context context, String uuid, int flags){
		super(context, uuid, flags);
	}

	/**
	 * 拿到pf数据，该方法为主线程调用  可直接更新界面。
	 * 
	 * @param pf pf信息
	 */
	@Override
	protected void onProgramsFound(PresentAndFollow pf, int flags) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onProgramsFound 22 start....l = "+ l);
		super.onProgramsFound(pf, flags);
		PFListener listener = l;
		if(listener != null){
			listener.onPfInfoUpdated(pf);	
		}
		Log.d(TAG,"onProgramsFound end....");
	}
	
	@Override
	protected int getMaxSectionBufferNumber(){
		return 300;
	}
	
	public void setListener(PFListener l){
		this.l=l;
	}
	/*
	 * 监听PF数据的获得  之后更新
	 */
	public static interface  PFListener{
		void onPfInfoUpdated(PresentAndFollow pf);
	}
	

}
