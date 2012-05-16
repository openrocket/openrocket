package net.sf.openrocket.android.thrustcurve;

import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.motor.ExtendedThrustCurveMotor;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.android.util.ProgressDialogFragment;
import net.sf.openrocket.motor.ThrustCurveMotor;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * TCQueryAction is a class which provides all the functionality required
 * to download motor data from thrustcurve.  It includes UI element for
 * progress and error reporting dialogs.
 * 
 * To use the class, instantiate a new instance of TCQueryAction passing in the
 * owning Activity.
 * 
 * The Activity should implement TCQueryAction.OnComplete or provide an implementation of
 * TCQueryAction.OnComplete to be notified when the download process is complete.
 * 
 * A search and download is started with TCQueryActivity.start( SearchRequest ).  The TCQueryActivity
 * produces and updates a progress dialog.  When the process is complete, the TCQueryActivity will notify
 * the registered TCQueryAction.OnComplete handler.
 * 
 * When the parent Activity is dismissed, it must call TCQueryAction.dismiss() to free resources.
 * 
 */
public abstract class TCQueryAction extends Fragment {

	private final static String PROGRESS_DIALOG_TAG = "progress_dialog";

	public interface OnTCQueryCompleteListener {
		public void onTCQueryComplete(String message);
	}

	protected AsyncTask<Void,Void,String> task;
	protected Handler handler;

	private OnTCQueryCompleteListener onCompleteListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		handler = new Handler();
		if ( savedInstanceState == null ) {
			// this means we are starting for the first time.
			task.execute((Void)null);
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return null;
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		Activity parent = getActivity();
		if ( parent instanceof OnTCQueryCompleteListener ) {
			onCompleteListener = (OnTCQueryCompleteListener) parent;
		}
	}

	/**
	 * The return value is a message string which may be displayed by the caller.
	 *
	 */
	protected abstract class TCQueryTask extends AsyncTask<Void,Void,String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			DialogFragment newFragment = ProgressDialogFragment.newInstance("", "");
			newFragment.show(getFragmentManager(), PROGRESS_DIALOG_TAG);
		}

		@Override
		protected void onPostExecute(String obj) {
			super.onPostExecute(obj);
			AndroidLogWrapper.d(TCQueryAction.class,"Finished loading " + TCQueryAction.this);
			dismiss();
			if (onCompleteListener != null ) {
				onCompleteListener.onTCQueryComplete(obj);
			}
		}
	}

	protected void writeMotor( TCMotor mi, ThrustCurveMotor thrustCurveMotor) throws Exception {

		DbAdapter mDbHelper = new DbAdapter(getActivity());
		mDbHelper.open();
		try {
			ExtendedThrustCurveMotor m = new ExtendedThrustCurveMotor(thrustCurveMotor);

			// Convert impulse class.  ThrustCurve puts mmx, 1/4a and 1/2a as A.
			m.setImpulseClass(mi.getImpulse_class());
			if ( "a".equalsIgnoreCase(mi.getImpulse_class())) {
				if( mi.getCommon_name().startsWith("1/2A") ) {
					m.setImpulseClass("1/2A");
				} else if (mi.getCommon_name().startsWith("1/4A") ) {
					m.setImpulseClass("1/4A");
				} else if (mi.getCommon_name().startsWith("Micro") ) {
					m.setImpulseClass("1/8A");
				}
			}

			// Convert Case Info.
			if ( mi.getCase_info() == null
					|| "single use".equalsIgnoreCase(mi.getCase_info())
					|| "single-use".equalsIgnoreCase(mi.getCase_info())) {
				m.setCaseInfo(mi.getType()+ " " + mi.getDiameter() + "x" + mi.getLength());
			} else {
				m.setCaseInfo(mi.getCase_info());
			}

			AndroidLogWrapper.d(TCQueryAction.class,"adding motor " + m.toString());
			// Write motor.
			mDbHelper.getMotorDao().insertOrUpdateMotor(m);
		} finally {
			mDbHelper.close();
		}
	}

	protected void dismiss() {
		AndroidLogWrapper.d(TCQueryAction.class,"dismiss the progress");
		ProgressDialogFragment progress = (ProgressDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_TAG);
		if ( progress != null ) {
			getActivity().getSupportFragmentManager().beginTransaction().remove(progress).commit();
		}
	}

	protected class UpdateMessage implements Runnable {
		private String newMessage;
		UpdateMessage( String message ) {
			this.newMessage = message;
		}
		@Override
		public void run() {
			ProgressDialogFragment progress = (ProgressDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_TAG);
			if ( progress != null )
				progress.setMessage(newMessage);
		}
	}

}
