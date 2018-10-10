package ipaneltv.toolkit.dvb;

import ipaneltv.dvbsi.BAT;
import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Section;
import ipaneltv.toolkit.SectionBuffer;
import ipaneltv.toolkit.SectionBuilder;
import ipaneltv.toolkit.SectionSaver;
import ipaneltv.toolkit.mediaservice.components.L10n;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler.ResourcesState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.net.telecast.FrequencyInfo;
import android.net.telecast.JSectionFilter;
import android.net.telecast.JStreamSelector;
import android.net.telecast.NetworkInterface;
import android.net.telecast.SectionFilter;
import android.net.telecast.StreamSelector;
import android.net.telecast.TransportManager;

/**
 * DVB��������
 */
public final class DvbSearchToolkit {

	final String TAG = "NewDvbSearchToolkit";

	/**
	 * ����ʵ��
	 * 
	 * @param netid
	 *            ����ID
	 * @return ����
	 */
	public static DvbSearchToolkit createInstance(String netid) {
		netid = UUID.fromString(netid).toString();
		return new DvbSearchToolkit(netid);
	}

	/**
	 * Ƶ������������
	 */
	public static interface FreqSearchListener {
		/** ��������ʼ */
		void onSearchStopped(FrequencyInfo fi);

		/** ��������Ҫ��ʾ��Ϣ */
		void onSearchMessage(FrequencyInfo fi, String msg);

		/** ���������� */
		void onSearchError(FrequencyInfo fi, String msg);
	}

	/**
	 * ��ֹ��ȡ
	 */
	public static interface TableReceiveTerminitor {
		/**
		 * ��ֹ
		 */
		void terminate();
	}

	/**
	 * ����ռ�����
	 */
	public static interface TableReceiveListener {
		/**
		 * ��ʼ��ȡ
		 * 
		 * @param t
		 *            ����ͨ���˶���������ֹ����
		 */
		public void onReceiveStart(TableReceiveTerminitor t);

		/**
		 * ����������ʧ��
		 * 
		 * @param msg
		 *            ��Ϣ
		 */
		public void onReceiveFailed(String msg);

		/**
		 * �����յ�section
		 * 
		 * @param sec
		 *            �����ݶ���
		 * @param finished
		 *            �Ƿ��Ѿ��������section�Ľ���
		 */
		public void onSectionGot(Section sec, boolean finished);
	}

	/**
	 * ����Ƶ��������ɳ�ʱʱ��
	 * 
	 * @param millis
	 *            ����
	 */
	public void setFreqFullSearchedTimeout(long millis) {
		if (millis <= 0)
			searchFreqTimeout = 0;
		else
			searchFreqTimeout = millis;
	}

	/**
	 * ���ñ���ȡ��ɳ�ʱʱ��
	 * 
	 * @param millis
	 *            ����
	 */
	public void setTableFullReceivedTimeout(long millis) {
		if (millis <= 0)
			tableFullReceivedTimeout = 0;
		else
			tableFullReceivedTimeout = millis;
	}

	/**
	 * �õ�ָ�����͵Ĵ����Ĭ������ӿ�ID
	 * 
	 * @param d
	 *            �������� "c","t" or "s"
	 * @return �ӿ�,���δ�ҵ�����null
	 */
	public NetworkInterface getDefaultInterfaceId(String d) {
		int dtype = -1;
		if (d.equalsIgnoreCase("c") || d.equalsIgnoreCase("cable")) {
			dtype = NetworkInterface.DELIVERY_CABLE;
		} else if (d.equalsIgnoreCase("s") || d.equalsIgnoreCase("satellite")) {
			dtype = NetworkInterface.DELIVERY_SATELLITE;
		} else if (d.equalsIgnoreCase("t") || d.equalsIgnoreCase("terrestrial")) {
			dtype = NetworkInterface.DELIVERY_TERRESTRIAL;
		} else {
			return null;
		}
		List<NetworkInterface> nis = tsManager.getNetworkInterfaces();
		NetworkInterface ni;
		for (int i = 0; i < nis.size(); i++) {
			if ((ni = nis.get(i)).getDevliveryType() == dtype)
				return ni;
		}
		return null;
	}

