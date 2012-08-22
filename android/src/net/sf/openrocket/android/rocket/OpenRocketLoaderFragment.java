package net.sf.openrocket.android.rocket;

import java.io.File;

import net.sf.openrocket.R;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.android.util.ProgressDialogFragment;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.DatabaseMotorFinderWithMissingMotors;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class OpenRocketLoaderFragment extends SherlockFragment {
	
	private final static String FILE_ARG_KEY = "file";
	
	public interface OnOpenRocketFileLoaded {
		public void onOpenRocketFileLoaded( OpenRocketLoaderResult result );
	}
	
	private File file;
	private OpenRocketLoaderTask task;
	private OnOpenRocketFileLoaded listener;
	
	public static OpenRocketLoaderFragment newInstance(File file) {
		OpenRocketLoaderFragment frag = new OpenRocketLoaderFragment();
		Bundle b = new Bundle();
		b.putSerializable(FILE_ARG_KEY, file);
		frag.setArguments(b);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		Bundle b = getArguments();
		file = (File) b.getSerializable(FILE_ARG_KEY);
		if ( task == null ) {
			// since we retain instance state, task will be non-null if it is already loading.
			task = new OpenRocketLoaderTask();
			task.execute(file);
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
		if ( parent instanceof OnOpenRocketFileLoaded ) {
			listener = (OnOpenRocketFileLoaded) parent;
		}
	}

	public class OpenRocketLoaderTask extends AsyncTask<File, Void, OpenRocketLoaderResult> {
		
		private final static String PROGRESS_DIALOG_TAG = "progress_dialog";
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			String loading = getActivity().getResources().getString(R.string.loading);
			DialogFragment newFragment = ProgressDialogFragment.newInstance("", loading);
			newFragment.show(getFragmentManager(), PROGRESS_DIALOG_TAG);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected OpenRocketLoaderResult doInBackground(File... arg0) {
			AndroidLogWrapper.d(OpenRocketLoaderTask.class, "doInBackgroud");
			
			GeneralRocketLoader rocketLoader = new GeneralRocketLoader();
			OpenRocketLoaderResult result = new OpenRocketLoaderResult();
			try {
				OpenRocketDocument rocket = rocketLoader.load(arg0[0], new DatabaseMotorFinderWithMissingMotors());
				rocket.getDefaultConfiguration().setAllStages();
				result.rocket = rocket;
				result.warnings = rocketLoader.getWarnings();
			} catch (RocketLoadException ex) {
				AndroidLogWrapper.e(OpenRocketLoaderTask.class, "doInBackground rocketLaoder.load threw {}", ex);
				result.loadingError = ex;
			}
			return result;
			
		}

		@Override
		protected void onPostExecute(OpenRocketLoaderResult result) {
			super.onPostExecute(result);
			AndroidLogWrapper.d(OpenRocketLoaderFragment.class,"Finished loading " + OpenRocketLoaderTask.this);
			Fragment progress = getActivity().getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_TAG);
			if ( progress != null ) {
				// Remove the fragment instead of trying to use DialogFragment.dismiss.
				// If the dialog is now currently shown, dismiss fails.
				getFragmentManager().beginTransaction().remove(progress).commitAllowingStateLoss();
			}
			if ( listener != null ) {
				listener.onOpenRocketFileLoaded(result);
			}
		}

	}

}
