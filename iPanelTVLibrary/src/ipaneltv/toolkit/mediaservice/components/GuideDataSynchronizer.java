package ipaneltv.toolkit.mediaservice.components;

import ipaneltv.dvbsi.Descriptor;
import ipaneltv.toolkit.ASSERT;
import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Section;
import ipaneltv.toolkit.SectionBuffer;
import ipaneltv.toolkit.TimerFormater;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.dvb.DvbConst;
import ipaneltv.toolkit.dvb.DvbObjectification;
import ipaneltv.toolkit.dvb.DvbObjectification.SiEITEvents;
import ipaneltv.toolkit.dvb.DvbObjectification.SiObject;
import ipaneltv.toolkit.mediaservice.LiveNetworkApplication;
import ipaneltv.toolkit.mediaservice.LiveNetworkApplication.AppComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.telecast.NetworkDatabase.Guides;
import android.net.telecast.SectionFilter;
import android.net.telecast.StreamObserver;
import android.net.telecast.dvb.DvbNetworkDatabase;
import android.net.telecast.dvb.DvbNetworkDatabase.ServiceEvents;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.SparseArray;

/**
 * ���ݸ��²��ԣ�
 * <p>
 * 1����ע��program����������д��events������guide��汾
 * <p>
 * 2����ǰ���е�Ƶ������������д��events������guide��汾
 * <p>
 * 3�����ݹ�����flags���Ӹ��²���
 * <p>
 * 
 * ���ݿ�ɾ�����ԣ�
 * <p>
 * 1���洢ʱ��Ƶ��Ϊ��λ����ɾ��
 * <p>
 * 2���洢ʱ��Ƶ��ӽ�Ŀ��Ϣ����ɾ��
 * <p>
 * 3����������ʱ����������ȫ��ɾ��
 * <p>
 */
public class GuideDataSynchronizer extends AppComponent {
	static final String TAG = GuideDataSynchronizer.class.getSimpleName();
	StreamObserver streamObserver;
	SparseArray<TransportStream> tss = new SparseArray<TransportStream>();
	HandlerThread procThread = new HandlerThread(TAG);
	Handler procHandler;
	Timer timer = new Timer();
	boolean prepareShotted = false;
	LinkedList<SectionBuffer> sbPool = new LinkedList<SectionBuffer>();
	int sbPoolOutSideSize = 0;
	private DvbObjectification mObjectf = null;
	private Context mContext;
	private Uri dburi;
	int flag = FALG_SYNC_DEFAULT;
	static final int DURATION_OF_HOUR = 60 * 60 * 1000;

	public static final int FALG_SYNC_DEFAULT = 0;
	public static final int FLAG_SYNC_PRIOR_SECTION = 0x1; // ������ȡ�׸�section����
	public static final int FLAG_SYNC_PREFETCH_SECTION = 0x2; // Ԥ��ȡȡsection����

	@SuppressWarnings("rawtypes")
	public GuideDataSynchronizer(LiveNetworkApplication app, Uri dburi, int flags) {
		super(app);
		mContext = app.getApplicationContext();
		this.dburi = dburi;
		this.flag = flags;
		mObjectf = new DvbObjectification();
		mObjectf.setBouquetNameEncoding("gbk");
		mObjectf.setEventNameEncoding("gbk");
		mObjectf.setNetworkNameEncoding("gbk");
		mObjectf.setServiceNameEncoding("gbk");
		mObjectf.setServiceProviderNameEncoding("utf-8");
		procThread.start();
		procHandler = new Handler(procThread.getLooper());
		IPanelLog.d(TAG, "GuideDataSynchronizer dburi=" + dburi.getAuthority());
	}

	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	public void release() {
		procHandler.getLooper().quit();
	}

	/**
	 * ���าд
	 * 
	 * @return
	 */
	protected DvbObjectification getDvbObjectification() {
		return mObjectf;
	}

	/**
	 * �Ƿ��п���Ԥȡ����<br>
	 * ������Ը�д���ṩ��ID
	 * 
	 * @return Ԥȡ���ݵĻ�����ID
	 */
	protected String getSectionPreretcherId() {
		return null;
	}

	/**
	 * ���าд
	 * 
	 * @return
	 */
	protected boolean isSectionPreretcherOutofDate() {
		return true;
	}

	/**
	 * ������Ը�д�Ըı仺������С��ֵ
	 * 
	 * @return ֵ����Ϊ 4K��������
	 */
	protected int getSectionFilterBufferSize() {
		return 4096 * 1024;
	}

