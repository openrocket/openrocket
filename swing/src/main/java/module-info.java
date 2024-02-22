module openrocket.swing {
	requires transitive openrocket.core;

	uses javax.script.ScriptEngineFactory;
	requires org.slf4j;
	requires java.desktop;
	requires com.miglayout.core;
	requires com.miglayout.swing;
	requires com.google.guice;
	requires org.jogamp.gluegen.rt;
	requires org.jogamp.jogl.all;
	requires java.scripting;
	requires org.fife.RSyntaxTextArea;
	requires java.xml.bind;
	requires jcommon;
	requires java.prefs;
	requires darklaf.core;
	requires com.jthemedetector;
	requires versioncompare;
	requires org.jfree.jfreechart;
	requires itextpdf;
	requires ch.qos.logback.core;
	requires ch.qos.logback.classic;
}