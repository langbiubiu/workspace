package ipaneltv.toolkit.dvb;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CarouselTookit {

	HashMap<Integer, DataCarouselModule> dcModules = new HashMap<Integer, DataCarouselModule>();
	HashMap<Integer, DataCarouselModule> ocModules = new HashMap<Integer, DataCarouselModule>();

	
	
	public void analyticDIIJson(String meta) {

		try {
			JSONArray jsonArray = new JSONObject(meta).getJSONArray("modulelist");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jo = (JSONObject) jsonArray.opt(i);
				DataCarouselModule info = new DataCarouselModule();
				int moduleId = (jo.getInt("module_id"));
				info.setModuleId(moduleId);
				info.setModuleSize(jo.getInt("module_size"));
				info.setModuleVersion(jo.getInt("module_version"));
				info.setBlockSize(jo.getInt("block_size"));
				info.setModuleName(jo.getString("module_name"));
				dcModules.put(moduleId, info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void analyticOcDiiJson(String meta) {

		try {
			JSONArray jsonArray = new JSONObject(meta).getJSONArray("modulelist");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jo = (JSONObject) jsonArray.opt(i);
				DataCarouselModule info = new DataCarouselModule();
				int moduleId = (jo.getInt("module_id"));
				info.setModuleId(moduleId);
				info.setModuleSize(jo.getInt("module_size"));
				info.setModuleVersion(jo.getInt("module_version"));
				info.setBlockSize(jo.getInt("block_size"));
				info.setModuleName(jo.getString("module_name"));
				dcModules.put(moduleId, info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


	/*
	 * ��ȡָ��module_id��ModuleInfo
	 */
	DataCarouselModule getModuleInfo(int moduleId) {
		if (dcModules != null)
			return dcModules.get(moduleId);
		return null;
	}

	/*
	 * ����ָ��module_nameǰ׺��ModuleInfo
	 */
	DataCarouselModule searchModuleByPrefix(String prefix) {
		if (dcModules != null)
			for (DataCarouselModule mi : dcModules.values()) {
				if (mi.getModuleName().startsWith(prefix))
					return mi;
			}
		return null;
	}

	/*
	 * ����ָ��module_name��ModuleInfo
	 */
	DataCarouselModule searchModuleByName(String moduleName) {
		if (dcModules != null)
			for (DataCarouselModule mi : dcModules.values()) {
				if (mi.getModuleName().equalsIgnoreCase(moduleName))
					return mi;
			}
		return null;
	}
}
