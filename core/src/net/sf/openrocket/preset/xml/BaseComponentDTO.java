package net.sf.openrocket.preset.xml;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.unit.UnitGroup;

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
	private AnnotatedMassDTO mass;
	@XmlElement(name = "Filled")
	private Boolean filled;
	@XmlInlineBinaryData
	@XmlJavaTypeAdapter(Base64Adapter.class)
	@XmlElement(name = "Thumbnail")
	private byte[] image;
	
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
		if (preset.has(ComponentPreset.DESCRIPTION)) {
			setDescription(preset.get(ComponentPreset.DESCRIPTION));
		}
		if (preset.has(ComponentPreset.MATERIAL)) {
			setMaterial(new AnnotatedMaterialDTO(preset.get(ComponentPreset.MATERIAL)));
		}
		if (preset.has(ComponentPreset.MASS)) {
			setMass(preset.get(ComponentPreset.MASS));
		}
		if (preset.has(ComponentPreset.FILLED)) {
			setFilled(preset.get(ComponentPreset.FILLED));
		}
		if (preset.has(ComponentPreset.IMAGE)) {
			setImageData(preset.get(ComponentPreset.IMAGE));
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
		return mass.getValue();
	}
	
	public void setMass(final AnnotatedMassDTO theMass) {
		mass = theMass;
	}
	
	public void setMass(final double theMass) {
		mass = new AnnotatedMassDTO(theMass);
	}
	
	public Boolean getFilled() {
		return filled;
	}
	
	public void setFilled(Boolean filled) {
		this.filled = filled;
	}
	
	public byte[] getImageData() {
		return image;
	}
	
	public void setImageData(final byte[] theImage) {
		image = theImage;
	}
	
	public BufferedImage getImage() throws IOException {
		if (image != null) {
			return ImageIO.read(new ByteArrayInputStream(image));
		}
		return null;
	}
	
	public void setImage(BufferedImage theImage) throws IOException {
		if (theImage != null) {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(theImage, "png", byteArrayOutputStream);
			image = byteArrayOutputStream.toByteArray();
		}
	}
	
	public abstract ComponentPreset asComponentPreset(List<MaterialDTO> materials) throws InvalidComponentPresetException;
	
	void addProps(TypedPropertyMap props, List<MaterialDTO> materialList) {
		props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(manufacturer));
		props.put(ComponentPreset.PARTNO, partNo);
		if (description != null) {
			props.put(ComponentPreset.DESCRIPTION, description);
		}
		Material m = find(materialList, material);
		if (m != null) {
			props.put(ComponentPreset.MATERIAL, find(materialList, material));
		}
		if (mass != null) {
			props.put(ComponentPreset.MASS, getMass());
		}
		if (filled != null) {
			props.put(ComponentPreset.FILLED, getFilled());
		}
		if (image != null) {
			props.put(ComponentPreset.IMAGE, image);
		}
	}
	
	protected Material find(List<MaterialDTO> materialList, AnnotatedMaterialDTO dto) {
		if (dto == null) {
			return null;
		}
		for (int i = 0; i < materialList.size(); i++) {
			MaterialDTO materialDTO = materialList.get(i);
			if (materialDTO.getType().name().equals(dto.type) && materialDTO.getName().equals(dto.material)) {
				return materialDTO.asMaterial();
			}
		}
		
		// Don't have one, first check OR's database
		Material m = Databases.findMaterial(dto.getORMaterialType(), dto.material);
		if (m != null) {
			return m;
		}
		
		return Databases.findMaterial(dto.getORMaterialType(), dto.material, 0.0);
		
	}
	
	static class AnnotatedMaterialDTO {
		@XmlAttribute(name = "Type")
		private String type;
		@XmlValue
		private String material;
		
		AnnotatedMaterialDTO() {
		}
		
		AnnotatedMaterialDTO(Material theMaterial) {
			type = theMaterial.getType().name();
			material = theMaterial.getName();
		}
		
		public Material.Type getORMaterialType() {
			if ("BULK".equals(type)) {
				return Material.Type.BULK;
			} else if ("SURFACE".equals(type)) {
				return Material.Type.SURFACE;
			} else if ("LINE".equals(type)) {
				return Material.Type.LINE;
			}
			throw new IllegalArgumentException("Inavlid material type " + type + " specified for Component");
		}
	}
	
	static class AnnotatedLengthDTO {
		@XmlAttribute(name = "Unit", required = false)
		private String unitName = "m";
		@XmlValue
		private double length;
		
		AnnotatedLengthDTO() {
		}
		
		AnnotatedLengthDTO(double length) {
			this.length = length;
		}
		
		public double getValue() {
			return UnitGroup.UNITS_LENGTH.getUnit(unitName).fromUnit(length);
		}
	}
	
	static class AnnotatedMassDTO {
		@XmlAttribute(name = "Unit", required = false)
		private String unitName = "kg";
		@XmlValue
		private double mass;
		
		AnnotatedMassDTO() {
		}
		
		AnnotatedMassDTO(double mass) {
			this.mass = mass;
		}
		
		public double getValue() {
			return UnitGroup.UNITS_MASS.getUnit(unitName).fromUnit(mass);
		}
	}
	
	static class Base64Adapter extends XmlAdapter<String, byte[]> {
		@Override
		public byte[] unmarshal(String s) {
			if (s == null) {
				return null;
			}
			return DatatypeConverter.parseBase64Binary(s);
		}
		
		@Override
		public String marshal(byte[] bytes) {
			if (bytes == null) {
				return null;
			}
			return DatatypeConverter.printBase64Binary(bytes);
		}
	}
}
