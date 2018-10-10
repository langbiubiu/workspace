package ipaneltv.toolkit.camodule;

public class CaModuleSession {

	final int id;
	final int pri;
	private final String name;
	volatile boolean attached = false;
	private CaNativeModule module;
	final Callback callback;

	CaModuleSession(CaNativeModule module, int id, String name, int pri, Callback callback) {
		this.id = id;
		this.name = name;
		this.pri = pri < 1 ? 1 : pri > 10 ? 10 : pri;
		this.module = module;
		this.callback = callback;
	}

	public final int getId() {
		return id;
	}

	public final int getPriority() {
		return pri;
	}

	public final String getName() {
		return name;
	}

	public final CaNativeModule getCaModule() {
		return module;
	}

	public final boolean isAttached() {
		return attached;
	}

	public boolean attach() {
		return module.attachSession(this);
	}

	public void deatch() {
		module.detachSession(this);
	}

	public String transmit(String json) {
		return module.sessionTransmit(this, json);
	}

	public void checkEntitlements() {
		module.checkEntitlements(this);
	}

	public void close() {
		deatch();
		module = null;
	}

	public static interface Callback {
		void onModulePaused();

		void onModuleResumed();

		void onModuleClosed();

		void onSessionLost();

		void onJsonMessage(String json);
	}

}
