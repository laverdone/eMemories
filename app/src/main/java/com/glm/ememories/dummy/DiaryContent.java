package com.glm.ememories.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DiaryContent {

	/**
	 * An array of sample (dummy) items.
	 */
	public static List<DiaryItem> ITEMS = new ArrayList<DiaryItem>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, DiaryItem> ITEM_MAP = new HashMap<String, DiaryItem>();

	public static void addItem(DiaryItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	
	public class DiaryItem {
		public String id;
		public String content;

		public DiaryItem(String id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString() {
			return content;
		}
	}
}
