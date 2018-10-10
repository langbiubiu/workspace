package ipanel.join.widget;

import ipanel.join.configuration.Screen;
import ipanel.join.configuration.ViewInflater;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScreenFragment extends Fragment {
	public static ScreenFragment createFragment(Screen data){
		Bundle args = new Bundle();
		args.putSerializable("screen", data);
		ScreenFragment f = new ScreenFragment();
		f.setArguments(args);
		return f;
	}

	Screen mScreen;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mScreen = (Screen) getArguments().getSerializable("screen");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("fragment", "ScreenFragment is create");
		if(mFragmentIsCreateFinshListener != null)
			mFragmentIsCreateFinshListener.isCreatefinished();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = ViewInflater.inflateView(getActivity(), null, mScreen.getView());
		if(container instanceof IConfigViewGroup){
			v.setLayoutParams(((IConfigViewGroup) container).genConfLayoutParams(mScreen.getView()));
		}
		return v;
	}
	
	public static  FragmentIsCreateFinshListener mFragmentIsCreateFinshListener;

	public static void  setmFragmentIsCreateFinshListener(
			FragmentIsCreateFinshListener fragmentIsCreateFinshListener) {
		mFragmentIsCreateFinshListener = fragmentIsCreateFinshListener;
	}

	public interface FragmentIsCreateFinshListener{
		public void isCreatefinished();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	
}
