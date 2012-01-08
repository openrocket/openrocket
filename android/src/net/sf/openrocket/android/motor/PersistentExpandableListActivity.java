package net.sf.openrocket.android.motor;

import java.util.ArrayList;
import java.util.List;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class PersistentExpandableListActivity extends ExpandableListActivity {
    private long[] expandedIds;

    @Override
    protected void onStart() {
        super.onStart();
        if (this.expandedIds != null) {
            restoreExpandedState(expandedIds);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        expandedIds = getExpandedIds();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.expandedIds = getExpandedIds();
        outState.putLongArray("ExpandedIds", this.expandedIds);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        long[] expandedIds = state.getLongArray("ExpandedIds");
        if (expandedIds != null) {
            restoreExpandedState(expandedIds);
        }
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
