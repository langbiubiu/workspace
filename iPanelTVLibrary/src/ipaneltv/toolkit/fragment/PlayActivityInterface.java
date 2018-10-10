package ipaneltv.toolkit.fragment;

import ipaneltv.toolkit.TimeToolkit.Weekday;
import ipaneltv.toolkit.db.DatabaseObjectification;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DvbDatabaseObjectification;
import ipaneltv.toolkit.entitlement.EntitlementObserver.EntitlementsState;

import java.util.List;
import java.util.Map.Entry;

import android.graphics.Rect;

// ===============================================================
// 可在主线程直接使用接口(接口实现对调用耗时的一律转为消息异步处理)
// 1 以下接口(Interface)均可在主线程(UI线程)中调用，
// 2 以下回调(Callback)均在主线程中实施，可直接接操作UI，不能阻塞
// ===============================================================
public interface PlayActivityInterface {
	static final int __IDMediaPlayerInterface = 0x10 << 16;

	public static interface LivePlayBaseInterface extends PlayActivityInterface {
		static final int __IDLivePlayBaseInterface = 0x100 << 16;
		static final int __ID_select = __IDLivePlayBaseInterface | 1;
		static final int __ID_setDisplay = __IDLivePlayBaseInterface | 2;
		static final int __ID_setVolume = __IDLivePlayBaseInterface | 3;
		static final int __ID_shift = __IDLivePlayBaseInterface | 4;
		static final int __ID_shiftPause = __IDLivePlayBaseInterface | 5;
		static final int __ID_setProgramFlags = __IDLivePlayBaseInterface | 6;
		static final int __ID_observeProgramGuide = __IDLivePlayBaseInterface | 7;

		/** 选择频道播放 */
		void select(long freq, int fflags, int program, int pflags);

		/** 设置视频的显示范围 */
		void setDisplay(int x, int y, int w, int h);

		/** 设置音量 */
		void setVolume(float v);

		/** 设置时移,第一次如果传入-1表示从当前时间开始,0则从起始时间开始 */
		void shift(String uri, int offsetOfShiftStartTime, int flags);

		/** 暂停时移 */
		void shiftPause(String uri);

		/** 设置节目播放的标志 */
		void setProgramFlags(int flags);

		/** 关注节目指南数据，focusTime距此时间近则优先处理,0表示不关注时间 */
		void observeProgramGuide(ChannelKey ch, long focusTime);

		public static interface Callback {
			static final int __ID_onContextReady = __IDLivePlayBaseInterface | 1001;
			static final int __ID_onSelectError = __IDLivePlayBaseInterface | 1002;
			static final int __ID_onLiveInfoUpdated = __IDLivePlayBaseInterface | 1003;
			static final int __ID_onCaModuleDispatched = __IDLivePlayBaseInterface | 1004;
			static final int __ID_onShiftStartTimeUpdated = __IDLivePlayBaseInterface | 1005;
			static final int __ID_onShiftMediaTimeSync = __IDLivePlayBaseInterface | 1006;

			/** 上下文已就绪 */
			void onContextReady(String group);

			/** 当选择的节目发生播放错误 */
			void onSelectError(String msg);

			/** 播放器的直播信息已经更新，需要首次/再次发起播放 */
			void onLiveInfoUpdated();

			/** ca模块已指定 */
			void onCaModuleDispatched(int moduleId);

			/** 时移的起始时间已更新（时移总是在当前时间结束 ） */
			void onShiftStartTimeUpdated(long start);

			/** 同步时移时间 */
			void onShiftMediaTimeSync(long t);
		}
	}
	
	
	/** 可在主线程直接使用接口(接口实现对调用耗时的一律转为消息异步处理) */
	public static interface VodPlayBaseInterface extends PlayActivityInterface {
		static final int __IDVodPlayBaseInterface = 0x200 << 16;
		static final int __ID_play = __IDVodPlayBaseInterface | 1;
		static final int __ID_stop = __IDVodPlayBaseInterface | 2;
		static final int __ID_pause = __IDVodPlayBaseInterface | 3;
		static final int __ID_resume = __IDVodPlayBaseInterface | 4;
		static final int __ID_setDisplay = __IDVodPlayBaseInterface | 5;
		static final int __ID_seek = __IDVodPlayBaseInterface | 6;
		static final int __ID_setRate = __IDVodPlayBaseInterface | 7;
		static final int __ID_setVolume = __IDVodPlayBaseInterface | 8;
		static final int __ID_setProgramFlag = __IDVodPlayBaseInterface | 9;

		/** 选择uri播放，注间type及streamType对应值 */
		void start(String uri, int type, int streamType, int flags);

		/** 停止播放 */
		void stop();

		/** 暂停播放 */
		void pause();

		/** 重新播放 */
		void resume();

		/** 设置视频的显示范围 */
		void setDisplay(Rect rect);

		/** SEEK到某个时间播放 */
		void seek(long time);

		/** 设置播放速率,范围-1.0f ~ 1.0f值 */
		void setRate(float rate);

		/** 设置音量 */
		void setVolume(float v);

		/** 设置节目播放的标志 */
		void setProgramFlag(int flags);

