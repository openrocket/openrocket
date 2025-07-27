package info.openrocket.core.thrustcurve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadResponse {

	private final Map<Integer, List<MotorBurnFile>> data = new HashMap<>();

	private String error = null;

	public void add(MotorBurnFile mbd) {
		List<MotorBurnFile> currentData = data.computeIfAbsent(mbd.getMotorId(), k -> new ArrayList<>());
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
