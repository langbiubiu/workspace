package com.ipanel.join.protocol.test;

import java.io.ByteArrayOutputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import com.ipanel.join.protocol.a7.domain.GetChannels;

import android.content.Context;

public class TestA7 {
	public static void test(Context ctx) {
		Serializer serializer = new Persister(new Format(
				"<?xml version=\"1.0\" encoding= \"UTF-8\" ?>"));

		try {
			GetChannels gc = new GetChannels();
			gc.setClientId("123");
			gc.setDeviceId("abcd");
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			serializer.write(gc, bao);
			System.out.println(new String(bao.toByteArray()));
			GetChannels gc2 = serializer.read(GetChannels.class, new String(bao.toByteArray()));
			bao = new ByteArrayOutputStream();
			serializer.write(gc2, bao);
			System.out.println(new String(bao.toByteArray()));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
