package ipaneltv.toolkit.db;

import ipaneltv.toolkit.TimeToolkit;
import ipaneltv.toolkit.db.DatabaseCursorHandler.ChannelCursorHandler;
import ipaneltv.toolkit.db.DatabaseCursorHandler.EventCursorHandler;
import ipaneltv.toolkit.db.DatabaseCursorHandler.GroupCursorHandler;
import ipaneltv.toolkit.db.DatabaseCursorHandler.GuideCursorHandler;
import ipaneltv.toolkit.db.DatabaseObjectification.Channel;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Group;
import ipaneltv.toolkit.db.DatabaseObjectification.Guide;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.net.telecast.NetworkDatabase;
import android.net.telecast.NetworkDatabase.Events;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;

public abstract class LiveProgramNavigator2 {
	static final String TAG = LiveProgramNavigator2.class.getName();
	static final int MASK_GROUP = 0x01, MASK_CHANNEL = 0x02, MASK_GUIDE = 0x04, MASK_EVENT = 0x08,
			MASK_TIMEUPDATE = 0x0F;
	private boolean preloadShotted = false;
	private HandlerThread groupThread = new HandlerThread("navi-group",
			Process.THREAD_PRIORITY_BACKGROUND);
	private HandlerThread channelThread = new HandlerThread("navi-channel",
			Process.THREAD_PRIORITY_BACKGROUND);
	private HandlerThread eventThread = new HandlerThread("navi-event",
			Process.THREAD_PRIORITY_BACKGROUND);
	private HandlerThread timeThread = new HandlerThread("navi-time",
			Process.THREAD_PRIORITY_BACKGROUND);
	private HandlerThread queryThread = new HandlerThread("navi-query",
			Process.THREAD_PRIORITY_BACKGROUND);
	private Context context;
	private Handler groupHandler, channelHandler, eventHandler, queryHandler;
	private Uri uri, channelsUri, groupsUri, eventsUri, guideUri;
	private Timer presentFollowTimer = new Timer();
	private SparseArray<Group> loadedGroups;
	private HashMap<ChannelKey, Channel> loaddedChannels;
	private HashMap<ChannelKey, Guide> loaddedGuides;
	private final Object loadMutex = new Object();
	private int loaddedCode = 0, loaddedGuidesVersion = 0;
	private PresentFollowsCheckTimerTask presentFollowsChecker = null;
	private boolean loadded = false;

	private ContentObserver guideObserver, channelObserver, groupObserver;

	private static TimeUpdateHandler timeHandler;
	static Timer timer = new Timer();
	public static final int DURATION_OF_DAY = 24 * 60 * 60 * 1000;
	public static final int CONPUTER_BASIC_TIME = 1970;
	public static final int PERIOD_TIME = 500;

	public static SimpleDateFormat formatter_a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public LiveProgramNavigator2(Context context, Uri uri) {
		this.context = context;
		this.uri = uri;
	}

	protected void close() {
		Looper lp;
		if ((lp = eventThread.getLooper()) != null)
			lp.quit();
		if ((lp = channelThread.getLooper()) != null)
			lp.quit();
		if ((lp = groupThread.getLooper()) != null)
			lp.quit();
		if ((lp = queryThread.getLooper()) != null)
			lp.quit();
	}

	protected abstract ChannelCursorHandler getLoadChannelCursorHandler(Context context, Uri uri,
			Handler handler);

	protected abstract GroupCursorHandler getLoadGroupCursorHandler(Context context, Uri uri,
			Handler handler);

	protected abstract EventCursorHandler getLoadEventCursorHandler(Context context, Uri uri,
			Handler handler);

	protected abstract GuideCursorHandler getLoadGuideCursorHandler(Context context, Uri uri,
			Handler handler);

	protected abstract EventCursorHandler createUpdateEventCursorHandler(Context context, Uri uri,
			Handler handler, ChannelKey ch);

	public static interface Listener {
		void onLoadFinished();
		
		void onEventsFinished();

		void onGroupsUpdate();

		void onChannelsUpdated();

		void onProgramsUpdated(ChannelKey key);

		void onPresentFollowUpdated();
	}

	protected synchronized void onLoadFinished() {
		loadded = true;
		Listener l = lis;
		if (l != null && l != loaddedNotified) {
			loaddedNotified = l;
			l.onLoadFinished();
		}
	}
	
