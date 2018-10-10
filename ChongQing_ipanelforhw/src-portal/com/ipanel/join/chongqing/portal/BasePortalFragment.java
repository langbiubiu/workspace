package com.ipanel.join.chongqing.portal;

import android.app.Fragment;
import android.view.View;

public class BasePortalFragment extends Fragment {

	/**
	 * find view in the fragment root view
	 * @param id
	 * @return
	 */
	public View findViewById(int id){
		View root = getView();
		if( root != null)
			return root.findViewById(id);
		return null;
	}
}
