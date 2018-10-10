package ipanel.join.configuration;

import ipanel.join.widget.IConfigView;

import android.view.ViewGroup;

public class Utils {

	public static android.view.View findViewByConfigId(android.view.View root,
			String id) {
		if (id != null && id.length() > 0) {
			if (root instanceof IConfigView) {
				if (id.equals(((IConfigView) root).getViewData().getId())) {
					return root;
				}
			}
			if (root instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup) root;
				int count = vg.getChildCount();
				for (int i = 0; i < count; i++) {
					android.view.View result = findViewByConfigId(
							vg.getChildAt(i), id);
					if (result != null)
						return result;
				}
			}
		}
		return null;
	}
}
