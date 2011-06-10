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

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.ColorChooserButton;
import net.sf.openrocket.gui.print.PaperOrientation;
import net.sf.openrocket.gui.print.PaperSize;
import net.sf.openrocket.gui.print.PrintSettings;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.GUIUtil;

/**
 * This class is a dialog for displaying advanced settings for printing rocket related info.
 */
public class PrintSettingsDialog extends JDialog {
	private static final LogHelper log = Application.getLogger();
	
	
	/**
	 * Construct a dialog for setting the advanced rocket print settings.
	 *
	 * @param parent the owning dialog
	 */
	public PrintSettingsDialog(Window parent, final PrintSettings settings) {
		super(parent, "Print settings", ModalityType.APPLICATION_MODAL);
		

		JPanel panel = new JPanel(new MigLayout("fill"));
		

		panel.add(new JLabel("Template fill color:"));
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
		

		panel.add(new JLabel("Template border color:"));
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
		panel.add(new JLabel("Paper size:"));
		panel.add(combo, "growx, wrap para");
		

		combo = new JComboBox(new EnumModel<PaperOrientation>(settings, "PaperOrientation"));
		panel.add(new JLabel("Paper orientation:"));
		panel.add(combo, "growx, wrap para*2");
		




		JButton button = new JButton("Reset");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Resetting print setting values to defaults");
				PrintSettings defaults = new PrintSettings();
				settings.loadFrom(defaults);
				fillColorButton.setSelectedColor(settings.getTemplateFillColor());
				borderColorButton.setSelectedColor(settings.getTemplateBorderColor());
			}
		});
		panel.add(button, "spanx, split, right");
		

		JButton closeButton = new JButton("Close");
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
