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
// �������߳�ֱ��ʹ�ýӿ�(�ӿ�ʵ�ֶԵ��ú�ʱ��һ��תΪ��Ϣ�첽����)
// 1 ���½ӿ�(Interface)���������߳�(UI�߳�)�е��ã�
// 2 ���»ص�(Callback)�������߳���ʵʩ����ֱ�ӽӲ���UI����������
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

		/** ѡ��Ƶ������ */
		void select(long freq, int fflags, int program, int pflags);

		/** ������Ƶ����ʾ��Χ */
		void setDisplay(int x, int y, int w, int h);

		/** �������� */
		void setVolume(float v);

		/** ����ʱ��,��һ���������-1��ʾ�ӵ�ǰʱ�俪ʼ,0�����ʼʱ�俪ʼ */
		void shift(String uri, int offsetOfShiftStartTime, int flags);

		/** ��ͣʱ�� */
		void shiftPause(String uri);

		/** ���ý�Ŀ���ŵı�־ */
		void setProgramFlags(int flags);

		/** ��ע��Ŀָ�����ݣ�focusTime���ʱ��������ȴ���,0��ʾ����עʱ�� */
		void observeProgramGuide(ChannelKey ch, long focusTime);

		public static interface Callback {
			static final int __ID_onContextReady = __IDLivePlayBaseInterface | 1001;
			static final int __ID_onSelectError = __IDLivePlayBaseInterface | 1002;
			static final int __ID_onLiveInfoUpdated = __IDLivePlayBaseInterface | 1003;
			static final int __ID_onCaModuleDispatched = __IDLivePlayBaseInterface | 1004;
			static final int __ID_onShiftStartTimeUpdated = __IDLivePlayBaseInterface | 1005;
			static final int __ID_onShiftMediaTimeSync = __IDLivePlayBaseInterface | 1006;

			/** �������Ѿ��� */
			void onContextReady(String group);

			/** ��ѡ��Ľ�Ŀ�������Ŵ��� */
			void onSelectError(String msg);

			/** ��������ֱ����Ϣ�Ѿ����£���Ҫ�״�/�ٴη��𲥷� */
			void onLiveInfoUpdated();

			/** caģ����ָ�� */
			void onCaModuleDispatched(int moduleId);

			/** ʱ�Ƶ���ʼʱ���Ѹ��£�ʱ�������ڵ�ǰʱ����� �� */
			void onShiftStartTimeUpdated(long start);

			/** ͬ��ʱ��ʱ�� */
			void onShiftMediaTimeSync(long t);
		}
	}
	
	
	/** �������߳�ֱ��ʹ�ýӿ�(�ӿ�ʵ�ֶԵ��ú�ʱ��һ��תΪ��Ϣ�첽����) */
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

		/** ѡ��uri���ţ�ע��type��streamType��Ӧֵ */
		void start(String uri, int type, int streamType, int flags);

		/** ֹͣ���� */
		void stop();

		/** ��ͣ���� */
		void pause();

		/** ���²��� */
		void resume();

		/** ������Ƶ����ʾ��Χ */
		void setDisplay(Rect rect);

		/** SEEK��ĳ��ʱ�䲥�� */
		void seek(long time);

		/** ���ò�������,��Χ-1.0f ~ 1.0fֵ */
		void setRate(float rate);

		/** �������� */
		void setVolume(float v);

		/** ���ý�Ŀ���ŵı�־ */
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

			/** �������Ѿ��� */
			void onContextReady(String group);

			/** VOD����ʱ�� */
			void onVodDuration(long d);

			/** �ؿ���ʼ����ֹʱ�� */
			void onSeeBackPeriod(long s, long e);

			/** ���ųɹ���ʧ�� */
			void onPlayStart(boolean b);

			/** ����ʵ������ */
			void onSourceRate(float r);

			/** ����ʵ��seek����λ�� */
			void onSourceSeek(long t);

			/** ����ͬ������ʱ�� */
			void onSyncMediaTime(long t);

			/** ���Ž�����β */
			void onPlayEnd(float r);

			/** ������ʾ��Ϣ */
			void onPlayMsg(String msg);

			/** ���Ŵ�����ʾ��Ϣ */
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
		
		/** �õ���������,�Ӳ�����null */
		List<DatabaseObjectification.Group> getGroups();

		/** �ύ�µķ�����Ϣ */
		void commitOwnedGroup(DatabaseObjectification.Group g, List<ChannelKey> list);

		/** �õ�ָ�������Ƶ���б� ,�Ӳ�����null */
		List<DvbDatabaseObjectification.Service> getGroupedChannels(DatabaseObjectification.Group g);

		/** �õ�Ƶ���������Ƶ���б� ,�Ӳ�����null */
		List<DvbDatabaseObjectification.Service> getNumberedChannels();

		/** �õ�ָ��Ƶ����ÿ�ս�Ŀָ�� ,�Ӳ�����null */
		List<DvbDatabaseObjectification.ProgramEvent> getDailyPrograms(DvbDatabaseObjectification.Service ch, Weekday d);

		/** �õ�ָ��Ƶ����ÿ�ս�Ŀָ��,�Ӳ�����null */
		List<DvbDatabaseObjectification.ProgramEvent> getDailyPrograms(DvbDatabaseObjectification.Service ch, int offsetOfToday);

		/** ��ǰƵ���Ƿ�֧��ʱ�ƣ����֧�ַ���uri */
		String getChannelShiftUri(DvbDatabaseObjectification.Service ch);

		/** ��ȡƵ������Ȩ��Ϣ */
		EntitlementsState getEntitlements(int moduledID);

		/** ��ǰƵ���Ƿ�֧��ʱ�����Ѷ����������������true */
		boolean getChannelShiftOrder(DvbDatabaseObjectification.Service ch);

		/** ��ǰƵ���Ƿ�֧��ʱ�����Ѷ����������������true */
		@SuppressWarnings("rawtypes")
		Entry getChannelShiftEntry(DvbDatabaseObjectification.Service ch);

		/** �õ�ʱ�ƵĽ�Ŀ�б�,�Ӳ�����null */
		List<DvbDatabaseObjectification.ProgramEvent> getShiftPrograms(DvbDatabaseObjectification.Service ch);
		
		public static interface Callback {
			static final int __ID_onNaviUpdated = __IDNaviBaseInterface | 2001;
			static final int __ID_onGuideUpdated = __IDNaviBaseInterface | 2002;
			static final int __ID_onPresentFollowUpdated = __IDNaviBaseInterface | 2003;
			static final int __ID_onCommitGroupError = __IDNaviBaseInterface | 2004;
			static final int __ID_onShiftUpdated = __IDNaviBaseInterface | 2005;
			
			/** �������ݸ���,��Ҫ�ٴ�/�״η��𲥷� */
			void onNaviUpdated();

			/** ָ�����ݸ��� */
			void onGuideUpdated(ChannelKey key);

			/** pf�и��� */
			void onPresentFollowUpdated();

			/** �ύ����ʧ�� */
			void onCommitGroupError(String name);

			/** ʱ����Ϣ���� */
			void onShiftUpdated();
		}
	}
}
