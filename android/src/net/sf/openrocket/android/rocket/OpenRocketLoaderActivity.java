package net.sf.openrocket.android.rocket;

import java.io.File;
import java.util.Set;

import net.sf.openrocket.R;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.android.thrustcurve.TCMissingMotorDownloadAction;
import net.sf.openrocket.android.thrustcurve.TCQueryAction;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.motor.ThrustCurveMotorPlaceholder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class OpenRocketLoaderActivity extends FragmentActivity
implements TCQueryAction.OnComplete
{

	private OpenRocketLoaderResult result;
	
	private Set<ThrustCurveMotorPlaceholder> missingMotors;
	private OpenRocketLoaderTask task;
	private ProgressDialog progress;
	private DialogFragment missingMotorDialog;
	private TCMissingMotorDownloadAction missingMotorDownloadAction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		missingMotorDownloadAction = new TCMissingMotorDownloadAction(this);
		if ( savedInstanceState == null || savedInstanceState.getBoolean("isLoading", false) == false ) {
			Intent i = getIntent();
			Uri file = i.getData();
			loadOrkFile(file);
		} else {
			progress = ProgressDialog.show(this, "Loading file", "");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("isLoading", true);
	}

	@Override
	protected void onDestroy() {
		if ( progress != null ) {
			if ( progress.isShowing() ) {
				progress.dismiss();
			}
			progress = null;
		}
		if ( missingMotorDownloadAction != null ) {
			missingMotorDownloadAction.dismiss();
		}

		super.onDestroy();
	}

	private void loadOrkFile( Uri file ) {
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class,"Use ork file: " + file);
		String path = file.getPath();
		File orkFile = new File(path);
		progress = ProgressDialog.show(this, "Loading file", "");

		task = new OpenRocketLoaderTask(this);

		task.execute(orkFile);

	}

	/**
	 * Called by the OpenRocketLoaderTask when it completes.
	 * is default visibility so it can be called from this package.
	 * 
	 * @param result
	 */
	void finishedLoading(OpenRocketLoaderResult result) {
		if ( progress != null && progress.isShowing() ) {
			progress.dismiss();
		}
		this.result = result;
		((Application)OpenRocketLoaderActivity.this.getApplication()).setRocketDocument( result.rocket );

		updateMissingMotors();

	}

	private void updateMissingMotors() {
		missingMotors = MissingMotorHelpers.findMissingMotors(result.rocket.getRocket());

		if ( missingMotors.size() > 0 ) {
			missingMotorDialog = MissingMotorDialogFragment.newInstance( missingMotors );
			missingMotorDialog.show(getSupportFragmentManager(), "missing motors");
			return;
		}

		displayWarningDialog();
	}

	/**
	 * Called when the TCMissingMotorDownload process finishes.
	 */
	@Override
	public void onComplete() {

		// Need to update the motor references.
		MissingMotorHelpers.updateMissingMotors(result.rocket.getRocket(), result.warnings);

		displayWarningDialog();
	}

	private void displayWarningDialog() {
		WarningSet warnings = result.warnings;
		if (warnings == null || warnings.isEmpty()) {
		} else {
			// TODO - Build a warning listing dialog
			DialogFragment newFragment = WarningDialogFragment.newInstance();
			newFragment.show(getSupportFragmentManager(), "dialog");
			return;
		}

		moveOnToViewer();
	}

	public void doFixMissingMotors() {

		missingMotorDialog.dismiss();

		missingMotorDownloadAction.setMissingMotors(missingMotors);
		missingMotorDownloadAction.start();

	}

	public void doNotFixMissingMotors() {
		missingMotorDialog.dismiss();
		displayWarningDialog();
	}

	private void moveOnToViewer() {
		Intent i = new Intent(this,OpenRocketViewer.class);
		startActivity(i);
		finish();
	}
}
