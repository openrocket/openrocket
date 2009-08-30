package net.sf.openrocket.util;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.IllegalFinPointException;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.rocketcomponent.FinSet.CrossSection;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;

public class Test {

	public static double noseconeLength=0.10,noseconeRadius=0.01;
	public static double bodytubeLength=0.20,bodytubeRadius=0.01,bodytubeThickness=0.001;
	
	public static int finCount=3;
	public static double finRootChord=0.04,finTipChord=0.05,finSweep=0.01,finThickness=0.003, finHeight=0.03;
	
	public static double materialDensity=1000;  // kg/m3
	

	public static Rocket makeRocket() {
		Rocket rocket;
		Stage stage,stage2;
		NoseCone nosecone;
		BodyTube bodytube, bt2;
		Transition transition;
		TrapezoidFinSet finset;
		
		rocket = new Rocket();
		stage = new Stage();
		stage.setName("Stage1");
		stage2 = new Stage();
		stage2.setName("Stage2");
		nosecone = new NoseCone(Transition.Shape.ELLIPSOID,noseconeLength,noseconeRadius);
		bodytube = new BodyTube(bodytubeLength,bodytubeRadius,bodytubeThickness);
		transition = new Transition();
		bt2 = new BodyTube(bodytubeLength,bodytubeRadius*2,bodytubeThickness);
		bt2.setMotorMount(true);

		finset = new TrapezoidFinSet(finCount,finRootChord,finTipChord,finSweep,finHeight);
		
		
		// Stage construction
		rocket.addChild(stage);
		rocket.addChild(stage2);

		
		// Component construction
		stage.addChild(nosecone);
		
		stage.addChild(bodytube);

		
		stage2.addChild(transition);
		
		stage2.addChild(bt2);
		
		bodytube.addChild(finset);
		
		
		rocket.getDefaultConfiguration().setAllStages();
		
		return rocket;
	}
	

	public static Rocket makeSmallFlyable() {
		Rocket rocket;
		Stage stage;
		NoseCone nosecone;
		BodyTube bodytube;
		TrapezoidFinSet finset;
		
		rocket = new Rocket();
		stage = new Stage();
		stage.setName("Stage1");

		nosecone = new NoseCone(Transition.Shape.ELLIPSOID,noseconeLength,noseconeRadius);
		bodytube = new BodyTube(bodytubeLength,bodytubeRadius,bodytubeThickness);

		finset = new TrapezoidFinSet(finCount,finRootChord,finTipChord,finSweep,finHeight);
		
		
		// Stage construction
		rocket.addChild(stage);

		
		// Component construction
		stage.addChild(nosecone);
		stage.addChild(bodytube);

		bodytube.addChild(finset);
		
		Material material = Prefs.getDefaultComponentMaterial(null, Material.Type.BULK);
		nosecone.setMaterial(material);
		bodytube.setMaterial(material);
		finset.setMaterial(material);
		
		String id = rocket.newMotorConfigurationID();
		bodytube.setMotorMount(true);
		
		for (Motor m: Databases.MOTOR) {
			if (m.getDesignation().equals("B4")) {
				bodytube.setMotor(id, m);
				break;
			}
		}
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

		nosecone = new NoseCone(Transition.Shape.ELLIPSOID,0.105,0.033);
		nosecone.setThickness(0.001);
		bodytube = new BodyTube(0.69,0.033,0.001);

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
		
		finset.setCantAngle(0*Math.PI/180);
		System.err.println("Fin cant angle: "+(finset.getCantAngle() * 180/Math.PI));
		
		mcomp = new MassComponent(0.2,0.03,0.045 + 0.060);
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
		
		for (Motor m: Databases.MOTOR) {
			if (m.getDesignation().equals("F12J")) {
				bodytube.setMotor(id, m);
				break;
			}
		}
		bodytube.setMotorOverhang(0.005);
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

		nosecone = new NoseCone(Transition.Shape.OGIVE,0.53,R);
		nosecone.setThickness(0.005);
		nosecone.setMassOverridden(true);
		nosecone.setOverrideMass(0.588);
		stage.addChild(nosecone);
		
		tube1 = new BodyTube(0.505,R,0.005);
		tube1.setMassOverridden(true);
		tube1.setOverrideMass(0.366);
		stage.addChild(tube1);
		
		tube2 = new BodyTube(0.605,R,0.005);
		tube2.setMassOverridden(true);
		tube2.setOverrideMass(0.427);
		stage.addChild(tube2);
		
		tube3 = new BodyTube(1.065,R,0.005);
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
		auxfinset.setBaseRotation(Math.PI/2);
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
		inner.setOuterRadius(0.08/2);
		inner.setInnerRadius(0.0762/2);
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
		finset.setBaseRotation(Math.PI/2);
		tube3.addChild(finset);
		
		
		finset.setCantAngle(0*Math.PI/180);
		System.err.println("Fin cant angle: "+(finset.getCantAngle() * 180/Math.PI));
		
		
		// Stage construction
		rocket.addChild(stage);
		rocket.setPerfectFinish(false);

		
		
		String id = rocket.newMotorConfigurationID();
		tube3.setMotorMount(true);
		
		for (Motor m: Databases.MOTOR) {
			if (m.getDesignation().equals("L540")) {
				tube3.setMotor(id, m);
				break;
			}
		}
		tube3.setMotorOverhang(0.02);
		rocket.getDefaultConfiguration().setMotorConfigurationID(id);

//		tube3.setIgnitionEvent(MotorMount.IgnitionEvent.NEVER);
		
		rocket.getDefaultConfiguration().setAllStages();
		
		
		return rocket;
	}
	
	
	
}
