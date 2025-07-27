package info.openrocket.swing.gui.dialogs;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.startup.Application;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.components.ColorChooserButton;
import info.openrocket.swing.gui.print.PaperOrientation;
import info.openrocket.swing.gui.print.PaperSize;
import info.openrocket.swing.gui.print.PrintSettings;
import info.openrocket.swing.gui.util.GUIUtil;

/**
 * This class is a dialog for displaying advanced settings for printing rocket related info.
 */
@SuppressWarnings("serial")
public class PrintSettingsDialog extends JDialog {
	private static final Logger log = LoggerFactory.getLogger(PrintSettingsDialog.class);
	private static final Translator trans = Application.getTranslator();

	
	/**
	 * Construct a dialog for setting the advanced rocket print settings.
	 *
	 * @param parent the owning dialog
	 */
	public PrintSettingsDialog(Window parent, final PrintSettings settings) {
		////Print settings
		super(parent, trans.get("title"), ModalityType.APPLICATION_MODAL);
		

		JPanel panel = new JPanel(new MigLayout("fill"));
		
		////Template fill color:
		panel.add(new JLabel(trans.get("lbl.Templatefillcolor")));
		final ColorChooserButton fillColorButton = new ColorChooserButton(settings.getTemplateFillColor());
		fillColorButton.addColorPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Color c = (Color) evt.getNewValue();
				log.info("Template fill color changed to " + c);
				settings.setTemplateFillColor(c);
			}
		});
		panel.add(fillColorButton, "wrap para");
		
		//// Template border color:
		panel.add(new JLabel(trans.get("lbl.Templatebordercolor")));
		final ColorChooserButton borderColorButton = new ColorChooserButton(settings.getTemplateBorderColor());
		borderColorButton.addColorPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Color c = (Color) evt.getNewValue();
				log.info("Template border color changed to " + c);
				settings.setTemplateBorderColor(c);
			}
		});
		panel.add(borderColorButton, "wrap para*2");
		


		final JComboBox<PaperSize> sizeCombo = new JComboBox<>(new EnumModel<PaperSize>(settings, "PaperSize"));
		////Paper size:
		panel.add(new JLabel(trans.get("lbl.Papersize")));
		panel.add( sizeCombo, "growx, wrap para");
		

		final JComboBox<PaperOrientation> orientCombo = new JComboBox<>(new EnumModel<PaperOrientation>(settings, "PaperOrientation"));
		//// Paper orientation:
		panel.add(new JLabel(trans.get("lbl.Paperorientation")));
		panel.add( orientCombo, "growx, wrap para*2");
		



		//// Reset
		JButton button = new JButton(trans.get("but.Reset"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Resetting print setting values to defaults");
				PrintSettings defaults = new PrintSettings();
				settings.loadFrom(defaults);
				fillColorButton.setSelectedColor(settings.getTemplateFillColor());
				borderColorButton.setSelectedColor(settings.getTemplateBorderColor());
			}
		});
		panel.add(button, "spanx, split, right");
		
		//// Close
		JButton closeButton = new JButton(trans.get("but.Close"));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrintSettingsDialog.this.setVisible(false);
			}
		});
		panel.add(closeButton, "right");
		
		this.add(panel);
		GUIUtil.setDisposableDialogOptions(this, closeButton);
	}
	

}
