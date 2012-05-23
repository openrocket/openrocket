package net.sf.openrocket.android;

import net.sf.openrocket.R;
import net.sf.openrocket.android.filebrowser.SimpleFileBrowser;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Main extends SherlockFragmentActivity {

	private static final int PICK_ORK_FILE_RESULT = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setTitle("");
		setContentView(R.layout.main);
		((Button) findViewById(R.id.main_open)).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Main.this.pickOrkFiles();
					}
				});
		((Button) findViewById(R.id.main_browse)).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ActivityHelpers.browseMotors(Main.this);
					}
				});
		((Button) findViewById(R.id.main_donate)).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ActivityHelpers.donate(Main.this);
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId() ) {
		case R.id.main_menu_preferences:
			ActivityHelpers.startPreferences(this);
			return true;
		case R.id.menu_about:
			ActivityHelpers.showAbout(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	private void pickOrkFiles( ) {
		Resources resources = this.getResources();
		String key = resources.getString(R.string.PreferenceUseInternalFileBrowserOption);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		boolean useinternalbrowser = pref.getBoolean(key, false);

		if ( useinternalbrowser ) {
			Intent intent = new Intent(Main.this, SimpleFileBrowser.class);
			startActivityForResult(intent,PICK_ORK_FILE_RESULT);
		} else {
			try {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("file/*");
				startActivityForResult(intent,PICK_ORK_FILE_RESULT);
			} catch ( ActivityNotFoundException ex ) { 
				// No activity for ACTION_GET_CONTENT  use internal file browser
				// update the preference value.
				pref.edit().putBoolean(key, false).commit();
				// fire our browser
				Intent intent = new Intent(Main.this, SimpleFileBrowser.class);
				startActivityForResult(intent,PICK_ORK_FILE_RESULT);
			}
		}		
	}
	public void pickOrkFiles( View v ) {
		pickOrkFiles();
	}

	public void browseMotors( View v ) {
		ActivityHelpers.browseMotors(this);
	}

}
