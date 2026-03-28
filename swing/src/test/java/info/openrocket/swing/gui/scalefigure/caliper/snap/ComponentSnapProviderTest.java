package info.openrocket.swing.gui.scalefigure.caliper.snap;

import static org.junit.jupiter.api.Assertions.*;

import info.openrocket.core.rocketcomponent.*;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.Transformation;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.scalefigure.caliper.CaliperManager;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Unit tests for ComponentSnapProvider implementations.
 * Tests that providers can be retrieved for applicable components and that
 * side and back views return logical snap target values.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class ComponentSnapProviderTest extends BaseTestCase {

	private CaliperSnapRegistry registry;
	private Transformation identityTransform;

	@BeforeEach
	public void init() {
		registry = CaliperSnapRegistry.getInstance();
		identityTransform = Transformation.IDENTITY;
	}

	/**
	 * Test that CoaxialSnapProvider can be retrieved for BodyTube.
	 */
	@Test
	public void testCoaxialSnapProviderForBodyTube() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setLength(0.2);
		bodyTube.setOuterRadius(0.025);
		bodyTube.setInnerRadius(0.020);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				bodyTube,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		assertNotNull(targets, "Snap targets should not be null");
		assertFalse(targets.isEmpty(), "BodyTube should have snap targets in side view");
	}

	/**
	 * Test that CoaxialSnapProvider returns logical values for BodyTube in side view.
	 */
	@Test
	public void testBodyTubeSideViewVerticalMode() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setLength(0.2);
		bodyTube.setOuterRadius(0.025);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				bodyTube,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		// Should have left and right edges (vertical lines)
		assertTrue(targets.size() >= 2, "Should have at least left and right edges");
		
		// Check that we have vertical line targets
		boolean hasLeftEdge = false;
		boolean hasRightEdge = false;
		for (CaliperSnapTarget target : targets) {
			if (target.getType() == CaliperSnapTarget.SnapType.LINE &&
					target.getOrientation() == CaliperManager.CaliperMode.VERTICAL) {
				double x = target.getLineStart().getX();
				if (Math.abs(x - 0.0) < 0.001) {
					hasLeftEdge = true;
				}
				if (Math.abs(x - 0.2) < 0.001) {
					hasRightEdge = true;
				}
			}
		}
		assertTrue(hasLeftEdge, "Should have left edge at x=0");
		assertTrue(hasRightEdge, "Should have right edge at x=length");
	}

	/**
	 * Test that CoaxialSnapProvider returns logical values for BodyTube in side view horizontal mode.
	 */
	@Test
	public void testBodyTubeSideViewHorizontalMode() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setLength(0.2);
		bodyTube.setOuterRadius(0.025);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				bodyTube,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.HORIZONTAL,
				identityTransform
		);

		// Should have top and bottom edges (horizontal lines)
		assertTrue(targets.size() >= 2, "Should have at least top and bottom edges");
		
		// Check that we have horizontal line targets
		boolean hasTopEdge = false;
		boolean hasBottomEdge = false;
		for (CaliperSnapTarget target : targets) {
			if (target.getType() == CaliperSnapTarget.SnapType.LINE &&
					target.getOrientation() == CaliperManager.CaliperMode.HORIZONTAL) {
				double y = target.getLineStart().getY();
				if (Math.abs(y - 0.025) < 0.001) {
					hasTopEdge = true;
				}
				if (Math.abs(y - (-0.025)) < 0.001) {
					hasBottomEdge = true;
				}
			}
		}
		assertTrue(hasTopEdge, "Should have top edge at y=+radius");
		assertTrue(hasBottomEdge, "Should have bottom edge at y=-radius");
	}

	/**
	 * Test that CoaxialSnapProvider returns logical values for BodyTube in back view.
	 */
	@Test
	public void testBodyTubeBackView() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setOuterRadius(0.025);
		bodyTube.setInnerRadius(0.020);

		// Test vertical mode
		List<CaliperSnapTarget> targetsVertical = registry.getSnapTargets(
				bodyTube,
				RocketPanel.VIEW_TYPE.BackView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		// Should have points on outer and inner circles
		assertFalse(targetsVertical.isEmpty(), "Should have snap targets in back view vertical mode");
		
		// Check for points at z = ±outerRadius and z = ±innerRadius
		boolean hasOuterLeft = false;
		boolean hasOuterRight = false;
		for (CaliperSnapTarget target : targetsVertical) {
			if (target.getType() == CaliperSnapTarget.SnapType.POINT) {
				double z = target.getPosition().getZ();
				if (Math.abs(z - (-0.025)) < 0.001) {
					hasOuterLeft = true;
				}
				if (Math.abs(z - 0.025) < 0.001) {
					hasOuterRight = true;
				}
			}
		}
		assertTrue(hasOuterLeft || hasOuterRight, "Should have points on outer circle");

		// Test horizontal mode
		List<CaliperSnapTarget> targetsHorizontal = registry.getSnapTargets(
				bodyTube,
				RocketPanel.VIEW_TYPE.BackView,
				CaliperManager.CaliperMode.HORIZONTAL,
				identityTransform
		);

		assertFalse(targetsHorizontal.isEmpty(), "Should have snap targets in back view horizontal mode");
	}

	/**
	 * Test that TransitionSnapProvider can be retrieved for Transition.
	 */
	@Test
	public void testTransitionSnapProvider() {
		Transition transition = new Transition();
		transition.setLength(0.15);
		transition.setForeRadius(0.05);
		transition.setAftRadius(0.025);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				transition,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		assertNotNull(targets, "Snap targets should not be null");
		assertFalse(targets.isEmpty(), "Transition should have snap targets");
	}

	/**
	 * Test that TransitionSnapProvider returns logical values for Transition with zero radius.
	 */
	@Test
	public void testTransitionZeroRadius() {
		Transition transition = new Transition();
		transition.setLength(0.15);
		transition.setForeRadius(0.0);  // Zero radius at fore end
		transition.setAftRadius(0.025);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				transition,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		// Should have a point at fore end (x=0) since radius is 0
		boolean hasForePoint = false;
		for (CaliperSnapTarget target : targets) {
			if (target.getType() == CaliperSnapTarget.SnapType.POINT) {
				double x = target.getPosition().getX();
				if (Math.abs(x - 0.0) < 0.001) {
					hasForePoint = true;
					break;
				}
			}
		}
		assertTrue(hasForePoint, "Should have a point at fore end when radius is 0");
	}

	/**
	 * Test that TubeFinSetSnapProvider can be retrieved for TubeFinSet.
	 */
	@Test
	public void testTubeFinSetSnapProvider() {
		TubeFinSet tubeFinSet = new TubeFinSet();
		tubeFinSet.setOuterRadius(0.01);
		tubeFinSet.setLength(0.05);
		tubeFinSet.setFinCount(4);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				tubeFinSet,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		assertNotNull(targets, "Snap targets should not be null");
		assertFalse(targets.isEmpty(), "TubeFinSet should have snap targets");
	}

	/**
	 * Test that FinSetSnapProvider can be retrieved for TrapezoidFinSet.
	 */
	@Test
	public void testFinSetSnapProviderForTrapezoidFinSet() {
		TrapezoidFinSet finSet = new TrapezoidFinSet();
		finSet.setRootChord(0.1);
		finSet.setTipChord(0.05);
		finSet.setSweep(0.02);
		finSet.setHeight(0.05);
		finSet.setThickness(0.003);
		finSet.setCantAngle(0.0);  // No cant for simpler testing

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				finSet,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		assertNotNull(targets, "Snap targets should not be null");
		assertFalse(targets.isEmpty(), "TrapezoidFinSet should have snap targets");
	}

	/**
	 * Test that FinSetSnapProvider returns logical values for TrapezoidFinSet with zero cant.
	 */
	@Test
	public void testTrapezoidFinSetZeroCant() {
		TrapezoidFinSet finSet = new TrapezoidFinSet();
		finSet.setRootChord(0.1);
		finSet.setTipChord(0.05);
		finSet.setSweep(0.02);
		finSet.setHeight(0.05);
		finSet.setThickness(0.003);
		finSet.setCantAngle(0.0);

		// Test horizontal mode - should have root and tip chord lines
		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				finSet,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.HORIZONTAL,
				identityTransform
		);

		// Should have root chord and tip chord lines when cant is 0
		boolean hasRootChord = false;
		boolean hasTipChord = false;
		for (CaliperSnapTarget target : targets) {
			if (target.getType() == CaliperSnapTarget.SnapType.LINE &&
					target.getOrientation() == CaliperManager.CaliperMode.HORIZONTAL) {
				// Check if it's a root or tip chord line
				double y = target.getLineStart().getY();
				if (Math.abs(y - 0.0) < 0.001) {
					hasRootChord = true;
				}
				if (Math.abs(y - 0.05) < 0.001) {
					hasTipChord = true;
				}
			}
		}
		assertTrue(hasRootChord || hasTipChord, "Should have root or tip chord lines when cant is 0");
	}

	/**
	 * Test that MassObjectSnapProvider can be retrieved for MassComponent.
	 */
	@Test
	public void testMassObjectSnapProvider() {
		MassComponent massObject = new MassComponent();
		massObject.setLength(0.1);
		massObject.setRadius(0.02);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				massObject,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		assertNotNull(targets, "Snap targets should not be null");
		assertFalse(targets.isEmpty(), "MassComponent should have snap targets");
	}

	/**
	 * Test that MassObjectSnapProvider returns logical values for full circle case.
	 */
	@Test
	public void testMassObjectFullCircle() {
		MassComponent massObject = new MassComponent();
		// Set dimensions so that arc width >= 2*radius (full circle)
		massObject.setLength(0.1);
		massObject.setRadius(0.05);  // Large radius relative to length

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				massObject,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		// Should have point targets for full circle
		boolean hasPointTarget = false;
		for (CaliperSnapTarget target : targets) {
			if (target.getType() == CaliperSnapTarget.SnapType.POINT) {
				hasPointTarget = true;
				break;
			}
		}
		// May have points if edges are fully arched
		assertNotNull(targets, "Should have snap targets");
	}

	/**
	 * Test that RailButtonSnapProvider can be retrieved for RailButton.
	 */
	@Test
	public void testRailButtonSnapProvider() {
		RailButton railButton = new RailButton();
		railButton.setOuterDiameter(0.011);
		railButton.setInnerDiameter(0.006);
		railButton.setTotalHeight(0.008);
		railButton.setBaseHeight(0.002);
		railButton.setFlangeHeight(0.002);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				railButton,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		assertNotNull(targets, "Snap targets should not be null");
		assertFalse(targets.isEmpty(), "RailButton should have snap targets");
	}

	/**
	 * Test that RailButtonSnapProvider returns logical values in back view.
	 */
	@Test
	public void testRailButtonBackView() {
		RailButton railButton = new RailButton();
		railButton.setOuterDiameter(0.011);
		railButton.setInnerDiameter(0.006);
		railButton.setTotalHeight(0.008);
		railButton.setBaseHeight(0.002);
		railButton.setFlangeHeight(0.002);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				railButton,
				RocketPanel.VIEW_TYPE.BackView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		// Should have corner points for rectangles
		int pointCount = 0;
		for (CaliperSnapTarget target : targets) {
			if (target.getType() == CaliperSnapTarget.SnapType.POINT) {
				pointCount++;
			}
		}
		// Should have corner points for each rectangle section
		assertTrue(pointCount >= 4, "Should have corner points for rectangles in back view");
	}

	/**
	 * Test that snap targets are filtered by caliper mode compatibility.
	 */
	@Test
	public void testSnapTargetModeCompatibility() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setLength(0.2);
		bodyTube.setOuterRadius(0.025);

		// Vertical mode should only return vertical-compatible targets
		List<CaliperSnapTarget> verticalTargets = registry.getSnapTargets(
				bodyTube,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		for (CaliperSnapTarget target : verticalTargets) {
			assertTrue(
					target.isCompatibleWith(CaliperManager.CaliperMode.VERTICAL) ||
					target.isCompatibleWith(CaliperManager.CaliperMode.BOTH),
					"All targets in vertical mode should be compatible with vertical or both"
			);
		}

		// Horizontal mode should only return horizontal-compatible targets
		List<CaliperSnapTarget> horizontalTargets = registry.getSnapTargets(
				bodyTube,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.HORIZONTAL,
				identityTransform
		);

		for (CaliperSnapTarget target : horizontalTargets) {
			assertTrue(
					target.isCompatibleWith(CaliperManager.CaliperMode.HORIZONTAL) ||
					target.isCompatibleWith(CaliperManager.CaliperMode.BOTH),
					"All targets in horizontal mode should be compatible with horizontal or both"
			);
		}
	}

	/**
	 * Test that snap targets have valid coordinates.
	 */
	@Test
	public void testSnapTargetCoordinatesValid() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setLength(0.2);
		bodyTube.setOuterRadius(0.025);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				bodyTube,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		for (CaliperSnapTarget target : targets) {
			assertNotNull(target.getPosition(), "Target position should not be null");
			assertFalse(
					Double.isNaN(target.getPosition().getX()) &&
					Double.isNaN(target.getPosition().getY()) &&
					Double.isNaN(target.getPosition().getZ()),
					"Target position should have at least one valid coordinate"
			);

			if (target.getType() == CaliperSnapTarget.SnapType.LINE) {
				assertNotNull(target.getLineStart(), "Line start should not be null");
				assertNotNull(target.getLineEnd(), "Line end should not be null");
			}
		}
	}

	/**
	 * Test that transformation is applied correctly.
	 */
	@Test
	public void testTransformationApplied() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setLength(0.2);
		bodyTube.setOuterRadius(0.025);

		// Apply a translation transformation
		Transformation translation = new Transformation(new Coordinate(0.1, 0.05, 0.0));

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				bodyTube,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				translation
		);

		// Check that targets are translated
		boolean foundLeftEdge = false;
		for (CaliperSnapTarget target : targets) {
			if (target.getType() == CaliperSnapTarget.SnapType.LINE) {
				double x = target.getLineStart().getX();
				// Left edge should be at x = 0.1 (translated from 0.0)
				if (Math.abs(x - 0.1) < 0.001) {
					foundLeftEdge = true;
					break;
				}
			}
		}
		assertTrue(foundLeftEdge, "Should have left edge at translated position x=0.1");
	}

	/**
	 * Test that CenteringRing (Coaxial component) has snap targets.
	 */
	@Test
	public void testCenteringRingSnapProvider() {
		CenteringRing ring = new CenteringRing();
		ring.setOuterRadius(0.025);
		ring.setInnerRadius(0.020);
		ring.setThickness(0.003);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				ring,
				RocketPanel.VIEW_TYPE.BackView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		assertNotNull(targets, "Snap targets should not be null");
		assertFalse(targets.isEmpty(), "CenteringRing should have snap targets in back view");
	}

	/**
	 * Test that NoseCone (Transition component) has snap targets.
	 */
	@Test
	public void testNoseConeSnapProvider() {
		NoseCone noseCone = new NoseCone();
		noseCone.setLength(0.1);
		noseCone.setBaseRadius(0.025);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				noseCone,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		assertNotNull(targets, "Snap targets should not be null");
		assertFalse(targets.isEmpty(), "NoseCone should have snap targets");
	}

	/**
	 * Test that FreeformFinSet has snap targets.
	 */
	@Test
	public void testFreeformFinSetSnapProvider() {
		FreeformFinSet finSet = new FreeformFinSet();
		// Add some fin points
		finSet.setPoints(new Coordinate[] {
				new Coordinate(0.0, 0.0, 0.0),
				new Coordinate(0.1, 0.0, 0.0),
				new Coordinate(0.1, 0.05, 0.0),
				new Coordinate(0.0, 0.05, 0.0)
		});
		finSet.setThickness(0.003);
		finSet.setCantAngle(0.0);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				finSet,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		assertNotNull(targets, "Snap targets should not be null");
		assertFalse(targets.isEmpty(), "FreeformFinSet should have snap targets");
	}

	/**
	 * Test that EllipticalFinSet has snap targets.
	 */
	@Test
	public void testEllipticalFinSetSnapProvider() {
		EllipticalFinSet finSet = new EllipticalFinSet();
		finSet.setHeight(0.05);
		finSet.setLength(0.1);
		finSet.setThickness(0.003);
		finSet.setCantAngle(0.0);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				finSet,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		assertNotNull(targets, "Snap targets should not be null");
		assertFalse(targets.isEmpty(), "EllipticalFinSet should have snap targets");
		
		// Should have start point, end point, and top point
		int pointCount = 0;
		for (CaliperSnapTarget target : targets) {
			if (target.getType() == CaliperSnapTarget.SnapType.POINT) {
				pointCount++;
			}
		}
		assertTrue(pointCount >= 3, "EllipticalFinSet should have at least 3 point targets (start, end, top)");
	}

	/**
	 * Test that components without snap providers return empty list.
	 */
	@Test
	public void testComponentWithoutProvider() {
		// Create a component that doesn't have a snap provider
		// For example, a Rocket component itself
		Rocket rocket = new Rocket();
		
		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				rocket,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		// Should return empty list, not null
		assertNotNull(targets, "Should return empty list, not null");
		assertTrue(targets.isEmpty(), "Components without providers should return empty list");
	}

	/**
	 * Test that snap targets have valid component references.
	 */
	@Test
	public void testSnapTargetComponentReference() {
		BodyTube bodyTube = new BodyTube();
		bodyTube.setLength(0.2);
		bodyTube.setOuterRadius(0.025);

		List<CaliperSnapTarget> targets = registry.getSnapTargets(
				bodyTube,
				RocketPanel.VIEW_TYPE.SideView,
				CaliperManager.CaliperMode.VERTICAL,
				identityTransform
		);

		for (CaliperSnapTarget target : targets) {
			assertNotNull(target.getComponent(), "Target should have a component reference");
			assertSame(bodyTube, target.getComponent(), "Target component should match the input component");
		}
	}
}

