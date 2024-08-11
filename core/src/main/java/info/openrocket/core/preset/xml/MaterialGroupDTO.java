package info.openrocket.core.preset.xml;

import info.openrocket.core.material.MaterialGroup;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

/**
 * A mirror enum of MaterialGroup, for the purposes of mapping to/from an XML
 * representation.
 */
@XmlEnum
public enum MaterialGroupDTO {
	@XmlEnumValue("Metals")
	METALS(MaterialGroup.METALS),
	@XmlEnumValue("Woods")
	WOODS(MaterialGroup.WOODS),
	@XmlEnumValue("Plastics")
	PLASTICS(MaterialGroup.PLASTICS),
	@XmlEnumValue("Fabrics")
	FABRICS(MaterialGroup.FABRICS),
	@XmlEnumValue("PaperProducts")
	PAPER(MaterialGroup.PAPER),
	@XmlEnumValue("Foams")
	FOAMS(MaterialGroup.FOAMS),
	@XmlEnumValue("Composites")
	COMPOSITES(MaterialGroup.COMPOSITES),
	@XmlEnumValue("Fibers")
	FIBERS(MaterialGroup.FIBERS),
	@XmlEnumValue("ThreadsLines")
	THREADS_LINES(MaterialGroup.THREADS_LINES),
	@XmlEnumValue("Other")
	OTHER(MaterialGroup.OTHER),
	@XmlEnumValue("Custom")
	CUSTOM(MaterialGroup.CUSTOM);

	private final MaterialGroup corollary;

	MaterialGroupDTO(MaterialGroup materialGroup) {
		this.corollary = materialGroup;
	}

	public MaterialGroup getORMaterialGroup() {
		return corollary;
	}

	public static MaterialGroupDTO asDTO(MaterialGroup targetGroup) {
		MaterialGroupDTO[] values = values();
		for (MaterialGroupDTO value : values) {
			if (value.corollary.equals(targetGroup)) {
				return value;
			}
		}
		//throw new IllegalArgumentException("Unknown MaterialGroup: " + targetGroup);
		return OTHER; // default
	}
}
