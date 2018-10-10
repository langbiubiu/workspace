package ipaneltv.toolkit;

import java.util.HashMap;

import android.content.Context;
import android.net.telecast.ca.CAManager;

public class SmartCardState {
	private static String TAG = "SmartCardState";
	private CAManager caManager;
	private static SmartCardState cardState;
	private SmartCardState(){}
	private boolean smartcardIsIn = true;
	private static HashMap<Integer, Boolean> card;
	private int currentModuleId = -1;
	public static String bouquetId = null;
	
	public static String getBouquetId(){
		IPanelLog.i(TAG, "getBouquetId start");
		if(bouquetId == null){
			IPanelLog.i(TAG, "getBouquetId bouquetId == null");
			return "0";
		}
		else{
			IPanelLog.i(TAG, "getBouquetId bouquetId = "+bouquetId);
			return bouquetId;
		}
	}
	
	public static SmartCardState getInstance(){
		if(cardState == null){
			card = new HashMap<Integer, Boolean>();
			cardState = new SmartCardState();
		}
			
		return cardState;
	}
	
	public void initCAState(Context context){
		IPanelLog.d(TAG, "initCAState");
		caManager = CAManager.createInstance(context);
		caManager.setCACardStateListener(cardStateListener);
		caManager.setCAModuleStateListener(moduleStateListener);
		caManager.queryCurrentCAState();
	}
	
	//获取region_id
	public String getCARegionId(String UUID){
		int[] modid = caManager.getCAModuleIDs(UUID);
		String cardNum = null;
		if (modid != null) {
			for (int mid : modid) {
				cardNum = caManager.getCAModuleProperty(mid,
						"CA_CARD_AREACODE");
				IPanelLog.d(TAG, String.format("moduleID %d, CA_CARD_AREACODE: %s",
						mid, cardNum));
				if (cardNum != null && cardNum.length() > 0)
					return cardNum;
			}
		}
		return cardNum;
	}
	
	//判断智能卡是否有效
	public int getSmartcardState(){
		if(card != null)
			return card.size();
		return -1;
	}
	
	CAManager.CAModuleStateListener moduleStateListener = new CAManager.CAModuleStateListener() {
		
		@Override
		public void onModuleRemove(int moduleID) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onModulePresent(int moduleID, int readerIndex) {
			IPanelLog.i(TAG, "onModulePresent start");
			currentModuleId = moduleID;
			IPanelLog.i(TAG, "onModulePresent end , currentModuleId = "+currentModuleId);
		}
		
		@Override
		public void onModuleAdd(int moduleID) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onModuleAbsent(int moduleID) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCAChange(int moduleID) {
			// TODO Auto-generated method stub
			
		}
	};
	
	CAManager.CACardStateListener cardStateListener = new CAManager.CACardStateListener(){

		@Override
		public void onCardPresent(int readerIndex) {
			// TODO Auto-generated method stub
			IPanelLog.d(TAG, "onCardPresent");
			card.put(readerIndex, smartcardIsIn);
			IPanelLog.d(TAG, "onCardPresent card.size()"+card.size());
		}

		@Override
		public void onCardAbsent(int readerIndex) {
			// TODO Auto-generated method stub
			IPanelLog.d(TAG, "onCardAbsent");
			card.remove(readerIndex);
			IPanelLog.d(TAG, "onCardAbsent card.size()"+card.size());
		}

		@Override
		public void onCardMuted(int readerIndex) {
			// TODO Auto-generated method stub
			IPanelLog.d(TAG, "onCardMuted");
		}

		@Override
		public void onCardReady(int readerIndex) {
			// TODO Auto-generated method stub
			IPanelLog.d(TAG, "onCardReady");
			card.put(readerIndex, smartcardIsIn);
			bouquetId = caManager.getCAModuleProperty(currentModuleId, "CA_CARD_BOUQUETID");
			IPanelLog.d(TAG, "onCardReady card.size()"+card.size() +" bouquetId = "+bouquetId);
		}

		@Override
		public void onCardVerified(int readerIndex, int moduleID) {
			// TODO Auto-generated method stub
			IPanelLog.d(TAG, "onCardVerified");
		}};
}
