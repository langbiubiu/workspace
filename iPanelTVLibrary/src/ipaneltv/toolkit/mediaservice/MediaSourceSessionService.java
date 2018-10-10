package ipaneltv.toolkit.mediaservice;

/**
 * 由服务端Service进行实现提供功能
 */
public abstract class MediaSourceSessionService extends MediaPlaySessionService {
	public static final String TAG = MediaSourceSessionService.class.getSimpleName();
	public abstract String getAreaCode();

}
