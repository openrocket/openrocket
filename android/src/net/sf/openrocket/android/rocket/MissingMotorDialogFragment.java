package net.sf.openrocket.android.rocket;

import java.util.Set;

import net.sf.openrocket.R;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.motor.ThrustCurveMotorPlaceholder;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class MissingMotorDialogFragment extends SherlockDialogFragment {

	private final static String MESSAGES_ARG_KEY = "messages";

	public static MissingMotorDialogFragment newInstance( Set<ThrustCurveMotorPlaceholder> missingMotors ) {
		MissingMotorDialogFragment frag = new MissingMotorDialogFragment();
		Bundle b = new Bundle();
		String[] messages = new String[ missingMotors.size() ];
		int index = 0;
		for( ThrustCurveMotorPlaceholder m : missingMotors ) {
			messages[index++] = m.getManufacturer() + " " + m.getDesignation();
		}
		b.putStringArray(MESSAGES_ARG_KEY, messages);
		frag.setArguments(b);
		frag.setCancelable(false);
		return frag;
	}

	private String buildMessage( String[] missingMotors ) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getString(R.string.missingMotorsMessageStart));
		for( String m : missingMotors ) {
			sb.append("\n").append(m);
		}
		sb.append("\n").append(this.getString(R.string.missingMotorsMessageEnd));
		return sb.toString();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		((OpenRocketLoaderActivity)getActivity()).doNotFixMissingMotors();
		super.onCancel(dialog);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AndroidLogWrapper.d(MissingMotorDialogFragment.class,"onCreateDialog");

		String[] messages = getArguments().getStringArray(MESSAGES_ARG_KEY);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//	                .setIcon(android.R.drawable.alert_dialog_icon)
		builder.setTitle(R.string.missingMotors);
		builder.setMessage(buildMessage(messages));
		builder.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				((OpenRocketLoaderActivity)getActivity()).doFixMissingMotors();
			}
		});

		builder.setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				((OpenRocketLoaderActivity)getActivity()).doNotFixMissingMotors();
			}
		});

		AlertDialog dialog =  builder.create();
		dialog.setOwnerActivity(getActivity());
		return dialog;
	}

}

