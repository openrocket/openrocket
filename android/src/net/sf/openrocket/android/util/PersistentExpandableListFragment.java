package net.sf.openrocket.android.util;

/*
 * TODO - this isn't working.
 */
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class PersistentExpandableListFragment extends ExpandableListFragment {
	private long[] expandedIds;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		AndroidLogWrapper.d(PersistentExpandableListFragment.class, "onCreate");
		super.onCreate(savedInstanceState);
		if ( savedInstanceState != null ) {
			expandedIds = savedInstanceState.getLongArray("ExpandedIds");
		}
	}

	@Override
	public void onStop() {
		AndroidLogWrapper.d(PersistentExpandableListFragment.class, "onStop");
		super.onStop();
		expandedIds = getExpandedIds();
	}

	@Override
    public void onStart() {
		AndroidLogWrapper.d(PersistentExpandableListFragment.class, "onStart");
        super.onStart();
        if (this.expandedIds != null) {
            restoreExpandedState(expandedIds);
        }
    }

	@Override
	public void onPause() {
		AndroidLogWrapper.d(PersistentExpandableListFragment.class, "onPause");
		super.onPause();
		expandedIds = getExpandedIds();
	}

	
	@Override
	public void onResume() {
		AndroidLogWrapper.d(PersistentExpandableListFragment.class, "onResume");
		super.onResume();
		if (this.expandedIds != null) {
			restoreExpandedState(expandedIds);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		this.expandedIds = getExpandedIds();
		outState.putLongArray("ExpandedIds", this.expandedIds);
	}

	private long[] getExpandedIds() {
		ExpandableListView list = getExpandableListView();
		ExpandableListAdapter adapter = getExpandableListAdapter();
		if (adapter != null) {
			int length = adapter.getGroupCount();
			ArrayList<Long> expandedIds = new ArrayList<Long>();
			for(int i=0; i < length; i++) {
				if(list.isGroupExpanded(i)) {
					expandedIds.add(adapter.getGroupId(i));
				}
			}
			return toLongArray(expandedIds);
		} else {
			return null;
		}
	}

	private void restoreExpandedState(long[] expandedIds) {
		this.expandedIds = expandedIds;
		if (expandedIds != null) {
			ExpandableListView list = getExpandableListView();
			ExpandableListAdapter adapter = getExpandableListAdapter();
			if (adapter != null) {
				for (int i=0; i<adapter.getGroupCount(); i++) {
					long id = adapter.getGroupId(i);
					if (inArray(expandedIds, id)) list.expandGroup(i);
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

	private static long[] toLongArray(List<Long> list)  {
		long[] ret = new long[list.size()];
		int i = 0;
		for (Long e : list)  
			ret[i++] = e.longValue();
		return ret;
	}
}
