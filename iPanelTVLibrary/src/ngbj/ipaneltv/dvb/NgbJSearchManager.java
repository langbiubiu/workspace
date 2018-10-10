package ngbj.ipaneltv.dvb;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.telecast.NetworkManager;
import android.net.telecast.SignalStatus;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

public class NgbJSearchManager {
	private static final String TAG = NgbJSearchManager.class.getSimpleName();
	static final String SERVICE_NAME = "android.intent.action.SearchService";
	private String serviceName;
	private Context context = null;
	private INgbJScanManager mService;
	HandlerThread handlerThread = new HandlerThread(TAG);
	Handler procHandler;
	Handler uiHandler;
	
	OnSearchListener searchlistener;
	OnSelectListener onSelectListener;	
	/**
	 * �������Ź�����ʵ��
	 * 
	 * @param c
	 *            �����Ķ���
	 * @return ����,ʧ���򷵻�null
	 */
	public static NgbJSearchManager createInstance(Context c, NetworkManager nm, String uuid) {
		String sname = nm.getNetworkProperty(uuid, "ngbj.ipaneltv.dvb.searchservice");
		if (sname == null)
			sname = SERVICE_NAME;
		return new NgbJSearchManager(c, sname);
	}
	//���Ȱ󶨷���
	NgbJSearchManager(Context c, String sname) {
		this.context = c;
		this.serviceName = sname;
		if(context != null){
			context.bindService(new Intent(serviceName), conn, Context.BIND_AUTO_CREATE);	
		}
		handlerThread.start();
		procHandler = new Handler(handlerThread.getLooper());
		uiHandler = new Handler(Looper.getMainLooper());
	}
	//��������ģʽ
	public void initScan(final int searchType, final int deliveryType, final int flags){
		procPost(new Runnable() {
			
			@Override
			public void run() {
				try {
					if(mService != null){
						mService.initScan(searchType, deliveryType, flags);
					}	
				} catch (Exception e) {
					Log.d(TAG, "initScan e = "+ e.getMessage());
				}
			}
		});
	}
	//�Զ�������׼��
	public void setMainFrequency(final long mainFrequency,final int modulation,final int symbolRate){
		
		procPost(new Runnable() {
			
			@Override
			public void run() {
				try {
					if(mService != null){
						mService.setMainFrequency(mainFrequency, modulation, symbolRate);
					}	
				} catch (Exception e) {
					Log.d(TAG, "setMainFrequency e = "+ e.getMessage());
				}
			}
		});
		
	}
	//�ֶ�����
	public void addScanManualParam(final long start, final long end, final int modulation, final int symbolRate, final int polarization){
		procPost(new Runnable() {
			
			@Override
			public void run() {
				try {
					if(mService != null){
						mService.addScanManualParam(start, end, modulation, symbolRate, polarization);
					}	
				} catch (Exception e) {
					Log.d(TAG, "addScanManualParam e = "+ e.getMessage());
				}
			}
		});
	}
	//��ʼ�������ж�������ʽ��������Ӧ������
	public void startScan(){
		procPost(new Runnable() {
			
			@Override
			public void run() {
				try {
					if(mService != null){
						mService.startScan();
					}	
				} catch (Exception e) {
					Log.d(TAG, "startScan e = "+ e.getMessage());
				}
			}
		});
	}
	//������������
	public void setDvbSearchListener(OnSearchListener listener){
		searchlistener = listener;
//		procUI(new Runnable() {
//			
//			@Override
//			public void run() {
//				OnSearchListener lis = searchlistener;
//				if(lis != null){
//					lis.onServiceConnected();
//				}
//			}
//		});
	}
	//������Ƶ����
	public void setonSelectListener(OnSelectListener listener){
		onSelectListener = listener;
	}
	//���������Ľ��
	public void saveScanResult(final int flags){
		procPost(new Runnable() {
			
			@Override
			public void run() {
				try {
					if(mService != null){
						mService.saveScanResult(flags);
					}	
				} catch (Exception e) {
					Log.d(TAG, "saveScanResult e = "+ e.getMessage());
				}
			}
		});
	}
	