		public static interface Callback {
			static final int __ID_onContextReady = __IDVodPlayBaseInterface | 1001;
			static final int __ID_onVodDuration = __IDVodPlayBaseInterface | 1002;
			static final int __ID_onSeeBackPeriod = __IDVodPlayBaseInterface | 1003;
			static final int __ID_onPlayStart = __IDVodPlayBaseInterface | 1004;
			static final int __ID_onSourceRate = __IDVodPlayBaseInterface | 1005;
			static final int __ID_onSourceSeek = __IDVodPlayBaseInterface | 1006;
			static final int __ID_onSyncMediaTime = __IDVodPlayBaseInterface | 1007;
			static final int __ID_onPlayEnd = __IDVodPlayBaseInterface | 1008;
			static final int __ID_onPlayMsg = __IDVodPlayBaseInterface | 1009;
			static final int __ID_onPlayError = __IDVodPlayBaseInterface | 1010;

			/** 上下文已就绪 */
			void onContextReady(String group);

			/** VOD播放时长 */
			void onVodDuration(long d);

			/** 回看起始和终止时间 */
			void onSeeBackPeriod(long s, long e);

			/** 播放成功或失败 */
			void onPlayStart(boolean b);

			/** 播放实际速率 */
			void onSourceRate(float r);

			/** 播放实际seek到的位置 */
			void onSourceSeek(long t);

			/** 播放同步更新时间 */
			void onSyncMediaTime(long t);

			/** 播放结束到尾 */
			void onPlayEnd(float r);

			/** 播放提示信息 */
			void onPlayMsg(String msg);

			/** 播放错误提示信息 */
			void onPlayError(String msg);
		}
	}

	public static interface NaviInterface extends PlayActivityInterface {
		static final int __IDNaviBaseInterface = 0x300 << 16;
		static final int __IDgetGroups = __IDNaviBaseInterface | 1;
		static final int __IDcommitOwnedGroup = __IDNaviBaseInterface | 2;
		static final int __IDgetGroupedChannels = __IDNaviBaseInterface | 3;
		static final int __IDgetNumberedChannels = __IDNaviBaseInterface | 4;
		static final int __IDgetDailyPrograms = __IDNaviBaseInterface | 5;
		static final int __IDgetDailyPrograms2 = __IDNaviBaseInterface | 6;
		static final int __IDgetChannelShiftUri = __IDNaviBaseInterface | 7;
		static final int __IDgetEntitlements = __IDNaviBaseInterface | 8;
		static final int __IDgetChannelShiftOrder = __IDNaviBaseInterface | 9;
		static final int __IDgetChannelShiftEntry = __IDNaviBaseInterface | 10;
		static final int __IDgetShiftPrograms = __IDNaviBaseInterface | 11;
		
		/** 得到分组数据,从不返回null */
		List<DatabaseObjectification.Group> getGroups();

		/** 提交新的分组信息 */
		void commitOwnedGroup(DatabaseObjectification.Group g, List<ChannelKey> list);

		/** 得到指定分组的频道列表 ,从不返回null */
		List<DvbDatabaseObjectification.Service> getGroupedChannels(DatabaseObjectification.Group g);

		/** 得到频道号排序的频道列表 ,从不返回null */
		List<DvbDatabaseObjectification.Service> getNumberedChannels();

		/** 得到指定频道的每日节目指南 ,从不返回null */
		List<DvbDatabaseObjectification.ProgramEvent> getDailyPrograms(DvbDatabaseObjectification.Service ch, Weekday d);

		/** 得到指定频道的每日节目指南,从不返回null */
		List<DvbDatabaseObjectification.ProgramEvent> getDailyPrograms(DvbDatabaseObjectification.Service ch, int offsetOfToday);

		/** 当前频道是否支持时移，如果支持返回uri */
		String getChannelShiftUri(DvbDatabaseObjectification.Service ch);

		/** 获取频道的授权信息 */
		EntitlementsState getEntitlements(int moduledID);

		/** 当前频道是否支持时移且已订购，如果订购返回true */
		boolean getChannelShiftOrder(DvbDatabaseObjectification.Service ch);

		/** 当前频道是否支持时移且已订购，如果订购返回true */
		@SuppressWarnings("rawtypes")
		Entry getChannelShiftEntry(DvbDatabaseObjectification.Service ch);

		/** 得到时移的节目列表,从不返回null */
		List<DvbDatabaseObjectification.ProgramEvent> getShiftPrograms(DvbDatabaseObjectification.Service ch);
		
		public static interface Callback {
			static final int __ID_onNaviUpdated = __IDNaviBaseInterface | 2001;
			static final int __ID_onGuideUpdated = __IDNaviBaseInterface | 2002;
			static final int __ID_onPresentFollowUpdated = __IDNaviBaseInterface | 2003;
			static final int __ID_onCommitGroupError = __IDNaviBaseInterface | 2004;
			static final int __ID_onShiftUpdated = __IDNaviBaseInterface | 2005;
			
			/** 导航数据更新,需要再次/首次发起播放 */
			void onNaviUpdated();

			/** 指南数据更新 */
			void onGuideUpdated(ChannelKey key);

			/** pf有更新 */
			void onPresentFollowUpdated();

			/** 提交分组失败 */
			void onCommitGroupError(String name);

			/** 时移信息更新 */
			void onShiftUpdated();
		}
	}
}
