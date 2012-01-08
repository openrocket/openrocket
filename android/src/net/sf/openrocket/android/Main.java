package net.sf.openrocket.android;

import net.sf.openrocket.R;
import net.sf.openrocket.android.motor.MotorHierarchicalBrowser;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

public class Main extends Activity {

	private static final int PICK_ORK_FILE_RESULT = 1;

	private static final int STOPSPLASH = 0;
	//time in milliseconds
	private static final long SPLASHTIME = 3000;

	private ImageView splash;

	//handler for splash screen
	private Handler splashHandler = new Handler() {
		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STOPSPLASH:
				//remove SplashScreen from view
				splash.setVisibility(View.GONE);
				break;
			}
			super.handleMessage(msg);
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		splash = (ImageView) findViewById(R.id.splashscreen);
		Message msg = new Message();
		msg.what = STOPSPLASH;
		splashHandler.sendMessageDelayed(msg, SPLASHTIME);
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
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		startActivityForResult(intent,PICK_ORK_FILE_RESULT);
	}

	public void browseMotors( View v ) {
		Intent i = new Intent(Main.this, MotorHierarchicalBrowser.class);
		startActivity(i);
	}

}
