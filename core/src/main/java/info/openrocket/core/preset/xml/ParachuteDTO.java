
package info.openrocket.core.preset.xml;

import info.openrocket.core.material.Material;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPresetFactory;
import info.openrocket.core.preset.InvalidComponentPresetException;
import info.openrocket.core.preset.TypedPropertyMap;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;

/**
 * Streamer preset XML handler.
 */
@JacksonXmlRootElement(localName = "Parachute")
public class ParachuteDTO extends BaseComponentDTO {

	@JacksonXmlProperty(localName = "Diameter")
	private AnnotatedLengthDTO diameter;
	@JacksonXmlProperty(localName = "Sides")
	private Integer sides;
	@JacksonXmlProperty(localName = "PackedDiameter")
	private AnnotatedLengthDTO PackedDiameter;
	@JacksonXmlProperty(localName = "PackedLength")
	private AnnotatedLengthDTO PackedLength;
	@JacksonXmlProperty(localName = "DragCoefficient")
	private AnnotatedLengthDTO dragCoefficient;
	@JacksonXmlProperty(localName = "LineCount")
	private Integer lineCount;
	@JacksonXmlProperty(localName = "LineLength")
	private AnnotatedLengthDTO lineLength;

	@JacksonXmlProperty(localName = "LineMaterial")
	private AnnotatedMaterialDTO lineMaterial;

	/**
	 * Default constructor.
	 */
	public ParachuteDTO() {
	}

	public double getDiameter() {
		return diameter.getValue();
	}

	public void setDiameter(AnnotatedLengthDTO diameter) {
		this.diameter = diameter;
	}

	@JsonIgnore
	public void setDiameter(double diameter) {
		this.diameter = new AnnotatedLengthDTO(diameter);
	}

	public Integer getSides() {
		return sides;
	}

	public void setSides(Integer sides) {
		this.sides = sides;
	}

	public double getPackedDiameter() {
		return PackedDiameter.getValue();
	}

	public void setPackedDiameter(AnnotatedLengthDTO PackedDiameter) {
		this.PackedDiameter = PackedDiameter;
	}

	@JsonIgnore
	public void setPackedDiameter(double PackedDiameter) {
		this.PackedDiameter = new AnnotatedLengthDTO(PackedDiameter);
	}

	public double getPackedLength() {
		return PackedLength.getValue();
	}

	public void setPackedLength(AnnotatedLengthDTO PackedLength) {
		this.PackedLength = PackedLength;
	}

	@JsonIgnore
	public void setPackedLength(double PackedLength) {
		this.PackedLength = new AnnotatedLengthDTO(PackedLength);
	}

	public double getDragCoefficient() {
		return dragCoefficient.getValue();
	}

	public void setDragCoefficient(AnnotatedLengthDTO DragCoefficient) {
		this.dragCoefficient = DragCoefficient;
	}

	@JsonIgnore
	public void setDragCoefficient(double DragCoefficient) {
		this.dragCoefficient = new AnnotatedLengthDTO(DragCoefficient);
	}

	public Integer getLineCount() {
		return lineCount;
	}

	public void setLineCount(Integer lineCount) {
		this.lineCount = lineCount;
	}

	public double getLineLength() {
		return lineLength.getValue();
	}

	public void setLineLength(AnnotatedLengthDTO lineLength) {
		this.lineLength = lineLength;
	}

	@JsonIgnore
	public void setLineLength(double lineLength) {
		this.lineLength = new AnnotatedLengthDTO(lineLength);
	}

	public AnnotatedMaterialDTO getLineMaterial() {
		return lineMaterial;
	}

	public void setLineMaterial(AnnotatedMaterialDTO lineMaterial) {
		this.lineMaterial = lineMaterial;
	}

	/**
	 * Most-useful constructor that maps a BodyTube preset to a BodyTubeDTO.
	 *
	 * @param preset the preset
	 *
	 * @throws info.openrocket.core.util.BugException thrown if the expected body
	 *                                                tube keys are not in the
	 *                                                preset
	 */
	public ParachuteDTO(final ComponentPreset preset) {
		super(preset);
		setDiameter(preset.get(ComponentPreset.DIAMETER));
		setLineCount(preset.get(ComponentPreset.LINE_COUNT));
		if (preset.has(ComponentPreset.LINE_LENGTH)) {
			setLineLength(preset.get(ComponentPreset.LINE_LENGTH));
		}
		if (preset.has(ComponentPreset.SIDES)) {
			setSides(preset.get(ComponentPreset.SIDES));
		}
		if (preset.has(ComponentPreset.PACKED_DIAMETER)) {
			setPackedDiameter(preset.get(ComponentPreset.PACKED_DIAMETER));
		}
		if (preset.has(ComponentPreset.PACKED_LENGTH)) {
			setPackedLength(preset.get(ComponentPreset.PACKED_LENGTH));
		}
		if (preset.has(ComponentPreset.CD)) {
			setDragCoefficient(preset.get(ComponentPreset.CD));
		}
		if (preset.has(ComponentPreset.LINE_MATERIAL)) {
			setLineMaterial(new AnnotatedMaterialDTO(preset.get(ComponentPreset.LINE_MATERIAL)));
		}
	}

	@Override
	public ComponentPreset asComponentPreset(Boolean legacy, java.util.List<MaterialDTO> materials)
			throws InvalidComponentPresetException {
		return asComponentPreset(legacy, ComponentPreset.Type.PARACHUTE, materials);
	}

	public ComponentPreset asComponentPreset(Boolean legacy, ComponentPreset.Type type, List<MaterialDTO> materials)
			throws InvalidComponentPresetException {
		TypedPropertyMap props = new TypedPropertyMap();
		props.put(ComponentPreset.LEGACY, legacy);
		addProps(props, materials);
		// TODO - seems some vendors use a bulk material for the sheet along with a
		// Thickness.
		// need to fix the MATERIAL packed into the componentpreset.
		props.put(ComponentPreset.TYPE, type);
		props.put(ComponentPreset.DIAMETER, this.getDiameter());
		if (this.PackedDiameter != null) {
			props.put(ComponentPreset.PACKED_DIAMETER, this.getPackedDiameter());
		}
		if (this.PackedLength != null) {
			props.put(ComponentPreset.PACKED_LENGTH, this.getPackedLength());
		}
		if (this.dragCoefficient != null) {
			props.put(ComponentPreset.CD, this.getDragCoefficient());
		}
		props.put(ComponentPreset.LINE_COUNT, this.getLineCount());
		if (this.lineLength != null) {
			props.put(ComponentPreset.LINE_LENGTH, this.getLineLength());
		}
		if (this.sides != null) {
			props.put(ComponentPreset.SIDES, this.getSides());
		}
		if (this.lineMaterial != null) {
			Material m = find(materials, this.lineMaterial);
			if (m != null) {
				props.put(ComponentPreset.LINE_MATERIAL, m);
			}
		}

		return ComponentPresetFactory.create(props);
	}
}
