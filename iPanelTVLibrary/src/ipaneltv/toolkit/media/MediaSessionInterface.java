package ipaneltv.toolkit.media;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;

import java.util.HashMap;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;

public interface MediaSessionInterface extends ReserveStateInterface {
	static final int __IDMediaSessionInterface = 0x1 << 16;
	static final int __ID_loosen = __IDMediaSessionInterface | 1;
	static final int __ID_reserve = __IDMediaSessionInterface | 2;
	static final int __ID_isReserved = __IDMediaSessionInterface | 3;

	@Override
	void loosen(boolean clearState);

	@Override
	boolean reserve();

	@Override
	boolean isReserved();

	public static interface LiveStreamSelectionBaseInterface extends MediaSessionInterface {
		static final int __IDLiveStreamSelectionBaseInterface = 0x100 << 16;
		static final int __ID_syncSignalStatus = __IDLiveStreamSelectionBaseInterface | 1;

		void syncSignalStatus();

		public static interface Callback {
			static final int __ID_onResponseSelect = __IDLiveStreamSelectionBaseInterface | 1000;
			static final int __ID_onStreamLost = __IDLiveStreamSelectionBaseInterface | 1001;
			static final int __ID_onStreamResumed = __IDLiveStreamSelectionBaseInterface | 1002;
			static final int __ID_onSyncSignalStatus = __IDLiveStreamSelectionBaseInterface | 1003;

			void onResponseSelect(boolean b);

			void onStreamLost();

			void onStreamResumed();

			/** @see FrequencyInfo.fromString() */
			void onSyncSignalStatus(String signalStatus);
		}
	}

	public static interface TeeveePlayerBaseInterface extends MediaSessionInterface {
		static final int __IDTeeveePlayerBaseInterface = 0x200 << 16;
		static final int __ID_stop = __IDTeeveePlayerBaseInterface | 1;
		static final int __ID_pause = __IDTeeveePlayerBaseInterface | 2;
		static final int __ID_resume = __IDTeeveePlayerBaseInterface | 3;
		static final int __ID_setVolume = __IDTeeveePlayerBaseInterface | 4;
		static final int __ID_setDisplay = __IDTeeveePlayerBaseInterface | 5;
		static final int __ID_setTeeveeWidget = __IDTeeveePlayerBaseInterface | 6;
		static final int __ID_checkTeeveeWidget = __IDTeeveePlayerBaseInterface | 7;
		static final int __ID_setProgramFlags = __IDTeeveePlayerBaseInterface | 8;
		static final int __ID_syncMediaTime = __IDTeeveePlayerBaseInterface | 9;
		static final int __ID_getPlayTime = __IDTeeveePlayerBaseInterface | 10;
		static final int __ID_checkPassword = __IDTeeveePlayerBaseInterface | 11;
		static final int __ID_clearCache = __IDTeeveePlayerBaseInterface | 12;

		void stop(int flag);

		void pause();

		void resume();

		long getPlayTime();

		void setVolume(float v);

		void setDisplay(int x, int y, int w, int h);
		
		void checkPassword(String pwd);

		void setProgramFlags(int flags);

		void setTeeveeWidget(int flags);

		void checkTeeveeWidget(int flags);

		void syncMediaTime();
		
		void clearCache(int flags);

		public static interface Callback {
			static final int __ID_onWidgetChecked = __IDTeeveePlayerBaseInterface | 1000;
			static final int __ID_onResponseStop = __IDTeeveePlayerBaseInterface | 1001;
			static final int __ID_onVolumeChange = __IDTeeveePlayerBaseInterface | 1002;
			static final int __ID_onSyncMediaTime = __IDTeeveePlayerBaseInterface | 1003;
			static final int __ID_onPlayError = __IDTeeveePlayerBaseInterface | 1004;
			static final int __ID_onSourceMediaChange = __IDTeeveePlayerBaseInterface | 1005;
			static final int __ID_onDescramError = __IDTeeveePlayerBaseInterface | 1006;
			static final int __ID_onChannelLocked = __IDTeeveePlayerBaseInterface | 1007;
			static final int __ID_onPasswprdChecked = __IDTeeveePlayerBaseInterface | 1008;

