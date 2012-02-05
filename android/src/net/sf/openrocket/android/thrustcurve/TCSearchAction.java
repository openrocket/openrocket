package net.sf.openrocket.android.thrustcurve;

import net.sf.openrocket.android.motor.ExtendedThrustCurveMotor;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import android.app.Activity;

public class TCSearchAction extends TCQueryAction {

	private SearchRequest request;
	
	public TCSearchAction(Activity parent) {
		super(parent);
	}
	
	public void setRequest( SearchRequest request ) {
		this.request = request;
	}

	protected Runnable getTask() {
		return new Downloader();
	}
	
	private class Downloader implements Runnable {

		@Override
		public void run() {
			try {
				handler.post( new UpdateMessage("Quering Thrustcurve"));
				SearchResponse res = new ThrustCurveAPI().doSearch(request);

				int total = res.getResults().size();
				int count = 1;
				for( TCMotor mi : res.getResults() ) {
					StringBuilder message = new StringBuilder();
					message.append("Downloading details ");
					if ( total > 1 ) {
						message.append(count);
						message.append(" of " );
						message.append(total);
						message.append("\n");
					}
					message.append(mi.getManufacturer());
					message.append(" ");
					message.append(mi.getCommon_name());
					handler.post(new UpdateMessage(message.toString()));
					count++;
					if ( mi.getData_files() == null || mi.getData_files().intValue() == 0 ) {
						continue;
					}

					MotorBurnFile b = new ThrustCurveAPI().downloadData(mi.getMotor_id());

					AndroidLogWrapper.d(TCQueryAction.class, mi.toString());

					ExtendedThrustCurveMotor m = new ExtendedThrustCurveMotor();

					m.setThrustCurveMotor( b.getThrustCurveMotor() );

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

					// Convert Case Info.
					if ( mi.getCase_info() == null
							|| "single use".equalsIgnoreCase(mi.getCase_info())
							|| "single-use".equalsIgnoreCase(mi.getCase_info())) {
						m.setCaseInfo(mi.getType()+ " " + mi.getDiameter() + "x" + mi.getLength());
					} else {
						m.setCaseInfo(mi.getCase_info());
					}

					AndroidLogWrapper.d(TCQueryAction.class,"adding motor " + m.toString());
					// Write motor.
					mDbHelper.getMotorDao().insertOrUpdateMotor(m);
				}
				if ( total < res.getMatches() ) {
					handler.post( new Error( total + " motors downloaded, " + res.getMatches() + " matched.  Try restricting the query more.") );
				} else {
					handler.post( new Dismiss());
				}
			}
			catch( Exception ex){
				AndroidLogWrapper.d(TCQueryAction.class,ex.toString());
				handler.post( new Error(ex.toString()) );
			}

		}
	}

}
