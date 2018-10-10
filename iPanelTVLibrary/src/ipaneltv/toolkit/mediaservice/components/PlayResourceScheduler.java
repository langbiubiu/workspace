package ipaneltv.toolkit.mediaservice.components;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.media.ReserveStateInterface;
import ipaneltv.toolkit.mediaservice.LiveNetworkApplication;
import ipaneltv.toolkit.mediaservice.LiveNetworkApplication.TunerInfo;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

import android.media.TeeveePlayer;
import android.media.TeeveePlayer.PlayStateListener;
import android.media.TeeveePlayer.ProgramStateListener;
import android.media.TeeveeRecorder;
import android.media.TeeveeRecorder.OnRecordStateListener;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.Uri;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.NetworkInterface;
import android.net.telecast.ProgramInfo;
import android.net.telecast.SignalStatus;
import android.net.telecast.StreamSelector;
import android.net.telecast.StreamSelector.SelectionStateListener;
import android.net.telecast.TransportManager;
import android.util.Log;

public class PlayResourceScheduler extends LiveNetworkApplication.AppComponent {
	public static final int PRIORITY_DEFAULT = 5;
	static final String TAG = PlayResourceScheduler.class.getSimpleName();
	private SelectorBundle selectorBundle = new SelectorBundle();
	private PlayerBundle playerBundle = new PlayerBundle();
	private List<ResourcesState> stats = new ArrayList<ResourcesState>();

	@SuppressWarnings("rawtypes")
	public PlayResourceScheduler(LiveNetworkApplication app) {
		super(app);
	}

	public final TransportManager getTransportManager() {
		return getApp().getTransportManager();
	}

	public ResourcesState createLivePlayState(boolean pushOnly) {
		return createLivePlayState(pushOnly, PRIORITY_DEFAULT, false);
	}

	public ResourcesState createLivePlayState(boolean pushOnly, int pri, boolean soft) {
		synchronized (stats) {
			ResourcesState ret = new ResourcesState(pri, soft);
			ret.selectorHandle = ret.createStreamSelectorHandle(pushOnly, 0);
			ret.playerHandle = ret.createTeeveePlayerHandle(1, 0);
			stats.add(ret);
			return ret;
		}
	}

	/**
	 * 
	 * @param pushOnly
	 *            是否为只推流的StreamSelector
	 * @param pri
	 *            优先级，值越大优先级越高。
	 * @param soft
	 *            是否可被抢占
	 * @param pipSize
	 *            支持的画中画数量
	 * @param selectorFlags
	 *            StreamSelector 参数
	 * @param playerFlags
	 *            player参数
	 * @return
	 */
	public ResourcesState createLivePlayState(boolean pushOnly, int pri, boolean soft, int pipSize,
			int selectorFlags, int playerFlags) {
		synchronized (stats) {
			ResourcesState ret = new ResourcesState(pri, soft);
			ret.selectorHandle = ret.createStreamSelectorHandle(pushOnly, 0);
			ret.playerHandle = ret.createTeeveePlayerHandle(pipSize, playerFlags);
			stats.add(ret);
			return ret;
		}
	}

	public ResourcesState createIpQamTsPlayState(boolean pushOnly) {
		return createIpQamTsPlayState(pushOnly, PRIORITY_DEFAULT, false);
	}

	public ResourcesState createIpQamTsPlayState(boolean pushOnly, int pri, boolean soft) {
		synchronized (stats) {
			ResourcesState ret = new ResourcesState(pri, soft);
			ret.selectorHandle = ret.createStreamSelectorHandle(pushOnly, 0);
			ret.playerHandle = ret.createTeeveePlayerHandle(1, 0);
			stats.add(ret);
			return ret;
		}
	}

	public ResourcesState createPushPlayState() {
		return createPushPlayState(PRIORITY_DEFAULT, false);
	}

	public ResourcesState createPushPlayState(int pri, boolean soft) {
		synchronized (stats) {
			ResourcesState ret = new ResourcesState(pri, soft);
			ret.selectorHandle = ret.createStreamSelectorHandle(true, 0);
			ret.playerHandle = ret.createTeeveePlayerHandle(1, 0);
			stats.add(ret);
			return ret;
		}
	}

	public ResourcesState createStreamSelectState(boolean pushOnly, int streamSize) {
		return createStreamSelectState(pushOnly, streamSize, PRIORITY_DEFAULT, false);
	}

	public ResourcesState createStreamSelectState(boolean pushOnly, int streamSize, int pri,
			boolean soft) {
		synchronized (stats) {
			ResourcesState ret = new ResourcesState(pri, soft);
			ret.selectorHandle = ret.createStreamSelectorHandle(pushOnly, streamSize < 0 ? 0
					: streamSize > 4 ? 4 : streamSize);
			stats.add(ret);
			return ret;
		}
	}

	// ===================================================================
	// ======================== ResourceBinding ==========================
	// ===================================================================
	static class ResourceBinding<T> {
		private ResourceWarper<T> resource;
		private Object owner = null;
		private Object version = null;

		ResourceBinding(ResourceWarper<T> r, Object o) {
			resource = r;
			owner = o;
			IPanelLog.d(TAG, "ResourceBinding this = " + this + ";resource = " + resource + ";o = "
					+ o);
		}

		ResourceWarper<T> reserve(Object o) {
			IPanelLog.d(TAG, "reserve o = " + o + ";owner = " + owner);
			if (o == null) {
				return null;
			} else if (o == owner) {
				return resource;
			} else if (owner != null) {
				return null;
			}// else owner ==null

			IPanelLog.d(TAG, "reserve this = " + this + ";resource = " + resource);
			if (owner == null && resource != null) {
				IPanelLog.d(TAG, "reserve resource = " + resource);
				if (resource.setWeak(false)) {
					owner = o;
					return resource;
				}
			}
			return null;
		}

		boolean clearLastVersion(Object v) {
			IPanelLog.d(TAG, "versionObject = " + version + " v = " + v);
			boolean ret = v == version;
			version = null;
			IPanelLog.d(TAG, "ret = " + ret);
			return ret;
		}

		boolean release(Object o, Object v) {
			IPanelLog.d(TAG, "release this = " + this + ";o = " + o + ";owner = " + owner);
			if (o == null)
				return false;
			if (o == owner) {
				owner = null;
				version = v;
				resource.setWeak(true);
				return true;
			}
			return false;
		}

		boolean isIdle() {
			return owner == null;
		}

		boolean isAvailable() {
			return resource.isAvailable();
		}

		void dispose() {
			resource.release();
			owner = null;
		}
	}

	// ===================================================================
	// ======================== ResourceBundle ===========================
	// ===================================================================
	abstract class ResourceBundle<T> {
		private List<ResourceBinding<T>> res = new ArrayList<ResourceBinding<T>>();