	/**
	 * ������Ը�д�Ըı���ȡEIT���ݵĳ�ʱʱ��
	 * 
	 * @return ֵ
	 */
	protected int getGuideSectinRetrieveTimeout() {
		return 5000;
	}

	/**
	 * �������าд�Ըı���󻺳�������
	 * 
	 * @return
	 */
	protected int getMaxSectionBufferNumber() {
		return 1000 * 2;
	}

	/**
	 * ����ɸ�д
	 * 
	 * @return
	 */
	protected int getFilterMaskByte() {
		return 0xf0;
	}

	/**
	 * ����ɸ�д
	 * 
	 * @return
	 */
	protected DvbObjectification.DescriptorHandler getEitDescriptorHandler() {
		return new DvbObjectification.DescriptorHandler() {
			public void onDescriptorFound(SiObject siobj, Descriptor d) {
				/*-IPanelLog.i(TAG, "onDescriptorFound descriptor tag=" + d.descriptor_tag());*/
			}
		};
	}

	/**
	 * �������าд�Ըı䳣פ����������
	 * 
	 * @return
	 */
	protected int getResidentSectionBufferNumber() {
		return 100;
	}

	/**
	 * �������าд�Ըı�UTCʱ����׼ֵ
	 * 
	 * @ EIT event:time of the event in Universal Time, Co-ordinated (UTC) and
	 * Modified Julian Date (MJD)
	 * <p>
	 * 
	 * EIT���ݸ����ط���׼��һ�£�һ������UTCʱ��Ϊ׼�����������Ҳ����UTC+8ʱ������������
	 * 
	 * @return
	 */
	protected int getUTCTimeZoneBaseValue() {
		return 0;
	}

	long getUTCTimeOffset() {
		return (long) (getUTCTimeZoneBaseValue() * DURATION_OF_HOUR);
	}

	public void observeProgramGuide(final ChannelKey ch, final long focus) {
		synchronized (TAG) {
			ensurePrepared();
		}
		postProc(new Runnable() {
			@Override
			public void run() {
				TransportStream t = tss.get(getSparseKey(ch.getFrequency()));
				if (t == null) {
					t = new TransportStream(ch.getFrequency());
					tss.put(getSparseKey(ch.getFrequency()), t);
				}
				t.observeGuide(ch, focus);
			}
		});
	}

	protected void postProc(Runnable r) {
		postProc(r, false);
	}

	protected void postProc(Runnable r, boolean head) {
		if (head)
			procHandler.postAtFrontOfQueue(r);
		else
			procHandler.post(r);
	}

	void ensurePrepared() {
		IPanelLog.d(TAG, "ensurePrepared prepareShotted = " + prepareShotted);
		if (!prepareShotted) {
			prepareShotted = true;
			streamObserver = getApp().getTransportManager().createObserver(getUUID());
			streamObserver.setStreamStateListener(new StreamObserver.StreamStateListener() {
				int last = 0;

				@Override
				public void onStreamAbsent(StreamObserver o, long freq) {
					IPanelLog.d(TAG, "onStreamAbsent freq = " + freq);
					onStreamPresent(o, freq, 0, last);
				}

				@Override
				public void onStreamPresent(StreamObserver o, final long freq, final int size,
						int prevSize) {
					IPanelLog.d(TAG, "onStreamPresent freq = " + freq + ",size=" + size);
					last = size;
					if(freq < 0 ){
						IPanelLog.d(TAG, "onStreamPresent freq <0");
						return;
					}
					postProc(new Runnable() {
						@Override
						public void run() {
							TransportStream t = tss.get(getSparseKey(freq));
							if (size > 0) {
								if (t == null) {
									t = new TransportStream(freq);
									tss.put(getSparseKey(freq), t);
								}
								t.setEnable(true);
							} else if (size == 0) {
								if (t != null)
									t.setEnable(false);
							}
						}
					}, true/* put at head of queue */);
				}
			});
			streamObserver.queryStreamState();
		}
	}

	SectionBuffer obtenSectionBuffer() {
		SectionBuffer ret = null;
		try {
			synchronized (sbPool) {
				if (sbPool.size() > 0)
					return ret = sbPool.pop();
				if (sbPoolOutSideSize >= getMaxSectionBufferNumber())
					return null;
			}
			return ret = SectionBuffer.createSectionBuffer(4096);
		} finally {
			if (ret != null)
				sbPoolOutSideSize++;
		}
	}

