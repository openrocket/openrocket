package net.sf.openrocket.android.db;

import net.sf.openrocket.android.motor.ExtendedThrustCurveMotor;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.android.util.AndroidLogWrapper.LogHelper;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.Coordinate;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MotorDao {
	
	private SQLiteDatabase mDb;
	
	private final static String DATABASE_TABLE = "motor";
	private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + DATABASE_TABLE;
	private final static String CREATE_TABLE =
			"create table " + DATABASE_TABLE + " ( " +
					"_id integer primary key, " +
					"unique_name text unique, " +
					"digest string, " +
					"designation text, " +
					"delays text, " +
					"diameter number, " +
					"tot_impulse_ns number, " +
					"avg_thrust_n number, " +
					"max_thrust_n number, " +
					"burn_time_s number, " +
					"length number," +
					"prop_mass_g number," +
					"tot_mass_g number," +
					"case_info text," +
					"manufacturer text," +
					"type text," +
					"impulse_class text," +
					"thrust_data blob," +
					"time_data blob," +
					"cg_data blob" +
					");";
	
	MotorDao(SQLiteDatabase mDb) {
		this.mDb = mDb;
	}
	
	static String[] create() {
		return new String[] { CREATE_TABLE };
	}
	
	static String[] update(int oldVer, int newVer) {
		return new String[] { DROP_TABLE, CREATE_TABLE };
	}
	
	public final static String ID = "_id";
	public final static String UNIQUE_NAME = "unique_name";
	public final static String DIGEST = "digest";
	public final static String DESIGNATION = "designation";
	public final static String DELAYS = "delays";
	public final static String DIAMETER = "diameter";
	public final static String TOTAL_IMPULSE = "tot_impulse_ns";
	public final static String AVG_THRUST = "avg_thrust_n";
	public final static String MAX_THRUST = "max_thrust_n";
	public final static String BURN_TIME = "burn_time_s";
	public final static String LENGTH = "length";
	public final static String CASE_INFO = "case_info";
	public final static String MANUFACTURER = "manufacturer";
	public final static String TYPE = "type";
	public final static String IMPULSE_CLASS = "impulse_class";
	public final static String THRUST_DATA = "thrust_data";
	public final static String TIME_DATA = "time_data";
	public final static String CG_DATA = "cg_data";
	
	private final static String[] ALL_COLS = new String[] {
			ID,
			DIGEST,
			DESIGNATION,
			DELAYS,
			DIAMETER,
			TOTAL_IMPULSE,
			AVG_THRUST,
			MAX_THRUST,
			BURN_TIME,
			LENGTH,
			CASE_INFO,
			TYPE,
			IMPULSE_CLASS,
			MANUFACTURER,
			THRUST_DATA,
			TIME_DATA,
			CG_DATA
	};
	
	public long insertOrUpdateMotor(ExtendedThrustCurveMotor mi) throws Exception {
		ContentValues initialValues = new ContentValues();
		initialValues.put(ID, mi.getId());
		initialValues.put(UNIQUE_NAME, mi.getManufacturer() + mi.getDesignation());
		initialValues.put(DIGEST, mi.getDigest());
		initialValues.put(DESIGNATION, mi.getDesignation());
		initialValues.put(DELAYS, ConversionUtils.delaysToString(mi.getStandardDelays()));
		initialValues.put(DIAMETER, mi.getDiameter());
		initialValues.put(TOTAL_IMPULSE, mi.getTotalImpulseEstimate());
		initialValues.put(AVG_THRUST, mi.getAverageThrustEstimate());
		initialValues.put(MAX_THRUST, mi.getMaxThrustEstimate());
		initialValues.put(BURN_TIME, mi.getBurnTimeEstimate());
		initialValues.put(LENGTH, mi.getLength());
		initialValues.put(CASE_INFO, mi.getCaseInfo());
		initialValues.put(TYPE, mi.getMotorType().name());
		initialValues.put(IMPULSE_CLASS, mi.getImpulseClass());
		initialValues.put(MANUFACTURER, mi.getManufacturer().getSimpleName());
		initialValues.put(THRUST_DATA, ConversionUtils.serializeArrayOfDouble(mi.getThrustPoints()));
		initialValues.put(TIME_DATA, ConversionUtils.serializeArrayOfDouble(mi.getTimePoints()));
		initialValues.put(CG_DATA, ConversionUtils.serializeArrayOfCoordinate(mi.getCGPoints()));
		
		AndroidLogWrapper.d(MotorDao.class, "insertOrUpdate Motor");
		long rv = mDb.insertWithOnConflict(DATABASE_TABLE, null, initialValues, SQLiteDatabase.CONFLICT_REPLACE);
		return rv;
	}
	
	/**
	 * Delete the motor and burn data with the given rowId
	 * 
	 * @param name name of motor to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteMotor(Long id) {
		
		boolean rv = mDb.delete(DATABASE_TABLE, ID + "=" + id, null) > 0;
		return rv;
	}
	
	/**
	 * 
	 * @param groupCol
	 * @param groupVal
	 * @return
	 */
	public Cursor fetchAllInGroups(String groupCol, String groupVal) {
		return mDb.query(DATABASE_TABLE,
				/* columns */ALL_COLS,
				/* selection */groupCol + "=?",
				/* selection args*/new String[] { groupVal },
				/* groupby */null,
				/* having*/null,
				/* orderby*/TOTAL_IMPULSE);
		
	}
	
	/**
	 * Fetch the groups based on groupCol
	 * @param groupCol
	 * @return
	 */
	public Cursor fetchGroups(String groupCol) {
		return mDb.query(true, DATABASE_TABLE,
				/* columns */new String[] {
				groupCol
				},
				/* selection */null,
				/* selection args*/null,
				/* groupby */null,
				/* having*/null,
				/* orderby*/groupCol,
				/* limit*/null);
		
	}
	
	/**
	 * Return a Cursor over the list of all motors
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllMotors() {
		
		return mDb.query(DATABASE_TABLE,
				/* columns */ALL_COLS,
				/* selection */null,
				/* selection args*/null,
				/* groupby */null,
				/* having*/null,
				/* orderby*/null);
	}
	
	private ExtendedThrustCurveMotor hydrateMotor(Cursor mCursor) throws Exception {
		ExtendedThrustCurveMotor mi;
		{
			String digest = mCursor.getString(mCursor.getColumnIndex(DIGEST));
			String designation = mCursor.getString(mCursor.getColumnIndex(DESIGNATION));
			String delayString = mCursor.getString(mCursor.getColumnIndex(DELAYS));
			double[] delays = ConversionUtils.stringToDelays(delayString);
			double diameter = mCursor.getDouble(mCursor.getColumnIndex(DIAMETER));
			double totImpulse = mCursor.getDouble(mCursor.getColumnIndex(TOTAL_IMPULSE));
			double avgImpulse = mCursor.getDouble(mCursor.getColumnIndex(AVG_THRUST));
			double maxThrust = mCursor.getDouble(mCursor.getColumnIndex(MAX_THRUST));
			double length = mCursor.getDouble(mCursor.getColumnIndex(LENGTH));
			Motor.Type type;
			try {
				type = Enum.valueOf(Motor.Type.class, mCursor.getString(mCursor.getColumnIndex(TYPE)));
			} catch (IllegalArgumentException e) {
				type = Motor.Type.UNKNOWN;
			}
			Manufacturer manufacturer = Manufacturer.getManufacturer(mCursor.getString(mCursor.getColumnIndex(MANUFACTURER)));
			double[] thrustData = ConversionUtils.deserializeArrayOfDouble(mCursor.getBlob(mCursor.getColumnIndex(THRUST_DATA)));
			double[] timeData = ConversionUtils.deserializeArrayOfDouble(mCursor.getBlob(mCursor.getColumnIndex(TIME_DATA)));
			Coordinate[] cgData = ConversionUtils.deserializeArrayOfCoordinate(mCursor.getBlob(mCursor.getColumnIndex(CG_DATA)));
			
			ThrustCurveMotor tcm = new ThrustCurveMotor(manufacturer,
					designation,
					"",
					type,
					delays,
					diameter,
					length,
					timeData,
					thrustData,
					cgData,
					digest
					);
			mi = new ExtendedThrustCurveMotor(tcm);
			
			mi.setId(mCursor.getLong(mCursor.getColumnIndex(ID)));
			mi.setCaseInfo(mCursor.getString(mCursor.getColumnIndex(CASE_INFO)));
			mi.setImpulseClass(mCursor.getString(mCursor.getColumnIndex(IMPULSE_CLASS)));
			
		}
		return mi;
		
	}
	
	public ExtendedThrustCurveMotor fetchMotor(Long id) throws Exception {
		Cursor mCursor = mDb.query(DATABASE_TABLE,
				/* columns */ALL_COLS,
				/* selection */ID + "=" + id,
				/* selection args*/null,
				/* groupby */null,
				/* having*/null,
				/* orderby*/null);
		if (mCursor == null) {
			return null;
		}
		try {
			if (mCursor.getCount() == 0) {
				return null;
			}
			mCursor.moveToFirst();
			return hydrateMotor(mCursor);
		} finally {
			mCursor.close();
		}
		
	}
	
	public ExtendedThrustCurveMotor fetchMotor(String manufacturerShortName, String designation) throws Exception {
		Cursor mCursor = mDb.query(DATABASE_TABLE,
				/* columns */ALL_COLS,
				/* selection */MANUFACTURER + "='" + manufacturerShortName + "' and " + DESIGNATION + "='" + designation + "'",
				/* selection args*/null,
				/* groupby */null,
				/* having*/null,
				/* orderby*/null);
		if (mCursor == null) {
			return null;
		}
		try {
			if (mCursor.getCount() == 0) {
				return null;
			}
			mCursor.moveToFirst();
			return hydrateMotor(mCursor);
		} catch( Exception ex ) {
			LogHelper.getInstance().debug("whoa!", ex);
			throw ex;
		} finally {
			mCursor.close();
		}
		
	}
	
}
