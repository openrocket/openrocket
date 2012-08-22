package net.sf.openrocket.android.rocket;

import java.io.File;
import java.util.Set;

import net.sf.openrocket.R;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.android.filebrowser.SimpleFileBrowser;
import net.sf.openrocket.android.thrustcurve.TCMissingMotorDownloadAction;
import net.sf.openrocket.android.thrustcurve.TCQueryAction;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.motor.ThrustCurveMotorPlaceholder;
import net.sf.openrocket.rocketcomponent.Rocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class OpenRocketLoaderActivity extends SherlockFragmentActivity
implements TCQueryAction.OnTCQueryCompleteListener, OpenRocketLoaderFragment.OnOpenRocketFileLoaded
{

	private static final int PICK_ORK_FILE_RESULT = 1;

	private final static String MISSING_MOTOR_DIAG_FRAGMENT_TAG = "missingmotordialog";
	private final static String MISSING_MOTOR_DOWNLOAD_FRAGMENT_TAG = "missingmotortask";

	/*
	 * Set to true when we have started to load a file.  Is saved in InstanceState.
	 */
	private boolean isLoading = false;
	/*
	 * Set to the Uri of the file we are supposed to load.  Is saved in InstanceState.
	 */
	private Uri fileToLoad = null;
	
	protected boolean isLoading() {
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class, "isLoading " + this.hashCode());
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class, "isLoading = " + isLoading);
		return isLoading;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		Intent i = getIntent();
		if (Intent.ACTION_VIEW.equals(i.getAction()) && i.getData() != null ) {
			Uri file = i.getData();
			fileToLoad = file;
			loadOrkFile();
		} else {
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class, "onSaveInstanceState " + this.hashCode());
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class, "isLoading = " + isLoading);
		outState.putBoolean("isLoading", isLoading);
		if ( fileToLoad != null ) {
			outState.putParcelable("fileToLoad", fileToLoad);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class, "onRestoreInstanceState");
		isLoading = savedInstanceState.getBoolean("isLoading",false);
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class, "isLoading = " + isLoading);
		if ( savedInstanceState.containsKey("fileToLoad") ) {
			fileToLoad = savedInstanceState.getParcelable("fileToLoad");
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class, "onResume");
		super.onResume();
		// Start loading a file if we have a file and are not already loading one.
		if ( fileToLoad != null && !isLoading ) {
			loadOrkFile();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class, "onActivityResult");
		switch ( requestCode ) {
		case PICK_ORK_FILE_RESULT:
			if(resultCode==RESULT_OK){
				Uri file = data.getData();
				fileToLoad = file;
				// It would be nice to just start loading the file - but that doesn't work correctly.
				// I'm uncertain if it is a bug in Android 14/15 or a bug in the v4 support library.
				// essentially what happens is, when the FileBrowserActivity is brought up,
				// this activity goes through the saveInstanceState calls to push it to the background.
				// When the FileBrowserActivity returns the result, this.onActivityResult is called
				// prior to any of the other lifecycle methods (onRestoreInstanceState as documented, but onStart is
				// a bug. Since onStart hasn't been called, this activity is not able to create fragments - which 
				// are used to indicate progress etc.
				// Instead of calling loadOrkFile() here, we push the file Uri into a member variable,
				// then check the member variable in onResume to actuall kick off the work.
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	protected void pickOrkFiles( ) {
		Resources resources = this.getResources();
		String key = resources.getString(R.string.PreferenceUseInternalFileBrowserOption);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		boolean useinternalbrowser = pref.getBoolean(key, true);

		if ( useinternalbrowser ) {
			Intent intent = new Intent(OpenRocketLoaderActivity.this, SimpleFileBrowser.class);
			startActivityForResult(intent,PICK_ORK_FILE_RESULT);
		} else {
			try {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("file/*");
				startActivityForResult(intent,PICK_ORK_FILE_RESULT);
			} catch ( ActivityNotFoundException ex ) { 
				// No activity for ACTION_GET_CONTENT  use internal file browser
				// update the preference value.
				pref.edit().putBoolean(key, false).commit();
				// fire our browser
				Intent intent = new Intent(OpenRocketLoaderActivity.this, SimpleFileBrowser.class);
				startActivityForResult(intent,PICK_ORK_FILE_RESULT);
			}
		}		
	}

	private void loadOrkFile( ) {
		// a little protection.
		if ( fileToLoad == null ) {
			return;
		}
		isLoading = true;
		CurrentRocketHolder.getCurrentRocket().setFileUri( fileToLoad );
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class,"Use ork file: " + fileToLoad);
		String path = fileToLoad.getPath();
		File orkFile = new File(path);

		// Also need commitAllowingState loss because of a bug in v4 dialog show.
		getSupportFragmentManager().beginTransaction()
		  .add( OpenRocketLoaderFragment.newInstance(orkFile), "loader")
		  .commitAllowingStateLoss();

	}

	/**
	 * Called by the OpenRocketLoaderTask when it completes.
	 * is default visibility so it can be called from this package.
	 * 
	 * @param result
	 */
	public void onOpenRocketFileLoaded(OpenRocketLoaderResult result) {
		if ( result.loadingError != null ) {

			ErrorLoadingFileDialogFragment errorDialog = ErrorLoadingFileDialogFragment.newInstance(R.string.loadingErrorMessage, result.loadingError.getLocalizedMessage());
			errorDialog.show(getSupportFragmentManager(),"errorDialog");

		} else {
			CurrentRocketHolder.getCurrentRocket().setRocketDocument( result.rocket );
			CurrentRocketHolder.getCurrentRocket().setWarnings( result.warnings );

			updateMissingMotors();
		}
	}

	private void updateMissingMotors() {
		Rocket rocket = CurrentRocketHolder.getCurrentRocket().getRocketDocument().getRocket();
		Set<ThrustCurveMotorPlaceholder> missingMotors = MissingMotorHelpers.findMissingMotors(rocket);

		if ( missingMotors.size() > 0 ) {
			MissingMotorDialogFragment missingMotorDialog = MissingMotorDialogFragment.newInstance( missingMotors );
			missingMotorDialog.show(getSupportFragmentManager(), MISSING_MOTOR_DIAG_FRAGMENT_TAG);
			return;
		}

		displayWarningDialog();
	}

	/**
	 * Called when the TCMissingMotorDownload process finishes.
	 */
	@Override
	public void onTCQueryComplete(String message) {

		Rocket rocket = CurrentRocketHolder.getCurrentRocket().getRocketDocument().getRocket();
		WarningSet warnings = CurrentRocketHolder.getCurrentRocket().getWarnings();
		// Need to update the motor references.
		MissingMotorHelpers.updateMissingMotors(rocket, warnings);

		displayWarningDialog();
	}

	private void displayWarningDialog() {
		WarningSet warnings = CurrentRocketHolder.getCurrentRocket().getWarnings();
		if (warnings == null || warnings.isEmpty()) {
		} else {
			DialogFragment newFragment = WarningDialogFragment.newInstance();
			newFragment.show(getSupportFragmentManager(), "dialog");
			return;
		}

		moveOnToViewer();
	}

	public void doFixMissingMotors() {
		Rocket rocket = CurrentRocketHolder.getCurrentRocket().getRocketDocument().getRocket();
		Set<ThrustCurveMotorPlaceholder> missingMotors = MissingMotorHelpers.findMissingMotors(rocket);

		TCMissingMotorDownloadAction motorfrag = TCMissingMotorDownloadAction.newInstance( missingMotors );
		getSupportFragmentManager().beginTransaction().add( motorfrag, MISSING_MOTOR_DOWNLOAD_FRAGMENT_TAG).commit();

	}

	public void doNotFixMissingMotors() {
		displayWarningDialog();
	}

	public void doDismissErrorDialog() {
		isLoading = false;
		fileToLoad = null;
	}
	
	public void moveOnToViewer() {
		isLoading = false;
		Intent i = new Intent(this,OpenRocketViewer.class);
		startActivity(i);
		finish();
	}
}
