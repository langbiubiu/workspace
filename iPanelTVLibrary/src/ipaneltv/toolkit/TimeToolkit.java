package ipaneltv.toolkit;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;
import android.text.format.Time;

/**
 * 媒体播放时间工具类
 */
public class TimeToolkit {
	public static enum Weekday {
		ONE, TWO, THREE, FOUR, FIVE, SIX, SUN
	};

	public static enum TodayOffset {
		LLLDAY(-3), LLDAY(-2), LDAY(-1), TODAY(0), NDAY(1), NNDAY(2), NNNDAY(3);
		private int offset;

		private TodayOffset(int offset) {
			this.offset = offset;
		}

		public int value() {
			return offset;
		}
	}

	public static final int DURATION_OF_DAY = 24 * 60 * 60 * 1000;
	private static final Time NOW = new Time();

	/** weekDay @see android.text.format.Time */
	public static final int getOffsetOfTodayByWeekday(Weekday weekDay) {
		int twd = toTimeWeekDay(weekDay);
		checkWeekDay(twd);
		synchronized (NOW) {
			NOW.setToNow();
			return twd - NOW.weekDay;
		}
	}

	public static final void getStartTimeByOffsetOfToday(int offOfToday, Time t) {
		long tn = System.currentTimeMillis() + offOfToday * DURATION_OF_DAY;
		t.set(tn);
		t.set(t.monthDay, t.month, t.year);
	}

	public static final long getStartTimeOfDayByMillisInDay(long millis) {
		Time t = new Time();
		t.set(millis);
		t.set(t.monthDay, t.month, t.year);
		return t.toMillis(false);
	}

	public static final int getOffsetOfTodayByStartTime(long start) {
		return (int) (start - System.currentTimeMillis()) / DURATION_OF_DAY;
	}

	public static final Weekday getWeekdayByTodayOffset(int offsetOfToday) {
		Time t = new Time();
		getStartTimeByOffsetOfToday(offsetOfToday, t);
		return toWeekDay(t.weekDay);
	}

	public static final Weekday getWeekdayByStartOfDay(long start) {
		Time t = new Time();
		t.set(start);
		return toWeekDay(t.weekDay);
	}

	public final static Weekday toWeekDay(int d) {
		switch (d) {
		case Time.SUNDAY:
			return Weekday.ONE;
		case Time.MONDAY:
			return Weekday.TWO;
		case Time.TUESDAY:
			return Weekday.THREE;
		case Time.WEDNESDAY:
			return Weekday.FOUR;
		case Time.THURSDAY:
			return Weekday.FIVE;
		case Time.FRIDAY:
			return Weekday.SIX;
		case Time.SATURDAY:
			return Weekday.SUN;
		default:
			throw new IllegalArgumentException("week day invaid, see android.text.format.Time");
		}
	}

	public final static int toTimeWeekDay(Weekday d) {
		switch (d) {
		case ONE:
			return Time.MONDAY;
		case TWO:
			return Time.TUESDAY;
		case THREE:
			return Time.WEDNESDAY;
		case FOUR:
			return Time.THURSDAY;
		case FIVE:
			return Time.FRIDAY;
		case SIX:
			return Time.SATURDAY;
		case SUN:
			return Time.SUNDAY;
		}
		return -1;
	}

	public final static void checkWeekDay(int weekday) {
		switch (weekday) {
		case Time.SUNDAY:
		case Time.MONDAY:
		case Time.TUESDAY:
		case Time.WEDNESDAY:
		case Time.THURSDAY:
		case Time.FRIDAY:
		case Time.SATURDAY:
			break;
		default:
			throw new IllegalArgumentException("week day invaid, see android.text.format.Time");
		}
	}

	private static Timer mediaClockTimer = new Timer();

	public static abstract class MediaClock {
		public static final int SEEK_SET = 0;
		public static final int SEEK_CUR = 1;
		public static final int SEEK_END = 2;
		private Object mutex = new Object();
		private TimerTask timer;
		private long seek, seekTime, pausedOffset = 0;
		private float rate;
		private Time glanceTime = new Time();
		private int where = -1;

		public MediaClock() {
		}

		/** 脉冲间隔 */
		public final void start(long pulse) {
			final long p = pulse < 200 ? 200 : pulse;
			synchronized (mutex) {
				if (timer != null) {
					throw new IllegalStateException("is started already!");
				}
				timer = new TimerTask() {
					@Override
					public void run() {
						synchronized (mutex) {
							if (timer != this)
								return;
							procPulse();
						}
					}
				};
				markSeek(0, 1.0f, SEEK_SET);
				mediaClockTimer.schedule(timer, p, p);
			}
		}

