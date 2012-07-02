package net.sf.openrocket.android.rocket;

import net.sf.openrocket.R;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.android.util.ProgressDialogFragment;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class OpenRocketSaverFragment extends Fragment {
	
	public static OpenRocketSaverFragment newInstance( boolean showProgressDialog) {
		OpenRocketSaverFragment frag = new OpenRocketSaverFragment();
		Bundle b = new Bundle();
		b.putBoolean(SHOW_PRGRESS_DIALOG_ARG, showProgressDialog);
		frag.setArguments(b);
		return frag;
	}
	
	public interface OnOpenRocketFileSaved {
		public void onOpenRocketFileSaved( Boolean result );
	}

	private final static String SHOW_PRGRESS_DIALOG_ARG = "net.sf.openrocket.android.ShowProgressDialog";
	
	private boolean showProgressDialog = true;
	private OpenRocketSaverTask task;
	private OnOpenRocketFileSaved listener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		Bundle b = getArguments();
		if ( b != null ) {
			showProgressDialog = b.getBoolean(SHOW_PRGRESS_DIALOG_ARG, true);
		}
		if ( task == null ) {
			// since we retain instance state, task will be non-null if it is already loading.
			task = new OpenRocketSaverTask();
			task.execute();
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
		if ( parent instanceof OnOpenRocketFileSaved ) {
			listener = (OnOpenRocketFileSaved) parent;
		}
	}

	public class OpenRocketSaverTask extends AsyncTask<Void, Void, Boolean> {
		
		private final static String PROGRESS_DIALOG_TAG = "progress_dialog";
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if ( showProgressDialog ) {
				String savingMessage = getActivity().getResources().getString(R.string.saving);
				DialogFragment newFragment = ProgressDialogFragment.newInstance("", savingMessage);
				newFragment.show(getFragmentManager(), PROGRESS_DIALOG_TAG);
			} else {
				((SherlockFragmentActivity)getActivity()).setSupportProgressBarIndeterminate(true);
				((SherlockFragmentActivity)getActivity()).setSupportProgress(0);
			}
			
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Boolean doInBackground(Void... arg0) {
			AndroidLogWrapper.d(OpenRocketSaverTask.class, "doInBackgroud");
			
			try {
				CurrentRocketHolder.getCurrentRocket().saveOpenRocketDocument();
				return true;
			} catch (Throwable ex) {
				AndroidLogWrapper.e(OpenRocketSaverTask.class, "doInBackground rocketLaoder.load threw {}", ex);
			}
			return false;
			
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			AndroidLogWrapper.d(OpenRocketSaverFragment.class,"Finished saving " + OpenRocketSaverTask.this);
			if ( showProgressDialog ) {
				Fragment progress = getActivity().getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_TAG);
				if ( progress != null ) {
					// Remove the fragment instead of trying to use DialogFragment.dismiss.
					// If the dialog is now currently shown, dismiss fails.
					getFragmentManager().beginTransaction().remove(progress).commitAllowingStateLoss();
				}
			} else {
				((SherlockFragmentActivity)getActivity()).setSupportProgress(Window.PROGRESS_END);
				((SherlockFragmentActivity)getActivity()).setSupportProgressBarVisibility(false);
			}
			if ( listener != null ) {
				listener.onOpenRocketFileSaved(result);
			}
		}

	}

}