			void onResponseStop();

			void onWidgetChecked(int flags);

			void onVolumeChange(float v);

			void onSyncMediaTime(long t);

			void onPlayError(String msg);

			void onDescramError(long f,int pn,int code,String err);

			void onSourceMediaChange(long time, long pts);
			
			void onChannelLocked(long freq,int program_number);
			
			void onPasswordChecked(boolean succ);
		}
	}

	public static interface LiveStreamSelectorInterface extends LiveStreamSelectionBaseInterface {
		static final int __IDLiveStreamSelectorInterface = __IDLiveStreamSelectionBaseInterface
				| (0x1 << 16);
		static final int __ID_select = __IDLiveStreamSelectorInterface | 1;
		static final int __ID_startStream = __IDLiveStreamSelectorInterface | 2;
		static final int __ID_stopStream = __IDLiveStreamSelectorInterface | 3;

		void select(String furi, int flags);

		void startStream(String localsock, int pid, int flags);

		void stopStream(String localsock, int pid);

		public static interface Callback extends LiveStreamSelectionBaseInterface.Callback {
			static final int __ID_onStreamStart = __IDLiveStreamSelectorInterface | 1000;
			static final int __ID_onStreamStop = __IDLiveStreamSelectorInterface | 1001;

			void onStreamStart(String furi, boolean succ);

			void onStreamStop(String furi);
		}
	}

	public static interface LiveProgramPlayerInterface extends LiveStreamSelectionBaseInterface,
			TeeveePlayerBaseInterface {
		static final int __IDLiveProgramPlayerInterface = __IDLiveStreamSelectionBaseInterface
				| __IDTeeveePlayerBaseInterface;
		static final int __ID_select = __IDLiveProgramPlayerInterface | 1;
		static final int __ID_solveProblem = __IDLiveProgramPlayerInterface | 2;
		static final int __ID_enterCaApp = __IDLiveProgramPlayerInterface | 3;
		static final int __ID_observeProgramGuide = __IDLiveProgramPlayerInterface | 4;
		static final int __ID_captureVideoFrame = __IDLiveProgramPlayerInterface | 5;
		static final int __ID_select_2 = __IDLiveProgramPlayerInterface | 6;
		static final int __ID_pipOpenPlayers = __IDLiveProgramPlayerInterface | 7;
		static final int __ID_pipClosePlayers = __IDLiveProgramPlayerInterface | 8;
		static final int __ID_pipSetFreqency = __IDLiveProgramPlayerInterface | 9;
		static final int __ID_pipSetProgram = __IDLiveProgramPlayerInterface | 10;
		static final int __ID_pipLoadAnimation = __IDLiveProgramPlayerInterface | 11;
		static final int __ID_pipActAnimation = __IDLiveProgramPlayerInterface | 12;
		static final int __ID_openTeeveeRecoder = __IDLiveProgramPlayerInterface | 13;
		static final int __ID_setTeeveeRecoder = __IDLiveProgramPlayerInterface | 14;
		static final int __ID_closeTeeveeRecoder = __IDLiveProgramPlayerInterface | 15;
		public static final int MASK_FREQUENCIES = 0x01;
		public static final int MASK_STREAMS = 0x02;
		public static final int MASK_ECMS = 0x04;

		void select(long freq, int fflags, int program, int pflags);

		void select(String furi, int fflags, String puri, int pflags);

		boolean pipOpenPlayers(int size, int flags);

		boolean pipClosePlayers();

		void pipSetFreqency(int index, long freq, int flags);

		void pipSetProgram(int index, int prog, int x,int y, int w,int h ,int flags);

		void pipLoadAnimation(ParcelFileDescriptor pfd);

		void pipActAnimation(int action, int p1, int p2, int flags);
		
		boolean openTeeveeRecoder(int flags);
		
		boolean setTeeveeRecoder(ParcelFileDescriptor pfd, long freq, int fflags,int pn, int pflags);
		
		void closeTeeveeRecoder();
		
		void solveProblem();

		void enterCaApp(String uri);

		void observeProgramGuide(ChannelKey key, long focusTime);

		void captureVideoFrame(int id);

		public static interface Callback extends LiveStreamSelectionBaseInterface.Callback,
				TeeveePlayerBaseInterface.Callback {
			static final int __ID_onProgramLost = __IDLiveProgramPlayerInterface | 1000;
			static final int __ID_onProgramReselected = __IDLiveProgramPlayerInterface | 1001;
			static final int __ID_onLiveInfoUpdated = __IDLiveProgramPlayerInterface | 1002;
			static final int __ID_onCaModuleDispatched = __IDLiveProgramPlayerInterface | 1003;
			static final int __ID_onRecordStart = __IDLiveProgramPlayerInterface | 1004;
			static final int __ID_onRecordError = __IDLiveProgramPlayerInterface | 1005;
			static final int __ID_onRecordEnd = __IDLiveProgramPlayerInterface | 1006;

			void onProgramLost();

			void onProgramReselected();

			void onLiveInfoUpdated(int mask);

			void onCaModuleDispatched(int moduleId);
			
			void onRecordStart(int program_number);
			
			void onRecordError(int program_number, String msg);
			
			void onRecordEnd(int program_number);
		}
	}

