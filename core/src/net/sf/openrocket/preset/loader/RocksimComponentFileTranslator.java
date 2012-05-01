package net.sf.openrocket.preset.loader;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.xml.OpenRocketComponentSaver;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Startup;
import net.sf.openrocket.util.ArrayList;

public class RocksimComponentFileTranslator {

	private static void printUsage() {
		System.err.println("RocksimComponentFileLoader <dir> <file>");
		System.err.println("<dir> is base directory for a set of Rocksim component csv files");
		System.err.println("<file> is where the orc file is written");
	}

	public static void main(String[] args) throws Exception {
		
		// How to control logging?
		
		if ( args.length < 2 || args.length > 2 ) {
			printUsage();
			throw new IllegalArgumentException("Invalid Command Line Params");
		}

		List<ComponentPreset> allPresets = new ArrayList<ComponentPreset>();

		RocksimComponentFileLoader.basePath = args[0];
		
		System.err.println("Loading csv files from directory " + args[0]);
		
		Startup.initializeLogging();
		Application.setPreferences(new SwingPreferences());

		MaterialLoader mats = new MaterialLoader();
		mats.load();

		Map<String, Material> materialMap = mats.getMaterialMap();
		System.err.println("\tMaterial types loaded: " + materialMap.size());
		
		{
			BodyTubeLoader bts = new BodyTubeLoader(materialMap);
			bts.load();
			allPresets.addAll(bts.getPresets());
			System.err.println("\tBody Tubes loaded: " + bts.getPresets().size());
		}
		{
			BulkHeadLoader bhs = new BulkHeadLoader(materialMap);
			bhs.load();
			allPresets.addAll(bhs.getPresets());
			System.err.println("\tBulkheads loaded: " + bhs.getPresets().size());
		}
		{
			CenteringRingLoader crs = new CenteringRingLoader(materialMap);
			crs.load();
			allPresets.addAll(crs.getPresets());
			System.err.println("\tCentering Rings loaded: " + crs.getPresets().size());
		}
		{
			TubeCouplerLoader tcs = new TubeCouplerLoader(materialMap);
			tcs.load();
			allPresets.addAll(tcs.getPresets());
			System.err.println("\tTube Couplers loaded: " + tcs.getPresets().size());
		}
		{
			EngineBlockLoader ebs = new EngineBlockLoader(materialMap);
			ebs.load();
			allPresets.addAll(ebs.getPresets());
			System.err.println("\tEngine Blocks loaded: " + ebs.getPresets().size());
		}
		{
			NoseConeLoader ncs = new NoseConeLoader(materialMap);
			ncs.load();
			allPresets.addAll(ncs.getPresets());
			System.err.println("\tNose Cones loaded: " + ncs.getPresets().size());
		}
		{
			TransitionLoader trs = new TransitionLoader(materialMap);
			trs.load();
			allPresets.addAll(trs.getPresets());
			System.err.println("\tTransitions loaded: " + trs.getPresets().size());
		}
		{
			LaunchLugLoader lls = new LaunchLugLoader(materialMap);
			lls.load();
			allPresets.addAll(lls.getPresets());
			System.err.println("\tLaunch Lugs loaded: " + lls.getPresets().size());
		}
		{
			StreamerLoader sts = new StreamerLoader(materialMap);
			sts.load();
			allPresets.addAll(sts.getPresets());
			System.err.println("\tStreamers loaded: " + sts.getPresets().size());
		}
		{
			ParachuteLoader pcs = new ParachuteLoader(materialMap);
			pcs.load();
			allPresets.addAll(pcs.getPresets());
			System.err.println("Parachutes loaded: " + pcs.getPresets().size());
		}
		System.err.println("\tMarshalling to XML");
		String xml = new OpenRocketComponentSaver().marshalToOpenRocketComponent(new ArrayList<Material>(materialMap.values()), allPresets);
		
		// Try parsing the file
		System.err.println("\tValidating XML");
		List<ComponentPreset> presets = new OpenRocketComponentSaver().unmarshalFromOpenRocketComponent(new StringReader(xml));
		
		System.err.println("\tWriting to file " + args[1]);
		File outfile = new File(args[1]);
		FileWriter fos = new FileWriter(outfile);
		fos.write(xml);
		fos.flush();
		fos.close();
		
	}

}
