package net.sf.openrocket.preset.loader;

import java.util.Arrays;

/**
 * Definition of the typical Rocksim component files and their formats.
 */
public enum RocksimComponentFileType {
    BODY_TUBE("BTDATA.CSV", "Mfg.", "Part No.", "Desc.", "Units", "ID", "OD", "Length", "Material", "Engine"),
    BULKHEAD("BHDATA.CSV", "Mfg.", "Part No.", "Desc.", "Units", "ID", "OD", "Length", "Material", "Engine", "Engine",
            "Engine", "Engine", "Engine", "Engine", "Engine", "Engine", "Engine", "Engine", "Engine"),
    CENTERING_RING("CRDATA.CSV", "Mfg.", "Part No.", "Desc.", "Units", "ID", "OD", "Length", "Material", "AutoSize"),
    CUSTOM_FIN("CSDATA.CSV"),
    ENGINE_BLOCK("EBDATA.CSV", "Mfg.", "Part No.", "Desc.", "Units", "ID", "OD", "Length", "Material", "CG", "Mass Units", "Mass", "AutoSize"),
    FIN("FSDATA.CSV"),
    LAUNCH_LUG("LLDATA.CSV", "Mfg.", "Part No.", "Desc.", "Units", "ID", "OD", "Length", "Material"),
    MASS_OBJECT("MODATA.CSV", "Mfg.", "Part no", "Desc", "Units", "Name", "Type", "Length", "Material", "Mass units", "Mass"),
    MATERIAL("MATERIAL.CSV", "Material Name", "Units", "Density", "Low", "High", "Class", "Rocketry Use", "Body Tubes",
            "Fin Sets", "Launch Lugs", "Cords", "Nose", "Chute", "Stream", "Trans", "Ring", "Bulkhead", "Engine Block", "Sleeve",
            "Tube Coupler", "spare", "spare", "spare", "spare", "spare", "spare", "spare", "Known Dim type", "Known Dim Units", "Known Dim Value"),
    NOSE_CONE("NCDATA.CSV", "Mfg.","Part No.","Desc.","Units","Length","Outer Dia","L/D Ratio","Insert Length","Insert OD",
            "Thickness","Shape","Config","Material","CG Loc","Mass Units","Mass","Base Ext. Len"),
    PARACHUTE("PCDATA.CSV"),
    SLEEVE("SLDATA.CSV"),
    STREAMER("STDATA.CSV", "Mfg.", "Part No.", "Desc.", "Units", "Length", "Width", "Thickness", "Count", "Material"),
    TUBE_COUPLER("TCDATA.CSV", "Mfg.", "Part No.", "Desc.", "Units", "ID", "OD", "Length", "Material", "Mass Units", "CG", "Mass", "AutoSize"),
    TRANSITION("TRDATA.CSV", "Mfg.", "Part No.", "Desc.", "Units", "Front Insert Len", "Front Insert OD", "Front OD", "Length",
            "Rear OD", "Core Dia.", "Rear Insert Len", "Rear Insert OD", "Thickness", "Config", "Material", "CG Loc",
            "Mass Units", "Mass", "Shape", "Shape", "Shape", "Shape", "Shape", "Shape", "Shape", "Shape", "Shape", "Shape", "Shape", "Shape");

    /**
     * The default filename for the type of data.
     */
    private final String defaultFileName;

    /**
     * The column names.
     */
    private final String[] columns;

    /**
     * Constructor.
     *
     * @param theDefaultFileName  the default filename
     * @param theColumns the array of column names in the file
     */
    private RocksimComponentFileType(final String theDefaultFileName, String... theColumns) {
        defaultFileName = theDefaultFileName;
        columns = theColumns;
    }

    /**
     * Get the typical file name used for this type of component data.
     *
     * @return a filename
     */
    public String getDefaultFileName() {
        return defaultFileName;
    }

    /**
     * Try to be omniscient and figure out what kind of data file it is given an array of header (column) names.
     *
     * @param headers an array of column names
     * @return the data type of the file, or null if unable to match the header names
     */
    public static RocksimComponentFileType determineType(String[] headers) {
        RocksimComponentFileType[] types = values();
        for (int i = 0; i < types.length; i++) {
            RocksimComponentFileType type = types[i];
            if (Arrays.equals(headers, type.columns)) {
                return type;
            }
        }
        return null;
    }
}