	public static interface HomedProgramPlayerInterface extends LiveStreamSelectionBaseInterface,
			TeeveePlayerBaseInterface {
		static final int __IDLiveProgramPlayerInterface = __IDLiveStreamSelectionBaseInterface
				| __IDTeeveePlayerBaseInterface | 0x200 << 8;
		static final int __ID_homed_select = __IDLiveProgramPlayerInterface | 1;
		static final int __ID_homed_solveProblem = __IDLiveProgramPlayerInterface | 2;
		static final int __ID_homed_enterCaApp = __IDLiveProgramPlayerInterface | 3;
		static final int __ID_homed_observeProgramGuide = __IDLiveProgramPlayerInterface | 4;
		static final int __ID_homed_captureVideoFrame = __IDLiveProgramPlayerInterface | 5;
		static final int __ID_homed_select_2 = __IDLiveProgramPlayerInterface | 6;
		static final int __ID_homed_start = __IDLiveProgramPlayerInterface | 7;
		static final int __ID_homed_pfd_start = __IDLiveProgramPlayerInterface | 8;
		static final int __ID_homed_redirect = __IDLiveProgramPlayerInterface | 9;
		static final int __ID_homed_startShift = __IDLiveProgramPlayerInterface | 10;
		public static final int MASK_FREQUENCIES = 0x01;
		public static final int MASK_STREAMS = 0x02;
		public static final int MASK_ECMS = 0x04;

		void select(long freq, int fflags, int program, int pflags);

		void select(String furi, int fflags, String puri, int pflags);

		void start(String furi, int fflags, String puri, int pflags);

		void startFd(long vfreq, ParcelFileDescriptor pfd, int fflags);
		
		/** 重定向数据源(清缓存),节目播放发保持 */
		void redirect(long vfreq, ParcelFileDescriptor pfd, int flags);
		
		void startShift(final long vfreq, ParcelFileDescriptor pfd, final int fflags);
		
		void solveProblem();

		void enterCaApp(String uri);

		void observeProgramGuide(ChannelKey key, long focusTime);

		void captureVideoFrame(int id);

		public static interface Callback extends LiveStreamSelectionBaseInterface.Callback,
				TeeveePlayerBaseInterface.Callback {
			static final int __ID_homed_onProgramLost = __IDLiveProgramPlayerInterface | 1000;
			static final int __ID_homed_onProgramReselected = __IDLiveProgramPlayerInterface | 1001;
			static final int __ID_homed_onLiveInfoUpdated = __IDLiveProgramPlayerInterface | 1002;
			static final int __ID_homed_onCaModuleDispatched = __IDLiveProgramPlayerInterface | 1003;
			static final int __ID_homed_onIpStoped = __IDLiveProgramPlayerInterface | 1004;
			static final int __ID_homed_onResponseStart = __IDLiveProgramPlayerInterface | 1005;

			void onProgramLost();

			void onProgramReselected();
			
			void onResponseStart(boolean succ);

			void onLiveInfoUpdated(int mask);

			void onCaModuleDispatched(int moduleId);
			
			void onIpStoped(long f,int pn);
		}
	}

