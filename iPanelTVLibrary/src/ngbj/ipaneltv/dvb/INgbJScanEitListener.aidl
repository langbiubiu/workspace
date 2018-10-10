package ngbj.ipaneltv.dvb;

import ngbj.ipaneltv.dvb.NgbJSIEvent;

interface INgbJScanEitListener {
	
	void onScanEitSuccess(in NgbJSIEvent[] siEvent);
	
}