	/**
	 * �õ�ָ�����͵Ĵ����Ĭ������ӿ�ID
	 * 
	 * @param d
	 *            �������� ����
	 * @return �ӿ�,���δ�ҵ�����null
	 */
	public NetworkInterface getDefaultInterfaceId(int dtype) {
		List<NetworkInterface> nis = tsManager.getNetworkInterfaces();
		NetworkInterface ni;
		for (int i = 0; i < nis.size(); i++) {
			if ((ni = nis.get(i)).getDevliveryType() == dtype)
				return ni;
		}
		return null;
	}

	/**
	 * ��������������
	 * 
	 * @param l
	 *            ��������
	 */
	public void setFreqSearchListener(FreqSearchListener l) {
		fsl = l;
	}

	/**
	 * ���ô���������
	 * 
	 * @param manager
	 *            ����
	 */
	public void setTransportManager(TransportManager manager) {
		this.tsManager = manager;
	}

	/**
	 * ������ѡ�������
	 * 
	 * @param mPlayResource
	 * 
	 */
	public void setStreamSelector(ResourcesState mPlayResource) {
		synchronized (mutex) {
			mStreamSelector = null;
			this.mPlayResource = mPlayResource;
		}
	}

	/**
	 * ������ѡ�������
	 * 
	 * @param mStreamSelector
	 *            ѡ����
	 */
	public void setStreamSelector(StreamSelector mStreamSelector) {
		synchronized (mutex) {
			mPlayResource = null;
			this.mStreamSelector = mStreamSelector;
		}
	}

	/**
	 * ����Ƶ������
	 * 
	 * @param fi
	 *            Ƶ����Ϣ
	 * @return �ɹ���������true,���򷵻�false
	 */
	public boolean startFreqSearch(FrequencyInfo fi) {
		synchronized (mutex) {
			IPanelLog.d(TAG, "startFreqSearch currentTask = " + currentTask);
			if (currentTask == null&& tsManager != null) {
				if(mPlayResource != null){
					currentTask = new FreqTask(mPlayResource, fi);
					if (currentTask.start()) {
						IPanelLog.d(TAG, "startFreqSearch success");
						return true;
					}
					currentTask = null;	
				}else if(mStreamSelector != null){
					currentTask = new FreqTask(mStreamSelector, fi);
					if (currentTask.start()) {
						IPanelLog.d(TAG, "startFreqSearch success");
						return true;
					}
					currentTask = null;	
				}
			}
		}
		IPanelLog.d(TAG, "startFreqSearch out");
		return false;
	}

	/**
	 * ֹͣƵ������
	 */
	public void stopFreqSearch() {
		synchronized (mutex) {
			IPanelLog.d(TAG, "synchronized stopFreqSearch in");
			if (currentTask != null) {
				currentTask.stop();
				currentTask = null;
				currentTS = null;
			}
		}
		IPanelLog.d(TAG, "synchronized stopFreqSearch out");
	}

