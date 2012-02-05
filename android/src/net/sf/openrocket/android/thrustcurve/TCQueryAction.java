package net.sf.openrocket.android.thrustcurve;

import net.sf.openrocket.android.db.DbAdapter;
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
public abstract class TCQueryAction {

	public interface OnComplete {
		public void onComplete();
	}

	protected DbAdapter mDbHelper;

	private ProgressDialog progress;
	private Thread downloadThread;
	protected Handler handler;

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

	protected abstract Runnable getTask();
	
	public void start() {
		handler = new Handler();
		progress = ProgressDialog.show(parent, null, "");

		downloadThread = new Thread( getTask() );
		downloadThread.start();

	}


	public void dismiss() {
		// TODO - need to kill the thread.

		mDbHelper.close();

		if ( progress != null && progress.isShowing() ) {
			progress.dismiss();
		}
	}

	protected class UpdateMessage implements Runnable {
		private String newMessage;
		UpdateMessage( String message ) {
			this.newMessage = message;
		}
		@Override
		public void run() {
			progress.setMessage(newMessage);
		}
	}

	protected class Dismiss implements Runnable {
		@Override
		public void run() {
			progress.dismiss();
			if (onCompleteListener != null ) {
				onCompleteListener.onComplete();
			}
			//			TCQueryActivity.this.finish();
		}
	}

	protected class Error implements Runnable {
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

}
