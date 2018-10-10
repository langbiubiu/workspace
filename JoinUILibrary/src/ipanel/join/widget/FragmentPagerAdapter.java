package ipanel.join.widget;

import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.Screen;

import org.json.JSONArray;
import org.json.JSONException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class FragmentPagerAdapter extends FragmentStatePagerAdapter {
	JSONArray mJsonArray;
	public FragmentPagerAdapter(FragmentManager fm, JSONArray jsa) {
		super(fm);
		this.mJsonArray = jsa;
	}

	@Override
	public Fragment getItem(int position) {
		try {
			String id = mJsonArray.getJSONObject(position).getString("id");
			Screen sc = ConfigState.getInstance().getConfiguration().findScreenById(id);
			if(sc != null)
				return ScreenFragment.createFragment(sc);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public float getPageWidth(int position) {
		try {
			return (float) mJsonArray.getJSONObject(position).getDouble("width");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1f;
	}

	@Override
	public int getCount() {
		return mJsonArray.length();
	}

}
