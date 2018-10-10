package ipaneltv.toolkit.wardship2;

import android.provider.BaseColumns;

/**
 * ��Ŀ�տ��໤�����ṩ��
 */
public class ProgramWardshipDatebase {

	/**
	 * ��������
	 */
	public static interface WardshipType {
		/** ���ŵ�-��ֱ�ӹۿ� */
		public static final int TYPE_OPEN = 0;
		/** ������-����ʾ�������� */
		public static final int TYPE_LOCKED = 1;
		/** �������-��Ӧ���Բ��� */
		public static final int TYPE_REFUSED = 2;
	}

	protected interface ProgramWardshipColumns {
		/** �������� long [key] */
		public static final String FREQUENCY = "frequency";
		/** �������� int */
		public static final String PROGRAM_NUMBER = "program_number";
		/**��������int */
		public static final String CHANNEL_NUMBER = "channel_number";
		/** �������� String */
		public static final String CHANNEL_NAME = "channel_name";
		/** �������� int */
		public static final String WARDSHIP = "wardship";
	}

	/**
	 * ��Ȩ������Ϣ
	 */
	public static final class ProgramWardships implements BaseColumns, ProgramWardshipColumns {
		/** ������� */
		public static final String TABLE_NAME = "program_wardship";
	}
}
