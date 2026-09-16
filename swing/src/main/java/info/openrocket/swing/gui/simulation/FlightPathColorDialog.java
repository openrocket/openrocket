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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Picks the track color for each stage of a flight.
 *
 * <p>This lives in a dialog of its own rather than in the export tab because a staged flight needs
 * one swatch per stage, and the tab has no room to grow a variable-length list. The colors are not
 * remembered between exports: a stage's position means something different in the next rocket, so
 * they are seeded from the built-in palette every time the tab is built.
 *
 * <p>Only the track color is chosen. The ground track is derived from it by darkening, and the
 * waypoint pins are tinted with it, so a stage reads as one thing on the map rather than three
 * unrelated ones.
 */
public class FlightPathColorDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final Translator trans = Application.getTranslator();

	private final Map<Integer, ColorChooserButton> buttons = new LinkedHashMap<>();
	private final List<String> stageNames;
	private Map<Integer, Integer> result;

	/**
	 * @param parent     the window to center on
	 * @param stageNames one name per stage, in flight-data order
	 * @param current    the colors in force, keyed by stage index; missing entries mean the palette
	 */
	public FlightPathColorDialog(Window parent, List<String> stageNames, Map<Integer, Integer> current) {
		super(parent, trans.get("SimExpPan.flightPath.colors.title"), ModalityType.APPLICATION_MODAL);
		this.stageNames = stageNames;

		JPanel content = new JPanel(new MigLayout("ins 10, fillx, wrap", "[grow]"));

		JPanel stages = new JPanel(new MigLayout("ins 5, fillx", "[grow][]"));
		stages.setBorder(BorderFactory.createTitledBorder(trans.get("SimExpPan.flightPath.colors.border")));
		// A shared chooser keeps the recent-swatches list common to every stage, which is what you
		// want when picking a set of colors that have to work together.
		JColorChooser shared = new JColorChooser();
		for (int i = 0; i < stageNames.size(); i++) {
			Integer chosen = current.get(i);
			int rgb = (chosen != null) ? chosen : FlightPathModelBuilder.defaultBranchColor(i);

			ColorChooserButton button = new ColorChooserButton(new Color(rgb), shared);
			buttons.put(i, button);
			stages.add(new JLabel(stageNames.get(i)), "growx");
			stages.add(button, "w 60!, h 20!, wrap");
		}
		content.add(stages, "growx");

		JLabel note = new JLabel(trans.get("SimExpPan.flightPath.colors.note"));
		content.add(note, "gaptop para");

		JPanel buttonRow = new JPanel(new MigLayout("ins 0, fillx", "[][grow][][]"));
		JButton reset = new JButton(trans.get("SimExpPan.flightPath.colors.reset"));
		reset.setToolTipText(trans.get("SimExpPan.flightPath.colors.reset.ttip"));
		reset.addActionListener(e -> {
			for (Map.Entry<Integer, ColorChooserButton> entry : buttons.entrySet()) {
				entry.getValue().setSelectedColor(new Color(
						FlightPathModelBuilder.defaultBranchColor(entry.getKey())));
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
	 * The colors the user settled on, keyed by stage index, or {@code null} if they canceled.
	 * A stage left at its palette color is not included, so an untouched dialog changes nothing.
	 */
	public Map<Integer, Integer> getResult() {
		return result;
	}

	private Map<Integer, Integer> collect() {
		Map<Integer, Integer> chosen = new LinkedHashMap<>();
		for (Map.Entry<Integer, ColorChooserButton> entry : buttons.entrySet()) {
			Color color = entry.getValue().getSelectedColor();
			if (color == null) {
				continue;
			}
			int rgb = color.getRGB() & 0xFFFFFF;
			if (rgb != FlightPathModelBuilder.defaultBranchColor(entry.getKey())) {
				chosen.put(entry.getKey(), rgb);
			}
		}
		return chosen;
	}

	/** The stage names this dialog was built for, in flight-data order. */
	public List<String> getStageNames() {
		return new ArrayList<>(stageNames);
	}
}
