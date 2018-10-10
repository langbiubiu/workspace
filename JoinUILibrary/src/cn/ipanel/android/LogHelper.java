/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * package-level logging flag
 */

package cn.ipanel.android;

public class LogHelper {
	public final static String DEFAULT_LOGTAG = "iPanel";
	public final static String DEFAULT_SPLIT = " -> ";

	public static String LOGTAG = DEFAULT_LOGTAG;
	public static boolean DEBUG = true;
	
	public static void setLogTag(String tag){
		LOGTAG=tag;
	}

	public static void v(String logMe) {
		if (DEBUG)
			android.util.Log.v(LOGTAG, logMe+"");
	}
	
	public static void d(String logMe) {
		if (DEBUG)
			android.util.Log.d(LOGTAG, logMe+"");
	}
	
	public static void i(String logMe) {
		if (DEBUG)
			android.util.Log.i(LOGTAG, logMe+"");
	}
	
	public static void w(String logMe) {
		if (DEBUG)
			android.util.Log.w(LOGTAG, logMe+"");
	}

	public static void e(String logMe) {
		if (DEBUG)
			android.util.Log.e(LOGTAG, logMe+"");
	}

	public static void e(String logMe, Exception ex) {
		if (DEBUG)
			android.util.Log.e(LOGTAG, logMe+"", ex);
	}

	public static void v(String tag, String logMe) {
		if (DEBUG)
			android.util.Log.v(LOGTAG + DEFAULT_SPLIT + tag, logMe);
	}

	public static void d(String tag, String logMe) {
		if (DEBUG)
			android.util.Log.d(LOGTAG + DEFAULT_SPLIT+ tag, logMe);
	}

	public static void i(String tag, String logMe) {
		if (DEBUG)
			android.util.Log.i(LOGTAG + DEFAULT_SPLIT + tag, logMe);
	}
	
	public static void w(String tag, String logMe) {
		if (DEBUG)
			android.util.Log.w(LOGTAG + DEFAULT_SPLIT + tag, logMe);
	}

	public static void e(String tag, String logMe) {
		if (DEBUG)
			android.util.Log.e(LOGTAG + DEFAULT_SPLIT + tag, logMe);
	}
}
