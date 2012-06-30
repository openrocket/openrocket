package net.sf.openrocket.android.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ErrorDialogFragment extends DialogFragment {

	public static ErrorDialogFragment newInstance( String message ) {
		ErrorDialogFragment dialog = new ErrorDialogFragment();
		Bundle b = new Bundle();
		b.putString("message",message);
		dialog.setArguments(b);
		return dialog;
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = getArguments().getString("message");
		final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
		dialog.setOwnerActivity(getActivity());
		dialog.setMessage(message);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Dismiss", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dialog.dismiss();
			}

		});
		return dialog;
	}

}

