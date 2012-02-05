package net.sf.openrocket.android.rocket;

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
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				getActivity().finish();
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				getActivity().finish();
			}
		});
		return builder.create();
	}
}
