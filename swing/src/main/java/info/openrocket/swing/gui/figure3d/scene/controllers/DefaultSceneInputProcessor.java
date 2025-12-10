package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.swing.gui.figure3d.core.math.Raycaster;
import info.openrocket.swing.gui.figure3d.input.InputState;
import info.openrocket.swing.gui.figure3d.scene.core.Light;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.ViewportDimensions;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;

/**
 * Handles input processing and user interaction within the 3D scene management system.
 * This controller processes various input events including mouse clicks, drags, scrolling,
 * and keyboard inputs, translating them into appropriate scene interactions such as camera
 * movement, object selection, and light manipulation.
 * 
 * <p>The handler integrates with OpenRocket components through raycasting for precise
 * object selection and interaction. It coordinates with the CameraController for view
 * manipulation and directly manages light positioning through mouse interaction.</p>
 * 
 * <p>Separated from Scene3DOrchestrator to improve separation of concerns and maintain
 * a clean architecture for input processing within the 3D visualization pipeline.</p>
 */
public class DefaultSceneInputProcessor implements SceneInputProcessor {

    private static final Logger log = LoggerFactory.getLogger(DefaultSceneInputProcessor.class);

    private final InputState inputState;
    private final Raycaster raycaster;
    private final SceneView scene;
    private final CameraControls cameraController;
    
    // Viewport dimensions for coordinate conversion
    private ViewportDimensions viewport;
    
    /**
     * Constructs a new InputHandler with the required dependencies for scene interaction.
     * 
     * @param inputState the input state tracker for mouse and keyboard events
     * @param raycaster the raycaster for 3D object intersection and selection
     * @param scene the scene containing objects and lights to interact with
     * @param cameraController the camera controller for view manipulation
     */
    public DefaultSceneInputProcessor(InputState inputState, Raycaster raycaster, SceneView scene, CameraControls cameraController) {
        this.inputState = inputState;
        this.raycaster = raycaster;
        this.scene = scene;
        this.cameraController = cameraController;
    }
    
    /**
     * Get the input state for external access (e.g., from UI components).
     * 
     * @return the input state instance containing current input status
     */
    @Override
    public InputState getInputState() {
        return inputState;
    }
    
    /**
     * Update the viewport dimensions for coordinate conversion between window and framebuffer coordinates.
     * This is essential for high-DPI display support and accurate raycasting.
     * 
     * @param viewport the new viewport dimensions
     */
    @Override
    public void updateDimensions(ViewportDimensions viewport) {
        this.viewport = viewport;
    }
    
    /**
     * Process all pending input events in the input queue.
     * This method should be called each frame to handle user interactions including
     * scrolling, mouse dragging, clicking, and double-clicking events.
     */
    @Override
    public void processInput() {
        processScrollInput();
        processMouseDragInput();
        processClickInput();
        processDoubleClickInput();
    }
    
    /**
     * Process scroll input for camera zooming.
     * Handles mouse wheel events to control camera dolly (zoom in/out).
     */
    private void processScrollInput() {
        if (inputState.scrollDelta != 0) {
            cameraController.handleScroll(inputState.scrollDelta);
            inputState.scrollDelta = 0;
        }
    }
    
    /**
     * Process mouse drag input for camera movement and light manipulation.
     * Handles different drag modes: orbit (default), pan (with modifier), and light dragging.
     */
    private void processMouseDragInput() {
        if (inputState.dx != 0 || inputState.dy != 0) {
            if (inputState.isLightDragging) {
                updateMainLightRadialAngle(inputState.dx, inputState.dy);
            } else if (inputState.isPanning) {
                cameraController.handlePan(inputState.dx, inputState.dy);
            } else {
                cameraController.handleOrbit(inputState.dx, inputState.dy);
            }
            inputState.dx = 0;
            inputState.dy = 0;
        }
    }
    
    /**
     * Process click input for object selection using raycasting.
     * Performs 3D object intersection testing to enable selection of rocket components
     * and other scene objects.
     */
    private void processClickInput() {
        Point click = inputState.clickPoint.getAndSet(null);
        if (click != null) {
            float[] pixelCoords = convertToFramebufferCoordinates(click.x, click.y);
            raycaster.update(pixelCoords[0], pixelCoords[1], viewport.getFramebufferWidth(), viewport.getFramebufferHeight(), cameraController.getCamera());
            scene.updateSelection(raycaster, inputState.isShiftPressed);
        }
    }
    
    /**
     * Process double-click input for object actions and component identification.
     * Uses raycasting to identify clicked objects and performs component-specific actions.
     */
    private void processDoubleClickInput() {
        Point doubleClick = inputState.doubleClickPoint.getAndSet(null);
        if (doubleClick != null) {
            float[] pixelCoords = convertToFramebufferCoordinates(doubleClick.x, doubleClick.y);
            
            raycaster.update(pixelCoords[0], pixelCoords[1], viewport.getFramebufferWidth(), viewport.getFramebufferHeight(), cameraController.getCamera());
            SceneObject intersectedObject = raycaster.getIntersectedObject(scene.getObjects());
            
            if (intersectedObject != null) {
                RocketComponent component = intersectedObject.getRocketComponent();
                if (component != null) {
                    log.debug("Double-clicked on: {}", component.getComponentName());
                } else {
                    log.debug("Double-clicked on a non-component object.");
                }
            }
        }
    }
    
    /**
     * Convert window coordinates to framebuffer coordinates for DPI-awareness.
     * This conversion is necessary for accurate raycasting on high-DPI displays.
     * 
     * @param windowX the X coordinate in window space
     * @param windowY the Y coordinate in window space
     * @return array containing framebuffer X and Y coordinates
     */
    private float[] convertToFramebufferCoordinates(int windowX, int windowY) {
        return new float[]{
            viewport.windowToFramebufferX(windowX),
            viewport.windowToFramebufferY(windowY)
        };
    }
    
    /**
     * Updates the main light's radial angle based on mouse movement during alt-drag.
     * Provides interactive control over directional lighting by rotating the light
     * direction around multiple axes based on mouse movement.
     * 
     * @param dx horizontal mouse delta for Y-axis rotation
     * @param dy vertical mouse delta for perpendicular axis rotation
     */
    private void updateMainLightRadialAngle(float dx, float dy) {
        Light mainLight = scene.getLightController().getLight(0);
        if (mainLight == null || mainLight.getType() != Light.LightType.DIRECTIONAL) {
            return;
        }

        // Sensitivity for rotation
        float sensitivity = 0.01f;
        float rotationY = dx * sensitivity; // Horizontal mouse movement rotates around Y-axis
        float rotationX = dy * sensitivity; // Vertical mouse movement rotates around X-axis

        // Get current light direction
        Vector3f direction = new Vector3f(mainLight.getDirection());

        // Rotate around the world Y-axis (for horizontal movement)
        direction.rotateY(rotationY);

        // Create a rotation axis perpendicular to the current light direction and the world Y-axis
        Vector3f rotationAxis = new Vector3f(direction).cross(new Vector3f(0, 1, 0)).normalize();
        if (rotationAxis.lengthSquared() > 0.001) {
            // Rotate around the perpendicular axis (for vertical movement)
            direction.rotateAxis(rotationX, rotationAxis.x, rotationAxis.y, rotationAxis.z);
        }

        // Update the light direction
        mainLight.setDirection(direction.x, direction.y, direction.z);
    }
}
