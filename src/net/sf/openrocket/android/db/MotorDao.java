package net.sf.openrocket.android.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import net.sf.openrocket.android.motor.Motor;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MotorDao {

	private static final String TAG = "MotorDao";

	private SQLiteDatabase mDb;

	private final static String DATABASE_TABLE = "motor";
	private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + DATABASE_TABLE;
	private final static String CREATE_TABLE =
			"create table "+ DATABASE_TABLE + " ( " +
					"_id integer primary key, "+
					"unique_name text unique, "+
					"name text, "+
					"diameter number, "+
					"tot_impulse_ns number, "+
					"avg_thrust_n number, "+
					"max_thrust_n number, "+
					"burn_time_s number, "+
					"length number," +
					"prop_mass_g number,"+
					"tot_mass_g number,"+
					"case_info text,"+
					"manufacturer text," +
					"impulse_class text," +
					"burndata blob"+
					");";

	MotorDao( SQLiteDatabase mDb ) {
		this.mDb = mDb;
	}

	static String[] create() { return new String[] {CREATE_TABLE}; }

	static String[] update( int oldVer, int newVer ) {
		return new String[] { DROP_TABLE, CREATE_TABLE };
	}

	public final static String ID = "_id";
	public final static String UNIQUE_NAME = "unique_name";
	public final static String NAME = "name";
	public final static String DIAMETER = "diameter";
	public final static String TOTAL_IMPULSE = "tot_impulse_ns"; 
	public final static String AVG_THRUST = "avg_thrust_n";
	public final static String MAX_THRUST = "max_thrust_n";
	public final static String BURN_TIME = "burn_time_s";
	public final static String LENGTH = "length";
	public final static String PROP_MASS = "prop_mass_g";
	public final static String TOT_MASS = "tot_mass_g";
	public final static String BURNDATA = "burndata";
	public final static String CASE_INFO = "case_info";
	public final static String MANUFACTURER = "manufacturer";
	public final static String IMPULSE_CLASS = "impulse_class";

	public long insertOrUpdateMotor(Motor mi) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, mi.getMotor_id());
		initialValues.put(NAME, mi.getName());
		initialValues.put(DIAMETER,mi.getDiameter());
		initialValues.put(TOTAL_IMPULSE,mi.getTotalImpulse());
		initialValues.put(AVG_THRUST,mi.getAvgThrust());
		initialValues.put(MAX_THRUST,mi.getMaxThrust());
		initialValues.put(BURN_TIME,mi.getBurnTime());
		initialValues.put(LENGTH, mi.getLength());
		initialValues.put(PROP_MASS, mi.getPropMass());
		initialValues.put(TOT_MASS,mi.getTotMass());
		initialValues.put(CASE_INFO, mi.getCaseInfo());
		initialValues.put(MANUFACTURER,mi.getManufacturer());
		initialValues.put(IMPULSE_CLASS,mi.getImpulseClass());
		initialValues.put(UNIQUE_NAME, mi.getManufacturer()+mi.getName());
		{
			// Serialize the Vector of burn data
			Vector<Double> burndata = mi.getBurndata();
			byte[] serObj = null;
			if ( burndata != null ) {
				try {
					ByteArrayOutputStream b = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(b);
					os.writeObject(burndata);
					os.close();
					serObj = b.toByteArray();
				} catch (Exception ex) {
					Log.d(TAG,"unable to serialze burndata");
				}
			}
			initialValues.put(BURNDATA, serObj);
		}


		Log.d(TAG,"insertOrUpdate Motor");
		long rv = mDb.insertWithOnConflict(DATABASE_TABLE, null, initialValues,SQLiteDatabase.CONFLICT_REPLACE);
		return rv;
	}

	/**
	 * Delete the motor and burn data with the given rowId
	 * 
	 * @param name name of motor to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteMotor(Long id) {

		boolean rv =  mDb.delete(DATABASE_TABLE, ID + "=" + id, null) > 0;
		return rv;
	}

	/**
	 * 
	 * @param groupCol
	 * @param groupVal
	 * @return
	 */
	public Cursor fetchAllInGroups( String groupCol, String groupVal ) {
		return mDb.query(DATABASE_TABLE, 
				/* columns */new String[] {
				ID,
				NAME,
				DIAMETER ,
				TOTAL_IMPULSE,
				AVG_THRUST ,
				MAX_THRUST ,
				BURN_TIME ,
				LENGTH,
				PROP_MASS,
				TOT_MASS,
				CASE_INFO,
				IMPULSE_CLASS,
				MANUFACTURER
		},
		/* selection */groupCol + "=?",
		/* selection args*/new String[] {groupVal},
		/* groupby */null,
		/* having*/null,
		/* orderby*/ NAME );

	}

	/**
	 * Fetch the groups based on groupCol
	 * @param groupCol
	 * @return
	 */
	public Cursor fetchGroups( String groupCol ) {
		return mDb.query(true, DATABASE_TABLE, 
				/* columns */new String[] {
				groupCol
		},
		/* selection */null,
		/* selection args*/null,
		/* groupby */null,
		/* having*/null,
		/* orderby*/null,
		/* limit*/ null);

	}

	/**
	 * Return a Cursor over the list of all motors
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllMotors() {

		return mDb.query(DATABASE_TABLE, 
				/* columns */new String[] {
				ID,
				NAME,
				DIAMETER ,
				TOTAL_IMPULSE,
				AVG_THRUST ,
				MAX_THRUST ,
				BURN_TIME ,
				LENGTH,
				PROP_MASS,
				TOT_MASS,
				CASE_INFO,
				IMPULSE_CLASS,
				MANUFACTURER
		},
		/* selection */null,
		/* selection args*/null,
		/* groupby */null,
		/* having*/null,
		/* orderby*/null);
	}

	public Motor fetchMotor(Long id ) throws SQLException {
		Cursor mCursor = mDb.query(DATABASE_TABLE, 
				/* columns */new String[] {
				ID,
				NAME ,
				DIAMETER ,
				TOTAL_IMPULSE ,
				AVG_THRUST ,
				MAX_THRUST ,
				BURN_TIME ,
				LENGTH,
				PROP_MASS,
				TOT_MASS,
				CASE_INFO,
				IMPULSE_CLASS,
				MANUFACTURER,
				BURNDATA
		},
		/* selection */ID + "="+id,
		/* selection args*/null,
		/* groupby */null,
		/* having*/null,
		/* orderby*/null);
		if ( mCursor == null ) {
			return null;
		}
		try {
			if (mCursor.getCount() == 0) {
				return null;
			}
			mCursor.moveToFirst();
			Motor mi = new Motor();
			mi.setMotor_id(mCursor.getLong(mCursor.getColumnIndex(ID)));
			mi.setName(mCursor.getString(mCursor.getColumnIndex(NAME)));
			mi.setDiameter(mCursor.getLong(mCursor.getColumnIndex(DIAMETER)));
			mi.setTotalImpulse(mCursor.getFloat(mCursor.getColumnIndex(TOTAL_IMPULSE)));
			mi.setAvgThrust(mCursor.getFloat(mCursor.getColumnIndex(AVG_THRUST)));
			mi.setMaxThrust(mCursor.getFloat(mCursor.getColumnIndex(MAX_THRUST)));
			mi.setBurnTime(mCursor.getFloat(mCursor.getColumnIndex(BURN_TIME)));
			mi.setLength(mCursor.getFloat(mCursor.getColumnIndex(LENGTH)));
			mi.setPropMass(mCursor.getDouble(mCursor.getColumnIndex(PROP_MASS)));
			mi.setCaseInfo(mCursor.getString(mCursor.getColumnIndex(CASE_INFO)));
			mi.setTotMass(mCursor.getDouble(mCursor.getColumnIndex(TOT_MASS)));
			mi.setManufacturer(mCursor.getString(mCursor.getColumnIndex(MANUFACTURER)));
			mi.setImpulseClass(mCursor.getString(mCursor.getColumnIndex(IMPULSE_CLASS)));

			{
				// Deserialize burndata column
				byte[] serObj = mCursor.getBlob(mCursor.getColumnIndex(BURNDATA));
				Vector<Double> burndata = null;
				if (serObj != null ) {
					try {
						ObjectInputStream is = new ObjectInputStream( new ByteArrayInputStream(serObj));
						burndata = (Vector<Double>) is.readObject();
					}
					catch (Exception ex) {
						Log.d(TAG,"cannot deserialize burndata");
					}
				}
				mi.setBurndata(burndata);
			}
			return mi;
		}
		finally {
			mCursor.close();
		}

	}

}
