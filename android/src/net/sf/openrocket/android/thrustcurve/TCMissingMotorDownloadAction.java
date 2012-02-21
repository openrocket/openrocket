package net.sf.openrocket.android.thrustcurve;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.motor.ThrustCurveMotorPlaceholder;

public class TCMissingMotorDownloadAction extends TCQueryAction {

	private final static String DESIGNATION_REGEX_STRING = "(Micro Maxx|Micro Maxx II|1/4A|1/2A|[A-O][0-9]*)";
	
	public static TCMissingMotorDownloadAction newInstance( Set<ThrustCurveMotorPlaceholder> missingMotors ) {
		TCMissingMotorDownloadAction frag = new TCMissingMotorDownloadAction();
		frag.task = frag.new Downloader(missingMotors);
		return frag;
	}

	private class Downloader extends TCQueryAction.TCQueryTask {

		private Set<ThrustCurveMotorPlaceholder> missingMotors;
		private Pattern designation_pattern = null;
		
		private Downloader( Set<ThrustCurveMotorPlaceholder> missingMotors ) {
			this.missingMotors = missingMotors;
			try {
				designation_pattern = Pattern.compile(DESIGNATION_REGEX_STRING);
			} catch ( Exception ex ) {
				AndroidLogWrapper.e(TCMissingMotorDownloadAction.class, "Exception in pattern compile {}", ex);
			}
		}
		
		private void downloadMissingMotor( ThrustCurveMotorPlaceholder motor ) {
			try {
					
				SearchRequest request = new SearchRequest();
				request.setManufacturer(motor.getManufacturer());
				String designation = motor.getDesignation();
				if ( designation_pattern != null ) {
					Matcher m = designation_pattern.matcher(designation);
					if ( m.find() ) {
						designation = m.group();
					}
				}
				AndroidLogWrapper.d(TCMissingMotorDownloadAction.class, "using designation {}", designation);
				request.setCommon_name(designation);

				handler.post( new UpdateMessage("Looking for " + motor.getManufacturer() + " " + motor.getDesignation()));

				SearchResponse res = ThrustCurveAPI.doSearch(request);

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

					List<MotorBurnFile> listOfMotors = ThrustCurveAPI.downloadData(mi.getMotor_id());

					ThrustCurveMotor bestMatch = ThrustCurveAPI.findBestMatch(motor, listOfMotors);
					writeMotor( mi, bestMatch);

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
			return null;
		}

	}

}
