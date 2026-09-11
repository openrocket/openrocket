package info.openrocket.core.preset.xml;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import info.openrocket.core.database.Databases;
import info.openrocket.core.material.Material;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.Chars;

/**
 * XML handler for materials.
 */
@JacksonXmlRootElement(localName = "Material")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaterialDTO {

	@JacksonXmlProperty(localName = "Name")
	private String name;
	@JacksonXmlProperty(localName = "Density")
	private double density;
	@JacksonXmlProperty(localName = "Type")
	private MaterialTypeDTO type;
	@JsonIgnore
	private String uom;
	@JacksonXmlProperty(localName = "ShearModulus")
	private Double inPlaneShearModulus;
	@JacksonXmlProperty(localName = "Group")
	private MaterialGroupDTO group;

	/**
	 * Default constructor.
	 */
	public MaterialDTO() {
	}

	public MaterialDTO(final Material theMaterial) {
		this(theMaterial.getName(), theMaterial.getDensity(), theMaterial.getInPlaneShearModulus(),
				MaterialTypeDTO.asDTO(theMaterial.getType()),
				theMaterial.getType().getUnitGroup().getDefaultUnit().toString(),
				MaterialGroupDTO.asDTO(theMaterial.getGroup()));
	}

	public MaterialDTO(final String theName, final double theDensity, final MaterialTypeDTO theType,
			final String theUom, final MaterialGroupDTO theGroup) {
		this(theName, theDensity, null, theType, theUom, theGroup);
	}

	public MaterialDTO(final String theName, final double theDensity, final Double theInPlaneShearModulus,
			final MaterialTypeDTO theType, final String theUom, final MaterialGroupDTO theGroup) {
		name = theName;
		density = theDensity;
		inPlaneShearModulus = theInPlaneShearModulus;
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

	@JsonGetter("UnitsOfMeasure")
	@JacksonXmlProperty(isAttribute = true, localName = "UnitsOfMeasure")
	public String getSerializedUom() {
		if (uom == null) return null;
		return uom.replace(Chars.SQUARED, '2').replace(Chars.CUBED, '3');
	}

	@JsonSetter("UnitsOfMeasure")
	public void setUom(final String theUom) {
		if (theUom == null) {
			uom = null;
		} else {
			uom = theUom.replace('2', Chars.SQUARED).replace('3', Chars.CUBED);
		}
	}

	public Double getInPlaneShearModulus() {
		return inPlaneShearModulus;
	}

	public void setInPlaneShearModulus(final Double theInPlaneShearModulus) {
		inPlaneShearModulus = theInPlaneShearModulus;
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
		// Convert density from stored UOM units to SI units
		double siDensity = density;
		if (uom != null && type != null) {
			Unit uomUnit = type.getORMaterialType().getUnitGroup().getUnit(uom);
			if (uomUnit != null) {
				siDensity = uomUnit.fromUnit(density);
			}
		}
		if (inPlaneShearModulus == null) {
			return Databases.findMaterial(type.getORMaterialType(), name, siDensity, group.getORMaterialGroup());
		}
		return Databases.findMaterial(type.getORMaterialType(), name, siDensity, inPlaneShearModulus,
				group.getORMaterialGroup());
	}

}
