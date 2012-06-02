package net.sf.openrocket.android.rocket;

import java.io.File;
import java.util.Set;

import net.sf.openrocket.R;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.android.thrustcurve.TCMissingMotorDownloadAction;
import net.sf.openrocket.android.thrustcurve.TCQueryAction;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.motor.ThrustCurveMotorPlaceholder;
import net.sf.openrocket.rocketcomponent.Rocket;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class OpenRocketLoaderActivity extends SherlockFragmentActivity
implements TCQueryAction.OnTCQueryCompleteListener, OpenRocketLoaderFragment.OnOpenRocketFileLoaded
{

	private final static String MISSING_MOTOR_DIAG_FRAGMENT_TAG = "missingmotordialog";
	private final static String MISSING_MOTOR_DOWNLOAD_FRAGMENT_TAG = "missingmotortask";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("");
		setContentView(R.layout.main);
		if ( savedInstanceState == null || savedInstanceState.getBoolean("isLoading", false) == false ) {
			Intent i = getIntent();
			Uri file = i.getData();
			loadOrkFile(file);
		} else {
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("isLoading", true);
	}

	private void loadOrkFile( Uri file ) {
		CurrentRocketHolder.getCurrentRocket().setFileUri( file );
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class,"Use ork file: " + file);
		String path = file.getPath();
		File orkFile = new File(path);

		getSupportFragmentManager().beginTransaction().add( OpenRocketLoaderFragment.newInstance(orkFile), "loader").commit();

	}

	/**
	 * Called by the OpenRocketLoaderTask when it completes.
	 * is default visibility so it can be called from this package.
	 * 
	 * @param result
	 */
	public void onOpenRocketFileLoaded(OpenRocketLoaderResult result) {
		if ( result.loadingError != null ) {
			
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setTitle("Error Loading File" );
			dialogBuilder.setMessage( result.loadingError.getLocalizedMessage());
			dialogBuilder.setOnCancelListener( new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					OpenRocketLoaderActivity.this.finish();
				}
			});
			dialogBuilder.create().show();

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
			DialogFragment missingMotorDialog = MissingMotorDialogFragment.newInstance( missingMotors );
			getSupportFragmentManager().beginTransaction().add(missingMotorDialog, MISSING_MOTOR_DIAG_FRAGMENT_TAG).commit();
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

	public void moveOnToViewer() {
		Intent i = new Intent(this,OpenRocketViewer.class);
		startActivity(i);
		finish();
	}
}
