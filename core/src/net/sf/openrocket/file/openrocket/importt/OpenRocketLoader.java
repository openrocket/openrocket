package net.sf.openrocket.file.openrocket.importt;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.Simulation.Status;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.AbstractRocketLoader;
import net.sf.openrocket.file.MotorFinder;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.file.simplesax.SimpleSAX;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.BodyComponent;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.ClusterConfiguration;
import net.sf.openrocket.rocketcomponent.Clusterable;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FinSet.TabRelativePosition;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.IllegalFinPointException;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.InternalComponent;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.ReferenceType;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.rocketcomponent.ShockCord;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.StructuralComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.rocketcomponent.ThicknessRingComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.simulation.CustomExpression;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.FlightEvent.Type;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.GeodeticComputationStrategy;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.Reflection;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Class that loads a rocket definition from an OpenRocket rocket file.
 * <p>
 * This class uses SAX to read the XML file format.  The 
 * #loadFromStream(InputStream) method simply sets the system up and 
 * starts the parsing, while the actual logic is in the private inner class
 * <code>OpenRocketHandler</code>.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OpenRocketLoader extends AbstractRocketLoader {
	private static final LogHelper log = Application.getLogger();


	@Override
	public OpenRocketDocument loadFromStream(InputStream source, MotorFinder motorFinder) throws RocketLoadException,
	IOException {
		log.info("Loading .ork file");
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setMotorFinder(motorFinder);

		InputSource xmlSource = new InputSource(source);
		OpenRocketHandler handler = new OpenRocketHandler(context);


		try {
			SimpleSAX.readXML(xmlSource, handler, warnings);
		} catch (SAXException e) {
			log.warn("Malformed XML in input");
			throw new RocketLoadException("Malformed XML in input.", e);
		}


		OpenRocketDocument doc = handler.getDocument();
		doc.getDefaultConfiguration().setAllStages();

		// Deduce suitable time skip
		double timeSkip = StorageOptions.SIMULATION_DATA_NONE;
		for (Simulation s : doc.getSimulations()) {
			if (s.getStatus() == Simulation.Status.EXTERNAL ||
					s.getStatus() == Simulation.Status.NOT_SIMULATED)
				continue;
			if (s.getSimulatedData() == null)
				continue;
			if (s.getSimulatedData().getBranchCount() == 0)
				continue;
			FlightDataBranch branch = s.getSimulatedData().getBranch(0);
			if (branch == null)
				continue;
			List<Double> list = branch.get(FlightDataType.TYPE_TIME);
			if (list == null)
				continue;

			double previousTime = Double.NaN;
			for (double time : list) {
				if (time - previousTime < timeSkip)
					timeSkip = time - previousTime;
				previousTime = time;
			}
		}
		// Round value
		timeSkip = Math.rint(timeSkip * 100) / 100;

		doc.getDefaultStorageOptions().setSimulationTimeSkip(timeSkip);
		doc.getDefaultStorageOptions().setCompressionEnabled(false); // Set by caller if compressed
		doc.getDefaultStorageOptions().setExplicitlySet(false);

		doc.clearUndo();
		log.info("Loading done");
		return doc;
	}

}



class DocumentConfig {

	/* Remember to update OpenRocketSaver as well! */
	public static final String[] SUPPORTED_VERSIONS = { "1.0", "1.1", "1.2", "1.3", "1.4", "1.5" };

	/**
	 * Divisor used in converting an integer version to the point-represented version.
	 * The integer version divided by this value is the major version and the remainder is
	 * the minor version.  For example 101 corresponds to file version "1.1".
	 */
	public static final int FILE_VERSION_DIVISOR = 100;


	////////  Component constructors
	static final HashMap<String, Constructor<? extends RocketComponent>> constructors = new HashMap<String, Constructor<? extends RocketComponent>>();
	static {
		try {
			// External components
			constructors.put("bodytube", BodyTube.class.getConstructor(new Class<?>[0]));
			constructors.put("transition", Transition.class.getConstructor(new Class<?>[0]));
			constructors.put("nosecone", NoseCone.class.getConstructor(new Class<?>[0]));
			constructors.put("trapezoidfinset", TrapezoidFinSet.class.getConstructor(new Class<?>[0]));
			constructors.put("ellipticalfinset", EllipticalFinSet.class.getConstructor(new Class<?>[0]));
			constructors.put("freeformfinset", FreeformFinSet.class.getConstructor(new Class<?>[0]));
			constructors.put("launchlug", LaunchLug.class.getConstructor(new Class<?>[0]));

			// Internal components
			constructors.put("engineblock", EngineBlock.class.getConstructor(new Class<?>[0]));
			constructors.put("innertube", InnerTube.class.getConstructor(new Class<?>[0]));
			constructors.put("tubecoupler", TubeCoupler.class.getConstructor(new Class<?>[0]));
			constructors.put("bulkhead", Bulkhead.class.getConstructor(new Class<?>[0]));
			constructors.put("centeringring", CenteringRing.class.getConstructor(new Class<?>[0]));

			constructors.put("masscomponent", MassComponent.class.getConstructor(new Class<?>[0]));
			constructors.put("shockcord", ShockCord.class.getConstructor(new Class<?>[0]));
			constructors.put("parachute", Parachute.class.getConstructor(new Class<?>[0]));
			constructors.put("streamer", Streamer.class.getConstructor(new Class<?>[0]));

			// Other
			constructors.put("stage", Stage.class.getConstructor(new Class<?>[0]));

		} catch (NoSuchMethodException e) {
			throw new BugException(
					"Error in constructing the 'constructors' HashMap.");
		}
	}


