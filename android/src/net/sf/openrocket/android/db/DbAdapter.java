package net.sf.openrocket.android.db;

import net.sf.openrocket.android.util.AndroidLogWrapper;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbAdapter {

    private static final String TAG = "DbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "openrocket";
    private static final int DATABASE_VERSION = 3;

    private final Context mCtx;

    private MotorDao motorDao;
    
    public MotorDao getMotorDao() {
		return motorDao;
	}
    
	private class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	executeSQL( db, MotorDao.create());
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            AndroidLogWrapper.w(DbAdapter.class, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            executeSQL(db, MotorDao.update(oldVersion, newVersion));
        }

        private void executeSQL( SQLiteDatabase db, String[] sqls ) {
        	for(String s: sqls ) {
        		db.execSQL(s);
        	}
        }

    }
 
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        motorDao = new MotorDao(mDb);
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

}