package ipaneltv.toolkit.dvb;

import android.util.SparseArray;

public class DataCarouselDir {
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_FILE = 1;
	public static final int TYPE_DIR = 2;
	public static final int TYPE_STREAM = 3;

	int objectKey;
	SparseArray<DataCarouselNode> node = new SparseArray<DataCarouselDir.DataCarouselNode>();

	public DataCarouselDir(int objectKey) {
		// TODO Auto-generated constructor stub
		this.objectKey = objectKey;
	}

	public void addNode(String fileName, int type, int tag, int tid, int moduleId, int objectKey) {
		DataCarouselNode n = new DataCarouselNode(fileName, type, tag, tid, moduleId, objectKey);
		node.put(objectKey, n);

	}

	public DataCarouselNode getNodeByKey(int objectKey) {
		if (node != null)
			return node.get(objectKey);
		return null;
	}

	public int getNodeSize() {
		if (node != null)
			return node.size();
		return 0;
	}

	public class DataCarouselNode {
		String fileName;
		int type;
		int tag;//
		int tid;// transactionId
		int moduleId;
		int objectKey;

		DataCarouselNode() {

		}

		DataCarouselNode(String fileName, int type, int tag, int tid, int moduleId, int objectKey) {
			this.fileName = fileName;
			this.type = type;
			this.tag = tag;
			this.tid = tid;
			this.moduleId = moduleId;
			this.objectKey = objectKey;

		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getTag() {
			return tag;
		}

		public void setTag(int tag) {
			this.tag = tag;
		}

		public int getTid() {
			return tid;
		}

		public void setTid(int tid) {
			this.tid = tid;
		}

		public int getModuleId() {
			return moduleId;
		}

		public void setModuleId(int moduleId) {
			this.moduleId = moduleId;
		}

		public int getObjectKey() {
			return objectKey;
		}

		public void setObjectKey(int objectKey) {
			this.objectKey = objectKey;
		}
	}

}