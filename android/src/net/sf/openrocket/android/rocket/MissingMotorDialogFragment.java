package net.sf.openrocket.android.rocket;

import java.util.Set;

import net.sf.openrocket.motor.ThrustCurveMotorPlaceholder;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class MissingMotorDialogFragment extends DialogFragment {

	Set<ThrustCurveMotorPlaceholder> missingMotors;

	public static MissingMotorDialogFragment newInstance( Set<ThrustCurveMotorPlaceholder> missingMotors ) {
		MissingMotorDialogFragment frag = new MissingMotorDialogFragment();
		frag.missingMotors = missingMotors;
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//	                .setIcon(android.R.drawable.alert_dialog_icon)
		builder.setTitle("Missing Motors");
		StringBuilder sb = new StringBuilder();
		sb.append("The following motors are missing:");
		for( ThrustCurveMotorPlaceholder m : missingMotors ) {
			sb.append("\n").append(m.getManufacturer()).append(" ").append(m.getDesignation());
		}
		sb.append("\nWould you like to download them from Thrustcurve?");
		builder.setMessage(sb.toString());
		builder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				((OpenRocketLoaderActivity)getActivity()).doFixMissingMotors();
			}
		}
				);
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				((OpenRocketLoaderActivity)getActivity()).doNotFixMissingMotors();
			}
		}
				);
		return builder.create();
	}
}

