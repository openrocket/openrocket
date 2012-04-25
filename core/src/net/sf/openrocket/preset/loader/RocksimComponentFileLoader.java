package net.sf.openrocket.preset.loader;

import au.com.bytecode.opencsv.CSVReader;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.file.preset.ColumnDefinition;
import net.sf.openrocket.file.rocksim.RocksimNoseConeCode;
import net.sf.openrocket.gui.print.PrintUnit;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPresetFactory;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.preset.xml.OpenRocketComponentSaver;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BugException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Primary entry point for parsing component CSV files that are in Rocksim format.
 */
public class RocksimComponentFileLoader {

    /**
     * Common unit of measure key.  Rocksim format allows different types of units.
     */
    public final static TypedKey<String> UNITS_OF_MEASURE = new TypedKey<String>("Units", String.class);

    /**
     * Read a comma separated component file and return the parsed contents as a list of string arrays.  Not for
     * production use - just here for smoke testing.
     *
     * @param type the type of component file to read; uses the default file name
     * @return a list (guaranteed never to be null) of string arrays.  Each element of the list represents a row in the
     *         component data file; the element in the list itself is an array of String, where each item in the array
     *         is a column (cell) in the row.  The string array is in sequential order as it appeared in the file.
     */
    public static List<String[]> load(RocksimComponentFileType type) {
        return load(RocksimComponentFileLoader.class.getResourceAsStream("/giantleaprocketry/" + type.getDefaultFileName()));
    }

    /**
     * Read a comma separated component file and return the parsed contents as a list of string arrays.
     *
     * @param file the file to read and parse
     * @return a list (guaranteed never to be null) of string arrays.  Each element of the list represents a row in the
     *         component data file; the element in the list itself is an array of String, where each item in the array
     *         is a column (cell) in the row.  The string array is in sequential order as it appeared in the file.
     */
    public static List<String[]> load(File file) throws FileNotFoundException {
        return load(new FileInputStream(file));
    }

    /**
     * Read a comma separated component file and return the parsed contents as a list of string arrays.
     *
     * @param is the stream to read and parse
     * @return a list (guaranteed never to be null) of string arrays.  Each element of the list represents a row in the
     *         component data file; the element in the list itself is an array of String, where each item in the array
     *         is a column (cell) in the row.  The string array is in sequential order as it appeared in the file.
     */
    public static List<String[]> load(InputStream is) {
        if (is == null) {
            return new ArrayList<String[]>();
        }
        InputStreamReader r = null;
        try {
            r = new InputStreamReader(is);

            // Create the CSV reader.  Use comma separator.
            CSVReader reader = new CSVReader(r, ',', '\'', '\\');

            //Read and throw away the header row.
            reader.readNext();

            //Read the rest of the file as data rows.
            return reader.readAll();
        }
        catch (IOException e) {
        }
        finally {
            if (r != null) {
                try {
                    r.close();
                }
                catch (IOException e) {
                }
            }
        }

        return new ArrayList<String[]>();
    }

    /**
     * Rocksim CSV units are either inches or mm.  A value of 0 or "in." indicate inches.  A value of 1 or "mm" indicate
     * millimeters.
     *
     * @param units the value from the file
     * @return true if it's inches
     */
    private static boolean isInches(String units) {
        String tmp = units.trim().toLowerCase();
        return "0".equals(tmp) || tmp.startsWith("in");
    }

    /**
     * Convert inches or millimeters to meters.
     *
     * @param units a Rocksim CSV string representing the kind of units.
     * @param value the original value within the CSV file
     * @return the value in meters
     */
    private static double convertLength(String units, double value) {
        if (isInches(units)) {
            return PrintUnit.INCHES.toMeters(value);
        }
        else {
            return PrintUnit.MILLIMETERS.toMeters(value);
        }
    }

