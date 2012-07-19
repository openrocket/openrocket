package net.sf.openrocket.android.thrustcurve;

import net.sf.openrocket.R;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.android.util.ErrorDialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TCQueryActivity extends SherlockFragmentActivity
implements TCQueryAction.OnTCQueryCompleteListener
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.TCMotorSearchFormTitle);
		setContentView(R.layout.tcqueryform);

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

						TCSearchAction motorfrag = TCSearchAction.newInstance( r );
						getSupportFragmentManager().beginTransaction().add( motorfrag, "dloader").commit();
					}
				});
	}

	@Override
	public void onTCQueryComplete(String message) {
		if ( message != null) {
			ErrorDialogFragment error = ErrorDialogFragment.newInstance(message);
			error.show(getSupportFragmentManager(), "ErrorDialog");
		} else {
			finish();
		}
	}

}