	public static interface MosaicPlayerInterface extends LiveStreamSelectionBaseInterface,
			TeeveePlayerBaseInterface {
		static final int __IDMosaicPlayerInterface = __IDLiveStreamSelectionBaseInterface
				| __IDTeeveePlayerBaseInterface | (0x1 << 16);
		static final int __ID_select = __IDMosaicPlayerInterface | 1;

		void select(long freq, int fflags, String puri, int pflags);

		public static interface Callback extends LiveStreamSelectionBaseInterface.Callback,
				TeeveePlayerBaseInterface.Callback {
			static final int __ID_onLiveInfoUpdated = __IDMosaicPlayerInterface | 1002;

			void onLiveInfoUpdated(int mask);
		}
	}

	public static interface LocalSockTsPlayerInterface extends TeeveePlayerBaseInterface {

		static final int __IDLocalSockTsPlayerInterface = __IDTeeveePlayerBaseInterface
				| (0x1 << 16);
		static final int __ID_start = __IDLocalSockTsPlayerInterface | 1;
		static final int __ID_setRate = __IDLocalSockTsPlayerInterface | 2;
		static final int __ID_redirect = __IDLocalSockTsPlayerInterface | 3;

		void start(String sockname, int fflags, String puri, int pflags);

		void setRate(float r);

		/** 重定向数据源(清缓存),节目播放发保持 */
		void redirect(String sockname, int flags);

		public static interface Callback extends TeeveePlayerBaseInterface.Callback {
			static final int __ID_onResponseStart = __IDLocalSockTsPlayerInterface | 1000;
			static final int __ID_onRateChange = __IDLocalSockTsPlayerInterface | 1002;
			static final int __ID_onResponseRedirect = __IDLocalSockTsPlayerInterface | 1003;
			static final int __ID_onPlayProcessing = __IDLocalSockTsPlayerInterface | 1004;
			static final int __ID_onPlaySuspending = __IDLocalSockTsPlayerInterface | 1005;
			static final int __ID_onPlayerPTSChange = __IDLocalSockTsPlayerInterface | 1006;

			void onResponseStart(boolean succ);

			void onRateChange(float r);

			void onResponseRedirect(boolean succ);

			void onPlayProcessing(int pn, long pts_time);

			void onPlayerPTSChange(int pn, long pts_time, int state);

			void onPlaySuspending(int pn);
		}
	}

	public static interface IpQamTsPlayerInterface extends TeeveePlayerBaseInterface {
		static final int __IDIpQamTsPlayerInterface = __IDTeeveePlayerBaseInterface | (0x2 << 16);
		static final int __ID_start = __IDIpQamTsPlayerInterface | 1;
		static final int __ID_setRate = __IDIpQamTsPlayerInterface | 2;
		static final int __ID_syncSignalStatus = __IDIpQamTsPlayerInterface | 3;

		void start(String furi, int fflags, String puri, int pflags);

		void setRate(float r);

		void syncSignalStatus();

		public static interface Callback extends TeeveePlayerBaseInterface.Callback {
			static final int __ID_onResponseStart = __IDIpQamTsPlayerInterface | 1000;
			static final int __ID_onRateChange = __IDIpQamTsPlayerInterface | 1002;
			static final int __ID_onSyncSignalStatus = __IDIpQamTsPlayerInterface | 1003;
			static final int __ID_onPlayerPTSChange = __IDIpQamTsPlayerInterface | 1004;
			static final int __ID_onStreamResumed = __IDIpQamTsPlayerInterface | 1005;
			static final int __ID_onStreamLost = __IDIpQamTsPlayerInterface | 1006;