	public void lockFreqency(final long freq,final int modulation,final int symbolRate){
		procPost(new Runnable() {
			
			@Override
			public void run() {
				try {
					if(mService != null){
						mService.lockFreqency(freq, modulation, symbolRate);
					}	
				} catch (Exception e) {
					Log.d(TAG, "LockFreqency e = "+ e.getMessage());
				}
			}
		});
	}
	//ȡ��
	public void cancel(){
		procPost(new Runnable() {
			
			@Override
			public void run() {
				try {
					if(mService != null){
						mService.cancel();
					}	
				} catch (Exception e) {
					Log.d(TAG, "cancel e = "+ e.getMessage());
				}
			}
		});
	}
	//�ͷ�
	public void release(){
		try {
			if(mService != null){
				mService.release();
				mService = null;
			}
			if(procHandler != null){
				procHandler.getLooper().quit();
				procHandler = null;
			}
			context.unbindService(conn);
		} catch (Exception e) {
			Log.d(TAG, "release e = "+ e.getMessage());
		}
	}
	
	/**
	 * ����������
	 */
	public static interface OnSearchListener {
		/**
		 * ����������
		 */
		public void onServiceConnected();
		
		/**
		 * �����ѶϿ�
		 */
		public void onServiceDisconnected();

		/**
		 * 
		 */
		public void onResponseStart(boolean succ);
		
		public void onFrequencySearch(String fi);

		public void onFrequencyEnd(String fi);

		public void onFrequencyNumber(int number);

		public void onSignalStatus(String ss);

		public void onServiceFound(String name, int type);

		public void onSearchFinished(boolean successed); 

		public void onRespWriteDatabase(boolean succ);

		public void onTipsShow(String msg);
	}
	/**
	 * ��Ƶ��������רΪ��Ƶ������ȡ��Ϣʹ�ã�
	 */
	public static interface OnSelectListener {
		/**
		 * ��Ƶ�ɹ�
		 */
		public void onSelectSuccess(long freq);
		
		public void onSelectFailed(long freq);
		
		public void onSelectionLost(long freq);
		
		public void onSelectionResumed(long freq);
		
