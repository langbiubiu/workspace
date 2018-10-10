package cn.ipanel.dlna;

import android.content.Context;
import android.content.SharedPreferences;

public class PersistStore {
	public static enum BoolSetting{
		DISABLE_AUTO_START;
		
		public boolean getSetting(Context c){
			return PersistStore.getSotore(c).getSetting(this);
		}
		
		public void setSetting(Context c, boolean value){
			PersistStore.getSotore(c).setSetting(this, value);
		}

		public void toggleSetting(Context c) {
			setSetting(c, !getSetting(c));
		}
	}
	
	private SharedPreferences sp;
	
	private static PersistStore store;
	
	public static synchronized PersistStore getSotore(Context context){
		if(store == null)
			store = new PersistStore(context);
		return store;
	}
	
	private PersistStore(Context context){
		sp = context.getSharedPreferences(PersistStore.class.getSimpleName(), 0);
	}
	
	private boolean getSetting(BoolSetting setting){
		return sp.getBoolean(setting.name(), false);
	}
	
	private void setSetting(BoolSetting setting, boolean v){
		sp.edit().putBoolean(setting.name(), v).commit();
	}
}