	////////  Parameter setters
	/*
	 * The keys are of the form Class:param, where Class is the class name and param
	 * the element name.  Setters are searched for in descending class order.
	 * A setter of null means setting the parameter is not allowed.
	 */
	static final HashMap<String, Setter> setters = new HashMap<String, Setter>();
	static {
		// RocketComponent
		setters.put("RocketComponent:name", new StringSetter(
				Reflection.findMethod(RocketComponent.class, "setName", String.class)));
		setters.put("RocketComponent:color", new ColorSetter(
				Reflection.findMethod(RocketComponent.class, "setColor", Color.class)));
		setters.put("RocketComponent:linestyle", new EnumSetter<LineStyle>(
				Reflection.findMethod(RocketComponent.class, "setLineStyle", LineStyle.class),
				LineStyle.class));
		setters.put("RocketComponent:position", new PositionSetter());
		setters.put("RocketComponent:overridemass", new OverrideSetter(
				Reflection.findMethod(RocketComponent.class, "setOverrideMass", double.class),
				Reflection.findMethod(RocketComponent.class, "setMassOverridden", boolean.class)));
		setters.put("RocketComponent:overridecg", new OverrideSetter(
				Reflection.findMethod(RocketComponent.class, "setOverrideCGX", double.class),
				Reflection.findMethod(RocketComponent.class, "setCGOverridden", boolean.class)));
		setters.put("RocketComponent:overridesubcomponents", new BooleanSetter(
				Reflection.findMethod(RocketComponent.class, "setOverrideSubcomponents", boolean.class)));
		setters.put("RocketComponent:comment", new StringSetter(
				Reflection.findMethod(RocketComponent.class, "setComment", String.class)));
		setters.put("RocketComponent:preset", new ComponentPresetSetter(
				Reflection.findMethod(RocketComponent.class, "loadPreset", ComponentPreset.class)));

		// ExternalComponent
		setters.put("ExternalComponent:finish", new EnumSetter<Finish>(
				Reflection.findMethod(ExternalComponent.class, "setFinish", Finish.class),
				Finish.class));
		setters.put("ExternalComponent:material", new MaterialSetter(
				Reflection.findMethod(ExternalComponent.class, "setMaterial", Material.class),
				Material.Type.BULK));

		// BodyComponent
		setters.put("BodyComponent:length", new DoubleSetter(
				Reflection.findMethod(BodyComponent.class, "setLength", double.class)));

		// SymmetricComponent
		setters.put("SymmetricComponent:thickness", new DoubleSetter(
				Reflection.findMethod(SymmetricComponent.class, "setThickness", double.class),
				"filled",
				Reflection.findMethod(SymmetricComponent.class, "setFilled", boolean.class)));

		// BodyTube
		setters.put("BodyTube:radius", new DoubleSetter(
				Reflection.findMethod(BodyTube.class, "setOuterRadius", double.class),
				"auto",
				Reflection.findMethod(BodyTube.class, "setOuterRadiusAutomatic", boolean.class)));

		// Transition
		setters.put("Transition:shape", new EnumSetter<Transition.Shape>(
				Reflection.findMethod(Transition.class, "setType", Transition.Shape.class),
				Transition.Shape.class));
		setters.put("Transition:shapeclipped", new BooleanSetter(
				Reflection.findMethod(Transition.class, "setClipped", boolean.class)));
		setters.put("Transition:shapeparameter", new DoubleSetter(
				Reflection.findMethod(Transition.class, "setShapeParameter", double.class)));

		setters.put("Transition:foreradius", new DoubleSetter(
				Reflection.findMethod(Transition.class, "setForeRadius", double.class),
				"auto",
				Reflection.findMethod(Transition.class, "setForeRadiusAutomatic", boolean.class)));
		setters.put("Transition:aftradius", new DoubleSetter(
				Reflection.findMethod(Transition.class, "setAftRadius", double.class),
				"auto",
				Reflection.findMethod(Transition.class, "setAftRadiusAutomatic", boolean.class)));

		setters.put("Transition:foreshoulderradius", new DoubleSetter(
				Reflection.findMethod(Transition.class, "setForeShoulderRadius", double.class)));
		setters.put("Transition:foreshoulderlength", new DoubleSetter(
				Reflection.findMethod(Transition.class, "setForeShoulderLength", double.class)));
		setters.put("Transition:foreshoulderthickness", new DoubleSetter(
				Reflection.findMethod(Transition.class, "setForeShoulderThickness", double.class)));
		setters.put("Transition:foreshouldercapped", new BooleanSetter(
				Reflection.findMethod(Transition.class, "setForeShoulderCapped", boolean.class)));

		setters.put("Transition:aftshoulderradius", new DoubleSetter(
				Reflection.findMethod(Transition.class, "setAftShoulderRadius", double.class)));
		setters.put("Transition:aftshoulderlength", new DoubleSetter(
				Reflection.findMethod(Transition.class, "setAftShoulderLength", double.class)));
		setters.put("Transition:aftshoulderthickness", new DoubleSetter(
				Reflection.findMethod(Transition.class, "setAftShoulderThickness", double.class)));
		setters.put("Transition:aftshouldercapped", new BooleanSetter(
				Reflection.findMethod(Transition.class, "setAftShoulderCapped", boolean.class)));

		// NoseCone - disable disallowed elements
		setters.put("NoseCone:foreradius", null);
		setters.put("NoseCone:foreshoulderradius", null);
		setters.put("NoseCone:foreshoulderlength", null);
		setters.put("NoseCone:foreshoulderthickness", null);
		setters.put("NoseCone:foreshouldercapped", null);

		// FinSet
		setters.put("FinSet:fincount", new IntSetter(
				Reflection.findMethod(FinSet.class, "setFinCount", int.class)));
		setters.put("FinSet:rotation", new DoubleSetter(
				Reflection.findMethod(FinSet.class, "setBaseRotation", double.class), Math.PI / 180.0));
		setters.put("FinSet:thickness", new DoubleSetter(
				Reflection.findMethod(FinSet.class, "setThickness", double.class)));
		setters.put("FinSet:crosssection", new EnumSetter<FinSet.CrossSection>(
				Reflection.findMethod(FinSet.class, "setCrossSection", FinSet.CrossSection.class),
				FinSet.CrossSection.class));
		setters.put("FinSet:cant", new DoubleSetter(
				Reflection.findMethod(FinSet.class, "setCantAngle", double.class), Math.PI / 180.0));
		setters.put("FinSet:tabheight", new DoubleSetter(
				Reflection.findMethod(FinSet.class, "setTabHeight", double.class)));
		setters.put("FinSet:tablength", new DoubleSetter(
				Reflection.findMethod(FinSet.class, "setTabLength", double.class)));
		setters.put("FinSet:tabposition", new FinTabPositionSetter());

		// TrapezoidFinSet
		setters.put("TrapezoidFinSet:rootchord", new DoubleSetter(
				Reflection.findMethod(TrapezoidFinSet.class, "setRootChord", double.class)));
		setters.put("TrapezoidFinSet:tipchord", new DoubleSetter(
				Reflection.findMethod(TrapezoidFinSet.class, "setTipChord", double.class)));
		setters.put("TrapezoidFinSet:sweeplength", new DoubleSetter(
				Reflection.findMethod(TrapezoidFinSet.class, "setSweep", double.class)));
		setters.put("TrapezoidFinSet:height", new DoubleSetter(
				Reflection.findMethod(TrapezoidFinSet.class, "setHeight", double.class)));

		// EllipticalFinSet
		setters.put("EllipticalFinSet:rootchord", new DoubleSetter(
				Reflection.findMethod(EllipticalFinSet.class, "setLength", double.class)));
		setters.put("EllipticalFinSet:height", new DoubleSetter(
				Reflection.findMethod(EllipticalFinSet.class, "setHeight", double.class)));

		// FreeformFinSet points handled as a special handler

		// LaunchLug
		setters.put("LaunchLug:radius", new DoubleSetter(
				Reflection.findMethod(LaunchLug.class, "setOuterRadius", double.class)));
		setters.put("LaunchLug:length", new DoubleSetter(
				Reflection.findMethod(LaunchLug.class, "setLength", double.class)));
		setters.put("LaunchLug:thickness", new DoubleSetter(
				Reflection.findMethod(LaunchLug.class, "setThickness", double.class)));
		setters.put("LaunchLug:radialdirection", new DoubleSetter(
				Reflection.findMethod(LaunchLug.class, "setRadialDirection", double.class),
				Math.PI / 180.0));

		// InternalComponent - nothing

		// StructuralComponent
		setters.put("StructuralComponent:material", new MaterialSetter(
				Reflection.findMethod(StructuralComponent.class, "setMaterial", Material.class),
				Material.Type.BULK));

		// RingComponent
		setters.put("RingComponent:length", new DoubleSetter(
				Reflection.findMethod(RingComponent.class, "setLength", double.class)));
		setters.put("RingComponent:radialposition", new DoubleSetter(
				Reflection.findMethod(RingComponent.class, "setRadialPosition", double.class)));
		setters.put("RingComponent:radialdirection", new DoubleSetter(
				Reflection.findMethod(RingComponent.class, "setRadialDirection", double.class),
				Math.PI / 180.0));

		// ThicknessRingComponent - radius on separate components due to differing automatics
		setters.put("ThicknessRingComponent:thickness", new DoubleSetter(
				Reflection.findMethod(ThicknessRingComponent.class, "setThickness", double.class)));

		// EngineBlock
		setters.put("EngineBlock:outerradius", new DoubleSetter(
				Reflection.findMethod(EngineBlock.class, "setOuterRadius", double.class),
				"auto",
				Reflection.findMethod(EngineBlock.class, "setOuterRadiusAutomatic", boolean.class)));

		// TubeCoupler
		setters.put("TubeCoupler:outerradius", new DoubleSetter(
				Reflection.findMethod(TubeCoupler.class, "setOuterRadius", double.class),
				"auto",
				Reflection.findMethod(TubeCoupler.class, "setOuterRadiusAutomatic", boolean.class)));

		// InnerTube
		setters.put("InnerTube:outerradius", new DoubleSetter(
				Reflection.findMethod(InnerTube.class, "setOuterRadius", double.class)));
		setters.put("InnerTube:clusterconfiguration", new ClusterConfigurationSetter());
		setters.put("InnerTube:clusterscale", new DoubleSetter(
				Reflection.findMethod(InnerTube.class, "setClusterScale", double.class)));
		setters.put("InnerTube:clusterrotation", new DoubleSetter(
				Reflection.findMethod(InnerTube.class, "setClusterRotation", double.class),
				Math.PI / 180.0));

		// RadiusRingComponent

		// Bulkhead
		setters.put("RadiusRingComponent:innerradius", new DoubleSetter(
				Reflection.findMethod(RadiusRingComponent.class, "setInnerRadius", double.class)));
		setters.put("Bulkhead:outerradius", new DoubleSetter(
				Reflection.findMethod(Bulkhead.class, "setOuterRadius", double.class),
				"auto",
				Reflection.findMethod(Bulkhead.class, "setOuterRadiusAutomatic", boolean.class)));

		// CenteringRing
		setters.put("CenteringRing:innerradius", new DoubleSetter(
				Reflection.findMethod(CenteringRing.class, "setInnerRadius", double.class),
				"auto",
				Reflection.findMethod(CenteringRing.class, "setInnerRadiusAutomatic", boolean.class)));
		setters.put("CenteringRing:outerradius", new DoubleSetter(
				Reflection.findMethod(CenteringRing.class, "setOuterRadius", double.class),
				"auto",
				Reflection.findMethod(CenteringRing.class, "setOuterRadiusAutomatic", boolean.class)));


		// MassObject
		setters.put("MassObject:packedlength", new DoubleSetter(
				Reflection.findMethod(MassObject.class, "setLength", double.class)));
		setters.put("MassObject:packedradius", new DoubleSetter(
				Reflection.findMethod(MassObject.class, "setRadius", double.class)));
		setters.put("MassObject:radialposition", new DoubleSetter(
				Reflection.findMethod(MassObject.class, "setRadialPosition", double.class)));
		setters.put("MassObject:radialdirection", new DoubleSetter(
				Reflection.findMethod(MassObject.class, "setRadialDirection", double.class),
				Math.PI / 180.0));

		// MassComponent
		setters.put("MassComponent:mass", new DoubleSetter(
				Reflection.findMethod(MassComponent.class, "setComponentMass", double.class)));

		// ShockCord
		setters.put("ShockCord:cordlength", new DoubleSetter(
				Reflection.findMethod(ShockCord.class, "setCordLength", double.class)));
		setters.put("ShockCord:material", new MaterialSetter(
				Reflection.findMethod(ShockCord.class, "setMaterial", Material.class),
				Material.Type.LINE));

		// RecoveryDevice
		setters.put("RecoveryDevice:cd", new DoubleSetter(
				Reflection.findMethod(RecoveryDevice.class, "setCD", double.class),
				"auto",
				Reflection.findMethod(RecoveryDevice.class, "setCDAutomatic", boolean.class)));
		setters.put("RecoveryDevice:deployevent", new EnumSetter<RecoveryDevice.DeployEvent>(
				Reflection.findMethod(RecoveryDevice.class, "setDeployEvent", RecoveryDevice.DeployEvent.class),
				RecoveryDevice.DeployEvent.class));
		setters.put("RecoveryDevice:deployaltitude", new DoubleSetter(
				Reflection.findMethod(RecoveryDevice.class, "setDeployAltitude", double.class)));
		setters.put("RecoveryDevice:deploydelay", new DoubleSetter(
				Reflection.findMethod(RecoveryDevice.class, "setDeployDelay", double.class)));
		setters.put("RecoveryDevice:material", new MaterialSetter(
				Reflection.findMethod(RecoveryDevice.class, "setMaterial", Material.class),
				Material.Type.SURFACE));

		// Parachute
		setters.put("Parachute:diameter", new DoubleSetter(
				Reflection.findMethod(Parachute.class, "setDiameter", double.class)));
		setters.put("Parachute:linecount", new IntSetter(
				Reflection.findMethod(Parachute.class, "setLineCount", int.class)));
		setters.put("Parachute:linelength", new DoubleSetter(
				Reflection.findMethod(Parachute.class, "setLineLength", double.class)));
		setters.put("Parachute:linematerial", new MaterialSetter(
				Reflection.findMethod(Parachute.class, "setLineMaterial", Material.class),
				Material.Type.LINE));

		// Streamer
		setters.put("Streamer:striplength", new DoubleSetter(
				Reflection.findMethod(Streamer.class, "setStripLength", double.class)));
		setters.put("Streamer:stripwidth", new DoubleSetter(
				Reflection.findMethod(Streamer.class, "setStripWidth", double.class)));

		// Rocket
		// <motorconfiguration> handled by separate handler
		setters.put("Rocket:referencetype", new EnumSetter<ReferenceType>(
				Reflection.findMethod(Rocket.class, "setReferenceType", ReferenceType.class),
				ReferenceType.class));
		setters.put("Rocket:customreference", new DoubleSetter(
				Reflection.findMethod(Rocket.class, "setCustomReferenceLength", double.class)));
		setters.put("Rocket:designer", new StringSetter(
				Reflection.findMethod(Rocket.class, "setDesigner", String.class)));
		setters.put("Rocket:revision", new StringSetter(
				Reflection.findMethod(Rocket.class, "setRevision", String.class)));

		// Stage
		setters.put("Stage:separationevent", new EnumSetter<Stage.SeparationEvent>(
				Reflection.findMethod(Stage.class, "setSeparationEvent", Stage.SeparationEvent.class),
				Stage.SeparationEvent.class));
		setters.put("Stage:separationdelay", new DoubleSetter(
				Reflection.findMethod(Stage.class, "setSeparationDelay", double.class)));

	}


