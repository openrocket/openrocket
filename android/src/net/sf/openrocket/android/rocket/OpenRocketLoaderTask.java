package net.sf.openrocket.android.rocket;

import java.io.File;

import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.DatabaseMotorFinderWithMissingMotors;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.openrocket.importt.OpenRocketLoader;
import android.app.Activity;
import android.os.AsyncTask;

public class OpenRocketLoaderTask extends AsyncTask<File, Void, OpenRocketLoaderResult> {
	
	private OpenRocketLoaderActivity parent;
	
	public OpenRocketLoaderTask( OpenRocketLoaderActivity parent ) {
		this.parent = parent;
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected OpenRocketLoaderResult doInBackground(File... arg0) {
		AndroidLogWrapper.d(OpenRocketLoaderTask.class, "doInBackgroud");
		
		OpenRocketLoader rocketLoader = new OpenRocketLoader();
		try {
			OpenRocketLoaderResult result = new OpenRocketLoaderResult();
			OpenRocketDocument rocket = rocketLoader.load(arg0[0], new DatabaseMotorFinderWithMissingMotors());
			result.rocket = rocket;
			result.warnings = result.warnings;
			return result;
		} catch (RocketLoadException ex) {
			AndroidLogWrapper.e(OpenRocketLoaderTask.class, "doInBackground rocketLaoder.load threw", ex);
		}
		return null;
		
	}

	@Override
	protected void onPostExecute(OpenRocketLoaderResult result) {
		super.onPostExecute(result);
		AndroidLogWrapper.d(OpenRocketLoaderActivity.class,"Finished loading " + OpenRocketLoaderTask.this);
		parent.finishedLoading(result);
	}

}
