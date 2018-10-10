package cn.ipanel.adapter;

import java.util.ArrayList;
import java.util.List;

public class AdapterDefinition {
	public String name;
	public String uri;
	public String selection;
	public String sortOrder;
	public String layout;

	public List<BindItem> bindItemList = new ArrayList<AdapterDefinition.BindItem>();
	public List<SelectItem> selectItemList = new ArrayList<AdapterDefinition.SelectItem>();

	public static class BindItem {
		public String from;
		public String to;
		public String as;

		public TransformItem transformItem;
		public List<MapItem> mapList = new ArrayList<AdapterDefinition.MapItem>();
	}

	public static class SelectItem {
		public String column;
	}

	public static class MapItem {
		public String fromValue;
		public String toValue;
	}

	public static class TransformItem {
		public String withExpression;
		public String withClass;
	}
}
