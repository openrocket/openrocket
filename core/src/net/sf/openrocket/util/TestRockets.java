package net.sf.openrocket.util;

import java.util.Random;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.material.Material.Type;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.IllegalFinPointException;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.InternalComponent;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.MotorMount.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.ReferenceType;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.startup.Application;

public class TestRockets {
	
	private final String key;
	private final Random rnd;
	
	
	public TestRockets(String key) {
		
		if (key == null) {
			Random rnd = new Random();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 6; i++) {
				int n = rnd.nextInt(62);
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
		
		
		Stage stage = new Stage();
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
		body.setIgnitionDelay(rnd.nextDouble() * 3);
		body.setIgnitionEvent((IgnitionEvent) randomEnum(IgnitionEvent.class));
		body.setLength(rnd(0.3));
		body.setMotorMount(rnd.nextBoolean());
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
	
	
	
	
	
	public Rocket makeSmallFlyable() {
		double noseconeLength = 0.10, noseconeRadius = 0.01;
		double bodytubeLength = 0.20, bodytubeRadius = 0.01, bodytubeThickness = 0.001;
		
		int finCount = 3;
		double finRootChord = 0.04, finTipChord = 0.05, finSweep = 0.01, finThickness = 0.003, finHeight = 0.03;
		
		
		Rocket rocket;
		Stage stage;
		NoseCone nosecone;
		BodyTube bodytube;
		TrapezoidFinSet finset;
		
		rocket = new Rocket();
		stage = new Stage();
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
		
		String id = rocket.newMotorConfigurationID();
		bodytube.setMotorMount(true);
		
		Motor m = Application.getMotorSetDatabase().findMotors(null, null, "B4", Double.NaN, Double.NaN).get(0);
		bodytube.setMotor(id, m);
		bodytube.setMotorOverhang(0.005);
		rocket.getDefaultConfiguration().setMotorConfigurationID(id);
		
		rocket.getDefaultConfiguration().setAllStages();
		
		
		return rocket;
	}
	
	
	public static Rocket makeBigBlue() {
		Rocket rocket;
		Stage stage;
		NoseCone nosecone;
		BodyTube bodytube;
		FreeformFinSet finset;
		MassComponent mcomp;
		
		rocket = new Rocket();
		stage = new Stage();
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
		System.err.println("Fin cant angle: " + (finset.getCantAngle() * 180 / Math.PI));
		
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
		
		String id = rocket.newMotorConfigurationID();
		bodytube.setMotorMount(true);
		
		//		Motor m = Application.getMotorSetDatabase().findMotors(null, null, "F12J", Double.NaN, Double.NaN).get(0);
		//		bodytube.setMotor(id, m);
		//		bodytube.setMotorOverhang(0.005);
		rocket.getDefaultConfiguration().setMotorConfigurationID(id);
		
		rocket.getDefaultConfiguration().setAllStages();
		
		
		return rocket;
	}
	
	
	
	public static Rocket makeIsoHaisu() {
		Rocket rocket;
		Stage stage;
		NoseCone nosecone;
		BodyTube tube1, tube2, tube3;
		TrapezoidFinSet finset;
		TrapezoidFinSet auxfinset;
		MassComponent mcomp;
		
		final double R = 0.07;
		
		rocket = new Rocket();
		stage = new Stage();
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
		System.err.println("Fin cant angle: " + (finset.getCantAngle() * 180 / Math.PI));
		
		
		// Stage construction
		rocket.addChild(stage);
		rocket.setPerfectFinish(false);
		
		
		
		String id = rocket.newMotorConfigurationID();
		tube3.setMotorMount(true);
		
		//		Motor m = Application.getMotorSetDatabase().findMotors(null, null, "L540", Double.NaN, Double.NaN).get(0);
		//		tube3.setMotor(id, m);
		//		tube3.setMotorOverhang(0.02);
		rocket.getDefaultConfiguration().setMotorConfigurationID(id);
		
		//		tube3.setIgnitionEvent(MotorMount.IgnitionEvent.NEVER);
		
		rocket.getDefaultConfiguration().setAllStages();
		
		
		return rocket;
	}
	
	
	
}
