package net.sf.openrocket.preset.xml;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.util.Chars;

/**
 * XML handler for materials.
 */
@XmlRootElement(name = "Material")
@XmlAccessorType(XmlAccessType.FIELD)
public class MaterialDTO {
	
	@XmlElement(name = "Name")
	private String name;
	@XmlElement(name = "Density")
	private double density;
	@XmlElement(name = "Type")
	private MaterialTypeDTO type;
	@XmlAttribute(name = "UnitsOfMeasure")
	private String uom;
	
	/**
	 * Default constructor.
	 */
	public MaterialDTO() {
	}
	
	public MaterialDTO(final Material theMaterial) {
		this(theMaterial.getName(), theMaterial.getDensity(), MaterialTypeDTO.asDTO(theMaterial.getType()),
				theMaterial.getType().getUnitGroup().getDefaultUnit().toString());
	}
	
	public MaterialDTO(final String theName, final double theDensity, final MaterialTypeDTO theType, final String theUom) {
		name = theName;
		density = theDensity;
		type = theType;
		uom = theUom;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(final String theName) {
		name = theName;
	}
	
	public double getDensity() {
		return density;
	}
	
	public void setDensity(final double theDensity) {
		density = theDensity;
	}
	
	public MaterialTypeDTO getType() {
		return type;
	}
	
	public void setType(final MaterialTypeDTO theType) {
		type = theType;
	}
	
	public String getUom() {
		return uom;
	}
	
	public void setUom(final String theUom) {
		uom = theUom;
	}
	
	Material asMaterial() {
		return Databases.findMaterial(type.getORMaterialType(), name, density);
	}
	
	
	/**
	 * Special directive to the JAXB system.  After the object is parsed from xml,
	 * we replace the '2' with Chars.SQUARED, and '3' with Chars.CUBED.  Just the 
	 * opposite transformation as doen in beforeMarshal.
	 * @param unmarshaller
	 * @param parent
	 */
	@SuppressWarnings("unused")
	private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		if (uom != null) {
			uom = uom.replace('2', Chars.SQUARED);
			uom = uom.replace('3', Chars.CUBED);
		}
	}
	
	/**
	 * Special directive to the JAXB system.  Before the object is serialized into xml,
	 * we strip out the special unicode characters for cubed and squared so they appear
	 * as simple "3" and "2" chars.  The reverse transformation is done in afterUnmarshal.
	 * @param marshaller
	 */
	@SuppressWarnings("unused")
	private void beforeMarshal(Marshaller marshaller) {
		if (uom != null) {
			uom = uom.replace(Chars.SQUARED, '2');
			uom = uom.replace(Chars.CUBED, '3');
		}
	}
}
