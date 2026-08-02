package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figure3d.math.Raycaster;
import info.openrocket.swing.gui.figure3d.input.InputState;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.Scene;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultSceneInputProcessorTest {

    @Test
    void rotatingRocketDoesNotTriggerRefit() {
        TestContext context = createProcessor();

        context.processor.processInput();

        verify(context.cameraControls, never()).focusOnRocket();
    }

    private TestContext createProcessor() {
        RenderingConfiguration renderingConfiguration = RenderingConfiguration.builder().build();
        renderingConfiguration.getVisualEffects().setRotateRocketOnDrag(true);

        Camera camera = Camera.builder()
                .withAspectRatio(1.0f)
                .withFixedCenterOfInterest(false)
                .build();
        Scene scene = Scene.builder(mock(Rocket.class), camera, renderingConfiguration).build();

        CameraControls cameraControls = mock(CameraControls.class);
        when(cameraControls.getCamera()).thenReturn(camera);

        InputState inputState = new InputState();
        inputState.dragJustStarted = true;
        inputState.addDrag(12.0f, 6.0f);

        Raycaster raycaster = mock(Raycaster.class);
        DefaultSceneInputProcessor processor =
                new DefaultSceneInputProcessor(inputState, raycaster, scene, cameraControls, renderingConfiguration);
        return new TestContext(processor, cameraControls);
    }

    private record TestContext(DefaultSceneInputProcessor processor, CameraControls cameraControls) {
    }
}
