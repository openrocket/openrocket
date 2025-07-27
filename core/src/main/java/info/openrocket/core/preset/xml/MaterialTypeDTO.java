
package info.openrocket.core.preset.xml;

import info.openrocket.core.material.Material;

/**
 * A mirror enum of Material.Type, for the purposes of mapping to/from an XML
 * representation.
 */
public enum MaterialTypeDTO {

    LINE(Material.Type.LINE),
    SURFACE(Material.Type.SURFACE),
    BULK(Material.Type.BULK);

    private final Material.Type corollary;

    private MaterialTypeDTO(final Material.Type theCorollary) {
        corollary = theCorollary;
    }

    public static MaterialTypeDTO asDTO(Material.Type targetType) {
        MaterialTypeDTO[] values = values();
        for (MaterialTypeDTO value : values) {
            if (value.corollary.equals(targetType)) {
                return value;
            }
        }
        return BULK; // default
    }

    public Material.Type getORMaterialType() {
        return corollary;
    }
}
