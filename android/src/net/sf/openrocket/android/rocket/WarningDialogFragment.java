package net.sf.openrocket.android.rocket;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.android.Application;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class WarningDialogFragment extends DialogFragment {

	public static WarningDialogFragment newInstance() {
		WarningDialogFragment frag = new WarningDialogFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
		//	                .setIcon(android.R.drawable.alert_dialog_icon)
		builder.setTitle("Warnings");
		WarningSet warnings = ((Application)(getActivity().getApplication())).getWarnings();
		StringBuilder message = new StringBuilder();
		for ( Warning w : warnings ) {
			message.append(w.toString()).append("\n");
		}
		builder.setMessage(message.toString());
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				((OpenRocketLoaderActivity)getActivity()).moveOnToViewer();
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				((OpenRocketLoaderActivity)getActivity()).moveOnToViewer();
			}
		});
		return builder.create();
	}
}
