package net.sf.openrocket.android.thrustcurve;

import java.util.ArrayList;
import java.util.List;


public class SearchResponse {

	private List<TCMotor> results = new ArrayList<TCMotor>();
	
	private int matches;
	
	private String error;

	public List<TCMotor> getResults() {
		return results;
	}

	public void setResults(List<TCMotor> results) {
		this.results = results;
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
