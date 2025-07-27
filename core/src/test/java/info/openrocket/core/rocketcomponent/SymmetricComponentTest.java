package info.openrocket.core.rocketcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;

import info.openrocket.core.util.BaseTestCase;

import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;

public class SymmetricComponentTest extends BaseTestCase {

	@Test
	public void testPreviousSymmetricComponent() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		AxialStage payloadStage = rocket.getStage(0);
		NoseCone payloadFairingNoseCone = (NoseCone) payloadStage.getChild(0);
		BodyTube payloadBody = (BodyTube) payloadStage.getChild(1);
		Transition payloadFairingTail = (Transition) payloadStage.getChild(2);
		BodyTube upperStageBody = (BodyTube) payloadStage.getChild(3);
		BodyTube interstageBody = (BodyTube) payloadStage.getChild(4);

		assertNull(payloadFairingNoseCone.getPreviousSymmetricComponent());
		assertEquals(payloadFairingNoseCone, payloadBody.getPreviousSymmetricComponent());
		assertEquals(payloadBody, payloadFairingTail.getPreviousSymmetricComponent());
		assertEquals(payloadFairingTail, upperStageBody.getPreviousSymmetricComponent());
		assertEquals(upperStageBody, interstageBody.getPreviousSymmetricComponent());

		AxialStage coreStage = rocket.getStage(1);
		BodyTube coreBody = (BodyTube) coreStage.getChild(0);

		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());

		ParallelStage boosterStage = (ParallelStage) rocket.getStage(2);
		NoseCone boosterCone = (NoseCone) boosterStage.getChild(0);
		BodyTube boosterBody = (BodyTube) boosterStage.getChild(1);

		assertNull(boosterCone.getPreviousSymmetricComponent());
		assertEquals(boosterCone, boosterBody.getPreviousSymmetricComponent());
	}

	@Test
	public void testPreviousSymmetricComponentInlineComponentAssembly() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();

		// Stage 0
		AxialStage payloadStage = rocket.getStage(0);
		NoseCone payloadFairingNoseCone = (NoseCone) payloadStage.getChild(0);
		BodyTube payloadBody = (BodyTube) payloadStage.getChild(1);
		Transition payloadFairingTail = (Transition) payloadStage.getChild(2);
		BodyTube upperStageBody = (BodyTube) payloadStage.getChild(3);
		BodyTube interstageBody = (BodyTube) payloadStage.getChild(4);

		// Stage 1
		AxialStage coreStage = rocket.getStage(1);
		BodyTube coreBody = (BodyTube) coreStage.getChild(0);

		// Booster stage
		ParallelStage boosterStage = (ParallelStage) rocket.getStage(2);
		boosterStage.setInstanceCount(1);
		boosterStage.setRadius(RadiusMethod.RELATIVE, 0);
		NoseCone boosterCone = (NoseCone) boosterStage.getChild(0);
		BodyTube boosterBody = (BodyTube) boosterStage.getChild(1);

		// Add inline pod set
		PodSet podSet = new PodSet();
		podSet.setName("Inline Pod Set");
		podSet.setInstanceCount(1);
		podSet.setRadius(RadiusMethod.FREE, 0);
		coreBody.addChild(podSet);
		podSet.setAxialOffset(AxialMethod.BOTTOM, 0);
		NoseCone podSetCone = new NoseCone();
		podSetCone.setLength(0.1);
		podSetCone.setBaseRadius(0.05);
		podSet.addChild(podSetCone);
		BodyTube podSetBody = new BodyTube(0.2, 0.05, 0.001);
		podSetBody.setName("Pod Set Body");
		podSet.addChild(podSetBody);
		TrapezoidFinSet finSet = new TrapezoidFinSet();
		podSetBody.addChild(finSet);

		// Add last stage
		AxialStage lastStage = new AxialStage();
		BodyTube lastStageBody = new BodyTube(0.2, 0.05, 0.001);
		lastStageBody.setName("Last Stage Body");
		lastStage.addChild(lastStageBody);
		rocket.addChild(lastStage);

		assertNull(payloadFairingNoseCone.getPreviousSymmetricComponent());
		assertEquals(payloadFairingNoseCone, payloadBody.getPreviousSymmetricComponent());
		assertEquals(payloadBody, payloadFairingTail.getPreviousSymmetricComponent());
		assertEquals(payloadFairingTail, upperStageBody.getPreviousSymmetricComponent());
		assertEquals(upperStageBody, interstageBody.getPreviousSymmetricComponent());

		assertNull(boosterCone.getPreviousSymmetricComponent());
		assertEquals(boosterCone, boosterBody.getPreviousSymmetricComponent());

		// case 1: pod set is larger, and at the back of the core stage
		assertEquals(podSetBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());

		// case 2: pod set is smaller, and at the back of the core stage
		podSetBody.setOuterRadius(0.02);
		assertEquals(coreBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());

		// case 3: pod set is equal, and at the back of the core stage
		podSetBody.setOuterRadius(0.0385);
		assertEquals(coreBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());

		// case 4: pod set is larger, and at the front of the core stage
		podSetBody.setOuterRadius(0.05);
		podSet.setAxialOffset(AxialMethod.TOP, 0);
		assertEquals(coreBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());

		// case 5: pod set is smaller, and at the front of the core stage
		podSetBody.setOuterRadius(0.02);
		assertEquals(coreBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());

		// case 6: pod set is equal, and at the front of the core stage
		podSetBody.setOuterRadius(0.0385);
		assertEquals(coreBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());

		// case 7: pod set is same length as core stage, and larger, and at the front of
		// the core stage
		podSetBody.setOuterRadius(0.05);
		podSetBody.setLength(0.7);
		assertEquals(podSetBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());

		// case 8: pod set is same length as core stage, and smaller, and at the front
		// of the core stage
		podSetBody.setOuterRadius(0.02);
		assertEquals(coreBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());

		// case 9: pod set is in larger, and in the middle of the core stage
		podSetBody.setLength(0.2);
		podSetBody.setOuterRadius(0.05);
		podSet.setAxialOffset(AxialMethod.MIDDLE, 0);
		assertEquals(coreBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());

		// case 10: pod set is in larger, and behind the back of the core stage
		podSet.setAxialOffset(AxialMethod.BOTTOM, 1);
		assertEquals(coreBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());

		// Add a booster inside the pod set
		ParallelStage insideBooster = new ParallelStage();
		insideBooster.setName("Inside Booster");
		insideBooster.setInstanceCount(1);
		insideBooster.setRadius(RadiusMethod.FREE, 0);
		podSetBody.addChild(insideBooster);
		insideBooster.setAxialOffset(AxialMethod.BOTTOM, 0);
		BodyTube insideBoosterBody = new BodyTube(0.2, 0.06, 0.001);
		insideBoosterBody.setName("Inside Booster Body");
		insideBooster.addChild(insideBoosterBody);

		// Case 1: inside booster is larger than pod set and flush to its end (both are
		// at the back of the core stage)
		podSet.setAxialOffset(AxialMethod.BOTTOM, 0);
		insideBoosterBody.setOuterRadius(0.06);
		assertEquals(insideBoosterBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());
		assertEquals(podSetCone, insideBoosterBody.getPreviousSymmetricComponent());

		// Case 2: inside booster is smaller than pod set and flush to its end
		insideBoosterBody.setOuterRadius(0.04);
		assertEquals(podSetBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());
		assertEquals(podSetCone, insideBoosterBody.getPreviousSymmetricComponent());

		// Case 3: inside booster is equal the pod set and flush to its end
		insideBoosterBody.setOuterRadius(0.05);
		assertEquals(podSetBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());
		assertEquals(podSetCone, insideBoosterBody.getPreviousSymmetricComponent());

		// Case 4: inside booster is larger than pod set and before the back (pod set at
		// the back of the core stage)
		insideBooster.setAxialOffset(AxialMethod.BOTTOM, -1);
		insideBoosterBody.setOuterRadius(0.06);
		assertEquals(podSetBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());
		assertEquals(podSetCone, insideBoosterBody.getPreviousSymmetricComponent());

		// Case 5: inside booster is larger than pod set and after the back (pod set at
		// the back of the core stage)
		insideBooster.setAxialOffset(AxialMethod.BOTTOM, 1);
		assertEquals(podSetBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());
		assertEquals(podSetBody, insideBoosterBody.getPreviousSymmetricComponent());

		// Case 6: inside booster is larger than pod set, pod set is before the back of
		// the core stage, inside booster is an equal amount after the back of the pod
		// set
		podSet.setAxialOffset(AxialMethod.BOTTOM, -1.5);
		insideBooster.setAxialOffset(AxialMethod.BOTTOM, 1.5);
		assertEquals(insideBoosterBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());
		assertEquals(podSetBody, insideBoosterBody.getPreviousSymmetricComponent());

		// Case 7: inside booster is larger than pod set, pod set is after the back of
		// the core stage, inside booster is an equal amount before the back of the pod
		// set
		podSet.setAxialOffset(AxialMethod.BOTTOM, 1.5);
		insideBooster.setAxialOffset(AxialMethod.BOTTOM, -1.5);
		assertEquals(insideBoosterBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());
		assertEquals(podSetCone, insideBoosterBody.getPreviousSymmetricComponent());

		// Case 8: inside booster is larger than pod set, pod set is before the back of
		// the core stage, inside booster is flush with the back of the pod set
		podSet.setAxialOffset(AxialMethod.BOTTOM, -1.5);
		insideBooster.setAxialOffset(AxialMethod.BOTTOM, 0);
		assertEquals(coreBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());
		assertEquals(podSetCone, insideBoosterBody.getPreviousSymmetricComponent());

		// Case 9: inside booster is larger than pod set, pod set is after the back of
		// the core stage, inside booster is flush with the back of the pod set
		podSet.setAxialOffset(AxialMethod.BOTTOM, 1.5);
		insideBooster.setAxialOffset(AxialMethod.BOTTOM, 0);
		assertEquals(coreBody, lastStageBody.getPreviousSymmetricComponent());
		assertEquals(coreBody, podSetCone.getPreviousSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getPreviousSymmetricComponent());
		assertEquals(interstageBody, coreBody.getPreviousSymmetricComponent());
		assertEquals(podSetCone, insideBoosterBody.getPreviousSymmetricComponent());
	}

	@Test
	public void testNextSymmetricComponent() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		AxialStage payloadStage = rocket.getStage(0);
		NoseCone payloadFairingNoseCone = (NoseCone) payloadStage.getChild(0);
		BodyTube payloadBody = (BodyTube) payloadStage.getChild(1);
		Transition payloadFairingTail = (Transition) payloadStage.getChild(2);
		BodyTube upperStageBody = (BodyTube) payloadStage.getChild(3);
		BodyTube interstageBody = (BodyTube) payloadStage.getChild(4);

		assertEquals(payloadBody, payloadFairingNoseCone.getNextSymmetricComponent());
		assertEquals(payloadFairingTail, payloadBody.getNextSymmetricComponent());
		assertEquals(upperStageBody, payloadFairingTail.getNextSymmetricComponent());
		assertEquals(interstageBody, upperStageBody.getNextSymmetricComponent());

		AxialStage coreStage = rocket.getStage(1);
		BodyTube coreBody = (BodyTube) coreStage.getChild(0);

		assertEquals(coreBody, interstageBody.getNextSymmetricComponent());
		assertNull(coreBody.getNextSymmetricComponent());

		ParallelStage boosterStage = (ParallelStage) rocket.getStage(2);
		NoseCone boosterCone = (NoseCone) boosterStage.getChild(0);
		BodyTube boosterBody = (BodyTube) boosterStage.getChild(1);

		assertEquals(boosterBody, boosterCone.getNextSymmetricComponent());
		assertNull(boosterBody.getNextSymmetricComponent());
	}

	@Test
	public void testNextSymmetricComponentInlineComponentAssembly() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();

		// Stage 0
		AxialStage payloadStage = rocket.getStage(0);
		NoseCone payloadFairingNoseCone = (NoseCone) payloadStage.getChild(0);
		BodyTube payloadBody = (BodyTube) payloadStage.getChild(1);
		Transition payloadFairingTail = (Transition) payloadStage.getChild(2);
		BodyTube upperStageBody = (BodyTube) payloadStage.getChild(3);
		BodyTube interstageBody = (BodyTube) payloadStage.getChild(4);

		// Stage 1
		AxialStage coreStage = rocket.getStage(1);
		BodyTube coreBody = (BodyTube) coreStage.getChild(0);

		// Booster stage
		ParallelStage boosterStage = (ParallelStage) rocket.getStage(2);
		boosterStage.setInstanceCount(1);
		boosterStage.setRadius(RadiusMethod.RELATIVE, 0);
		NoseCone boosterCone = (NoseCone) boosterStage.getChild(0);
		BodyTube boosterBody = (BodyTube) boosterStage.getChild(1);

		// Add inline pod set
		PodSet podSet = new PodSet();
		podSet.setName("Inline Pod Set");
		podSet.setInstanceCount(1);
		podSet.setRadius(RadiusMethod.FREE, 0);
		coreBody.addChild(podSet);
		podSet.setAxialOffset(AxialMethod.TOP, 0);
		BodyTube podSetBody = new BodyTube(0.2, 0.05, 0.001);
		podSetBody.setName("Pod Set Body");
		podSet.addChild(podSetBody);
		TrapezoidFinSet finSet = new TrapezoidFinSet();
		podSetBody.addChild(finSet);
		NoseCone podSetCone = new NoseCone();
		podSetCone.setLength(0.1);
		podSetCone.setBaseRadius(0.05);
		podSetCone.setFlipped(true);
		podSet.addChild(podSetCone);

		// Add last stage
		AxialStage lastStage = new AxialStage();
		BodyTube lastStageBody = new BodyTube(0.2, 0.05, 0.001);
		lastStageBody.setName("Last Stage Body");
		lastStage.addChild(lastStageBody);
		rocket.addChild(lastStage);

		assertEquals(payloadBody, payloadFairingNoseCone.getNextSymmetricComponent());
		assertEquals(payloadFairingTail, payloadBody.getNextSymmetricComponent());
		assertEquals(upperStageBody, payloadFairingTail.getNextSymmetricComponent());
		assertEquals(interstageBody, upperStageBody.getNextSymmetricComponent());

		assertNull(lastStageBody.getNextSymmetricComponent());
		assertEquals(boosterBody, boosterCone.getNextSymmetricComponent());

		// case 1: pod set is larger, and at the front of the core stage
		assertEquals(podSetBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());

		// case 2: pod set is smaller, and at the front of the core stage
		podSetBody.setOuterRadius(0.02);
		assertEquals(coreBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());

		// case 3: pod set is equal, and at the front of the core stage
		podSetBody.setOuterRadius(0.0385);
		assertEquals(coreBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());

		// case 4: pod set is larger, and at the back of the core stage
		podSetBody.setOuterRadius(0.05);
		podSet.setAxialOffset(AxialMethod.BOTTOM, 0);
		assertEquals(coreBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());

		// case 5: pod set is smaller, and at the back of the core stage
		podSetBody.setOuterRadius(0.02);
		assertEquals(coreBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());

		// case 6: pod set is equal, and at the back of the core stage
		podSetBody.setOuterRadius(0.0385);
		assertEquals(coreBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());

		// case 7: pod set is same length as core stage, and larger, and at the back of
		// the core stage
		podSetBody.setOuterRadius(0.05);
		podSetBody.setLength(0.7);
		assertEquals(podSetBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());

		// case 8: pod set is same length as core stage, and smaller, and at the back of
		// the core stage
		podSetBody.setOuterRadius(0.02);
		assertEquals(coreBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());

		// case 9: pod set is in larger, and in the middle of the core stage
		podSetBody.setLength(0.2);
		podSetBody.setOuterRadius(0.05);
		podSet.setAxialOffset(AxialMethod.MIDDLE, 0);
		assertEquals(coreBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());

		// case 10: pod set is in larger, and behind the front of the core stage
		podSet.setAxialOffset(AxialMethod.TOP, 1);
		assertEquals(coreBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());

		// Add a booster inside the pod set
		ParallelStage insideBooster = new ParallelStage();
		insideBooster.setName("Inside Booster");
		insideBooster.setInstanceCount(1);
		insideBooster.setRadius(RadiusMethod.FREE, 0);
		podSetBody.addChild(insideBooster);
		insideBooster.setAxialOffset(AxialMethod.TOP, 0);
		BodyTube insideBoosterBody = new BodyTube(0.2, 0.06, 0.001);
		insideBoosterBody.setName("Inside Booster Body");
		insideBooster.addChild(insideBoosterBody);

		// Case 1: inside booster is larger than pod set and flush to its front (both
		// are at the front of the core stage)
		podSet.setAxialOffset(AxialMethod.TOP, 0);
		insideBoosterBody.setOuterRadius(0.06);
		assertEquals(insideBoosterBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());
		assertNull(insideBoosterBody.getNextSymmetricComponent());

		// Case 2: inside booster is smaller than pod set and flush to its front
		insideBoosterBody.setOuterRadius(0.04);
		assertEquals(podSetBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());
		assertNull(insideBoosterBody.getNextSymmetricComponent());

		// Case 3: inside booster is equal the pod set and flush to its front
		insideBoosterBody.setOuterRadius(0.05);
		assertEquals(podSetBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());
		assertNull(insideBoosterBody.getNextSymmetricComponent());

		// Case 4: inside booster is larger than pod set and before the front (pod set
		// at the front of the core stage)
		insideBooster.setAxialOffset(AxialMethod.TOP, -1);
		insideBoosterBody.setOuterRadius(0.06);
		assertEquals(podSetBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());
		assertNull(insideBoosterBody.getNextSymmetricComponent());

		// Case 5: inside booster is larger than pod set and after the front (pod set at
		// the front of the core stage)
		insideBooster.setAxialOffset(AxialMethod.TOP, 1);
		assertEquals(podSetBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());
		assertNull(insideBoosterBody.getNextSymmetricComponent());

		// Case 6: inside booster is larger than pod set, pod set is before the front of
		// the core stage, inside booster is an equal amount after the front of the pod
		// set
		podSet.setAxialOffset(AxialMethod.TOP, -1.5);
		insideBooster.setAxialOffset(AxialMethod.TOP, 1.5);
		assertEquals(insideBoosterBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());
		assertNull(insideBoosterBody.getNextSymmetricComponent());

		// Case 7: inside booster is larger than pod set, pod set is after the front of
		// the core stage, inside booster is an equal amount before the front of the pod
		// set
		podSet.setAxialOffset(AxialMethod.TOP, 1.5);
		insideBooster.setAxialOffset(AxialMethod.TOP, -1.5);
		assertEquals(insideBoosterBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());
		assertNull(insideBoosterBody.getNextSymmetricComponent());

		// Case 8: inside booster is larger than pod set, pod set is before the front of
		// the core stage, inside booster is flush with the front of the pod set
		podSet.setAxialOffset(AxialMethod.TOP, -1.5);
		insideBooster.setAxialOffset(AxialMethod.TOP, 0);
		assertEquals(coreBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());
		assertNull(insideBoosterBody.getNextSymmetricComponent());

		// Case 9: inside booster is larger than pod set, pod set is after the front of
		// the core stage, inside booster is flush with the front of the pod set
		podSet.setAxialOffset(AxialMethod.TOP, 1.5);
		insideBooster.setAxialOffset(AxialMethod.TOP, 0);
		assertEquals(coreBody, interstageBody.getNextSymmetricComponent());
		assertEquals(podSetCone, podSetBody.getNextSymmetricComponent());
		assertNull(podSetCone.getNextSymmetricComponent());
		assertEquals(lastStageBody, coreBody.getNextSymmetricComponent());
		assertNull(insideBoosterBody.getNextSymmetricComponent());
	}
}
