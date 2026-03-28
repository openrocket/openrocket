package info.openrocket.swing.gui.scalefigure.caliper;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.ComponentChangeListener;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.core.util.Transformation;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.figureelements.CaliperLine;
import info.openrocket.swing.gui.figureelements.HorizontalCaliperLine;
import info.openrocket.swing.gui.scalefigure.RocketFigure;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.scalefigure.caliper.snap.CaliperSnapRegistry;
import info.openrocket.swing.gui.scalefigure.caliper.snap.CaliperSnapTarget;
import info.openrocket.core.startup.Application;
import info.openrocket.core.l10n.Translator;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

/**
 * Manages the caliper measurement tool for the rocket panel.
 * Handles all caliper-related state, UI components, and interactions.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class CaliperManager {
	private static final Translator trans = Application.getTranslator();

	private static final double SNAP_PIXEL_TOLERANCE = 8.0; // Tolerance in pixels for snap target detection
	private static final double SHIFT_DRAG_SNAP_PIXEL_TOLERANCE = 20; // Tolerance in pixels for shift-drag snapping

	/**
	 * Caliper mode: VERTICAL for measuring horizontal distances, HORIZONTAL for measuring vertical distances.
	 */
	public enum CaliperMode {
		VERTICAL,
		HORIZONTAL,
		BOTH			// Vertical and horizontal
	}

	/**
	 * Internal state class for storing caliper positions per view type.
	 */
	private static final class CaliperState {
		double caliper1X = Double.NaN;
		double caliper2X = Double.NaN;
		double caliper1Y = Double.NaN;
		double caliper2Y = Double.NaN;
	}

	// References to external components
	private final RocketFigure figure;
	private final OpenRocketDocument document;
	private final ViewTypeProvider viewTypeProvider;
	private final Runnable figureUpdateCallback;
	private final Runnable focusRequestCallback;

	// Caliper state
	private boolean enabled = false;
	private CaliperMode mode = CaliperMode.VERTICAL;
	private boolean wasEnabledBefore3d = false;
	private boolean snapEnabled = true;  // Whether shift-drag snapping is enabled

	// Caliper line elements
	private final CaliperLine caliper1Line;
	private final CaliperLine caliper2Line;
	private final HorizontalCaliperLine caliper1HorizontalLine;
	private final HorizontalCaliperLine caliper2HorizontalLine;

	// Caliper positions in model coordinates
	private double caliper1X = Double.NaN;
	private double caliper2X = Double.NaN;
	private double caliper1Y = Double.NaN;
	private double caliper2Y = Double.NaN;

	// Dragging state
	private CaliperLine draggingCaliperLine = null;
	private HorizontalCaliperLine draggingHorizontalCaliperLine = null;

	// UI components
	private UnitSelector unitSelector;

	// Models
	private final DoubleModel distanceModel;
	private final DoubleModel horizontalDistanceModel;

	// State per view type
	private final CaliperState sideViewCaliperState = new CaliperState();
	private final CaliperState backViewCaliperState = new CaliperState();

	// Snap mode state
	private boolean snapModeActive = false;
	private Integer activeSnapCaliper = null;  // 1 or 2, or null if not in snap mode
	private CaliperSnapTarget hoveredSnapTarget = null;
	private CaliperSnapTarget shiftDragSnappedTarget = null;  // Target currently snapped to during shift-drag
	private final List<CaliperSnapTarget> currentSnapTargets = new ArrayList<>();
	private boolean alwaysShowSnapTargets = false;  // Debug mode: always show all snap targets

	/**
	 * Interface for getting the current view type from the panel.
	 */
	public interface ViewTypeProvider {
		RocketPanel.VIEW_TYPE getCurrentViewType();
	}

	/**
	 * Interface for updating the info message label.
	 */
	public interface InfoMessageUpdater {
		void updateInfoMessage(String messageKey);
	}

	private InfoMessageUpdater infoMessageUpdater = null;

	/**
	 * Create a new CaliperManager.
	 *
	 * @param figure the rocket figure to add caliper elements to
	 * @param document the document containing the rocket
	 * @param viewTypeProvider provider for the current view type
	 * @param figureUpdateCallback callback to update the figure when caliper state changes
	 * @param focusRequestCallback callback to request focus when entering snap mode
	 */
	public CaliperManager(RocketFigure figure, OpenRocketDocument document,
						  ViewTypeProvider viewTypeProvider, Runnable figureUpdateCallback,
						  Runnable focusRequestCallback) {
		this.figure = figure;
		this.document = document;
		this.viewTypeProvider = viewTypeProvider;
		this.figureUpdateCallback = figureUpdateCallback;
		this.focusRequestCallback = focusRequestCallback;

		// Listen to rocket changes to exit snap mode when rocket structure changes
		Rocket rocket = document.getRocket();
		rocket.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				// Exit snap mode when rocket changes (e.g., component added/removed, undo/redo)
				if (snapModeActive) {
					exitSnapMode();
				}
			}
		});
		
		rocket.addComponentChangeListener(new ComponentChangeListener() {
			@Override
			public void componentChanged(ComponentChangeEvent e) {
				// Exit snap mode when components change (e.g., component modified)
				if (snapModeActive) {
					exitSnapMode();
				}
			}
		});

		// Listen to figure rotation changes to update snap targets
		figure.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				// Update snap targets when view rotation changes
				if (snapModeActive && enabled) {
					updateSnapTargets();
					figureUpdateCallback.run();
				}
			}
		});

		// Initialize models
		distanceModel = new DoubleModel(0.0, UnitGroup.UNITS_LENGTH);
		horizontalDistanceModel = new DoubleModel(0.0, UnitGroup.UNITS_LENGTH);

		// Initialize caliper line elements
		caliper1Line = new CaliperLine(0.0);
		caliper1Line.setHandleLabel("1");
		caliper2Line = new CaliperLine(0.0);
		caliper2Line.setHandleLabel("2");
		caliper1Line.setSiblingLine(caliper2Line);
		caliper2Line.setSiblingLine(caliper1Line);
		caliper1HorizontalLine = new HorizontalCaliperLine(0.0);
		caliper1HorizontalLine.setHandleLabel("1");
		caliper2HorizontalLine = new HorizontalCaliperLine(0.0);
		caliper2HorizontalLine.setHandleLabel("2");
		caliper1HorizontalLine.setSiblingLine(caliper2HorizontalLine);
		caliper2HorizontalLine.setSiblingLine(caliper1HorizontalLine);

		// Create UI components
		createUIComponents();
	}

	/**
	 * Create the UI components for the caliper tool.
	 */
	private void createUIComponents() {
		unitSelector = new UnitSelector(distanceModel);
	}

	/**
	 * Get the unit selector.
	 *
	 * @return the unit selector
	 */
	public UnitSelector getUnitSelector() {
		return unitSelector;
	}

	/**
	 * Get the current distance model (vertical or horizontal based on mode).
	 *
	 * @return the current distance model
	 */
	public DoubleModel getCurrentDistanceModel() {
		return (mode == CaliperMode.HORIZONTAL) ? horizontalDistanceModel : distanceModel;
	}

	/**
	 * Get the current caliper mode.
	 *
	 * @return the current caliper mode
	 */
	public CaliperMode getMode() {
		return mode;
	}

	/**
	 * Set the caliper mode.
	 *
	 * @param newMode the new caliper mode
	 */
	public void setMode(CaliperMode newMode) {
		if (mode != newMode && (newMode == CaliperMode.VERTICAL || newMode == CaliperMode.HORIZONTAL)) {
			mode = newMode;
			// Exit snap mode when switching between vertical and horizontal
			if (snapModeActive) {
				exitSnapMode();
			}
			// Update unit selector model based on mode
			if (unitSelector != null) {
				if (mode == CaliperMode.HORIZONTAL) {
					unitSelector.setModel(horizontalDistanceModel);
				} else {
					unitSelector.setModel(distanceModel);
				}
			}
			updateCaliperElements();
			figureUpdateCallback.run();
		}
	}

	/**
	 * Set whether the caliper tool is enabled.
	 *
	 * @param enabled true to enable, false to disable
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (enabled) {
			// Initialize positions if not already set
			if ((mode == CaliperMode.VERTICAL && (Double.isNaN(caliper1X) || Double.isNaN(caliper2X))) ||
					(mode == CaliperMode.HORIZONTAL && (Double.isNaN(caliper1Y) || Double.isNaN(caliper2Y)))) {
				initializeCaliperPositions();
			}
		}
		updateCaliperElements();
		refreshInfoMessage();
		figureUpdateCallback.run();
	}

	/**
	 * Check if the caliper tool is enabled.
	 *
	 * @return true if enabled, false otherwise
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Check whether shift-drag snapping is enabled.
	 *
	 * @return true if snap is enabled
	 */
	public boolean isSnapEnabled() {
		return snapEnabled;
	}

	/**
	 * Set whether shift-drag snapping is enabled.
	 *
	 * @param snapEnabled true to enable snapping
	 */
	public void setSnapEnabled(boolean snapEnabled) {
		this.snapEnabled = snapEnabled;
		refreshInfoMessage();
	}

	/**
	 * Update the footprint info message to reflect the current caliper state.
	 * Called when caliper is enabled/disabled, snap is toggled, or dragging ends.
	 */
	public void refreshInfoMessage() {
		if (infoMessageUpdater == null) {
			return;
		}
		if (!enabled) {
			infoMessageUpdater.updateInfoMessage("RocketPanel.lbl.infoMessage");
		} else if (snapEnabled) {
			infoMessageUpdater.updateInfoMessage("RocketPanel.lbl.infoMessage.caliperSnap");
		} else {
			infoMessageUpdater.updateInfoMessage("RocketPanel.lbl.infoMessage.caliperDragging");
		}
	}

	/**
	 * Returns the info message key to show while a caliper is being dragged.
	 */
	private String getDraggingInfoMessageKey() {
		return snapEnabled
				? "RocketPanel.lbl.infoMessage.caliperDraggingSnap"
				: "RocketPanel.lbl.infoMessage.caliperDragging";
	}

	/**
	 * Handle mouse press events for caliper dragging.
	 *
	 * @param screenX the screen X coordinate
	 * @param screenY the screen Y coordinate
	 * @param screenToModel function to convert screen coordinates to model coordinates
	 * @return true if a caliper line was clicked and dragging started
	 */
	public boolean handleMousePressed(int screenX, int screenY,
									  java.util.function.Function<Point, Point2D.Double> screenToModel) {
		if (!enabled) {
			return false;
		}

		Point screenPoint = new Point(screenX, screenY);
		
		// Get the transform from model to screen coordinates for hit testing
		java.awt.geom.AffineTransform transform = getModelToScreenTransform();
		if (transform == null) {
			return false;
		}
		
		// Get visible rectangle for horizontal caliper handle positioning
		Rectangle visibleRect = figure.getVisibleRect();

		// Use screen-space hit testing instead of model-space tolerance
		// This is more reliable across different DPI settings and platforms
		// Check both handle and line itself for dragging
		if (mode == CaliperMode.VERTICAL) {
			// Check vertical caliper lines - try handle first, then line
			if (caliper1Line != null) {
				// Check handle bounds
				java.awt.geom.Rectangle2D.Double handleBounds = caliper1Line.getHandleBounds(transform);
				boolean hitHandle = handleBounds != null && handleBounds.contains(screenPoint.x, screenPoint.y);
				
				// Check if near the line itself
				boolean hitLine = caliper1Line.isPointNearLine(screenPoint.x, screenPoint.y, transform, visibleRect);
				
				if (hitHandle || hitLine) {
					draggingCaliperLine = caliper1Line;
					// Update info message when dragging starts
					if (infoMessageUpdater != null) {
						infoMessageUpdater.updateInfoMessage(getDraggingInfoMessageKey());
					}
					return true;
				}
			}
			if (caliper2Line != null) {
				// Check handle bounds
				java.awt.geom.Rectangle2D.Double handleBounds = caliper2Line.getHandleBounds(transform);
				boolean hitHandle = handleBounds != null && handleBounds.contains(screenPoint.x, screenPoint.y);
				
				// Check if near the line itself
				boolean hitLine = caliper2Line.isPointNearLine(screenPoint.x, screenPoint.y, transform, visibleRect);
				
				if (hitHandle || hitLine) {
					draggingCaliperLine = caliper2Line;
					// Update info message when dragging starts
					if (infoMessageUpdater != null) {
						infoMessageUpdater.updateInfoMessage(getDraggingInfoMessageKey());
					}
					return true;
				}
			}
		} else {
			// Check horizontal caliper lines - try handle first, then line
			if (caliper1HorizontalLine != null && visibleRect != null) {
				// Check handle bounds
				java.awt.geom.Rectangle2D.Double handleBounds = caliper1HorizontalLine.getHandleBounds(transform, visibleRect);
				boolean hitHandle = handleBounds != null && handleBounds.contains(screenPoint.x, screenPoint.y);
				
				// Check if near the line itself
				boolean hitLine = caliper1HorizontalLine.isPointNearLine(screenPoint.x, screenPoint.y, transform, visibleRect);
				
				if (hitHandle || hitLine) {
					draggingHorizontalCaliperLine = caliper1HorizontalLine;
					// Update info message when dragging starts
					if (infoMessageUpdater != null) {
						infoMessageUpdater.updateInfoMessage(getDraggingInfoMessageKey());
					}
					return true;
				}
			}
			if (caliper2HorizontalLine != null && visibleRect != null) {
				// Check handle bounds
				java.awt.geom.Rectangle2D.Double handleBounds = caliper2HorizontalLine.getHandleBounds(transform, visibleRect);
				boolean hitHandle = handleBounds != null && handleBounds.contains(screenPoint.x, screenPoint.y);
				
				// Check if near the line itself
				boolean hitLine = caliper2HorizontalLine.isPointNearLine(screenPoint.x, screenPoint.y, transform, visibleRect);
				
				if (hitHandle || hitLine) {
					draggingHorizontalCaliperLine = caliper2HorizontalLine;
					// Update info message when dragging starts
					if (infoMessageUpdater != null) {
						infoMessageUpdater.updateInfoMessage(getDraggingInfoMessageKey());
					}
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * Get the transform from model to screen coordinates.
	 * 
	 * @return the AffineTransform, or null if not available
	 */
	private java.awt.geom.AffineTransform getModelToScreenTransform() {
		try {
			Point origin = figure.getSubjectOrigin();
			double scale = figure.getAbsoluteScale();
			java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
			transform.translate(origin.x, origin.y);
			transform.scale(scale, -scale); // Y is inverted
			return transform;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Handle mouse drag events for caliper dragging.
	 *
	 * @param screenX the screen X coordinate
	 * @param screenY the screen Y coordinate
	 * @param screenToModel function to convert screen coordinates to model coordinates
	 * @param shiftDown whether the Shift key is held down (enables snapping)
	 * @return true if a caliper line is being dragged
	 */
	public boolean handleMouseDragged(int screenX, int screenY,
									  java.util.function.Function<Point, Point2D.Double> screenToModel,
									  boolean shiftDown) {
		if (!enabled) {
			return false;
		}

		Point screenPoint = new Point(screenX, screenY);
		Point2D.Double modelPoint = screenToModel.apply(screenPoint);
		if (modelPoint == null) {
			return false;
		}

		boolean wasDragging = isDragging();

		// Dragging a vertical caliper line
		if (draggingCaliperLine != null) {
			double newX = modelPoint.x;

			// Snap when Shift is held OR snap is globally enabled
			if (shiftDown || snapEnabled) {
				// Ensure snap targets are up to date (they may not be if not in snap mode)
				// Temporarily calculate snap targets if needed
				boolean wasInSnapMode = snapModeActive;
				if (!snapModeActive) {
					// Temporarily calculate snap targets for snapping during drag
					updateSnapTargets();
				}
				
				// Find nearest snap target using the caliper line's position
				// For vertical caliper, we only care about X distance (or Z in back view)
				RocketPanel.VIEW_TYPE viewType = viewTypeProvider.getCurrentViewType();
				CaliperSnapTarget nearest = findNearestSnapTargetForCaliper(
						screenX, screenY, screenToModel, CaliperMode.VERTICAL, newX, modelPoint.y);
				if (nearest != null) {
					// Snap to the target's X coordinate (for vertical caliper)
					newX = nearest.getSnapValue(CaliperMode.VERTICAL, viewType);
					// Track the snapped target for highlighting
					if (shiftDragSnappedTarget != nearest) {
						shiftDragSnappedTarget = nearest;
						figureUpdateCallback.run();
					}
				} else {
					// No target found, clear the snapped target
					if (shiftDragSnappedTarget != null) {
						shiftDragSnappedTarget = null;
						figureUpdateCallback.run();
					}
				}
				
				// Clear snap targets if we weren't in snap mode (cleanup)
				if (!wasInSnapMode) {
					currentSnapTargets.clear();
				}
			} else {
				// Shift not held, clear the snapped target
				if (shiftDragSnappedTarget != null) {
					shiftDragSnappedTarget = null;
					figureUpdateCallback.run();
				}
			}
			
			if (draggingCaliperLine == caliper1Line) {
				caliper1Line.setDragPositionLabel(unitSelector.getSelectedUnit().toStringUnit(newX));
				setCaliperLinePosition(true, newX);
			} else if (draggingCaliperLine == caliper2Line) {
				caliper2Line.setDragPositionLabel(unitSelector.getSelectedUnit().toStringUnit(newX));
				setCaliperLinePosition(false, newX);
			}
			return true;
		}

		// Dragging a horizontal caliper line
		if (draggingHorizontalCaliperLine != null) {
			double newY = modelPoint.y;

			// Snap when Shift is held OR snap is globally enabled
			if (shiftDown || snapEnabled) {
				// Ensure snap targets are up to date (they may not be if not in snap mode)
				// Temporarily calculate snap targets if needed
				boolean wasInSnapMode = snapModeActive;
				if (!snapModeActive) {
					// Temporarily calculate snap targets for snapping during drag
					updateSnapTargets();
				}
				
				// Find nearest snap target using the caliper line's position
				// For horizontal caliper, we only care about Y distance
				RocketPanel.VIEW_TYPE viewType = viewTypeProvider.getCurrentViewType();
				CaliperSnapTarget nearest = findNearestSnapTargetForCaliper(
						screenX, screenY, screenToModel, CaliperMode.HORIZONTAL, modelPoint.x, newY);
				if (nearest != null) {
					// Snap to the target's Y coordinate (for horizontal caliper)
					newY = nearest.getSnapValue(CaliperMode.HORIZONTAL, viewType);
					// Track the snapped target for highlighting
					if (shiftDragSnappedTarget != nearest) {
						shiftDragSnappedTarget = nearest;
						figureUpdateCallback.run();
					}
				} else {
					// No target found, clear the snapped target
					if (shiftDragSnappedTarget != null) {
						shiftDragSnappedTarget = null;
						figureUpdateCallback.run();
					}
				}
				
				// Clear snap targets if we weren't in snap mode (cleanup)
				if (!wasInSnapMode) {
					currentSnapTargets.clear();
				}
			} else {
				// Shift not held, clear the snapped target
				if (shiftDragSnappedTarget != null) {
					shiftDragSnappedTarget = null;
					figureUpdateCallback.run();
				}
			}
			
			if (draggingHorizontalCaliperLine == caliper1HorizontalLine) {
				caliper1HorizontalLine.setDragPositionLabel(unitSelector.getSelectedUnit().toStringUnit(newY));
				setHorizontalCaliperLinePosition(true, newY);
			} else if (draggingHorizontalCaliperLine == caliper2HorizontalLine) {
				caliper2HorizontalLine.setDragPositionLabel(unitSelector.getSelectedUnit().toStringUnit(newY));
				setHorizontalCaliperLinePosition(false, newY);
			}
			return true;
		}

		// Update UI if we just started dragging
		if (!wasDragging && isDragging()) {
			figureUpdateCallback.run();
		}

		return false;
	}

	/**
	 * Handle mouse release events to stop caliper dragging.
	 */
	public void handleMouseReleased() {
		boolean wasDragging = isDragging();
		draggingCaliperLine = null;
		draggingHorizontalCaliperLine = null;
		caliper1Line.setDragPositionLabel(null);
		caliper2Line.setDragPositionLabel(null);
		caliper1HorizontalLine.setDragPositionLabel(null);
		caliper2HorizontalLine.setDragPositionLabel(null);
		// Clear shift-drag snapped target when dragging stops
		if (shiftDragSnappedTarget != null) {
			shiftDragSnappedTarget = null;
		}
		// Update UI if we were dragging
		if (wasDragging) {
			refreshInfoMessage();
			figureUpdateCallback.run();
		}
	}

	/**
	 * Check if a caliper line is currently being dragged.
	 *
	 * @return true if dragging, false otherwise
	 */
	public boolean isDragging() {
		return draggingCaliperLine != null || draggingHorizontalCaliperLine != null;
	}

	/**
	 * Set the info message updater callback.
	 *
	 * @param updater the updater to use for updating the info message
	 */
	public void setInfoMessageUpdater(InfoMessageUpdater updater) {
		this.infoMessageUpdater = updater;
	}

	/**
	 * Handle mouse move events for caliper hover effects.
	 *
	 * @param screenX the screen X coordinate
	 * @param screenY the screen Y coordinate
	 * @param screenToModel function to convert screen coordinates to model coordinates
	 */
	public void handleMouseMoved(int screenX, int screenY,
								 java.util.function.Function<Point, Point2D.Double> screenToModel) {
		if (!enabled) {
			return;
		}

		Point screenPoint = new Point(screenX, screenY);
		java.awt.geom.AffineTransform transform = getModelToScreenTransform();
		Rectangle visibleRect = figure.getVisibleRect();

		boolean repaintNeeded = false;

		if (mode == CaliperMode.VERTICAL && caliper1Line != null && caliper2Line != null) {
			boolean nearCal1 = transform != null && isHandleOrLineHit(caliper1Line, screenPoint, transform, visibleRect);
			boolean nearCal2 = transform != null && isHandleOrLineHit(caliper2Line, screenPoint, transform, visibleRect);

			boolean ind1Hovered = transform != null && visibleRect != null
					&& isIndicatorHit(caliper1Line.getIndicatorBounds(caliper1Line.getScreenX(transform), visibleRect, caliper2Line.getScreenX(transform)), screenPoint);
			boolean ind2Hovered = transform != null && visibleRect != null
					&& isIndicatorHit(caliper2Line.getIndicatorBounds(caliper2Line.getScreenX(transform), visibleRect, caliper1Line.getScreenX(transform)), screenPoint);

			boolean cal1WasHovered = caliper1Line.isHovered();
			boolean cal2WasHovered = caliper2Line.isHovered();
			boolean ind1WasHovered = caliper1Line.isIndicatorHovered();
			boolean ind2WasHovered = caliper2Line.isIndicatorHovered();

			caliper1Line.setHovered(nearCal1);
			caliper2Line.setHovered(nearCal2);
			caliper1Line.setIndicatorHovered(ind1Hovered);
			caliper2Line.setIndicatorHovered(ind2Hovered);

			repaintNeeded = (cal1WasHovered != nearCal1 || cal2WasHovered != nearCal2
					|| ind1WasHovered != ind1Hovered || ind2WasHovered != ind2Hovered);
		} else if (mode == CaliperMode.HORIZONTAL && caliper1HorizontalLine != null && caliper2HorizontalLine != null) {
			boolean nearCal1 = transform != null && visibleRect != null
					&& isHandleOrLineHit(caliper1HorizontalLine, screenPoint, transform, visibleRect);
			boolean nearCal2 = transform != null && visibleRect != null
					&& isHandleOrLineHit(caliper2HorizontalLine, screenPoint, transform, visibleRect);

			boolean ind1Hovered = transform != null && visibleRect != null
					&& isIndicatorHit(caliper1HorizontalLine.getIndicatorBounds(caliper1HorizontalLine.getScreenY(transform), visibleRect, caliper2HorizontalLine.getScreenY(transform)), screenPoint);
			boolean ind2Hovered = transform != null && visibleRect != null
					&& isIndicatorHit(caliper2HorizontalLine.getIndicatorBounds(caliper2HorizontalLine.getScreenY(transform), visibleRect, caliper1HorizontalLine.getScreenY(transform)), screenPoint);

			boolean cal1WasHovered = caliper1HorizontalLine.isHovered();
			boolean cal2WasHovered = caliper2HorizontalLine.isHovered();
			boolean ind1WasHovered = caliper1HorizontalLine.isIndicatorHovered();
			boolean ind2WasHovered = caliper2HorizontalLine.isIndicatorHovered();

			caliper1HorizontalLine.setHovered(nearCal1);
			caliper2HorizontalLine.setHovered(nearCal2);
			caliper1HorizontalLine.setIndicatorHovered(ind1Hovered);
			caliper2HorizontalLine.setIndicatorHovered(ind2Hovered);

			repaintNeeded = (cal1WasHovered != nearCal1 || cal2WasHovered != nearCal2
					|| ind1WasHovered != ind1Hovered || ind2WasHovered != ind2Hovered);
		}

		// Repaint if hover state changed
		if (repaintNeeded) {
			figureUpdateCallback.run();
		}
	}

	private boolean isHandleOrLineHit(CaliperLine line, Point screenPoint,
									  java.awt.geom.AffineTransform transform, Rectangle visibleRect) {
		java.awt.geom.Rectangle2D.Double handleBounds = line.getHandleBounds(transform);
		boolean hitHandle = handleBounds != null && handleBounds.contains(screenPoint.x, screenPoint.y);
		boolean hitLine = line.isPointNearLine(screenPoint.x, screenPoint.y, transform, visibleRect);
		return hitHandle || hitLine;
	}

	private boolean isHandleOrLineHit(HorizontalCaliperLine line, Point screenPoint,
									  java.awt.geom.AffineTransform transform, Rectangle visibleRect) {
		java.awt.geom.Rectangle2D.Double handleBounds = line.getHandleBounds(transform, visibleRect);
		boolean hitHandle = handleBounds != null && handleBounds.contains(screenPoint.x, screenPoint.y);
		boolean hitLine = line.isPointNearLine(screenPoint.x, screenPoint.y, transform, visibleRect);
		return hitHandle || hitLine;
	}

	private boolean isIndicatorHit(java.awt.geom.Rectangle2D.Double bounds, Point screenPoint) {
		return bounds != null && bounds.contains(screenPoint.x, screenPoint.y);
	}

	/**
	 * Returns true if the given screen point hits an out-of-view caliper indicator.
	 */
	public boolean isPressingIndicator(int screenX, int screenY) {
		if (!enabled) return false;
		java.awt.geom.AffineTransform transform = getModelToScreenTransform();
		if (transform == null) return false;
		Rectangle visibleRect = figure.getVisibleRect();
		if (visibleRect == null) return false;
		Point p = new Point(screenX, screenY);

		if (mode == CaliperMode.VERTICAL) {
			if (caliper1Line != null) {
				java.awt.geom.Rectangle2D.Double b = caliper1Line.getIndicatorBounds(caliper1Line.getScreenX(transform), visibleRect, caliper2Line.getScreenX(transform));
				if (isIndicatorHit(b, p)) return true;
			}
			if (caliper2Line != null) {
				java.awt.geom.Rectangle2D.Double b = caliper2Line.getIndicatorBounds(caliper2Line.getScreenX(transform), visibleRect, caliper1Line.getScreenX(transform));
				if (isIndicatorHit(b, p)) return true;
			}
		} else {
			if (caliper1HorizontalLine != null) {
				java.awt.geom.Rectangle2D.Double b = caliper1HorizontalLine.getIndicatorBounds(caliper1HorizontalLine.getScreenY(transform), visibleRect, caliper2HorizontalLine.getScreenY(transform));
				if (isIndicatorHit(b, p)) return true;
			}
			if (caliper2HorizontalLine != null) {
				java.awt.geom.Rectangle2D.Double b = caliper2HorizontalLine.getIndicatorBounds(caliper2HorizontalLine.getScreenY(transform), visibleRect, caliper1HorizontalLine.getScreenY(transform));
				if (isIndicatorHit(b, p)) return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if any caliper line, handle, or out-of-view indicator is currently hovered.
	 */
	public boolean isAnyLineHovered() {
		if (mode == CaliperMode.VERTICAL) {
			return (caliper1Line != null && (caliper1Line.isHovered() || caliper1Line.isIndicatorHovered()))
					|| (caliper2Line != null && (caliper2Line.isHovered() || caliper2Line.isIndicatorHovered()));
		} else {
			return (caliper1HorizontalLine != null && (caliper1HorizontalLine.isHovered() || caliper1HorizontalLine.isIndicatorHovered()))
					|| (caliper2HorizontalLine != null && (caliper2HorizontalLine.isHovered() || caliper2HorizontalLine.isIndicatorHovered()));
		}
	}

	/**
	 * Handle mouse exit events to clear hover state.
	 */
	public void handleMouseExited() {
		if (!enabled) {
			return;
		}

		boolean wasHovered = false;
		if (mode == CaliperMode.VERTICAL && caliper1Line != null && caliper2Line != null) {
			wasHovered = caliper1Line.isHovered() || caliper2Line.isHovered()
					|| caliper1Line.isIndicatorHovered() || caliper2Line.isIndicatorHovered();
			caliper1Line.setHovered(false);
			caliper2Line.setHovered(false);
			caliper1Line.setIndicatorHovered(false);
			caliper2Line.setIndicatorHovered(false);
		} else if (mode == CaliperMode.HORIZONTAL && caliper1HorizontalLine != null && caliper2HorizontalLine != null) {
			wasHovered = caliper1HorizontalLine.isHovered() || caliper2HorizontalLine.isHovered()
					|| caliper1HorizontalLine.isIndicatorHovered() || caliper2HorizontalLine.isIndicatorHovered();
			caliper1HorizontalLine.setHovered(false);
			caliper2HorizontalLine.setHovered(false);
			caliper1HorizontalLine.setIndicatorHovered(false);
			caliper2HorizontalLine.setIndicatorHovered(false);
		}
		if (wasHovered) {
			figureUpdateCallback.run();
		}
	}

	/**
	 * Update the caliper elements in the figure based on enabled state and mode.
	 */
	public void updateCaliperElements() {
		if (enabled) {
			// Only set snap mode (transparency) on the active snap caliper
			boolean caliper1IsActive = snapModeActive && activeSnapCaliper != null && activeSnapCaliper == 1;
			boolean caliper2IsActive = snapModeActive && activeSnapCaliper != null && activeSnapCaliper == 2;
			
			caliper1Line.setSnapMode(caliper1IsActive);
			caliper2Line.setSnapMode(caliper2IsActive);
			caliper1HorizontalLine.setSnapMode(caliper1IsActive);
			caliper2HorizontalLine.setSnapMode(caliper2IsActive);
			
			if (mode == CaliperMode.VERTICAL) {
				figure.addRelativeTopExtra(caliper1Line);
				figure.addRelativeTopExtra(caliper2Line);
			} else {
				figure.addRelativeTopExtra(caliper1HorizontalLine);
				figure.addRelativeTopExtra(caliper2HorizontalLine);
			}
		}
		// Note: We don't clear the extras here - that's handled by the caller
	}

	/**
	 * Save the current caliper state for the current view type.
	 */
	public void saveCurrentCaliperState() {
		RocketPanel.VIEW_TYPE viewType = viewTypeProvider.getCurrentViewType();
		CaliperState state = getCaliperStateForView(viewType);
		if (state == null) {
			return;
		}
		state.caliper1X = caliper1X;
		state.caliper2X = caliper2X;
		state.caliper1Y = caliper1Y;
		state.caliper2Y = caliper2Y;
	}

	/**
	 * Load the caliper state for the specified view type.
	 *
	 * @param viewType the view type to load state for
	 */
	public void loadCaliperStateForView(RocketPanel.VIEW_TYPE viewType) {
		CaliperState state = getCaliperStateForView(viewType);
		if (state == null) {
			return;
		}
		caliper1X = state.caliper1X;
		caliper2X = state.caliper2X;
		caliper1Y = state.caliper1Y;
		caliper2Y = state.caliper2Y;

		// If state is uninitialized (NaN), initialize it for this view
		if (Double.isNaN(caliper1X) || Double.isNaN(caliper2X) ||
				Double.isNaN(caliper1Y) || Double.isNaN(caliper2Y)) {
			initializeCaliperPositions();
			return;
		}

		if (caliper1Line != null) {
			caliper1Line.setX(caliper1X);
		}
		if (caliper2Line != null) {
			caliper2Line.setX(caliper2X);
		}
		if (caliper1HorizontalLine != null) {
			caliper1HorizontalLine.setY(caliper1Y);
		}
		if (caliper2HorizontalLine != null) {
			caliper2HorizontalLine.setY(caliper2Y);
		}

		updateCaliperDistance();
		updateCaliperHorizontalDistance();
	}

	/**
	 * Called when switching to 3D view - saves state and disables caliper.
	 */
	public void onSwitchTo3D() {
		saveCurrentCaliperState();
		wasEnabledBefore3d = enabled;
		if (enabled) {
			setEnabled(false);
		}
	}

	/**
	 * Called when switching to 2D view - restores caliper state if it was enabled.
	 */
	public void onSwitchTo2D() {
		if (wasEnabledBefore3d) {
			setEnabled(true);
		}
		wasEnabledBefore3d = false;
	}

	/**
	 * Get the caliper state for a specific view type.
	 */
	private CaliperState getCaliperStateForView(RocketPanel.VIEW_TYPE viewType) {
		if (viewType == RocketPanel.VIEW_TYPE.BackView) {
			return backViewCaliperState;
		} else if (viewType == RocketPanel.VIEW_TYPE.TopView || viewType == RocketPanel.VIEW_TYPE.SideView) {
			return sideViewCaliperState;
		}
		return null;
	}

	/**
	 * Set the position of a vertical caliper line.
	 */
	private void setCaliperLinePosition(boolean cal1Handle, double position) {
		if (Double.isNaN(position)) {
			return;
		}
		if (cal1Handle) {
			caliper1X = position;
			if (caliper1Line != null) {
				caliper1Line.setX(position);
			}
		} else {
			caliper2X = position;
			if (caliper2Line != null) {
				caliper2Line.setX(position);
			}
		}
		updateCaliperDistance();

		saveCurrentCaliperState();
		figureUpdateCallback.run();
	}

	/**
	 * Set the position of a horizontal caliper line.
	 */
	private void setHorizontalCaliperLinePosition(boolean cal1Handle, double position) {
		if (Double.isNaN(position)) {
			return;
		}
		if (cal1Handle) {
			caliper1Y = position;
			if (caliper1HorizontalLine != null) {
				caliper1HorizontalLine.setY(position);
			}
		} else {
			caliper2Y = position;
			if (caliper2HorizontalLine != null) {
				caliper2HorizontalLine.setY(position);
			}
		}
		updateCaliperHorizontalDistance();

		saveCurrentCaliperState();
		figureUpdateCallback.run();
	}
	
	/**
	 * Move a caliper line back into view at 5% from the corresponding edge.
	 * 
	 * @param cal1Handle true for caliper 1, false for caliper 2
	 * @param visibleRect the visible viewport rectangle in screen coordinates
	 * @param screenToModel function to convert screen coordinates to model coordinates
	 */
	public void moveCaliperLineIntoView(boolean cal1Handle, Rectangle visibleRect, 
	                                    java.util.function.Function<Point, Point2D.Double> screenToModel) {
		if (visibleRect == null || !enabled) {
			return;
		}
		
		// Get the visible bounds in model coordinates
		Point2D.Double topLeft = screenToModel.apply(new Point(visibleRect.x, visibleRect.y));
		Point2D.Double bottomRight = screenToModel.apply(
				new Point(visibleRect.x + visibleRect.width, visibleRect.y + visibleRect.height));
		
		if (mode == CaliperMode.VERTICAL) {
			// Vertical mode: move X position to 5% from left or right edge
			double currentX = cal1Handle ? caliper1X : caliper2X;
			double siblingX = cal1Handle ? caliper2X : caliper1X;
			double visibleWidth = bottomRight.x - topLeft.x;
			double margin = visibleWidth * 0.05;
			double newX;

			// Determine which edge (left or right) based on current position
			double centerX = (topLeft.x + bottomRight.x) / 2.0;
			if (currentX < centerX) {
				newX = topLeft.x + margin;
			} else {
				newX = bottomRight.x - margin;
			}

			// If the sibling is already in view at the same spot, offset by one margin gap
			boolean siblingInView = siblingX >= topLeft.x && siblingX <= bottomRight.x;
			if (siblingInView && Math.abs(siblingX - newX) < margin) {
				newX = currentX < centerX ? siblingX + margin : siblingX - margin;
			}

			setCaliperLinePosition(cal1Handle, newX);
		} else {
			// Horizontal mode: move Y position to 5% from top or bottom edge
			// Note: Y coordinates may be inverted after screenToModel conversion
			// So we need to find the actual min and max Y values
			double minY = Math.min(topLeft.y, bottomRight.y);
			double maxY = Math.max(topLeft.y, bottomRight.y);
			double visibleHeight = maxY - minY;
			double margin = visibleHeight * 0.05;

			double currentY = cal1Handle ? caliper1Y : caliper2Y;
			double siblingY = cal1Handle ? caliper2Y : caliper1Y;
			double newY;

			// Determine which edge (top or bottom) based on current position
			double centerY = (minY + maxY) / 2.0;
			if (currentY < centerY) {
				newY = minY + margin;
			} else {
				newY = maxY - margin;
			}

			// If the sibling is already in view at the same spot, offset by one margin gap
			boolean siblingInView = siblingY >= minY && siblingY <= maxY;
			if (siblingInView && Math.abs(siblingY - newY) < margin) {
				newY = currentY < centerY ? siblingY + margin : siblingY - margin;
			}

			setHorizontalCaliperLinePosition(cal1Handle, newY);
		}
	}
	
	/**
	 * Get the caliper line elements for checking indicator bounds.
	 */
	public CaliperLine getCaliper1Line() {
		return caliper1Line;
	}
	
	public CaliperLine getCaliper2Line() {
		return caliper2Line;
	}
	
	public HorizontalCaliperLine getCaliper1HorizontalLine() {
		return caliper1HorizontalLine;
	}
	
	public HorizontalCaliperLine getCaliper2HorizontalLine() {
		return caliper2HorizontalLine;
	}

	/**
	 * Returns the caliper number (1 or 2) whose diamond handle was clicked at the given screen
	 * coordinates, or 0 if no handle was clicked.
	 *
	 * @param screenX absolute screen X coordinate (viewport-relative + scroll offset)
	 * @param screenY absolute screen Y coordinate (viewport-relative + scroll offset)
	 * @return 1, 2, or 0
	 */
	public int getHandleClickedAt(int screenX, int screenY) {
		if (!enabled) return 0;
		java.awt.geom.AffineTransform transform = getModelToScreenTransform();
		if (transform == null) return 0;
		Rectangle visibleRect = figure.getVisibleRect();
		if (mode == CaliperMode.VERTICAL) {
			if (caliper1Line != null) {
				java.awt.geom.Rectangle2D.Double b = caliper1Line.getHandleBounds(transform);
				if (b != null && b.contains(screenX, screenY)) return 1;
			}
			if (caliper2Line != null) {
				java.awt.geom.Rectangle2D.Double b = caliper2Line.getHandleBounds(transform);
				if (b != null && b.contains(screenX, screenY)) return 2;
			}
		} else {
			if (caliper1HorizontalLine != null && visibleRect != null) {
				java.awt.geom.Rectangle2D.Double b = caliper1HorizontalLine.getHandleBounds(transform, visibleRect);
				if (b != null && b.contains(screenX, screenY)) return 1;
			}
			if (caliper2HorizontalLine != null && visibleRect != null) {
				java.awt.geom.Rectangle2D.Double b = caliper2HorizontalLine.getHandleBounds(transform, visibleRect);
				if (b != null && b.contains(screenX, screenY)) return 2;
			}
		}
		return 0;
	}

	/**
	 * Get the current position of a caliper in model coordinates.
	 * Returns X for vertical mode, Y for horizontal mode.
	 *
	 * @param caliperNumber 1 or 2
	 * @return position in metres
	 */
	public double getCaliperPosition(int caliperNumber) {
		if (mode == CaliperMode.VERTICAL) {
			return caliperNumber == 1 ? caliper1X : caliper2X;
		} else {
			return caliperNumber == 1 ? caliper1Y : caliper2Y;
		}
	}

	/**
	 * Set the position of a caliper in model coordinates.
	 * Sets X for vertical mode, Y for horizontal mode.
	 *
	 * @param caliperNumber 1 or 2
	 * @param value position in metres
	 */
	public void setCaliperPosition(int caliperNumber, double value) {
		if (mode == CaliperMode.VERTICAL) {
			setCaliperLinePosition(caliperNumber == 1, value);
		} else {
			setHorizontalCaliperLinePosition(caliperNumber == 1, value);
		}
	}

	/**
	 * Calculate and update the vertical caliper distance.
	 */
	private void updateCaliperDistance() {
		if (Double.isNaN(caliper1X) || Double.isNaN(caliper2X)) {
			return;
		}
		double distance = Math.abs(caliper2X - caliper1X);
		distanceModel.setValue(distance);
	}

	/**
	 * Calculate and update the horizontal caliper distance.
	 */
	private void updateCaliperHorizontalDistance() {
		if (Double.isNaN(caliper1Y) || Double.isNaN(caliper2Y)) {
			return;
		}
		double distance = Math.abs(caliper2Y - caliper1Y);
		if (horizontalDistanceModel != null) {
			horizontalDistanceModel.setValue(distance);
		}
	}

	/**
	 * Initialize caliper positions based on rocket bounds.
	 */
	private void initializeCaliperPositions() {
		FlightConfiguration curConfig = document.getSelectedConfiguration();
		BoundingBox bounds = curConfig.getBoundingBox();
		RocketPanel.VIEW_TYPE currentView = viewTypeProvider.getCurrentViewType();

		if (currentView == RocketPanel.VIEW_TYPE.BackView) {
			// For back view, use symmetric positions around 0 (center of screen)
			if (bounds == null || bounds.span().getY() <= 0) {
				// Default symmetric positions if bounds are invalid
				caliper1X = -0.1;
				caliper2X = 0.1;
				// For horizontal caliper: cal 1 at top (positive Y), cal 2 at bottom (negative Y)
				caliper1Y = 0.1;
				caliper2Y = -0.1;
			} else {
				// Use symmetric positions based on rocket dimensions
				double halfSpanX = bounds.span().getY() / 2.0;  // Y dimension in back view
				double halfSpanY = bounds.span().getZ() / 2.0;  // Z dimension in back view
				caliper1X = -halfSpanX;
				caliper2X = halfSpanX;
				// For horizontal caliper: cal 1 at top (positive Y), cal 2 at bottom (negative Y)
				caliper1Y = halfSpanY;
				caliper2Y = -halfSpanY;
			}
		} else {
			// For side/top views, use fractional positions of rocket length (X dimension)
			// Check if bounds are invalid or if this is the default empty bounding box (0 to 1)
			boolean isEmptyBounds = bounds == null ||
					bounds.span().getX() <= 0 ||
					(MathUtil.equals(bounds.min.getX(), 0.0) && MathUtil.equals(bounds.max.getX(), 1.0));

			if (isEmptyBounds) {
				// Default absolute positions if bounds are invalid or empty (no components)
				// Use positions that are more likely to be visible (closer to center)
				caliper1X = 0.0;
				caliper2X = 0.2;
				// For horizontal caliper: cal 1 at top (positive Y), cal 2 at bottom (negative Y)
				caliper1Y = 0.05;
				caliper2Y = -0.05;
			} else {
				// Use 15% and 85% of rocket length for X
				double length = bounds.span().getX();
				caliper1X = bounds.min.getX() + 0.15 * length;
				caliper2X = bounds.min.getX() + 0.85 * length;
				// Use symmetric positions for Y based on rocket height
				// For horizontal caliper: cal 1 at top (positive Y), cal 2 at bottom (negative Y)
				double halfSpanY = bounds.span().getY() / 2.0;
				caliper1Y = halfSpanY;
				caliper2Y = -halfSpanY;
			}
		}

		if (caliper1Line != null) {
			caliper1Line.setX(caliper1X);
		}
		if (caliper2Line != null) {
			caliper2Line.setX(caliper2X);
		}
		if (caliper1HorizontalLine != null) {
			caliper1HorizontalLine.setY(caliper1Y);
		}
		if (caliper2HorizontalLine != null) {
			caliper2HorizontalLine.setY(caliper2Y);
		}

		updateCaliperDistance();
		updateCaliperHorizontalDistance();

		saveCurrentCaliperState();
	}

	/**
	 * Enter snap mode for the specified caliper.
	 *
	 * @param caliperNumber 1 or 2
	 */
	public void enterSnapMode(int caliperNumber) {
		if (caliperNumber != 1 && caliperNumber != 2) {
			throw new IllegalArgumentException("Caliper number must be 1 or 2");
		}
		snapModeActive = true;
		activeSnapCaliper = caliperNumber;
		hoveredSnapTarget = null;
		updateSnapTargets();
		// Update info message when entering snap mode
		if (infoMessageUpdater != null) {
			infoMessageUpdater.updateInfoMessage("RocketPanel.lbl.infoMessage.snapMode");
		}
		figureUpdateCallback.run();
		
		// Request focus on the scroll pane so Escape key works
		if (focusRequestCallback != null) {
			focusRequestCallback.run();
		}
	}

	/**
	 * Exit snap mode.
	 */
	public void exitSnapMode() {
		snapModeActive = false;
		activeSnapCaliper = null;
		if (hoveredSnapTarget != null) {
			hoveredSnapTarget = null;
			figureUpdateCallback.run();  // Update to clear highlight
		}
		currentSnapTargets.clear();
		refreshInfoMessage();
		figureUpdateCallback.run();
	}

	/**
	 * Check if snap mode is currently active.
	 *
	 * @return true if snap mode is active
	 */
	public boolean isSnapModeActive() {
		return snapModeActive;
	}

	/**
	 * Get the currently active snap caliper (1 or 2), or null if not in snap mode.
	 *
	 * @return the active snap caliper number, or null
	 */
	public Integer getActiveSnapCaliper() {
		return activeSnapCaliper;
	}

	/**
	 * Get the currently hovered snap target, or null if none.
	 *
	 * @return the hovered snap target, or null
	 */
	public CaliperSnapTarget getHoveredSnapTarget() {
		return hoveredSnapTarget;
	}

	/**
	 * Get the snap target currently locked onto during shift-drag.
	 *
	 * @return the shift-drag snapped target, or null
	 */
	public CaliperSnapTarget getShiftDragSnappedTarget() {
		return shiftDragSnappedTarget;
	}

	/**
	 * Get the list of all current snap targets.
	 *
	 * @return the list of current snap targets
	 */
	public List<CaliperSnapTarget> getCurrentSnapTargets() {
		return new ArrayList<>(currentSnapTargets);
	}

	/**
	 * Set whether to always show all snap targets (debug mode).
	 *
	 * @param alwaysShow true to always show all snap targets, false to only show on hover
	 */
	public void setAlwaysShowSnapTargets(boolean alwaysShow) {
		if (alwaysShowSnapTargets != alwaysShow) {
			alwaysShowSnapTargets = alwaysShow;
			figureUpdateCallback.run();
		}
	}

	/**
	 * Check if always showing snap targets is enabled (debug mode).
	 *
	 * @return true if always showing snap targets is enabled
	 */
	public boolean isAlwaysShowSnapTargets() {
		return alwaysShowSnapTargets;
	}

	/**
	 * Update the list of snap targets based on current view type and caliper mode.
	 */
	/**
	 * Set CG and CP positions for snap targets.
	 * Called from RocketPanel to provide CG/CP positions.
	 */
	private Double cgX = Double.NaN;
	private Double cgY = Double.NaN;
	private Double cpX = Double.NaN;
	private Double cpY = Double.NaN;
	
	public void setCGPosition(double x, double y) {
		this.cgX = x;
		this.cgY = y;
	}
	
	public void setCPPosition(double x, double y) {
		this.cpX = x;
		this.cpY = y;
	}
	
	public void updateSnapTargets() {
		currentSnapTargets.clear();
		
		if (!enabled) {
			return;
		}

		RocketPanel.VIEW_TYPE viewType = viewTypeProvider.getCurrentViewType();
		if (viewType == null) {
			return;
		}
		if (viewType.is3d) {
			return;
		}

		// Get component transformations from the figure
		Map<RocketComponent, List<Transformation>> componentTransforms =
				figure.getComponentTransformations();

		// Get snap targets from the registry
		CaliperSnapRegistry registry = CaliperSnapRegistry.getInstance();
		
		for (Map.Entry<RocketComponent, List<Transformation>> entry :
				componentTransforms.entrySet()) {
			RocketComponent component = entry.getKey();
			List<Transformation> transforms = entry.getValue();
			
			for (Transformation transform : transforms) {
				List<CaliperSnapTarget> targets = registry.getSnapTargets(
						component, viewType, mode, transform);
				currentSnapTargets.addAll(targets);
			}
		}
		
		// Add CG and CP snap targets if positions are valid
		// Only add for side/top view (not back view, as CG/CP are 2D positions)
		if (viewType == RocketPanel.VIEW_TYPE.SideView || viewType == RocketPanel.VIEW_TYPE.TopView) {
			if (!Double.isNaN(cgX) && !Double.isNaN(cgY)) {
				// Create a dummy component reference for CG (we'll use null since it's not a component)
				// Actually, we need a component. Let's use the rocket itself.
				RocketComponent rocket = document.getRocket();
				Coordinate cgPos = new Coordinate(cgX, cgY, 0);
				currentSnapTargets.add(new CaliperSnapTarget(cgPos, CaliperManager.CaliperMode.BOTH, 
						rocket, "Center of Gravity"));
			}
			if (!Double.isNaN(cpX) && !Double.isNaN(cpY)) {
				RocketComponent rocket = document.getRocket();
				Coordinate cpPos = new Coordinate(cpX, cpY, 0);
				currentSnapTargets.add(new CaliperSnapTarget(cpPos, CaliperManager.CaliperMode.BOTH, 
						rocket, "Center of Pressure"));
			}
		}
	}

	/**
	 * Find the nearest snap target to the given screen coordinates.
	 * Can be called even when not in snap mode (e.g., when Shift-dragging).
	 *
	 * @param screenX screen X coordinate
	 * @param screenY screen Y coordinate
	 * @param screenToModel function to convert screen to model coordinates
	 * @return the nearest compatible snap target, or null if none within tolerance
	 */
	public CaliperSnapTarget findNearestSnapTarget(int screenX, int screenY,
												   java.util.function.Function<Point, Point2D.Double> screenToModel) {
		// Allow finding snap targets even when not in explicit snap mode
		// (e.g., when Shift-dragging)
		if (!enabled) {
			return null;
		}
		if (currentSnapTargets.isEmpty()) {
			return null;
		}

		Point screenPoint = new Point(screenX, screenY);
		Point2D.Double modelPoint = screenToModel.apply(screenPoint);
		if (modelPoint == null) {
			return null;
		}

		RocketPanel.VIEW_TYPE viewType = viewTypeProvider.getCurrentViewType();
		
		// Convert to Coordinate for distance calculation
		// In back view, screen X maps to model Z, screen Y maps to model Y
		// In side/top view, screen X maps to model X, screen Y maps to model Y
		Coordinate point;
		if (viewType == RocketPanel.VIEW_TYPE.BackView) {
			// Back view: modelPoint.x is actually Z coordinate
			point = new Coordinate(0, modelPoint.y, modelPoint.x);
		} else {
			// Side/Top view: modelPoint.x is X coordinate
			point = new Coordinate(modelPoint.x, modelPoint.y, 0);
		}

		// Fixed pixel tolerance: 8 pixels
		// Convert pixel tolerance to model coordinates using the figure's scale
		double scale = figure.getAbsoluteScale();
		double modelTolerance = SNAP_PIXEL_TOLERANCE / scale;

		CaliperSnapTarget nearest = null;
		double minDistance = Double.MAX_VALUE;

		for (CaliperSnapTarget target : currentSnapTargets) {
			if (!target.isCompatibleWith(mode)) {
				continue;
			}

			double distance = target.getDistanceToPoint(point, viewType);
			if (distance < modelTolerance && distance < minDistance) {
				minDistance = distance;
				nearest = target;
			}
		}

		return nearest;
	}

	/**
	 * Find the nearest snap target for a caliper line being dragged.
	 * This version uses the caliper line's position (not the mouse position) for more accurate snapping.
	 * Uses 1D distance calculation (only in the relevant direction) for shift-drag snapping.
	 *
	 * @param screenX screen X coordinate (for reference)
	 * @param screenY screen Y coordinate (for reference)
	 * @param screenToModel function to convert screen to model coordinates
	 * @param caliperMode the caliper mode (VERTICAL or HORIZONTAL)
	 * @param caliperX the caliper's X position in model coordinates (or Z for back view vertical)
	 * @param caliperY the caliper's Y position in model coordinates
	 * @return the nearest compatible snap target, or null if none within tolerance
	 */
	private CaliperSnapTarget findNearestSnapTargetForCaliper(int screenX, int screenY,
															   java.util.function.Function<Point, Point2D.Double> screenToModel,
															   CaliperMode caliperMode,
															   double caliperX, double caliperY) {
		if (!enabled) {
			return null;
		}
		if (currentSnapTargets.isEmpty()) {
			return null;
		}

		RocketPanel.VIEW_TYPE viewType = viewTypeProvider.getCurrentViewType();
		
		// Create a point representing the caliper line's position
		Coordinate point;
		if (viewType == RocketPanel.VIEW_TYPE.BackView) {
			// Back view: for vertical caliper, caliperX is actually Z coordinate
			if (caliperMode == CaliperMode.VERTICAL) {
				point = new Coordinate(0, caliperY, caliperX);
			} else {
				// Horizontal caliper in back view
				point = new Coordinate(0, caliperY, caliperX);
			}
		} else {
			// Side/Top view: caliperX is X coordinate
			point = new Coordinate(caliperX, caliperY, 0);
		}

		// Use higher tolerance for shift-drag snapping
		// Convert pixel tolerance to model coordinates using the figure's scale
		double scale = figure.getAbsoluteScale();
		double modelTolerance = SHIFT_DRAG_SNAP_PIXEL_TOLERANCE / scale;

		CaliperSnapTarget nearest = null;
		double minDistance = Double.MAX_VALUE;

		for (CaliperSnapTarget target : currentSnapTargets) {
			if (!target.isCompatibleWith(caliperMode)) {
				continue;
			}

			// For shift-drag snapping, calculate 1D distance in the relevant direction only
			double distance = getOneDimensionalDistance(target, point, caliperMode, viewType);
			if (distance < modelTolerance && distance < minDistance) {
				minDistance = distance;
				nearest = target;
			}
		}

		return nearest;
	}

	/**
	 * Calculate 1D distance from a point to a snap target in the relevant direction only.
	 * For vertical caliper in side view: X direction
	 * For horizontal caliper in side/back view: Y direction
	 * For vertical caliper in back view: Z direction
	 *
	 * @param target the snap target
	 * @param point the point in model coordinates
	 * @param caliperMode the caliper mode
	 * @param viewType the view type
	 * @return the 1D distance in model coordinates
	 */
	private double getOneDimensionalDistance(CaliperSnapTarget target, Coordinate point,
											  CaliperMode caliperMode, RocketPanel.VIEW_TYPE viewType) {
		double targetValue = target.getSnapValue(caliperMode, viewType);
		double pointValue;
		
		if (caliperMode == CaliperMode.VERTICAL) {
			if (viewType == RocketPanel.VIEW_TYPE.BackView) {
				// Vertical caliper in back view: use Z coordinate
				pointValue = point.getZ();
			} else {
				// Vertical caliper in side/top view: use X coordinate
				pointValue = point.getX();
			}
		} else {
			// Horizontal caliper: always use Y coordinate
			pointValue = point.getY();
		}
		
		return Math.abs(targetValue - pointValue);
	}

	/**
	 * Handle mouse move in snap mode - update hovered target.
	 *
	 * @param screenX screen X coordinate
	 * @param screenY screen Y coordinate
	 * @param screenToModel function to convert screen to model coordinates
	 */
	public void handleSnapModeMouseMoved(int screenX, int screenY,
										java.util.function.Function<Point, Point2D.Double> screenToModel) {
		if (!snapModeActive) {
			return;
		}

		CaliperSnapTarget nearest = findNearestSnapTarget(screenX, screenY, screenToModel);
		if (nearest != hoveredSnapTarget) {
			hoveredSnapTarget = nearest;
			figureUpdateCallback.run();
		}
	}

	/**
	 * Handle mouse click in snap mode - snap to the nearest target.
	 *
	 * @param screenX screen X coordinate
	 * @param screenY screen Y coordinate
	 * @param screenToModel function to convert screen to model coordinates
	 * @return true if snapping occurred, false otherwise
	 */
	public boolean handleSnapModeMouseClicked(int screenX, int screenY,
											  java.util.function.Function<Point, Point2D.Double> screenToModel) {
		if (!snapModeActive || activeSnapCaliper == null) {
			return false;
		}

		CaliperSnapTarget target = findNearestSnapTarget(screenX, screenY, screenToModel);
		if (target == null) {
			// Clicked on empty space - exit snap mode
			exitSnapMode();
			return false;
		}

		// Snap the caliper to the target
		RocketPanel.VIEW_TYPE viewType = viewTypeProvider.getCurrentViewType();
		double snapValue = target.getSnapValue(mode, viewType);
		boolean isCaliper1 = (activeSnapCaliper == 1);

		if (mode == CaliperMode.VERTICAL) {
			setCaliperLinePosition(isCaliper1, snapValue);
		} else {
			setHorizontalCaliperLinePosition(isCaliper1, snapValue);
		}

		// Exit snap mode after snapping
		exitSnapMode();
		return true;
	}
}

