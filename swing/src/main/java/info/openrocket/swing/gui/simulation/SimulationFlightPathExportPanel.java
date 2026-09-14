package info.openrocket.swing.gui.simulation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.awt.Font;
import java.awt.Window;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.flightpath.FlightPathExportOptions;
import info.openrocket.core.file.flightpath.FlightPathExportOptions.AltitudeReference;
import info.openrocket.core.file.flightpath.FlightPathExportOptions.StageTrackStart;
import info.openrocket.core.file.flightpath.FlightPathExportOptions.Waypoint;
import info.openrocket.core.file.flightpath.FlightPathExporter;
import info.openrocket.core.file.flightpath.FlightPathTemplate;
import info.openrocket.core.file.flightpath.FlightPathTemplateRepository;
import info.openrocket.core.gui.util.SimpleFileFilter;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;

import info.openrocket.swing.gui.dialogs.SwingWorkerDialog;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.widgets.SaveFileChooser;
import net.miginfocom.swing.MigLayout;

/**
 * The "3D Path" tab: exports a simulation's flight path to KML, waypoint CSV, GPX, or a
 * user-supplied Mustache template. The latitude/longitude come from the simulated flight,
 * which OpenRocket extrapolates from the configured launch position.
 */
public class SimulationFlightPathExportPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Translator trans = Application.getTranslator();

	private static final String PREF_NODE = "FlightPathExport";
	private static final String PREF_FORMAT = "format";

	/**
	 * MigLayout constraint for anything that draws text, giving it a few pixels more than it asks
	 * for.
	 *
	 * <p>The bundled Inter UI font measures a shade narrower through {@link java.awt.FontMetrics},
	 * which is what sizes a label, than the look and feel paints it. A label laid out at exactly
	 * its preferred width therefore fails the fit test by a fraction of a pixel, and Swing does not
	 * trim a fraction: it drops whole characters until the text plus an ellipsis fits, costing
	 * three or four of them. Without this every label in this panel came out as "Burno..." or
	 * "Color pins by sta...".
	 */
	private static String textWidth() {
		Font font = UIManager.getFont("Label.font");
		float size = (font != null) ? font.getSize2D() : 12f;
		return "w pref+" + Math.max(8, Math.round(size * 0.75f)) + "px";
	}

	private final Simulation simulation;
	private final JComboBox<String> formatSelector;
	private final List<FlightPathTemplate> templates;
	private final FlightPathOptionsPanel options;

	/**
	 * @param simulation the simulation whose flight path will be exported
	 */
	public SimulationFlightPathExportPanel(Simulation simulation) {
		super(new MigLayout("ins 10, fillx, wrap", "[grow]"));
		this.simulation = simulation;

		Preferences prefs = Application.getPreferences().getNode(PREF_NODE);

		this.templates = new FlightPathTemplateRepository().getTemplates();
		List<String> names = new ArrayList<>();
		for (FlightPathTemplate t : templates) {
			names.add(t.getDisplayName());
		}
		this.formatSelector = new JComboBox<>(names.toArray(new String[0]));

		JPanel formatPanel = new JPanel(new MigLayout("fillx, ins 5"));
		formatPanel.setBorder(BorderFactory.createTitledBorder(trans.get("SimExpPan.border.Format")));
		formatPanel.add(new JLabel(trans.get("SimExpPan.flightPath.lbl.format")), textWidth());
		formatPanel.add(formatSelector, "growx");
		add(formatPanel, "growx");

		this.options = new FlightPathOptionsPanel(isStaged(simulation));
		options.load(prefs);
		add(options, "growx");

		// Restore the previously selected template.
		String saved = prefs.get(PREF_FORMAT, null);
		if (saved != null) {
			for (int i = 0; i < templates.size(); i++) {
				if (templates.get(i).getId().equals(saved)) {
					formatSelector.setSelectedIndex(i);
					break;
				}
			}
		}
	}

	/**
	 * Whether the flight produced more than one branch, i.e. the rocket staged. The staged-track
	 * option has nothing to choose between on a single-stage flight.
	 */
	private static boolean isStaged(Simulation simulation) {
		return simulation.hasSimulationData() && simulation.getSimulatedData().getBranchCount() > 1;
	}

	/**
	 * Show a save dialog and write the flight path with the selected template. Returns
	 * {@code true} if a file was written, and shows a confirmation dialog on success.
	 */
	public boolean doExport() {
		// The flight path comes entirely from the simulated data, so refuse to export a
		// simulation that has never been run rather than writing an empty track.
		if (!simulation.hasSimulationData()) {
			JOptionPane.showMessageDialog(this,
					trans.get("SimExpPan.flightPath.noData.desc"),
					trans.get("SimExpPan.flightPath.noData.title"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		int idx = formatSelector.getSelectedIndex();
		if (idx < 0 || templates.isEmpty()) {
			return false;
		}
		FlightPathTemplate template = templates.get(idx);
		String ext = template.getExtension();

		FileFilter filter = new SimpleFileFilter(
				template.getDisplayName() + " (*." + ext + ")", "." + ext);

		JFileChooser chooser = new SaveFileChooser();
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(Application.getPreferences().getDefaultDirectory());

		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return false;

		File file = chooser.getSelectedFile();
		if (file == null)
			return false;

		file = FileHelper.forceExtension(file, ext);
		if (!FileHelper.confirmWrite(file, this)) {
			return false;
		}

		Application.getPreferences().setDefaultDirectory(chooser.getCurrentDirectory());

		// Persist the flight-path options and remember the chosen format.
		Preferences prefs = Application.getPreferences().getNode(PREF_NODE);
		FlightPathExportOptions opts = options.toOptions();
		options.store(prefs);
		prefs.put(PREF_FORMAT, template.getId());

		FlightPathExporter exporter = new FlightPathExporter(simulation, simulation.getSimulatedData(), opts);

		if (!exporter.hasLaunchPosition()) {
			int result = JOptionPane.showConfirmDialog(this,
					trans.get("SimExpPan.flightPath.noPosition.desc"),
					trans.get("SimExpPan.flightPath.noPosition.title"),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result != JOptionPane.OK_OPTION) {
				return false;
			}
		}

		// Write on a background worker, exactly like the CSV export: quick writes finish
		// silently, long ones show a modal progress dialog that blocks the config dialog
		// (and its Export button) until completion.
		final File outFile = file;
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try (OutputStream os = new BufferedOutputStream(new FileOutputStream(outFile))) {
					exporter.export(template, os);
				}
				return null;
			}
		};

		Window parent = SwingUtilities.getWindowAncestor(this);
		if (!SwingWorkerDialog.runWorker(parent, trans.get("SimExpPan.flightPath.progress.title"),
				trans.get("SimExpPan.flightPath.progress.desc") + " " + outFile.getName() + "...", worker)) {
			// User cancelled the write.
			outFile.delete();
			return false;
		}

		try {
			worker.get();
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			JOptionPane.showMessageDialog(this, new String[] {
					trans.get("SimExpPan.flightPath.error.desc"),
					cause != null ? cause.getMessage() : e.getMessage() },
					trans.get("SimExpPan.flightPath.error.title"), JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (InterruptedException e) {
			return false;
		}

		return true;
	}

	/**
	 * Options controlling the templated flight-path export: units, which waypoints to
	 * emit, path geometry, and flight metadata the simulation itself does not carry.
	 */
	private static class FlightPathOptionsPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		/** Waypoints enabled by default (matches {@link FlightPathExportOptions}). */
		private static final EnumSet<Waypoint> DEFAULT_WAYPOINTS = EnumSet.allOf(Waypoint.class);

		/** Columns the waypoint checkboxes are laid out in; keep in step with the grid's columns. */
		private static final int WAYPOINT_COLUMNS = 3;

		private final JComboBox<Unit> altitudeUnit = createUnitCombo();
		private final JComboBox<Unit> distanceUnit = createUnitCombo();
		private final Map<Waypoint, JCheckBox> waypointBoxes = new EnumMap<>(Waypoint.class);
		private final JCheckBox flightPath = new JCheckBox(trans.get("SimExpPan.flightPath.lbl.flightPath"));
		private final JCheckBox groundTrack = new JCheckBox(trans.get("SimExpPan.flightPath.lbl.groundTrack"));
		private final JSpinner stride = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
		private final JComboBox<String> stageTrackStart = new JComboBox<>(new String[] {
				trans.get("SimExpPan.flightPath.stageTracks.separation"),
				trans.get("SimExpPan.flightPath.stageTracks.pad") });
		private final JCheckBox waypointLabels = new JCheckBox(trans.get("SimExpPan.flightPath.lbl.waypointLabels"));
		private final JCheckBox colorPins = new JCheckBox(trans.get("SimExpPan.flightPath.lbl.colorPins"));
		private final JComboBox<String> altitudeReference = new JComboBox<>(new String[] {
				trans.get("SimExpPan.flightPath.altitudeRef.automatic"),
				trans.get("SimExpPan.flightPath.altitudeRef.ground"),
				trans.get("SimExpPan.flightPath.altitudeRef.seaLevel") });

		FlightPathOptionsPanel(boolean staged) {
			super(new MigLayout("ins 0, fillx, wrap", "[grow]"));

			// Each box is a single growing column, and every row inside it is a nested panel with
			// its own simple grid. Spanning controls across columns of one big grid instead lets
			// MigLayout under-report the width the box needs, and the dialog then hands it less
			// than that and paints the labels with an ellipsis.

			JPanel units = new JPanel(new MigLayout("ins 5, fillx, wrap", "[grow]"));
			units.setBorder(BorderFactory.createTitledBorder(trans.get("SimExpPan.flightPath.border.units")));

			// The two unit pickers are a pair of short controls, so they share a row.
			JPanel unitRow = new JPanel(new MigLayout("ins 0, fillx", "[][grow]para[][grow]"));
			unitRow.add(new JLabel(trans.get("SimExpPan.flightPath.lbl.altitude")), textWidth());
			unitRow.add(altitudeUnit, "growx");
			unitRow.add(new JLabel(trans.get("SimExpPan.flightPath.lbl.distance")), textWidth());
			unitRow.add(distanceUnit, "growx");
			units.add(unitRow, "growx");

			// The altitude reference keeps a row of its own: its choices are whole phrases.
			JPanel refRow = new JPanel(new MigLayout("ins 0, fillx", "[][grow]"));
			JLabel altRefLabel = new JLabel(trans.get("SimExpPan.flightPath.lbl.altitudeRef"));
			String altRefTtip = trans.get("SimExpPan.flightPath.altitudeRef.ttip");
			altRefLabel.setToolTipText(altRefTtip);
			altitudeReference.setToolTipText(altRefTtip);
			refRow.add(altRefLabel, textWidth());
			refRow.add(altitudeReference, "growx");
			units.add(refRow, "growx");
			add(units, "growx");

			JPanel wp = new JPanel(new MigLayout("ins 5, fillx, wrap", "[grow]"));
			wp.setBorder(BorderFactory.createTitledBorder(trans.get("SimExpPan.flightPath.border.waypoints")));
			JPanel waypointGrid = new JPanel(new MigLayout("ins 0", "[]para[]para[]"));
			int col = 0;
			for (Waypoint w : Waypoint.values()) {
				JCheckBox box = new JCheckBox(waypointLabel(w));
				waypointBoxes.put(w, box);
				boolean endOfRow = (col % WAYPOINT_COLUMNS == WAYPOINT_COLUMNS - 1);
				waypointGrid.add(box, endOfRow ? textWidth() + ", wrap" : textWidth());
				col++;
			}
			wp.add(waypointGrid);

			// The two marker options are a pair, so they sit side by side rather than stacked.
			JPanel markerRow = new JPanel(new MigLayout("ins 0", "[]para[]"));
			waypointLabels.setToolTipText(trans.get("SimExpPan.flightPath.waypointLabels.ttip"));
			markerRow.add(waypointLabels, textWidth());
			colorPins.setToolTipText(trans.get("SimExpPan.flightPath.colorPins.ttip"));
			markerRow.add(colorPins, textWidth());
			wp.add(markerRow, "gaptop para");
			add(wp, "growx");

			JPanel path = new JPanel(new MigLayout("ins 5, fillx, wrap", "[grow]"));
			path.setBorder(BorderFactory.createTitledBorder(trans.get("SimExpPan.flightPath.border.path")));
			JPanel geometryRow = new JPanel(new MigLayout("ins 0", "[]para[]"));
			geometryRow.add(flightPath, textWidth());
			geometryRow.add(groundTrack, textWidth());
			path.add(geometryRow);

			JPanel strideRow = new JPanel(new MigLayout("ins 0", "[][]"));
			strideRow.add(new JLabel(trans.get("SimExpPan.flightPath.lbl.stride")), textWidth());
			strideRow.add(stride);
			path.add(strideRow);

			// Only a staged flight has a shared ascent to draw once or once per stage.
			JPanel stageRow = new JPanel(new MigLayout("ins 0, fillx", "[][grow]"));
			JLabel stageTrackLabel = new JLabel(trans.get("SimExpPan.flightPath.lbl.stageTracks"));
			stageRow.add(stageTrackLabel, textWidth());
			stageRow.add(stageTrackStart, "growx");
			if (!staged) {
				stageTrackLabel.setEnabled(false);
				stageTrackStart.setEnabled(false);
				String ttip = trans.get("SimExpPan.flightPath.stageTracks.singleStage.ttip");
				stageTrackLabel.setToolTipText(ttip);
				stageTrackStart.setToolTipText(ttip);
			}
			path.add(stageRow, "growx");
			add(path, "growx");
		}

		FlightPathExportOptions toOptions() {
			FlightPathExportOptions o = new FlightPathExportOptions();
			o.setAltitudeUnit((Unit) altitudeUnit.getSelectedItem());
			o.setDistanceUnit((Unit) distanceUnit.getSelectedItem());
			for (Map.Entry<Waypoint, JCheckBox> e : waypointBoxes.entrySet()) {
				o.setWaypoint(e.getKey(), e.getValue().isSelected());
			}
			o.setIncludeFlightPath(flightPath.isSelected());
			o.setIncludeGroundTrack(groundTrack.isSelected());
			o.setPathStride((Integer) stride.getValue());
			o.setStageTrackStart(StageTrackStart.values()[Math.max(0, stageTrackStart.getSelectedIndex())]);
			o.setAltitudeReference(AltitudeReference.values()[Math.max(0, altitudeReference.getSelectedIndex())]);
			o.setShowWaypointLabels(waypointLabels.isSelected());
			o.setColorWaypointPins(colorPins.isSelected());
			return o;
		}

		void load(Preferences p) {
			setUnit(altitudeUnit, p.get("altitudeUnit", null));
			setUnit(distanceUnit, p.get("distanceUnit", null));
			for (Map.Entry<Waypoint, JCheckBox> e : waypointBoxes.entrySet()) {
				boolean def = DEFAULT_WAYPOINTS.contains(e.getKey());
				e.getValue().setSelected(p.getBoolean("wp." + e.getKey().name(), def));
			}
			flightPath.setSelected(p.getBoolean("includeFlightPath", true));
			groundTrack.setSelected(p.getBoolean("includeGroundTrack", true));
			stride.setValue(p.getInt("pathStride", 1));
			stageTrackStart.setSelectedIndex(stageTrackStartOrdinal(p.get("stageTrackStart", null)));
			altitudeReference.setSelectedIndex(altitudeReferenceOrdinal(p.get("altitudeReference", null)));
			waypointLabels.setSelected(p.getBoolean("showWaypointLabels", true));
			colorPins.setSelected(p.getBoolean("colorWaypointPins", true));
		}

		/** Preferences hold the enum name; fall back to the default on anything unrecognized. */
		private static int altitudeReferenceOrdinal(String name) {
			if (name != null) {
				for (AltitudeReference value : AltitudeReference.values()) {
					if (value.name().equals(name))
						return value.ordinal();
				}
			}
			
			return AltitudeReference.AUTOMATIC.ordinal();
		}

		/** Preferences hold the enum name; fall back to the default on anything unrecognized. */
		private static int stageTrackStartOrdinal(String name) {
			if (name != null) {
				for (StageTrackStart value : StageTrackStart.values()) {
					if (value.name().equals(name))
						return value.ordinal();
				}
			}

			return StageTrackStart.SEPARATION.ordinal();
		}

		void store(Preferences p) {
			p.put("altitudeUnit", ((Unit) altitudeUnit.getSelectedItem()).getUnit());
			p.put("distanceUnit", ((Unit) distanceUnit.getSelectedItem()).getUnit());
			for (Map.Entry<Waypoint, JCheckBox> e : waypointBoxes.entrySet()) {
				p.putBoolean("wp." + e.getKey().name(), e.getValue().isSelected());
			}
			p.putBoolean("includeFlightPath", flightPath.isSelected());
			p.putBoolean("includeGroundTrack", groundTrack.isSelected());
			p.putInt("pathStride", (Integer) stride.getValue());
			p.put("stageTrackStart",
					StageTrackStart.values()[Math.max(0, stageTrackStart.getSelectedIndex())].name());
			p.put("altitudeReference",
					AltitudeReference.values()[Math.max(0, altitudeReference.getSelectedIndex())].name());
			p.putBoolean("showWaypointLabels", waypointLabels.isSelected());
			p.putBoolean("colorWaypointPins", colorPins.isSelected());
		}

		private static JComboBox<Unit> createUnitCombo() {
			JComboBox<Unit> combo = new JComboBox<>();
			UnitGroup group = UnitGroup.UNITS_DISTANCE;
			for (int i = 0; i < group.getUnitCount(); i++) {
				combo.addItem(group.getUnit(i));
			}
			combo.setSelectedItem(group.getDefaultUnit());
			return combo;
		}

		private static void setUnit(JComboBox<Unit> combo, String name) {
			if (name == null) {
				return;
			}
			try {
				combo.setSelectedItem(UnitGroup.UNITS_DISTANCE.getUnit(name));
			} catch (IllegalArgumentException ignore) {
				// Unknown unit name in preferences; keep the default.
			}
		}

		private static String waypointLabel(Waypoint w) {
			switch (w) {
				case PAD: return trans.get("FlightPathExport.waypoint.pad");
				case LIFTOFF: return trans.get("FlightPathExport.waypoint.liftoff");
				case BURNOUT: return trans.get("FlightPathExport.waypoint.burnout");
				case APOGEE: return trans.get("FlightPathExport.waypoint.apogee");
				case RECOVERY: return trans.get("FlightPathExport.waypoint.recovery");
				case LANDING: return trans.get("FlightPathExport.waypoint.landing");
				case MAX_VELOCITY: return trans.get("FlightPathExport.waypoint.maxVelocity");
				case MAX_ACCELERATION: return trans.get("FlightPathExport.waypoint.maxAcceleration");
				default: return w.name();
			}
		}
	}
}