	protected void onEventsFinished(){
		Listener l = lis;
		if (l != null)
			l.onEventsFinished();
	}

	protected void onGroupsUpdate() {
		Listener l = lis;
		if (l != null)
			l.onGroupsUpdate();
	}

	protected void onChannelsUpdated() {
		Listener l = lis;
		if (l != null)
			l.onChannelsUpdated();
	}

	protected void onProgramsUpdated(ChannelKey key) {
		Listener l = lis;
		if (l != null)
			l.onProgramsUpdated(key);
	}

	protected void onPresentFollowUpdated() {
		Listener l = lis;
		if (l != null)
			l.onPresentFollowUpdated();
	}

	private Listener lis, loaddedNotified = null;

	public synchronized void setListener(Listener l) {
		lis = l;
		loaddedNotified = null;
	}

	public synchronized void queryState() {
		Log.d(TAG, "queryState");
		queryHandler.post(new Runnable() {

			@Override
			public void run() {
				if (loadded) {
					Listener l = lis;
					if (l != null && loaddedNotified == null) {
						loaddedNotified = l;
						l.onLoadFinished();
					}
				}
			}
		});
		Log.d(TAG, "queryState end");
	}

	protected HashMap<ChannelKey, Channel> getLoaddedChannels() {
		return loaddedChannels;
	}

	protected Object getLockMutex() {
		return loadMutex;
	}

	protected SparseArray<Group> getLoadedGroups() {
		return loadedGroups;
	}

	protected List<Program> getLoadedPrograms(ChannelKey ch) {
		/*
		 * SoftReference<List<Program>> p_list = loaddedGuides.get(ch).list;
		 * return p_list==null?null : p_list.get();
		 */

		if (loaddedGuides != null && loaddedGuides.get(ch) != null) {
			List<Program> p_list = loaddedGuides.get(ch).list2;
			return p_list;
		} else {
			return null;
		}

	}

	protected Program[] getLoaddedPresentFollows(ChannelKey key) {
		// Log.i(TAG, "------------getLoaddedPresentFollows   key="+key);
		if (loaddedGuides.get(key) != null)
			return loaddedGuides.get(key).pf;
		return null;
	}

