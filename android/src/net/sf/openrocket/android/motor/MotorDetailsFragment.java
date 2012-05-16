package net.sf.openrocket.android.motor;

import net.sf.openrocket.R;
import net.sf.openrocket.android.db.ConversionUtils;
import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.unit.UnitGroup;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class MotorDetailsFragment extends DialogFragment {

	EditText manuField;
	EditText nameField;
	EditText delaysField;
	EditText caseField;
	EditText impulseClassField;
	EditText diameterField;
	EditText lengthField;

	ExtendedThrustCurveMotor motor;

	public static MotorDetailsFragment newInstance( long motorId ) {
		MotorDetailsFragment fragment = new MotorDetailsFragment();
		Bundle b = new Bundle();
		b.putLong("motorId", motorId);
		fragment.setArguments(b);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE,getTheme());

		Long motorId;
		if ( savedInstanceState != null ) {
			motorId = savedInstanceState.getLong("motorId");
		} else {
			Bundle b = getArguments();
			motorId = b.getLong("motorId");
		}
		DbAdapter mDbHelper = new DbAdapter(getActivity());
		mDbHelper.open();
		try {
			motor = mDbHelper.getMotorDao().fetchMotor(motorId);
		} catch ( Exception e ) {
		}
		mDbHelper.close();
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putLong("motorId", motor.getId());
	}

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
		init();
		/* TODO - enable saving.
		((Button) v.findViewById(R.id.motorDetailsSaveButton)).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MotorDetailsFragment.this.saveChanges();
					}
				});
				*/
		return v;
	}

	private void init( ) {
		manuField.setText( motor.getManufacturer().getDisplayName());
		nameField.setText( motor.getDesignation() );
		delaysField.setText( ConversionUtils.delaysToString(motor.getStandardDelays()) );
		caseField.setText( motor.getCaseInfo());
		impulseClassField.setText( motor.getImpulseClass());
		diameterField.setText( UnitGroup.UNITS_MOTOR_DIMENSIONS.toString(motor.getDiameter()) );
		lengthField.setText( UnitGroup.UNITS_LENGTH.getUnit("mm").toString(motor.getLength()) );
	}

	private void saveChanges() {
		DbAdapter mDbHelper = new DbAdapter(getActivity());
		mDbHelper.open();
		try {
			mDbHelper.getMotorDao().insertOrUpdateMotor(motor);
		} catch ( Exception e ) {
		}

	}
}
