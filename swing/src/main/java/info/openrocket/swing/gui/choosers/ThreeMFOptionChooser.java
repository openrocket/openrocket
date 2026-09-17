package info.openrocket.swing.gui.choosers;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.threemf.export.ThreeMFExportOptions;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.text.NumberFormat;
import java.text.ParsePosition;

/** Options panel for a generic, independently editable 3MF print package. */
public class ThreeMFOptionChooser extends JPanel implements OptionChooser {
    private static final Translator trans = Application.getTranslator();

    private final JCheckBox exportChildren;
    private final JCheckBox autoOrient;
    private final JTextField buildWidth;
    private final JTextField buildDepth;
    private final JTextField buildHeight;
    private final JTextField partSpacing;
    private final JLabel buildWidthError = new JLabel();
    private final JLabel buildDepthError = new JLabel();
    private final JLabel buildHeightError = new JLabel();
    private final JLabel partSpacingError = new JLabel();
    private final Border normalFieldBorder;

    public ThreeMFOptionChooser(ThreeMFExportOptions options) {
        super(new MigLayout("fillx", "[][grow,fill]", ""));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 0, 0),
                BorderFactory.createTitledBorder(trans.get("ThreeMFOptionChooser.title"))));

        exportChildren = new JCheckBox(trans.get("ThreeMFOptionChooser.exportChildren"),
                options.isExportChildren());
        autoOrient = new JCheckBox(trans.get("ThreeMFOptionChooser.autoOrient"), options.isAutoOrient());
        add(exportChildren, "span 2, wrap");
        add(autoOrient, "span 2, wrap 12lp");

        add(new JLabel(trans.get("ThreeMFOptionChooser.buildVolume")), "span 2, wrap 4lp");
        buildWidth = addNumberField("ThreeMFOptionChooser.width", options.getBuildWidth(), buildWidthError);
        buildDepth = addNumberField("ThreeMFOptionChooser.depth", options.getBuildDepth(), buildDepthError);
        buildHeight = addNumberField("ThreeMFOptionChooser.height", options.getBuildHeight(), buildHeightError);
        partSpacing = addNumberField("ThreeMFOptionChooser.spacing", options.getPartSpacing(), partSpacingError);
        normalFieldBorder = buildWidth.getBorder();

        JLabel note = new JLabel("<html>" + trans.get("ThreeMFOptionChooser.note") + "</html>");
        add(note, "span 2, growx, wrap");
    }

    private JTextField addNumberField(String labelKey, double value, JLabel errorLabel) {
        add(new JLabel(trans.get(labelKey)));
        JTextField field = new JTextField(NumberFormat.getNumberInstance().format(value), 8);
        add(field, "split 2");
        add(new JLabel("mm"), "wrap");
        errorLabel.setForeground(new Color(190, 40, 40));
        add(errorLabel, "skip 1, span 1, growx, wrap");
        return field;
    }

    public boolean validateOptions() {
        boolean valid = true;
        valid &= validatePositive(buildWidth, buildWidthError);
        valid &= validatePositive(buildDepth, buildDepthError);
        valid &= validatePositive(buildHeight, buildHeightError);
        valid &= validateNonNegative(partSpacing, partSpacingError);
        return valid;
    }

    public ThreeMFExportOptions getOptions() {
        if (!validateOptions()) {
            throw new IllegalStateException("Invalid 3MF export options");
        }
        ThreeMFExportOptions options = new ThreeMFExportOptions();
        options.setExportChildren(exportChildren.isSelected());
        options.setAutoOrient(autoOrient.isSelected());
        options.setBuildWidth(parse(buildWidth));
        options.setBuildDepth(parse(buildDepth));
        options.setBuildHeight(parse(buildHeight));
        options.setPartSpacing(parse(partSpacing));
        return options;
    }

    private boolean validatePositive(JTextField field, JLabel errorLabel) {
        Double value = tryParse(field);
        return setValidation(field, errorLabel, value != null && value > 0,
                trans.get("ThreeMFOptionChooser.error.positive"));
    }

    private boolean validateNonNegative(JTextField field, JLabel errorLabel) {
        Double value = tryParse(field);
        return setValidation(field, errorLabel, value != null && value >= 0,
                trans.get("ThreeMFOptionChooser.error.nonNegative"));
    }

    private boolean setValidation(JTextField field, JLabel errorLabel, boolean valid, String error) {
        field.setBorder(valid ? normalFieldBorder : BorderFactory.createLineBorder(new Color(190, 40, 40)));
        field.setToolTipText(valid ? null : error);
        errorLabel.setText(valid ? "" : error);
        return valid;
    }

    private double parse(JTextField field) {
        Double value = tryParse(field);
        if (value == null) {
            throw new IllegalArgumentException("Invalid number");
        }
        return value;
    }

    private Double tryParse(JTextField field) {
        String text = field.getText().trim();
        ParsePosition position = new ParsePosition(0);
        Number number = NumberFormat.getNumberInstance().parse(text, position);
        if (number == null || position.getIndex() != text.length()) {
            return null;
        }
        double value = number.doubleValue();
        return Double.isFinite(value) ? value : null;
    }

    @Override
    public void storeOptions(OpenRocketDocument document, ApplicationPreferences preferences) {
        preferences.saveThreeMFExportOptions(getOptions());
    }

    JTextField getBuildWidthField() {
        return buildWidth;
    }

    JTextField getPartSpacingField() {
        return partSpacing;
    }
}
