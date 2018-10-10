package ipaneltv.toolkit.ca;

import android.net.telecast.ca.CAManager;

public class CaModule {
	public static final int STATE_ABSENT = 0;
	public static final int STATE_PRESENT = 1;
	public static final int STATE_VERIFIED = 3;
	int moduleId;
	int casysIds[];
	int state;
	int slotId = -1;

	public int getModuleId() {
		return moduleId;
	}

	public int[] getCaSystemIds() {
		return casysIds;
	}

	public int getState() {
		return state;
	}

	public int getSlotId() {
		return slotId;
	}

	public String getSessionServiceName(CAManager cam) {
		return getSessionServiceName(cam, moduleId);
	}

	public static String getSessionServiceName(CAManager cam, int moduleId) {
		try {
			return cam.getCAModuleProperty(moduleId, CAManager.PROP_NAME_SESSION_SERVICE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getMaxChannelSize(CAManager cam, int defaultValue) {
		return getMaxChannelSize(cam, moduleId, defaultValue);
	}

	public static int getMaxChannelSize(CAManager cam, int moduleId, int defaultValue) {
		try {
			String value = cam.getCAModuleProperty(moduleId,
					CAManager.PROP_NAME_MAX_DESCRAMBLING_SIZE);
			if (value != null)
				return Integer.parseInt(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public String getEntitlementDatabaseUri(CAManager cam) {
		return getEntitlementDatabaseUri(cam, moduleId);
	}

	public static String getEntitlementDatabaseUri(CAManager cam, int moduleId) {
		try {
			return cam.getCAModuleProperty(moduleId, CAManager.PROP_NAME_ENTITLEMENT_URI);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getAreaCode(CAManager cam) {
		return getAreaCode(cam, null);
	}

	public String getAreaCode(CAManager cam, String defaultValue) {
		return getAreaCode(cam, moduleId, defaultValue);
	}

	public static String getAreaCode(CAManager cam, int moduleId, String defaultValue) {
		try {
			return cam.getCAModuleProperty(moduleId, CAManager.PROP_NAME_AREA_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public int getModuleSN(CAManager cam) {
		return getModuleSN(cam, -1);
	}

	public int getModuleSN(CAManager cam, int defaultValue) {
		return getModuleSN(cam, moduleId, defaultValue);
	}

	public static int getModuleSN(CAManager cam, int moduleId, int defaultValue) {
		try {
			String id = cam.getCAModuleProperty(moduleId, CAManager.PROP_NAME_MODULE_SN);
			if (id != null)
				return Integer.parseInt(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public int getCardNumber(CAManager cam) {
		return getCardNumber(cam, -1);
	}

	public int getCardNumber(CAManager cam, int defaultValue) {
		return getCardNumber(cam, moduleId, defaultValue);
	}

	public static int getCardNumber(CAManager cam, int moduleId, int defaultValue) {
		try {
			String value = cam.getCAModuleProperty(moduleId, CAManager.PROP_NAME_CARD_NUMNER);
			if (value != null)
				return Integer.parseInt(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public int getAssociatedBouquetId(CAManager cam) {
		return getAssociatedBouquetId(cam, -1);
	}

	public int getAssociatedBouquetId(CAManager cam, int defaultValue) {
		try {
			String value = cam.getCAModuleProperty(moduleId,
					CAManager.PROP_NAME_ASSOCIATED_BOUQUET_ID);
			if (value != null)
				return Integer.parseInt(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public static int getAssociatedBouquetId(CAManager cam, int moduleId, int defaultValue) {
		try {
			String value = cam.getCAModuleProperty(moduleId,
					CAManager.PROP_NAME_ASSOCIATED_BOUQUET_ID);
			if (value != null)
				return Integer.parseInt(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}
}
