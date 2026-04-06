package info.openrocket.core.preset.xml;

import info.openrocket.core.material.MaterialGroup;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A mirror enum of MaterialGroup, for the purposes of mapping to/from an XML
 * representation.
 */
public enum MaterialGroupDTO {
	@JsonProperty("Metals")
	METALS(MaterialGroup.METALS),
	@JsonProperty("Woods")
	WOODS(MaterialGroup.WOODS),
	@JsonProperty("Plastics")
	PLASTICS(MaterialGroup.PLASTICS),
	@JsonProperty("Fabrics")
	FABRICS(MaterialGroup.FABRICS),
	@JsonProperty("PaperProducts")
	PAPER(MaterialGroup.PAPER),
	@JsonProperty("Foams")
	FOAMS(MaterialGroup.FOAMS),
	@JsonProperty("Composites")
	COMPOSITES(MaterialGroup.COMPOSITES),
	@JsonProperty("Fibers")
	FIBERS(MaterialGroup.FIBERS),
  @JsonProperty("Elastics")
  ELASTICS(MaterialGroup.ELASTICS),
  @JsonProperty("Kevlars")
  KEVLARS(MaterialGroup.KEVLARS),
  @JsonProperty("Nylons")
  NYLONS(MaterialGroup.NYLONS),
	@JsonProperty("Other")
	OTHER(MaterialGroup.OTHER),
	@JsonProperty("Custom")
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
