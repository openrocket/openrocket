package net.sf.openrocket.android.rocket;

import java.io.File;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.openrocket.OpenRocketLoader;
import android.os.AsyncTask;
import android.util.Log;

public class OpenRocketLoaderTask extends AsyncTask<File, Void, OpenRocketDocument> {

	private final static String TAG = "OpenRocketLoaderTask";
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected OpenRocketDocument doInBackground(File... arg0) {
		Log.d(TAG,"doInBackgroud");
		
		OpenRocketLoader rocketLoader = new OpenRocketLoader();
		try {
			OpenRocketDocument rocket = rocketLoader.load(arg0[0]);
			return rocket;
		}
		catch( RocketLoadException ex ) {
			Log.e(TAG, "doInBackground rocketLaoder.load threw", ex);
		}
		return null;
		
	}

}