			// static final int __ID_onPlayProcessing =
			// __IDIpQamTsPlayerInterface | 1004;
			// static final int __ID_onPlaySuspending =
			// __IDIpQamTsPlayerInterface | 1005;

			void onResponseStart(boolean succ);

			void onRateChange(float r);

			void onSyncSignalStatus(String signalStatus);

			void onStreamResumed();
			
			void onStreamLost();
			
			// void onPlayProcessing(int pn, long pts_time);
			//
			// void onPlaySuspending(int pn);

			void onPlayerPTSChange(int pn, long pts_time, int state);
		}
	}
	
	public static interface HomedHttpPlayerInterface extends TeeveePlayerBaseInterface {

		static final int __IDLocalSockTsPlayerInterface = __IDTeeveePlayerBaseInterface
				| (0x3 << 16);
		static final int __ID_start = __IDLocalSockTsPlayerInterface | 1;
		static final int __ID_setRate = __IDLocalSockTsPlayerInterface | 2;
		static final int __ID_redirect = __IDLocalSockTsPlayerInterface | 3;
		static final int __ID_start_fd = __IDLocalSockTsPlayerInterface | 4;

		void start(String sockname, int fflags, String puri, int pflags);

		void startFd(long vfreq, ParcelFileDescriptor pfd, int fflags);
		
		void setRate(float r);

		/** 重定向数据源(清缓存),节目播放发保持 */
		void redirect(long vfreq, ParcelFileDescriptor pfd, int flags);

		public static interface Callback extends TeeveePlayerBaseInterface.Callback {
			static final int __ID_onResponseStart = __IDLocalSockTsPlayerInterface | 1000;
			static final int __ID_onRateChange = __IDLocalSockTsPlayerInterface | 1002;
			static final int __ID_onResponseRedirect = __IDLocalSockTsPlayerInterface | 1003;
			static final int __ID_onPlayProcessing = __IDLocalSockTsPlayerInterface | 1004;
			static final int __ID_onPlaySuspending = __IDLocalSockTsPlayerInterface | 1005;
			static final int __ID_onPlayerPTSChange = __IDLocalSockTsPlayerInterface | 1006;

			void onResponseStart(boolean succ);

			void onRateChange(float r);

			void onResponseRedirect(boolean succ);

			void onPlayProcessing(int pn, long pts_time);

			void onPlayerPTSChange(int pn, long pts_time, int state);

			void onPlaySuspending(int pn);
		}
	}

	public static interface TsPlayerSourceBaseInterface extends MediaSessionInterface {
		static final int __IDTsPlayerSourceBaseInterface = 0x400 << 16;
		static final int __ID_start = __IDTsPlayerSourceBaseInterface | 1;
		static final int __ID_stop = __IDTsPlayerSourceBaseInterface | 2;
		static final int __ID_pause = __IDTsPlayerSourceBaseInterface | 3;
		static final int __ID_resume = __IDTsPlayerSourceBaseInterface | 4;
		static final int __ID_seek = __IDTsPlayerSourceBaseInterface | 5;
		static final int __ID_start_2 = __IDTsPlayerSourceBaseInterface | 6;
		static final int __ID_seek_fd = __IDTsPlayerSourceBaseInterface | 7;

		void pause();

		void resume();

		void stop();

		void seek(long millis);
		
		void seek(long millis,ParcelFileDescriptor pfd);

