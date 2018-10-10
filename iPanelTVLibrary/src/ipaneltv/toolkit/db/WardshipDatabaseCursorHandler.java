package ipaneltv.toolkit.db;

import ipaneltv.uuids.db.ExtendDatabase;

import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.telecast.dvb.DvbNetworkDatabase.Services;
import android.os.Handler;

public class WardshipDatabaseCursorHandler extends CursorHandlerBase {
	protected int i_id = -1;
	protected int v_id = 0;
	protected int i_freq = -1;
	protected long v_freq = 0;
	protected int i_program = -1;
	protected int v_program = 0;
	protected int i_channnel_number = -1;
	protected int v_channnel_number = 0;
	protected int i_hide = -1;
	protected int v_hide = 0;
	protected int i_setvice_type = -1;
	protected int v_setvice_type = 0;
	protected int i_channel_name = -1;
	protected String v_channel_name = null;
	protected Vector<WardshipProgram> wardshipPrograms = new Vector<WardshipProgram>();

	public WardshipDatabaseCursorHandler(Context context, Uri uri, String projection[],
			String selection, String[] selectionArgs, String sortOrder, Handler handler) {
		super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
	}

	@Override
	public void onCursorNotFound() {
	}

	@Override
	public void onCursorStart(Cursor c) {
		wardshipPrograms.clear();
		i_id = c.getColumnIndex(Services._ID);
		i_freq = c.getColumnIndex(Services.FREQUENCY);
		i_program = c.getColumnIndex(Services.PROGRAM_NUMBER);
		i_channnel_number = c.getColumnIndex(Services.CHANNEL_NUMBER);
		i_channel_name = c.getColumnIndex(Services.CHANNEL_NAME);
		i_hide = c.getColumnIndex(ExtendDatabase.ExtendServices.HIDED);
		i_setvice_type = c.getColumnIndex(Services.SERVICE_TYPE);
	}

	@Override
	public void onRecordFound(Cursor c) {
		if (i_id >= 0)
			v_id = c.getInt(i_id);
		if (i_freq >= 0)
			v_freq = c.getLong(i_freq);
		if (i_program >= 0)
			v_program = c.getInt(i_program);
		if (i_channnel_number >= 0)
			v_channnel_number = c.getInt(i_channnel_number);
		if (i_channel_name >= 0)
			v_channel_name = c.getString(i_channel_name);
		if (i_hide >= 0)
			v_hide = c.getInt(i_hide);
		if (i_setvice_type >= 0)
			v_setvice_type = c.getInt(i_setvice_type);
	}
	
	@Override
	public void onCursorEnd(Cursor c) {
		
	}
	
	protected WardshipProgram appendRecord() {
		WardshipProgram program = createWardshipProgram();
		program.id = v_id;
		program.frequency = v_freq;
		program.program_number = v_program;
		program.channel_number = v_channnel_number;
		program.channel_name = v_channel_name;
		program.hide = v_hide;
		program.type = v_setvice_type;
		if (v_setvice_type != 2&&v_setvice_type != 192 && v_setvice_type != 12 && v_setvice_type != 0
				&& !"cdl".equals(v_channel_name)) {
			if((v_channnel_number >= 0) && (v_channnel_number < 10)){
				program.local_number = "00"+v_channnel_number;
			}else if((v_channnel_number >= 10) && (v_channnel_number < 100)){
				program.local_number = "0"+v_channnel_number;
			}else{
				program.local_number = v_channnel_number+"";
			}
			wardshipPrograms.add(program);
		}
		return program;
	}

	protected WardshipProgram createWardshipProgram() {
		return new WardshipProgram();
	}

	

	public Vector<WardshipProgram> getPrograms() {
		return wardshipPrograms;
	}
}