		public final void stop() {
			synchronized (mutex) {
				TimerTask t = timer;
				timer = null;
				if (t != null) {
					t.cancel();
					handler.obtainMessage(MSG_END, SEEK_CUR, 0);
				}
			}
		}

		public final synchronized void pause() {
			if (pausedOffset != 0)
				return;
			pausedOffset = glance();
		}

		public final synchronized void resume() {
			if (pausedOffset != 0) {
				markSeek(pausedOffset, rate, SEEK_CUR);
				pausedOffset = 0;
			}
		}

		/** 返回相对起始时间的偏移量 */
		public final long seek(int where, long offset, float rate) {
			if (rate == 0)
				throw new IllegalArgumentException("invalid rate!");
			synchronized (mutex) {
				if (timer == null)
					return -1;// is stoped
				long start = getStart();
				long end = getEnd();
				long duration = end - start;
				if (end <= start)
					throw new RuntimeException("end time <= start time!!!!!");
				switch (where) {
				case SEEK_SET: {
					if (offset < 0) {
						procEnd(SEEK_SET);
						return 0;
					}
					if (offset == 0 && rate < 0) {
						procEnd(SEEK_SET);
						return 0;
					}
					if (duration <= offset) {
						procEnd(SEEK_END);
						return duration;
					}
					markSeek(offset, rate, SEEK_SET);
					return offset;
				}
				case SEEK_END: {
					if (offset > 0) {
						procEnd(SEEK_END);
						return duration;
					}
					if (offset == 0 && rate > 0) {
						procEnd(SEEK_END);
						return duration;
					}
					if (duration <= -offset) {
						procEnd(SEEK_SET);
						return 0;
					}
					markSeek(offset, rate, SEEK_END);
					return offset;
				}
				case SEEK_CUR: {
					offset += glance();
					if (offset < start) {
						procEnd(SEEK_SET);
						return 0;
					} else if (offset > end) {
						procEnd(SEEK_END);
						return duration;
					} else if (offset == start && rate < 0) {
						procEnd(SEEK_SET);
						return 0;
					} else if (offset == end && rate > 0) {
						procEnd(SEEK_END);
						return duration;
					}
					markSeek(offset, rate, SEEK_CUR);
					return offset;
				}
				default:
					throw new IllegalArgumentException("bad seek wherence");
				}
			}
		}

		private long glance() {
			long duration = System.currentTimeMillis() - seekTime;
			duration = (long) (((double) duration) * rate);
			if (where == SEEK_END)
				return getEnd() + seek + duration;
			else
				return seek + duration;
		}

		private void procPulse() {
			if (pausedOffset != 0)
				return;
			long start = getStart();
			long end = getEnd();
			long offset = glance();
			if (end <= start)
				throw new IllegalStateException("end <= start!!!");

			if (offset >= end)
				procEnd(SEEK_END);
			else if (offset <= start)
				procEnd(SEEK_SET);
			if (rate != 0) {
				glanceTime.set(start + offset);
			} else {
				throw new IllegalStateException("rate is 0");
			}
			handler.obtainMessage(MSG_PULSE, new Object[] { glanceTime, Long.valueOf(offset) })
					.sendToTarget();
		}

		private void procEnd(int seek) {
			long m = -1;
			if (seek == SEEK_END)
				m = getEnd();
			else if (seek == SEEK_SET)
				m = getStart();
			glanceTime.set(m);
			handler.obtainMessage(MSG_PULSE, new Object[] { glanceTime, Long.valueOf(m) })
					.sendToTarget();
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
			handler.obtainMessage(MSG_END, seek, 0).sendToTarget();
		}

		private void markSeek(long s, float r, int w) {
			seek = s;
			rate = r;
			where = w;
			seekTime = System.currentTimeMillis();
		}

		/** 子类实现 */
		protected abstract long getStart();

		/** 子类实现 */
		protected abstract long getEnd();

		/**
		 * 主线程回调,t时间，和相对start的偏移量millis
		 */
		protected abstract void onClockPulse(Time t, long millis);

		/**
		 * 主线程回调<br>
		 * SEEK_SET 到头 <br>
		 * SEEK_CUR 停止 <br>
		 * SEEK_END 到尾 <br>
		 */
		protected abstract void onClockEnd(int wherence);

		static final int MSG_END = 1;
		static final int MSG_PULSE = 2;
		private Handler handler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				try {
					switch (msg.what) {
					case MSG_END:
						onClockEnd(msg.arg1);
						break;
					case MSG_PULSE:
						Object[] args = (Object[]) msg.obj;
						onClockPulse((Time) args[0], (Long) args[1]);
						break;
					default:
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
		});
	}

}
