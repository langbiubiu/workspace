package ipaneltv.toolkit.mediaservice;

/**
 * �ɷ����Service����ʵ���ṩ����
 */
public abstract class MediaSourceSessionService extends MediaPlaySessionService {
	public static final String TAG = MediaSourceSessionService.class.getSimpleName();
	public abstract String getAreaCode();

}