	/**
	 * ��ӱ�����
	 * 
	 * @param bufSize
	 *            �����ݻ�������С(128-4096)
	 * @param tableId
	 *            ��ID
	 * @param listener
	 *            ������
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean addTableSearching(int bufSize, int pid, int tableId,
			TableReceiveListener listener, int program) {
		return addTableSearching(bufSize, pid, tableId, listener, program, 0);
	}

	/**
	 * 
	 * @param bufSize
	 *            �����ݻ�������С(128-4096)
	 * @param pid
	 *            ��pid
	 * @param tableId
	 *            ��ID
	 * @param listener
	 *            ������
	 * @param program
	 *            Ĭ�ϴ�-1�����������Ƶ���Ź�������
	 * @param flags
	 *            Ĭ�ϴ�0����ʾʹ��Ĭ�ϵı���ˣ�����Ϊ���������ֶ�ֹͣ������
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean addTableSearching(int bufSize, int pid, int tableId,
			TableReceiveListener listener, int program, int flags) {
		IPanelLog.d(TAG, "synchronized addTableSearching pid=" + pid + ",tableid=" + tableId);
		if (tableId < 0 || tableId > 65536)
			throw new IllegalArgumentException("invalid table id:" + tableId);
		synchronized (mutex) {
			IPanelLog.d(TAG, "synchronized addTableSearching currentTask = " + currentTask);
			if (currentTask != null) {
				IPanelLog.d(TAG, "synchronized addTableSearching return");
				return currentTask.startTableTask(bufSize, pid, tableId, listener, program, flags);
			}
		}
		IPanelLog.d(TAG, "synchronized addTableSearching out");
		return false;
	}
	/**
	 * 
	 * @param bufSize
	 *            �����ݻ�������С(128-4096)
	 * @param pid
	 *            ��pid
	 * @param tableId
	 *            ��ID
	 * @param listener
	 *            ������
	 * @param program
	 *            Ĭ�ϴ�-1�����������Ƶ���Ź�������
	 * @param flags
	 *            Ĭ�ϴ�0����ʾʹ��Ĭ�ϵı���ˣ�����Ϊ���������ֶ�ֹͣ������
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean addTableSearching(int bufSize,int filterSize, int pid, int tableId,
			TableReceiveListener listener, int program, int flags) {
		IPanelLog.d(TAG, "synchronized addTableSearching pid=" + pid + ",tableid=" + tableId);
		if (tableId < 0 || tableId > 65536)
			throw new IllegalArgumentException("invalid table id:" + tableId);
		synchronized (mutex) {
			IPanelLog.d(TAG, "synchronized addTableSearching currentTask = " + currentTask);
			if (currentTask != null) {
				IPanelLog.d(TAG, "synchronized addTableSearching return");
				return currentTask.startTableTask(bufSize,filterSize, pid, tableId, listener, program, flags);
			}
		}
		IPanelLog.d(TAG, "synchronized addTableSearching out");
		return false;
	}

	/**
	 * ��Ӽ�ʱ������
	 * 
	 * @param tt
	 *            ����
	 * @param delay
	 *            ��ʱ
	 * @param duration
	 *            ����
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean addTimerTask(TimerTask tt, int delay, int duration) {
		synchronized (mutex) {
			IPanelLog.d(TAG, "synchronized addTimerTask in");
			if (currentTask != null) {
				currentTask.timer.schedule(tt, delay, duration);
				IPanelLog.d(TAG, "synchronized addTimerTask return");
				return true;
			}
		}
		IPanelLog.d(TAG, "synchronized addTimerTask out");
		return false;
	}

	class FreqTask extends TimerTask implements StreamSelector.SelectionStateListener {

		private HashSet<TableTask> tasks = new HashSet<TableTask>();
		private Object mObject = new Object();
		ResourcesState mPlayResource;
		StreamSelector ss;
		FrequencyInfo fi;
		private boolean started = false;
		Timer timer = new Timer();

		public FreqTask(ResourcesState mPlayResource, FrequencyInfo fi) {
			this.mPlayResource = mPlayResource;
			this.fi = fi;
		}
		
		public FreqTask(StreamSelector ss, FrequencyInfo fi) {
			this.ss = ss;
			this.fi = fi;
		}

		public long getFrequencyValue() {
			return fi.getFrequency();
		}

		public boolean start() {
			synchronized (tasks) {
				synchronized (timer) {
					IPanelLog.d(TAG, "synchronized TimerTask start in");
					started = false;
					try {
						if (mPlayResource != null) {
							if (mPlayResource.reserve()) {
								IPanelLog.d(TAG, "synchronized TimerTask start in 11");
								mPlayResource.setNetworkUUID(uuid);
								mPlayResource.getSelector().setSelectionStateListener(this);
								if ((started = mPlayResource.getSelector().select(fi,
										StreamSelector.SELECT_FLAG_FORCE))) {
									IPanelLog.i(TAG, "fi select " + fi + ",timeout = " + searchFreqTimeout);
									if (searchFreqTimeout > 0)
										timer.schedule(this, searchFreqTimeout);
								} else {
									IPanelLog.d(TAG, "start liruiy select failed fi = "+fi);
								}
							}
						} else if (ss != null) {
							ss.setNetworkUUID(uuid);
							ss.setSelectionStateListener(this);
							if ((started = ss.select(fi, StreamSelector.SELECT_FLAG_FORCE))) {
								IPanelLog.i(TAG, "fi select " + fi + ",timeout = " + searchFreqTimeout);
								if (searchFreqTimeout > 0)
									timer.schedule(this, searchFreqTimeout);
							} else {
								IPanelLog.d(TAG, "start stream select failed");
							}
						}
						IPanelLog.d(TAG, "start FreqTask");
					} finally {
						if (!started) {
							if (mPlayResource != null) {
								mPlayResource.getSelector().setSelectionStateListener(null);
							}
							if (ss != null) {
								ss.setSelectionStateListener(null);
							}
						}
					}
				}
			}
			IPanelLog.d(TAG, "synchronized TimerTask start out");
			return started;
		}

		public void stop() {
			boolean stopped = false;
			synchronized (timer) {
				if (!started) {
					return;
				}
				started = false;
			}
			synchronized (tasks) {
				IPanelLog.d(TAG, "synchronized TimerTask stop in");
				if (mPlayResource != null) {
					mPlayResource.getSelector().setSelectionStateListener(null);
				}
				if (mStreamSelector != null) {
					mStreamSelector.setSelectionStateListener(null);
				}
				timer.cancel();
				for (TableTask task : tasks) {
					try {
						task.stop();
						task.release();
					} catch (Exception e) {
						IPanelLog.d(TAG, "synchronized TimerTask stop exception");
						e.printStackTrace();
					}
				}
				tasks.clear();
				stopped = true;
			}
			IPanelLog.d(TAG, "synchronized TimerTask stop out");
			if (stopped) {
				FreqSearchListener l = fsl;
				if (l != null) {
					currentTask = null;
					l.onSearchStopped(fi);
				}
			}
		}

		public boolean startTableTask(int bufSize, int pid, int tableId,
				TableReceiveListener listener, int program) {
			return startTableTask(bufSize, pid, tableId, listener, program, 0);
		}

		/**
		 * 
		 * @param bufSize
		 * @param pid
		 * @param tableId
		 * @param listener
		 * @param program
		 * @param flags
		 *            Ĭ�ϴ�0:ʹ��Ĭ�ϵĹ��˷�ʽ��1��Ϊ�����ˣ����ֶ�ֹͣ��ȡ��
		 * @return
		 */
		public boolean startTableTask(int bufSize, int pid, int tableId,
				TableReceiveListener listener, int program, int flags) {
			synchronized (tasks) {
				IPanelLog.d(TAG, "synchronized TimerTask startTableTask in");
				TableTask task = new TableTask(bufSize,0, pid, tableId, listener, program, flags);
				if (task.start()) {
					IPanelLog.d(TAG, "task.start...");
					tasks.add(task);
					if (tableFullReceivedTimeout > 0){
						timer.schedule(task, tableFullReceivedTimeout);
					}
					IPanelLog.d(TAG, "synchronized TimerTask startTableTask return t");
					return true;
				}else{
					IPanelLog.d(TAG, "task.start...failed");
				}
			}
			IPanelLog.d(TAG, "synchronized TimerTask startTableTask out");
			return false;
		}
		/**
		 * 
		 * @param bufSize
		 * @param filterSize
		 * @param pid
		 * @param tableId
		 * @param listener
		 * @param program
		 * @param flags
		 *            Ĭ�ϴ�0:ʹ��Ĭ�ϵĹ��˷�ʽ��1��Ϊ�����ˣ����ֶ�ֹͣ��ȡ��
		 * @return
		 */
		public boolean startTableTask(int bufSize, int filterSize,int pid, int tableId,
				TableReceiveListener listener, int program, int flags) {
			synchronized (tasks) {
				IPanelLog.d(TAG, "synchronized TimerTask startTableTask in");
				TableTask task = new TableTask(bufSize,filterSize, pid, tableId, listener, program, flags);
				if (task.start()) {
					IPanelLog.d(TAG, "task.start...");
					tasks.add(task);
					if (tableFullReceivedTimeout > 0){
						timer.schedule(task, tableFullReceivedTimeout);
					}
					IPanelLog.d(TAG, "synchronized TimerTask startTableTask return t");
					return true;
				}else{
					IPanelLog.d(TAG, "task.start...failed");
				}
			}
			IPanelLog.d(TAG, "synchronized TimerTask startTableTask out");
			return false;
		}