	/**
	 * Search for a enum value that has the corresponding name as an XML value.  The current
	 * conversion from enum name to XML value is to lowercase the name and strip out all 
	 * underscore characters.  This method returns a match to these criteria, or <code>null</code>
	 * if no such enum exists.
	 * 
	 * @param <T>			then enum type.
	 * @param name			the XML value, null ok.
	 * @param enumClass		the class of the enum.
	 * @return				the found enum value, or <code>null</code>.
	 */
	public static <T extends Enum<T>> Enum<T> findEnum(String name,
			Class<? extends Enum<T>> enumClass) {

		if (name == null)
			return null;
		name = name.trim();
		for (Enum<T> e : enumClass.getEnumConstants()) {
			if (e.name().toLowerCase(Locale.ENGLISH).replace("_", "").equals(name)) {
				return e;
			}
		}
		return null;
	}


	/**
	 * Convert a string to a double including formatting specifications of the OpenRocket
	 * file format.  This accepts all formatting that is valid for 
	 * <code>Double.parseDouble(s)</code> and a few others as well ("Inf", "-Inf").
	 * 
	 * @param s		the string to parse.
	 * @return		the numerical value.
	 * @throws NumberFormatException	the the string cannot be parsed.
	 */
	public static double stringToDouble(String s) throws NumberFormatException {
		if (s == null)
			throw new NumberFormatException("null string");
		if (s.equalsIgnoreCase("NaN"))
			return Double.NaN;
		if (s.equalsIgnoreCase("Inf"))
			return Double.POSITIVE_INFINITY;
		if (s.equalsIgnoreCase("-Inf"))
			return Double.NEGATIVE_INFINITY;
		return Double.parseDouble(s);
	}
}





/**
 * The starting point of the handlers.  Accepts a single <openrocket> element and hands
 * the contents to be read by a OpenRocketContentsHandler.
 */
class OpenRocketHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private OpenRocketContentHandler handler = null;

	public OpenRocketHandler(DocumentLoadingContext context) {
		this.context = context;
	}

	/**
	 * Return the OpenRocketDocument read from the file, or <code>null</code> if a document
	 * has not been read yet.
	 * 
	 * @return	the document read, or null.
	 */
	public OpenRocketDocument getDocument() {
		return handler.getDocument();
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		// Check for unknown elements
		if (!element.equals("openrocket")) {
			warnings.add(Warning.fromString("Unknown element " + element + ", ignoring."));
			return null;
		}

		// Check for first call
		if (handler != null) {
			warnings.add(Warning.fromString("Multiple document elements found, ignoring later "
					+ "ones."));
			return null;
		}

		// Check version number
		String version = null;
		String creator = attributes.remove("creator");
		String docVersion = attributes.remove("version");
		for (String v : DocumentConfig.SUPPORTED_VERSIONS) {
			if (v.equals(docVersion)) {
				version = v;
				break;
			}
		}
		if (version == null) {
			String str = "Unsupported document version";
			if (docVersion != null)
				str += " " + docVersion;
			if (creator != null && !creator.trim().equals(""))
				str += " (written using '" + creator.trim() + "')";
			str += ", attempting to read file anyway.";
			warnings.add(str);
		}

		context.setFileVersion(parseVersion(docVersion));

		handler = new OpenRocketContentHandler(context);
		return handler;
	}


	private int parseVersion(String docVersion) {
		if (docVersion == null)
			return 0;

		Matcher m = Pattern.compile("^([0-9]+)\\.([0-9]+)$").matcher(docVersion);
		if (m.matches()) {
			int major = Integer.parseInt(m.group(1));
			int minor = Integer.parseInt(m.group(2));
			return major * DocumentConfig.FILE_VERSION_DIVISOR + minor;
		} else {
			return 0;
		}
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		attributes.remove("version");
		attributes.remove("creator");
		super.closeElement(element, attributes, content, warnings);
	}


}


/**
 * Handles the content of the <openrocket> tag.
 */
class OpenRocketContentHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final OpenRocketDocument doc;
	private final Rocket rocket;

	private boolean rocketDefined = false;
	private boolean simulationsDefined = false;

	public OpenRocketContentHandler(DocumentLoadingContext context) {
		this.context = context;
		this.rocket = new Rocket();
		this.doc = new OpenRocketDocument(rocket);
	}


	public OpenRocketDocument getDocument() {
		if (!rocketDefined)
			return null;
		return doc;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		if (element.equals("rocket")) {
			if (rocketDefined) {
				warnings.add(Warning
						.fromString("Multiple rocket designs within one document, "
								+ "ignoring later ones."));
				return null;
			}
			rocketDefined = true;
			return new ComponentParameterHandler(rocket, context);
		}

		if (element.equals("simulations")) {
			if (simulationsDefined) {
				warnings.add(Warning
						.fromString("Multiple simulation definitions within one document, "
								+ "ignoring later ones."));
				return null;
			}
			simulationsDefined = true;
			return new SimulationsHandler(doc, context);
		}

		warnings.add(Warning.fromString("Unknown element " + element + ", ignoring."));

		return null;
	}
}




/**
 * A handler that creates components from the corresponding elements.  The control of the
 * contents is passed on to ComponentParameterHandler.
 */
class ComponentHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final RocketComponent parent;

	public ComponentHandler(RocketComponent parent, DocumentLoadingContext context) {
		this.parent = parent;
		this.context = context;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		// Attempt to construct new component
		Constructor<? extends RocketComponent> constructor = DocumentConfig.constructors
				.get(element);
		if (constructor == null) {
			warnings.add(Warning.fromString("Unknown element " + element + ", ignoring."));
			return null;
		}

		RocketComponent c;
		try {
			c = constructor.newInstance();
		} catch (InstantiationException e) {
			throw new BugException("Error constructing component.", e);
		} catch (IllegalAccessException e) {
			throw new BugException("Error constructing component.", e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleWrappedException(e);
		}

		parent.addChild(c);

		return new ComponentParameterHandler(c, context);
	}
}


/**
 * A handler that populates the parameters of a previously constructed rocket component.
 * This uses the setters, or delegates the handling to another handler for specific
 * elements.
 */
class ComponentParameterHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final RocketComponent component;

	public ComponentParameterHandler(RocketComponent c, DocumentLoadingContext context) {
		this.component = c;
		this.context = context;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		// Check for specific elements that contain other elements
		if (element.equals("subcomponents")) {
			return new ComponentHandler(component, context);
		}
		if (element.equals("motormount")) {
			if (!(component instanceof MotorMount)) {
				warnings.add(Warning.fromString("Illegal component defined as motor mount."));
				return null;
			}
			return new MotorMountHandler((MotorMount) component, context);
		}
		if (element.equals("finpoints")) {
			if (!(component instanceof FreeformFinSet)) {
				warnings.add(Warning.fromString("Illegal component defined for fin points."));
				return null;
			}
			return new FinSetPointHandler((FreeformFinSet) component, context);
		}
		if (element.equals("motorconfiguration")) {
			if (!(component instanceof Rocket)) {
				warnings.add(Warning.fromString("Illegal component defined for motor configuration."));
				return null;
			}
			return new MotorConfigurationHandler((Rocket) component, context);
		}


		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {

		if (element.equals("subcomponents") || element.equals("motormount") ||
				element.equals("finpoints") || element.equals("motorconfiguration")) {
			return;
		}

		// Search for the correct setter class

		Class<?> c;
		for (c = component.getClass(); c != null; c = c.getSuperclass()) {
			String setterKey = c.getSimpleName() + ":" + element;
			Setter s = DocumentConfig.setters.get(setterKey);
			if (s != null) {
				// Setter found
				s.set(component, content, attributes, warnings);
				break;
			}
			if (DocumentConfig.setters.containsKey(setterKey)) {
				// Key exists but is null -> invalid parameter
				c = null;
				break;
			}
		}
		if (c == null) {
			warnings.add(Warning.fromString("Unknown parameter type '" + element + "' for "
					+ component.getComponentName() + ", ignoring."));
		}
	}
}


/**
 * A handler that reads the <point> specifications within the freeformfinset's
 * <finpoints> elements.
 */
class FinSetPointHandler extends AbstractElementHandler {
	@SuppressWarnings("unused")
	private final DocumentLoadingContext context;
	private final FreeformFinSet finset;
	private final ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();

	public FinSetPointHandler(FreeformFinSet finset, DocumentLoadingContext context) {
		this.finset = finset;
		this.context = context;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}


	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {

		String strx = attributes.remove("x");
		String stry = attributes.remove("y");
		if (strx == null || stry == null) {
			warnings.add(Warning.fromString("Illegal fin points specification, ignoring."));
			return;
		}
		try {
			double x = Double.parseDouble(strx);
			double y = Double.parseDouble(stry);
			coordinates.add(new Coordinate(x, y));
		} catch (NumberFormatException e) {
			warnings.add(Warning.fromString("Illegal fin points specification, ignoring."));
			return;
		}

		super.closeElement(element, attributes, content, warnings);
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		try {
			finset.setPoints(coordinates.toArray(new Coordinate[0]));
		} catch (IllegalFinPointException e) {
			warnings.add(Warning.fromString("Freeform fin set point definitions illegal, ignoring."));
		}
	}
}


class MotorMountHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final MotorMount mount;
	private MotorHandler motorHandler;

	public MotorMountHandler(MotorMount mount, DocumentLoadingContext context) {
		this.mount = mount;
		this.context = context;
		mount.setMotorMount(true);
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		if (element.equals("motor")) {
			motorHandler = new MotorHandler(context);
			return motorHandler;
		}

		if (element.equals("ignitionevent") ||
				element.equals("ignitiondelay") ||
				element.equals("overhang")) {
			return PlainTextHandler.INSTANCE;
		}

		warnings.add(Warning.fromString("Unknown element '" + element + "' encountered, ignoring."));
		return null;
	}



	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {

		if (element.equals("motor")) {
			String id = attributes.get("configid");
			if (id == null || id.equals("")) {
				warnings.add(Warning.fromString("Illegal motor specification, ignoring."));
				return;
			}

			Motor motor = motorHandler.getMotor(warnings);
			mount.setMotor(id, motor);
			mount.setMotorDelay(id, motorHandler.getDelay(warnings));
			return;
		}

		if (element.equals("ignitionevent")) {
			MotorMount.IgnitionEvent event = null;
			for (MotorMount.IgnitionEvent e : MotorMount.IgnitionEvent.values()) {
				if (e.name().toLowerCase(Locale.ENGLISH).replaceAll("_", "").equals(content)) {
					event = e;
					break;
				}
			}
			if (event == null) {
				warnings.add(Warning.fromString("Unknown ignition event type '" + content + "', ignoring."));
				return;
			}
			mount.setIgnitionEvent(event);
			return;
		}

		if (element.equals("ignitiondelay")) {
			double d;
			try {
				d = Double.parseDouble(content);
			} catch (NumberFormatException nfe) {
				warnings.add(Warning.fromString("Illegal ignition delay specified, ignoring."));
				return;
			}
			mount.setIgnitionDelay(d);
			return;
		}

		if (element.equals("overhang")) {
			double d;
			try {
				d = Double.parseDouble(content);
			} catch (NumberFormatException nfe) {
				warnings.add(Warning.fromString("Illegal overhang specified, ignoring."));
				return;
			}
			mount.setMotorOverhang(d);
			return;
		}

		super.closeElement(element, attributes, content, warnings);
	}
}




