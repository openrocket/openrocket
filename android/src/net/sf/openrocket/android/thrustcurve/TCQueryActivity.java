package net.sf.openrocket.android.thrustcurve;

import net.sf.openrocket.R;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class TCQueryActivity extends Activity
implements TCQueryAction.OnComplete
{

	private TCSearchAction queryAction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tcqueryform);
		
		queryAction = new TCSearchAction(this);

		final Spinner manufacturerField = (Spinner) findViewById(R.id.TCMotorSearchFormManufacturerField);
		final Spinner impulseField = (Spinner) findViewById(R.id.TCMotorSearchFormImpulseField);
		final Spinner diameterField = (Spinner) findViewById(R.id.TCMotorSearchFormDiameterField);
		final EditText commonNameField = (EditText) findViewById(R.id.TCMotorSearchFormCommonNameField);

		Button submitButton = (Button) findViewById(R.id.TCMotorSearchFromSubmitButton);
		submitButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick( View v ) {
						AndroidLogWrapper.d(TCQueryActivity.class,"submit button clicked");
						
						String commonName = commonNameField.getText().toString();

						SearchRequest r = new SearchRequest();
						if ( manufacturerField.getSelectedItemPosition() != 0) {
							String m = (String) manufacturerField.getSelectedItem();
							AndroidLogWrapper.d(TCQueryActivity.class,"manufacturer = " + m);
							r.setManufacturer(m);
						}
						if ( impulseField.getSelectedItemPosition() != 0  ) {
							String impulse = (String) impulseField.getSelectedItem();
							AndroidLogWrapper.d(TCQueryActivity.class,"impulse = " + impulse);
							r.setImpulse_class(impulse);
						}
						if ( diameterField.getSelectedItemPosition() != 0 ) {
							String diameter = (String)diameterField.getSelectedItem();
							AndroidLogWrapper.d(TCQueryActivity.class,"diameter = " + diameter);
							r.setDiameter(diameter);
						}
						r.setCommon_name(commonName);

						queryAction.setRequest(r);
						queryAction.start();
					}
				}
				);
	}

	@Override
	public void onComplete() {
		finish();
	}

	/*
	 * TODO - ??
	@Override
	public Object onRetainNonConfigurationInstance() {
		return downloadThread;
	}
	 */
	@Override
	protected void onDestroy() {
		queryAction.dismiss();
		super.onDestroy();
	}

}

