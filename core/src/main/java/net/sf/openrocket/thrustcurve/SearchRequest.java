package net.sf.openrocket.thrustcurve;

public class SearchRequest {
	
	private String manufacturer;
	private String designation;
	private String brand_name;
	
	private String common_name;
	private String impulse_class;
	private Integer diameter;
	
	/*
	public enum Type {
		"SU";
		"reload";
		"hybrid"
	};
	*/
	private String type;
	
	public void setManufacturer(String manufacturer) {
		this.manufacturer = null;
		if (manufacturer != null) {
			manufacturer = manufacturer.trim();
			if (!"".equals(manufacturer)) {
				this.manufacturer = manufacturer;
			}
		}
	}
	
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	
	public void setBrand_name(String brand_name) {
		this.brand_name = brand_name;
	}
	
	public void setCommon_name(String common_name) {
		if (common_name == null) {
			this.common_name = null;
			return;
		}
		this.common_name = common_name.trim();
		if ("".equals(this.common_name)) {
			this.common_name = null;
		}
	}
	
	public void setImpulse_class(String impulse_class) {
		this.impulse_class = null;
		if (impulse_class != null) {
			this.impulse_class = impulse_class.trim();
			if ("".equals(impulse_class)) {
				this.impulse_class = null;
			}
		}
	}
	
	public void setDiameter(Integer diameter) {
		this.diameter = diameter;
	}
	
	public void setDiameter(String diameter) {
		this.diameter = null;
		if (diameter == null) {
			return;
		}
		try {
			this.diameter = Integer.decode(diameter);
		} catch (NumberFormatException ex) {
			this.diameter = null;
		}
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		StringBuilder w = new StringBuilder();
		
		w.append("<?xml version=\"1.0\" encoding=\"ascii\"?>\n");
		w.append("<search-request\n");
		w.append(" xmlns=\"http://www.thrustcurve.org/2014/SearchRequest\"\n");
		w.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		w.append(" xsi:schemaLocation=\"http://www.thrustcurve.org/2014/SearchRequest http://www.thrustcurve.org/2014/search-request.xsd\">\n");
		
		if (manufacturer != null) {
			w.append("  <manufacturer>").append(manufacturer).append("</manufacturer>\n");
		}
		if (designation != null) {
			w.append("  <designation>").append(designation).append("</designation>\n");
		}
		if (brand_name != null) {
			w.append("  <brand-name>").append(brand_name).append("</brand-name>\n");
		}
		if (common_name != null) {
			w.append("  <common-name>").append(common_name).append("</common-name>\n");
		}
		if (impulse_class != null) {
			w.append("  <impulse-class>").append(impulse_class).append("</impulse-class>\n");
		}
		if (diameter != null) {
			w.append("  <diameter>").append(diameter).append("</diameter>\n");
		}
		if (type != null) {
			w.append("  <type>").append(type).append("</type>\n");
		}
		w.append("<data-fields>*</data-fields>");
        w.append("<max-results>0</max-results>");
		w.append("</search-request>\n");
		return w.toString();
	}
}
