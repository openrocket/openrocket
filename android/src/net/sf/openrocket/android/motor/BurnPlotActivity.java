package net.sf.openrocket.android.motor;

import net.sf.openrocket.android.util.AndroidLogWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class BurnPlotActivity extends FragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidLogWrapper.d(BurnPlotActivity.class,"onCreate Bundle = "+ String.valueOf(savedInstanceState));

		Intent i = getIntent();
		long motorId = i.getLongExtra("Motor",-1);

		BurnPlotFragment burnPlot = BurnPlotFragment.newInstance(motorId);
		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, burnPlot).commit();
		
	}

}
