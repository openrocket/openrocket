package net.sf.openrocket.util;

import java.util.ArrayList;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Random;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
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
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.InstanceContext;
import net.sf.openrocket.rocketcomponent.InstanceMap;
import net.sf.openrocket.rocketcomponent.InternalComponent;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.ReferenceType;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.ShockCord;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.rocketcomponent.position.*;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.impl.ScriptingExtension;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.startup.Application;

public class TestRockets {
	
	public final static FlightConfigurationId TEST_FCID_0 = new FlightConfigurationId("d010716e-ce0e-469d-ae46-190f3653ebbf");
	public final static FlightConfigurationId TEST_FCID_1 = new FlightConfigurationId("f41bee5b-ebb8-4d92-bce7-53001577a313");
	public final static FlightConfigurationId TEST_FCID_2 = new FlightConfigurationId("3e8d1280-53c2-4234-89a7-de215ef5cd69");
	public final static FlightConfigurationId TEST_FCID_3 = new FlightConfigurationId("415a5485-f2da-4c2a-8803-394220ae58b8");
	public final static FlightConfigurationId TEST_FCID_4 = new FlightConfigurationId("5abc18ec-a200-46f1-90c4-60b6995fc933");

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
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("A"))
				.setDesignation("F12X")
				.setDescription("Desc")
				.setMotorType(Motor.Type.UNKNOWN)
				.setStandardDelays(new double[] {})
				.setDiameter(0.024)
				.setLength(0.07)
				.setTimePoints(new double[] { 0, 1, 2 })
				.setThrustPoints(new double[] { 0, 1, 0 })
				.setCGPoints(new Coordinate[] { Coordinate.NUL, Coordinate.NUL, Coordinate.NUL })
				.setDigest("digestA")
				.build();
	} 
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_A8_18mm(){
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("Estes"))
				.setDesignation("A8")
				.setDescription(" SU Black Powder")
				.setMotorType(Motor.Type.SINGLE)
				.setStandardDelays(new double[] {0,3,5})
				.setDiameter(0.018)
				.setLength(0.070)
				.setTimePoints(new double[] { 0, 1, 2 })
				.setThrustPoints(new double[] { 0, 9, 0 })
				.setCGPoints(new Coordinate[] {
						new Coordinate(0.035, 0, 0, 0.0164),new Coordinate(.035, 0, 0, 0.0145),new Coordinate(.035, 0, 0, 0.0131)})
				.setDigest("digest A8 test")
				.build();
	}
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_B4_18mm(){
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("Estes"))
				.setDesignation("B4")
				.setDescription(" SU Black Powder")
				.setMotorType(Motor.Type.SINGLE)
				.setStandardDelays(new double[] {0,3,5})
				.setDiameter(0.018)
				.setLength(0.070)
				.setTimePoints(new double[] { 0, 1, 2 })
				.setThrustPoints(new double[] { 0, 11.4, 0 })
				.setCGPoints(new Coordinate[] {
						new Coordinate(0.035, 0, 0, 0.0195),new Coordinate(.035, 0, 0, 0.0155),new Coordinate(.035, 0, 0, 0.013)})
				.setDigest("digest B4 test")
				.build();
	}
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_C6_18mm(){
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("Estes"))
				.setDesignation("C6")
				.setDescription(" SU Black Powder")
				.setMotorType(Motor.Type.SINGLE)
				.setStandardDelays(new double[] {0,3,5,7})
				.setDiameter(0.018)
				.setLength(0.070)
				.setTimePoints(new double[] { 0, 1, 2 })
				.setThrustPoints(new double[] { 0, 6, 0 })
				.setCGPoints(new Coordinate[] {
						new Coordinate(0.035, 0, 0, 0.0227),new Coordinate(.035, 0, 0, 0.0165),new Coordinate(.035, 0, 0, 0.012)})
				.setDigest("digest C6 test")
				.build();
	}
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_D21_18mm(){
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("AeroTech"))
				.setDesignation("D21")
				.setDescription("Desc")
				.setMotorType(Motor.Type.SINGLE)
				.setStandardDelays(new double[] {})
				.setDiameter(0.018)
				.setLength(0.070)
				.setTimePoints(new double[] { 0, 1, 2 })
				.setThrustPoints(new double[] { 0, 32, 0 })
				.setCGPoints(new Coordinate[] {
						new Coordinate(.035, 0, 0, 0.025),new Coordinate(.035, 0, 0, .020),new Coordinate(.035, 0, 0, 0.0154)})
				.setDigest("digest D21 test")
				.build();
	}
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_M1350_75mm(){
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("AeroTech"))
				.setDesignation("M1350")
				.setDescription("Desc")
				.setMotorType(Motor.Type.SINGLE)
				.setStandardDelays(new double[] {})
				.setDiameter(0.075)
				.setLength(0.622)
				.setTimePoints(new double[] { 0, 1, 2 })
				.setThrustPoints(new double[] { 0, 1357, 0 })
				.setCGPoints(new Coordinate[] {
						new Coordinate(.311, 0, 0, 4.808),new Coordinate(.311, 0, 0, 3.389),new Coordinate(.311, 0, 0, 1.970)})
				.setDigest("digest M1350 test")
				.build();
	}
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	private static Motor generateMotor_G77_29mm(){
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("AeroTech"))
				.setDesignation("G77")
				.setDescription("Desc")
				.setMotorType(Motor.Type.SINGLE)
				.setStandardDelays(new double[] {4,7,10})
				.setDiameter(0.029)
				.setLength(0.124)
				.setTimePoints(new double[] { 0, 1, 2 })
				.setThrustPoints(new double[] { 0, 1, 0 })
				.setCGPoints(new Coordinate[] {
						new Coordinate(.062, 0, 0, 0.123),new Coordinate(.062, 0, 0, .0935),new Coordinate(.062, 0, 0, 0.064)})
				.setDigest("digest G77 test")
				.build();
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
			i.setAxialMethod((AxialMethod) randomEnum(AxialMethod.class));
			i.setAxialOffset(rnd(0.3));
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
		
		rocket.createFlightConfiguration( TEST_FCID_0 );
		rocket.createFlightConfiguration( TEST_FCID_1 );
		rocket.createFlightConfiguration( TEST_FCID_2 );
		rocket.createFlightConfiguration( TEST_FCID_3 );
		rocket.createFlightConfiguration( TEST_FCID_4 );
		
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
			finset.setAxialMethod(AxialMethod.BOTTOM);
			finset.setName("3 Fin Set");
			bodytube.addChild(finset);
			
			LaunchLug lug = new LaunchLug();
			lug.setName("Launch Lugs");
			lug.setAxialMethod(AxialMethod.TOP);
			lug.setAxialOffset(0.111);
			lug.setLength(0.050);
			lug.setOuterRadius(0.0022);
			lug.setInnerRadius(0.0020);
			bodytube.addChild(lug);
			
			InnerTube inner = new InnerTube();
			inner.setAxialMethod(AxialMethod.TOP);
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
				thrustBlock.setAxialMethod(AxialMethod.TOP);
				thrustBlock.setAxialOffset(0.0);
				thrustBlock.setLength(0.005);
				thrustBlock.setOuterRadius(0.009);
				thrustBlock.setThickness(0.0008);
				thrustBlock.setName("Engine Block");
				inner.addChild(thrustBlock);
				inner.setMotorMount( true);
			
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner, TEST_FCID_0);
					Motor mtr =	TestRockets.generateMotor_A8_18mm();
					motorConfig.setMotor( mtr);
					motorConfig.setEjectionDelay(0.0);
					inner.setMotorConfig( motorConfig, TEST_FCID_0);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,TEST_FCID_1);
					Motor mtr =	TestRockets.generateMotor_B4_18mm();
					motorConfig.setMotor( mtr);
					motorConfig.setEjectionDelay(3.0);
					inner.setMotorConfig( motorConfig, TEST_FCID_1);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner, TEST_FCID_2);
					Motor mtr =	TestRockets.generateMotor_C6_18mm();
					motorConfig.setEjectionDelay(3.0);
					motorConfig.setMotor( mtr);
					inner.setMotorConfig( motorConfig, TEST_FCID_2);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,TEST_FCID_3);
					Motor mtr =	TestRockets.generateMotor_C6_18mm();
					motorConfig.setEjectionDelay(5.0);
					motorConfig.setMotor( mtr);
					inner.setMotorConfig( motorConfig, TEST_FCID_3);
				}
				{
					MotorConfiguration motorConfig = new MotorConfiguration(inner,TEST_FCID_4);
					Motor mtr =	TestRockets.generateMotor_C6_18mm();
					motorConfig.setEjectionDelay(7.0);
					motorConfig.setMotor( mtr);
					inner.setMotorConfig( motorConfig, TEST_FCID_4);
				}
			}
		
			// parachute
			Parachute chute = new Parachute();
			chute.setAxialMethod(AxialMethod.TOP);
			chute.setName("Parachute");
			chute.setAxialOffset(0.028);
			chute.setOverrideMass(0.002);
			chute.setMassOverridden(true);
			bodytube.addChild(chute);
			
			// bulkhead x2
			CenteringRing centerings = new CenteringRing();
			centerings.setName("Centering Rings");
			centerings.setAxialMethod(AxialMethod.TOP);
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
		
		// preserve default default configuration of rocket -- to test what the default is set to upon initialization.
		
		rocket.enableEvents();
		return rocket;
	}
	
	public static final void splitRocketFins( BodyTube body, TrapezoidFinSet fins, int finCount){
		// actually remove the fins
		body.removeChild(fins);

		TrapezoidFinSet template = fins; 
		template.setFinCount(1);
		// and manually add in the equivalent the others
		for(int finNumber=1; finNumber<finCount; ++finNumber){
			final double rootChord = template.getRootChord();
			final double tipChord = template.getTipChord();
			final double sweep = template.getSweep();
			final double height = template.getHeight();
			final TrapezoidFinSet singleFin = new TrapezoidFinSet(1, rootChord, tipChord, sweep, height);
			singleFin.setAngleOffset( finNumber * Math.PI * 2.0 / finCount);
			singleFin.setThickness( template.getThickness());
			singleFin.setAxialMethod( template.getAxialMethod());
			singleFin.setName(String.format("Single Fin #%d", finNumber));
			body.addChild(singleFin);
		}
	}

	// This is an extra stage tacked onto the end of an Estes Alpha III
	// http://www.rocketreviews.com/alpha-iii---estes-221256.html
	// 
	// This function is used for unit, integration tests, DO NOT CHANGE WITHOUT UPDATING TESTS
	public static final Rocket makeBeta(){
		Rocket rocket = makeEstesAlphaIII();
		rocket.setName("Kit-bash Beta");
		
		AxialStage sustainerStage = (AxialStage)rocket.getChild(0);
		sustainerStage.setName( "Sustainer Stage");
		BodyTube sustainerBody = (BodyTube)sustainerStage.getChild(1);
		final double sustainerRadius = sustainerBody.getAftRadius();
		final double sustainerThickness = sustainerBody.getThickness();
		
		AxialStage boosterStage = new AxialStage();
		boosterStage.setName("Booster Stage");
		rocket.addChild( boosterStage );
		{
			BodyTube boosterBody = new BodyTube(0.06, sustainerRadius, sustainerThickness);
			boosterBody.setName("Booster Body");
			boosterStage.addChild( boosterBody);
			{
				TubeCoupler coupler = new TubeCoupler();
				coupler.setName("Coupler");
				coupler.setOuterRadiusAutomatic(true);
				coupler.setThickness( sustainerThickness );
				coupler.setLength(0.03);
				coupler.setAxialMethod(AxialMethod.TOP);
				coupler.setAxialOffset(-0.015);
				boosterBody.addChild(coupler);
				
				int finCount = 3;
				double finRootChord = .05;
				double finTipChord = .03;
				double finSweep = 0.02;
				double finHeight = 0.03;
				FinSet finset = new TrapezoidFinSet(finCount, finRootChord, finTipChord, finSweep, finHeight);
				finset.setName("Booster Fins");
				finset.setThickness( 0.0032);
				finset.setAxialMethod(AxialMethod.BOTTOM);
				finset.setAxialOffset(0.);
				boosterBody.addChild(finset);

				// Motor mount
				InnerTube boosterMMT = new InnerTube();
				boosterMMT.setName("Booster MMT");
				boosterMMT.setAxialOffset(0.005);
				boosterMMT.setAxialMethod(AxialMethod.BOTTOM);
				boosterMMT.setOuterRadius(0.019 / 2);
				boosterMMT.setInnerRadius(0.018 / 2);
				boosterMMT.setLength(0.05);
				boosterMMT.setMotorMount(true);
				{
					MotorConfiguration motorConfig= new MotorConfiguration(boosterMMT, TEST_FCID_1 );
					Motor mtr = generateMotor_D21_18mm();
					motorConfig.setMotor(mtr);
					boosterMMT.setMotorConfig( motorConfig, TEST_FCID_1);
				}
				boosterBody.addChild(boosterMMT);
			}

			// Tail Cone
			Transition boosterTail = new Transition();
			boosterTail.setForeRadius(0.012);
			boosterTail.setAftRadius(0.01);
			boosterTail.setLength(0.005);
			boosterTail.setName("Booster Tail Cone");
			boosterStage.addChild( boosterTail);
		}

		rocket.setSelectedConfiguration( TEST_FCID_1 );
		rocket.getSelectedConfiguration().setAllStages();
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
		final Coordinate[] finPoints = {
				new Coordinate(0, 0),
				new Coordinate(0.115, 0.072),
				new Coordinate(0.255, 0.072),
				new Coordinate(0.255, 0.037),
				new Coordinate(0.150, 0)};
		finset.setPoints(finPoints);

		finset.setThickness(0.003);
		finset.setFinCount(4);
		
		finset.setCantAngle(0 * Math.PI / 180);
		//System.err.println("Fin cant angle: " + (finset.getCantAngle() * 180 / Math.PI));
		
		mcomp = new MassComponent(0.2, 0.03, 0.045 + 0.060);
		mcomp.setAxialMethod(AxialMethod.TOP);
		mcomp.setAxialOffset(0);
		
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
		coupler.setAxialMethod(AxialMethod.BOTTOM);
		coupler.setAxialOffset(-0.14);
		tube1.addChild(coupler);
		
		// Parachute
		MassComponent mass = new MassComponent(0.05, 0.05, 0.280);
		mass.setAxialMethod(AxialMethod.TOP);
		mass.setAxialOffset(0.2);
		tube1.addChild(mass);
		
		// Cord
		mass = new MassComponent(0.05, 0.05, 0.125);
		mass.setAxialMethod(AxialMethod.TOP);
		mass.setAxialOffset(0.2);
		tube1.addChild(mass);
		
		// Payload
		mass = new MassComponent(0.40, R, 1.500);
		mass.setAxialMethod(AxialMethod.TOP);
		mass.setAxialOffset(0.25);
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
		auxfinset.setAxialMethod(AxialMethod.TOP);
		auxfinset.setAxialOffset(0.28);
		auxfinset.setAngleOffset(Math.PI / 2);
		tube1.addChild(auxfinset);
		
		coupler = new TubeCoupler();
		coupler.setOuterRadiusAutomatic(true);
		coupler.setLength(0.28);
		coupler.setAxialMethod(AxialMethod.TOP);
		coupler.setAxialOffset(0.47);
		coupler.setMassOverridden(true);
		coupler.setOverrideMass(0.360);
		tube2.addChild(coupler);
		
		// Parachute
		mass = new MassComponent(0.1, 0.05, 0.028);
		mass.setAxialMethod(AxialMethod.TOP);
		mass.setAxialOffset(0.14);
		tube2.addChild(mass);
		
		Bulkhead bulk = new Bulkhead();
		bulk.setOuterRadiusAutomatic(true);
		bulk.setMassOverridden(true);
		bulk.setOverrideMass(0.050);
		bulk.setAxialMethod(AxialMethod.TOP);
		bulk.setAxialOffset(0.27);
		tube2.addChild(bulk);
		
		// Chord
		mass = new MassComponent(0.1, 0.05, 0.125);
		mass.setAxialMethod(AxialMethod.TOP);
		mass.setAxialOffset(0.19);
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
		center.setAxialMethod(AxialMethod.BOTTOM);
		center.setAxialOffset(0);
		tube3.addChild(center);
		
		center = new CenteringRing();
		center.setInnerRadiusAutomatic(true);
		center.setOuterRadiusAutomatic(true);
		center.setLength(0.005);
		center.setMassOverridden(true);
		center.setOverrideMass(0.038);
		center.setAxialMethod(AxialMethod.TOP);
		center.setAxialOffset(0.28);
		tube3.addChild(center);
		
		center = new CenteringRing();
		center.setInnerRadiusAutomatic(true);
		center.setOuterRadiusAutomatic(true);
		center.setLength(0.005);
		center.setMassOverridden(true);
		center.setOverrideMass(0.038);
		center.setAxialMethod(AxialMethod.TOP);
		center.setAxialOffset(0.83);
		tube3.addChild(center);
		
		finset = new TrapezoidFinSet();
		finset.setRootChord(0.495);
		finset.setTipChord(0.1);
		finset.setHeight(0.185);
		finset.setThickness(0.005);
		finset.setSweep(0.3);
		finset.setAxialMethod(AxialMethod.BOTTOM);
		finset.setAxialOffset(-0.03);
		finset.setAngleOffset(Math.PI / 2);
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
	
	public final static String FALCON_9H_FCID_1="test_config #1: [ M1350, G77]";
	public final static int FALCON_9H_PAYLOAD_STAGE_NUMBER=0;
	public final static int FALCON_9H_CORE_STAGE_NUMBER=1;
	public final static int FALCON_9H_BOOSTER_STAGE_NUMBER=2;
	
	
	
	// This function is used for unit, integration tests, DO NOT CHANGE (without updating tests).
	// Comments starting with cp: are center of pressure calculations
	// for components of the rocket.  Note that a cp: without a weight
	// has a weight of 0 (see for example body tubes) -- included for
	// completeness.
	// Instanced components (ie components on the side boosters) have
	// a cp: for each instance.
	// The unit tests change the number of fins on the side boosters;
	// cp: comments are shown for 1-fin, 2-fin, and 3-fin cases
	public static Rocket makeFalcon9Heavy() {
		Rocket rocket = new Rocket();
		rocket.setName("Falcon9H Scale Rocket");

		FlightConfigurationId selFCID = rocket.createFlightConfiguration( new FlightConfigurationId( FALCON_9H_FCID_1 ));
        rocket.setSelectedConfiguration(selFCID);

		// ====== Payload Stage ======
		// ====== ====== ====== ======
		AxialStage payloadStage = new AxialStage();
		payloadStage.setName("Payload Fairing Stage");
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
			// cp:(0.05900,0.00000,0.00000,w=2.00000)
			
			BodyTube payloadBody = new BodyTube(0.132, 0.052, 0.001);
			payloadBody.setName("PL Fairing Body");
			payloadStage.addChild(payloadBody);
			// cp:(0.18400,0.00000,0.00000)
			
			Transition payloadFairingTail = new Transition();
			payloadFairingTail.setName("PL Fairing Transition");
			payloadFairingTail.setLength(0.014);
			payloadFairingTail.setThickness(0.002);
			payloadFairingTail.setForeRadiusAutomatic(true);
			payloadFairingTail.setAftRadiusAutomatic(true);
			payloadStage.addChild(payloadFairingTail);
			// cp:(0.25665,0.00000,0.00000,w=-0.90366)
			
			BodyTube upperStageBody= new BodyTube(0.18, 0.0385, 0.001);
			upperStageBody.setName("Upper Stage Body");
			payloadStage.addChild( upperStageBody);
			// cp:(0.35400,0.00000,0.00000)
			
			{
				// Parachute
				Parachute upperChute= new Parachute();
				upperChute.setName("Parachute");
				upperChute.setAxialMethod(AxialMethod.MIDDLE);
				upperChute.setAxialOffset(0.0);
				upperChute.setDiameter(0.3);
				upperChute.setLineCount(6);
				upperChute.setLineLength(0.3);
				upperStageBody.addChild( upperChute);
				
				// Cord
				ShockCord cord = new ShockCord();
				cord.setName("Shock Cord");
				cord.setAxialMethod(AxialMethod.BOTTOM);
				cord.setAxialOffset(0.0);
				cord.setCordLength(0.4);
		    	upperStageBody.addChild( cord);
			}
			
			BodyTube interstage= new BodyTube(0.12, 0.0385, 0.001);
			interstage.setName("Interstage");
			payloadStage.addChild( interstage);
			// cp:(0.50400,0.00000,0.00000)
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
			// cp:(0.96400,0.00000,0.00000)
			{
				MotorConfiguration coreMotorConfig = new MotorConfiguration(coreBody, selFCID);
				Motor mtr = TestRockets.generateMotor_M1350_75mm();
				coreMotorConfig.setMotor( mtr);
				coreBody.setMotorMount( true);
				FlightConfigurationId motorConfigId = selFCID;
				coreBody.setMotorConfig( coreMotorConfig, motorConfigId);	 
			
				// ====== Booster Stage Set ======
				// ====== ====== ====== ======
				ParallelStage boosterStage = new ParallelStage();
				boosterStage.setName("Booster Stage");
				coreBody.addChild( boosterStage);
				boosterStage.setAxialMethod(AxialMethod.BOTTOM);
				boosterStage.setAxialOffset(0.0);
				boosterStage.setInstanceCount(2);
				boosterStage.setRadius( RadiusMethod.SURFACE, 0.0 );
				boosterStage.setAngleMethod( AngleMethod.RELATIVE );
				
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
					// cp:(0.52400,0.07700,0.00000,w=1.09634)
					// cp:(0.52400,-0.07700,0.00000,w=1.09634)
					
					BodyTube boosterBody = new BodyTube(0.8, 0.0385, 0.001);
					boosterBody.setName("Booster Body");
					boosterBody.setOuterRadiusAutomatic(true);
					boosterStage.addChild( boosterBody);
					// cp:(0.96400,0.07700,0.00000)
					// cp:(0.96400,-0.07700,0.00000)
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
						
						MotorConfiguration boosterMotorConfig = new MotorConfiguration( boosterMotorTubes, selFCID);
						Motor boosterMotor = TestRockets.generateMotor_G77_29mm();
						boosterMotorConfig.setMotor( boosterMotor );
						boosterMotorTubes.setMotorConfig( boosterMotorConfig, motorConfigId);
						boosterMotorTubes.setMotorOverhang(0.01234);
						
						TrapezoidFinSet boosterFins = new TrapezoidFinSet();
						boosterBody.addChild(boosterFins);
						boosterFins.setName("Booster Fins");
						boosterFins.setFinCount(3);
						boosterFins.setThickness(0.003);
						boosterFins.setCrossSection(CrossSection.ROUNDED);
						boosterFins.setRootChord(0.32);
						boosterFins.setTipChord(0.12);
						boosterFins.setHeight(0.10);
						boosterFins.setSweep(0.18);
						boosterFins.setAxialMethod(AxialMethod.BOTTOM);
						boosterFins.setAxialOffset(0.0);
					}
				}
				
			}
		}
		
		rocket.enableEvents();
		rocket.setSelectedConfiguration( selFCID);
		rocket.getFlightConfiguration( selFCID).setAllStages();
		
		return rocket;
	}

	// This is a simple four-fin rocket with large endplates on the
	// fins, for testing CG and CP calculations with fins on pods.
	// not a complete rocket (no motor mount nor recovery system)

	// it's parameterized to make things easy to change, but be sure
	// to adjust the unit test to match!
	public static Rocket makeEndPlateRocket() {
		
		// rocket design parameters
		double radius = 0.01; // note this is diameter/2!
		int finCount = 4;     // also determines pod count
		
		// nose cone
		double noseThick = 0.002;
		double noseLength = 0.05;
		
		// body tube
		double bodyWallThick = 0.002;
		double bodyLength = 0.254;

		// main body tube fins
		int bodyFinCount = 4;
		double bodyFinRootChord = 0.05;
		double bodyFinTipChord = bodyFinRootChord;
		double bodyFinHeight = 0.025;
		double bodyFinSweep = 0.0;
		AxialMethod bodyFinAxialMethod = AxialMethod.BOTTOM;
		double bodyFinAxialOffset = 0.0;
		double bodyFinThickness = 0.003;
				
		// pods for end plates
		int podSetCount = bodyFinCount;
		double podSetOffset = bodyFinHeight;
		AxialMethod podSetAxialMethod = bodyFinAxialMethod;
		double podSetAxialOffset = 0.0;

		// "phantom" tube on pods to give us somewhere to connect end
		// plates
		double phantomLength = bodyFinTipChord;
		double phantomRadius = 0;
		double phantomWallThickness = 0;

		// end plates
		int endPlateCount = 2;
		double endPlateRootChord = bodyFinTipChord;
		double endPlateTipChord = endPlateRootChord;
		double endPlateSweep = 0.0;
		double endPlateThickness = bodyFinThickness;
		double endPlateHeight = bodyFinHeight;
		double endPlateRotation = Math.PI/2.0;
		AxialMethod endPlateAxialMethod = AxialMethod.BOTTOM;
		double endPlateAxialOffset = 0;
	
		// create bare rocket
		Rocket rocket = new Rocket();
		rocket.enableEvents();
		rocket.setName("End Plate Test");

		// rocket has one stage
		AxialStage sustainer = new AxialStage();
		rocket.addChild(sustainer);
		sustainer.setName("Sustainer");
		
		// nose cone
		NoseCone noseCone = new NoseCone(Transition.Shape.OGIVE, noseLength, radius);
		sustainer.addChild(noseCone);
		noseCone.setName("Nose Cone");
		// cp:(0.02303,0.00000,0.00000,w=2.00000)

		// body tube
		BodyTube bodyTube = new BodyTube(bodyLength, radius, bodyWallThick);
		sustainer.addChild(bodyTube);
		bodyTube.setName("Body tube");
		// cp:(0.17700,0.00000,0.00000)
		
		// Trapezoidal fin set on body tube
		TrapezoidFinSet bodyFinSet = new TrapezoidFinSet(bodyFinCount, bodyFinRootChord, bodyFinTipChord, bodyFinSweep, bodyFinHeight);
		bodyTube.addChild(bodyFinSet);
		bodyFinSet.setName("Body Tube FinSet");
		bodyFinSet.setAxialMethod(bodyFinAxialMethod);
		bodyFinSet.setAxialOffset(bodyFinAxialOffset);
		bodyFinSet.setThickness(bodyFinThickness);
		// cp:(0.26650,0.00000,0.00000,w=15.24857)

		// Pod set to put an end plate on each fin
		PodSet podSet = new PodSet();
		bodyTube.addChild(podSet);
		podSet.setName("Pod Set");
		podSet.setInstanceCount(podSetCount);
		podSet.setRadiusOffset(podSetOffset);
		podSet.setAxialMethod(podSetAxialMethod);
		podSet.setAxialOffset(podSetAxialOffset);

		// 0-diameter "body tube" to give us something to hook the
		// endplates to.  Note that this causes a "thick fins"
		// warning.
		BodyTube phantom = new BodyTube(phantomLength, phantomRadius, phantomWallThickness);
		podSet.addChild(phantom);
		phantom.setName("Phantom");
		// cp:(0.25400,0.03540,0.00000)
		// cp:(0.25400,0.00000,0.03540)
		// cp:(0.25400,-0.03540,0.00000)
		// cp:(0.25400,-0.00000,-0.03540)

		// end plates
		TrapezoidFinSet endPlate = new TrapezoidFinSet(endPlateCount, endPlateRootChord, endPlateTipChord, endPlateSweep, endPlateHeight);
		phantom.addChild(endPlate);
		endPlate.setName("End plates");
		endPlate.setAngleOffset(endPlateRotation);
		endPlate.setAxialMethod(endPlateAxialMethod);
		endPlate.setAxialOffset(endPlateAxialOffset);
		endPlate.setThickness(endPlateThickness);
		// cp:(0.26650,0.03540,0.00000,w=0.00000)
		// cp:(0.26650,0.00000,0.03540,w=11.86000)
		// cp:(0.26650,-0.03540,0.00000,w=0.00000)
		// cp:(0.26650,-0.00000,-0.03540,w=11.86000)
		
		// Total cp:(0.25461,-0.00000,0.00000,w=40.96857)

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
		Rocket rocket = makeEstesAlphaIII();
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

	// Alpha III modified to put fins on "phantom" pods
	public static final Rocket makeEstesAlphaIIIWithPods() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();

		// find the body and fins
		final InstanceMap imap = rocket.getSelectedConfiguration().getActiveInstances();
	    for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: imap.entrySet() ) {		
			RocketComponent c = entry.getKey();
			if (c instanceof TrapezoidFinSet) {
				final TrapezoidFinSet fins = (TrapezoidFinSet) c;
				final BodyTube body = (BodyTube) fins.getParent();
				body.removeChild(fins);
				
				// create a PodSet to hook the fins to
				PodSet podset = new PodSet();
				podset.setInstanceCount(fins.getFinCount());
				
				body.addChild(podset);
				
				// put a phantom body tube on the pods
				BodyTube podBody = new BodyTube(fins.getRootChord(), 0);
				podBody.setName("Pod Body");
				podset.addChild(podBody);
				
				// change the number of fins to 1 and put the revised
				// finset on the podbody
				fins.setFinCount(1);
				podBody.addChild(fins);
			}
		}

		return rocket;
	}
	
	/**
	 * dump a test rocket to a file, so we can open it in OR
	 */
	static void dumpRocket(Rocket rocket, String filename) {

		OpenRocketDocument doc = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		OpenRocketSaver saver = new OpenRocketSaver();
		try {
			FileOutputStream str = new FileOutputStream(filename);
			saver.save(str, doc, null);
		}
		catch (Exception e) {
			System.err.println("exception " + e);
		}
	}
}
