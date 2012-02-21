package net.sf.openrocket.android.thrustcurve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DownloadResponse {

	private Map<Integer,List<MotorBurnFile>> data = new HashMap<Integer,List<MotorBurnFile>>();
	
	private String error = null;
	
	public void add( MotorBurnFile mbd ) {
		List<MotorBurnFile> currentData = data.get(mbd.getMotorId());
		if ( currentData == null ) {
			currentData = new ArrayList<MotorBurnFile>();
			data.put(mbd.getMotorId(), currentData);
		}
		currentData.add(mbd);
	}

	public List<MotorBurnFile> getData(Integer motor_id) {
		return data.get(motor_id);
	}
	
	public void setError(String error) {
		this.error = error;
	}
	
	public String getError() {
		return error;
	}
	
	@Override
	public String toString() {
		return "DownloadResponse [error=" + error + ", data=" + data + "]";
	}

}