	void recyleSectionBuffer(SectionBuffer sb) {
		ASSERT.assertTrue(sb != null);
		synchronized (sbPool) {
			if (sbPool.size() >= getResidentSectionBufferNumber()) {
				sb.release();
			} else {
				sbPool.push(sb);
			}
			sbPoolOutSideSize--;
		}
	}

	class TransportStream extends TimerTask implements SectionFilter.SectionDisposeListener {
		final long freq;
		SectionFilter filter;
		int presentSize = 0;
		boolean enabled = false, filtering = false, receiveDelayed = true;
		boolean bReceive = false;
		ChannelKey focuChannel, lastfocuChannel;
		long focusTime;
		int focusCount = 0;
		SectionVersionStatus sectionVersionStatus = null;
		GuideScheduleList sch;

		TransportStream(long freq) {
			this.freq = freq;
		}

		void setEnable(boolean b) {
			IPanelLog.d(TAG, "setEnable b=" + b + ",enabled=" + enabled + ",focuChannel=" + focuChannel);
			if (!enabled && b) {
				// focuChannel == null�ļ������
				// 1 Ƶ���������
				// 2 ��һ��
				// 3 Ƶ���л�
				// 4 ������ȡ��ʱ
				if (focuChannel != null /* && receiveDelayed */) // ǰ����ȡ��ɹ���һ��ʱ�����������
					start();
				enabled = b;
				IPanelLog.d(TAG, "setEnable enabled=" + enabled);
			} else if (enabled && !b) {
				enabled = b;
				stop();
			}

			bReceive = false;
		}

		void observeGuide(ChannelKey ch, long focus) {
			focuChannel = ch;
			focusTime = focus;
			bReceive = false;
			IPanelLog.d(TAG, "observeGuide enabled=" + enabled + ",filtering=" + filtering);

			if (enabled && !filtering /* && receiveDelayed */) {
				start();
			}
		}

		boolean checkUpdate() {
			IPanelLog.d(TAG, "focuChannel = "+focuChannel.toString()+" lastfocuChannel = "+lastfocuChannel+" focuChannel.equals(lastfocuChannel = "+focuChannel.equals(lastfocuChannel));
			if (!bReceive && !focuChannel.equals(lastfocuChannel)) {
				bReceive = true;
				lastfocuChannel = focuChannel;
				return true;
			}

			return false;
		}

		void procSection(SectionBuffer sb) {
			Section s = new Section(sb);
			DvbObjectification.SiEITEvents seit = mObjectf.parseEITEvents(s,
					getEitDescriptorHandler());
			int tid = s.table_id();
			int service_id = seit.service_id;
			IPanelLog.d(TAG, "procSection tableid=" + tid + ",service_id=" + service_id+";s.section_number() = "+s.section_number()+";s.last_section_number() = "+s.last_section_number());
			if (sectionVersionStatus.addSections(s, service_id) < 0) {
				IPanelLog.d(TAG, "procSection onSectionRetrieved old section");
				return;
			}

			if (tid >= DvbConst.TID_EIT_ACTUAL_FIRST && tid <= DvbConst.TID_EIT_ACTUAL_LAST) {
				ChannelKey ch = ChannelKey.obten(focuChannel.getFrequency(), service_id);
				sch.addScheduleActual(ch, tid, s.section_number(), seit);

				if ((flag & FLAG_SYNC_PRIOR_SECTION) == FLAG_SYNC_PRIOR_SECTION) {
					if (focuChannel.getProgram() == service_id && checkUpdate()) {
						IPanelLog.d(TAG, "procSection prior service id=" + service_id + ",tid=" + tid);
						writeFocusEventData(focuChannel.getFrequency(), service_id);
					}
				}

				if (sectionVersionStatus.isFull(service_id)) { // ��ǰ�Ľ�Ŀ������ȫ
					IPanelLog.d(TAG, "procSection receive full service id=" + service_id + ",tid=" + tid);

					// flush to db
					writeServiceEventData(focuChannel.getFrequency(), service_id);

					// �����ʱ����Ҳ��ȡ��ɣ���ֹͣ��ȡ��
					if (sectionVersionStatus.isReady()) { // ����Ƶ��������ȫ
						IPanelLog.d(TAG, "procSection receive full freq=" + focuChannel.getFrequency());

						// flush to db
						// writeFreqEventData(focuChannel.getFrequency());

						stop();

						receiveDelayed = false;
						//timer.schedule(this, 30 * 60 * 1000);
					}
				} else if (sectionVersionStatus.isReady()) { // ����Ƶ��������ȫ
					IPanelLog.d(TAG, "procSection receive full freq" + focuChannel.getFrequency());

					// flush to db
					// writeFreqEventData(focuChannel.getFrequency());

					stop();

					/** ��Ƶ���Ѿ���ȡ��ϣ���ʱ��Сʱ */
					receiveDelayed = false;
					//timer.schedule(this, 30 * 60 * 1000);
				}
			}
		}

