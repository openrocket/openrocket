package net.sf.openrocket.android.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public abstract class ChangeEventBroadcastReceiver extends BroadcastReceiver {

	public void register( Context context ) {
		LocalBroadcastManager.getInstance(context).registerReceiver( this, 
				new IntentFilter(Events.MESSAGE_ACTION) );
	}
	
	public void unregister( Context context ) {
		LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		int type = intent.getIntExtra(Events.TYPE, -1);
		switch( type ) {
		case Events.CONFIGS_CHANGED:
			doMotorConfigsChanged();
			doSimsChanged();
			break;
		case Events.SIMS_CHANGED:
			doSimsChanged();
			break;
		case Events.SIM_COMPLETE:
			doSimComplete();
			break;
		}
	}

	protected abstract void doSimComplete();

	protected abstract void doSimsChanged();
	
	protected abstract void doMotorConfigsChanged();

}
