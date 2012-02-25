package net.sf.openrocket.android;

import net.sf.openrocket.android.rocket.OpenRocketLoaderActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AboutDialogFragment extends DialogFragment {

	public static AboutDialogFragment newInstance() {
		AboutDialogFragment frag = new AboutDialogFragment();
		return frag;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//	                .setIcon(android.R.drawable.alert_dialog_icon)
		builder.setTitle("About");
		StringBuilder sb = new StringBuilder();
		sb.append("OpenRocket\n");
		sb.append("Copyright 2007-2012 Sampo Niskanen\n");
		sb.append("\n");
		sb.append("The android port contains third party software:\n");
		sb.append(" AChartEngine - Apache License 2.0\n");
		sb.append(" Android Open Source Project - Apache License 2.0\n");
		sb.append(" Android tree-view-list - 2-clause BSD licensed\n");
		builder.setMessage(sb.toString());
		builder.setCancelable(true);
		return builder.create();
	}

}