class MotorConfigurationHandler extends AbstractElementHandler {
	@SuppressWarnings("unused")
	private final DocumentLoadingContext context;
	private final Rocket rocket;
	private String name = null;
	private boolean inNameElement = false;

	public MotorConfigurationHandler(Rocket rocket, DocumentLoadingContext context) {
		this.rocket = rocket;
		this.context = context;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		if (inNameElement || !element.equals("name")) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return null;
		}
		inNameElement = true;

		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		name = content;
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {

		String configid = attributes.remove("configid");
		if (configid == null || configid.equals("")) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		if (!rocket.addMotorConfigurationID(configid)) {
			warnings.add("Duplicate motor configuration ID used.");
			return;
		}

		if (name != null && name.trim().length() > 0) {
			rocket.setMotorConfigurationName(configid, name);
		}

		if ("true".equals(attributes.remove("default"))) {
			rocket.getDefaultConfiguration().setMotorConfigurationID(configid);
		}

		super.closeElement(element, attributes, content, warnings);
	}
}


class MotorHandler extends AbstractElementHandler {
	/** File version where latest digest format was introduced */
	private static final int MOTOR_DIGEST_VERSION = 104;

	private final DocumentLoadingContext context;
	private Motor.Type type = null;
	private String manufacturer = null;
	private String designation = null;
	private String digest = null;
	private double diameter = Double.NaN;
	private double length = Double.NaN;
	private double delay = Double.NaN;

	public MotorHandler(DocumentLoadingContext context) {
		this.context = context;
	}


	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}


	/**
	 * Return the motor to use, or null.
	 */
	public Motor getMotor(WarningSet warnings) {
		return context.getMotorFinder().findMotor(type, manufacturer, designation, diameter, length, digest, warnings);
	}

	/**
	 * Return the delay to use for the motor.
	 */
	public double getDelay(WarningSet warnings) {
		if (Double.isNaN(delay)) {
			warnings.add(Warning.fromString("Motor delay not specified, assuming no ejection charge."));
			return Motor.PLUGGED;
		}
		return delay;
	}


	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {

		content = content.trim();

		if (element.equals("type")) {

			// Motor type
			type = null;
			for (Motor.Type t : Motor.Type.values()) {
				if (t.name().toLowerCase(Locale.ENGLISH).equals(content.trim())) {
					type = t;
					break;
				}
			}
			if (type == null) {
				warnings.add(Warning.fromString("Unknown motor type '" + content + "', ignoring."));
			}

		} else if (element.equals("manufacturer")) {

			// Manufacturer
			manufacturer = content.trim();

		} else if (element.equals("designation")) {

			// Designation
			designation = content.trim();

		} else if (element.equals("digest")) {

			// Digest is used only for file versions saved using the same digest algorithm
			if (context.getFileVersion() >= MOTOR_DIGEST_VERSION) {
				digest = content.trim();
			}

		} else if (element.equals("diameter")) {

			// Diameter
			diameter = Double.NaN;
			try {
				diameter = Double.parseDouble(content.trim());
			} catch (NumberFormatException e) {
				// Ignore
			}
			if (Double.isNaN(diameter)) {
				warnings.add(Warning.fromString("Illegal motor diameter specified, ignoring."));
			}

		} else if (element.equals("length")) {

			// Length
			length = Double.NaN;
			try {
				length = Double.parseDouble(content.trim());
			} catch (NumberFormatException ignore) {
			}

			if (Double.isNaN(length)) {
				warnings.add(Warning.fromString("Illegal motor diameter specified, ignoring."));
			}

		} else if (element.equals("delay")) {

			// Delay
			delay = Double.NaN;
			if (content.equals("none")) {
				delay = Motor.PLUGGED;
			} else {
				try {
					delay = Double.parseDouble(content.trim());
				} catch (NumberFormatException ignore) {
				}

				if (Double.isNaN(delay)) {
					warnings.add(Warning.fromString("Illegal motor delay specified, ignoring."));
				}

			}

		} else {
			super.closeElement(element, attributes, content, warnings);
		}
	}

}



class SimulationsHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final OpenRocketDocument doc;
	private SingleSimulationHandler handler;

	public SimulationsHandler(OpenRocketDocument doc, DocumentLoadingContext context) {
		this.doc = doc;
		this.context = context;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		if (!element.equals("simulation")) {
			warnings.add("Unknown element '" + element + "', ignoring.");
			return null;
		}

		handler = new SingleSimulationHandler(doc, context);
		return handler;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		attributes.remove("status");
		super.closeElement(element, attributes, content, warnings);
	}


}

class SingleSimulationHandler extends AbstractElementHandler {

	private final DocumentLoadingContext context;

	private final OpenRocketDocument doc;

	private String name;

	private SimulationConditionsHandler conditionHandler;
	private FlightDataHandler dataHandler;
	private CustomExpressionsHandler customExpressionsHandler;
	
	private ArrayList<CustomExpression> customExpressions = new ArrayList<CustomExpression>();
	private final List<String> listeners = new ArrayList<String>();

	public SingleSimulationHandler(OpenRocketDocument doc, DocumentLoadingContext context) {
		this.doc = doc;
		this.context = context;
	}

	public void setCustomExpressions(ArrayList<CustomExpression> expressions){
		this.customExpressions = expressions;
	}
	
	public ArrayList<CustomExpression> getCustomExpressions(){
		return customExpressions;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		if (element.equals("name") || element.equals("simulator") ||
				element.equals("calculator") || element.equals("listener")) {
			return PlainTextHandler.INSTANCE;
		} else if (element.equals("customexpressions")) {
			customExpressionsHandler = new CustomExpressionsHandler(this, context);
			return customExpressionsHandler;
		} else if (element.equals("conditions")) {
			conditionHandler = new SimulationConditionsHandler(doc.getRocket(), context);
			return conditionHandler;
		} else if (element.equals("flightdata")) {
			dataHandler = new FlightDataHandler(this, context);
			return dataHandler;
		} else {
			warnings.add("Unknown element '" + element + "', ignoring.");
			return null;
		}
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {

		if (element.equals("name")) {
			name = content;
		} else if (element.equals("simulator")) {
			if (!content.trim().equals("RK4Simulator")) {
				warnings.add("Unknown simulator '" + content.trim() + "' specified, ignoring.");
			}
		} else if (element.equals("calculator")) {
			if (!content.trim().equals("BarrowmanCalculator")) {
				warnings.add("Unknown calculator '" + content.trim() + "' specified, ignoring.");
			}
		} else if (element.equals("listener") && content.trim().length() > 0) {
			listeners.add(content.trim());
		}

	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {

		String s = attributes.get("status");
		Simulation.Status status = (Status) DocumentConfig.findEnum(s, Simulation.Status.class);
		if (status == null) {
			warnings.add("Simulation status unknown, assuming outdated.");
			status = Simulation.Status.OUTDATED;
		}

		SimulationOptions conditions;
		if (conditionHandler != null) {
			conditions = conditionHandler.getConditions();
		} else {
			warnings.add("Simulation conditions not defined, using defaults.");
			conditions = new SimulationOptions(doc.getRocket());
		}

		if (name == null)
			name = "Simulation";

		FlightData data;
		if (dataHandler == null)
			data = null;
		else
			data = dataHandler.getFlightData();

		Simulation simulation = new Simulation(doc, doc.getRocket(), status, name,
				conditions, listeners, data);
		
		// Note : arraylist implementation in simulation different from standard one
		for (CustomExpression exp : customExpressions){
			exp.setSimulation(simulation);
			if (exp.checkAll())
				simulation.addCustomExpression(exp);
		}
				
		doc.addSimulation(simulation);
	}
}

class CustomExpressionsHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final SingleSimulationHandler simHandler;
	public CustomExpression currentExpression = new CustomExpression();
	private final ArrayList<CustomExpression> customExpressions = new ArrayList<CustomExpression>();

	
	public CustomExpressionsHandler(SingleSimulationHandler simHandler, DocumentLoadingContext context) {
		this.context = context;
		this.simHandler = simHandler;
	}
	
	@Override
	public ElementHandler openElement(String element,
			HashMap<String, String> attributes, WarningSet warnings)
			throws SAXException {
		
		if (element.equals("expression")){
			currentExpression = new CustomExpression();
		}
		
		return this;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
		String content, WarningSet warnings) {
		
		if (element.equals("expression"))
			customExpressions.add(currentExpression);
		
		if (element.equals("name"))
			currentExpression.setName(content);
		 
		else if (element.equals("symbol"))
			currentExpression.setSymbol(content);
		
		else if (element.equals("unit"))
			currentExpression.setUnit(content);
		
		else if (element.equals("expressionstring"))
			currentExpression.setExpression(content);
		
	}
	
	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {
		simHandler.setCustomExpressions(customExpressions);
	}
}
	
class SimulationConditionsHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private SimulationOptions conditions;
	private AtmosphereHandler atmosphereHandler;