		void removeTableTask(TableTask task) {
			boolean stopit = false;
			synchronized (tasks) {
				IPanelLog.d(TAG, "synchronized TimerTask removeTableTask in pid=" + task.pid + ",tid="
						+ task.coef[0]);
				task.stop();
				tasks.remove(task);
				task.cancel();
				stopit = (tasks.size() == 0);
			}
			IPanelLog.d(TAG, "synchronized TimerTask removeTableTask out");
			if (stopit)
				stop();
		}

		@Override
		public void onSelectStart(StreamSelector selector) {
			IPanelLog.d(TAG, "call onSelectStart");
		}

		@Override
		public void onSelectSuccess(StreamSelector selector) {
			synchronized (mObject) {
				IPanelLog.d(TAG, "call onSelectSuccess");
				if (started) {
					try {
						FreqSearchListener l = fsl;
						if (l != null) {
							l.onSearchMessage(fi, L10n.code_904);
							IPanelLog.d(TAG, "onSelectSuccess success");
						}
					} catch (Exception e) {
						IPanelLog.d(TAG, "onSelectSuccess error=" + e);
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void onSelectFailed(StreamSelector selector) {
			boolean stopit = false;
			synchronized (mObject) {
				IPanelLog.d(TAG, "call onSelectFailed");
				if (started) {
					FreqSearchListener l = fsl;
					try {
						if (l != null)
							l.onSearchError(fi, L10n.code_905);
					} catch (Exception e) {
						IPanelLog.e(TAG, "onSelectFailed error=" + e);
						e.printStackTrace();
					} finally {
						stopit = true;
					}
				}
			}
			IPanelLog.d(TAG, "synchronized TimerTask onSelectFailed out");
			if (stopit)
				stop();
		}

		@Override
		public void onSelectionLost(StreamSelector selector) {
			synchronized (mObject) {
				IPanelLog.d(TAG, "call onSelectionLost");
				if (started) {
					FreqSearchListener l = fsl;
					if (l != null) {
						IPanelLog.i(TAG, "signal is losted");
						l.onSearchMessage(fi, L10n.code_906);
					}
				}
			}
		}

		@Override
		public void onSelectionResumed(StreamSelector selector) {
			synchronized (mObject) {
				IPanelLog.d(TAG, "call onSelectionResumed");
				if (started) {
					FreqSearchListener l = fsl;
					if (l != null) {
						IPanelLog.i(TAG, "signal is resumed");
						l.onSearchMessage(fi, null);
					}
				}
			}
		}

		public void onFreqSearchTimeout() {
			synchronized (mObject) {
				IPanelLog.d(TAG, "call onFreqSearchTimeout");
				if (started) {
					try {
						FreqSearchListener l = fsl;
						if (l != null)
							l.onSearchMessage(fi, L10n.code_907);
						stop();
					} catch (Exception e) {
					}
				}
			}
		}

		@Override
		public void run() {
			onFreqSearchTimeout();// ��������Ƶ��ʱ�䳬ʱ
		}

		class TableTask extends TimerTask implements SectionFilter.SectionDisposeListener,
				TableReceiveTerminitor {
			private byte coef[] = new byte[] { 0, 0, 0, 0, 0 };
			private byte mask[] = new byte[] { (byte) 0xFF, 0x0, 0x0, 0, 0 };
			private byte excl[] = new byte[] { 0, 0, 0, 0, 0 };
			private int pid = -1,tableid = -1, flags = 0,fs=0;
			SectionFilter f = null;
			SectionBuffer sbuffer = null;
			Section section;
			TableReceiveListener lis;
			DvbSiTable table = new DvbSiTable();
			private boolean running = false;
			private boolean isFulled = false;
			
			boolean removeit = false;

			TableTask(int bs, int fs,int pid, int tableid, TableReceiveListener lis, int program, int flags) {
				this.pid = pid;
				this.flags = flags;
				this.tableid = tableid;
				this.fs = fs;
				coef[0] = (byte) tableid;
				this.lis = lis;
				sbuffer = SectionBuffer.createSectionBuffer(bs);
				section = new Section(sbuffer);
				if (program != -1) {
					coef[3] = (byte) ((program & 0xff00) >> 8);
					coef[4] = (byte) (program & 0x00ff);
					mask[3] = (byte) 0xff;
					mask[4] = (byte) 0xff;
					IPanelLog.d(TAG, "TableTask program_number = " + program);
					IPanelLog.d(TAG, ",coef[3]=" + coef[3] + ",coef[4]=" + coef[4]);
				}
				IPanelLog.d(TAG, "TableTask is start with pid = " + pid + ",tableid=" + tableid);
			}

			boolean openFilter() {
				IPanelLog.i(TAG, "openFilter f = "+f);
				if (f == null) {
					if (mStreamSelector != null && mStreamSelector instanceof JStreamSelector) {
						IPanelLog.i(TAG, "openFilter  createInstance JSectionFilter");
						if ((f = JSectionFilter.createInstance(uuid)) == null) {
							IPanelLog.d(TAG, "createJSectionFilter failed");
							return false;
						}
					} else {
						IPanelLog.i(TAG, "openFilter  createFilter");
						if ((f = tsManager.createFilter(uuid, fs)) == null) {
							IPanelLog.d(TAG, "createFilter failed");
							return false;
						}
					}
					f.setFrequency(getFrequencyValue());
					//�����еĵط�bat�����ֻ��һ�����������Ϊalways��ģʽ������������������ж�����
					if(tableid == DvbConst.TID_BAT||flags == 3||flags == 2){
						f.setAcceptionMode(SectionFilter.ACCEPT_ALWAYS);
					}else{
						f.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);	
					}
					f.setCARequired(false);
					f.setTimeout(5000);
					f.setSectionDisposeListener(this);
					IPanelLog.i(TAG, "openFilter  createFilter true");
					return true;
				}
				return false;
			}

			public void release() {
				if (f != null) {
					f.release();
					sbuffer.release();
					sbuffer = null;
					f = null;
				}
			}

			public boolean start() {
				try {
					IPanelLog.i(TAG, "tabletask start");
					if (openFilter()){
						IPanelLog.i(TAG, "tabletask start before running = ");
						running = f.start(pid, coef, mask, excl, 5);
						IPanelLog.i(TAG, "tabletask start running = "+running);
					}else{
						IPanelLog.i(TAG, "tabletask start failed ");
					}
				} finally {
					if (!running)
						release();
				}
				return running;
			}

			public void stop() {
				running = false;
				super.cancel();
				release();
				IPanelLog.i(TAG, "tabletask is end with pid = " + pid);
			}

			@Override
			public void onStreamLost(SectionFilter f) {
				boolean removeit = false;
				synchronized (timer) {
					if (!started) {
						return;
					}
//				synchronized (tasks) {
					IPanelLog.d(TAG, "synchronized TableTask onStreamLost in");
					if (running) {
						try {
							IPanelLog.i("onReceiveFailed", "onReceiveFailed onstreamlost");
							lis.onReceiveFailed(L10n.code_908);
						} catch (Exception e) {
						} finally {
							removeit = true;
						}
					}
//				}
				}
				IPanelLog.d(TAG, "synchronized TableTask onStreamLost out 1");
				if(removeit){
					new Thread(new Runnable() {

						@Override
						public void run() {
							removeTableTask(TableTask.this);
						}
					}).start();	
				}
			
			}

			@Override
			public void onReceiveTimeout(SectionFilter f) {
				boolean removeit = false;
				synchronized (timer) {
					if (!started) {
						return;
					}
//				synchronized (tasks) {
					IPanelLog.d(TAG, "synchronized TableTask onReceiveTimeout in");
					if (running) {
						try {
							IPanelLog.i("onReceiveFailed", "onReceiveFailed onReceiveTimeout");
							lis.onReceiveFailed(L10n.code_909);
						} catch (Exception e) {
						} finally {
							removeit = true;
						}
					}
//				}
				}
				IPanelLog.d(TAG, "synchronized TableTask onReceiveTimeout out 1");
				if(removeit){
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							removeTableTask(TableTask.this);
						}
					}).start();	
				}
			}

			public void onTableFullReceivedTimeout() {
				boolean removeit = false;
				synchronized (timer) {
					if (!started) {
						return;
					}
//				synchronized (tasks) {
					IPanelLog.d(TAG, "synchronized TableTask onTableFullReceivedTimeout in");
					if (running) {
						try {
							IPanelLog.i("onReceiveFailed", "onReceiveFailed onTableFullReceivedTimeout");
							lis.onReceiveFailed(L10n.code_910);
						} catch (Exception e) {
						} finally {
							removeit = true;
						}
					}
//				}
				}
				IPanelLog.d(TAG, "synchronized TableTask onTableFullReceivedTimeout out 1");
				if(removeit){
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							removeTableTask(TableTask.this);
						}
					}).start();	
				}
			}

			@Override
			public void onSectionRetrieved(SectionFilter f, int len) {
				IPanelLog.i(TAG,"onSectionRetrieved 1 task = " + this.toString());
				synchronized (timer) {
					if (!started) {
						return;
					}
					if(!removeit){
						IPanelLog.d(TAG, "onSectionRetrieved in");
						//					synchronized (tasks) {
						IPanelLog.d(TAG, "synchronized TableTask onSectionRetrieved in");
						if (running) {
							IPanelLog.i(TAG, "onSectionRetrieved f = " + f.getFrequency());
							IPanelLog.d(TAG, "onSectionRetrieved pid=" + f.getStreamPID());
							sbuffer.copyFrom(f);
							section.reset();
							boolean full = false;
							IPanelLog.d(TAG,
									"onSectionRetrieved section.table_id = " + section.table_id()
											+ ";f.getStreamPID() = " + f.getStreamPID()
											+ ";section.version_number() = " + section.version_number()
											+ ";section.section_number() = " + section.section_number()
											+ ";section.last_section_number() = "
											+ section.last_section_number()+";flags = "+ flags);
							if(isFulled){
								return;
							}
							if (flags == 0) {
								if (section.table_id() == DvbConst.TID_BAT) {
									if (table != null) {
										int i = table.addSections(section, BAT.bouquet_id(section));
										IPanelLog.d(TAG, "onSectionRetrieved i = " + i);
										if (i == -1) {
											return;
										} else if (i == -2) {
											if (table.isReady()) {
												full = true;
												isFulled = true;
											} else {
												return;
											}
										}
									}
								} else {
									if (table == null ? false : table.addSections(section, 0) < 0) {
										IPanelLog.e(TAG, " ----------");
										IPanelLog.d(TAG, "synchronized TableTask onSectionRetrieved return 2");
										return;
									}
									if (table != null && table.isReady()) {
										full = true;
										isFulled = true;
										IPanelLog.d(TAG, "onSectionRetrieved full = " + full);
									}
								}
							//shanghai OCN ��Ƶ���е�bat���ȥ�����Ĳ�һ�������flagsΪ1�Ĺ��ˡ�
							}else if(flags == 1){
								if (table == null ? false : table.addSections(section, 0) < 0) {
									IPanelLog.e(TAG, " ----------");
									IPanelLog.d(TAG, "synchronized TableTask onSectionRetrieved return 2");
									return;
								}
								if (table != null && table.isReady()) {
									full = true;
									IPanelLog.d(TAG, "onSectionRetrieved full = " + full);
								}
								//������������� ��ɽ��˽�б�
							}else if(flags == 2){
								if (section.table_id() == 0xFC||section.table_id() == 0x8F) {
									if (table != null) {
										//�ο�ֵ��bat�е�bouquet_id������ͬһ��λ�ã���˽����䷽����ȡ
										int i = table.addSections(section, BAT.bouquet_id(section));
										IPanelLog.d(TAG, "onSectionRetrieved i = " + i);
										if (i == -1) {
											return;
										} else if (i == -2) {
											if (table.isReady()) {
												full = true;
												isFulled = true;
											} else {
												return;
											}
										}
									}
								} 
							//ʹ��crcУ��
							}else if(flags == 3){
								byte[] b = new byte[section.getSectionBuffer().getDataLength() - 4];
								section.getSectionBuffer().read(b);
								int crc = SectionBuilder.calculateCRC32(b, b.length);
								IPanelLog.d(TAG,"onSectionRetrieved crc = "+ crc +";section.crc_32() = "+ section.crc_32());
								if(crc != section.crc_32()){
									IPanelLog.d(TAG,"invilid section");
									return;
								}
								if (section.table_id() == DvbConst.TID_BAT) {
									if (table != null) {
										int i = table.addSections(section, BAT.bouquet_id(section));
										IPanelLog.d(TAG, "onSectionRetrieved i = " + i);
										if (i == -1) {
											return;
										} else if (i == -2) {
											if (table.isReady()) {
												full = true;
												isFulled = true;
											} else {
												return;
											}
										}
									}
								} else {
									if (table == null ? false : table.addSections(section, 0) < 0) {
										IPanelLog.e(TAG, " ----------");
										IPanelLog.d(TAG, "synchronized TableTask onSectionRetrieved return 2");
										return;
									}
									if (table != null && table.isReady()) {
										full = true;
										isFulled = true;
										IPanelLog.d(TAG, "onSectionRetrieved full = " + full);
									}
								}
							}
							try {
								checkThenDup(sbuffer, f.getStreamPID(), section.table_id(),
										section.section_number(), section.crc_32());
								lis.onSectionGot(section, full);
							} catch (Exception e) {
								IPanelLog.d(TAG, "synchronized TableTask onSectionRetrieved excetion");
								e.printStackTrace();
							} finally {
								removeit = full;
							}
						}
//					}
					}
				}
					IPanelLog.d(TAG, "synchronized TableTask onSectionRetrieved out 1");
					
				if(removeit){
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							removeTableTask(TableTask.this);
						}
					}).start();	
				}
				
			}

			@Override
			public void run() {
				onTableFullReceivedTimeout();
			}

			@Override
			public void terminate() {
				synchronized (tasks) {
					if (running)
						removeTableTask(this);
				}
			}
		}

	}

	private FreqSearchListener fsl;
	private TransportManager tsManager;
	ResourcesState mPlayResource;
	private StreamSelector mStreamSelector;
	private final String uuid;
	private Object mutex = new Object();
	private FreqTask currentTask = null;
	private long searchFreqTimeout = 3 * 60 * 1000;
	private long tableFullReceivedTimeout = 1 * 60 * 1000;
	private DvbNetworkMapping.TransportStream currentTS = null;
	private HashMap<String,SectionDuplication> dups = new HashMap<String ,SectionDuplication>();

	DvbSearchToolkit(String uuid) {
		this.uuid = uuid;
	}

	// �����������ڽ�Section�洢��SectionStorage
	public void addSectionDuplication(int pid, int tid, int flags) {
		dups.put(pid+"_"+tid, new SectionDuplication(pid, flags));
	}

	class SectionDuplication {
		int pid, flags;

		public SectionDuplication(int pid, int flags) {
			this.pid = pid < 0 ? -1 : pid;
			this.flags = flags;
		}

		public int getFlags() {
			return flags;
		}

		public void setFlags(int flags) {
			this.flags = flags;
		}
	}

	void checkThenDup(SectionBuffer sBuffer, int pid, int tid, int section_number, int crc) {
		SectionDuplication sd = dups.get(pid+"_"+tid);
		int cur_crc = 0;
		IPanelLog.d(TAG, "call checkThenDup crc=" + crc + ",sd=" + sd + ",currentTs=" + currentTS);
		if (sd != null && currentTS != null) {
			if ((pid < 0 ? -1 : pid) == sd.pid) {
				int sLen = sBuffer.getDataLength();
				if (sLen <= 0) {
					return;
				}
				if (sd.getFlags() == SectionSaver.CRC_CHECK) {
					cur_crc = crc;
				} else if (sd.getFlags() == SectionSaver.NO_CRC_CHECK) {
					cur_crc = 0;
				} else if (sd.getFlags() == SectionSaver.OTHER_CHECK) {

				}
				IPanelLog.d(TAG, "freq----" + currentTask.getFrequencyValue());
//				IPanelLog.d(TAG, "ts_id----" + currentTS.siTransportStram.transport_stream_id);
//				IPanelLog.d(TAG, "frequency_info----"
//						+ currentTS.siTransportStram.frequency_info.getFrequencyInfo()
//								.getFrequency());
				IPanelLog.d(TAG, " pid --" + pid + "---tid---" + tid + "---sn---" + section_number
						+ "----crc--" + cur_crc);
				// IPanelLog.d(TAG," ddd--"+currentTS.getSectionBuffer(pid, tid,
				// section_number,cur_crc));
				if (currentTS.getSectionBuffer(pid, tid, section_number, cur_crc) == null) {
					SectionBuffer s = SectionBuffer.createSectionBuffer(sLen);
					if (s != null) {
						int ret = s.copyFrom(sBuffer);
						IPanelLog.d(TAG, "ret = "+ ret);
						currentTS.setSectionBuffer(pid, tid, section_number, cur_crc, s);
					}
				}
			}
		}
	}

	public void setCurrentTransportStream(DvbNetworkMapping.TransportStream currentTS) {
		IPanelLog.d(TAG, "ready to set currentThransportStream");
		this.currentTS = currentTS;
	}
}