		int getSparseKey() {
			return GuideDataSynchronizer.getSparseKey(freq);
		}

		private boolean startFilter(SectionFilter f) {
			if (f != null) {
				f.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);
				f.setFrequency(freq);
				f.setCARequired(false);
				f.setTimeout(getGuideSectinRetrieveTimeout());
				byte[] coef = new byte[] { (byte) DvbConst.TID_EIT_ACTUAL_FIRST };
				byte[] mask = new byte[] { (byte) getFilterMaskByte() };
				byte[] excl = new byte[] { (byte) 0x00 };
				return f.start(DvbConst.PID_EIT, coef, mask, excl, 1);
			}
			return false;
		}

		public synchronized boolean start() {
			if (!filtering) {
				sectionVersionStatus = new SectionVersionStatus();
				sch = new GuideScheduleList(dburi.getAuthority());
				if (filter == null) {
					filter = getApp().getTransportManager().createFilter(getUUID(),
							getSectionFilterBufferSize());
					if (filter == null)
						return false;
					filter.setSectionDisposeListener(this);
				}
				IPanelLog.d(TAG, "start filter = " + filter);
				filtering = startFilter(filter);
			}
			IPanelLog.d(TAG, "start result = " + filtering);
			return filtering;
		}

		public synchronized void writeFreqEventData(long freq) { // ÿ��service id���ݶ��洢�ˣ��϶�����Ҫ��freq�洢��
			if (sch != null) {
				// delete db
				sch.deleteEpgEventsByFreq(freq);

				// flush to db
				sch.writeAllEpgScheculeEvent(freq);
				sch.clearAllSiEvent();
			}
		}

		public synchronized void writeServiceEventData(long freq, int serviceId) {
			if (sch != null) {
				// delete db
				sch.deleteEpgEventsByServieId(freq, serviceId);

				// flush to db
				sch.writeEpgScheduleServiceId(freq, serviceId);
				sch.clearSiEventService(ChannelKey.obten(freq, serviceId));
			}
		}

		public synchronized void writeFocusEventData(long freq, int serviceId) {
			if (sch != null) {
				// delete db
				sch.deleteEpgEventsByServieId(freq, serviceId);
				
				// flush to db
				sch.writeEpgScheduleServiceId(freq, serviceId);
				// �ڴ˲������������
			}
		}

		public synchronized void stop() {
			IPanelLog.d(TAG, "stop result = " + filtering);
			if (filtering) {
				filtering = false;
				enabled = false;

				sch = null;
				sectionVersionStatus = null;
				filter.stop();
			}
		}

		@Override
		public void onStreamLost(SectionFilter f) {// ��Ƶ��
			IPanelLog.d(TAG, "call onStreamLost");
			postProc(new Runnable() {
				@Override
				public void run() {
					stop();
					if (enabled)
						start();// restart
				}
			}, true);
		}

		@Override
		public void onReceiveTimeout(SectionFilter f) {// ��ʱδ��ȡ���κ�����
			postProc(new Runnable() {
				@Override
				public void run() {
					stop();
				}
			}, true);
		}

		@Override
		public void onSectionRetrieved(SectionFilter f, int len) {
			final SectionBuffer sb = obtenSectionBuffer();
			if (sb == null) {
				IPanelLog.w(TAG, "SectionBuffer insufficient ,data drop!");
				return;
			}
			sb.copyFrom(f);
			postProc(new Runnable() {
				@Override
				public void run() {
					try {
						if (enabled && filtering)
							procSection(sb);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						recyleSectionBuffer(sb);
					}
				}
			});
		}

