package net.sf.openrocket.file.wavefrontobj.export;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.defaults.DefaultAppearance;
import net.sf.openrocket.file.wavefrontobj.DefaultMtl;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Color;

import java.util.List;

/**
 * Helper class for exporting a rocket component's appearance to an MTL file.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class MtlExpoter {
    private final DefaultObj obj;
    private final RocketComponent component;
    private final String materialName;
    private List<DefaultMtl> materials;

    /**
     * Export the appearance of a rocket component
     * <b>NOTE: </b> you still have to call {@link #doExport()} to actually export the appearance.
     * @param obj The obj file that will use the material
     * @param component The component to export the appearance of
     * @param materialName The name of the material to generate
     * @param materials The list of materials to add the new material(s) to
     */
    public MtlExpoter(DefaultObj obj, RocketComponent component, String materialName, List<DefaultMtl> materials) {
        this.obj = obj;
        this.component = component;
        this.materialName = materialName;
        this.materials = materials;
    }

    /**
     * Export the appearance of the component to the MTL file. Also sets the active material group in the OBJ file.
     */
    public void doExport() {
        // Set the active material group
        obj.setActiveMaterialGroupName(materialName);
        DefaultMtl material = new DefaultMtl(materialName);

        // Get the component appearance
        Appearance appearance = component.getAppearance();
        if (appearance == null) {
            appearance = DefaultAppearance.getDefaultAppearance(component);
        }

        // Apply coloring
        Color color = appearance.getPaint();
        final float r = color.getRed()/255f;
        final float g = color.getGreen()/255f;
        final float b = color.getBlue()/255f;
        material.setKd(r, g, b);                                // Diffuse color
        material.setKa(0f, 0f, 0f);                             // No emission
        material.setKs(1f, 1f, 1f);                             // Use white specular highlights
        material.setD(color.getAlpha()/255f);                   // Opacity
        material.setNs((float) appearance.getShine() * 750);    // Shine (max is 1000, but this too strong compared to OpenRocket's max)
        material.setIllum(2);                                   // Use Phong reflection (specular highlights etc.)

        // TODO: Apply texture

        materials.add(material);
    }
}
