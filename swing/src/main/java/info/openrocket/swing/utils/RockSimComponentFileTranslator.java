package info.openrocket.swing.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.List;

import info.openrocket.core.material.Material;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.loader.BodyTubeLoader;
import info.openrocket.core.preset.loader.BulkHeadLoader;
import info.openrocket.core.preset.loader.CenteringRingLoader;
import info.openrocket.core.preset.loader.EngineBlockLoader;
import info.openrocket.core.preset.loader.LaunchLugLoader;
import info.openrocket.core.preset.loader.MaterialHolder;
import info.openrocket.core.preset.loader.MaterialLoader;
import info.openrocket.core.preset.loader.NoseConeLoader;
import info.openrocket.core.preset.loader.ParachuteLoader;
import info.openrocket.core.preset.loader.StreamerLoader;
import info.openrocket.core.preset.loader.TransitionLoader;
import info.openrocket.core.preset.loader.TubeCouplerLoader;
import info.openrocket.core.preset.xml.OpenRocketComponentSaver;
import info.openrocket.core.util.ArrayList;

public class RockSimComponentFileTranslator {

    private static PrintStream LOGGER = System.err;

    private static void printUsage() {
        LOGGER.println("RockSimComponentFileLoader <dir> <file>");
        LOGGER.println("<dir> is base directory for a set of RockSim component csv files");
        LOGGER.println("<file> is where the orc file is written");
    }

    public static void main(String[] args) throws Exception {

		BasicApplication app = new BasicApplication();
		app.initializeApplication();
		
        // How to control logging?

        if (args.length < 2 || args.length > 2) {
            printUsage();
            throw new IllegalArgumentException("Invalid Command Line Params");
        }

        List<ComponentPreset> allPresets = new ArrayList<ComponentPreset>();

        LOGGER.println("Loading csv files from directory " + args[0]);

        MaterialHolder materialMap = loadAll(allPresets, new File(args[0]));
        LOGGER.println("\tMarshalling to XML");
        String xml = new OpenRocketComponentSaver().marshalToOpenRocketComponent(new ArrayList<Material>(materialMap.values()), allPresets, true);

        // Try parsing the file
        LOGGER.println("\tValidating XML");
        // Throw away the result, we're just parsing for validation.
        new OpenRocketComponentSaver().unmarshalFromOpenRocketComponent(new StringReader(xml));

        LOGGER.println("\tWriting to file " + args[1]);
        File outfile = new File(args[1]);
        FileWriter fos = new FileWriter(outfile);
        fos.write(xml);
        fos.flush();
        fos.close();
    }

    /**
     * Set a print stream as a logger.  Defaults to System.err.
     *
     * @param ps a stream to log to
     */
    public static void setLogger(PrintStream ps) {
        if (ps != null) {
            LOGGER = ps;
        }
    }

    /**
     * Load all presets.  The loaded presets are added to the list parameter.  The loaded materials are returned in the
     * MaterialHolder instance.
     *
     * @param theAllPresets a list of ComponentPreset that gets populated as the result of loading; must not be null on
     *                      invocation
     *
     * @return a holder of the materials loaded
     */
    public static MaterialHolder loadAll(final List<ComponentPreset> theAllPresets, File theBasePathToLoadFrom) {
        MaterialLoader mats = new MaterialLoader(theBasePathToLoadFrom);
        mats.load();

        MaterialHolder materialMap = mats.getMaterialMap();
        LOGGER.println("\tMaterial types loaded: " + materialMap.size());

        {
            BodyTubeLoader bts = new BodyTubeLoader(materialMap, theBasePathToLoadFrom);
            bts.load();
            theAllPresets.addAll(bts.getPresets());
            LOGGER.println("\tBody Tubes loaded: " + bts.getPresets().size());
        }
        {
            BulkHeadLoader bhs = new BulkHeadLoader(materialMap, theBasePathToLoadFrom);
            bhs.load();
            theAllPresets.addAll(bhs.getPresets());
            LOGGER.println("\tBulkheads loaded: " + bhs.getPresets().size());
        }
        {
            CenteringRingLoader crs = new CenteringRingLoader(materialMap, theBasePathToLoadFrom);
            crs.load();
            theAllPresets.addAll(crs.getPresets());
            LOGGER.println("\tCentering Rings loaded: " + crs.getPresets().size());
        }
        {
            TubeCouplerLoader tcs = new TubeCouplerLoader(materialMap, theBasePathToLoadFrom);
            tcs.load();
            theAllPresets.addAll(tcs.getPresets());
            LOGGER.println("\tTube Couplers loaded: " + tcs.getPresets().size());
        }
        {
            EngineBlockLoader ebs = new EngineBlockLoader(materialMap, theBasePathToLoadFrom);
            ebs.load();
            theAllPresets.addAll(ebs.getPresets());
            LOGGER.println("\tEngine Blocks loaded: " + ebs.getPresets().size());
        }
        {
            NoseConeLoader ncs = new NoseConeLoader(materialMap, theBasePathToLoadFrom);
            ncs.load();
            theAllPresets.addAll(ncs.getPresets());
            LOGGER.println("\tNose Cones loaded: " + ncs.getPresets().size());
        }
        {
            TransitionLoader trs = new TransitionLoader(materialMap, theBasePathToLoadFrom);
            trs.load();
            theAllPresets.addAll(trs.getPresets());
            LOGGER.println("\tTransitions loaded: " + trs.getPresets().size());
        }
        {
            LaunchLugLoader lls = new LaunchLugLoader(materialMap, theBasePathToLoadFrom);
            lls.load();
            theAllPresets.addAll(lls.getPresets());
            LOGGER.println("\tLaunch Lugs loaded: " + lls.getPresets().size());
        }
        {
            StreamerLoader sts = new StreamerLoader(materialMap, theBasePathToLoadFrom);
            sts.load();
            theAllPresets.addAll(sts.getPresets());
            LOGGER.println("\tStreamers loaded: " + sts.getPresets().size());
        }
        {
            ParachuteLoader pcs = new ParachuteLoader(materialMap, theBasePathToLoadFrom);
            pcs.load();
            theAllPresets.addAll(pcs.getPresets());
            LOGGER.println("Parachutes loaded: " + pcs.getPresets().size());
        }
        return materialMap;
    }

}
