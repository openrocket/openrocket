package info.openrocket.swing.gui.figure3d.input;

import info.openrocket.swing.gui.figure3d.window.CursorQuery;

import java.awt.Point;
import java.awt.event.KeyEvent;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

/**
 * GLFW-specific mouse input handler that distinguishes between clicks, drags, and double-clicks.
 * Populates shared InputState with processed mouse events for 3D scene interaction.
 *
 * Note: There are two input paths in this codebase.
 * 1) GLFW path: this class receives events via GLFW callbacks set by the window backend.
 * 2) AWT path: {@code ui.info.openrocket.swing.gui.figure3d.GLScenePanel} registers Swing listeners and writes to the same
 *    InputState through the orchestrator. Both paths converge on the same SceneInputProcessor.
 */
public class MouseInputHandler implements InputHandler {

    private final InputState inputState;
    private final CursorQuery windowManager;
    private final KeyboardHandler keyboardHandler;

    // Internal state to manage click vs. drag
    private double lastX, lastY;
    private Point pressPoint = null;
    private boolean isDragging = false;
    private boolean isMiddleMousePressed = false;
    private int activeDragButton = -1;
    private static final double CLICK_DRAG_THRESHOLD_SQ = 5.0 * 5.0;

    // State for double-click detection
    private long lastClickTime = 0;
    private Point lastClickPoint = null;
    private static final long DOUBLE_CLICK_INTERVAL = 750; // Time in milliseconds
    private static final double DOUBLE_CLICK_DISTANCE_THRESHOLD_SQ = 10.0 * 10.0; // Max distance for double-click

    /**
     * Creates a mouse input handler.
     * @param inputState shared state object to update with mouse events
     * @param windowManager GLFW window manager for cursor position queries
     * @param keyboardHandler keyboard handler for modifier key state
     */
    public MouseInputHandler(InputState inputState, CursorQuery windowManager, KeyboardHandler keyboardHandler) {
        this.inputState = inputState;
        this.windowManager = windowManager;
        this.keyboardHandler = keyboardHandler;
    }

    @Override
    public void handleMouseButton(int button, int action, int mods) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            if (action == GLFW_PRESS) {
                // On press, we just record the initial state for drag detection.
                double[] pos = windowManager.getCursorPosition();
                pressPoint = new Point((int)pos[0], (int)pos[1]);
                lastX = pos[0];
                lastY = pos[1];
                isDragging = false;
                activeDragButton = button;
                
                // Update shift key state for multi-selection
                inputState.isShiftPressed = keyboardHandler.isKeyPressed(KeyEvent.VK_SHIFT);
            } else if (action == GLFW_RELEASE) {
                // On release, we determine if it was a click or a double-click.
                if (!isDragging && pressPoint != null) {
                    long currentTime = System.currentTimeMillis();
                    boolean isWithinTimeThreshold = currentTime - lastClickTime < DOUBLE_CLICK_INTERVAL;
                    boolean isWithinDistanceThreshold = lastClickPoint != null && 
                        lastClickPoint.distanceSq(pressPoint.x, pressPoint.y) <= DOUBLE_CLICK_DISTANCE_THRESHOLD_SQ;
                    
                    if (isWithinTimeThreshold && isWithinDistanceThreshold) {
                        // This is a double-click - same location clicked quickly
                        inputState.doubleClickPoint.set(pressPoint);
                        // Clear any pending single click since this is now a double-click
                        inputState.clickPoint.set(null);
                        lastClickTime = 0; // Reset timer
                        lastClickPoint = null; // Reset location
                    } else {
                        // This is a single-click - either too slow or different location
                        inputState.clickPoint.set(pressPoint);
                        lastClickTime = currentTime;
                        lastClickPoint = new Point(pressPoint.x, pressPoint.y);
                    }
                }
                // Reset drag state at the end of the interaction.
                pressPoint = null;
                isDragging = false;
                activeDragButton = -1;
                inputState.isLightDragging = false;
                inputState.isPanning = false;
            }
        } else if (button == GLFW_MOUSE_BUTTON_MIDDLE) {
            if (action == GLFW_PRESS) {
                isMiddleMousePressed = true;
                double[] pos = windowManager.getCursorPosition();
                lastX = pos[0];
                lastY = pos[1];
            } else if (action == GLFW_RELEASE) {
                isMiddleMousePressed = false;
            }
        } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
            if (action == GLFW_PRESS) {
                double[] pos = windowManager.getCursorPosition();
                pressPoint = new Point((int) pos[0], (int) pos[1]);
                lastX = pos[0];
                lastY = pos[1];
                isDragging = false;
                activeDragButton = button;
            } else if (action == GLFW_RELEASE) {
                pressPoint = null;
                isDragging = false;
                activeDragButton = -1;
                inputState.isLightDragging = false;
                inputState.isPanning = false;
            }
        }
    }

    @Override
    public void handleMouseMovement(double xpos, double ypos) {
        if (pressPoint != null) {
            if (!isDragging && pressPoint.distanceSq(xpos, ypos) > CLICK_DRAG_THRESHOLD_SQ) {
                isDragging = true;
                inputState.dragJustStarted = true;
            }

            if (isDragging) {
                inputState.addDrag((float) (xpos - lastX), (float) (ypos - lastY));
                updateDragMode();
            }
        }
        
        // Process middle mouse button movement for panning
        if (isMiddleMousePressed) {
            inputState.addDrag((float) (xpos - lastX), (float) (ypos - lastY));
            inputState.isPanning = true; // Middle mouse is always panning
        }
        
        // Always update last position for the next delta calculation
        lastX = xpos;
        lastY = ypos;
    }

    @Override
    public void handleScroll(double xoffset, double yoffset) {
        inputState.addScroll((float) yoffset * -1.0f);
    }

    private void updateDragMode() {
        boolean isRightDrag = activeDragButton == GLFW_MOUSE_BUTTON_RIGHT;
        boolean isAltDrag = keyboardHandler.isKeyPressed(KeyEvent.VK_ALT);
        inputState.isLightDragging = isRightDrag || isAltDrag;
        inputState.isPanning = !inputState.isLightDragging && keyboardHandler.isKeyPressed(KeyEvent.VK_CONTROL);
    }
}