	public SimulationConditionsHandler(Rocket rocket, DocumentLoadingContext context) {
		this.context = context;
		conditions = new SimulationOptions(rocket);
		// Set up default loading settings (which may differ from the new defaults)
		conditions.setGeodeticComputation(GeodeticComputationStrategy.FLAT);
	}

	public SimulationOptions getConditions() {
		return conditions;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		if (element.equals("atmosphere")) {
			atmosphereHandler = new AtmosphereHandler(attributes.get("model"), context);
			return atmosphereHandler;
		}
		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {

		double d = Double.NaN;
		try {
			d = Double.parseDouble(content);
		} catch (NumberFormatException ignore) {
		}


		if (element.equals("configid")) {
			if (content.equals("")) {
				conditions.setMotorConfigurationID(null);
			} else {
				conditions.setMotorConfigurationID(content);
			}
		} else if (element.equals("launchrodlength")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch rod length defined, ignoring.");
			} else {
				conditions.setLaunchRodLength(d);
			}
		} else if (element.equals("launchrodangle")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch rod angle defined, ignoring.");
			} else {
				conditions.setLaunchRodAngle(d * Math.PI / 180);
			}
		} else if (element.equals("launchroddirection")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch rod direction defined, ignoring.");
			} else {
				conditions.setLaunchRodDirection(d * Math.PI / 180);
			}
		} else if (element.equals("windaverage")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal average windspeed defined, ignoring.");
			} else {
				conditions.setWindSpeedAverage(d);
			}
		} else if (element.equals("windturbulence")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal wind turbulence intensity defined, ignoring.");
			} else {
				conditions.setWindTurbulenceIntensity(d);
			}
		} else if (element.equals("launchaltitude")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch altitude defined, ignoring.");
			} else {
				conditions.setLaunchAltitude(d);
			}
		} else if (element.equals("launchlatitude")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch latitude defined, ignoring.");
			} else {
				conditions.setLaunchLatitude(d);
			}
		} else if (element.equals("launchlongitude")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal launch longitude.");
			} else {
				conditions.setLaunchLongitude(d);
			}
		} else if (element.equals("geodeticmethod")) {
			GeodeticComputationStrategy gcs =
					(GeodeticComputationStrategy) DocumentConfig.findEnum(content, GeodeticComputationStrategy.class);
			if (gcs != null) {
				conditions.setGeodeticComputation(gcs);
			} else {
				warnings.add("Unknown geodetic computation method '" + content + "'");
			}
		} else if (element.equals("atmosphere")) {
			atmosphereHandler.storeSettings(conditions, warnings);
		} else if (element.equals("timestep")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal time step defined, ignoring.");
			} else {
				conditions.setTimeStep(d);
			}
		}
	}
}


class AtmosphereHandler extends AbstractElementHandler {
	@SuppressWarnings("unused")
	private final DocumentLoadingContext context;
	private final String model;
	private double temperature = Double.NaN;
	private double pressure = Double.NaN;

	public AtmosphereHandler(String model, DocumentLoadingContext context) {
		this.model = model;
		this.context = context;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {

		double d = Double.NaN;
		try {
			d = Double.parseDouble(content);
		} catch (NumberFormatException ignore) {
		}

		if (element.equals("basetemperature")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal base temperature specified, ignoring.");
			}
			temperature = d;
		} else if (element.equals("basepressure")) {
			if (Double.isNaN(d)) {
				warnings.add("Illegal base pressure specified, ignoring.");
			}
			pressure = d;
		} else {
			super.closeElement(element, attributes, content, warnings);
		}
	}


	public void storeSettings(SimulationOptions cond, WarningSet warnings) {
		if (!Double.isNaN(pressure)) {
			cond.setLaunchPressure(pressure);
		}
		if (!Double.isNaN(temperature)) {
			cond.setLaunchTemperature(temperature);
		}

		if ("isa".equals(model)) {
			cond.setISAAtmosphere(true);
		} else if ("extendedisa".equals(model)) {
			cond.setISAAtmosphere(false);
		} else {
			cond.setISAAtmosphere(true);
			warnings.add("Unknown atmospheric model, using ISA.");
		}
	}

}


class FlightDataHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;

	private FlightDataBranchHandler dataHandler;
	private WarningSet warningSet = new WarningSet();
	private List<FlightDataBranch> branches = new ArrayList<FlightDataBranch>();
	
	private SingleSimulationHandler simHandler;
	private FlightData data;


	public FlightDataHandler(SingleSimulationHandler simHandler, DocumentLoadingContext context) {
		this.context = context;
		this.simHandler = simHandler;
	}

	public FlightData getFlightData() {
		return data;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		if (element.equals("warning")) {
			return PlainTextHandler.INSTANCE;
		}
		if (element.equals("databranch")) {
			if (attributes.get("name") == null || attributes.get("types") == null) {
				warnings.add("Illegal flight data definition, ignoring.");
				return null;
			}
			dataHandler = new FlightDataBranchHandler(	attributes.get("name"),
														attributes.get("types"), 
														simHandler, context);
			return dataHandler;
		}

		warnings.add("Unknown element '" + element + "' encountered, ignoring.");
		return null;
	}


	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {

		if (element.equals("databranch")) {
			FlightDataBranch branch = dataHandler.getBranch();
			if (branch.getLength() > 0) {
				branches.add(branch);
			}
		} else if (element.equals("warning")) {
			warningSet.add(Warning.fromString(content));
		}
	}


	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {

		if (branches.size() > 0) {
			data = new FlightData(branches.toArray(new FlightDataBranch[0]));
		} else {
			double maxAltitude = Double.NaN;
			double maxVelocity = Double.NaN;
			double maxAcceleration = Double.NaN;
			double maxMach = Double.NaN;
			double timeToApogee = Double.NaN;
			double flightTime = Double.NaN;
			double groundHitVelocity = Double.NaN;
			double launchRodVelocity = Double.NaN;
			double deploymentVelocity = Double.NaN;

			try {
				maxAltitude = DocumentConfig.stringToDouble(attributes.get("maxaltitude"));
			} catch (NumberFormatException ignore) {
			}
			try {
				maxVelocity = DocumentConfig.stringToDouble(attributes.get("maxvelocity"));
			} catch (NumberFormatException ignore) {
			}
			try {
				maxAcceleration = DocumentConfig.stringToDouble(attributes.get("maxacceleration"));
			} catch (NumberFormatException ignore) {
			}
			try {
				maxMach = DocumentConfig.stringToDouble(attributes.get("maxmach"));
			} catch (NumberFormatException ignore) {
			}
			try {
				timeToApogee = DocumentConfig.stringToDouble(attributes.get("timetoapogee"));
			} catch (NumberFormatException ignore) {
			}
			try {
				flightTime = DocumentConfig.stringToDouble(attributes.get("flighttime"));
			} catch (NumberFormatException ignore) {
			}
			try {
				groundHitVelocity =
						DocumentConfig.stringToDouble(attributes.get("groundhitvelocity"));
			} catch (NumberFormatException ignore) {
			}
			try {
				launchRodVelocity = DocumentConfig.stringToDouble(attributes.get("launchrodvelocity"));
			} catch (NumberFormatException ignore) {
			}
			try {
				deploymentVelocity = DocumentConfig.stringToDouble(attributes.get("deploymentvelocity"));
			} catch (NumberFormatException ignore) {
			}

			data = new FlightData(maxAltitude, maxVelocity, maxAcceleration, maxMach,
					timeToApogee, flightTime, groundHitVelocity, launchRodVelocity, deploymentVelocity);
		}

		data.getWarningSet().addAll(warningSet);
		data.immute();
	}


}


