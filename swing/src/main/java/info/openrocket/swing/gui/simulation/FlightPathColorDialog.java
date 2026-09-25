package info.openrocket.swing.gui.simulation;

import info.openrocket.core.file.flightpath.FlightPathModelBuilder;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

import info.openrocket.swing.gui.components.ColorChooserButton;
import info.openrocket.swing.gui.util.GUIUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/**
 * Picks the flight-path, ground-track and waypoint-pin colors for each stage of a flight.
 *
 * <p>This lives in a dialog of its own rather than in the export tab because a staged flight needs
 * three swatches per stage, and the tab has no room to grow a variable-length list.
 *
 * <p>The three are independent. Each starts on a built-in default and is exported exactly as it is
 * shown, so changing one never moves another.
 */
public class FlightPathColorDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final Translator trans = Application.getTranslator();

	/** The three swatches of one stage, in the order they are laid out. */
	private static final class StageRow {
		private ColorChooserButton path;
		private ColorChooserButton ground;
		private ColorChooserButton pin;
	}

	/** The colors the user settled on, each map holding only the stages moved off their default. */
	public static final class Result {
		private final Map<Integer, Integer> path;
		private final Map<Integer, Integer> ground;
		private final Map<Integer, Integer> pin;

		private Result(Map<Integer, Integer> path, Map<Integer, Integer> ground, Map<Integer, Integer> pin) {
			this.path = Collections.unmodifiableMap(path);
			this.ground = Collections.unmodifiableMap(ground);
			this.pin = Collections.unmodifiableMap(pin);
		}

		/** Flight-path colors as RRGGBB, keyed by stage index. */
		public Map<Integer, Integer> getPathColors() {
			return path;
		}

		/** Ground-track colors as RRGGBB, keyed by stage index. */
		public Map<Integer, Integer> getGroundColors() {
			return ground;
		}

		/** Waypoint-pin colors as RRGGBB, keyed by stage index. */
		public Map<Integer, Integer> getPinColors() {
			return pin;
		}
	}

	private final Map<Integer, StageRow> rows = new LinkedHashMap<>();
	private final List<String> stageNames;
	private Result result;

	/**
	 * @param parent       the window to center on
	 * @param stageNames   one name per stage, in flight-data order
	 * @param pathColors   the flight-path colors in force, keyed by stage index; missing entries
	 *                     mean the built-in default
	 * @param groundColors the ground-track colors in force; missing entries mean the built-in default
	 * @param pinColors    the pin colors in force; missing entries mean the built-in default
	 */
	public FlightPathColorDialog(Window parent, List<String> stageNames,
			Map<Integer, Integer> pathColors, Map<Integer, Integer> groundColors,
			Map<Integer, Integer> pinColors) {
		super(parent, trans.get("SimExpPan.flightPath.colors.title"), ModalityType.APPLICATION_MODAL);
		this.stageNames = stageNames;

		JPanel content = new JPanel(new MigLayout("ins 10, fillx, wrap", "[grow]"));

		JPanel stages = new JPanel(new MigLayout("ins 5, fillx", "[grow][][][]"));
		stages.setBorder(BorderFactory.createTitledBorder(trans.get("SimExpPan.flightPath.colors.border")));

		stages.add(new JLabel(), "growx");
		stages.add(columnHeader("SimExpPan.flightPath.colors.col.path"), "align center");
		stages.add(columnHeader("SimExpPan.flightPath.colors.col.ground"), "align center");
		stages.add(columnHeader("SimExpPan.flightPath.colors.col.pin"), "align center, wrap");

		// A shared chooser keeps the recent-swatches list common to every stage, which is what you
		// want when picking a set of colors that have to work together.
		JColorChooser shared = new JColorChooser();
		for (int i = 0; i < stageNames.size(); i++) {
			StageRow row = new StageRow();
			row.path = swatch(pathColors, i, FlightPathModelBuilder::defaultBranchColor, shared);
			row.ground = swatch(groundColors, i, FlightPathModelBuilder::defaultGroundColor, shared);
			row.pin = swatch(pinColors, i, FlightPathModelBuilder::defaultPinColor, shared);

			rows.put(i, row);
			stages.add(new JLabel(stageNames.get(i)), "growx");
			stages.add(row.path, "w 60!, h 20!");
			stages.add(row.ground, "w 60!, h 20!");
			stages.add(row.pin, "w 60!, h 20!, wrap");
		}
		content.add(stages, "growx");

		JPanel buttonRow = new JPanel(new MigLayout("ins 0, fillx", "[][grow][][]"));
		JButton reset = new JButton(trans.get("SimExpPan.flightPath.colors.reset"));
		reset.setToolTipText(trans.get("SimExpPan.flightPath.colors.reset.ttip"));
		reset.addActionListener(e -> {
			for (Map.Entry<Integer, StageRow> entry : rows.entrySet()) {
				int index = entry.getKey();
				entry.getValue().path.setSelectedColor(
						new Color(FlightPathModelBuilder.defaultBranchColor(index)));
				entry.getValue().ground.setSelectedColor(
						new Color(FlightPathModelBuilder.defaultGroundColor(index)));
				entry.getValue().pin.setSelectedColor(
						new Color(FlightPathModelBuilder.defaultPinColor(index)));
			}
		});
		buttonRow.add(reset);
		buttonRow.add(new JLabel(), "growx");

		JButton cancel = new JButton(trans.get("dlg.but.cancel"));
		cancel.addActionListener(e -> {
			result = null;
			dispose();
		});
		buttonRow.add(cancel, "tag cancel");

		JButton ok = new JButton(trans.get("dlg.but.ok"));
		ok.addActionListener(e -> {
			result = collect();
			dispose();
		});
		buttonRow.add(ok, "tag ok");
		content.add(buttonRow, "growx");

		setContentPane(content);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		GUIUtil.setDisposableDialogOptions(this, ok);
		pack();
		setLocationRelativeTo(parent);
	}

	/**
	 * The colors the user settled on, or {@code null} if they canceled. A swatch left on its default
	 * is not included, so an untouched dialog changes nothing.
	 */
	public Result getResult() {
		return result;
	}

	/** The stage names this dialog was built for, in flight-data order. */
	public List<String> getStageNames() {
		return new ArrayList<>(stageNames);
	}

	/** One swatch, showing the color in force for a stage or that stage's default. */
	private static ColorChooserButton swatch(Map<Integer, Integer> colors, int index,
			IntUnaryOperator defaultColor, JColorChooser shared) {
		Integer chosen = (colors == null) ? null : colors.get(index);
		int rgb = (chosen != null) ? chosen : defaultColor.applyAsInt(index);
		return new ColorChooserButton(new Color(rgb), shared);
	}

	private static JLabel columnHeader(String key) {
		JLabel label = new JLabel(trans.get(key));
		label.setToolTipText(trans.get(key + ".ttip"));
		return label;
	}

	private Result collect() {
		Map<Integer, Integer> path = new LinkedHashMap<>();
		Map<Integer, Integer> ground = new LinkedHashMap<>();
		Map<Integer, Integer> pin = new LinkedHashMap<>();

		for (Map.Entry<Integer, StageRow> entry : rows.entrySet()) {
			int index = entry.getKey();
			StageRow row = entry.getValue();

			put(path, index, row.path, FlightPathModelBuilder.defaultBranchColor(index));
			put(ground, index, row.ground, FlightPathModelBuilder.defaultGroundColor(index));
			put(pin, index, row.pin, FlightPathModelBuilder.defaultPinColor(index));
		}

		return new Result(path, ground, pin);
	}

	private static void put(Map<Integer, Integer> into, int index, ColorChooserButton button,
			int defaultRgb) {
		Color color = button.getSelectedColor();
		if (color == null) {
			return;
		}
		int rgb = color.getRGB() & 0xFFFFFF;
		if (rgb != defaultRgb) {
			into.put(index, rgb);
		}
	}
}