		public void onSignalStatus(SignalStatus ss);
	}
	//�������Ӽ����ص�
	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					try {
						mService = null;
						OnSearchListener listener = searchlistener;
						if(listener != null){
							listener.onServiceDisconnected();
						}
					} catch (Exception e) {
						Log.e(TAG, "onServiceDisconnected" + e);
					}
				}
			});
		}

		@Override
		public void onServiceConnected(ComponentName name, final IBinder service) {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					try {
						mService = INgbJScanManager.Stub.asInterface(service);
						//������������ļ�����
						mService.setDvbSearchListener(dvbSearchListener);
						//������Ƶ�ļ�����
						mService.setSelectListener(selectListener);
						OnSearchListener listener = searchlistener;
						if(listener != null){
							listener.onServiceConnected();
						}
					} catch (Exception e) {
						Log.e(TAG, "onServiceConnected error:" + e);
					}
				}
			});
		}
	};
	
	void procPost(Runnable r){
		try {
			if(procHandler != null){
				procHandler.post(r);	
			}	
		} catch (Exception e) {
			Log.e(TAG, "procPost e = "+ e.getMessage());
//			e.printStackTrace();
			Log.d( TAG, "procPost" + e );
		}
	}
	void procUI(Runnable r){
		try {
			if(uiHandler != null){
				uiHandler.post(r);		
			}
		} catch (Exception e) {
			Log.e(TAG, "procUI e = "+ e.getMessage());
//			e.printStackTrace();
			Log.d( TAG, "procPost" + e );
		}
	}
	
	
	//DVB��������
	IDvbSearchListener dvbSearchListener  = new IDvbSearchListener.Stub(){

		@Override
		public void onResponseStart(final boolean succ) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					try {
						OnSearchListener listener = searchlistener;
						if(listener != null){
							listener.onResponseStart(succ);
						}
					} catch (Exception e) {
						Log.e(TAG, "onResponseStart" + e);
					}
				}
			});
		}

		@Override
		public void onFrequencySearch(final String fi) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					try {
						OnSearchListener listener = searchlistener;
						if(listener != null){
							listener.onFrequencySearch(fi);
						}
					} catch (Exception e) {
						Log.e(TAG, "onFrequencySearch" + e);
					}
				}
			});
		}

		@Override
		public void onFrequencyEnd(final String fi) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					try {
						OnSearchListener listener = searchlistener;
						if(listener != null){
							listener.onFrequencyEnd(fi);
						}
					} catch (Exception e) {
						Log.e(TAG, "onFrequencyEnd" + e);
					}
				}
			});
		}

		@Override
		public void onFrequencyNumber(final int number) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					try {
						OnSearchListener listener = searchlistener;
						if(listener != null){
							listener.onFrequencyNumber(number);
						}
					} catch (Exception e) {
						Log.e(TAG, "onFrequencyNumber" + e);
					}
				}
			});
		}

		@Override
		public void onSignalStatus(final String ss) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					try {
						OnSearchListener listener = searchlistener;
						if(listener != null){
							listener.onSignalStatus(ss);
						}
					} catch (Exception e) {
						Log.e(TAG, "onFrequencySearch" + e);
					}
				}
			});
		}

		@Override
		public void onServiceFound(final String name, final int type) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					try {
						OnSearchListener listener = searchlistener;
						if(listener != null){
							listener.onServiceFound(name,type);
						}
					} catch (Exception e) {
						Log.e(TAG, "onFrequencySearch" + e);
					}
				}
			});
		}

		@Override
		public void onSearchFinished(final boolean successed) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					try {
						OnSearchListener listener = searchlistener;
						if(listener != null){
							listener.onSearchFinished(successed);
						}
					} catch (Exception e) {
						Log.e(TAG, "onSearchFinished" + e);
					}
				}
			});
		}

		@Override
		public void onRespWriteDatabase(final boolean succ) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					try {
						OnSearchListener listener = searchlistener;
						if(listener != null){
							listener.onRespWriteDatabase(succ);
						}
					} catch (Exception e) {
						Log.e(TAG, "onRespWriteDatabase" + e);
					}
				}
			});
		}

		@Override
		public void onTipsShow(final String msg) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					try {
						OnSearchListener listener = searchlistener;
						if(listener != null){
							listener.onTipsShow(msg);
						}
					} catch (Exception e) {
						Log.e(TAG, "onTipsShow" + e);
					}
				}
			});
		}
		
	};
	//��Ƶ����
	ISelectListener selectListener = new ISelectListener.Stub() {
		
		@Override
		public void onSignalStatus(final String ss) throws RemoteException {
			
			procUI(new Runnable() {
				
				@Override
				public void run() {
					OnSelectListener listener = onSelectListener;
					SignalStatus signalStatus = null;;
					if(ss != null){
						try {
							signalStatus = SignalStatus.fromString(ss);	
						} catch (Exception e) {
							Log.e(TAG, "onSignalStatus e = "+ e.getMessage());
						}
					}
					if(listener != null){
						listener.onSignalStatus(signalStatus);
					}
				}
			});
		}
		
		@Override
		public void onSelectionResumed(final long freq) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					OnSelectListener listener = onSelectListener;
					if(listener != null){
						listener.onSelectionResumed(freq);
					}
				}
			});
		}
		
		@Override
		public void onSelectionLost(final long freq) throws RemoteException {
			
			procUI(new Runnable() {
				
				@Override
				public void run() {
					OnSelectListener listener = onSelectListener;
					if(listener != null){
						listener.onSelectionLost(freq);
					}
				}
			});
		}
		
		@Override
		public void onSelectSuccess(final long freq) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					OnSelectListener listener = onSelectListener;
					if(listener != null){
						listener.onSelectSuccess(freq);
					}
				}
			});
		}

		@Override
		public void onSelectFailed(final long freq) throws RemoteException {
			procUI(new Runnable() {
				
				@Override
				public void run() {
					OnSelectListener listener = onSelectListener;
					if(listener != null){
						listener.onSelectFailed(freq);
					}
				}
			});
		}
	};
}
