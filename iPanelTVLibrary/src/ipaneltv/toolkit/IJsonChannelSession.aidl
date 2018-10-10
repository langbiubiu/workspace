package ipaneltv.toolkit;

import android.os.Parcelable;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import ipaneltv.toolkit.JsonParcelable;


interface IJsonChannelSession{
	void updateCallbackVersion(int v);
	String transmit(int code, String json, in JsonParcelable p, in Bundle b, int nv);
	oneway void atransmit(int code, String json, in JsonParcelable p, in Bundle b);
	void close();
}