package ipaneltv.toolkit.dsmcc;

import ipaneltv.toolkit.dsmcc.IDsmccCallback;
import android.os.ParcelFileDescriptor;

interface IDsmccDownloader{
	void setCallback(IDsmccCallback cb);	
	int objectOpen(String netid, long freq);
	void objectClose(int o);
	void objectQueryReachable(int o);
	int objectOpenFd(int o,String filePath);
	boolean objectSetFds(int o, in ParcelFileDescriptor meta,int mlen, in ParcelFileDescriptor data,int dlen);
	boolean objectLoadPmt(int o, int pid, int ver, int tout);
	boolean objectLoadDsi(int o, int pid, int ver, int tout);
	boolean objectLoadDii(int o, int pid, int transid, int ver, int tout);
	boolean objectLoadModule(int o, int pid, int id, int ver, int mlen, int tout);	
	boolean objectLoadBigFile(int o, int pid, in int[] mid,String path, int ver, int tout);
}


                                                                                                                                          