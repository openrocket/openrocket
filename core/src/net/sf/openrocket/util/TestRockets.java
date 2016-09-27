package net.sf.openrocket.util;

import java.util.Random;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.Material.Type;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPresetFactory;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.ClusterConfiguration;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.IllegalFinPointException;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.InternalComponent;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.ReferenceType;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.rocketcomponent.ShockCord;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.impl.ScriptingExtension;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.startup.Application;

public class TestRockets {
	
	private final String key;
	private final Random rnd;
	
	
	public TestRockets(String key) {
		
		if (key == null) {
			Random myRnd = new Random();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 6; i++) {
				int n = myRnd.nextInt(62);
				if (n < 10) {
					sb.append((char) ('0' + n));
				} else if (n < 36) {
					sb.append((char) ('A' + n - 10));
				} else {
					sb.append((char) ('a' + n - 36));
				}
			}
			key = sb.toString();
		}
		
		this.key = key;
		this.rnd = new Random(key.hashCode());
		
	}
	
	// Minimal motor without any useful numbers data
	private static ThrustCurveMotor getTestMotor() {
		return new ThrustCurveMotor(
				Manufacturer.getManufacturer("A"),
				"F12X", "Desc", Motor.Type.UNKNOWN, new double[] {},
				0.024, 0.07, new double[] { 0, 1, 2 }, new double[] { 0, 1, 0 },
				new Coordinate[] { Coordinate.NUL, Coordinate.NUL, Coordinate.NUL }, "digestA");
	} 
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_A8_18mm(){
		// public ThrustCurveMotor(Manufacturer manufacturer, String designation, String description,
		//			Motor.Type type, double[] delays, double diameter, double length,
		//			double[] time, double[] thrust,
		//          Coordinate[] cg, String digest);
		ThrustCurveMotor mtr = new ThrustCurveMotor(
				Manufacturer.getManufacturer("Estes"),"A8", " SU Black Powder", 
				Motor.Type.SINGLE, new double[] {0,3,5}, 0.018, 0.070,
				new double[] { 0, 1, 2 }, new double[] { 0, 9, 0 },
				new Coordinate[] {
					new Coordinate(0.035, 0, 0, 0.0164),new Coordinate(.035, 0, 0, 0.0145),new Coordinate(.035, 0, 0, 0.0131)}, 
				"digest A8 test");
		return mtr;
	}
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_B4_18mm(){
		// public ThrustCurveMotor(Manufacturer manufacturer, String designation, String description,
		//			Motor.Type type, double[] delays, double diameter, double length,
		//			double[] time, double[] thrust,
		//          Coordinate[] cg, String digest);
		ThrustCurveMotor mtr = new ThrustCurveMotor(
				Manufacturer.getManufacturer("Estes"),"B4", " SU Black Powder", 
				Motor.Type.SINGLE, new double[] {0,3,5}, 0.018, 0.070,
				new double[] { 0, 1, 2 }, new double[] { 0, 11.4, 0 },
				new Coordinate[] {
					new Coordinate(0.035, 0, 0, 0.0195),new Coordinate(.035, 0, 0, 0.0155),new Coordinate(.035, 0, 0, 0.013)}, 
				"digest B4 test");
		return mtr;
	}
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_C6_18mm(){
		// public ThrustCurveMotor(Manufacturer manufacturer, String designation, String description,
		//			Motor.Type type, double[] delays, double diameter, double length,
		//			double[] time, double[] thrust,
		//          Coordinate[] cg, String digest);
		ThrustCurveMotor mtr = new ThrustCurveMotor(
				Manufacturer.getManufacturer("Estes"),"C6", " SU Black Powder", 
				Motor.Type.SINGLE, new double[] {0,3,5,7}, 0.018, 0.070,
				new double[] { 0, 1, 2 }, new double[] { 0, 6, 0 },
				new Coordinate[] {
					new Coordinate(0.035, 0, 0, 0.0227),new Coordinate(.035, 0, 0, 0.0165),new Coordinate(.035, 0, 0, 0.012)}, 
				"digest C6 test");
		return mtr;
	}
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_D21_18mm(){
		ThrustCurveMotor mtr = new ThrustCurveMotor(
			Manufacturer.getManufacturer("AeroTech"),"D21", "Desc", 
			Motor.Type.SINGLE, new double[] {}, 0.018, 0.07,
			new double[] { 0, 1, 2 }, new double[] { 0, 32, 0 },
			new Coordinate[] {
				new Coordinate(.035, 0, 0, 0.025),new Coordinate(.035, 0, 0, .020),new Coordinate(.035, 0, 0, 0.0154)}, 
			"digest D21 test");
	    return mtr;
	}
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_M1350_75mm(){
		// public ThrustCurveMotor(Manufacturer manufacturer, String designation, String description,
		//			Motor.Type type, double[] delays, double diameter, double length,
		//			double[] time, double[] thrust,
		//          Coordinate[] cg, String digest);
		ThrustCurveMotor mtr = new ThrustCurveMotor(
				Manufacturer.getManufacturer("AeroTech"),"M1350", "Desc", 
				Motor.Type.SINGLE, new double[] {}, 0.075, 0.622,
				new double[] { 0, 1, 2 }, new double[] { 0, 1357, 0 },
				new Coordinate[] {
					new Coordinate(.311, 0, 0, 4.808),new Coordinate(.311, 0, 0, 3.389),new Coordinate(.311, 0, 0, 1.970)}, 
				"digest M1350 test");
		return mtr;
	}
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_G77_29mm(){
		// public ThrustCurveMotor(Manufacturer manufacturer, String designation, String description,
		//			Motor.Type type, double[] delays, double diameter, double length,
		//			double[] time, double[] thrust,
		//          Coordinate[] cg, String digest);
		ThrustCurveMotor mtr = new ThrustCurveMotor(
				Manufacturer.getManufacturer("AeroTech"),"G77", "Desc", 
				Motor.Type.SINGLE, new double[] {4,7,10},0.029, 0.124,
				new double[] { 0, 1, 2 }, new double[] { 0, 1, 0 },
				new Coordinate[] {
					new Coordinate(.062, 0, 0, 0.123),new Coordinate(.062, 0, 0, .0935),new Coordinate(.062, 0, 0, 0.064)}, 
				"digest G77 test");
		return mtr;
	}
	
	// 
	public static Rocket makeNoMotorRocket() {
		Rocket rocket;
		AxialStage stage;
		NoseCone nosecone;
		BodyTube bodytube;
		
		
		rocket = new Rocket();
		stage = new AxialStage();
		stage.setName("Stage1");
		// Stage construction
		rocket.addChild(stage);
				
		nosecone = new NoseCone(Transition.Shape.ELLIPSOID, 0.105, 0.033);
		nosecone.setThickness(0.001);
		bodytube = new BodyTube(0.69, 0.033, 0.001);
		bodytube.setMotorMount(true);
		
		TrapezoidFinSet finset = new TrapezoidFinSet(3, 0.495, 0.1, 0.3, 0.185);
		finset.setThickness(0.005);
		bodytube.addChild(finset);
		
		// Component construction
		stage.addChild(nosecone);
		stage.addChild(bodytube);
		
		rocket.enableEvents();
		return rocket;
	}
	
	/**
	 * Create a new test rocket based on the value 'key'.  The rocket utilizes most of the 
	 * properties and features available.  The same key always returns the same rocket,
	 * but different key values produce slightly different rockets.  A key value of
	 * <code>null</code> generates a rocket using a random key.
	 * <p>
	 * The rocket created by this method is not fly-worthy.  It is also NOT guaranteed
	 * that later versions would produce exactly the same rocket!
	 * 
	 * @return		a rocket design.
	 */
	public Rocket makeTestRocket() {
		
		Rocket rocket = new Rocket();
		setBasics(rocket);
		rocket.setCustomReferenceLength(rnd(0.05));
		rocket.setDesigner("Designer " + key);
		rocket.setReferenceType((ReferenceType) randomEnum(ReferenceType.class));
		rocket.setRevision("Rocket revision " + key);
		rocket.setName(key);
		
		AxialStage stage = new AxialStage();
		setBasics(stage);
		rocket.addChild(stage);
		
		NoseCone nose = new NoseCone();
		setBasics(stage);
		nose.setAftRadius(rnd(0.03));
		nose.setAftRadiusAutomatic(rnd.nextBoolean());
		nose.setAftShoulderCapped(rnd.nextBoolean());
		nose.setAftShoulderLength(rnd(0.02));
		nose.setAftShoulderRadius(rnd(0.02));
		nose.setAftShoulderThickness(rnd(0.002));
		nose.setClipped(rnd.nextBoolean());
		nose.setThickness(rnd(0.002));
		nose.setFilled(rnd.nextBoolean());
		nose.setForeRadius(rnd(0.1)); // Unset
		nose.setLength(rnd(0.15));
		nose.setShapeParameter(rnd(0.5));
		nose.setType((Shape) randomEnum(Shape.class));
		stage.addChild(nose);
		
		Transition shoulder = new Transition();
		setBasics(shoulder);
		shoulder.setAftRadius(rnd(0.06));
		shoulder.setAftRadiusAutomatic(rnd.nextBoolean());
		shoulder.setAftShoulderCapped(rnd.nextBoolean());
		shoulder.setAftShoulderLength(rnd(0.02));
		shoulder.setAftShoulderRadius(rnd(0.05));
		shoulder.setAftShoulderThickness(rnd(0.002));
		shoulder.setClipped(rnd.nextBoolean());
		shoulder.setThickness(rnd(0.002));
		shoulder.setFilled(rnd.nextBoolean());
		shoulder.setForeRadius(rnd(0.03));
		shoulder.setForeRadiusAutomatic(rnd.nextBoolean());
		shoulder.setForeShoulderCapped(rnd.nextBoolean());
		shoulder.setForeShoulderLength(rnd(0.02));
		shoulder.setForeShoulderRadius(rnd(0.02));
		shoulder.setForeShoulderThickness(rnd(0.002));
		shoulder.setLength(rnd(0.15));
		shoulder.setShapeParameter(rnd(0.5));
		shoulder.setThickness(rnd(0.003));
		shoulder.setType((Shape) randomEnum(Shape.class));
		stage.addChild(shoulder);
		
		BodyTube body = new BodyTube();
		setBasics(body);
		body.setThickness(rnd(0.002));
		body.setFilled(rnd.nextBoolean());
		body.setLength(rnd(0.3));
		//body.setMotorMount(rnd.nextBoolean());
		body.setMotorOverhang(rnd.nextGaussian() * 0.03);
		body.setOuterRadius(rnd(0.06));
		body.setOuterRadiusAutomatic(rnd.nextBoolean());
		stage.addChild(body);
		
		Transition boattail = new Transition();
		setBasics(boattail);
		boattail.setAftRadius(rnd(0.03));
		boattail.setAftRadiusAutomatic(rnd.nextBoolean());
		boattail.setAftShoulderCapped(rnd.nextBoolean());
		boattail.setAftShoulderLength(rnd(0.02));
		boattail.setAftShoulderRadius(rnd(0.02));
		boattail.setAftShoulderThickness(rnd(0.002));
		boattail.setClipped(rnd.nextBoolean());
		boattail.setThickness(rnd(0.002));
		boattail.setFilled(rnd.nextBoolean());
		boattail.setForeRadius(rnd(0.06));
		boattail.setForeRadiusAutomatic(rnd.nextBoolean());
		boattail.setForeShoulderCapped(rnd.nextBoolean());
		boattail.setForeShoulderLength(rnd(0.02));
		boattail.setForeShoulderRadius(rnd(0.05));
		boattail.setForeShoulderThickness(rnd(0.002));
		boattail.setLength(rnd(0.15));
		boattail.setShapeParameter(rnd(0.5));
		boattail.setThickness(rnd(0.003));
		boattail.setType((Shape) randomEnum(Shape.class));
		stage.addChild(boattail);
		
		MassComponent mass = new MassComponent();
		setBasics(mass);
		mass.setComponentMass(rnd(0.05));
		mass.setLength(rnd(0.05));
		mass.setRadialDirection(rnd(100));
		mass.setRadialPosition(rnd(0.02));
		mass.setRadius(rnd(0.05));
		nose.addChild(mass);
		
		rocket.enableEvents();
		return rocket;
	}
	
	
	private void setBasics(RocketComponent c) {
		c.setComment(c.getComponentName() + " comment " + key);
		c.setName(c.getComponentName() + " name " + key);
		
		c.setCGOverridden(rnd.nextBoolean());
		c.setMassOverridden(rnd.nextBoolean());
		c.setOverrideCGX(rnd(0.2));
		c.setOverrideMass(rnd(0.05));
		c.setOverrideSubcomponents(rnd.nextBoolean());
		
		if (c.isMassive()) {
			// Only massive components are drawn
			c.setColor(randomColor());
			c.setLineStyle((LineStyle) randomEnum(LineStyle.class));
		}
		
		if (c instanceof ExternalComponent) {
			ExternalComponent e = (ExternalComponent) c;
			e.setFinish((Finish) randomEnum(Finish.class));
			double d = rnd(100);
			e.setMaterial(Databases.findMaterial(Type.BULK, "Testmat " + d, d));
		}
		
		if (c instanceof InternalComponent) {
			InternalComponent i = (InternalComponent) c;
			i.setRelativePosition((Position) randomEnum(Position.class));
			i.setPositionValue(rnd(0.3));
		}
	}
	
	private double rnd(double scale) {
		return (rnd.nextDouble() * 0.2 + 0.9) * scale;
	}
	
	private Color randomColor() {
		return new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
	}
	
	private <T extends Enum<T>> Enum<T> randomEnum(Class<T> c) {
		Enum<T>[] values = c.getEnumConstants();
		if (values.length == 0)
			return null;
		
		return values[rnd.nextInt(values.length)];
	}
	
	// This is a Estes Alpha III 
	// http://www.rocketreviews.com/alpha-iii---estes-221256.html
	// It is picked as a standard, simple, validation rocket. 
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	public static final Rocket makeEstesAlphaIII(){
		Rocket rocket = new Rocket();
		FlightConfigurationId fcid[] = new FlightConfigurationId[5]; 
		fcid[0] = rocket.getSelectedConfiguration().getFlightConfigurationID();
		rocket.createFlightConfiguration(fcid[0]);
		fcid[1] = new FlightConfigurationId();
		rocket.createFlightConfiguration(fcid[1]);
		fcid[2] = new FlightConfigurationId();
		rocket.createFlightConfiguration(fcid[2]);
		fcid[3] = new FlightConfigurationId();
		rocket.createFlightConfiguration(fcid[3]);
		fcid[4] = new FlightConfigurationId();
		rocket.createFlightConfiguration(fcid[4]);
		
		rocket.setName("Estes Alpha III / Code Verification Rocket");
		AxialStage stage = new AxialStage();
		stage.setName("Stage");
		rocket.addChild(stage);
				
		double noseconeLength = 0.07;
		double noseconeRadius = 0.012;
		NoseCone nosecone = new NoseCone(Transition.Shape.OGIVE, noseconeLength, noseconeRadius);
		nosecone.setAftShoulderLength(0.02);
		nosecone.setAftShoulderRadius(0.011);
		nosecone.setName("Nose Cone");
		stage.addChild(nosecone);
		
		double bodytubeLength = 0.20;
		double bodytubeRadius = 0.012;
		double bodytubeThickness = 0.0003;
		BodyTube bodytube = new BodyTube(bodytubeLength, bodytubeRadius, bodytubeThickness);
		bodytube.setName("Body Tube");
		stage.addChild(bodytube);
		
		TrapezoidFinSet finset;
		{
			int finCount = 3;
			double finRootChord = .05;
			double finTipChord = .03;
			double finSweep = 0.02;
			double finHeight = 0.05;
			finset = new TrapezoidFinSet(finCount, finRootChord, finTipChord, finSweep, finHeight);
			finset.setThickness( 0.0032);
			finset.setRelativePosition(Position.BOTTOM);
			finset.setName("3 Fin Set");
			bodytube.addChild(finset);
			
			LaunchLug lug = new LaunchLug();
			lug.setName("Launch Lugs");
			lug.setRelativePosition(Position.TOP);
			lug.setAxialOffset(0.111);
			lug.setLength(0.050);
			lug.setOuterRadius(0.0022);
			lug.setInnerRadius(0.0020);
			bodytube.addChild(lug);
			
			InnerTube inner = new InnerTube();
			inner.setRelativePosition(Position.TOP);
			inner.setAxialOffset(0.133);
			inner.setLength(0.07);
			inner.setOuterRadius(0.009);
			inner.setThickness(0.0003);
			inner.setMotorMount(true);
			inner.setName("Motor Mount Tube");
			bodytube.addChild(inner);
			
			{
				// MotorBlock 
				EngineBlock thrustBlock= new EngineBlock();
				thrustBlock.setRelativePosition(Position.TOP);
				thrustBlock.setAxialOffset(0.0);
				thrustBlock.setLength(0.005);
				thrustBlock.setOuterRadius(0.009);
				thrustBlock.setThickness(0.0008);
				thrustBlock.setName("Engine Block");
				inner.addChild(thrustBlock);
				inner.setMotorMount( true);
				
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,fcid[0]);
					Motor mtr =	TestRockets.generateMotor_A8_18mm();
					motorConfig.setMotor( mtr);
					motorConfig.setEjectionDelay(0.0);
					inner.setMotorConfig( motorConfig, fcid[0]);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,fcid[1]);
					Motor mtr =	TestRockets.generateMotor_B4_18mm();
					motorConfig.setMotor( mtr);
					motorConfig.setEjectionDelay(3.0);
					inner.setMotorConfig( motorConfig, fcid[1]);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,fcid[2]);
					Motor mtr =	TestRockets.generateMotor_C6_18mm();
					motorConfig.setEjectionDelay(3.0);
					motorConfig.setMotor( mtr);
					inner.setMotorConfig( motorConfig, fcid[2]);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,fcid[3]);
					Motor mtr =	TestRockets.generateMotor_C6_18mm();
					motorConfig.setEjectionDelay(5.0);
					motorConfig.setMotor( mtr);
					inner.setMotorConfig( motorConfig, fcid[3]);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,fcid[4]);
					Motor mtr =	TestRockets.generateMotor_C6_18mm();
					motorConfig.setEjectionDelay(7.0);
					motorConfig.setMotor( mtr);
					inner.setMotorConfig( motorConfig, fcid[4]);
				}
			}
		
			// parachute
			Parachute chute = new Parachute();
			chute.setRelativePosition(Position.TOP);
			chute.setName("Parachute");
			chute.setAxialOffset(0.028);
			chute.setOverrideMass(0.002);
			chute.setMassOverridden(true);
			bodytube.addChild(chute);
			
			// bulkhead x2
			CenteringRing centerings = new CenteringRing();
			centerings.setName("Centering Rings");
			centerings.setRelativePosition(Position.TOP);
			centerings.setAxialOffset(0.14);
			centerings.setLength(0.006);
			centerings.setInstanceCount(2);
			centerings.setInstanceSeparation(0.035);
			bodytube.addChild(centerings);
		}
		
		Material material = Application.getPreferences().getDefaultComponentMaterial(null, Material.Type.BULK);
		nosecone.setMaterial(material);
		bodytube.setMaterial(material);
		finset.setMaterial(material);
		
		rocket.setSelectedConfiguration( rocket.getFlightConfiguration( fcid[0]));
		rocket.getSelectedConfiguration().setAllStages();
		rocket.enableEvents();
		return rocket;
	}
	
	// This is an extra stage tacked onto the end of an Estes Alpha III 
	// http://www.rocketreviews.com/alpha-iii---estes-221256.html
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	public static final Rocket makeBeta(){
		Rocket rocket = new Rocket();
		rocket.setName("Kit-bash Beta");
		AxialStage sustainerStage = new AxialStage();
		sustainerStage.setName("Sustainer Stage");
		rocket.addChild(sustainerStage);
		FlightConfigurationId fcid[] = new FlightConfigurationId[5];
		for( int i=0; i< fcid.length; ++i){
			fcid[i] = new FlightConfigurationId();
			rocket.createFlightConfiguration(fcid[i]);
		}
		FlightConfiguration selectedConfiguration = rocket.getFlightConfiguration(fcid[0]);
				
		double noseconeLength = 0.07;
		double noseconeRadius = 0.012;
		NoseCone nosecone = new NoseCone(Transition.Shape.OGIVE, noseconeLength, noseconeRadius);
		nosecone.setAftShoulderLength(0.025);
		nosecone.setAftShoulderRadius(0.012);
		nosecone.setName("Nose Cone");
		sustainerStage.addChild(nosecone);
		
		double bodytubeLength = 0.20;
		double bodytubeRadius = 0.012;
		double bodyTubeThickness = 0.0003;
		BodyTube bodytube = new BodyTube(bodytubeLength, bodytubeRadius, bodyTubeThickness);
		bodytube.setName("Body Tube");
		sustainerStage.addChild(bodytube);
		
		TrapezoidFinSet finset;
		{
			int finCount = 3;
			double finRootChord = .05;
			double finTipChord = .03;
			double finSweep = 0.02;
			double finHeight = 0.05;
			finset = new TrapezoidFinSet(finCount, finRootChord, finTipChord, finSweep, finHeight);
			finset.setThickness( 0.0032);
			finset.setRelativePosition(Position.BOTTOM);
			finset.setName("3 Fin Set");
			bodytube.addChild(finset);
			
			LaunchLug lug = new LaunchLug();
			lug.setName("Launch Lugs");
			lug.setRelativePosition(Position.TOP);
			lug.setAxialOffset(0.111);
			lug.setLength(0.050);
			lug.setOuterRadius(0.0022);
			lug.setInnerRadius(0.0020);
			bodytube.addChild(lug);
			
			InnerTube inner = new InnerTube();
			inner.setRelativePosition(Position.TOP);
			inner.setAxialOffset(0.133);
			inner.setLength(0.07);
			inner.setOuterRadius(0.009);
			inner.setThickness(0.0003);
			inner.setMotorMount(true);
			inner.setName("Motor Mount Tube");
			bodytube.addChild(inner);
			
			{
				// MotorBlock 
				EngineBlock thrustBlock= new EngineBlock();
				thrustBlock.setRelativePosition(Position.TOP);
				thrustBlock.setAxialOffset(0.0);
				thrustBlock.setLength(0.005);
				thrustBlock.setOuterRadius(0.009);
				thrustBlock.setThickness(0.0008);
				thrustBlock.setName("Engine Block");
				inner.addChild(thrustBlock);
				inner.setMotorMount( true);
				
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,fcid[0]);
					Motor mtr = TestRockets.generateMotor_A8_18mm();
					motorConfig.setEjectionDelay(0.0);
					motorConfig.setMotor( mtr);
					inner.setMotorConfig( motorConfig, fcid[0]);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,fcid[1]);
					Motor mtr = TestRockets.generateMotor_B4_18mm();
					motorConfig.setEjectionDelay(3.0);
					motorConfig.setMotor( mtr);
					inner.setMotorConfig( motorConfig, fcid[1]);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,fcid[2]);
					Motor mtr = TestRockets.generateMotor_C6_18mm();
					motorConfig.setEjectionDelay(3.0);
					motorConfig.setMotor( mtr);
					inner.setMotorConfig( motorConfig, fcid[2]);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,fcid[3]);
					Motor mtr = TestRockets.generateMotor_C6_18mm();
					motorConfig.setEjectionDelay(5.0);
					motorConfig.setMotor( mtr);
					inner.setMotorConfig( motorConfig, fcid[3]);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,fcid[4]);
					Motor mtr = TestRockets.generateMotor_C6_18mm();
					motorConfig.setEjectionDelay(7.0);
					motorConfig.setMotor( mtr);
					inner.setMotorConfig( motorConfig, fcid[4]);
				}
			}
		
			// parachute
			Parachute chute = new Parachute();
			chute.setRelativePosition(Position.TOP);
			chute.setName("Parachute");
			chute.setAxialOffset(0.028);
			chute.setOverrideMass(0.002);
			chute.setMassOverridden(true);
			bodytube.addChild(chute);
			
			// bulkhead x2
			CenteringRing centerings = new CenteringRing();
			centerings.setName("Centering Rings");
			centerings.setRelativePosition(Position.TOP);
			centerings.setAxialOffset(0.14);
			centerings.setLength(0.006);
			centerings.setInstanceCount(2);
			centerings.setInstanceSeparation(0.035);
			bodytube.addChild(centerings);
		}
		
		Material material = Application.getPreferences().getDefaultComponentMaterial(null, Material.Type.BULK);
		nosecone.setMaterial(material);
		bodytube.setMaterial(material);
		finset.setMaterial(material);
		
		{
			AxialStage boosterStage = new AxialStage();
			boosterStage.setName("Booster");
			
			BodyTube boosterTube = new BodyTube(0.06, bodytubeRadius, bodyTubeThickness);
			boosterStage.addChild(boosterTube);
			
			TubeCoupler coupler = new TubeCoupler();
			coupler.setName("Interstage");
			coupler.setOuterRadiusAutomatic(true);
			coupler.setThickness( bodyTubeThickness);
			coupler.setLength(0.03);
			coupler.setRelativePosition(Position.TOP);
			coupler.setPositionValue(-1.5);
			boosterTube.addChild(coupler);
			
			int finCount = 3;
			double finRootChord = .05;
			double finTipChord = .03;
			double finSweep = 0.02;
			double finHeight = 0.05;
			finset = new TrapezoidFinSet(finCount, finRootChord, finTipChord, finSweep, finHeight);
			finset.setThickness( 0.0032);
			finset.setRelativePosition(Position.BOTTOM);
			finset.setPositionValue(1);
			finset.setName("Booster Fins");
			boosterTube.addChild(finset);
			
			// Motor mount
			InnerTube boosterMMT = new InnerTube();
			boosterMMT.setName("Booster MMT");
			boosterMMT.setPositionValue(0.005);
			boosterMMT.setRelativePosition(Position.BOTTOM);
			boosterMMT.setOuterRadius(0.019 / 2);
			boosterMMT.setInnerRadius(0.018 / 2);
			boosterMMT.setLength(0.075);
			boosterTube.addChild(boosterMMT);
			
			rocket.addChild(boosterStage);

			boosterMMT.setMotorMount(true);
			{
				MotorConfiguration motorConfig= new MotorConfiguration(boosterMMT,fcid[0]);
				Motor mtr = generateMotor_D21_18mm();
				motorConfig.setMotor(mtr);
				boosterMMT.setMotorConfig( motorConfig, fcid[0]);
			}

		}
		rocket.getSelectedConfiguration().setAllStages();
		rocket.setSelectedConfiguration( selectedConfiguration );
		rocket.enableEvents();
		return rocket;
	}
	
	
	public static Rocket makeSmallFlyable() {
		double noseconeLength = 0.10, noseconeRadius = 0.01;
		double bodytubeLength = 0.20, bodytubeRadius = 0.01, bodytubeThickness = 0.001;
		
		int finCount = 3;
		@SuppressWarnings("unused")
		double finRootChord = 0.04, finTipChord = 0.05, finSweep = 0.01, finThickness = 0.003, finHeight = 0.03;
		
		Rocket rocket;
		AxialStage stage;
		NoseCone nosecone;
		BodyTube bodytube;
		TrapezoidFinSet finset;
		
		rocket = new Rocket();
		stage = new AxialStage();
		stage.setName("Stage1");
		
		nosecone = new NoseCone(Transition.Shape.ELLIPSOID, noseconeLength, noseconeRadius);
		bodytube = new BodyTube(bodytubeLength, bodytubeRadius, bodytubeThickness);
		
		finset = new TrapezoidFinSet(finCount, finRootChord, finTipChord, finSweep, finHeight);
		
		// Stage construction
		rocket.addChild(stage);
		
		// Component construction
		stage.addChild(nosecone);
		stage.addChild(bodytube);
		
		bodytube.addChild(finset);
		
		Material material = Application.getPreferences().getDefaultComponentMaterial(null, Material.Type.BULK);
		nosecone.setMaterial(material);
		bodytube.setMaterial(material);
		finset.setMaterial(material);
		
		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConfigurationId fcid = config.getFlightConfigurationID();
		
		ThrustCurveMotor motor = getTestMotor();
		MotorConfiguration instance = new MotorConfiguration( bodytube, fcid );
		instance.setMotor( motor);
		instance.setEjectionDelay(5);
		
		bodytube.setMotorConfig( instance, fcid);
		bodytube.setMotorOverhang(0.005);
		
		config.setAllStages();
		rocket.enableEvents();
		return rocket;
	}
	
	
	public static Rocket makeBigBlue() {
		Rocket rocket;
		AxialStage stage;
		NoseCone nosecone;
		BodyTube bodytube;
		FreeformFinSet finset;
		MassComponent mcomp;
		
		rocket = new Rocket();
		stage = new AxialStage();
		stage.setName("Stage1");
		
		nosecone = new NoseCone(Transition.Shape.ELLIPSOID, 0.105, 0.033);
		nosecone.setThickness(0.001);
		bodytube = new BodyTube(0.69, 0.033, 0.001);
		
		finset = new FreeformFinSet();
		try {
			finset.setPoints(new Coordinate[] {
					new Coordinate(0, 0),
					new Coordinate(0.115, 0.072),
					new Coordinate(0.255, 0.072),
					new Coordinate(0.255, 0.037),
					new Coordinate(0.150, 0)
			});
		} catch (IllegalFinPointException e) {
			e.printStackTrace();
		}
		finset.setThickness(0.003);
		finset.setFinCount(4);
		
		finset.setCantAngle(0 * Math.PI / 180);
		//System.err.println("Fin cant angle: " + (finset.getCantAngle() * 180 / Math.PI));
		
		mcomp = new MassComponent(0.2, 0.03, 0.045 + 0.060);
		mcomp.setRelativePosition(Position.TOP);
		mcomp.setPositionValue(0);
		
		// Stage construction
		rocket.addChild(stage);
		rocket.setPerfectFinish(false);
		
		// Component construction
		stage.addChild(nosecone);
		stage.addChild(bodytube);
		
		bodytube.addChild(finset);
		
		bodytube.addChild(mcomp);
		
		//		Material material = new Material("Test material", 500);
		//		nosecone.setMaterial(material);
		//		bodytube.setMaterial(material);
		//		finset.setMaterial(material);
		
//		FlightConfiguration config = rocket.getDefaultConfiguration();
//		FlightConfigurationID fcid = config.getFlightConfigurationID();
//		config.setAllStages();
		
		//		Motor m = Application.getMotorSetDatabase().findMotors(null, null, "F12J", Double.NaN, Double.NaN).get(0);
		//		bodytube.setMotor(id, m);
		//		bodytube.setMotorOverhang(0.005);
		
		rocket.enableEvents();
		return rocket;
	}
	
	
	
	
	public static Rocket makeIsoHaisu() {
		Rocket rocket;
		AxialStage stage;
		NoseCone nosecone;
		BodyTube tube1, tube2, tube3;
		TrapezoidFinSet finset;
		TrapezoidFinSet auxfinset;
		@SuppressWarnings("unused")
		MassComponent mcomp;
		
		final double R = 0.07;
		
		rocket = new Rocket();
		stage = new AxialStage();
		stage.setName("Stage1");
		
		nosecone = new NoseCone(Transition.Shape.OGIVE, 0.53, R);
		nosecone.setThickness(0.005);
		nosecone.setMassOverridden(true);
		nosecone.setOverrideMass(0.588);
		stage.addChild(nosecone);
		
		tube1 = new BodyTube(0.505, R, 0.005);
		tube1.setMassOverridden(true);
		tube1.setOverrideMass(0.366);
		stage.addChild(tube1);
		
		tube2 = new BodyTube(0.605, R, 0.005);
		tube2.setMassOverridden(true);
		tube2.setOverrideMass(0.427);
		stage.addChild(tube2);
		
		tube3 = new BodyTube(1.065, R, 0.005);
		tube3.setMassOverridden(true);
		tube3.setOverrideMass(0.730);
		stage.addChild(tube3);
		
		LaunchLug lug = new LaunchLug();
		tube1.addChild(lug);
		
		TubeCoupler coupler = new TubeCoupler();
		coupler.setOuterRadiusAutomatic(true);
		coupler.setThickness(0.005);
		coupler.setLength(0.28);
		coupler.setMassOverridden(true);
		coupler.setOverrideMass(0.360);
		coupler.setRelativePosition(Position.BOTTOM);
		coupler.setPositionValue(-0.14);
		tube1.addChild(coupler);
		
		// Parachute
		MassComponent mass = new MassComponent(0.05, 0.05, 0.280);
		mass.setRelativePosition(Position.TOP);
		mass.setPositionValue(0.2);
		tube1.addChild(mass);
		
		// Cord
		mass = new MassComponent(0.05, 0.05, 0.125);
		mass.setRelativePosition(Position.TOP);
		mass.setPositionValue(0.2);
		tube1.addChild(mass);
		
		// Payload
		mass = new MassComponent(0.40, R, 1.500);
		mass.setRelativePosition(Position.TOP);
		mass.setPositionValue(0.25);
		tube1.addChild(mass);
		
		auxfinset = new TrapezoidFinSet();
		auxfinset.setName("CONTROL");
		auxfinset.setFinCount(2);
		auxfinset.setRootChord(0.05);
		auxfinset.setTipChord(0.05);
		auxfinset.setHeight(0.10);
		auxfinset.setSweep(0);
		auxfinset.setThickness(0.008);
		auxfinset.setCrossSection(CrossSection.AIRFOIL);
		auxfinset.setRelativePosition(Position.TOP);
		auxfinset.setPositionValue(0.28);
		auxfinset.setBaseRotation(Math.PI / 2);
		tube1.addChild(auxfinset);
		
		coupler = new TubeCoupler();
		coupler.setOuterRadiusAutomatic(true);
		coupler.setLength(0.28);
		coupler.setRelativePosition(Position.TOP);
		coupler.setPositionValue(0.47);
		coupler.setMassOverridden(true);
		coupler.setOverrideMass(0.360);
		tube2.addChild(coupler);
		
		// Parachute
		mass = new MassComponent(0.1, 0.05, 0.028);
		mass.setRelativePosition(Position.TOP);
		mass.setPositionValue(0.14);
		tube2.addChild(mass);
		
		Bulkhead bulk = new Bulkhead();
		bulk.setOuterRadiusAutomatic(true);
		bulk.setMassOverridden(true);
		bulk.setOverrideMass(0.050);
		bulk.setRelativePosition(Position.TOP);
		bulk.setPositionValue(0.27);
		tube2.addChild(bulk);
		
		// Chord
		mass = new MassComponent(0.1, 0.05, 0.125);
		mass.setRelativePosition(Position.TOP);
		mass.setPositionValue(0.19);
		tube2.addChild(mass);
		
		InnerTube inner = new InnerTube();
		inner.setOuterRadius(0.08 / 2);
		inner.setInnerRadius(0.0762 / 2);
		inner.setLength(0.86);
		inner.setMassOverridden(true);
		inner.setOverrideMass(0.388);
		tube3.addChild(inner);
		
		CenteringRing center = new CenteringRing();
		center.setInnerRadiusAutomatic(true);
		center.setOuterRadiusAutomatic(true);
		center.setLength(0.005);
		center.setMassOverridden(true);
		center.setOverrideMass(0.038);
		center.setRelativePosition(Position.BOTTOM);
		center.setPositionValue(0);
		tube3.addChild(center);
		
		center = new CenteringRing();
		center.setInnerRadiusAutomatic(true);
		center.setOuterRadiusAutomatic(true);
		center.setLength(0.005);
		center.setMassOverridden(true);
		center.setOverrideMass(0.038);
		center.setRelativePosition(Position.TOP);
		center.setPositionValue(0.28);
		tube3.addChild(center);
		
		center = new CenteringRing();
		center.setInnerRadiusAutomatic(true);
		center.setOuterRadiusAutomatic(true);
		center.setLength(0.005);
		center.setMassOverridden(true);
		center.setOverrideMass(0.038);
		center.setRelativePosition(Position.TOP);
		center.setPositionValue(0.83);
		tube3.addChild(center);
		
		finset = new TrapezoidFinSet();
		finset.setRootChord(0.495);
		finset.setTipChord(0.1);
		finset.setHeight(0.185);
		finset.setThickness(0.005);
		finset.setSweep(0.3);
		finset.setRelativePosition(Position.BOTTOM);
		finset.setPositionValue(-0.03);
		finset.setBaseRotation(Math.PI / 2);
		tube3.addChild(finset);
		
		finset.setCantAngle(0 * Math.PI / 180);
		//System.err.println("Fin cant angle: " + (finset.getCantAngle() * 180 / Math.PI));
		
		// Stage construction
		rocket.addChild(stage);
		rocket.setPerfectFinish(false);
		
		FlightConfiguration config = rocket.getSelectedConfiguration();
//		FlightConfigurationID fcid = config.getFlightConfigurationID();

		//		Motor m = Application.getMotorSetDatabase().findMotors(null, null, "L540", Double.NaN, Double.NaN).get(0);
		//		tube3.setMotor(id, m);
		//		tube3.setMotorOverhang(0.02);
		
		//		tube3.setIgnitionEvent(MotorMount.IgnitionEvent.NEVER);
		
		config.setAllStages();
		rocket.enableEvents();
		return rocket;
	}
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	public static Rocket makeFalcon9Heavy() {
		Rocket rocket = new Rocket();
		rocket.setName("Falcon9H Scale Rocket");
		FlightConfiguration selConfig = rocket.getSelectedConfiguration();
		FlightConfigurationId selFCID = selConfig.getFlightConfigurationID();
		
		// ====== Payload Stage ======
		// ====== ====== ====== ======
		AxialStage payloadStage = new AxialStage();
		payloadStage.setName("Payload Fairing");
		rocket.addChild(payloadStage);

		{
			NoseCone payloadFairingNoseCone = new NoseCone(Transition.Shape.POWER, 0.118, 0.052);
			payloadFairingNoseCone.setName("PL Fairing Nose");
			payloadFairingNoseCone.setThickness(0.001);
			payloadFairingNoseCone.setShapeParameter(0.5);
			//payloadFairingNoseCone.setLength(0.118);
			//payloadFairingNoseCone.setAftRadius(0.052);
			payloadFairingNoseCone.setAftShoulderRadius( 0.051 );
	        payloadFairingNoseCone.setAftShoulderLength( 0.02 );
	        payloadFairingNoseCone.setAftShoulderThickness( 0.001 );
	        payloadFairingNoseCone.setAftShoulderCapped( false );
	        payloadStage.addChild(payloadFairingNoseCone);
			
			BodyTube payloadBody = new BodyTube(0.132, 0.052, 0.001);
			payloadBody.setName("PL Fairing Body");
			payloadStage.addChild(payloadBody);
			
			Transition payloadFairingTail = new Transition();
			payloadFairingTail.setName("PL Fairing Transition");
			payloadFairingTail.setLength(0.014);
			payloadFairingTail.setThickness(0.002);
			payloadFairingTail.setForeRadiusAutomatic(true);
			payloadFairingTail.setAftRadiusAutomatic(true);
			payloadStage.addChild(payloadFairingTail);
			
			BodyTube upperStageBody= new BodyTube(0.18, 0.0385, 0.001);
			upperStageBody.setName("Upper Stage Body ");
			payloadStage.addChild( upperStageBody);
			
			{
				// Parachute
				Parachute upperChute= new Parachute();
				upperChute.setName("Parachute");
				upperChute.setRelativePosition(Position.MIDDLE);
				upperChute.setPositionValue(0.0);
				upperChute.setDiameter(0.3);
				upperChute.setLineCount(6);
				upperChute.setLineLength(0.3);
				upperStageBody.addChild( upperChute);
				
				// Cord
				ShockCord cord = new ShockCord();
				cord.setName("Shock Cord");
				cord.setRelativePosition(Position.BOTTOM);
				cord.setPositionValue(0.0);
				cord.setCordLength(0.4);
		    	upperStageBody.addChild( cord);
			}
			
			BodyTube interstage= new BodyTube(0.12, 0.0385, 0.001);
			interstage.setName("Interstage");
			payloadStage.addChild( interstage);
		}

		// ====== Core Stage ====== 
		// ====== ====== ====== ======
		AxialStage coreStage = new AxialStage();
		coreStage.setName("Core Stage");
		rocket.addChild(coreStage);

		{
			BodyTube coreBody = new BodyTube(0.8, 0.0385, 0.001);
			// 74 mm inner dia
			coreBody.setName("Core Stage Body");
			coreBody.setMotorMount(true);
			coreStage.addChild( coreBody);
			{
				MotorConfiguration motorConfig = new MotorConfiguration(coreBody, selFCID);
				Motor mtr = TestRockets.generateMotor_M1350_75mm();
				motorConfig.setMotor( mtr);
				coreBody.setMotorMount( true);
				FlightConfigurationId motorConfigId = selConfig.getFlightConfigurationID();
				coreBody.setMotorConfig( motorConfig, motorConfigId);	 
			}
			
			TrapezoidFinSet coreFins = new TrapezoidFinSet();
			coreFins.setName("Core Fins");
			coreFins.setFinCount(4);
			coreFins.setRelativePosition(Position.BOTTOM);
			coreFins.setPositionValue(0.0);
			coreFins.setBaseRotation( Math.PI / 4);
			coreFins.setThickness(0.003);
			coreFins.setCrossSection(CrossSection.ROUNDED);
			coreFins.setRootChord(0.32);
			coreFins.setTipChord(0.12);
			coreFins.setHeight(0.12);
			coreFins.setSweep(0.18);
			coreBody.addChild(coreFins);
		
			
			// ====== Booster Stage Set ======
			// ====== ====== ====== ======
			ParallelStage boosterStage = new ParallelStage();
			boosterStage.setName("Booster Stage");
			coreStage.addChild( boosterStage);
			boosterStage.setRelativePositionMethod(Position.BOTTOM);
			boosterStage.setAxialOffset(0.0);
			boosterStage.setInstanceCount(2);
			boosterStage.setRadialOffset(0.075);
			
			{
				NoseCone boosterCone = new NoseCone(Transition.Shape.POWER, 0.08, 0.0385);
				boosterCone.setShapeParameter(0.5);
				boosterCone.setName("Booster Nose");
				boosterCone.setThickness(0.002);
				//payloadFairingNoseCone.setLength(0.118);
				//payloadFairingNoseCone.setAftRadius(0.052);
				boosterCone.setAftShoulderRadius( 0.051 );
				boosterCone.setAftShoulderLength( 0.02 );
				boosterCone.setAftShoulderThickness( 0.001 );
				boosterCone.setAftShoulderCapped( false );
				boosterStage.addChild( boosterCone);
				
				BodyTube boosterBody = new BodyTube(0.8, 0.0385, 0.001);
				boosterBody.setName("Booster Body");
				boosterBody.setOuterRadiusAutomatic(true);
				boosterStage.addChild( boosterBody);
				
				{
					InnerTube boosterMotorTubes = new InnerTube();
					boosterMotorTubes.setName("Booster Motor Tubes");
					boosterMotorTubes.setLength(0.15);
					boosterMotorTubes.setOuterRadius(0.015); // => 29mm motors
					boosterMotorTubes.setThickness(0.0005);
					boosterMotorTubes.setClusterConfiguration( ClusterConfiguration.CONFIGURATIONS[5]); // 4-ring
					//boosterMotorTubes.setClusterConfiguration( ClusterConfiguration.CONFIGURATIONS[13]); // 9-star
					boosterMotorTubes.setClusterScale(1.0);
					boosterBody.addChild( boosterMotorTubes);
					
					FlightConfigurationId motorConfigId = selConfig.getFlightConfigurationID();
					MotorConfiguration motorConfig = new MotorConfiguration( boosterMotorTubes, selFCID);
					Motor mtr = TestRockets.generateMotor_G77_29mm();
					motorConfig.setMotor(mtr);
					boosterMotorTubes.setMotorConfig( motorConfig, motorConfigId);
					boosterMotorTubes.setMotorOverhang(0.01234);
				}
			}
		}
		
		rocket.enableEvents();
		rocket.setSelectedConfiguration(selConfig);
		selConfig.setAllStages();
		
		return rocket;
	}
	
	/*
	 * Create a new file version 1.00 rocket
	 */
	public static OpenRocketDocument makeTestRocket_v100() {
		Rocket rocket = new Rocket();
		rocket.setName("v100");
		
		// make stage
		AxialStage stage = new AxialStage();
		stage.setName("Stage1");
		
		// make body tube 
		BodyTube bodyTube = new BodyTube(12, 1, 0.05);
		stage.addChild(bodyTube);
		
		rocket.addChild(stage);
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}
	
	/*
	 * Create a new file version 1.01 rocket with finTabs
	 */
	public static OpenRocketDocument makeTestRocket_v101_withFinTabs() {
		
		Rocket rocket = new Rocket();
		rocket.setName("v101_withFinTabs");
		
		// make stage
		AxialStage stage = new AxialStage();
		stage.setName("Stage1");
		rocket.addChild(stage);
		
		// make body tube 
		BodyTube bodyTube = new BodyTube(12, 1, 0.05);
		stage.addChild(bodyTube);
		
		// make fins with fin tabs and add to body tube
		TrapezoidFinSet fins = new TrapezoidFinSet();
		fins.setFinCount(3);
		fins.setFinShape(1.0, 1.0, 0.0, 1.0, .005);
		fins.setTabHeight(0.25);
		fins.setTabLength(0.25);
		bodyTube.addChild(fins);
		
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}
	
	/*
	 * Create a new file version 1.01 rocket with tube coupler child
	 */
	public static OpenRocketDocument makeTestRocket_v101_withTubeCouplerChild() {
		
		Rocket rocket = new Rocket();
		rocket.setName("v101_withTubeCouplerChild");
		
		// make stage
		AxialStage stage = new AxialStage();
		stage.setName("Stage1");
		rocket.addChild(stage);
		
		// make body tube 
		BodyTube bodyTube = new BodyTube(12, 1, 0.05);
		stage.addChild(bodyTube);
		
		// make tube coupler with centering ring, add to stage
		TubeCoupler tubeCoupler = new TubeCoupler();
		CenteringRing centeringRing = new CenteringRing();
		tubeCoupler.addChild(centeringRing);
		bodyTube.addChild(tubeCoupler);
		
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}
	
	/*
	 * Create a new file version 1.04 rocket with motor in flight config
	 */
	public static OpenRocketDocument makeTestRocket_v104_withMotor() {
		
		Rocket rocket = new Rocket();
		rocket.setName("v104_withMotorConfig");
		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConfigurationId fcid = config.getFlightConfigurationID();
		config.setName("F12X");
		
		// make stage
		AxialStage stage = new AxialStage();
		stage.setName("Stage1");
		rocket.addChild(stage);
		
		// make body tube 
		BodyTube bodyTube = new BodyTube(12, 1, 0.05);
		stage.addChild(bodyTube);
		
		// make inner tube with motor mount flag set
		InnerTube innerTube = new InnerTube();
		bodyTube.addChild(innerTube);
		
		// create motor config and add a motor to it
		ThrustCurveMotor motor = getTestMotor();
		MotorConfiguration motorConfig = new MotorConfiguration(innerTube, fcid);
		motorConfig.setMotor(motor);
		motorConfig.setEjectionDelay(5);
		
		// add motor config to inner tube (motor mount)
		innerTube.setMotorConfig( motorConfig, fcid);
		
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}
	
	/*
	 * Create a new file version 1.04 rocket with simulation data
	 */
	public static OpenRocketDocument makeTestRocket_v104_withSimulationData() {
		
		Rocket rocket = new Rocket();
		rocket.setName("v104_withSimulationData");
		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConfigurationId fcid = config.getFlightConfigurationID();
		config.setName("F12X");
		
		// make stage
		AxialStage stage = new AxialStage();
		stage.setName("Stage1");
		rocket.addChild(stage);
		
		// make body tube 
		BodyTube bodyTube = new BodyTube(12, 1, 0.05);
		stage.addChild(bodyTube);
		
		// make inner tube with motor mount flag set
		InnerTube innerTube = new InnerTube();
		bodyTube.addChild(innerTube);
		
		// create motor config and add a motor to it
		ThrustCurveMotor motor = getTestMotor();
		MotorConfiguration motorConfig = new MotorConfiguration(innerTube, fcid);
		motorConfig.setMotor(motor);
		motorConfig.setEjectionDelay(5);
		
		// add motor config to inner tube (motor mount)
		innerTube.setMotorConfig(motorConfig, fcid);
				
		OpenRocketDocument rocketDoc = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		
		// create simulation data
		Simulation simulation1 = new Simulation(rocket);
		simulation1.setFlightConfigurationId(fcid);
		
		rocketDoc.addSimulation(simulation1);
		Simulation simulation2 = new Simulation(rocket);
		rocketDoc.addSimulation(simulation2);
		
		rocket.enableEvents();
		return rocketDoc;
	}
	
	/*
	 * Create a new file version 1.05 rocket with custom expression 
	 */
	public static OpenRocketDocument makeTestRocket_v105_withCustomExpression() {
		Rocket rocket = new Rocket();
		rocket.setName("v105_withCustomExpression");
		
		// make stage
		AxialStage stage = new AxialStage();
		stage.setName("Stage1");
		rocket.addChild(stage);
		
		// make body tube 
		BodyTube bodyTube = new BodyTube();
		stage.addChild(bodyTube);
		
		OpenRocketDocument rocketDoc = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		
		CustomExpression expression = new CustomExpression(rocketDoc, "name", "symbol", "unit", "expression");
		rocketDoc.addCustomExpression(expression);
		
		rocket.enableEvents();
		return rocketDoc;
	}
	
	/*
	 * Create a new file version 1.05 rocket with component preset
	 */
	public static OpenRocketDocument makeTestRocket_v105_withComponentPreset() {
		Rocket rocket = new Rocket();
		rocket.setName("v105_withComponentPreset");
		
		// make stage
		AxialStage stage = new AxialStage();
		stage.setName("Stage1");
		rocket.addChild(stage);
		
		// make body tube 
		BodyTube bodyTube = new BodyTube();
		
		TypedPropertyMap presetspec = new TypedPropertyMap();
		presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.BODY_TUBE);
		presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
		presetspec.put(ComponentPreset.PARTNO, "partno");
		presetspec.put(ComponentPreset.LENGTH, 2.0);
		presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
		presetspec.put(ComponentPreset.INNER_DIAMETER, 1.0);
		presetspec.put(ComponentPreset.MASS, 100.0);
		ComponentPreset preset;
		try {
			preset = ComponentPresetFactory.create(presetspec);
			bodyTube.loadPreset(preset);
			stage.addChild(bodyTube);
		} catch (InvalidComponentPresetException e) {
			// should never happen
			e.printStackTrace();
		}
		
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}
	
	/*
	 * Create a new file version 1.05 rocket with lower stage recovery device
	 */
	public static OpenRocketDocument makeTestRocket_v105_withLowerStageRecoveryDevice() {
		Rocket rocket = new Rocket();
		rocket.setName("v105_withLowerStageRecoveryDevice");
		
		// make 1st stage
		AxialStage stage1 = new AxialStage();
		stage1.setName("Stage1");
		rocket.addChild(stage1);
		
		// make 1st stage body tube
		BodyTube bodyTube1 = new BodyTube(5, 1, 0.05);
		stage1.addChild(bodyTube1);
		
		// make 1st stage recovery device with deployment config in default
		RecoveryDevice parachute = new Parachute();
		DeploymentConfiguration deploymentConfig = new DeploymentConfiguration();
		deploymentConfig.setDeployEvent(DeployEvent.LOWER_STAGE_SEPARATION);
		parachute.getDeploymentConfigurations().setDefault(deploymentConfig);
		bodyTube1.addChild(parachute);
		
		// make 2nd stage
		AxialStage stage2 = new AxialStage();
		stage2.setName("Stage2");
		rocket.addChild(stage2);
		
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}
	
	/*
	 * Create a new file version 1.06 rocket with appearance
	 */
	public static OpenRocketDocument makeTestRocket_v106_withAppearance() {
		Rocket rocket = new Rocket();
		rocket.setName("v106_withAppearance");
		
		// make stage
		AxialStage stage = new AxialStage();
		stage.setName("Stage1");
		rocket.addChild(stage);
		
		// make body tube with an appearance setting
		BodyTube bodyTube = new BodyTube(12, 1, 0.05);
		Appearance appearance = new Appearance(new Color(100, 25, 50), 1, null);
		bodyTube.setAppearance(appearance);
		stage.addChild(bodyTube);
		
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}
	
	/*
	 * Create a new file version 1.06 rocket with flight configuration with motor mount ignition configuration
	 */
	public static OpenRocketDocument makeTestRocket_v106_withMotorMountIgnitionConfig() {
		Rocket rocket = new Rocket();
		rocket.setName("v106_withwithMotorMountIgnitionConfig");
		FlightConfigurationId fcid = new FlightConfigurationId();
		
		// make stage
		AxialStage stage = new AxialStage();
		stage.setName("Stage1");
		rocket.addChild(stage);
		
		// make body tube 
		BodyTube bodyTube = new BodyTube(12, 1, 0.05);
		stage.addChild(bodyTube);
		
		// make inner tube with motor mount flag set
		InnerTube innerTube = new InnerTube();
		bodyTube.addChild(innerTube);
		
		// make inner tube with motor mount flag set
		MotorConfiguration motorConfig = new MotorConfiguration(innerTube, fcid);
		Motor mtr = getTestMotor();
		motorConfig.setMotor( mtr);
		innerTube.setMotorConfig(motorConfig,fcid);
		
		// set ignition parameters for motor mount
		// inst.setIgnitionEvent( IgnitionEvent.AUTOMATIC);
		motorConfig.setIgnitionDelay(2);
		
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}
	
	/*
	 * Create a new file version 1.06 rocket with flight configuration with recovery device deployment configuration non-default
	 */
	public static OpenRocketDocument makeTestRocket_v106_withRecoveryDeviceDeploymentConfig() {
		Rocket rocket = new Rocket();
		rocket.setName("v106_withRecoveryDeviceDeploymentConfig");
		FlightConfigurationId testFCID = new FlightConfigurationId("testParachute");
		
		// make stage
		AxialStage stage = new AxialStage();
		stage.setName("Stage1");
		rocket.addChild(stage);
		
		// make body tube 
		BodyTube bodyTube = new BodyTube(12, 1, 0.05);
		stage.addChild(bodyTube);
		
		// make recovery device with deployment config
		RecoveryDevice parachute = new Parachute();
		DeploymentConfiguration deploymentConfig = new DeploymentConfiguration();
		deploymentConfig.setDeployAltitude(1000);
		parachute.getDeploymentConfigurations().set(testFCID, deploymentConfig);
		bodyTube.addChild(parachute);
		
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}
	
	/*
	 * Create a new file version 1.06 rocket with flight configuration with stage separation configuration
	 */
	public static OpenRocketDocument makeTestRocket_v106_withStageSeparationConfig() {
		Rocket rocket = new Rocket();
		rocket.setName("v106_withStageSeparationConfig");
		FlightConfigurationId fcid = new FlightConfigurationId("3SecondDelay");
		// make 1st stage
		AxialStage stage1 = new AxialStage();
		stage1.setName("Stage1");
		rocket.addChild(stage1);
		
		// make 1st stage body tube
		BodyTube bodyTube1 = new BodyTube(5, 1, 0.05);
		stage1.addChild(bodyTube1);
		
		// make1st stage  recovery device 
		RecoveryDevice parachute = new Parachute();
		bodyTube1.addChild(parachute);
		
		// set stage separation configuration
		StageSeparationConfiguration stageSepConfig = new StageSeparationConfiguration();
		stageSepConfig.setSeparationDelay(3);
		stage1.getSeparationConfigurations().set(fcid, stageSepConfig);
		
		// make 2nd stage
		AxialStage stage2 = new AxialStage();
		stage2.setName("Stage2");
		rocket.addChild(stage2);
		
		// make 2st stage body tube
		BodyTube bodyTube2 = new BodyTube(12, 1, 0.05);
		stage2.addChild(bodyTube2);
		
		rocket.enableEvents();
		return OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
	}
	
	
	public static OpenRocketDocument makeTestRocket_v107_withSimulationExtension(String script) {
		Rocket rocket = makeSmallFlyable();
		OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		Simulation sim = new Simulation(rocket);
		ScriptingExtension ext = new ScriptingExtension();
		ext.setEnabled(true);
		ext.setLanguage("JavaScript");
		ext.setScript(script);
		sim.getSimulationExtensions().add(ext);
		document.addSimulation(sim);
		
		rocket.enableEvents();
		return document;
	}
	
	public static OpenRocketDocument makeTestRocket_v108_withBoosters() {
		Rocket rocket = makeFalcon9Heavy();
		OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		return document;
	}
	
	/*
	 * Create a new test rocket for testing OpenRocketSaver.estimateFileSize()
	 */
	public static OpenRocketDocument makeTestRocket_for_estimateFileSize() {
		Rocket rocket = new Rocket();
		rocket.setName("for_estimateFileSize");
		
		// make 1st stage
		AxialStage stage1 = new AxialStage();
		stage1.setName("Stage1");
		rocket.addChild(stage1);
		
		// make 1st stage body tube
		BodyTube bodyTube1 = new BodyTube(5, 1, 0.05);
		stage1.addChild(bodyTube1);
		
		TrapezoidFinSet fins1 = new TrapezoidFinSet();
		fins1.setFinCount(3);
		fins1.setFinShape(1.5, 1.5, 0.0, 1.5, .005);
		bodyTube1.addChild(fins1);
		
		// make 1st stage recovery device with deployment config in default
		RecoveryDevice parachute = new Parachute();
		DeploymentConfiguration deploymentConfig = new DeploymentConfiguration();
		deploymentConfig.setDeployEvent(DeployEvent.LOWER_STAGE_SEPARATION);
		deploymentConfig.setDeployEvent(DeployEvent.ALTITUDE);
		parachute.getDeploymentConfigurations().setDefault(deploymentConfig);
		bodyTube1.addChild(parachute);
		
		// make 2nd stage
		AxialStage stage2 = new AxialStage();
		stage2.setName("Stage2");
		rocket.addChild(stage2);
		
		// make 2nd stage nose cone
		NoseCone noseCone = new NoseCone(Transition.Shape.OGIVE, 6 * 0.5, 0.5);
		stage2.addChild(noseCone);
		
		// make 2nd stage body tube
		BodyTube bodyTube2 = new BodyTube(15, 1, 0.05);
		stage2.addChild(bodyTube2);
		
		// make 2nd stage fins
		TrapezoidFinSet fins2 = new TrapezoidFinSet();
		fins2.setFinCount(3);
		fins2.setFinShape(1.0, 1.0, 0.0, 1.0, .005);
		bodyTube2.addChild(fins2);
		
		OpenRocketDocument rocketDoc = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		
		// create simulation data
		Simulation simulation1 = new Simulation(rocket);
		simulation1.getOptions().setISAAtmosphere(false); // helps cover code in saveComponent()
		simulation1.getOptions().setTimeStep(0.05);
		rocketDoc.addSimulation(simulation1);
		
		Simulation simulation2 = new Simulation(rocket);
		simulation2.getOptions().setISAAtmosphere(true); // helps cover code in saveComponent()
		simulation2.getOptions().setTimeStep(0.05);
		rocketDoc.addSimulation(simulation2);
		
		SimulationListener simulationListener = new AbstractSimulationListener();
		try {
			simulation1.simulate(simulationListener);
			simulation2.simulate(simulationListener);
		} catch (SimulationException e) {
			// do nothing, we don't care
		}
		
		rocket.enableEvents();
		return rocketDoc;
	}
	

	
}
