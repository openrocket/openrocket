package net.sf.openrocket.android.thrustcurve;

import java.util.Set;

import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.motor.ThrustCurveMotorPlaceholder;

public class TCMissingMotorDownloadAction extends TCQueryAction {

	public static TCMissingMotorDownloadAction newInstance( Set<ThrustCurveMotorPlaceholder> missingMotors ) {
		TCMissingMotorDownloadAction frag = new TCMissingMotorDownloadAction();
		frag.task = frag.new Downloader(missingMotors);
		return frag;
	}

	private class Downloader extends TCQueryAction.TCQueryTask {

		private Set<ThrustCurveMotorPlaceholder> missingMotors;
		
		private Downloader( Set<ThrustCurveMotorPlaceholder> missingMotors ) {
			this.missingMotors = missingMotors;
		}
		
		private void downloadMissingMotor( ThrustCurveMotorPlaceholder motor ) {
			try {
				SearchRequest request = new SearchRequest();
				request.setManufacturer(motor.getManufacturer());
				request.setDesignation(motor.getDesignation());

				handler.post( new UpdateMessage("Looking for " + motor.getManufacturer() + " " + motor.getDesignation()));

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

					AndroidLogWrapper.d(TCQueryAction.class, mi.toString());

					MotorBurnFile b = new ThrustCurveAPI().downloadData(mi.getMotor_id());

					writeMotor( mi, b);

				}
			}
			catch( Exception ex){
				AndroidLogWrapper.d(TCQueryAction.class,ex.toString());
				handler.post( new UpdateMessage("Failed") );

			}

		}

		@Override
		protected String doInBackground(Void... arg0) {
			for ( ThrustCurveMotorPlaceholder motor : missingMotors ) {
				AndroidLogWrapper.d(TCMissingMotorDownloadAction.class, "Motor: {}", motor);
				downloadMissingMotor(motor);
			}
			dismiss();
			return null;
		}

	}

}