		ResourceBinding<T> reserve(Object owner, ResourceTag tag) {
			IPanelLog.i(TAG, "reserve");
			garbage();
			IPanelLog.i(TAG, "reserve tag =" + tag);
			List<ResourceBinding<T>> disable = new ArrayList<ResourceBinding<T>>();
			for (ResourceBinding<T> rb : res) {
				if (rb.isIdle() && rb.isAvailable()) {
					IPanelLog.i(TAG, "reserve isAvailable and isIdle");
					if (checkMatch(rb.resource, tag)) {
						IPanelLog.i(TAG, "checkMatch is true");
						if (rb.reserve(owner) != null) {
							return rb;
						} else {
							// 对于从home切换到dtmb再切到直播有setweak 失败的情况。reserve
							// 失败则判断是否是资源已经被抢的原因。并释放以接触绑定。
							if (!rb.isAvailable()) {
								rb.dispose();
								disable.add(rb);
								// res.remove(rb);
							}
						}
					}
				}
			}
			IPanelLog.i(TAG, "checkMatch is false 1 res.size() = " + res.size());
			res.removeAll(disable);
			IPanelLog.i(TAG, "checkMatch is false res.size() = " + res.size());
			ResourceWarper<T> r = onCreateResource(tag);
			ResourceBinding<T> ret = new ResourceBinding<T>(r, owner);
			if (ret.reserve(owner) != null) {
				res.add(ret);
				return ret;
			}
			return null;
		}

		void garbage() {// 关闭失效的资源
			List<ResourceBinding<T>> weaken = null;
			IPanelLog.d(TAG, "garbage in");
			for (ResourceBinding<T> rb : res) {
				if (!rb.isAvailable()) {
					IPanelLog.d(TAG, "garbage rb = " + rb);
					if (weaken == null)
						weaken = new ArrayList<ResourceBinding<T>>();
					weaken.add(rb);
				}
			}
			clean(weaken);
			IPanelLog.d(TAG, "garbage end");
		}

		private void clean(List<ResourceBinding<T>> list) {
			IPanelLog.d(TAG, "clean list = " + list);
			if (list == null)
				return;
			res.removeAll(list);
			IPanelLog.d(TAG, "clean list 22 = " + list);
			for (ResourceBinding<T> rb : list) {
				IPanelLog.d(TAG, "Resource clean really to release type = " + rb.resource.get());
				rb.dispose();
			}
		}

		boolean checkMatch(ResourceWarper<T> r, ResourceTag tag) {
			IPanelLog.d(TAG, "checkMatch r.getTag()= " + r.getTag() + ";tag = " + tag);
			return r.getTag() == null ? tag == null
					: (tag == null ? false : r.getTag().equals(tag));
		}

		abstract ResourceWarper<T> onCreateResource(ResourceTag p);

	}

	class SelectorBundle extends ResourceBundle<StreamSelector> {
		ResourceWarper<StreamSelector> onCreateResource(ResourceTag p) {
			TransportManager tm = getTransportManager();
			SelectorTag tag = (SelectorTag) p;
			int ifid = tag.dtype < 0 ? -1 : 0;
			Log.d(TAG, "onCreateResource tag.dtype = " + tag.dtype);
			if (tag.dtype > 0) {
				NetworkInterface ni = tm.getDefaultNetworkInterfaceByType(tag.dtype);
				Log.d(TAG, "onCreateResource ni = " + ni);
				if (ni == null) {
					ifid = 1001;
				} else {
					ifid = ni.getId();
				}
			}
			StreamSelector ss = null;
			IPanelLog.d(TAG, "onCreateResource ifid = " + ifid);
			if (tag.streamSize > 0) {
				ss = tm.createSelector(ifid, StreamSelector.CREATE_FLAG_DEFAULT);
			} else {
				ss = tm.createSelector(ifid);
			}
			if (ss != null) {
				return new StreamSelectorResource(ss, tag);
			}
			return null;
		}
	}

	static interface ResourceTag {
	}

	public class SelectorTag implements ResourceTag {
		SelectorTag(int dt, int s) {
			dtype = dt < 0 ? -1 : dt;
			streamSize = s;
		}

		int dtype;
		int streamSize;

		@Override
		public boolean equals(Object o) {
			if (o instanceof SelectorTag) {
				SelectorTag t = (SelectorTag) o;
				IPanelLog.i(TAG, "equals dtype =" + dtype + "t.dtype" + t.dtype + "streamSize"
						+ streamSize + "t.streamSize" + t.streamSize);
				return dtype == t.dtype && streamSize == t.streamSize;
			}
			return false;
		}
	}

	public class PlayerTag implements ResourceTag {
		public PlayerTag(int size, int f) {
			pipSize = size;
			flags = f;
		}

		int flags;
		int pipSize;

		@Override
		public boolean equals(Object o) {
			if (o instanceof PlayerTag) {
				PlayerTag t = (PlayerTag) o;
				Log.d(TAG, "equals 2 flags = " + flags + ";pipSize = " + pipSize + ";t.flags = "
						+ t.flags + ";t.pipSize = " + t.pipSize);
				return flags == t.flags && pipSize == t.pipSize;
			}
			return false;
		}
	}

	class PlayerBundle extends ResourceBundle<TeeveePlayer> {
		ResourceWarper<TeeveePlayer> onCreateResource(ResourceTag p) {
			TeeveePlayer tp;
			PlayerTag pt = (PlayerTag) p;
			Log.d(TAG, "pt.pipSize = " + pt.pipSize);
			// 基低默认传支持一路画中画，带音视频。后续有可能需要多路画中画//TODO
			if (pt.pipSize > 0) {
				tp = TeeveePlayer.createTeeveePlayer(getApp(), pt.pipSize, pt.flags);
			} else {
				tp = TeeveePlayer.createTeeveePlayer(getApp(), 0, TeeveePlayer.CREATE_FLAG_DEFAULT);
			}
			if (tp != null) {
				if (!tp.prepare()) {
					tp.release();
					tp = null;
				}
				if (tp != null)
					return new TeeveePlayerResource(tp, pt);
			}
			return null;
		}

	}

	// ===================================================================
	// ======================== ResourceHandle ===========================
	// ===================================================================
	public abstract class ResourceHandle<T> {
		private ResourceBundle<T> bundle;
		private ResourceBinding<T> binding = null;
		private ResourceWarper<T> r = null;
		private final Object versionObject = new Object();

		ResourceHandle(ResourceBundle<T> bundle) {
			this.bundle = bundle;
		}

		T get() {
			if (r == null)
				throw new RuntimeException("is not reserved!");
			return r.get();
		}

		ResourceWarper<T> getW() {
			if (r == null)
				throw new RuntimeException("is not reserved!");
			return r;
		}

		ResourceWarper<T> getW2() {
			return r;
		}

		abstract void setListenerEnable(boolean b);

