package net.sf.openrocket.android.thrustcurve;

import java.util.List;

import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.motor.ThrustCurveMotor;

public class TCSearchAction extends TCQueryAction {

	public static TCSearchAction newInstance( SearchRequest searchRequest ) {
		TCSearchAction frag = new TCSearchAction();
		frag.task = frag.new Downloader(searchRequest);
		return frag;
	}

	private class Downloader extends TCQueryAction.TCQueryTask {

		private SearchRequest searchRequest;
		
		private Downloader( SearchRequest searchRequest ) {
			this.searchRequest = searchRequest;
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				handler.post( new UpdateMessage("Quering Thrustcurve"));
				SearchResponse res = ThrustCurveAPI.doSearch(searchRequest);

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

					AndroidLogWrapper.d(TCQueryAction.class, mi.toString());

					List<MotorBurnFile> b = ThrustCurveAPI.downloadData(mi.getMotor_id());
					List<ThrustCurveMotor> motors = ThrustCurveAPI.extractAllMotors(b);
					if ( motors != null && motors.size() > 0 ) {
						for( ThrustCurveMotor motor : motors ) {
							writeMotor( mi, motor);
						}
					}
				}
				if ( total < res.getMatches() ) {
					dismiss();
					return "" + total + " motors downloaded, " + res.getMatches() + " matched.  Try restricting the query more.";
				} else {
					dismiss();
					return null;
				}
			}
			catch( Exception ex){
				AndroidLogWrapper.d(TCSearchAction.class,ex.toString());
				return ex.toString();
			}
		}
	}

}
