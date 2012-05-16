package net.sf.openrocket.android;

import java.util.Collections;
import java.util.List;

import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.motor.ExtendedThrustCurveMotor;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.database.MotorDatabase;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.Motor.Type;
import android.content.Context;

public class MotorDatabaseAdapter implements MotorDatabase {

	private DbAdapter mDbHelper;

	public MotorDatabaseAdapter( Context ctx ) {
		mDbHelper = new DbAdapter(ctx);
		mDbHelper.open();
	}

	@Override
	public List<? extends Motor> findMotors(Type type, String manufacturer,
			String designation, double diameter, double length) {

		AndroidLogWrapper.d(MotorDatabaseAdapter.class,"find motor: type="+ String.valueOf(type));
		AndroidLogWrapper.d(MotorDatabaseAdapter.class,"find motor: manu="+ manufacturer);
		AndroidLogWrapper.d(MotorDatabaseAdapter.class,"find motor: designation="+ designation);
		AndroidLogWrapper.d(MotorDatabaseAdapter.class,"find motor: diameter=" +diameter);
		AndroidLogWrapper.d(MotorDatabaseAdapter.class,"find motor: length="+ length);

		try {
			ExtendedThrustCurveMotor m = mDbHelper.getMotorDao().fetchMotor(manufacturer, designation);
			if ( m != null ) {
				return Collections.singletonList(m);
			}
		} catch ( Exception ex ) {

		}
		return Collections.<Motor>emptyList();
	}

}
