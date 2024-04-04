package info.openrocket.core.file.wavefrontobj.export;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.Decal;
import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.file.wavefrontobj.DefaultMtl;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.DefaultTextureOptions;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Helper class for exporting a rocket component's appearance to an MTL file.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class AppearanceExporter {
    private final DefaultObj obj;
    private final Appearance appearance;
    private final File file;
    private final OBJExportOptions options;
    private final String materialName;
    private final List<DefaultMtl> materials;

    private static final Logger log = LoggerFactory.getLogger(AppearanceExporter.class);

    /**
     * Export the appearance of a rocket component
     * <b>NOTE: </b> you still have to call {@link #doExport()} to actually export the appearance.
     * @param obj The obj file that will use the material
     * @param appearance The appearance to export
     * @param file The file that the OBJ is exported to
     * @param options The options to use for exporting the OBJ
     * @param materialName The name of the material to generate
     * @param materials The list of materials to add the new material(s) to
     */
    public AppearanceExporter(DefaultObj obj, Appearance appearance, File file, OBJExportOptions options,
                              String materialName, List<DefaultMtl> materials) {
        this.obj = obj;
        this.appearance = appearance;
        this.file = file;
        this.options = options;
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

        // Apply coloring
        applyColoring(appearance, material, options);

        // Apply texture
        applyTexture(appearance, material);

        materials.add(material);
    }

    private void applyTexture(Appearance appearance, DefaultMtl material) {
        Decal texture = appearance.getTexture();
        if (texture == null) {
            return;
        }

        final DefaultTextureOptions textureOptions = new DefaultTextureOptions();

        // The decal file is stored inside the .ork, so first export it to the export directory
        final File decalFile;
        try {
            String exportDir = file.getParent();
            String fileName = FileUtils.removeExtension(file.getName());
            Path decalDir = Path.of(exportDir, fileName + "_img");
            Files.createDirectories(decalDir);

            DecalImage decal = texture.getImage();
            String decalName = FileUtils.getFileNameFromPath(decal.getName());
            decalFile = new File(decalDir.toString(), decalName);       // TODO: should name be unique?
            decalFile.createNewFile();                                  // TODO: check if you want to overwrite?
            decal.exportImage(decalFile);
            log.info("Exported decal image to {}", decalFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to export decal image", e);
            return;
        }

        textureOptions.setFileName(decalFile.getAbsolutePath());

        // Texture scale
        final Coordinate scale = texture.getScale();
        float scaleX = (float) scale.x;
        float scaleY = (float) scale.y;
        textureOptions.setS(scaleX, scaleY, 1f);

        // Texture offset
        // Need an extra offset because the texture scale origin is different in OR
        final Coordinate origin = texture.getOffset();
        float origX = (float) (scaleX*(-1 - origin.x) + 1);
        float origY = (float) (scaleY*(-1 - origin.y) + 1);

        textureOptions.setO(origX, origY, 0f);

        // Texture rotation is not possible in MTL...

        // Texture repeat (not very extensive in MTL...)
        Decal.EdgeMode edgeMode = texture.getEdgeMode();
        switch (edgeMode) {
            case REPEAT, MIRROR -> textureOptions.setClamp(false);
            default -> textureOptions.setClamp(true);
        }

        // Apply the texture
        material.setMapKdOptions(textureOptions);
    }

    private static void applyColoring(Appearance appearance, DefaultMtl material, OBJExportOptions options) {
        ORColor color = appearance.getPaint();
        final float r = convertColorToFloat(color.getRed(), options.isUseSRGB());
        final float g = convertColorToFloat(color.getGreen(), options.isUseSRGB());
        final float b = convertColorToFloat(color.getBlue(), options.isUseSRGB());
        material.setKd(r, g, b);                                // Diffuse color
        material.setKa(0f, 0f, 0f);                             // No emission
        material.setKs(.25f, .25f, .25f);                       // Not too strong specular highlights
        material.setD(color.getAlpha()/255f);                   // Opacity
        material.setNs((float) appearance.getShine() * 750);    // Shine (max is 1000, but this too strong compared to OpenRocket's max)
        material.setIllum(2);                                   // Use Phong reflection (specular highlights etc.)
    }

    private static float convertColorToFloat(int color, boolean sRGB) {
        float convertedColor = color / 255f;
        if (sRGB) {
            convertedColor = linearTosRGB(convertedColor);
        }
        return convertedColor;
    }

    private static float linearTosRGB(float linear) {
        return (float) Math.pow(linear, 2.2);
    }
}
