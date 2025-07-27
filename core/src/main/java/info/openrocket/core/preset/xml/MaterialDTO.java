package info.openrocket.core.preset.xml;

import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import info.openrocket.core.database.Databases;
import info.openrocket.core.material.Material;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.Chars;

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
	@XmlElement(name = "Group")
	private MaterialGroupDTO group;

	/**
	 * Default constructor.
	 */
	public MaterialDTO() {
	}

	public MaterialDTO(final Material theMaterial) {
		this(theMaterial.getName(), theMaterial.getDensity(), MaterialTypeDTO.asDTO(theMaterial.getType()),
				theMaterial.getType().getUnitGroup().getDefaultUnit().toString(),
				MaterialGroupDTO.asDTO(theMaterial.getGroup()));
	}

	public MaterialDTO(final String theName, final double theDensity, final MaterialTypeDTO theType,
			final String theUom, final MaterialGroupDTO theGroup) {
		name = theName;
		density = theDensity;
		type = theType;
		uom = theUom;
		group = theGroup;
		if (group == null) {
			group = MaterialGroupDTO.OTHER;
		}
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

	public MaterialGroupDTO getGroup() {
		return group;
	}

	public void setGroup(MaterialGroupDTO group) {
		this.group = group;
	}

	Material asMaterial() {
		if (group == null) {
			group = MaterialGroupDTO.OTHER;
		}
		return Databases.findMaterial(type.getORMaterialType(), name, density, group.getORMaterialGroup());
	}

	/**
	 * Special directive to the JAXB system. After the object is parsed from xml,
	 * we replace the '2' with Chars.SQUARED, and '3' with Chars.CUBED. Just the
	 * opposite transformation as done in beforeMarshal.
	 * 
	 * @param unmarshaller
	 * @param parent
	 */
	@SuppressWarnings("unused")
	private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		if (uom != null) {
			uom = uom.replace('2', Chars.SQUARED);
			uom = uom.replace('3', Chars.CUBED);
			if (type != null) {
				// The density value is stored in the XML file in the units of measure, but OR
				// expects the density to be
				// in SI units, so we need to convert it to SI units
				Unit uomUnit = type.getORMaterialType().getUnitGroup().getUnit(getUom());
				density = uomUnit.fromUnit(density);
			}
		}
	}

	/**
	 * Special directive to the JAXB system. Before the object is serialized into
	 * xml,
	 * we strip out the special unicode characters for cubed and squared so they
	 * appear
	 * as simple "3" and "2" chars. The reverse transformation is done in
	 * afterUnmarshal.
	 * 
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