		/**
		 * return == 0, 资源未被抢占，保持原样.<br>
		 * return == 1, 资源被抢，但已恢复 .<br>
		 * return ==-1, 资源保留失败
		 */
		int reserve(ResourceTag tag) throws RuntimeException {
			IPanelLog.i(TAG, "reserve 1 binding = " + binding);
			IPanelLog.d(TAG, "versionObject = " + versionObject);
			try {
				if (binding != null) {
					// 尝试恢复资源
					if ((r = binding.reserve(this)) != null) {
						IPanelLog.i(TAG, "reserve 111 r = " + r);
						return binding.clearLastVersion(versionObject) ? 0 : 1;
					}
					IPanelLog.i(TAG, "reserve 222 r binding =null");
					binding = null;
				}
				// 尝试获取新的资源
				if ((binding = bundle.reserve(this, tag)) != null) {
					r = binding.reserve(this);
					return 1;
				}
			} catch (Exception e) {
				Log.e(TAG, "reserve error = " + e.getMessage());
			}

			// 失败
			return -1;
		}

		void release() {
			IPanelLog.d(TAG, "release binding = " + binding);
			if (binding != null) {
				if (binding.release(this, versionObject)) {
					// setListenerEnable(false);
					IPanelLog.i(TAG, "release listener");
				}
				// DO NOT set rb == null!!!
			}
			r = null;
		}

		void destroy() {
			if (binding != null) {
				binding.dispose();
				binding = null;
			}
		}

	}

	// ===================================================================
	// ======================== ResourcesState ===========================
	// ===================================================================
	public class ResourcesState implements ReserveStateInterface {

		private Object stateMutex = new Object();
		private Object pipMutex = new Object();
		StreamSelectorHandle selectorHandle;
		TeeveePlayerHandle playerHandle;
		StreamSelector pipSelector;
		TeeveePlayer pipPlayer;
		ResourceTag selectTag = null, playerTag = null;
		FileDescriptor fd;
		private boolean reserved = false, listend = false, soft, stateCleared = false,
				opened = false;
		private Object tag;
		private int priority = PRIORITY_DEFAULT;
		List<TeeveePlayer> pipPlayerList = null;
		TeeveeRecorder recorder;
		StreamSelector selector;
		int recoderFlag;

		ResourcesState(int p, boolean soft) {
			p = p < 0 ? 0 : p > 10 ? 10 : p;
			this.priority = p;
			this.soft = soft;
		}

		public boolean openPipPlayers(int size, int falg) {
			synchronized (stateMutex) {
				if (size > ((PlayerTag) playerTag).pipSize) {
					return false;
				} else if (pipPlayerList.size() >= size) {
					return true;
				} else if (pipPlayerList.size() > 0) {
					size = size - pipPlayerList.size();
				}
				for (int i = 0; i < size; i++) {
					TeeveePlayer pipPlayer = TeeveePlayer.createPipTeeveePlayer(playerHandle.get(),
							falg);
					if (pipPlayer != null) {
						if (pipPlayer.prepare()) {
							if (pipPlayer.setDataSource(selectorHandle.get())) {
								pipPlayerList.add(pipPlayer);
							}
						} else {
							pipPlayer.release();
							break;
						}
					} else {
						// 创建画中画播放器失败，可能是已经达到上限。
						Log.e(TAG, "openPipPlayers createPipTeeveePlayer failed !!");
						break;
					}
				}
				return true;
			}
		}

		public boolean closePipPlayers() {
			synchronized (stateMutex) {
				for (int i = 0; i < pipPlayerList.size(); i++) {
					Log.d(TAG, "closePipPlayers i = " + i);
					TeeveePlayer player = pipPlayerList.get(i);
					player.setPlayStateListener(null);
					player.stop();
					player.release();
				}
				pipPlayerList.clear();
			}
			return true;
		}

		public void pipSetFreqency(int index, long freq, int flag) {

		}

		public boolean pipSetProgram(int index, ProgramInfo p, int x, int y, int w, int h, int flag) {
			synchronized (stateMutex) {
				if (index > pipPlayerList.size() || p == null || !isReserved())
					return false;
				pipPlayerList.get(index).start();
				if (pipPlayerList.get(index).selectProgram(p, flag)) {
					pipPlayerList.get(index).setDisplay(x, y, w, h);
					return true;
				}
			}
			return false;
		}

		public boolean openTeeveeRecoder(int flags) {
			synchronized (stateMutex) {
				if (recorder == null) {
					recoderFlag = flags;
					switch (flags) {
					case 0:
						recorder = TeeveeRecorder.createTeeveeRecorder(getApp());
						Log.i(TAG, "TeeveeRecorder createTeeveeRecorder");
						if (recorder != null) {
							Log.i(TAG, "TeeveeRecorder  prepare");
							if (recorder.prepare()) {
								return recorder.setDataSource(selectorHandle.get());
							} else {
								Log.i(TAG, "TeeveeRecorder  release");
								recorder.release();
								recorder = null;
							}
						}
						break;
					case 1:
						TransportManager tm = getTransportManager();
						NetworkInterface ni = tm
								.getDefaultNetworkInterfaceByType(NetworkInterface.DELIVERY_CABLE);
						recorder = TeeveeRecorder.createTeeveeRecorder(getApp());
						if (ni != null) {
							selector = tm.createSelector(ni.getId(),
									StreamSelector.CREATE_FLAG_DEFAULT);
						}
						Log.i(TAG, "TeeveeRecorder 11 createTeeveeRecorder");
						if (recorder != null && selector != null) {
							selector.setNetworkUUID(getUUID());
							Log.i(TAG, "TeeveeRecorder 11  prepare");
							if (recorder.prepare()) {
								return recorder.setDataSource(selector);
							} else {
								Log.i(TAG, "TeeveeRecorder 1  release");
								recorder.release();
								recorder = null;
							}
						}
						break;

					default:
						break;
					}

					return false;
				}
				return true;
			}
		}

		public void setOnRecordStateListener(OnRecordStateListener l) {
			synchronized (stateMutex) {
				if (recorder != null) {
					recorder.setOnRecordStateListener(l);
				}
			}
		}

		public void closeTeeveeRecoder() {
			Log.i(TAG, "closeTeeveeRecoder1");
			synchronized (stateMutex) {
				if (recorder != null) {
					recorder.setOnRecordStateListener(null);
					recorder.stop();
					recorder.release();
					recorder = null;
				}
				Log.i(TAG, "closeTeeveeRecoder 22");
				if (selector != null) {
					selector.release();
					selector = null;
				}
				Log.i(TAG, "closeTeeveeRecoder 33");
				recoderFlag = 0;
			}
		}