class FlightDataBranchHandler extends AbstractElementHandler {
	@SuppressWarnings("unused")
	private final DocumentLoadingContext context;
	private final FlightDataType[] types;
	private final FlightDataBranch branch;
	
	private static final LogHelper log = Application.getLogger();
	private final SingleSimulationHandler simHandler;
	
	public FlightDataBranchHandler(String name, String typeList, SingleSimulationHandler simHandler, DocumentLoadingContext context) {
		this.simHandler = simHandler;
		this.context = context;
		String[] split = typeList.split(",");
		types = new FlightDataType[split.length];
		for (int i = 0; i < split.length; i++) {
			String typeName = split[i];
			FlightDataType matching = findFlightDataType(typeName);
			types[i] = matching;
			//types[i] = FlightDataType.getType(typeName, matching.getSymbol(), matching.getUnitGroup());
		}

		// TODO: LOW: May throw an IllegalArgumentException
		branch = new FlightDataBranch(name, types);
	}
	
	// Find the full flight data type given name only
	// Note: this way of doing it requires that custom expressions always come before flight data in the file,
	// not the nicest but this is always the case anyway.
	private FlightDataType findFlightDataType(String name){
		
		// Look in built in types
		for (FlightDataType t : FlightDataType.ALL_TYPES){
			if (t.getName().equals(name) ){
				return t;
			}
		}
		
		// Look in custom expressions
		for (CustomExpression exp : simHandler.getCustomExpressions()){
			if (exp.getName().equals(name) ){
				return exp.getType();
			}
		}
		
		// Look in custom expressions, meanwhile set priority based on order in file
		/*
		int totalExpressions = simHandler.getCustomExpressions().size();
		for (int i=0; i<totalExpressions; i++){
			CustomExpression exp = simHandler.getCustomExpressions().get(i);			
			if (exp.getName().equals(name) ){
				FlightDataType t = exp.getType();
				t.setPriority(-1*(totalExpressions-i));
				return exp.getType();
			}
		}
		*/
		
		log.warn("Could not find the flight data type '"+name+"' used in the XML file. Substituted type with unknown symbol and units.");
		return FlightDataType.getType(name, "Unknown", UnitGroup.UNITS_NONE);
	}

	public FlightDataBranch getBranch() {
		branch.immute();
		return branch;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		if (element.equals("datapoint"))
			return PlainTextHandler.INSTANCE;
		if (element.equals("event"))
			return PlainTextHandler.INSTANCE;

		warnings.add("Unknown element '" + element + "' encountered, ignoring.");
		return null;
	}


	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) {

		if (element.equals("event")) {
			double time;
			FlightEvent.Type type;

			try {
				time = DocumentConfig.stringToDouble(attributes.get("time"));
			} catch (NumberFormatException e) {
				warnings.add("Illegal event specification, ignoring.");
				return;
			}

			type = (Type) DocumentConfig.findEnum(attributes.get("type"), FlightEvent.Type.class);
			if (type == null) {
				warnings.add("Illegal event specification, ignoring.");
				return;
			}

			branch.addEvent(new FlightEvent(type, time));
			return;
		}

		if (!element.equals("datapoint")) {
			warnings.add("Unknown element '" + element + "' encountered, ignoring.");
			return;
		}

		// element == "datapoint"


		// Check line format
		String[] split = content.split(",");
		if (split.length != types.length) {
			warnings.add("Data point did not contain correct amount of values, ignoring point.");
			return;
		}

		// Parse the doubles
		double[] values = new double[split.length];
		for (int i = 0; i < values.length; i++) {
			try {
				values[i] = DocumentConfig.stringToDouble(split[i]);
			} catch (NumberFormatException e) {
				warnings.add("Data point format error, ignoring point.");
				return;
			}
		}

		// Add point to branch
		branch.addPoint();
		for (int i = 0; i < types.length; i++) {
			branch.setValue(types[i], values[i]);
		}
	}
}





/////////////////    Setters implementation


////  Interface
interface Setter {
	/**
	 * Set the specified value to the given component.
	 * 
	 * @param component		the component to which to set.
	 * @param value			the value within the element.
	 * @param attributes	attributes for the element.
	 * @param warnings		the warning set to use.
	 */
	public void set(RocketComponent component, String value,
			HashMap<String, String> attributes, WarningSet warnings);
}


////  StringSetter - sets the value to the contained String
class StringSetter implements Setter {
	private final Reflection.Method setMethod;

	public StringSetter(Reflection.Method set) {
		setMethod = set;
	}

	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {
		setMethod.invoke(c, s);
	}
}

////  IntSetter - set an integer value
class IntSetter implements Setter {
	private final Reflection.Method setMethod;

	public IntSetter(Reflection.Method set) {
		setMethod = set;
	}

	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {
		try {
			int n = Integer.parseInt(s);
			setMethod.invoke(c, n);
		} catch (NumberFormatException e) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
	}
}


//// BooleanSetter - set a boolean value
class BooleanSetter implements Setter {
	private final Reflection.Method setMethod;

	public BooleanSetter(Reflection.Method set) {
		setMethod = set;
	}

	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {

		s = s.trim();
		if (s.equalsIgnoreCase("true")) {
			setMethod.invoke(c, true);
		} else if (s.equalsIgnoreCase("false")) {
			setMethod.invoke(c, false);
		} else {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
	}
}



////  DoubleSetter - sets a double value or (alternatively) if a specific string is encountered
////  calls a setXXX(boolean) method.
class DoubleSetter implements Setter {
	private final Reflection.Method setMethod;
	private final String specialString;
	private final Reflection.Method specialMethod;
	private final double multiplier;

	/**
	 * Set only the double value.
	 * @param set	set method for the double value. 
	 */
	public DoubleSetter(Reflection.Method set) {
		this.setMethod = set;
		this.specialString = null;
		this.specialMethod = null;
		this.multiplier = 1.0;
	}

	/**
	 * Multiply with the given multiplier and set the double value.
	 * @param set	set method for the double value.
	 * @param mul	multiplier.
	 */
	public DoubleSetter(Reflection.Method set, double mul) {
		this.setMethod = set;
		this.specialString = null;
		this.specialMethod = null;
		this.multiplier = mul;
	}

	/**
	 * Set the double value, or if the value equals the special string, use the
	 * special setter and set it to true.
	 * 
	 * @param set			double setter.
	 * @param special		special string
	 * @param specialMethod	boolean setter.
	 */
	public DoubleSetter(Reflection.Method set, String special,
			Reflection.Method specialMethod) {
		this.setMethod = set;
		this.specialString = special;
		this.specialMethod = specialMethod;
		this.multiplier = 1.0;
	}


	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {

		s = s.trim();

		// Check for special case
		if (specialMethod != null && s.equalsIgnoreCase(specialString)) {
			specialMethod.invoke(c, true);
			return;
		}

		// Normal case
		try {
			double d = Double.parseDouble(s);
			setMethod.invoke(c, d * multiplier);
		} catch (NumberFormatException e) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
	}
}


class OverrideSetter implements Setter {
	private final Reflection.Method setMethod;
	private final Reflection.Method enabledMethod;

