package ipanel.join.configuration;

import org.json.JSONException;
import org.json.JSONObject;

public class ResManager {
	private JSONObject stringMap = new JSONObject();
	
	public String getString(String key){
		if(key == null)
			return key;
		if(!key.startsWith("@string/"))
			return key;
		
		String result = stringMap.optString(key.substring("@string/".length()));
		if(result == null || result.length() == 0)
			return key;
		return result;
	}
	
	public void replaceStringRefs(Configuration configuration){
		for(Screen sc : configuration.getScreen()){
			replaceStringRefs(sc.getView());
		}
		for(Style style : configuration.getStyles()){
			for(Bind bind : style.getBind()){
				if(bind.value != null)
					bind.value.value = getString(bind.value.value);
			}
		}
	}
	
	private void replaceStringRefs(View view) {
		for(Bind bd : view.getBind()){
			if(bd.value != null)
				bd.value.value = getString(bd.value.value);
		}
		for(Action action : view.getAction()){
			for(Bind bind : action.getBind()){
				if(bind.value != null)
					bind.value.value = getString(bind.value.value);
			}
		}
		for(View v : view.getView()){
			replaceStringRefs(v);
		}
	}

	public static ResManager createManager(String jsonData){
		ResManager res = new ResManager();
		try {
			JSONObject root = new JSONObject(jsonData);
			if(root.has("stringMap"))
				res.stringMap = root.getJSONObject("stringMap");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
}