		public boolean setTeeveeRecoder(FileDescriptor fd, int off, int len, FrequencyInfo fi,
				int fflags, ProgramInfo pi, int pflags) {
			Log.i(TAG, "setTeeveeRecoder fd = " + fd + ";fi = " + fi + ";pflags = " + pflags
					+ "; pi = " + pi + "; pflags = " + pflags + ";recoderFlag = " + recoderFlag);
			synchronized (stateMutex) {
				if (recoderFlag == 1) {
					if (selector != null) {
						if (!selector.select(fi, fflags)) {
							return false;
						}
					} else {
						return false;
					}
				}
				if (recorder != null) {
					Log.i(TAG, "TeeveeRecorder  start");
					recorder.start();
					return recorder.selectProgram(fd, off, len, pi, pflags);
				}
				return false;
			}
		}

		public boolean isHomedPipPlayerOpened(){
			synchronized (stateMutex) {
				synchronized (pipMutex) {
					return opened;
				}
			}
		}
		
		public void homedPause(){
			synchronized (pipMutex) {
				if (opened) {
					paused = true;
					pipPlayer.pause();
				}
			}
		}
		public void homedResume(){
			synchronized (pipMutex) {
				if (opened) {
					paused = false;
					pipPlayer.resume();
				}
			}
		}
		
		public boolean openHomedPipPlayer() {
			synchronized (stateMutex) {
				synchronized (pipMutex) {
					if (!opened) {
						try {
							TransportManager tm = getTransportManager();
							pipSelector = tm.createSelector(-1, StreamSelector.CREATE_FLAG_DEFAULT);
							pipPlayer = TeeveePlayer.createPipTeeveePlayer(playerHandle.get(), 0);
							if (pipPlayer != null && pipSelector != null) {
								pipSelector.setNetworkUUID(getUUID());
								pipPlayer.setPlayStateListener(psl);
								if (pipPlayer.prepare()) {
									boolean b = pipPlayer.setDataSource(pipSelector);
									opened = true;
									Log.d(TAG, "openHomedPipPlayer b = " + b);
								} else {
									closeHomedPipPlayer(true);
									return false;
								}
							} else {
								// 创建画中画播放器失败，可能是已经达到上限。
								Log.e(TAG, "openHomedPipPlayer createPipTeeveePlayer failed !!");
								closeHomedPipPlayer(true);
								return false;
							}
						} catch (Exception e) {
							Log.d(TAG, "openHomedPipPlayer error = " + e.getMessage());
							if (pipSelector != null) {
								pipSelector.release();
								pipSelector = null;
							}
							if (pipPlayer != null) {
								pipPlayer.release();
								pipPlayer = null;
							}
							return false;
						}
					}
					return true;
				}
			}
		}

		PlayStateListener psl;

		public void setHomedPlayerListener(PlayStateListener psl) {
			IPanelLog.i(TAG, "setHomedPlayerListenerpsl = " + psl);
			this.psl = psl;
		}

		boolean started = false;
		boolean paused = false;
		int pflags = 0;
		
