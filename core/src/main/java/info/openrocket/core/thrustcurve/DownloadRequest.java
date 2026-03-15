package info.openrocket.core.thrustcurve;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class DownloadRequest {

	private static final Gson GSON = new Gson();
	private final ArrayList<String> motorIds = new ArrayList<>();

	private String format = null;

	public void add(String motorId) {
		this.motorIds.add(motorId);
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public String toString() {
		JsonObject json = new JsonObject();
		JsonArray ids = new JsonArray();
		for (String motorId : motorIds) {
			ids.add(motorId);
		}
		json.add("motorIds", ids);
		if (format != null) {
			json.addProperty("format", format);
		}
		return GSON.toJson(json);
	}

}
