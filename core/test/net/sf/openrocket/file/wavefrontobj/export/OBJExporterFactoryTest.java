package net.sf.openrocket.file.wavefrontobj.export;

import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import net.sf.openrocket.util.TestRockets;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OBJExporterFactoryTest extends BaseTestCase {
    @Test
    public void testExport() throws IOException {
        Rocket rocket = OpenRocketDocumentFactory.createNewRocket().getRocket();
        AxialStage sustainer = rocket.getStage(0);

        NoseCone noseCone = new NoseCone();
        noseCone.setBaseRadius(0.05);
        noseCone.setLength(0.1);
        noseCone.setShoulderLength(0.01);
        noseCone.setShoulderRadius(0.03);
        noseCone.setShoulderThickness(0.002);
        noseCone.setShoulderCapped(false);
        sustainer.addChild(noseCone);

        BodyTube bodyTube = new BodyTube();
        bodyTube.setOuterRadius(0.05);
        bodyTube.setThickness(0.005);
        bodyTube.setLength(0.3);
        sustainer.addChild(bodyTube);

        LaunchLug launchLug = new LaunchLug();
        launchLug.setLength(0.05);
        launchLug.setOuterRadius(0.02);
        launchLug.setThickness(0.005);
        launchLug.setInstanceCount(2);
        launchLug.setInstanceSeparation(0.1);
        bodyTube.addChild(launchLug);

        TrapezoidFinSet finSet = new TrapezoidFinSet();
        finSet.setRootChord(0.05);
        finSet.setTabLength(0.03);
        finSet.setTabHeight(0.01);
        finSet.setTabOffset(-0.0075);
        finSet.setCantAngle(Math.toRadians(10));
        bodyTube.addChild(finSet);

        TubeFinSet tubeFinSet = new TubeFinSet();
        tubeFinSet.setFinCount(4);
        tubeFinSet.setOuterRadius(0.01);
        tubeFinSet.setLength(0.05);
        tubeFinSet.setBaseRotation(Math.PI / 8);
        tubeFinSet.setAxialOffset(-0.1);
        bodyTube.addChild(tubeFinSet);

        Transition transition = new Transition();
        transition.setLength(0.1);
        transition.setForeRadius(0.05);
        transition.setAftRadius(0.025);
        transition.setThickness(0.003);
        transition.setShapeType(Transition.Shape.PARABOLIC);
        transition.setShapeParameter(0.7);
        sustainer.addChild(transition);

        Parachute parachute = new Parachute();
        parachute.setRadiusAutomatic(false);
        parachute.setRadius(0.05);
        parachute.setLength(0.075);
        parachute.setRadialPosition(0.02);
        parachute.setRadialDirection(Math.PI / 3);
        bodyTube.addChild(parachute);

        RailButton railButton = new RailButton();
        railButton.setScrewHeight(0.0025);
        railButton.setAngleOffset(Math.toRadians(67));
        bodyTube.addChild(railButton);
        List<RocketComponent> components = List.of(rocket);

        Path tempFile = Files.createTempFile("testExport", ".obj");

        finSet.setFinCount(1);
        finSet.setAngleOffset(Math.toRadians(45));

        TestRockets.dumpRocket(rocket, "/Users/SiboVanGool/Downloads/test.ork");
        OBJExporterFactory exporterFactory = new OBJExporterFactory(components, true, false, true,
                "/Users/SiboVanGool/Downloads/testExport.obj");
        exporterFactory.doExport();

        Files.delete(tempFile);
    }
}
