open module info.openrocket.core {
	uses javax.script.ScriptEngineFactory;
	uses javax.script.ScriptEngine;
	uses javax.script.Bindings;
	uses javax.script.ScriptContext;
	uses info.openrocket.core.optimization.services.OptimizableParameterService;
	uses info.openrocket.core.optimization.services.SimulationModifierService;

	// Libraries
	requires com.google.guice;
	requires java.desktop;
	requires java.scripting;
	requires org.graalvm.js.scriptengine;
	requires org.graalvm.sdk;
	requires org.graalvm.js;
	requires org.graalvm.truffle;
	requires transitive de.javagl.obj;
	requires com.sun.istack.runtime;
	requires jakarta.activation;
	requires jakarta.inject;
	requires java.prefs;
	requires jakarta.xml.bind;
	requires io.github.classgraph;
	requires org.glassfish.jakarta.json;
	requires org.slf4j;
	requires com.opencsv;
	requires org.commonmark;
	requires org.locationtech.jts;

	// TODO: I'm a JPMS noob, so I just exported each package. Should really check which ones are actually needed.
	exports info.openrocket.core.aerodynamics;
	exports info.openrocket.core.aerodynamics.barrowman;
	exports info.openrocket.core.appearance;
	exports info.openrocket.core.appearance.defaults;
	exports info.openrocket.core.arch;
	exports info.openrocket.core.communication;
	exports info.openrocket.core.database;
	exports info.openrocket.core.database.motor;
	exports info.openrocket.core.document;
	exports info.openrocket.core.document.attachments;
	exports info.openrocket.core.document.events;
	exports info.openrocket.core.file;
	exports info.openrocket.core.file.iterator;
	exports info.openrocket.core.file.motor;
	exports info.openrocket.core.file.openrocket;
	exports info.openrocket.core.file.openrocket.importt;
	exports info.openrocket.core.file.openrocket.savers;
	exports info.openrocket.core.file.rasaero;
	exports info.openrocket.core.file.rasaero.export;
	exports info.openrocket.core.file.rasaero.importt;
	exports info.openrocket.core.file.rocksim;
	exports info.openrocket.core.file.rocksim.export;
	exports info.openrocket.core.file.rocksim.importt;
	exports info.openrocket.core.file.simplesax;
	exports info.openrocket.core.file.wavefrontobj;
	exports info.openrocket.core.file.wavefrontobj.export;
	exports info.openrocket.core.file.svg.export;
	exports info.openrocket.core.formatting;
	exports info.openrocket.core.gui.util;
	exports info.openrocket.core.l10n;
	exports info.openrocket.core.logging;
	exports info.openrocket.core.masscalc;
	exports info.openrocket.core.material;
	exports info.openrocket.core.models.atmosphere;
	exports info.openrocket.core.models.gravity;
	exports info.openrocket.core.models.wind;
	exports info.openrocket.core.motor;
	exports info.openrocket.core.optimization.general;
	exports info.openrocket.core.optimization.general.multidim;
	exports info.openrocket.core.optimization.general.onedim;
	exports info.openrocket.core.optimization.rocketoptimization;
	exports info.openrocket.core.optimization.rocketoptimization.domains;
	exports info.openrocket.core.optimization.rocketoptimization.goals;
	exports info.openrocket.core.optimization.rocketoptimization.modifiers;
	exports info.openrocket.core.optimization.rocketoptimization.parameters;
	exports info.openrocket.core.optimization.services;
	exports info.openrocket.core.plugin;
	exports info.openrocket.core.preset;
	exports info.openrocket.core.preset.loader;
	exports info.openrocket.core.preset.xml;
	exports info.openrocket.core.rocketcomponent;
	exports info.openrocket.core.rocketcomponent.position;
	exports info.openrocket.core.rocketvisitors;
	exports info.openrocket.core.scripting;
	exports info.openrocket.core.simulation;
	exports info.openrocket.core.simulation.customexpression;
	exports info.openrocket.core.simulation.exception;
	exports info.openrocket.core.simulation.extension;
	exports info.openrocket.core.simulation.extension.example;
	exports info.openrocket.core.simulation.extension.impl;
	exports info.openrocket.core.simulation.listeners;
	exports info.openrocket.core.simulation.listeners.example;
	exports info.openrocket.core.simulation.listeners.system;
	exports info.openrocket.core.startup;
	exports info.openrocket.core.thrustcurve;
	exports info.openrocket.core.unit;
	exports info.openrocket.core.util;
	exports info.openrocket.core.util.enums;
	exports info.openrocket.core.utils;

	// Service providers
	// Also edit core/src/main/resources/META-INF/services !! (until gradle-modules-plugin supports service
	// copying, see https://github.com/java9-modularity/gradle-modules-plugin/issues/85)
	provides info.openrocket.core.optimization.services.OptimizableParameterService with
			info.openrocket.core.optimization.services.DefaultOptimizableParameterService;
	provides info.openrocket.core.optimization.services.SimulationModifierService with
			info.openrocket.core.optimization.services.DefaultSimulationModifierService;
}
