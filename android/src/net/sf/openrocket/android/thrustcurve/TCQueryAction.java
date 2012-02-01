package net.sf.openrocket.android.thrustcurve;

import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.motor.ExtendedThrustCurveMotor;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;

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
public class TCQueryAction {

	public interface OnComplete {
		public void onComplete();
	}

	private DbAdapter mDbHelper;

	private ProgressDialog progress;
	private Thread downloadThread;
	private Handler handler;

	private final Activity parent;
	private OnComplete onCompleteListener;

	/**
	 * Create a new TCQueryAction.
	 * 
	 * If the parent implements TCQueryAction.OnComplete, it will be used as the
	 * onCompleteListener and notified when the process is finished.
	 * 
	 * @param parent
	 */
	public TCQueryAction( Activity parent ) {
		this.parent = parent;

		mDbHelper = new DbAdapter(this.parent);
		mDbHelper.open();

		if (parent instanceof OnComplete ) {
			this.onCompleteListener = (OnComplete) parent;
		}
	}

	public void setOnCompleteListener(OnComplete onCompleteListener) {
		this.onCompleteListener = onCompleteListener;
	}
	
	public void start( SearchRequest request) {
		Downloader d = new Downloader(request);

		handler = new Handler();
		progress = ProgressDialog.show(parent, null, "");

		downloadThread = new Thread( d );
		downloadThread.start();

	}

	public void dismiss() {
		// TODO - need to kill the thread.
		
		mDbHelper.close();
		
		if ( progress != null && progress.isShowing() ) {
			progress.dismiss();
		}
	}
	
	private class UpdateMessage implements Runnable {
		private String newMessage;
		UpdateMessage( String message ) {
			this.newMessage = message;
		}
		@Override
		public void run() {
			progress.setMessage(newMessage);
		}
	}

	private class Dismiss implements Runnable {
		@Override
		public void run() {
			progress.dismiss();
			if (onCompleteListener != null ) {
				onCompleteListener.onComplete();
			}
			//			TCQueryActivity.this.finish();
		}
	}

	private class Error implements Runnable {
		private String newMessage;
		Error( String message ) {
			this.newMessage = message;
		}
		@Override
		public void run() {
			progress.dismiss();
			final AlertDialog dialog = new AlertDialog.Builder(parent).create();
			dialog.setMessage(newMessage);
			dialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Dismiss", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					dialog.dismiss();
				}

			});
			dialog.show();
		}
	}

	private class Downloader implements Runnable {

		SearchRequest request;

		Downloader( SearchRequest request ) {
			this.request = request;
		}

		@Override
		public void run() {
			try {
				handler.post( new UpdateMessage("Quering Thrustcurve"));
				SearchResponse res = new ThrustCurveAPI().doSearch(request);

				int total = res.getResults().size();
				int count = 1;
				for( TCMotor mi : res.getResults() ) {
					handler.post(new UpdateMessage("Downloading details " + count + " of " + total));
					count++;
					if ( mi.getData_files() == null || mi.getData_files().intValue() == 0 ) {
						continue;
					}

					MotorBurnFile b = new ThrustCurveAPI().downloadData(mi.getMotor_id());

					AndroidLogWrapper.d(TCQueryAction.class, mi.toString());

					ExtendedThrustCurveMotor m = new ExtendedThrustCurveMotor();

					m.setThrustCurveMotor( b.getThrustCurveMotor() );

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
				}
				if ( total < res.getMatches() ) {
					handler.post( new Error( total + " motors downloaded, " + res.getMatches() + " matched.  Try restricting the query more.") );
				} else {
					handler.post( new Dismiss());
				}
			}
			catch( Exception ex){
				AndroidLogWrapper.d(TCQueryAction.class,ex.toString());
				handler.post( new Error(ex.toString()) );
			}

		}
	}

}
