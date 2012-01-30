package net.sf.openrocket.android.motor;

import net.sf.openrocket.R;
import net.sf.openrocket.android.db.ConversionUtils;
import net.sf.openrocket.motor.ThrustCurveMotor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class MotorDetailsFragment extends Fragment {

	EditText manuField;
	EditText nameField;
	EditText delaysField;
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
		delaysField = (EditText) v.findViewById(R.id.motorDetailsDelays);
		caseField = (EditText) v.findViewById(R.id.motorDetailsCaseInfo);
		impulseClassField = (EditText) v.findViewById(R.id.motorDetailsImpuseClass);
		diameterField = (EditText) v.findViewById(R.id.motorDetailsDiameter);
		lengthField = (EditText) v.findViewById(R.id.motorDetailsLength);
		return v;
	}

	public void init( ExtendedThrustCurveMotor m ) {
		ThrustCurveMotor tcm = m.getThrustCurveMotor();
		manuField.setText( tcm.getManufacturer().getDisplayName());
		nameField.setText( tcm.getDesignation() );
		delaysField.setText( ConversionUtils.delaysToString(tcm.getStandardDelays()) );
		caseField.setText( m.getCaseInfo());
		impulseClassField.setText( m.getImpulseClass());
		diameterField.setText( String.valueOf(tcm.getDiameter()*1000.0) );
		lengthField.setText( String.valueOf(tcm.getLength()*1000.0) );
		
	}
	
}
