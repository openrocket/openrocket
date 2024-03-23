open module info.openrocket.swing {
	requires transitive info.openrocket.core;

	uses javax.script.ScriptEngineFactory;
	uses javax.script.ScriptEngine;
	uses javax.script.Bindings;
	uses javax.script.ScriptContext;
	uses info.openrocket.swing.gui.rocketfigure.RocketComponentShapeService;

	// Libraries
	requires org.slf4j;
	requires java.desktop;
	requires com.miglayout.core;
	requires com.miglayout.swing;
	requires com.google.guice;
	requires org.jogamp.gluegen.rt;
	requires org.jogamp.jogl.all;
	requires java.scripting;
	requires org.fife.RSyntaxTextArea;
	requires jakarta.xml.bind;
	requires jcommon;
	requires java.prefs;
	requires com.jthemedetector;
	requires versioncompare;
	requires org.jfree.jfreechart;
	requires itextpdf;
	requires ch.qos.logback.core;
	requires ch.qos.logback.classic;
	requires jakarta.inject;
	requires com.formdev.flatlaf;
	requires com.formdev.flatlaf.extras;
	requires com.formdev.flatlaf.intellijthemes;

	// Service providers
	// Also edit swing/src/main/resources/META-INF/services !! (until gradle-modules-plugin supports service
	// copying, see https://github.com/java9-modularity/gradle-modules-plugin/issues/85)
	provides info.openrocket.swing.gui.rocketfigure.RocketComponentShapeService with
			info.openrocket.swing.gui.rocketfigure.BodyTubeShapes,
			info.openrocket.swing.gui.rocketfigure.ComponentAssemblyShapes,
			info.openrocket.swing.gui.rocketfigure.FinSetShapes,
			info.openrocket.swing.gui.rocketfigure.LaunchLugShapes,
			info.openrocket.swing.gui.rocketfigure.MassComponentShapes,
			info.openrocket.swing.gui.rocketfigure.ParachuteShapes,
			info.openrocket.swing.gui.rocketfigure.ParallelStageShapes,
			info.openrocket.swing.gui.rocketfigure.PodSetShapes,
			info.openrocket.swing.gui.rocketfigure.RailButtonShapes,
			info.openrocket.swing.gui.rocketfigure.RingComponentShapes,
			info.openrocket.swing.gui.rocketfigure.RocketComponentShapes,
			info.openrocket.swing.gui.rocketfigure.ShockCordShapes,
			info.openrocket.swing.gui.rocketfigure.StreamerShapes,
			info.openrocket.swing.gui.rocketfigure.SymmetricComponentShapes,
			info.openrocket.swing.gui.rocketfigure.TransitionShapes,
			info.openrocket.swing.gui.rocketfigure.TubeFinSetShapes,
			info.openrocket.swing.gui.rocketfigure.TubeShapes;
}