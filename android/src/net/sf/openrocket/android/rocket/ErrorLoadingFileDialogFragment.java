package net.sf.openrocket.android.rocket;

import net.sf.openrocket.R;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class ErrorLoadingFileDialogFragment extends SherlockDialogFragment {

	public static ErrorLoadingFileDialogFragment newInstance( int titleRes, String message ) {
		ErrorLoadingFileDialogFragment dialog = new ErrorLoadingFileDialogFragment();
		Bundle b = new Bundle();
		b.putString("message", message);
		b.putInt("titleRes", titleRes);
		dialog.setArguments(b);
		dialog.setCancelable(true);
		return dialog;
	}
	

	@Override
	public void onCancel(DialogInterface dialog) {
		((OpenRocketLoaderActivity)getActivity()).doDismissErrorDialog();
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AndroidLogWrapper.d(ErrorLoadingFileDialogFragment.class,"onCreateDialog");

		String message = getArguments().getString("message");
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		if ( getArguments().containsKey("titleRes") ) {
			int titleRes = getArguments().getInt("titleRes");
			builder.setTitle(titleRes);
		}
		
		builder.setMessage(message);
		builder.setNeutralButton(R.string.dismiss,  new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				((OpenRocketLoaderActivity)getActivity()).doDismissErrorDialog();
			}

		});
		
		final AlertDialog dialog = builder.create();
		dialog.setOwnerActivity(getActivity());
		return dialog;
	}


}