		public static interface Callback {
			static final int __ID_onResponseStart = __IDTsPlayerSourceBaseInterface | 1000;
			static final int __ID_onResponseStop = __IDTsPlayerSourceBaseInterface | 1001;
			static final int __ID_onResponsePause = __IDTsPlayerSourceBaseInterface | 1002;
			static final int __ID_onResponseResume = __IDTsPlayerSourceBaseInterface | 1003;
			static final int __ID_onSourceSeek = __IDTsPlayerSourceBaseInterface | 1004;
			static final int __ID_onEndOfSource = __IDTsPlayerSourceBaseInterface | 1005;
			static final int __ID_onSourcePlayed = __IDTsPlayerSourceBaseInterface | 1006;
			static final int __ID_onSourceError = __IDTsPlayerSourceBaseInterface | 1007;
			static final int __ID_onSourceMessage = __IDTsPlayerSourceBaseInterface | 1008;
			static final int __ID_onSyncMediaTime = __IDTsPlayerSourceBaseInterface | 1009;
			static final int __ID_onSourceMediaChange = __IDTsPlayerSourceBaseInterface | 1010;
			static final int __ID_onSourceSinker = __IDTsPlayerSourceBaseInterface | 1011;

			void onResponseStart(boolean b);

			void onResponseStop();

			void onResponsePause(boolean b);

			void onResponseResume();

			void onSourceSeek(long t);

			void onEndOfSource(float rate);

			void onSourceError(String msg);

			void onSourceMessage(String msg);

			void onSourceSinker(String furi, String localsock, int pid);

			/** 应用程序通过协议类型区分:ipqam,localsock */
			void onSourcePlayed(String streamUri, String programUri);

			void onSyncMediaTime(long time);

			void onSourceMediaChange(long time, long pts);
		}
	}

	public static interface TsPlayerInetSourceInterface extends TsPlayerSourceBaseInterface {
		static final int __IDTsPlayerInetSourceInterface = __IDTsPlayerSourceBaseInterface
				| (0x1 << 16);

		static final int __ID_setRate = __IDTsPlayerInetSourceInterface | 1;
		static final int __ID_setCache = __IDTsPlayerInetSourceInterface | 2;
		static final int __ID_playCache = __IDTsPlayerInetSourceInterface | 3;

		public static enum Provider {
			Sihua("SihuaVodTsPlayerSource"), // Sihua VOD server
			Huawei("HuaweiVodTsPlayerSource"), // Huawei VOD server
			iPanel("iPanelVodTsPlayerSource"), // iPanel VOD server
			Ngod("NgodVodTsPlayerSource"), // Ngod VOD server
			iPanel_Ngod("iPanelNgodVodTsPlayerSource"), // iPanel VOD server
			Seachange("SeachangeVodTsPlayerSource"), // Seachange VOD server
			HomedHttp("HomedHttpTsPlayerSource"),// homed http server
			Native("NativeTsPlayerSource");// native server

			private String name;

			private Provider(String name) {
				this.name = name;
			}

			public String getName() {
				return name;
			}
		}

		/** 时移源 */
		public static final int TYPE_TIMESHIFT = 1;
		/** 回看源 */
		public static final int TYPE_SEEBACK = 2;
		/** 点播源 */
		public static final int TYPE_VOD = 3;
		/** http源 */
		public static final int TYPE_HTTP = 4;
		/** 本地源 */
		public static final int TYPE_NATIVE = 5;
		/** 数据流类型-IPQAM */
		public static final int STREAM_TYPE_IPQAM = 1;
		/** 数据流类型-INET */
		public static final int STREAM_TYPE_INET = 2;
		/** 数据流类型-IPQAM-SINKER */
		public static final int STREAM_TYPE_IPQAM_SINKER = 3;

		/** 时移标志-从最近时间开始播 */
		public static final int FLAGS_TIMESHIFT_FROM_END = 0x0;
		/** 时移标志-从起始时间开始播 */
		public static final int FLAGS_TIMESHIFT_FROM_SET = 0x1;

		void start(String uri, int type, int streamType, int flags);

		void start(ParcelFileDescriptor pfd, String uri, int type, int streamType, int flags);

		void setRate(float rate);

		void setCache(int bufsize);

		void playCache();

		public static interface Callback extends TsPlayerSourceBaseInterface.Callback {
			static final int __ID_onSourceRate = __IDTsPlayerInetSourceInterface | 1000;
			static final int __ID_onShiftStartTime = __IDTsPlayerInetSourceInterface | 1001;
			static final int __ID_onSeeBackPeriod = __IDTsPlayerInetSourceInterface | 1002;
			static final int __ID_onVodDuration = __IDTsPlayerInetSourceInterface | 1003;
			static final int __ID_onCachingState = __IDTsPlayerInetSourceInterface | 1004;

