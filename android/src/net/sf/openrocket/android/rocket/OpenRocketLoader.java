package net.sf.openrocket.android.rocket;

import java.io.File;

import net.sf.openrocket.R;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.document.OpenRocketDocument;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class OpenRocketLoader extends FragmentActivity {
	private static final String TAG = "OpenRocketLoader";

	private ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Intent i = getIntent();
		Uri file = i.getData();
		loadOrkFile(file);
	}

	@Override
	protected void onDestroy() {
		if ( progress != null ) {
			if ( progress.isShowing() ) {
				progress.dismiss();
			}
			progress = null;
		}
		super.onDestroy();
	}

	private void loadOrkFile( Uri file ) {
		Log.d(TAG,"Use ork file: " + file);
		String path = file.getPath();
		File orkFile = new File(path);
		progress = ProgressDialog.show(this, "Loading file", "");

		final OpenRocketLoaderTask task = new OpenRocketLoaderTask() {

			/* (non-Javadoc)
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(OpenRocketDocument result) {
				super.onPostExecute(result);
				((Application)OpenRocketLoader.this.getApplication()).setRocketDocument( result );
				Log.d(TAG,"Finished loading " + OpenRocketLoader.this);
				finishedLoading();
			}

		};

		task.execute(orkFile);

	}
	
	private void finishedLoading() {
		if ( progress.isShowing() ) {
			progress.dismiss();
		}
		
		Intent i = new Intent(this,OpenRocketViewer.class);
		startActivity(i);
		finish();
	}


}
