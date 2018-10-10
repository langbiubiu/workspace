package com.ipanel.join.chongqing.live;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.net.telecast.ca.CAManager;
import android.net.telecast.ca.CAManager.CACardStateListener;
import android.net.telecast.ca.CAManager.CAModuleStateListener;
import android.util.Log;

public class VODCACard {
	
	private String TAG="";
	private CAManager mCAManager;
	private String caId="";// 只能卡号
	
	boolean searchCard=false;
	private boolean caCard=false;
	Timer mTimer=new Timer();
	TimerTask caTask;
	TimerTask invaLidCATask;
	private String homeID=null;// homeID
	
	boolean isCAExisted=false;//判断是否插卡
	
	public void initCA(Context context)
	{
		mCAManager = CAManager.createInstance(context.getApplicationContext());
	
  		mCAManager.setCAModuleStateListener(mCAManagerListener);
		mCAManager.setCACardStateListener(mCACardStateListener);

		
		mCAManager.queryCurrentCAState();
		startgetCAIDTimer();
	}
	
	public boolean isCardValid(){
		return isCAExisted;
	}
	
	public String getCAID() {
		String msg ="nocard";
		try{
			int[] moduleIDs = mCAManager.getCAModuleIDs("UUID");//这里就是得到UUID的地方
			int moduleID = 0;
			
			if(moduleIDs!=null&&moduleIDs.length>0){
				moduleID = moduleIDs[0];
			}
		
			msg = mCAManager.getCAModuleProperty(moduleID,"CA_CARD_NUMBER");
			
			if(msg==null){

				msg="nocard";
			}else{
	
				caCard=true;
			}
		
		}catch(Exception e){
			msg="nocard";
		}
		
		return msg;
	}
	
	CACardStateListener mCACardStateListener=new CACardStateListener(){

		@Override
		public void onCardAbsent(int arg0) {
			// TODO Auto-generated method stub
			Log.v(TAG, "-----------onCardAbsent------------------");
			//mMPlayer.onDestory();
			isCAExisted=false;
			caCard=false;
			searchCard=false;
//			if(frame!=null){
//				frame.cAStop();
//			}
			//mMControl.pause();
		}

		@Override
		public void onCardMuted(int arg0) {
			// TODO Auto-generated method stub
			Log.v(TAG, "-----------onCardMuted------------------");
			startCATimer("no_card", 31);
		}

		@Override
		public void onCardPresent(int arg0) {
			Log.v(TAG, "-----------onCardPresent------------------");
			//startTimer();
			// TODO Auto-generated method stub
			//caCard=true;
			//getAllCAData();
			
		}

		@Override
		public void onCardReady(int arg0) {
			// TODO Auto-generated method stub
			isCAExisted=true;
			Log.v(TAG, "-----------onCardReady------------------");
			startgetCAIDTimer();
		}

		@Override
		public void onCardVerified(int readerIndex, int moduleID) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	CAModuleStateListener mCAManagerListener = new CAModuleStateListener(){

		
		/*public void onCardAbsent(int arg0) {
			// TODO Auto-generated method stub
			Log.v(TAG, "---------onCardAbsent--------------");
			
		}*/
		@Override
		public void onModuleRemove(int arg0) {
			Log.v(TAG, "---------onModuleRemove--------------");
			// TODO Auto-generated method stub
			//VODCAManager.getInstance().sendCAMsg(mActivity.getResources().getString(R.string.caremove));
			//startTimer();
		}
		@Override
		public void onCAChange(int arg0) {
			// TODO Auto-generated method stub
			Log.v(TAG, "---------onCAChange--------------");
			
		}

		@Override
		public void onModuleAbsent(int arg0) {
			// TODO Auto-generated method stub
			isCAExisted=true;
			Log.v(TAG, "---------onModuleAbsent--------------");
		}


		@Override
		public void onModuleAdd(int arg0) {
			// TODO Auto-generated method stub
			Log.v(TAG, "---------onModuleAdd--------------");
		}

		@Override
		public void onModulePresent(int arg0, int arg1) {
			// TODO Auto-generated method stub
			Log.v(TAG, "---------onModulePresent--------------");
		}
	};
	
	int times=0;
	public void startgetCAIDTimer(){//
		if(caTask!=null){
			caTask.cancel();
		}
		times=0;
		caTask=new TimerTask(){

			//@Override
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(times<15){
					times++;
					if(!getCAID().equals("nocard")){
						searchCard=true;
						times=18;
						getAllCAData();
					
						caTask.cancel();
					}else if(times==15&&getCAID().equals("nocard")){
//						if(frame!=null){
//							if(isCAExisted){
//								frame.setTips(getString(R.string.ca_no), 31);
//							}else{
//								frame.setTips(getString(R.string.ca_no_cha), 31);
//							}
//							caTask.cancel();
//						}
					}
				}else if(times==16){
					//caTask.cancel();
				}else{
					
					
				}
			}
		};
		try{
			mTimer.schedule(caTask, 0, 1000);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void startCATimer(final String tips,int index){ // 无效CA 卡的
		if(invaLidCATask!=null){
			invaLidCATask.cancel();
		}
		
		invaLidCATask=new TimerTask(){
			//@Override
			@Override
			public void run() {
				// TODO Auto-generated method stub
//				if(frame!=null){
//					frame.setTips(tips, 31);
//				}
			}
		};
		mTimer.schedule(invaLidCATask,5000);
	}
	
	public void getAllCAData(){
    	caId= getCAID();
		if(caId.equals("nocard") || caId==null){
			caId="nocard";
		}else{//缺少验证的接口，先不做
//			homeID=joinGetData.getHomeID(caId);
//			 if(homeID==null){
//				startCATimer(getString(R.string.ca_no_homeid), 32);
//				return;
//			}else if(homeID.equals("homeid")){
//				startCATimer(getString(R.string.ca_no_register), 32);
//				return;
//			}
//			Log.v(TAG, "------------------1--------homeID="+homeID);
//			joinGetData.getServicePackage(homeID);
//			Log.v(TAG, "------------------2--------homeID="+homeID);
//			joinGetData.getTicketsByHomeId(homeID);
//			Log.v(TAG, "------------------3--------homeID="+homeID);
//		
		}
    }

}
