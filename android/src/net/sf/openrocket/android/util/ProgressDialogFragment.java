package net.sf.openrocket.android.util;

import java.lang.reflect.Method;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {

	ProgressDialog progressDialog;

	public static ProgressDialogFragment newInstance(String title, String message) {
		ProgressDialogFragment fragment = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("message", message);
		fragment.setArguments(args);

		return fragment;
	}

	/**
	 * Expose the private method to allow dismissing a dialog after saveInstanceState.
	 */
	public void dismissAllowingStateLoss() {
		try {
			Method dismissInternalMethod = ProgressDialogFragment.class.getMethod("dismissInternal", Boolean.class);
			dismissInternalMethod.setAccessible(true);
			dismissInternalMethod.invoke(this, true);
		} catch (Exception ex ) {
			AndroidLogWrapper.d(ProgressDialogFragment.class, "Exception calling dismissAllowingStateInteral");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public ProgressDialog onCreateDialog(Bundle savedInstanceState) {
		String title = null;
		String message = null;
		Bundle args = getArguments();
		if ( args != null ) {
			title = getArguments().getString("title");
			message = getArguments().getString("message");
		}

		AndroidLogWrapper.d(ProgressDialogFragment.class, "onCreateDialog");
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setTitle(title);
		progressDialog.setMessage(message);

		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);

		progressDialog.show();
		return progressDialog;
	}

	public void setMessage( String message ) {
		progressDialog.setMessage(message);
	}
}