	public OverrideSetter(Reflection.Method set, Reflection.Method enabledMethod) {
		this.setMethod = set;
		this.enabledMethod = enabledMethod;
	}

	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {

		try {
			double d = Double.parseDouble(s);
			setMethod.invoke(c, d);
			enabledMethod.invoke(c, true);
		} catch (NumberFormatException e) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
	}
}

////  EnumSetter  -  sets a generic enum type
class EnumSetter<T extends Enum<T>> implements Setter {
	private final Reflection.Method setter;
	private final Class<T> enumClass;

	public EnumSetter(Reflection.Method set, Class<T> enumClass) {
		this.setter = set;
		this.enumClass = enumClass;
	}

	@Override
	public void set(RocketComponent c, String name, HashMap<String, String> attributes,
			WarningSet warnings) {

		Enum<?> setEnum = DocumentConfig.findEnum(name, enumClass);
		if (setEnum == null) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		setter.invoke(c, setEnum);
	}
}


////  ColorSetter  -  sets a Color value
class ColorSetter implements Setter {
	private final Reflection.Method setMethod;

	public ColorSetter(Reflection.Method set) {
		setMethod = set;
	}

	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {

		String red = attributes.get("red");
		String green = attributes.get("green");
		String blue = attributes.get("blue");

		if (red == null || green == null || blue == null) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		int r, g, b;
		try {
			r = Integer.parseInt(red);
			g = Integer.parseInt(green);
			b = Integer.parseInt(blue);
		} catch (NumberFormatException e) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		if (r < 0 || g < 0 || b < 0 || r > 255 || g > 255 || b > 255) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		Color color = new Color(r, g, b);
		setMethod.invoke(c, color);

		if (!s.trim().equals("")) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}
	}
}

////ComponentPresetSetter  -  sets a ComponentPreset value
class ComponentPresetSetter implements Setter {
	private final Reflection.Method setMethod;

	public ComponentPresetSetter(Reflection.Method set) {
		this.setMethod = set;
	}

	@Override
	public void set(RocketComponent c, String name, HashMap<String, String> attributes,
			WarningSet warnings) {
// FIXME - probably need more data in the warning messages - like what component preset...
		String manufacturerName = attributes.get("manufacturer");
		if ( manufacturerName == null ) {
			warnings.add(Warning.fromString("Invalid ComponentPreset, no manufacturer specified.  Ignored"));
			return;
		}

		String productNo = attributes.get("partno");
		if ( productNo == null ) {
			warnings.add(Warning.fromString("Invalid ComponentPreset, no partno specified.  Ignored"));
			return;
		}

		String digest = attributes.get("digest");
		if ( digest == null ) {
			warnings.add(Warning.fromString("Invalid ComponentPreset, no digest specified."));
		}

		String type = attributes.get("type");
		if ( type == null ) {
			warnings.add(Warning.fromString("Invalid ComponentPreset, no type specified."));
		}

		List<ComponentPreset> presets = Application.getComponentPresetDao().find( manufacturerName, productNo );

		ComponentPreset matchingPreset = null;

		for( ComponentPreset preset: presets ) {
			if ( digest != null && preset.getDigest().equals(digest) ) {
				// Found one with matching digest.  Take it.
				matchingPreset = preset;
				break;
			}
			if ( type != null && preset.getType().name().equals(type) && matchingPreset != null) {
				// Found the first one with matching type.
				matchingPreset = preset;
			}
		}

		// Was any found?
		if ( matchingPreset == null ) {
			warnings.add(Warning.fromString("No matching ComponentPreset found"));
			return;
		}
		
		if ( digest != null && !matchingPreset.getDigest().equals(digest) ) {
			warnings.add(Warning.fromString("ComponentPreset has wrong digest"));
		}

		setMethod.invoke(c, matchingPreset);
	}
}


////MaterialSetter  -  sets a Material value
class MaterialSetter implements Setter {
	private final Reflection.Method setMethod;
	private final Material.Type type;

	public MaterialSetter(Reflection.Method set, Material.Type type) {
		this.setMethod = set;
		this.type = type;
	}

	@Override
	public void set(RocketComponent c, String name, HashMap<String, String> attributes,
			WarningSet warnings) {

		Material mat;

		// Check name != ""
		name = name.trim();
		if (name.equals("")) {
			warnings.add(Warning.fromString("Illegal material specification, ignoring."));
			return;
		}

		// Parse density
		double density;
		String str;
		str = attributes.remove("density");
		if (str == null) {
			warnings.add(Warning.fromString("Illegal material specification, ignoring."));
			return;
		}
		try {
			density = Double.parseDouble(str);
		} catch (NumberFormatException e) {
			warnings.add(Warning.fromString("Illegal material specification, ignoring."));
			return;
		}

		// Parse thickness
		//		double thickness = 0;
		//		str = attributes.remove("thickness");
		//		try {
		//			if (str != null)
		//				thickness = Double.parseDouble(str);
		//		} catch (NumberFormatException e){
		//			warnings.add(Warning.fromString("Illegal material specification, ignoring."));
		//			return;
		//		}

		// Check type if specified
		str = attributes.remove("type");
		if (str != null && !type.name().toLowerCase(Locale.ENGLISH).equals(str)) {
			warnings.add(Warning.fromString("Illegal material type specified, ignoring."));
			return;
		}

		mat = Databases.findMaterial(type, name, density, false);

		setMethod.invoke(c, mat);
	}
}




class PositionSetter implements Setter {

	@Override
	public void set(RocketComponent c, String value, HashMap<String, String> attributes,
			WarningSet warnings) {

		RocketComponent.Position type = (Position) DocumentConfig.findEnum(attributes.get("type"),
				RocketComponent.Position.class);
		if (type == null) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		double pos;
		try {
			pos = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
			return;
		}

		if (c instanceof FinSet) {
			((FinSet) c).setRelativePosition(type);
			c.setPositionValue(pos);
		} else if (c instanceof LaunchLug) {
			((LaunchLug) c).setRelativePosition(type);
			c.setPositionValue(pos);
		} else if (c instanceof InternalComponent) {
			((InternalComponent) c).setRelativePosition(type);
			c.setPositionValue(pos);
		} else {
			warnings.add(Warning.FILE_INVALID_PARAMETER);
		}

	}
}


class FinTabPositionSetter extends DoubleSetter {

	public FinTabPositionSetter() {
		super(Reflection.findMethod(FinSet.class, "setTabShift", double.class));
	}

	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {

		if (!(c instanceof FinSet)) {
			throw new IllegalStateException("FinTabPositionSetter called for component " + c);
		}

		String relative = attributes.get("relativeto");
		FinSet.TabRelativePosition position =
				(TabRelativePosition) DocumentConfig.findEnum(relative,
						FinSet.TabRelativePosition.class);

		if (position != null) {

			((FinSet) c).setTabRelativePosition(position);

		} else {
			if (relative == null) {
				warnings.add("Required attribute 'relativeto' not found for fin tab position.");
			} else {
				warnings.add("Illegal attribute value '" + relative + "' encountered.");
			}
		}

		super.set(c, s, attributes, warnings);
	}


}


class ClusterConfigurationSetter implements Setter {

	@Override
	public void set(RocketComponent component, String value, HashMap<String, String> attributes,
			WarningSet warnings) {

		if (!(component instanceof Clusterable)) {
			warnings.add("Illegal component defined as cluster.");
			return;
		}

		ClusterConfiguration config = null;
		for (ClusterConfiguration c : ClusterConfiguration.CONFIGURATIONS) {
			if (c.getXMLName().equals(value)) {
				config = c;
				break;
			}
		}

		if (config == null) {
			warnings.add("Illegal cluster configuration specified.");
			return;
		}

		((Clusterable) component).setClusterConfiguration(config);
	}
}