			void onSourceRate(float r);

			void onVodDuration(long d);

			void onShiftStartTime(long start);

			void onSeeBackPeriod(long start, long end);

			void onCachingState(float p);
		}
	}

	public static interface TsPlayerFileSourceInterface extends TsPlayerSourceBaseInterface {
		static final int __IDTsPlayerFileSourceInterface = __IDTsPlayerSourceBaseInterface
				| (0x2 << 16);
		static final int __ID_getFilePath = __IDTsPlayerFileSourceInterface | 1;

		/** 循环推送 */
		public static final int FLAG_START_LOOP = 0x01;

		void start(String url, int flags);

		String getFilePath();

		public static interface Callback extends TsPlayerSourceBaseInterface.Callback {
			static final int __ID_onSourceRewind = __IDTsPlayerFileSourceInterface | 1000;

			void onSourceRewind();
		}
	}

	public static interface LiveCaModuleSessionBaseInterface extends MediaSessionInterface {
		static final int __IDLiveCaModuleSessionInterface = 0x800 << 16;
		static final int __ID_queryNextScrollMessage = __IDLiveCaModuleSessionInterface | 1;
		static final int __ID_queryUnreadMailSize = __IDLiveCaModuleSessionInterface | 2;
		static final int __ID_checkEntitlementUpdate = __IDLiveCaModuleSessionInterface | 3;
		static final int __ID_queryUnreadMail = __IDLiveCaModuleSessionInterface | 4;

		void queryNextScrollMessage();

		void queryUnreadMailSize();

		void checkEntitlementUpdate();

		void queryUnreadMail(String token);

		public static interface Callback {
			static final int __ID_onScrollMessage = __IDLiveCaModuleSessionInterface | 1000;
			static final int __ID_onUnreadMailSize = __IDLiveCaModuleSessionInterface | 1001;
			static final int __ID_UrgencyMails = __IDLiveCaModuleSessionInterface | 1002;

			void onScrollMessage(String msg);

			void onUnreadMailSize(int n);

			void onUrgencyMails(String token, Bundle b);
		}
	}

	public static interface SettingsCaModuleSessionInterface extends MediaSessionInterface {
		static final int __IDSettingsCaModuleSessionInterface = 0x801 << 16;
		static final int __ID_updateSettings = __IDSettingsCaModuleSessionInterface | 1;
		static final int __ID_querySettings = __IDSettingsCaModuleSessionInterface | 2;
		static final int __ID_queryReadableEntries = __IDSettingsCaModuleSessionInterface | 3;
		static final int __ID_buyEntitlement = __IDSettingsCaModuleSessionInterface | 4;
		static final int __ID_checkEntitlementUpdate = __IDSettingsCaModuleSessionInterface | 5;

		void updateSettings(String token, Bundle b);

		void querySettings(String token, Bundle b);

		void queryReadableEntries();

		void buyEntitlement(String uri, String ext);

		void checkEntitlementUpdate();

		public static interface Callback {
			static final int __ID_onReadableEntries = __IDSettingsCaModuleSessionInterface | 1000;
			static final int __ID_onResponseQuerySettings = __IDSettingsCaModuleSessionInterface | 1002;
			static final int __ID_onResponseUpdateSettings = __IDSettingsCaModuleSessionInterface | 1003;
			static final int __ID_onResponseBuyEntitlement = __IDSettingsCaModuleSessionInterface | 1004;
			static final int __ID_onSettingsUpdated = __IDSettingsCaModuleSessionInterface | 1005;

			void onReadableEntries(HashMap<String, String> entries);

			void onResponseQuerySettings(String token, Bundle b);

			void onResponseUpdateSettings(String token, String err);

			void onResponseBuyEntitlement(String uri, String err);

			void onSettingsUpdated(String token);

		}
	}
}
