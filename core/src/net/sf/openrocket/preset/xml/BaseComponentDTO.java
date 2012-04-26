
package net.sf.openrocket.preset.xml;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.TypedPropertyMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.List;

/**
 * Base class for the external representation of all component presets.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseComponentDTO {

	@XmlElement(name = "Manufacturer")
	private String manufacturer;
	@XmlElement(name = "PartNumber")
	private String partNo;
	@XmlElement(name = "Description")
	private String description;
	@XmlElement(name = "Material")
	private AnnotatedMaterialDTO material;
	@XmlElement(name = "Mass")
	private Double mass;
	@XmlElement(name="Filled")
	private Boolean filled;

	/**
	 * Default constructor.
	 */
	protected BaseComponentDTO() {
	}

	/**
	 * Constructor.
	 *
	 * @param preset  the preset to use to pull data values out of
	 *
	 * @throws net.sf.openrocket.util.BugException thrown if the expected body tube keys are not in the preset
	 */
	protected BaseComponentDTO(final ComponentPreset preset) {
		setManufacturer(preset.getManufacturer().getSimpleName());
		setPartNo(preset.getPartNo());
		if ( preset.has(ComponentPreset.DESCRIPTION )) {
			setDescription(preset.get(ComponentPreset.DESCRIPTION));
		}
		if ( preset.has(ComponentPreset.MATERIAL)) {
			setMaterial(new AnnotatedMaterialDTO(preset.get(ComponentPreset.MATERIAL)));
		}
		if (preset.has(ComponentPreset.MASS)) {
			setMass(preset.get(ComponentPreset.MASS));
		}
		if ( preset.has(ComponentPreset.FILLED) ) {
			setFilled( preset.get(ComponentPreset.FILLED));
		}
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(final String theManufacturer) {
		manufacturer = theManufacturer;
	}

	public String getPartNo() {
		return partNo;
	}

	public void setPartNo(final String thePartNo) {
		partNo = thePartNo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String theDescription) {
		description = theDescription;
	}

	public AnnotatedMaterialDTO getMaterial() {
		return material;
	}

	public void setMaterial(final AnnotatedMaterialDTO theMaterial) {
		material = theMaterial;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(final double theMass) {
		mass = theMass;
	}

	public Boolean getFilled() {
		return filled;
	}

	public void setFilled(Boolean filled) {
		this.filled = filled;
	}

	public abstract ComponentPreset asComponentPreset(List<MaterialDTO> materials) throws InvalidComponentPresetException;

	void addProps(TypedPropertyMap props, List<MaterialDTO> materialList) {
		props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(manufacturer));
		props.put(ComponentPreset.PARTNO, partNo);
		if ( description != null ) {
			props.put(ComponentPreset.DESCRIPTION, description);
		}
		Material m = find(materialList, material);
		if ( m != null ) {
			props.put(ComponentPreset.MATERIAL, find(materialList, material));
		}
		if ( mass != null ) {
			props.put(ComponentPreset.MASS, mass);
		}
		if ( filled != null ) {
			props.put(ComponentPreset.FILLED, filled);
		}
	}

	private Material find(List<MaterialDTO> materialList, AnnotatedMaterialDTO dto) {
		for (int i = 0; i < materialList.size(); i++) {
			MaterialDTO materialDTO =  materialList.get(i);
			if (materialDTO.getType().name().equals(dto.type) && materialDTO.getName().equals(dto.material)) {
				return materialDTO.asMaterial();
			}
		}
		//Otherwise fallback and look at factory default materials.
		return Databases.findMaterial(Material.Type.valueOf(material.type), material.material);
	}

	static class AnnotatedMaterialDTO {
		@XmlAttribute(name = "Type")
		private String type;
		@XmlValue
		private String material;

		AnnotatedMaterialDTO() {}

		AnnotatedMaterialDTO(Material theMaterial) {
			type = theMaterial.getType().name();
			material = theMaterial.getName();
		}
	}
}
