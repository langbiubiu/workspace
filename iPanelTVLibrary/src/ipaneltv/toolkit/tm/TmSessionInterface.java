package ipaneltv.toolkit.tm;

public interface TmSessionInterface {
	static final int __IDTmSessionInterface = 0x1 << 16;
	static final int __ID_close = __IDTmSessionInterface | 1;

	void close();

	public static interface LiveTmSessionInterface extends TmSessionInterface {
		static final int __IDLiveTmSessionInterface = 0x2 << 16;
		static final int __ID_uploadCurrentChannelInfo = __IDLiveTmSessionInterface | 1;
		static final int __ID_uploadChannelRatings = __IDLiveTmSessionInterface | 2;
		static final int __ID_uploadProgramRatings = __IDLiveTmSessionInterface | 3;

		void uploadCurrentChannelInfo(String json);

		void uploadChannelRatings(String json);

		void uploadProgramRatings(String json);

		public static interface Callback {
			static final int __ID_onQueryCurrentChannelInfo = __IDLiveTmSessionInterface | 1001;
			static final int __ID_onQueryChannelRatings = __IDLiveTmSessionInterface | 1002;
			static final int __ID_onQueryProgramRatings = __IDLiveTmSessionInterface | 1003;
			static final int __ID_onChannelConnected = __IDLiveTmSessionInterface | 1004;

			void onQueryCurrentChannelInfo();

			void onQueryChannelRatings(String json);

			void onQueryProgramRatings(String json);
		}
	}

	public static interface VodTmSessionInterface extends TmSessionInterface {
		static final int __IDVodTmSessionInterface = 0x3 << 16;
		static final int __ID_uploadCurrentVodInfo = __IDVodTmSessionInterface | 1;

		void uploadCurrentVodInfo(String json);

		public static interface Callback {
			static final int __ID_onQueryCurrentVodInfo = __IDVodTmSessionInterface | 1001;

			void onQueryCurrentVodInfo();
		}
	}

	public static interface PlatformTmSessionInterface extends TmSessionInterface {

	}

	public static interface AppTmSessionInterface extends TmSessionInterface {

	}

}
