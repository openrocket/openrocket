package net.sf.openrocket.android.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class PersistentExpandableListView extends ExpandableListView {

	public PersistentExpandableListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public PersistentExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PersistentExpandableListView(Context context) {
		super(context);
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle b = new Bundle();
		long[] expandedIds = getExpandedIds();
		b.putLongArray("ExpandedIds", expandedIds);
		return b;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		Bundle b = (Bundle) state;
		long[] expandedIds = b.getLongArray("ExpandedIds");
		restoreExpandedState(expandedIds);
	}

	private long[] getExpandedIds() {
		ExpandableListAdapter adapter = getExpandableListAdapter();
		if (adapter != null) {
			int length = adapter.getGroupCount();
			ArrayList<Long> expandedIds = new ArrayList<Long>();
			for(int i=0; i < length; i++) {
				if(this.isGroupExpanded(i)) {
					expandedIds.add(adapter.getGroupId(i));
				}
			}
			return toLongArray(expandedIds);
		} else {
			return null;
		}
	}

	private static long[] toLongArray(List<Long> list)  {
		long[] ret = new long[list.size()];
		int i = 0;
		for (Long e : list)  
			ret[i++] = e.longValue();
		return ret;
	}

	private void restoreExpandedState(long[] expandedIds) {
		if (expandedIds != null) {
			ExpandableListAdapter adapter = getExpandableListAdapter();
			if (adapter != null) {
				for (int i=0; i<adapter.getGroupCount(); i++) {
					long id = adapter.getGroupId(i);
					if (inArray(expandedIds, id)) this.expandGroup(i);
				}
			}
		}
	}

	private static boolean inArray(long[] array, long element) {
		for (long l : array) {
			if (l == element) {
				return true;
			}
		}
		return false;
	}


}
