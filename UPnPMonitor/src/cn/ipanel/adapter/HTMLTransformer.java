package cn.ipanel.adapter;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.content.Context;
import android.database.Cursor;
import cn.ipanel.adapter.Adapters.CursorTransformation;

public class HTMLTransformer extends CursorTransformation {

	public HTMLTransformer(Context context) {
		super(context);
	}

	@Override
	public String transform(Cursor cursor, int columnIndex) {
		String html = cursor.getString(columnIndex);
		HtmlCleaner cleaner = new HtmlCleaner();
		
		TagNode root = cleaner.clean(html);
		TagNode img = root.findElementHavingAttribute("src", true);
		return img.getAttributeByName("src");
	}

}
