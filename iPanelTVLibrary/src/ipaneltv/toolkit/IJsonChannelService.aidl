package ipaneltv.toolkit;

import ipaneltv.toolkit.IJsonChannelCallback;
import ipaneltv.toolkit.IJsonChannelSession;
import android.os.Bundle;

interface IJsonChannelService{
	IJsonChannelSession createSession(String name, in IJsonChannelCallback cb, in Bundle bundle);
}