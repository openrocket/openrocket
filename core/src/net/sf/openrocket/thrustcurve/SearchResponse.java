package net.sf.openrocket.thrustcurve;

import java.util.ArrayList;
import java.util.List;


public class SearchResponse {
	
	private final List<TCMotor> results = new ArrayList<TCMotor>();
	
	private int matches;
	
	private String error;
	
	public List<TCMotor> getResults() {
		return results;
	}
	
	void addMotor(TCMotor motor) {
		results.add(motor);
	}
	
	public int getMatches() {
		return matches;
	}
	
	public void setMatches(int matches) {
		this.matches = matches;
	}
	
	public String getError() {
		return error;
	}
	
	public void setError(String error) {
		this.error = error;
	}
	
	@Override
	public String toString() {
		return "SearchResult [results=" + results + ", matches=" + matches
				+ ", error=" + error + "]";
	}
	
}
