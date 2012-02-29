package net.sf.openrocket.android;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutDialogFragment extends DialogFragment {

	public static AboutDialogFragment newInstance() {
		AboutDialogFragment frag = new AboutDialogFragment();
		return frag;
	}
	
	@Override
	public AlertDialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//	                .setIcon(android.R.drawable.alert_dialog_icon)
		builder.setTitle("About OpenRocket");
		StringBuilder sb = new StringBuilder();
		sb.append("<p>Copyright 2007-2012 Sampo Niskanen and others</p>");
		sb.append("<p>Android port by Kevin Ruland</p>");
		sb.append("<p/>");
		sb.append("<p>Licensed under GPLv3 or later. ");
		sb.append("Full source available on <a href=\"http://openrocket.sourceforge.net/\">SourceForge</a></p>");
		sb.append("<p>The android port contains third party software:</p>");
		sb.append("<nbsp/><p>AChartEngine - Apache License 2.0</p>");
		sb.append("<nbsp/><p>Android Open Source Project - Apache License 2.0</p>");
		sb.append("<nbsp/><p>Android tree-view-list - 2-clause BSD licensed</p>");
		String s = sb.toString();
		builder.setMessage(Html.fromHtml(s));
		builder.setCancelable(true);
		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		((TextView) this.getDialog().findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
	}

	
}
