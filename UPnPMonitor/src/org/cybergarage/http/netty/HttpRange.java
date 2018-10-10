package org.cybergarage.http.netty;

import java.util.ArrayList;
import java.util.List;

import cn.ipanel.dlna.Logger;

public class HttpRange {
	public static final String UNITE_BYTES = "bytes";
	private String byteUnite;
	private List<RangeIndex> ranges = new ArrayList<RangeIndex>();

	public static HttpRange parseRange(String str) {
		if (str == null || str.length() == 0)
			return null;
		Logger.d("parse range: "+str);
		HttpRange range = new HttpRange();
		int idx = str.indexOf('=');
		if (idx == -1)
			return null;
		range.byteUnite = str.substring(0, idx);
		if(!UNITE_BYTES.equals(range.byteUnite))
			return null;
		str = str.substring(idx + 1);

		String[] ris = str.split(",");
		for (String ri : ris) {
			idx = ri.indexOf('-');
			if (idx == -1)
				continue;
			RangeIndex rangeIndex = new RangeIndex();
			String start = null;
			String end = null;
			if (idx > 0)
				start = ri.substring(0, idx);
			if (idx < ri.length() - 1)
				end = ri.substring(idx + 1);
			try {
				if (start != null)
					rangeIndex.start = Long.parseLong(start);
				if (end != null)
					rangeIndex.end = Long.parseLong(end);
				range.ranges.add(rangeIndex);
			} catch (NumberFormatException e) {

			}
		}
		return range;
	}
	
	public long getFirstRangeStart(long length){
		if(ranges.size() >0)
			return ranges.get(0).getStart(length);
		return 0;
	}
	
	public long getTotalCount(long length){
		int total = 0;
		for(RangeIndex ri : ranges){
			total += (ri.getEnd(length) - ri.getStart(length));
		}
		Logger.d("total bytes:"+total);
		return total;
	}

	public String getByteUnite() {
		return byteUnite;
	}

	public List<RangeIndex> getRanges() {
		return ranges;
	}

	public static class RangeIndex {
		private long start = -1;
		private long end = -1;

		public long getStart(long length) {
			if (start == -1) {
				if (end != -1)
					return Math.max(0, length - end - 1);
				else
					return 0;
			}
			return start;
		}

		public long getEnd(long length) {
			if (end == -1) {
				return length - 1;
			}
			if (start == -1) {
				return length - 1;
			}
			return Math.min(length -1, end);
		}
		
		public String toContentRange(long length){
			StringBuilder sb = new StringBuilder();
			sb.append(UNITE_BYTES);
			sb.append(' ');
			sb.append(getStart(length));
			sb.append('-');
			sb.append(getEnd(length));
			sb.append('/');
			sb.append(length);
			Logger.d(sb.toString());
			return sb.toString();
		}

	}
}