    /**
     * Remove all occurrences of the given character.  Note: this is done because some manufacturers embed double
     * quotes in their descriptions or material names.  Those are stripped away because they cause all sorts of
     * matching/lookup issues.
     *
     * @param target      the target string to be operated upon
     * @param toBeRemoved the character to remove
     * @return target, minus every occurrence of toBeRemoved
     */
    private static String stripAll(String target, Character toBeRemoved) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < target.length(); i++) {
            Character c = target.charAt(i);
            if (!c.equals(toBeRemoved)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Convert all words in a given string to Camel Case (first letter capitalized). Words are assumed to be
     * separated by a space.  Note: this is done because some manufacturers define their material name in Camel Case
     * but the component part references the material in lower case.  That causes matching/lookup issues that's
     * easiest handled this way (rather than converting everything to lower case.
     *
     * @param target the target string to be operated upon
     * @return target, with the first letter of each word in uppercase
     */
    private static String toCamelCase(String target) {
        StringBuilder sb = new StringBuilder();
        String[] t = target.split("[ ]");
        if (t != null && t.length > 0) {
            for (String aT : t) {
                String s = aT;
                s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
                sb.append(s).append(" ");
            }
            return sb.toString().trim();
        }
        else {
            return target;
        }
    }

    /**
     * The core loading method, shared by all component types.
     *
     * @param theData     the data as read from the CSV file
     * @param keyMap      the list of typed keys that specify the preset's expected columns
     * @param materialMap a map of material name to OR Material; this is sourced from a MATERIAL.CSV file that must
     *                    accompany the component CSV file.
     * @param type        the kind of component
     * @return a collection of preset's
     */
    private static Collection<ComponentPreset> commonLoader(final List<String[]> theData,
                                                            final List<TypedKey<?>> keyMap,
                                                            final Map<String, Material> materialMap,
                                                            final ComponentPreset.Type type) {
        Collection<ComponentPreset> result = new ArrayList<ComponentPreset>();
        List<TypedPropertyMap> templates = new java.util.ArrayList<TypedPropertyMap>();
        Set<String> favorites = Application.getPreferences().getComponentFavorites();
        Integer uom = null;

        ColumnDefinition[] columns = new ColumnDefinition[keyMap.size()];
        for (int i = 0; i < keyMap.size(); i++) {
            TypedKey key = keyMap.get(i);
            if (key != null) {
                columns[i] = new ColumnDefinition(key);
                if (key.getName().equals("Units")) {
                    uom = i;
                }
            }
        }

        for (int i = 0; i < theData.size(); i++) {
            String[] item = theData.get(i);
            TypedPropertyMap preset = new TypedPropertyMap();

            for (int j = 0; j < columns.length; j++) {
                if (j < item.length) {
                    String value = item[j];
                    if (value == null) {
                        continue;
                    }
                    value = value.trim();
                    value = stripAll(value, '"');
                    if (value.length() == 0) {
                        continue;
                    }
                    final TypedKey typedKey = columns[j].getKey();
                    //If it's the material, then pull it out of our internal map.  The map references the
                    //data from the associated MATERIAL.CSV file that is mandatory.
                    if (typedKey.equals(ComponentPreset.MATERIAL)) {
                        preset.put(ComponentPreset.MATERIAL, materialMap.get(value));
                    }
                    //The shape of a nosecone or transition must get mapped from Rocksim to OR.
                    else if (typedKey.equals(ComponentPreset.SHAPE)) {
                        preset.put(ComponentPreset.SHAPE, RocksimNoseConeCode.fromShapeNameOrCode(value).asOpenRocket());
                    }
                    else {
                        //Rocksim allows different types of length units.  They must be converted and normalized to OR.
                        final UnitGroup unitGroup = typedKey.getUnitGroup();
                        if (unitGroup != null && unitGroup.equals(UnitGroup.UNITS_LENGTH)) {
                            columns[j].setProperty(preset, convertLength(item[uom], Double.valueOf(value)));
                        }
                        else {
                            columns[j].setProperty(preset, value);
                        }
                    }
                }
            }
            //Set what kind of component this is.
            preset.put(ComponentPreset.TYPE, type);
            //Add to the collection.
            templates.add(preset);
        }

        for (TypedPropertyMap o : templates) {
            try {
                ComponentPreset preset = ComponentPresetFactory.create(o);
                if (favorites.contains(preset.preferenceKey())) {
                    preset.setFavorite(true);
                }
                result.add(preset);
            }
            catch (InvalidComponentPresetException ex) {
                throw new BugException(ex);
            }
        }

        return result;
    }

    static class BodyTubeLoader {
        private final static int MFG_INDEX = 0;
        private final static int PART_NO_INDEX = 1;
        private final static int DESCRIPTION_INDEX = 2;
        private final static int UNITS_INDEX = 3;
        private final static int ID_INDEX = 4;
        private final static int OD_INDEX = 5;
        private final static int LENGTH_INDEX = 6;
        private final static int MATERIAL_INDEX = 7;

        public final static List<TypedKey<?>> keyMap = new ArrayList<TypedKey<?>>(8);

        static {
            keyMap.add(MFG_INDEX, ComponentPreset.MANUFACTURER);
            keyMap.add(PART_NO_INDEX, ComponentPreset.PARTNO);
            keyMap.add(DESCRIPTION_INDEX, ComponentPreset.DESCRIPTION);
            keyMap.add(UNITS_INDEX, UNITS_OF_MEASURE);
            keyMap.add(ID_INDEX, ComponentPreset.INNER_DIAMETER);
            keyMap.add(OD_INDEX, ComponentPreset.OUTER_DIAMETER);
            keyMap.add(LENGTH_INDEX, ComponentPreset.LENGTH);
            keyMap.add(MATERIAL_INDEX, ComponentPreset.MATERIAL);
        }

        public Collection<ComponentPreset> load(Map<String, Material> materialMap) {
            List<String[]> data = RocksimComponentFileLoader.load(RocksimComponentFileType.BODY_TUBE);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.BODY_TUBE);
        }

        public Collection<ComponentPreset> load(Map<String, Material> materialMap, File file) throws
                                                                                              FileNotFoundException {
            List<String[]> data = RocksimComponentFileLoader.load(file);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.BODY_TUBE);
        }

    }

    /**
     * Tube coupler parser.  Although there are additional fields in the file, they are not used by
     * most (any?) manufacturers so we ignore them entirely.
     */
    static class TubeCouplerLoader extends BodyTubeLoader {
        public Collection<ComponentPreset> load(Map<String, Material> materialMap) {
            List<String[]> data = RocksimComponentFileLoader.load(RocksimComponentFileType.TUBE_COUPLER);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.TUBE_COUPLER);
        }

        public Collection<ComponentPreset> load(Map<String, Material> materialMap, File file) throws
                                                                                              FileNotFoundException {
            List<String[]> data = RocksimComponentFileLoader.load(file);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.TUBE_COUPLER);
        }
    }

    /**
     * Engine block parser.  Although there are additional fields in the file, they are not used by
     * most (any?) manufacturers so we ignore them entirely.
     */
    static class EngineBlockLoader extends BodyTubeLoader {
        public Collection<ComponentPreset> load(Map<String, Material> materialMap) {
            List<String[]> data = RocksimComponentFileLoader.load(RocksimComponentFileType.ENGINE_BLOCK);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.ENGINE_BLOCK);
        }

        public Collection<ComponentPreset> load(Map<String, Material> materialMap, File file) throws
                                                                                              FileNotFoundException {
            List<String[]> data = RocksimComponentFileLoader.load(file);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.ENGINE_BLOCK);
        }
    }


    static class BulkheadLoader extends BodyTubeLoader {
        public Collection<ComponentPreset> load(Map<String, Material> materialMap) {
            List<String[]> data = RocksimComponentFileLoader.load(RocksimComponentFileType.BULKHEAD);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.BULK_HEAD);
        }

        public Collection<ComponentPreset> load(Map<String, Material> materialMap, File file) throws
                                                                                              FileNotFoundException {
            List<String[]> data = RocksimComponentFileLoader.load(file);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.BULK_HEAD);
        }
    }

    static class CenteringRingLoader extends BodyTubeLoader {
        public Collection<ComponentPreset> load(Map<String, Material> materialMap) {
            List<String[]> data = RocksimComponentFileLoader.load(RocksimComponentFileType.CENTERING_RING);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.CENTERING_RING);
        }

        public Collection<ComponentPreset> load(Map<String, Material> materialMap, File file) throws
                                                                                              FileNotFoundException {
            List<String[]> data = RocksimComponentFileLoader.load(file);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.CENTERING_RING);
        }
    }

    static class NoseConeLoader {
        public static final int MFG_INDEX = 0;
        public static final int PART_NO_INDEX = 1;
        public static final int DESCRIPTION_INDEX = 2;
        public static final int UNITS_INDEX = 3;
        public static final int LENGTH_INDEX = 4;
        public static final int OUTER_DIA_INDEX = 5;
        public static final int LD_RATIO_INDEX = 6;
        public static final int INSERT_LENGTH_INDEX = 7;
        public static final int INSERT_OD_INDEX = 8;
        public static final int THICKNESS_INDEX = 9;
        public static final int SHAPE_INDEX = 10;
        public static final int CONFIG_INDEX = 11;
        public static final int MATERIAL_INDEX = 12;
        public static final int CG_LOC_INDEX = 13;
        public static final int MASS_UNITS_INDEX = 14;
        public static final int MASS_INDEX = 15;
        public static final int BASE_EXT_LEN_INDEX = 16;

        public final static TypedKey<Double> LD_RATIO = new TypedKey<Double>("Len/Dia Ratio", Double.class);
        public final static TypedKey<Double> BASE_EXT_LEN = new TypedKey<Double>("Base Ext Len", Double.class, UnitGroup.UNITS_LENGTH);
        public final static TypedKey<String> CONFIG = new TypedKey<String>("Config", String.class);
        public final static TypedKey<Double> CG_LOC = new TypedKey<Double>("CG Loc", Double.class, UnitGroup.UNITS_LENGTH);
        public final static List<TypedKey<?>> keyMap = new ArrayList<TypedKey<?>>(17);

        static {
            keyMap.add(MFG_INDEX, ComponentPreset.MANUFACTURER);
            keyMap.add(PART_NO_INDEX, ComponentPreset.PARTNO);
            keyMap.add(DESCRIPTION_INDEX, ComponentPreset.DESCRIPTION);
            keyMap.add(UNITS_INDEX, UNITS_OF_MEASURE);
            keyMap.add(LENGTH_INDEX, ComponentPreset.LENGTH);
            keyMap.add(OUTER_DIA_INDEX, ComponentPreset.AFT_OUTER_DIAMETER);
            keyMap.add(LD_RATIO_INDEX, LD_RATIO);
            keyMap.add(INSERT_LENGTH_INDEX, ComponentPreset.AFT_SHOULDER_LENGTH);
            keyMap.add(INSERT_OD_INDEX, ComponentPreset.AFT_SHOULDER_DIAMETER);
            keyMap.add(THICKNESS_INDEX, ComponentPreset.THICKNESS);
            keyMap.add(SHAPE_INDEX, ComponentPreset.SHAPE);
            keyMap.add(CONFIG_INDEX, CONFIG);
            keyMap.add(MATERIAL_INDEX, ComponentPreset.MATERIAL);
            keyMap.add(CG_LOC_INDEX, CG_LOC);
            keyMap.add(MASS_UNITS_INDEX, UNITS_OF_MEASURE);
            keyMap.add(MASS_INDEX, ComponentPreset.MASS);
            keyMap.add(BASE_EXT_LEN_INDEX, BASE_EXT_LEN);
        }

        public Collection<ComponentPreset> load(Map<String, Material> materialMap) {
            List<String[]> data = RocksimComponentFileLoader.load(RocksimComponentFileType.NOSE_CONE);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.NOSE_CONE);
        }

        public Collection<ComponentPreset> load(Map<String, Material> materialMap, File file) throws
                                                                                              FileNotFoundException {
            List<String[]> data = RocksimComponentFileLoader.load(file);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.NOSE_CONE);
        }
    }

    static class TransitionLoader {
        public static final int MFG_INDEX = 0;
        public static final int PART_NO_INDEX = 1;
        public static final int DESCRIPTION_INDEX = 2;
        public static final int UNITS_INDEX = 3;
        public static final int FRONT_INSERT_LENGTH_INDEX = 4;
        public static final int FRONT_INSERT_OD_INDEX = 5;
        public static final int FRONT_OD_INDEX = 6;
        public static final int LENGTH_INDEX = 7;
        public static final int REAR_OD_INDEX = 8;
        public static final int CORE_DIA_INDEX = 9;
        public static final int REAR_INSERT_LENGTH_INDEX = 10;
        public static final int REAR_INSERT_OD_INDEX = 11;
        public static final int THICKNESS_INDEX = 12;
        public static final int CONFIG_INDEX = 13;
        public static final int MATERIAL_INDEX = 14;
        public static final int CG_LOC_INDEX = 15;
        public static final int MASS_UNITS_INDEX = 16;
        public static final int MASS_INDEX = 17;
        public static final int SHAPE_INDEX = 18;

        public final static TypedKey<String> CONFIG = new TypedKey<String>("Config", String.class);
        public final static TypedKey<String> IGNORE = new TypedKey<String>("Ignore", String.class);
        public final static TypedKey<Double> CG_LOC = new TypedKey<Double>("CG Loc", Double.class, UnitGroup.UNITS_LENGTH);
        public final static List<TypedKey<?>> keyMap = new ArrayList<TypedKey<?>>(19);

        static {
            keyMap.add(MFG_INDEX, ComponentPreset.MANUFACTURER);
            keyMap.add(PART_NO_INDEX, ComponentPreset.PARTNO);
            keyMap.add(DESCRIPTION_INDEX, ComponentPreset.DESCRIPTION);
            keyMap.add(UNITS_INDEX, UNITS_OF_MEASURE);
            keyMap.add(FRONT_INSERT_LENGTH_INDEX, ComponentPreset.FORE_SHOULDER_LENGTH);
            keyMap.add(FRONT_INSERT_OD_INDEX, ComponentPreset.FORE_SHOULDER_DIAMETER);
            keyMap.add(FRONT_OD_INDEX, ComponentPreset.FORE_OUTER_DIAMETER);
            keyMap.add(LENGTH_INDEX, ComponentPreset.LENGTH);
            keyMap.add(REAR_OD_INDEX, ComponentPreset.AFT_OUTER_DIAMETER);
            keyMap.add(CORE_DIA_INDEX, IGNORE);
            keyMap.add(REAR_INSERT_LENGTH_INDEX, ComponentPreset.AFT_SHOULDER_LENGTH);
            keyMap.add(REAR_INSERT_OD_INDEX, ComponentPreset.AFT_SHOULDER_DIAMETER);
            keyMap.add(THICKNESS_INDEX, ComponentPreset.THICKNESS);
            keyMap.add(CONFIG_INDEX, CONFIG);
            keyMap.add(MATERIAL_INDEX, ComponentPreset.MATERIAL);
            keyMap.add(CG_LOC_INDEX, CG_LOC);
            keyMap.add(MASS_UNITS_INDEX, UNITS_OF_MEASURE);
            keyMap.add(MASS_INDEX, ComponentPreset.MASS);
            keyMap.add(SHAPE_INDEX, ComponentPreset.SHAPE);
        }

        public Collection<ComponentPreset> load(Map<String, Material> materialMap) {
            List<String[]> data = RocksimComponentFileLoader.load(RocksimComponentFileType.TRANSITION);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.TRANSITION);
        }

        public Collection<ComponentPreset> load(Map<String, Material> materialMap, File file) throws
                                                                                              FileNotFoundException {
            List<String[]> data = RocksimComponentFileLoader.load(file);
            return commonLoader(data, keyMap, materialMap, ComponentPreset.Type.TRANSITION);
        }
    }

    static class MaterialLoader {
        private final static int MATERIAL_INDEX = 0;
        private final static int UNITS_INDEX = 1;
        private final static int DENSITY_INDEX = 2;
        private final static int LOW_INDEX = 3;
        private final static int HIGH_INDEX = 4;
        private final static int CLASS_INDEX = 5;
        private final static int ROCKETRY_USE_INDEX = 6;
        private final static int BODY_TUBES_INDEX = 7;
        public static final int FIN_SETS_INDEX = 8;
        public static final int LAUNCH_LUGS_INDEX = 9;
        public static final int CORDS_INDEX = 10;
        public static final int NOSE_INDEX = 11;
        public static final int PARACHUTE_INDEX = 12;
        public static final int STREAMER_INDEX = 13;
        public static final int TRANSITION_INDEX = 14;
        public static final int RING_INDEX = 15;
        public static final int BULKHEAD_INDEX = 16;
        public static final int ENGINE_BLOCK_INDEX = 17;
        public static final int SLEEVE_INDEX = 18;
        public static final int TUBE_COUPLER_INDEX = 19;
        public static final int KNOWN_DIM_TYPE_INDEX = 27;
        public static final int KNOWN_DIM_UNITS_INDEX = 28;
        public static final int KNOWN_DIM_VALUE_INDEX = 29;

        public final static List<TypedKey<?>> keyMap = new ArrayList<TypedKey<?>>(8);
        public final static TypedKey<String> MATERIAL_NAME = new TypedKey<String>("Material Name", String.class);
        public final static TypedKey<Double> DENSITY = new TypedKey<Double>("Density", Double.class);

        static class MaterialAdapter {
            Material.Type type;
            double conversionFactor;

            MaterialAdapter(Material.Type theType, double cf) {
                type = theType;
                conversionFactor = cf;
            }
        }

        private final static Map<String, MaterialAdapter> materialAdapterMap = new HashMap<String, MaterialAdapter>();

        static {
            materialAdapterMap.put("g/cm", new MaterialAdapter(Material.Type.LINE, 0.1d));
            materialAdapterMap.put("g/cm2", new MaterialAdapter(Material.Type.SURFACE, 10.0d));
            materialAdapterMap.put("g/cm3", new MaterialAdapter(Material.Type.BULK, 1000.0d));
            materialAdapterMap.put("kg/m3", new MaterialAdapter(Material.Type.BULK, 1d));
            materialAdapterMap.put("lb/ft3", new MaterialAdapter(Material.Type.BULK, 16.0184634d));
            materialAdapterMap.put("oz/in", new MaterialAdapter(Material.Type.LINE, 1.11612296d));
            materialAdapterMap.put("oz/in2", new MaterialAdapter(Material.Type.SURFACE, 43.9418487));

            keyMap.add(MATERIAL_INDEX, MATERIAL_NAME);
            keyMap.add(UNITS_INDEX, UNITS_OF_MEASURE);
            keyMap.add(DENSITY_INDEX, DENSITY);
        }

        static Map<String, Material> load() {
            List<String[]> data = RocksimComponentFileLoader.load(RocksimComponentFileType.MATERIAL);
            Map<String, Material> materialMap = new HashMap<String, Material>();

            for (int i = 0; i < data.size(); i++) {
                try {
                    String[] strings = data.get(i);
                    MaterialAdapter ma = materialAdapterMap.get(strings[UNITS_INDEX]);
                    double metricDensity = ma.conversionFactor * Double.parseDouble(strings[DENSITY_INDEX]);
                    final String cleanedMaterialName = stripAll(strings[MATERIAL_INDEX], '"').trim();
                    final Material material = Databases.findMaterial(ma.type, cleanedMaterialName,
                            metricDensity, true);
                    materialMap.put(cleanedMaterialName, material);
                    materialMap.put(cleanedMaterialName.toLowerCase(), material);
                    materialMap.put(toCamelCase(cleanedMaterialName), material);
                }
                catch (Exception e) {
                    //Trap a bad row and move on
                    //TODO: log it?  Display to user?
                }
            }
            return materialMap;
        }
    }

    public static void main(String[] args) {
        Application.setPreferences(new SwingPreferences());
        Map<String, Material> materialMap = MaterialLoader.load();
        Collection<ComponentPreset> presetNC = new NoseConeLoader().load(materialMap);
        Collection<ComponentPreset> presetBC = new BodyTubeLoader().load(materialMap);
        Collection<ComponentPreset> presetBH = new BulkheadLoader().load(materialMap);
        Collection<ComponentPreset> presetCR = new CenteringRingLoader().load(materialMap);
        Collection<ComponentPreset> presetTC = new TubeCouplerLoader().load(materialMap);
        Collection<ComponentPreset> presetTR = new TransitionLoader().load(materialMap);
        Collection<ComponentPreset> presetEB = new EngineBlockLoader().load(materialMap);
/*
        for (Iterator<ComponentPreset> iterator = presetNC.iterator(); iterator.hasNext(); ) {
            ComponentPreset next = iterator.next();
            System.err.println(next);
        }
        for (Iterator<ComponentPreset> iterator = presetBC.iterator(); iterator.hasNext(); ) {
            ComponentPreset next = iterator.next();
            System.err.println(next);
        }
        for (Iterator<ComponentPreset> iterator = presetBH.iterator(); iterator.hasNext(); ) {
            ComponentPreset next = iterator.next();
            System.err.println(next);
        }
        for (Iterator<ComponentPreset> iterator = presetCR.iterator(); iterator.hasNext(); ) {
            ComponentPreset next = iterator.next();
            System.err.println(next);
        }
        for (Iterator<ComponentPreset> iterator = presetTC.iterator(); iterator.hasNext(); ) {
            ComponentPreset next = iterator.next();
            System.err.println(next);
        }
        for (Iterator<ComponentPreset> iterator = presetTR.iterator(); iterator.hasNext(); ) {
            ComponentPreset next = iterator.next();
            System.err.println(next);
        }
        for (Iterator<ComponentPreset> iterator = presetEB.iterator(); iterator.hasNext(); ) {
            ComponentPreset next = iterator.next();
            System.err.println(next);
        }
*/
        List<ComponentPreset> allPresets = new ArrayList<ComponentPreset>();
        allPresets.addAll(presetBC);
        allPresets.addAll(presetBH);
        allPresets.addAll(presetCR);
        allPresets.addAll(presetEB);
        allPresets.addAll(presetNC);
        allPresets.addAll(presetTC);
        allPresets.addAll(presetTR);

        String xml = new OpenRocketComponentSaver().marshalToOpenRocketComponent(new ArrayList<Material>(materialMap.values()), allPresets);
        System.err.println(xml);
        try {
            List<ComponentPreset> presets = new OpenRocketComponentSaver().unmarshalFromOpenRocketComponent(new StringReader(xml));
        }
        catch (InvalidComponentPresetException e) {
            e.printStackTrace();
        }
    }
}

//Errata:
//The oddities I've found thus far in the stock Rocksim data:
//1. BTDATA.CSV - Totally Tubular goofed up their part no. and description columns (They messed up TCDATA also)
//2. NCDATA.CSV - Estes Balsa nose cones are classified as G10 Fiberglass
//3. TRDATA.CSV - Apogee Saturn LEM Transition has no part number; Balsa Machining transitions have blank diameter