		@Override
		public void run() {
			receiveDelayed = true;
		}

	}

	static int getSparseKey(long f) {
		return (int) f / 1000;
	}

	public class GuideScheduleList {
		static final String TAG = "GuideScheduleList";
		String mAuthority; // EPG ContentProvider Uri ��ַ

		// ChannelKey ��Ϊ����
		private HashMap<ChannelKey, EitEventGroup> siEventsSchedule = new HashMap<ChannelKey, EitEventGroup>();

		class EitEventGroup {
			int eit_last_tid = 0x50;
			int eit_last_sn = 0;
			private SiEITEvents[/* tid-0x50 */][/* sn */] siServiceEvents;
		}

//		HashMap<ChannelKey, EitEventKey> eventKeys = new HashMap<ChannelKey, EitEventKey>();
//
//		public EitEventKey getEventKey(ChannelKey ch, int tableid) {
//			synchronized (eventKeys) {
//				EitEventKey k = eventKeys.get(ch);
//				if (k == null) {
//					k = new EitEventKey(ch, tableid);
//					eventKeys.put(ch, k);
//				}
//				return k;
//			}
//		}
//
//		class EitEventKey {
//			ChannelKey ch;
//			int tableid;
//
//			public EitEventKey(ChannelKey _ch, int _tid) {
//				this.ch = _ch;
//				this.tableid = _tid;
//			}
//
//			public void clearAll() {
//				eventKeys.clear();
//			}
//
//			@Override
//			public int hashCode() {
//				return ch.hashCode() + (tableid << 16);
//			}
//
//			@Override
//			public boolean equals(Object o) {
//				if (o instanceof EitEventKey) {
//					EitEventKey ol = (EitEventKey) o;
//					if (ol.equals(ch)) {
//						if (ol.tableid == tableid) {
//							return true;
//						}
//					}
//				}
//				return false;
//			}
//
//			@Override
//			public String toString() {
//				return ch.toString() + "-" + tableid;
//			}
//		}

		GuideScheduleList(String authority) {
			this.mAuthority = authority;
		}

		public void addScheduleActual(ChannelKey ch, int tableid, int section_number,
				DvbObjectification.SiEITEvents se) {
			EitEventGroup eg = siEventsSchedule.get(ch);
			if (eg == null) {
				eg = new EitEventGroup();
				eg.siServiceEvents = new SiEITEvents[16][256];
				siEventsSchedule.put(ch, eg);
			}
			eg.siServiceEvents[tableid - 0x50][section_number] = se;
			if (eg.eit_last_tid < tableid)
				eg.eit_last_tid = tableid;
			if (eg.eit_last_sn < section_number)
				eg.eit_last_sn = section_number;
		}

		public SiEITEvents getSiEventsSchedule(EitEventGroup eg, int tableid, int section_number) {
			if (eg == null)
				return null;
			return eg.siServiceEvents[tableid - 0x50][section_number];
		}

		public void clearAllSiEvent() {
			synchronized (siEventsSchedule) {
				for (ChannelKey ch : siEventsSchedule.keySet()) {
					EitEventGroup eg = siEventsSchedule.get(ch);
					if (eg != null) {
						eg.eit_last_sn = 0x50;
						eg.eit_last_tid = 0;
						eg.siServiceEvents = null;
					}
				}
				siEventsSchedule.clear();
			}
		}

		public void clearSiEventService(ChannelKey ch) {
			synchronized (siEventsSchedule) {
				EitEventGroup eg = siEventsSchedule.get(ch);
				if (eg != null) {
					eg.eit_last_sn = 0x50;
					eg.eit_last_tid = 0;
					eg.siServiceEvents = null;
					siEventsSchedule.remove(ch);
				}
			}
		}

		private String[] getArgs(ChannelKey key) {
			return new String[] { key.getFrequency() + "", key.getProgram() + "" };
		}

		private String getEitAuthority() {
			return mAuthority;
		}

		private Uri getEitEventsProviderUri() {
			String table = ServiceEvents.TABLE_NAME;
			return Uri.parse("content://" + mAuthority + "/" + table);
		}

		private Uri getEitGuidesProviderUri(ChannelKey key) {
			String table = Guides.TABLE_NAME;
			return Uri.parse("content://" + mAuthority + "/" + table);
		}

		/** ������Ƶ��Ϊ��λ����ɾ��Events������ */
		public void deleteEpgEventsByFreq(long freq) {
			Uri epg_provider_insert_uri = getEitEventsProviderUri();
			String where = ServiceEvents.FREQUENCY + "=" + freq;
			IPanelLog.d(TAG, "deleteEpgEventsByFreq freq = " + freq);
			try {
				mContext.getContentResolver().delete(epg_provider_insert_uri, where, null);
			} catch (Exception e) {
				IPanelLog.d(TAG, "deleteEpgEventsByFreq error:" + e);
				e.printStackTrace();
			}
		}

		/** ������Ƶ��+��Ŀ��Ϊ��λ����ɾ��Events������ */
		public void deleteEpgEventsByServieId(long freq, int serviceId) {
			Uri epg_provider_insert_uri = getEitEventsProviderUri();
			String where = ServiceEvents.FREQUENCY + "=? and " + ServiceEvents.PROGRAM_NUMBER
					+ "=?";
			String[] selectionArgs = new String[] { freq + "", serviceId + "" };

			IPanelLog.d(TAG, "deleteEpgEventsByServieId freq= " + freq + ",program number=" + serviceId);
			try {
				mContext.getContentResolver().delete(epg_provider_insert_uri, where, selectionArgs);
			} catch (Exception e) {
				IPanelLog.d(TAG, "deleteEpgEventsByServieId error:" + e);
				e.printStackTrace();
			}
		}

		/**
		 * duration: A 24-bit field containing the duration of the event in hours, minutes, 
		 * seconds. format: 6 digits, 4-bit BCD = 24 bit. 
		 * EXAMPLE:  01:45:30 is coded as "0x014530".
		 */
		private long formatDuration(long duration) {
			int flag = 1;
			long j = 0;
			String s = Long.toHexString(duration);
			int i = s.length();
			while (i > 0) {
				if (flag > 3600) {
					break;
				}
				String s1 = s.substring(i - 2 > 0 ? i - 2 : 0, i);
				j = j + Integer.parseInt(s1) * flag;
				i = i - 2;
				flag = flag * 60;

			}
			j = j * 1000;
			return j;
		}

		/** д���ע��Events���� */
		boolean writeFocusEvents(ChannelKey key, SiEITEvents seit) {
			boolean bSucc = false;
			Uri insert_uri = getEitEventsProviderUri();
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			IPanelLog.d(TAG, "writeFocusEvents uri = " + insert_uri);
			if (seit == null ? true : seit.event == null) {
				IPanelLog.d(TAG, "writeFocusEvents sievent is null");
				return false;
			}

			int ssize = seit.event.size();
			IPanelLog.d(TAG, "writeFocusEvents event_size = " + ssize);
			for (int k = 0; k < ssize; k++) {
				SiEITEvents.Event se = seit.event.get(k);
				if (se == null) {
					IPanelLog.d(TAG, "writeFocusEvents event is null");
					continue;
				}

				long start_time = TimerFormater.rfc3339tolong(se.start_time) + getUTCTimeOffset();
				long duration = formatDuration(se.duration);
				long end_time = start_time + duration;
				IPanelLog.d(TAG, "pn=" + seit.service_id + ",start_time=" + start_time
						+ ",se.start_time=" + se.start_time);
				ContentValues values = new ContentValues();
				values.put(DvbNetworkDatabase.Events.FREQUENCY, key.getFrequency());
				values.put(DvbNetworkDatabase.Events.PROGRAM_NUMBER, seit.service_id);
				values.put(DvbNetworkDatabase.Events.EVENT_NAME, se.event_name);
				values.put(DvbNetworkDatabase.Events.EVENT_NAME_EN, se.event_name_en);
				values.put(DvbNetworkDatabase.Events.START_TIME, start_time);
				values.put(DvbNetworkDatabase.Events.DURATION, duration);
				values.put(DvbNetworkDatabase.Events.END_TIME, end_time);
				values.put(DvbNetworkDatabase.ServiceEvents.EVENT_ID, se.event_id);
				values.put(DvbNetworkDatabase.ServiceEvents.IS_FREE_CA, se.free_ca_mode);
				values.put(DvbNetworkDatabase.ServiceEvents.RUNNING_STATUS, se.running_status);
				values.put(DvbNetworkDatabase.ServiceEvents.SHORT_EVENT_NAME, se.short_event_name);

				operations.add(ContentProviderOperation.newInsert(insert_uri).withValues(values)
						.build());
			}

			/** ����������д�����ݿ� */
			if (operations != null && operations.size() > 0) {
				try {
					ContentProviderResult[] results = mContext.getContentResolver().applyBatch(
							getEitAuthority(), operations);
					IPanelLog.d(TAG, "writeFocusEvents service_id =" + seit.service_id);
					if (results != null) {
						IPanelLog.d(TAG, "writeFocusEvents bsucc=" + bSucc);

						// ����һ�°汾��
						updateGuideVersion(key);
						bSucc = true;
					}
				} catch (Exception e) {
					IPanelLog.d(TAG, "writeFocusEvents error=" + e);
					e.printStackTrace();
				}
			}

			return bSucc;
		}
		
		/** ����Guide��汾 */
		void updateGuideVersion(ChannelKey key) {
			Uri epg_provider_update_uri = getEitGuidesProviderUri(key);
			String selection = Guides.FREQUENCY + "=? and " + Guides.PROGRAM_NUMBER + "=?";
			String[] selectionArgs = getArgs(key);
			IPanelLog.d(TAG, "updateGuideVersion epg_provider_update_uri=" + epg_provider_update_uri);
			IPanelLog.d(TAG, "updateGuideVersion freq=" + key.getFrequency() + ",pn=" + key.getProgram());
			Cursor cur = mContext.getContentResolver().query(epg_provider_update_uri, null,
					selection, selectionArgs, null);
			if (cur != null) {
				if (cur.moveToFirst()) {
					do {
						int version = cur.getInt(cur.getColumnIndex("version"));
						ContentValues updateValues = new ContentValues();
						updateValues.put("version", ++version);
						mContext.getContentResolver().update(epg_provider_update_uri, updateValues,
								selection, selectionArgs);
						IPanelLog.d(TAG, "updateGuideVersion version=" + version);
					} while (cur.moveToNext());
				}
			}
		}

		void insertEpgContentValues(ChannelKey ch, SiEITEvents.Event se,
				ArrayList<ContentProviderOperation> operations) {
			Uri epg_provider_insert_uri = getEitEventsProviderUri();
			if (se != null) {
				ContentValues values = new ContentValues();
				values.put(DvbNetworkDatabase.Events.FREQUENCY, ch.getFrequency());
				values.put(DvbNetworkDatabase.Events.PROGRAM_NUMBER, ch.getProgram());
				values.put(DvbNetworkDatabase.Events.EVENT_NAME, se.event_name);
				values.put(DvbNetworkDatabase.Events.EVENT_NAME_EN, se.event_name_en);
				long start_time = TimerFormater.rfc3339tolong(se.start_time) + getUTCTimeOffset();
				long duration = formatDuration(se.duration);
				long end_time = start_time + duration;
				values.put(DvbNetworkDatabase.Events.START_TIME, start_time);
				values.put(DvbNetworkDatabase.Events.END_TIME, end_time);
				values.put(DvbNetworkDatabase.Events.DURATION, duration);
				values.put(DvbNetworkDatabase.ServiceEvents.EVENT_ID, se.event_id);
				values.put(DvbNetworkDatabase.ServiceEvents.IS_FREE_CA, se.free_ca_mode);
				values.put(DvbNetworkDatabase.ServiceEvents.RUNNING_STATUS, se.running_status);
				values.put(DvbNetworkDatabase.ServiceEvents.SHORT_EVENT_NAME, se.short_event_name);
				operations.add(ContentProviderOperation.newInsert(epg_provider_insert_uri)
						.withValues(values).build());

				IPanelLog.d(TAG, "start_time=" + start_time + ",se.start_time=" + se.start_time);
				IPanelLog.d(TAG, "end_time=" + end_time);
				IPanelLog.d(TAG, "duration=" + duration);
				IPanelLog.d(TAG, "event_id=" + se.event_id);
				IPanelLog.d(TAG, "free_ca_mode=" + se.free_ca_mode);
				IPanelLog.d(TAG, "event_name=" + se.event_name);
				IPanelLog.d(TAG, "program_number=" + ch.getProgram());
			}
		}

		boolean writeAllEpgScheculeEvent(long freq) {
			boolean result = false;
			IPanelLog.d(TAG, "writeAllEpgScheculeEvent freq= " + freq);
			int program_number = 0;

			synchronized (siEventsSchedule) {
				for (ChannelKey key : siEventsSchedule.keySet()) {
					EitEventGroup eg = siEventsSchedule.get(key);
					if (eg == null) {
						IPanelLog.d(TAG, "writeAllEpgScheculeEvent null");
						continue;
					}

					ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

					IPanelLog.d(TAG, "writeAllEpgScheculeEvent last_tid=" + eg.eit_last_tid + ",last_sn="
							+ eg.eit_last_sn);
					for (int tid = DvbConst.TID_EIT_ACTUAL_FIRST; tid <= eg.eit_last_tid; tid++) {
						for (int j = 0; j <= eg.eit_last_sn; j += 8) {
							SiEITEvents seit = getSiEventsSchedule(eg, tid, j);
							if (seit == null) {
								IPanelLog.d(TAG, "writeAllEpgScheculeEvent seit null");
								continue;
							}
							program_number = seit.service_id;
							if (seit.event == null) {
								IPanelLog.d(TAG, "writeAllEpgScheculeEvent event null");
								continue;
							}
							int ssize = seit.event.size();
							IPanelLog.d(TAG, "writeAllEpgScheculeEvent program_number = "
									+ program_number);
							for (int k = 0; k < ssize; k++) {
								SiEITEvents.Event se = seit.event.get(k);
								ChannelKey ch = ChannelKey.obten(freq, program_number);
								insertEpgContentValues(ch, se, operations);
							}
						}
					}

					/** ����������д�����ݿ� */
					if (operations != null && operations.size() > 0) {
						try {
							ContentProviderResult[] results = mContext.getContentResolver()
									.applyBatch(getEitAuthority(), operations);
							IPanelLog.d(TAG, "writeAllEpgScheculeEvent results =" + results);
							if (results != null) {
								IPanelLog.d(TAG, "writeAllEpgScheculeEvent freq=" + freq + ",pn="
										+ program_number);
								// ����һ�°汾��
								updateGuideVersion(ChannelKey.obten(freq, program_number));
								result = true;
							}
						} catch (Exception e) {
							IPanelLog.d(TAG, "writeAllEpgScheculeEvent e=" + e);
							e.printStackTrace();
						}
					}
				}
			}
			
			return result;
		}

		boolean writeEpgScheduleServiceId(long freq, int service_id) {
			boolean result = false;
			IPanelLog.d(TAG, "writeEpgScheduleServiceId freq= " + freq);
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			int program_number = 0;

			synchronized (siEventsSchedule) {
				ChannelKey key = ChannelKey.obten(freq, service_id);
				if (key == null) {
					IPanelLog.d(TAG, "writeEpgScheduleServiceId key is null");
					return false;
				}

				EitEventGroup eg = siEventsSchedule.get(key);
				if (eg == null) {
					IPanelLog.d(TAG, "writeEpgScheduleServiceId eg is null");
					return false;
				}

				IPanelLog.d(TAG, "writeEpgScheduleServiceId last_tid=" + eg.eit_last_tid + ",last_sn="
						+ eg.eit_last_sn);
				for (int tid = DvbConst.TID_EIT_ACTUAL_FIRST; tid <= eg.eit_last_tid; tid++) {
					for (int j = 0; j <= eg.eit_last_sn; j += 8) {
						SiEITEvents seit = getSiEventsSchedule(eg, tid, j);
						if (seit == null) {
							IPanelLog.d(TAG, "writeEpgScheduleServiceId seit null");
							continue;
						}
						program_number = seit.service_id;
						if (seit.event == null) {
							IPanelLog.d(TAG, "writeEpgScheduleServiceId event null");
							continue;
						}
						int ssize = seit.event.size();
						IPanelLog.d(TAG, "writeEpgScheduleServiceId program_number = " + program_number);
						for (int k = 0; k < ssize; k++) {
							SiEITEvents.Event se = seit.event.get(k);
							ChannelKey ch = ChannelKey.obten(freq, program_number);
							insertEpgContentValues(ch, se, operations);
						}
					}
				}

				ASSERT.assertTrue(program_number == service_id);
				/** ����������д�����ݿ� */
				if (operations != null && operations.size() > 0) {
					try {
						ContentProviderResult[] results = mContext.getContentResolver().applyBatch(
								getEitAuthority(), operations);
						IPanelLog.d(TAG, "writeEpgScheduleServiceId results =" + results);
						if (results != null) {
							IPanelLog.d(TAG, "writeEpgScheduleServiceId freq=" + freq + ",pn="
									+ program_number);
							// ����һ�°汾��
							updateGuideVersion(ChannelKey.obten(freq, program_number));
							result = true;
						}
					} catch (Exception e) {
						IPanelLog.d(TAG, "writeEpgScheduleServiceId e=" + e);
						e.printStackTrace();
					}
				}
			}

			return result;
		}
	}
}
