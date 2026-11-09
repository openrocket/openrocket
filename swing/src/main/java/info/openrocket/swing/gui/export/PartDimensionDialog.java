package info.openrocket.swing.gui.export;

import info.openrocket.core.file.cad.export.CADDimensionVerifier;
import info.openrocket.core.file.cad.export.CADDimensionVerifier.Check;
import info.openrocket.core.file.cad.export.FlatPart.Dimension;
import info.openrocket.core.file.cad.export.FlatPart.DimensionValue;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.StringUtils;
import info.openrocket.swing.gui.util.GUIUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Shows the dimensions of the parts in an exported CAD file, as measured from the file, next to the design values.
 */
public class PartDimensionDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();
	/** Preferred widths of the part, dimension, design, in file and check columns, in pixels */
	private static final int[] COLUMN_WIDTHS = {170, 240, 100, 100, 90};

	/**
	 * @param owner  the window that the dialog is shown over
	 * @param file   the file that was exported
	 * @param result the comparison of the file with the design
	 */
	public PartDimensionDialog(Window owner, File file, CADDimensionVerifier.Result result) {
		super(owner, trans.get("PartExport.dimensions.title"), ModalityType.APPLICATION_MODAL);

		Set<String> warnings = warnings(result);
		String summary = summary(file, result, warnings);
		String hint = trans.get("PartExport.dimensions.hint." + result.format().name());

		DefaultTableModel model = new DefaultTableModel(new Object[]{
				trans.get("PartExport.col.part"),
				trans.get("PartExport.col.dimension"),
				trans.get("PartExport.col.design"),
				trans.get("PartExport.col.file"),
				trans.get("PartExport.col.check")}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		for (Check check : result.checks()) {
			DimensionValue dim = check.dimension();
			model.addRow(new Object[]{
					check.part().name(),
					String.format(trans.get("PartExport.dim." + dim.dimension().name()), dim.index()),
					format(dim.dimension(), dim.value()),
					Double.isNaN(check.measured()) ? "" : format(dim.dimension(), check.measured()),
					trans.get("PartExport.status." + check.status().name())});
		}
		JTable table = new JTable(model);
		table.setFillsViewportHeight(true);
		// The columns are wide enough for the longest texts in the common fonts of all platforms
		for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
		}
		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(new java.awt.Dimension(760, 300));

		JPanel top = new JPanel(new BorderLayout(0, 5));
		top.add(wrappedLabel(summary), BorderLayout.NORTH);
		if (!warnings.isEmpty()) {
			top.add(wrappedLabel(warningText(warnings)), BorderLayout.CENTER);
		}
		top.add(wrappedLabel(hint), BorderLayout.SOUTH);

		JButton close = new JButton(trans.get("dlg.but.close"));
		close.addActionListener(e -> dispose());
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(close);

		JPanel panel = new JPanel(new BorderLayout(0, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(top, BorderLayout.NORTH);
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.SOUTH);
		setContentPane(panel);
		GUIUtil.setDisposableDialogOptions(this, close);
	}

	/**
	 * @return the warnings of all parts in the file, without duplicates
	 */
	static Set<String> warnings(CADDimensionVerifier.Result result) {
		Set<String> warnings = new LinkedHashSet<>();
		for (Check check : result.checks()) {
			warnings.addAll(check.part().part().getWarnings());
		}
		return warnings;
	}

	/**
	 * @return the headline of the dialog as HTML; it does not say that everything matches if a part has a warning
	 */
	static String summary(File file, CADDimensionVerifier.Result result, Set<String> warnings) {
		String name = StringUtils.escapeHtml(file.getName());
		if (result.units() == CADDimensionVerifier.Units.OTHER) {
			return String.format(trans.get("PartExport.dimensions.notMillimeters"), name);
		} else if (!result.isAllMatching()) {
			return String.format(trans.get("PartExport.dimensions.mismatch"), name);
		} else if (!warnings.isEmpty()) {
			return String.format(trans.get("PartExport.dimensions.warning"), name);
		}
		return String.format(trans.get("PartExport.dimensions.allMatch"), name);
	}

	/**
	 * @return the warnings as HTML, one per line
	 */
	static String warningText(Set<String> warnings) {
		List<String> lines = new ArrayList<>();
		for (String warning : warnings) {
			lines.add(StringUtils.escapeHtml(warning));
		}
		return String.join("<br>", lines);
	}

	private static String format(Dimension dimension, double value) {
		if (dimension == Dimension.VOLUME) {
			// There is no volume unit group, so volumes are shown in cm³
			return String.format(Locale.getDefault(), "%.3f cm³", value * 1e6);
		}
		return UnitGroup.UNITS_LENGTH.toStringUnit(value);
	}

	private static JLabel wrappedLabel(String text) {
		return new JLabel("<html><body style='width: 540px'>" + text + "</body></html>");
	}
}
