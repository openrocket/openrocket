package info.openrocket.core.preset.xml;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import info.openrocket.core.material.MaterialGroup;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import info.openrocket.core.database.Databases;
import info.openrocket.core.material.Material;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.InvalidComponentPresetException;
import info.openrocket.core.preset.TypedPropertyMap;
import info.openrocket.core.unit.UnitGroup;

/**
 * Base class for the external representation of all component presets.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
		@JsonSubTypes.Type(value = BodyTubeDTO.class, name = "BodyTube"),
		@JsonSubTypes.Type(value = TubeCouplerDTO.class, name = "TubeCoupler"),
		@JsonSubTypes.Type(value = NoseConeDTO.class, name = "NoseCone"),
		@JsonSubTypes.Type(value = TransitionDTO.class, name = "Transition"),
		@JsonSubTypes.Type(value = BulkHeadDTO.class, name = "BulkHead"),
		@JsonSubTypes.Type(value = CenteringRingDTO.class, name = "CenteringRing"),
		@JsonSubTypes.Type(value = EngineBlockDTO.class, name = "EngineBlock"),
		@JsonSubTypes.Type(value = LaunchLugDTO.class, name = "LaunchLug"),
		@JsonSubTypes.Type(value = RailButtonDTO.class, name = "RailButton"),
		@JsonSubTypes.Type(value = StreamerDTO.class, name = "Streamer"),
		@JsonSubTypes.Type(value = ParachuteDTO.class, name = "Parachute")})
public abstract class BaseComponentDTO {

	@JacksonXmlProperty(localName = "Manufacturer")
	private String manufacturer;
	@JacksonXmlProperty(localName = "PartNumber")
	private String partNo;
	@JacksonXmlProperty(localName = "Description")
	private String description;
	@JacksonXmlProperty(localName = "Material")
	private AnnotatedMaterialDTO material;
	@JacksonXmlProperty(localName = "Mass")
	private AnnotatedMassDTO mass;
	@JacksonXmlProperty(localName = "Filled")
	private Boolean filled;
	@JsonSerialize(using = Base64Serializer.class)
	@JsonDeserialize(using = Base64Deserializer.class)
	@JacksonXmlProperty(localName = "Thumbnail")
	private byte[] image;

	/**
	 * Default constructor.
	 */
	protected BaseComponentDTO() {
	}

	/**
	 * Constructor.
	 *
	 * @param preset the preset to use to pull data values out of
	 *
	 * @throws info.openrocket.core.util.BugException thrown if the expected body
	 *                                                tube keys are not in the
	 *                                                preset
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

	@com.fasterxml.jackson.annotation.JsonIgnore
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

	@com.fasterxml.jackson.annotation.JsonIgnore
	public BufferedImage getImage() throws IOException {
		if (image != null) {
			return ImageIO.read(new ByteArrayInputStream(image));
		}
		return null;
	}

	@com.fasterxml.jackson.annotation.JsonIgnore
	public void setImage(BufferedImage theImage) throws IOException {
		if (theImage != null) {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(theImage, "png", byteArrayOutputStream);
			image = byteArrayOutputStream.toByteArray();
		}
	}

	public abstract ComponentPreset asComponentPreset(Boolean legacy, List<MaterialDTO> materials)
			throws InvalidComponentPresetException;

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
		for (MaterialDTO materialDTO : materialList) {
			if (materialDTO.getType().name().equals(dto.type) && materialDTO.getName().equals(dto.material)) {
				return materialDTO.asMaterial();
			}
		}

		// Don't have one, first check OR's database
		Material m = Databases.findMaterial(dto.getORMaterialType(), dto.material);
		double materialDensity = 0.0;
		if (m != null) {
			materialDensity = m.getDensity();
			// If the material was found and has a group in ELASTICS/KEVLARS/NYLONS,
			// and the original group was ThreadsLines, use the found material's group
			if (dto.materialGroup != null && "ThreadsLines".equals(dto.materialGroup)) {
				MaterialGroup foundGroup = m.getGroup();
				if (foundGroup == MaterialGroup.ELASTICS || foundGroup == MaterialGroup.KEVLARS || foundGroup == MaterialGroup.NYLONS) {
					return m;
				}
			} else if (dto.materialGroup == null || !"ThreadsLines".equals(dto.materialGroup)) {
				// If not ThreadsLines, return the found material
				return m;
			}
		}

		// Use backward compatibility helper for the group
		MaterialGroup group = MaterialGroup.loadFromDatabaseStringWithBackwardCompatibility(
				dto.materialGroup, dto.getORMaterialType(), dto.material, materialDensity);
		return Databases.findMaterial(dto.getORMaterialType(), dto.material, 0.0, group);

	}

	static class AnnotatedMaterialDTO {
		@JacksonXmlProperty(isAttribute = true, localName = "Type")
		private String type;
		@JacksonXmlText
		private String material;
		@JacksonXmlProperty(isAttribute = true, localName = "Group")
		private String materialGroup;

		AnnotatedMaterialDTO() {
		}

		AnnotatedMaterialDTO(Material theMaterial) {
			type = theMaterial.getType().name();
			material = theMaterial.getName();
			materialGroup = theMaterial.getGroup().getDatabaseString();
		}

		@JacksonXmlProperty(isAttribute = true, localName = "Type")
		public void setType(String type) {
			this.type = type;
		}

		@JacksonXmlProperty(isAttribute = true, localName = "Group")
		public void setGroup(String materialGroup) {
			this.materialGroup = materialGroup;
		}

		@com.fasterxml.jackson.annotation.JsonIgnore
		public Material.Type getORMaterialType() {
			if ("BULK".equals(type)) {
				return Material.Type.BULK;
			} else if ("SURFACE".equals(type)) {
				return Material.Type.SURFACE;
			} else if ("LINE".equals(type)) {
				return Material.Type.LINE;
			}
			throw new IllegalArgumentException("Invalid material type " + type + " specified for Component");
		}
	}

	static class AnnotatedLengthDTO {
		@JacksonXmlProperty(isAttribute = true, localName = "Unit")
		private String unitName;
		@JacksonXmlText
		private double length;

		AnnotatedLengthDTO() {
			this.unitName = "m";
		}

		AnnotatedLengthDTO(double length) {
			this.unitName = "m";
			this.length = length;
		}

		@JacksonXmlProperty(isAttribute = true, localName = "Unit")
		public void setUnit(String unitName) {
			this.unitName = unitName;
		}

		public double getValue() {
			return UnitGroup.UNITS_LENGTH.getUnit(unitName).fromUnit(length);
		}
	}

	static class AnnotatedMassDTO {
		@JacksonXmlProperty(isAttribute = true, localName = "Unit")
		private String unitName;
		@JacksonXmlText
		private double mass;

		AnnotatedMassDTO() {
			unitName = "kg";
		}

		AnnotatedMassDTO(double mass) {
			unitName = "kg";
			this.mass = mass;
		}

		@JacksonXmlProperty(isAttribute = true, localName = "Unit")
		public void setUnit(String unitName) {
			this.unitName = unitName;
		}

		public double getValue() {
			return UnitGroup.UNITS_MASS.getUnit(unitName).fromUnit(mass);
		}
	}

	static class Base64Serializer extends StdSerializer<byte[]> {
		public Base64Serializer() {
			super(byte[].class);
		}

		@Override
		public void serialize(byte[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
			if (value == null) {
				gen.writeNull();
				return;
			}
			gen.writeString(java.util.Base64.getEncoder().encodeToString(value));
		}
	}

	static class Base64Deserializer extends StdDeserializer<byte[]> {
		public Base64Deserializer() {
			super(byte[].class);
		}

		@Override
		public byte[] deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
			String s = p.getText();
			if (s == null) {
				return null;
			}
			return java.util.Base64.getDecoder().decode(s);
		}
	}
}
