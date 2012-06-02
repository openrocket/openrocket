package net.sf.openrocket.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class RocketChangedEventHandler extends Handler {

	public RocketChangedEventHandler() {
	}

	public RocketChangedEventHandler(Callback callback) {
		super(callback);
	}

	public RocketChangedEventHandler(Looper looper) {
		super(looper);
	}

	public RocketChangedEventHandler(Looper looper, Callback callback) {
		super(looper, callback);
	}

	public static final int MOTOR_CONFIGS_CHANGED = 1;
	public static final int SIMS_CHANGED = 2;

	public void simsChangedMessage() {
		Message m = this.obtainMessage(SIMS_CHANGED);
		this.dispatchMessage(m);
	}
	
	public void configsChangedMessage() {
		Message m = this.obtainMessage(MOTOR_CONFIGS_CHANGED);
		this.dispatchMessage(m);
	}

	@Override
	public void handleMessage(Message msg) {
		int what = msg.what;
		switch ( what ) {
		case SIMS_CHANGED:
			doSimsChanged();
			break;
		case MOTOR_CONFIGS_CHANGED:
			doMotorConfigsChanged();
			break;
		default:
			super.handleMessage(msg);
		}
	}

	protected abstract void doSimsChanged();
	
	protected abstract void doMotorConfigsChanged();

}
