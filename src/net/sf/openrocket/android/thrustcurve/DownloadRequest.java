package net.sf.openrocket.android.thrustcurve;

import java.util.ArrayList;

class DownloadRequest {

	private ArrayList<Integer> motorIds = new ArrayList<Integer>();
	
	private String format = null;
	
	public void add( Integer motorId ) {
		this.motorIds.add(motorId);
	}
	
	public void setFormat( String format ) {
		this.format = format;
	}
	
	@Override
	public String toString() {
		StringBuilder w = new StringBuilder();
		
        w.append("<?xml version=\"1.0\" encoding=\"ascii\"?>\n");
        w.append("<download-request\n");
        w.append(" xmlns=\"http://www.thrustcurve.org/2008/DownloadRequest\"\n");
        w.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        w.append(" xsi:schemaLocation=\"http://www.thrustcurve.org/2008/DownloadRequest http://www.thrustcurve.org/2008/download-request.xsd\">\n");

        if ( format != null ) {
        	w.append("  <format>").append(format).append("</format>\n");
        }
        
        w.append("  <motor-ids>\n");
          for( Integer i : motorIds ) {
        	  w.append("      <id>").append(i).append("</id>\n");
          }
        w.append("  </motor-ids>\n");
        w.append("</download-request>\n");
        return w.toString();
	}

	
}
