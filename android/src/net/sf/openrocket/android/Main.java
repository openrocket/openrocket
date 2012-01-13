package net.sf.openrocket.android;

import net.sf.openrocket.R;
import net.sf.openrocket.android.filebrowser.SimpleFileBrowser;
import net.sf.openrocket.android.motor.MotorHierarchicalBrowser;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class Main extends Activity {

	private static final int PICK_ORK_FILE_RESULT = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch ( requestCode ) {
		case PICK_ORK_FILE_RESULT:
			if(resultCode==RESULT_OK){
				Uri file = data.getData();
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(file);
				startActivity(intent);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void pickOrkFiles( View v ) {
		try {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("file/*");
			startActivityForResult(intent,PICK_ORK_FILE_RESULT);
		} catch ( ActivityNotFoundException ex ) { 
			// No activity for ACTION_GET_CONTENT  use internal file browser
			Intent intent = new Intent(Main.this, SimpleFileBrowser.class);
			startActivityForResult(intent,PICK_ORK_FILE_RESULT);
		}
	}

	public void browseMotors( View v ) {
		Intent i = new Intent(Main.this, MotorHierarchicalBrowser.class);
		startActivity(i);
	}

}