	public void queryChannelGuide(final ChannelKey ch) {
		eventHandler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (loadMutex) {
					if (loaddedGuides != null)
						Log.d(TAG, "loaddedGuides size = " + loaddedGuides.size());
					else
						Log.d(TAG, "loaddedGuides == null");
					Guide g = loaddedGuides.get(ch);
					List<Program> list = null;// 这句不要改
					if (g.list != null ? (list = g.list) == null : false)
						g.list = null;
					if (list == null) {
						Log.d(TAG, "rquest the database again");
						updateProgramEvents(ch);
					} else {
						Log.d(TAG, "get epg and notify");
						onProgramsUpdated(ch);
					}
				}
			}
		});
	}

	public final synchronized void preload() {
		Log.i(TAG, "--------------preload-method--preloadShotted=" + preloadShotted);
		if (preloadShotted)
			return;
		preloadShotted = true;
		groupThread.start();
		groupHandler = new Handler(groupThread.getLooper());
		channelThread.start();
		channelHandler = new Handler(channelThread.getLooper());
		eventThread.start();
		eventHandler = new Handler(eventThread.getLooper());
		timeThread.start();
		timeHandler = new TimeUpdateHandler(timeThread.getLooper());
		queryThread.start();
		queryHandler = new Handler(queryThread.getLooper());

		channelsUri = Uri.withAppendedPath(uri, NetworkDatabase.Channels.TABLE_NAME);
		groupsUri = Uri.withAppendedPath(uri, NetworkDatabase.Groups.TABLE_NAME);
		eventsUri = Uri.withAppendedPath(uri, NetworkDatabase.Events.TABLE_NAME);
		guideUri = Uri.withAppendedPath(uri, NetworkDatabase.Guides.TABLE_NAME);
		postLoadGroups();
		postLoadChannels();
		postLoadGuides();
//		postLoadEvents();
	}

	private void procQueryEnd(int mask) {
		Log.d(TAG, "procQueryEnd mask = " + mask);
		if ((loaddedCode & 7) == 7) {
			switch (mask) {
			case MASK_GROUP:
				onGroupsUpdate();
				break;
			case MASK_CHANNEL:
				onChannelsUpdated();
				break;
			case MASK_GUIDE:
				break;
			case MASK_EVENT:
				Log.i(TAG, "----------MASK_EVENT-");
				onEventsFinished();
				break;
			}
		} else {
			loaddedCode = loaddedCode | mask;
			if ((loaddedCode & 7) == 7) {
				onLoadFinished();
			}
		}
	}

	protected void postLoadGroups() {
		final GroupCursorHandler lg = getLoadGroupCursorHandler(context, groupsUri, groupHandler);
		lg.setQueryHandler(new QueryHandler() {
			@Override
			public void onQueryStart() {
				if (groupObserver == null)
					context.getContentResolver().registerContentObserver(groupsUri, false,
							setGroupObserver());
			}

			@Override
			public void onQueryEnd() {
				synchronized (loadMutex) {
					Log.d(TAG, "postLoadGroups loaddedCode=" + loaddedCode);
					loadedGroups = lg.groups;
					if (loadedGroups != null)
						Log.d(TAG,
								"after query groups loadedGroups.size() = " + loadedGroups.size());
					else
						Log.d(TAG, "loadedGroups == null");
					procQueryEnd(MASK_GROUP);
				}
			}
		});
		lg.postQuery();
	}

	protected void postLoadChannels() {
		final ChannelCursorHandler lc = getLoadChannelCursorHandler(context, channelsUri,
				channelHandler);
		lc.setQueryHandler(new QueryHandler() {
			@Override
			public void onQueryStart() {
				if (channelObserver == null)
					context.getContentResolver().registerContentObserver(channelsUri, false,
							setChannelObserver());
			}

			@Override
			public void onQueryEnd() {
				synchronized (loadMutex) {
					Log.d(TAG, "postLoadChannels loaddedCode=" + loaddedCode);
					loaddedChannels = lc.channels;
					if (loaddedChannels != null)
						Log.d(TAG, "after query channels loaddedChannels.size() = "
								+ loaddedChannels.size());
					else
						Log.d(TAG, "loaddedChannels == null");
					procQueryEnd(MASK_CHANNEL);
				}
			}
		});
		lc.postQuery();
	}

	private void procGuideListUpdated(HashMap<ChannelKey, Guide> olds,
			HashMap<ChannelKey, Guide> news) {
		List<Guide> removed = new ArrayList<Guide>();
		List<Guide> changed = new ArrayList<Guide>();
		for (Entry<ChannelKey, Guide> e : olds.entrySet()) {
			Guide g = news.get(e.getKey());
			if (g == null)
				removed.add(e.getValue());
			else if (g.version != e.getValue().version) {
				changed.add(e.getValue());
			}
		}
		for (Entry<ChannelKey, Guide> e : news.entrySet()) {
			if (!olds.containsKey(e.getKey()))
				changed.add(e.getValue());
		}

		/*
		 * for (Entry<ChannelKey, Guide> e : loaddedGuides.entrySet()){
		 * Log.i(TAG,
		 * "-----111-----validate loaddedGuides the channel:"+e.getValue
		 * ().getChannelKey
		 * ().toString()+"  event list num="+e.getValue().list2.size()); }
		 */

		for (Guide g : removed) {
			Log.i(TAG, "-----222-----validate loaddedGuides the channel:"
					+ g.getChannelKey().toString());
			loaddedGuides.remove(g.key);
		}
		Log.i(TAG,
				"----old.size() is zero--pre--loaddedGuides---" + loaddedGuides.size()
						+ ";changed.size=" + changed.size() + ";news=" + news.size() + ";olds="
						+ olds.size());
		// loaddedGuides = news;

		for (Guide g : changed) {
			loaddedGuides.put(g.key, news.get(g.key));
			updateProgramEvents(g.key);
		}

		/*
		 * for (Entry<ChannelKey, Guide> e : loaddedGuides.entrySet()){
		 * Log.i(TAG,
		 * "-----333-----validate loaddedGuides the channel:"+e.getValue
		 * ().getChannelKey
		 * ().toString()+"  event list num="+e.getValue().list2.size()); }
		 */
	}

	protected void postLoadGuides() {
		final GuideCursorHandler lg = getLoadGuideCursorHandler(context, guideUri, eventHandler);
		lg.setQueryHandler(new QueryHandler() {
			@Override
			public void onQueryStart() {
				if (guideObserver == null) {
					context.getContentResolver().registerContentObserver(guideUri, false,
							setGuideObserver());
				}
			}

			@Override
			public void onQueryEnd() {
				synchronized (loadMutex) {
					Log.d(TAG, "postLoadGuides 11= " + loaddedGuides + ";  loaddedCode="
							+ loaddedCode);
					if (loaddedGuides == null) {
						loaddedGuides = lg.guides;
					} else {
						procGuideListUpdated(loaddedGuides, lg.guides);
						// 如果 ，第一次loaddedGuides的大小为0的话，
						// loaddedGuides = lg.guides;
					}
					Log.d(TAG, "postLoadGuides 22= " + loaddedGuides);
					procQueryEnd(MASK_GUIDE);
				}
			}
		});
		lg.postQuery();
	}

	protected void postLoadEvents() {
		final EventCursorHandler le = getLoadEventCursorHandler(context, eventsUri, eventHandler);
		le.setQueryHandler(new QueryHandler() {
			@Override
			public void onQueryEnd() {
				synchronized (loadMutex) {
					Log.d(TAG, "postLoadEvents loaddedCode 22=" + loaddedCode
							+ ";le.programs.entrySet().size() = " + le.programs.entrySet().size());
					Guide g = null;
					for (Entry<ChannelKey, List<Program>> e : le.programs.entrySet()) {
						if (g != null ? !g.key.equals(e.getKey()) : false) {
							g = null;
						}
						if ((g = loaddedGuides.get(e.getKey())) != null) {
							procUpdateGuidePrograms(g, e.getValue());
							if (g.pf != null) {
								g.isPFLoadedNum = 1;
							}
						}
						Log.d(TAG, "postLoadEvents e.getKey() = " + e.getKey() + ";g = " + g);
					}
					Log.d(TAG, "----key:" + (g != null ? g.getChannelKey().toString() : "G = NULL")
							+ "->le=" + le + ";le.programs=" + le.programs.entrySet().size());
					presentFollowsChecker = new PresentFollowsCheckTimerTask(loaddedGuidesVersion);
					presentFollowTimer.schedule(presentFollowsChecker, 0);
					procQueryEnd(MASK_EVENT);
				}
			}
		});
		le.postQuery();
	}

	private List<Program> procUpdateGuidePrograms(Guide g, List<Program> got) {
		// Log.i("presentandfollow", "g 3 procUpdateGuidePrograms is go in");
		int n = got.size();
		List<Program> list = new ArrayList<Program>();
		for (int i = 2; i < n; i++) {
			list.add(got.get(i));
		}
		// 原先采用的
		g.list = list;
		g.list2 = list;
		/*
		 * for (Program p : list) { Log.i(TAG,"------list------appoint - key:"+
		 * g.getChannelKey().toString()+";name="+(p != null ? p.getName() :
		 * null)+";time="+(p != null ?
		 * (p.getStart()+"-:-"+getTodayTime(p.getStart())) : null)); }
		 */
		if (g.pf == null) {
			g.pf = new Program[] { got.get(0), got.get(1) };
		} else {
			if (got.get(0) != null && g.pf[0] == null)
				g.pf[0] = got.get(0);
			if (got.get(1) != null && g.pf[1] == null)
				g.pf[1] = got.get(1);
		}
		for (Program p : g.pf) {
			Log.i(TAG,
					"------pf------appoint - key:"
							+ g.getChannelKey().toString()
							+ ";program ="
							+ p
							+ ";name="
							+ (p != null ? p.getName() : null)
							+ ";time="
							+ (p != null ? (p.getStart() + "-:-" + getTodayTime(p.getStart()))
									: null));
		}
		return list;
	}

	public static String[][] createEventLoadSelection(int startOffsetOfToday, int endOffsetOfToday) {
		if (startOffsetOfToday > endOffsetOfToday || endOffsetOfToday - startOffsetOfToday > 16
				|| startOffsetOfToday < -16)
			throw new IllegalArgumentException();
		Log.i(TAG, "go in .......createEventLoadSelection mehtod");
		Time t = new Time();
		TimeToolkit.getStartTimeByOffsetOfToday(0, t);
		if (t.year == CONPUTER_BASIC_TIME) {
			timer.schedule(new MyTimerTask(t, 0, timeHandler), 0, PERIOD_TIME);
		} else {
			if (timer != null) {
				timer.cancel();
			}
		}
		long tstart = t.toMillis(false);
		long start = tstart - startOffsetOfToday * TimeToolkit.DURATION_OF_DAY;
		long end = tstart + (endOffsetOfToday + 1) * TimeToolkit.DURATION_OF_DAY;
		String selection = Events.START_TIME + ">=? AND " + Events.END_TIME + "<?";
		String args[] = new String[] { String.valueOf(start), String.valueOf(end) };
		Log.d(TAG, "Today_time : " + "start_time = " + String.valueOf(start) + "<-->"
				+ getTodayTime(start) + " end_time = " + String.valueOf(end) + "<-->"
				+ getTodayTime(end));
		return new String[][] { new String[] { selection }, args };
	}

	static class MyTimerTask extends TimerTask {

		Time t;
		TimeUpdateHandler timeHandler;
		int offOfToday;

		public MyTimerTask(Time t, int offOfToday, TimeUpdateHandler timeHandler) {
			super();
			this.t = t;
			this.timeHandler = timeHandler;
			this.offOfToday = offOfToday;
		}

		@Override
		public void run() {
			int year = t.year;
			Log.i(TAG, "----------year=" + year);
			long tn = System.currentTimeMillis() + offOfToday * DURATION_OF_DAY;
			t.set(tn);
			if ((year = t.year) != CONPUTER_BASIC_TIME) {
				t.set(t.monthDay, t.month, t.year);
				timeHandler.sendEmptyMessage(MASK_TIMEUPDATE);
			}
		}

	}

	public static String[][] createEventUpdateSelection(ChannelKey ch) {
		String[][] ret = new String[2][];
		String[] args = null;
		String selection = Events.FREQUENCY + "=? AND " + Events.PROGRAM_NUMBER + "=?";
		args = new String[] { String.valueOf(ch.getFrequency()), String.valueOf(ch.getProgram()) };
		ret[0] = new String[] { selection };
		ret[1] = args;
		return ret;
	}

	private final void updateProgramEvents(final ChannelKey ch) {
		Log.i(TAG, "----updateProgramEvents---is  go in-ch=" + ch.toString());
		final EventCursorHandler ec = createUpdateEventCursorHandler(context, eventsUri,
				eventHandler, ch);

		ec.setQueryHandler(new QueryHandler() {

			@Override
			public void onQueryEnd() {
				synchronized (loadMutex) {
					loaddedGuidesVersion++;
					// 在这一步 ，loaddedGuides的大小可能为0
					Guide g = loaddedGuides.get(ch);
					Log.i(TAG, "----updateProgramEvents--g -is=" + g);
					List<Program> got = ec.programs.get(ch);

					// 验证programsize 是否为1
					Log.i(TAG, "----updateProgramEvents--ec.programs-is=" + ec.programs.size());

					@SuppressWarnings("unused")
					List<Program> ret = null;/*- 的确要这样确保软引用在此函数期间有效 */
					if (g != null && got != null) {
						ret = procUpdateGuidePrograms(g, got);
						if (g.pf != null && g.isPFLoadedNum == 0) {
							g.isPFLoadedNum = 2;
						}
					}
					if (presentFollowsChecker != null) {
						presentFollowsChecker.cancel();
						presentFollowsChecker = new PresentFollowsCheckTimerTask(
								loaddedGuidesVersion);
						presentFollowTimer.schedule(presentFollowsChecker, 0);
					}
					Log.d(TAG, "updateProgramEvents onQueryEnd ch = " + ch.toString());
					onProgramsUpdated(ch);
				}
			}
		});
		ec.postQuery();
	}

	private long syncOutOfDatePresentFollows() {
		int ret = 60 * 60 * 1000;
		long now = System.currentTimeMillis();
		boolean updated = false;
		for (Guide g : loaddedGuides.values()) {
			if (g.pf == null)
				continue;
			Program p = g.pf[0];
			Program f = g.pf[1];
			if (p != null) {
				if (!p.isPresent(now)) {
					g.pf[0] = (p = null);
					updated = true;
				} else {
					/**
					 * g.isPFLoadedNum: 0:是当前的，但是 从没有更新过 1:是当前的，但有更新过
					 * 2:是当前的，但有从没有更新过,但现在加载了
					 */
					Log.i(TAG, "---------22---syncOutOfDatePresentFollows g is present:"
							+ g.isPFLoadedNum);
					if (g.isPFLoadedNum == 2) {
						updated = true;
						g.isPFLoadedNum = 1;
					}
				}
			}
			if (p == null) {
				if (f != null ? f.isPresent(now) : false) {
					g.pf[0] = (p = f);
					g.pf[1] = (f = null);
					updated = true;
				}
			}
			if (p == null || f == null) {
				// Log.i(TAG,
				// " ----------------validata  g.list="+g.list+";g.list2="+g.list2);
				// 原先采用的
				// List<Program> list = g.list2;
				List<Program> list = g.list2;
				if (list != null) {
					int n = list.size(), i = 0;
					if (p == null) {
						for (; i < n; i++) {
							Program p2 = list.get(i);
							if (p2.isPresent(now)) {
								g.pf[0] = (p = p2);
								if (i < n - 1) {
									g.pf[1] = (f = list.get(i + 1));
									updated = true;
								}
							}
						}
					}
					if (f == null) {
						for (; i < n; i++) {
							Program f2 = list.get(i);
							if (f2.start > now) {
								g.pf[1] = (f = f2);
								updated = true;
							}
						}
					}
				}
			}

			long end = p != null ? p.end : f != null ? f.end : now + 60 * 60 * 1000;
			int delay = (int) (end - now);
			// Log.d(TAG, "syncOutOfDatePresentFollows delay =" + delay +
			// ",end=" + end + ",now=" + now);
			delay = delay < 0 ? 0 : delay;
			ret = ret > delay ? delay : ret;
			// Log.d(TAG, "syncOutOfDatePresentFollows ret =" + ret +
			// ",updated=" + updated);
		}
		Log.d(TAG, "syncOutOfDatePresentFollows ret =" + ret + ",updated=" + updated);
		if (updated)
			onPresentFollowUpdated();
		return ret;
	}

	class PresentFollowsCheckTimerTask extends TimerTask {
		private int lv;

		PresentFollowsCheckTimerTask(int lv) {
			this.lv = lv;
		}

		@Override
		public void run() {
			synchronized (loadMutex) {
				long delay = syncOutOfDatePresentFollows();
				Log.i(TAG, "------------------delay=" + delay + ";loaddedGuidesVersion="
						+ loaddedGuidesVersion + ";lv=" + lv);
				if (delay > 0 & loaddedGuidesVersion == lv) { /*- delay 为0 会不断刷新，导致死循环*/
					presentFollowsChecker = new PresentFollowsCheckTimerTask(lv);
					presentFollowTimer.schedule(presentFollowsChecker, delay);
				}
			}
		}
	};

	public ContentObserver setGroupObserver() {
		groupObserver = new ContentObserver(groupHandler) {
			public void onChange(boolean selfChange) {
				Log.i(TAG, "--------------go in Observe  groupObserver");
				postLoadGroups();
			};
		};
		return groupObserver;
	}

	public ContentObserver setChannelObserver() {
		channelObserver = new ContentObserver(channelHandler) {
			public void onChange(boolean selfChange) {
				Log.i(TAG, "--------------go in Observe channelObserver");
				postLoadChannels();
			};
		};
		return channelObserver;
	}

	public ContentObserver setGuideObserver() {

		guideObserver = new ContentObserver(eventHandler) {
			public void onChange(boolean selfChange) {
				Log.i(TAG, "--------lvby------go in Observe  guideObserver");
				postLoadGuides();
			};
		};
		return guideObserver;
	}

	class TimeUpdateHandler extends Handler {
		public TimeUpdateHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "TimeUpdateHandler msg = " + msg.what + ":" + msg.obj);
			switch (msg.what) {
			case MASK_TIMEUPDATE:
				postLoadEvents();
				break;
			default:
				break;
			}
		}
	}

	public static String getTodayTime(long time) {
		return formatter_a.format(new Date(time));
	}
}