package net.sf.openrocket.gui.dialogs;

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

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.ColorChooserButton;
import net.sf.openrocket.gui.print.PaperOrientation;
import net.sf.openrocket.gui.print.PaperSize;
import net.sf.openrocket.gui.print.PrintSettings;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.startup.Application;

/**
 * This class is a dialog for displaying advanced settings for printing rocket related info.
 */
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
		


		JComboBox combo = new JComboBox(new EnumModel<PaperSize>(settings, "PaperSize"));
		////Paper size:
		panel.add(new JLabel(trans.get("lbl.Papersize")));
		panel.add(combo, "growx, wrap para");
		

		combo = new JComboBox(new EnumModel<PaperOrientation>(settings, "PaperOrientation"));
		//// Paper orientation:
		panel.add(new JLabel(trans.get("lbl.Paperorientation")));
		panel.add(combo, "growx, wrap para*2");
		



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
