package net.sf.openrocket.android.rocket;

import java.util.Set;

import net.sf.openrocket.motor.ThrustCurveMotorPlaceholder;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class MissingMotorDialogFragment extends DialogFragment {
	
	private final static String MESSAGE_ARG_KEY = "message";

	public static MissingMotorDialogFragment newInstance( Set<ThrustCurveMotorPlaceholder> missingMotors ) {
		MissingMotorDialogFragment frag = new MissingMotorDialogFragment();
		Bundle b = new Bundle();
		b.putString(MESSAGE_ARG_KEY, buildMessage(missingMotors));
		frag.setArguments(b);
		return frag;
	}

	private static String buildMessage( Set<ThrustCurveMotorPlaceholder> missingMotors ) {
		StringBuilder sb = new StringBuilder();
		sb.append("The following motors are missing:");
		for( ThrustCurveMotorPlaceholder m : missingMotors ) {
			sb.append("\n").append(m.getManufacturer()).append(" ").append(m.getDesignation());
		}
		sb.append("\nWould you like to download them from Thrustcurve?");
		return sb.toString();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setCancelable(false);
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		String message = getArguments().getString(MESSAGE_ARG_KEY);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//	                .setIcon(android.R.drawable.alert_dialog_icon)
		builder.setTitle("Missing Motors");
		builder.setMessage(message);
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

	/**
	 * Work around for dialog getting dismissed on orientation change.  See code.google.com/p/android/issues/detail?id=17423
	 */
	@Override
	public void onDestroyView() {
		if ( getDialog() != null  && getRetainInstance() ) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}
	
	
}

