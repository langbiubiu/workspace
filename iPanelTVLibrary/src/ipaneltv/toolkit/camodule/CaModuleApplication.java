package ipaneltv.toolkit.camodule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Application;
import android.net.Uri;
import android.util.SparseArray;

public class CaModuleApplication extends Application {

	private String uuid;
	private String workDir, entUriString;
	private Uri entURi = null;
	private SparseArray<CaNativeModule> mods = new SparseArray<CaNativeModule>();
	private int snCounter = 13;

	public CaModuleApplication(String uuid, String entUriString) {
		this.uuid = UUID.fromString(uuid).toString();
		this.entUriString = entUriString;
	}

	public final String getNetworkUUID() {
		return uuid;
	}

	public String ensureWorkDir() {
		return ensureWorkDir(null);
	}

	public synchronized Uri getEntitlemenUri() {
		if (entURi == null) {
			entURi = Uri.parse(entUriString);
		}
		return entURi;
	}

	public synchronized String ensureWorkDir(String path) {
		if (workDir == null) {
			if (path != null && (path.charAt(0) == '/')) {
				workDir = path;
			} else {
				File f = new File(this.getApplicationContext().getFilesDir() + "/"
						+ (path == null ? "" : path));
				if (!f.exists())
					if (!f.mkdirs())
						throw new RuntimeException("make work dirs failed!!");
				workDir = f.getAbsolutePath();
			}
		}
		return workDir;
	}

	protected void addModule(CaNativeModule m) {
		if (m.moduleSn != -1)
			throw new RuntimeException("module can add only once!");
		synchronized (mods) {
			m.moduleSn = snCounter++;
			mods.put(m.moduleSn, m);
		}
	}

	public int moduleSize() {
		return mods.size();
	}

	public CaNativeModule getCaModule(String libname) {
		synchronized (mods) {
			int n = mods.size();
			for (int i = 0; i < n; i++) {
				CaNativeModule m = mods.valueAt(i);
				if (m.getLibraryName().equals(libname))
					return m;
			}
			return null;
		}
	}

	public CaNativeModule getCaModule(int sn) {
		synchronized (mods) {
			return mods.get(sn);
		}
	}

	public CaNativeModule getCaModuleByModuleId(int id) {
		synchronized (mods) {
			int n = mods.size();
			for (int i = 0; i < n; i++) {
				CaNativeModule m = mods.valueAt(i);
				if (m.getCaModuleId() == id)
					return m;
			}
			return null;
		}
	}

	public List<CaNativeModule> getCaModules() {
		synchronized (mods) {
			List<CaNativeModule> ret = new ArrayList<CaNativeModule>();
			int n = mods.size();
			for (int i = 0; i < n; i++) {
				ret.add(mods.valueAt(i));
			}
			return ret;
		}
	}

	public List<String> getCaModulesLibraryName() {
		synchronized (mods) {
			List<String> ret = new ArrayList<String>();
			int n = mods.size();
			for (int i = 0; i < n; i++) {
				CaNativeModule m = mods.valueAt(i);
				ret.add(m.getLibraryName());
			}
			return ret;
		}
	}

}
