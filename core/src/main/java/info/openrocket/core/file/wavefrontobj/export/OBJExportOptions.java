package info.openrocket.core.file.wavefrontobj.export;

import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultCoordTransform;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.rocketcomponent.Rocket;

public class OBJExportOptions {
    // ! Update Preferences when adding new options !

    /**
     * If true, export all children of the components as well
     */
    private boolean exportChildren;
    /**
     * If true, export the motors of the components as well.
     */
    private boolean exportMotors;
    /**
     * If true, export the appearance of the components to an MTL file.
     */
    private boolean exportAppearance;
    /**
     * If true, export each component as a separate OBJ file.
     */
    private boolean exportAsSeparateFiles;
    /**
     * If true, remove the offset of the object so it is centered at the origin (but the bottom of the object is at x=0).
     * 'x' being the longitudinal axis (depends on the used {@link CoordTransform}).
     */
    private boolean removeOffset;
    /**
     * If true, triangulate all faces (convert quads and higher-order polygons to triangles)
     */
    private boolean triangulate;
    /**
     * The method to use for triangulation.
     */
    private ObjUtils.TriangulationMethod triangulationMethod;
    /**
     * If true, use sRGB colors instead of linear color space.
     */
    private boolean useSRGB;
    /**
     * The level of detail to use for the export (e.g. low-quality, normal quality...).
     */
    private ObjUtils.LevelOfDetail LOD;
    /**
     * The coordinate transformer to use for the export.
     * This is used to convert the coordinates from the rocket's coordinate system to the OBJ coordinate system (which is arbitrary).
     */
    private CoordTransform transformer;
    /**
     * The scaling factor to use for the export (1 = no scaling).
     */
    private float scaling;

    public OBJExportOptions(Rocket rocket) {
        this.exportChildren = false;
        this.exportAppearance = false;
        this.exportAsSeparateFiles = false;
        this.removeOffset = true;
        this.triangulate = false;
        this.LOD = ObjUtils.LevelOfDetail.NORMAL_QUALITY;
        this.transformer = new DefaultCoordTransform(rocket.getLength());
        this.scaling = 1.0f;
    }

    public boolean isExportChildren() {
        return exportChildren;
    }

    public void setExportChildren(boolean exportChildren) {
        this.exportChildren = exportChildren;
    }

    public boolean isExportMotors() {
        return exportMotors;
    }

    public void setExportMotors(boolean exportMotors) {
        this.exportMotors = exportMotors;
    }

    public boolean isRemoveOffset() {
        return removeOffset;
    }

    public void setRemoveOffset(boolean removeOffset) {
        this.removeOffset = removeOffset;
    }

    public boolean isTriangulate() {
        return triangulate;
    }

    public void setTriangulate(boolean triangulate) {
        this.triangulate = triangulate;
    }

    public ObjUtils.TriangulationMethod getTriangulationMethod() {
        return triangulationMethod;
    }

    public void setTriangulationMethod(ObjUtils.TriangulationMethod triangulationMethod) {
        this.triangulationMethod = triangulationMethod;
    }

    public boolean isExportAppearance() {
        return exportAppearance;
    }

    public void setExportAppearance(boolean exportAppearance) {
        this.exportAppearance = exportAppearance;
    }

    public ObjUtils.LevelOfDetail getLOD() {
        return LOD;
    }

    public void setLOD(ObjUtils.LevelOfDetail LOD) {
        this.LOD = LOD;
    }

    public CoordTransform getTransformer() {
        return transformer;
    }

    public void setTransformer(CoordTransform transformer) {
        this.transformer = transformer;
    }

    public boolean isExportAsSeparateFiles() {
        return exportAsSeparateFiles;
    }

    public void setExportAsSeparateFiles(boolean exportAsSeparateFiles) {
        this.exportAsSeparateFiles = exportAsSeparateFiles;
    }

    public float getScaling() {
        return scaling;
    }

    public double getScalingDouble() {
        return scaling;
    }

    public void setScaling(float scaling) {
        this.scaling = scaling;
    }

    public void setScalingDouble(double scaling) {
        this.scaling = (float) scaling;
    }

    public boolean isUseSRGB() {
        return useSRGB;
    }

    public void setUseSRGB(boolean useSRGB) {
        this.useSRGB = useSRGB;
    }
}
