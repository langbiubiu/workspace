package cn.ipanel.smackui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.jivesoftware.smack.packet.Message;

import android.util.Log;

/**
 * Simple in memory store
 * @author Zexu
 *
 */
public class MessageStore {
	public interface OnNewMessageListener {
		public void onNewMessage();
	}

	public static class MessageEntry {
		public final String participant;
		public OnNewMessageListener mListener;
		public final List<Message> messages = new ArrayList<Message>();

		public int missedMsgCount = 0;

		public MessageEntry(String user) {
			this.participant = user;
		}

		public synchronized void addMessage(Message msg) {
			Log.d("MessageStore", msg.getFrom() +": "+ msg.getBody());
			messages.add(msg);
			if (mListener != null) {
				mListener.onNewMessage();
				missedMsgCount = 0;
			} else {
				missedMsgCount++;
				// TODO a message is received but not displayed, should pose
				// some kind of notification
			}
		}

	}

	private HashMap<String, MessageEntry> hashMap = new HashMap<String, MessageStore.MessageEntry>();

	private static MessageStore sMessageStore = new MessageStore();

	public static MessageStore getStore() {
		return sMessageStore;
	}

	public synchronized MessageEntry getEntry(String participant) {
		String key = Utils.getUidInJID(participant);
		MessageEntry entry = hashMap.get(key);
		if (entry == null) {
			entry = new MessageEntry(participant);
			hashMap.put(key, entry);
		}
		return entry;
	}
}
