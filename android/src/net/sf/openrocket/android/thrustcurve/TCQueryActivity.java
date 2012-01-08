package net.sf.openrocket.android.thrustcurve;

import java.util.Vector;

import net.sf.openrocket.R;
import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.motor.Motor;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class TCQueryActivity extends Activity {

	private static final String TAG = "ThrustCurveQueryActivity";

	private DbAdapter mDbHelper;

	private ProgressDialog progress;
	private Thread downloadThread;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tcqueryform);

		mDbHelper = new DbAdapter(this);
		mDbHelper.open();

		final Spinner manufacturerField = (Spinner) findViewById(R.id.TCMotorSearchFormManufacturerField);
		final Spinner impulseField = (Spinner) findViewById(R.id.TCMotorSearchFormImpulseField);
		final Spinner diameterField = (Spinner) findViewById(R.id.TCMotorSearchFormDiameterField);
		final EditText commonNameField = (EditText) findViewById(R.id.TCMotorSearchFormCommonNameField);

		Button submitButton = (Button) findViewById(R.id.TCMotorSearchFromSubmitButton);
		submitButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick( View v ) {
						Log.d(TAG,"submit button clicked");

						String commonName = commonNameField.getText().toString();

						SearchRequest r = new SearchRequest();
						if ( manufacturerField.getSelectedItemPosition() != 0) {
							String m = (String) manufacturerField.getSelectedItem();
							Log.d(TAG,"manufacturer = " + m);
							r.setManufacturer(m);
						}
						if ( impulseField.getSelectedItemPosition() != 0  ) {
							String impulse = (String) impulseField.getSelectedItem();
							Log.d(TAG,"impulse = " + impulse);
							r.setImpulse_class(impulse);
						}
						if ( diameterField.getSelectedItemPosition() != 0 ) {
							String diameter = (String)diameterField.getSelectedItem();
							Log.d(TAG,"diameter = " + diameter);
							r.setDiameter(diameter);
						}
						r.setCommon_name(commonName);

						Downloader d = new Downloader(r);

						handler = new Handler();
						progress = ProgressDialog.show(TCQueryActivity.this, null, "");

						downloadThread = new Thread( d );
						downloadThread.start();
					}
				}
				);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return downloadThread;
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		if ( progress != null ) {
			if ( progress.isShowing() ) {
				progress.dismiss();
			}
			progress = null;
		}
		super.onDestroy();
	}

	private class UpdateMessage implements Runnable {
		private String newMessage;
		UpdateMessage( String message ) {
			this.newMessage = message;
		}
		@Override
		public void run() {
			progress.setMessage(newMessage);
		}
	}

	private class Dismiss implements Runnable {
		@Override
		public void run() {
			progress.dismiss();
			TCQueryActivity.this.finish();
		}
	}

	private class Error implements Runnable {
		private String newMessage;
		Error( String message ) {
			this.newMessage = message;
		}
		@Override
		public void run() {
			progress.dismiss();
			final AlertDialog dialog = new AlertDialog.Builder(TCQueryActivity.this).create();
			dialog.setMessage(newMessage);
			dialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Dismiss", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					dialog.dismiss();
				}

			});
			dialog.show();
		}
	}
	private class Downloader implements Runnable {

		SearchRequest request;

		Downloader( SearchRequest request ) {
			this.request = request;
		}

		@Override
		public void run() {
			try {
				handler.post( new UpdateMessage("Quering Thrustcurve"));
				SearchResponse res = new ThrustCurveAPI().doSearch(request);

				int total = res.getResults().size();
				int count = 1;
				for( TCMotor mi : res.getResults() ) {
					handler.post(new UpdateMessage("Downloading details " + count + " of " + total));
					count++;
					if ( mi.getData_files() == null || mi.getData_files().intValue() == 0 ) {
						continue;
					}

					MotorBurnFile b = new ThrustCurveAPI().downloadData(mi.getMotor_id());

					if ( b != null ) {
						if ( b.getLength() != null ) {
							mi.setLength( b.getLength() );
						}
						if ( b.getPropWeightG() != null ) {
							mi.setProp_mass_g(b.getPropWeightG());
						}
						if ( b.getTotWeightG() != null ) {
							mi.setTot_mass_g(b.getTotWeightG());
						}
						if ( b.getDelays() != null ) {
							mi.setDelays(b.getDelays());
						}
						mi.setBurndata(b.getDatapoints());
					}
					Log.d(TAG, mi.toString());

					// convert to Motors.  One per delay.
					Motor m = new Motor();
					// Base name of motor.
					String name = mi.getCommon_name() + "-";

					m.setManufacturer(mi.getManufacturer_abbr());
					// Convert impulse class.  ThrustCurve puts mmx, 1/4a and 1/2a as A.
					m.setImpulseClass(mi.getImpulse_class());
					if ( "a".equalsIgnoreCase(mi.getImpulse_class())) {
						if( mi.getCommon_name().startsWith("1/2A") ) {
							m.setImpulseClass("1/2A");
						} else if (mi.getCommon_name().startsWith("1/4A") ) {
							m.setImpulseClass("1/4A");
						} else if (mi.getCommon_name().startsWith("Micro") ) {
							m.setImpulseClass("1/8A");
						}
					}
					m.setAvgThrust(mi.getAvg_thrust_n());
					m.setBurndata(mi.getBurndata());
					m.setBurnTime(mi.getBurn_time_s());
					m.setDiameter(mi.getDiameter() == null ? null : mi.getDiameter().longValue());
					m.setLength(mi.getLength());
					m.setMaxThrust(mi.getMax_thrust_n());
					m.setPropMass(mi.getProp_mass_g());
					m.setTotalImpulse(mi.getTot_impulse_ns());
					m.setTotMass(mi.getTot_mass_g());
					// Convert Case Info.
					if ( mi.getCase_info() == null
							|| "single use".equalsIgnoreCase(mi.getCase_info())
							|| "single-use".equalsIgnoreCase(mi.getCase_info())) {
						m.setCaseInfo(mi.getType()+ " " + mi.getDiameter() + "x" + mi.getLength());
					} else {
						m.setCaseInfo(mi.getCase_info());
					}

					Vector<String> delays = new Vector<String>();
					{
						String delaysString = mi.getDelays();
						if ( delaysString != null ) {
							delaysString = delaysString.trim();
						}

						if ( delaysString == null || "".equals(delaysString)) {
							delays.add("");
						} else {
							String[] delayString = delaysString.split(",");
							for( String d : delayString )  {
								delays.add( d.trim() );
							}
						}
					}

					for( String d: delays ) {
						if ( "100".equals(d) ) {
							m.setName(name + "P");
						} else {
							m.setName(name + d);
						}
						mDbHelper.getMotorDao().insertOrUpdateMotor(m);
					}
				}
				if ( total < res.getMatches() ) {
					handler.post( new Error( total + " motors downloaded, " + res.getMatches() + " matched.  Try restricting the query more.") );
				} else {
					handler.post( new Dismiss());
				}
			}
			catch( Exception ex){
				Log.d(TAG,ex.toString());
				handler.post( new Error(ex.toString()) );
			}

		}
	}
}

