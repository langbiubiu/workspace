package ipaneltv.toolkit;

import android.os.Bundle;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.JsonReflectionInvokParcelable;

interface IJsonChannelCallback{
	oneway void onVersion(int v);
	oneway void onCallback(int cmd, String json, in JsonParcelable p,in Bundle b,boolean vn);
	oneway void onReflectionCallback(in JsonReflectionInvokParcelable p, String json);
}