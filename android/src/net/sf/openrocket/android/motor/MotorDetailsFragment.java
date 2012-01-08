package net.sf.openrocket.android.motor;

import net.sf.openrocket.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class MotorDetailsFragment extends Fragment {

	EditText manuField;
	EditText nameField;
	EditText caseField;
	EditText impulseClassField;
	EditText diameterField;
	EditText lengthField;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.motor_detail_form, container, false);
		manuField = (EditText) v.findViewById(R.id.motorDetailsManufacturer);
		nameField = (EditText) v.findViewById(R.id.motorDetailsName);
		caseField = (EditText) v.findViewById(R.id.motorDetailsCaseInfo);
		impulseClassField = (EditText) v.findViewById(R.id.motorDetailsImpuseClass);
		diameterField = (EditText) v.findViewById(R.id.motorDetailsDiameter);
		lengthField = (EditText) v.findViewById(R.id.motorDetailsLength);
		return v;
	}

	public void init( Motor m ) {
		manuField.setText( m.getManufacturer());
		nameField.setText( m.getName() );
		caseField.setText( m.getCaseInfo());
		impulseClassField.setText( m.getImpulseClass());
		diameterField.setText( m.getDiameter().toString() );
		lengthField.setText( m.getLength().toString() );
		
	}
	
}