		public boolean closeHomedPipPlayer(boolean clear) {
			synchronized (stateMutex) {
				synchronized (pipMutex) {
					if (opened) {
						opened = false;
						started = false;
						IPanelLog.i(TAG, "closeHomedPipPlayer  4 pipSelector = " + pipSelector
								+ ";pipPlayer = " + pipPlayer +";clear = "+ clear);
						if (pipPlayer != null) {
							if(clear){
								pipPlayer.setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
								pipPlayer.stop();
								pipPlayer.setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);	
							}else{
								pipPlayer.setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
								pipPlayer.stop();
								pipPlayer.setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);	
							}
							pipPlayer.release();
							pipPlayer = null;
							IPanelLog.i(TAG, "closeHomedPipPlayer 11 out");
						}
						if (pipSelector != null) {
							pipSelector.release();
							fd = null;
							pipSelector = null;
							IPanelLog.i(TAG, "closeHomedPipPlayer out");
						}
					}
				}
			}
			return true;
		}

		public void homedSetDisplay(int x, int y, int w, int h) {
			synchronized (pipMutex) {
				if (opened) {
					pipPlayer.setDisplay(x, y, w, h);
				}
			}
		}

		public void homedSetVolume(float v) {
			synchronized (pipMutex) {
				if (opened) {
					Log.d(TAG, "homedSetVolume v = "+ v);
					pipPlayer.setVolume(v);
				}
			}
		}

		public void homedStop(int flags) {
			synchronized (pipMutex) {
				if (opened) {
					Log.d(TAG, "homedStop 22 flags= " + flags);
					started = false;
					if ((flags & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_BLACK) {
						Log.d(TAG, "homedStop black");
						pipPlayer.setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
						pipPlayer.stop();
						pipPlayer.setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
						pipSelector.select((FileDescriptor) null, 0);
					} else if ((flags & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) {
						pipPlayer.setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
						pipPlayer.stop();
						pipPlayer.setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
						pipSelector.select((FileDescriptor) null, 0);
					}
				}
			}
		}

		public boolean homedstart(String sockname, int fflags, ProgramInfo pinfo, int pflags) {
			synchronized (pipMutex) {
				if (opened) {
					if (sockname != null) {
						if (!selectVFrequecy(sockname, fflags)) {
							Log.e(TAG, "homedstart start>selectVFrequecy failed");
							return false;
						}
					}
					Log.d(TAG, "homedstart select program 111111 = " + pinfo +";started = "+ started);
					if(paused){
						homedResume();
					}
					pipPlayer.start();
					pipPlayer.clear();
					Log.d(TAG, "homedstart select program 222 cc");
					if (!pipPlayer.selectProgram(pinfo, pflags)) {
						Log.e(TAG, "TeeveePlayer select failed");
						return false;
					}
					Log.d(TAG, "homedstart select program end");
					return true;
				}
			}
			return false;
		}
		public boolean homedstartQuick(String sockname, int fflags, ProgramInfo pinfo, int pflags) {
			synchronized (pipMutex) {
				if (opened) {
					if (sockname != null) {
						if (!selectVFrequecy(sockname, fflags)) {
							Log.e(TAG, "homedstartQuick start>selectVFrequecy failed");
							return false;
						}
					}
					Log.d(TAG, "homedstartQuick select program 22222 = " + pinfo +";started = "+ started+
							";psused = "+ paused+";pflags = "+ pflags+";this.pflags = "+ this.pflags);
					if(paused){
						homedResume();
					}
					if(!started||pflags != this.pflags){
						pipPlayer.stop();
						pipPlayer.start();
						Log.d(TAG, "homedstartQuick select program 222 cc");
						if (!pipPlayer.selectProgram(pinfo, pflags)) {
							Log.e(TAG, "homedstartQuick TeeveePlayer select failed");
							return false;
						}
						started = true;
						this.pflags = pflags;
					}
					Log.d(TAG, "homedstartQuick select program end");
					return true;
				}
			}
			return false;
		}

		public boolean homedSelectFd(long vfreqency, FileDescriptor fd, int fflags) {
			synchronized (pipMutex) {
				Log.d(TAG, "homedSelectFd 2 vfreqency = " + vfreqency);
				if (opened) {
					pipPlayer.setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
					pipSelector.select((FileDescriptor) null, 0);
					Log.d(TAG, "homedSelectFd 3");
					pipSelector.setVirtualFrequency(vfreqency);
					Log.d(TAG, "homedSelectFd 44");
					pipSelector.clear();
					Log.d(TAG, "homedSelectFd> fd = " + fd);
					if (!pipSelector.select(fd, fflags)) {
						Log.e(TAG, "homedSelectFd StreamSelector select failed");
						return false;
					}
					this.fd = fd;
					Log.d(TAG, "homedSelectFd ok");
					return true;
				}
			}
			return false;
		}
		public boolean homedSelectFdQuick(long vfreqency, FileDescriptor fd, int fflags) {
			synchronized (pipMutex) {
				Log.d(TAG, "homedSelectFdQuick 2 vfreqency = " + vfreqency);
				if (opened) {
					pipPlayer.setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
					pipSelector.select((FileDescriptor) null, 0);
					Log.d(TAG, "homedSelectFdQuick 3");
					pipSelector.setVirtualFrequency(vfreqency);
					Log.d(TAG, "homedSelectFdQuick 44");
					pipSelector.clear();
					pipPlayer.clear();
					Log.d(TAG, "homedSelectFdQuick> fd = " + fd);
					if (!pipSelector.select(fd, fflags)) {
						Log.e(TAG, "homedSelectFdQuick StreamSelector select failed");
						return false;
					}
					this.fd = fd;
					Log.d(TAG, "homedSelectFdQuick ok");
					return true;
				}
			}
			return false;
		}

		public void homedClear() {
			Log.d(TAG, "homedClear in");
			synchronized (pipMutex) {
				Log.d(TAG, "homedClear opened = " + opened + ";fd = " + fd);
				if (opened && fd != null) {
					pipPlayer.setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
					// 先进行数据注入停止，并清除demux中的缓存
					pipSelector.select((FileDescriptor) null, 0);
					fd = null;
				}
				Log.d(TAG, "homedClear end");
			}
		}

		private boolean selectVFrequecy(String localsock, int fflags) {
			LocalSocket sock = new LocalSocket();
			try {
				Uri uri = Uri.parse(localsock);
				String sockname = uri.getAuthority();
				if (!"-1".equals(sockname)) {
					long freq = Long.parseLong(uri.getQueryParameter("vfrequency"));
					Log.d(TAG, "selectVFrequecy> (sockname=" + sockname + ",freq=" + freq + ")");
					sock.connect(new LocalSocketAddress(sockname));
					FileDescriptor fd = sock.getFileDescriptor();
					pipSelector.setVirtualFrequency(freq);
					pipSelector.clear();
					Log.d(TAG, "selectVFrequecy> fd = " + fd);
					if (!pipSelector.select(fd, fflags)) {
						Log.e(TAG, "StreamSelector select failed");
						return false;
					}
					this.fd = fd;
					Log.d(TAG, "selectVFrequecy ok");
				}
				return true;
			} catch (Exception e) {
				Log.e(TAG, "selectVFrequecy e:" + e.getMessage());
				e.printStackTrace();
				return false;
			} finally {
				closeSocket(sock);
			}
		}

		private void closeSocket(LocalSocket s) {
			if (s != null) {
				try {
					s.close();
				} catch (Exception e) {
					Log.e(TAG, "close socket failed");
				}
			}
		}

		public void close() {
			closeHomedPipPlayer(true);
			closePipPlayers();
			closeTeeveeRecoder();
			if (selectorHandle != null)
				selectorHandle.release();
			if (playerHandle != null)
				playerHandle.release();
		}

		public Object getTag() {
			return tag;
		}

		public void setTag(Object tag) {
			this.tag = tag;
		}

		void ensureListen() {
			if (!listend) {
				listend = true;
				if (selectorHandle != null)
					selectorHandle.setListenerEnable(true);
				if (playerHandle != null)
					playerHandle.setListenerEnable(true);
			}
		}

		private final boolean canRace(ResourcesState s) {// 可抢占
			IPanelLog.d(TAG, "canRace s.priority = " + s.priority + ";priority = " + priority
					+ ";s.soft = " + s.soft);
			return s.priority < priority || (s.priority == priority && s.soft);
		}

		private boolean racePlayer() {
			boolean ret = false;
			IPanelLog.d(TAG, "racePlayer 111");
			ResourcesState race = null;
			for (ResourcesState s : stats) {
				if (s.isReserved()) {
					if (playerBundle.checkMatch(s.getPlayer().getW(), playerTag)) {
						if (s.playerHandle != null && canRace(s)) {
							if (race == null ? true : race.canRace(s))
								race = s;// 找优先级最低的
						}
					}
				}
			}

			IPanelLog.d(TAG, "racePlayer race = " + race);
			if (race != null) {
				race.loosen(true);
				ret = playerHandle.reserve(playerTag) != -1;
			}
			return ret;
		}

		private boolean raceSelector() {
			boolean ret = false;
			ResourcesState race = null;
			IPanelLog.d(TAG, "raceSelector 111");
			for (ResourcesState s : stats) {
				if (s.isReserved()) {
					if (selectorBundle.checkMatch(s.getSelector().getW(), selectTag)) {
						if (s.selectorHandle != null && canRace(s)) {
							if (race == null ? true : race.canRace(s))
								race = s;// 找优先级最低的
						}
					}
				}
			}

			IPanelLog.d(TAG, "raceSelector race = " + race);
			if (race != null) {
				race.loosen(true);
				ret = selectorHandle.reserve(selectTag) != -1;
			}
			return ret;
		}

		private boolean doReserve() {
			IPanelLog.d(TAG, "#############doReserve");
			int respect = 0, result = 0;
			boolean rebuilded = false;
			if (playerHandle != null) {// player放前面
				respect++;
				switch (playerHandle.reserve(playerTag)) {
				case 0:
					result++;
					break;
				case 1:
					result++;
					rebuilded = true;
					break;
				case -1:
					if (racePlayer()) {
						result++;
						rebuilded = true;
					}
					break;
				}
			}
			if (selectorHandle != null) {
				respect++;
				switch (selectorHandle.reserve(selectTag)) {
				case 0:
					result++;
					break;
				case 1:
					result++;
					rebuilded = true;
					break;
				case -1:
					if (raceSelector()) {
						result++;
						rebuilded = true;
					}
					break;
				}
			}
			IPanelLog.d(TAG, "respect = " + respect + " result = " + result);
			if ((reserved = (respect == result))) {
				IPanelLog.d(TAG, "reserved()  rebuilded = " + rebuilded + ",stateCleared = "
						+ stateCleared + ";listend=" + listend);
				if (rebuilded || stateCleared) {
					repair();
				} else {
					ensureListen();
				}
			}
			return reserved;
		}

		@Override
		public boolean reserve() {
			synchronized (stats) {
				synchronized (stateMutex) {
					boolean ret = false;
					try {
						IPanelLog.d(TAG, "reserved = " + reserved);
						if (!(ret = reserved))
							ret = doReserve();
					} catch (Exception e) {
						IPanelLog.e(TAG, "reserve e = " + e.toString());
					} finally {
						if (!ret) {
							if (selectorHandle != null)
								selectorHandle.release();
							if (playerHandle != null)
								playerHandle.release();
						}
					}
					return ret;
				}
			}
		}

		public void setNetworkUUID(String UUID) {
			synchronized (stateMutex) {
				synchronized (stateMutex) {
					if (reserved && selectorHandle != null) {
						selectorHandle.get().setNetworkUUID(UUID);
					}
				}

			}
		}

		private boolean repair() {
			stateCleared = false;
			if (selectorHandle != null) {
				selectorHandle.setListenerEnable(true);
				selectorHandle.get().setNetworkUUID(getUUID());
			}
			if (playerHandle != null) {
				if (selectorHandle == null)
					throw new RuntimeException("神马State有 TeeveePlayer 而没有 StreamSelector ?");
				IPanelLog.d(TAG, "repair playerHandle.get().getDataSource() = "
						+ playerHandle.get().getDataSource() + ";selectorHandle.get() = "
						+ selectorHandle.get());
				if (playerHandle.get().getDataSource() != selectorHandle.get()) {
					if (playerHandle.get().getDataSource() != null) {
						IPanelLog.d(TAG, "repair null");
						playerHandle.get().stop();
						playerHandle.get().setDataSource(null);
					}
					IPanelLog.d(TAG,
							"repair setDAtaSource playerHandle.get() " + playerHandle.get());
					playerHandle.get().stop();
					playerHandle.get().setDataSource(selectorHandle.get());
				}
				playerHandle.setListenerEnable(true);
			}
			return true;
		}

		private void clearState() {
			if (selectorHandle != null)
				selectorHandle.setListenerEnable(false);
			if (playerHandle != null) {
				IPanelLog.d(TAG, "clearState 1");
				playerHandle.get().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
				playerHandle.setListenerEnable(false);
				playerHandle.get().stop();
				playerHandle.get().setDataSource(null);
				IPanelLog.d(TAG, "clearState black 1");
				playerHandle.get().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			}
			stateCleared = true;
		}

		@Override
		public void loosen(boolean clearState) {
			IPanelLog.d(TAG, "loosen clearState = " + clearState + ";reserved = " + reserved
					+ ";this = " + this);
			synchronized (stats) {
				synchronized (stateMutex) {
					if (reserved) {
						reserved = false;
						if (clearState)
							clearState();
						if (selectorHandle != null)
							selectorHandle.release();
						if (playerHandle != null)
							playerHandle.release();
						getApp().setTunerInfo(null);;
					}
				}
			}
		}

		public void destroy() {
			IPanelLog.d(TAG, "destroy");
			synchronized (stats) {
				synchronized (stateMutex) {
					reserved = false;
					clearState();
					if (selectorHandle != null)
						selectorHandle.destroy();
					if (playerHandle != null)
						playerHandle.destroy();
				}
			}
		}

		@Override
		public boolean isReserved() {
			return reserved;
		}

		public StreamSelectorHandle getSelector() {
			return selectorHandle;
		}

		public TeeveePlayerHandle getPlayer() {
			return playerHandle;
		}

		StreamSelectorHandle createStreamSelectorHandle(boolean pushOnly, int ss) {
			return new StreamSelectorHandle(pushOnly ? -1 : getApp().deliveryType, ss);
		}

		TeeveePlayerHandle createTeeveePlayerHandle(int pipSize, int flags) {
			return new TeeveePlayerHandle(pipSize, flags);
		}

		public class StreamSelectorHandle extends ResourceHandle<StreamSelector> {
			SelectionStateListener ssl;

			StreamSelectorHandle(int dt, int ss) {
				super(selectorBundle);
				selectTag = new SelectorTag(dt, ss);
			}

			void setListenerEnable(boolean b) {
				IPanelLog.d(TAG, "setListenerEnable go in b=" + b);
				StreamSelectorResource rw = (StreamSelectorResource) getW();
				rw.setSelectionStateListener(b ? ssl : null, getApp());
			}

			public void setSelectionStateListener(SelectionStateListener l) {
				ssl = l;
				IPanelLog.d(TAG, "setSelectionStateListener go in ssl=" + ssl);

				try {
					StreamSelectorResource rw = (StreamSelectorResource) getW2();
					if (rw != null) {
						rw.setSelectionStateListener(ssl, getApp());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public boolean select(FrequencyInfo fi, int flags) {
				IPanelLog.d(TAG, "select in");
				synchronized (stateMutex) {
					IPanelLog.d(TAG, "select 11");
					if (!isReserved())
						return false;
					IPanelLog.d(TAG, "select 222");
					return ((StreamSelectorResource) getW()).select(fi, flags);
				}
			}

			public boolean select(FileDescriptor fd, int flags) {
				synchronized (stateMutex) {
					if (!isReserved())
						return false;
					return ((StreamSelectorResource) getW()).select(fd, flags);
				}
			}

			public boolean receive(int pid, FileDescriptor fd, int flags) {
				synchronized (stateMutex) {
					if (!isReserved())
						return false;
					return ((StreamSelectorResource) getW()).receive(pid, fd, flags);
				}
			}

			public SignalStatus getSignalStatus() {
				synchronized (stateMutex) {
					if (!isReserved())
						return null;
					return get().getSignalStatus();
				}
			}

			public boolean clear(){
				synchronized (stateMutex) {
					if (!isReserved())
						return false;
					return get().clear();
				}
			}
			
			public void setVirtualFrequency(long freq) {
				synchronized (stateMutex) {
					if (!isReserved())
						return;
					get().setVirtualFrequency(freq);
				}
			}
		}

		public class TeeveePlayerHandle extends ResourceHandle<TeeveePlayer> {
			PlayStateListener psl = null;
			ProgramStateListener gsl = null;
			PlayerProcessPTSListener ptsl;

			TeeveePlayerHandle(int pipSize, int flags) {
				super(playerBundle);
				playerTag = new PlayerTag(pipSize, flags);
			}

			void setListenerEnable(boolean b) {
				IPanelLog.i(TAG, "setListenerEnable b =" + b);
				TeeveePlayerResource rw = (TeeveePlayerResource) getW();
				rw.setTeeveePlayerListener(b ? psl : null, b ? gsl : null, b ? ptsl : null);
			}

			public void setListener(PlayStateListener l1, ProgramStateListener l2,
					PlayerProcessPTSListener l3) {
				IPanelLog.i(TAG, "setListener 111111 l1 = " + l1 + " l2" + l2);
				psl = l1;
				gsl = l2;
				ptsl = l3;
			}

			public void setListener(PlayStateListener l1, ProgramStateListener l2) {
				setListener(l1, l2, null);
			}

			public boolean selectProgram(ProgramInfo p, int flags) {
				IPanelLog.i(TAG, "selectProgram 111111");
				synchronized (stateMutex) {
					IPanelLog.i(TAG, "selectProgram 222222");
					if (!isReserved())
						return false;
					IPanelLog.i(TAG, "selectProgram 333333");
					boolean b = ((TeeveePlayerResource) getW()).selectProgram(p, flags);
					IPanelLog.i(TAG, "selectProgram b = " + b);
					return b;
				}
			}

			public boolean start() {
				synchronized (stateMutex) {
					if (!isReserved())
						return false;
					return get().start();
				}
			}

			public void stop() {
				synchronized (stateMutex) {
					if (!isReserved())
						return;
					get().stop();
				}
			}

			public boolean pause() {
				synchronized (stateMutex) {
					if (!isReserved())
						return false;
					return get().pause();
				}
			}

			public void resume() {
				synchronized (stateMutex) {
					if (!isReserved())
						return;
					get().resume();
				}
			}
			
			public void clearCache(){
				synchronized (stateMutex) {
					if (!isReserved())
						return;
					get().clear();
				}
			}

			public boolean setFreeze(boolean b, int flags) {
				synchronized (stateMutex) {
					if (!isReserved())
						return false;
					return get().setFreeze(b, flags);
				}
			}

			public long getPlayTime() {
				synchronized (stateMutex) {
					if (!isReserved())
						return 0;
					return get().getPlayTime();
				}
			}

			// public long getPlayProcessPtsTime() {
			// if (!isReserved())
			// return 0;
			// long x;
			// x = get().getPlayProcessPtsTime();
			// return x;
			// }

			public boolean setDisplay(int x, int y, int width, int height) {
				synchronized (stateMutex) {
					if (!isReserved())
						return false;
					return get().setDisplay(x, y, width, height);
				}
			}

			public boolean setVolume(float v) {
				IPanelLog.d(TAG, "setVolume in v = " + v);
				synchronized (stateMutex) {
					IPanelLog.d(TAG, "setVolume 11 ");
					if (!isReserved())
						return false;
					IPanelLog.d(TAG, "setVolume 22 ");
					return get().setVolume(v, v);
				}
			}

			public boolean loadAnimation(FileDescriptor fd) {
				synchronized (stateMutex) {
					if (!isReserved())
						return false;
					return get().loadAnimation(fd);
				}
			}

			public void actAnimation(int id, int p1, int p2, int flags) {
				synchronized (stateMutex) {
					if (isReserved())
						get().actAnimation(id, p1, p2, flags);
				}
			}

			public boolean captureVideoFrame(int id) {
				synchronized (stateMutex) {
					if (!isReserved()) {
						return get().captureVideoFrame(id);
					}
					return false;
				}
			}
		}

	}

	// ===================================================================
	// ======================== ResourceWarper ===========================
	// ===================================================================
	abstract static class ResourceWarper<T> {
		private T obj;
		private Object tag;
		private volatile int startCount = 0, msgStartCount = 0;// 版本只能在此处记录
		private boolean listenPending = true, bad = false;

		public ResourceWarper(T o, Object tag) {
			obj = o;
			this.tag = tag;
			IPanelLog.d(TAG, "ResourceWarper this = " + this + "; obj = " + obj);
		}

		final T get() {
			IPanelLog.d(TAG, "get this = " + this + ";obj = " + obj);
			return obj;
		}

		final Object getTag() {
			return tag;
		}

		final int incStartCount() {
			return ++startCount;
		}

		final void incMsgStartCount() {
			msgStartCount++;
		}

		final boolean msgMatchStartCount() {// invoked in callback
			IPanelLog.i(TAG, "msgStartCount=" + msgStartCount + ":startCount=" + startCount
					+ ";listenPending=" + listenPending);
			return msgStartCount == startCount && !listenPending;
		}

		final void pendingListen(boolean b) {
			listenPending = b;
		}

		final void release() {
			IPanelLog.d(TAG, "release this = " + this + ";obj = " + obj);
			doRelease(get());
			tag = null;
			obj = null;
		}

		final boolean setWeak(boolean b) {
			IPanelLog.d(TAG, "setWeak b = " + b + ";this = " + this);
			if (b) {
				if (isWeak(get()))
					return true;
				return setWeak(get(), b);
				// IPanelLog.d(TAG, "setWeak weak = " + bad);
				// return bad;
				// return setWeak(get(), b);
			} else {
				IPanelLog.d(TAG, "setWeak 111");
				T t = get();
				if (t == null) {
					bad = true;
					return false;
				}
				if (!isWeak(get()))
					return true;
				IPanelLog.d(TAG, "setWeak 222");
				// return (bad = setWeak(get(), b));
				bad = !setWeak(get(), b);
				IPanelLog.d(TAG, "this = " + this + ";bad = " + bad);
				return !bad;
			}
		}

		final boolean isAvailable() {// weaken or released
			IPanelLog.d(TAG, "isAvailable 2 this = " + this + ";bad = " + bad);
			T t = get();
			if (t == null) {
				return false;
			}
			return !bad && !isReleased(t);
		}

		abstract boolean setWeak(T t, boolean b);

		abstract boolean isWeak(T t);

		abstract boolean isReleased(T t);

		abstract void doRelease(T t);
	}

	static final class StreamSelectorResource extends ResourceWarper<StreamSelector> implements
			SelectionStateListener {
		boolean bad = false;
		SelectionStateListener lis;
		LiveNetworkApplication<?, ?, ?, ?, ?> app;
		FrequencyInfo fi;

		StreamSelectorResource(StreamSelector ss, SelectorTag tag) {
			super(ss, tag);
			ss.setSelectionStateListener(this);
		}

		synchronized void setSelectionStateListener(SelectionStateListener l,
				LiveNetworkApplication<?, ?, ?, ?, ?> app) {
			pendingListen(true);
			lis = l;
			this.app = app;
		}

		synchronized boolean select(FrequencyInfo fi, int flags) {
			if (get().select(fi, flags)) {
				incStartCount();
				pendingListen(false);
				this.fi = fi;
				return true;
			}
			return false;
		}

		synchronized boolean select(FileDescriptor fd, int flags) {
			if (get().select(fd, flags)) {
				incStartCount();
				pendingListen(false);
				return true;
			}
			return false;
		}

		synchronized boolean receive(int pid, FileDescriptor fd, int flags) {
			if (get().receive(pid, fd, flags)) {
				incStartCount();
				pendingListen(false);
				return true;
			}
			return false;
		}

		@Override
		public synchronized void onSelectStart(StreamSelector selector) {
			IPanelLog.i(TAG, "_----------onSelectStart ------");
			incMsgStartCount();
			SelectionStateListener l = lis;
			if (l != null)
				l.onSelectStart(selector);
		}

		@Override
		public synchronized void onSelectSuccess(StreamSelector selector) {
			IPanelLog.i(TAG, "onSelectSuccess---");
			SelectionStateListener l = lis;
			if (msgMatchStartCount() && l != null) {
				l.onSelectSuccess(selector);
			}
			setTunerInfo(selector, 0);
		}

		@Override
		public synchronized void onSelectFailed(StreamSelector selector) {
			IPanelLog.i(TAG, "onSelectFailed---");
			pendingListen(false);
			SelectionStateListener l = lis;
			if (msgMatchStartCount() && l != null) {
				l.onSelectFailed(selector);
			}
			setTunerInfo(selector, -1);
		}

		@Override
		public synchronized void onSelectionLost(StreamSelector selector) {
			IPanelLog.i(TAG, "onSelectionLost---");
			pendingListen(false);
			SelectionStateListener l = lis;
			if (msgMatchStartCount() && l != null) {
				l.onSelectionLost(selector);
			}
			setTunerInfo(selector, -2);
		}

		@Override
		public synchronized void onSelectionResumed(StreamSelector selector) {
			IPanelLog.i(TAG, "onSelectionResumed---");
			SelectionStateListener l = lis;
			if (msgMatchStartCount() && l != null) {
				l.onSelectionResumed(selector);
			}
			setTunerInfo(selector, 0);
		}

		@Override
		boolean setWeak(StreamSelector t, boolean b) {
			return t.setWeakMode(b);
		}

		@Override
		boolean isWeak(StreamSelector t) {
			return t.isWeakMode();
		}

		@Override
		boolean isReleased(StreamSelector t) {
			return t.isReleased();
		}

		@Override
		void doRelease(StreamSelector t) {
			t.release();
		}

		void setTunerInfo(StreamSelector selector, int status) {
			TunerInfo info = app.getTunerInfo();
			if (info == null) {
				info = new TunerInfo();
			}
			info.freq = selector.getCurrentFrequency();
			if (fi != null) {
				info.modulation = fi.getParameter(FrequencyInfo.MODULATION);
				info.symbol_rate = fi.getParameter(FrequencyInfo.SYMBOL_RATE);
			}
			info.ss = selector.getSignalStatus();
			info.status = status;
			app.setTunerInfo(info);
		}
	}

	static final class TeeveePlayerResource extends ResourceWarper<TeeveePlayer> implements
			PlayStateListener, ProgramStateListener {
		private PlayStateListener lis1;
		private ProgramStateListener lis2;
		private PlayerProcessPTSListener lis3;

		TeeveePlayerResource(TeeveePlayer tp, PlayerTag tag) {
			super(tp, tag);
			tp.setPlayStateListener(this);
			tp.setProgramStateListener(this);
		}

		synchronized void setTeeveePlayerListener(PlayStateListener l1, ProgramStateListener l2,
				PlayerProcessPTSListener l3) {
			pendingListen(true);
			IPanelLog.d(TAG, "setTeeveePlayerListener l1 = " + l1 + " l2 =" + l2);
			lis1 = l1;
			lis2 = l2;
			lis3 = l3;
		}

		synchronized boolean selectProgram(ProgramInfo p, int flags) {
			IPanelLog.i(TAG, "selectProgram 111111111");
			if (get().selectProgram(p, flags)) {
				incStartCount();
				pendingListen(false);
				IPanelLog.d(TAG, "slectProgram inc=" + msgMatchStartCount()
						+ ";p.getProgramNumber() = " + p.getProgramNumber());
				return true;
			}
			return false;
		}

		@Override
		public synchronized void onProgramReselect(int program_number, String newuri) {
			IPanelLog.d(TAG, "onProgramReselect--go in lis2=" + lis2);
			ProgramStateListener l = lis2;
			if (/* msgMatchStartCount() && */l != null) {
				IPanelLog.d(TAG, "onProgramReselect");
				l.onProgramReselect(program_number, newuri);
			}
			IPanelLog.d(TAG, "onProgramReselect--go out lis2=" + lis2);
		}

		@Override
		public synchronized void onProgramDiscontinued(int program_number) {
			IPanelLog.d(TAG, "onProgramDiscontinued--go in lis2=" + lis2);
			ProgramStateListener l = lis2;
			if (msgMatchStartCount() && l != null) {
				l.onProgramDiscontinued(program_number);
			}
			IPanelLog.d(TAG, "onProgramDiscontinued--go out lis2=" + lis2);
		}

		@Override
		public synchronized void onSelectionStart(TeeveePlayer player, int program_number) {
			IPanelLog.d(TAG, "onSelectionStart in program_number=" + program_number);
			incMsgStartCount();
			PlayStateListener l = lis1;
			if (msgMatchStartCount() && l != null) {
				l.onSelectionStart(player, program_number);
			}
			IPanelLog.d(TAG, "onSelectionStart out program_number=" + program_number);
		}

		@Override
		public synchronized void onPlayProcessing(int program_number) {
			IPanelLog.d(TAG, "onPlayProcessing--go in lis3=" + lis3);
			PlayerProcessPTSListener ptsl = lis3;
			if (/* msgMatchStartCount()&& */ptsl != null) {
				ptsl.onPlayerPTSChange(program_number, get().getPlayProcessPtsTime(), 1);
			}
			IPanelLog.d(TAG, "onPlayProcessing--go out lis2=" + lis2);
		}

		@Override
		public synchronized void onPlaySuspending(int program_number) {
			IPanelLog.d(TAG, "onPlaySuspending--go in lis2=" + lis2);
			if (/* msgMatchStartCount() && */lis3 != null) {
				lis3.onPlaySuspend(program_number);
			}

			IPanelLog.d(TAG, "onPlaySuspending--go out lis2=" + lis2);
		}

		@Override
		public synchronized void onPlayError(int program_number, String msg) {
			IPanelLog.d(TAG, "onPlayError--go in lis2=" + lis2);
			PlayStateListener l = lis1;
			if (msgMatchStartCount() && l != null) {
				l.onPlayError(program_number, msg);
			}
			IPanelLog.d(TAG, "onPlayError--go out lis2=" + lis2);
		}

		@Override
		boolean setWeak(TeeveePlayer t, boolean b) {
			return t.setWeakMode(b);
		}

		@Override
		boolean isWeak(TeeveePlayer t) {
			return t.isWeakMode();
		}

		@Override
		boolean isReleased(TeeveePlayer t) {
			return !t.isPrepared();
		}

		@Override
		void doRelease(TeeveePlayer t) {
			t.release();
		}

	}

	public interface PlayerProcessPTSListener {
		public void onPlayerPTSChange(int program_number, long process_pts_time, int state);

		public void onPlayProcess(int program_number, long process_pts_time);

		public void onPlaySuspend(int program_number);
	}

}