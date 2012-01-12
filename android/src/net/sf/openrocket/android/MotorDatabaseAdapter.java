package net.sf.openrocket.android;

import java.util.Collections;
import java.util.List;

import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.motor.ExtendedThrustCurveMotor;
import net.sf.openrocket.database.MotorDatabase;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.Motor.Type;
import android.content.Context;
import android.util.Log;

public class MotorDatabaseAdapter implements MotorDatabase {

	private final static String TAG = "MotorDatabaseAdapter";
	private DbAdapter mDbHelper;

	public MotorDatabaseAdapter( Context ctx ) {
		mDbHelper = new DbAdapter(ctx);
		mDbHelper.open();
	}

	@Override
	public List<? extends Motor> findMotors(Type type, String manufacturer,
			String designation, double diameter, double length) {

		Log.d(TAG,"find motor: type="+ type.toString());
		Log.d(TAG,"find motor: manu="+ manufacturer);
		Log.d(TAG,"find motor: designation="+ designation);
		Log.d(TAG,"find motor: diameter=" +diameter);
		Log.d(TAG,"find motor: length="+ length);

		try {
			ExtendedThrustCurveMotor m = mDbHelper.getMotorDao().fetchMotor(manufacturer, designation);
			if ( m != null ) {
				return Collections.singletonList(m.getThrustCurveMotor());
			}
		} catch ( Exception ex ) {

		}
		return Collections.<Motor>emptyList();
	}

}
