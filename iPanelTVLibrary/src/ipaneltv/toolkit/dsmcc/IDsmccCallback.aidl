package ipaneltv.toolkit.dsmcc;

import java.util.List;

 interface IDsmccCallback{	
	oneway void onLoadFinish(int oid, boolean update,int len);
	oneway void onLoadFailed(int oid, int code,String es);	
	oneway void onLoadReachable(int oid, boolean b,int len);	
}

