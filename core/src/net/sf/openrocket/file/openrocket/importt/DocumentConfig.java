package net.sf.openrocket.file.openrocket.importt;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Locale;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.BodyComponent;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.ReferenceType;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.ShockCord;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.StructuralComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.rocketcomponent.ThicknessRingComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.Reflection;

class DocumentConfig {
	
	/* Remember to update OpenRocketSaver as well! */
	public static final String[] SUPPORTED_VERSIONS = { "1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7" };
	
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
			constructors.put("tubefinset", TubeFinSet.class.getConstructor(new Class<?>[0]));
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
		setters.put("FinSet:filletradius", new DoubleSetter(
				Reflection.findMethod(FinSet.class, "setFilletRadius", double.class)));
		setters.put("FinSet:filletmaterial", new MaterialSetter(
				Reflection.findMethod(FinSet.class, "setFilletMaterial", Material.class),
				Material.Type.BULK));
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
		
		// TubeFinSet
		setters.put("TubeFinSet:fincount", new IntSetter(
				Reflection.findMethod(TubeFinSet.class, "setFinCount", int.class)));
		setters.put("TubeFinSet:rotation", new DoubleSetter(
				Reflection.findMethod(TubeFinSet.class, "setBaseRotation", double.class), Math.PI / 180.0));
		setters.put("TubeFinSet:thickness", new DoubleSetter(
				Reflection.findMethod(TubeFinSet.class, "setThickness", double.class)));
		setters.put("TubeFinSet:length", new DoubleSetter(
				Reflection.findMethod(TubeFinSet.class, "setLength", double.class)));
		setters.put("TubeFinSet:radius", new DoubleSetter(
				Reflection.findMethod(TubeFinSet.class, "setOuterRadius", double.class),
				"auto",
				Reflection.findMethod(TubeFinSet.class, "setOuterRadiusAutomatic", boolean.class)));
		
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
		/*setters.put("MassComponent:masscomponenttype", new DoubleSetter(
				Reflection.findMethod(MassComponent.class, "setMassComponentType", double.class)));*/
		setters.put("MassComponent:masscomponenttype", new EnumSetter<MassComponent.MassComponentType>(
				Reflection.findMethod(MassComponent.class, "setMassComponentType", MassComponent.MassComponentType.class),
				MassComponent.MassComponentType.class));
		/*		setters.put("Transition:shape", new EnumSetter<Transition.Shape>(
						Reflection.findMethod(Transition.class, "setType", Transition.Shape.class),
						Transition.Shape.class));*/
		
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
		setters.put("RecoveryDevice:deployevent", new EnumSetter<DeployEvent>(
				Reflection.findMethod(RecoveryDevice.class, "getDeploymentConfiguration"),
				Reflection.findMethod(DeploymentConfiguration.class, "setDeployEvent", DeployEvent.class),
				DeployEvent.class));
		setters.put("RecoveryDevice:deployaltitude", new DoubleSetter(
				Reflection.findMethod(RecoveryDevice.class, "getDeploymentConfiguration"),
				Reflection.findMethod(DeploymentConfiguration.class, "setDeployAltitude", double.class)));
		setters.put("RecoveryDevice:deploydelay", new DoubleSetter(
				Reflection.findMethod(RecoveryDevice.class, "getDeploymentConfiguration"),
				Reflection.findMethod(DeploymentConfiguration.class, "setDeployDelay", double.class)));
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
		setters.put("Stage:separationevent", new EnumSetter<StageSeparationConfiguration.SeparationEvent>(
				Reflection.findMethod(Stage.class, "getStageSeparationConfiguration"),
				Reflection.findMethod(StageSeparationConfiguration.class, "setSeparationEvent", StageSeparationConfiguration.SeparationEvent.class),
				StageSeparationConfiguration.SeparationEvent.class));
		setters.put("Stage:separationdelay", new DoubleSetter(
				Reflection.findMethod(Stage.class, "getStageSeparationConfiguration"),
				Reflection.findMethod(StageSeparationConfiguration.class, "setSeparationDelay", double.class)));
		
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
